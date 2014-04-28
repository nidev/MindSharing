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
		for (ESentence es: get(paragraph_index))
		{
			List<MExpression> aresults = ma.analyze(es.getWholeText());
			aresults = ma.postProcess(aresults);
			aresults = ma.leaveJustBest(aresults);
			

			// 루틴 내에서 이전 객체를 재사용하기 위해 외부로 이동
			MCandidate last_mc = null;
			Morpheme last_morpheme = null;
			
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
					
					Iterator<MCandidate> mc_iter = mexp.iterator();
					//P.d(TAG, mexp.toString());
					while (mc_iter.hasNext())
					{
						MCandidate cur_mc = mc_iter.next();
						
						
						Iterator<Morpheme> morph_iter = cur_mc.iterator();  
						
						while (morph_iter.hasNext())
						{
							Morpheme cur_morpheme = morph_iter.next();
							P.d("형태소", "--[단어:%s] %s %s %s %s", cur_mc.getExp(), cur_morpheme.getCharSetName(), cur_morpheme.getString(), cur_morpheme.getTag(), cur_morpheme.getComposed());
							// 형태소간 관계를 비교한다.
							// TODO: 효율적인 태그 비교 방법이 필요함
							if (isTagIn(cur_morpheme.getTag(), "VA", "VXA"))
							{
								// TODO: 만약에 어미가 형용사구라면,
								// 앞의 형태소로 의미를 찾아야할듯하다. 근데 원형은 어떻게 얻지?
								if (isTagIn(last_morpheme.getTag(), "MAG"))
								{
									String word = last_morpheme.getString();
									//P.d("형태소", "형용사 앞의 부사: %s", last_morpheme.getString());
									// TODO 2: 혹시 switch-case구문으로 정리 가능?
									if (EQueryTool.isNegativeADV(word))
									{
										es.add(new EmoUnit(word).setTag(EmoUnit.WordTag.InvertNextDesc));
									}
									else if (EQueryTool.isEnhancer(word))
									{
										es.add(new EmoUnit(word).setTag(EmoUnit.WordTag.DescEnhancer));
									}
									else if (EQueryTool.isReducer(word))
									{
										es.add(new EmoUnit(word).setTag(EmoUnit.WordTag.DescReducer));
									}
									else
									{
										es.add(new EmoUnit(word).setTag(EmoUnit.WordTag.Skip));
									}
								}
								// TODO: -다, -이다 등 문장을 마치는 어휘인지 체크하여, 문장을 마치면 DescSubject로 다시 태그
								es.add(new EmoUnit(cur_morpheme.getString()).setTag(EmoUnit.WordTag.DescNextObject));
							}
							else
							{
								es.add(new EmoUnit(cur_morpheme.getString()).setTag(EmoUnit.WordTag.Skip));
							}
							last_morpheme = cur_morpheme;
						}
						
						last_mc = cur_mc; // 이전 후보군을 다시 기억하고 다음 후보로 감
					}
				}
				else
				{
					/*
					 * 한글이 아닌 경우, 이모티콘 탐색 등을 시도한다.
					 */
					P.d(TAG, "한글이 아닌 문자. 이모티콘 탐색을 시도합니다.");
					if (EQueryTool.isEmoticon(mexp.getExp()))
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
		
		// phase 2: EmoUnit 배열을 순회하면서 태그 재설정
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
