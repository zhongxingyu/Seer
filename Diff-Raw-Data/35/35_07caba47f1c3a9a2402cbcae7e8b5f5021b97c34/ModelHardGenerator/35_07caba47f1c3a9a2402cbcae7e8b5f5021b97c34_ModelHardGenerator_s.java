public class ModelHardGenerator extends SudokuGenerator{
     // The number of "given" cells.
     private static final int HARD_DIFFICULTY = 30;
 
     public ModelHardGenerator(ModelGrid currentGrid, ModelGrid answerGrid) {
         super(currentGrid, answerGrid);
    }
 	
     @Override
     protected void removeCells() {
         int remove = ModelGrid.NUM_CELLS - HARD_DIFFICULTY;
         for(int i=0; i<remove; i++) {
             this.removeRandomCellInBox(i%9);
         }
     }
 }
