package jnu.mindsharing.chainengine;

import java.util.List;

import jnu.mindsharing.common.DatabaseConstants.WORD_TYPE;
import jnu.mindsharing.common.HList;
import jnu.mindsharing.common.Hana;
import jnu.mindsharing.common.P;
import jnu.mindsharing.common.XTag_atomize;

import org.snu.ids.ha.ma.MCandidate;
import org.snu.ids.ha.ma.MExpression;
import org.snu.ids.ha.ma.Morpheme;
import org.snu.ids.ha.ma.MorphemeAnalyzer;

/**
 * 엔진에서 결합조건 등을 파악하기위해, 내부 태그로 변환하고 주어와 서술어를 파악하는 전처리 클래스이다.
 * 
 * @author nidev
 *
 */
public class TextPreprocessor
{
	String TAG="TPreproc";
	String contents;
	HList internal;
	
	/**
	 * ESentence 객체를 받아 작업을 준비한다.
	 * @param es_given ESentence 객체
	 */
	public TextPreprocessor(String rawText)
	{
		contents = rawText;
	}
	
	/**
	 * 첫번째 매개변수로 주어진 태그가, 두번째부터 나오는 태그들 중에 존재하는지 체크한다.
	 * @param goal 찾으려는 태그
	 * @param candidates 후보군
	 * @return true(태그가 후보 중에 존재함), false(존재하지 않음)
	 */
	private boolean isTagIn(String goal, String...candidates)
	{
		for (String test: candidates)
		{
			if (goal.equalsIgnoreCase(test))
			{
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Hana들이 모여있는 ArrayList를 하나의 Hana(태그:Object)로 합친다. 명사 어휘를 합칠 때 이용한다.
	 * @param units 명사 어휘 Hana들이 포함된 배열
	 * @return 합성된 Hana 객체
	 */
	private Hana mergeIntoObject(HList units)
	{
		// 주어진 Hana의 어휘를 합쳐서 하나의 객체로 만들고,
		// 그 객체를 Object 어휘로 태그한다.
		StringBuffer strbuff = new StringBuffer();
		for (Hana unit : units)
		{
			strbuff.append(unit.toString());
			strbuff.append(" ");
		}
		return new Hana(strbuff.toString().trim()).setXTag(XTag_atomize.Object);
	}
	
	/**
	 * 따옴표 해체 작업을 수행하고, unquoted 플래그를 true로 바꾼다.
	 */
	private void performUnquoting()
	{
		if (contents.contains("\""))
		{
			// TODO: 실질적인 해체 작업
			P.e(TAG, "따옴표가 포함되어있음.");
		}
	}
	
	/**
	 * 어휘를 형태소 분석기로 분석하고, 형태소 분석기 태그를 내부 태그로 변환하며 감정 분석 단위로 변환한다.
	 * 분석된 결과는 어휘 타입과 단어 덩어리가 포함된 객체가 담긴 ArrayList이다.
	 * 변환이 완료되면 tagged 플래그를 true로 바꾸고, 이 결과값은 isEverythingDone()으로 체크한다.
	 * 이 단계에서 감정값 평가는 <b>절대</b> 하지 않는다.
	 * @param ma 꼬꼬마 형태소 분석기 객체
	 * @see EmotionAlgorithm
	 */
	private void performTagging(MorphemeAnalyzer ma) throws Exception
	{
		List<MExpression> aresults = ma.analyze(contents);
		if (aresults == null)
		{
			return;
			// throw new RuntimeException("분석기 오류 발생. 심각한 오류입니다.");
		}
		// WARNING: postProcess 과정에서 토큰 품질이 감소한다. 쓸데없이 자세히 잘라서, SNS텍스트 분석에는 부적합함
		//aresults = ma.postProcess(aresults);
		//aresults = ma.leaveJustBest(aresults);
		
		/*
		 * CAUTION: 이 단계에서 절대 논리적 어휘 연결 과정을 파악하지말 것. 순수하게 태깅과 감정관련 값들, 문장 단위 분해에만 주목한다.
		 */
		
		
		
		// phase1: 어휘 품사에 맞춰서 필요한 태그를 설정한다.
		for(int i=0; i < aresults.size(); i++)
		{
			/*
			 * 꼬꼬마 형태소 분석기는 중간 결과를 활용하기 매우 어렵게 되어있음.
			 */
			MExpression mexp = aresults.get(i);
			
			if (mexp.isEmpty()) continue;
			
			if (!mexp.isNotHangul())
			{
				MCandidate cur_mc = SejongMCandidateRefiner.refineAndSelectBest(mexp);
				for (Morpheme cur_morpheme: cur_mc)
				{
					P.d("형태소", "--[단어:%s] %s %s %s %s", cur_mc.getExp(), cur_morpheme.getCharSetName(), cur_morpheme.getString(), cur_morpheme.getTag(), cur_morpheme.getComposed());

					// 꼬꼬마 형태소의 태그를, 내부의 태그로 새롭게 변환한다.
					String mtag = cur_morpheme.getTag();
					String word = cur_morpheme.getString();
					if (isTagIn(mtag, "MAG", "ECE")) // 부사, ~하고
					{
						internal.add(new Hana(word, WORD_TYPE.op).setXTag(XTag_atomize.DescOp));
					}
					else if (isTagIn(mtag, "VA", "VXA"))
					{
						// 형용사 어휘
						internal.add(new Hana(cur_morpheme.getString()+"다").setXTag(XTag_atomize.AdjectMarker));
					}
					else if (isTagIn(mtag, "VV"))
					{
						// 동사 어휘
						// VV 태그는 명사+하다 의 조합이 아닌 동사들
						// TODO: 현재는 인과 관계파악없이 서술어의 어감으로 파악한다.
						internal.add(new Hana(cur_morpheme.getString()+"다").setXTag(XTag_atomize.VerbMarker));
					}
					else if (isTagIn(mtag, "VXV", "VX"))
					{
						// 있다/없다/(명사)하다 와 같은 보조동사 어휘
						// 처리할지 말지는 고민 중
						internal.add(new Hana(cur_morpheme.getString()+"다").setXTag(XTag_atomize.VerbMarker));
					}
					else if (isTagIn(mtag, "ECD")) // 의존적 종결어미 (--하'지')
					{
						internal.add(new Hana(cur_morpheme.getString()).setXTag(XTag_atomize.DescOp));
					}
					else if (isTagIn(mtag, "MDT", "MDN"))
					{
						// 관형사... 체언을 자세히 꾸며준다.
						// 따라서 뒤에오는 Object를 자세히 설명해준다.
						internal.add(new Hana(cur_morpheme.getString()).setXTag(XTag_atomize.DeterminerMarker));
						
					}
					else if (isTagIn(mtag, "EFN"))
					{
						// 마침표 등지의 끝나는 위치에 Skip 토큰을 추가한다.
						internal.add(new Hana(cur_morpheme.getString()).setXTag(XTag_atomize.EndOfSentence));
					}
					else if (isTagIn(mtag, "JKS", "JX"))
					{
						// TODO: 앞부분의 명사(NNG)태그의 연속을 주어로 파악할 수 있도록 한다.
						internal.add(new Hana(cur_morpheme.getString()).setXTag(XTag_atomize.SubjectTrailMarker));
					}
					else if (isTagIn(mtag, "XSV", "XSA"))
					{
						internal.add(new Hana(cur_morpheme.getString()+"다").setXTag(XTag_atomize.DescTrailMarker));
					}
					else if (isTagIn(mtag, "JKM", "JKG", "JKC", "JKQ"))
					{
						// XXX: 부사격/관형격/보격/인용격 조사 사용안함
						// 필요하면 2단계를 고치시오
						internal.add(new Hana(cur_morpheme.getString()).setXTag(XTag_atomize.UnhandledTrailMarker));
						
					}
					else if (isTagIn(mtag, "JKO"))
					{
						// TODO: 앞부분의 명사(NNG)태그의 연속을 목적어나 일반 객체 어휘로 파악할 수 있도록 한다.
						internal.add(new Hana(cur_morpheme.getString()).setXTag(XTag_atomize.ObjectTrailMarker));
					}
					else if (isTagIn(mtag, "XR", "XSN"))
					{
						/*
						 * XR 은 어근 태그 (복잡+하다 에서 복잡 부분)
						 * 동사로 분류하고 사전에서는 명사로 찾아서 효율성을 높힌다.
						 */
						internal.add(new Hana(cur_morpheme.getString(), WORD_TYPE.verb).setXTag(XTag_atomize.VerbMarker));
					}
					else if (isTagIn(mtag, "NNG", "NP", "NNP", "NNB", "UN")) // UN 태그에 주목
					{
						// 명사 어휘 덩어리를 만들기 위한 태그 부분 
						/*
						 * 주목: UN 은 형태소 분석기에서 '명사'로 추정한 어휘이다. '인명'도 엄밀히 명사의 범주에 속하기 때문에, 인명 인식을 위해서 추가한다.
						 */
						// DescOp 중에는 명사 어휘도 존재한다.
						
						internal.add(new Hana(cur_morpheme.getString()).setXTag(XTag_atomize.NounMarker));
					}
					else
					{
						// 관심 없는 어휘는 ESentence 객체에 변환된 태그로 넣지않고 무시한다.
					}
				}
			}
			else
			{
				// 한글이 아닌 경우, 이모티콘 탐색 등을 시도한다.
				// TODO: 다음 문자가 한글이 아닐때까지 이모티콘 join을 한 후에 한 객체로 정리한다.
				P.d(TAG, "한글이 아닌 문자를 한 덩어리로 합성합니다.");
				String emoticon_base;
				int j = i;
				emoticon_base = "";
				while (j < (aresults.size()-1))
				{
					mexp = aresults.get(j);
					emoticon_base += mexp.getExp();
					if (aresults.get(j).isNotHangul())
					{
						j++;
					}
					else
					{
						break;
					}
					
				}
				i = j-1; // 내부 루프에서 전진한만큼 동기화
				
				internal.add(new Hana(emoticon_base, WORD_TYPE.noun).setXTag(XTag_atomize.NounMarker)); // 내부적으로 명사로 간주한다.
				continue;
			}
			
			
		}
		
		// Skip 으로 끝나지 않았다면, 패딩을 추가해준다.

		internal.add(new Hana(".").setXTag(XTag_atomize.EndOfSentence));		
	}
	
	/**
	 * 명사 어휘들끼리 결합이나, 어근과 조사의 결합 등을 확인한다. 결합이 완료되면 jointed 플래그가 true로 설정된다.
	 */
	private void performJointing()
	{
		// phase 2: Hana 배열을 순회하면서 태그 재설정 (Marker는 감정값 수신후에 다른 태그로 변경한다. phase2이후로 *Marker가 남아있으면 안됨)
		// NounMarker 끼리 뭉쳐서 Object 태그로 만든다.
		
		// 2-1: 명사 결합 문제로 인해 서술어를 먼저 변환한다.
		HList noun_queue = new HList();
		for (int es_idx=0; es_idx < internal.size(); es_idx++)
		{
			Hana em = internal.get(es_idx);
			String tag = em.getXTag();
			if (tag == XTag_atomize.DescTrailMarker)
			{
				// (명사)+하다 의 어휘를 다시 하나의 서술어로 합친다.
				if (es_idx > 0)
				{
					if (internal.get(es_idx-1).getXTag() == XTag_atomize.NounMarker)
					{
						// XXX: 수정 필요
						Hana new_em = new Hana(internal.get(es_idx-1).toString() + internal.get(es_idx).toString());
						internal.set(es_idx-1, new Hana().setXTag(XTag_atomize.Skip));
						internal.set(es_idx, new_em.setXTag(XTag_atomize.Desc));
						
					}
					else
					{
						P.e(TAG, "-하다 표현 앞에는 명사 어휘가 와야합니다. 그런데 오지 않았습니다. 심각한 오류입니다.");
						internal.get(es_idx).setXTag(XTag_atomize.Skip);
					}
					
				}
			}
			else if (tag == XTag_atomize.VerbMarker)
			{
				em.setXTag(XTag_atomize.Desc);
			}
			else if (tag == XTag_atomize.AdjectMarker)
			{
				em.setXTag(XTag_atomize.Desc);
			}
			else
			{
				// 마커를 건들지 않음
			}
		}
		
		// 2-2: 명사 어휘를 결합한다.
		for (int es_idx=0; es_idx < internal.size(); es_idx++)
		{
			Hana hn = internal.get(es_idx);
			String tag = hn.getXTag();
			// 명사 파생 접미사(XSN)는 반드시 본 명사에 붙어야한다.
			if (tag == XTag_atomize.NounMarker && noun_queue.isEmpty())
			{
				P.e("NOUN_MERGER", "명사 어휘 합성 중 - %s", hn.toString());
				noun_queue.add(hn);
				continue;
			}
			else
			{
				// 합친 만큼 나머지를 null로 덮는다.
				if (!noun_queue.isEmpty())
				{
					// noun_queue를 한 Hana으로 합친다.
					Hana new_em = mergeIntoObject(noun_queue);
					for (int backoffset=1; backoffset <= noun_queue.size(); backoffset++)
					{
						internal.set(es_idx-backoffset, new Hana().setXTag(XTag_atomize.Skip));
					}
					internal.set(es_idx-1, new_em);
					noun_queue.clear();
				}
			}
		}
		
		// 2-2: 조사와 Object들 사이의 관계를 파악해, 기본적인 서술 관계를 파악하고, 확실한 경우 주어를 설정한다.
		for (int es_idx=0; es_idx < internal.size(); es_idx++)
		{
			Hana em = internal.get(es_idx);
			String tag = em.getXTag();
			if (tag == XTag_atomize.UnhandledTrailMarker)
			{
				// 이 마커를 삭제하고, Object는 생존하도록 남긴다.
				em.setXTag(XTag_atomize.Skip);
		
			}
			else if (tag == XTag_atomize.SubjectTrailMarker || tag == XTag_atomize.ObjectTrailMarker)
			{
				if (es_idx > 0)
				{
					if (internal.get(es_idx-1).getXTag() == XTag_atomize.Object)
					{
						internal.get(es_idx - 1).setXTag(tag == XTag_atomize.SubjectTrailMarker ? XTag_atomize.Subject : XTag_atomize.Object);
					}
					internal.set(es_idx, new Hana().setXTag(XTag_atomize.Skip));
				}
				else
				{
					P.e(TAG, "주어/목적어가 없음에도 조사로 감지되었습니다. 이것은 심각한 오류입니다.");
					em.setXTag(XTag_atomize.Skip);
				}
			}
			else if (tag == XTag_atomize.Desc)
			{
				if ((es_idx+2) <= internal.size())
				{
					// (서술어)한 (명사) ex) 예쁜 그녀
					// 의 결합 조건을 체크한다.
					if (internal.get(es_idx+1).getXTag() == XTag_atomize.Object || internal.get(es_idx+1).getXTag() == XTag_atomize.Subject)
					{
						// XXX: 수정 필요
						em.setXTag(XTag_atomize.DescOp);
					}
					else
					{
						// XXX: 수정 필요
						if (internal.get(es_idx+1).getXTag() == XTag_atomize.DescOp
								&& internal.get(es_idx+2).getXTag() == XTag_atomize.Desc)
						{
							// -지 아니하다/않다/못하다
							String depender = internal.get(es_idx+1).toString();
							String verb = internal.get(es_idx+2).toString();
							// XXX: 문자열 상수가 아닌, 태그로 비교하는 방법을 만들자.
							if (depender.equals("지") && isTagIn(verb, "아니하다", "않다", "못하다"))
							{
								Hana inverter = new Hana(String.format("-%s %s", depender, verb)).setXTag(XTag_atomize.DescOp);
								Hana desc = internal.get(es_idx);
								internal.set(es_idx, new Hana().setXTag(XTag_atomize.Skip));
								internal.set(es_idx+1, inverter);
								internal.set(es_idx+2, desc);
							}
						}
					}
				}
			}
			else 
			{
				;
			}
		}
		
		// 모든 작업이 완료된 후에도, 마지막 서술어가 Desc라면 DescSubject로 변환한다.
		// TODO: 실행 위치 변경
		/*
		if (internal.last().getXTag() == XTag_atomize.Desc)
		{
			// XXX: 목적어를 취하는 동사라면?
			// 이것을 체크할 방법은?
			internal.last().setXTag(XTag_atomize.DescSubject);
		}
		*/
		
		// Compaction 코드는 ESentence 내부로 옮겼음.
		internal.compaction();
	}
	
	/**
	 * Sense 모듈로 부터 토큰의 기본감정 확률/추정감정 확률을 수신한다.
	 * @param ss
	 */
	private void performHListEvaluation(Sense ss)
	{
		for (Hana hn : internal)
		{
			if (hn.getXTag().equals(XTag_atomize.Desc))
			{
				Hana value = ss.ask(hn.toString());
				if (value != null)
				{
					hn.merge(value);
				}
			}
		}
		
	}
	
	/**
	 * 전처리가 완료되면, TextProcessor 생성 후 결과물을 HList로 반환한다. 내부의 내용은 전처리기에 의해 변경되어있다.
	 * 만약 작업이 완료되지않았다면, null을 반환한다.
	 * 
	 * @return 전처리 완료된 ESentence 객체, 또는 null
	 * @throws Exception 
	 */
	public HList atomize(MorphemeAnalyzer ma) throws Exception
	{
		
		performUnquoting();
		performTagging(ma);
		performJointing();
		performHListEvaluation(new Sense());
		return internal;
	}

}
