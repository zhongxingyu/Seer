 package guibin.zhang.leetcode.permutationAndCombination;
 
 import java.util.ArrayList;
import java.util.Arrays;
 
 /**
  * Given a set of distinct integers, S, return all possible subsets.
  * 
  * Note:
  * 
  * Elements in a subset must be in non-descending order.
  * The solution set must not contain duplicate subsets.
  * For example,
  * If S = [1,2,3], a solution is:
  * 
  * [
  *   [3],
  *   [1],
  *   [2],
  *   [1,2,3],
  *   [1,3],
  *   [2,3],
  *   [1,2],
  *   []
  * ]
  * 
  * @author Gubin Zhang <guibin.beijing@gmail.com>
  */
 public class Subsets {
     
     public ArrayList<ArrayList<Integer>> subsets(int[] S) {
         
         Arrays.sort(S);
         ArrayList<ArrayList<Integer>> result = new ArrayList<ArrayList<Integer>>();
         result.add(new ArrayList<Integer>());
         //Select each item from S
         for(int i = 0; i < S.length; i ++) {
             int size = result.size();
             //Pick up the existing result, add the item to it.
             for(int j = 0; j < size; j++) {
                 ArrayList<Integer> subset = new ArrayList<Integer>(result.get(j));
                 subset.add(S[i]);
                 result.add(subset);
             }
         }
         return result;
     }
     
     /**
      * http://blog.csdn.net/u011095253/article/details/9158397
      * http://discuss.leetcode.com/questions/253/subsets
      * @param S
      * @return 
      */
     public ArrayList<ArrayList<Integer>> subsets_v2(int[] S) {
         
         Arrays.sort(S);
         ArrayList<ArrayList<Integer>> result = new ArrayList<ArrayList<Integer>>();
         ArrayList<Integer> tmp = new ArrayList<Integer>();
         result.add(tmp);
         dfs(result, tmp, S, 0);
         return result;
     }
     
     public void dfs(ArrayList<ArrayList<Integer>> res, ArrayList<Integer> tmp, int[] S, int pos) {
         for(int i = pos; i < S.length; i ++) {
             tmp.add(S[i]);
             res.add(new ArrayList<Integer>(tmp));
             dfs(res, tmp, S, i + 1);
             tmp.remove(tmp.size() - 1);
         }
     }
     
     public static void main(String[] args) {
         Subsets s = new Subsets();
         int[] set = {1, 2, 3};
         ArrayList<ArrayList<Integer>> result = s.subsets_v2(set);
         System.out.println("size=" + result.size());
         for(ArrayList<Integer> row : result) {
             for(int i : row) {
                 System.out.print(i + ",");
             }
             System.out.println();
         }
     }
 }
