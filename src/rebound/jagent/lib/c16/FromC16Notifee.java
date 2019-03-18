/*
 * Created on Jan 18, 2006
 * 	by the wonderful Eclipse(c)
 */
package rebound.jagent.lib.c16;

public interface FromC16Notifee
{
	public void startC16Reading();
	public void finC16ReadingFrame(int index, int count);
	public void finC16Reading();
	
	public void startC16PostConverting();
	public void finC16PostConvertingFrame(int index, int count);
	public void finC16PostConverting();
}
