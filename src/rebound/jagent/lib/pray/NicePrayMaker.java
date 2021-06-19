/*
 * Created on May 14, 2006
 * 	by the wonderful Eclipse(c)
 */
package rebound.jagent.lib.pray;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import rebound.jagent.lib.pray.template.PrayTemplate;
import rebound.jagent.lib.pray.template.TemplateParser;

public class NicePrayMaker
extends PrayMaker
{
	public void niceWrite(File praySource, String destination) throws IOException, InvalidNameException
	{
		PrayTemplate template = null;
		{
			FileInputStream in = new FileInputStream(praySource);
			
			TemplateParser parser = new TemplateParser();
			parser.setIn(in);
			
			parser.parse();
			parser.setContext(praySource);
			
			in.close();
			
			template = parser.getTemplate();
		}
		
		FileOutputStream out = new FileOutputStream(destination);
		setOut(out);
		
		start();
		writePrayTemplate(template);
		
		out.close();
	}
	
	public void niceWrite(String src, String d) throws IOException, InvalidNameException
	{
		niceWrite(new File(src), d);
	}
	
	public void niceWrite(File praySource) throws IOException, InvalidNameException
	{
		//Name destination file
		String dest = praySource.getName();
		
		int extdot = dest.indexOf(".");
		if (extdot > 0)
			dest = dest.substring(0, extdot);
		dest += ".agents";
		
		niceWrite(praySource, dest);
	}
	
	public void niceWrite(String praySource) throws IOException, InvalidNameException
	{
		niceWrite(new File(praySource));
	}
}
