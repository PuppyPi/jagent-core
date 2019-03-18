/*
 * Created on Jan 13, 2006
 * 	by the wonderful Eclipse(c)
 */
package rebound.jagent.lib.pray.template;

import java.io.File;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class PrayTemplate
{
	//Also a directory
	protected File dir;
	
	//Keys are source names, values are names for PRAY blocks
	protected Hashtable<String, String> inlineFileNames;
	protected Hashtable<String, String> inlineFileIDs;
	
	//The others
	protected List<Group> groups;
	
	
	
	protected String desiredOutputFile;
	
	
	public PrayTemplate()
	{
		super();
		inlineFileNames = new Hashtable<String, String>(3);
		inlineFileIDs = new Hashtable<String, String>(3);
		groups = new ArrayList<Group>();
	}
	
	public PrayTemplate(File dir)
	{
		this();
		this.dir = dir;
	}
	
	
	
	@Override
	public boolean equals(Object obj)
	{
		if (obj == this)
			return true;
		if (obj == null || !(obj instanceof PrayTemplate))
			return false;
		
		PrayTemplate o = (PrayTemplate)obj;
		
		return
		eq(inlineFileIDs, o.inlineFileIDs) &&
		eq(inlineFileNames, o.inlineFileNames) &&
		
		seteq(groups, o.groups);
	}
	
	private static boolean eq(Object a, Object b)
	{
		if (a == b) return true;
		if (a == null || b == null) return false;
		return a.equals(b);
	}
	
	private static boolean seteq(List a, List b)
	{
		if (a == b) return true;
		if (a == null || b == null) return false;
		
		int length = a.size();
		if (b.size() != length)
			return false;
		
		List a2 = new ArrayList(a);
		List b2 = new ArrayList(b);
		
		for (int i = 0; i < length; i++)
		{
			Object v = a2.get(i);
			int idx = b2.indexOf(v);
			if (idx == -1)
				return false;
			b2.remove(idx);
		}
		
		return true;
	}
	
	
	
	
	
	public void addGroup(Group g)
	{
		g.setDir(getDir());
		groups.add(g);
	}
	
	public void addInline(String id, String realFilename, String prayFilename)
	{
		inlineFileIDs.put(realFilename, id);
		inlineFileNames.put(realFilename, prayFilename);
	}
	
	
	public int getGroupCount()
	{
		return groups.size();
	}
	
	public Group getGroup(int index)
	{
		return groups.get(index);
	}
	
	public int getInlineFilesCount()
	{
		return inlineFileNames.size();
	}
	
	public void setDir(File dir)
	{
		this.dir = dir;
		for (int i = 0; i < groups.size(); i++)
			groups.get(i).setDir(dir);
	}
	
	public File getDir()
	{
		return this.dir;
	}
	
	public String[] getInlineSourcefiles()
	{
		Set keySet = inlineFileNames.keySet();
		Iterator i = keySet.iterator();
		String[] rv = new String[keySet.size()];
		int e = 0;
		while (i.hasNext())
			rv[e++] = (String)i.next();
		return rv;
	}
	
	public String getInlineFilePrayName(String sourcename)
	{
		return inlineFileNames.get(sourcename);
	}
	
	public String getInlineFilePrayID(String sourcename)
	{
		return inlineFileIDs.get(sourcename);
	}
	
	
	/**
	 * This is an optional informational field that stores a pray source's preference for the name of the output file, or <code>null</code> if it has no such preference.
	 */
	public String getDesiredOutputFile()
	{
		return this.desiredOutputFile;
	}
	
	public void setDesiredOutputFile(String desiredOutputFile)
	{
		this.desiredOutputFile = desiredOutputFile;
	}
}
