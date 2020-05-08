public class ModelIntermediateGenerator extends ModelGenerator{
    // The number of "given" cells.
 	private static final int INTERMEDIATE_DIFFICULTY = 45;

 	public ModelIntermediateGenerator(ModelGrid currentGrid, ModelGrid answerGrid) {
 		super(currentGrid, answerGrid);
 	}
 	
 	@Override
 	protected void removeCells() {
        int remove = ModelGrid.NUM_CELLS - INTERMEDIATE_DIFFICULTY;
         for(int i=0; i<remove; i++) {
             this.removeRandomCellInBox(i%9);
         }
 	}
 }
