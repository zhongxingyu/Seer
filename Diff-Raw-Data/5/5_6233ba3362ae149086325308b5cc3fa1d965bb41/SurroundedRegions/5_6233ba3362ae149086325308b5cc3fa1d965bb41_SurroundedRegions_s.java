 /**
  * Given a 2D board containing 'X' and 'O', capture all regions surrounded by
  * 'X'.
  * A region is captured by flipping all 'O's into 'X's in that surrounded region
  * .
  * For example,
  * X X X X
  * X O O X
  * X X O X
  * X O X X
  * After running your function, the board should be:
  * X X X X
  * X X X X
  * X X X X
  * X O X X
  */
 
 public class SurroundedRegions {
 	public void solve(char[][] board) {
 		// Start typing your Java solution below
 		// DO NOT write main() function
 		if (board.length == 0) {
 			return;
 		}
 		boolean isAlive[][] = new boolean[board.length][board[0].length];
 
 		for (int i = 1; i < board[0].length - 1; i++) {
 			dfs(0, i, isAlive, board);
 			dfs(board.length - 1, i, isAlive, board);
 		}
 		
 		for (int i = 1; i < board.length - 1; i++) {
 			dfs(i, 0, isAlive, board);
			dfs(i, board.length - 1, isAlive, board);
 		}
 
 		for (int i = 1; i < board.length - 1; i++) {
			for (int j = 1; j < board.length - 1; j++) {
 				if (!isAlive[i][j]) {
 					board[i][j] = 'X';
 				}
 			}
 		}
 	}
 
 	public void dfs(int row, int col, boolean[][] isAlive, char[][] board) {
 		if (board[row][col] == 'X' || isAlive[row][col]) {
 			return;
 		} else if (board[row][col] == 'O') {
 			isAlive[row][col] = true;
 		}
 
 		if (col - 1 > 0) {
 			dfs(row, col - 1, isAlive, board);
 		}
 		if (col + 1 < isAlive[0].length - 1) {
 			dfs(row, col + 1, isAlive, board);
 		}
 		if (row - 1 > 0) {
 			dfs(row - 1, col, isAlive, board);
 		}
 		if (row + 1 < isAlive.length - 1) {
 			dfs(row + 1, col, isAlive, board);
 		}
 
 	}
 }
