 package guibin.zhang.leetcode.permutationAndCombination;
 
 import java.util.ArrayList;
import java.util.Arrays;
 
 /**
  *  Given a collection of integers that might contain duplicates, S, return all possible subsets.
  *  
  *  Note:
  *  
  *  Elements in a subset must be in non-descending order.
  *  The solution set must not contain duplicate subsets.
  *  For example,
  *  If S = [1,2,2], a solution is:
  *  
  *  [
  *    [2],
  *    [1],
  *    [1,2,2],
  *    [2,2],
  *    [1,2],
  *    []
  *  ]
  * 
  * @author Gubin Zhang <guibin.beijing@gmail.com>
  */
 public class SubsetsII {
     
     /**
      * http://blog.csdn.net/zyfo2/article/details/8897695
      * @param num
      * @return 
      */
     public ArrayList<ArrayList<Integer>> subsetsWithDup(int[] num) {
         Arrays.sort(num);
         ArrayList<ArrayList<Integer>> result = new ArrayList<ArrayList<Integer>>();
         result.add(new ArrayList<Integer>());
         
         int start = 0;
         for(int i = 0; i < num.length; i ++) {
             int size = result.size();
             for(int j = start; j < size; j ++) {
                 ArrayList<Integer> subset = new ArrayList<Integer>(result.get(j));
                 subset.add(num[i]);
                 result.add(subset);
             }
             if(i < num.length - 1 && num[i] == num[i + 1]) {
                 start = size;
             } else {
                 start = 0;
             }
         }
         
         return result;
     }
     
     public ArrayList<ArrayList<Integer>> subsetsWithDup_v2(int[] num) {
         Arrays.sort(num);
         ArrayList<ArrayList<Integer>> result = new ArrayList<ArrayList<Integer>>();
         ArrayList<Integer> tmp = new ArrayList<Integer>();
         result.add(tmp);
         dfs(result, new ArrayList<Integer>(), num, 0);
         return result;
     }
     
     public void dfs(ArrayList<ArrayList<Integer>> result, ArrayList<Integer> tmp, int[] num, int pos) {
         for(int i = pos; i < num.length; i++) {
             if(i > pos && num[i - 1] == num[i]) {
                 continue;
             }
             tmp.add(num[i]);
             result.add(new ArrayList<Integer>(tmp));
             dfs(result, tmp, num, i + 1);
             tmp.remove(tmp.size() - 1);
         }
     }
     
     public static void main(String[] args) {
         SubsetsII sII = new SubsetsII();
         int[] num = {1, 2, 2, 2};
         
         ArrayList<ArrayList<Integer>> result = sII.subsetsWithDup(num);
         System.out.println("size=" + result.size());
         for(ArrayList<Integer> row : result) {
             for(int i : row) {
                 System.out.print(i + ",");
             }
             System.out.println();
         }
     }
 }
