package jnu.mindsharing.common;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 
 * 디버깅을 위한 메시지 로그 도구
 * 
 * @author nidev
 *
 */
public class P
{
	final static String baseFormat = "[%s = %s] %s";
	private static StringBuffer internalBuffer = new StringBuffer(2<<16);;
	
	/**
	 * 로그에 삽입할 월/일/시각 정보를 문자열로 반환한다.
	 * @return 시각을 나타내는 문자열
	 */
	public static String getDatetime()
	{
		SimpleDateFormat date = new SimpleDateFormat("MM-dd HH:mm:ss");
		return date.format(new Date());
	}
	
	/**
	 * 내부 버퍼에 로그를 기록한다.
	 * @param logline 로그
	 */
	private static void logToInternal(String logline)
	{
		 internalBuffer.append(logline);
		 internalBuffer.append("\r\n");
	}
	 
	/**
	 * 내부 버퍼를 초기화한다.
	 */
	public static void resetBuffer()
	{
		internalBuffer.delete(0, internalBuffer.length());
	}
	
	/**
	 * 내부 버퍼에 기록된 로그를 모두 추출한다.
	 * @return 로그 텍스트
	 */
	public static String getString()
	{
		return internalBuffer.toString();
	}

	/**
	 * 디버그 메시지를 출력한다.
	 * @param msgid 메시지 출력을 한 위치(직접 입력)
	 * @param msg 메시지
	 * @param args 메시지에 포맷 문자열이 사용된 경우, 추가로 입력할 값들 
	 */
	synchronized public static void d(String msgid, Object msg, Object...args)
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
	
	/**
	 * 내부 버퍼와 표준 출력에 각각 긴 줄을 하나 삽입한다. 
	 */
	synchronized public static void b()
	{
		System.out.println(" --------------------------------------------------- ");
		logToInternal(" --------------------------------------------------- ");
	}
	
	/**
	 * 오류 메시지를 출력한다.
	 * @param msgid 메시지 출력을 한 위치(직접 입력)
	 * @param msg 메시지
	 * @param args 메시지에 포맷 문자열이 사용된 경우, 추가로 입력할 값들 
	 */
	synchronized public static void e(String msgid, Object msg, Object...args)
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
