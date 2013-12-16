package libs;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.StringTokenizer;


public class EmotionNegativeValue extends EmotionPositiveValue
{
	/*
	 * Java study point:
	 * 'extends EmotionPositiveValue' 라는 키워드는 무슨 짓을 해버렸길래 이곳에 아무 내용이 없어도,
	 * 코드가 정상적으로 동작할 수 있는걸까?
	 */
	final static String DICTIONARY_PATH = "/libs/Negative Words.txt"; 

	// findWords 는 사전 파일 경로에 따라 다르게 동작하므로,
	// NegativeValue에서도 새로 구현을 해주는 게 맞는 것 같다.
	// 따라서 EmotionPositiveValue를 상속하는 대신, @Override 키워드를 추가함.
	@Override
	public String findWords(String unit)//단어검색 
	{
		try
		{
			//버퍼 열기
			/*
			 * 버퍼 여는 방법이 약간 바뀌었다
			 * 이것은 나중에 JAR 파일로 패키징해서도 패키지 내부의 사전 파일을 안전하게 읽을 수 있도록한다.
			 * 소스 코드 설명에 대해서는 src/examples/ 에 있는 readingResourceExample.java 를 참고
			 */
			BufferedReader in = new BufferedReader(new InputStreamReader(EmotionPositiveValue.class.getResourceAsStream(DICTIONARY_PATH)));
			
			String str;
			while((str = in.readLine()) != null)
			{
				// !로 시작하는 줄은 읽지 않는다. (사전에 주석을 달기 위함)
				if (str.startsWith("!"))
				{
					continue;
				}
				StringTokenizer s = new StringTokenizer(str,",");
				// 토큰 갯수가 남아있는지 체크한다. 이게 남아있지 않으면, 사전 파일에서 빈 줄 발견시 사망하게 됨.
				if (s.hasMoreElements())
				{
					String Estr=s.nextToken();
					if(unit.matches(Estr))
					{
						in.close();
						return str;
					}
				}
			}
			// 버퍼 닫기
			in.close();
		}
		catch (IOException e)
		{
	        System.err.println(e); // 에러가 있다면 메시지 출력
	        System.exit(1);
	    }
		return null;//단어 없으면 null 리턴
	}
}
