/*
 * Created on Jan 5, 2006
 * 	by the wonderful Eclipse(c)
 */
package rebound.jagent.lib.c16;

public interface ToC16Notifee
{
	public void startC16PreConverting();
	public void finC16PreConvertingFrame(int index, int count);
	public void finC16PreConverting();
	
	public void startC16Writing();
	public void finC16WritingFrame(int index, int count);
	public void finC16Writing();
}
