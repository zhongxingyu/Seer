 import java.util.Comparator;
 
 /**
  * 
  */
 
 /**
  * @author steve
  *
  */
 public class MinComparator implements Comparator<Move> {
 
 	/* (non-Javadoc)
 	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
 	 */
 	@Override
 	public int compare(Move m1, Move m2) {
 		if(m1.isWin && m2.isWin && m1.score > 1 && m2.score > 1) {
			//Red win, so we want the highest depth
			//but we are selecting MIN
 			return m2.depth - m1.depth;
 		} else if(m1.score != m2.score) {
 			return m1.score > m2.score ? 1 : -1;
 		} else {
 			//If all things are equal, take the closest
			return m1.depth - m2.depth;
 		}
 	}
 
 }
