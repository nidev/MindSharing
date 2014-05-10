package jnu.mindsharing.common;

import java.util.ArrayList;

public class ESentence extends ArrayList<EmoUnit> implements EmoSkeleton
{
	private static final long serialVersionUID = 996822297786456411L;
	private String originalText;
	/*
	 * 이 자체가 EmoUnit을 저장할 수 있는 ArrayList이다. 잊지 말 것.
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
	
	@Override
	public boolean add(EmoUnit e)
	{
		P.d("ES.ADD", "%s:%s", e.getOrigin(), e.getTag().toString());
		return super.add(e);
	}
	
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
	
	

}
