package jnu.mindsharing.legacy.libs;


import java.util.ArrayList;
import java.util.StringTokenizer;

public class SentenceSplit {
	public static ArrayList<String> split(String sentence)
	{
		ArrayList<String> list= new ArrayList<String>();
		StringTokenizer st = new StringTokenizer(sentence," ");
		//" "단위로 문장나눔
		while(st.hasMoreElements())//list에 " "단위로 저장
		{
			list.add(st.nextToken());
		}
		return list;
	}
	public static void main(String[] args)
	{
		System.out.print("문장 띄여쓰기로 나누기\n");
		System.out.print("나는 밥을 먹었다.\n");
		ArrayList<String> list=split("나는 밥을 먹었다.");
		for(String x : list)//list 내용 프린트 iterator를 이용 간략화
		{
			System.out.println(x);
		}
	}
}