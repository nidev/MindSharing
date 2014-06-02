package jnu.mindsharing.common;

import java.util.ArrayList;
import java.util.HashMap;

public class Nuri extends NuriTypes
{

	int subjectType;
	String subjectSrc;
	EmoUnit subjectEmo;
	ArrayList<EmoUnit> relations;
	
	public Nuri()
	{
		subjectEmo = new EmoUnit();
		relations = new ArrayList<EmoUnit>();
	}
	
	public void setSubject(int o, String word, EmoUnit emotion_value)
	{
		subjectType = o;
		subjectSrc = word;
		subjectEmo.importVectors(emotion_value);
	}
	
	public int getSubjectType()
	{
		return subjectType;
	}
	
	public String getSubjectName()
	{
		return subjectSrc;
	}
	
	public EmoUnit getSubjectEmo()
	{
		return subjectEmo;
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
