/*
 * Created on Sep 14, 2009
 * 	by the great Eclipse(c)
 */
package rebound.jagent.lib.caos2pray.exc;

import java.io.IOException;

/**
 * This signifies an IOException was encountered during the loading of some config file.
 * @author RProgrammer
 */
public class ConfigReadException
extends Exception
{
	private static final long serialVersionUID = 1L;
	
	public ConfigReadException(IOException exc)
	{
		super(exc);
	}
}
