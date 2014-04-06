/**
 * 
 */
package jnu.mindsharing.common;

import org.snu.ids.ha.ma.Eojeol;
import org.snu.ids.ha.ma.Sentence;

/**
 * @author nidev
 *
 */
public interface PeperoLogics
{
	public int length();
	public void defaultFront();
	public void defaultBack();
	public int posFront();
	public int posBack();
	public Eojeol readFront(); // w[f] -> w[f+1] (단 f+1 < b), f++
	public Eojeol readBack(); // w[b] -> w[b-1] (단 f < b-1), b--
	public boolean kissed(); // if f == b, halt condition
	
	/*
	 * 나 는 철수 에게 밥 먹 고 싶 다 는 말 을 들 었 다
	 *         ^Front                                ^Back
	 *    '들'에 현재 Back이 있다면 에서 '을' 토큰과 '었' 토큰을 추출함
	 */
	public Eojeol peekFrontPrev();
	
	/*
	 * 나 는 철수 에게 밥 먹 고 싶 다 는 말 을 들 었 다
	 *         ^Front                                ^Back
	 *    '들'에 현재 Back이 있다면 에서 '을' 토큰과 '었' 토큰을 추출함
	 */
	public Eojeol peekFrontNext();
	
	/*
	 * 나 는 철수 에게 밥 먹 고 싶 다 는 말 을 들 었 다
	 *         ^Front                                ^Back
	 *    '들'에 현재 Back이 있다면 에서 '을' 토큰과 '었' 토큰을 추출함
	 */
	public Eojeol[] peekFrontBoth();
	
	/*
	 * 나 는 철수 에게 밥 먹 고 싶 다 는 말 을 들 었 다
	 *         ^Front                                ^Back
	 *    '들'에 현재 Back이 있다면 에서 '을' 토큰을 추출함
	 */
	public Eojeol peekBackPrev();
	
	/*
	 * 나 는 철수 에게 밥 먹 고 싶 다 는 말 을 들 었 다
	 *         ^Front                                ^Back
	 *    '들'에 현재 Back이 있다면 에서 '었' 토큰을 추출함
	 */
	public Eojeol peekBackNext();
	
	/*
	 * 나 는 철수 에게 밥 먹 고 싶 다 는 말 을 들 었 다
	 *         ^Front                                ^Back
	 *    '들'에 현재 Back이 있다면 에서 '을' 토큰과 '었' 토큰을 추출함
	 */
	public Eojeol[] peekBackBoth();
	
	/*
	 * 나 는 철수 에게 밥 먹 고 싶 다 는 말 을 들 었 다
	 *     ^Front                          ^Back
	 *     일 경우, 철수에게 밥 먹고 싶다 를 새 문장으로 추출함
	 */
	public Sentence getSubSentence(); // 안은 문장 분리
}
