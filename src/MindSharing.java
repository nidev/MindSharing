import libs.SentenceSplit;

public class MindSharing
{
	static boolean USE_DEBUG = true; // dbg() 함수에서 사용하는 변수, 참이면 디버깅 출력을 모두 보여준다.
	static String SW_NAME = "Mind Sharing"; // 소프트웨어 이름 정적 선언
	static int SW_VERSION_MAJOR = 0; // 소프트웨어 버전(1.55 에서 '1' 부분)
	static int SW_VERSION_MINOR = 1; // 소프트웨어 버전(1.55 에서 '55' 부분)
	
	
	public static String getVersionString()
	{
		// 버전 정보를 문자열로 리턴
		return String.format("Version %d.%d", SW_VERSION_MAJOR, SW_VERSION_MINOR);
	}
	
	public static void printHelp()
	{
		// 매개변수로 아무것도 입력되지 않았을 때 도움말 보여주기
		String help_message = "분석할 문장을 매개변수로 입력받습니다.";
		System.out.println(help_message);
	}
	
	public static void dbg(String debug_message)
	{
		// println 을 간단히 실행하려고 만든 함수
		if (USE_DEBUG)
		{
			System.out.print("[MindSharing] ");
			System.out.println(debug_message);
		}
	}

	public static void main(String[] args)
	{
		dbg("런처 시작");
		// 매개 변수 없이 실행된 경우, 그래픽 화면 실행
		if (args.length == 0)
		{
			dbg("그래픽 화면을 준비합니다.");
			new MindSharingUI();
		}
		else
		{
			dbg("매개변수가 입력됨. 콘솔 모드로 실행합니다.");
			// 매개 변수 입력되었는지 확인
			if (args[0].isEmpty()) // 입력 X
			{
				printHelp();
				System.exit(0); // 도움말을 출력하고 종료
				
			}
			else // 입력 O
			{
				SentenceSplit.split(args[0]);
				
			}
		}
		dbg("런처 종료");
	}

}
