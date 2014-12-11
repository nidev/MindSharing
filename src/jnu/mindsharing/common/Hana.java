package jnu.mindsharing.common;

import java.io.Serializable;

/**
 * 감정 정보처리를 위한 단위 객체이다.
 * 
 * 이 개체는 다음과 같은 요소를 가지고 있다.
 * 
 * 2차원 벡터 - [감정적으로 긍정/부정을 판단하는 확률, 상황변화적으로 긍정/부정을 판단하는 확률]
 * 세기(Amplification) - 확률을 감정값 출력으로 변환하기 위한 값
 * 
 * 
 * [행복, 즐거움, 만족] 을 나타내는 eprob+ (값 범위: 0~1) => Emotion -> +
 * [슬픔, 상심, 불만족] 을 나타내는 eprob- (값 범위: -1~0) => Emotion -> -
 * [발전, 생산, 탄생] 을 나타내는 sprob+ (값 범위: 0~1) => State -> +
 * [쇠퇴, 파괴, 죽음] 을 나타내는 sprob- (값 범위: -1~0) => State -> -
 * 
 * 벡터의 최대 크기는 sqrt(2)이며 각 벡터는 x, y 각각 [-1, 1] 내에서만 결정한다. 
 * 
 * 이 객체는 감정값을 주고 받는 자료구조로 사용할 수 있다. (getConfiguration().equals("Meta"))
 * 기본적으로 당연히 Expression을 전달하는 자료구조이다. (getConfiguration().equals("Expression"))
 * 
 * Expression으로 객체가 생성될 때는 Sense 모듈을 통하여 자동으로 감정값을 평가 받는다.
 * 
 * @author nidev
 *
 */

public class Hana extends DatabaseConstants implements Serializable
{
	enum PROJECTILE_FUNCTIONS {LOGARITHM_BASE10, LOGARITHM_NATURAL, LINEAR};
	final PROJECTILE_FUNCTIONS useFunction = PROJECTILE_FUNCTIONS.LOGARITHM_BASE10;
	
	public static String[] configuration = {"Meta", "Expression"};
	private static final long serialVersionUID = -7264959704236402603L;
	private String configuredAs;
	private String content;
	private String extendedTag;
	private int wordtype;
	private double eprob = 0.0;
	private double sprob = 0.0;
	private int multiplier = 1; // 기본 증폭을 위한 수, 기본 어휘를 위한 기능임. 1배는 기본 출력을 내보냄
	private int amplifier = 1; // 출력. 최소 출력은 1임. (즉, 최소는 확률값을 출력으로 내보낸다.
	
	public Hana()
	{
		configuredAs = configuration[0];
		extendedTag = "-";
	}
	
	public Hana(String expr)
	{
		configuredAs = configuration[1];
		extendedTag = "";
		content = expr;
	}
	
	public Hana(String expr, int wt)
	{
		configuredAs = configuration[1];
		content = expr;
		wordtype = wt;
		extendedTag = "";
	}
	
	public String getConfiguration()
	{
		return configuredAs;
	}
	
	public int getMultiplier()
	{
		return multiplier;
	}
	
	public Hana setMultiplier(int mult)
	{
		multiplier = mult;
		return this; // joined 연산 지원
	}
	
	public int getAmplifier()
	{
		return amplifier;
	}
	
	public Hana setAmplifier(int amp)
	{
		amplifier = amp;
		return this; // joined 연산 지원
	}
	
	public double[] getProb()
	{
		double[] vp = new double[2];
		vp[0] = eprob;
		vp[1] = sprob;
		return vp;
	}
	
	public Hana setProb(double given_eprob, double given_sprob)
	{
		eprob = given_eprob;
		sprob = given_sprob;
		cutProbVector();
		return this; // joined 연산 지원
	}
	
	public boolean areZeroProbs()
	{
		return (eprob == 0.0 && sprob == 0.0);
	}
	
	public void cutProbVector()
	{
		if (eprob < -1.0)
			eprob = -1.0;
		else if (eprob > 1.0)
			eprob = 1.0;
		
		if (sprob < -1.0)
			sprob = -1.0;
		else if (sprob > 1.0)
			sprob = 1.0;
	}
	
	public Hana merge(Hana operand)
	{
		// 합친다....
		if (operand != null)
		{
			eprob = operand.getProb()[0];
			sprob = operand.getProb()[1];
			amplifier = operand.getAmplifier();
		}
		return this;
	}
	
	public Hana setXTag(String msg)
	{
		extendedTag = msg;
		return this;
	}
	
	public String getXTag()
	{
		return extendedTag;
	}
	
	public Hana setWordtype(int wordtype)
	{
		this.wordtype = wordtype;
		return this; // joined 연산 지원
	}
	
	public int getWordtype()
	{
		return wordtype;
	}
	
	public String debugString()
	{
		return String.format("<Object:Hana expr=%s, wt=%d, ep=%f, sp=%f, amp=%d>",
			content, wordtype, eprob, sprob, amplifier);
	}
	
	/**
	 * 출현빈도(Amp)와 eprob/sprob를 입력으로하여, 지정된 함수를 사용해 전사시켜 감정값을 추출한다.
	 * @return 0번은 eprob기반 감정값, 1번은 sprob기반 감정값
	 */
	public double[] getProjectiles()
	{
		double[] projects = new double[2];
		switch(useFunction)
		{
		case LOGARITHM_BASE10:
			projects[0] = ProjectileFunction.projectileFunctionLog10(eprob, amplifier);
			projects[1] = ProjectileFunction.projectileFunctionLog10(sprob, amplifier);
			break;
		case LOGARITHM_NATURAL:
			projects[0] = ProjectileFunction.projectileFunctionLn(eprob, amplifier);
			projects[1] = ProjectileFunction.projectileFunctionLn(sprob, amplifier);
			break;
		case LINEAR:
			projects[0] = ProjectileFunction.projectileFunctionLinear(eprob, amplifier);
			projects[1] = ProjectileFunction.projectileFunctionLinear(sprob, amplifier);
			break;
		default:
			projects[0] = eprob;
			projects[1] = sprob;
			break;
		}
		return projects;
	}
	
	/**
	 * 내부에 저장된 문자열을 반환한다. configuredAs=meta 인 경우에는 null이 반환된다.
	 * @return 객체 컨텐츠(생성시 입력된 문자열), 없다면 null
	 */
	@Override
	public String toString()
	{
		return content;
	}

}
