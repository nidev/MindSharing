package jnu.mindsharing.common;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class ExprHash
{
	final String USE_MD5 = "MD5";
	final String USE_SHA1 = "SHA-1";
	final String USE_SHA256 = "SHA-256";
	
	String hashValue;
	
	public ExprHash(String source)
	{
		String TAG = "ExprHasher";
		try
		{
			MessageDigest mdobject = MessageDigest.getInstance(USE_MD5);
			byte[] byte_digest =  mdobject.digest(source.getBytes("UTF-8"));
			StringBuffer sb = new StringBuffer();
			for (byte b : byte_digest)
			{
				sb.append(String.format("%02x", b & 0xff));
			}
			hashValue = sb.toString();

		}
		catch (NoSuchAlgorithmException | UnsupportedEncodingException e)
		{
			P.e(TAG, "There is no hash algorithm like this.");
			hashValue = null;
		}
	}
	
	/**
	 * 단어의 해시값을 반환한다.
	 * @return 해시 문자열, 실패한 경우는 null
	 */
	@Override
	public String toString()
	{
		return hashValue;
	}

}
