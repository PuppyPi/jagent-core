/*
 * Created on Jan 5, 2006
 * 	by the wonderful Eclipse(c)
 */
package rebound.jagent.lib.s16;

public interface ToS16Notifee
{
	public void startS16PreConverting();
	public void finS16PreConvertingFrame(int index, int count);
	public void finS16PreConverting();
	
	public void startS16Writing();
	public void finS16WritingFrame(int index, int count);
	public void finS16Writing();
}
