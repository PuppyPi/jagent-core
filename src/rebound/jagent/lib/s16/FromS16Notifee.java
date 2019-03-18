/*
 * Created on Jan 18, 2006
 * 	by the wonderful Eclipse(c)
 */
package rebound.jagent.lib.s16;

public interface FromS16Notifee
{
	public void startS16Reading();
	public void finS16ReadingFrame(int index, int count);
	public void finS16Reading();
	
	public void startS16PostConverting();
	public void finS16PostConvertingFrame(int index, int count);
	public void finS16PostConverting();
}
