/*
 * Created on Jan 18, 2006
 * 	by the wonderful Eclipse(c)
 */
package rebound.jagent.lib.blk;

public interface FromBLKNotifee
{
	public void startBLKReading();
	public void finBLKReadingBlock(int index);
	public void finBLKReading();
	
	public void startBLKPostConverting();
	public void finBLKPostConverting();
}
