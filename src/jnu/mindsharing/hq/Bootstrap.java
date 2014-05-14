/**
 * 
 */
package jnu.mindsharing.hq;

import jnu.mindsharing.chainengine.ChainEngine;
import jnu.mindsharing.chainengine.EQueryTool;
import jnu.mindsharing.common.EQueryConstants;
import jnu.mindsharing.common.P;

import org.restlet.resource.ServerResource;

/**
 * @author nidev
 *
 */
public class Bootstrap extends ServerResource
{
	static String TAG = "CEBoot";

	/**
	 * @args Set of arguments for running from CLI, given by a user.
	 */
	public static void main(String[] args)
	{
		P.d(TAG, "엔진 가동. 버전 정보를 확인합니다.");
		
		ChainEngine chainEngine = new ChainEngine();
		RESTServer restServer = new RESTServer(); // TODO: passing chainEngine
		
		P.d(TAG, "API 서버 버전\t= %d (%s)", restServer.getVersionNumber(), restServer.getVersionCode());
		P.d(TAG, "내부 라이브러리: %s", restServer.getLicenseInfo());
		P.d(TAG, "체인 엔진 버전\t= %d (%s)", chainEngine.getVersionNumber(), chainEngine.getVersionCode());
		P.d(TAG, "내부 라이브러리: %s", chainEngine.getLicenseInfo());
		P.b();
		P.d(TAG, "메모리 부족으로 프로그램이 종료될 경우, -xm512M 옵션을 추가하여 재가동하십시오.");
		P.d(TAG, "사전 로딩 및 데이터베이스 연결 작업을 수행합니다.");
		if (!EQueryTool.testDatabaseConnection(EQueryConstants.SQL_HOST, EQueryConstants.SQL_DBNAME))
		{
			P.e(TAG, "데이터베이스를 사용할 수 없습니다.");
			System.exit(-1);
		}
		//으어아으 나중에
		chainEngine.createKKMAAnalyzer();
		P.d(TAG, "API 서버 시작");
		try
		{
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
		finally
		{
			/*
			P.d(TAG, "남은 API 요청을 조회합니다. 아래의 정보는 처리되지않은 API 요청입니다.");
			// restServer.showQueue();
			P.b();
			P.d(TAG, "엔진을 안전하게 종료합니다.");
			// chainEngine.closeSafely();
			P.d(TAG, "종료.");
			*/
		}
	}
}
