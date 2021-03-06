package jnu.mindsharing.chainengine;

import jnu.mindsharing.common.P;

import org.snu.ids.ha.ma.MCandidate;
import org.snu.ids.ha.ma.MExpression;

/**
 * 세종 말뭉치를 사용한 꼬꼬마 형태소 분석기의 결과 중 일부를 정제하기 위한 클래스이다.
 * '-하다' 의 처리와, '조금'이라는 어휘를 명사로 취급하여 부사로 처리되지 않는 문제를 임기응변한다.
 * 
 * @author nidev
 *
 */
public class SejongMCandidateRefiner
{
	/**
	 * 약간의 트릭을 사용해 형태소 후보를 정제한다.
	 * 
	 * @param mexp 꼬꼬마 형태소 분석기의 MExpression 객체(형태소 후보 배열)
	 * @return 최적의 형태소 후보
	 */
	public static MCandidate refineAndSelectBest(MExpression mexp)
	{		
		// 가난하다, 건강하다와 같은 표현에서 문제가 생겨서 회피하려고 함
		if (mexp.getExp().contains("하다"))
		{
			// 국어사전을 찾아본 결과, '-하다'를 고유명사로 사용하는 경우는 없음.
			// 설령 있다고 해도 빈도는 매우 낮다. 따라서, '하다'를 고유 명사로 취급하는 형태소 후보는 삭제되어야한다.
			// 그게 1번 후보에 있는게 꼬꼬마 형태소 분석기의 문제.
			P.e("SJRefine", "\'--하다\' 표현에서, 첫번째 형태소 후보를 제거합니다.");
			P.e("SJRefine", "제거된 후보는 다음과 같습니다: %s", mexp.get(0).toSimpleStr());
			mexp.remove(0);
		}
		else if (mexp.getExp().equals("조금"))
		{
			// '조금'을 명사로 쓰는 빈도는 비교적 낮음.
			// 이미지 추출을 위해서, 명사 어휘를 희생한다.
			mexp.remove(0);
		}
		
		return mexp.getBest();
	}

}
