 import java.util.List;
 
 /**
  * from Lab #4 CS 2334, Section 0?? September 26, 2013
  * <P>
  * This class implements a binary search method so you can modify it
  * to count the number of comparisons for Project 2.
  * </P>
  * 
 * @author
  * @version 1.0
  */
 public class Searcher {
 
 	
 	int count = 0;
 	/**
 	 * This performs a binary search for a key in an list.
 	 * 
 	 * The only requirement is that the type T inside the list must 
 	 * implement the Comparable<T> interface.
 	 * 
 	 * @param list
 	 * @param key
 	 * @return
 	 *      Returns -1 if the key is not found in the list and an
 	 *      index between [0, ..., list.size()] if the key is found.
 	 */
 	public <T extends Comparable<T>> int binarySearch(List<T> list, T key) {
 		int left  = 0;
 		int right = list.size() - 1;
 		
 		count = 1;
 		
 		// While there are elements in the range [left,.., right].
 		while( right - left + 1 > 0) {
 			// Pick the middle point of the range [left, ... , right].
 			
 			int middleIndex = ( left + right ) / 2;
 			T middleElement = list.get(middleIndex);
 			int comparisonValue = middleElement.compareTo(key);
 			
 			if (comparisonValue < 0) {
 				left = middleIndex + 1;
 			}
 			else if (comparisonValue > 0) {
 				right = middleIndex - 1;
 			}
 			else {
 				return middleIndex;
 			}
 			count++;
 		}
 		
 		// If the element was not found.
 		return -1;	
 	}
 }
