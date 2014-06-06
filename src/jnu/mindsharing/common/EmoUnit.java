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
	 * DescNextObject : 뒤에 오는 Object를 수식하는 서술어이다.
	 * 
	 * * 연산자 태그(이 태그는 다음에 오는 감정값에 영향을 미친다)
	 * InvertNextDesc : 다음 서술어가 갖는 감정값을 반전한다.
	 * NextDescEnhacer : 다음 서술어의 감정값을 강화한다.
	 * NextDescReducer : 다음 서술어의 감정값을 약화한다.
	 * NextDescDepender : 현재 서술어가 다음 서술어에 영향을 미칠 수 있어, 다음 서술어를 참고한다.
	 * 
	 * * 특수 태그
	 * Emoticon : 한글 문자가 아닌 이모티콘을 나타낸다.
	 * 
	 */
	public static enum WordTag {
		Skip,
		UnhandledTrailMarker, SubjectTrailMarker, ObjectTrailMarker, DescTrailMarker, NounMarker, AdjectMarker, VerbMarker, ReferenceMarker, QuantityComingMarker, DeterminerMarker, 
		Subject, Object, Desc, DescSubject, DescNextObject, 
		InvertNextDesc, NextDescEnhancer, NextDescReducer, NextDescDepender, 
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
	
	final public String[] vectorTitles = {JOY, SORROW, GROWTH, CEASE};
	
	private HashMap<String, Enum<EPower>> vectorTable;
	private Enum<WordTag> wordTag = WordTag.Skip;
	private String extTag;
	private String emotionOrigin = null;
	
	private String TAG = "EmoUnit";
	

	public EmoUnit()
	{
		emotionOrigin = "";
		defaultTable();
		extTag = "";
	}
	
	public EmoUnit(String source)
	{
		emotionOrigin = source;
		defaultTable();
		extTag = "";
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
	
	public int[] getVectorAsIntArray()
	{
		int emovector[] = new int[vectorTitles.length];
		for (int index = 0 ; index < vectorTitles.length ; index++)
		{
			emovector[index] = epowerToInt(getVectorSize(vectorTitles[index]));
		}
		return emovector;
	}
	
	private EmoUnit increase(String title)
	{
		// TODO: title validation check
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
	
	private EmoUnit decrease(String title)
	{
		// TODO: title validation check
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
	
	public EmoUnit enhance(String title)
	{
		if (getVectorSize(title) != EmoUnit.EPower.None)
		{
			increase(title);
		}
		return this;
	}
	
	public EmoUnit reduce(String title)
	{
		if (getVectorSize(title) != EmoUnit.EPower.Formal)
		{
			decrease(title);
		}
		return this;
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
	
	public String[] getTitles()
	{
		return vectorTitles;
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
	
	public EmoUnit setExt(String exttag)
	{
		extTag = exttag;
		return this;
	}
	
	public EmoUnit setExt()
	{
		extTag = "";
		return this;
	}
	
	public String getExt()
	{
		return extTag;
	}
	
	public boolean importVectors(EmoUnit supplied)
	{
		// 주어진 EmoUnit에서 값을 가져온다.
		// 현재 객체에 저장된 값은 무시된다
		if (supplied == null)
		{
			return false;
		}
		else
		{
			for (String title: vectorTitles)
			{
				vectorTable.put(title, supplied.getVectorSize(title));
			}
			return true;
		}
	}
	
	public boolean importVectors(int joy, int sorrow, int growth, int cease)
	{
		if (joy < 0 || sorrow < 0 || growth < 0 || cease < 0)
		{
			return false;
		}
		else
		{
			for (; joy > 0 ; joy--)
			{
				increase(JOY);
			}
			for (; sorrow > 0 ; sorrow--)
			{
				increase(SORROW);
			}
			for (; growth > 0 ; growth--)
			{
				increase(GROWTH);
			}
			for (; cease > 0 ; cease--)
			{
				increase(CEASE);
			}
			return true;
		}
	}
	
	public boolean hasZeroEmotion()
	{
		return getVectorSize(JOY) == EPower.None &&
				getVectorSize(SORROW) == EPower.None &&
				getVectorSize(GROWTH) == EPower.None &&
				getVectorSize(CEASE) == EPower.None;
	}
}
