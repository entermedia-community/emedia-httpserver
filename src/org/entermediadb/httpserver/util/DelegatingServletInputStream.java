package org.entermediadb.httpserver.util;

import java.io.IOException;
import java.io.InputStream;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;

public class DelegatingServletInputStream extends ServletInputStream
{
	protected InputStream toclientinputstream;
	public DelegatingServletInputStream(InputStream inclientinputstream)
	{
		toclientinputstream = inclientinputstream;
	}
	@Override
	public boolean isFinished()
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isReady()
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setReadListener(ReadListener inArg0)
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public int read() throws IOException
	{
		return toclientinputstream.read();
	}

	@Override
	public int read(byte[] inB, int inOff, int inLen) throws IOException
	{
		return toclientinputstream.read(inB, inOff, inLen);
	}
	
	@Override
	public byte[] readAllBytes() throws IOException
	{
		// TODO Auto-generated method stub
		return toclientinputstream.readAllBytes();
	}

	
}
