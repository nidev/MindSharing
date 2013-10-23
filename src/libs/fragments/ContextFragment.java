package libs.fragments;

import java.util.ArrayList;
import java.util.HashMap;

// 작업 중 //

public class ContextFragment implements BaseFragment
{

	public ContextFragment()
	{
		// TODO Auto-generated constructor stub
	}

	@Override
	public ArrayList<String> getTexts()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ArrayList<BaseFragment> getFragments()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getAverageEmotionVector()
	{
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public HashMap<String, Integer> getEmotionVectors()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int lengthFragments()
	{
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int lengthRemainingFragments()
	{
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean isNextFragmentOK()
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void resetNextFragmentPosition()
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setIgnoreFlag()
	{
		// configureContextFragment 가 완료되었는지 설정
		
	}
	
	public boolean isFragmentReady()
	{
		return false;
	}
	
	public void setFragmentReadyFlag()
	{
		// configureContextFragment 가 완료되었을 때 설정
	}

	@Override
	public BaseFragment getNextFragments()
	{
		// TODO Auto-generated method stub
		return null;
	}
}
