package libs;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import libs.interfaces.*;

//인터페이스 구현에 필요한 자료는 src/libs/interfaces/EmotionValue.java 안에 설명되어있음!

public class EmotionPositiveValue implements EmotionValue
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
	//str=in.readLine();

	final static String DICTIONARY_PATH = System.getProperty("user.dir") + "\\src\\libs\\Positve Words.txt"; 
	
	public static void main(String[] args) throws IOException
	{
		
		String findStr = "기쁨";
		int lineNumber = 1;               //행 번호
		/*
		// 테스트용: 프로퍼티 확인용 함수
		Properties prop = System.getProperties();
		for (Object obj: prop.keySet())
		{
			ELog.d(obj.toString(), obj.hashCode());
		}
		*/
		
		try
		{
			// 버퍼 열기
			BufferedReader in = new BufferedReader(new FileReader(DICTIONARY_PATH));
			
			String str;
			while((str = in.readLine()) != null)
			{
				// IF-ELSE: 만약 매치되지 않았을 때는 어떤 일이 일어나는걸까?
				// while 문을 빠져나간 다음에, 결과가 있었는지 없었는지 boolean 변수로 확인시켜줄 수 있다면,
				// 상황에 알맞는 처리를 해줄 수 있을듯.
				
				// matches() 메소드는 패턴을 필요로 하는데, '기쁨'이라는 단어만으로는 '기쁨,1'이 있는 줄을 매칭할 수 없을지도 모른다.
				// findStr에 기쁨,1 을 넣어보고 돌려서, 출력이 나오는지 확인하고, 어떻게 고칠지 고민해보자.
				if(str.matches(findStr)) 
				{
					System.out.format("%3d: %s%n",lineNumber, str);
				}
				else
				{
					// 만약 어떤 처리가 필요하다면
				}
				lineNumber++;                  //행 번호 증가
			}
			// 버퍼 닫기
			in.close();
		}
		catch (IOException e)
		{
	        System.err.println(e); // 에러가 있다면 메시지 출력
	        // e.printStackTrace(); // << 보통 이 함수를 오류 메시지 출력에 많이 이용. 참고로만 알아두삼.
	        System.exit(1);
	    } 
	}

	@Override
	public boolean isInDictionary(String unit)
	{
		// 인터페이스 구현에 필요한 자료는 src/libs/interfaces/EmotionValue.java 안에 설명되어있음!
		return false;
	}

	@Override
	public int getConstant(String unit)
	{
		// 인터페이스 구현에 필요한 자료는 src/libs/interfaces/EmotionValue.java 안에 설명되어있음!
		return 0;
	}

	@Override
	public String lossySearch(String keyword)
	{
		// 인터페이스 구현에 필요한 자료는 src/libs/interfaces/EmotionValue.java 안에 설명되어있음!
		return null;
	}
}
