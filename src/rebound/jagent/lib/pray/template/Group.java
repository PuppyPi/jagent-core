/*
 * Created on Jan 13, 2006
 * 	by the wonderful Eclipse(c)
 */
package rebound.jagent.lib.pray.template;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;
import rebound.bits.Bytes;
import rebound.io.JRECompatIOUtilities;
import rebound.jagent.lib.FormatMismatchException;
import rebound.jagent.lib.PathBoss;

/**
 * This corresponds to a tag block.
 * @author RProgrammer
 */
public class Group
{
	protected String ID;
	protected String name;
	protected String dir; //Needs a trailing /
	
	protected Vector<String> scriptFiles; //needs .setSize()
	protected List<String> intTagNames;
	protected List<String> strTagNames;
	protected List<Integer> intTagVals;
	protected List<String> strTagVals;
	
	public Group()
	{
		super();
		intTagNames = new ArrayList<String>();
		intTagVals = new ArrayList<Integer>();
		strTagNames = new ArrayList<String>();
		strTagVals = new ArrayList<String>();
		scriptFiles = new Vector<String>();
	}
	
	
	
	@Override
	public boolean equals(Object obj)
	{
		if (obj == this)
			return true;
		if (obj == null || !(obj instanceof Group))
			return false;
		
		Group o = (Group)obj;
		
		return
		eq(ID, o.ID) &&
		eq(name, o.name) &&
		
		eq(scriptFiles, o.scriptFiles) && //this one *is* order-dependent
		mapeq(this.intTagNames, this.intTagVals, o.intTagNames, o.intTagVals) &&
		mapeq(this.strTagNames, this.strTagVals, o.strTagNames, o.strTagVals);
	}
	
	private static boolean eq(Object a, Object b)
	{
		if (a == b) return true;
		if (a == null || b == null) return false;
		return a.equals(b);
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
	
	
	public void addTag(String key, TagVal val)
	{
		if (val.isIntegerTagVal())
		{
			addIntTag(key, val.getIntegerValue());
		}
		else
		{
			addStringTag(key, val.getStringValue());
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
		scriptFiles.add(file);
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
		return scriptFiles.size() > 0;
	}
	
	public List<String> getScriptFiles()
	{
		return this.scriptFiles;
	}
	
	/**
	 * Instantiate a List of java.io.File's
	 */
	public List<File> getScriptFilesAsFiles()
	{
		List<File> files = new ArrayList<File>(scriptFiles.size());
		for (String name : scriptFiles)
			files.add(new File(dir, name));
		return files;
	}
	
	
	/**
	 * Call this after {@link #writeScriptToFile(InputStream, int) writing} all the scripts if you realize that there was only one script.
	 */
	public void recognizeSingletonScript()
	{
		//Rename the "Foo script 1.cos" file to "Foo script.cos"
		
		if (scriptFiles.size() == 1)
		{
			File oldScriptFile = getIndexedScriptFileName(1);
			
			//Even if the file is erroneous and has only one script with a number > 1, the writeScriptToFile() method shouldn't store it in position [0]
			if (!scriptFiles.get(0).equals(oldScriptFile.getName()))
				throw new AssertionError("Internal Inconsistency: #wSTF=\""+scriptFiles.get(0)+"\"     #rSS=\""+oldScriptFile.getName()+"\"");
			
			File newScriptFile = getSingletonScriptFileName();
			
			boolean success = oldScriptFile.renameTo(newScriptFile);
			
			if (success)
			{
				scriptFiles.set(0, newScriptFile.getName());
			}
		}
	}
	
	public void writeScriptToFile(InputStream scriptData, int scriptNumber) throws IOException, FormatMismatchException
	{
		//Make some file, deposit the script in it, then tell the group what it's called
		
		if (scriptNumber < 1)
			throw new FormatMismatchException("Invalid script number: "+scriptNumber+"   for block \""+this.getName()+"\"");
		
		//Check if we already did this script number
		if (scriptNumber <= scriptFiles.size())
			if (scriptFiles.get(scriptNumber-1) != null)
				throw new FormatMismatchException("Duplicate scripts at \"Script "+scriptNumber+"\"   for block \""+this.getName()+"\"");
		
		File f = null;
		{
			f = getIndexedScriptFileName(scriptNumber);
			
			if (f.exists())
			{
				System.err.println("Script file exists: "+f.getAbsolutePath());
				throw new IOException("Script file exists: "+f.getName());
			}
		}
		
		FileOutputStream out = new FileOutputStream(f);
		
		int len = Bytes.getLittleInt(scriptData);
		JRECompatIOUtilities.pumpFixed(scriptData, out, len);
		
		if (scriptNumber > scriptFiles.size())
			scriptFiles.setSize(scriptNumber);
		scriptFiles.set((scriptNumber-1), f.getName());
		
		out.close();
	}
	
	protected File getIndexedScriptFileName(int scriptNumber)
	{
		return new File(dir, PathBoss.getInstance().getEscapedNameOnCurrentHostOS(this.getName()+" script "+scriptNumber+".cos"));
	}
	
	protected File getSingletonScriptFileName()
	{
		return new File(dir, PathBoss.getInstance().getEscapedNameOnCurrentHostOS(this.getName()+" script.cos"));
	}
	
	
	
	
	
	public static int getScriptNumber(String scriptTagName)
	{
		return Integer.parseInt(scriptTagName.substring(7));
	}
}
