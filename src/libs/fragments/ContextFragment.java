package libs.fragments;

import java.util.ArrayList;

import libs.ELog;

// 작업 중 //

public class ContextFragment extends BaseFragment
{
	private boolean isReadyToUse = false; // 준비되면 true
	//private long elapsed_build_time = 0; // 생성에 소요된 시간
	//private long elapsed_analyzing_time = 0; // 분석에 소요된 시간
	
	private long timeOnCreation = 0; // 클래스 생성시각
	private long startTimeOnBuild = 0; // 텍스트 분해 시작시각
	private long endTimeOnBuild = 0; // 텍스트 분해 종료시각
	private long startTimeOnAnalysis = 0; // 텍스트 분석 시작시각
	private long endTimeOnAnalysis = 0; // 텍스트 분석 종료시각
	
	private int contextId = -1;
	private String TAG = "CTX=";	
	

	public ContextFragment()
	{
		super(); // BaseFragment() 생성자 실행
		timeOnCreation = System.currentTimeMillis();
		contextId = hashCode();
		TAG += contextId; // CTX= 뒤에 객체 아이디를 붙여줌
		ELog.d(TAG, "컨텍스트 객체 생성 완료. (텍스트 없음)");
		
		
	}
	
	public ContextFragment(String p_sourceText, ArrayList<String> p_slices)
	{
		super(p_sourceText, p_slices); // BaseFragment() 생성자 실행
		timeOnCreation = System.currentTimeMillis();
		contextId = hashCode();
		TAG += contextId; // CTX= 뒤에 객체 아이디를 붙여줌
		ELog.d(TAG, "컨텍스트 객체 생성 완료. (텍스트 있음)");
	}
	
	public void setStartTimeOnBuild()
	{
		startTimeOnBuild = System.currentTimeMillis();
	}

	public void setEndTimeOnBuild()
	{
		endTimeOnBuild = System.currentTimeMillis();
	}
	
	public void setStartTimeOnAnalysis()
	{
		startTimeOnAnalysis = System.currentTimeMillis();;
	}
	
	public void setEndTimeOnAnalysis()
	{
		endTimeOnAnalysis = System.currentTimeMillis();
	}
	
	public boolean isReadyToUse()
	{
		return isReadyToUse;
	}
	
	public void setReadyToUse()
	{
		isReadyToUse = true;
	}
	
	public void unsetReadyToUse()
	{
		isReadyToUse = false;
	}
}
