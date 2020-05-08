 /**
  * Removes any values that can be whilst still maintaining a unique solution
  * @author laura
  *
  */
 public class ExhaustiveRemover implements Removalist {
 	
 	/**
 	 * Constructor for ExhaustiveRemover
 	 * @param b board to remove from
 	 */
 	public ExhaustiveRemover (Board b){
 		this.b = b;
 		checker = new SudokuSolver();
 		if (b.isDifficultyEasy()){
 			maxRemoved = REMOVE_ON_EASY;
 		} else if (b.isDifficultyMedium()){
 			maxRemoved = REMOVE_ON_MEDIUM;
 		} else if (b.isDifficultyHard()){
 			maxRemoved = REMOVE_ON_HARD;
 		}
 		numRemoved = 0;
 	}
 
 
 	@Override
 	/**
 	 * Removes all values that can be whilst still maintaining a unique solution
 	 */
 	public boolean removeIfCan(Position p) {
 		int row = p.getRow();
 		int col = p.getCol();
		/*if(!b.isInitiallySet(row+1, col+1)){
			System.out.println("error");
		}*/
 		b.setIfInitiallySet(row+1, col+1, false);
 		if(checker.isSudokuSolutionUnique(b)){
 			numRemoved++;
 			return true;
 		} else {
 			b.setIfInitiallySet(row+1, col+1, true);
 			return false;
 		}
 	}
 
 	@Override
 	/**
 	 * Terminates once the number removed exceeds the maximum limit for that difficulty
 	 */
 	public boolean shouldTerminate() {
 		if (numRemoved >= maxRemoved){
 			return true;
 		}
 		return false;
 	}
 	
 	Board b;
 	SudokuSolver checker;
 	int maxRemoved;
 	int numRemoved;
 
 	
 	private static final int REMOVE_ON_EASY = 0;
 	private static final int REMOVE_ON_MEDIUM = 20;
 	private static final int REMOVE_ON_HARD = 100;
 
 
 }
