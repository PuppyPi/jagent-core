/*
 * Created on May 25, 2006
 * 	by the wonderful Eclipse(c)
 */
package rebound.jagent.lib.caos2pray.scanner;

import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import rebound.exceptions.ImpossibleException;
import rebound.io.util.UniversalNewlineReader;

/**
 * This parses CAOS files generally; it is not specific to C2P,
 * except that it does not parse much at all, separating the file into {@link Token}s.
 * These tokens are simply String Literals, CAOS code, or Comments (which is all that C2P is interested in).
 * NB: String Literals are parsed exceptionally since they might contain a '*' which is not a comment.
 * <br>
 * This is a reusable component, via the {@link #init(Reader)} methods.
 * @author RProgrammer
 */
public class CosParser
{
	//Re-use the same token
	protected Token token = new Token();
	
	protected Reader in;
	
	
	public CosParser()
	{
		super();
	}
	
	public CosParser(Reader r)
	{
		this();
		init(r);
	}
	
	/**
	 * @param data The actual cos file data
	 */
	public CosParser(String data)
	{
		this();
		init(data);
	}
	
	public CosParser(InputStream i)
	{
		this();
		init(new InputStreamReader(i));
	}
	
	/**
	 * Don't forget to {@link #close()} it when you're done.
	 */
	public CosParser(File file) throws FileNotFoundException
	{
		this(new FileInputStream(file));
	}
	
	
	
	
	
	
	public void init(Reader r)
	{
		if (in != null)
			throw new IllegalStateException("Parser is already committed. (it's disposable)");
		UniversalNewlineReader unr = UniversalNewlineReader.wrap(r);
		in = unr;
		token = new Token();
	}
	
	
	/**
	 * [re-]initializes the parser from a raw input source.
	 */
	public void init(InputStream i) throws IOException
	{
		init(new InputStreamReader(i, "UTF-8"));
	}
	
	/**
	 * [re-]initializes the parser from a String of the actual data.
	 * @param data The actual contents of a cos file
	 */
	public void init(String data)
	{
		init(new StringReader(data));
	}
	
	
	
	
	
	
	
	/**
	 * This parses a token's worth of data and returns it as such.<br>
	 * <b>Note</b>: This reuses the same token object; you must not hold a reference to it (or its fields will change under your nose)!<br>
	 * Upon EOF this returns <code>null</code>.
	 * <br>
	 * <br>
	 * Syntax contracts and quirks:
	 * <ul>
	 * 	<li>Char and String literal tokens' text will not contain the enclosing quotes. "\"","\"" or "'","'"</li>
	 * 	<li>Char and String literal tokens' text will not be descaped (eg, it could contain the \" and \n, etc.  CAOS escapes)</li>
	 * 	<li>Comment tokens' text will not contain enclosing "*","\n" or "*",EOF</li>
	 * 	<li>CAOS token's text will not be changed, but will not include delimiters from other tokens (eg, the terminating newline of a previous comment)</li>
	 * </ul>
	 */
	public Token next() throws IOException
	{
		try
		{
			char c = read();
			StringBuilder buff = new StringBuilder();
			
			if (c == '"')
			{
				//STRING
				token.type = TokenType.STRING;
				
				while (true)
				{
					//EOF makes an invalid string, so don't worry about returning the truncated literal
					c = read();
					
					if (c == '\"')
						break;
					
					if (c == '\\')
					{
						c = read();
						buff.append('\\');
						buff.append(c);
					}
					else
					{
						buff.append(c);
					}
				}
				//Don't pushback the closing '"', eat it
			}
			else if (c == '\'')
			{
				//(char) STRING
				token.type = TokenType.CHAR;
				
				while (true)
				{
					//EOF makes an invalid string, so don't worry about returning the truncated literal
					c = read();
					
					if (c == '\'')
						break;
					
					if (c == '\\')
					{
						c = read();
						buff.append('\\');
						buff.append(c);
					}
					else
					{
						buff.append(c);
					}
				}
			}
			else if (c == '*')
			{
				//COMMENT
				token.type = TokenType.COMMENT;
				
				while (true)
				{
					try
					{
						c = read();
					}
					catch (EOFException exc)
					{
						break;
					}
					
					if (c == '\n')
						break;
					
					buff.append(c);
				}
				//Don't push back the newline, just eat it
			}
			else
			{
				//CAOS
				token.type = TokenType.CAOS;
				
				while (true)
				{
					buff.append(c);
					
					try
					{
						c = read();
					}
					catch (EOFException exc)
					{
						break;
					}
					
					if (c == '"' || c == '\'' || c == '*')
					{
						pushback(c);
						break;
					}
				}
			}
			
			token.text = buff.toString();
			
			return token;
		}
		catch (EOFException exc)
		{
			return null;
		}
	}
	
	boolean hasPushback = false;
	char pushbackChar = 0;
	
	protected char read() throws EOFException, IOException
	{
		if (hasPushback)
		{
			hasPushback = false;
			return pushbackChar;
		}
		else
		{
			int c = in.read();
			if (c == -1)
				throw new EOFException();
			return (char)c;
		}
	}
	
	protected void pushback(char c)
	{
		if (hasPushback)
			throw new IllegalStateException("Can only push back 1 char");
		
		pushbackChar = c;
		hasPushback = true;
	}
	
	
	/**
	 * Simply closes the underlying reader.  ({@link #next()} doesn't do this)
	 */
	public void close() throws IOException
	{
		in.close();
	}
	
	
	
	
	
	public static class Token
	{
		protected TokenType type;
		protected String text;
		
		public Token()
		{
			super();
		}
		
		public Token(TokenType type, String text)
		{
			super();
			this.type = type;
			this.text = text;
		}
		
		
		public TokenType getType()
		{
			return this.type;
		}
		
		public String getText()
		{
			return this.text;
		}
		
		public String getOriginalText()
		{
			switch (type)
			{
				case CAOS:
					return text;
				case COMMENT:
					return "*"+text+"\n";
				case STRING:
					return '"'+text+'"';
				case CHAR:
					return "'"+text+"'";
				default:
					throw new ImpossibleException("enum escape");
			}
		}
		
		
		
		public Token clone()
		{
			return new Token(getType(), getText());
		}
		
		@Override
		public boolean equals(Object obj)
		{
			if (obj != null && obj instanceof Token)
			{
				Token o = (Token)obj;
				return
				o.getType() == this.getType()
				&&
				(
					o.getText() == null && this.getText() == null
					||
					o.getText() != null && o.getText().equals(this.getText())
				);
			}
			return false;
		}
		
		@Override
		public int hashCode()
		{
			final int prime = 31;
			int result = 1;
			result = prime * result + ((this.text == null) ? 0 : this.text.hashCode());
			result = prime * result + ((this.type == null) ? 0 : this.type.hashCode());
			return result;
		}
	}
	
	
	
	public static enum TokenType
	{
		/**
		 * A blob of caos code between string literals and comments
		 */
		CAOS,
		
		/**
		 * A line comment (CAOS only has single-line comments)
		 * The {@link Token#getText()} excludes the '*' and the '\n'
		 */
		COMMENT,
		
		/**
		 * A "String" literal
		 * The {@link Token#getText()} excludes the enclosing double quotes.
		 */
		STRING,
		
		/**
		 * A 'char' literal
		 * The {@link Token#getText()} excludes the enclosing quotes.
		 */
		CHAR,
	}
}
