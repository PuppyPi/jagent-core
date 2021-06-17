/*
 * Created on Jan 13, 2006
 * 	by the wonderful Eclipse(c)
 */
package rebound.jagent.lib.pray.blocks;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import rebound.bits.Bytes;
import rebound.jagent.lib.pray.BlockHeader;
import rebound.jagent.lib.pray.InvalidNameException;
import rebound.jagent.lib.pray.blocks.makers.InlineFileBlockMaker;
import rebound.jagent.lib.pray.blocks.makers.TagBlockMaker;
import rebound.jagent.lib.pray.template.Group;
import rebound.jagent.lib.pray.template.PrayTemplate;

public class MetaBlockMaker
{
	protected PrayTemplate prayTemplate = new PrayTemplate();
	protected InlineFileBlockMaker fileBlockMaker = new InlineFileBlockMaker();
	protected TagBlockMaker tagBlockMaker = new TagBlockMaker();
	
	
	public void writeTagBlock(OutputStream out, int index) throws IOException, InvalidNameException
	{
		Group g = prayTemplate.getGroup(index);
		tagBlockMaker.make(out, g);
	}
	
	public void writeInlineFile(OutputStream out, String source) throws IOException, InvalidNameException
	{
		File src = new File(prayTemplate.getDir().getPath()+'/'+source);
		if (!src.exists())
			throw new FileNotFoundException("Inline File \""+source+"\" not found!");
		
		fileBlockMaker.make(out, src, prayTemplate.getInlineFilePrayID(source), prayTemplate.getInlineFilePrayName(source));
	}
	
	
	//Accessors
	public int getCount()
	{
		return prayTemplate.getGroupCount()+prayTemplate.getInlineFilesCount();
	}
	
	public int getTagCount()
	{
		return prayTemplate.getGroupCount();
	}
	
	public String[] getInlineFilesSources()
	{
		return prayTemplate.getInlineSourcefiles();
	}
	
	public PrayTemplate getPrayTemplate()
	{
		return this.prayTemplate;
	}
	
	public void setPrayTemplate(PrayTemplate prayTemplateFile)
	{
		this.prayTemplate = prayTemplateFile;
	}
	
	
	
	
	//Utility method
	public static void writeHeader(OutputStream out, BlockHeader b) throws IOException
	{
		//ID
		out.write(b.getId());
		
		//Name
		byte[] name = b.getName().getBytes(StandardCharsets.UTF_8);
		byte[] fullname = new byte[128];
		System.arraycopy(name, 0, fullname, 0, Math.min(name.length, 127)); //There must be at least one terminating 0 for spec compatibility
		out.write(fullname);
		
		//PRAY Length
		Bytes.putLittleInt(out, b.getLengthInFile());
		
		//Uncompressed length
		Bytes.putLittleInt(out, b.getOriginalLength());
		
		int flags = b.isCompressed() ? 1 : 0;
		Bytes.putLittleInt(out, flags);
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
