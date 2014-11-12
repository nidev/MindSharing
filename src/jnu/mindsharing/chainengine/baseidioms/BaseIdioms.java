package jnu.mindsharing.chainengine.baseidioms;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.StringTokenizer;

import jnu.mindsharing.common.P;

public class BaseIdioms
{
	public static enum DSC {SET20, SET50, SET100, SET200};
	public class Idiom
	{
		String w;
		int e;
		int p;
		
		public Idiom(String w, int e, int p) // emotional/progression
		{
			this.w = w;
			this.e = e;
			this.p = p;
		}
		
		public int getE()
		{
			return e;
		}
		
		public int getP()
		{
			return p;
		}
		
		@Override
		public String toString()
		{
			return w;
		}
		
	}
	private final String TAG="BaseIdioms";
	private ArrayList<Idiom> idioms;
	
	
	public BaseIdioms()
	{
		idioms = new ArrayList<Idiom>();
	}
	
	public synchronized boolean loadSet(DSC datasetLength) throws Exception
	{
		InputStream source=null;
		switch (datasetLength)
		{
		case SET20:
			source = getClass().getResourceAsStream("set-020.csv");
			break;
		case SET50:
			source = getClass().getResourceAsStream("set-050.csv");
			break;
		case SET100:
			source = getClass().getResourceAsStream("set-100.csv");
			break;
		case SET200:
			source = getClass().getResourceAsStream("set-200.csv");
			break;
		default:
			throw new Exception("Invalid datasetLength is given. Available enums are SET20/50/100/200.");
			
		}
		
		idioms.clear();
		
		BufferedReader br = new BufferedReader(new InputStreamReader(source));
		String line;
		while ((line = br.readLine()) != null)
		{
			StringTokenizer rcsv = new StringTokenizer(line, ",");
			// WordName, E Prob(-1, +1),  
			idioms.add(new Idiom(rcsv.nextToken(), Integer.valueOf(rcsv.nextToken()), Integer.valueOf(rcsv.nextToken())));
	
		}
		br.close();
		P.d(TAG, "기본 단어 세트 변경: %s", datasetLength.toString());
		return false; // stub
	}
	
	public boolean isLoaded()
	{
		if (idioms.size() == 0)
		{
			return false;
		}
		else
		{
			return true;
		}
	}
	
	public ArrayList<Idiom> retIdioms()
	{
		return idioms;
	}
}