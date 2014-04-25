package jnu.mindsharing.chainengine;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import jnu.mindsharing.common.EParagraph;
import jnu.mindsharing.common.ESentence;
import jnu.mindsharing.common.EmoUnit;
import jnu.mindsharing.common.P;

import org.snu.ids.ha.ma.MExpression;
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
	
	public void addESentenceTo(int paragraph_index, ESentence es)
	{
		get(paragraph_index).add(es);
	}
	
	public void process(int paragraph_index) throws Exception
	{
		// phase 1: 문장마다 형태소 분석 후 단어 분리
		for (ESentence es: get(paragraph_index))
		{
			List<MExpression> aresults = ma.analyze(es.getWholeText());
			aresults = ma.postProcess(aresults);
			aresults = ma.leaveJustBest(aresults);
			
			// TODO: 주어목적어 후보군 생성 작업이 이루어지지않음
			for(int i=0; i < aresults.size(); i++)
			{
				/*
				 * TODO: 반드시 코드 동작을 검증하기 바람.
				 * 꼬꼬마 형태소 분석기는 중간 결과를 활용하기 매우 어렵게 되어있음.
				 */
				MExpression mexp = aresults.get(i);
				P.d(TAG, "형태소 분석기 어휘 : %s" , mexp.getExp());
				
			}
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
				P.d(TAG, "====> 문장 (%x): 내부 문장수 %d개 감정 단어 %d개", es.length(), es.eUnitLength());
			}
		}
	}
}
