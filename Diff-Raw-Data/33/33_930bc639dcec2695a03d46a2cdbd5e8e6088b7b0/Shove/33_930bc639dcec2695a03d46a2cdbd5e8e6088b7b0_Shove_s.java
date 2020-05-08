 package shove;
 public class Shove {
       
       /** Move A[A.length-1] so that it is just after the nearest 
         *  previous item that is <= A[A.length-1], or to A[0] if 
         *  there isn't such an item. Move all succeeding items 
         *  to the right (i.e., up one index) . */
 
 	 static void moveOver(int[] A) {
 	     int current = A.length - 1;
	     while (A[current] > A[current-1] && current > 0){
 		 int tmp = A[current-1];
 		 A[current-1] = A[current];
		 A[current-1] = tmp;
 		 current--;
 	     }
 	 }
    /** The final position of ELT in B. */
    static int position(int[] B, int elt) {
	return 0;
    }
 }
