package jnu.mindsharing.common;

public class ProjectileFunction
{
	public static double projectileFunctionLinear(double prob, int amp)
	{
		return prob * amp;
	}
	
	public static double projectileFunctionLog10(double prob, int amp)
	{
		return prob * Math.log10(10 + amp);
	}
	
	public static double projectileFunctionLn(double prob, int amp)
	{
		return prob * Math.log(Math.E + amp);
	}
}
