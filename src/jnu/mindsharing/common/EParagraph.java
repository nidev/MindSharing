package jnu.mindsharing.common;

import java.util.ArrayList;

/**
 * 
 * ESentence 객체들을 담는 EParagraph 클래스이다. 단순히 ArrayList로써의 역할 외에도, 약간의 메소드를 제공한다.
 * 
 * @author nidev
 *
 */
public class EParagraph extends ArrayList<ESentence> implements EmoSkeleton
{
	private static final long serialVersionUID = -3413429082720166182L;
	
	private String originalText;
	
	/**
	 * 분석 전의 문단 원문을 넣어 객체를 생성한다.
	 * @param text 문단 문자열 
	 */
	public EParagraph(String text)
	{
		originalText = text;
	}

	/**
	 * 주어진 문자열에 갖고 있는 ESentence 객체를 반환한다. 없다면 null을 반환한다.
	 * @param text 검색어
	 * @return ESentence 객체, 또는 null
	 */
	public ESentence findESentenceByText(String text)
	{
		for (ESentence es: this)
		{
			if (es.getWholeText().equals(text))
			{
				return es;
			}
		}
		return null;
	}

	@Override
	public String getWholeText()
	{
		return originalText;
	}

	@Override
	public String[] getTexts()
	{
		return (String[]) toArray();
	}

	@Override
	public int length()
	{
		return size();
	}
}
