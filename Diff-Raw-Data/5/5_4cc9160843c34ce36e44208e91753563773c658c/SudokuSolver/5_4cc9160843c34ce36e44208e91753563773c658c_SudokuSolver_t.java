 
 public class SudokuSolver implements UniqueChecker {
 	public SudokuSolver() {
 		
 	}
 	@Override
 	public boolean isSudokuSolutionUnique(Board boardToSolve) {
 		int board[][] = new int[this.BOARD_SIZE][this.BOARD_SIZE];
 		for(int row = 1; row <= this.BOARD_SIZE; row++) {
 			for(int col = 1; col <= this.BOARD_SIZE; col++) {
 				if(boardToSolve.isCurrentlyVisibleCell(row, col)) {
					board[row-1][col-1] = boardToSolve.getCellValue(row, col);	
 				} else {
					board[row-1][col-1] = this.EMPTY;
 				}
 			}
 		}
 		SudokuState initialState = new SudokuState(board);
 		initialState.solve();
 		return initialState.solved();
 	}
 	private static final int BOARD_SIZE = 9;
 	private static final int EMPTY = -1;
 }
