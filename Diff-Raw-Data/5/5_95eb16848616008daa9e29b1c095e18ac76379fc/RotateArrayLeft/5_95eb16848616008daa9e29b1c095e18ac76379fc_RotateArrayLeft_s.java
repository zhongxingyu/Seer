 /**
  * This program rotates all of the elements in an array left by a given k
 * value.  It runs in O(n) time and uses O(1) additional space (it operates
  * in-place).
  *
  * @author      John Kurlak <john@kurlak.com>
  * @date        1/20/2013
  */
 public class RotateArrayLeft {
     /**
      * Runs the program with an example array.
      *
      * @param args      The command-line arguments.
      */
     public static void main(String[] args) {
         int[] array = new int[] { 5, 3, 2, 18, 20 };
         int k = 2;
 
         printArray(array);
         System.out.println("rotated to the left " + k + " is:");
         rotateArrayLeft(array, k);
         printArray(array);
     }
 
     /**
      * Rotates all of the elements in an array left by the given k value.
      * If k is negative, it rotates the elements in the array right.
      * This method modifies the array in place, so it does not return
      * anything.
      *
      * @param array The array to shift.
      * @param k     The number of indices by which to shift the array.
      */
     public static void rotateArrayLeft(int[] array, int k) {
         if (array == null) {
             return;
         }
 
         int n = array.length;
 
         // Handle negative k values (rotate to right)
         if (k < 0) {
             k = n - Math.abs(k);
         }
 
         // Ensure k is in interval [0, n)
        k %= n;
 
         // Rotate the array
         reverseArray(array, 0, k - 1);
         reverseArray(array, k, n - 1);
         reverseArray(array, 0, n - 1);
     }
 
     /**
      * Reverses all values in an array from the given start index to the given end index.
      * The reversal happens in-place, so there is no return value.
      *
      * @param array         The array to reverse.
      * @param startIndex    The start index of the subarray to reverse.
      * @param endIndex      The end index of the subarray to reverse.
      */
     public static void reverseArray(int[] array, int startIndex, int endIndex) {
         for (int leftIndex = startIndex, rightIndex = endIndex; leftIndex < rightIndex; leftIndex++, rightIndex--) {
             swap(array, leftIndex, rightIndex);
         }
     }
 
     /**
      * Swaps two elements in a list.
      *
      * @param list      The list.
      * @param index1    The index of the first element to swap.
      * @param index2    The index of the second element to swap.
      */
     public static void swap(int[] list, int index1, int index2) {
         int temp = list[index1];
         list[index1] = list[index2];
         list[index2] = temp;
     }
 
     /**
      * Prints out the contents of an array.
      *
      * @param array     The array.
      */
     public static void printArray(int[] array) {
         System.out.print("{ ");
 
         if (array == null) {
             System.out.println("}");
             return;
         }
 
         for (int i = 0; i < array.length; i++) {
             String separator = (i == (array.length - 1)) ? " " : ", ";
             System.out.print(array[i] + separator);
         }
 
         System.out.println("}");
     }
 }
