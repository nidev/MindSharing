package jnu.mindsharing.common;

public class EQueryConstants
{
	public final static String SQL_HOST = "localhost";
	public final static String SQL_DBNAME = "mindsharing";
	public final static String SQL_JDBCHEAD = "jdbc:mysql://";
	
	// 그것, 그거해, 이거 로 지칭하는 대상을 추론하기 위한 것
	/*
	 * 한 단어로 수렴이 가능한 케이스에 대해서만 처리 작업을 할 것.
	 * 나머지는 무리일 수 있음.
	 * 
	 * ex)
	 * 짜장면은 맛있는 음식이다. -> 짜장면(=음식)은 맛있다
	 * 한글날은 멋진 날이다. -> 한글날은 멋지다
	 * 던지는 것은 못된 행동이다. -> 던지는 건 못됐다.
	 * 50세면 짧은 나이가 아니다. -> 50세는 짧지 않다.
	 * ... 등으로 변환하려고 하는데 잘될지 모르겠음  
	 * 
	 */
	public static long CATEGORY_FOOD = 0; // 음식
	public static long CATEGORY_TIME_PERIOD = 0; // 날짜, 기간, 기념일
	public static long CATEGORY_ACTION = 0; // 행동(파괴, 생성) 
	public static long CATEGORY_PLACE = 0; // 장소
	public static long CATEGORY_AGE = 0;
	public static long CATEGORY_RELIGION = 0; // 종교, 신앙
	public static long CATEGORY_SPORTS = 0; // 스포츠, 운동
	public static long CATEGORY_ENTERTAINMENT = 0; // 영화, 예능, TV쇼
	public static long CATEGORY_HISTORICAL_EVENT = 0; // 역사적 사건
}
