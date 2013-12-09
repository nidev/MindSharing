package libs.fragments;

import java.util.ArrayList;

import libs.ELog;

/*
interface BaseFragment
{
	// 모든 것을 구현할 필요는 없음. 
	public String getSourceText(); // 작업 전 본문 가져오기
	public ArrayList<String> getSlicedText(); // 나눠진 본문 가져오기
	public ArrayList<?> getFragments(); // 프래그먼트 가져오기
	
	public int lengthFragments(); // HashMap 길이 리턴
	public int lengthRemainingFragments(); // HashMap 전체 길이에서 nextFragmentId 를 뺀 값
	public boolean isNextFragmentOK(); // 다음 프래그먼트를 가져올 수 있는지 여부 출력
	public void resetNextFragmentPosition(); // nextFragmentId를 0으로 만듦
	public void setIgnoreFlag(); // 무시 설정 (아직 모름)
	
	public Object getNextFragments(); // 다음 프래그먼트 가져오기
}
*/

public class BaseFragment
{
	// 분석된 텍스트 원본 자체가 저장되어있는 변수
	// 형태소 레벨 프래그먼트는 이곳이 비어있다.
	public ArrayList<BaseFragment> fragments = null;
	 // 문장 부호든, 띄어쓰기든, 형태소든 나눠진 단어를 저장
	public ArrayList<String> slicedText = null;
	// 원본 텍스트
	public String sourceText = "";
	// 현재 프래그먼트 위치 (getNextFragment)에서 사용
	public int current_fragment_id = 0;
	
	// 객체 ID
	private int id = -1;
	// 디버깅 태그
	public String TAG = "BSF=";
	
	public BaseFragment()
	{
		fragments = new ArrayList<BaseFragment>();
		slicedText = new ArrayList<String>();
		id = hashCode();
		TAG += id;
	}
	
	public BaseFragment(String p_sourceText, ArrayList<String> p_slices)
	{
		fragments = new ArrayList<BaseFragment>();
		slicedText = p_slices;
		sourceText = p_sourceText;
		for (String slice: slicedText)
		{
			fragments.add(new BaseFragment(slice));
		}
		id = hashCode();
		TAG += id;
	}
	
	public BaseFragment(String p_sourceText)
	{
		fragments = new ArrayList<BaseFragment>();
		slicedText = new ArrayList<String>();
		slicedText.add(p_sourceText);
		sourceText = p_sourceText;
		id = hashCode();
		TAG += id;
	}
	
	public String getSourceText()
	{
		return sourceText;
	}
	
	public ArrayList<String> getSlicedText() // 나눠진 본문 가져오기
	{
		return slicedText;
		
	}
	
	public ArrayList<BaseFragment> getFragments() // 프래그먼트 가져오기
	{
		return fragments;
		
	}
	
	public int lengthFragments() // HashMap 길이 리턴
	{
		return fragments.size();
		
	}
	
	public int lengthRemainingFragments() // HashMap 전체 길이에서 nextFragmentId 를 뺀 값
	{
		return fragments.size() - current_fragment_id;
	}
	
	public boolean isNextFragmentOK() // 다음 프래그먼트를 가져올 수 있는지 여부 출력
	{
		return (lengthRemainingFragments() > 0);
		
	}
	
	public void resetNextFragmentPosition() // nextFragmentId를 0으로 만듦
	{
		current_fragment_id = 0;
	}
	
	public BaseFragment getNextFragments() // 다음 프래그먼트 가져오기
	{
		return fragments.get(current_fragment_id++);
	}
	
	public void setIgnoreFlag() // 무시 설정 (아직 모름)
	{
		
	}
	
	public void selfPrintInfo() // 자기 자신의 현재 상태를 출력하는 함수
	{
		ELog.d(TAG, "Self-information");
		ELog.d(TAG, "current_fragment_id = " + current_fragment_id);
		ELog.d(TAG, "Fragments.size() = " + fragments.size());
		ELog.d(TAG, "slicedText.size() = " + slicedText.size());
		ELog.printArrayList(TAG, slicedText);
	}
}