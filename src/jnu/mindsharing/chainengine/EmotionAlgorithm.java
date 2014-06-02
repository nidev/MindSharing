package jnu.mindsharing.chainengine;

import java.util.ArrayList;

import jnu.mindsharing.common.EParagraph;
import jnu.mindsharing.common.ESentence;
import jnu.mindsharing.common.EmoUnit;
import jnu.mindsharing.common.Nuri;
import jnu.mindsharing.common.NuriTypes;
import jnu.mindsharing.common.P;

import org.snu.ids.ha.ma.MorphemeAnalyzer;

public class EmotionAlgorithm extends ArrayList<Nuri>
{
	private static final long serialVersionUID = -6004998827195979333L;
	
	private String TAG = "Algorithm";
	private MorphemeAnalyzer ma;
	private ArrayList<EParagraph> epr;
	
	public EmotionAlgorithm(MorphemeAnalyzer hostMA)
	{
		// TODO: null 체크
		ma = hostMA;
		epr = new ArrayList<EParagraph>();
	}
	
	public int createNewParagraph(String text)
	{
		// Return: index
		epr.add(new EParagraph(text));
		
		return size()-1;
	}
	
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
	
	public void addESentenceTo(int paragraph_index, ESentence es)
	{
		epr.get(paragraph_index).add(es);
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
	
	public Nuri buildNuriFromESentence(ESentence es)
	{
		Nuri nri = new Nuri();
		nri.setSubject(NuriTypes.PERSON, es.getSubject().getOrigin(), es.getSubjectDesc());
		
		// XXX: 잘 구성해야함
		
		
		return null;
	}
	
	public void process(int paragraph_index) throws Exception
	{
		// phase 1: 문장마다 형태소 분석 후 어휘 탐색
		EQueryTool eq = new EQueryTool();
		
		for (ESentence es: epr.get(paragraph_index))
		{
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
			
			// 관계 분석 단계 - 2: 인칭 문제를 해결한다. 여기에서 주어를 발견할 수 없다면, Object 중에 주어를 선정하고, 그게 안되면 주어 '나'를 삽입한다.
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
		}
		// TODO: ^^^^^^^^^^^ 관계 분석 단계에서 가능한 모든 결합조건을 파악해서 문장 태그를 마친다.
		// CAUTION: 여기에서부터는 Destructive routine이 실행된다. 위에서 태그된 자료 중 일부는 사라지고, ESentence는 Nuri로 완전히 재구성된다.
		
		// 재구성(refactoring)
		for (ESentence es: epr.get(paragraph_index))
		{
			add(buildNuriFromESentence(es));
		}
		
		// phase 5: SegmentArray 순회하면서 전체 감정 값 추출
		// phase 
		// phase 6: EmoUnit 배열 순회하면서 글쓴이의 감정 파악
		// (연관 단어: 날씨, 기분, 감정, 빈정 등의 키워드에 이루어지는 수식들), 기타 상황 서술어(짜증난다)
		
		epr.clear(); // 사용후 EParagraph결과물 모두 버림
	}
	

	
	public void processAll() throws Exception
	{
		int index = 0;
		for (index = 0; index < size(); index++)
		{
			process(index);
		}
		
	}
	
	
	public ResultProcessor extractResultProcessor()
	{
		displayEStatus();
		ResultProcessor resp = new ResultProcessor(this);
		return resp;
	}
	
	public void displayEStatus()
	{
		P.d(TAG, "ArrayList<EParagraph> epr 내의 총 문단 %d개", size());
		for (EParagraph ep : epr)
		{
			P.d(TAG, "==> 문단 (%x): 문장 d개", ep.hashCode(), ep.length());
			for (ESentence es : ep)
			{
				P.d(TAG, "====> 문장 (%x): 문장길이 %d글자 감정 단어 %d개", es.hashCode(), es.getWholeText().length(), es.size());
			}
		}
		
		P.d(TAG, "Nuri 객체 수 %d개", size());
	}
}
