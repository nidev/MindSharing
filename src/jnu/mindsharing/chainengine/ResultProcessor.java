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
	public ResultProcessor(ArrayList<HList> sentences)
	{
		TAG = TAG + hashCode();
		json = new JSONObject();
		if (sentences == null)
		{
			json.put("data", new JSONArray());
		}
		else
		{
			JSONArray hn_array = new JSONArray();
			for (HList hlist: sentences)
			{
				JSONObject json_sentence = new JSONObject();
				int amp = hlist.get(0).getAmplifier();
				double[] vp = hlist.get(0).getProb();
				double[] value = hlist.get(0).getProjectiles();
				json_sentence.put("ctx_eprob", vp[0]);
				json_sentence.put("ctx_sprob", vp[1]);
				json_sentence.put("amp", amp);
				json_sentence.put("ctx_evalue", value[0]);
				json_sentence.put("ctx_svalue", value[1]);
				
				JSONArray json_wordlist = new JSONArray();
				for (Hana hn: hlist.subList(1, -1))
				{
					double[] mapped_to = hn.getProjectiles();
					
					JSONObject json_word = new JSONObject();
					json_word.put("word", hn.toString());
					json_word.put("tag", hn.getXTag());
					json_word.put("eprob", hn.getProb()[0]);
					json_word.put("sprob", hn.getProb()[1]);
					json_word.put("amp", hn.getAmplifier());
					json_word.put("evalue", mapped_to[0]);
					json_word.put("svalue", mapped_to[1]);
					json_wordlist.add(json_word);
				}
				
				json_sentence.put("words", json_wordlist);
				hn_array.add(json_sentence);
			}
			json.put("data", hn_array);
			
		}
		P.d(TAG, "JSON 데이터 생성완료.");
		
		StringBuffer buffer = new StringBuffer(1024);
		buffer.append("# -- START OF ANALYSIS RECORD --\r\n");
		buffer.append("# -- ENCODING: UTF-8 --\r\n");
		if (sentences == null)
		{
			buffer.append("# No result\r\n");
		}
		else
		{
			buffer.append("# Structured sentences\r\n");
			
			int sentence_no = 0;
			
			for (HList hlist: sentences)
			{
				sentence_no++;
				buffer.append(String.format("# Sentence #%d has Subject as %s (%d words)", sentence_no, hlist.get(1).toString()));
				buffer.append("\r\n");
				int amp = hlist.get(0).getAmplifier();
				double[] vp = hlist.get(0).getProb();
				buffer.append(String.format("# Emotional p-vector %.4f / State p-vector %.4f / Amplifier: %d ", vp[0], vp[1], amp));
				buffer.append("\r\n");
				buffer.append("# Expressions in this sentence are:\r\n");
				for (Hana hn: hlist.subList(1, -1))
				{
					double[] mapped_to = hn.getProjectiles();
					buffer.append(String.format("%s(tag:%s) [e %.4f s %.4f ^ %d ] mapped to (%4f, %4f)",
							hn.toString(), hn.getXTag(), hn.getProb()[0], hn.getProb()[1], hn.getAmplifier(),
							mapped_to[0], mapped_to[1]));
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
