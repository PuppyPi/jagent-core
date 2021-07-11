/*
 * Created on Jan 15, 2006
 * 	by the wonderful Eclipse(c)
 */
package rebound.jagent.lib.pray.blocks.makers;

import static rebound.text.StringUtilities.*;
import static rebound.util.collections.CollectionUtilities.*;
import static rebound.util.objectutil.BasicObjectUtilities.*;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import rebound.bits.Bytes;
import rebound.io.util.FSIOUtilities;
import rebound.jagent.lib.pray.BlockHeader;
import rebound.jagent.lib.pray.CaosUtilitiesForJagent;
import rebound.jagent.lib.pray.CaosUtilitiesForJagent.CaosAndRemoveScript;
import rebound.jagent.lib.pray.InvalidNameException;
import rebound.jagent.lib.pray.blocks.MetaBlockMaker;
import rebound.jagent.lib.pray.template.Group;
import rebound.text.StringUtilities;

public class TagBlockMaker
{
	protected boolean mergeScripts = true;  //default to historical behavior
	
	public void make(OutputStream out, Group g) throws IOException, InvalidNameException
	{
		//Hot take: Non-Jagent tools running on Windows should just deal with this like Jagent does (and escape illegal characters), because agents with names that include "<C3>" and "<DS>" have been around for decades xD'
		//		if (!PathBoss.getInstance().isNameCrossplatformFriendly(g.getName()))
		//			throw new InvalidNameException(g.getName(), "characters in the name are illegal on some platforms");
		
		BlockHeader b = new BlockHeader();
		b.setId(StringUtilities.encodeTextToByteArrayReportingUnchecked(g.getID(), StandardCharsets.ISO_8859_1));
		b.setName(universalNewlines(g.getName()));
		
		
		//Now we (more robustly, if more memory-expensive) calculate length by writing it to memory, simply getting the length, writing the block header, then dumping the already-written block contents data
		/*
		//	<Calculate length
		int length = 0;
		
		//Int tags
		length += 4; //Int val count
		for (int i = 0; i < g.getIntValCount(); i++)
		{
			length += 4; //CSLen
			length += g.getIntTagName(i).getBytes(StandardCharsets.ISO_8859_1).length; //CSVal
			
			length += 4; //val
		}
		
		//Str tags
		length += 4; //Int val count
		for (int i = 0; i < g.getStrValCount(); i++)
		{
			length += 4; //name.CSLen
			length += g.getStrTagName(i).getBytes(StandardCharsets.ISO_8859_1).length; //name.CSVal
			
			length += 4; //val.CSLen
			length += g.getStrTagValue(i).getBytes(StandardCharsets.ISO_8859_1).length; //val.CSVal
		}
		
		//Script(s)
		if (g.hasScripts())
		{
			length += 4;
			length += "Script Count".getBytes(StandardCharsets.ISO_8859_1).length;
			
			length += 4; //Val
			
			
			length += 4;
			length += "Script 1".getBytes(StandardCharsets.ISO_8859_1).length;
			
			length += 4;
			length += g.getScriptsLength();
		}
		//	Calculate length>
		 */
		
		
		//Write data
		byte[] blockContents = null;
		{
			ByteArrayOutputStream buffer = new ByteArrayOutputStream();
			
			
			int scriptCount = isMergeScripts() ? (g.hasScripts() ? 1 : 0) : g.getScriptFileNames().size();
			
			
			//Int tags
			{
				//Number of int tags
				Bytes.putLittleInt(buffer, g.getIntValCount() + (g.hasScripts() ? 1 : 0)); //Tag count; the +1 is for the "Script Count" int-tag
				
				//Normal int tags
				for (int i = 0; i < g.getIntValCount(); i++)
				{
					//Int-tag name pstring
					writeLString(buffer, g.getIntTagName(i));
					
					//Int-tag value integer
					Bytes.putLittleInt(buffer, g.getIntTagValue(i));
				}
				
				//Script Count
				if (g.hasScripts())
				{
					//Int-tag name pstring
					writeLString(buffer, "Script Count");
					
					//Int-tag value integer
					Bytes.putLittleInt(buffer, scriptCount);
				}
			}
			
			
			
			
			//Str tags
			{
				//Number of str tags
				Bytes.putLittleInt(buffer, g.getStrValCount()+scriptCount); //Tag count
				
				
				boolean mergingScripts = g.hasScripts() && isMergeScripts();
				
				Integer removeScriptTagIndex = null;
				
				//Normal str tags
				for (int i = 0; i < g.getStrValCount(); i++)
				{
					String n = g.getStrTagName(i);
					
					if (eq(n, "Remove script") && mergingScripts)
					{
						//Let it be handled below X3
						removeScriptTagIndex = i;
					}
					else
					{
						//Str-tag name pstring
						writeLString(buffer, n);
						
						//Str-tag value pstring
						writeLString(buffer, g.getStrTagValue(i));
					}
				}
				
				
				final File removeScriptFile = g.getRemoveScriptFile();
				final String removeScriptFromFile = removeScriptFile == null ? null : StringUtilities.decodeTextToStringReportingUnchecked(FSIOUtilities.readAll(removeScriptFile), StandardCharsets.UTF_8);
				
				if (removeScriptFromFile != null)
				{
					if (mergingScripts)
					{
						//Let it be handled below X3
					}
					else
					{
						//Whoever made the Group g should make sure not to add both remove script sources when not merging scripts otherwise there will be two "Remove script" tags!
						
						//Str-tag name pstring
						writeLString(buffer, "Remove script");
						
						//Str-tag value pstring
						writeLString(buffer, removeScriptFromFile);
					}
				}
				
				
				//Script(s)
				if (g.hasScripts())
				{
					//The modern spec only supports one script entry, we must concatenate them ahead of time
					if (isMergeScripts())
					{
						final List<File> scriptFiles = g.getScriptFiles();
						
						final List<String> scripts;
						{
							scripts = new ArrayList<>();
							for (File f : scriptFiles)
								scripts.add(StringUtilities.decodeTextToStringReportingUnchecked(FSIOUtilities.readAll(f), StandardCharsets.UTF_8));
						}
						
						final String wholeScript;
						final String completeRemoveScript;
						{
							List<String> wholes = new ArrayList<>();
							List<String> removes = new ArrayList<>();
							
							if (removeScriptTagIndex != null)
								removes.add(g.getStrTagValue(removeScriptTagIndex));
							
							removes.add(removeScriptFromFile);
							
							List<String> removeScriptsInGivenOrder = new ArrayList<>(scripts.size());
							
							boolean first = true;
							for (String script : scripts)
							{
								CaosAndRemoveScript r = CaosUtilitiesForJagent.parse(script);
								
								if (!isAllWhitespace(r.otherCaos))
									wholes.add(r.otherCaos);
								
								if (!isAllWhitespace(r.removeScript))
								{
									if (!first || !g.getCutOutRemoveScriptFromFirstScript())
										removeScriptsInGivenOrder.add(r.removeScript);
								}
								
								first = false;
							}
							
							removes.addAll(reversed(removeScriptsInGivenOrder));
							
							wholeScript = CaosUtilitiesForJagent.mergeCaosen(wholes);
							completeRemoveScript = CaosUtilitiesForJagent.mergeCaosen(removes);
						}
						
						
						
						//Main script!
						{
							//Str-tag name pstring
							writeLString(buffer, "Script 1");
							
							//Str-tag value pstring
							writeLString(buffer, wholeScript);
						}
						
						//Remove script!
						{
							//Str-tag name pstring
							writeLString(buffer, "Remove script");
							
							//Str-tag value pstring
							writeLString(buffer, completeRemoveScript);
						}
					}
					
					//No merge; leave as individual scripts (I think this is necessary for something like the Norn Garden, which has rscr's in each script file; it seems to work with all the scripts (my multi-script agents never did though :P ))
					else
					{
						List<File> scriptFiles = g.getScriptFiles();
						for (int i = 0; i < scriptFiles.size(); i++)
						{
							File scriptFile = scriptFiles.get(i);
							
							String script = decodeTextToStringReportingUnchecked(FSIOUtilities.readAll(scriptFile), StandardCharsets.UTF_8);
							
							if (g.getCutOutRemoveScriptFromFirstScript() && i == 0)
							{
								script = CaosUtilitiesForJagent.parse(script).otherCaos;
							}
							
							
							//Str-tag name pstring
							writeLString(buffer, "Script "+(i+1));
							
							//Str-tag value pstring
							writeLString(buffer, script);
						}
					}
				}
			}
			
			
			blockContents = buffer.toByteArray();
			buffer = null;
		}
		
		
		int blockContentsLength = blockContents.length;
		
		b.setLengthInFile(blockContentsLength);
		b.setCompressed(false);
		b.setOriginalLength(blockContentsLength);
		
		//Write header
		MetaBlockMaker.writeHeader(out, b);
		
		//Write contents
		out.write(blockContents);
	}
	
	protected void writeLString(OutputStream out, String str) throws IOException
	{
		byte[] bstr = StringUtilities.encodeTextToByteArrayReportingUnchecked(universalNewlines(str), StandardCharsets.ISO_8859_1);
		Bytes.putLittleInt(out, bstr.length);
		out.write(bstr);
	}
	
	
	
	public boolean isMergeScripts()
	{
		return this.mergeScripts;
	}
	
	public void setMergeScripts(boolean mergeScripts)
	{
		this.mergeScripts = mergeScripts;
	}
}
