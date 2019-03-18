/*
 * Created on Jan 4, 2006
 * 	by the wonderful Eclipse(c)
 */
package rebound.jagent.lib.s16;

import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import rebound.bits.Bytes;
import rebound.bits.Endianness;
import rebound.hci.graphics2d.ImageUtilities;
import rebound.io.RandomAccessFileWrapper;
import rebound.io.iio.RandomAccessBytes;
import rebound.io.iio.unions.CloseableFlushableRandomAccessBytesInterface;
import rebound.jagent.lib.FormatMismatchException;

public class ToS16Converter
{
	protected boolean bits565 = false;
	protected ToS16Notifee notifee;
	protected BufferedImage[] frames;
	
	
	
	public void write(File path) throws IOException, FormatMismatchException
	{
		try (CloseableFlushableRandomAccessBytesInterface file = new RandomAccessFileWrapper(new RandomAccessFile(path, "rw")))
		{
			write(file);
		}
	}
	
	public void write(RandomAccessBytes file) throws IOException, FormatMismatchException
	{
		normalizeImages(); //Convert the images to 565 or 555
		
		if (notifee != null) notifee.startS16Writing();
		DataMap map = new DataMap();
		
		file.setLength(0); //Take care of pre-existing data
		
		writeImageDatii(file, map);
		
		file.seek(0);
		writeHeaders(file, map); //Headers
	}
	
	
	//Internals of write()
	protected void writeHeaders(RandomAccessBytes file, DataMap map) throws IOException, FormatMismatchException
	{
		//Check sizes for overflow
		if (frames.length >= 65536)
			throw new FormatMismatchException("Too many frames ("+frames.length+") to store in an S16 (max: 65535 or 32767)");
		
		
		
		//Write file header
		//Flags
		int flags = 0;
		if (bits565)
			flags |= 1;
		Bytes.putLittleInt(file, flags);
		
		
		//Frame count
		Bytes.putLittleShort(file, (short)frames.length);
		
		
		//Write frame headers
		for (int i = 0; i < frames.length; i++)
			writeFrameHeader(file, i, map);
	}
	
	protected void writeFrameHeader(RandomAccessBytes file, int frameIndex, DataMap map) throws IOException, FormatMismatchException
	{
		//Check sizes for overflow
		int width = frames[frameIndex].getWidth();
		int height = frames[frameIndex].getHeight();
		
		if (width >= 65536)
			throw new FormatMismatchException("Image is too wide ("+width+") to store in an S16 (max: 65535 or 32767)");
		if (height >= 65536)
			throw new FormatMismatchException("Image is too tall ("+height+") to store in an S16 (max: 65535 or 32767)");
		
		
		
		
		//Offset
		int offset0 = map.offsets[frameIndex];
		Bytes.putLittleInt(file, offset0);
		
		//Dimensions
		Bytes.putLittleShort(file, (short)width);
		Bytes.putLittleShort(file, (short)height);
	}
	
	
	
	protected void calculateHeaderLen(DataMap map)
	{
		map.headersLength = 4; //flags
		map.headersLength += 2; //frame count
		for (int i = 0; i < frames.length; i++)
		{
			map.headersLength += 4; //Line0 offset
			map.headersLength += 2; //Width
			map.headersLength += 2; //Height
		}
	}
	
	
	
	protected void writeImageDatii(RandomAccessBytes file, DataMap map) throws IOException
	{
		calculateHeaderLen(map);
		file.seek(map.headersLength);
		map.offsets = new int[frames.length];
		for (int i = 0; i < frames.length; i++)
		{
			writeImageData(file, i, map);
			if (notifee != null) notifee.finS16WritingFrame(i, frames.length);
		}
		
		if (notifee != null) notifee.finS16Writing();
	}
	
	protected void writeImageData(RandomAccessBytes file, int frameIndex, DataMap map) throws IOException
	{
		map.offsets[frameIndex] = (int)file.getFilePointer();
		byte[] rawImageData = ImageUtilities.extractRawImageData_5X5_bytes(frames[frameIndex], bits565, Endianness.Little);
		file.write(rawImageData);
	}
	
	
	
	
	
	protected void normalizeImages()
	{
		ColorConvertOp converter = new ColorConvertOp(ColorSpace.getInstance(ColorSpace.CS_sRGB), null);
		BufferedImage dest = null, src = null;
		if (notifee != null) notifee.startS16PreConverting();
		for (int i = 0; i < frames.length; i++)
		{
			if (!isImageOnRightMode(frames[i]) || src.getRaster().getDataBuffer().getOffset() != 0)
			{
				src = frames[i];
				dest = new BufferedImage(src.getWidth(), src.getHeight(), isBits565() ? BufferedImage.TYPE_USHORT_565_RGB : BufferedImage.TYPE_USHORT_555_RGB);
				converter.filter(src, dest);
				frames[i] = dest;
				if (notifee != null) notifee.finS16PreConvertingFrame(i, frames.length);
			}
		}
		if (notifee != null) notifee.finS16PreConverting();
	}
	
	
	protected boolean isImageOnRightMode(BufferedImage img)
	{
		return
		(bits565 && img.getType() == BufferedImage.TYPE_USHORT_565_RGB)
		||
		(!bits565 && img.getType() == BufferedImage.TYPE_USHORT_555_RGB);
	}
	
	
	
	
	
	public boolean isBits565()
	{
		return this.bits565;
	}
	
	public void setBits565(boolean bits565)
	{
		this.bits565 = bits565;
	}
	
	public void setFrames(BufferedImage[] frames)
	{
		this.frames = frames.clone();
	}
	
	public ToS16Notifee getNotifee()
	{
		return notifee;
	}
	
	public void setNotifee(ToS16Notifee notifee)
	{
		this.notifee = notifee;
	}
}
