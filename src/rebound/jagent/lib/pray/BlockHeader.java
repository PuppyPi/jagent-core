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
	int lengthInFile, originalLength;
	boolean compressed;
	byte[] id;
	String name;
	String sId;
	
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
