package rebound.jagent.lib.pray;

import static rebound.text.StringUtilities.*;
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
		return encodeTextToByteArrayReportingUnchecked(this.name(), StandardCharsets.ISO_8859_1);
	}
}
