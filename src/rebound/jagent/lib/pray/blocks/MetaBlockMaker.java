/*
 * Created on Jan 13, 2006
 * 	by the wonderful Eclipse(c)
 */
package rebound.jagent.lib.pray.blocks;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import rebound.bits.Bytes;
import rebound.jagent.lib.pray.BlockHeader;

public class MetaBlockMaker
{
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
}
