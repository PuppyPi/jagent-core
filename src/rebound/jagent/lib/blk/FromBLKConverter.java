/*
 * Created on Jan 4, 2006
 * 	by the wonderful Eclipse(c)
 */
package rebound.jagent.lib.blk;

import java.awt.Point;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferUShort;
import java.awt.image.Raster;
import java.awt.image.SampleModel;
import java.awt.image.SinglePixelPackedSampleModel;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import rebound.bits.Bytes;
import rebound.bits.Unsigned;
import rebound.io.BasicIOUtilities;
import rebound.io.RandomAccessFileWrapper;
import rebound.io.iio.RandomAccessBytes;
import rebound.io.iio.unions.CloseableFlushableRandomAccessBytesInterface;
import rebound.jagent.lib.FormatMismatchException;
import rebound.util.collections.ArrayUtilities;

public class FromBLKConverter
{
	protected static final int[] BITMASKS_565 = {0xF800, 0x7E0, 0x1F};
	protected static final int[] BITMASKS_555 = {0x7C00, 0x3E0, 0x1F};
	
	protected boolean bits565;
	protected BufferedImage background;
	protected FromBLKNotifee notifee;
	
	
	
	public void read(File path) throws IOException, FormatMismatchException
	{
		try (CloseableFlushableRandomAccessBytesInterface file = new RandomAccessFileWrapper(new RandomAccessFile(path, "r")))
		{
			read(file);
		}
	}
	
	public void read(RandomAccessBytes file) throws IOException, FormatMismatchException
	{
		DataMap map = new DataMap();
		if (notifee != null) notifee.startBLKReading();
		readHeaders(file, map);
		readImageData(file, map);
		if (notifee != null) notifee.finBLKReading();
	}
	
	//Internals of read()
	protected void readHeaders(RandomAccessBytes file, DataMap map) throws IOException, FormatMismatchException
	{
		int flags = Bytes.getLittleInt(file);
		bits565 = (flags & 1) != 0;
		if ((flags & 2) != 0)
			throw new FormatMismatchException("Attempting to read (what looks like) a C16 file, when expecting a BLK");
		
		int width = Unsigned.upcast(Bytes.getLittleShort(file));
		int height = Unsigned.upcast(Bytes.getLittleShort(file));
		int count = Unsigned.upcast(Bytes.getLittleShort(file));
		
		if (count != width * height)
			throw new FormatMismatchException("Invalid BLK header (count != width * height)");
		
		map.offsets = new int[count];
		
		//The ordering of BLK blocks is not scanning; it's flipped
		int i = 0;
		for (int x = 0; x < width; x++)
		{
			for (int y = 0; y < height; y++)
			{
				readHeader(file, map, i);
				i++;
			}
		}
		
		background = new BufferedImage(width*128, height*128, bits565 ? BufferedImage.TYPE_USHORT_565_RGB : BufferedImage.TYPE_USHORT_555_RGB);
	}
	
	protected void readHeader(RandomAccessBytes file, DataMap map, int index) throws IOException, FormatMismatchException
	{
		int offset = Bytes.getLittleInt(file);
		int width = Unsigned.upcast(Bytes.getLittleShort(file)); //width, which == 128
		int height = Unsigned.upcast(Bytes.getLittleShort(file)); //height, which == 128
		
		if (width != 128)
			throw new FormatMismatchException("Width of block "+index+" is not 128");
		if (height != 128)
			throw new FormatMismatchException("Height of block "+index+" is not 128");
		
		map.offsets[index] = offset+4;
	}
	
	
	protected void readImageData(RandomAccessBytes file, DataMap map) throws IOException
	{
		int i = 0;
		for (int x = 0; x < getWidthInBlocks(); x++)
		{
			for (int y = 0; y < getHeightInBlocks(); y++)
			{
				readBlock(file, map, i, x, y);
				if (notifee != null) notifee.finBLKReadingBlock(i);
				i++;
			}
		}
	}
	
	protected void readBlock(RandomAccessBytes file, DataMap map, int index, int blockx, int blocky) throws IOException
	{
		file.seek(map.offsets[index]);
		byte[] rawdata = new byte[128*128*2];
		
		BasicIOUtilities.readFully(file, rawdata);
		
		short[] shortedData = ArrayUtilities.mergeElements8to16LE(rawdata, 0, rawdata.length);
		
		SampleModel sm = new SinglePixelPackedSampleModel(DataBuffer.TYPE_USHORT, 128, 128, bits565 ? BITMASKS_565 : BITMASKS_555);
		DataBuffer db = new DataBufferUShort(shortedData, shortedData.length);
		WritableRaster raster = Raster.createWritableRaster(sm, db, new Point(blockx*128, blocky*128));
		
		background.getRaster().setRect(raster);
	}
	
	
	
	
	
	
	
	
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
	
	protected int getRealX(int xblock, int xInBlock)
	{
		return (xblock * 128) + xInBlock;
	}
	
	protected int getRealY(int yblock, int yInBlock)
	{
		return (yblock * 128) + yInBlock;
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	public FromBLKNotifee getNotifee()
	{
		return this.notifee;
	}
	
	public void setNotifee(FromBLKNotifee notifee)
	{
		this.notifee = notifee;
	}
	
	public boolean isBits565()
	{
		return this.bits565;
	}
	
	public BufferedImage getBackground()
	{
		return this.background;
	}
}
