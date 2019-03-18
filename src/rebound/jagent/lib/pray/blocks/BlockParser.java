/*
 * Created on Jan 13, 2006
 * 	by the wonderful Eclipse(c)
 */
package rebound.jagent.lib.pray.blocks;

import java.io.IOException;
import java.io.InputStream;
import rebound.jagent.lib.FormatMismatchException;
import rebound.jagent.lib.pray.BlockHeader;
import rebound.jagent.lib.pray.template.PrayTemplate;

public interface BlockParser
{
	/**
	 * The general contract is this:<br>
	 * The Block b, is the header info.<br>
	 * The input stream is the block data.<br>
	 * The PrayTemplate will be the pray source.<br>
	 * Any files you create should be in <code>template.getDir()</code><br>
	 * 
	 * @param b - The block header information
	 * @param data - The block data
	 * @param template - The place to store information about the block, and also the directory to make data files
	 */
	public void parseBlock(BlockHeader b, InputStream data, PrayTemplate template) throws IOException, FormatMismatchException;
	
	public boolean canHandle(BlockHeader b);
}
