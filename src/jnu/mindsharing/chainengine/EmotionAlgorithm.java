package jnu.mindsharing.chainengine;

import java.util.ArrayList;

import jnu.mindsharing.common.HList;
import jnu.mindsharing.common.Hana;
import jnu.mindsharing.common.P;
import jnu.mindsharing.common.XTag_atomize;
import jnu.mindsharing.common.XTag_logical;

import org.snu.ids.ha.ma.MorphemeAnalyzer;

/**
 * 텍스트에서 감정을 추출해내는 알고리즘이 있는 클래스이다. 이곳에 텍스트를 추가하는 건 엔진의 역할이다.
 * 
 * @see TextPreprocessor
 * @author nidev
 *
 */
public class EmotionAlgorithm
{
	private static final long serialVersionUID = -6004998827195979333L;
	
	private String TAG = "Algorithm";
	private MorphemeAnalyzer ma;
	private HList hlst;
	
	enum ESENTENCE_TRAVERSE_MODE {NORMAL, INCREASE_NEXT, DECREASE_NEXT, INVERT_NEXT, DESC_JOIN};
	
	/**
	 * 알고리즘 클래스 초기화
	 * @param hostMA 엔진에서 제공한 꼬꼬마 형태소 분석기 객체
	 * @param ml 엔진에서 제공한 학습기 모듈 객체
	 */
	public EmotionAlgorithm(MorphemeAnalyzer hostMA)
	{
		// TODO: null 체크
		ma = hostMA;
	}
	
	/**
	 * HList 객체 내부 데이터를 검토한다.
	 * @param hl HList 객체
	 */
	public void inspectHList(HList hl)
	{
		// for debugging
		P.e(TAG,  "Item Inspector");
		for (Hana inspect_hn: hl)
		{
			if (inspect_hn == null)
			{
				P.e(TAG, " =inspect> null");
			}
			else
			{
				P.e(TAG, " =inspect> content: %s", inspect_hn.toString() + "/" + inspect_hn.getXTag());
			}
		}
		P.e(TAG, "Inspector end");
	}
	
	/**
	 * 주어진 인덱스에 해당하는 문단을 처리한다. 예외가 발생할 수 있다.
	 * @param paragraph_index EParagraph의 문단 인덱스
	 */
	public ArrayList<HList> feed(String sourceText) throws Exception
	{
		// phase 1: 문장마다 형태소 분석 후 어휘 탐색
		P.d(TAG, "Process");
		Sense ss = new Sense();
		
		// XXX: 왜 null?
		if (ss == null) continue;
		
		TextPreprocessor preproc = new TextPreprocessor(sourceText);
		
		// 전처리기를 사용해서 전처리 작업을 모두 수행한다.
		// TextPreprocessor.java 참고
		preproc.performUnquoting(); // 따옴표 해체
		preproc.performTagging(ma); // 세종 말뭉치 태그를 내부 태그로 변환함
		preproc.performJointing(); // 명사 어휘 결합 
		
		if (!preproc.isEverythingDone())
		{
			throw new Exception("텍스트 전처리 부분에서 실패함");
		}
		
		HList hlist = preproc.export();
		
		// 현재 preprocessor를 통과한 모든 어휘는 기본적인 감정값 평가가 모두 완료되어있다!
		// 복합 서술어 처리
		// '크게 퍼뜨리다' 를 '크다/퍼뜨리다'로 결합시켜 새로운 정보 객체를 구성한다.
		// 구성이 완료되면 이전 객체는 null 로 설정하고 compaction 시에 사라질 수 있도록 변경한다.
		for (int hn_idx=0; hn_idx < hlist.size() ; hn_idx++)
		{
			if (hlist.get(hn_idx).getXTag().equals(XTag_atomize.DescOp))
			{
				Hana base = hlist.get(hn_idx);
				// Increase와 Decrease는 출력 레벨을 조절한다.
				// Log10 을 출력함수로 사용한다면 10 으로 레벨을 감소하거나 증가시킨다.
				// logE를 사용한다면 e 로 조절
				String descop_type = identifyDescOp(base.toString());
				if (descop_type.equals(XTag_logical.DescOp_Decrease))
				{
					
				}
				else if (descop_type.equals(XTag_logical.DescOp_Increase))
				{
					
				}
				else if (descop_type.equals(XTag_logical.DescOp_InvertPrev))
				{
					
				}
				else if (descop_type.equals(XTag_logical.DescOp_InvertNext))
				{
					
				}
				else
				{
					// should not reach here, or skip
				}
			}
		}
		
		hlist.compaction();
		
		for (int hn_idx=0; hn_idx < hlist.size(); hn_idx++)
		{
			if (hlist.get(hn_idx).getXTag().equals(XTag_atomize.DescOp))
			{
				Hana base = hlist.get(hn_idx);
				// 여기에서 부사 + 형용사 -> 형용사, 반전이나 강화/감소 연산이 완료된 서술어를 필요하다면 연결한다.
				String descop_type = identifyDescOp(base.toString());
				if (descop_type.equals(XTag_logical.DescOp_Join))
				{
					
					// NullPointerException 으로부터 first, prev, next, last 연산은 안전하므로
					// 마음 놓고 비교가 가능하다.
					if (hlist.prev(hn_idx).getXTag().equals(XTag_atomize.Desc) &&
						hlist.next(hn_idx).getXTag().equals(XTag_atomize.Desc))
					{
						Hana base_hn = hlist.prev(hn_idx);
						Hana aux_hn = hlist.next(hn_idx);
						Hana new_hn = new Hana(base_hn.toString() + "/" + aux_hn.toString());
						
						new_hn.merge(base_hn).merge(aux_hn);
						new_hn.setXTag(XTag_atomize.Desc);
						hlist.prev(hn_idx).setXTag(XTag_atomize.Skip);
						hlist.next(hn_idx).setXTag(XTag_atomize.Skip);
						hlist.set(hn_idx+1, new_hn);
						
					}
				}
			}
		}
		// TODO: 있다, 없다, 하다 에 대한 처리도 필요함. 이 경우는 Object Desc(sentence end)식으로 구성된 경우가 많음.
		// Compaction 한번 더 실시
		hlist.compaction();
		
		// TODO: 현재 hlist 내부의 어휘에 대해 논리 관계를 파악하며 EndOfSentence 키워드를 기준으로 문장을 분할한다.
		// 문장 요소 별로 HList 를 새로 구성하고, 주어와 목적어, 서술어<->부사 사이의 관계를 정리한다.
		ArrayList<HList> allText;
		// allText 내부의 HList는 다음과 같은 구조로 구성한다.
		//   0                       |   1   2   3   4  5  6  7  8  9 .... ... last
		// --------------------------|---------------------------------------------------------------
		// 머리 부분으로써 문장 전체 |  주어 ................................. 주어서술어
		// 아무런 내용이 없더라도 0번은 항상 존재해야한다.
		// 삽입 순서는 주어/{서술어}/주어서술 동사
		
		// HList 내부의 인칭 문제를 해결한다. 여기에서 주어를 발견할 수 없다면, Object 중에 주어를 선정하고, 그게 안되면 주어 '나'를 삽입한다.
		// 아직 'Marker'와 'Skip'으로 남아있는 어휘를 모두 제거한다, NextDescDepender를 읽고 전후 관계를 파악한다.
		// 

		// 주어가 없는 경우에 탐색 작업을 진행 (Object 끌어올림)
		if (!hlist.hasSubject())
		{
			// 혹시 대명사가 주어일 수도 있다. (우선순위 1)
			for (Hana em:es)
			{
				if (hn.getXTag() == XTag_atomize.ReferenceMarker)
				{
					hn.setTag(XTag_atomize.Subject);
					break;
				}
			}
			
			if (!hlist.hasSubject())
			{
				// 일단, 첫번째로 Object 로 마크된 Hana를 주어로 선정한다. (우선순위 2)
				for (Hana em : es)
				{
					if (hn.getXTag() == XTag_atomize.Object)
					{
						// XXX: 현재 휴리스틱 규칙은 없음
						// 분명 오류를 내는 알고리즘임
						hn.setTag(XTag_atomize.Subject);
						break;
					}
						
				}
			}
			
			if (!hlist.hasSubject())
			{
				// .. 최후의 방법. (우선순위 3)
				hlist.add(0, new Hana("나").setTag(XTag_atomize.Subject));
			}
		}
		
		
		// TODO: HList를 연산하여 감정값을 내놓는다. 
		
		
		
		
		// ^^^^^^^^^^^ 관계 분석 단계에서 가능한 모든 결합조건을 파악해서 문장 태그를 마쳤다.
		// 문장의 감정 정보를 정리하고, 모르는 어휘에 대한 학습을 마친다.

		
		//TODO:  allText로부터 글 전체의 감정값을 요약한다. 강세 어휘를 추출한다.
		
		return allText;
	}
	
	/**
	 * DescOp 으로 분류된 어휘가 어떤 연산을 수행하는지 분류해낸다.
	 * @return 연산자 유형 정수로 반환한다.
	 */
	public String identifyDescOp(String word)
	{
		// DescOp 안에는 꼬꼬마 형태소 분석기 태그의 MAG나 ECE에 해당하는 어휘가 입력된다.
		// ECE: 고(~하고), 지만(~하지만 ~하다), ㄴ데(그러한데)
		// MAG: 너무, 매우, 정말, 조금(뒤에 명사나 서술어가 올때 부사로 처리됨), 약간, 안(아니), 별로, 
		
		return ""; // stub
	}
	
	/**
	 * ResultProcessor 객체를, 알고리즘 객체로부터 추출한다.
	 * @return JSON이나 TXT로 출력이 준비된 ResultProcessor 객체
	 */
	public static ResultProcessor extractResultProcessor(ArrayList<HList> hlist)
	{
		ResultProcessor resp = new ResultProcessor(hlist);
		return resp;
	}
	
	/**
	 * EmotionAlgorithm 객체의 상태를 보여준다.
	 */
	public void displayEStatus()
	{
		P.d(TAG, "형태소 분석기 상태: ");
		P.d(TAG, "Sense 모듈 상태: ");
	}
}
