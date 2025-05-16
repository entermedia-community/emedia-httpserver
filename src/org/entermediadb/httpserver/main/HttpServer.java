package org.entermediadb.httpserver.main;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.servlet.Filter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.entermediadb.httpserver.util.OutputFiller;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class HttpServer
{
	private static final Log log = LogFactory.getLog(HttpServer.class);

	private static final int PORT = 8080;
	private static final int THREAD_POOL_SIZE = 10;
	private static final int CONNECTION_TIMEOUT = 30000; // 30 seconds
	private static final int SHUTDOWN_TIMEOUT = 5; // 5 seconds to wait for graceful shutdown
	private boolean running;
	private ServerSocket serverSocket;
	private ExecutorService executorService;
	protected JSONObject fieldConfig; 
	protected ClassLoader fieldCustomClassLoader;
	
	protected SessionManager fieldSessionManager;
	
	public SessionManager getSessionManager()
	{
		if (fieldSessionManager == null)
		{
			fieldSessionManager = new SessionManager();
		}

		return fieldSessionManager;
	}

	public void setSessionManager(SessionManager inSessionManager)
	{
		fieldSessionManager = inSessionManager;
	}
	protected OutputFiller fieldOutputFiller;
	protected OutputFiller getOutputFiller()
	{
		if (fieldOutputFiller == null)
		{
			fieldOutputFiller = new OutputFiller();
		}
		return fieldOutputFiller;
	}
	protected Filter fieldDefaultFilter;
	
	//https://jenkov.com/tutorials/java-reflection/dynamic-class-loading-reloading.html#example
	public ClassLoader getCustomClassLoader()
	{
		if (fieldCustomClassLoader == null)
		{
			fieldCustomClassLoader = getClass().getClassLoader(); //For now
			
		}

		return fieldCustomClassLoader;
	}

	public void setCustomClassLoader(ClassLoader inCustomClassLoader)
	{
		fieldCustomClassLoader = inCustomClassLoader;
	}

	public JSONObject getConfig()
	{
		return fieldConfig;
	}

	public void setConfig(JSONObject inConfig)
	{
		fieldConfig = inConfig;
	}

	
	
	public static void main(String[] args) throws Exception
	{
		long start = System.currentTimeMillis();
		log.info("eMedia HTTP/1.1 starting on port " + PORT);
		 String rootPath = System.getProperty("user.dir");
	     Path filePath = Paths.get(rootPath, "emedia.json");
	     if( !Files.exists( filePath ) )
	     {
	    	 log.error( "No config found" + filePath);
	    	 return;
	     }
	     JSONObject setupinfo =  (JSONObject)new JSONParser().parse(Files.readString(filePath));
	     
		HttpServer server = new HttpServer();
		server.init(setupinfo);
		// Add shutdown hook for graceful shutdown on Ctrl+C
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			log.info("Shutting down server...");
			server.shutdown();
		}));

		server.getDefaultFilter();
		long end = System.currentTimeMillis();
		double seconds = (end-start)/1000D;
		log.info("Press Ctrl+C to shutdown");
		log.info("Server started in " + seconds + " seconds");
		server.listen();
	}

	protected void init(JSONObject inSetupinfo)
	{
		setConfig(inSetupinfo);
		String root = (String)inSetupinfo.get("websitepath");
		System.setProperty("oe.root.path",root);
	}

	protected void listen()
	{
		running = true;
		executorService = Executors.newFixedThreadPool(THREAD_POOL_SIZE);

		try {
			serverSocket = new ServerSocket(PORT);

			while (running) {
				Socket clientSocket = serverSocket.accept();
				clientSocket.setSoTimeout(CONNECTION_TIMEOUT);
				ClientHandler handler = new ClientHandler(); //TODO: Lookup
				handler.setConfig(getConfig());
				handler.setFilter(getDefaultFilter());
				handler.setOutputFiller(getOutputFiller());
				handler.setSessionManager(getSessionManager());
				handler.setClientSocket(clientSocket);
				executorService.execute(handler);
				log.debug("New client connection accepted from " + clientSocket.getInetAddress().getHostAddress());
			}
		} catch (IOException e) {
			if (running) {
				// Only log error if we weren't shutting down
				log.error("Server error: " + e.getMessage(), e);
			}
		} finally {
			shutdown();
		}
	}
	private Filter getDefaultFilter()
	{
		if( fieldDefaultFilter == null)
		{
			String beanname = (String)getConfig().get("defaultfilter");
			try
			{
				fieldDefaultFilter =  (Filter)getCustomClassLoader().loadClass(beanname).newInstance();
				fieldDefaultFilter.init(null);
			}
			catch (Exception ex)
			{
				// TODO Auto-generated catch block
				ex.printStackTrace();
				throw new RuntimeException("Could not init filter",ex);
			}
//			if( created instanceof BeanLoaderAware)
//			{
//				setProperty(created,"beanLoader",this);
//			}
		}
		return fieldDefaultFilter;	
	}

	protected void shutdown()
	{
		running = false;
		
		// Close the server socket first to stop accepting new connections
		if (serverSocket != null && !serverSocket.isClosed()) {
			try {
				serverSocket.close();
				log.debug("Server socket closed");
			} catch (IOException e) {
				log.error("Error closing server socket: " + e.getMessage(), e);
			}
		}

		// Shutdown the executor service gracefully
		if (executorService != null && !executorService.isShutdown()) {
			try {
				// Disable new tasks from being submitted
				executorService.shutdown();
				log.debug("Executor service shutdown initiated");
				
				// Wait for existing tasks to terminate
				if (!executorService.awaitTermination(SHUTDOWN_TIMEOUT, TimeUnit.SECONDS)) {
					// Force shutdown if graceful shutdown fails
					log.warn("Forcing shutdown after timeout...");
					executorService.shutdownNow();
					
					// Wait a bit more for tasks to respond to being cancelled
					if (!executorService.awaitTermination(1, TimeUnit.SECONDS)) {
						log.error("Executor service did not terminate");
					}
				}
			} catch (InterruptedException e) {
				// (Re-)Cancel if current thread also interrupted
				executorService.shutdownNow();
				// Preserve interrupt status
				Thread.currentThread().interrupt();
				log.warn("Shutdown interrupted", e);
			}
		}
		
		log.info("Server shutdown complete");
	}
}
