package test;

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
		np.isInDictionary("Test");

	}

}
