/*
 * Created on Jan 5, 2006
 * 	by the wonderful Eclipse(c)
 */
package rebound.jagent.lib.blk;

public interface ToBLKNotifee
{
	public void startBLKfPreConverting();
	public void finBLKPreConverting();
	
	public void startBLKWriting();
	public void finBLKWritingBlock(int index);
	public void finBLKWriting();
}
