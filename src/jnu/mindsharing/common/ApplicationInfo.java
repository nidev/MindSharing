package jnu.mindsharing.common;


/**
 * 각 패키지 중, 가장 중심이 되는 클래스에서 해당 클래스의 정보를 제공하기 위한 인터페이스이다.
 * 
 * @author nidev
 *
 */
public interface ApplicationInfo
{
	/**
	 * 어플리케이션 정보를 제공하는 클래스는 버전 코드를 제공해야한다.
	 * @return 버전 코드
	 */
	public String getVersionCode();
	
	/**
	 * 어플리케이션 정보를 제공하는 클래스는 버전 넘버를 제공해야한다.
	 * @return 버전 넘버
	 */
	public int getVersionNumber();
	
	/**
	 * 어플리케이션 정보를 제공하는 클래스는 내부 라이브러리의 저작권 정보를 제공해야한다.
	 * @return 라이센스 정보
	 */
	public String getLicenseInfo();
}
