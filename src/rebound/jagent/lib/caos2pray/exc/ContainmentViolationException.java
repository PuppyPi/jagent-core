/*
 * Created on Sep 12, 2009
 * 	by the great Eclipse(c)
 */
package rebound.jagent.lib.caos2pray.exc;

/**
 * Ie, you use an absolute path, or a relative path with '..' where you aren't supposed to.
 * @author RProgrammer
 */
public class ContainmentViolationException
extends ScanningException
{
	private static final long serialVersionUID = 1L;
	
	public ContainmentViolationException()
	{
		super();
	}
	
	public ContainmentViolationException(String message, Throwable cause)
	{
		super(message, cause);
	}
	
	public ContainmentViolationException(String message)
	{
		super(message);
	}
	
	public ContainmentViolationException(Throwable cause)
	{
		super(cause);
	}
	
	@Override
	public String getDetailMessage()
	{
		return getMessage();
	}
}
