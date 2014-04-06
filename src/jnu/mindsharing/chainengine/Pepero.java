package jnu.mindsharing.chainengine;

import org.snu.ids.ha.ma.Eojeol;
import org.snu.ids.ha.ma.Sentence;

import jnu.mindsharing.common.PeperoLogics;

public class Pepero implements PeperoLogics
{
	private Sentence se;
	private int f;
	private int b;
	
	public Pepero(Sentence oneSentence)
	{
		se = oneSentence;
		f = 0;
		b = se.size()-1;
	}

	@Override
	public int length()
	{
		return se.size();
	}

	@Override
	public int posFront()
	{
		return f;
	}

	@Override
	public int posBack()
	{
		return b;
	}

	@Override
	public Eojeol readFront()
	{
		Eojeol fe = se.get(f);
		f++;
		return fe;
	}

	@Override
	public Eojeol readBack()
	{
		Eojeol be = se.get(b);
		b--;
		return be;
	}

	@Override
	public boolean kissed()
	{
		return f > b;
	}

	@Override
	public Eojeol peekFrontPrev()
	{
		return se.get(f-1);
	}

	@Override
	public Eojeol peekFrontNext()
	{
		return se.get(f+1);
	}

	@Override
	public Eojeol[] peekFrontBoth()
	{
		Eojeol fb[] = {se.get(f-1), se.get(f+1)};
		return fb;
	}

	@Override
	public Eojeol peekBackPrev()
	{
		return se.get(b+1);
	}

	@Override
	public Eojeol peekBackNext()
	{
		return se.get(b-1);
	}

	@Override
	public Eojeol[] peekBackBoth()
	{
		Eojeol bb[] = {se.get(b+1), se.get(b-1)};
		return bb;
	}

	@Override
	public Sentence getSubSentence()
	{
		return null;
	}

	@Override
	public void defaultFront()
	{
		f = 0;
	}

	@Override
	public void defaultBack()
	{
		b = 0;
	}
}
