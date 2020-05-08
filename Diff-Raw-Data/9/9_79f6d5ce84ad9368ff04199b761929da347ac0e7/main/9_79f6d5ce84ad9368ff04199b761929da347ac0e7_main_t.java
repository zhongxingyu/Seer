 import java.util.Scanner;
 
 public class main {
 	
     public static void main (String args[]) {
 		Scanner sudoScan = new Scanner(System.in);
 		Sudoku s = new Sudoku(9,9);
 		
		// Indlaoeiser fra input
 		//System.out.println("Loading SuDoku from file...");
 		for (int i = 0; i < 9; i++) {
 			for (int j = 0; j < 9; j++) {
 				s.setSudo(i, j, sudoScan.nextInt());
 			}
 		}
 		s.printSudo();
 		System.out.println("");
 		
 		s.tryToFill(0, 0, true);
 		// if (s.solvable) {
 		// 	// System.out.print("\n  Total number of solutions  : ");
 		// 	// System.out.println(s.int2string(s.solutions, 8));
 		// 	// System.out.print("  Total number of rekursions : ");
 		// 	// System.out.println(s.int2string(s.rekursions, 8));
 		// }
 		// else {
 		// 	System.out.println("\nGiven SuDoku is unsolvable.");
 		// 	System.out.print("	- Pleas restart and load another SuDoku...\n");
 		// }
     }
 }
