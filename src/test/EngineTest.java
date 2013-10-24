package test;

import libs.ELog;
import libs.SylphEngine;
import libs.fragments.BaseFragment;
import libs.fragments.ContextFragment;

public class EngineTest
{
	static String TAG = "EngineTest";

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
		ContextFragment res = engine.analyze("test");
		// res.selfPrintInfo();
		for (BaseFragment base: res.getFragments())
		{
			recursiveSelfPrintInfo(base);
			/*
			base.selfPrintInfo();
			ELog.e(TAG, "TEST");
			for (BaseFragment next: base.getFragments())
			{
				//ELog.e(TAG, "TEST");
				//next.selfPrintInfo();
			}
			*/
		}
		ELog.d(TAG, "테스트 종료");
	}

}
