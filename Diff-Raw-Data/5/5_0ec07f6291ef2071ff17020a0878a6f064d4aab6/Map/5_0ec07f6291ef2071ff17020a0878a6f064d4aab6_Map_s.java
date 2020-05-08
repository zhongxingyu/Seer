 /* 
  * File Name: Map.java
  * Contributors:	Jonathan Bradley 	- 7/18/2013
  * 					Ryan Meier			- 
  * 					Ben Emrick			-
  * 
  * Purpose: This class creates a map grid to keep track of each game tile
  * 
  * Future Goals: change constructor to read-in a text file for different
  * 		levels once we have the different types of tiles developed
  */
 package td.map;
 
 import java.awt.Graphics;
 import java.awt.image.BufferedImage;
 
 import td.Game;
 
 public class Map {
 
 	// Set up needed variables
 	private int mapWidth, mapHeight;
 	private Tile[][] mapGrid;
 	private int TileID;
 	private int mapWidthPixels, mapHeightPixels;
 	private Boolean hasChanged;
 	private BufferedImage mapImage;
 	public int startTileID, endTileID;
 	//public Pathfinder path;
 
 	// Constructor - this will need to be modified once we have a config file
 	// set up for maps
 	// currently only creates tiles of grass
 	public Map(int width, int height) {
 		this.mapGrid = new Tile[width][height]; // Map is nothing more than a 2D
 												// array of Tiles
 		TileID = 0;
 		mapWidth = width;
 		mapHeight = height;
 		for (int i = 0; i < width; i++) {
 			for (int k = 0; k < height; k++) {
 				this.mapGrid[i][k] = new Tile("artAssets/Concrete.png",
 						TileID++, false);
 			}
 		}
 		mapWidthPixels = mapGrid[0][0].getWidth() * mapWidth; // Sprite width *
 																// Tile width *
 		mapHeightPixels = mapGrid[0][0].getHeight() * mapHeight; // Sprite
 																 // height *
 																 // Tile height
 
 		// Test section for a simple maze to test the pathfinder
 		startTileID = (3 * width) + 0;
 		changeTile("artAssets/grass.png", 3,  0,  true);
 		changeTile("artAssets/grass.png", 3,  1,  true);
 		changeTile("artAssets/grass.png", 3,  2,  true);
 		changeTile("artAssets/grass.png", 3,  3,  true);
 		changeTile("artAssets/grass.png", 3,  4,  true);
 		changeTile("artAssets/grass.png", 4,  4,  true);
 		changeTile("artAssets/grass.png", 5,  4,  true);
 		changeTile("artAssets/grass.png", 6,  4,  true);
 		changeTile("artAssets/grass.png", 7,  4,  true);
 		changeTile("artAssets/grass.png", 8,  4,  true);
 		changeTile("artAssets/grass.png", 8,  5,  true);
 		changeTile("artAssets/grass.png", 8,  6,  true);
 		changeTile("artAssets/grass.png", 8,  7,  true);
 		changeTile("artAssets/grass.png", 8,  8,  true);
 		changeTile("artAssets/grass.png", 9,  8,  true);
 		changeTile("artAssets/grass.png", 10, 8,  true);
 		changeTile("artAssets/grass.png", 11, 8,  true);
 		changeTile("artAssets/grass.png", 11, 9,  true);
 		changeTile("artAssets/grass.png", 11, 10, true);
 		changeTile("artAssets/grass.png", 11, 11, true);
 		changeTile("artAssets/grass.png", 11, 12, true);
 		changeTile("artAssets/grass.png", 10, 12, true);
 		changeTile("artAssets/grass.png", 9,  12, true);
 		changeTile("artAssets/grass.png", 8,  12, true);
 		changeTile("artAssets/grass.png", 7,  12, true);
 		changeTile("artAssets/grass.png", 6,  12, true);
 		changeTile("artAssets/grass.png", 6,  11, true);
 		changeTile("artAssets/grass.png", 6,  10, true);
 		changeTile("artAssets/grass.png", 6,  9,  true);
 		changeTile("artAssets/grass.png", 5,  9,  true);
 		changeTile("artAssets/grass.png", 4,  9,  true);
 		changeTile("artAssets/grass.png", 3,  9,  true);
 		changeTile("artAssets/grass.png", 2,  9,  true);
 		changeTile("artAssets/grass.png", 2,  10, true);
 		changeTile("artAssets/grass.png", 2,  11, true);
 		changeTile("artAssets/grass.png", 2,  12, true);
 		changeTile("artAssets/grass.png", 2,  13, true);
 		changeTile("artAssets/grass.png", 2,  14, true);
 		endTileID = (2 * height) + 14;
 		// End test-map section
 
 //		path = new Pathfinder();
 //		path.init();
 
 		generateMapImage();
 	}
 
 	// Returns the Map height (in tiles)
 	public int getHeight() {
 		return mapHeight;
 	}
 
 	// Returns the Map Width (in tiles)
 	public int getWidth() {
 		return mapWidth;
 	}
 
 	// Returns the Map Height (in pixels)
 	public int getHeightPixels() {
 		return mapHeightPixels;
 	}
 
 	// Returns the Map Width (in pixels)
 	public int getWidthPixels() {
 		return mapWidthPixels;
 	}
 
 	// I don't think this is needed - might remove later?
 	public Tile[][] getMap() {
 		return mapGrid;
 	}
 
 	// Returns a specific Tile, by x and y coordinates
 	public Tile getTile(int x, int y) {
 		return mapGrid[x][y];
 	}
 
 	// returns the BufferedImage of the map
 	public BufferedImage getMapImage() {
 		return mapImage;
 	}
 
 	// Creates a fresh BufferedImage if the map layout were to change
 	private void generateMapImage() {
 		mapImage = new BufferedImage(mapWidthPixels, mapHeightPixels,
 				BufferedImage.TYPE_INT_ARGB);
 		Graphics g = mapImage.createGraphics();
 		for (int i = 0; i < mapWidth; i++) {
 			for (int k = 0; k < mapHeight; k++) {
 				Tile temp = mapGrid[i][k];
 				int tempx = i * temp.getWidth();
				int tempy = k * temp.getHeight();
 				g.drawImage(temp.getImage(), tempx, tempy, temp.getWidth(),
 						temp.getHeight(), null);
 			}
 		}
 		hasChanged = false;
 	}
 
 	// Changes a specific Tile in the map by Tile ID
 	public void changeTile(String img, int ID, boolean bool) {
 		// use black magic to find the location in the grid
 		mapGrid[(int) (Math.floor(ID / mapWidth))][(ID % mapWidth)] = new Tile(
 				img, ID, bool);
 		hasChanged = true;
 	}
 
 	// Changes a specific Tile in the map by x and y coordinate
 	public void changeTile(String img, int i, int k, boolean bool) {
 		mapGrid[i][k] = new Tile(img, ((i * mapHeight) + k), bool);
 		hasChanged = true;
 	}
 
 	// WIP
 	public void tick() {
 
 	}
 
 	// If the map has changed, it renders a new map, else it simply ends
 	// Screen.java will call the Buffered map image later in the render loop
 	public void render() {
 		if (hasChanged) {
 			generateMapImage();
 		}
 	}
 }
