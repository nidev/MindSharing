package libs;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.StringTokenizer;

import libs.interfaces.EmotionValue;

//인터페이스 구현에 필요한 자료는 src/libs/interfaces/EmotionValue.java 안에 설명되어있음!

public class EmotionPositiveValue implements EmotionValue
{
	final static String DICTIONARY_PATH = "/libs/Positive Words.txt"; 

	// 주의: main() 함수 여기에다가 작성하지 말자!
	// main()으로 호출해볼 수 있는 곳: src의 'test' 폴더 안에 있는 실행기들!
	
	@Override
	public boolean isInDictionary(String unit)//사전에 단어가 있는가?
	{
		if(findWords(unit) == null)
			return false;
		else
			return true;
	}

	public int getConstant(String unit)//단어가 있을때 감정값, 단어가 없을때 감정값 0
	{
		if(findWords(unit) == null)
		{
			return 0;
		}
		else
		{
			StringTokenizer s = new StringTokenizer(findWords(unit),",");
			s.nextToken();
			int i=Integer.valueOf(s.nextToken());
			return i;
		}
	}
	
	@Override
	public String lossySearch(String keyword)
	{
		while(keyword.length()!=0)
		{
			if(isInDictionary(keyword)==true)
				return keyword;
			else if(isInDictionary(keyword)==false)
			{
				keyword=delLastChar(keyword);
			}
		}
		// 인터페이스 구현에 필요한 자료는 src/libs/interfaces/EmotionValue.java 안에 설명되어있음!
		return null;
	}
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
	
	public String lastChar(String keyword)// 단어의  끝글자 리턴
	{
		String lastC = keyword.substring(keyword.length()-1);
		return  lastC;
	}
	public String elseChar(String keyword)
	{
		String elseC = keyword.substring(0,keyword.length()-1);
		return elseC;
	}
	public String delLastChar(String keyword)
	{
		char temp[] = new char[1];
		temp = lastChar(keyword).toCharArray();
	
		int sub[] = new int[3];
		for(int i=0;i<3;i++)
			sub[i]=0;

        sub[0] = (temp[0] - 0xAC00) / (21*28); //초성의 위치
        sub[1] = ((temp[0] - 0xAC00) % (21*28)) / 28; //중성의 위치
        sub[2] = (temp[0] -0xAC00) % (28);//종성의 위치
        if(sub[2] != 0)
        {
	        char[] ch = new char[1];
	        ch[0] = (char) (0xAC00 + (sub[0]*21*28) + (sub[1]*28));
	        return elseChar(keyword)+ch[0];
        }
        else
        	return elseChar(keyword);
	}
}
