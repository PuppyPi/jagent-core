/*
 * Created on Jan 4, 2006
 * 	by the wonderful Eclipse(c)
 */
package rebound.jagent.lib.s16;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import rebound.bits.Bytes;
import rebound.bits.Endianness;
import rebound.bits.Unsigned;
import rebound.hci.graphics2d.ImageUtilities;
import rebound.io.iio.RandomAccessBytes;
import rebound.io.iio.sio.RandomAccessFileWrapper;
import rebound.io.iio.unions.CloseableFlushableRandomAccessBytesInterface;
import rebound.io.util.BasicIOUtilities;
import rebound.jagent.lib.FormatMismatchException;

public class FromS16Converter
{
	protected Color alphaColor = Color.BLACK; //note: this changes the color space and can expand the image in memory
	protected boolean bits565;
	protected BufferedImage[] frames;
	protected FromS16Notifee notifee;
	
	
	
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
		if (notifee != null) notifee.startS16Reading();
		readHeaders(file, map);
		readImageDatii(file, map);
		if (notifee != null) notifee.finS16Reading();
	}
	
	protected void readHeaders(RandomAccessBytes file, DataMap map) throws IOException, FormatMismatchException
	{
		int flags = Bytes.getLittleInt(file);
		bits565 = (flags & 1) != 0;
		if ((flags & 2) != 0)
			throw new FormatMismatchException("Attempting to read (what looks like) a C16 file, when expecting an S16");
		
		int count = Unsigned.upcast(Bytes.getLittleShort(file));
		map.offsets = new int[count];
		map.widths = new int[count];
		map.heights = new int[count];
		frames = new BufferedImage[count];
		
		for (int i = 0; i < count; i++)
			readHeader(file, map, i);
	}
	
	protected void readHeader(RandomAccessBytes file, DataMap map, int index) throws IOException
	{
		int offset = Bytes.getLittleInt(file);
		int width = Unsigned.upcast(Bytes.getLittleShort(file));
		int height = Unsigned.upcast(Bytes.getLittleShort(file));
		
		map.offsets[index] = offset;
		map.widths[index] = width;
		map.heights[index] = height;
	}
	
	
	
	
	protected void readImageDatii(RandomAccessBytes file, DataMap map) throws IOException, FormatMismatchException
	{
		for (int i = 0; i < frames.length; i++)
		{
			readImageData(file, map, i);
			if (notifee != null) notifee.finS16ReadingFrame(i, frames.length);
		}
	}
	
	protected void readImageData(RandomAccessBytes file, DataMap map, int index) throws IOException, FormatMismatchException
	{
		boolean is565 = bits565;
		int width = map.widths[index];
		int height = map.heights[index];
		
		
		//Read it all
		byte[] imagedata = null;
		{
			int offset = map.offsets[index];
			int imageDataLength = 2 * width * height; //2 bytes per pixel
			
			//Valiate no EOF
			{
				if (imageDataLength + offset > file.length())
					throw new FormatMismatchException("S16 File appears to be truncated at image "+index);
			}
			
			
			file.seek(offset);
			
			imagedata = new byte[imageDataLength];
			
			BasicIOUtilities.readFully(file, imagedata);
		}
		
		
		//Wrap it in an ... Image, since the JRE natively supports 555 and 565
		{
			frames[index] = ImageUtilities.wrapRawImageData_5X5_bytes(width, height, imagedata, 0, imagedata.length, is565, Endianness.Little);
			
			if (getAlphaColor() != null)
			{
				frames[index] = ImageUtilities.colorToAlpha(frames[index], getAlphaColor().getRGB()); //it might reallocate the image
			}
		}
	}
	
	
	
	
	
	
	
	public FromS16Notifee getNotifee()
	{
		return this.notifee;
	}
	
	public void setNotifee(FromS16Notifee notifee)
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
	
	public Color getAlphaColor()
	{
		return this.alphaColor;
	}
	
	public void setAlphaColor(Color alphaColor)
	{
		this.alphaColor = alphaColor;
	}
}
