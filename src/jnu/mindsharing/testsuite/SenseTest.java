package jnu.mindsharing.testsuite;

import jnu.mindsharing.chainengine.Sense;
import jnu.mindsharing.common.ExprHash;
import jnu.mindsharing.common.Hana;
import jnu.mindsharing.common.P;

/**
 * Sense 학습 모듈을 테스트하기 위한 테스트 패키지.
 * java -jar mindsharing.jar jnu.mindsharing.testsuite.SenseTest 로 커맨드라인에서 호출 가능
 * 
 * 특별한 결과물은 없으며, 표준 출력으로 완료 메시지를 출력합니다.
 * @author nidev
 *
 */
public class SenseTest
{
	static String TAG = "SenseTest";

	public static void main(String[] args)
	{
		P.d(TAG, "감정값 평가 및 학습 모듈 Sense를 테스트합니다.");
		Sense ss = new Sense(20);
		ss.sanitizeTableStructure();
		ss.addNewdex("테스트");
		ss.addRecord((new ExprHash("테스트")).toString(), 0.0, 0.0, System.currentTimeMillis());
		Hana test_res = ss.ask("테스트");
		if (test_res != null)
		{
			P.d(TAG, "감정값 수신 완료");
		}
		else
		{
			P.d(TAG, "테스트 결과 수신 실패");
		}
	}

}
