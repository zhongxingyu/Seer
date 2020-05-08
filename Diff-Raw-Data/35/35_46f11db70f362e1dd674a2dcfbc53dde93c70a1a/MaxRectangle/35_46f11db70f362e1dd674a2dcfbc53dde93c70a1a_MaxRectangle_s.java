 /*
  * Given a 2D binary matrix filled with 0's and 1's, find the
  *  largest rectangle containing all ones and return its area.
  */
 import java.util.Stack;
 public class MaxRectangle {
     
     // Solution for  O(N^4)
     private int maxRectArea(char[][] matrix, int row, int col) {
         int area = 1;
         int minWidth = Integer.MAX_VALUE;
         for (int i = row; i < matrix.length && matrix[i][col] == '1'; i++) {
             int width = 1;
             while (width < minWidth && width+col < matrix[i].length
                     &&  matrix[i][col+width] == '1')
                 width++;
             
             minWidth = Math.min(minWidth, width);
             area = Math.max(area, (i-row+1)*minWidth);
         }
 
         return area;
     }
     
     public int maximalRectangle(char[][] matrix) {
         if (matrix == null || matrix.length == 0 || matrix[0].length == 0)
             return 0;
 
         int max = 0;
         
         for (int i = 0; i < matrix.length; i++) {
             for (int j = 0; j < matrix[i].length; j++) {
                 if (matrix[i][j] == '1') {
                     max = Math.max(max, maxRectArea(matrix, i, j));
                 }
             }
         }
         
         return max;
         
     }
     
     private int maxHistogram(int[] ht) {
         Stack<Integer> stack = new Stack<Integer>();
         int idx = 0;
         int max = 0;
         int size = ht.length;
         while (idx < size) {
             if (stack.isEmpty() || ht[idx] >= ht[stack.peek()]) {
                 stack.push(idx);
                 idx++;
             } else {
                 int lastIdx = stack.pop();
                 int area;
                 if (stack.isEmpty())
                     area = idx*ht[lastIdx];
                 else
                     area = (idx-stack.peek()-1)*ht[lastIdx];
                 max = Math.max(max, area);
             }
         }
         return max;
     }
     
     // Solution for  O(N^3)
     public int maximalRectangle2(char[][] matrix) {
         if (matrix == null || matrix.length == 0 || matrix[0].length == 0)
             return 0;
 
         int max = 0;
         int row = matrix.length;
         int col = matrix[0].length;
         int[][] height = new int[row][col+1];
         
         int i, j;
         
         for (i = 0; i < row; i++) {
             for (j = 0; j < col; j++) {
                 if (matrix[i][j] == '0')
                     height[i][j] = 0;
                 else {
                     if (i == 0)
                         height[i][j] = 1;
                     else
                         height[i][j] = height[i-1][j] + 1;
                 }
                     
             }
             height[i][col] = 0;
             max = Math.max(max, maxHistogram(height[i]));
         }
 
         
         return max;
     }
     
     // Solution for  O(N^2)
     public int maximalRectangle3(char[][] matrix) {
         if (matrix == null || matrix.length == 0 || matrix[0].length == 0)
             return 0;   
         int max = 0;
         int row = matrix.length;
         int col = matrix[0].length;
         
         int i, j;
         
         int[] height = new int[col]; 
         int[] left = new int[col];
         int[] right = new int[col];
         
         for (i = 0; i < col; i++) {
             height[i] = 0;
             left[i] = 0;
             right[i] = col;
         }
         
         for (i = 0; i < row; i++) {
             int lastLeft = 0;
             int lastRight = col;            
             for (j = 0; j < col; j++) {
                 if (matrix[i][j] == '1') {
                    height[i]++;
                    left[i] = Math.max(left[i], lastLeft);
                 } else {
                    lastLeft = i;
                    height[i] = 0;
                    left[i] = 0;
                    right[i] = col;
                 }
                 
             }
             for (j = col-1; j >= 0; j--) {
                 if (matrix[i][j] == '1') {
                    right[i] = Math.min(right[i], lastRight);
                    max = Math.max(max, height[i]*(right[i]-left[i]-1));
                 } else {
                     lastRight = j;
                 }
             }
         }
         
         
         
         return max;
     }
 }
