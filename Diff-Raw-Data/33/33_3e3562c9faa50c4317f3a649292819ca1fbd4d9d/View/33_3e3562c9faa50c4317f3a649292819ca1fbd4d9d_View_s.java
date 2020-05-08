 package internal.view;
 
 /**
  *
  * @author ablaine
  */
 public class View {
 	public static final int GRID_UNITS_HORI = 8;
 	public static final int GRID_UNITS_VERT = 8;
 	public static final int GRID_SPACING = 2;
 	public static final int TILE_SIZE = 80;
	public static final int BOARD_WIDTH  = (TILE_SIZE * GRID_UNITS_HORI) + (GRID_SPACING + (GRID_UNITS_HORI + 1));
	public static final int BOARD_HEIGHT = (TILE_SIZE * GRID_UNITS_VERT) + (GRID_SPACING + (GRID_UNITS_VERT + 1));
 
 	public static final int WORLD_WIDTH = BOARD_WIDTH;
 	public static final int WORLD_HEIGHT = BOARD_HEIGHT;
 
 	private BoardDisplay boardDisplay;
 
 	public View(BoardDisplay board) {
 		board = boardDisplay;
 	}
 
 }
