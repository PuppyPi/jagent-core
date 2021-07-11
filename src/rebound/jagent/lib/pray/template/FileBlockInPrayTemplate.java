package rebound.jagent.lib.pray.template;

import static java.util.Objects.*;
import java.util.Arrays;
import rebound.concurrency.immutability.StaticallyConcurrentlyImmutable;
import rebound.jagent.lib.archive.CreaturesArchiveFormat;

public class FileBlockInPrayTemplate
implements StaticallyConcurrentlyImmutable
{
	protected final String sourceFileName;
	
	protected final byte[] id;  //Todo replace raw byte[]s with ImmutableByteArrayList's XD''
	protected final String name;
	protected final boolean archiveFormatInPray;
	
	
	public FileBlockInPrayTemplate(String sourceFileName, byte[] id, String name, boolean archiveFormatInPray)
	{
		this.sourceFileName = requireNonNull(sourceFileName);
		this.id = requireNonNull(id);
		this.name = requireNonNull(name);
		this.archiveFormatInPray = archiveFormatInPray;
	}
	
	public String getSourceFileName()
	{
		return sourceFileName;
	}
	
	public byte[] getId()
	{
		return id;
	}
	
	public String getName()
	{
		return name;
	}
	
	/**
	 * If true, the data will be in {@link CreaturesArchiveFormat} format in the PRAY Chunk file, but in the unwrapped/unzipped version on disk (in the {@link #getSourceFileName() source file})
	 */
	public boolean isArchiveFormatInPray()
	{
		return archiveFormatInPray;
	}
	
	
	
	
	
	
	
	
	
	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + (archiveFormatInPray ? 1231 : 1237);
		result = prime * result + Arrays.hashCode(id);
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((sourceFileName == null) ? 0 : sourceFileName.hashCode());
		return result;
	}
	
	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		FileBlockInPrayTemplate other = (FileBlockInPrayTemplate) obj;
		if (archiveFormatInPray != other.archiveFormatInPray)
			return false;
		if (!Arrays.equals(id, other.id))
			return false;
		if (name == null)
		{
			if (other.name != null)
				return false;
		}
		else if (!name.equals(other.name))
			return false;
		if (sourceFileName == null)
		{
			if (other.sourceFileName != null)
				return false;
		}
		else if (!sourceFileName.equals(other.sourceFileName))
			return false;
		return true;
	}
}
