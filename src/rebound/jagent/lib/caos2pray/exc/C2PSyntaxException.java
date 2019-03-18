/*
 * Created on Sep 9, 2009
 * 	by the great Eclipse(c)
 */
package rebound.jagent.lib.caos2pray.exc;

/**
 * This designates a syntax error in the embedded C2P directives.
 * @author RProgrammer
 */
public class C2PSyntaxException
extends ScanningException
{
	private static final long serialVersionUID = 1L;
	
	public C2PSyntaxException()
	{
		super();
	}

	public C2PSyntaxException(String message, Throwable cause)
	{
		super(message, cause);
	}

	public C2PSyntaxException(String message)
	{
		super(message);
	}

	public C2PSyntaxException(Throwable cause)
	{
		super(cause);
	}

	@Override
	public String getDetailMessage()
	{
		return "CAOS2PRAY Syntax Error: "+getMessage();
	}
}
