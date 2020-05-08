 /*
  * Solver.java
  * 
  * Version information
  *
  * Date
  * 
  * Copyright notice
  */
 public class Solver {
 	private Sudoku sudoku;
 	private Strategy[] strategies = {
 			new OneOfEachStrategy(),
 			//new LockedStrategy(),
 			//new HiddenTwinStrategy()
 			};
 	
 	public Solver(Sudoku puzzle) {
 		this.sudoku = puzzle;
 	}
 	
 	public void solve() throws SudokuException {
 		boolean madeProgress = true;
 		
 		while(madeProgress && !sudoku.isSolved()) {
 			madeProgress = false;
 			for(Strategy strat : strategies) {
 				if (strat.removePossibilities(sudoku)) {
 					madeProgress = true;
 					break;
 				}
 			}
 			if (enterValue())
 				madeProgress = true;
 		}
 	}
 
 	public Sudoku getSudoku() {
 		return sudoku;
 	}
 	
 	// if there is one possibility left then that will be the value
 	private boolean enterValue() throws SudokuException {
 		for(CellContainer c : sudoku.getAllContainers()) {
 			if(c.isSolved())
 				continue;
 			// skip a container if needed
 			if(!enterSingle(c)) {
 				if (!enterHiddenSingle(c));
 					continue;
 			}
 			else
 				return true;
 		}
 		return false;
 	}
 	
 	private boolean enterSingle(CellContainer container) throws SudokuException {
 		for(Cell c : container.getCells()) {
 			if(!c.hasValue() && c.getPossibilities().size() == 1) {
 				c.setValue(c.getPossibilities().get(0));
 				return true;
 			}
 		}
 		return false;
 	}
 	
 	private boolean enterHiddenSingle(CellContainer container) throws SudokuException {
		int[] values = new int[Sudoku.SUDOKU_SIZE];
		Cell hiddenSingle = null;
		int hiddenSingleValue = 0;
 		
		for(Cell c : container.getCells()) {
			if(!c.hasValue()) {
				for(int i : c.getPossibilities()) {
					values[i-1] += 1;
					if(values[i-1] == 1) {
						hiddenSingle = c;
						hiddenSingleValue = i;
					}
				}
 			}
 		}
		if(hiddenSingle.getPossibilities().size() == 1) {
			hiddenSingle.setValue(hiddenSingleValue);
 		}
 		
 		return false;
 	}
 }
 
 
 
 
 
 
 
 
