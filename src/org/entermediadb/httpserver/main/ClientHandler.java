package org.entermediadb.httpserver.main;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.servlet.Filter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.entermediadb.httpserver.http.HttpRequest;
import org.entermediadb.httpserver.http.HttpResponse;
import org.entermediadb.httpserver.http.HttpSession;
import org.entermediadb.httpserver.util.OutputFiller;
import org.json.simple.JSONObject;


public class ClientHandler implements Runnable
{
    private static final Log log = LogFactory.getLog(ClientHandler.class);
    private Socket clientSocket;
    private WebSocketManager webSocketManager;
    private SessionManager fieldSessionManager;
    public SessionManager getSessionManager()
	{
		return fieldSessionManager;
	}
	public void setSessionManager(SessionManager inSessionManager)
	{
		fieldSessionManager = inSessionManager;
	}
	protected JSONObject fieldConfig; 
	protected OutputFiller fieldOutputFiller;
	protected OutputFiller getOutputFiller()
	{
		if (fieldOutputFiller == null)
		{
			fieldOutputFiller = new OutputFiller();
		}
		return fieldOutputFiller;
	}
	public void setOutputFiller(OutputFiller inOutputFiller)
	{
		fieldOutputFiller = inOutputFiller;
	}
	public JSONObject getConfig()
	{
		return fieldConfig;
	}

	public void setConfig(JSONObject inConfig)
	{
		fieldConfig = inConfig;
	}

    public void setClientSocket(Socket socket)
    {
        this.clientSocket = socket;
    }
    protected Filter fieldFilter;
    
    public Filter getFilter()
	{
		return fieldFilter;
	}

	public void setFilter(Filter inFilter)
	{
		fieldFilter = inFilter;
	}

	@Override
    public void run()
    {
        try {
            while (!clientSocket.isClosed()) {
                if (webSocketManager == null) {

                    BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                    HttpRequest request = parseRequest(in);
                    if (request == null) break;

                    if (WebSocketManager.isWebSocketUpgradeRequest(request)) {
                        webSocketManager = new WebSocketManager(clientSocket);
                        webSocketManager.handleWebSocketUpgrade(request);
                        log.debug("WebSocket connection upgraded for client: " + clientSocket.getInetAddress().getHostAddress());
                    } else {
                        OutputStream rawoutputstream = clientSocket.getOutputStream();
                        handleHttpRequest(request,rawoutputstream);
                    	//clientSocket.close();

                        log.debug("Handled HTTP request: " + request.getMethod() + " " + request.getRequestURI() + " from " + clientSocket.getInetAddress().getHostAddress());
                        if ("close".equalsIgnoreCase(request.getHeader("Connection"))) {
                            break;
                        }
                    }
                } else {
                    webSocketManager.handleWebSocketFrame();
                }
            }
        } catch (Throwable e) {
            log.error("Error handling client request: " + e.getMessage(), e);
        }
    }

    private HttpRequest parseRequest(BufferedReader in) throws IOException
    {
    	//java.net.SocketTimeoutException  Allways have headers to read right?
        String requestLine = null; 
        
        try
        {
        	requestLine  = in.readLine();
        }
        catch( SocketTimeoutException ex)
        {
        	log.error("Timeout issue?" + ex);
        }
        if (requestLine == null) return null;

        String[] requestParts = requestLine.split(" ");
        if (requestParts.length != 3) {
            log.warn("Invalid request line received: " + requestLine);
            return null;
        }

        HttpRequest request = new HttpRequest();
        
        // Set basic request information
        request.setMethod(requestParts[0]);
        // Parse URI for path and query string
        try {
            URI uri = new URI(requestParts[1]);
            request.setPath(uri.getPath());
            request.setQueryString(uri.getQuery());
        } catch (URISyntaxException e) {
            log.warn("Invalid URI in request, using raw path: " + requestParts[1], e);
            request.setPath(requestParts[1]);
        }
        request.setHttpVersion(requestParts[2]);
        request.setProtocol(requestParts[2]);

        // Set connection information
        request.setRemoteAddr(clientSocket.getInetAddress().getHostAddress());
        request.setRemotePort(clientSocket.getPort());
        request.setLocalPort(clientSocket.getLocalPort());
        request.setServerPort(clientSocket.getLocalPort());

        // Parse headers
        String headerLine;
        while ((headerLine = in.readLine()) != null && !headerLine.isEmpty()) {
            String[] headerParts = headerLine.split(": ", 2);
            if (headerParts.length == 2) {
                request.addHeader(headerParts[0], headerParts[1]);
            } else {
                log.warn("Invalid header line received: " + headerLine);
            }
        }

        String host = request.getHeader("Host");
        if( host != null )
        {
        	host = host.split(":")[0];
            request.setServerName(host);
        }
        CookieManager cookieManager = new CookieManager();
        cookieManager.parseCookieHeader(request.getHeaders("Cookie"));
        cookieManager.parseCookieHeader(request.getHeaders("Set-Cookie"));
        // Parse cookies from Cookie header
        request.setCookieManager(cookieManager);

        // Parse body if Content-Length is present
        if (request.getHeader("Content-Length") != null) {
            int contentLength = request.getContentLength();
            char[] body = new char[contentLength];
            int bytesRead = in.read(body, 0, contentLength);
            if (bytesRead < contentLength) {
                log.warn("Incomplete request body received. Expected " + contentLength + " bytes but got " + bytesRead);
            }
            request.setBody(new String(body, 0, bytesRead));
        }

        // Parse parameters from query string and/or body
        request.parseParameters();

        HttpSession session = getSessionManager().loadSession(request); 
        request.setSession(session);

        log.debug("Parsed request: " + request.getMethod() + " " + request.getRequestURI());
        return request;
    }

	private void handleHttpRequest(HttpRequest request, OutputStream rawoutputstream) throws IOException
    {
        HttpResponse response = new HttpResponse(rawoutputstream);
        response.setOutputFiller(getOutputFiller());
        response.setCookieManager(request.getCookieManager());
        try
        {
        	render(request,response);
        } catch (Exception e) {
            log.error("Error processing request: " + request.getMethod() + " " + request.getRequestURI(), e);
            response.sendError(HttpResponse.SC_INTERNAL_SERVER_ERROR, "Internal Server Error");
        }
    }

    protected void render(HttpRequest inRequest, HttpResponse inResponse) throws Exception
	{
    	//Call the standard eMedia Engine via an Interface
    	//log.debug("Response sent for: " + request.getMethod() + " " + request.getRequestURI());
    	Filter filter = getFilter();
    	filter.doFilter(inRequest, inResponse,null);
    	//inResponse.endTransmission();
	}

//	
//
//    private void handleSetCookie(HttpRequest request, HttpResponse response) {
//        String cookieName = request.getParameter("cookieName");
//        String cookieValue = request.getParameter("cookieValue");
//
//        if (cookieName != null && cookieValue != null) {
//            response.addCookie(CookieManager.createSecureCookie(cookieName, cookieValue));
//            log.debug("Set cookie: " + cookieName + "=" + cookieValue);
//        } else {
//            log.warn("Invalid cookie parameters received");
//        }
//
//        // Redirect back to root
//        response.setStatus(HttpResponse.SC_FOUND);
//        response.setHeader("Location", "/");
//    }


} 