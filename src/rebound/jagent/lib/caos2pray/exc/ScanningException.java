/*
 * Created on May 28, 2006
 * 	by the wonderful Eclipse(c)
 */
package rebound.jagent.lib.caos2pray.exc;

public abstract class ScanningException
extends Exception
{
	private static final long serialVersionUID = 1L;
	
	public ScanningException()
	{
		super();
	}
	
	public ScanningException(String message, Throwable cause)
	{
		super(message, cause);
	}
	
	public ScanningException(String message)
	{
		super(message);
	}
	
	public ScanningException(Throwable cause)
	{
		super(cause);
	}
	
	
	
	public abstract String getDetailMessage();
}
