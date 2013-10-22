package libs;

import java.text.SimpleDateFormat;
import java.util.Date;


/**
 * 전역 디버깅용 메시지 출력 함수
 * 
 */
public class ELog
{
	private static int BUFFER_SIZE = 128<<20; // 128kB
	private static StringBuffer buffer = new StringBuffer(BUFFER_SIZE);
	private static String formatString_debug = "%s: %s\r\n";
	private static String formatString_error = "%s: *ERROR* %s\r\n";
	
	public static boolean isWritable()
	{
		// 버퍼 사용가능 여부 확인
		return (buffer != null && buffer.length() < BUFFER_SIZE);
	}
	
	public static void resetBuffer()
	{
		// 버퍼 비우기
		buffer.delete(0, buffer.length());
	}
	public static String getFullBuffer()
	{
		// 버퍼 내용 가져오기
		return buffer.toString();
	}
	
	public static void addTimelineToBuffer()
	{
		SimpleDateFormat date = new SimpleDateFormat("---- [yyyy-MM-dd HH:mm:ss] ----");
		buffer.append(date.format(new Date()));
		buffer.append("\r\n");
	}
	
	public static void d(String id, String msg)
	{
		// 로그 함수
		// System.out 으로 출력도 하고, 중앙 메모리에 기억도 해둠
		String output = String.format(formatString_debug, id, msg);
		System.out.print(output);
		buffer.append(output);
		
	}
	
	public static void d(String id, boolean value)
	{
		d(id, "Boolean type. value = " + String.valueOf(value));
		
	}
	
	public static void d(String id, int value)
	{
		d(id, "Integer type. value = " + String.valueOf(value));
	}
	
	public static void d(String id, float value)
	{
		d(id, "Float type. value = " + String.valueOf(value));
	}
	
	public static void d(String id, double value)
	{
		d(id, "Double type. value = " + String.valueOf(value));
	}
	
	public static void d(String id, long value)
	{
		d(id, "Long integer type. value = " + String.valueOf(value));
	}
	
	public static void e(String id, String msg)
	{
		// 로그 함수
		// System.err 으로 출력도 하고, 중앙 메모리에 기억도 해둠
		String output = String.format(formatString_error, id, msg);
		System.err.print(output);
		buffer.append(output);
	}
	
	public static void e(String id, boolean value)
	{
		e(id, "Boolean type. value = " + String.valueOf(value));
		
	}
	
	public static void e(String id, int value)
	{
		e(id, "Integer type. value = " + String.valueOf(value));
	}
	
	public static void e(String id, float value)
	{
		e(id, "Float type. value = " + String.valueOf(value));
	}
	
	public static void e(String id, double value)
	{
		e(id, "Double type. value = " + String.valueOf(value));
	}
	
	public static void e(String id, long value)
	{
		e(id, "Long integer type. value = " + String.valueOf(value));
	}
	
	public static void printArray(String id, Object[] array)
	{
		d(id + "/Array", "ARRAY LENGTH = " + array.length);
		for (int i=0; i < array.length ; i++)
		{
			d(id + "/Array", String.format("\t[%d] = \'%s\'", i, String.valueOf(array[i])));
		}
		
	}
	
	public static void main(String[] args)
	{
		/* 이 파일을 그냥 실행하면 다음과 같은 출력을 얻을 수 있다.

		ELogTag: Elog Module Test begins
		ELogTag: Integer type. value = 5
		ELogTag: *ERROR* Fake error
		ELogTag: Double type. value = 5.5555
		ELogTag: Long integer type. value = 100
		ELogTag: Double type. value = 0.0
		Grains/Array: ARRAY LENGTH = 3
		Grains/Array: 	[0] = 'Rice'
		Grains/Array: 	[1] = 'Wheat'
		Grains/Array: 	[2] = 'Oat'
		ELogTag: *ERROR* Integer type. value = 5
		ELogTag: *ERROR* Double type. value = 5.5555
		ELogTag: *ERROR* Long integer type. value = 100
		ELogTag: *ERROR* Double type. value = 0.0
		ELogTag: Elog Module Test begins
		---- [2013-10-22 11:16:38] ----
		ELogTag: *ERROR* Fake error
		ELogTag: Integer type. value = 5
		ELogTag: Double type. value = 5.5555
		ELogTag: Long integer type. value = 100
		ELogTag: Double type. value = 0.0
		Grains/Array: ARRAY LENGTH = 3
		Grains/Array: 	[0] = 'Rice'
		Grains/Array: 	[1] = 'Wheat'
		Grains/Array: 	[2] = 'Oat'
		ELogTag: *ERROR* Integer type. value = 5
		ELogTag: *ERROR* Double type. value = 5.5555
		ELogTag: *ERROR* Long integer type. value = 100
		ELogTag: *ERROR* Double type. value = 0.0
		
		ELogTag: Freeing all buffer
		ELogTag: Test Over.
		 */
		String TAG = "ELogTag";
		String[] testArray = {"Rice", "Wheat", "Oat"};
		
		d(TAG, "Elog Module Test begins");
		addTimelineToBuffer();
		e(TAG, "Fake error");
		d(TAG, 5);
		d(TAG, 5.5555);
		d(TAG, (long)100);
		d(TAG, (double) 0.000);
		printArray("Grains", testArray);
		e(TAG, 5);
		e(TAG, 5.5555);
		e(TAG, (long)100);
		e(TAG, (double) 0.000);
		System.out.println(getFullBuffer());
		d(TAG, "Freeing all buffer");
		resetBuffer();
		d(TAG, "Test Over.");
	}

}
