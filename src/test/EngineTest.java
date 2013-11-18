package test;

import libs.ELog;
import libs.SylphEngine;
import libs.fragments.BaseFragment;
import libs.fragments.ContextFragment;

/*
 * 주석 꼼꼼히 달기:
 * 
 * 변수에 주석을 달 때는 변수 뒤에 달아주기
 * ex) int emotion_value = 0; // 감정 변수
 * 
 * 함수에 주석을 달때는 함수 선언문 위에, / * * /를 사용해서 달아주기
 * ex)
 * 
 */
public class EngineTest
{
	static String TAG = "EngineTest";
	static String EXAMPLE_TEXT = "하늘은 맑고 푸르다. 오늘 나의 기분은 매우 좋다. 나의 하루가 상쾌한 하루가 될 것 같다. 나는 긍정의 힘을 믿는다.";

	public EngineTest()
	{
		// TODO Auto-generated constructor stub
	}
	
	public static void recursiveSelfPrintInfo(BaseFragment base)
	{
		while (base.isNextFragmentOK())
		{
			BaseFragment next = base.getNextFragments();
			next.selfPrintInfo();
			if (next.isNextFragmentOK())
			{
				recursiveSelfPrintInfo(next);
			}
		}
	}
	
	public static void recursiveSelfPrintInfo(ContextFragment ctx)
	{
		recursiveSelfPrintInfo((BaseFragment) ctx);
	}

	public static void main(String[] args)
	{
		/*
		 * 엔진 실행코드 예제
		 * 
		 * 1. new SylphEngine() 으로 엔진 객체 받아옴
		 * 2. analyze() 메소드로 분석 결과(ContextFragment) 받아옴
		 * 3. 분석 결과 사용
		 */
		ELog.d(TAG, "엔진 테스트를 시작합니다.");
		SylphEngine engine = new SylphEngine();
		SylphEngine.initEngine();
		ELog.d(TAG, "엔진 객체 생성 완료");
		ContextFragment result = engine.analyze(EXAMPLE_TEXT);
		
		ELog.d(TAG, "테스트 종료");
	}

}
