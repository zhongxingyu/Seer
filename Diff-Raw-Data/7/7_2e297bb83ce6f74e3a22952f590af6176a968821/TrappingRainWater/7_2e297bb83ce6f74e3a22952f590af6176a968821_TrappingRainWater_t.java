 package guibin.zhang.leecode.listAndArray;
 
 import java.util.Stack;
 
 /**
  * 
  * Given n non-negative integers representing an elevation map where the width of each bar is 1, 
  * compute how much water it is able to trap after raining.
  * 
  * For example, 
  * Given [0,1,0,2,1,0,1,3,2,1,2,1], return 6.
  * 
  * The above elevation map is represented by array [0,1,0,2,1,0,1,3,2,1,2,1]. 
  * In this case, 6 units of rain water (blue section) are being trapped. 
  * 
  * This problem is similar with ContainerWithMostWater and LagestRectangleInHistogram
  * Prefer to the method **trap_v3**, which is very similar with LargestRectangleInHistogram
  * 
  * 
  * http://n00tc0d3r.blogspot.com/2013/06/trapping-rain-water.html
  * 
  * @author Gubin Zhang <guibin.beijing@gmail.com>
  */
 public class TrappingRainWater {
     
     /**
      * 
      * http://yucoding.blogspot.com/2013/05/leetcode-question-111-trapping-rain.html
      * 
      * 对于任何一个坐标，检查其左右的最大坐标，然后相减就是容积。所以，
      * 1. 从左往右扫描一遍，对于每一个坐标，求取左边最大值。
      * 2. 从右往左扫描一遍，对于每一个坐标，求最大右值。
      * 3. 再扫描一遍，求取容积并加和。
      *    #2和#3可以合并成一个循环，
      * 
      * @param A
      * @return 
      */
     public int trap(int[] A) {
         // Start typing your Java solution below
         // DO NOT write main() function
         int len = A.length;
         int[] maxL = new int[len];
         int[] maxR = new int[len];
         for (int i = 0; i < len; i++) {
             maxL[i] = i == 0 ? A[i] : Math.max(maxL[i - 1], A[i]);
         }
         for (int i = len - 1; i >= 0; i--) {
             maxR[i] = i == len - 1 ? A[i] : Math.max(maxR[i + 1], A[i]);
         }
 
         int allSum = 0;
         for (int i = 1; i < len - 1; i++) {
             allSum += A[i];
         }
         int waterSum = 0;
         for (int i = 1; i < len - 1; i++) {
             waterSum += Math.min(maxL[i], maxR[i]);
         }
         return waterSum - allSum;
     }
     
     /**
      * Merge the last three loops into the second loop.
      * @param A
      * @return 
      */
     public int trap_v2(int[] A) {
         // Start typing your Java solution below
         // DO NOT write main() function
         int len = A.length;
         int[] maxL = new int[len];
         int[] maxR = new int[len];
         int allSum = 0;
         int waterSum = 0;
 
         for (int i = 0; i < len; i++) {
             maxL[i] = i == 0 ? A[i] : Math.max(maxL[i - 1], A[i]);
         }
         for (int i = len - 1; i >= 0; i--) {
             maxR[i] = i == len - 1 ? A[i] : Math.max(maxR[i + 1], A[i]);
             allSum += A[i];
             waterSum += Math.min(maxL[i], maxR[i]);
         }
 
         return waterSum - allSum;
     }
     
     
     public int trap_v3(int[] A) {
         
         int vol = 0;
         int len = A.length;
         int curr = 0;
         //Skip zeros
         while (curr < len && A[curr] == 0) curr++;
         
         Stack<Integer> indices = new Stack<Integer>();
        for (; curr < len; curr ++) {
             while (!indices.isEmpty() && A[curr] >= A[indices.peek()]) {
                 int b = indices.pop();
                 if (indices.isEmpty()) break;
                 //这里 -1 的原因是 curr 距离 indices.peek() 中间隔了一个较小的 indices.pop.
                 //A[b] 是较小的 indices.pop 的高度
                 //curr - indices.peek() - 1 is the bottom.
                 //(Math.min - A[b]) is the delta height that can contain the water.
                vol += (curr - indices.peek() - 1) * (Math.min(A[curr], A[indices.peek()]) - A[b]);
             }
             indices.push(curr);
         }
         
         return vol;
     }
     
     /**
      * This method is not correct.
      * Although you find the inflection point， 
      * if there are higher columns on both sides, 
      * the correct water area should be lager. For example:
      *               g
      * a            /
      *  \        e /
      *   \  c  /\ /
      *    \/ \/  f 
      *    b   d  
      * The correct water level is a, not c or e. 
      * So the method to find the inflection point is incorrect.
      * 
      * @param A
      * @return 
      */
     public int trap_error(int[] A) {
         // Start typing your Java solution below
         // DO NOT write main() function
         int len = A.length;
         if (len <= 2) {
             return 0;
         }
         
         //points is used to record inflection point of the curve.
         int[] points = new int[len];
         int p = 0;
         points[0] = p;
         for (int i = 1; i < len - 1; i++) {
             if (A[i - 1] > A[i] && A[i] <= A[i + 1]) {
                 p++;
                 points[p] = i;
             } else if (A[i - 1] >= A[i] && A[i] < A[i + 1]) {
                 p++;
                 points[p] = i;
             } else if (A[i - 1] < A[i] && A[i] >= A[i + 1]) {
                 p++;
                 points[p] = i;
             } else if (A[i - 1] <= A[i] && A[i] > A[i + 1]) {
                 p++;
                 points[p] = i;
             }
             /**
              * Remove the duplicated ones, in the following case
              * \
              *  \    /
              *   ---
              */
             if (p > 0 && A[points[p]] == A[points[p - 1]]) {
                 p--;
             }
         }
 
         if (A[points[p]] < A[len - 1]) {
             p++;
             points[p] = len - 1;
         }
 
         int sum = 0;
         if (p <= 1) {
             return 0;
         }
 
         for (int i = 1; i < p; i++) {
             if (A[points[i - 1]] > A[points[i]] && A[points[i]] < A[points[i + 1]]) {
                 int top = Math.min(A[points[i - 1]], A[points[i + 1]]);
                 for (int j = points[i - 1] + 1; j < points[i + 1]; j++) {
                     if (A[j] < top) {
                         sum += top - A[j];
                     }
                 }
             }
         }
         return sum;
     }
     
     public static void main(String[] args) {
         TrappingRainWater tr = new TrappingRainWater();
         int[] a = {4,2,0,3,2,5};
         System.out.println(tr.trap(a));
     }
 }
