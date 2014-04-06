package jnu.mindsharing.common;

import java.text.SimpleDateFormat;
import java.util.Date;

public class P
{
	final static String baseFormat = "[%s = %s] %s";
	private static StringBuffer internalBuffer = new StringBuffer(2<<16);;
	
	public static String getDatetime()
	{
		SimpleDateFormat date = new SimpleDateFormat("MM-dd HH:mm:ss");
		return date.format(new Date());
	}
	
	private static void logToInternal(String logline)
	{
		 internalBuffer.append(logline);
	}
	 
	public static void resetBuffer()
	{
		internalBuffer.delete(0, internalBuffer.length());
	}
	
	public static String getString()
	{
		return internalBuffer.toString();
	}

	public static void d(String msgid, Object msg, Object...args)
	{
		if (args.length == 0)
		{
			String logline = String.format(baseFormat, getDatetime(), msgid, msg.toString());
			System.out.println(logline);
			logToInternal(logline);
		}
		else
		{
			String new_msg = String.format(msg.toString(), args);
			String logline = String.format(baseFormat, getDatetime(), msgid, new_msg);
			System.out.println(logline);
			logToInternal(logline);
		}
	}
	
	public static void b()
	{
		System.out.println(" --------------------------------------------------- ");
		logToInternal(" --------------------------------------------------- ");
	}
	
	public static void e(String msgid, Object msg, Object...args)
	{
		if (args.length == 0)
		{
			String logline = String.format(baseFormat, getDatetime(), msgid, msg.toString());
			System.err.println(logline);
			logToInternal(logline);
		}
		else
		{
			String new_msg = String.format(msg.toString(), args);
			String logline = String.format(baseFormat, getDatetime(), msgid, new_msg);
			System.err.println(logline);
			logToInternal(logline);
		}
	}
}
