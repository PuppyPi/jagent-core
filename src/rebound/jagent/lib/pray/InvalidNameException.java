/*
 * Created on Feb 15, 2010
 * 	by the great Eclipse(c)
 */
package rebound.jagent.lib.pray;

public class InvalidNameException
extends Exception
{
	private static final long serialVersionUID = 1L;
	
	protected final String offendingName;
	
	public InvalidNameException()
	{
		super("Invalid name");
		this.offendingName = null;
	}
	
	public InvalidNameException(String offendingName)
	{
		super("The name '"+offendingName+"' is invalid");
		this.offendingName = offendingName;
	}
	
	public InvalidNameException(String offendingName, String reason)
	{
		super("The name '"+offendingName+"' is invalid because "+reason);
		this.offendingName = offendingName;
	}
	
	public String getOffendingName()
	{
		return this.offendingName;
	}
}
