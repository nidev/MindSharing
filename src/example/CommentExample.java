/**
 * MindSharing Project
 */
package example;

/**
 * @author 작성자
 *
 * 주석 달기 예제 파일.
 * 클래스 윗부분에는 간단히 작성자를 적어주는데, 공동 작업을 했다면 모두의 이름을 적어도 상관없지만,
 * 대표적으로 한 사람의 이름을 적어줍니다.
 * 참고로 이클립스에서 '파랗게' 보이는 이 주석은 Javadoc이라고, 자동 문서화 툴에 의해 처리됩니다. 
 * 
 * Javadoc 생성시 VM Option에는 -locale ko_KR -encoding UTF-8 -charset UTF-8 -docencoding UTF-8 를 추가해야 오류가 나지 않습니다.
 * 
 * 이 파일로 생성된 Javadoc 예제 문서는 doc 폴더에서 확인할 수 있습니다.
 */
public class CommentExample
{
	// 클래스 내에 선언해둔 변수들이 있다면, 아래와 같이 간략하게 설명해둡니다.
	
	// 내 이름을 선언
	final String myName = "Chonnam";

	/**
	 * 함수를 설명하는 부분입니다. 각 매개변수에 대해 설명을 달아줍니다.
	 * 따로 타입을 언급하진 않고, 변수명만 입력하면 됩니다.
	 * 입력 순서는 변수명, 변수 설명입니다.
	 *  
	 * @param x X좌표를 입력 받습니다.
	 * @param y Y좌표를 입력받습니다.
	 * @param name 유저 네임을 입력받습니다.
	 */
	public static void example(int x, int y, String name)
	{
		// 변수에 대해서도 설명이 필요하다면 달아줍시다.
		
		// 방 번호 입력
		int[] room = {0, 1, 2, 3, 4};
		
		// for문을 돌리기 위해 막연히 사용될 것 같은 변수는 굳이 주석을 달지 않아도 됩니다.
		int i;
		
		// for문이나 while문, if 문에 대해서도 설명을 달아줍시다. (보통 윗줄에 달면 좋습니다.)
		
		// 각 방 번호에 대해 이름을 출력하는 작업을 한다.
		for (int room_number: room)
		{
			i = 0;
			while (i++ < 3)
			{
				// x 가 4이상이고 y가 5이상일 때 이름을 세 번 출력한다.
				if (x > 3 && y > 4)
				{
					System.out.println(name);
				}
				
			}
		}
	}
}
