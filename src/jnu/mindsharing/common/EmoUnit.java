package jnu.mindsharing.common;

import java.util.HashMap;

/**
 * 기본적인 감정정보를 담는 객체이다. 단어 하나는 이 클래스를 확장하여 제작된다.
 * 
 * 이 개체는 기본적으로 4가지 방향에 대한 벡터를 가지고 있다.
 * 
 * [행복, 즐거움, 만족] 을 나타내는 joy (값 범위: 0~2) => Emotional
 * [슬픔, 상심, 불만족] 을 나타내는 sorrow (값 범위: 0~2) => Emotional
 * [발전, 생산, 탄생] 을 나타내는 growth (값 범위: 0~2) => Conditional(State)
 * [쇠퇴, 파괴, 죽음] 을 나타내는 cease (값 범위: 0~2) => Condition(State)
 * 
 * 0은 값이 없는 상태, 1은 통상적으로 사용되는 어휘, 2는 직설적으로 사용하거나 강한 어휘에 설정한다.
 * ex) 죽다(sorrow 1, cease 1) -> 뒈지다(sorrow 1, cease 2)와 같은 표현이 가능함
 * 
 * 잘 추상화한 것 같으니, 글 전체를 계산하는 객체에서도 재사용이 가능할듯. (약간의 확장이 더 필요한듯하다. 한 문장 계산 후, 로그로 줄이고 다른 객체에 벡터를 저장해야함.)
 * @author nidev
 *
 */
public class EmoUnit
{
	public static enum EPower {None, Formal,  Mild, Strong, Extreme};
	/*
	 * 형태소 분석기에서, 각 어휘에 품사를 태그해놓듯, 감정 유닛들도 태그를 해놓는 편이 좋을 것 같다.
	 * 
	 * 이 태그는 처음에는 Pass로 지정하고, 1차 어휘 분석 이후에 수식 관계 및 주술관계 분석을 한 후에
	 * 태그를 변경한다. 태그 유형은 다음과 같다.
	 * 
	 * (구현된 태그 앞에는 *를 붙인다.)
	 * *Skip - 어휘를 처리하지 않는다.
	 * UnknownDesc - 문장을 마치는 서술어이나, 데이터베이스에 등록되지않은 서술어이다. 감정값은 전체 흐름에 맞춰 조절한다.
	 * SubjectWord - 주어(이/가) 또는, 서술의 대상(은/는)이다.
	 * ObjectWord - 주어를 제외한 모든 명사이다.
	 * Desc - 서술어이다. 감정 정보가 포함된 서술어이지만, 역할이 분명하지 않다.
	 * InvertNextDesc - 다음 서술어를 반전시킨다. (감정 값 반전 등등) 반전 후 이 토큰은 Skip된다.
	 * UnidentifiedDesc - 새로 입력된 서술어이다. 감정표현이 있는지 없는지 모른다. 
	 * DescSubject - 주어를 수식하는, 문장을 마치는 서술어이다.
	 * DescEnhancer - 다음에 오는 EmoUnit중 태그가 DescSubject|DescNextObject|Desc라면 increase() 를 호출하게 한다. 증가 후 이 토큰은 Skip된다.
	 * DescReducer - 다음에 오는 EmoUnit중 태그가 DescSubject|DescNextObject|Desc라면 decrease() 를 호출하게 한다. 감소 후 이 토큰은 Skip된다.
	 * DescNextObject - 감정값이 등록된, 다음 ObjectWord를 수식하는 어휘이다
	 * 
	 * SentenceEnhancer - 다음 문장의 감정값의 증대, 강화가 있음을 알린다.(게다가, 더욱이)
	 * InverseNextSentence - 다음 문장의 감정값 반전이 있음을 알린다.(역접)
	 * *Emoticon - 문장 전체를 향한 이모티콘, 혹은 이모티콘 자체를 가르킨다.
	 * 
	 *  
	 * 
	 * 표현을 약하게 만드는 어휘들(조금, 약간)
	 * (태그 정리바람)
	 * 
	 * 1차 어휘 분석 이후에 2차 어휘 분석에서는 주술-목술 관계, 기타 어휘의 감정분석을 하여 태그를 설정한다.
	 * 
	 * 3차에서는 태그를 따라가며 O(n)의 복잡도로 문장 전체의 값을 계산한다.
	 * 
	 */
	public static enum WordTag {
		Skip,
		SubjectWord, ObjectWord,
		UnidentifiedDesc, Desc, DescSubject, DescNextObject, 
		InvertNextDesc, DescEnhancer, DescReducer, SentenceEnhancer, SentenceInverter,
		Emoticon};
	/*
	 * 한가지 더 고려해야할 토큰이 있다. 예를 들어,
	 * 
	 * "한글날은 좋은 날이다" 의 경우
	 * 
	 * SubjectWord (조사) DescNextObject ObjectWord 인데, ObjectWord(날)는 SubjectWord를 지칭하는 대명사이다.
	 * 따라서 DescNextObject 가 DescSubject 태그로 치환될 수도 있다.
	 * 
	 * 대명사의 경우를 대비해 findWCategoryByWord() 를 추가해야할 것 같다. (일이 커지네.)
	 */
	protected static int INVALID_ENUM = -1;
	final public String JOY = "joy";
	final public String SORROW = "sorrow";
	final public String GROWTH = "growth";
	final public String CEASE = "cease";
	
	final String[] vectorTitles = {JOY, SORROW, GROWTH, CEASE};
	
	private HashMap<String, Enum<EPower>> vectorTable;
	private Enum<WordTag> wordTag = WordTag.Skip;
	private String emotionOrigin = null;
	
	private String TAG = "EmoUnit";
	

	public EmoUnit()
	{
		defaultTable();
	}
	
	public EmoUnit(String source)
	{
		emotionOrigin = source;
		defaultTable();
	}
	
	public void defaultTable()
	{
		if (vectorTable == null)
		{
			vectorTable = new HashMap<String, Enum<EPower>>();
		}
		for (String title: vectorTitles)
		{
			vectorTable.put(title, EPower.None);
		}
	}
	
	public static int epowerToInt(Enum<EPower> ev)
	{
		int num = 0;
		for (EPower ep: EPower.values())
		{
			if (ep.equals(ev))
			{
				return num;
			}
			num++;
				
		}
		return INVALID_ENUM;
	}
	
	public EmoUnit increase(String title)
	{
		// TODO: title validation check
		// Can't go more than EPower.Strong
		Enum<EPower> currentValue = vectorTable.get(title);
		if (currentValue == EPower.None)
		{
			vectorTable.put(title, EPower.Formal);
		}
		else if (currentValue == EPower.Formal)
		{
			vectorTable.put(title, EPower.Mild);
		}
		else if (currentValue == EPower.Mild)
		{
			vectorTable.put(title, EPower.Strong);
		}
		else if (currentValue == EPower.Strong)
		{
			vectorTable.put(title, EPower.Extreme);
		}
		else
		{
			P.e(TAG, "감정의 세기는 EPower.Extreme 을 넘을 수 없습니다. (증가 요청된 감정벡터: %s)", title);
		}
		return this; // Chaining을 위한 기법. ex) emounit_object.increase().invert()
	}
	
	public EmoUnit decrease(String title)
	{
		// TODO: title validation check
		// Can't go more than EPower.Strong
		Enum<EPower> currentValue = vectorTable.get(title);
		if (currentValue == EPower.Extreme)
		{
			vectorTable.put(title, EPower.Strong);
		}
		else if (currentValue == EPower.Strong)
		{
			vectorTable.put(title, EPower.Mild);
		}
		else if (currentValue == EPower.Mild)
		{
			vectorTable.put(title, EPower.Formal);
		}
		else if (currentValue == EPower.Formal)
		{
			vectorTable.put(title, EPower.None);
		}
		else
		{
			P.e(TAG, "감정의 세기는 EPower.None 밑으로 갈 수 없습니다. (감소 요청된 감정벡터: %s)", title);
		}
		return this; // Chaining을 위한 기법. ex) emounit_object.increase().decrease()
	}
	
	public EmoUnit invertEmotionals()
	{
		// This function will exchange each strength of two vectors.
		// current Joy -> next Sorrow
		// current Sorrow -> next Joy
		Enum<EPower> tmp;
		tmp = vectorTable.get(JOY);
		vectorTable.put(JOY, vectorTable.get(SORROW));
		vectorTable.put(SORROW, tmp);
		P.d(TAG, "감정 반전이 발생하였습니다.");
		return this; // Chaining을 위한 기법. ex) emounit_object.increase().invertEmotionals()
	}
	
	public EmoUnit invertConditionals()
	{
		// This function will exchange each strength of two vectors.
		// current Growth -> next Cease
		// current Cease -> next Growth
		Enum<EPower> tmp;
		tmp = vectorTable.get(GROWTH);
		vectorTable.put(GROWTH, vectorTable.get(CEASE));
		vectorTable.put(CEASE, tmp);
		P.d(TAG, "상태/상황 반전이 발생하였습니다.");
		return this; // Chaining을 위한 기법. ex) emounit_object.increase().invertConditionals()
	}
	
	public EmoUnit invertAll()
	{
		invertEmotionals();
		return invertConditionals();
	}
	
	public String getOrigin()
	{
		return emotionOrigin;
	}
	
	public Enum<EPower> getVectorSize(String title)
	{
		return vectorTable.get(title);
	}
	
	public EmoUnit setTag(Enum<WordTag> wt)
	{
		wordTag = wt;
		return this;
	}
	
	public Enum<WordTag> getTag()
	{
		return wordTag;
	}
}
