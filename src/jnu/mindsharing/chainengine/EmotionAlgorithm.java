package jnu.mindsharing.chainengine;

import java.util.ArrayList;

import jnu.mindsharing.common.EParagraph;
import jnu.mindsharing.common.ESentence;
import jnu.mindsharing.common.EmoUnit;
import jnu.mindsharing.common.Nuri;
import jnu.mindsharing.common.NuriTypes;
import jnu.mindsharing.common.P;

import org.snu.ids.ha.ma.MorphemeAnalyzer;

/**
 * 텍스트에서 감정을 추출해내는 알고리즘이 있는 클래스이다. 이곳에 텍스트를 추가하는 건 엔진의 역할이다.
 * 
 * @see TextPreprocessor
 * @author nidev
 *
 */
public class EmotionAlgorithm extends ArrayList<Nuri>
{
	private static final long serialVersionUID = -6004998827195979333L;
	
	private String TAG = "Algorithm";
	private MorphemeAnalyzer ma;
	private HistoriaModule mlearn;
	private ArrayList<EParagraph> epr;
	
	enum ESENTENCE_TRAVERSE_MODE {NORMAL, INCREASE_NEXT, DECREASE_NEXT, INVERT_NEXT, DESC_JOIN};
	
	/**
	 * 알고리즘 클래스 초기화
	 * @param hostMA 엔진에서 제공한 꼬꼬마 형태소 분석기 객체
	 * @param ml 엔진에서 제공한 학습기 모듈 객체
	 */
	public EmotionAlgorithm(MorphemeAnalyzer hostMA, HistoriaModule ml)
	{
		// TODO: null 체크
		ma = hostMA;
		epr = new ArrayList<EParagraph>();
		mlearn = ml;
	}
	
	/**
	 * 새 텍스트 문단을 ArrayList<EParagraph>에 추가하여 처리할 수 있도록 준비한다.
	 * @param text 분석할 텍스트 문단
	 * @return 현재 문단의 인덱스
	 */
	public int createNewParagraph(String text)
	{
		epr.add(new EParagraph(text));
		return epr.size()-1;
	}
	
	/**
	 * 주어진 문자열과 일치하는 EParagraph 문단을 반환한다. 없다면 null 을 반환한다.
	 * @param text 찾아볼 문자열
	 * @return 해당 문단을 포함하고 있는 EPragraph 객체, 또는 null
	 * 
	 */
	public EParagraph findEParagraphByText(String text)
	{
		for (EParagraph ep: epr)
		{
			if (ep.getWholeText().equals(text)) // md5나 CRC같은 해시를 사용하지 않으면 비교가 느릴듯.
			{
				return ep;
			}
		}
		return null;
	}
	
	/**
	 * 해당 인덱스에 위치한 EParagraph 객체 안에, 분석할 문장이 들어있는 ESentence 객체를 추가한다.
	 * @param paragraph_index createNewParagraph()에서 반환한 문단 인덱스
	 * @param es ESentence 객체
	 */
	public void addESentenceTo(int paragraph_index, ESentence es)
	{
		epr.get(paragraph_index).add(es);
	}

	
	/**
	 * ESentence 객체 내부 데이터를 검토한다.
	 * @param es ESentence 객체
	 */
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
	
	/**
	 * 태깅과 결합, 관계분석 작업이 끝난 ESentence 객체로부터, 정제된 정보 객체인 Nuri를 생성한다.
	 * @param es 분석 작업이 완료된 ESentence 객체
	 * @return Nuri 객체
	 */
	public Nuri buildNuriFromESentence(ESentence es)
	{
		Nuri nri = new Nuri();
		EmoUnit es_subject = es.getSubject();
		
		nri.setSubject(NuriTypes.PERSON, es_subject.getOrigin()+"/"+es_subject.getExt());
		
		// 관련 감정 어휘를 모두 Relation으로 추가한다.
		for (EmoUnit em : es)
		{
			if (!em.hasZeroEmotion() || em.getTag() == EmoUnit.WordTag.Desc || em.getTag() == EmoUnit.WordTag.Object)
			{
				nri.addRelations(em);
			}
		}
		return nri;
	}
	
	/**
	 * 주어진 인덱스에 해당하는 문단을 처리한다. 예외가 발생할 수 있다.
	 * @param paragraph_index EParagraph의 문단 인덱스
	 */
	public void process(int paragraph_index) throws Exception
	{
		// phase 1: 문장마다 형태소 분석 후 어휘 탐색
		P.d(TAG, "Process");
		EQueryTool eq = new EQueryTool();
		
		for (ESentence es: epr.get(paragraph_index))
		{
			// XXX: 왜 null?
			if (es == null) continue;
			
			TextPreprocessor es_tp= new TextPreprocessor(es);
			// 전처리기를 사용해서 전처리 작업을 모두 수행한다.
			// TextPreprocessor.java 참고
			es_tp.performUnquoting(); // 따옴표 해체
			es_tp.performTagging(ma, eq); // 세종 말뭉치 태그를 내부 태그로 변환함
			es_tp.performJointing(); // 명사 어휘 결합 또는, 'xx하다'처럼 체언과 용언이 결합한 사례를 모두 합침
			
			if (es_tp.isEverythingDone())
			{
				P.d(TAG, "전처리/분해/명사결합 작업 완료: %s", es.getWholeText());
			}
			
			// 현재 es 객체 내부에는 전처리 작업이 모두 완료되어있다.
			
			// 관계 분석 단계 - 1 : DescObject, DescSubject, Desc 태그된 어휘에 대해 감정값을 탐색한다.
			// TODO: 동음이의어 처리는 어떻게 할 것인가?
			for (EmoUnit em: es)
			{
				Enum<EmoUnit.WordTag> tag = em.getTag();
				if (tag == EmoUnit.WordTag.Desc || tag == EmoUnit.WordTag.DescNextObject || tag == EmoUnit.WordTag.DescSubject)
				{
					String source = em.getOrigin();
					EmoUnit result = eq.queryWord(source);
					if (result == null)
					{
						
						if (mlearn.findWord(source) != null)
						{
							P.e(TAG, "HistoriaModule에서 (%s) 단어의 감정값을 받았습니다.", source);
							double[] historia_emovalue = mlearn.getHistory(source);
							em.importVectors((int)Math.round(historia_emovalue[0]), (int)Math.round(historia_emovalue[1]),
									(int)Math.round(historia_emovalue[2]), (int)Math.round(historia_emovalue[3]));
						}
						else
						{
							P.e(TAG, "(%s) 단어는 감정값을 알 수가 없습니다.", em.getOrigin());
						}
						
					}
					else
					{
						P.e(TAG, "데이터베이스에서 (%s) 단어의 감정값을 받았습니다. (추정값)", source);
						em.importVectors(result);
					}
				}
			}
			
			// 관계 분석 단계 - 2: 인칭 문제를 해결한다. 여기에서 주어를 발견할 수 없다면, Object 중에 주어를 선정하고, 그게 안되면 주어 '나'를 삽입한다.
			// 아직 'Marker'와 'Skip'으로 남아있는 어휘를 모두 제거한다, NextDescDepender를 읽고 전후 관계를 파악한다.
			// 

			// 주어가 없는 경우에 탐색 작업을 진행
			if (!es.hasSubject())
			{
				// 혹시 대명사가 주어일 수도 있다. (우선순위 1)
				for (EmoUnit em:es)
				{
					if (em.getTag() == EmoUnit.WordTag.ReferenceMarker)
					{
						em.setTag(EmoUnit.WordTag.Subject);
						break;
					}
				}
				
				if (!es.hasSubject())
				{
					// 일단, 첫번째로 Object 로 마크된 EmoUnit를 주어로 선정한다. (우선순위 2)
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
				}
				
				if (!es.hasSubject())
				{
					// ... 최후의 방법. (우선순위 3)
					es.add(0, new EmoUnit("나").setTag(EmoUnit.WordTag.Subject));
				}
			}
			
			
			// 복합 서술어 처리
			// '크게 퍼뜨리다' 를 '크다 + 퍼뜨리다'로 결합시켜 새로운 EmoUnit을 구성한다.
			for (int es_idx=0; es_idx < es.size() ; es_idx++)
			{
				if (es.get(es_idx).getTag() == EmoUnit.WordTag.NextDescDepender)
				{
					if (es_idx > 0 && es_idx+1 < es.size())
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
							es.set(es_idx+1, newemo);
							
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
		}
		
		// EmoUnit 간의 연산 작업 수행 (수식어 + 명사 결합, 다음 감정 어휘의 감정값 증가/감소/반전)
		
		for (ESentence es: epr.get(paragraph_index))
		{
			EmoUnit op_src = null;
			Enum<ESENTENCE_TRAVERSE_MODE> tm;
			tm = ESENTENCE_TRAVERSE_MODE.NORMAL;
			for (EmoUnit em: es)
			{
				// 결합 작업은 모두 끝났으므로, 이 단계에서 extTag를 지운다. 그리고 여기에, 결합시 문맥 정보를 담는다.
				em.setExt();
				// P.e(TAG, "탐색 중, 현재 : %s, 모드 %s", em.getOrigin(), tm.toString());
				if (tm == ESENTENCE_TRAVERSE_MODE.NORMAL)
				{
					if (em.getTag() == EmoUnit.WordTag.DescNextObject)
					{
						op_src = em;
						tm = ESENTENCE_TRAVERSE_MODE.DESC_JOIN;
					}
					else if (em.getTag() == EmoUnit.WordTag.NextDescReducer)
					{
						op_src = em;
						tm = ESENTENCE_TRAVERSE_MODE.DECREASE_NEXT;
					}
					else if (em.getTag() == EmoUnit.WordTag.NextDescEnhancer)
					{
						op_src = em;
						tm = ESENTENCE_TRAVERSE_MODE.INCREASE_NEXT;
					}
					else if (em.getTag() == EmoUnit.WordTag.InvertNextDesc)
					{
						op_src = em;
						tm = ESENTENCE_TRAVERSE_MODE.INVERT_NEXT;
					}
				}
				else
				{
					if (em.getTag() == EmoUnit.WordTag.Subject || em.getTag() == EmoUnit.WordTag.Object)
					{
						if (tm == ESENTENCE_TRAVERSE_MODE.DESC_JOIN)
						{
							
							em.setExt(op_src.getOrigin()+"+"+op_src.getExt());
							em.importVectors(op_src);
							// 감정값 복사 후, 연산자로 사용된 EmoUnit을 폐기한다.
							op_src.setTag(EmoUnit.WordTag.Skip);
							op_src.defaultTable();
							tm = ESENTENCE_TRAVERSE_MODE.NORMAL;
						}
					}
					else if (em.getTag() == EmoUnit.WordTag.Desc
							|| em.getTag() == EmoUnit.WordTag.DescNextObject
							|| em.getTag() == EmoUnit.WordTag.DescSubject)
					{
						if (tm == ESENTENCE_TRAVERSE_MODE.DECREASE_NEXT)
						{
							em.reduce(em.JOY);
							em.reduce(em.SORROW);
							em.setExt(op_src.getOrigin());
							// 나머지는 인과관계이므로 안줄여도 될듯.
							op_src.setTag(EmoUnit.WordTag.Skip);
							// 다음에 서술어가 추가로 존재하므로 DESC_JOIN 모드로 변경한다.
							op_src = em;
							tm = ESENTENCE_TRAVERSE_MODE.DESC_JOIN;
						}
						else if (tm == ESENTENCE_TRAVERSE_MODE.INCREASE_NEXT)
						{
							em.enhance(em.JOY);
							em.enhance(em.SORROW);
							em.setExt(op_src.getOrigin());
							// 나머지는 인과관계이므로 안줄여도 될듯.
							op_src.setTag(EmoUnit.WordTag.Skip);
							// 다음에 서술어가 추가로 존재하므로 DESC_JOIN 모드로 변경한다.
							op_src = em;
							tm = ESENTENCE_TRAVERSE_MODE.DESC_JOIN;
						}
						else if (tm == ESENTENCE_TRAVERSE_MODE.INVERT_NEXT)
						{
							em.invertAll();
							em.setExt(op_src.getOrigin());
							// 나머지는 인과관계이므로 안줄여도 될듯.
							op_src.setTag(EmoUnit.WordTag.Skip);
							// 다음에 서술어가 추가로 존재하므로 DESC_JOIN 모드로 변경한다.
							op_src = em;
							tm = ESENTENCE_TRAVERSE_MODE.DESC_JOIN;
						}
						else
						{
							continue;
						}
					}
					else
					{
						continue;
					}
				}
			}
		}
		
		
		
		
		// TODO: ^^^^^^^^^^^ 관계 분석 단계에서 가능한 모든 결합조건을 파악해서 문장 태그를 마친다.
		// CAUTION: 여기에서부터는 Destructive routine이 실행된다. 위에서 태그된 자료 중 일부는 사라지고, ESentence는 Nuri로 완전히 재구성된다.
		
		// 재구성(refactoring)
		for (ESentence es: epr.get(paragraph_index))
		{
			add(buildNuriFromESentence(es));
		}
		epr.clear(); // 사용후 EParagraph결과물 모두 버림
		
		// phase 5: EmotionAlgorithm 내부에 저장된 Nuri 객체들에서 전체 감정 값 추출, 정규화 및 수치화
		double[] fulltext_emo = new double[4];
		fulltext_emo[0] = 0.0; // joy
		fulltext_emo[1] = 0.0; // sorrow
		fulltext_emo[2] = 0.0; // growth
		fulltext_emo[3] = 0.0; // cease
		
		for (Nuri nri:this)
		{
			double[] normalized_emo = new double[4];
			normalized_emo[0] = 0.0; // joy
			normalized_emo[1] = 0.0; // sorrow
			normalized_emo[2] = 0.0; // growth
			normalized_emo[3] = 0.0; // cease
			int relations_length = nri.getRelations().size();
			if (relations_length > 0)
			{
				for (EmoUnit em : nri.getRelations())
				{
					int emovalues[] = em.getVectorAsIntArray();
					normalized_emo[0] += emovalues[0];
					normalized_emo[1] += emovalues[1];
					normalized_emo[2] += emovalues[2];
					normalized_emo[3] += emovalues[3];
				}
				
				normalized_emo[0] /= relations_length;
				normalized_emo[1] /= relations_length;
				normalized_emo[2] /= relations_length;
				normalized_emo[3] /= relations_length;
				
				fulltext_emo[0] += normalized_emo[0];
				fulltext_emo[1] += normalized_emo[1];
				fulltext_emo[2] += normalized_emo[2];
				fulltext_emo[3] += normalized_emo[3];
			}
			nri.setContextEmo(normalized_emo);
		}
		
		// phrase 6: Desc/DescSubject 중에 감정값이 없는 어휘에 대해, 전체 문맥에서 얻은
		// 평균 감정값을 HistoriaModule에 학습시킨다.
		if (size() > 0)
		{
			fulltext_emo[0] = fulltext_emo[0]/size();
			fulltext_emo[1] = fulltext_emo[1]/size();
			fulltext_emo[2] = fulltext_emo[2]/size();
			fulltext_emo[3] = fulltext_emo[3]/size();

			for (Nuri nri:this)
			{
				for (EmoUnit em : nri.getRelations())
				{
					if (em.hasZeroEmotion() && (em.getTag() == EmoUnit.WordTag.Desc || em.getTag() == EmoUnit.WordTag.DescSubject))
					{
						P.d(TAG, "HistoriaModule에 학습 중 : %s", em.getOrigin());
						mlearn.addHistory(em.getOrigin(), fulltext_emo);
					}
				}
			}
		}
	}
	

	
	/**
	 * ArrayList<EParagraph>에 저장된 문장을 모두 처리한다. process() 에서 발생한 예외가 넘어올 수 있다.
	 */
	public void processAll() throws Exception
	{
		int index = 0;
		for (index = 0; index < epr.size(); index++)
		{
			process(index);
		}
		
	}
	
	/**
	 * ResultProcessor 객체를, 알고리즘 객체로부터 추출한다.
	 * @return JSON이나 TXT로 출력이 준비된 ResultProcessor 객체
	 */
	public ResultProcessor extractResultProcessor()
	{
		ResultProcessor resp = new ResultProcessor(this);
		return resp;
	}
	
	/**
	 * EmotionAlgorithm 객체의 상태를 보여준다.
	 */
	public void displayEStatus()
	{
		P.d(TAG, "ArrayList<EParagraph> epr 내의 총 문단 %d개", size());
		for (EParagraph ep : epr)
		{
			P.d(TAG, "==> 문장 %d개", ep.length());
			for (ESentence es : ep)
			{
				P.d(TAG, "====> 문장 (%x): 문장길이 %d글자 감정 단어 %d개", es.hashCode(), es.getWholeText().length(), es.size());
			}
		}
		
		P.d(TAG, "Nuri 객체 수 %d개", size());
	}
}
