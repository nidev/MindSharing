package jnu.mindsharing.common;

import java.util.ArrayList;

public class EParagraph extends ArrayList<ESentence> implements EmoSkeleton
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -3413429082720166182L;
	
	private String originalText;

	public EParagraph(String text)
	{
		originalText = text;
	}

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
		// TODO Auto-generated method stub
		return size();
	}
	
	

}
