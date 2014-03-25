package jnu.mindsharing.legacy.libs;


public class JasoSplit {
	public static void main(String[] args)
	{
        String typo = "각";
        /*String[] st =*/ split(typo);
       /* for (String x : st)
        {
        	System.out.println(x);
        }*/
	}
	public static String[] split(String kword)
	{
		String a[] = new String[3];
        // typo스트링의 글자수 만큼 list에 담아둠
        for(int i = 0; i < kword.length(); i++)
        {
        	char comVal = (char) (kword.charAt(i)-0xAC00);
        	
        	if (comVal >= 0 && comVal <= 11172)
        	{
        		// 한글일경우
        		        		                
                // 초성만 입력 했을 시엔 초성은 무시해서 List에 추가
        		char uniVal = (char)comVal;
 
        		// 유니코드 표에 맞추어 초성 중성 종성을 분리
        		char cho = (char) ((((uniVal - (uniVal % 28)) / 28) / 21) + 0x1100);
        		char jung = (char) ((((uniVal - (uniVal % 28)) / 28) % 21) + 0x1161);
        		char jong = (char) ((uniVal % 28) + 0x11a7);
 
        		if(cho!=4519)
        			System.out.println(cho+" ");
        		if(jung!=4519)
        			System.out.println(jung+" ");
        		if(jong!=4519)
        			System.out.println(jong+" ");
        		a[0]=String.valueOf(cho);
        		a[1]=String.valueOf(jung);
        		a[2]=String.valueOf(jong);;
        	}
        	else
        	{
        		// 한글이 아닐경우
        		comVal = (char) (comVal+0xAC00);
        		System.out.println(comVal+" ");
        		a[0]=" ";
        		a[1]=" ";
        		a[2]=" ";
        	}
        }
		return a;
	}
}
