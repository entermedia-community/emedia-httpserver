package org.entermediadb.httpserver.http;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.entermediadb.httpserver.main.CookieManager;
import org.entermediadb.httpserver.util.ChunkedOutputStream;
import org.entermediadb.httpserver.util.DelegatingServletOutputStream;
import org.entermediadb.httpserver.util.OutputFiller;
import org.entermediadb.httpserver.util.SimpleDateFormatPerThread;

public class HttpResponse implements HttpServletResponse {
    private final ChunkedOutputStream chunkedoutputStream;
    private int status = SC_OK;
    private String statusMessage = "OK";
    private final Map<String, String> headers = new HashMap<>();
    private byte[] body;
    private String characterEncoding = StandardCharsets.UTF_8.name();
    private String contentType;
    private Locale locale = Locale.getDefault();
    private ServletOutputStream servletOutputStream;
    private PrintWriter writer;
    private boolean committed = false;
    private int bufferSize = 8192;
    private boolean isChunked = false;
    private CookieManager fieldCookieManager;
    private OutputFiller fieldOutputFiller;
    protected static final SimpleDateFormatPerThread fieldLastModFormat = new SimpleDateFormatPerThread("EEE, dd MMM yyyy HH:mm:ss z");
	
    public OutputFiller getOutputFiller()
	{
		return fieldOutputFiller;
	}
    
    public boolean isChunked()
    {
    	return isChunked;
    }

	public HttpResponse(OutputStream out) {
        this.chunkedoutputStream = new org.entermediadb.httpserver.util.ChunkedOutputStream(this,out);
        headers.put("Server", "EntermediaDB-HTTP/1.1");
        headers.put("Date", fieldLastModFormat.format( new Date()));
       // headers.put("Cache-Control","max-age=28800, must-revalidate");
    }

    public CookieManager getCookieManager()
	{
		return fieldCookieManager;
	}
    
    public void setCookieManager(CookieManager manager) {
        this.fieldCookieManager = manager;
    }

    public void setOutputFiller(OutputFiller filler) {
        this.fieldOutputFiller = filler;
    }
    
    private boolean headersSent = false;

    public void writeHeaders() throws IOException {
        if (!headersSent) {
//            if (!headers.containsKey("Content-Length") && !isChunked) {
//                // If neither content-length nor chunked encoding is set,
//                // default to chunked encoding
//                headers.put("Transfer-Encoding", "chunked");
//                isChunked = true;
//            }
            writeResponseHeaders();
            headersSent = true;
            committed = true;
        }
    }


    @Override
    public ServletOutputStream getOutputStream() throws IOException {
        if (servletOutputStream == null) {
            servletOutputStream = new DelegatingServletOutputStream(chunkedoutputStream);
        }
        return servletOutputStream;
    }

	public void writeResponseHeaders() throws IOException {
        StringBuilder responseHeader = new StringBuilder();
        responseHeader.append("HTTP/1.1 ").append(status).append(" ").append(statusMessage).append("\r\n");
        
        // Add cookies
            for (javax.servlet.http.Cookie cookie : getCookieManager().getAllCookies() ) {
                responseHeader.append("Set-Cookie: ").append(encodeCookie(cookie)).append("\r\n");
            }

        // Add other headers
        for (Map.Entry<String, String> header : headers.entrySet()) {
            responseHeader.append(header.getKey()).append(": ").append(header.getValue()).append("\r\n");
        }

        responseHeader.append("\r\n");
        chunkedoutputStream.toClientStream().write(responseHeader.toString().getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public void flushBuffer() throws IOException {
        if (writer != null) {
            writer.flush();
        }
        if (servletOutputStream != null) {
            servletOutputStream.flush();
        }
        chunkedoutputStream.flush();
    }

    @Override
    public void setContentLength(int len) {
        if (isChunked) {
            throw new IllegalStateException("Cannot set content length when using chunked encoding");
        }
        setHeader("Content-Length", String.valueOf(len));
    }

    @Override
    public void setContentLengthLong(long len) {
        if (isChunked) {
            throw new IllegalStateException("Cannot set content length when using chunked encoding");
        }
        setHeader("Content-Length", String.valueOf(len));
    }

    public void setChunkedEncoding() {
        if (headers.containsKey("Content-Length")) {
            throw new IllegalStateException("Cannot use chunked encoding when content length is set");
        }
        isChunked = true;
        setHeader("Transfer-Encoding", "chunked");
    }

    @Override
    public void addCookie(javax.servlet.http.Cookie cookie) {
        getCookieManager().addCookie(cookie);
    }

    @Override
    public boolean containsHeader(String name) {
        return headers.containsKey(name);
    }

    @Override
    public String encodeURL(String url) {
        return url; // No session encoding implemented
    }

    @Override
    public String encodeRedirectURL(String url) {
        return url; // No session encoding implemented
    }

    @Override
    public String encodeUrl(String url) {
        return encodeURL(url);
    }

    @Override
    public String encodeRedirectUrl(String url) {
        return encodeRedirectURL(url);
    }

    @Override
    public void sendError(int sc, String msg) throws IOException {
        if (isCommitted()) {
            throw new IllegalStateException("Response already committed");
        }
        setStatus(sc, msg);
        setContentType("text/html");
        String content = "<html><body><h1>Error " + sc + "</h1><p>" + msg + "</p></body></html>";
        stream(content);
    }

    @Override
    public void sendError(int sc) throws IOException {
        sendError(sc, getStatusMessage(sc));
    }

    @Override
    public void sendRedirect(String location) throws IOException {
        if (isCommitted()) {
            throw new IllegalStateException("Response already committed");
        }
        resetBuffer();
        setStatus(SC_FOUND);
        setHeader("Location", location);
        flushBuffer();
    }

    @Override
    public void setDateHeader(String name, long date) {
        headers.put(name, fieldLastModFormat.format( new Date(date)));
    }

    @Override
    public void addDateHeader(String name, long date) {
        String existing = headers.get(name);
        if (existing != null) {
            headers.put(name, existing + ", " + fieldLastModFormat.format(new Date(date)));
        } else {
            setDateHeader(name, date);
        }
    }

    @Override
    public void setHeader(String name, String value) {
        headers.put(name, value);
    }

    @Override
    public void addHeader(String name, String value) {
        String existing = headers.get(name);
        if (existing != null) {
            headers.put(name, existing + ", " + value);
        } else {
            setHeader(name, value);
        }
    }

    @Override
    public void setIntHeader(String name, int value) {
        setHeader(name, String.valueOf(value));
    }

    @Override
    public void addIntHeader(String name, int value) {
        addHeader(name, String.valueOf(value));
    }

    @Override
    public void setStatus(int sc) {
        this.status = sc;
        this.statusMessage = getStatusMessage(sc);
    }

    @Override
    public void setStatus(int sc, String sm) {
        this.status = sc;
        this.statusMessage = sm;
    }

    @Override
    public int getStatus() {
        return status;
    }

    @Override
    public String getHeader(String name) {
        return headers.get(name);
    }

    @Override
    public Collection<String> getHeaders(String name) {
        String header = headers.get(name);
        if (header == null) {
            return Collections.emptyList();
        }
        return Arrays.asList(header.split(",\\s*"));
    }

    @Override
    public Collection<String> getHeaderNames() {
        return headers.keySet();
    }

    @Override
    public String getCharacterEncoding() {
        return characterEncoding;
    }

    @Override
    public void setCharacterEncoding(String charset) {
        this.characterEncoding = charset;
    }

    @Override
    public String getContentType() {
        return contentType;
    }

    @Override
    public void setContentType(String type) {
        this.contentType = type;
        setHeader("Content-Type", type);
        
        // Parse charset from content type if present
        if (type != null && type.toLowerCase().contains("charset=")) {
            String charset = type.substring(type.toLowerCase().indexOf("charset=") + 8).trim();
            setCharacterEncoding(charset);
        }
    }

    @Override
    public PrintWriter getWriter() throws IOException {
        if (writer == null) {
            writer = new PrintWriter(new OutputStreamWriter(chunkedoutputStream, getCharacterEncoding()));
        }
        return writer;
    }

    @Override
    public void resetBuffer() {
        if (isCommitted()) {
            throw new IllegalStateException("Response already committed");
        }
        // Reset the body
        // Reset streams if they exist
        this.writer = null;
        this.servletOutputStream = null;
    }

    @Override
    public boolean isCommitted() {
        return committed;
    }

    @Override
    public void reset() {
        if (isCommitted()) {
            throw new IllegalStateException("Response already committed");
        }
        resetBuffer();
        status = SC_OK;
        statusMessage = "OK";
        characterEncoding = StandardCharsets.UTF_8.name();
        contentType = null;
        locale = Locale.getDefault();
    }

    @Override
    public void setLocale(Locale loc) {
        this.locale = loc;
        String language = locale.toLanguageTag();
        setHeader("Content-Language", language);
    }

    @Override
    public Locale getLocale() {
        return locale;
    }

    // Helper methods
    private String getStatusMessage(int status) {
        switch (status) {
            case SC_OK: return "OK";
            case SC_CREATED: return "Created";
            case SC_ACCEPTED: return "Accepted";
            case SC_NO_CONTENT: return "No Content";
            case SC_MOVED_PERMANENTLY: return "Moved Permanently";
            case SC_FOUND: return "Found";
            case SC_NOT_MODIFIED: return "Not Modified";
            case SC_BAD_REQUEST: return "Bad Request";
            case SC_UNAUTHORIZED: return "Unauthorized";
            case SC_FORBIDDEN: return "Forbidden";
            case SC_NOT_FOUND: return "Not Found";
            case SC_INTERNAL_SERVER_ERROR: return "Internal Server Error";
            case SC_NOT_IMPLEMENTED: return "Not Implemented";
            case SC_BAD_GATEWAY: return "Bad Gateway";
            case SC_SERVICE_UNAVAILABLE: return "Service Unavailable";
            default: return "Unknown Status";
        }
    }

    private String encodeCookie(javax.servlet.http.Cookie cookie) {
        StringBuilder sb = new StringBuilder();
        sb.append(cookie.getName()).append("=").append(cookie.getValue());
        
        if (cookie.getPath() != null) {
            sb.append("; Path=").append(cookie.getPath());
        }
        if (cookie.getDomain() != null) {
            sb.append("; Domain=").append(cookie.getDomain());
        }
        if (cookie.getMaxAge() >= 0) {
            sb.append("; Max-Age=").append(cookie.getMaxAge());
        }
        if (cookie.getSecure()) {
            sb.append("; Secure");
        }
        if (cookie.isHttpOnly()) {
            sb.append("; HttpOnly");
        }
        
        return sb.toString();
    }

    public void stream(String inContent) {
    	try
    	{
	        if (!isCommitted()) {
	        	byte[] b = inContent.getBytes(getCharacterEncoding());
	            setContentLength(b.length);
	            writeHeaders();
	            getOutputStream().write(b);
	            flushBuffer();
	        }
	        //endTransmission();
	    }
    	catch( Exception ex)
    	{
    		throw new RuntimeException("Error Sending ",ex);
    	}
    }

    // Method to send response (keeping for compatibility)
    public void stream(Reader inContent) throws IOException {
        if (!isCommitted()) {
            writeHeaders();
            getOutputFiller().fill(inContent, getWriter());
        }
        flushBuffer();
    }

	@Override
	public int getBufferSize()
	{
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setBufferSize(int inArg0)
	{
		// TODO Auto-generated method stub
		
	}

	public void endChunk()
	{
		// TODO Auto-generated method stub
		
	}

	public void endTransmission() throws IOException
	{
		chunkedoutputStream.end();
	}
} 