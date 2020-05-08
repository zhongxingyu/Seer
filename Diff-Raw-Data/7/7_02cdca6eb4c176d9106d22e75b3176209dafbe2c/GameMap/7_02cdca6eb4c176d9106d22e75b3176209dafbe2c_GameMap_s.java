 package vooga.towerdefense.model;
 
 import java.awt.Dimension;
 import java.awt.Graphics2D;
 import java.awt.Point;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.List;
 
 import util.Location;
 import util.Pixmap;
 import vooga.rts.util.Vector;
 import vooga.towerdefense.action.Action;
 import vooga.towerdefense.action.actionlist.FollowPath;
 import vooga.towerdefense.attributes.AttributeConstants;
 import vooga.towerdefense.attributes.AttributeManager;
 import vooga.towerdefense.gameelements.GameElement;
 
 /**
  * The GameMap holds all of the state corresponding to an entire game at a given
  * moment, including all towers and units and the tiles that comprise the map
  * itself.
  * 
  * @author Erick Gonzalez
  * @author Jimmy Longley
  */
 public class GameMap {
 
 	private List<GameElement> myGameElements;
 	private Tile[][] myGrid;
 	private Dimension myDimensions;
 	private GameElement myGhostImage;
 	private Pathfinder myPathfinder;
 	public Location myEndLocation;
 	public Location mySpawnLocation;
 	private Pixmap myBackgroundImage;
 	private Dimension myTileSize;
 
 	/**
 	 * 
 	 * @param background
 	 *            a background image
 	 * @param tileSize
 	 * @param width
 	 *            the width of the map, in pixels
 	 * @param height
 	 *            the height of the map, in pixels
 	 * @param destination
 	 *            the destination point of all units
 	 */
 	public GameMap(Tile[][] grid, Pixmap background, Dimension mapDimensions,
 			Dimension tileSize, Location spawnLocation, Location endLocation) {
 		myBackgroundImage = background;
 		myDimensions = mapDimensions;
 		myGameElements = new ArrayList<GameElement>();
 		myGrid = grid;
 		myPathfinder = new Pathfinder(myGrid, tileSize);
 		myTileSize = tileSize;
 
 		// TODO: pull these from file
 //		myEndLocation = myGrid[myGrid.length-1][myGrid[0].length/2].getCenter();
 //		mySpawnLocation = myGrid[0][myGrid[0].length/2].getCenter();
 		mySpawnLocation = spawnLocation;
 		myEndLocation = endLocation;
 		updatePaths();
 	}
 
 	/**
 	 * Updates the entire game based on an interval of time elapsed
 	 * 
 	 * @param elapsedTime
 	 *            time elapsed since last game clock tick.
 	 */
 	public void update(double elapsedTime) {
 		for (int i = 0; i < myGameElements.size(); ++i) {
 			myGameElements.get(i).update(elapsedTime);
 		}
 	}
 
 	/**
 	 * Adds a game element to the given tile t.
 	 * 
 	 * @param e
 	 *            a game element
 	 */
 	public void addToMap(GameElement e) {
 		myGameElements.add(e);
 	}
 
 	/**
 	 * tests if an element is a tower
 	 * 
 	 * @param e
 	 * @return
 	 */
 	public boolean isTower(GameElement e) {
		return e.getAttributeManager().getAttribute(AttributeConstants.NAME)
				.equals("tower");
 	}
 
 	/**
 	 * Makes tiles covered by this game element neither buildable or walkable, and sets their element.
 	 * 
 	 * @param e
 	 */
 	public void blockTiles(GameElement e) {
 		int tilesWide = (int) Math.ceil(e.getSize().getWidth()
 				/ myTileSize.getWidth());
 		int tilesTall = (int) Math.ceil(e.getSize().getHeight()
 				/ myTileSize.getHeight());
 
 		
 		for (int i = 0; i < tilesWide; i++) {
 			for (int j = 0; j < tilesTall; j++) {
 				Location location = new Location(e.getCenter().getX() + i
 						* myTileSize.getWidth(), e.getCenter().getY() + j
 						* myTileSize.getHeight());
 				
 				Tile t = getTile(location);
 				t.setTower(e);
 				t.setWalkable(false);
 				t.setBuildable(false);
 			}
 		}
 	}
 
 	/**
 	 * Given a point on the map, returns the Tile enclosing that point.
 	 * 
 	 * @param point
 	 *            a point (x, y) on the game map, where x and y are measured in
 	 *            pixels.
 	 * @return a Tile object containing this point (x, y)
 	 */
 	public Tile getTile(Point point) {
 		return myGrid[(int) (point.getX() / getTileSize().getWidth())][(int) (point
 				.getY() / getTileSize().getHeight())];
 	}
 
 	/**
 	 * Given a location on the map, returns the Tile enclosing that point.
 	 * 
 	 * @param location
 	 *            a location (x, y) on the game map, where x and y are measured
 	 *            in pixels.
 	 * @return a Tile object containing this point (x, y)
 	 */
 	public Tile getTile(Location location) {
 		try {
 			return myGrid[(int) (location.getX() / getTileSize().getWidth())][(int) (location
 					.getY() / getTileSize().getHeight())];
 		} catch (ArrayIndexOutOfBoundsException e) {
 			return null;
 		}
 	}
 
 	/**
 	 * gets the background image for this map.
 	 * 
 	 * @return pixmap that is the background
 	 */
 	public Pixmap getBackgroundImage() {
 		return myBackgroundImage;
 	}
 
 	/**
 	 * 
 	 * @param pen
 	 *            a pen used to draw elements on this map.
 	 */
 	public void paint(Graphics2D pen) {
 		if (myBackgroundImage != null) {
 			pen.drawImage(myBackgroundImage.getImg(), 0, 0, myDimensions.width,
 					myDimensions.height, null);
 		}
 		paintTiles(pen);
 		paintGameElements(pen);
 		if (myGhostImage != null)
 			myGhostImage.paint(pen);
 	}
 
 	private void paintTiles(Graphics2D pen) {
 		for (int i = 0; i < myGrid.length; ++i) {
 			for (int j = 0; j < myGrid[i].length; ++j) {
 				myGrid[i][j].paint(pen);
 			}
 		}
 	}
 
 	private void paintGameElements(Graphics2D pen) {
 		for (int i = 0; i < myGameElements.size(); ++i) {
 			myGameElements.get(i).paint(pen);
 		}
 	}
 
 	/**
 	 * 
 	 * @return a list of all available game elements.
 	 */
 	public List<GameElement> getAllGameElements() {
 		return myGameElements;
 	}
 
 	/**
 	 * 
 	 * @param gameElement
 	 *            simply adds a game element to the map.
 	 */
 	public void addGameElement(GameElement gameElement) {
 		myGameElements.add(gameElement);
 	}
 
 	/**
 	 * 
 	 * @param gameElement
 	 *            game element to be removed
 	 */
 	public void removeGameElement(GameElement gameElement) {
 		myGameElements.remove(gameElement);
 	}
 
 	/**
 	 * Gets howMany number of the closest targets within a radius of a circle
 	 * centered at source.
 	 * 
 	 * @param source
 	 *            the center of the circle
 	 * @param radius
 	 *            the radius of the circle
 	 * @param howMany
 	 *            the number of units closest to source, within radius
 	 * @return howMany number of elements within radius sorted by distance from
 	 *         source
 	 */
 	public List<GameElement> getTargetsWithinRadius(Location source,
 			double radius, int howMany) {
 		List<GameElement> elementsWithinRadius = getTargetsWithinRadius(source,
 				radius);
 		int lastIndex = howMany > elementsWithinRadius.size() ? elementsWithinRadius
 				.size() : howMany;
 		return elementsWithinRadius.subList(0, lastIndex);
 	}
 
 	/**
 	 * Gets the targets within a radius of a circle centered at source.
 	 * 
 	 * @param source
 	 *            the center of the circle
 	 * @param radius
 	 *            the radius of the circle
 	 * @return all elements within radius sorted by distance from source
 	 */
 	public List<GameElement> getTargetsWithinRadius(Location source,
 			double radius) {
 		List<GameElement> elementsWithinRadius = getElementsWithinRadius(
 				source, radius);
 		sortGameElementsByDistanceToSource(elementsWithinRadius, source);
 		return elementsWithinRadius;
 	}
 
 	private void sortGameElementsByDistanceToSource(
 			List<GameElement> elementsWithinRadius, Location source) {
 
 		class GameElementComparator implements Comparator<GameElement> {
 			private Location mySource;
 
 			public GameElementComparator(Location source) {
 				mySource = source;
 			}
 
 			@Override
 			public int compare(GameElement o1, GameElement o2) {
 				return Vector.distanceBetween(mySource, o1.getCenter())
 						- Vector.distanceBetween(mySource, o2.getCenter()) > 0 ? 1
 						: -1;
 			}
 		}
 
 		Collections.sort(elementsWithinRadius,
 				new GameElementComparator(source));
 	}
 
 	private List<GameElement> getElementsWithinRadius(Location source,
 			double radius) {
 		List<GameElement> elementsWithinRadius = new ArrayList<GameElement>();
 
 		for (GameElement gameElement : myGameElements) {
 			if (gameElement != null) {
 				if (Vector.distanceBetween(source, gameElement.getCenter()) <= radius) {
 					elementsWithinRadius.add(gameElement);
 				}
 			}
 		}
 		return elementsWithinRadius;
 	}
 
 	/**
 	 * Returns a Path object representing the shortest path between two
 	 * locations.
 	 * 
 	 * @param start
 	 *            the start location
 	 * @param finish
 	 *            the end location
 	 * @return the shortest path between these two locations
 	 */
 	public Path getShortestPath(Location start, Location finish) {
 		Location startIndex = getTileIndexFromLocation(start);
 		Location finishIndex = getTileIndexFromLocation(finish);
 		Path thePath = myPathfinder.getShortestPath(startIndex, finishIndex);
 		thePath.add(finish);
 		return thePath;
 	}
 
 	private Location getTileIndexFromLocation(Location location) {
 		return new Location((int) ((location.getX() - 1) / getTileSize()
 				.getWidth()), (int) ((location.getY() - 1) / getTileSize()
 				.getHeight()));
 	}
 
 	/**
 	 * When the grid is modified, every element that follows a path must
 	 * recalculate it's path.
 	 */
 	public void updatePaths() {
 		for (GameElement e : myGameElements) {
 			for (Action action : e.getActions())
 				if (action.getClass().equals(FollowPath.class)) {
 					Path p = getShortestPath(e.getCenter(), myEndLocation);
 					((FollowPath) action).setPath(p);
 				}
 		}
 
 	}
 
 	/**
 	 * Temporarily adds an element onto the map for display only in order to aid
 	 * the user in placing a tower on the map.
 	 * 
 	 * @param itemImage
 	 * @param location
 	 * @param size
 	 */
 	public void addGhostImage(Pixmap itemImage, Location location,
 			Dimension size) {
 		myGhostImage = new GameElement(itemImage, location, size,
 				new AttributeManager());
 	}
 
 	/**
 	 * Resets the ghost image so that it dissapears after the user is no longer
 	 * placing a tower.
 	 */
 	public void resetGhostImage() {
 		myGhostImage = null;
 	}
 
 	/**
 	 * Used to determine if a ghost image should be painted, it tests if a tower
 	 * can be built at a particular point.
 	 * 
 	 * @param p
 	 * @return
 	 */
 	public boolean isBuildable(Point p) {
 		return getTile(p).isBuildable();
 	}
 
 	/**
 	 * Used to determine if a ghost image should be painted, it tests if a tower
 	 * can be built at a particular location.
 	 * 
 	 * @param l
 	 *            a location
 	 * @return
 	 */
 	public boolean isBuildable(Location l) {
 		Tile t = getTile(l);
 		return (t == null) ? false : t.isBuildable();
 	}
 
 	public Location getSpawnLocation() {
 		return mySpawnLocation;
 	}
 
 	public Location getEndLocation() {
 		return myEndLocation;
 	}
 
 	public Dimension getTileSize() {
 		return myTileSize;
 	}
 }
