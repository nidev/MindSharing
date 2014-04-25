package jnu.mindsharing.common;

import java.util.ArrayList;
import java.util.HashMap;

public class ESentence extends ArrayList<EmoUnit> implements EmoSkeleton
{
	private static final long serialVersionUID = 996822297786456411L;
	private String originalText;
	/*
	 * 이 자체가 EmoUnit을 저장할 수 있는 ArrayList이다. 잊지 말 것.
	 */
	public HashMap<String, EmoUnit> emotions; 

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

	@Override
	public int length()
	{
		return size();
	}
	
	public int eUnitLength()
	{
		return emotions.size();
	}

}
