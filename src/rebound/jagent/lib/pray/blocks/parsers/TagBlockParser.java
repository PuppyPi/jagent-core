/*
 * Created on May 14, 2006
 * 	by the wonderful Eclipse(c)
 */
package rebound.jagent.lib.pray.blocks.parsers;

import static rebound.text.StringUtilities.*;
import static rebound.util.objectutil.BasicObjectUtilities.*;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.StandardCharsets;
import rebound.bits.Bytes;
import rebound.io.util.JRECompatIOUtilities;
import rebound.jagent.lib.FormatMismatchException;
import rebound.jagent.lib.pray.BlockHeader;
import rebound.jagent.lib.pray.JagentRecognizedPrayChunkBlockIds;
import rebound.jagent.lib.pray.blocks.BlockParser;
import rebound.jagent.lib.pray.template.Group;
import rebound.jagent.lib.pray.template.PrayTemplate;
import rebound.text.StringUtilities;

public class TagBlockParser
implements BlockParser
{
	public void parseBlock(BlockHeader b, InputStream data, PrayTemplate template) throws IOException, FormatMismatchException
	{
		Group g = new Group();
		template.addGroup(g); //Let the template configure it a bit
		
		g.setID(decodeTextToStringReporting(b.getId(), StandardCharsets.UTF_8));
		g.setName(b.getName());
		
		int intTagCount = Bytes.getLittleInt(data);
		int ival = 0;
		String name = null;
		for (int i = 0; i < intTagCount; i++)
		{
			//Read int val
			name = readLString(data);
			ival = Bytes.getLittleInt(data);
			g.addIntTag(name, ival);
		}
		
		
		int strTagCount = Bytes.getLittleInt(data);
		String sval = null;
		boolean hasAtLeastOneScript = false;
		boolean hasOtherScripts = false; //scripts other than "Script 1", that is.
		for (int i = 0; i < strTagCount; i++)
		{
			//Read str val
			name = readLString(data);
			
			if (Group.isScriptTag(name))
			{
				hasAtLeastOneScript = true;
				int scriptNumber = Group.getScriptNumber(name);
				if (scriptNumber != 1)
					hasOtherScripts = true;
				
				int len = Bytes.getLittleInt(data);
				
				byte[] encoded = new byte[len];
				JRECompatIOUtilities.readFully(data, encoded);
				
				String script;
				try
				{
					script = StringUtilities.decodeTextToStringReporting(encoded, StandardCharsets.UTF_8);
				}
				catch (CharacterCodingException exc)
				{
					System.err.println("Warning: malformed or non-UTF8 input on tag " +repr(name));
					script = null;
					
					g.writeRawScriptToFile(encoded, scriptNumber);
				}
				
				if (script != null)
					g.writeScriptToFile(script, scriptNumber);
			}
			else
			{
				sval = readLString(data);
				g.addStringTag(name, sval);
				
				if (eq(name, "Remove script"))  //This is case sensitive in Creatures, so it is here too!!
				{
					g.writeRemoveScriptToFile(sval);
				}
			}
		}
		
		
		if (hasAtLeastOneScript && !hasOtherScripts)
		{
			//We can only do this after all scripts have been written.
			g.recognizeSingletonScript();
		}
	}
	
	protected String readLString(InputStream in) throws IOException, EOFException
	{
		int len = Bytes.getLittleInt(in);
		byte[] raw = new byte[len];
		JRECompatIOUtilities.readFully(in, raw);
		return universalNewlines(decodeTextToStringReporting(raw, StandardCharsets.UTF_8));
	}
	
	
	public boolean canHandle(BlockHeader b)
	{
		//Todo blacklist blocks instead of whitelisting??  (ie, what is the behavior of 'unknown' block id's; assume they're tag blocks or something else or assume nothing? :> )
		//TODO allow configurability by the user :>  (eg, from a text file placed next to the .jar or in their home folder for overrides :> )
		
		JagentRecognizedPrayChunkBlockIds id = JagentRecognizedPrayChunkBlockIds.lookup(b.getId());
		
		return
		id == JagentRecognizedPrayChunkBlockIds.AGNT ||
		id == JagentRecognizedPrayChunkBlockIds.DSAG ||
		id == JagentRecognizedPrayChunkBlockIds.LIVE ||
		id == JagentRecognizedPrayChunkBlockIds.EGGS ||
		id == JagentRecognizedPrayChunkBlockIds.SFAM ||
		id == JagentRecognizedPrayChunkBlockIds.DFAM ||
		id == JagentRecognizedPrayChunkBlockIds.EXPC ||
		id == JagentRecognizedPrayChunkBlockIds.DSEX ||
		
		id == JagentRecognizedPrayChunkBlockIds.DSGB;
		
		//Not CREA, PHOT, FILE, etc.
	}
}
