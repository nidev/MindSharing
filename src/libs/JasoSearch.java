package libs;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class JasoSearch
{
	public static void main(String[] args) throws IOException
	{
		BufferedReader in = new BufferedReader(new FileReader("C:\\Users\\yjpark\\Desktop\\File\\메모장\\긍정 관련 단어.txt"));
		
		String str;
		while(true)
		{
			str=in.readLine();
			if(str==null)
				break;
			
			System.out.println(str);
		}
		in.close();
	}
}
