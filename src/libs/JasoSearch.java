package libs;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class JasoSearch
{
	/*
	 * System.getProperty() 로는 자바 시스템 관련 설정 값들을 가져 올 수 있다.
	 * 그 중 getProperty("user.dir")은 현재 작업 폴더를 가져오는 명령어.
	 * 3-2-term-project 폴더 자체를 가르키므로, src폴더나 bin 폴더에 접근하려면 직접 뒤의 경로를 적어주어야한다.
	 */
	final static String DICTIONARY_PATH = System.getProperty("user.dir") + "\\src\\libs\\긍정 관련 단어.txt"; 
	
	public static void main(String[] args) throws IOException
	{
		/*
		// 프로퍼티 확인용 함수
		Properties prop = System.getProperties();
		for (Object obj: prop.keySet())
		{
			ELog.d(obj.toString(), obj.hashCode());
		}
		*/
		
		// 버퍼 열기
		BufferedReader in = new BufferedReader(new FileReader(DICTIONARY_PATH));
		
		String str;
		while(true)
		{
			// 혹시 텍스트파일이 UTF-8이 아니라 그런 것 같다.
			// 텍스트 파일도 모두 UTF-8로 작성하거나 (즉, 이클립스 내에서 텍스트파일 작업),
			// 윈도우즈 인코딩(MS949) 로 작성된 파일을 UTF-8로 변환하는 과정이 필요함.
			str=in.readLine();
			
			if(str==null)
			{
				// 문자열이 없는 경우에는 종료
				break;
			}
			else
			{
				// 있는 경우에는?
				System.out.println(str);
			}
		}
		// 버퍼 닫기
		in.close();
	}
}
