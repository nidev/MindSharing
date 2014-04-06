package jnu.mindsharing.chainengine;

import jnu.mindsharing.common.P;

public class CEResultObject
{
	/*
	 * 이 오브젝트는 JSON 구조와 거의 동일하게 구성될 것이므로...
	 */
	private String TAG = "CERO";
	
	public CEResultObject()
	{
		// 어...
		P.d(TAG, "CEResultObject 생성 중. EmotionAlgorithm에서 넘어온 값을 바탕으로 결과 객체를 구성합니다.");
	}
	
	public String toJSON()
	{
		return "{code: 1, msg:'Not ready'}";
	}
	
	public String toXML()
	{
		return "<CEResultObject><code>1</code><msg>Not Ready</msg></CEResultObject>";
	}

}
