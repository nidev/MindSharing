/**
 * 
 */
package jnu.mindsharing.hq;

import jnu.mindsharing.chainengine.ChainEngine;
import jnu.mindsharing.chainengine.Sense;
import jnu.mindsharing.common.ExprHash;
import jnu.mindsharing.common.Hana;
import jnu.mindsharing.common.P;

import org.restlet.resource.ServerResource;

/**
 * Boostrap은 엔진과 API 서버를 동시에 가동하기위한 편리한 클래스이다. 엔진 가동과 데이터베이스 연결, API 서버 가동을 담당한다.
 * 
 * @author nidev
 *
 */
public class Bootstrap extends ServerResource
{
	static String TAG = "CEBoot";
	enum RUN_MODE {NORMAL, SENSE_TEST};

	/**
	 * 서버와 메시지 로거를 가동한다. 메시지는 기본적으로 표준 출력을 통해 출력된다.
	 * @param args 명령행 옵션들(사용되지 않음)
	 * @return none
	 */
	public static void main(String[] args)
	{
		RUN_MODE mode = RUN_MODE.NORMAL;
		
		P.d(TAG, "엔진 가동. 버전 정보를 확인합니다.");
		
		ChainEngine chainEngine = new ChainEngine();
		RESTServer restServer = new RESTServer(); // TODO: passing chainEngine
		
		P.d(TAG, "API 서버 버전\t= %d (%s)", restServer.getVersionNumber(), restServer.getVersionCode());
		P.d(TAG, "내부 라이브러리: %s", restServer.getLicenseInfo());
		P.d(TAG, "체인 엔진 버전\t= %d (%s)", chainEngine.getVersionNumber(), chainEngine.getVersionCode());
		P.d(TAG, "내부 라이브러리: %s", chainEngine.getLicenseInfo());
		P.d(TAG, "2D 차트 라이브러리\t= JFreeChart (LGPL 라이센스를 따릅니다.)");
		P.b();
		P.d(TAG, "메모리 부족으로 프로그램이 종료될 경우, -xm512M 옵션을 추가하여 재가동하십시오.");
		P.d(TAG, "사전 로딩 및 데이터베이스 연결 작업을 수행합니다.");

		switch(mode)
		{
		case NORMAL:
			P.d(TAG, "API 서버 시작");
			try
			{
				//으어아으 나중에
				chainEngine.createKKMAAnalyzer();
				restServer.run(chainEngine);
			}
			catch (Exception e)
			{
				P.b();
				P.e(TAG, "API 서버의 서브릿 시작 도중 오류가 발생하였습니다.");
				P.e(TAG, "이 오류는 API 서버 오류이거나 체인 엔진 내부의 오류일 수 있습니다. 자세한 내용은 아래의 Traceback을 확인해주십시오.");
				e.printStackTrace();
				P.e(TAG, "Exception class: %s,  Exception message: %s", e.toString(), e.getMessage());
				P.e(TAG, "이 오류는 로그 시스템에 기록되었습니다.");
			}
			break;
		case SENSE_TEST:
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
			break;
		default:
			;
		}
		
	}
}
