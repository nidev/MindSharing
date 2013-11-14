package libs;

import java.io.DataInputStream;
import java.util.ArrayList;
import java.util.Arrays;

import libs.fragments.BaseFragment;
import libs.fragments.ContextFragment;

/*
 * 이 파일은 코드가 돌아가지 않더라고 큰 걱정 안해도 괜찮음.
 */

/**
 * 분석 엔진 '실프' 스케치
 * ----
 * 클래스 ContextFragment: '문단' 분석 결과 반환에 사용
 * 함수 long .getElapsed() (BaseFragment에 없음)
 * -분석에 소요된 시간 반환(밀리초)
 * 함수 String .getSolutionInHtml() (BaseFragment에 없음)
 * -분석 과정과 결과를 html 형식 스트링으로 반환
 * 
 * 
 * 함수 static void .initEngine()
 * -엔진처리에 필요한 DB파일과 메모리 상태 점검
 * 함수 ContextFragment analyze(String text, long max_time)
 * -분석 시작 함수. 반드시 new SylphEngine().analyze() 으로 실행
 * ----
 * 
 * @author nidev
 *
 */
public class SylphEngine
{
	final static String TAG = "Sylph";
	final static boolean USE_THREAD = true; // 쓰레드 사용 여부
	final static int MAX_THREADS = 20; // 최대 쓰레드 갯수
	final static int MAX_SUB_FRAGMENTS = 50; // 한 프래그먼트가 가질 수 있는 작은 프래그먼트 갯수
	final static int ENGINE_VERSION_MAJOR = 0; // 엔진 버전 xx.yyy 중 xx
	final static int ENGINE_VERSION_MINOR = 1; // 엔진 버전 xx.yyy 중 yyy
	final static String DATABASE_FOLDER = "./database/"; // 데이터 베이스 폴더
	
	final static int FILL_SENTENCE_LEVEL = 1; // fillBaseFragment에서 나누는 수준을 설정: 문장 레벨
	final static int FILL_WORD_LEVEL = 2; // fillBaseFragment에서 나누는 수준을 설정: 단어 레벨
	final static int FILL_UNIT_LEVEL = 4; // // fillBaseFragment에서 나누는 수준을 설정: 형태소 레벨

	public SylphEngine()
	{
	}
	
	public static void initEngine()
	{
		ELog.d(TAG, "엔진 점검 중");
		ELog.d(TAG, "엔진 버전 " + ENGINE_VERSION_MAJOR + "." + ENGINE_VERSION_MINOR);
		ELog.d(TAG, "엔진 점검 완료");
	}
	
	public BaseFragment fillBaseFragment(BaseFragment bft, int level) throws IllegalArgumentException
	{
		switch (level)
		{
		case FILL_SENTENCE_LEVEL:
			for (String slice: bft.slicedText)
			{
				
				ArrayList<String> array = SentenceSplit.split(slice);
				for (String key: array)
				{
					// ELog.d(TAG, key);
					BaseFragment word_fragment = new BaseFragment(key);
					
					bft.fragments.add(fillBaseFragment(word_fragment, FILL_WORD_LEVEL));
					// bft.selfPrintInfo();
					// ELog.e(TAG, "---------------------------------------");
				}
				
			}
			break;
		case FILL_WORD_LEVEL:
			// 지금은 사용하지 않음
			for (String slice: bft.slicedText)
			{
				/*
				 진짜로 사용할 코드
				ArrayList<String> array = UnitSplit.split(slice);
				for (String key: array)
				{
					BaseFragment word_fragment = new BaseFragment();
					bft.fragments.add(fillBaseFragment(word_fragment, FILL_UNIT_LEVEL));
				}
				*/
				// ELog.d(TAG, slice);
				// 테스트용 코드
				BaseFragment unit_fragment = new BaseFragment("형태소+"+slice);
				bft.fragments.add(unit_fragment);
			}
			break;
		case FILL_UNIT_LEVEL:
			// 형태소 분해된 단어에 각각 감정값을 0으로 채워주고 루프 종료
			break;
		default:
			throw new IllegalArgumentException("Unacceptable Level");
		}
		return bft;
	}
	
	public ContextFragment fillContextFragment(ContextFragment fctx) throws IllegalArgumentException
	{
		ELog.d(TAG, "문장을 나누고, 클래스로 입력하는 작업을 수행합니다.");
		
		if (fctx.sourceText.length() == 0)
		{
			throw new IllegalArgumentException();
		}
		
		try
		{
			// 문단 분해
			for (String slice: fctx.slicedText)
			{
				BaseFragment fragment = new BaseFragment(slice);
				fctx.fragments.add(fillBaseFragment(fragment, FILL_SENTENCE_LEVEL));
				
			}
			fctx.setReadyToUse(); // 컨텍스트 준비 완료 플래그 설정
		}
		catch (Exception e)
		{
			ELog.e(TAG, "문장 분해 도중 오류가 발생했습니다.");
			e.printStackTrace();
		}
		return fctx;
		
	}
	
	public ContextFragment analyze(String sourceText)
	{
		ELog.d(TAG, "텍스트 분석을 시작합니다.");
		
		ContextFragment fctx = new ContextFragment(sourceText, PhraseSplit.split(sourceText));

		fctx.setStartTimeOnBuild();
		
		fillContextFragment(fctx);
		
		fctx.setEndTimeOnBuild();
		if (fctx.isReadyToUse())
		{
			fctx.setStartTimeOnAnalysis();
			// 2단계 사전을 활용한 분석 작업
			// 1. 형태소 레벨로 내려가 값을 산출함
			// 2. 단어 레벨의 전체 값을 계산함
			// 3. 문장 레벨의 전체 값을 계산함
			// 4. 문단 레벨의 전체 값을 계산함
			// 5. ContextFragment 내용 갱신(작업 종료시각, 사용가능성 여부 등등)
			
			/*
			 * 감정값 계산 미구현 상태
			 */
			fctx.setEndTimeOnAnalysis();
			return fctx;
		}
		else
		{
			ELog.e(TAG, "분석 도중 오류가 발생한 것 같습니다. ELog를 확인해부세요.");
			return null; // 분석 결과 도중 오류가 발생한 경우에는 null 리턴. ELog 확인 바람
		}
		
	}
	
	public ContextFragment analyze(DataInputStream source)
	{
		// 파일에서 문자열 읽어온 다음, analyze(sourceText) 호출해주면 됨!
		return null;
	}
}
