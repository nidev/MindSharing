/**
 * 
 */
package jnu.mindsharing.hq;

import jnu.mindsharing.chainengine.ChainEngine;
import jnu.mindsharing.chainengine.MappingGraphDrawer;
import jnu.mindsharing.chainengine.Sense;
import jnu.mindsharing.common.ExprHash;
import jnu.mindsharing.common.HList;
import jnu.mindsharing.common.Hana;
import jnu.mindsharing.common.P;

import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
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
	
	@Option(name="-cfg", usage="Override builtin configuration", metaVar="ConfigFile")
	private static String configurationFile = "(default)";
	@Option(name="-v", usage="Enable verbose mode for debugging")
	private static boolean verboseMode;
	@Option(name="-extbipath", usage="Override internal baseIdiom dictionary", metaVar="folder")
	private static String externalBaseIdiomPath = ".";
	@Option(name="-h", usage="Show this help message")
	private static boolean showHelp;
	

	public static void showMemoryWarning()
	{
		P.d(TAG, "메모리 부족으로 프로그램이 종료될 경우, -xm512M 옵션을 추가하여 재가동하십시오.");
	}
	
	public static void initiateFullSystem(ChainEngine chainEngine, RESTServer restServer)
	{
		P.d(TAG, "API 서버 버전\t= %d (%s)", restServer.getVersionNumber(), restServer.getVersionCode());
		P.d(TAG, "내부 라이브러리: %s", restServer.getLicenseInfo());
		P.d(TAG, "체인 엔진 버전\t= %d (%s)", chainEngine.getVersionNumber(), chainEngine.getVersionCode());
		P.d(TAG, "내부 라이브러리: %s", chainEngine.getLicenseInfo());
		try
		{
			//으어아으 나중에
			chainEngine.createKKMAAnalyzer();
			restServer.run(chainEngine);
		}
		catch (Exception e)
		{
			P.b();
			P.e(TAG, "API 서버의 서브릿 시작 도중 오류가 발생하였습니다. 이 오류는 API 서버 오류이거나 체인 엔진 내부의 오류일 수 있습니다. 자세한 내용은 아래의 Traceback을 확인해주십시오.");
			e.printStackTrace();
			P.e(TAG, "Exception class: %s,  Exception message: %s", e.toString(), e.getMessage());
			P.e(TAG, "이 오류는 로그 시스템에 기록되었습니다. 서버가 가동될 수 없습니다.");
			System.exit(-1);
		}
	}
	
	/**
	 * 서버와 메시지 로거를 가동한다. 메시지는 기본적으로 표준 출력을 통해 출력된다.
	 * @param args 명령행 옵션들(사용되지 않음)
	 * @return none
	 */
	public static void main(String[] args)
	{
		CmdLineParser parser = new CmdLineParser(new Bootstrap());
		
		P.d(TAG, "MindSharing Bootstrapper started");
		ChainEngine chainEngine;
		RESTServer restServer;
		
		if (parser.getArguments().size() == 0)
		{
			chainEngine = new ChainEngine();
			restServer = new RESTServer(); // TODO: passing chainEngine
			initiateFullSystem(chainEngine, restServer);
		}
		else
		{
			if (showHelp)
			{
				parser.printUsage(System.out);
			}
			// XXX: 다른 옵션이 처리 되지 않음
		}
	}
}
