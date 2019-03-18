/*
 * Created on Jan 5, 2006
 * 	by the wonderful Eclipse(c)
 */
package rebound.jagent.lib.blk;

class DataMap
{
	/*
	 * Offsets start at beginning of data
	 * 
	 * Row #0 is really 
	 * 
	 * Dimensions are assigned as follows:
	 * 	offsets[frameIndex][rowIndex]
	 */
	int[] offsets;
	
	int headersLength;
}
