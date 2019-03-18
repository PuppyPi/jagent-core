/*
 * Created on Jan 13, 2006
 * 	by the wonderful Eclipse(c)
 */
package rebound.jagent.lib.pray;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import rebound.jagent.lib.pray.blocks.MetaBlockMaker;
import rebound.jagent.lib.pray.template.PrayTemplate;

public class PrayMaker
{
	protected OutputStream out;
	protected MetaBlockMaker baker;
	protected PrayMakerNotifee notifee;
	
	public PrayMaker()
	{
		super();
		baker = new MetaBlockMaker();
	}
	
	public PrayMaker(PrayMakerNotifee n)
	{
		this();
		notifee = n;
	}
	
	public void make() throws IOException, InvalidNameException
	{
		if (notifee != null) notifee.startWritingPray();
		
		//Magic number
		out.write(PrayParser.PRAYMAGIC);
		
		int index = 0;
		
		for (int i = 0; i < baker.getTagCount(); i++)
		{
			baker.writeTagBlock(out, i);
			if (notifee != null) notifee.finWritingBlock(index);
			index++;
		}
		
		String[] FILEsrcs = baker.getInlineFilesSources();
		for (int i = 0; i < FILEsrcs.length; i++)
		{
			baker.writeInlineFile(out, FILEsrcs[i]);
			if (notifee != null) notifee.finWritingBlock(index);
			index++;
		}
		
		out.close(); //Todo this really should be flushed and closed by the caller
		
		if (notifee != null) notifee.finWritingPray();
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
	
	
	
	public int getBlockCount()
	{
		return baker.getCount();
	}
	
	public void setPrayTemplate(PrayTemplate prayTemplate)
	{
		baker.setPrayTemplate(prayTemplate);
	}
	
	public void setDir(File dir)
	{
		baker.getPrayTemplate().setDir(dir);
	}
	
	public File getDir()
	{
		return baker.getPrayTemplate().getDir();
	}
	
	
	
	
	
	
	
	//Settings
	public boolean isMergeScripts()
	{
		return baker.isMergeScripts();
	}
	
	public void setMergeScripts(boolean value)
	{
		baker.setMergeScripts(value);
	}
}
