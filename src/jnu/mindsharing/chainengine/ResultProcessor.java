package jnu.mindsharing.chainengine;

import jnu.mindsharing.common.EParagraph;
import jnu.mindsharing.common.ESentence;
import jnu.mindsharing.common.EmoUnit;
import jnu.mindsharing.common.P;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class ResultProcessor
{
	/*
	 * 이 오브젝트는 JSON 구조와 거의 동일하게 구성될 것이므로...
	 */
	private String TAG = "CERO";
	
	private JSONObject json;
	private String txt;
	
	@SuppressWarnings(value = { "unchecked" })
	public ResultProcessor(EParagraph epr)
	{
		TAG = "-" + hashCode();
		json = new JSONObject();
		if (epr == null)
		{
			json.put("data", new JSONArray());
		}
		else
		{
			JSONArray paragraphs_array = new JSONArray();
			for (ESentence es: epr)
			{
				JSONObject sentence_object = new JSONObject();
				sentence_object.put("text", es.getWholeText());
				sentence_object.put("emounit_size", es.size());
				
				JSONArray emounits_array = new JSONArray();
				for (EmoUnit em: es)
				{
					JSONObject emounit_object = new JSONObject();
					emounit_object.put("text", em.getOrigin());
					emounit_object.put("tag", em.getTag().toString());
					emounit_object.put(em.JOY, EmoUnit.epowerToInt(em.getVectorSize(em.JOY)));
					emounit_object.put(em.SORROW, EmoUnit.epowerToInt(em.getVectorSize(em.SORROW)));
					emounit_object.put(em.GROWTH, EmoUnit.epowerToInt(em.getVectorSize(em.GROWTH)));
					emounit_object.put(em.CEASE, EmoUnit.epowerToInt(em.getVectorSize(em.CEASE)));
					
					emounits_array.add(emounit_object);
					
				}
				sentence_object.put("emounits", emounits_array);
				paragraphs_array.add(sentence_object);
			}
			json.put("data", paragraphs_array);
			
		}
		P.d(TAG, "JSON 데이터 생성완료.");
		
		StringBuffer buffer = new StringBuffer(1024);
		buffer.append("# -- START OF ANALYSIS RECORD --\r\n");
		buffer.append("# -- ENCODING: UTF-8 --\r\n");
		if (epr == null)
		{
			buffer.append("# Void EParagraph\r\n");
		}
		else
		{
			buffer.append("# EParagraph\r\n");
			buffer.append(epr.getWholeText());
			buffer.append("\r\n");
			for (ESentence es: epr)
			{
				buffer.append(String.format("# ESentence : %s(%d EmoUnits)", es.getWholeText(), es.size()));
				buffer.append("\r\n");
				for (EmoUnit em: es)
				{
					buffer.append(String.format("%s\t= %s(%d),%s(%d),%s(%d),%s(%d) tag: %s",
							em.getOrigin(),
							em.JOY, EmoUnit.epowerToInt(em.getVectorSize(em.JOY)),
							em.SORROW, EmoUnit.epowerToInt(em.getVectorSize(em.SORROW)),
							em.GROWTH, EmoUnit.epowerToInt(em.getVectorSize(em.GROWTH)),
							em.CEASE, EmoUnit.epowerToInt(em.getVectorSize(em.CEASE)),
							em.getTag().toString()
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
	
	@SuppressWarnings(value = { "unchecked" })
	public void addErrorInfo(String error_code, String error_message)
	{
		// XXX: 좋은 코드가 아님. Object를 통으로 받기 때문에, 경고가 뜬다.
		json.put("error", error_code);
		json.put("error_msg", error_message);
		// null check?
		txt = txt + String.format("# error (%s) : error_msg(%s)\r\n", error_code, error_message);
	}
	
	public String toJSON()
	{
		return json.toJSONString();
	}
	
	public String toTXT()
	{
		return txt;
	}
}
