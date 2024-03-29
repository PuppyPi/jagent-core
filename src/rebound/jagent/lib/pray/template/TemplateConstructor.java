/*
 * Created on May 12, 2006
 * 	by the wonderful Eclipse(c)
 */
package rebound.jagent.lib.pray.template;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import rebound.text.StringUtilities;

public class TemplateConstructor
{
	protected Writer out;
	protected PrayTemplate template;
	
	public void construct() throws IOException
	{
		//Language?
		out.write("\"en-GB\"\n\n\n");
		
		//Write groups
		for (int i = 0; i < template.getGroupCount(); i++)
			writeGroup(template.getGroup(i));
		
		//Write inlines
		writeInlines();
		
		out.write("\n");
		
		out.flush();
	}
	
	
	
	protected void writeGroup(Group g) throws IOException
	{
		//group .... "<name>"
		out.write("group ");
		writeEscaped(g.ID);
		out.write(" \"");
		writeEscaped(g.name);
		out.write("\"\n\n");
		
		//sTags
		for (int i = 0; i < g.getStrValCount(); i++)
		{
			out.write("\"");
			writeEscaped(g.getStrTagName(i));
			out.write("\" \"");
			writeEscaped(g.getStrTagValue(i));
			out.write("\"\n");
		}
		out.write("\n");
		
		//Scripts
		if (g.hasScripts())
		{
			//I just put a pointer in (@ script1.cos), the block parser already dumped the script into a cos file
			
			out.write("\"Script Count\" ");
			out.write(Integer.toString(g.getScriptFileNames().size()));
			out.write("\n");
			
			for (int i = 0; i < g.getScriptFileNames().size(); i++)
			{
				out.write("\"Script ");
				out.write(Integer.toString(i+1));
				out.write("\" @ \"");
				writeEscaped(g.getScriptFileNames().get(i));
				out.write("\"\n");
			}
			
			out.write("\n");
		}
		
		//iTags
		for (int i = 0; i < g.getIntValCount(); i++)
		{
			if (!g.getIntTagName(i).equalsIgnoreCase("Script Count"))
			{
				out.write("\"");
				writeEscaped(g.getIntTagName(i));
				out.write("\" ");
				out.write(Integer.toString(g.getIntTagValue(i)));
				out.write("\n");
			}
		}
		out.write("\n\n\n");
	}
	
	
	protected void writeInlines() throws IOException
	{
		for (FileBlockInPrayTemplate block : template.getFileBlocks())
		{
			String id = StringUtilities.decodeTextToStringReportingUnchecked(block.getId(), StandardCharsets.ISO_8859_1);
			
			out.write("inline ");
			
			if (block.isArchiveFormatInPray())
				out.write("archive ");
			
			out.write(id);
			out.write(" \"");
			writeEscaped(block.getSourceFileName());
			out.write("\" \"");
			writeEscaped(block.getName());
			out.write("\"\n");
		}
	}
	
	
	
	
	protected void writeEscaped(String val) throws IOException
	{
		char c = 0;
		for (int i = 0; i < val.length(); i++)
		{
			c = val.charAt(i);
			if (c == '\n')
				out.write("\\n");
			else
			{
				if (c == '\"')
					out.write('\\');
				out.write(c);
			}
		}
	}
	
	
	
	
	
	public void setOut(Writer out)
	{
		this.out = out;
	}
	
	public void setOut(OutputStream bout)
	{
		CharsetEncoder encoder = StandardCharsets.UTF_8.newEncoder();
		encoder.onUnmappableCharacter(CodingErrorAction.REPORT);
		encoder.onMalformedInput(CodingErrorAction.REPORT);
		
		this.out = new OutputStreamWriter(bout, encoder);
	}
	
	public void setTemplate(PrayTemplate template)
	{
		this.template = template;
	}
	
	public Writer getOut()
	{
		return this.out;
	}
	
	public PrayTemplate getTemplate()
	{
		return this.template;
	}
}
