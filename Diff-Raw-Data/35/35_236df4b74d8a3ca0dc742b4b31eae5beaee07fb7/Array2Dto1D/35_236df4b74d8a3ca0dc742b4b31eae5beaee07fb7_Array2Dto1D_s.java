 package careercup;
 
 import java.util.Arrays;
 
 /**
  * Created by Sobercheg on 10/20/13.
  * <p/>
  * <a href="http://www.careercup.com/question?id=5678547255427072">Question</a>
  * <p/>
  * Q: Given a sorted 2D N x N array (where array[i][j] < array[i][j+1] and array[i][j] < array[i+1][j]),
  * can you write a function that converts this to a sorted 1D array?
  */
 public class Array2Dto1D {
 
    private static int array1DIndex = 0;

     public static int[] convert(int[][] array2D) {
        array1DIndex = 0;
         int[] columnIndex = new int[array2D.length];
         int[] output = new int[array2D.length * array2D[0].length];
//        output[0] = array2D[0][0];
        convertInternal(array2D, columnIndex, 0, output, Integer.MAX_VALUE);
         return output;
     }
 
    private static void convertInternal(int[][] array2D, int[] rowIndex, int column, int[] array1D, int limit) {
        if (column >= array2D.length) return;
        if (rowIndex[column] >= array2D[column].length) return;
 
         for (int row = rowIndex[column]; row < array2D[column].length; row++) {
             int value = array2D[column][row];
            boolean switchColumn = column + 1 < array2D.length && value > array2D[column + 1][rowIndex[column + 1]];
            if (switchColumn) {
                convertInternal(array2D, rowIndex, column + 1, array1D, value);
             }
            if (value > limit) return;
             rowIndex[column] = row + 1;
            array1D[array1DIndex] = value;
            array1DIndex++;
         }
        convertInternal(array2D, rowIndex, column + 1, array1D, Integer.MAX_VALUE);
     }
 
     public static void main(String[] args) {
         int[][] A = new int[][]{
                 {1, 2, 3, 300, 301},
                 {100, 101, 102, 400, 401},
                 {200, 500, 501, 502, 503},
         };
         int[] output = convert(A);
         int[] expected = {1, 2, 3, 100, 101, 102, 200, 300, 301, 400, 401, 500, 501, 502, 503};
         compare(output, expected);
 
         A = new int[][]{{1}};
         output = convert(A);
         expected = new int[]{1};
         compare(output, expected);
 
     }
 
     private static void compare(int[] output, int[] expected) {
         System.out.println(Arrays.toString(output));
         System.out.println("As expected: " + Arrays.equals(expected, output));
     }
 
 }
