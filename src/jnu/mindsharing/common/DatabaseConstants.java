package jnu.mindsharing.common;

public class DatabaseConstants
{
	public class DB_URI
	{
		public static final String dbms = "postgresql";
		public static final String host = "localhost";
		public static final String dbname = "mindsharing";
		public static final String user = "mindsharing";
		public static final String password = "mindsharing";
		
		public DB_URI() { }
		@Override
		public String toString()
		{
			return String.format("jdbc:%s://%s/%s", dbms, host, dbname);
		}
		
	}
	
	public class WORD_TYPE
	{
		public static final int noun = 0; // includes emoticon/emoji
		public static final int verb = 1; // 동사
		public static final int adject = 2; // 형용사
		public static final int op = 4; // 부사(인데 감정 세기에 영향을 주는)
		public static final int exclam = 8; // 감탄사
	}
}
