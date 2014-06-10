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
 * 감정값을 데이터베이스로 가져오기 위한 도구이다.
 * 
 * @author nidev
 *
 */
public class EQueryTool
{
	final int TYPE_WORD = 0;
	final int TYPE_EMOTICON = 1;
	
	private String TAG = "EmotionQuery";
	
	private Connection db;
	
	
	/**
	 * 사전에 준비된 데이터베이스 연결이 있다면, 이를 사용해서 감정 데이터베이스에 접근한다.
	 * @param provided_db 사전에 준비된 데이터베이스 Connection
	 */
	public EQueryTool(Connection provided_db) throws NullPointerException
	{
		if (provided_db == null)
		{
			throw new NullPointerException();
		}
		db = provided_db;
	}
	
	/**
	 * 기본 데이터베이스 연결 설정을 사용해, 감정 데이터베이스에 연결한다.
	 */
	public EQueryTool()
	{
		// EQueryConstants.java 에 정의된 기본 호스트를 사용한다.
		db = getDatabaseConnection();
	}
	
	/**
	 * 호스트 주소와 데이터베이스 이름을 사용해, 감정 데이터베이스에 연결한다.
	 * @param host 서버 주소
	 * @param database_name 데이터베이스 이름
	 */
	public EQueryTool(String host, String database_name)
	{
		db = getDatabaseConnection(host, database_name);
	}
	
	/**
	 * 주소와 데이터베이스 이름을 사용해 데이터베이스 Connection을 가져온다. 예외가 발생하면 null 을 반환한다.
	 * @param host 서버 주소
	 * @param dbname 데이터베이스 이름
	 * @return 데이터베이스 Connection, 또는 null
	 */
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
	
	/**
	 * 기본 설정을 사용해 데이터베이스 Connection을 가져온다. 예외가 발생하면 null을 반환한다.
	 * @return 데이터베이스 Connection, 또는 null
	 */
	public static Connection getDatabaseConnection()
	{
		return getDatabaseConnection(EQueryConstants.SQL_HOST, EQueryConstants.SQL_DBNAME);
	}
	
	/**
	 * 데이터베이스로 연결이 가능한지 설정을 테스트한다. 가능한 경우 true, 실패한 경우에는 false를 반환한다.
	 * @param host 서버 주소
	 * @param dbname 데이터베이스 이름
	 * @return true (성공시), false (실패시)
	 */
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
	 * 명사 어휘의 범주를 파악한다. (미구현)
	 * @param word 명사 어휘
	 * @return 범주 값(long 타입)
	 */
	public long queryNounCategory(String word)
	{
		P.d(TAG, "Query? [CATEGORY OF] %s", word);
		return 0;
	}
	
	/**
	 * 주어진 표현의 감정값을 수신한다.
	 * @param word 한국어표현
	 * @param type TYPE_WORD(일반 어휘), TYPE_EMOTICON(이모티콘)
	 * @return 감정값이 담긴 EmoUnit 객체, 또는 null (어휘가 없음)
	 * @see EmoUnit
	 */
	public EmoUnit queryExpression(String word, int type)
	{
		P.d(TAG, "Query? [EMOTION VALUE OF] %s [TYPE] %d", word, type);
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
	
	/**
	 * 일반 어휘의 감정값을 가져온다.
	 * @param word 한국어 어휘
	 * @return EmoUnit 객체
	 * @see EmoUnit
	 */
	public EmoUnit queryWord(String word)
	{
		return queryExpression(word, TYPE_WORD);
	}
	
	/**
	 * 이모티콘의 감정값을 가져온다. (미구현)
	 * @param word 이모티콘
	 * @return EmoUnit 객체
	 * @see EmoUnit
	 */
	public EmoUnit queryEmoticon(String word)
	{
		return queryExpression(word, TYPE_EMOTICON);
	}
	
	/**
	 * 주어진 문자열이 이모티콘 데이터베이스에 있는지 판별한다. (미구현)
	 * @param word 문자열
	 * @return true(이모티콘 DB에 있는 경우), false(이모티콘 DB에 없음)
	 */
	public boolean isEmoticon(String word)
	{
		return queryEmoticon(word) != null ? true : false;
	}
	
	/**
	 * 감정값을 강화하는 어휘인지 확인한다.
	 * @param word 일반 어휘
	 * @return true(해당하는 경우), false(아닌 경우)
	 */
	public boolean isEnhancer(String word)
	{
		P.d(TAG, "Query? [EMOTION ENHANCER] %s", word);
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
	
	/**
	 * 감정값을 약화하는 어휘인지 확인한다.
	 * @param word 일반 어휘
	 * @return true(해당하는 경우), false(아닌 경우)
	 */
	public boolean isReducer(String word)
	{
		P.d(TAG, "Query? [EMOTION REDUCER] %s", word);
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
	
	/**
	 * 감정값을 반전시키는 어휘인지 확인한다.
	 * @param word 일반 어휘
	 * @return true(해당하는 경우), false(아닌 경우)
	 */
	public boolean isNegativeADV(String word)
	{
		P.d(TAG, "Query? [NEGATIVE ADVERB] %s", word);
		return word.equals("안") || word.equals("아니");
	}
}
