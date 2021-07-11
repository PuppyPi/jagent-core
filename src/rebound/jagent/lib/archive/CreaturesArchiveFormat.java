package rebound.jagent.lib.archive;

import static rebound.io.util.JRECompatIOUtilities.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

/**
 * This is the wrapper "Creatures Zip File" syntax of
 * 		• CREA blocks in exported creatures
 * 		• GLST blocks in exported creatures
 * 		• World save data
 * 		• The "caos.syntax" file
 * 
 * See https://creatures.wiki/CreaturesArchive
 */
public class CreaturesArchiveFormat
{
	/*
	 * Header (79 bytes)
	 * 		"Creatures Evolution Engine - Archived information file. zLib 1.13 compressed."
	 * 		There are also these two bytes after that but before the zlib data 1A 04  (don't ask me what they're for, probably delimiting the text, but I've never seen, say, zLib 1.12 or anything XD so we just treat it as a hard standard for now at least!)
	 * 
	 * Body:
	 * 		zlib compressed data, usually level 6, with zlib wrapping header
	 */
	public static final byte[] HEADER = {(byte)0x43, (byte)0x72, (byte)0x65, (byte)0x61, (byte)0x74, (byte)0x75, (byte)0x72, (byte)0x65, (byte)0x73, (byte)0x20, (byte)0x45, (byte)0x76, (byte)0x6F, (byte)0x6C, (byte)0x75, (byte)0x74, (byte)0x69, (byte)0x6F, (byte)0x6E, (byte)0x20, (byte)0x45, (byte)0x6E, (byte)0x67, (byte)0x69, (byte)0x6E, (byte)0x65, (byte)0x20, (byte)0x2D, (byte)0x20, (byte)0x41, (byte)0x72, (byte)0x63, (byte)0x68, (byte)0x69, (byte)0x76, (byte)0x65, (byte)0x64, (byte)0x20, (byte)0x69, (byte)0x6E, (byte)0x66, (byte)0x6F, (byte)0x72, (byte)0x6D, (byte)0x61, (byte)0x74, (byte)0x69, (byte)0x6F, (byte)0x6E, (byte)0x20, (byte)0x66, (byte)0x69, (byte)0x6C, (byte)0x65, (byte)0x2E, (byte)0x20, (byte)0x7A, (byte)0x4C, (byte)0x69, (byte)0x62, (byte)0x20, (byte)0x31, (byte)0x2E, (byte)0x31, (byte)0x33, (byte)0x20, (byte)0x63, (byte)0x6F, (byte)0x6D, (byte)0x70, (byte)0x72, (byte)0x65, (byte)0x73, (byte)0x73, (byte)0x65, (byte)0x64, (byte)0x2E, (byte)0x1A, (byte)0x04};
	
	
	
	public static final int LEVEL_DEFAULT = 9;
	public static final int LEVEL_CAOS_SYNTAX = 6;
	public static final int LEVEL_WORLD = 5;
	public static final int LEVEL_GLST = 6;
	//CREA is zlib levels 2-5
	
	
	
	
	public static void uncompress(InputStream in, OutputStream out) throws IOException
	{
		byte[] h = new byte[HEADER.length];
		readFully(in, h);
		
		//Zlib data follows this
		InflaterInputStream inflater = new InflaterInputStream(in, new Inflater(false));  //nowrap = false, use the zlib header (as opposed to reading without a header)
		pump(inflater, out);
	}
	
	public static void compress(InputStream in, OutputStream out, int level) throws IOException
	{
		out.write(HEADER);
		
		//Zlib data follows this
		DeflaterOutputStream deflater = new DeflaterOutputStream(out, new Deflater(level, false));  //nowrap = false, use the zlib header (as opposed to reading without a header)
		pump(in, deflater);
		deflater.finish();
		deflater.flush();
	}
}
