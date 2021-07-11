/*
 * Created on May 14, 2006
 * 	by the wonderful Eclipse(c)
 */
package rebound.jagent.lib.pray.blocks.parsers;

import java.io.EOFException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;
import java.util.Arrays;
import javax.annotation.Nullable;
import rebound.io.util.JRECompatIOUtilities;
import rebound.jagent.lib.PathBoss;
import rebound.jagent.lib.archive.CreaturesArchiveFormat;
import rebound.jagent.lib.pray.BlockHeader;
import rebound.jagent.lib.pray.JagentRecognizedPrayChunkBlockIds;
import rebound.jagent.lib.pray.blocks.BlockParser;
import rebound.jagent.lib.pray.template.FileBlockInPrayTemplate;
import rebound.jagent.lib.pray.template.PrayTemplate;

public class FileBlockParser
implements BlockParser
{
	public void parseBlock(BlockHeader b, InputStream data, PrayTemplate template) throws IOException
	{
		//Is archive format?
		boolean archive;
		{
			byte[] archiveMagic = CreaturesArchiveFormat.HEADER;
			PushbackInputStream s = new PushbackInputStream(data, archiveMagic.length);
			
			byte[] c = JRECompatIOUtilities.readAsMuchAsPossibleToNew(s, archiveMagic.length);
			
			archive = Arrays.equals(c, archiveMagic);
			
			s.unread(c);
			
			data = s;
		}
		
		
		
		//Get filename
		File file = null;
		{
			String preferredFilename = null;
			{
				preferredFilename = b.getName();
				
				JagentRecognizedPrayChunkBlockIds id = JagentRecognizedPrayChunkBlockIds.lookup(b.getId());
				
				//replace ".photo" with ".s16"
				if (id == JagentRecognizedPrayChunkBlockIds.PHOT && preferredFilename.endsWith(".photo"))
					preferredFilename = preferredFilename.substring(0, preferredFilename.length()-".photo".length())+".s16";
				
				//replace ".genetics" with ".gen"
				if (id == JagentRecognizedPrayChunkBlockIds.GENE && preferredFilename.endsWith(".genetics"))
					preferredFilename = preferredFilename.substring(0, preferredFilename.length()-".genetics".length())+".gen";
				
				//replace ".creature" with ".crea"  (since .creature already means a PRAY chunk file!! X'D )
				if (id == JagentRecognizedPrayChunkBlockIds.CREA && preferredFilename.endsWith(".creature"))
					preferredFilename = preferredFilename.substring(0, preferredFilename.length()-".creature".length())+".crea";
				
				//replace ".glist.creature" with ".glist"
				if (id == JagentRecognizedPrayChunkBlockIds.GLST && preferredFilename.endsWith(".glist.creature"))
					preferredFilename = preferredFilename.substring(0, preferredFilename.length()-".glist.creature".length())+".glist";
				
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
		
		try (FileOutputStream fileout = new FileOutputStream(file))
		{
			if (archive)
			{
				CreaturesArchiveFormat.uncompress(data, fileout);
			}
			else
			{
				long actual = JRECompatIOUtilities.pumpFixed(data, fileout, fileLength);
				
				if (actual < fileLength)
					throw new EOFException("Premature EOF detected in "+b.getIdTextBestEffort()+" block \""+b.getName()+"\", correct length is specified to be "+fileLength+", actual length in file is "+actual+".    Most likely the file was truncated somehow (eg, incomplete download).");
			}
		}
		
		
		template.getFileBlocks().add(new FileBlockInPrayTemplate(file.getName(), b.getId(), b.getName(), archive));
	}
	
	
	
	public boolean canHandle(BlockHeader b)
	{
		//		JagentRecognizedPrayChunkBlockIds id = JagentRecognizedPrayChunkBlockIds.lookup(b.getId());
		//		
		//		return
		//		id == JagentRecognizedPrayChunkBlockIds.FILE ||
		//		id == JagentRecognizedPrayChunkBlockIds.GENE ||
		//		id == JagentRecognizedPrayChunkBlockIds.PHOT;
		
		// .....We can just use this for raw blocks!! XDD'
		return true;
	}
	
	
	
	public static boolean isNormalFileTypeBlock(@Nullable JagentRecognizedPrayChunkBlockIds id)
	{
		return
		id == JagentRecognizedPrayChunkBlockIds.FILE ||
		id == JagentRecognizedPrayChunkBlockIds.GENE ||
		id == JagentRecognizedPrayChunkBlockIds.PHOT;
	}
}
