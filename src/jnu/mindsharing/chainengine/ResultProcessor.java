package jnu.mindsharing.chainengine;

import jnu.mindsharing.common.EParagraph;
import jnu.mindsharing.common.P;

public class ResultProcessor
{
	/*
	 * 이 오브젝트는 JSON 구조와 거의 동일하게 구성될 것이므로...
	 */
	private String TAG = "CERO";
	
	public ResultProcessor(EParagraph epr)
	{
		// 어...
		P.d(TAG, "CEResultObject 생성 중. EParagraph 객체 내부를 탐색하는 중입니다.");
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
