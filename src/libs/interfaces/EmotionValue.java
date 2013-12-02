package libs.interfaces;

/*
 * 감정값 탐색용 라이브러리 인터페이스 선언
 * 
 * 내부의 구현에 상관없이, EmotionNegativeValue와 EmotionPositiveValue는
 * 다음과 같은 인터페이스(외부 함수)를 제공하며, 충환과 윤장이 자유롭게 구현해볼 수 있도록 하자.
 */

// 이 인터페이스를 implements 받는 클래스는, 반드시 이 기능을 구현해야한다.

/**
 * 이 인터페이스는, EmotionNegativeValue와 EmotionPositiveValue 클래스 파일을,
 * 다른 파일에서 쉽게 이용할 수 있고, 통일된 방법으로 이용할 수 있도록 하는 방법을
 * 제공한다.
 * @author nidev
 *
 */
public interface EmotionValue
{
	// 해당된 단어가, 사전 파일(텍스트파일)에 들어있는지 확인하고, 있으면 true, 없으면 false를 반환한다.
	/*
	 * 예)
	 * Positive Words.txt를 사전으로 쓸 때, 그 파일에 '기쁨,1' 이라는 데이터가 있고, isInDictionary("기쁨")으로 단어를 찾는 경우,
	 * 기쁨이라는 단어가 있으므로 true를 리턴한다.
	 * 반대로 isInDictionary("슬픔")을 입력하면, 슬픔이라는 데이터가 없으므로 false를 반환한다.
	 */

	public boolean isInDictionary(String unit);
	
	// 해당 단어에 해당하는 감정 값을 가져온다.
	/*
	 * 예)
	 * Positive Words.txt를 사전으로 쓸 때, 그 파일에 '기쁨,1' 이라는 데이터가 있고, getConstant("기쁨")으로 단어를 찾는 경우,
	 * 해당하는 값은 ','뒤의 1이므로 1을 리턴한다.
	 * 반대로 getConstant("슬픔")을 입력하면, 슬픔이라는 데이터가 없으므로 false를 반환한다.
	 */
	public int getConstant(String unit);
	
	// 사전에서 근접한 단어 또는 일치하는 단어를 찾아낸다. 반환값은 문자열 그대로이다.
	/*
	 * 예)
	 * Positive Words.txt를 사전으로 쓸 때, 그 파일에 '행복,1'이라는 데이터가 있지만, isInDictionary("행복한")으로 찾게 되면,
	 * 그 단어가 사전에 없게 된다. 이때, findSimilar("행복한") 으로 찾게 되면, 가장 근접한 '행복'이란 단어를 찾게 된다.
	 * 그리고 '행복'이란 문자열을 반환하며, 다시 getConstant("행복")을 하게 되면 행복에 해당하는 값이 얻어진다.
	 * 
	 * 탐색 알고리즘:
	 * step 1: 입력된 단어를 사전에서 찾는다.
	 * step 1-1: 사전에 있다면, 단어를 그대로 반환하고 종료한다.
	 * step 1-2: 사전에 없다면, step 2를 진행한다.
	 * step 2: 입력된 단어의 '마지막 글자'의 종성을 제거하고, 검색한다. 종성이 없다면, step 3으로 간다.
	 * (ex. '행복한'이 처음 입력되었다면 '행복하'라는 단어를 만든다.)
	 * step 2-1: 사전에 있다면, 단어를 그대로 반환하고 종료한다.
	 * step 2-2: 사전에 없다면, step 3을 진행한다.
	 * step 3: 입력된 단어의 마지막 글자에 종성이 사라진 상태이므로, 마지막 글자를 지운다.
	 * (ex. '행복한'이 처음 입력 되었다면 '행복'이라는 문자열로 바뀌게 됨)
	 * step 3-1: 글자가 한 글자 밖에 남지 않은 경우에는 null 포인터를 반환하고 종료한다.
	 * step 4: 사전에서 step 3의 단어를 검색한다.
	 * step 4-1: 사전에 있다면, 단어를 그대로 반환하고 종료한다.
	 * step 4-2: 사전에 없다면, step 2로 가서 같은 작업을 반복한다.
	 */
	public String lossySearch(String keyword);
}
