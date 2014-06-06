package jnu.mindsharing.common;

import java.util.ArrayList;

public class Nuri extends NuriTypes
{

	int subjectType;
	String subjectSrc;
	double contextEmo[];
	ArrayList<EmoUnit> relations;
	
	public Nuri()
	{
		contextEmo = new double[4];
		relations = new ArrayList<EmoUnit>();
	}
	
	public void setSubject(int o, String word)
	{
		subjectType = o;
		subjectSrc = word;
		
	}
	
	public void setContextEmo(double[] emovalues)
	{
		contextEmo[0] = emovalues[0];
		contextEmo[1] = emovalues[1];
		contextEmo[2] = emovalues[2];
		contextEmo[3] = emovalues[3];
	}
	
	public double[] getContextEmo()
	{
		return contextEmo;
	}
	
	public int getSubjectType()
	{
		return subjectType;
	}
	
	public String getSubjectName()
	{
		return subjectSrc;
	}
	
	public ArrayList<EmoUnit> getRelations()
	{
		return relations;
	}
	
	public void addRelations(EmoUnit em)
	{
		relations.add(em);
	}
}
