/*
 * Created on Jan 13, 2006
 * 	by the wonderful Eclipse(c)
 */
package rebound.jagent.lib.pray.blocks;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import rebound.jagent.lib.FormatMismatchException;
import rebound.jagent.lib.pray.BlockHeader;
import rebound.jagent.lib.pray.blocks.parsers.FileBlockParser;
import rebound.jagent.lib.pray.blocks.parsers.TagBlockParser;
import rebound.jagent.lib.pray.template.PrayTemplate;

public class MetaBlockParser
{
	protected static ArrayList<BlockParser> bps = new ArrayList<BlockParser>();
	
	public static void registerBlockParser(BlockParser bp)
	{
		bps.add(0, bp);  //reverse order so that the Inline File block manager isn't in front of anything ever XD
	}
	
	public static void unregisterBlockParser(BlockParser bp)
	{
		bps.remove(bp);
	}
	
	
	static
	{
		//Load default parsers
		MetaBlockParser.registerBlockParser(new FileBlockParser());  //*Always* do this last as it accepts any block!!
		MetaBlockParser.registerBlockParser(new TagBlockParser());
	}
	
	
	public void parseBlock(BlockHeader b, InputStream data, PrayTemplate template) throws IOException, FormatMismatchException
	{
		for (BlockParser curr : bps)
		{
			if (curr.canHandle(b))
			{
				curr.parseBlock(b, data, template);
				return;
			}
		}
		
		throw new AssertionError("The file block maker should've gotten it! D:");
	}
}
