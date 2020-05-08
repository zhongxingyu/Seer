 package state;
 
 import java.awt.Image;
 import java.util.ArrayList;
 import java.util.Random;
 
 public class World {
 	private Tile[][] worldTile;
 
 	private Random random = new Random();
 
 	public String generateRandomTile(){
 		int rand = random.nextInt(5);
 		if(rand==1)
 			return "tile";
 		else if (rand == 2)
 			return "dark-sand";
 		else if (rand == 3)
 			return "barren-grass";
 		else if (rand == 4)
 			return "dark-sand";
 		else
 			return "Grass";
 	}
 
 	public World(){
 		worldTile = new Tile[100][100];
 		for(int x = 0; x < 100; x++)
 			for(int y = 0; y < 100; y++) {
 				worldTile[x][y] = new Tile(generateRandomTile());
 			}
 
 		for(int x = 3; x < 100; x += 10)
 			for(int y = 3; y < 100; y += 10)
 				addStructure(new Structure(x, y, 3, 3, "dark-tree"));
 	}
 
 	public World(Tile[][] tiles) {
 		worldTile = tiles;
 	}
 
 	/**
 	 * Adds a structure to all tiles it overlaps and returns true.
 	 * If the structure can't be placed, returns false without changing anything.
 	 */
 	public boolean addStructure(Structure s) {
 		int x = s.getX(), y = s.getY(), w = s.getWidth(), h = s.getHeight();
 
		if(x-w < -1 || y-h < -1 || x >= getXSize() || y >= getYSize())
 			return false;
 
 		// check for overlap
 		for(int X = 0; X < w; X++)
 			for(int Y = 0; Y < h; Y++)
				if(worldTile[x-X][y-Y].getStructure() != null)
 					return false; // can't have two structures on one tile
 
 		// place the structure
 		for(int X = 0; X < w; X++)
 			for(int Y = 0; Y < h; Y++)
				worldTile[x-X][y-Y].setStructure(s);
 
 		return true;
 	}
 
 	public Tile getTile(int x, int y) {
 		return worldTile[x][y];
 	}
 
 	public void setTile(int x, int y, Tile t) {
 		worldTile[x][y] = t;
 	}
 
 	public int getXSize() {
 		return worldTile.length;
 	}
 
 	public int getYSize() {
 		return worldTile[0].length;
 	}
 }
