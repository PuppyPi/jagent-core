package rebound.jagent.lib.pray;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import javax.annotation.Nullable;

public enum JagentRecognizedPrayChunkBlockIds
{
	FILE,
	AGNT,
	DSAG,
	LIVE,
	EGGS,
	DFAM,
	SFAM,
	CHUM,
	EXPC,
	DSEX,
	GLST,
	CREA,
	GENE,
	PHOT,
	
	DSGB,
	;
	
	
	
	
	private static final JagentRecognizedPrayChunkBlockIds[] vals = JagentRecognizedPrayChunkBlockIds.class.getEnumConstants();
	
	/**
	 * @return the id or null if not recognized!
	 */
	public static @Nullable JagentRecognizedPrayChunkBlockIds lookup(byte[] rawid)
	{
		for (JagentRecognizedPrayChunkBlockIds v : vals)
			if (Arrays.equals(rawid, v.toRawId()))
				return v;
		return null;
	}
	
	public byte[] toRawId()
	{
		return this.name().getBytes(StandardCharsets.UTF_8);  //UTF-8 not ASCII for forward-compatibility, although dubious I admit, with only four bytes ^^'
	}
}
