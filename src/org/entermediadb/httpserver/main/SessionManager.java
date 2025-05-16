package org.entermediadb.httpserver.main;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.entermediadb.httpserver.http.Cookie;
import org.entermediadb.httpserver.http.HttpRequest;
import org.entermediadb.httpserver.http.HttpSession;

public class SessionManager
{
	protected Map<String,HttpSession> fieldSessions = new HashMap(); //Make this timeout
	
	public HttpSession loadSession(HttpRequest inReq)
	{
		javax.servlet.http.Cookie cookie = inReq.getCookie("JSESSIONID");
		if( cookie == null)
		{
			cookie = new Cookie("JSESSIONID",UUID.randomUUID().toString());
			cookie.setMaxAge(Integer.MAX_VALUE);
			inReq.getCookieManager().addCookie(cookie);
		}
		String sessionid = cookie.getValue();
		HttpSession session = fieldSessions.get(sessionid);
		if( session == null)
		{
			session = new HttpSession();
			fieldSessions.put(sessionid,session);
		}
		return session;
	}
}
