/*
 * Created on Jan 4, 2006
 * 	by the wonderful Eclipse(c)
 */
package rebound.jagent.lib.c16;

import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.awt.image.DataBufferUShort;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import rebound.bits.Bytes;
import rebound.hci.graphics2d.ImageUtilities;
import rebound.io.iio.RandomAccessBytes;
import rebound.io.iio.sio.RandomAccessFileWrapper;
import rebound.io.iio.unions.CloseableFlushableRandomAccessBytesInterface;
import rebound.jagent.lib.FormatMismatchException;
import rebound.util.collections.ArrayUtilities;

public class ToC16Converter
{
	protected boolean transparencyEmulation = false;
	protected boolean bits565 = true;
	protected ToC16Notifee notifee;
	protected BufferedImage[] frames;
	protected byte[][] frameAlphaMaps;
	
	
	
	public void write(File path) throws IOException, FormatMismatchException
	{
		try (CloseableFlushableRandomAccessBytesInterface file = new RandomAccessFileWrapper(new RandomAccessFile(path, "rw")))
		{
			write(file);
		}
	}
	
	public void write(RandomAccessBytes file) throws IOException, FormatMismatchException
	{
		normalizeImages();
		
		if (notifee != null) notifee.startC16Writing();
		DataMap map = new DataMap();
		
		file.setLength(0); //Delete any pre-existing data
		
		calculateHeaderLen(map);
		file.seek(map.headersLength);
		
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
		int flags = 2;
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
			throw new FormatMismatchException("Image is too wide ("+width+") to store in a C16 (max: 65535 or 32767)");
		if (height >= 65536)
			throw new FormatMismatchException("Image is too tall ("+height+") to store in a C16 (max: 65535 or 32767)");
		
		
		
		
		//Row 0 Offset
		Bytes.putLittleInt(file, map.offsets[frameIndex][0]);
		
		//Dimensions
		Bytes.putLittleShort(file, (short)width);
		Bytes.putLittleShort(file, (short)height);
		
		//Other offsets
		for (int row = 1; row < height; row++)
		{
			Bytes.putLittleInt(file, map.offsets[frameIndex][row]);
		}
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
			map.headersLength += 4 * (frames[i].getHeight()-1); //Offsets
		}
	}
	
	
	
	protected void writeImageDatii(RandomAccessBytes file, DataMap map) throws IOException, FormatMismatchException
	{
		map.offsets = new int[frames.length][];
		for (int i = 0; i < frames.length; i++)
		{
			writeImageData(file, i, map);
			if (notifee != null) notifee.finC16WritingFrame(i, frames.length);
		}
		if (notifee != null) notifee.finC16Writing();
	}
	
	protected void writeImageData(RandomAccessBytes file, int frameIndex, DataMap map) throws IOException, FormatMismatchException
	{
		//Find runs of transparent pixels
		map.offsets[frameIndex] = new int[frames[frameIndex].getHeight()];
		for (int y = 0; y < frames[frameIndex].getHeight(); y++)
		{
			writeRow(file, map, frameIndex, y);
		}
		
		//Extra bits
		Bytes.putLittleShort(file, (short)0);
	}
	
	
	
	
	
	protected void writeRow(RandomAccessBytes file, DataMap map, int frameIndex, int y) throws IOException, FormatMismatchException
	{
		map.offsets[frameIndex][y] = (int)file.getFilePointer();
		
		//<Predec
		BufferedImage normalizedImage = frames[frameIndex];
		int width = normalizedImage.getWidth();
		int rowOffset = y * width; //Offset in alphamap and image data
		boolean transparent = false;
		int length = 0;
		//Predec>
		
		//todo: If this breaks, try using the offset provided by the DataBuffer, instead of assuming 0
		short[] rawImageData = ((DataBufferUShort)normalizedImage.getRaster().getDataBuffer()).getData();
		byte[] alphaMap = frameAlphaMaps[frameIndex];
		
		
		//Double-check sizes for overflow
		if (width >= 32768)
			throw new FormatMismatchException("Image is too wide ("+normalizedImage.getWidth()+"x"+normalizedImage.getHeight()+") to store in a C16 (max: 32767x65535)");
		
		
		int x = 0;
		
		while (x < width)
		{
			//Make Run
			
			//Calculate parameters
			//			boolean transparent = false; //already declared, for performance
			//			int length = 0;
			{
				if (alphaMap == null)
				{
					if (isTransparencyEmulation())
					{
						//Transparency emulation mode
						//Pull alpha from alphaMap
						transparent = rawImageData[rowOffset + x] == 0; //Black == Transparent
						
						//Calculate length
						length = 1;
						while (true)
						{
							if (x + length >= width)
								break;
							if ((rawImageData[rowOffset + x + length] == 0) != transparent)
								break;
							else
								length++;
						}
					}
					else
					{
						//All is opaque
						transparent = false;
						length = width;
					}
				}
				else
				{
					//Pull alpha from alphaMap
					transparent = alphaMap[rowOffset + x] >= 0; //0 is transparent, 1-255 is opaque
					
					//Calculate length
					length = 1;
					while (true)
					{
						if (x + length >= width)
							break;
						if ((alphaMap[rowOffset + x + length] >= 0) != transparent)
							break;
						else
							length++;
					}
				}
			}
			
			
			//Write run
			{
				//Header
				{
					short runHeader = (short)(length << 1);
					if (!transparent)
						runHeader |= 1;
					Bytes.putLittleShort(file, runHeader);
				}
				
				if (!transparent)
				{
					//Write Color data
					byte[] split = ArrayUtilities.splitElements16to8LE(rawImageData, rowOffset+x, length);
					file.write(split);
				}
			}
			
			x += length;
		}
		
		//End-Of-Line tag (in practice, superfluous)
		Bytes.putLittleShort(file, (short)0);
	}
	
	
	
	
	
	
	
	
	protected boolean isImageOnRightMode(BufferedImage img)
	{
		return
		(bits565 && img.getType() == BufferedImage.TYPE_USHORT_565_RGB)
		||
		(!bits565 && img.getType() == BufferedImage.TYPE_USHORT_555_RGB);
	}
	
	protected void normalizeImages()
	{
		if (notifee != null) notifee.startC16PreConverting();
		
		BufferedImage[] converted = new BufferedImage[frames.length];
		frameAlphaMaps = new byte[frames.length][];
		
		ColorConvertOp converter = new ColorConvertOp(ColorSpace.getInstance(ColorSpace.CS_sRGB), null);
		
		
		BufferedImage dest = null, src = null;
		for (int i = 0; i < frames.length; i++)
		{
			src = frames[i];
			
			frameAlphaMaps[i] = ImageUtilities.getAlpha(src); //Extract alpha separately, since 565/555 can't hold it
			
			if (isImageOnRightMode(src) && src.getRaster().getDataBuffer().getOffset() == 0)
				dest = src;
			else
			{
				dest = new BufferedImage(src.getWidth(), src.getHeight(), isBits565() ? BufferedImage.TYPE_USHORT_565_RGB : BufferedImage.TYPE_USHORT_555_RGB);
				converter.filter(src, dest);
			}
			
			converted[i] = dest;
			
			if (notifee != null) notifee.finC16PreConvertingFrame(i, frames.length);
		}
		
		this.frames = converted; //Replace old with new
		
		if (notifee != null) notifee.finC16PreConverting();
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
		this.frames = frames;
	}
	
	public boolean isTransparencyEmulation()
	{
		return this.transparencyEmulation;
	}
	
	public void setTransparencyEmulation(boolean transparencyEmulation)
	{
		this.transparencyEmulation = transparencyEmulation;
	}
	
	public ToC16Notifee getNotifee()
	{
		return notifee;
	}
	
	public void setNotifee(ToC16Notifee notifee)
	{
		this.notifee = notifee;
	}
}
