package jnu.mindsharing.chainengine;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;

import jnu.mindsharing.common.ApplicationInfo;
import jnu.mindsharing.common.ESentence;
import jnu.mindsharing.common.P;
import jnu.mindsharing.legacy.libs.PhraseSplit;

import org.snu.ids.ha.ma.MorphemeAnalyzer;

public class ChainEngine implements ApplicationInfo
{
	final String versionCode = "chronicle";
	final String dbURI = "mindsharing_db:mysql://localhost/mindsharing";
	final int versionNumber = 1;
	private MorphemeAnalyzer kkmaMA;
	private Connection mindsharing_db;
	
	private String TAG = "Engine";

	public ChainEngine()
	{
		kkmaMA = null;
		mindsharing_db = null;
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
		return "세종 꼬꼬마 형태소 분석기(http://kkma.snu.ac.kr/) / JSON.simple(http://code.google.com/p/json-simple/) JAR를 사용하였습니다.";
	}
	
	public boolean connectMysql()
	{
		P.d(TAG, "데이터베이스 연결 중입니다. (%s)", dbURI);
		try
		{
			mindsharing_db = DriverManager.getConnection(dbURI, "mindsharing", "mindsharing");
			P.d(TAG, "데이터베이스를 확인하였습니다. 종료 전까지 연결이 유지됩니다.");
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
	
	public ArrayList<String> splitIntoSentences(String source)
	{
		return PhraseSplit.split(source);
	}
	
	public ResultProcessor analyze(String source_paragraph)
	{
		if (kkmaMA == null || mindsharing_db == null)
		{
			P.e(TAG, "엔진이 준비되어있지 않습니다. 분석 루틴을 호출할 수 없습니다.");
			return null;
		}
		// TODO: 사전 정제작업?
		// purifier.rb 의 자바 버전!
		// 문장 단위 전처리 작업 시작
		EmotionAlgorithm eprocess = new EmotionAlgorithm(kkmaMA, mindsharing_db);
		
		try
		{
			P.d(TAG, "알고리즘에 필요한 문장 정보를 제공하는 중입니다.");
			// 나중에 문단 수가 늘어나면 for 문으로 확장할 것.
			int current_para = 0; 
			current_para = eprocess.createNewParagraph(source_paragraph);
			for (String sentence: splitIntoSentences(source_paragraph))
			{
				eprocess.addESentenceTo(current_para, new ESentence(sentence));
			}
			
			eprocess.processAll();
			
			return eprocess.extractResultProcessor(eprocess.get(current_para));
			
			/*
			Timer timer = new Timer();
			timer.start();
			List<MExpression> ret = kkmaMA.analyze(source);
			timer.stop();
			timer.printMsg("분해작업 소요시간");
			P.d(TAG, "형태소를 정제합니다.");
			*/
			//kkmaMA.closeLogger();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}
}
