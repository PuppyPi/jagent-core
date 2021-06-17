/*
 * Created on Jan 12, 2006
 * 	by the wonderful Eclipse(c)
 */
package rebound.jagent.lib.pray;

import static rebound.text.StringUtilities.*;
import java.nio.charset.StandardCharsets;


/**
 * This class contains the entire block header, but no data.
 * @author Sean
 */
public class BlockHeader
{
	//Todo make this a proper java enum XD
	
	public static final int
	
	ID_UNKNOWN = -1,
	
	//FILE-like blocks :>
	ID_FILE = 0,
	ID_PHOT = 13,
	
	//Tag blocks :>
	ID_AGNT = 1,
	ID_DSAG = 2,
	ID_LIVE = 3,
	ID_EGGS = 4,
	ID_DFAM = 5,
	ID_SFAM = 6,
	ID_CHUM = 7,
	ID_EXPC = 8,
	ID_DSEX = 9,
	ID_DSGB = 14,  //:D!
	
	//Other blocks! :>
	ID_GLST = 10,
	ID_CREA = 11,
	ID_GENE = 12
	;
	
	protected static final byte[][] ids = new byte[][]
	{
		new byte[]{0x46, 0x49, 0x4c, 0x45}, //FILE
		new byte[]{0x41, 0x47, 0x4e, 0x54}, //AGNT
		new byte[]{0x44, 0x53, 0x41, 0x47}, //DSAG
		new byte[]{0x4c, 0x49, 0x56, 0x45}, //LIVE
		new byte[]{0x45, 0x47, 0x47, 0x53}, //EGGS
		new byte[]{0x44, 0x46, 0x41, 0x4d}, //DFAM
		new byte[]{0x53, 0x46, 0x41, 0x4d}, //SFAM
		new byte[]{0x43, 0x48, 0x55, 0x4d}, //CHUM
		new byte[]{0x45, 0x58, 0x50, 0x43}, //EXPC
		new byte[]{0x44, 0x53, 0x45, 0x58}, //DSEX
		new byte[]{0x47, 0x4c, 0x53, 0x54}, //GLST
		new byte[]{0x43, 0x52, 0x45, 0x41}, //CREA
		new byte[]{0x47, 0x45, 0x4e, 0x45}, //GENE
		new byte[]{0x50, 0x48, 0x4f, 0x54}, //PHOT
		
		new byte[]{0x44, 0x53, 0x47, 0x42}, //DSGB
	};
	public static byte[] getIDCode(int id)
	{
		return ids[id];
	}
	
	int lengthInFile, originalLength;
	boolean compressed;
	byte[] id;
	String name;
	String sId;
	
	public int getID()
	{
		boolean allClean = false;
		for (int ID = 0; ID < ids.length; ID++)
		{
			allClean = true;
			for (int b = 0; b < 4 && allClean; b++)
				allClean &= id[b] == ids[ID][b];
			if (allClean)
				return ID;
		}
		return -1;
	}
	
	public byte[] getRawID()
	{
		return id;
	}
	
	public boolean isCompressed()
	{
		return this.compressed;
	}
	
	public byte[] getId()
	{
		return this.id;
	}
	
	public String getIdTextBestEffort()
	{
		if (sId == null)
			sId = decodeTextToStringReplacing(id, StandardCharsets.UTF_8);
		
		return sId;
	}
	
	public int getLengthInFile()
	{
		return this.lengthInFile;
	}
	
	public String getName()
	{
		return this.name;
	}
	
	public int getOriginalLength()
	{
		return this.originalLength;
	}
	
	
	
	
	
	
	public void setCompressed(boolean compressed)
	{
		this.compressed = compressed;
	}
	
	public void setId(byte[] id)
	{
		this.id = id;
		this.sId = null;
	}
	
	public void setLengthInFile(int lengthInFile)
	{
		this.lengthInFile = lengthInFile;
	}
	
	public void setName(String name)
	{
		this.name = name;
	}
	
	public void setOriginalLength(int originalLength)
	{
		this.originalLength = originalLength;
	}
}
