/*
 * Created on May 25, 2006
 * 	by the wonderful Eclipse(c)
 */
package rebound.jagent.lib.caos2pray;

import java.io.File;
import java.io.IOException;
import rebound.jagent.lib.caos2pray.exc.ConfigReadException;
import rebound.jagent.lib.caos2pray.exc.DependencyException;
import rebound.jagent.lib.caos2pray.exc.ScanningException;
import rebound.jagent.lib.caos2pray.exc.ScriptReadException;
import rebound.jagent.lib.caos2pray.scanner.C2PParser;
import rebound.jagent.lib.caos2pray.scanner.CosParser;
import rebound.jagent.lib.pray.template.PrayTemplate;

public class CAOS2PRAY
{
	//Todo make this stateless, not static
	
	private CAOS2PRAY() {}
	
	/**
	 * Convenience method wrapping a {@link TemplateConverter}
	 * @see TemplateConverter#convert()
	 */
	public static PrayTemplate convert(File cosfile) throws ScriptReadException, ScanningException, DependencyException, ConfigReadException
	{
		try
		{
			initialize(); //calling more than once doesn't hurt
		}
		catch (IOException exc)
		{
			throw new ConfigReadException(exc);
		}
		
		TemplateConverter conv = new TemplateConverter(cosfile, new CosParser(), new C2PParser());
		conv.convert();
		return conv.getTemplate();
	}
	
	public static void initialize() throws IOException
	{
		FriendlyTags.loadConfig();
	}
}
