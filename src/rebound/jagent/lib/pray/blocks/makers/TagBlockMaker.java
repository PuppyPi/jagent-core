/*
 * Created on Jan 15, 2006
 * 	by the wonderful Eclipse(c)
 */
package rebound.jagent.lib.pray.blocks.makers;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import rebound.bits.Bytes;
import rebound.io.util.FSIOUtilities;
import rebound.jagent.lib.PathBoss;
import rebound.jagent.lib.pray.BlockHeader;
import rebound.jagent.lib.pray.InvalidNameException;
import rebound.jagent.lib.pray.blocks.MetaBlockMaker;
import rebound.jagent.lib.pray.template.Group;
import rebound.jagent.lib.pray.template.TagVal;

public class TagBlockMaker
{
	protected boolean mergeScripts = true; //default to historical behavior
	
	public void make(OutputStream out, Group g) throws IOException, InvalidNameException
	{
		if (!PathBoss.getInstance().isNameCrossplatformFriendly(g.getName()))
			throw new InvalidNameException(g.getName(), "characters in the name are illegal on some platforms");
		
		BlockHeader b = new BlockHeader();
		b.setId(g.getID().getBytes("ASCII"));
		b.setName(g.getName());
		
		
		//Now we (more robustly, if more memory-expensive) calculate length by writing it to memory, simply getting the length, writing the block header, then dumping the already-written block contents data
		/*
		//	<Calculate length
		int length = 0;
		
		//Int tags
		length += 4; //Int val count
		for (int i = 0; i < g.getIntValCount(); i++)
		{
			length += 4; //CSLen
			length += g.getIntTagName(i).getBytes("ASCII").length; //CSVal
			
			length += 4; //val
		}
		
		//Str tags
		length += 4; //Int val count
		for (int i = 0; i < g.getStrValCount(); i++)
		{
			length += 4; //name.CSLen
			length += g.getStrTagName(i).getBytes("ascii").length; //name.CSVal
			
			length += 4; //val.CSLen
			length += g.getStrTagValue(i).getBytes("ascii").length; //val.CSVal
		}
		
		//Script(s)
		if (g.hasScripts())
		{
			length += 4;
			length += "Script Count".getBytes("ascii").length;
			
			length += 4; //Val
			
			
			length += 4;
			length += "Script 1".getBytes("ascii").length;
			
			length += 4;
			length += g.getScriptsLength();
		}
		//	Calculate length>
		 */
		
		
		//Write data
		byte[] blockContents = null;
		{
			ByteArrayOutputStream buffer = new ByteArrayOutputStream();
			
			
			int scriptCount = isMergeScripts() ? (g.hasScripts() ? 1 : 0) : g.getScriptFiles().size();
			
			
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
				
				
				//Normal str tags
				for (int i = 0; i < g.getStrValCount(); i++)
				{
					//Str-tag name pstring
					writeLString(buffer, g.getStrTagName(i));
					
					//Str-tag value pstring
					writeLString(buffer, g.getStrTagValue(i));
				}
				
				
				//Script(s)
				if (g.hasScripts())
				{
					//The modern spec only supports one script entry, we must concatenate them ahead of time
					if (isMergeScripts())
					{
						//Str-tag name pstring
						writeLString(buffer, "Script 1");
						
						List<File> scriptFiles = g.getScriptFilesAsFiles();
						
						//Predict the length
						long predictedLength = 0;
						{
							for (int i = 0; i < scriptFiles.size(); i++)
							{
								File scriptFile = scriptFiles.get(i);
								long length = scriptFile.length();
								
								predictedLength += length;
								if (i < scriptFiles.size()-1)
									predictedLength += 1; //the added space
							}
						}
						
						//Write it all out
						{
							//Str-tag value pstring length integer
							Bytes.putLittleInt(buffer, (int)predictedLength); //SAME HERE! xD
							
							long actualTotalLength = 0;
							
							//Str-tag value pstring text
							for (int i = 0; i < scriptFiles.size(); i++)
							{
								File scriptFile = scriptFiles.get(i);
								long length = scriptFile.length();
								
								long actualLength = FSIOUtilities.dumpFileToOutputStream(scriptFile, buffer);
								actualTotalLength += actualLength;
								
								if (length != actualLength)
									throw new IOException("File was not apparent length (appeared to be "+length+" bytes, but was actually "+actualLength+" bytes): "+scriptFile.getAbsolutePath());
								
								if (i < scriptFiles.size()-1)
								{
									buffer.write(' ');
									actualTotalLength += 1;
								}
							}
							
							if (actualTotalLength != predictedLength)
								throw new IOException("Files were not apparent length (predicted to be "+predictedLength+" bytes, but was actually "+actualTotalLength+" bytes)");
						}
					}
					
					//No merge; leave as individual scripts (I think this is necessary for something like the Norn Garden, which has rscr's in each script file; it seems to work with all the scripts (my multi-script agents never did though :P ))
					else
					{
						List<File> scriptFiles = g.getScriptFilesAsFiles();
						for (int i = 0; i < scriptFiles.size(); i++)
						{
							File scriptFile = scriptFiles.get(i);
							long length = scriptFile.length();
							
							//Str-tag name pstring
							writeLString(buffer, "Script "+(i+1));
							
							//Str-tag value pstring
							Bytes.putLittleInt(buffer, (int)length); //MUST CAST TO INT (32 BITS)!!  WILL TAKE DAYS TO DEBUG IF OVERLOADING IS FORGOTTEN!  xD
							long actualLength = FSIOUtilities.dumpFileToOutputStream(scriptFile, buffer);
							
							if (length != actualLength)
								throw new IOException("File was not apparent length (appeared to be "+length+" bytes, but was actually "+actualLength+" bytes): "+scriptFile.getAbsolutePath());
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
	
	protected void writeTagVal(OutputStream out, TagVal val) throws IOException
	{
		if (val.isStringTagVal())
			writeLString(out, val.getStringValue());
		else
			Bytes.putLittleInt(out, val.getIntegerValue());
	}
	
	protected void writeLString(OutputStream out, String str) throws IOException
	{
		byte[] bstr = str.getBytes("ascii");
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
