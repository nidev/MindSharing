package jnu.mindsharing.chainengine;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;

import jnu.mindsharing.utility.ApplicationInfo;
import jnu.mindsharing.utility.P;

import org.snu.ids.ha.ma.MExpression;
import org.snu.ids.ha.ma.MorphemeAnalyzer;
import org.snu.ids.ha.ma.Sentence;
import org.snu.ids.ha.util.Timer;

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
			P.d(TAG, "데이터베이스를 확인하였습니다.");
			jdbc.close();
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
	
	public CEResultObject analyze(String source)
	{
		if (kkmaMA == null || jdbc == null)
		{
			P.e(TAG, "엔진이 준비되어있지 않습니다. 분석 루틴을 호출할 수 없습니다.");
			return null;
		}
		else
		{
			// TODO: 사전 정제작업?
			// purifier.rb 의 자바 버전!
			P.d(TAG, "형태소 분해 작업을 시행합니다.");
			try
			{
				Timer timer = new Timer();
				timer.start();
				List<MExpression> ret = kkmaMA.analyze(source);
				timer.stop();
				timer.printMsg("분해작업 소요시간");
				
				P.d(TAG, "형태소를 정제합니다.");

				ret = kkmaMA.postProcess(ret);
				ret = kkmaMA.leaveJustBest(ret);

				List<Sentence> snts = kkmaMA.divideToSentences(ret);
				
				// 감정 처리 알고리즘을 담은 클래스 생성
				EmotionAlgorithm eabase = new EmotionAlgorithm();
				for(int i=0; i < snts.size(); i++)
				{
					Sentence snt = snts.get(i);
					P.b();
					P.d(TAG, "[%d/%d] 현재 문장: %s", i, snts.size(), snt.getSentence());
					EmotionAlgorithm easub = new EmotionAlgorithm(eabase);
					easub.listen(snt);
					if (easub.currentEmotionRate() == 0)
					{
						// 변화가 발생하지 않음.
						P.d(TAG, " => 이 문장은 사용하지 않습니다.");
					}
					{
						// 변화 발생시, 새로 학습한 내용을 받아들임.
						eabase = easub;
					}
				}
				
				return eabase.toCEResultObject();
				//kkmaMA.closeLogger();
			}
			catch (Exception e)
			{
				e.printStackTrace();
				return null;
			}
		}
	}

}
