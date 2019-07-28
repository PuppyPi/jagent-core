/*
 * Created on Jan 4, 2006
 * 	by the wonderful Eclipse(c)
 */
package rebound.jagent.lib.blk;

import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.awt.image.DataBufferUShort;
import java.awt.image.SinglePixelPackedSampleModel;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import rebound.bits.Bytes;
import rebound.exceptions.ImpossibleException;
import rebound.io.iio.RandomAccessBytes;
import rebound.io.iio.sio.RandomAccessFileWrapper;
import rebound.io.iio.unions.CloseableFlushableRandomAccessBytesInterface;
import rebound.jagent.lib.FormatMismatchException;
import rebound.util.collections.ArrayUtilities;

public class ToBLKConverter
{
	protected boolean bits565 = false;
	protected BufferedImage background;
	protected ToBLKNotifee notifee;
	
	
	
	public int getWidthInBlocks()
	{
		int w = background.getWidth();
		int bw = w / 128;
		if (w % 128 != 0)
			bw++;
		return bw;
	}
	
	public int getHeightInBlocks()
	{
		int h = background.getHeight();
		int bh = h / 128;
		if (h % 128 != 0)
			bh++;
		return bh;
	}
	
	public int getBlockCount()
	{
		return getWidthInBlocks() * getHeightInBlocks();
	}
	
	
	public void write(File path) throws IOException, FormatMismatchException
	{
		try (CloseableFlushableRandomAccessBytesInterface file = new RandomAccessFileWrapper(new RandomAccessFile(path, "rw")))
		{
			write(file);
		}
	}
	
	public void write(RandomAccessBytes file) throws IOException, FormatMismatchException
	{
		normalizeBackground();
		
		
		if (notifee != null) notifee.startBLKWriting();
		DataMap map = new DataMap();
		
		file.setLength(0); //Delete any pre-existing data
		
		writeImageData(file, map);
		
		//It's easier to record the info in the headers as we go then come back and write it later
		file.seek(0);
		writeHeaders(file, map); //Headers
	}
	
	
	//Internals of write()
	protected void writeHeaders(RandomAccessBytes file, DataMap map) throws IOException, FormatMismatchException
	{
		//Check sizes for overflow
		if (getWidthInBlocks() >= 65536)
			throw new FormatMismatchException("Image is too wide ("+getWidthInBlocks()+" blocks) to store in a BLK (max: 65535 or 32767)");
		if (getHeightInBlocks() >= 65536)
			throw new FormatMismatchException("Image is too tall ("+getHeightInBlocks()+" blocks) to store in a BLK (max: 65535 or 32767)");
		if (getBlockCount() >= 65536)
			throw new FormatMismatchException("Image is too large ("+getWidthInBlocks()+"x"+getHeightInBlocks()+"="+getBlockCount()+" blocks total) to store in a BLK (max: 65535 or 32767)");
		
		//Note: individual "frames" needn't be checked because they will always be   <= 128  x  <= 128   !
		
		
		//Write file header
		//Flags
		int flags = 0;
		if (bits565)
			flags |= 1;
		Bytes.putLittleInt(file, flags);
		
		
		//Dimensions
		Bytes.putLittleShort(file, (short)getWidthInBlocks());
		Bytes.putLittleShort(file, (short)getHeightInBlocks());
		short count = (short)getBlockCount();
		Bytes.putLittleShort(file, count);
		
		
		//Write block headers
		for (int i = 0; i < count; i++)
			writeBlockHeader(file, i, map);
	}
	
	protected void writeBlockHeader(RandomAccessBytes file, int blockIndex, DataMap map) throws IOException
	{
		//Offset
		int offset0 = map.offsets[blockIndex]-4;
		Bytes.putLittleInt(file, offset0);
		
		//Dimensions
		short width = (short)128;
		short height = (short)128;
		Bytes.putLittleShort(file, width);
		Bytes.putLittleShort(file, height);
	}
	
	
	
	protected void calculateHeaderLen(DataMap map)
	{
		map.headersLength = 4; //flags
		map.headersLength += 2; //width in blocks
		map.headersLength += 2; //height in blocks
		map.headersLength += 2; //block count
		
		for (int i = 0; i < getBlockCount(); i++)
		{
			map.headersLength += 4; //Line0 offset
			map.headersLength += 2; //Width
			map.headersLength += 2; //Height
		}
	}
	
	
	
	protected void writeImageData(RandomAccessBytes file, DataMap map) throws IOException
	{
		calculateHeaderLen(map);
		file.seek(map.headersLength);
		map.offsets = new int[getBlockCount()];
		int i = 0;
		
		//The ordering of BLK blocks is not scanning; it's flipped
		for (int blockx = 0; blockx < getWidthInBlocks(); blockx++)
		{
			for (int blocky = 0; blocky < getHeightInBlocks(); blocky++)
			{
				map.offsets[i] = (int)file.getFilePointer();
				writeBlock(file, blockx, blocky);
				if (notifee != null) notifee.finBLKWritingBlock(blocky*getWidthInBlocks()+blockx);
				i++;
			}
		}
		
		if (notifee != null) notifee.finBLKWriting();
	}
	
	protected void writeBlock(RandomAccessBytes file, int blockx, int blocky) throws IOException
	{
		int blockwidth = 0;
		int blockheight = 0;
		{
			if (background.getWidth() % 128 == 0)
				blockwidth = 128;
			else
				if (blockx == background.getWidth() / 128)
					blockwidth = background.getWidth() % 128;
				else
					blockwidth = 128;
			
			if (background.getHeight() % 128 == 0)
				blockheight = 128;
			else
				if (blocky == background.getHeight() / 128)
					blockheight = background.getHeight() % 128;
				else
					blockheight = 128;
		}
		
		
		//Get a reference to the image data
		short[] data = null;
		{
			if (!(background.getRaster().getSampleModel() instanceof SinglePixelPackedSampleModel))
				throw new ImpossibleException("A BufferedImage of 565/555 does not have a SinglePixelPackedSampleModel?!");
			DataBufferUShort db = (DataBufferUShort)background.getRaster().getDataBuffer();
			data = db.getData();
		}
		
		
		//Write the image data directly to a file
		{
			//Write black(zeros) to the portions of the image section that aren't covered in the 128x128 block (since not all images are multiples of 128x128)
			byte[] rowPadding = blockwidth < 128 ? new byte[(128 - blockwidth) * 2] : null;
			
			for (int row = 0; row < blockheight; row++)
			{
				int offset = (blocky*128+row)*background.getWidth() + (blockx*128);
				byte[] splitData = ArrayUtilities.splitElements16to8LE(data, offset, blockwidth);
				file.write(splitData);
				
				if (blockwidth < 128)
					file.write(rowPadding);
			}
			
			if (blockheight < 128)
			{
				//More blackness
				byte[] bottomPadding = new byte[(128 - blockheight) * 128 * 2];
				file.write(bottomPadding);
			}
		}
	}
	
	
	
	
	
	protected boolean isImageOnRightMode(BufferedImage img)
	{
		return
		(bits565 && img.getType() == BufferedImage.TYPE_USHORT_565_RGB)
		||
		(!bits565 && img.getType() == BufferedImage.TYPE_USHORT_555_RGB);
	}
	
	protected void normalizeBackground()
	{
		if (!isImageOnRightMode(background) || background.getRaster().getDataBuffer().getOffset() != 0)
		{
			ColorConvertOp converter = new ColorConvertOp(ColorSpace.getInstance(ColorSpace.CS_sRGB), null);
			BufferedImage converted = new BufferedImage(background.getWidth(), background.getHeight(), isBits565() ? BufferedImage.TYPE_USHORT_565_RGB : BufferedImage.TYPE_USHORT_555_RGB);
			converter.filter(background, converted);
			this.background = converted;
		}
	}
	
	
	
	
	
	
	
	
	public boolean isBits565()
	{
		return this.bits565;
	}
	
	public void setBits565(boolean bits565)
	{
		this.bits565 = bits565;
	}
	
	public void setBackground(BufferedImage background)
	{
		this.background = background;
	}
	
	public ToBLKNotifee getNotifee()
	{
		return notifee;
	}
	
	public void setNotifee(ToBLKNotifee notifee)
	{
		this.notifee = notifee;
	}
}
