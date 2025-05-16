package org.entermediadb.httpserver.http;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.AsyncContext;
import javax.servlet.DispatcherType;
import javax.servlet.ReadListener;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpUpgradeHandler;
import javax.servlet.http.Part;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.entermediadb.httpserver.main.ClientHandler;
import org.entermediadb.httpserver.main.CookieManager;
import org.entermediadb.httpserver.util.DelegatingServletInputStream;

public class HttpRequest implements HttpServletRequest {
	
    private static final Log log = LogFactory.getLog(HttpRequest.class);

    private String method;
    private String path;
    private String httpVersion;
    private Map<String, Collection> headers = new HashMap<>();
    private Map<String, String[]> parameters = new HashMap<>();
    private String characterEncoding = "UTF-8";
    private String queryString;
    private String remoteAddr;
    private int remotePort;
    private String localAddr;
    private int localPort;
    private String serverName;
    private int serverPort;
    private String protocol;
    private String scheme = "http";
    private ServletContext servletContext;
    private HttpSession session;
    private Map<String, Object> attributes = new HashMap<>();
    private InputStream fromClientInputStream;
    private BufferedReader reader;
    private ServletInputStream servletinputStream;
    protected org.entermediadb.httpserver.http.HttpSession fieldClientSession;

    
    protected CookieManager fieldCookieManager;
    public CookieManager getCookieManager()
	{
		return fieldCookieManager;
	}



	public void setCookieManager(CookieManager inCookieManager)
	{
		fieldCookieManager = inCookieManager;
	}



	// Constructor
    public HttpRequest(InputStream input, BufferedReader inreader) 
    {
    	fromClientInputStream = input;
    	reader = inreader;
    	
//        try {
//            InetAddress localHost = InetAddress.getLocalHost();
//            this.localAddr = localHost.getHostAddress();
//            this.serverName = localHost.getHostName();
//        } catch (Exception e) {
//            this.localAddr = "127.0.0.1";
//            this. 		   = "localhost";
//        }
    }

  
    
    // Setters for internal use
    public void setMethod(String method) { this.method = method; }
    public void setPath(String path) { this.path = path; }
    public void setHttpVersion(String version) { this.httpVersion = version; }
    public void setQueryString(String queryString) { this.queryString = queryString; }
    public void setRemoteAddr(String addr) { this.remoteAddr = addr; }
    public void setRemotePort(int port) { this.remotePort = port; }
    public void setLocalPort(int port) { this.localPort = port; }
    public void setServerPort(int port) { this.serverPort = port; }
    public void setProtocol(String protocol) { this.protocol = protocol; }

    // HttpServletRequest implementation
    @Override
    public String getAuthType() { return null; }

    public javax.servlet.http.Cookie getCookie(String name) 
    {
    	javax.servlet.http.Cookie cookie = getCookieManager().getCookie(name);
        return cookie;
    }
    @Override
    public javax.servlet.http.Cookie[] getCookies() {
        Collection<javax.servlet.http.Cookie> cookies = getCookieManager().getAllCookies();
        return cookies.toArray(new Cookie[cookies.size()]);
    }

    @Override
    public long getDateHeader(String name) {
        String value = getHeader(name);
        if (value == null) return -1L;
        try {
            return Date.parse(value);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Cannot parse date header");
        }
    }

    @Override
    public String getHeader(String name) {
    	Collection values = headers.get(name);
    	if( values != null && !values.isEmpty() )
    	{
    		return (String)values.iterator().next();
    	}
    	return null;
    			
    }

    public Map<String,Collection> getHeaders() {
    	return headers;
    }
    
    @Override
    public Enumeration<String> getHeaders(String name) {
        Collection values = headers.get(name);
        if (values == null) return Collections.emptyEnumeration();
        return Collections.enumeration(values);
    }

    @Override
    public Enumeration<String> getHeaderNames() {
        return Collections.enumeration(headers.keySet());
    }

    @Override
    public int getIntHeader(String name) {
        String value = getHeader(name);
        if (value == null) return -1;
        return Integer.parseInt(value);
    }

    @Override
    public String getMethod() { return method; }

    @Override
    public String getPathInfo() { return null; }

    @Override
    public String getPathTranslated() { return null; }

    @Override
    public String getContextPath() { return ""; }

    @Override
    public String getQueryString() { return queryString; }

    @Override
    public String getRemoteUser() { return null; }

    @Override
    public boolean isUserInRole(String role) { return false; }

    @Override
    public Principal getUserPrincipal() { return null; }

    @Override
    public String getRequestedSessionId() { return null; }

    @Override
    public String getRequestURI() { return path; }

    @Override
    public StringBuffer getRequestURL() {
        StringBuffer url = new StringBuffer();
        url.append(scheme).append("://")
           .append(serverName).append(":")
           .append(serverPort)
           .append(getRequestURI());
        return url;
    }

    @Override
    public String getServletPath() { return ""; }

    @Override
    public HttpSession getSession(boolean create) { 
    	return getSession();
    }

    @Override
    public HttpSession getSession() { 
    	return fieldClientSession;
    }
    public void setSession(org.entermediadb.httpserver.http.HttpSession inSession) { 
    	fieldClientSession = inSession;
    }

    @Override
    public String changeSessionId() { return null; }

    @Override
    public boolean isRequestedSessionIdValid() { return false; }

    @Override
    public boolean isRequestedSessionIdFromCookie() { return false; }

    @Override
    public boolean isRequestedSessionIdFromURL() { return false; }

    @Override
    public boolean isRequestedSessionIdFromUrl() { return false; }

    @Override
    public boolean authenticate(HttpServletResponse response) { return false; }

    @Override
    public void login(String username, String password) { }

    @Override
    public void logout() { }

    @Override
    public Collection<Part> getParts() { return Collections.emptyList(); }

    @Override
    public Part getPart(String name) { return null; }

    @Override
    public <T extends HttpUpgradeHandler> T upgrade(Class<T> handlerClass) { return null; }

    @Override
    public Object getAttribute(String name) {
        return attributes.get(name);
    }

    @Override
    public Enumeration<String> getAttributeNames() {
        return Collections.enumeration(attributes.keySet());
    }

    @Override
    public String getCharacterEncoding() {
        return characterEncoding;
    }

    @Override
    public void setCharacterEncoding(String env) {
        this.characterEncoding = env;
    }

    @Override
    public int getContentLength() {
        String length = getHeader("Content-Length");
        return length != null ? Integer.parseInt(length) : -1;
    }

    @Override
    public long getContentLengthLong() {
        String length = getHeader("Content-Length");
        return length != null ? Long.parseLong(length) : -1L;
    }

    @Override
    public String getContentType() {
        return getHeader("Content-Type");
    }

    @Override
    public ServletInputStream getInputStream() {
        if (servletinputStream == null) {
        	servletinputStream = new DelegatingServletInputStream(fromClientInputStream);
        }
        return servletinputStream;
    }

    @Override
    public String getParameter(String name) {
        String[] values = parameters.get(name);
        return values != null && values.length > 0 ? values[0] : null;
    }

    @Override
    public Enumeration<String> getParameterNames() {
        return Collections.enumeration(parameters.keySet());
    }

    @Override
    public String[] getParameterValues(String name) {
        return parameters.get(name);
    }

    @Override
    public Map<String, String[]> getParameterMap() {
        return Collections.unmodifiableMap(parameters);
    }

    @Override
    public String getProtocol() {
        return protocol != null ? protocol : httpVersion;
    }

    @Override
    public String getScheme() {
        return scheme;
    }

    @Override
    public String getServerName() {
        return serverName;
    }
    public void setServerName(String inName)
    {
        serverName = inName;
    }

    @Override
    public int getServerPort() {
        return serverPort;
    }

    @Override
    public BufferedReader getReader() throws IOException {
        return reader;
    }

    @Override
    public String getRemoteAddr() {
        return remoteAddr;
    }

    @Override
    public String getRemoteHost() {
        return remoteAddr;
    }

    @Override
    public void setAttribute(String name, Object o) {
        attributes.put(name, o);
    }

    @Override
    public void removeAttribute(String name) {
        attributes.remove(name);
    }

    @Override
    public Locale getLocale() {
        String languages = getHeader("Accept-Language");
        if (languages == null || languages.isEmpty()) {
            return Locale.getDefault();
        }
        return Locale.forLanguageTag(languages.split(",")[0]);
    }

    @Override
    public Enumeration<Locale> getLocales() {
        String languages = getHeader("Accept-Language");
        if (languages == null || languages.isEmpty()) {
            return Collections.enumeration(Collections.singletonList(Locale.getDefault()));
        }
        List<Locale> locales = new ArrayList<>();
        for (String lang : languages.split(",")) {
            locales.add(Locale.forLanguageTag(lang.trim().split(";")[0]));
        }
        return Collections.enumeration(locales);
    }

    @Override
    public boolean isSecure() {
        return "https".equalsIgnoreCase(scheme);
    }

    @Override
    public RequestDispatcher getRequestDispatcher(String path) {
        return null;
    }

    @Override
    public String getRealPath(String path) {
        return null;
    }

    @Override
    public int getRemotePort() {
        return remotePort;
    }

    @Override
    public String getLocalName() {
        return serverName;
    }

    @Override
    public String getLocalAddr() {
        return localAddr;
    }

    @Override
    public int getLocalPort() {
        return localPort;
    }

    @Override
    public ServletContext getServletContext() {
        return servletContext;
    }

    @Override
    public AsyncContext startAsync() throws IllegalStateException {
        throw new IllegalStateException("Async not supported");
    }

    @Override
    public AsyncContext startAsync(ServletRequest servletRequest, ServletResponse servletResponse) throws IllegalStateException {
        throw new IllegalStateException("Async not supported");
    }

    @Override
    public boolean isAsyncStarted() {
        return false;
    }

    @Override
    public boolean isAsyncSupported() {
        return false;
    }

    @Override
    public AsyncContext getAsyncContext() {
        return null;
    }

    @Override
    public DispatcherType getDispatcherType() {
        return DispatcherType.REQUEST;
    }

    // Helper method for internal use
    public void addHeader(String name, String value) 
    {
    	Collection existing = headers.get(name);
    	if( existing == null)
    	{
    		existing = new ArrayList();
    		headers.put(name,existing);
    	}
        existing.add(value);
    }

    // Helper method for internal use
    public void parseParameters() {
        // Parse query string parameters
        if (queryString != null) {
            addParameters(queryString);
        }

        // Parse POST parameters from body
        if ("POST".equalsIgnoreCase(method)) {
            String contentType = getContentType();
            if (contentType != null && contentType.startsWith("application/x-www-form-urlencoded")) {
            	String body = readBody();
                addParameters(body);
            }
        }
    }

    private String readBody()
	{
    	StringBuffer all = new StringBuffer();
    	try
    	{
	    	String nextline = getReader().readLine();
	    	while(nextline != null)
	    	{
	    		all.append(nextline);
	    		nextline = getReader().readLine();
	    	}
    	}
    	catch( Exception ex)
    	{
    		log.error(ex);
    	}
    	return all.toString();
	}



	private void addParameters(String params) {
        for (String param : params.split("&")) {
            String[] pair = param.split("=", 2);
            String name = pair[0];
            String value = pair.length > 1 ? pair[1] : "";
            String[] existing = parameters.get(name);
            if (existing == null) {
                parameters.put(name, new String[]{value});
            } else {
                String[] newValues = Arrays.copyOf(existing, existing.length + 1);
                newValues[existing.length] = value;
                parameters.put(name, newValues);
            }
        }
    }
} 