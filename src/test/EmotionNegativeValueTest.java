package test;

import libs.ELog;
import libs.EmotionNegativeValue;

public class EmotionNegativeValueTest
{

	public static void main(String[] args)
	{
		/*
		 * 원래 자바는 이게 정석이라고 하더군. ㅋㅋㅋㅋㅋ
		 * 실행기는 따로 만들고, 생성할 객체는 다른 파일로 뺀다!
		 */
		EmotionNegativeValue np = new EmotionNegativeValue();
		ELog.d("사전에 있다 없다.", np.isInDictionary("Test"));
		ELog.d("감정수치.", np.getConstant("Test"));
		ELog.d("단어찾기.", np.findWords("Test"));

	}

}
