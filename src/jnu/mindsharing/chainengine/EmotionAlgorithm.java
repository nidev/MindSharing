package jnu.mindsharing.chainengine;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Iterator;
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
	EmoUnit internal;
	private MorphemeAnalyzer ma;
	private Connection db;
	
	public EmotionAlgorithm(MorphemeAnalyzer hostMA, Connection mindsharing_db)
	{
		// TODO: null 체크
		ma = hostMA;
		db = mindsharing_db;
		
		internal = new EmoUnit();
	}
	
	public void reset()
	{
		internal.defaultTable();
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
	
	public void process(int paragraph_index) throws Exception
	{
		// phase 1: 문장마다 형태소 분석 후 어휘 탐색
		EQueryTool eq = new EQueryTool(db);
		
		for (ESentence es: get(paragraph_index))
		{
			List<MExpression> aresults = ma.analyze(es.getWholeText());
			aresults = ma.postProcess(aresults);
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
									es.add(new EmoUnit(word).setTag(EmoUnit.WordTag.InvertNextDesc));
								}
								else if (eq.isEnhancer(word))
								{
									es.add(new EmoUnit(word).setTag(EmoUnit.WordTag.DescEnhancer));
								}
								else if (eq.isReducer(word))
								{
									es.add(new EmoUnit(word).setTag(EmoUnit.WordTag.DescReducer));
								}
								else
								{
									es.add(new EmoUnit(word).setTag(EmoUnit.WordTag.Skip));
								}
							}
							else if (isTagIn(mtag, "VA", "VXA"))
							{
								es.add(new EmoUnit(cur_morpheme.getString()).setTag(EmoUnit.WordTag.AdjectMarker));
							}
							else if (isTagIn(mtag, "VERB?"))
							{
								// TODO: 현재는 인과 관계파악없이 서술어의 어감으로 파악한다.
								es.add(new EmoUnit(cur_morpheme.getString()).setTag(EmoUnit.WordTag.VerbMarker));
							}
							else if (isTagIn(mtag, "SubjectTrail?"))
							{
								// TODO: 앞부분의 명사(NNG)태그의 연속을 주어로 파악할 수 있도록 한다.
								es.add(new EmoUnit(cur_morpheme.getString()).setTag(EmoUnit.WordTag.SubjectTrailMarker));
							}
							else if (isTagIn(mtag, "ObjectTrail?"))
							{
								// TODO: 앞부분의 명사(NNG)태그의 연속을 목적어나 일반 객체 어휘로 파악할 수 있도록 한다.
								es.add(new EmoUnit(cur_morpheme.getString()).setTag(EmoUnit.WordTag.ObjectTrailMarker));
							}
							else if (isTagIn(mtag, "Nouns?"))
							{
								// TODO: 나중에 하나의 EmoUnit으로 합친다!
								es.add(new EmoUnit(cur_morpheme.getString()).setTag(EmoUnit.WordTag.NounMarker));
							}
							else
							{
								// TODO: 3차를 넘길 때쯤 Skip 부분은 모두 trim한다.
								es.add(new EmoUnit(cur_morpheme.getString()).setTag(EmoUnit.WordTag.Skip));
							}
						}
					}
				}
				else
				{
					/*
					 * 한글이 아닌 경우, 이모티콘 탐색 등을 시도한다.
					 */
					P.d(TAG, "한글이 아닌 문자. 이모티콘 탐색을 시도합니다.");
					if (eq.isEmoticon(mexp.getExp()))
					{
						es.add(new EmoUnit(mexp.getExp()).setTag(EmoUnit.WordTag.Emoticon));
					}
					else
					{
						es.add(new EmoUnit(mexp.getExp()).setTag(EmoUnit.WordTag.Skip));
					}
				}
				
			}
		}
		
		// phase 2: EmoUnit 배열을 순회하면서 태그 재설정 (Marker는 감정값 수신후에 다른 태그로 변경한다. phase2이후로 *Marker가 남아있으면 안됨)
		
		// 형태소간 관계를 비교한다.
		// TODO: 효율적인 태그 비교 방법이 필요함
		// TODO: 만약에 어미가 형용사구라면,
		// 앞의 형태소로 의미를 찾아야할듯하다. 근데 원형은 어떻게 얻지?
		// TODO: -다, -이다 등 문장을 마치는 어휘인지 체크하여, 문장을 마치면 DescSubject로 다시 태그
		// phase 3: EmoUnit 배열의 감정값 정규화
		// phase 4: EmoUnit
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
