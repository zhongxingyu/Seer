 package se.chalmers.tda367.std.core;
 
 import java.util.*;
 
 import se.chalmers.tda367.std.core.tiles.*;
 import se.chalmers.tda367.std.core.tiles.enemies.IEnemy;
 import se.chalmers.tda367.std.utilities.*;
 
 /**
  * Represents the whole game board in a grid system.
  * @author Johan Gustafsson
  * @modified Emil Johansson
  * @date Mar 22, 2012
  */
 public class GameBoard {
 	private Map testMap = new Map();
 	private IBoardTile[][] board;
 	private Position startPos;
 	private Position endPos;
 	private final int width;
 	private final int height;
 	private List<Position> waypoints;
 	
 	public GameBoard(Position startPos, Position endPos){	
 		this(Properties.INSTANCE.getDefaultBoardWidth(), Properties.INSTANCE.getDefaultBoardHeight(), startPos, endPos);
 	}
 	
 	public GameBoard(int width, int height, Position startPos, Position endPos){
 		if(width <= 0 || height <= 0) {
 			throw new IllegalArgumentException("Width and/or height cannot be equal to or smaller than zero");
 		}
 		this.width = width;
 		this.height = height;
 		board =  new IBoardTile[this.width][this.height];
 		if(!posOnBoard(startPos) || !posOnBoard(endPos)) {
 			throw new IllegalArgumentException("Start and/or end position is not on the board.");
 		}
 		this.startPos = startPos;
 		this.endPos = endPos;
 		this.waypoints = new ArrayList<Position>();
 		IBoardTile tile = new TerrainTile(new Sprite());
		initBoard();
 	}
 	
 	/**
 	 * Returns a list of enemies that is inside the radius based on supplied position
 	 * @param p
 	 * @param radius
 	 * @return List of enemies.
 	 */
 	public List<IEnemy> getEnemiesInRadius(Position p, int radius){
 		List<IEnemy> enemies = new ArrayList<IEnemy>();
 		
 		for(int y = p.getY()-radius; y < p.getY()+radius; y++) {
 			for(int x = p.getX()-radius; x < p.getX()+radius; x++) {
 				if(posOnBoard(x, y) && getTileAt(x, y) instanceof IEnemy) {
 					enemies.add((IEnemy) getTileAt(x, y));
 				}
 			}
 		}
 		Collections.sort(enemies);
 		return enemies;
 	}
 
 	/**
 	 * Move the IBoardTile from the old position to the new position
 	 * @param oldP
 	 * @param newP
 	 */
 	public void moveTile(Position oldP, Position newP){
 		IBoardTile newPosTile = getTileAt(newP);
 		
 		placeTile(getTileAt(oldP), newP);
 		placeTile(newPosTile, oldP);
 	}
 	
 	/**
 	 * Place given tile on given position.
 	 * @param tile
 	 * @param p
 	 */
 	public void placeTile(IBoardTile tile, Position p){
 		if(posOnBoard(p)) {
 			if(tile instanceof WaypointTile) {
 				Position tmp = new Position(p.getX()*16+8, p.getY()*16+8);
 				waypoints.add(tmp);
 			}
 			board[p.getX()][p.getY()] = tile;
 		} else {
 			System.out.println("Bad coordinates");
 		}
 	}
 	
 	/**
 	 * Returns the tile from given x and y values.
 	 * @param x
 	 * @param y
 	 * @return IBoardTile from given x and y values.
 	 */
 	public IBoardTile getTileAt(int x, int y) {
 		return board[x][y];
 	}
 	
 	/**
 	 * Returns the tile from given position.
 	 * @param p
 	 * @return IBoardTile from given position.
 	 */
 	public IBoardTile getTileAt(Position p){
 		return getTileAt(p.getX(), p.getY());
 	}
 	
 	/**
 	 * Check if a given x and y values is inside the boundaries of the board.
 	 * @param p
 	 * @return true if given x and y values are on the game board.
 	 */
 	public boolean posOnBoard(int x, int y){
 		if(x < 0 || y < 0) {
 			return false;
 		}
 		if(x >= board[0].length || y >= board.length) {
 			return false;
 		}
 		return true;
 	}
 	
 	/**
 	 * Check if a given position is inside the boundaries of the board.
 	 * @param p
 	 * @return true if position is on the game board.
 	 */
 	public boolean posOnBoard(Position p){
 		return posOnBoard(p.getX(), p.getY());
 	}
 	
 	private void initBoard(){
 		int[][] map = testMap.getMap();
 		IBoardTile buildableTile = new BuildableTile(new Sprite());
 		for(int i = 0; i < map.length;i++){
 			for(int j = 0; j < map[i].length;j++){
 				if(map[i][j] == 0){
 					board[i][j] = buildableTile; 
 				} else { //TODO should probably change PathTile-creation
 					board[i][j] = new PathTile(new Sprite(), testMap.getBoardValueAtPos(new Position(i,j)), new Position(i,j));
 				}
 			}
 			
 		}
 	}
 	
 	/**
 	 * Overrides toString
 	 * @return a string representation of the game board.
 	 */
 	public String toString() {
 		StringBuilder str = new StringBuilder();
 		for (int x = 0; x < board.length; x++) {
 			for (int y = 0; y < board[x].length; y++) {
 				str.append('[');
 				str.append(board[x][y].toString());
 				str.append(']');
 			}
 			str.append("\n");
 		}
 		return str.toString();
 		
 	}
 	
 	/**
 	 * @return the width of the game board.
 	 */
 	public int getWidth() {
 		return width;
 	}
 	
 	/**
 	 * @return the height of the game board.
 	 */
 	public int getHeight() {
 		return height;
 	}
 	
 	/**
 	 * Method to get the enemy's starting position on the game board.
 	 * @return a position containing the coordinates of the enemy starting position.
 	 */
 	public Position getStartPos() {
 		return startPos;
 	}
 	
 	/**
 	 * Method to get the end/goal position on the game board.
 	 * @return a position containing the coordinates of the end/goal position.
 	 */
 	public Position getEndPos() {
 		return endPos;
 	}
 	
 	/**
 	 * TODO: Fix javadoc
 	 */
 	public List<Position> getWaypoints() { 
		return new ArrayList<Position>(waypoints);
 	}
 	
 	/**
 	 * Method for checking if a given position on the game board is buildable.
 	 * @param p position to check if buildable.
 	 * @return true if given position is buildable. Returns false if the position isn't buildable or on the game board.
 	 */
 	public boolean canBuildAt(Position p) {
 		if(!posOnBoard(p)) {
 			return false;
 		}
 		return getTileAt(p) instanceof IBuildableTile;
 	}
 	
 	/**
 	 * Method for checking if there is an enemy on a given position.
 	 * @param p position to check for an enemy.
 	 * @return true if an enemy is on the given position. Returns false if given position is outside the board or no enemy is at the position.
 	 */
 	public boolean isEnemyAt(Position p) {
 		if(!posOnBoard(p)) {
 			return false;
 		}
 		return getTileAt(p) instanceof IEnemy;
 	}
 	public Map getMap(){
 		return testMap;
 	}
 }
