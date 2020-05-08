 public class TreasureHunter extends Player {
 
 	private Random rand;
 
 	public TreasureHunter(Cell currentCell, long sleep) {
 		super(currentCell, sleep);
 		rand = new Random();
 	}
 
 	protected void step() {
 		if (currentCell.isExit()) {
 			currentCell.getLabyrinth().huntersWin();
 		} else {
			List<Cell> adjacentCells = currentCell.adjacentCells();
 			int decisionIndex = rand.nextInt(adjacentCells.size());
 			Cell nextCell = adjacentCells.get(decisionIndex);
 			currentCell.removePlayer(this);
 			nextCell.addPlayer(this);
 			currentCell = nextCell;
 		}
 	}
 }
