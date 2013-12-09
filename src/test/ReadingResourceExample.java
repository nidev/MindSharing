package test;


public class ReadingResourceExample
{
	public static void main(String[] args)
	{
		try
		{
			System.out.println(ReadingResourceExample.class.getResourceAsStream("libs.ELog").toString());
			//ReadingResourceExample.class.getre
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}
