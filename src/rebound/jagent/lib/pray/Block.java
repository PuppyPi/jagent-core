package rebound.jagent.lib.pray;

/**
 * The entire block in memory! 8>
 */
public class Block
{
	protected BlockHeader header;
	protected byte[] data;
	
	public BlockHeader getHeader()
	{
		return header;
	}
	
	public Block(BlockHeader header, byte[] data)
	{
		this.header = header;
		this.data = data;
	}
	
	public void setHeader(BlockHeader header)
	{
		this.header = header;
	}
	
	/**
	 * The raw data in the file :>
	 * + This is the *compressed* data if the block {@link BlockHeader#isCompressed() is compressed}!
	 */
	public byte[] getData()
	{
		return data;
	}
	
	public void setData(byte[] data)
	{
		this.data = data;
	}
}
