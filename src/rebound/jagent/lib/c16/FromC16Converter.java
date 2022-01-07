/*
 * Created on Jan 4, 2006
 * 	by the wonderful Eclipse(c)
 */
package rebound.jagent.lib.c16;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import rebound.bits.Bytes;
import rebound.bits.Unsigned;
import rebound.io.iio.RandomAccessBytes;
import rebound.io.iio.sio.RandomAccessFileWrapper;
import rebound.io.iio.unions.CloseableFlushableRandomAccessBytesInterface;
import rebound.io.util.BasicIOUtilities;
import rebound.jagent.lib.FormatMismatchException;

public class FromC16Converter
{
	protected boolean bits565;
	protected BufferedImage[] frames;
	protected int[] widths, heights;
	protected FromC16Notifee notifee;
	
	
	
	public void read(File path) throws IOException, FormatMismatchException
	{
		try (CloseableFlushableRandomAccessBytesInterface file = new RandomAccessFileWrapper(path, false))
		{
			read(file);
		}
	}
	
	public void read(RandomAccessBytes file) throws IOException, FormatMismatchException
	{
		DataMap map = new DataMap();
		if (notifee != null) notifee.startC16Reading();
		readHeaders(file, map);
		readImageDatii(file, map);
		if (notifee != null) notifee.finC16Reading();
	}
	
	//Internals of read()
	protected void readHeaders(RandomAccessBytes file, DataMap map) throws IOException, FormatMismatchException
	{
		int flags = Bytes.getLittleInt(file);
		bits565 = (flags & 1) != 0;
		if ((flags & 2) == 0)
			throw new FormatMismatchException("Attempting to read (what looks like) a S16 or BLK file, when expecting a C16");
		
		int count = Unsigned.upcast(Bytes.getLittleShort(file));
		map.offsets = new int[count][];
		widths = new int[count];
		heights = new int[count];
		frames = new BufferedImage[count];
		
		for (int i = 0; i < count; i++)
			readHeader(file, map, i);
	}
	
	protected void readHeader(RandomAccessBytes file, DataMap map, int index) throws IOException
	{
		int line0 = Bytes.getLittleInt(file);
		int width = Unsigned.upcast(Bytes.getLittleShort(file));
		int height = Unsigned.upcast(Bytes.getLittleShort(file));
		
		widths[index] = width;
		heights[index] = height;
		map.offsets[index] = new int[height];
		map.offsets[index][0] = line0;
		
		for (int o = 1; o < height; o++)
		{
			map.offsets[index][o] = Bytes.getLittleInt(file);
		}
	}
	
	
	protected void readImageDatii(RandomAccessBytes file, DataMap map) throws IOException, FormatMismatchException
	{
		for (int i = 0; i < frames.length; i++)
		{
			readImageData(file, map, i);
			if (notifee != null) notifee.finC16ReadingFrame(i, frames.length);
		}
	}
	
	protected void readImageData(RandomAccessBytes file, DataMap map, int index) throws IOException, FormatMismatchException
	{
		BufferedImage img = frames[index] = new BufferedImage(widths[index], heights[index], BufferedImage.TYPE_INT_ARGB);
		int x = 0;
		
		long flen = file.length();
		
		for (int row = 0;  row < img.getHeight();  row++)
		{
			file.seek(map.offsets[index][row]);
			
			x = 0;
			while (x < img.getWidth())
			{
				//Hang-up bugfix
				if (file.getFilePointer() >= flen)
					throw new FormatMismatchException("C16 File appears to be truncated at image "+index);
				
				x += readRun(file, index, x, row);
			}
		}
	}
	
	//Return the length of the run (in pixels)
	protected int readRun(RandomAccessBytes file, int index, int x, int y) throws IOException
	{
		short tag = Bytes.getLittleShort(file);
		
		boolean transparent = (tag & 1) == 0;
		
		int runlength = 0;
		{
			runlength = tag;
			
			//Unmask the run length
			runlength = runlength >>> 1;
		}
		
		
		
		
		if (transparent)
		{
			//Do nothing, since the databuffer elements in a BufferedImage default to 0, which includes the alpha
		}
		else
		{
			//Read and store raw data
			byte[] rawdata = new byte[runlength * 2];
			BasicIOUtilities.readFully(file, rawdata);
			
			int[] directImageData = ((DataBufferInt)frames[index].getRaster().getDataBuffer()).getData();
			int i = 0;
			int e = y * widths[index] + x;
			int hiPacked = 0, argbPacked = 0;
			while (i < rawdata.length)
			{
				hiPacked = (rawdata[i] & 0xFF) | ((rawdata[i+1] & 0xFF) << 8);
				i+=2;
				
				if (bits565)
				{
					argbPacked =
					0xFF000000 | //Alpha
					((hiPacked & 0xF800) << 8) | //Red
					((hiPacked & 0x7E0) << 5) | //Green
					((hiPacked & 0x1F) << 3); //Blue
				}
				else
				{
					argbPacked =
					0xFF000000 | //Alpha
					((hiPacked & 0x7C00) << 9) | //Red
					((hiPacked & 0x3E0) << 6) | //Green
					((hiPacked & 0x1F) << 3); //Blue
				}
				
				directImageData[e++] = argbPacked;
			}
		}
		
		return runlength;
	}
	
	
	
	
	
	
	public FromC16Notifee getNotifee()
	{
		return this.notifee;
	}
	
	public void setNotifee(FromC16Notifee notifee)
	{
		this.notifee = notifee;
	}
	
	public boolean isBits565()
	{
		return this.bits565;
	}
	
	public BufferedImage[] getFrames()
	{
		return this.frames;
	}
}
