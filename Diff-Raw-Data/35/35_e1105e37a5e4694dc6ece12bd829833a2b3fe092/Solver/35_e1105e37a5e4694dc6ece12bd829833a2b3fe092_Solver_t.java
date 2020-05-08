import java.util.*;

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
			//new NakedTwinStrategy(),
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
		List<ArrayList<Cell>> values = new ArrayList<ArrayList<Cell>>();
 		
		for(int i = 1; i <= Sudoku.SUDOKU_SIZE; i++) {
			values.add(new ArrayList<Cell>());
			
			for(Cell c : container.getCells()) {
				if(!c.hasValue() && c.getPossibilities().contains(i))
					values.get(i-1).add(c);	
 			}
 		}
		
		for(int i = 1; i < Sudoku.SUDOKU_SIZE; i++) {
			List<Cell> l = values.get(i-1);
			if(l.size() == 1) {
				l.get(0).setValue(i);
				return true;
			}
 		}
 		
 		return false;
 	}
 }
 
 
 
 
 
 
 
 
