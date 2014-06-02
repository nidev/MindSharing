package jnu.mindsharing.chainengine;

import java.util.ArrayList;

import jnu.mindsharing.common.EmoUnit;
import jnu.mindsharing.common.Nuri;
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
	public ResultProcessor(ArrayList<Nuri> ea_result)
	{
		TAG = "-" + hashCode();
		json = new JSONObject();
		if (ea_result == null)
		{
			json.put("data", new JSONArray());
		}
		else
		{
			JSONArray nri_array = new JSONArray();
			for (Nuri nri: ea_result)
			{
				JSONObject subject = new JSONObject();
				subject.put("subject", nri.getSubjectName());
				subject.put("subject_type", nri.getSubjectType());
				
				JSONArray subjectEmo = new JSONArray();
				EmoUnit subem = nri.getSubjectEmo();
				subjectEmo.add(EmoUnit.epowerToInt(subem.getVectorSize(subem.JOY)));
				subjectEmo.add(EmoUnit.epowerToInt(subem.getVectorSize(subem.JOY)));
				subjectEmo.add(EmoUnit.epowerToInt(subem.getVectorSize(subem.JOY)));
				subjectEmo.add(EmoUnit.epowerToInt(subem.getVectorSize(subem.JOY)));
				
				subject.put("subject_emotion", subjectEmo);
				
				
				JSONArray relations_array = new JSONArray();
				for (EmoUnit em: nri.getRelations())
				{
					JSONObject relation_object = new JSONObject();
					relation_object.put("text", em.getOrigin());
					relation_object.put("tag", em.getTag().toString());
					relation_object.put(em.JOY, EmoUnit.epowerToInt(em.getVectorSize(em.JOY)));
					relation_object.put(em.SORROW, EmoUnit.epowerToInt(em.getVectorSize(em.SORROW)));
					relation_object.put(em.GROWTH, EmoUnit.epowerToInt(em.getVectorSize(em.GROWTH)));
					relation_object.put(em.CEASE, EmoUnit.epowerToInt(em.getVectorSize(em.CEASE)));
					
					relations_array.add(relation_object);
					
				}
				subject.put("relations", relations_array);
				nri_array.add(subject);
			}
			json.put("data", nri_array);
			
		}
		P.d(TAG, "JSON 데이터 생성완료.");
		
		StringBuffer buffer = new StringBuffer(1024);
		buffer.append("# -- START OF ANALYSIS RECORD --\r\n");
		buffer.append("# -- ENCODING: UTF-8 --\r\n");
		if (ea_result == null)
		{
			buffer.append("# No result\r\n");
		}
		else
		{
			buffer.append("# Structured sentences\r\n");
			
			for (Nuri nri: ea_result)
			{
				buffer.append(String.format("# Subject on %s (%d relations)", nri.getSubjectName(), nri.getRelations().size()));
				buffer.append("\r\n");
				EmoUnit subem = nri.getSubjectEmo();
				buffer.append(String.format("# Subject emotion : " + String.format("%s(%d),%s(%d),%s(%d),%s(%d)",
						subem.JOY, EmoUnit.epowerToInt(subem.getVectorSize(subem.JOY)),
						subem.SORROW, EmoUnit.epowerToInt(subem.getVectorSize(subem.SORROW)),
						subem.GROWTH, EmoUnit.epowerToInt(subem.getVectorSize(subem.GROWTH)),
						subem.CEASE, EmoUnit.epowerToInt(subem.getVectorSize(subem.CEASE)))));
				buffer.append("\r\n");
				buffer.append("# Emotional expression related to subject\r\n");
				for (EmoUnit em: nri.getRelations())
				{
					buffer.append(String.format("%s(%d),%s(%d),%s(%d),%s(%d) %s\t:%s",
							em.JOY, EmoUnit.epowerToInt(em.getVectorSize(em.JOY)),
							em.SORROW, EmoUnit.epowerToInt(em.getVectorSize(em.SORROW)),
							em.GROWTH, EmoUnit.epowerToInt(em.getVectorSize(em.GROWTH)),
							em.CEASE, EmoUnit.epowerToInt(em.getVectorSize(em.CEASE)),
							em.getTag().toString(), em.getOrigin()
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
