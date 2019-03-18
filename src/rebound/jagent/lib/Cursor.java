/*
 * Created on Jan 5, 2006
 * 	by the wonderful Eclipse(c)
 */
package rebound.jagent.lib;

public class Cursor
{
	protected int pos;
	
	public void advance()
	{
		advance(1);
	}
	
	public void advance(int a)
	{
		pos += a;
	}
	
	public int getPos()
	{
		return pos;
	}
	
	public int iteratePos()
	{
		return pos++;
	}
	
	public void reset()
	{
		pos = 0;
	}
	
	public void seek(int newPos)
	{
		pos = newPos;
	}
}
