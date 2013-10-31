package lee;
import java.util.ArrayList;
import java.util.StringTokenizer;
public class PhraseSplit {
	public static ArrayList<String> split(String sentence)
	{
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
		String st="나는 보았다.너 뭐하니?나가!\n";
		System.out.print("끝마침문자로 문장 나누기(. ? !)\n");
		System.out.print(st);
		ArrayList<String> list=split(st);
		for(String x:list)
		{
			System.out.println(x);
		}
	}
}
