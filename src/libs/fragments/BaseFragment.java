package libs.fragments;

import java.util.ArrayList;
import java.util.HashMap;

/*
 * 각 프래그먼트 객체가 사용할 인터페이스를 정의하는 인터페이스 파일
 */

interface BaseFragment
{
	
	// 모든 것을 구현할 필요는 없음. 
	
	// 분석된 텍스트 원본 자체가 저장되어있는 변수 
	ArrayList<String> sourceTexts = new ArrayList<String>();
	
	//  getNextFragment()를 하면 얻는 다음 Fragment
	int nextFragmentId = 0;
	// 계산된 감정 평균값
	int averageEmotionValue = 0;
	// 분석 결과 저장을 위한 HashMap
	HashMap<BaseFragment, Integer> fragments = new HashMap<BaseFragment, Integer>();

	
	public ArrayList<String> getTexts(); // 본문 가져오기
	public ArrayList<BaseFragment> getFragments(); // 프래그먼트 가져오기
	public int getAverageEmotionVector(); // 평균 감정값 리턴
	
	public HashMap<String, Integer> getEmotionVectors(); // 분석 결과가 저장된 HashMap 전체 리턴
	public int lengthFragments(); // HashMap 길이 리턴
	public int lengthRemainingFragments(); // HashMap 전체 길이에서 nextFragmentId 를 뺀 값
	public boolean isNextFragmentOK(); // 다음 프래그먼트를 가져올 수 있는지 여부 출력
	public void resetNextFragmentPosition(); // nextFragmentId를 0으로 만듦
	public void setIgnoreFlag(); // 무시 설정 (아직 모름)
	
	public BaseFragment getNextFragments(); // 다음 프래그먼트 가져오기
}
