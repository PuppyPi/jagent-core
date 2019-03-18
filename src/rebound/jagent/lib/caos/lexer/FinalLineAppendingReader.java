/*
 * Created on Feb 14, 2010
 * 	by the great Eclipse(c)
 */
package rebound.jagent.lib.caos.lexer;

import java.io.IOException;
import java.io.Reader;

/**
 * Appends a '\n' to the end of the data in the underlying {@link Reader}.
 * @author RProgrammer
 */
public class FinalLineAppendingReader
extends Reader
{
	protected final Reader reader;
	protected final char eofText;
	
	protected boolean appendedEOFText = false;
	
	public FinalLineAppendingReader(Reader underlying)
	{
		this(underlying, '\n');
	}
	
	/**
	 * @param eofChar This is normally '\n', but you can set it to anything
	 */
	public FinalLineAppendingReader(Reader underlying, char eofChar)
	{
		super();
		this.reader = underlying;
		this.eofText = eofChar;
	}
	
	@Override
	public int read(char[] cbuf, int off, int len) throws IOException
	{
		if (len > 0)
		{
			int amt = reader.read(cbuf, off, len);
			if (amt == -1 && !appendedEOFText)
			{
				cbuf[off] = eofText;
				appendedEOFText = true;
				return 1;
			}
			else
			{
				return amt;
			}
		}
		else
		{
			return 0;
		}
	}
	
	@Override
	public void close() throws IOException
	{
		reader.close();
	}
	
	
	protected char[] buf1 = new char[1];
	@Override
	public int read() throws IOException
	{
		int amt = 0;
		while (true)
		{
			amt = read(buf1, 0, 1);
			if (amt == -1)
				return -1;
			else if (amt == 1)
				return buf1[0];
		}
	}
}
