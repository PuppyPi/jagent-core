/*
 * Created on Jan 13, 2006
 * 	by the wonderful Eclipse(c)
 */
package rebound.jagent.lib.pray.template;

import static rebound.text.StringUtilities.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import javax.annotation.Nonnull;
import rebound.exceptions.TextSyntaxException;
import rebound.jagent.lib.Cursor;
import rebound.text.StringUtilities;
import rebound.util.Either;

//Todo revamp this X'D

public class TemplateParser
{
	protected Reader in;
	protected PrayTemplate template;
	
	
	private static final int
	TAG_NONE = 0,
	TAG_GROUP = 1,
	TAG_INLINE = 2;
	
	public void parse() throws IOException
	{
		Cursor c = new Cursor();
		template = new PrayTemplate();
		ArrayList<Group> groups = new ArrayList<Group>();
		
		discardLine(c);  //en-GB  (just discard it..I don't even know what this is for; it's not like the tag names are localized x'D )
		
		boolean lastWasCS = false;
		char curr = 0;
		int tagPos = 0, tagType = 0;
		Group currGroup = null;
		String k = null;
		Either<Integer, String> v = null;
		
		while (true)
		{
			//Read and check eof
			{
				int b = in.read();
				if (b == -1)
					break;
				else
					curr = (char)b;
			}
			
			
			c.advance();
			if (lastWasCS && curr == '-')
			{
				finishComment(c);
				lastWasCS = false;
			}
			else
			{
				lastWasCS = false;
				
				if (curr == '"')
				{
					k = readSpontaneousQuoted(c);
					v = readKnownTagVal(c);
					
					if (currGroup != null)
					{
						if (k.startsWith("Script ") && k.length() > "Script ".length() && !k.substring("Script ".length()).equalsIgnoreCase("Count"))
						{
							if (v.getValueIfB() != null)
								currGroup.addScript(v.getValueIfB());
						}
						else if (!k.equals("Script Count"))
							currGroup.addTag(k, v);
					}
				}
				else if (curr == 'g' || curr == 'G')
				{
					//Start group tag
					tagPos = 0;
					tagType = TAG_GROUP;
				}
				else if ((curr == 'i' || curr == 'I') && !(tagType == TAG_INLINE && tagPos == 2))
				{
					//inline
					//^
					tagPos = 0;
					tagType = TAG_INLINE;
				}
				else
				{
					if
					(
					tagType == TAG_GROUP &&
					(curr == 'r' || curr == 'R' && tagPos == 0) ||
					(curr == 'o' || curr == 'O' && tagPos == 1) ||
					(curr == 'u' || curr == 'U' && tagPos == 2) ||
					(curr == 'p' || curr == 'P' && tagPos == 3)
					)
					{
						tagPos++;
					}
					else if (tagType == TAG_GROUP && curr == ' ' && tagPos == 4)
					{
						//Finished with tag
						currGroup = new Group();
						groups.add(currGroup);
						readGroupTag(currGroup, c);
					}
					
					else if
					(
					tagType == TAG_INLINE &&
					(curr == 'n' || curr == 'N' && tagPos == 0) ||
					(curr == 'l' || curr == 'L' && tagPos == 1) ||
					(curr == 'i' || curr == 'I' && tagPos == 2) ||
					(curr == 'n' || curr == 'N' && tagPos == 3) ||
					(curr == 'e' || curr == 'E' && tagPos == 4)
					)
					{
						tagPos++;
					}
					else if (tagType == TAG_INLINE && curr == ' ' && tagPos == 5)
					{
						//Finished with tag
						readInlineTag(template, c);
					}
					
					else
					{
						//A tag broke
						tagType = TAG_NONE;
						tagPos = 0;
					}
				}
			}
		}
		
		template.groups = groups;
	}
	
	protected void discardLine(Cursor c) throws IOException
	{
		while (true)
		{
			int i = in.read();
			
			if (i == -1)
				return;
			
			char curr = (char)i;
			c.advance();
			
			if (curr == '\r' || curr == '\n')
				return;
		}
	}
	
	//	Data cursor must be after first (-
	protected void finishComment(Cursor c) throws IOException
	{
		char curr = 0;
		char[] currBuff = new char[1];
		boolean lastWasDash = false;
		while (in.read(currBuff) == 1)
		{
			curr = currBuff[0];
			c.advance();
			if (curr == '-')
				lastWasDash = true;
			else if (curr == ')')
				if (lastWasDash)
					return;
		}
	}
	
	//Data cursor must be before first " of string
	protected Either<Integer, String> readKnownTagVal(Cursor c) throws IOException
	{
		boolean stringVal = true;
		boolean inTheQuotes = false;
		boolean lastWasCS = false; //CommentStart '('
		boolean lastWasEscape = false;
		char curr = 0;
		char[] currBuff = new char[1];
		StringBuilder string = new StringBuilder();
		while (in.read(currBuff) == 1)
		{
			c.advance();
			curr = currBuff[0];
			if (stringVal && !inTheQuotes && lastWasCS && curr == '-')
			{
				finishComment(c);
				lastWasCS = false;
			}
			else
			{
				if (lastWasCS)
					lastWasCS = false;
				
				if (!stringVal && !(curr == '0' || curr == '1' || curr == '2' || curr == '3' || curr == '4' || curr == '5' || curr == '6' || curr == '7' || curr == '8' || curr == '9'))
				{
					//End of # val
					return Either.forA(Integer.parseInt(string.toString()));
				}
				else if (!inTheQuotes && curr == '"')
					inTheQuotes = true;
				else if (!inTheQuotes && (curr == '0' || curr == '1' || curr == '2' || curr == '3' || curr == '4' || curr == '5' || curr == '6' || curr == '7' || curr == '8' || curr == '9'))
				{
					stringVal = false;
					string.append(curr);
				}
				else if (inTheQuotes && lastWasEscape)
				{
					if (curr == 'n')
						string.append('\n');
					else if (curr == 'r')
						string.append('\r');
					else if (curr == 't')
						string.append('\t');
					else //takes care of \\ \" and exceptions
						string.append(curr);
					lastWasEscape = false;
				}
				else if (inTheQuotes) //&& !lastWasEscape
				{
					if (curr == '\\')
						lastWasEscape = true;
					else if (curr == '"')
						return Either.forB(string.toString());
					else
						string.append(curr);
				}
			}
		}
		
		return stringVal ? Either.forB(string.toString()) : Either.forA(Integer.parseInt(string.toString()));
	}
	
	
	
	//Data cursor must be before first " of string
	protected String readKnownQuoted(Cursor c) throws IOException
	{
		return readKnownQuotedOrWord(c, false).getValueIfA();
	}
	
	/**
	 * @return A means Quoted, B means unquoted
	 */
	protected @Nonnull Either<String, String> readKnownQuotedOrWord(Cursor c, boolean allowUnquotedToken) throws IOException
	{
		boolean inTheQuotes = false;
		boolean unquoted = false;
		boolean lastWasCS = false; //CommentStart '('
		boolean lastWasEscape = false;
		char curr = 0;
		StringBuilder string = new StringBuilder();
		
		while (true)
		{
			int i = in.read();
			
			if (i == -1)  //EOF
			{
				if (inTheQuotes)
					throw TextSyntaxException.inst("Premature EOF, expected ending double-quote!");
				else if (unquoted)
					return Either.forB(string.toString());
				else
					throw TextSyntaxException.inst("Premature EOF, expected "+(allowUnquotedToken ? "either an unquoted token or a double-quoted string" : "a double-quoted string")+"!");
			}
			
			curr = (char)i;
			c.advance();
			
			if (!inTheQuotes && lastWasCS && curr == '-')
			{
				finishComment(c);
				lastWasCS = false;
			}
			else
			{
				if (lastWasCS)
					lastWasCS = false;
				
				if (!inTheQuotes)
				{
					if (curr == '"')
						inTheQuotes = true;
					else if (!Character.isWhitespace(curr))
					{
						if (allowUnquotedToken)
						{
							unquoted = true;
							string.append(curr);
						}
						else
						{
							throw TextSyntaxException.inst("Unexpected double-quote was none was expected!: "+repr(curr));
						}
					}
				}
				else if (unquoted)
				{
					if (Character.isWhitespace(curr))
						return Either.forB(string.toString());
					else if (curr == '"')
						throw TextSyntaxException.inst("Unexpected character where double-quote was expected!: "+repr(curr));
					else
						string.append(curr);
				}
				else if (inTheQuotes && lastWasEscape)
				{
					if (curr == 'n')
						string.append('\n');
					else if (curr == 'r')
						string.append('\r');
					else if (curr == 't')
						string.append('\t');
					else //takes care of \\ \" and exceptions
						string.append(curr);
				}
				else if (inTheQuotes) //&& !lastWasEscape
				{
					if (curr == '\\')
						lastWasEscape = true;
					else if (curr == '"')
						return Either.forA(string.toString());
					else
						string.append(curr);
				}
			}
		}
	}
	
	
	
	protected @Nonnull String readUnquotedToken(Cursor c) throws IOException
	{
		char curr = 0;
		StringBuilder string = new StringBuilder();
		
		while (true)
		{
			int i = in.read();
			
			if (i == -1)  //EOF
				return string.toString();
			
			curr = (char)i;
			c.advance();
			
			if (Character.isWhitespace(curr))
				return string.toString();
			else if (curr == '"')
				throw TextSyntaxException.inst("Unexpected double-quote where none was expected!   (Text so far: "+repr(string.toString())+")");
			else
				string.append(curr);
		}
	}
	
	
	
	
	//Data cursor must be after first " of string
	protected String readSpontaneousQuoted(Cursor c) throws IOException
	{
		boolean lastWasEscape = false;
		char curr = 0;
		StringBuilder string = new StringBuilder();
		char[] currBuff = new char[1];
		while (in.read(currBuff) == 1)
		{
			curr = currBuff[0];
			c.advance();
			
			if (lastWasEscape)
			{
				if (curr == 'n')
					string.append('\n');
				else if (curr == 'r')
					string.append('\r');
				else if (curr == 't')
					string.append('\t');
				else //takes care of \\ \" and exceptions
					string.append(curr);
			}
			else//if (!lastWasEscape)
			{
				if (curr == '\\')
					lastWasEscape = true;
				else if (curr == '"')
					return string.toString();
				else
					string.append(curr);
			}
		}
		return string.toString();
	}
	
	
	//Data cursor must be after 'group ' and thus on first char in ID
	protected void readGroupTag(Group g, Cursor c) throws IOException
	{
		//Read ID
		char[] id = new char[4];
		in.read(id);
		c.advance(4);
		g.ID = new String(id);
		
		g.name = readKnownQuoted(c);
	}
	
	//Data cursor must be after 'inline ' and thus before the 'F' in 'FILE'
	protected void readInlineTag(PrayTemplate template, Cursor c) throws IOException
	{
		boolean archiveFormatInPray;
		String id;
		{
			String f = readUnquotedToken(c);
			
			if (f.equalsIgnoreCase("archive"))
			{
				archiveFormatInPray = true;
				id = readUnquotedToken(c);
			}
			else
			{
				archiveFormatInPray = false;
				id = f;
			}
		}
		
		byte[] idBytes = StringUtilities.encodeTextToByteArrayReportingUnchecked(id, StandardCharsets.ISO_8859_1);
		
		if (idBytes.length != 4)
			throw TextSyntaxException.inst("PRAY Chunk block type id's (eg, FILE, PHOT, GENE, CREA, etc.) must always be four bytes!");
		
		String realFilename = readKnownQuoted(c);
		String prayFilename = readKnownQuoted(c);
		
		template.getFileBlocks().add(new FileBlockInPrayTemplate(realFilename, idBytes, prayFilename, archiveFormatInPray));
	}
	
	
	
	/**
	 * This sets the various context-variables stored in a {@link PrayTemplate} for you, given the original pray.txt file.
	 * @param prayfile The .txt file, NOT the directory containing it
	 */
	public void setContext(File prayfile)
	{
		template.setDir(prayfile.getAbsoluteFile().getParentFile());
	}
	
	
	
	
	
	public PrayTemplate getTemplate()
	{
		return this.template;
	}
	
	public Reader getIn()
	{
		return this.in;
	}
	
	public void setIn(Reader in)
	{
		this.in = in;
	}
	
	public void setIn(InputStream in)
	{
		CharsetDecoder decoder = StandardCharsets.UTF_8.newDecoder();
		decoder.onUnmappableCharacter(CodingErrorAction.REPORT);
		decoder.onMalformedInput(CodingErrorAction.REPORT);
		
		this.setIn(new InputStreamReader(in, decoder));
	}
}
