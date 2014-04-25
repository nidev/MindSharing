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
	protected static enum EPower {None, Formal,  Mild, Strong, Extreme};
	protected static int INVALID_ENUM = -1;
	final String JOY = "joy";
	final String SORROW = "sorrow";
	final String GROWTH = "growth";
	final String CEASE = "cease";
	
	final String[] vectorTitles = {JOY, SORROW, GROWTH, CEASE};
	
	private HashMap<String, Enum<EPower>> vectorTable;
	private String emotionOrigin = null;
	private String TAG = "EmoUnit";
	

	public EmoUnit()
	{
		vectorTable = new HashMap<String, Enum<EPower>>();
		defaultTable();
	}
	
	public EmoUnit(String source)
	{
		emotionOrigin = source;
		
	}
	
	public void defaultTable()
	{
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
	
	public void increase(String title)
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
	}
	
	public void decrease(String title)
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
	}
	
	public void invertEmotionals()
	{
		// This function will exchange each strength of two vectors.
		// current Joy -> next Sorrow
		// current Sorrow -> next Joy
		Enum<EPower> tmp;
		tmp = vectorTable.get(JOY);
		vectorTable.put(JOY, vectorTable.get(SORROW));
		vectorTable.put(SORROW, tmp);
		P.d(TAG, "감정 반전이 발생하였습니다.");
	}
	
	public void invertConditionals()
	{
		// This function will exchange each strength of two vectors.
		// current Growth -> next Cease
		// current Cease -> next Growth
		Enum<EPower> tmp;
		tmp = vectorTable.get(GROWTH);
		vectorTable.put(GROWTH, vectorTable.get(CEASE));
		vectorTable.put(CEASE, tmp);
		P.d(TAG, "상태/상황 반전이 발생하였습니다.");
	}
	
	public void invertAll()
	{
		invertEmotionals();
		invertConditionals();
	}
}
