package jnu.mindsharing.legacy.test;

import jnu.mindsharing.legacy.libs.ELog;
import jnu.mindsharing.legacy.libs.EmotionPositiveValue;

public class EmotionPositiveValueTest
{

	public static void main(String[] args)
	{
		/*
		 * 원래 자바는 이게 정석이라고 하더군. ㅋㅋㅋㅋㅋ
		 * 실행기는 따로 만들고, 생성할 객체는 다른 파일로 뺀다!
		 */
		EmotionPositiveValue ep = new EmotionPositiveValue();
		ELog.d("단어검색 결과.", ep.lossySearch(""));

	}

}
