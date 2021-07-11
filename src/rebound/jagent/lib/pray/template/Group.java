/*
 * Created on Jan 13, 2006
 * 	by the wonderful Eclipse(c)
 */
package rebound.jagent.lib.pray.template;

import static rebound.util.objectutil.BasicObjectUtilities.*;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;
import javax.annotation.Nullable;
import rebound.exceptions.NotSupportedReturnPath;
import rebound.exceptions.NotYetImplementedException;
import rebound.io.util.FSIOUtilities;
import rebound.jagent.lib.FormatMismatchException;
import rebound.jagent.lib.PathBoss;
import rebound.text.StringUtilities;
import rebound.util.Either;
import rebound.util.objectutil.Equivalenceable;

/**
 * This corresponds to a tag block.
 * @author RProgrammer
 */
public class Group
implements Equivalenceable
{
	protected String ID;
	protected String name;
	protected String dir; //Needs a trailing /
	
	protected Vector<String> scriptFileNames; //needs .setSize()    //TODO Make this a List<String> storing the code in memory (who would ever be using this on a computer with less than, what, *a megabyte of ram*!? XD )
	protected boolean cutOutRemoveScriptFromFirstScript = false;  //TODO this is such a kludge x'D
	protected List<String> intTagNames;
	protected List<String> strTagNames;
	protected List<Integer> intTagVals;
	protected List<String> strTagVals;
	protected @Nullable String removeScriptFilename;
	
	public Group()
	{
		super();
		intTagNames = new ArrayList<String>();
		intTagVals = new ArrayList<Integer>();
		strTagNames = new ArrayList<String>();
		strTagVals = new ArrayList<String>();
		scriptFileNames = new Vector<String>();
	}
	
	
	
	@Override
	public boolean equivalent(Object obj) throws NotSupportedReturnPath
	{
		if (obj == this)
			return true;
		if (obj == null || !(obj instanceof Group))
			return false;
		
		Group o = (Group)obj;
		
		return
		eq(ID, o.ID) &&
		eq(name, o.name) &&
		
		eq(scriptFileNames, o.scriptFileNames) && //this one *is* order-dependent
		mapeq(this.intTagNames, this.intTagVals, o.intTagNames, o.intTagVals) &&
		mapeq(this.strTagNames, this.strTagVals, o.strTagNames, o.strTagVals) &&
		eq(removeScriptFilename, removeScriptFilename);
	}
	
	@Override
	public int hashCodeOfContents()
	{
		throw new NotYetImplementedException();  //Todo ^^'
	}
	
	
	
	private static boolean mapeq(List ak, List av, List bk, List bv)
	{
		if (ak == bk && av == bv) return true;
		if (ak == null || av == null || bk == null || bv == null) return false;
		
		int length = ak.size();
		if (av.size() != length || bk.size() != length || bv.size() != length)
			return false;
		
		Hashtable a = null;
		{
			a = new Hashtable();
			for (int i = 0; i < length; i++)
				a.put(ak.get(i), av.get(i));
		}
		
		Hashtable b = null;
		{
			b = new Hashtable();
			for (int i = 0; i < length; i++)
				b.put(bk.get(i), bv.get(i));
		}
		
		return a.equals(b);
	}	
	
	
	public void addTag(String key, Either<Integer, String> val)
	{
		if (val.isA())
		{
			addIntTag(key, val.getValueIfA());
		}
		else
		{
			addStringTag(key, val.getValueIfB());
		}
	}
	
	public void addIntTag(String key, int val)
	{
		intTagNames.add(key);
		intTagVals.add(val);
	}
	
	public void addStringTag(String key, String val)
	{
		strTagNames.add(key);
		strTagVals.add(val);
	}
	
	/**
	 * Tests if a string PRAY tag is a script data tag (eg, "Script 1")
	 * @param key The PRAY string-tag key
	 */
	public static boolean isScriptTag(String key)
	{
		if (key.startsWith("Script ") && !key.equals("Script Count"))
		{
			try
			{
				Integer.parseInt(key.substring("Script ".length()));
			}
			catch (NumberFormatException exc)
			{
				return false;
			}
			return true;
		}
		return false;
	}
	
	public void addScript(String file)
	{
		scriptFileNames.add(file);
	}
	
	public String getID()
	{
		return ID;
	}
	
	public void setID(String id)
	{
		this.ID = id;
	}
	
	public String getName()
	{
		return name;
	}
	
	public void setName(String name)
	{
		this.name = name;
	}
	
	public void setDir(File dir)
	{
		this.dir = dir.getAbsolutePath()+File.separatorChar;
	}
	
	public String getDir()
	{
		return this.dir;
	}
	
	public int getIntTagValue(int index)
	{
		return intTagVals.get(index);
	}
	
	public String getStrTagValue(int index)
	{
		return strTagVals.get(index);
	}
	
	public String getIntTagName(int index)
	{
		return intTagNames.get(index);
	}
	
	public String getStrTagName(int index)
	{
		return strTagNames.get(index);
	}
	
	
	public int getIntValCount()
	{
		return intTagNames.size();
	}
	
	public int getStrValCount()
	{
		return strTagNames.size();
	}
	
	public boolean hasScripts()
	{
		return scriptFileNames.size() > 0;
	}
	
	public boolean getCutOutRemoveScriptFromFirstScript()
	{
		return cutOutRemoveScriptFromFirstScript;
	}
	
	public void setCutOutRemoveScriptFromFirstScript(boolean cutOutRemoveScriptFromFirstScript)
	{
		this.cutOutRemoveScriptFromFirstScript = cutOutRemoveScriptFromFirstScript;
	}
	
	public List<String> getScriptFileNames()
	{
		return this.scriptFileNames;
	}
	
	/**
	 * Instantiate a List of java.io.File's
	 */
	public List<File> getScriptFiles()
	{
		List<File> files = new ArrayList<File>(scriptFileNames.size());
		for (String name : scriptFileNames)
			files.add(new File(dir, name));
		return files;
	}
	
	public @Nullable String getRemoveScriptFilename()
	{
		return removeScriptFilename;
	}
	
	public @Nullable File getRemoveScriptFile()
	{
		return removeScriptFilename == null ? null : new File(dir, removeScriptFilename);
	}
	
	
	/**
	 * Call this after {@link #writeScriptToFile(String, int) writing} all the scripts if you realize that there was only one script.
	 */
	public void recognizeSingletonScript()
	{
		//Rename the "Foo script 1.cos" file to "Foo script.cos"
		
		if (scriptFileNames.size() == 1)
		{
			File oldScriptFile = getIndexedScriptFileName(1);
			
			//Even if the file is erroneous and has only one script with a number > 1, the writeScriptToFile() method shouldn't store it in position [0]
			if (!scriptFileNames.get(0).equals(oldScriptFile.getName()))
				throw new AssertionError("Internal Inconsistency: #wSTF=\""+scriptFileNames.get(0)+"\"     #rSS=\""+oldScriptFile.getName()+"\"");
			
			File newScriptFile = getSingletonScriptFileName();
			
			boolean success = oldScriptFile.renameTo(newScriptFile);
			
			if (success)
			{
				scriptFileNames.set(0, newScriptFile.getName());
			}
		}
	}
	
	public void writeRemoveScriptToFile(String scriptData) throws IOException, FormatMismatchException
	{
		File f = getRemoveScriptFileName();
		
		writeToCosFile(scriptData, f);
		
		removeScriptFilename = f.getName();
	}
	
	public void writeScriptToFile(String scriptData, int scriptNumber) throws IOException, FormatMismatchException
	{
		_writeScriptToFile(scriptData, scriptNumber);
	}
	
	public void writeRawScriptToFile(byte[] scriptData, int scriptNumber) throws IOException, FormatMismatchException
	{
		_writeScriptToFile(scriptData, scriptNumber);
	}
	
	protected void _writeScriptToFile(Object scriptData, int scriptNumber) throws IOException, FormatMismatchException
	{
		//Make some file, deposit the script in it, then tell the group what it's called
		
		if (scriptNumber < 1)
			throw new FormatMismatchException("Invalid script number: "+scriptNumber+"   for block \""+this.getName()+"\"");
		
		//Check if we already did this script number
		if (scriptNumber <= scriptFileNames.size())
			if (scriptFileNames.get(scriptNumber-1) != null)
				throw new FormatMismatchException("Duplicate scripts at \"Script "+scriptNumber+"\"   for block \""+this.getName()+"\"");
		
		File f = getIndexedScriptFileName(scriptNumber);
		
		writeToCosFile(scriptData, f);
		
		if (scriptNumber > scriptFileNames.size())
			scriptFileNames.setSize(scriptNumber);
		scriptFileNames.set((scriptNumber-1), f.getName());
	}
	
	protected void writeToCosFile(Object scriptData, File f) throws IOException, FormatMismatchException
	{
		if (f.exists())
		{
			System.err.println("Script file exists: "+f.getAbsolutePath());
			throw new IOException("Script file exists: "+f.getName());
		}
		
		if (scriptData instanceof String)
		{
			String d = (String) scriptData;
			d = StringUtilities.universalNewlines(d);  //for consistency with praysource.txt!
			FSIOUtilities.writeAll(f, StringUtilities.encodeTextToByteArrayReportingUnchecked(d, StandardCharsets.UTF_8));
		}
		else
		{
			byte[] d = (byte[]) scriptData;
			FSIOUtilities.writeAll(f, d);
		}
	}
	
	protected File getIndexedScriptFileName(int scriptNumber)
	{
		return new File(dir, PathBoss.getInstance().getEscapedNameOnCurrentHostOS(this.getName()+" script "+scriptNumber+".cos"));
	}
	
	protected File getSingletonScriptFileName()
	{
		return new File(dir, PathBoss.getInstance().getEscapedNameOnCurrentHostOS(this.getName()+" script.cos"));
	}
	
	protected File getRemoveScriptFileName()
	{
		return new File(dir, PathBoss.getInstance().getEscapedNameOnCurrentHostOS(this.getName()+" remove script.cos"));
	}
	
	
	
	
	
	public static int getScriptNumber(String scriptTagName)
	{
		return Integer.parseInt(scriptTagName.substring(7));
	}
}
