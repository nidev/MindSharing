import java.util.*;
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
		ArrayList<String> list=split("나는 밥을 먹었다.");
		for(int i=0; i<list.size();i++)//list 내용 프린트
		{
			System.out.println(list.get(i));
		}
	}
}