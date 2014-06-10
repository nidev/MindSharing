package jnu.mindsharing.common;

import java.util.ArrayList;

/**
 * ESentence로부터, 정제된 정보를 추출하여 보관하는 Nuri 객체의 클래스이다.
 * 주어를 찾으려면 ESentence에서 직접 Subject로 태그된 어휘를 찾아야했지만, 여기에서는 이미 설정된 주어를 간편하게 가져올 수 있다.
 * 또한 Nuri 객체 내부의 평균 감정값을 별도로 기억하고, 불러올 수 있도록 구현되어있다. 이 감정값은 원시 감정값들로부터 계산되었다.
 * 
 * '주어와 주어에 대한 서술어 관계'를 제외한 어휘 중에서도, 감정값을 갖고 있는 어휘는 Relation(연관 감정 어휘)로 취급하고, relations 배열에 어휘를 저장한다.
 * 
 * @author nidev
 *
 */
public class Nuri extends NuriTypes
{

	int subjectType;
	String subjectSrc;
	double contextEmo[];
	ArrayList<EmoUnit> relations;
	
	/**
	 * Nuri 객체를 생성한다.
	 */
	public Nuri()
	{
		contextEmo = new double[4];
		relations = new ArrayList<EmoUnit>();
	}
	
	/**
	 * 주어를 설정한다.
	 * @param o 주어 타입(미구현) - NuriTypes를 참고하기 바람(사람 주어, 물건 주어-실존하는 것, 물건 주어-추상적인 것으로 구분 중)
	 * @param word 주어 문자열
	 * @see NuriTypes
	 */
	public void setSubject(int o, String word)
	{
		subjectType = o;
		subjectSrc = word;
		
	}
	
	/**
	 * 문장 전체에서 계산된 평균 감정값을, contextEmo로 저장한다.
	 * @param emovalues joy/sorrow/growth/cease 순서대로 계산된 감정값이 담긴 double 배열
	 */
	public void setContextEmo(double[] emovalues)
	{
		contextEmo[0] = emovalues[0];
		contextEmo[1] = emovalues[1];
		contextEmo[2] = emovalues[2];
		contextEmo[3] = emovalues[3];
	}
	
	/**
	 * 문장 전체의 평균 감정값을 가져온다.
	 * @return joy/sorrow/growth/cease 순서대로 감정값이 담긴 double 배열
	 */
	public double[] getContextEmo()
	{
		return contextEmo;
	}
	
	/**
	 * 주어의 유형을 반환한다.
	 * @return 주어의 타입(정수)
	 * @see NuriTypes
	 */
	public int getSubjectType()
	{
		return subjectType;
	}
	
	/**
	 * 주어 문자열을 가져온다.
	 * @return 주어 문자열
	 */
	public String getSubjectName()
	{
		return subjectSrc;
	}
	
	/**
	 * 감정값이 담긴 Relation 어휘들의 어레이 리스트를 반환한다.
	 * @return Relation이 담긴 어레이리스트
	 */
	public ArrayList<EmoUnit> getRelations()
	{
		return relations;
	}
	
	/**
	 * Relation 어레이리스트에, 새로운 연관 감정 어휘를 추가한다.
	 * @param em 감정 어휘가 담긴 EmoUnit
	 */
	public void addRelations(EmoUnit em)
	{
		relations.add(em);
	}
}
