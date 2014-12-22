package jnu.mindsharing.common;


public class DescOpHelper
{
	// DescOp 안에는 꼬꼬마 형태소 분석기 태그의 MAG나 ECE에 해당하는 어휘가 입력된다.
	// ECE: 고(~하고), 지만(~하지만 ~하다), ㄴ데(그러한데)
	// MAG: 너무, 매우, 정말, 조금(뒤에 명사나 서술어가 올때 부사로 처리됨), 약간, 안(아니), 별로,
	final String[] join = {"고", "지만", "ㄴ데"};
	final String[] negate_forward = {"안", "별로" };
	final String[] negate_backward = {"지"}; // ~지 않다, ~
	final String[] emphasize = {"너무", "매우", "정말", "참"};
	final String[] minimize = {"조금", "약간", "살짝", "좀"};
	
	public DescOpHelper()
	{
		;
	}
	
	public boolean isIn(String[] array, String target)
	{
		for (String key : array)
		{
			if (key.equals(target)) return true;
		}
		return false;
	}
	
	public String identify(String word)
	{
		if (isIn(join, word))
			return XTag_logical.DescOp_Join;
		else if (isIn(negate_forward, word))
			return XTag_logical.DescOp_InvertNext;
		else if (isIn(negate_backward, word))
			return XTag_logical.DescOp_JoinInverted;
		else if (isIn(emphasize, word))
			return XTag_logical.DescOp_Increase;
		else if (isIn(minimize, word))
			return XTag_logical.DescOp_Decrease;
		else
			return XTag_logical.DescOp_Undefined;
	}
	
}
