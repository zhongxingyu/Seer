 import java.util.ArrayList;
 import java.util.Random;
 
 public class unchartedTable {
 	private int[][] uncharted;
 	updateTable update;
 	Ants game;
 	movementList moves;
 	private int mapHeight;
 	private int mapWidth;
 
 	/**
 	 * constructor
 	 * 
 	 * @param update
 	 *            contains and manages updateTables
 	 * @param game
 	 *            contains the game itself
 	 * @param moves
 	 *            contains order list from the current turn
 	 */
 	public unchartedTable(updateTable update, Ants game, movementList moves) {
 		this.game = game;
 		this.update = update;
 		this.moves = moves;
 		this.mapHeight = game.getRows();
 		this.mapWidth = game.getCols();
 		initializeUncharted();
 	}
 
 	/**
 	 * @return uncharted contains the information about how many turns it has
 	 *         been when the tile was last visible
 	 */
 	public int[][] getUncharted() {
 		return uncharted;
 	}
 
 	/**
 	 * initializes uncharted
 	 */
 	public void initializeUncharted() {
 		uncharted = new int[mapHeight][mapWidth];
 		for (int i = 0; i < mapHeight; i++) {
 			for (int j = 0; j < mapWidth; j++) {
 				uncharted[i][j] = 31;
 			}
 		}
 	}
 
 	/**
 	 * sets previously unknown tile of uncharted table to either as obstacle or
 	 * seen.
 	 * 
 	 * @param tile
 	 *            that is being updated
 	 */
 	public void setUnknownTile(Tile tile) {
 		Ilk type = game.getIlk(tile);
 		if (type == Ilk.WATER) {
 			uncharted[tile.getRow()][tile.getCol()] = -1;
 		} else {
 			uncharted[tile.getRow()][tile.getCol()] = 0;
 		}
 	}
 
 	/**
 	 * set uncharted table's tile to seen
 	 * 
 	 * @param tile
 	 *            that is being updated
 	 */
 	public void setToSeen(Tile tile) {
 		uncharted[tile.getRow()][tile.getCol()] = 0;
 	}
 
 	/**
 	 * establishes new view area
 	 * 
 	 * @param tile
 	 *            location of the ant
 	 */
 	public void setViewArea(Tile tile) {
 		int[][] easternLine = update.getEastTable();
 		int[][] westernLine = update.getWestTable();
 		Tile newTile;
 		for (int i = 0; i < westernLine.length; i++) {
 			for (int j = westernLine[i][0] + 1; j < easternLine[i][0]; j++) {
 				newTile = setCoordinates(tile, j, westernLine[i][1]);
 				if (uncharted[newTile.getRow()][newTile.getCol()] == 31) {
 					setUnknownTile(newTile);
 				} else {
 					setToSeen(newTile);
 				}
 			}
 		}
 	}
 
 	/**
 	 * goes through uncharted and raises the value of areas that are unseen by
 	 * own ants
 	 */
 	public void raiseUncharted() {
 		Tile tile;
 		for (int i = 0; i < uncharted.length; i++) {
 			for (int j = 0; j < uncharted[0].length; j++) {
 				tile = new Tile(i, j);
 				if (((uncharted[i][j] + 5) < 30 && uncharted[i][j] > -1 && game.isVisible(tile))) {
 					uncharted[i][j] += 5;
 				}
 			}
 		}
 	}
 
 	/**
 	 * goes through all the available directions and return the one that is most
 	 * interesting to check
 	 * 
 	 * @param tile
 	 *            location of the ant
 	 * @return direction where ant will next move
 	 */
 	public Aim getBestDirection(Tile tile) {
		int value = 0;
 		int max = 0;
 		Aim direction = null;
 		ArrayList<Aim> directions = getMovableDirections(tile);
 		if (directions.size() == 0) {
 			return null;
 		}
 		for (int i = 0; i < directions.size(); i++) {
 			value = getMoveValue(tile, directions.get(i));
 			if (max < value) {
 				max = value;
 				direction = directions.get(i);
 			} else if (max == value) {
 				direction = getRandomDirection(direction, directions.get(i));
 			}
 		}
 		return direction;
 	}
 
 	/**
 	 * picks from two directions one and returns it
 	 * 
 	 * @param direction
 	 * @param otherDirection
 	 * @return one of the given directions
 	 */
 	public Aim getRandomDirection(Aim direction, Aim otherDirection) {
 		Random rand = new Random();
 		if (rand.nextInt(2) == 0) {
 			return direction;
 		} else {
 			return otherDirection;
 		}
 	}
 
 	/**
 	 * sets new coordinates so that they are within the limits of the table
 	 * 
 	 * @param tile
 	 *            current location of the ant
 	 * @param borderX
 	 *            horizontal border of ants vision
 	 * @param borderY
 	 *            vertical border of ants vision
 	 * @return tile that contains location just outside the ants vision range
 	 */
 	public Tile setCoordinates(Tile tile, int borderX, int borderY) {
 		return new Tile(isOverBoard((tile.getRow() + borderY), mapHeight), isOverBoard((tile.getCol() + borderX), mapWidth));
 	}
 
 	/**
 	 * counts how much ant would gain from moving to the direction given in
 	 * table
 	 * 
 	 * @param tile
 	 *            the current location of the ant
 	 * @param direction
 	 *            where ant would move
 	 * @return value is the amount gain from moving to this direction
 	 */
 	public int getMoveValue(Tile tile, Aim direction) {
 		Tile newTile;
 		int value = 0;
 		int[][] table = update.getUpdateTable(direction);
 		for (int i = 0; i < table.length; i++) {
 			newTile = setCoordinates(tile, table[i][0], table[i][1]);
 			value += uncharted[newTile.getRow()][newTile.getCol()];
 		}
 		return value;
 	}
 
 	/**
 	 * checks if tile is blocked
 	 * 
 	 * @param tile
 	 *            that is being checked
 	 * @return true if tile is blocked false if tile is free
 	 */
 	public boolean isTileBlocked(Tile tile) {
 		if (game.getIlk(tile) == Ilk.LAND || game.getIlk(tile) == Ilk.DEAD) {
 			return false;
 		} else {
 			return true;
 		}
 	}
 
 	/**
 	 * gets all the directions ant can move to
 	 * 
 	 * @param tile
 	 *            location of the ant
 	 * @return table containing all the possible directions ant can move to
 	 */
 	public ArrayList<Aim> getMovableDirections(Tile tile) {
 		Tile newTile;
 		ArrayList<Aim> table = new ArrayList<Aim>();
 		Aim[] directions = { Aim.NORTH, Aim.EAST, Aim.SOUTH, Aim.WEST };
 		for (int i = 0; i < directions.length; i++) {
 			newTile = game.getTile(tile, directions[i]);
 			if (!isTileBlocked(newTile) && !moves.isToBeOccupied(newTile)) {
 				table.add(directions[i]);
 			} else if (game.getIlk(newTile) == Ilk.MY_ANT && moves.isAntsLocationFree(newTile)) {
 				table.add(directions[i]);
 			}
 		}
 		return table;
 	}
 
 	/**
 	 * places the given value to be within borders
 	 * 
 	 * @param value
 	 *            given value
 	 * @param max
 	 *            is the limit of the given area
 	 * @return value that is within given borders
 	 */
 	public int isOverBoard(int value, int max) {
 		if (value >= max) {
 			return value - max;
 		} else if (value < 0) {
 			return max + value;
 		} else {
 			return value;
 		}
 	}
 
 	/**
 	 * sets the new area covered by the view radius to zero.
 	 * 
 	 * @param tile
 	 *            location where ant use to be
 	 * @param direction
 	 *            where ant moved to
 	 */
 	public void updateUnknown(Tile tile, Aim direction) {
 		Tile newTile;
 		int[][] table = update.getUpdateTable(direction);
 		for (int i = 0; i < table.length; i++) {
 			newTile = setCoordinates(tile, table[i][0], table[i][1]);
 			if (uncharted[newTile.getRow()][newTile.getCol()] == 31) {
 				setUnknownTile(newTile);
 			} else {
 				setToSeen(newTile);
 			}
 		}
 	}
 
 }
