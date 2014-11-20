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
					+ "birthdate DATE NOT NULL, " // 입력일시
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
		BaseIdioms.Idiom idiom = isBaseIdiom(word);
		if (idiom != null)
		{
			Hana hn = new Hana();
			hn.getConfiguration(); // == Meta, should be consume()d by original object.
			// XXX: multiplier는 가져오십시오. 그럼 이만
			hn.setAmplifier(1).setMultiplier(1).setProb(idiom.getE(), idiom.getP());
			return hn; // TODO: more configuration?
		}
		else
		{
			// Not a base idiom. searching database.
			try
			{
				PreparedStatement stmt = db.prepareStatement("SELECT * FROM Newdex WHERE expression = ?");
				stmt.setString(1, word);

				ResultSet res = stmt.executeQuery();
				// 어휘의 중복은 아직 고려하지 않았다. 동음이의어 처리 불가능
				if (res.next())
				{

					Hana hn = new Hana();
					
					// 이번 행을 첫 정보로 확인하고, 고정되었는지 체크한다.
					if (res.getBoolean("locked"))
					{
						
						hn.getConfiguration(); // == Meta
						hn.setMultiplier(1).setAmplifier(1);
						hn.setProb(res.getDouble("eprob_locked"), res.getDouble("sprob_locked"));
						
					}
					// inferFromRecords()를 실행한다.
					// 만약 고정되어있지 않다면, 추론된 eprob과 sprob을 추가로 가져오고, 여기에서 출력을 결정한다. 배수는 항상 1이다.
					double[] inferred = inferFromRecords(res.getString("exprhash"));
					// 순서대로 amplifier, eprob_guessed, sprob_guessed 이다.
					hn.setAmplifier((int) inferred[0]);
					hn.setProb(inferred[0], inferred[1]);
					return hn;
				}
				else
				{
					// 데이터 베이스에 없는 어휘이다. 새로 추가한다.
					// addNewdex() 를 사용한다.
					addNewdex(word);
					return new Hana(word);
				}
			}
			catch (SQLException e)
			{
				P.e(TAG, "쿼리 도중 오류가 발생하였습니다.");
				e.printStackTrace();
				return null;
			}
		}
	}
	
	public double[] inferFromRecords(String expr_hash)
	{
		// inferFromRecords는 마지막 레코드 1000개만을 처리한다. LIMIT 1000
		try
		{
			PreparedStatement sql = db.prepareStatement("SELECT eprob, sprob, rate FROM Dexrecord WHERE exprhash = ? ORDER BY id LIMIT 1000");
			ResultSet res = sql.executeQuery();
			int num_records = 0;
			double eprob = 0.0;
			double sprob = 0.0;
			double et, st;
			// 정규분포를 위한 변수들
			double mean, variance;
			while (res.next())
			{
			// XXX: 공사 중
				num_records++;
				
			}
			
		}
		catch (SQLException e)
		{
			P.e(TAG, "데이터 추론 도중 오류가 발생했습니다. (SQL오류)");
			e.printStackTrace();
			return null;
		}
		return null;
	}
	
	public boolean addRecord(String exprhash, double eprob, double sprob, double rate, long timestamp)
	{
		return false; // stub
	}
	
	public boolean addNewdex(String word)
	{
		return false; // stub
	}
	
	public String getSenseStatDigest()
	{
		return null;
	}
	
}
