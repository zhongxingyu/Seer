 import java.io.IOException;
 import java.util.ArrayList;
 
 /**
  * Starter bot implementation. Some methods in this class have been added or
  * modified.
  */
 public class MyBot extends Bot {
 	updateTable update;
 	unchartedTable uncharted;
 	Ants ants;
 	movementList moves;
 	private int antsNumber = 0;
 	private int turn = 0;
 
 	/**
 	 * Main method executed by the game engine for starting the bot.
 	 * 
 	 * @param args
 	 *            command line arguments
 	 * 
 	 * @throws IOException
 	 *             if an I/O error occurs
 	 */
 	public static void main(String[] args) throws IOException {
 		new MyBot().readSystemInput();
 	}
 /**
  * checks if any of the ants have died during other players turns
  */
 	public void checkCasualties() {
 		ArrayList<movement> table;
 		table = moves.getList();
 		for (int i = 0; i < table.size(); i++) {
 			if (ants.getIlk(table.get(i).getNewTile()) == Ilk.DEAD) {
 				antsNumber -= 1;
 			}
 		}
 	}
 /**
  * makes updates to game state
  */
 	public void updateState() {
 		ArrayList<movement> table;
 		if (turn == 5) {
 			uncharted.raiseUncharted();
 			turn = 0;
 		} else {
 			turn++;
 		}
 		table = moves.getList();
 		antsNumber = table.size();
 		checkCasualties();
 		for (int i = 0; i < table.size(); i++) {
 			uncharted.updateUnknown(table.get(i).getCurrentTile(), table.get(i).getDirection());
 		}
 		moves.emptyMoveList();
 	}
 /**
  * sets new ants with view area
  * @param tile location of the ant
  * @param count tells how many ants all ready have the view area and are not dead 
  */
 	public void setNewAnt(Tile tile, int count) {
 		if (antsNumber < count) {
 			uncharted.setViewArea(tile);
 		}
 	}
 
 	/**
 	 * executes bots turn
 	 */
 	@Override
 	public void doTurn() {
 		Aim direction;
 		int count = 0;
 		ants = getAnts();
 		moves = new movementList(ants);
 		update = new updateTable(ants.getViewRadius2());
 		uncharted = new unchartedTable(update, ants, moves);
 		updateState();
 		for (Tile myAnt : ants.getMyAnts()) {
 			setNewAnt(myAnt, count);
 			direction = uncharted.getBestDirection(myAnt);
 			if (direction != null) {
 				moves.addMove(myAnt, direction);
 				ants.issueOrder(myAnt, direction);
 			}
 		}
 	}
 
 }
