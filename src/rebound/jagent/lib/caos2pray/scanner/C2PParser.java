/*
 * Created on May 25, 2006
 * 	by the wonderful Eclipse(c)
 */
package rebound.jagent.lib.caos2pray.scanner;

import java.io.IOException;
import java.util.ArrayList;
import rebound.jagent.lib.caos2pray.TemplateConverter;
import rebound.jagent.lib.caos2pray.exc.C2PSyntaxException;
import rebound.jagent.lib.caos2pray.exc.NotIntegerTagException;
import rebound.jagent.lib.caos2pray.scanner.CosParser.Token;
import rebound.jagent.lib.caos2pray.scanner.CosParser.TokenType;

/**
 * This pulls low-level tokens from a {@link CosParser} and extracts C2P-specific meaning from them.
 * It does not interpret the directives, but merely records them in a {@link C2PCosFile}.
 * <br>
 * This is a reusable component, via the {@link #init(CosParser)} method.
 * <br>
 * Note: This does not set the {@link C2PCosFile#getThisFile()}
 * @author RProgrammer
 */
public class C2PParser
{
	public static final char DIRECTIVE_INITIATOR = '#';
	
	protected CosParser parser;
	protected C2PCosFile c2p;
	
	public C2PParser()
	{
		super();
	}
	
	public C2PParser(CosParser p)
	{
		this();
		init(p);
	}
	
	
	/**
	 * [re-]initialized this parser with the given input source.
	 */
	public void init(CosParser rawparser)
	{
		this.parser = rawparser;
		this.c2p = new C2PCosFile();
	}
	
	
	
	//<Scanning
	/**
	 * This scans the entire cos file and packages the c2p-specific data in a {@link C2PCosFile} object.
	 */
	public void scan() throws IOException, NotIntegerTagException, C2PSyntaxException
	{
		while (true)
		{
			Token t = parser.next();
			
			if (t == null)
				break;
			
			else if (t.getType() == TokenType.COMMENT)
			{
				String val = t.getText(); //this doesn't have the '*'
				if (val.length() >= 1)
				{
					if (val.charAt(0) == DIRECTIVE_INITIATOR)
						handleDirective(val.substring(1)); //Minus the initiator
				}
			}
			
			
			else if (t.getType() == TokenType.CAOS)
			{
				//Check for rscr
				if (t.getText().toLowerCase().contains("rscr"))
				{
					//The rest of the file is a remove script, no further C2P directives are parsed
					
					String removeScriptPart = null;
					{
						int rscrPos = t.getText().toLowerCase().indexOf("rscr");
						removeScriptPart = t.getText().substring(rscrPos+4);
					}
					
					StringBuilder rscr = new StringBuilder();
					Token first = new Token(TokenType.CAOS, removeScriptPart);
					
					//Populate
					{
						Token n = first;
						
						while (true)
						{
							if (n == null)
								break;
							else if (n.getType() != TokenType.COMMENT) //There's no reason to include comments in a remove script; they will already be in the main script
							{
								if (n.getType() == TokenType.CAOS)
								{
									int endmPos = n.getText().toLowerCase().indexOf("endm");
									if (endmPos != -1)
									{
										String leftover = n.getText().substring(0, endmPos);
										rscr.append(leftover);
										break;
									}
								}
								
								rscr.append(n.getOriginalText());
							}
							
							n = parser.next();
						}
					}
					
					c2p.setCAOSRemoveScript(rscr.toString().trim());
					
					break; //We just consumed the rest of the file! (or at least as much as we care about)
				}
				//else: Ignore
			}
			
			//else: Ignore (although, interestingly, it would be a CAOS syntax error to have a loose literal
		}
	}
	
	
	
	
	
	
	
	//Note: $directive doesn't include the "*#"
	protected void handleDirective(String directive) throws NotIntegerTagException, C2PSyntaxException
	{
		directive = directive.trim(); //trim whitespace
		
		if (directive.length() >= 3) //3 chars (eg, "k=v" or "c a") is the shortest tagline or command
		{
			String cmd = TemplateConverter.getCommand(directive);
			
			//Command
			if (cmd != null)
			{
				String[] args = parseCommandArgs(directive, cmd.length());
				
				c2p.addCommand(cmd, args);
			}
			
			
			//Tag
			else
			{
				int mid = directive.indexOf("=");
				
				if (mid == -1)
					throw new C2PSyntaxException("Tag directive is missing '=':    *"+DIRECTIVE_INITIATOR+directive);
				
				if (mid > 0 && mid < directive.length() - 1) //Not first and not last
				{
					String name = directive.substring(0, mid).trim();
					String val = directive.substring(mid+1).trim();
					
					if (val.length() > 0)
					{
						char post = val.charAt(0);
						
						if (post == '"') //String tag
						{
							if (val.length() < 2)
								throw new C2PSyntaxException("Missing end quote at:    *#"+directive);
							if (val.charAt(val.length()-1) != '"')
								throw new C2PSyntaxException("Missing end quote at:    *#"+directive);
							
							c2p.addStringTag(name, descapeQuotedString(val.substring(1, val.length()-1)));
						}
						else //Integer tag
						{
							try
							{
								c2p.addIntegerTag(name, Integer.parseInt(val));
							}
							catch (NumberFormatException exc)
							{
								throw new NotIntegerTagException(name);
							}
						}
					}
				}
			}
		}
	}
	
	
	
	protected static String[] parseCommandArgs(String directive, int offset) throws C2PSyntaxException
	{
		ArrayList<String> args = new ArrayList<String>();
		
		char[] cd = directive.toCharArray();
		int i = offset;
		
		//Loop through delimiter-arg pairs
		while (true)
		{
			//Skip whitespace
			while (i < cd.length && Character.isWhitespace(cd[i]))
				i++;
			
			if (i < cd.length)
			{
				//We must be in an arg now, the question is whether it's quoted
				//The same code that scans unquoted can't be used for quoted because it will falsely think any whitespace in the quotes is the end-of-arg
				if (cd[i] == '"')
				{
					i++;
					int start = i; //exclude the first quote
					
					//Scan through the quoted string, ignoring whitespace and escapes (eg, \")
					while (true)
					{
						if (i >= cd.length)
							throw new C2PSyntaxException("You left off the end quote of the last quoted-argument at:    *#"+directive);
						
						//This may be the first char in a \" escape sequence
						if (cd[i] == '\\')
						{
							i+=2; //just skip the escape now, we'll parse it later
						}
						
						//Ah, the end
						else if (cd[i] == '"')
						{
							//This is the only means by which the scanning loop should end
							i++;
							//If this brings i to EOF, that's fine
							break;
						}
						
						//Skip normal chars (including whitespace)
						else
						{
							i++;
						}
					}
					
					//This works for i==len and Not
					String raw = new String(cd, start, i-start-1);
					String descaped = descapeQuotedString(raw);
					args.add(descaped);
				}
				else
				{
					int start = i;
					
					//Skip blackspace
					while (i < cd.length && !Character.isWhitespace(cd[i]))
						i++;
					
					//This works for i==len and Not
					args.add(new String(cd, start, i-start));
				}
			}
			else
				break;
		}
		
		
		return args.toArray(new String[args.size()]);
	}
	
	
	/**
	 * This implements the escape parsing code for both command arguments and string tag values.
	 */
	protected static String descapeQuotedString(String raw)
	{
		//Handle \n, \\, and \" escapes
		char c = 0;
		StringBuilder prettyVal = new StringBuilder();
		
		int i = 0;
		for (i = 0; i < raw.length(); i++) //Don't bother with last character, an escape has two
		{
			c = raw.charAt(i);
			if (c == '\\' && i < raw.length()-1)
			{
				i++;
				c = raw.charAt(i); //the escape code
				if (c == 'n')
					prettyVal.append('\n');
				else
					prettyVal.append(c); //Any escape besides \n is escaping the escape code (eg: \", \\, \')
			}
			else
			{
				prettyVal.append(c);
			}
		}
		
		return prettyVal.toString();
	}
	//Scanning>
	
	
	
	
	
	

	//<Results
	/**
	 * -gets the results of the parsing.
	 */
	public C2PCosFile getC2PCosFile()
	{
		return this.c2p;
	}
	//Results>
}
