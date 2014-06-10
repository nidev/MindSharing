package jnu.mindsharing.common;

/**
 * EmoUnit 객체를 담고 있을 컨테이너는 반드시 다음 인터페이스를 지원해야한다.
 * 
 * @author nidev
 *
 */
public interface EmoSkeleton
{
	/**
	 * EmoUnit으로 가공되기 전의 텍스트를 제공해야한다.
	 * @return 가공 전의 본문
	 */
	public String getWholeText();
	
	/**
	 * EmoUnit으로 가공되기 전의 텍스트를 제공해야한다.
	 * @return 가공 전의 본문, 배열
	 */
	public String[] getTexts();
	
	/**
	 * EmoUnit 들의 갯수를 제공하는 인터페이스를 제공해야한다.
	 * @return EmoUnit 또는 그것의 상위 컨테이너의 수
	 */
	public int length();
}
