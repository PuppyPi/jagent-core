/*
 * Created on Jan 13, 2006
 * 	by the wonderful Eclipse(c)
 */
package rebound.jagent.lib.pray.template;

public class TagVal
{
	protected String sval;
	protected int ival;
	protected boolean whichOne;
	
	public TagVal(String val)
	{
		super();
		sval = val;
		ival = 0;
		whichOne = true;
	}
	
	public TagVal(int val)
	{
		super();
		sval = null;
		ival = val;
		whichOne = false;
	}
	
	
	public String getStringValue()
	{
		return sval;
	}
	
	public int getIntegerValue()
	{
		return ival;
	}
	
	public boolean isStringTagVal()
	{
		return whichOne;
	}
	
	public boolean isIntegerTagVal()
	{
		return !whichOne;
	}
}
