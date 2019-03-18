/*
 * Created on May 25, 2006
 * 	by the wonderful Eclipse(c)
 */
package rebound.jagent.lib.caos2pray;

import java.io.EOFException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import rebound.annotations.semantic.meta.dependencies.DependencyFile;
import rebound.jagent.ResourceHog;
import rebound.jagent.lib.caos2pray.scanner.C2PCosFile;

//Todo make this not static
//Todo make it read into an internal Map<String, String>

/**
 * This class is in charge of all 'friendliness'.<br>
 * Exactly: It replaces easy tags with their true PRAY equivalents (i.e. "Desc" -> "Agent Description")
 * @author Sean
 */
@DependencyFile("./res/friendliness.nice")
public class FriendlyTags
{
	private FriendlyTags() {} //static class (bleh)
	
	
	/**
	 * This performs all replacements of friendly tags with proper tags on a given {@link C2PCosFile}.
	 */
	public static void postProcess(C2PCosFile r)
	{
		for (int i = 0; i < getFriendlyCount(); i++)
			r.replaceTagName(getFriendlyEasy(i), getFriendlyReplacement(i));
	}
	
	
	
	
	
	
	
	//Friendlies
	public static final String NICE_FILE = "friendliness.nice";
	protected static ArrayList<String> easies = null;
	protected static ArrayList<String> replacements = null;
	
	public static int getFriendlyCount()
	{
		return easies.size();
	}
	
	public static String getFriendlyEasy(int i)
	{
		return easies.get(i);
	}
	
	public static String getFriendlyReplacement(int i)
	{
		return replacements.get(i);
	}
	
	
	
	//The PPConfig file
	/*
	 * Format example:
	 * 
	 * "desc" --> "Agent Description"
	 * "anim" --> "Agent Animation String"
	 * 
	 * 
	 * Strict format description:
	 * 	There must always be an even number of quoted strings.
	 * 	The odd numbered quoted strings (first, third, ...) are the easies, i.e. what you put in your CAOS, they are case-insensitive
	 * 	The even numbered quoted strings (second, fourth, ...) are the longs or trues, i.e. what actually goes in the pray file
	 * 	There may be any number of any character except " between quoted strings  (eg, '\n' or ' --> ')
	 */
	
	/**
	 * This may be called any number of times; the config is cached on the first successful read.
	 */
	public static void loadConfig() throws IOException, FileNotFoundException
	{
		if (easies == null) //is it cached?
		{
			//Try
			ArrayList<String> easies = null;
			ArrayList<String> replacements = null;
			{
				easies = new ArrayList<String>();
				replacements = new ArrayList<String>();
				
				String r = FriendlyTags.class.getPackage().getName().replace('.', '/')+"/res/"+NICE_FILE;
				InputStream config = ResourceHog.getResourceAsStream(r);
				if (config == null)
					throw new FileNotFoundException(r);
				Reader in = new InputStreamReader(config);
				
				try
				{
					char c = 0;
					boolean easy = true; //false = long
					StringBuilder buff = new StringBuilder(); //Just so we don't have to recreate it every time
					while (true)
					{
						c = read(in);
						if (c == '"') //This ignores empty tabs, comments, you name it!
						{
							
							//Once in the quoted string, read the data within
							while ((c = read(in)) != '"')
							{
								if (c == '\\') //Don't forget your escapes!
								{
									c = read(in);
									if (c == 'n')
										buff.append('\n');
									else
										buff.append(c);
								}
								else
									buff.append(c);
							}
							
							if (easy)
								easies.add(buff.toString());
							else
								replacements.add(buff.toString());
							
							easy = !easy;
							buff.setLength(0);
						}
					}
				}
				catch (EOFException exc)
				{
				}
			}
			
			//Success!
			FriendlyTags.easies = easies;
			FriendlyTags.replacements = replacements;
		}
	}
	
	protected static char read(Reader r) throws EOFException, IOException
	{
		int c = r.read();
		if (c == -1)
			throw new EOFException();
		return (char)c;
	}
}
