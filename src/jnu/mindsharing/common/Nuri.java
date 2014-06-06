package jnu.mindsharing.common;

import java.util.ArrayList;

public class Nuri extends NuriTypes
{

	int subjectType;
	String subjectSrc;
	EmoUnit contextEmo;
	ArrayList<EmoUnit> relations;
	
	public Nuri()
	{
		contextEmo = new EmoUnit();
		relations = new ArrayList<EmoUnit>();
	}
	
	public void setSubject(int o, String word)
	{
		subjectType = o;
		subjectSrc = word;
		
	}
	
	public void setContextEmo(EmoUnit context)
	{
		if (context != null)
		{
			contextEmo.importVectors(context);
		}
	}
	
	public EmoUnit getContextEmo()
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
	
	public EmoUnit getcontextEmo()
	{
		return contextEmo;
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
