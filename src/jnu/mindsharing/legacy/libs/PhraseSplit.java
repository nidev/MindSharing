package jnu.mindsharing.legacy.libs; // 외부에서 작업해왔어도, 패키지 이름은 우리에게 맞춰서~

import java.util.ArrayList;
import java.util.StringTokenizer;
public class PhraseSplit
{
	public static ArrayList<String> split(String sentence)
	{
		/*
		 * 문장을 나누는 기준은 보통 마침표(.), 쉼표(,), 느낌표(!) 등이 있을 수 있으나,
		 * 
		 * 문장이 복잡해지면 따옴표(큰/작은)와 말줄임표가 섞일 수도 있으니, 나중에 확장할 수 있도록 토큰 생성 규칙을 좀 더 다듬을 것.
		 */
		ArrayList<String>list=new ArrayList<String>();
		StringTokenizer st = new StringTokenizer(sentence,".!?");
		while(st.hasMoreElements())//list에 " "단위로 저장
		{
			list.add(st.nextToken());
		}
		return list;
	}
	
	public static void main(String[] args)
	{
		// 시험용 문자열
		String st ="나는 보았다.너 뭐하니?나가!\n";
		
		System.out.print("끝마침문자로 문장 나누기(. ? !)\n");
		// 예제 문장 출력
		System.out.print(st);
		
		// 문자열 나눈 결과를 ArrayList<String>으로 받음.
		ArrayList<String> list=split(st);
		
		// 결과 출력
		for(String x:list)
		{
			System.out.println(x);
		}
	}
}
