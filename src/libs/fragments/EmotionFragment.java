package libs.fragments;

import java.util.HashMap;

// 감정값을 담기 위한 전용 프래그먼트
public class EmotionFragment extends BaseFragment
{
	// 계산된 감정 평균값
	public int emotionValue = 0;
	// 분석 결과 저장을 위한 HashMap
	public HashMap<String, Integer> text_to_emotion_map = null;
	
	public EmotionFragment(String p_sourceText)
	{
		// 감정 단어는 하나만 등록된다고 가정한다.
		super(p_sourceText);
	}
	
	public int getAverageEmotionValue() // 평균 감정값 리턴
	{
		return emotionValue;
	}
	
	/*
	public HashMap<String, Integer> getEmotionVectors() // 분석 결과가 저장된 HashMap 전체 리턴
	{
		HashMap<String, Integer> emotion_map = new HashMap<String, Integer>();
		for (BaseFragment fragment : fragments)
		{
			//emotion_map.put(fragment.getSourceText(), fragment.getAverageEmotionVector());
		}
		return emotion_map;
	}
	*/
}
