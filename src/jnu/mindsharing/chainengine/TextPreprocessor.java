package jnu.mindsharing.chainengine;

import java.util.ArrayList;
import java.util.List;

import jnu.mindsharing.common.ESentence;
import jnu.mindsharing.common.EmoUnit;
import jnu.mindsharing.common.P;

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
	ESentence es;
	boolean unquoted, tagged, jointed;
	String TAG="TPreproc";
	
	/**
	 * ESentence 객체를 받아 작업을 준비한다.
	 * @param es_given ESentence 객체
	 */
	public TextPreprocessor(ESentence es_given)
	{
		es = es_given;
		unquoted = false;
		tagged = false;
		jointed = false;
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
	 * EmoUnit들이 모여있는 ArrayList를 하나의 EmoUnit(태그:Object)로 합친다. 명사 어휘를 합칠 때 이용한다.
	 * @param units 명사 어휘 EmoUnit들이 포함된 배열
	 * @return 합성된 EmoUnit 객체
	 */
	private EmoUnit mergeIntoObject(ArrayList<EmoUnit> units)
	{
		// 주어진 EmoUnit의 어휘를 합쳐서 하나의 객체로 만들고,
		// 그 객체를 Object 어휘로 태그한다.
		StringBuffer strbuff = new StringBuffer();
		for (EmoUnit unit : units)
		{
			strbuff.append(unit.getOrigin());
			strbuff.append(" ");
		}
		return new EmoUnit(strbuff.toString().trim()).setTag(EmoUnit.WordTag.Object);
	}
	
	/**
	 * 따옴표 해체 작업을 수행하고, unquoted 플래그를 true로 바꾼다.
	 */
	public void performUnquoting()
	{
		if (es.getWholeText().contains("\""))
		{
			P.e(TAG, "따옴표가 포함되어있음.");
		}
		// over
		unquoted = true;
	}
	
	/**
	 * 어휘를 형태소 분석기로 분석하고, 형태소 분석기 태그를 내부 태그로 변환한다. 변환이 완료되면 tagged 플래그를 true로 바꾼다.
	 * @param ma 꼬꼬마 형태소 분석기 객체
	 * @param eq EQueryTool 객체
	 * @see EQueryTool
	 */
	public void performTagging(MorphemeAnalyzer ma, EQueryTool eq) throws Exception
	{
		List<MExpression> aresults = ma.analyze(es.getWholeText());
		if (aresults == null)
		{
			return;
			// throw new RuntimeException("분석기 오류 발생. 심각한 오류입니다.");
		}
		// WARNING: postProcess 과정에서 토큰 품질이 감소한다. 쓸데없이 자세히 잘라서, SNS텍스트 분석에는 부적합함
		//aresults = ma.postProcess(aresults);
		//aresults = ma.leaveJustBest(aresults);
		
		
		
		// phase1: 어휘 품사에 맞춰서 필요한 태그를 설정한다.
		// TODO: 주어목적어 후보군 생성 작업이 이루어지지않음
		for(int i=0; i < aresults.size(); i++)
		{
			/*
			 * TODO: 반드시 코드 동작을 검증하기 바람.
			 * 꼬꼬마 형태소 분석기는 중간 결과를 활용하기 매우 어렵게 되어있음.
			 */
			MExpression mexp = aresults.get(i);
			
			// P.d(TAG, "형태소 분석기 어휘 : %s" , mexp.getExp());
			if (mexp.isEmpty()) continue;
			
			if (!mexp.isNotHangul())
			{
				//P.d(TAG, "-%s- 형태소 후보 갯수 : %d개", mexp.getExp(), mexp.size());
				MCandidate cur_mc = SejongMCandidateRefiner.refineAndSelectBest(mexp);
				for (Morpheme cur_morpheme: cur_mc)
				{
					P.d("형태소", "--[단어:%s] %s %s %s %s", cur_mc.getExp(), cur_morpheme.getCharSetName(), cur_morpheme.getString(), cur_morpheme.getTag(), cur_morpheme.getComposed());

					// 꼬꼬마 형태소의 태그를, 내부의 태그로 새롭게 변환한다.
					String mtag = cur_morpheme.getTag();
					if (isTagIn(mtag, "MAG"))
					{
						String word = cur_morpheme.getString();
						//P.d("형태소", "형용사 앞의 부사: %s", last_morpheme.getString());
						if (eq.isNegativeADV(word))
						{
							// 안, 아니, 아니하, 전혀, 절대 등등
							es.add(new EmoUnit(word).setTag(EmoUnit.WordTag.InvertNextDesc));
						}
						else if (eq.isEnhancer(word))
						{
							// 매우, 정말, 진짜, 확실히
							es.add(new EmoUnit(word).setTag(EmoUnit.WordTag.NextDescEnhancer));
						}
						else if (eq.isReducer(word))
						{
							// 약간, 조금, 살짝
							es.add(new EmoUnit(word).setTag(EmoUnit.WordTag.NextDescReducer));
						}
						else
						{
							es.add(new EmoUnit(word).setTag(EmoUnit.WordTag.Skip));
						}
					}
					else if (isTagIn(mtag, "VA", "VXA"))
					{
						// 형용사 어휘
						es.add(new EmoUnit(cur_morpheme.getString()+"다").setTag(EmoUnit.WordTag.AdjectMarker));
					}
					else if (isTagIn(mtag, "VV"))
					{
						// 동사 어휘
						// VV 태그는 명사+하다 의 조합이 아닌 동사들
						// TODO: 현재는 인과 관계파악없이 서술어의 어감으로 파악한다.
						es.add(new EmoUnit(cur_morpheme.getString()+"다").setTag(EmoUnit.WordTag.VerbMarker));
					}
					else if (isTagIn(mtag, "VXV", "VX"))
					{
						// 있다/없다/(명사)하다 와 같은 보조동사 어휘
						// 처리할지 말지는 고민 중
						es.add(new EmoUnit(cur_morpheme.getString()+"다").setTag(EmoUnit.WordTag.VerbMarker));
					}
					else if (isTagIn(mtag, "ECD")) // 의존적 종결어미 (--하'지')
					{
						es.add(new EmoUnit(cur_morpheme.getString()).setTag(EmoUnit.WordTag.NextDescDepender));
					}
					else if (isTagIn(mtag, "MDT", "MDN"))
					{
						// 관형사... 체언을 자세히 꾸며준다.
						// 따라서 뒤에오는 Object를 자세히 설명해준다.
						es.add(new EmoUnit(cur_morpheme.getString()).setTag(EmoUnit.WordTag.DeterminerMarker));
						
					}
					else if (isTagIn(mtag, "EFN"))
					{
						// 마침표 등지의 끝나는 위치에 Skip 토큰을 추가한다.
						es.add(new EmoUnit(cur_morpheme.getString()).setTag(EmoUnit.WordTag.Skip));
					}
					else if (isTagIn(mtag, "JKS", "JX"))
					{
						// TODO: 앞부분의 명사(NNG)태그의 연속을 주어로 파악할 수 있도록 한다.
						es.add(new EmoUnit(cur_morpheme.getString()).setTag(EmoUnit.WordTag.SubjectTrailMarker));
					}
					else if (isTagIn(mtag, "XSV", "XSA"))
					{
						es.add(new EmoUnit(cur_morpheme.getString()+"다").setTag(EmoUnit.WordTag.DescTrailMarker));
					}
					else if (isTagIn(mtag, "JKM", "JKG", "JKC", "JKQ"))
					{
						// XXX: 부사격/관형격/보격/인용격 조사 사용안함
						// 필요하면 2단계를 고치시오
						es.add(new EmoUnit(cur_morpheme.getString()).setTag(EmoUnit.WordTag.UnhandledTrailMarker));
						
					}
					else if (isTagIn(mtag, "JKO"))
					{
						// TODO: 앞부분의 명사(NNG)태그의 연속을 목적어나 일반 객체 어휘로 파악할 수 있도록 한다.
						es.add(new EmoUnit(cur_morpheme.getString()).setTag(EmoUnit.WordTag.ObjectTrailMarker));
					}
					else if (isTagIn(mtag, "NNG", "XR", "XSN", "NP", "NNP", "NNB", "UN")) // UN 태그에 주목
					{
						// 명사 어휘 덩어리를 만들기 위한 태그 부분 
						/*
						 * XR 은 어근 태그 (복잡+하다 에서 복잡 부분)
						 * 
						 * 서로 다른 명사의 타입을 하나로 합치다보면 문제가 발생한다.
						 * 구분할 수 있게 Ext 태그를 다시 설정함이 좋을듯하다.
						 * 
						 * 주목: UN 은 형태소 분석기에서 '명사'로 추정한 어휘이다. '인명'도 엄밀히 명사의 범주에 속하기 때문에, 인명 인식을 위해서 추가한다.
						 */
						es.add(new EmoUnit(cur_morpheme.getString()).setTag(EmoUnit.WordTag.NounMarker).setExt(mtag));
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
				P.d(TAG, "한글이 아닌 문자. 이모티콘 탐색을 시도합니다.");
				if (eq.isEmoticon(mexp.getExp()))
				{
					// 이모티콘은 Preprocessing 단계에서 감정값이 평가된다.
					EmoUnit new_emo = new EmoUnit(mexp.getExp()).setTag(EmoUnit.WordTag.Emoticon);
					new_emo.importVectors(eq.queryEmoticon(mexp.getExp()));
					es.add(new_emo);
				}
				else
				{
					// TODO: 알파벳 중 약어, 일본어 글자 중 일부는 국내에서도 사용하고 있다.
					// ex) KTX, FDA 등등. 어떻게 처리할까?
					es.add(new EmoUnit(mexp.getExp()).setTag(EmoUnit.WordTag.Skip));
				}
			}
			
			
		}
		
		// Skip 으로 끝나지 않았다면, 패딩을 추가해준다.
		if (es.getLastEmoUnit().getTag() != EmoUnit.WordTag.Skip)
		{
			es.add(new EmoUnit(".").setTag(EmoUnit.WordTag.Skip));
		}
		
		// over
		tagged = true;
	}
	
	/**
	 * 명사 어휘들끼리 결합이나, 어근과 조사의 결합 등을 확인한다. 결합이 완료되면 jointed 플래그가 true로 설정된다.
	 */
	public void performJointing()
	{
		// phase 2: EmoUnit 배열을 순회하면서 태그 재설정 (Marker는 감정값 수신후에 다른 태그로 변경한다. phase2이후로 *Marker가 남아있으면 안됨)
		// NounMarker 끼리 뭉쳐서 Object 태그로 만든다.
		
		// 2-1: 명사 결합 문제로 인해 서술어를 먼저 변환한다.
		ArrayList<EmoUnit> noun_queue = new ArrayList<EmoUnit>();
		for (int es_idx=0; es_idx < es.size(); es_idx++)
		{
			EmoUnit em = es.get(es_idx);
			Enum<EmoUnit.WordTag> tag = em.getTag();
			if (tag == EmoUnit.WordTag.DescTrailMarker)
			{
				// (명사)+하다 의 어휘를 다시 하나의 서술어로 합친다.
				if (es_idx > 0)
				{
					if (es.get(es_idx-1).getTag() == EmoUnit.WordTag.NounMarker)
					{
						EmoUnit new_em = new EmoUnit(es.get(es_idx-1).getOrigin() + es.get(es_idx).getOrigin());
						es.set(es_idx-1, new EmoUnit().setTag(EmoUnit.WordTag.Skip));
						es.set(es_idx, new_em.setTag(EmoUnit.WordTag.Desc).setExt("DESC"));
						
					}
					else
					{
						P.e(TAG, "-하다 표현 앞에는 명사 어휘가 와야합니다. 그런데 오지 않았습니다. 심각한 오류입니다.");
						es.get(es_idx).setTag(EmoUnit.WordTag.Skip);
					}
					
				}
			}
			else if (tag == EmoUnit.WordTag.VerbMarker)
			{
				em.setTag(EmoUnit.WordTag.Desc);
				em.setExt("VERB");
			}
			else if (tag == EmoUnit.WordTag.AdjectMarker)
			{
				em.setTag(EmoUnit.WordTag.Desc);
				em.setExt("SUPPORTIVE");
			}
			else
			{
				// 마커를 건들지 않음
			}
		}
		
		// 2-2: 명사 어휘를 결합한다.
		for (int es_idx=0; es_idx < es.size(); es_idx++)
		{
			EmoUnit em = es.get(es_idx);
			Enum<EmoUnit.WordTag> tag = em.getTag();
			String ext = em.getExt();
			// 명사 파생 접미사(XSN)는 반드시 본 명사에 붙어야한다.
			if (tag == EmoUnit.WordTag.NounMarker &&
					(noun_queue.isEmpty()
							|| noun_queue.get(noun_queue.size()-1).getExt().equals(ext)
							|| noun_queue.get(noun_queue.size()-1).getExt().equals("XSN")
							|| ext.equals("XSN")))
			{
				P.e("NOUN_MERGER", "명사 어휘 합성 중 %s - %s", em.getOrigin(), ext);
				noun_queue.add(em);
				continue;
			}
			else
			{
				// 합친 만큼 나머지를 null로 덮는다.
				if (!noun_queue.isEmpty())
				{
					// noun_queue를 한 EmoUnit으로 합친다.
					EmoUnit new_em = mergeIntoObject(noun_queue);
					for (int backoffset=1; backoffset <= noun_queue.size(); backoffset++)
					{
						es.set(es_idx-backoffset, new EmoUnit().setTag(EmoUnit.WordTag.Skip));
					}
					es.set(es_idx-1, new_em);
					noun_queue.clear();
				}
			}
		}
		
		// 2-2: 조사와 Object들 사이의 관계를 파악해, 기본적인 서술 관계를 파악하고, 확실한 경우 주어를 설정한다.
		for (int es_idx=0; es_idx < es.size(); es_idx++)
		{
			EmoUnit em = es.get(es_idx);
			Enum<EmoUnit.WordTag> tag = em.getTag();
			if (tag == EmoUnit.WordTag.UnhandledTrailMarker)
			{
				// 이 마커를 삭제하고, Object는 생존하도록 남긴다.
				em.setTag(EmoUnit.WordTag.Skip);
		
			}
			else if (tag == EmoUnit.WordTag.SubjectTrailMarker || tag == EmoUnit.WordTag.ObjectTrailMarker)
			{
				if (es_idx > 0)
				{
					if (es.get(es_idx-1).getTag() == EmoUnit.WordTag.Object)
					{
						es.get(es_idx - 1).setTag(tag == EmoUnit.WordTag.SubjectTrailMarker ? EmoUnit.WordTag.Subject : EmoUnit.WordTag.Object);
					}
					es.set(es_idx, new EmoUnit().setTag(EmoUnit.WordTag.Skip));
				}
				else
				{
					P.e(TAG, "주어/목적어가 없음에도 조사로 감지되었습니다. 이것은 심각한 오류입니다.");
					P.e(TAG, "입력된 텍스트 : %s", es.getWholeText());
					em.setTag(EmoUnit.WordTag.Skip);
				}
			}
			else if (tag == EmoUnit.WordTag.Desc)
			{
				if ((es_idx+2) <= es.size())
				{
					// (서술어)한 (명사) ex) 예쁜 그녀
					// 의 결합 조건을 체크한다.
					if (es.get(es_idx+1).getTag() == EmoUnit.WordTag.Object || es.get(es_idx+1).getTag() == EmoUnit.WordTag.Subject)
					{
						em.setTag(EmoUnit.WordTag.DescNextObject);
					}
					else
					{
						if (es.get(es_idx+1).getTag() == EmoUnit.WordTag.NextDescDepender
								&& es.get(es_idx+2).getTag() == EmoUnit.WordTag.Desc)
						{
							// -지 아니하다/않다/못하다
							String depender = es.get(es_idx+1).getOrigin();
							String verb = es.get(es_idx+2).getOrigin();
							// XXX: 문자열 상수가 아닌, 태그로 비교하는 방법을 만들자.
							if (depender.equals("지") && isTagIn(verb, "아니하다", "않다", "못하다"))
							{
								EmoUnit inverter = new EmoUnit(String.format("-%s %s", depender, verb)).setTag(EmoUnit.WordTag.InvertNextDesc);
								EmoUnit desc = es.get(es_idx);
								es.set(es_idx, new EmoUnit().setTag(EmoUnit.WordTag.Skip));
								es.set(es_idx+1, inverter);
								es.set(es_idx+2, desc);
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
		if (es.getLastEmoUnit().getTag() == EmoUnit.WordTag.Desc)
		{
			// XXX: 목적어를 취하는 동사라면?
			// 이것을 체크할 방법은?
			es.getLastEmoUnit().setTag(EmoUnit.WordTag.DescSubject);
		}
		
		// Compaction 코드는 ESentence 내부로 옮겼음.
		es.compactSkips();
		// over
		jointed = true;
		
	}
	
	/**
	 * 모든 플래그가 true인지 체크한다. (작업 완료 여부)
	 * @return true(모두 완료), false(일부 완료)
	 */
	public boolean isEverythingDone()
	{
		if (unquoted && tagged && jointed)
		{
			return true;
		}
		else
		{
			return false;
		}
	}
	
	/**
	 * 전처리가 완료되면, TextProcessor 생성 때 받은 es 객체를 반환한다. 내부의 내용은 전처리기에 의해 변경되어있다.
	 * 만약 작업이 완료되지않았다면, null을 반환한다.
	 * @return 전처리 완료된 ESentence 객체, 또는 null
	 */
	public ESentence export()
	{
		return isEverythingDone() ? es : null;
	}

}
