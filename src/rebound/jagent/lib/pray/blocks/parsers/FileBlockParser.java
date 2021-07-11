/*
 * Created on May 14, 2006
 * 	by the wonderful Eclipse(c)
 */
package rebound.jagent.lib.pray.blocks.parsers;

import static rebound.text.StringUtilities.*;
import java.io.EOFException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import rebound.io.util.JRECompatIOUtilities;
import rebound.jagent.lib.PathBoss;
import rebound.jagent.lib.pray.BlockHeader;
import rebound.jagent.lib.pray.JagentRecognizedPrayChunkBlockIds;
import rebound.jagent.lib.pray.blocks.BlockParser;
import rebound.jagent.lib.pray.template.PrayTemplate;

public class FileBlockParser
implements BlockParser
{
	public void parseBlock(BlockHeader b, InputStream data, PrayTemplate template) throws IOException
	{
		//Get filename
		File file = null;
		{
			String preferredFilename = null;
			{
				preferredFilename = b.getName();
				if (JagentRecognizedPrayChunkBlockIds.lookup(b.getId()) == JagentRecognizedPrayChunkBlockIds.PHOT && preferredFilename.endsWith(".photo"))
					//replace ".photo" with ".s16"
					preferredFilename = preferredFilename.substring(0, preferredFilename.length()-6)+".s16";
				
				//Files with slashes in their name have problems to say the least. ;)
				preferredFilename = PathBoss.getInstance().getEscapedNameOnCurrentHostOS(preferredFilename);
			}
			
			file = new File(template.getDir(), preferredFilename);
			
			//If it already exists, rename the dest. file
			if (file.exists())
			{
				//Complain only the first time (since duplicate inlines are a violation of PRAY)
				System.err.println("Warning: Possible PRAY Violation (duplicate inline file): Inline file \""+b.getName()+"\" exists: "+file.getAbsolutePath());
				
				
				String pre = null, post = null; //the pre includes the parent directory's path
				{
					String d = file.getParent();
					String n = file.getName();
					int ep = n.lastIndexOf('.');
					
					if (ep == -1)
					{
						pre = file.getPath();
						post = "";
					}
					else
					{
						pre = new File(d, n.substring(0, ep)).getPath();
						post = n.substring(ep);
					}
				}
				
				File f = null;
				int i = 2;
				do
				{
					f = new File(pre+" ("+i+")"+post);
					i++;
					
					if (i >= 65536)
						throw new IOException("Seriously?! How many files do you have?   {"+b.getName()+","+file.getAbsolutePath()+"}");
				}
				while (f.exists());
				
				file = f;
			}
		}
		
		long fileLength = b.isCompressed() ? b.getOriginalLength() : b.getLengthInFile();
		FileOutputStream fileout = new FileOutputStream(file);
		
		long actual = JRECompatIOUtilities.pumpFixed(data, fileout, fileLength);
		
		if (actual < fileLength)
		{
			try
			{
				fileout.close();
			}
			catch (IOException exc)
			{
				System.err.println("IOException closing file:");
				exc.printStackTrace();
			}
			
			throw new EOFException("Premature EOF detected in "+b.getIdTextBestEffort()+" block \""+b.getName()+"\", correct length is specified to be "+fileLength+", actual length in file is "+actual+".    Most likely the file was truncated somehow (eg, incomplete download).");
		}
		else
		{
			fileout.close();
		}
		
		
		template.addInline(decodeTextToStringReporting(b.getId(), StandardCharsets.UTF_8), file.getName(), b.getName());
	}
	
	public boolean canHandle(BlockHeader b)
	{
		JagentRecognizedPrayChunkBlockIds id = JagentRecognizedPrayChunkBlockIds.lookup(b.getId());
		
		return
		id == JagentRecognizedPrayChunkBlockIds.FILE ||
		id == JagentRecognizedPrayChunkBlockIds.PHOT;
	}
}
