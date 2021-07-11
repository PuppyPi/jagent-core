/*
 * Created on Jan 15, 2006
 * 	by the wonderful Eclipse(c)
 */
package rebound.jagent.lib.pray.blocks.makers;

import static rebound.io.util.JRECompatIOUtilities.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import rebound.jagent.lib.PathBoss;
import rebound.jagent.lib.archive.CreaturesArchiveFormat;
import rebound.jagent.lib.pray.BlockHeader;
import rebound.jagent.lib.pray.InvalidNameException;
import rebound.jagent.lib.pray.JagentRecognizedPrayChunkBlockIds;
import rebound.jagent.lib.pray.blocks.MetaBlockMaker;
import rebound.jagent.lib.pray.blocks.parsers.FileBlockParser;

public class FileBlockMaker
{
	public static void make(OutputStream out, File sourceFile, byte[] prayID, String prayName, boolean archiveFormatInPray) throws IOException, InvalidNameException
	{
		if (FileBlockParser.isNormalFileTypeBlock(JagentRecognizedPrayChunkBlockIds.lookup(prayID)))
		{
			if (!PathBoss.getInstance().isNameCrossplatformFriendly(prayName))
				throw new InvalidNameException(prayName, "characters in the name are illegal on some platforms");
		}
		
		
		File tempArchiveFile = null;
		
		try
		{
			if (archiveFormatInPray)
			{
				tempArchiveFile = File.createTempFile(sourceFile.getName(), ".creaturesarchive");
				
				try (FileInputStream filein = new FileInputStream(sourceFile))
				{
					try (OutputStream fileout = new FileOutputStream(tempArchiveFile))
					{
						CreaturesArchiveFormat.compress(filein, fileout, 9);
					}
				}
				
				sourceFile = tempArchiveFile;
			}
			
			
			BlockHeader header = new BlockHeader();
			header.setCompressed(false);
			header.setOriginalLength((int)sourceFile.length());
			header.setLengthInFile((int)sourceFile.length());
			header.setName(prayName);
			header.setId(prayID);
			
			MetaBlockMaker.writeHeader(out, header);
			
			try (FileInputStream filein = new FileInputStream(sourceFile))
			{
				pump(filein, out);
			}
		}
		finally
		{
			if (tempArchiveFile != null)
				tempArchiveFile.delete();
		}
	}
}
