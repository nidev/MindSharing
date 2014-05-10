/**
 * 
 */
package jnu.mindsharing.chainengine;

import java.sql.Connection;

import jnu.mindsharing.common.EmoUnit;
import jnu.mindsharing.common.P;

/**
 * @author nidev
 *
 */
public class EQueryTool
{
	
	private String TAG = "EQTool";
	Connection msdb;
	
	public EQueryTool(Connection db)
	{
		msdb = db;
	}

	/**
	 * @param args
	 */
	public long queryNounCategory(String word)
	{
		// STUB
		P.d(TAG, "Quering noun category : %s", word);
		return 0;
	}
	
	public EmoUnit queryEmotionalDescriptor(String word)
	{
		// STUB
		P.d(TAG, "Quering emotional-descriptor : %s", word);
		return null;
	}
	
	public EmoUnit queryEmoticon(String word)
	{
		// STUB
		P.d(TAG, "Quering emoticon : %s", word);
		return null;
	}
	
	public boolean isEmoticon(String word)
	{
		// STUB
		P.d(TAG, "Quering emoticon : %s", word);
		return true; // STUB: 일단 들어가는지 체크함
	}
	
	public boolean isEnhancer(String word)
	{
		// STUB
		P.d(TAG, "Quering enhancer : %s", word);
		return true; // STUB: 감정을 강화하는가
	}
	
	public boolean isReducer(String word)
	{
		// STUB
		P.d(TAG, "Quering reducer : %s", word);
		return true; // STUB: 감정을 감소하는가
	}
	
	public boolean isNegativeADV(String word)
	{
		// STUB
		P.d(TAG, "Quering negative-adverb : %s", word);
		return word.equals("안") || word.equals("아니");
	}
}
