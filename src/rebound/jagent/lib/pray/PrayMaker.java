/*
 * Created on Jan 13, 2006
 * 	by the wonderful Eclipse(c)
 */
package rebound.jagent.lib.pray;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import rebound.jagent.lib.pray.blocks.MetaBlockMaker;
import rebound.jagent.lib.pray.blocks.makers.InlineFileBlockMaker;
import rebound.jagent.lib.pray.blocks.makers.TagBlockMaker;
import rebound.jagent.lib.pray.template.Group;
import rebound.jagent.lib.pray.template.PrayTemplate;

public class PrayMaker
{
	protected InlineFileBlockMaker fileBlockMaker = new InlineFileBlockMaker();
	protected TagBlockMaker tagBlockMaker = new TagBlockMaker();
	
	protected OutputStream out;
	protected int overallIndex = 0;
	protected PrayMakerNotifee notifee;
	
	
	public PrayMaker()
	{
		super();
	}
	
	public PrayMaker(PrayMakerNotifee n)
	{
		this();
		notifee = n;
	}
	
	public void start() throws IOException
	{
		//Magic number
		out.write(PrayParser.PRAYMAGIC);
		
		overallIndex = 0;
	}
	
	public void writePrayTemplate(PrayTemplate template) throws IOException, InvalidNameException
	{
		//Tag blocks
		int n = template.getGroupCount();
		for (int i = 0; i < n; i++)
		{
			Group g = template.getGroup(i);
			tagBlockMaker.make(out, g);
			
			if (notifee != null) notifee.finWritingBlock(overallIndex);
			overallIndex++;
		}
		
		String[] FILEsrcs = template.getInlineSourcefiles();
		for (int i = 0; i < FILEsrcs.length; i++)
		{
			String source = FILEsrcs[i];
			
			File src = new File(template.getDir().getPath()+'/'+source);
			if (!src.exists())
				throw new FileNotFoundException("Inline File \""+source+"\" not found!");
			
			fileBlockMaker.make(out, src, template.getInlineFilePrayID(source), template.getInlineFilePrayName(source));
			
			if (notifee != null) notifee.finWritingBlock(overallIndex);
			overallIndex++;
		}
	}
	
	public void writeLowlevelBlocks(List<Block> blocks) throws IOException
	{
		for (Block b : blocks)
		{
			writeLowlevelBlock(b);
		}
	}
	
	public void writeLowlevelBlock(Block block) throws IOException
	{
		//Write header
		MetaBlockMaker.writeHeader(out, block.getHeader());
		
		//Write contents
		out.write(block.getData());
	}
	
	
	
	
	public static void makeSimply(List<Block> blocks, OutputStream out) throws IOException
	{
		//Magic number
		out.write(PrayParser.PRAYMAGIC);
		
		for (Block b : blocks)
		{
			//Write header
			MetaBlockMaker.writeHeader(out, b.getHeader());
			
			//Write contents
			out.write(b.getData());
		}
	}
	
	
	
	
	
	
	public void setOut(OutputStream out)
	{
		this.out = out;
	}
	
	public OutputStream getOut()
	{
		return this.out;
	}
	
	
	
	
	
	
	
	
	//Settings
	public boolean isMergeScripts()
	{
		return tagBlockMaker.isMergeScripts();
	}
	
	public void setMergeScripts(boolean value)
	{
		tagBlockMaker.setMergeScripts(value);
	}
}
