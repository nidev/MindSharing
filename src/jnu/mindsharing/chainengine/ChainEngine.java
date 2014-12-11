package jnu.mindsharing.chainengine;

import java.util.ArrayList;

import jnu.mindsharing.common.ApplicationInfo;
import jnu.mindsharing.common.HList;
import jnu.mindsharing.common.P;

import org.snu.ids.ha.ma.MorphemeAnalyzer;

/**
 * 한국어 텍스트 분해 및 분석을 하는 ChainEngine의 메인 클래스이다. 꼬꼬마 형태소 분석기를 내부에서 사용하고, HistoriaModule을 통해 학습을 한다.
 * 
 * 
 * @author nidev
 * @see HistoriaModule
 */
public class ChainEngine implements ApplicationInfo
{
	final String versionCode = "chronicle";
	final int versionNumber = 1;
	private MorphemeAnalyzer kkmaMA; // 형태소 분석기
	
	private String TAG = "Engine";

	/**
	 * 꼬꼬마 분석기 객체를 null 로 초기화하고, 학습 모듈을 준비한다.
	 */
	public ChainEngine()
	{
		kkmaMA = null;
	}
	
	/**
	 * 엔진 버전 코드를 반환한다.
	 * @return 엔진 버전 코드
	 */
	@Override
	public String getVersionCode()
	{
		return versionCode;
	}

	/**
	 * 엔진 버전 넘버를 반환한다.
	 * @return 엔진 버전 넘버
	 */
	@Override
	public int getVersionNumber()
	{
		return versionNumber;
	}

	/**
	 * 엔진 라이브러리 저작권 정보를 반환한다.
	 * @return 라이브러리 저작권 텍스트
	 */
	@Override
	public String getLicenseInfo()
	{
		return "세종 꼬꼬마 형태소 분석기(http://kkma.snu.ac.kr/) / JSON.simple(http://code.google.com/p/json-simple/) JAR를 사용하였습니다.";
	}
	
	/**
	 * 꼬꼬마 형태서 분석기를 생성한다. (외부에서 별도로 생성하지말 것)
	 */
	public void createKKMAAnalyzer()
	{
		// 최대 1분 가량 시간이 사전 로딩에 사용됨.
		kkmaMA = new MorphemeAnalyzer();
		kkmaMA.createLogger(null); // 형태소 분석기의 출력을 표준 출력으로 만듦
	}
	
	/**
	 * 주어진 텍스트를 분석하고, 그 결과가 담긴 ResultProcess를 내놓는다.
	 * @param source_paragraph 입력 텍스트
	 * @return JSON과 TXT 포맷으로 결과를 얻을 수 있는 ResultProcessor 객체
	 */
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
		EmotionAlgorithm eprocess = new EmotionAlgorithm(kkmaMA);
		try
		{
			P.d(TAG, "알고리즘에 필요한 문장 정보를 제공하는 중입니다.");
			
			ArrayList<HList> result = eprocess.feed(source_paragraph);
			return EmotionAlgorithm.extractResultProcessor(result);
			
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}
}
