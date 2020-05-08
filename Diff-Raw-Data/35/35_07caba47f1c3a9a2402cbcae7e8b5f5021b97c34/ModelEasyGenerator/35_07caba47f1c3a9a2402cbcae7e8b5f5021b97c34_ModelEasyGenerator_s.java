public class ModelEasyGenerator extends SudokuGenerator{
     // The number of "given" cells.
 	private static final int EASY_DIFFICULTY = 60;
 	public ModelEasyGenerator(ModelGrid currentGrid, ModelGrid answerGrid) {
 		super(currentGrid, answerGrid);
 	}
 	
 	@Override
 	protected void removeCells() {
         int remove = ModelGrid.NUM_CELLS - EASY_DIFFICULTY;
         for(int i=0; i<remove; i++) {
             this.removeRandomCellInBox(i%9);
         }
     }
 }
