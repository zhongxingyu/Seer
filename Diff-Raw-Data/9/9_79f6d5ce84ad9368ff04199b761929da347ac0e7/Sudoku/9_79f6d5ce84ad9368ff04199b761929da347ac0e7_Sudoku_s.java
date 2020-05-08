 import java.util.Scanner;
 
 public class Sudoku {
 	private int[][] sudo;
 	public int rekursions = 0, solutions = 0;
 	public Boolean solvable = new Boolean(false);
 		
 	public Sudoku(int col, int row) {
 		sudo = new int[row][col];
 		}	
 	
 	public void setSudo(int row, int col, int n) {
 		sudo[row][col] = n;
 	}
 	
 	// Rekursionen
 	public void tryToFill(int row, int col, Boolean print) {
 		if (sudo[row][col] == 0) {
 			rekursions++;
 			for (int n = 1; n < 10; n++) {
 				if (isValid(row, col, n)) {
 					setSudo(row, col, n);
 					tryToFillNext(row, col, print);
 					setSudo(row, col, 0);
 				}
 			}
 		}
 		else tryToFillNext(row, col, print);
 	}
 	
 	// take care of wrapping rows
 	public void tryToFillNext(int row, int col, Boolean print) {
 		if (row == 8 && col == 8) {
 			solutions++;
 			if (print)
 				printSudo();
 			solvable = true;
 		}
 		else {
 			if (col == 8)
 				tryToFill(row+1, 0, print);
 			else tryToFill(row, col+1, print);
 		}
 	}
 	
 	public boolean isValid(int row, int col, int n) {
 		if (validRC(row, col, n)==false ||
 			validSq(row, col, n)==false) return false;
 		return true;
 	}
 	
 	// kontrollerer forekomsten af n i row og col
 	public boolean validRC(int row, int col, int n) {
 		for (int i = 0; i < 9; i++)
 			if (getSudo(row, i) == n ||
 				getSudo(i, col) == n) return false;
 		return true;
 	}
 		
 	public boolean validSq(int row, int col, int n) {
 		int sqRow = ((row)/3)*3;
 		int sqCol = ((col)/3)*3;
 		
 		for (int i=sqRow; i < sqRow+3; i++) {
 			for (int j = sqCol; j < sqCol+3; j++) {
 				if (getSudo(i, j)==n) return false;
 			}
 		}
 		return true;
 	}
 	
 	public int getSudo(int row, int col) {
 		return sudo[row][col];
 	}
 	
 	public static String int2string(int i, int felt) {
 		String space20="                    ";  // 20 blanke
 		String s=Integer.toString(i);
 		return space20.substring(0,felt-s.length())+s;
 	}
 	
 	public static String horizontalSep(int cols) {
		String str = "", repString = "   ---------";		
 		for (int i=0; i < cols; i++) {
 			str+= repString;
 		}
 		
 		return str;
 	}
 	
 	public void printSudo() {
 	// System.out.println(horizontalSep(3));
 		for (int i = 0; i < 9; i++) {
 			// System.out.print("  |");
 			for (int j = 0; j < 9; j++) {
 				System.out.print(getSudo(i,j)+" ");
 			}
 			System.out.println("");
 		} // end i-for
 		// System.out.println("");
 	} // end printSudo
 }
 
