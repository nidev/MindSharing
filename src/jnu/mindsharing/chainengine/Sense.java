/**
 * 
 */
package jnu.mindsharing.chainengine;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import jnu.mindsharing.chainengine.baseidioms.BaseIdioms;
import jnu.mindsharing.common.DatabaseConstants;
import jnu.mindsharing.common.Hana;
import jnu.mindsharing.common.P;

/**
 * Mighty Special and Responsible module for Database Learning (Mighty SR)
 * 
 * 기본적으로 다음과 역할을 수행한다.
 * 
 * 1) 기본 어휘의 감정 확률 수신
 * 2) 확장 어휘의 추정 감정 확률 수신
 * 3) 피드백 인터페이스
 * 4) 데이터베이스 통계
 * 
 * @author nidev
 *
 */


public class Sense extends DatabaseConstants
{
	private String TAG = "Sense";
	private Connection db = null;
	private BaseIdioms bi = null;
	
	private String last_err_msg;
	

	/**
	 * 사전에 준비된 데이터베이스 연결이 있다면, 이를 사용해서 감정 데이터베이스에 접근한다.
	 * @param provided_db 사전에 준비된 데이터베이스 Connection
	 */
	public Sense()
	{
		P.d(TAG, "Sense Class created (With baseIdioms = 100)");
		bi = new BaseIdioms();
		try
		{
			bi.loadSet(BaseIdioms.DSC.SET100);
		}
		catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		} // 기본 어휘는 100개 세트를 사용한다.
	}
	
	public Sense(int dsc_size)
	{
		// 
		P.d(TAG, "Sense Class created (With baseIdioms = %d)", dsc_size);
		bi = new BaseIdioms();
		try
		{
			switch(dsc_size)
			{
			case 20:
				bi.loadSet(BaseIdioms.DSC.SET20);
			case 50:
				bi.loadSet(BaseIdioms.DSC.SET50);
			case 100:
				bi.loadSet(BaseIdioms.DSC.SET100);
			case 200:
				bi.loadSet(BaseIdioms.DSC.SET200);
			default:
				P.e(TAG, "Couldn't identify dataset size. Load SET100");
				bi.loadSet(BaseIdioms.DSC.SET100);
			}
		}
		catch (Exception e)
		{
			// stub
			e.printStackTrace();
		}
	}
	
	/**
	 * 주소와 데이터베이스 이름을 사용해 데이터베이스 Connection을 가져온다. 예외가 발생하면 null 을 반환한다.
	 * @return 데이터베이스 Connection, 또는 null
	 */
	public Connection getDatabaseConnection()
	{
		String uri = (new DB_URI()).toString();
		String localTAG = "GET-DB";

		try
		{
			db = DriverManager.getConnection(uri, DB_URI.user, DB_URI.password);
			P.d(localTAG, "데이터베이스에 연결되었습니다.");
			last_err_msg = null;
			// Concrete Database Structure.
			sanitizeTableStructure();
			return db;
		}
		catch (SQLException e)
		{
			StringBuffer err_msg = new StringBuffer();
			err_msg.append("데이터베이스 연결에 실패했습니다. ");
			err_msg.append(String.format("Exception class: %s,  Exception message: %s", e.toString(), e.getMessage()));
			err_msg.append("\r\n꼭 Sense.getLastError() 로 오류를 체크하십시오.");
			e.printStackTrace();
			last_err_msg = err_msg.toString();
			P.e(localTAG, last_err_msg);
			return null;
		}
	}
	
	/**
	 * getDatabaseConnection에서 null을 반환받은 경우, 오류메시지를 확인할 수 있다.
	 * 만약 오류가 발생하지 않았다면 null을 반환한다.
	 * @return 오류메시지(String)
	 */
	public String getLastError()
	{
		return last_err_msg;
	}
	
	/**
	 * 학습 시스템을 위한 기본 테이블을 구성한다. 실패하면 false를 반환한다
	 * @return boolean값
	 */
	public boolean sanitizeTableStructure()
	{
		try
		{
			// Newly-learned words
			PreparedStatement create_newdex_table = db.prepareStatement(
					"CREATE TABLE IF NOT EXIST Newdex ("
					+ "id SERIAL CONSTRAINT expr_PK PRIMARY KEY, "
					+ "expression TEXT NOT NULL, "
					+ "exprtype SMALLINT NOT NULL, "
					+ "locked BOOLEAN DEFAULT FALSE"
					+ "eprob_lock NUMERIC DEFAULT 0.0, "
					+ "sprob_lock NUMERIC DEFAULT 0.0, "
					+ "exprhash TEXT NOT NULL);"
					);
			create_newdex_table.execute();
			// Machine Learning record
			PreparedStatement create_record_table = db.prepareStatement(
					"CREATE TABLE IF NOT EXIST Dexrecord ("
					+ "id SERIAL CONSTRAINT expr_PK PRIMARY KEY, "
					+ "exprhash TEXT NOT NULL, "
					+ "eprob NUMERIC DEFAULT 0.0, "
					+ "sprob NUMERIC DEFAULT 0.0, "
					+ "rate NUMERIC DEFAULT 0.0;"
					);
			create_record_table.execute();
			// Statistics record
			// Every learning should be recorded
			PreparedStatement create_stat_table = db.prepareStatement(
					"CREATE TABLE IF NOT EXIST Newdex ("
					+ "id SERIAL CONSTRAINT expr_PK PRIMARY KEY, "
					+ "evtype SMALLINT NOT NULL, "
					+ "words_given SMALLINT NOT NULL, "
					+ "words_emotional SMALLINT NOT NULL, "
					+ "source_name TEXT NOT NULL);"
					);
			create_stat_table.execute();
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
		return false;
	}
	
	/**
	 * 기본 어휘에 존재하는지 확인한다.
	 * @param word 기본어휘
	 * @return 존재하는 경우 해당하는 BaseIdioms.Idiom 객체, 아니면 null
	 */
	public BaseIdioms.Idiom isBaseIdiom(String word)
	{
		for (BaseIdioms.Idiom i: bi.retIdioms())
		{
			if (i.toString().equals(word))
			{
				return i;
			}
		}
		return null;
	}
	/**
	 * 주어진 표현의 감정값을 수신한다. (동음이의어 처리가 안되어있음)
	 * @param word 한국어표현
	 * @return 감정값이 담긴 EmoUnit 객체, 또는 null (어휘가 없음)
	 * @see EmoUnit
	 */
	public Hana ask(String word)
	{
		P.d(TAG, "Query Emotional/State information on (%s)", word);
		P.d(TAG, "... Searching on BaseIdioms");
		if (isBaseIdiom(word) != null)
		{
			Hana baseidiom = new Hana
		}
			
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
	
	public boolean addRecord()
	{
		return false; // stub
	}
	
	
	public String getSenseStatDigest()
	{
		return null;
	}
	
}
