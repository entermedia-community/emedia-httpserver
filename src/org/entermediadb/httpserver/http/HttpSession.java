package org.entermediadb.httpserver.http;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSessionContext;

public class HttpSession implements javax.servlet.http.HttpSession
{
	protected Map<String,Object> fieldAttributes = new HashMap();
	
	@Override
	public long getCreationTime()
	{
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getId()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getLastAccessedTime()
	{
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public ServletContext getServletContext()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setMaxInactiveInterval(int inInterval)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public int getMaxInactiveInterval()
	{
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public HttpSessionContext getSessionContext()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object getAttribute(String inName)
	{
		return getValue(inName);
	}

	@Override
	public Object getValue(String inName)
	{
		return fieldAttributes.get(inName);
	}

	@Override
	public Enumeration<String> getAttributeNames()
	{
		return java.util.Collections.enumeration(fieldAttributes.keySet());
	}

	@Override
	public String[] getValueNames()
	{
		Set<String> set = fieldAttributes.keySet();
		String[] array = set.toArray(new String[set.size()]);
		return array;
	}

	@Override
	public void setAttribute(String inName, Object inValue)
	{
		fieldAttributes.put(inName,inValue);
	}

	@Override
	public void putValue(String inName, Object inValue)
	{
		// TODO Auto-generated method stub
		fieldAttributes.put(inName,inValue);
	}

	@Override
	public void removeAttribute(String inName)
	{
		// TODO Auto-generated method stub
		fieldAttributes.remove(inName);
	}

	@Override
	public void removeValue(String inName)
	{
		// TODO Auto-generated method stub
		fieldAttributes.remove(inName);
	}

	@Override
	public void invalidate()
	{
		// TODO Auto-generated method stub
		fieldAttributes.clear();
	}

	@Override
	public boolean isNew()
	{
		// TODO Auto-generated method stub
		return false;
	}

}
