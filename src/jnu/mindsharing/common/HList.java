/**
 * 
 */
package jnu.mindsharing.common;

import java.util.ArrayList;

/**
 * @author nidev
 *
 */
public class HList extends ArrayList<Hana>
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1986635344537411432L;
	
	public Hana last()
	{
		if (size() > 0)
		{
			return get(size()-1);
		}
		return new Hana(); // TODO: 차라리 null 대신에 더미 객체를 주면 훨씬 좋지 않을까!
	}
	
	public Hana first()
	{
		if (size() > 0)
		{
			return get(0);
		}
		return new Hana();
	}
	
	public Hana next(int pos)
	{
		if (size() > pos+1)
		{
			return get(pos+1);
		}
		return new Hana();
	}
	
	public Hana prev(int pos)
	{
		if (size() > pos)
		{
			return get(pos);
		}
		return new Hana();
	}
	
	/**
	 * HList에서 Skip으로 태그된 어휘를 모두 제거하고, 전후 사이즈 차이를 반환한다.
	 * @return
	 */
	public int compaction()
	{
		int current = size();
		for (int i = 0; i < size(); i++)
		{
			if (get(i).getXTag().equals(XTag_atomize.Skip))
				set(i, null);
		}
		while(remove(null));
		return current  - size();
	}
}
