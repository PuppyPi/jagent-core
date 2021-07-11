/*
 * Created on Jan 13, 2006
 * 	by the wonderful Eclipse(c)
 */
package rebound.jagent.lib.pray.template;

import static rebound.text.StringUtilities.*;
import static rebound.util.collections.CollectionUtilities.*;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import rebound.annotations.semantic.allowedoperations.WritableValue;
import rebound.annotations.semantic.reachability.LiveValue;
import rebound.annotations.semantic.temporal.ConstantReturnValue;
import rebound.bits.DataEncodingUtilities;
import rebound.exceptions.BinarySyntaxException;

public class PrayTemplate
{
	//Keys are filenames
	protected List<FileBlockInPrayTemplate> fileBlocks;
	
	//The others (tag-format blocks :> )
	protected List<Group> groups;
	
	
	
	//Also a directory
	protected File dir;
	
	protected String desiredOutputFile;
	
	
	
	public PrayTemplate()
	{
		super();
		fileBlocks = new ArrayList<>();
		groups = new ArrayList<>();
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
		eqv(fileBlocks, o.fileBlocks) &&
		eqv(groups, o.groups);
	}
	
	
	
	
	
	
	@WritableValue
	@ConstantReturnValue
	@LiveValue
	public List<FileBlockInPrayTemplate> getFileBlocks()
	{
		return fileBlocks;
	}
	
	@WritableValue
	@ConstantReturnValue
	@LiveValue
	public List<Group> getGroups()
	{
		return groups;
	}
	
	
	
	public void addGroup(Group g)
	{
		g.setDir(getDir());
		getGroups().add(g);
	}
	
	public void addInline(String id, String realFilename, String prayFilename)
	{
		byte[] idBytes = encodeTextToByteArrayReportingUnchecked(id, StandardCharsets.ISO_8859_1);
		
		if (idBytes.length != 4)
			throw BinarySyntaxException.inst("Inline file block id is not 4 bytes!: "+repr(id)+" → "+DataEncodingUtilities.encodeHex(idBytes, DataEncodingUtilities.HEX_UPPERCASE, " "));
		
		getFileBlocks().add(new FileBlockInPrayTemplate(realFilename, idBytes, prayFilename, false));
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
		return getFileBlocks().size();
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
