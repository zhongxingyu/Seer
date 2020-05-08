 /*12:06*/
 
 public class Solution {
 
     public boolean searchMatrix(int[][] matrix, int target) {
         if (matrix == null || matrix.length == 0 || matrix[0].length == 0)
             return false;    
 
         int start = 0;
         int end = matrix.length-1;
 
         while(start <= end){
            int mid = start+ ((end-start)>>1);
             if(matrix[mid][0] < target){
                 start = mid+1;
             }else if(matrix[mid][0] > target){
                 end = mid-1;
             }else{
                 return true; 
             }
         }
         int row = end;
         if(row<0)
             return false;
         start = 0;
         end = matrix[end].length-1;
         while(start <=end){
            int mid = start+ ((end-start)>>1);
             if(matrix[row][mid] < target){
                 start = mid+1;
             }else if(matrix[row][mid] > target){
                 end = mid-1;
             }else{
                 return true; 
             }
         }
         return false;
     }
 }
