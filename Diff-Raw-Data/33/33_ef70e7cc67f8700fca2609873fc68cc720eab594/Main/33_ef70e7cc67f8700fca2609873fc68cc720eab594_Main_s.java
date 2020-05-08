 /*
  * Main
  * 
  * Version 0.1
  * 
  * 
  * Gnerall conventions:
  * - All class constants desricptiveName
  * - All input parameters pDesricptiveName
  * - All local variables lDescriptiveName
  * - "GameState" is used equivalent to "Node"; one GameState is expanded, has parent and successors... 
  * 
  */
 
 import java.io.*;
 
 public class Main {
 
 	/**
 	 * @param args
 	 * 
 	 */
 	
 	public static boolean debugMode = true;
 	
 	public static void main(String[] args) throws IOException {
 
 		 final Client client = new Client();
 		 final Board board = client.getBoardFromFile();
 		 final Visualizer visual = new Visualizer();
 		 
 		 Solver solver = new Solver(); //Reaches info incl. initial state from Map staticaly.

 		 //System.out.println(solver.solutionPath());
 		 
 		 //Board.printGoalGrad(1);
 
		 Visualizer.printOriginalMap(0);
		 Visualizer.printCleanMap(0);
		 Visualizer.printGoalGrad(1);
 		 
 	} // main
 } // End Main
