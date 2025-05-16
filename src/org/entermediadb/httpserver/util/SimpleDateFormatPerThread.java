package org.entermediadb.httpserver.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Allows thread safe access to a shared date format
 * @author cburkey
 */

public class SimpleDateFormatPerThread 
{
	protected ThreadLocal<DateFormat> perThreadCache = new ThreadLocal<DateFormat>();
    protected String fieldFormat;
    protected Locale fieldLocale;
    
	public String getFormat() 
	{
		return fieldFormat;
	}

	public void setFormat(String inFormat) 
	{
		fieldFormat = inFormat;
	}

	public SimpleDateFormatPerThread(String inFormat) 
	{
		setFormat(inFormat);
	}
	
	public SimpleDateFormatPerThread(String inFormat, Locale inLocale) 
	{
		setFormat(inFormat);
		setLocale(inLocale);
	}
	
	
	public Date parse(String inDate) 
    {
		DateFormat format = perThreadCache.get();
		if( format == null)
		{
			format = new SimpleDateFormat(fieldFormat);
			format.setTimeZone(TimeZone.getTimeZone("GMT"));
			perThreadCache.set(format);
		}
		try
		{	
			return format.parse(inDate);
		}
		catch( ParseException ex)
		{
			throw new RuntimeException(ex);
		}
    }

	public String format(Date inDate)
	{
		DateFormat format = perThreadCache.get();
		if( format == null)
		{
			format = new SimpleDateFormat(fieldFormat);
			format.setTimeZone(TimeZone.getTimeZone("GMT"));
			perThreadCache.set(format);
		}
		return format.format(inDate);
		
	}

	public void setLocale(Locale locale)
	{
		fieldLocale = locale;
	}

}
