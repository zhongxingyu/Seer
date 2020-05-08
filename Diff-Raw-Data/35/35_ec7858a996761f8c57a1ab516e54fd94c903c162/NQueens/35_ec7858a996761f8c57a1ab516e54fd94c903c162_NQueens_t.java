 package guibin.zhang.leetcode.matrix;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 /**
  * 
  * The n-queens puzzle is the problem of placing n queens on an n*n chessboard 
  * such that no two queens attack each other.
  * 
  * Given an integer n, return all distinct solutions to the n-queens puzzle.
  * 
  * Each solution contains a distinct board configuration of the n-queens' placement, 
  * where 'Q' and '.' both indicate a queen and an empty space respectively.
  * 
  * For example,
  * There exist two distinct solutions to the 4-queens puzzle:
  * 
  * [
  *  [".Q..",  // Solution 1
  *   "...Q",
  *   "Q...",
  *   "..Q."],
  * 
  *  ["..Q.",  // Solution 2
  *   "Q...",
  *   "...Q",
  *   ".Q.."]
  * ]
  * 
  * 
  * http://blog.csdn.net/u011095253/article/details/9158473
  * 
  * @author Gubin Zhang <guibin.beijing@gmail.com>
  */
 public class NQueens {
 
     /**
      * 我们把这一题分成几个小问题: 
      * 1. 传统的dfs递归
      * 2. 验证放置Queen的地方是否合法
      * 3. 输出Board结果
      * 
      * 用一个数组对应Board里面的每一行，数组每一个值对应这一行***放置Queen的列号**
      * 比如： int[ ] {3,1,4,2} 代表放置的地点分别为[1,3], [2,1], [3,4], [4,2].
      * 这么一来，我们用很简单的用数组表示了整个Board，而且在isValid函数里判断的时候会非常简洁，
      * 而且把输出Board单独隔离了出来
      * 
      * @param n
      * @return 
      */
     public List<String[]> solveNQueens(int n) {
         // Start typing your Java solution below
         // DO NOT write main() function
         List<String[]> res = new ArrayList<>();
 
         int[] loc = new int[n];
         dfs(res, loc, 0);
         return res;
     }
 
     /**
      * 
      * @param result
      * @param branch
     * @param idx row id
     */
    public void dfs_v2(List<String[]> result, int[] branch, int idx) {
        
        if (idx == branch.length) {
            String[] board = new String[branch.length];
            for (int i = 0; i < board.length; i++) {//i row id
                StringBuilder sb = new StringBuilder();
                for (int j = 0; j < board.length; j++) {//j col id
                    if (j == branch[i]) sb.append("Q");
                    else sb.append(".");
                }
                board[i] = sb.toString();//No. i row is ready
            }
            result.add(board);
            return;
        }
        
        //Try each position on each line of the board
        for (int j = 0; j < branch.length; j++) {//j col id
            branch[idx] = j;//idx is rowid, branch[idx] is co lid
            if (isValid(branch, idx)) {
                dfs(result, branch, idx + 1);
            }
        }
    }
    
    /**
     * 
     * @param result
     * @param branch
      * @param idx The index/(row id) that put queen.
      */
     public void dfs(List<String[]> result, int[] branch, int idx) {
         
         if (idx == branch.length) {
             printBoard(result, branch);
             return;
         }
         
         //Standard dfs to tranverse all the possibilities on each idx.
         for (int i = 0; i < branch.length; i++) {
             branch[idx] = i;//Index is the row id, the value is the col id
             //If existing layout is valid, then keep on dfs to extend neighbour nodes
             if (isValid(branch, idx)) {
                 dfs(result, branch, idx + 1);
             }
         }
     }
 
     /**
      * Check if the branch(board layout) is valid, from row 0 to row idx(exclusive).
      * Valid condition:
      * 1. Not in same row, i != idx, that is it.
      * 2. Not in same column, branch[i] != branch[idx].
      * 3. Not in same diagonal direction, diffOfColIds == diffOfRowIds
      * @param branch
      * @param idx
      * @return 
      */
     public boolean isValid(int[] branch, int idx) {
         
         for (int i = 0; i < idx; i++) {
             //Since i != curr, the row id is not same.
             //loc[i] and loc[curr] are the col id, they should not be same.
             //If they are in the same diagonal direction, the diffOfColIds == diffOfRowIds
             if (branch[i] == branch[idx] || Math.abs(branch[i] - branch[idx]) == idx - i) {
                 return false;
             }
         }
         return true;
     }
 
     /**
      * Convert 1D branch into 2D board.
      * @param result
      * @param branch 
      */
     public void printBoard(List<String[]> result, int[] branch) {
         
         int n = branch.length;
         String[] board = new String[n];
         for (int i = 0; i < n; i++) {//i -> row id
             StringBuilder row = new StringBuilder();
             for (int j = 0; j < n; j++) {
                 if (j == branch[i]) {//loc[i] -> colid, loc[i] == j is to find the col id
                     row.append("Q");
                 } else {
                     row.append(".");
                 }
             }
            board[i] = row.toString();
         }
         result.add(board);
     }
     
     public static void main(String[] args) {
         NQueens nq = new NQueens();
         int i = 1;
         for(String[] as : nq.solveNQueens(5)) {
             System.out.println("-----" + (i++) + "-----");
             Arrays.stream(as).forEach(s -> System.out.println(s));
         }
     }
 }
