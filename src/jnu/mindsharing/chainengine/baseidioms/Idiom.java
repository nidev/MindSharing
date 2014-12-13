package jnu.mindsharing.chainengine.baseidioms;

import jnu.mindsharing.common.P;

public class Idiom
{
	private String word;
	private int vector_e;
	private int vector_s;
	
	public Idiom(String _w, int _e, int _s) // emotional/progression
	{
		word = _w;
		vector_e = _e;
		vector_s = _s;
	}
	
	public void desc()
	{
		P.d("Idiom", "어휘 객체 보기 (%s, %d, %d)", word, vector_e, vector_s);
	}
	
	public int base_e()
	{
		return vector_e;
	}
	
	public int base_s()
	{
		return vector_s;
	}
	
	@Override
	public String toString()
	{
		return word;
	}
	
}