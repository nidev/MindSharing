/**
 * 
 */
package jnu.mindsharing.chainengine;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import jnu.mindsharing.common.EQueryConstants;
import jnu.mindsharing.common.EmoUnit;
import jnu.mindsharing.common.P;

/**
 * @author nidev
 *
 */
public class EQueryTool
{
	final int TYPE_WORD = 0;
	final int TYPE_EMOTICON = 1;
	
	private String TAG = "EQTool";
	
	private Connection db;
	
	
	public EQueryTool(Connection provided_db) throws NullPointerException
	{
		if (provided_db == null)
		{
			throw new NullPointerException();
		}
		db = provided_db;
	}
	
	public EQueryTool()
	{
		// EQueryConstants.java 에 정의된 기본 호스트를 사용한다.
		db = getDatabaseConnection();
	}
	
	public EQueryTool(String host, String database_name)
	{
		db = getDatabaseConnection(host, database_name);
	}
	
	public static Connection getDatabaseConnection(String host, String dbname)
	{
		String uri = EQueryConstants.SQL_JDBCHEAD + host + "/" + dbname;
		String localTAG = "GET-DB";
		
		Connection mindsharing_db = null;
		try
		{
			mindsharing_db = DriverManager.getConnection(uri, "mindsharing", "mindsharing");
			P.d(localTAG, "데이터베이스에 연결되었습니다.");
			return mindsharing_db;
		}
		catch (SQLException e)
		{
			P.e(localTAG, "데이터베이스 연결에 실패했습니다.");
			e.printStackTrace();
			P.e(localTAG, "Exception class: %s,  Exception message: %s", e.toString(), e.getMessage());
			P.e(localTAG, "안전을 위해 프로그램 전체를 종료합니다. 이 오류는 로그 시스템에 기록되었습니다.");
			System.exit(-1);
			return null;
		}
	}
	
	public static Connection getDatabaseConnection()
	{
		return getDatabaseConnection(EQueryConstants.SQL_HOST, EQueryConstants.SQL_DBNAME);
	}
	
	public static boolean testDatabaseConnection(String host, String dbname)
	{
		String uri = EQueryConstants.SQL_JDBCHEAD + host + "/" + dbname;
		String localTAG = "TEST-DB";
		
		P.d(localTAG, "데이터베이스 연결 중입니다. (%s)", uri);
		Connection mindsharing_db = getDatabaseConnection(host, dbname);
		try
		{
			if (mindsharing_db != null)
			{
				P.d(localTAG, "데이터베이스를 확인하였습니다.");
				mindsharing_db.close();
				return true;
			}
			else
			{
				throw new Exception();
			}
		}
		catch (Exception e)
		{
			P.e(localTAG, "데이터베이스 이용 불가능");
			return false;
		}
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
	
	public EmoUnit queryExpression(String word, int type)
	{
		P.d(TAG, "Quering ... %s", word);
		try
		{
			PreparedStatement stmt = db.prepareStatement("SELECT * FROM Expression WHERE word = ? AND type = ?");
			
			stmt.setString(1, word);
			stmt.setInt(2, type);
			ResultSet res = stmt.executeQuery();
			
			EmoUnit em = null;
			while (res.next())
			{
				if (em == null)
				{
					em = new EmoUnit(word);
					// XXX: Hardcoded titles
					em.importVectors(res.getInt("joy"), res.getInt("sorrow"), res.getInt("growth"), res.getInt("cease"));
					break;
				}
			}
			return em;
		}
		catch (SQLException e)
		{
			P.e(TAG, "쿼리 도중 오류가 발생하였습니다.");
			e.printStackTrace();
			return null;
		}
	}
	
	public EmoUnit queryWord(String word)
	{
		P.d(TAG, "Quering word on %s requested", word);
		return queryExpression(word, TYPE_WORD);
	}
	
	public EmoUnit queryEmoticon(String word)
	{
		P.d(TAG, "Quering emoticon on %s requested", word);
		return queryExpression(word, TYPE_EMOTICON);
	}
	
	public boolean isEmoticon(String word)
	{
		return queryEmoticon(word) != null ? true : false;
	}
	
	public boolean isEnhancer(String word)
	{
		// XXX: Please avoid hardcode
		P.d(TAG, "Quering enhancer : %s", word);
		String enhancers[] = {"매우", "잘", "정말", "진짜", "진심", "참", "너무"};
		for (String keyword: enhancers)
		{
			if (word.equals(keyword))
			{
				return true;
			}
		}
		return false;
	}
	
	public boolean isReducer(String word)
	{
		// XXX: Please avoid hardcode
		P.d(TAG, "Quering reducer : %s", word);
		String reducers[] = {"약간", "조금", "살짝"};
		for (String keyword: reducers)
		{
			if (word.equals(keyword))
			{
				return true;
			}
		}
		return false;
	}
	
	public boolean isNegativeADV(String word)
	{
		// STUB
		P.d(TAG, "Quering negative-adverb : %s", word);
		return word.equals("안") || word.equals("아니");
	}
}
