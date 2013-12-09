package libs;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.StringTokenizer;

import libs.interfaces.EmotionValue;

public class EmotionNegativeValue implements EmotionValue
{
	/*
	 * System.getProperty() 로는 자바 시스템 관련 설정 값들을 가져 올 수 있다.

	 * 그 중 getProperty("user.dir")은 현재 작업 폴더를 가져오는 명령어.
	 * 3-2-term-project 폴더 자체를 가르키므로, src폴더나 bin 폴더에 접근하려면 직접 뒤의 경로를 적어주어야한다.
	 */
	// 그리고 파일명은 꼭 영어로!
	// 텍스트파일이 UTF-8이 아니라 그런 것 같다.
	// 텍스트 파일도 모두 UTF-8로 작성하거나 (즉, 이클립스 내에서 텍스트파일 작업),
	// 윈도우즈 인코딩(MS949) 로 작성된 파일을 UTF-8로 변환하는 과정이 필요함.

	final static String DICTIONARY_PATH = System.getProperty("user.dir") + "\\src\\libs\\Positve Words.txt"; 
	
	// 주의: main() 함수 여기에다가 작성하지 말자!
	// main()으로 호출해볼 수 있는 곳: src의 'test' 폴더 안에 있는 실행기들!
	
	@Override
	public boolean isInDictionary(String unit)//사전에 단어가 있는가?
	{
		if(findWords(unit) == null)
			return false;
		else
			return true;
	}

	@Override
	public int getConstant(String unit)//단어가 있을때 감정값, 단어가 없을때 감정값 0
	{
		if(findWords(unit) == null)
		{
			return 0;
		}
		else
		{
			StringTokenizer s = new StringTokenizer(findWords(unit),",");
			s.nextToken();
			int i=Integer.valueOf(s.nextToken());
			return i;
		}
	}

	@Override
	public String lossySearch(String keyword)
	{
		
		
		// 인터페이스 구현에 필요한 자료는 src/libs/interfaces/EmotionValue.java 안에 설명되어있음!
		//한글 초성
	    final char[] first = {'ㄱ', 'ㄲ', 'ㄴ', 'ㄷ', 'ㄸ', 'ㄹ', 'ㅁ', 'ㅂ',
	        'ㅃ', 'ㅅ', 'ㅆ', 'ㅇ', 'ㅈ', 'ㅉ', 'ㅊ', 'ㅋ', 'ㅌ', 'ㅍ', 'ㅎ'};
	    //한글 중성
	    final char[] middle = {'ㅏ', 'ㅐ', 'ㅑ', 'ㅒ', 'ㅓ', 'ㅔ', 'ㅕ', 'ㅖ', 
	        'ㅗ', 'ㅘ', 'ㅙ', 'ㅚ', 'ㅛ', 'ㅜ', 'ㅝ', 'ㅞ', 'ㅟ', 'ㅠ', 'ㅡ',
	        'ㅢ', 'ㅣ'};
	    //한글 종성
	    final char[] last = {' ', 'ㄱ', 'ㄲ', 'ㄳ', 'ㄴ', 'ㄵ', 'ㄶ', 'ㄷ', 
	        'ㄹ', 'ㄺ', 'ㄻ', 'ㄼ', 'ㄽ', 'ㄾ', 'ㄿ', 'ㅀ', 'ㅁ',
	        'ㅂ', 'ㅄ', 'ㅅ', 'ㅆ', 'ㅇ', 'ㅈ', 'ㅊ', 'ㅋ', 'ㅌ', 'ㅍ', 'ㅎ'};
	    /**
	    *한글 한 글자(char)를 받아 초성, 중성, 종성의 위치를 int[]로 반환 한다.
	    *@param char : 한글 한 글자
	    *@return int[] : 한글 초, 중, 종성의 위치( ex:가 0,0,0 )
	    */
	    public int[] split(char c){
	        int sub[] = new int[3];
	        sub[0] = (c - 0xAC00) / (21*28); //초성의 위치
	        sub[1] = ((c - 0xAC00) % (21*28)) / 28; //중성의 위치
	        sub[2] = (c -0xAC00) % (28);//종성의 위치
	        return sub;
	    }
	    
	    /**
	     *한글 한 글자를 구성 할 초성, 중성, 종성을 받아 조합 후 char[]로 반환 한다.
	     *@param int[] : 한글 초, 중, 종성의 위치( ex:가 0,0,0 )
	     *@return char[] : 한글 한 글자
	     */
	     public char[] combine(int[] sub){
	         char[] ch = new char[1];
	         ch[0] = (char) (0xAC00 + (sub[0]*21*28) + (sub[1]*28) + sub[2]);
	         return ch;
	     }
	     
	     /**
	     *한글 초,중,종성 분리/조합 테스트 메소드
	     */
	     public void doSomething(){
	         int[] x = null;
	         String str = "그래도 살만한 세상이다. 아?? 구랗쥐 구람";
	         int loop =  str.length();
	         char c;
	         System.out.println( "============한글 분리============" );
	         for( int i = 0; i < loop; i++ ){
	             c = str.charAt( i );
	             if( c >= 0xAC00 ){
	                 x = split( c );
	                 System.out.println( str.substring( i, i+1) + " : 초=" + first[x[0]] 
	                         + "\t중="+middle[x[1]]);
	                         //+ "\t종="+last[x[2]] );
	             }else{
	                 System.out.println( str.substring( i, i+1) );
	             }
	         }
	         System.out.println( "\r\n============한글 조합============" );
	         System.out.println( "0,0,0 : " +
	                     new String( combine( new int[]{0,0,0} ) ) );
	         System.out.println( "2,0,0 : " + 
	                     new String( combine( new int[]{2,0,0} ) ) );
	         System.out.println( "3,0,0 : " + 
	                     new String( combine( new int[]{3,0,0} ) ) );
	         System.out.println( "11,11,12 : " + 
	                     new String( combine( new int[]{11,11,10} ) ) );
	         System.out.println( "10,11,12 : " + 
	                     new String( combine( new int[]{10,11,14} ) ) );
	     }
		//return null;
	}
	public String findWords(String unit)//단어검색
	{
		String findStr = "사교";

		try
		{
			//버퍼 열기
			BufferedReader in = new BufferedReader(new FileReader(DICTIONARY_PATH));
			
			String str;
			while((str = in.readLine()) != null)
			{
				StringTokenizer s = new StringTokenizer(str,",");
				String Estr=s.nextToken();
				if(Estr.matches(findStr))
				{
					in.close();
					return str;
				}
			}
			// 버퍼 닫기
			in.close();
		}
		catch (IOException e)
		{
	        System.err.println(e); // 에러가 있다면 메시지 출력
	        System.exit(1);
	    }
		return null;//단어 없으면 null 리턴
	}
}
