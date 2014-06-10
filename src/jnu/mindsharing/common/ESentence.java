package jnu.mindsharing.common;

import java.util.ArrayList;

/**
 * 문장에 포함된 감정요소를 담고 있는 EmoUnit을 포함하고 있는 ESentence 클래스이다. 이것의 상위 객체로는 EParagraph가 있다.
 * 
 * @author nidev
 * @see EParagraph
 *
 */
public class ESentence extends ArrayList<EmoUnit> implements EmoSkeleton
{
	private static final long serialVersionUID = 996822297786456411L;
	private String originalText;
	
	/**
	 * 분석 전의 원문 텍스트와 함께 ESentence 객체를 생성한다.
	 * @param text 원문
	 */
	public ESentence(String text)
	{
		originalText = text;
	}
	
	@Override
	public String getWholeText()
	{
		return originalText;
	}

	@Override
	public String[] getTexts()
	{
		String[] arr = {originalText};
		return arr;
	}
	
	public EmoUnit getLastEmoUnit()
	{
		if (size() == 0)
		{
			return null;
		}
		return get(size()-1);
	}
	
	@Override
	public int length()
	{
		return size();
	}
	
	/**
	 * ArrayList.add 메소드를 오버라이드하여, 무엇이 여기에 추가하는지 디버깅할 수 있는 메시지 출력을 추가했다.
	 * @param e EmoUnit 객체
	 * @return true(추가 성공시), false(추가에 실패한 경우)
	 */
	@Override
	public boolean add(EmoUnit e)
	{
		P.d("ES.ADD", "%s:%s", e.getOrigin(), e.getTag().toString());
		return super.add(e);
	}
	
	/**
	 * WordTag.Subject 를 갖고 있는 EmoUnit이 어레이리스트 내부에 존재하는지 확인한다.
	 * @see EmoUnit
	 * @return true(주어 어휘가 존재하는 경우), false(주어 어휘가 없음)
	 */
	public boolean hasSubject()
	{
		for (EmoUnit em: this)
		{
			if (em.getTag() == EmoUnit.WordTag.Subject)
			{
				return true;
			}
		}
		return false;
	}
	
	/**
	 * WordTag.Subject 로 태그된 첫번째 어휘를 반환한다. 없다면 null을 반환한다.
	 * @see EmoUnit
	 * @return WordTag.Subject로 태그된 EmoUnit 객체, 또는 null
	 */
	public EmoUnit getSubject()
	{
		for (EmoUnit em: this)
		{
			if (em.getTag() == EmoUnit.WordTag.Subject)
			{
				return em;
			}
		}
		return null;
	}

	
	/**
	 * 어레이리스트 내부에 WordTag.Skip 으로 태그된 어휘들을 모두 삭제하고, 메모리 확보작업을 수행한다.
	 * @see EmoUnit 
	 */
	public void compactSkips()
	{
		// Compaction!
		// Skip 태그들을 모두 null 로 마크하고 제거한다.
		for (int es_idx=0; es_idx < size() ; es_idx++)
		{
			if (get(es_idx).getTag() == EmoUnit.WordTag.Skip)
			{
				set(es_idx, null);
			}
		}
		while(remove(null)); // 합친 후 null 모두 제거
		trimToSize();
	}

}
