package jnu.mindsharing.common;

/**
 * 하나에서 사용할 확장 태그 목록(문장 형태소 분석용 태그)
 * @author nidev
 *
 */
public class XTag_atomize
{
	/*
	* 형태소 분석기에서, 각 어휘에 품사를 태그해놓듯, 감정 유닛들도 태그를 해놓는 편이 좋을 것 같다.
	* 
	* 이 태그는 처음에는 Pass로 지정하고, 1차 어휘 분석 이후에 수식 관계 및 주술관계 분석을 한 후에
	* 태그를 변경한다. 태그 유형은 다음과 같다.
	* 
	* 
	* * 범용 태그
	* Skip - 어휘를 처리하지 않는다. 처리되지않는 어휘는 나중에 삭제된다.
	* 
	* * 마커 태그(초기에 어휘 품사만 가지고 결정한다)
	* SubjectTrail : 앞의 어휘가 주어이다.
	* ObjectTrail : 앞의 어휘는 목적어이다.
	* DescTrail : 앞의 어휘가 서술어일 가능성이 높다.
	* Noun : 이 어휘는 명사이다.
	* Adject : 이 어휘는 형용사이다.
	* Verb : 이 어휘는 동사이다.
	* Refererence : 이 어휘는 지시하는 대상이 있는 대명사이다.
	* 
	* * 역할 태그(마커 태그를 바탕으로 EmotionAlgorithm이 합성, 소거를 통해 역할 태그로 정리한다.)
	* Subject : 해당 어휘는 주어이다
	* Object : 해당 어휘는 목적어 또는 일반 명사들의 덩어리이다.
	* Desc : 수식 관계를 파악할 수 없는 서술어이다.
	* DescSubject : 주어를 수식하는 서술어이다.
	* 
	* * 연산자 태그(이 태그는 다음에 오는 감정값에 영향을 미친다)
	* DescOp: 연산자 태그로 표시한다. 보통 어휘 품사는 부사이다. 이 새 태그는 아래의 4가지 연산을 포함한다.
	* - InvertNextDesc : 다음 서술어가 갖는 감정값을 반전한다.
	* - NextDescEnhacer : 다음 서술어의 감정값을 강화한다.
	* - NextDescReducer : 다음 서술어의 감정값을 약화한다.
	* - NextDescDepender : 현재 서술어가 다음 서술어에 영향을 미칠 수 있어, 다음 서술어를 참고한다.
	* - DescNextObject : 뒤에 오는 Object를 수식하는 서술어이다.
	* 
	* 
	*/
	
	// EmoUnit에서 포팅해온 Enum 코드를 그대로 가져옴. 호환성 확보 및 시간 단축
	public static final String Skip = "NIL";
	public static final String UnhandledTrailMarker = "+U";
	public static final String SubjectTrailMarker = "+S";
	public static final String ObjectTrailMarker = "+O";
	public static final String DescTrailMarker = "+D";
	public static final String NounMarker = "N";
	public static final String AdjectMarker = "ADJ";
	public static final String VerbMarker = "V";
	public static final String ReferenceMarker = "RF";
	public static final String QuantityComingMarker = "Q";
	public static final String DeterminerMarker = "D?";
	public static final String Subject = "S";
	public static final String Object = "O";
	public static final String Desc = "D";
	public static final String DescSubject = "DS";
	public static final String DescOp = "D^v?";
	public static final String EndOfSentence = ".";
	//public static final String DescNextObject = "D>O";
	//public static final String InvertNextDesc = "I>D";
	//public static final String NextDescEnhancer = "^D";
	//public static final String NextDescReducer = "vD";
	//public static final String NextDescDepender = "?D";
	public static final String Emoticon = "EMO";
}
