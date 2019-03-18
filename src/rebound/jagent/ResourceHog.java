/*
 * Created on Aug 15, 2009
 * 	by the great Eclipse(c)
 */
package rebound.jagent;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * This gets a resource wether its in the .jar file, or loose in my debug environment.
 * @author RProgrammer
 */
public class ResourceHog
{
	public static URL getResource(String name)
	{
		URL u = ResourceHog.class.getResource("/"+name);
		if (u != null)
			return u;
		
		u = ResourceHog.class.getResource(name);
		if (u != null)
			return u;
		
		File f = new File(name).getAbsoluteFile();
		if (f.exists())
		{
			try
			{
				return f.toURI().toURL();
			}
			catch (MalformedURLException exc)
			{
				System.err.println("ResourceHog:");
				exc.printStackTrace();
				return null;
			}
		}
		
		return null;
	}
	
	
	public static InputStream getResourceAsStream(String name) throws IOException
	{
		URL u = getResource(name);
		if (u == null)
			return null;
		else
			return u.openStream();
	}
}
