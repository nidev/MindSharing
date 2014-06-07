package jnu.mindsharing.chainengine;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

/**
 * HistoriaModule 은 ChainEngine에서 분석한 결과를, 학습시키는 목표로 제작된 모듈이다.
 * 현재는 데이터베이스를 지원하지않으며, 메모리 상에서 휘발성 데이터로 기억을 하게 된다.
 * 
 * 해시맵은 key:어휘 => value: double[] 문장 감정값의 log10 값
 * 
 * 
 * @author nidev
 *
 */
public class HistoriaModule
{
	private HashMap<String, double[]> history;
	private long timeBegin;

	public HistoriaModule()
	{
		timeBegin = System.currentTimeMillis();
		history = new HashMap<String, double[]>();
	}
	
	public void addHistory(String word)
	{
		double[] void_value = new double[4];
		void_value[0] = 0;
		void_value[1] = 0;
		void_value[2] = 0;
		void_value[3] = 0;
		
		history.put(word, void_value);
	}
	
	public void addHistory(String word, double[] contextEmo)
	{
		if (!history.containsKey(word))
		{
			addHistory(word);
		}
		
		double[] stored_value = getHistory(word);
		double[] new_value = new double[4];
		// 음수로 가지 않도록 하기 위해서, 항상 로그값을 0이상으로 만들어준다.
		new_value[0] = stored_value[0] + Math.log10(contextEmo[0]+1);
		new_value[1] = stored_value[1] + Math.log10(contextEmo[1]+1);
		new_value[2] = stored_value[2] + Math.log10(contextEmo[2]+1);
		new_value[3] = stored_value[3] + Math.log10(contextEmo[3]+1);
		history.put(word, new_value);
	}
	
	public String findWord(String keyword)
	{
		for (String key: history.keySet())
		{
			if (key.equals(keyword))
			{
				return key;
			}
		}
		return null;
	}
	
	public double[] getHistory(String word)
	{
		if (history.containsKey(word))
		{
			return history.get(word);
		}
		else
		{
			return null;
		}
	}
	
	public String digest()
	{
		String br = "\r\n";
		StringBuffer s = new StringBuffer();
		s.append("HistoriaModule Digest (Machine Learning) - ");
		s.append(String.format("%d words are learned.%s", history.size(), br));
		s.append(br);
		s.append("===================================================================");s.append(br);
		s.append("| Word(Descriptor)  | External weight for JOY/SORROW/GROWTH/CEASE |");s.append(br);
		s.append("===================================================================");s.append(br);
		for (String word : history.keySet())
		{
			double[] v = history.get(word);
			s.append(String.format("%20s |  %.5f / %.5f / %.5f / %.5f%s", word, v[0], v[1], v[2], v[3], br));
		}
		s.append("===================================================================");s.append(br);
		SimpleDateFormat timeformat = new SimpleDateFormat("yyyy-MM-dd a hh:mm:ss");
		s.append("Historia has made since " + timeformat.format(new Date(timeBegin)) + ".");s.append(br);
		
		return s.toString(); 
	}
	
	public void printDigest()
	{
		System.out.println(digest());
	}

}
