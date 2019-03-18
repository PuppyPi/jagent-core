/*
 * Created on Jan 5, 2006
 * 	by the wonderful Eclipse(c)
 */
package rebound.jagent.lib.s16;

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
	int[] offsets, widths, heights;
	
	int headersLength;
}
