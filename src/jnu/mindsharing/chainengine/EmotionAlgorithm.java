package jnu.mindsharing.chainengine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Stack;

import org.snu.ids.ha.ma.Eojeol;
import org.snu.ids.ha.ma.Sentence;

public class EmotionAlgorithm
{
	// Constants
	private String[] emotkeys = {"rise", "create", "develop", "fall", "destroy", "cease"};
	// Learning history
	private HashMap<String, Long> emotable;
	private ArrayList<Long> emoflow;
	private ArrayList<String> learned_word;
	
	// Operations
	private Stack<Eojeol> backtoken;
	private int forward_pos; // pepero mode
	private int backward_pos; // pepero mode
	
	public EmotionAlgorithm()
	{
		// 초기 상태로 시작함. 아무런 상태를 갖지않음.
	}
	
	public EmotionAlgorithm(EmotionAlgorithm ea)
	{
		// 학습된 결과를 가져와서 재학습함
		// TODO: 값과 배운 문장을 기억함
		
	}
	
	public void in()
	{
		// TODO: 여지까지 배운 값들을 모두 잊음
	}
	
	public long currentEmotionRate()
	{
		// TODO: 계산 공식 추가
		return 0;
	}
	
	public void listen(Sentence se)
	{
		// 문장 흐름 이해를 위해서 스택에 순서대로 거꾸로 집어넣음
		for (Eojeol eo : se)
		{
			backtoken.push(eo);
		}
		/*
		for( int j = 0; j < st.size(); j++ )
		{
			System.out.println(st.get(j));
		}
		*/
	}
	
	public void symphathize(EmotionAlgorithm ea)
	{
		// 공감 함수
		// 이 것은 다른 알고리즘이 학습한 감정만을 가져온다.
		// 지금까지 발생했던 어휘와 이력을 가져오는 new EmotionAlgorithm(object_EmotionAlgorithm)과는 다름
	}
	
	public CEResultObject toCEResultObject()
	{
		return null;
	}

}
