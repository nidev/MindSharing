package jnu.mindsharing.chainengine;

import java.util.ArrayList;

import jnu.mindsharing.common.ApplicationInfo;
import jnu.mindsharing.common.ESentence;
import jnu.mindsharing.common.P;
import jnu.mindsharing.legacy.libs.PhraseSplit;

import org.snu.ids.ha.ma.MorphemeAnalyzer;

public class ChainEngine implements ApplicationInfo
{
	final String versionCode = "chronicle";
	final int versionNumber = 1;
	private MorphemeAnalyzer kkmaMA; // 형태소 분석기
	private HistoriaModule mlearn;  // 기계학습 모듈
	
	private String TAG = "Engine";

	public ChainEngine()
	{
		kkmaMA = null;
		mlearn = new HistoriaModule();
		mlearn.printDigest();
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
		if (kkmaMA == null)
		{
			P.e(TAG, "엔진이 준비되어있지 않습니다. 분석 루틴을 호출할 수 없습니다.");
			return null;
		}
		// TODO: 사전 정제작업?
		// purifier.rb 의 자바 버전!
		// 문장 단위 전처리 작업 시작
		EmotionAlgorithm eprocess = new EmotionAlgorithm(kkmaMA, mlearn);
		eprocess.clear();
		
		try
		{
			P.d(TAG, "알고리즘에 필요한 문장 정보를 제공하는 중입니다.");
			// 나중에 문단 수가 늘어나면 for 문으로 확장할 것.
			int current_para; 
			current_para = eprocess.createNewParagraph(source_paragraph);
			for (String sentence: splitIntoSentences(source_paragraph))
			{
				eprocess.addESentenceTo(current_para, new ESentence(sentence));
			}
			
			eprocess.processAll();
			
			return eprocess.extractResultProcessor();
			
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}
	
	public String getHistoriaModuleDigest()
	{
		return mlearn.digest();
	}
}
