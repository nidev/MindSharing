/**
 * 
 */
package jnu.mindsharing.chainengine;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import jnu.mindsharing.chainengine.baseidioms.BaseIdioms;
import jnu.mindsharing.chainengine.baseidioms.Idiom;
import jnu.mindsharing.common.DatabaseConstants;
import jnu.mindsharing.common.ExprHash;
import jnu.mindsharing.common.HList;
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
	
	private class Pair<GivenType>
	{
		public GivenType first, second;
		
		public Pair(GivenType f, GivenType s)
		{
			first = f;
			second = s;
		}
	}
	

	/**
	 * 사전에 준비된 데이터베이스 연결이 있다면, 이를 사용해서 감정 데이터베이스에 접근한다.
	 * @param provided_db 사전에 준비된 데이터베이스 Connection
	 */
	public Sense()
	{
		P.d(TAG, "Sense Class created (With baseIdioms = 200)");
		bi = new BaseIdioms();
		try
		{
			bi.loadSet(BaseIdioms.DSC.SET200);
		}
		catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		} // 기본 어휘는 200개 세트를 사용한다.
		
		negotiateDatabaseConnection();
	}
	
	public Sense(int dsc_size)
	{
		boolean load_okay = false;
		
		P.d(TAG, "Sense Class created (With baseIdioms = %d)", dsc_size);
		bi = new BaseIdioms();
		
		try
		{
			switch(dsc_size)
			{
			case 20:
				load_okay |= bi.loadSet(BaseIdioms.DSC.SET20);
				if (load_okay) break;
			case 50:
				load_okay |= bi.loadSet(BaseIdioms.DSC.SET50);
				if (load_okay) break;
			case 100:
				load_okay |= bi.loadSet(BaseIdioms.DSC.SET100);
				if (load_okay) break;
			case 200:
				load_okay |= bi.loadSet(BaseIdioms.DSC.SET200);
				if (load_okay) break;
			default:
				P.e(TAG, "Couldn't identify dataset size. Load SET200");
				load_okay |= bi.loadSet(BaseIdioms.DSC.SET200);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
		negotiateDatabaseConnection();
	}
	
	/**
	 * 주소와 데이터베이스 이름을 사용해 데이터베이스 Connection을 가져온다. 예외가 발생하면 null 을 반환한다.
	 * @return 데이터베이스 Connection, 또는 null
	 */
	private Connection negotiateDatabaseConnection()
	{
		String uri = (new DB_URI()).toString();

		try
		{
			db = DriverManager.getConnection(uri, DB_URI.user, DB_URI.password);
			P.d(TAG, "데이터베이스에 연결되었습니다.");
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
			P.e(TAG, last_err_msg);
			return null;
		}
	}
	
	/**
	 * Sense 모듈은 객체 생성과 동시에 데이터베이스 커넥션을 하나 소모한다. 가비지 컬렉션이 이루어지기 전에는 닫히지 않는다.
	 * 따라서, 전역적으로 사용할 게 아니라면 명시적으로 닫는 함수를 사용해줘야한다.
	 * @return DB커넥션이 열려서 닫은 경우는 true, 이미 닫힌 경우에는 false
	 */
	public boolean closeExplicitly()
	{
		try
		{
			if (!db.isClosed())
			{
				db.close();
				return true;
			}
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
		return false;
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
					"CREATE TABLE IF NOT EXISTS Newdex ("
					+ "id SERIAL CONSTRAINT nexdex_PK PRIMARY KEY, "
					+ "expression TEXT NOT NULL, "
					+ "exprtype SMALLINT NOT NULL, "
					+ "locked BOOLEAN DEFAULT FALSE, "
					+ "eprob_lock NUMERIC DEFAULT 0.0, "
					+ "sprob_lock NUMERIC DEFAULT 0.0, "
					+ "exprhash TEXT NOT NULL);"
					);
			create_newdex_table.execute();
			// Machine Learning record
			PreparedStatement create_record_table = db.prepareStatement(
					"CREATE TABLE IF NOT EXISTS Dexrecord ("
					+ "id SERIAL CONSTRAINT dexrecord_PK PRIMARY KEY, "
					+ "exprhash TEXT NOT NULL, "
					+ "birthdate DATE NOT NULL, " // 입력일시
					+ "eprob NUMERIC DEFAULT 0.0, "
					+ "sprob NUMERIC DEFAULT 0.0);"
					);
			create_record_table.execute();
			// Statistics record
			// Every learning should be recorded
			PreparedStatement create_stat_table = db.prepareStatement(
					"CREATE TABLE IF NOT EXISTS Stats ("
					+ "id SERIAL CONSTRAINT stats_PK PRIMARY KEY, "
					+ "evtype SMALLINT NOT NULL, "
					+ "words_given SMALLINT NOT NULL, "
					+ "words_emotional SMALLINT NOT NULL, "
					+ "source_name TEXT NOT NULL);"
					);
			create_stat_table.execute();
			P.d(TAG, "데이터베이스 준비 완료");
			return true;
		}
		catch (SQLException e)
		{
			P.e(TAG, "데이터베이스 준비 실패");
			e.printStackTrace();
		}
		return false;
	}
	
	/**
	 * 기본 어휘에 존재하는지 확인한다.
	 * @param word 기본어휘
	 * @return 존재하는 경우 해당하는 BaseIdioms.Idiom 객체, 아니면 null
	 */
	public Idiom isBaseIdiom(String word)
	{
		for (Idiom idiom: bi.retIdioms())
		{
			if (idiom.toString().equals(word))
			{
				return idiom;
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
		//P.d(TAG, "Query Emotional/State information on (%s)", word);
		//P.d(TAG, "... Searching on BaseIdioms");
		Idiom idiom = isBaseIdiom(word);
		if (idiom != null)
		{
			//P.d(TAG, "조회 됨: " + idiom.toString());
			Hana hn = new Hana();
			hn.getConfiguration(); // == Meta, should be consume()d by original object.
			
			// 추론용 함수는 사용하지만, 빈도수에 영향을 미치기 위해서 레코드를 삽입한다.
			double[] inferred = inferFromRecords((new ExprHash(word)).toString());
			hn.setAmplifier((int)inferred[0]).setMultiplier(1).setProb(idiom.base_e()*1.0, idiom.base_s()*1.0);
			return hn; // TODO: more configuration?
		}
		else
		{
			//P.d(TAG, "... Inferring from Database");
			// Not a base idiom. searching database.
			try
			{
				PreparedStatement stmt = db.prepareStatement("SELECT * FROM Newdex WHERE expression = ?");
				stmt.setFetchSize(20000);
				stmt.setString(1, word);

				ResultSet res = stmt.executeQuery();
				// 어휘의 중복은 아직 고려하지 않았다. 동음이의어 처리 불가능
				if (res.next())
				{
					Hana hn = new Hana();
					
					// inferFromRecords()를 실행한다. 여기에서 출력의 세기와 추론된 확률을 수신한다.
					double[] inferred = inferFromRecords(res.getString("exprhash"));
					
					// 이번 행을 첫 정보로 확인하고, 고정되었는지 체크한다.
					if (res.getBoolean("locked"))
					{
						hn.getConfiguration(); // == Meta
						hn.setProb(res.getDouble("eprob_locked"), res.getDouble("sprob_locked"));
					}
					else
					{
						// 고정되어있지 않으므로 추론된 값을 사용한다.
						hn.setProb(inferred[1], inferred[2]);
					}

					hn.setAmplifier((int) inferred[0]);
					
					// 현재 커서를 닫는다
					res.close();
					return hn;
				}
				else
				{
					// 데이터 베이스에 없는 어휘이다. 새로 추가한다.
					// addNewdex() 를 사용한다.
					P.d(TAG, "Found new word : " + word);
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
	
	/**
	 * 데이터베이스 레코드로부터 eprob과 sprob을 추정하고, 출현 빈도수(amplifier)를 내놓는다.
	 * @param expr_hash UTF-8로 작성된 단어의 sha-1 해시값
	 * @return double 배열, 0번은 출현 빈도수(amplifier), 1번은 eprob 추정값, 2번은 sprob 추정값
	 */
	public double[] inferFromRecords(String expr_hash)
	{
		// inferFromRecords는 마지막 레코드 1000개만을 처리한다. LIMIT 1000
		try
		{
			PreparedStatement sql = db.prepareStatement("SELECT eprob, sprob FROM Dexrecord WHERE exprhash = ? ORDER BY id LIMIT 1000");
			sql.setFetchSize(20000);
			sql.setString(1,  expr_hash);
			ResultSet res = sql.executeQuery();
			ArrayList<Pair<Double>> pair_probs = new ArrayList<Pair<Double>>();			
			double[] final_output = {0, 0, 0};
			
			
			while (res.next())
			{
				Pair<Double> v = new Pair<Double>(res.getDouble("eprob"), res.getDouble("sprob"));
				pair_probs.add(v);
				final_output[0] += 1;
			}
			res.close();

			
			// 레코드에 들어온 결과들을 바탕으로 정규분포 값을 구한다.
			double[] ndist = normalDistributionOnProbs(pair_probs);
			
			// TODO:
			// 1*v = 68.3% / 2*v = 95.5% / 3*v = 99.7%
			int[] count_pairs = countPairsInRangeOf(pair_probs,
					new Pair<Double>(ndist[0]-ndist[1], ndist[2] - ndist[3]),
					new Pair<Double>(ndist[0]+ndist[1], ndist[2] + ndist[3])); 
			if (count_pairs[0] >= ((int) pair_probs.size()* 0.68) && count_pairs[1] >= ((int) pair_probs.size()* 0.68))
			{
				// 만약 1*v에서 만족한다면, 평균치를 그대로 출력한다.
				final_output[0] = pair_probs.size();
				final_output[1] = ndist[0]; 
				final_output[2] = ndist[2];
			}
			else
			{
				// 정규분포를 만족하지 못하는 경우, 표준편차를 빼서 보정된 값을 출력한다.
				final_output[0] = pair_probs.size();
				final_output[1] = ndist[0] - ndist[1]; 
				final_output[2] = ndist[2] - ndist[3];
			}			
			return final_output;
			
			
		}
		catch (SQLException e)
		{
			P.e(TAG, "데이터 추론 도중 오류가 발생했습니다. (SQL오류)");
			e.printStackTrace();
			return null;
		}
	}
	/**
	 * Pair<Double> 가 들어있는 배열을 받아서, 주어진 범위의 원소 갯수를 카운트한다.
	 * @return int배열, 0번은 eprob에 대한 카운트 1번은 sprob에 대한 카운트
	 */
	public int[] countPairsInRangeOf(ArrayList<Pair<Double>> pair_probs, Pair<Double> from, Pair<Double> to)
	{
		int[] counts = {0, 0};
		for (Pair<Double> p: pair_probs)
		{
			if (p.first >= from.first && p.first <= to.first)
				counts[0] += 1;
			if (p.second >= from.second && p.second <= to.second)
				counts[1] += 1;
		}
		return counts;
	}
	
	/**
	 * Pair<Double> 가 들어있는 배열을 받아서 정규분포를 계산한다. 반환하는 순서는 return 을 참고
	 * @param pair_probs Pair<Double> 가 들어있는 배열 
	 * @return double 배열. 순서는 감정확률, 감정확률 표준 편차, 상태변이확률, 상태변이확률 표준 편차
	 */
	public double[] normalDistributionOnProbs(ArrayList<Pair<Double>> pair_probs)
	{
		// 정규분포를 위한 변수들(평균, 표준편차)
		double emean = 0, smean = 0, e_stddevi = 0, s_stddevi = 0;
		double sum_eprob = 0.0, sum_sprob = 0.0;
		double[] final_output = {0.0, 0.0, 0.0, 0.0};
		
		int num_records = pair_probs.size();
		
		for (Pair<Double> p: pair_probs)
		{
			sum_eprob += p.first;
			sum_sprob += p.second;
		}
		
		
		if (num_records == 0)
		{
			// Zero-division이 일어날 수도 있음
			return final_output;
		}
		
		emean = sum_eprob / num_records;
		smean = sum_sprob / num_records;
		
		// 표준편차 계산
		sum_eprob = sum_sprob = 0.0;
		for (Pair<Double> p: pair_probs)
		{
			sum_eprob += Math.pow(p.first - emean, 2);
			sum_sprob += Math.pow(p.second - smean, 2);
		}
		e_stddevi = Math.pow(sum_eprob / num_records, 0.5);
		s_stddevi = Math.pow(sum_sprob / num_records, 0.5);
		
		final_output[0] = emean;
		final_output[1] = e_stddevi;
		final_output[2] = smean;
		final_output[3] = s_stddevi;
		return final_output;
	}
	
	public boolean addRecord(String exprhash, double eprob, double sprob, long timestamp_milli)
	{
		try
		{
			PreparedStatement sql = db.prepareStatement("INSERT INTO Dexrecord (exprhash, birthdate, eprob, sprob) VALUES (?, ?, ?, ?);");
			sql.setString(1, exprhash);
			sql.setDate(2,  new java.sql.Date(timestamp_milli));
			sql.setDouble(3, eprob);
			sql.setDouble(4, sprob);
			
			
			sql.execute();
			sql.close();
		}
		catch (SQLException e)
		{
			// XXX: Please implement here
			e.printStackTrace();
		}
		return false; // stub
	}
	
	@Deprecated
	public boolean setLockedProbs(String exprhash, double eprob_locked, double sprob_locked)
	{
		/*
		 * Not implemented
		 */
		return false; // stub
	}
	
	public boolean addNewdex(String word)
	{
		// XXX: 트랜잭션 문제 있음
		// 데이터베이스 락킹, 또는 유니크 옵션을 해시 속성에 추가할 것
		try
		{
			PreparedStatement sql = db.prepareStatement("INSERT INTO Newdex (expression, exprtype, locked, eprob_lock, sprob_lock, exprhash) VALUES (?, ?, ?, ?, ?, ?);");
			sql.setString(1, word);
			sql.setInt(2, WORD_TYPE.verb);
			sql.setBoolean(3, false);
			sql.setDouble(4, 0.0);
			sql.setDouble(5, 0.0);
			sql.setString(6, (new ExprHash(word).toString()));
			
			sql.execute();
			sql.close();
		}
		catch (SQLException e)
		{
			// XXX: Please implement here
			e.printStackTrace();
		}
		return false;
	}
	
	/**
	 * 데이터베이스에 학습된 어휘들을 모두 수신하여 추정 감정값과 세기를 함께 반환해준다.
	 * @return HList 배열
	 */
	public HList genearteNewdexMap()
	{
		try
		{
			// 기본 어휘도 여기에서 추가하자. 함께 보여져야지.
			ArrayList<Pair<String>> dexes = new ArrayList<Pair<String>>();
			for (Idiom idiom: bi.retIdioms())
			{
				dexes.add(new Pair<String>(idiom.toString(), idiom.toString()));
			}
			PreparedStatement sql = db.prepareStatement("SELECT expression, exprhash FROM Newdex;");
			sql.setFetchSize(20000);
			
			ResultSet res = sql.executeQuery();
			while (res.next())
			{
				dexes.add(new Pair<String>(res.getString("expression"), res.getString("exprhash")));
			}
			
			if (dexes.size() == 0)
			{
				return new HList();
			}
			else
			{
				HList finalResults = new HList();
				for (Pair<String> expr_exprhash_pair: dexes)
				{
					Hana hn = ask(expr_exprhash_pair.first);
					if (hn != null)
						finalResults.add(new Hana(expr_exprhash_pair.first).merge(hn));
				}
				return finalResults;
				
			}
			
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
		return new HList();
	}
	
	/**
	 * 데이터베이스에 학습된 어휘들을 모두 수신하여 추정 감정값과 세기를 문자열로 요약하여 반환한다.
	 * @param newdexMap generateNewdexMap에서 반환된 HList 객체
	 * @return 요약문, 만약 쿼리에 실패했다면 null
	 */
	public String generateNewdexTableDigest(HList newdexMap)
	{
		StringBuffer sb = new StringBuffer();
		SimpleDateFormat sdf = new SimpleDateFormat();
		sb.append("Learning Record Digest\r\n");
		sb.append("Created at " + sdf.format(new Date()) + "\r\n");
		sb.append("Total words on given table: " + newdexMap.size() + "\r\n");
		sb.append("===================================================\r\n");
		sb.append("|Emotional|State    |Amplifier| String            |\r\n");
		sb.append("===================================================\r\n");
		
					
		if (newdexMap.size() > 0)
		{
			for (Hana hn: newdexMap)
			{
				if (hn != null)
					sb.append(String.format("|%+1.6f|%+1.6f|%9d/ %s\r\n", hn.getProb()[0], hn.getProb()[1], hn.getAmplifier(), hn.toString()));
			}
		}
		return sb.toString();
	}
}
