/**
 * MindSharing Project
 */


package jnu.mindsharing.legacy.examples;


/**
 * JAR(Java Archived Resource) 형태로 압축했을 때는, 절대 경로로 사전에 접근할 수 없기 때문에,
 * 패키지 내에서도 접근할 수 있는 방법을 사용해야하는데, 그 방법이 this.class.getResourceAsStream이다.
 * 
 * 이 예제 파일은 libs 폴더 안에 있는 긍정 단어 사전을 직접 읽는 예제이다.
 * @author nidev
 *
 */
public class ReadingResourceExample
{
	public static void main(String[] args)
	{
		try
		{
			System.out.println("아래 출력에 null 이라고 나오지 않으면 정상적으로 인식된 것!");
			System.out.println(ReadingResourceExample.class.getResourceAsStream("/libs/Positive Words.txt").toString());
			//ReadingResourceExample.class.getre
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}
