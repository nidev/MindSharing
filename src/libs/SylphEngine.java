package libs;

import java.io.DataInputStream;
import java.util.ArrayList;

import libs.fragments.ContextFragment;
import libs.fragments.SentenceFragment;
import libs.fragments.WordFragment;

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

	public SylphEngine()
	{
	}
	
	public static void initEngine()
	{
		ELog.d(TAG, "엔진 점검 중");
		ELog.d(TAG, "엔진 버전 " + ENGINE_VERSION_MAJOR + "." + ENGINE_VERSION_MINOR);
		ELog.d(TAG, "엔진 점검 완료");
	}
	
	public ContextFragment analyze(String sourceText)
	{
		ContextFragment fctx = new ContextFragment();
		
		ArrayList<String> sentences = PhraseSplit.split(sourceText); // XXX: 소스 작업 중
		
		// 1단계 분해 작업
		SentenceFragment[] fstcs = new SentenceFragment[sentences.size()];
		for (String sentence: sentences)
		{
			// 문장 프래그먼트 저장 안됨
			ArrayList<String> words = SentenceSplit.split(sentence);
			for (String word: words)
			{
				// 단어 프래그먼트 저장 안됨
				ArrayList<String> units = UnitSplit.split(word); // XXX: 소스 작업 중
				for (String unit: units)
				{
					// 단어 프래그먼트에 형태소 저장안됨
				}
				
			}
			
		}
		
		// 2단계 사전을 활용한 분석 작업
		// 1. 단어 레벨까지 내려가 값을 탐색함
		// 2. 문장 레벨로 올라와 전체 값을 계산함
		// 3. 문단 레벨로 올라와 전체 값을 계산함
		// 4. ContextFragment 내용 갱신
		
		// 
		return fctx;
	}
	
	public ContextFragment analyze(DataInputStream source)
	{
		return null;
	}
}
