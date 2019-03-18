/*
 * Created on May 27, 2006
 * 	by the wonderful Eclipse(c)
 */
package rebound.jagent.lib.caos2pray.exc;

import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * This wraps an IOException thrown while scanning a .cos file.
 * {@link #isRemove()} tells if it was a remove script that was being scanned.
 * @author RProgrammer
 */
public class ScriptReadException
extends Exception
{
	private static final long serialVersionUID = 1L;
	
	protected boolean remove = false;
	
	public ScriptReadException(IOException cause, String rfile, boolean remove)
	{
		super(rfile, cause);
		this.remove = remove;
	}
	
	public ScriptReadException(IOException cause, String rfile)
	{
		this(cause, rfile, false);
	}
	
	
	public boolean isRemove()
	{
		return this.remove;
	}
	
	public String getScript()
	{
		return getMessage();
	}
	
	public IOException getIOExc()
	{
		return (IOException)getCause();
	}
	
	public boolean isFileNotFound()
	{
		return getCause() instanceof FileNotFoundException;
	}
}
