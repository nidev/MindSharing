package jnu.mindsharing.chainengine;

import java.util.ArrayList;
import java.util.HashMap;

import jnu.mindsharing.common.P;

import org.snu.ids.ha.ma.Eojeol;
import org.snu.ids.ha.ma.Sentence;

public class EmotionAlgorithm
{
	// Constants
	// 3가지 긍정 감정축과 3가지 부정 감정축
	private String[] emotkeys = {"rise", "create", "develop", "fall", "destroy", "cease"};
	// Learning history
	private HashMap<String, Long> emotable;
	private ArrayList<Long> emoflow;
	private ArrayList<String> learned_word;
	private String TAG = "EmoAl";
	
	public EmotionAlgorithm()
	{
		// 초기 상태로 시작함. 아무런 상태를 갖지않음.
		emotable = new HashMap<String, Long>();
		for (String emotkey: emotkeys)
		{
			emotable.put(emotkey, 0L);
		}
		emoflow = new ArrayList<Long>();
		learned_word = new ArrayList<String>();
	}
	
	public EmotionAlgorithm(EmotionAlgorithm ea)
	{
		// 학습된 결과를 가져와서 재학습함
		// TODO: 값과 배운 문장을 기억함
		emotable = new HashMap<String, Long>();
		for (String emotkey: emotkeys)
		{
			emotable.put(emotkey, ea.emotable.get(emotkey));
		}
		emoflow = new ArrayList<Long>();
		emoflow.addAll(ea.emoflow);
		learned_word = new ArrayList<String>();
		learned_word.addAll(ea.learned_word);
		TAG = "EmoAl:Successor";
	}
	
	public void reset()
	{
		// TODO: 여지까지 배운 값들을 모두 잊음
		for (String emotkey: emotkeys)
		{
			emotable.put(emotkey, 0L);
		}
		emoflow.clear();
		learned_word.clear();
	}
	
	public long currentEmotionRate()
	{
		// TODO: 계산 공식 추가
		// 일단은 가장 심플한 방법으로 계산한다.
		long sum = 0;
		for (long value :emotable.values())
		{
			sum += Math.abs(value);
		}
		return sum;
	}
	
	public void listen(Sentence se, String target)
	{
		Pepero ppr = new Pepero(se);
		while (!ppr.kissed())
		{
			Eojeol token = ppr.readFront();
			P.d(TAG, "어절 : %s", token.toString());
			if (target != null)
			{
				// 주어: 키워드, 목적어: 키워드
				// 키워드의 흐름
			}
			else
			{
				// 주어: 나, 목적어: 나
				//일반 흐름
			}
		}
	}
	
	public void listen(Sentence se)
	{
		listen(se, null);
	}
	
	public void symphathize(EmotionAlgorithm ea)
	{
		// 공감 함수
		// 이 것은 다른 알고리즘이 학습한 감정만을 가져온다.
		// 지금까지 발생했던 어휘와 이력을 가져오는 new EmotionAlgorithm(object_EmotionAlgorithm)과는 다름
		for (String emotkey: emotkeys)
		{
			emotable.put(emotkey, ea.emotable.get(emotkey));
		}
	}
	
	public CEResultObject toCEResultObject()
	{
		CEResultObject cer = new CEResultObject();
		return cer;
	}
}
