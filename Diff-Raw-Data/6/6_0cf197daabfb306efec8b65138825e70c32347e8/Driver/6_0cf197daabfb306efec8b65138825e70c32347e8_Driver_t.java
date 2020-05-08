 package driver;
 
 import java.util.*;
 
 
 /*
  * A driver and a primitive GUI that displays the solution.
  * The driver has three parts: creating the puzzle, solving the puzzle
  * and displaying the solution.
 */
 
 public class Driver {
 
     public static void main(String[] args) throws InstantiationException, ClassNotFoundException, IllegalAccessException {
 
     	// Creates the concrete puzzle, RushHour, creates a SequentialPuzzleSolver
     	// and initializes the SequentialPuzzleSolver with RushHour.
     	
     	//framework.Puzzle thePuzzle = new rushhour.RushHour();
         //framework.SequentialPuzzleSolver theSolver = new framework.SequentialPuzzleSolver(thePuzzle);                    
         
     	// YOUR CODE HERE: there are three additional configurations of the puzzle
         // Configuration 1, shown below, initializes the sequential solver with the other puzzle,
     	// CheckersSolitaire
     	
     	// framework.Puzzle thePuzzle = new checkerssolitaire.CheckersSolitaire();
     	// framework.SequentialPuzzleSolver theSolver = new framework.SequentialPuzzleSolver(thePuzzle);
   
     	// Configuration 2, below, initializes the concurrent solver with RushHour
     	 framework.Puzzle thePuzzle1 = new rushhour.RushHour();
     	 framework.ConcurrentPuzzleSolver theSolver1 = new framework.ConcurrentPuzzleSolver(thePuzzle1);  	
  
     	// Configuration 3, below, initializes the concurrent solver with CheckersSolitaire
     	// framework.Puzzle thePuzzle = new checkerssolitaire.CheckersSolitaire();
     	// framework.ConcurrentPuzzleSolver theSolver = new framework.ConcurrentPuzzleSolver(thePuzzle);  	
     	
     	// DO NOT MODIFY ANYTHING BEYOND THIS LINE 
       	
     	
     	// Solves the puzzle and draws the solution
     	LinkedList theList = theSolver1.solve();
    	if (theList == null) {
    		System.out.println("No Solution Found.");
    	} else {
    		thePuzzle1.drawSolution(theList);
    	}
                 
     }
      
 }
