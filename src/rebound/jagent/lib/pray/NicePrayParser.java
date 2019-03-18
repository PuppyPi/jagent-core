/*
 * Created on May 14, 2006
 * 	by the wonderful Eclipse(c)
 */
package rebound.jagent.lib.pray;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import rebound.jagent.lib.FormatMismatchException;
import rebound.jagent.lib.pray.template.TemplateConstructor;

public class NicePrayParser
extends PrayParser
{
	public NicePrayParser()
	{
		super();
	}
	
	public NicePrayParser(String agent) throws FileNotFoundException
	{
		this();
		setAgentFile(agent);
	}
	
	public void writePraySource() throws IOException
	{
		String filename = originalFile;
		if (filename == null)
			filename = "praysource.txt";
		else
		{
			int extdot = filename.indexOf(".");
			if (extdot > 0)
				filename = filename.substring(0, extdot);
			filename += ".txt";
		}
		File psfile = new File(getDir(), filename);
		
		FileOutputStream out = new FileOutputStream(psfile);
		TemplateConstructor c = new TemplateConstructor();
		c.setOut(out);
		c.setTemplate(template);
		c.construct();
		out.close();
	}
	
	
	public void easyParse() throws IOException, FormatMismatchException
	{
		parse();
		in.close();
		
		writePraySource();
	}
	
	public void setAgentFile(File file) throws FileNotFoundException
	{
		super.setIn(file);
		super.setDir(file.getParentFile());
	}
	
	public void setAgentFile(String file) throws FileNotFoundException
	{
		setAgentFile(new File(file));
	}
}
