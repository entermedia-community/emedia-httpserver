package org.entermediadb.httpserver.main;

import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import org.entermediadb.httpserver.http.Cookie;

public class CookieManager {
    private final Map<String, javax.servlet.http.Cookie> cookies = new HashMap<>();

    public void parseCookieHeader(Enumeration cookieHeaders) {
        if (cookieHeaders != null) {
        	while( cookieHeaders.hasMoreElements())
        	{
        		String cookieHeader = (String)cookieHeaders.nextElement();
	            String[] cookiePairs = cookieHeader.split("; ");
	            for (String cookiePair : cookiePairs) {
	                String[] parts = cookiePair.split("=", 2);
	                if (parts.length == 2) {
	                    String name = parts[0];
	                    Cookie value = new Cookie(parts[0], parts[1]);
	                    addCookie(value);
	                }
	            }
			} 
        }
    }

    public javax.servlet.http.Cookie getCookie(String name) {
        return cookies.get(name);
    }

    public Collection<javax.servlet.http.Cookie> getAllCookies() {
        return cookies.values();
    }

    public void addCookie(javax.servlet.http.Cookie cookie) {
        cookies.put(cookie.getName(), cookie);
    }

    public void removeCookie(String name) {
        cookies.remove(name);
    }

    public static Cookie createSecureCookie(String name, String value) {
        Cookie cookie = new Cookie(name, value);
        cookie.setSecure(true);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        return cookie;
    }
} 