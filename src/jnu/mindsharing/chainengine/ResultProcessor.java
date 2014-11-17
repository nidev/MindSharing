package jnu.mindsharing.chainengine;

import java.util.ArrayList;

import jnu.mindsharing.common.HList;
import jnu.mindsharing.common.Hana;
import jnu.mindsharing.common.P;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

/**
 * EmotionAlgorithm에 담긴 분석 결과를, JSON과 TXT 출력으로 사람이 보기 좋게 변환하는 클래스이다.
 * 
 * @author nidev
 *
 */
public class ResultProcessor
{
	/*
	 * 이 오브젝트는 JSON 구조와 거의 동일하게 구성될 것이므로...
	 */
	private String TAG = "RP";
	
	private JSONObject json;
	private String txt;
	
	/**
	 * Nuri 객체를 담고 있는 배열(ex. EmotionAlgorithm 객체)를 JSON나 TXT로 변환한다.
	 * @param hlist EmotionAlgorithm 객체
	 */
	@SuppressWarnings(value = { "unchecked" })
	public ResultProcessor(ArrayList<HList> hlist)
	{
		TAG = TAG + hashCode();
		json = new JSONObject();
		if (hlist == null)
		{
			json.put("data", new JSONArray());
		}
		else
		{
			JSONArray nri_array = new JSONArray();
			for (Hana nri: hlist)
			{
				
			}
			json.put("data", nri_array);
			
		}
		P.d(TAG, "JSON 데이터 생성완료.");
		
		StringBuffer buffer = new StringBuffer(1024);
		buffer.append("# -- START OF ANALYSIS RECORD --\r\n");
		buffer.append("# -- ENCODING: UTF-8 --\r\n");
		if (hlist == null)
		{
			buffer.append("# No result\r\n");
		}
		else
		{
			buffer.append("# Structured sentences\r\n");
			
			for (Nuri nri: hlist)
			{
				buffer.append(String.format("# Subject on %s (%d relations)", nri.getSubjectName(), nri.getRelations().size()));
				buffer.append("\r\n");
				double[] conem = nri.getContextEmo();
				buffer.append(String.format("# Context emotion : JOY(%.5f),SORROW(%.5f),GROWTH(%.5f),CEASE(%.5f)", conem[0], conem[1], conem[2], conem[3]));
				buffer.append("\r\n");
				buffer.append("# Emotional expression related to subject\r\n");
				for (EmoUnit em: nri.getRelations())
				{
					buffer.append(String.format("%s(%d),%s(%d),%s(%d),%s(%d) %s/%s",
							em.JOY, EmoUnit.epowerToInt(em.getVectorSize(em.JOY)),
							em.SORROW, EmoUnit.epowerToInt(em.getVectorSize(em.SORROW)),
							em.GROWTH, EmoUnit.epowerToInt(em.getVectorSize(em.GROWTH)),
							em.CEASE, EmoUnit.epowerToInt(em.getVectorSize(em.CEASE)),
							em.getOrigin(), em.getExt()
							));
					buffer.append("\r\n");
				}
					
			}
		}
		buffer.append("# -- END OF ANALYSIS RECORD --\r\n");
		txt = buffer.toString();
		buffer.delete(0, buffer.length());
		P.d(TAG, "TXT 데이터 생성 완료");
	}
	
	/**
	 * 변환 후의 JSON결과물과 TXT결과물에 각각 엔진 오류 정보를 추가한다.
	 * @param error_code 오류 코드
	 * @param error_message 오류 메시지
	 */
	@SuppressWarnings(value = { "unchecked" })
	public void addErrorInfo(String error_code, String error_message)
	{
		// XXX: 좋은 코드가 아님. Object를 통으로 받기 때문에, 경고가 뜬다.
		json.put("error", error_code);
		json.put("error_msg", error_message);
		// null check?
		txt = txt + String.format("# error (%s) : error_msg(%s)\r\n", error_code, error_message);
	}
	
	/**
	 * JSON 데이터를 반환한다.
	 * @return JSON 객체를 나타내는 문자열
	 */
	public String toJSON()
	{
		return json.toJSONString();
	}
	
	/**
	 * 사람이 보기 편한 텍스트로 요약된 결과를 반환한다.
	 * @return 텍스트 문자열
	 */
	public String toTXT()
	{
		return txt;
	}
}
