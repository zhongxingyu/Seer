 package com.madbros.adventurecraft;
 
 import java.util.HashMap;
 
 import com.madbros.adventurecraft.Items.*;
 import com.madbros.adventurecraft.TileTypes.*;
 import com.madbros.adventurecraft.Utils.*;
 
 public class Constants {
 	public static HashMap<Integer, Item> ITEM_HASH = new HashMap<Integer, Item>();
 	public static HashMap<Integer, Tile> TILE_HASH = new HashMap<Integer, Tile>(); 
 	
 	public static HashMap<Integer, Integer> TOP_LEFT_AUTO_TILE_HASH = new HashMap<Integer, Integer>();
 	public static HashMap<Integer, Integer> TOP_RIGHT_AUTO_TILE_HASH = new HashMap<Integer, Integer>();
 	public static HashMap<Integer, Integer> BOTTOM_LEFT_AUTO_TILE_HASH = new HashMap<Integer, Integer>();
 	public static HashMap<Integer, Integer> BOTTOM_RIGHT_AUTO_TILE_HASH = new HashMap<Integer, Integer>();
 	
 	//game constants
 	public static final int INITIAL_WINDOW_WIDTH = 1280;// 640
 	public static final int INITIAL_WINDOW_HEIGHT = 800;//480
 	public static final int RENDER_MARGIN = 1;
 	public static final int FRAME_RATE = 60;
 	public static final String GAME_TITLE = "Adventure Craft";
 	
 	public static final int PIXEL_MIN = 0;
 	public static final int PIXEL_MAX = 32;
 	
 	public static final String SAVE_LOC = "saves/";
 	public static final String CHUNKS_FOLDER = "chunks/";
 	
 	//character constants
 	public static final int CHARACTER_SIZE = 64;
 	public static final int UP = 1;
 	public static final int DOWN = 2;
 	public static final int LEFT = 3;
 	public static final int RIGHT = 4;
 	public static final int WALK_DOWN = 0;
 	public static final int WALK_RIGHT = 9;
 	public static final int WALK_UP = 18;
 	public static final int WALK_LEFT = 27;
 	public static final boolean VERTICAL = true;
 	public static final boolean HORIZONTAL = false;
 	
 	
 	//block constants
 	public static final int TILE_SIZE = 64;
 	public static final int TEXTURE_SIZE = 32;
 	
 	//inventory constants
 	public static final int INV_MENU_TILE_SIZE = 32;
 	public static final int INV_CELL_SIZE = 50;
 	public static final int ITEM_SIZE =32;
 	
 	public static final int INV_LENGTH = 10;
 	public static final int INV_HEIGHT = 4;
 	
 	public static final Margin INV_MENU_MARGIN = new Margin(50, 50, 20, 100);
 	public static final Margin INV_CELL_MARGIN = new Margin(0, 20, 0, 8);
 	
 	public static final Rect INV_BACKDROP_RECT = new Rect(INV_MENU_MARGIN.left, INV_MENU_MARGIN.top, 
 														  INITIAL_WINDOW_WIDTH - INV_MENU_MARGIN.getHorizontalLength(),
 														  INITIAL_WINDOW_HEIGHT - INV_MENU_MARGIN.getVerticalLength());
 	public static final Rect INV_BAR_RECT = new Rect((INITIAL_WINDOW_WIDTH - (INV_CELL_SIZE + INV_CELL_MARGIN.right) * INV_LENGTH + INV_CELL_SIZE) / 2, INITIAL_WINDOW_HEIGHT-50);
	public static final Rect INV_BAG_RECT = new Rect((INITIAL_WINDOW_WIDTH - (INV_CELL_SIZE + INV_CELL_MARGIN.right) * INV_LENGTH + INV_CELL_SIZE) / 2, INV_BACKDROP_RECT.y2()- INV_BAR_RECT.h*4);
	public static final Rect INV_CHAR_RECT = new Rect(INV_BACKDROP_RECT.x2() - CHARACTER_SIZE*4, INV_BACKDROP_RECT.y + 30, CHARACTER_SIZE*4, CHARACTER_SIZE*4);
 	public static final Rect INV_CRAFTING_RECT = new Rect(130, 50, 2*(INV_CELL_SIZE+2), 2*(INV_CELL_SIZE+2));
 	
 	//item/cell constants
 	public static final int ITEM_OFFSET = 5;
 	public static final int BAR = 0;
 	public static final int BAG = 1;
 	public static final int CRAFTING = 2;
 	public static final int CRAFTED = 3;
 	
 	//level constants
 	public static final int CHUNK_SIZE = 16;	//keep between 12 and 16
 	public static final int CHUNKS_IN_A_ROW = 5;	//also columns
 	public static final int TILES_PER_ROW = CHUNK_SIZE*CHUNKS_IN_A_ROW;	//also columns
 	
 	//game states
 	public static enum State {
 		MAIN, INVENTORY, MAIN_MENU, NEW_GAME_MENU;
 	}
 	
 	//Blocks
 	public static final int AIR = 0;
 	public static final int GRASS = 1;
 	public static final int DIRT = 2;
 	public static final int SAND = 3;
 	public static final int BEDROCK = 4;
 	public static final int WATER = 5;
 	public static final int TREE = 6;
 	public static final int HOLE = 7;
 	public static final int DARK_DIRT = 8;
 	
 	//Block Layers
 	public static final int DARK_DIRT_LAYER = 0;
 	public static final int LIGHT_DIRT_LAYER = 1;
 	public static final int GRASS_LAYER = 2;
 	public static final int WATER_LAYER = 3;
 	
 	//Collisions
 	public static enum Collision {
 		X_COLLISION, Y_COLLISION, DOUBLE_COLLISION;
 	}
 	
 	//Mouse
 	public static final int LEFT_MOUSE_BUTTON = 0;
 	public static final int RIGHT_MOUSE_BUTTON = 1;
 	public static final int MIDDLE_MOUSE_BUTTON = 2;
 	
 	
 	//Animations
 	public static final int[][] BREAKING_ANIMATION = {{0,1}, {1, 1}, {2, 1}, {3, 1}, {4, 1}};
 	
 	//Items
 	public static final int NONE = 0;
 	public static final int EMPTY = 0;
 	public static final int GRASS_SEED = 1;
 	public static final int EARTH_CLUMP = 2;
 	public static final int SAND_CLUMP = 3;
 	public static final int LOG = 4;
 	public static final int PLANK = 5;
 	
 	public static final int SHOVEL = 6;
 	
 	public static final int SWORD = 7;
 	
 	//Debugger
 	public static final int	BYTES_IN_MEGABYTE = 1048576;
 	public static final int DEBUG_MENU_SIZEX = 200;
 	public static final int DEBUG_MENU_SIZEY = 30;
 	public static final float DEBUG_FONT_SIZE = 14f;
 	public static final int NUMBER_OF_FRAMES_USED_FOR_SAMPLE_MEAN = 30;
 	public static final int DISPLAY_STARTX = 10;
 	public static final int DISPLAY_STARTY = 20;
 	public static final int DISPLAY_MARGIN = 14;
 	
 	public static final int BLOCK_COLLISION_BUTTON = 0;
 	public static final int RECT_COLLISION_BUTTON = 1;
 	public static final int CHUNK_BOUNDARIES_BUTTON = 2;
 	public static final int COLLISION_DETECTION_BUTTON = 3;
 	
 	//Auto Tiling
 	public static final int TOP_LEFT_TILE= 0;
 	public static final int TOP_TILE = 1;
 	public static final int TOP_RIGHT_TILE= 2;
 	public static final int LEFT_TILE = 3;
 	public static final int MIDDLE_TILE = 4;
 	public static final int RIGHT_TILE = 5;
 	public static final int BOTTOM_LEFT_TILE = 6;
 	public static final int BOTTOM_TILE = 7;
 	public static final int BOTTOM_RIGHT_TILE = 8;
 	
 	public static final int MERGE_TILE_TOP_LEFT = 9;
 	public static final int MERGE_TILE_TOP_RIGHT = 10;
 	public static final int MERGE_TILE_BOTTOM_LEFT = 11;
 	public static final int MERGE_TILE_BOTTOM_RIGHT = 12;
 	
 	//Main Menu
 	public static final int MAIN_MENU_WIDTH = 100;
 	public static final int MAIN_MENU_HEIGHT = 30;
 	public static final int MAIN_MENU_STARTX = Game.centerScreenX - MAIN_MENU_WIDTH / 2;
 	public static final int MAIN_MENU_STARTY = 40;
 	
 	public Constants() {
 		ITEM_HASH.put(NONE, new NoItem());
 		ITEM_HASH.put(GRASS_SEED, new GrassSeed());
 		ITEM_HASH.put(EARTH_CLUMP, new EarthClump());
 		ITEM_HASH.put(SAND_CLUMP, new SandClump());
 		ITEM_HASH.put(LOG, new Log());
 		ITEM_HASH.put(PLANK, new Plank());
 		ITEM_HASH.put(SHOVEL, new Shovel());
 		ITEM_HASH.put(SWORD, new Sword());
 		
 		TILE_HASH.put(GRASS, new GrassTile());
 		TILE_HASH.put(DIRT, new DirtTile());
 		TILE_HASH.put(WATER, new WaterTile());
 		TILE_HASH.put(TREE, new TreeTile());
 		TILE_HASH.put(DARK_DIRT, new DarkDirtTile());
 		TILE_HASH.put(AIR, new NoTile());
 		
 		TOP_LEFT_AUTO_TILE_HASH.put(0, TOP_LEFT_TILE);
 		TOP_LEFT_AUTO_TILE_HASH.put(1, TOP_LEFT_TILE);
 		TOP_LEFT_AUTO_TILE_HASH.put(2, LEFT_TILE);
 		TOP_LEFT_AUTO_TILE_HASH.put(3, LEFT_TILE);
 		TOP_LEFT_AUTO_TILE_HASH.put(8, TOP_TILE);
 		TOP_LEFT_AUTO_TILE_HASH.put(9, TOP_TILE);
 		TOP_LEFT_AUTO_TILE_HASH.put(10, MERGE_TILE_BOTTOM_RIGHT);	//MERGE_TILE_TOP_LEFT);
 		TOP_LEFT_AUTO_TILE_HASH.put(11, MIDDLE_TILE);
 		
 		TOP_RIGHT_AUTO_TILE_HASH.put(0, TOP_RIGHT_TILE);
 		TOP_RIGHT_AUTO_TILE_HASH.put(2, RIGHT_TILE);
 		TOP_RIGHT_AUTO_TILE_HASH.put(4, TOP_RIGHT_TILE);
 		TOP_RIGHT_AUTO_TILE_HASH.put(6, RIGHT_TILE);
 		TOP_RIGHT_AUTO_TILE_HASH.put(16, TOP_TILE);
 		TOP_RIGHT_AUTO_TILE_HASH.put(18, MERGE_TILE_BOTTOM_LEFT);	//MERGE_TILE_TOP_RIGHT);
 		TOP_RIGHT_AUTO_TILE_HASH.put(20, TOP_TILE);
 		TOP_RIGHT_AUTO_TILE_HASH.put(22, MIDDLE_TILE);
 
 		BOTTOM_LEFT_AUTO_TILE_HASH.put(0, BOTTOM_LEFT_TILE);
 		BOTTOM_LEFT_AUTO_TILE_HASH.put(8, BOTTOM_TILE);
 		BOTTOM_LEFT_AUTO_TILE_HASH.put(32, BOTTOM_LEFT_TILE);
 		BOTTOM_LEFT_AUTO_TILE_HASH.put(40, BOTTOM_TILE);
 		BOTTOM_LEFT_AUTO_TILE_HASH.put(64, LEFT_TILE);
 		BOTTOM_LEFT_AUTO_TILE_HASH.put(72, MERGE_TILE_TOP_RIGHT);	//MERGE_TILE_BOTTOM_LEFT);
 		BOTTOM_LEFT_AUTO_TILE_HASH.put(96, LEFT_TILE);
 		BOTTOM_LEFT_AUTO_TILE_HASH.put(104, MIDDLE_TILE);
 
 		BOTTOM_RIGHT_AUTO_TILE_HASH.put(0, BOTTOM_RIGHT_TILE);
 		BOTTOM_RIGHT_AUTO_TILE_HASH.put(16, BOTTOM_TILE);
 		BOTTOM_RIGHT_AUTO_TILE_HASH.put(64, RIGHT_TILE);
 		BOTTOM_RIGHT_AUTO_TILE_HASH.put(80, MERGE_TILE_TOP_LEFT);	//MERGE_TILE_BOTTOM_RIGHT);
 		BOTTOM_RIGHT_AUTO_TILE_HASH.put(128, BOTTOM_RIGHT_TILE);
 		BOTTOM_RIGHT_AUTO_TILE_HASH.put(144, BOTTOM_TILE);
 		BOTTOM_RIGHT_AUTO_TILE_HASH.put(192, RIGHT_TILE);
 		BOTTOM_RIGHT_AUTO_TILE_HASH.put(208, MIDDLE_TILE);
 	}
 }
