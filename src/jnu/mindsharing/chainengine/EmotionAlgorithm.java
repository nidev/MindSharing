package jnu.mindsharing.chainengine;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import jnu.mindsharing.common.EParagraph;
import jnu.mindsharing.common.ESentence;
import jnu.mindsharing.common.EmoUnit;
import jnu.mindsharing.common.P;

import org.snu.ids.ha.ma.MCandidate;
import org.snu.ids.ha.ma.MExpression;
import org.snu.ids.ha.ma.Morpheme;
import org.snu.ids.ha.ma.MorphemeAnalyzer;

public class EmotionAlgorithm extends ArrayList<EParagraph>
{
	private static final long serialVersionUID = -6004998827195979333L;
	
	private String TAG = "Algorithm";
	private MorphemeAnalyzer ma;
	
	public EmotionAlgorithm(MorphemeAnalyzer hostMA)
	{
		// TODO: null 체크
		ma = hostMA;
	}
	
	public int createNewParagraph(String text)
	{
		// Return: index
		add(new EParagraph(text));
		return size()-1;
	}
	
	public EParagraph findEParagraphByText(String text)
	{
		for (EParagraph ep: this)
		{
			if (ep.getWholeText().equals(text)) // md5나 CRC같은 해시를 사용하지 않으면 비교가 느릴듯.
			{
				return ep;
			}
		}
		return null;
	}
	
	public boolean isTagIn(String goal, String...candidates)
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
	
	public void addESentenceTo(int paragraph_index, ESentence es)
	{
		get(paragraph_index).add(es);
	}
	
	public EmoUnit mergeIntoObject(ArrayList<EmoUnit> units)
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
	
	public void inspectESentence(ESentence es)
	{
		// for debugging
		P.e(TAG,  "Item Inspector");
		for (EmoUnit inspect_em: es)
		{
			if (inspect_em == null)
			{
				P.e(TAG, " =inspect> null");
			}
			else
			{
				P.e(TAG, " =inspect> content: %s", inspect_em.getOrigin());
			}
		}
		P.e(TAG, "Inspector end");
	}
	
	public void process(int paragraph_index) throws Exception
	{
		// phase 1: 문장마다 형태소 분석 후 어휘 탐색
		EQueryTool eq = new EQueryTool();
		
		for (ESentence es: get(paragraph_index))
		{
			List<MExpression> aresults = ma.analyze(es.getWholeText());
			if (aresults == null)
			{
				continue;
			}
			// WARNING: postProcess 과정에서 토큰 품질이 감소한다. 쓸데없이 자세히 잘라서, SNS텍스트 분석에는 부적합함
			//aresults = ma.postProcess(aresults);
			aresults = ma.leaveJustBest(aresults);
			
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
					// P.d(TAG, "-%s- 형태소 후보 갯수 : %d개", mexp.getExp(), mexp.size());
					for (MCandidate cur_mc : mexp)
					{
						for (Morpheme cur_morpheme: cur_mc)
						{
							P.d("형태소", "--[단어:%s] %s %s %s %s", cur_mc.getExp(), cur_morpheme.getCharSetName(), cur_morpheme.getString(), cur_morpheme.getTag(), cur_morpheme.getComposed());
							// 형태소간 관계를 비교한다.
							// TODO: 효율적인 태그 비교 방법이 필요함
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
							else if (isTagIn(mtag, "NNG", "XR", "XSN", "NP", "NNP", "NNB"))
							{
								// 명사 어휘 덩어리를 만들기 위한 태그 부분
								// XXX: XR 은 어근 태그
								// 복잡+ 하다. 인데.... 후
								/*
								 * TODO: 서로 다른 명사의 타입을 하나로 합치다보면 문제가 발생한다.
								 * 구분할 수 있게 태그를 다시 설정함이 좋을듯하다.
								 * 
								 * 또는 꼬꼬마의 태그를 그대로 EmoUnit에 저장할 수 있게 하거나.
								 */
								if (mtag.equals("NP"))
								{
									// 대명사는 가르키는 대상이 존재하므로 일단 ReferenceMarker로.
									es.add(new EmoUnit(cur_morpheme.getString()).setTag(EmoUnit.WordTag.ReferenceMarker));
								}
								else
								{
									es.add(new EmoUnit(cur_morpheme.getString()).setTag(EmoUnit.WordTag.NounMarker).setExt(mtag));
									
								}
							}
							else
							{
								//
								// TODO: 3차를 넘길 때쯤 Skip 부분은 모두 trim한다.
								// es.add(new EmoUnit(cur_morpheme.getString()).setTag(EmoUnit.WordTag.Skip));
							}
						}
					}
				}
				else
				{
					// 한글이 아닌 경우, 이모티콘 탐색 등을 시도한다.
					P.d(TAG, "한글이 아닌 문자. 이모티콘 탐색을 시도합니다.");
					if (eq.isEmoticon(mexp.getExp()))
					{
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
			
			// 2-2: 전체적인 어휘 뭉치기 작업을 진행한다.
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
				
				if (tag == EmoUnit.WordTag.UnhandledTrailMarker)
				{
					// XXX: 이 마커와, 그 앞의 명사 어휘는 삭제됨.
					if (es_idx > 0)
					{
						es.set(es_idx, new EmoUnit().setTag(EmoUnit.WordTag.Skip));
						es.set(es_idx-1, new EmoUnit().setTag(EmoUnit.WordTag.Skip));
					}
					else
					{
						P.e(TAG, "격조사인데, 앞에 체언이 없이 격조사가 나타났습니다. 이것은 심각한 오류입니다.");
						P.e(TAG, "입력된 텍스트 : %s", es.getWholeText());
						em.setTag(EmoUnit.WordTag.Skip);
					}
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
					if ((es_idx+1) == es.size())
					{
						if (es.getLastEmoUnit().getTag() == EmoUnit.WordTag.Desc)
						{
							// XXX: 조건문 재조정 필요. 왜냐하면 후에 다른 서술어가 오는지 체크해야하기 때문
							es.getLastEmoUnit().setTag(EmoUnit.WordTag.DescSubject);
						}
					}
					else
					{
						if (es.get(es_idx+1).getTag() == EmoUnit.WordTag.Object || es.get(es_idx+1).getTag() == EmoUnit.WordTag.Subject)
						{
							em.setTag(EmoUnit.WordTag.DescNextObject);
						}
						else
						{
							if ((es_idx+2) < es.size())
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
				}
				else 
				{
					;
				}
			}
			
			// Compaction 코드는 ESentence 내부로 옮겼음.
			es.compactSkips();
			
			// phase 3: DescObject, DescSubject, Desc 태그된 어휘에 대해 감정값을 탐색한다.
			// TODO: 동음이의어 처리는 어떻게 할 것인가?
			for (EmoUnit em: es)
			{
				Enum<EmoUnit.WordTag> tag = em.getTag();
				if (tag == EmoUnit.WordTag.Desc || tag == EmoUnit.WordTag.DescNextObject || tag == EmoUnit.WordTag.DescSubject)
				{
					EmoUnit result = eq.queryWord(em.getOrigin());
					if (result == null)
					{
						P.e(TAG, "모르는 어휘 : %s", em.getOrigin());
					}
					else
					{
						em.importVectors(result);
					}
				}
			}
			
			// phase 4: 인칭 문제를 해결한다. 여기에서 주어를 발견할 수 없다면, Object 중에 주어를 선정하고, 그게 안되면 주어 '나'를 삽입한다.
			// 아직 'Marker'와 'Skip'으로 남아있는 어휘를 모두 제거한다, NextDescDepender를 읽고 전후 관계를 파악한다.
			// 
			// TODO: 역할이 불분명한 서술어(Desc로 태그 된)의 연관관계를 파악한다.
			if (!es.hasSubject())
			{
				// object중에 주어를 선정하는 과정이 없음. 일반적으로 first object를 주어로 간주하면 된다.
				for (EmoUnit em : es)
				{
					if (em.getTag() == EmoUnit.WordTag.Object)
					{
						// XXX: 현재 휴리스틱 규칙은 없음
						// 분명 오류를 내는 알고리즘임
						em.setTag(EmoUnit.WordTag.Subject);
						break;
					}
						
				}
				if (!es.hasSubject())
				{
					// 그래도 없다면 임의로 주어를 삽입한다.
					es.add(0, new EmoUnit("나").setTag(EmoUnit.WordTag.Subject));
				}
			}
			
			for (int es_idx=0; es_idx < es.size() ; es_idx++)
			{
				// 크게(크다 + 게) + 퍼뜨리다
				// => 크다, 퍼뜨리다 식으로 이미지를 추출한다.
				if (es.get(es_idx).getTag() == EmoUnit.WordTag.NextDescDepender)
				{
					if (es_idx+1 < es.size())
					{
						if (es.get(es_idx-1).getTag() == EmoUnit.WordTag.Desc &&
							es.get(es_idx+1).getTag() == EmoUnit.WordTag.Desc)
						{
							EmoUnit baseemo = es.get(es_idx-1);
							EmoUnit auxemo = es.get(es_idx+1);
							EmoUnit newemo = new EmoUnit(baseemo.getOrigin() + " " + auxemo.getOrigin());
							newemo.importVectors(baseemo);
							newemo.setTag(EmoUnit.WordTag.Desc);
							es.get(es_idx-1).setTag(EmoUnit.WordTag.Skip);
							es.get(es_idx).setTag(EmoUnit.WordTag.Skip);
							// 근데 이건 필요한가?
							if (auxemo.getOrigin().equals("없다") || auxemo.getOrigin().equals("않다") || auxemo.getOrigin().equals("아니하다"))
							{
								
								es.set(es_idx+1, newemo.invertAll());
								
							}
							else
							{

								es.set(es_idx+1, newemo);
								
							}
							
						}
						else if(es.get(es_idx+1).getTag() == EmoUnit.WordTag.Subject
								|| es.get(es_idx+1).getTag() == EmoUnit.WordTag.Object)
						{
							// ~하는데 ~가 하다
						}
					}
				}
			}
			// TODO: 있다, 없다, 하다 에 대한 처리도 필요함. 이 경우는 Object Desc(sentence end)식으로 구성된 경우가 많음.
			// Compaction 한번 더 실시
			es.compactSkips();
			

			// phase 5: EmoUnit 배열의 감정값 정규화
			// phase 6: EmoUnit 배열 순회하면서 글쓴이의 감정 파악
			// (연관 단어: 날씨, 기분, 감정, 빈정 등의 키워드에 이루어지는 수식들), 기타 상황 서술어(짜증난다)
		}
	}
	

	
	public void processAll() throws Exception
	{
		int index = 0;
		for (index = 0; index < size(); index++)
		{
			process(index);
		}
		
	}
	
	
	public ResultProcessor extractResultProcessor(EParagraph epr)
	{
		ResultProcessor resp = new ResultProcessor(epr);
		return resp;
	}
	
	public void displayEStatus()
	{
		P.d(TAG, "총 문단 %d개", size());
		for (EParagraph ep : this)
		{
			P.d(TAG, "==> 문단 (%x): 문장 d개", ep.hashCode(), ep.length());
			for (ESentence es : ep)
			{
				P.d(TAG, "====> 문장 (%x): 문장길이 %d글자 감정 단어 %d개", es.hashCode(), es.getWholeText().length(), es.size());
			}
		}
	}
}
