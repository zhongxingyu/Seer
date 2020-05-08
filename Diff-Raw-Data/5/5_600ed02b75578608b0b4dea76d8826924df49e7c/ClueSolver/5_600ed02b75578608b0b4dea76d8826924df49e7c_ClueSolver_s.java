 /**
  * 
  */
 package framework;
 
 import java.util.ArrayList;
 
 import exception.NoSolutionsException;
 
 /**
  * @author Ben Griffiths
  *
  */
 public interface ClueSolver {	
 	/**
 	 * getBestSolution - returns the solution for the given clue that has the highest confidence level
 	 * @param clue - the clue whose best solution is to be returned
 	 * @return the solution with the highest confidence level
 	 */
 	public String getBestSolution(Clue clue);
 	/**
 	 * getSolutions - return a list of all the valid solutions found for the given clue
 	 * @param clue - the clue whose solutions are to be returned
 	 * @param proposedSolutions - an ArrayList of Solution objects resulting from a query of the model for an answer to the clue
	 * @return a subset of the list of proposedSolutions, each member of which matches the requirements of the clue
	 * @throws NoSolutionsException - if the proposedSolutions argument is null or empty
 	 */
 	public ArrayList<Solution> getSolutions(Clue clue, ArrayList<Solution> proposedSolutions) throws NoSolutionsException;
 }
