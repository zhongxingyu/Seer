 package yuuki.world;
 
 import java.awt.Point;
 
 /**
  * Contains the walkable points adjacent to some other point.
  */
 public class WalkGraph {
 	
 	/**
 	 * Whether the east point is valid.
 	 */
 	private boolean hasEast;
 	
 	/**
 	 * Whether the north point is valid.
 	 */
 	private boolean hasNorth;
 	
 	/**
 	 * Whether the north-east point is valid.
 	 */
 	private boolean hasNorthEast;
 	
 	/**
 	 * Whether the north-west point is valid.
 	 */
 	private boolean hasNorthWest;
 	
 	/**
 	 * Whether the south point is valid.
 	 */
 	private boolean hasSouth;
 	
 	/**
 	 * Whether the south-east point is valid.
 	 */
 	private boolean hasSouthEast;
 	
 	/**
 	 * Whether the south-west point is valid.
 	 */
 	private boolean hasSouthWest;
 	
 	/**
 	 * Whether the west point is valid.
 	 */
 	private boolean hasWest;
 	
 	/**
 	 * The coordinates of the center of this WalkGraph.
 	 */
 	private Point p;
 	
 	/**
 	 * Creates a new WalkGraph.
 	 * 
 	 * @param position The position of the center tile of this WalkGraph,
 	 * relative to the Land that it came from.
 	 * @param tiles An array containing the center tile and the surrounding
 	 * eight tiles.
 	 */
 	public WalkGraph(Point position, TileGrid tiles) {
 		this.p = position;
 		setValidity(tiles);
 	}
 	
 	/**
 	 * Gets the coordinates of the eastern tile.
 	 * 
 	 * @return The point containing the coordinates of the tile if is a valid
 	 * tile to walk on; otherwise, null.
 	 */
 	public Point getEast() {
 		return (hasEast) ? new Point(p.x + 1, p.y) : null;
 	}
 	
 	/**
 	 * Gets the coordinates of the northern tile.
 	 * 
 	 * @return The point containing the coordinates of the tile if is a valid
 	 * tile to walk on; otherwise, null.
 	 */
 	public Point getNorth() {
 		return (hasNorth) ? new Point(p.x, p.y - 1) : null;
 	}
 	
 	/**
 	 * Gets the coordinates of the north-eastern tile.
 	 * 
 	 * @return The point containing the coordinates of the tile if is a valid
 	 * tile to walk on; otherwise, null.
 	 */
 	public Point getNorthEast() {
 		return (hasNorthEast) ? new Point(p.x + 1, p.y - 1) : null;
 	}
 	
 	/**
 	 * Gets the coordinates of the north-western tile.
 	 * 
 	 * @return The point containing the coordinates of the tile if is a valid
 	 * tile to walk on; otherwise, null.
 	 */
 	public Point getNorthWest() {
 		return (hasNorthWest) ? new Point(p.x - 1, p.y - 1) : null;
 	}
 	
 	/**
 	 * Gets the position of the center tile of this graph.
 	 * 
 	 * @return The position of the center tile.
 	 */
 	public Point getPosition() {
 		return p;
 	}
 	
 	/**
 	 * Gets the coordinates of the southern tile.
 	 * 
 	 * @return The point containing the coordinates of the tile if is a valid
 	 * tile to walk on; otherwise, null.
 	 */
 	public Point getSouth() {
 		return (hasSouth) ? new Point(p.x, p.y + 1) : null;
 	}
 	
 	/**
 	 * Gets the coordinates of the south-eastern tile.
 	 * 
 	 * @return The point containing the coordinates of the tile if is a valid
 	 * tile to walk on; otherwise, null.
 	 */
 	public Point getSouthEast() {
		return (hasSouthEast) ? new Point(p.x + 1, p.y + 1) : null;
 	}
 	
 	/**
 	 * Gets the coordinates of the south-western tile.
 	 * 
 	 * @return The point containing the coordinates of the tile if is a valid
 	 * tile to walk on; otherwise, null.
 	 */
 	public Point getSouthWest() {
		return (hasSouthWest) ? new Point(p.x - 1, p.y + 1) : null;
 	}
 	
 	/**
 	 * Gets the number of valid directions.
 	 * 
 	 * @return The number of valid direction.
 	 */
 	public int getValidCount() {
 		int c = 0;
 		if (hasNorthWest) {
 			c++;
 		}
 		if (hasNorth) {
 			c++;
 		}
 		if (hasNorthEast) {
 			c++;
 		}
 		if (hasWest) {
 			c++;
 		}
 		if (hasEast) {
 			c++;
 		}
 		if (hasSouthWest) {
 			c++;
 		}
 		if (hasSouth) {
 			c++;
 		}
 		if (hasSouthEast) {
 			c++;
 		}
 		return c;
 	}
 	
 	/**
 	 * Gets the coordinates of the western tile.
 	 * 
 	 * @return The point containing the coordinates of the tile if is a valid
 	 * tile to walk on; otherwise, null.
 	 */
 	public Point getWest() {
 		return (hasWest) ? new Point(p.x - 1, p.y) : null;
 	}
 	
 	/**
 	 * Checks whether this WalkGraph returns a valid point for each of the 8
 	 * directions.
 	 * 
 	 * @return True if this WalkGraph returns a valid point for each of the 8
 	 * directions; otherwise, false.
 	 */
 	public boolean isFullyValid() {
 		return (getValidCount() == 8);
 	}
 	
 	/**
 	 * Checks if the tile at the given position is walkable.
 	 * 
 	 * @param grid The TileGrid to check.
 	 * @param x The x-coordinate to check.
 	 * @param y The y-coordinate to check.
 	 * 
 	 * @return True if a tile exists at the given coordinates and it is
 	 * walkable; otherwise, false.
 	 */
 	private boolean checkTile(TileGrid grid, int x, int y) {
 		if (x >= 0 && x <= grid.getWidth() && y >= 0 &&
 				y <= grid.getHeight()) {
 			return grid.tileAt(x, y).isWalkable();
 		} else {
 			return false;
 		}
 	}
 	
 	/**
 	 * Sets whether each direction is valid based on whether the tile in each
 	 * direction is walkable.
 	 * 
 	 * @param tiles An array containing the center tile and the surrounding
 	 * eight tiles.
 	 */
 	private void setValidity(TileGrid tiles) {
 		int xOff = 3 - tiles.getWidth();
 		int yOff = 3 - tiles.getHeight();
 		hasNorthWest	= checkTile(tiles, 0 - xOff, 0 - yOff);
 		hasNorth		= checkTile(tiles, 1 - xOff, 0 - yOff);
 		hasNorthEast	= checkTile(tiles, 2 - xOff, 0 - yOff);
 		hasWest			= checkTile(tiles, 0 - xOff, 1 - yOff);
 		hasEast			= checkTile(tiles, 2 - xOff, 1 - yOff);
 		hasSouthWest	= checkTile(tiles, 0 - xOff, 2 - yOff);
 		hasSouth		= checkTile(tiles, 1 - xOff, 2 - yOff);
 		hasSouthEast	= checkTile(tiles, 2 - xOff, 2 - yOff);
 	}
 	
 }
