package libs;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.StringTokenizer;

import libs.interfaces.EmotionValue;

public class EmotionNegativeValue implements EmotionValue
{
	/*
	 * System.getProperty() 로는 자바 시스템 관련 설정 값들을 가져 올 수 있다.

	 * 그 중 getProperty("user.dir")은 현재 작업 폴더를 가져오는 명령어.
	 * 3-2-term-project 폴더 자체를 가르키므로, src폴더나 bin 폴더에 접근하려면 직접 뒤의 경로를 적어주어야한다.
	 */
	// 그리고 파일명은 꼭 영어로!
	// 텍스트파일이 UTF-8이 아니라 그런 것 같다.
	// 텍스트 파일도 모두 UTF-8로 작성하거나 (즉, 이클립스 내에서 텍스트파일 작업),
	// 윈도우즈 인코딩(MS949) 로 작성된 파일을 UTF-8로 변환하는 과정이 필요함.

	final static String DICTIONARY_PATH = System.getProperty("user.dir") + "\\src\\libs\\Positve Words.txt"; 
	
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

	@Override
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
		
		
		// 인터페이스 구현에 필요한 자료는 src/libs/interfaces/EmotionValue.java 안에 설명되어있음!
		return null;
	}
	public String findWords(String unit)//단어검색
	{
		String findStr = "사교";

		try
		{
			//버퍼 열기
			BufferedReader in = new BufferedReader(new FileReader(DICTIONARY_PATH));
			
			String str;
			while((str = in.readLine()) != null)
			{
				StringTokenizer s = new StringTokenizer(str,",");
				String Estr=s.nextToken();
				if(Estr.matches(findStr))
				{
					in.close();
					return str;
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
