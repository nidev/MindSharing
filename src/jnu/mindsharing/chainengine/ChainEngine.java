package jnu.mindsharing.chainengine;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import javax.sql.rowset.JdbcRowSet;

import org.snu.ids.ha.ma.MorphemeAnalyzer;

import jnu.mindsharing.utility.ApplicationInfo;
import jnu.mindsharing.utility.P;

public class ChainEngine implements ApplicationInfo
{
	final String versionCode = "chronicle";
	final String dbURI = "jdbc:mysql://localhost/mindsharing";
	final int versionNumber = 1;
	private MorphemeAnalyzer kkmaMA;
	private Connection jdbc;
	
	private String TAG = "Engine";

	public ChainEngine()
	{
		kkmaMA = null;
		jdbc = null;
	}
	
	@Override
	public String getVersionCode()
	{
		return versionCode;
	}

	@Override
	public int getVersionNumber()
	{
		return versionNumber;
	}

	@Override
	public String getLicenseInfo()
	{
		return "ChainEngine.java utilizes 세종 꼬꼬마 형태소 분석기(http://kkma.snu.ac.kr/), including JAR file into the engine.";
	}
	
	public boolean connectMysql()
	{
		P.d(TAG, "데이터베이스 연결 중입니다. (%s)", dbURI);
		try
		{
			jdbc = DriverManager.getConnection(dbURI, "mindsharing", "mindsharing");
			P.d(TAG, "데이터베이스에 연결되었습니다.");
			return true;
		}
		catch (SQLException e)
		{
			P.e(TAG, "데이터베이스 연결에 실패했습니다. 아래는 오류 메시지입니다.");
			e.printStackTrace();
			P.e(TAG, "Exception class: %s,  Exception message: %s", e.toString(), e.getMessage());
			P.e(TAG, "이 오류는 로그 시스템에 기록되었습니다.");
			return false;
		}
	}
	
	public void createKKMAAnalyzer()
	{
		// 최대 1분 가량 시간이 사전 로딩에 사용됨.
		kkmaMA = new MorphemeAnalyzer();
		kkmaMA.createLogger(null); // 형태소 분석기의 출력을 표준 출력으로 만듦
	}

}
