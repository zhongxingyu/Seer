 package se.chalmers.dryleafsoftware.androidrally.model.gameBoard;
 
 import java.util.List;
 
 public class GameBoard {
 	Tile[][] tiles = null;
<<<<<<< HEAD
 	List<Laser> lasers;
=======
 	public static final int EAST = 1;
 	public static final int WEST = 3;
 	public static final int SOUTH = 2;
 	public static final int NORTH = 0;
>>>>>>> 61fe9de0cb39190dcd16be1f1dd412373de4821d
 	
 	public GameBoard(int width, int height, int gameBoard) {
 		tiles = new Tile[width][height];
 		for(int i = 0; i < width; i++) {
 			for(int j = 0; j < height; j++){
 				tiles[i][j] = new Tile(0, null);
 			}
 		}
 		lasers = new ArrayList<Laser>();
 		
 	}
 	
 	public Tile getTile(int x, int y){
 		return tiles[x][y];
 	}
 	
 	public int[][] getStartingPositions(){
 		return null;// TODO
 	}
 }
