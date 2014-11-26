package jnu.mindsharing.chainengine;

import java.util.ArrayList;

import jnu.mindsharing.common.DescOpHelper;
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
		
		TextPreprocessor preproc = new TextPreprocessor(sourceText);
		
		// 전처리기를 사용해서 전처리 작업을 모두 수행한다.
		// TextPreprocessor.java 참고
		HList atoms = preproc.atomize(ma);
		
		// 현재 preprocessor를 통과한 모든 어휘는 기본적인 감정값 평가가 모두 완료되어있다!
		// 복합 서술어 처리
		// 구성이 완료되면 이전 객체는 null 로 설정하고 compaction 시에 사라질 수 있도록 변경한다.
		// 문장 요소 별로 HList 를 새로 구성하고, 주어와 목적어, 서술어<->부사 사이의 관계를 정리한다.
		for (int hn_idx=0; hn_idx < atoms.size() ; hn_idx++)
		{
			if (atoms.get(hn_idx).getXTag().equals(XTag_atomize.DescOp))
			{
				Hana base = atoms.get(hn_idx);
				// Increase와 Decrease는 출력 레벨을 조절한다.
				// Log10 을 출력함수로 사용한다면 10 으로 레벨을 감소하거나 증가시킨다.
				// logE를 사용한다면 e 로 조절
				String descop_type = identifyDescOp(base.toString());
				if (descop_type.equals(XTag_logical.DescOp_Decrease))
				{
					// XXX: Fix
					int pos = atoms.findFirstPosForXTagFrom(XTag_atomize.Desc, hn_idx);
					if (pos != -1)
						atoms.get(pos).setAmplifier(atoms.get(0).getAmplifier()/10);
					// 연산 역할을 성공적으로 수행하였는지와 무관하게, 현재 토큰은 파괴된다.
					atoms.get(hn_idx).setXTag(XTag_atomize.Skip);
				}
				else if (descop_type.equals(XTag_logical.DescOp_Increase))
				{
					// XXX: Fix
					int pos = atoms.findFirstPosForXTagFrom(XTag_atomize.Desc, hn_idx);
					if (pos != -1)
						atoms.get(pos).setAmplifier(atoms.get(0).getAmplifier()*10);
					// 연산 역할을 성공적으로 수행하였는지와 무관하게, 현재 토큰은 파괴된다.
					atoms.get(hn_idx).setXTag(XTag_atomize.Skip);
				}
				else if (descop_type.equals(XTag_logical.DescOp_InvertPrev))
				{
					// XXX: Fix
					int pos = atoms.findFirstPosForXTagFrom(XTag_atomize.Desc, hn_idx);
					if (pos != -1)
						atoms.prev(pos).setAmplifier(atoms.get(0).getAmplifier());
					// 연산 역할을 성공적으로 수행하였는지와 무관하게, 현재 토큰은 파괴된다.
					atoms.get(hn_idx).setXTag(XTag_atomize.Skip);
				}
				else if (descop_type.equals(XTag_logical.DescOp_InvertNext))
				{
					// XXX: Fix
					int pos = atoms.findFirstPosForXTagFrom(XTag_atomize.Desc, hn_idx);
					if (pos != -1)
						atoms.get(pos).setAmplifier(atoms.get(0).getAmplifier());
					// 연산 역할을 성공적으로 수행하였는지와 무관하게, 현재 토큰은 파괴된다.
					atoms.get(hn_idx).setXTag(XTag_atomize.Skip);
				}
				else
				{
					// nothing to do
				}
			}
		}
		
		atoms.compaction();
		
		for (int hn_idx=0; hn_idx < atoms.size(); hn_idx++)
		{
			if (atoms.get(hn_idx).getXTag().equals(XTag_atomize.DescOp))
			{
				Hana base = atoms.get(hn_idx);
				// 여기에서 부사 + 형용사 -> 형용사, 반전이나 강화/감소 연산이 완료된 서술어를 필요하다면 연결한다.
				String descop_type = identifyDescOp(base.toString());
				if (descop_type.equals(XTag_logical.DescOp_Join))
				{
					
					// NullPointerException 으로부터 first, prev, next, last 연산은 안전하므로
					// 마음 놓고 비교가 가능하다.
					if (atoms.prev(hn_idx).getXTag().equals(XTag_atomize.Desc) &&
						atoms.next(hn_idx).getXTag().equals(XTag_atomize.Desc))
					{
						Hana base_hn = atoms.prev(hn_idx);
						Hana aux_hn = atoms.next(hn_idx);
						Hana new_hn = new Hana(base_hn.toString() + "/" + aux_hn.toString());
						
						new_hn.merge(base_hn).merge(aux_hn);
						new_hn.setXTag(XTag_atomize.Desc);
						atoms.prev(hn_idx).setXTag(XTag_atomize.Skip);
						atoms.next(hn_idx).setXTag(XTag_atomize.Skip);
						atoms.set(hn_idx, new_hn);
						
					}
				}
			}
		}
		// TODO: 있다, 없다, 하다 에 대한 처리도 필요함. 이 경우는 Object Desc(sentence end)식으로 구성된 경우가 많음.
		// Compaction 한번 더 실시
		atoms.compaction();
		
		// 현재 hlist 내부의 어휘에 대해 EndOfSentence 키워드를 기준으로 문장을 분할한다.
		ArrayList<HList> sentences = new ArrayList<HList>();
		sentences.add(new HList());
		int pos = 0;
		for (Hana hn : atoms)
		{
			if (hn.getXTag().equals(XTag_atomize.EndOfSentence))
			{
				sentences.add(new HList());
				pos++;
			}
			else
			{
				sentences.get(pos).add(hn);
			}
		}
		
		atoms.clear();
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
		for (HList hl : sentences)
		{
			alignHList(hl);
		}
		
		
		
		// TODO: HList를 연산하여 감정값을 내놓는다. 확률 출력과 감정값 출력으로 나눠, 확률은 Sense모듈이 모르는 어휘를 학습할 수 있도록 던져준다.
		
		//TODO:  sentences로부터 글 전체의 감정값을 요약한다. 강세 어휘를 추출한다.
		
		return sentences;
	}
	
	/**
	 * DescOp 으로 분류된 어휘가 어떤 연산을 수행하는지 분류해낸다. DescOpHelper 의 간편 메소드로 제작되었다.
	 * @return 연산자 유형 정수로 반환한다.
	 */
	public String identifyDescOp(String word)
	{
		// 구현은 common 패키지의 DescOpHelper에 되어있다.
		return (new DescOpHelper()).identify(word);
	}
	
	/**
	 * HList 내부의 객체들을, (헤더) - (주어) ------------{(수식 어휘들)}------------ (최종 서술어) 구조로 재정렬한다.
	 * @return 없음
	 */
	public void alignHList(HList hl)
	{
		hl.add(0, new Hana()); // 헤더
		
		if (hl.findFirstPosForXTag(XTag_atomize.Subject) != -1)
		{
			hl.swap(hl.findFirstPosForXTag(XTag_atomize.Subject), 1);	
		}
		else
		{
			// 주어 탐색
			// 혹시 대명사가 주어일 수도 있다. (우선순위 1)
			if (hl.findFirstPosForXTag(XTag_atomize.ReferenceMarker) != -1)
			{
				hl.findHanaForXTag(XTag_atomize.ReferenceMarker).setXTag(XTag_atomize.Subject);
				hl.swap(hl.findFirstPosForXTag(XTag_atomize.Subject), 1);
			}
			
			if (hl.findFirstPosForXTag(XTag_atomize.Subject) == -1)
			{
				// 일단, 첫번째로 Object 로 마크된 Hana를 주어로 선정한다. (우선순위 2)
				if (hl.findFirstPosForXTag(XTag_atomize.Object) != -1)
				{
					hl.findHanaForXTag(XTag_atomize.Object).setXTag(XTag_atomize.Subject);
					hl.swap(hl.findFirstPosForXTag(XTag_atomize.Subject), 1);
				}
			}
			
			if (hl.findFirstPosForXTag(XTag_atomize.Subject) == -1)
			{
				// .. 최후의 방법. (우선순위 3)
				hl.add(1, new Hana("나").setXTag(XTag_atomize.Subject));
			}
		}
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
