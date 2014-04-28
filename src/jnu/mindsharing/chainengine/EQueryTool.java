/**
 * 
 */
package jnu.mindsharing.chainengine;

/**
 * @author nidev
 *
 */
public class EQueryTool
{

	/**
	 * @param args
	 */
	public static long queryLastEValue(String word)
	{
		return 0;
	}
	
	public static long queryLastEdata(String word)
	{
		return 0;
	}
	
	public static long queryFullEdata(String word)
	{
		return 0;
	}
	
	public static boolean appendNewEdata(String word, String srctext, long evalue)
	{
		return false;
	}
	
	public static long queryCategory(String word)
	{
		return 0;
	}
	
	public static boolean isEmoticon(String word)
	{
		return true; // STUB: 일단 들어가는지 체크함
	}
	
	public static boolean isEnhancer(String word)
	{
		return true; // STUB: 감정을 강화하는가
	}
	
	public static boolean isReducer(String word)
	{
		return true; // STUB: 감정을 감소하는가
	}
	
	public static boolean isNegativeADV(String word)
	{
		// 부정적인 접두 부사
		return word.equals("안") || word.equals("아니");
	}
}
