/*
 * Created on Jan 15, 2006
 * 	by the wonderful Eclipse(c)
 */
package rebound.jagent.lib.pray.blocks.makers;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import rebound.io.util.JRECompatIOUtilities;
import rebound.jagent.lib.PathBoss;
import rebound.jagent.lib.pray.BlockHeader;
import rebound.jagent.lib.pray.InvalidNameException;
import rebound.jagent.lib.pray.blocks.MetaBlockMaker;

public class InlineFileBlockMaker
{
	public void make(OutputStream out, File sourceFile, String prayID, String prayFilename) throws IOException, InvalidNameException
	{
		if (!PathBoss.getInstance().isNameCrossplatformFriendly(prayFilename))
			throw new InvalidNameException(prayFilename, "characters in the name are illegal on some platforms");
		
		BlockHeader header = new BlockHeader();
		header.setCompressed(false);
		header.setOriginalLength((int)sourceFile.length());
		header.setLengthInFile((int)sourceFile.length());
		header.setName(prayFilename);
		header.setId(prayID.getBytes(StandardCharsets.UTF_8));
		
		MetaBlockMaker.writeHeader(out, header);
		
		FileInputStream filein = new FileInputStream(sourceFile);
		JRECompatIOUtilities.pump(filein, out);
		filein.close();
	}
}
