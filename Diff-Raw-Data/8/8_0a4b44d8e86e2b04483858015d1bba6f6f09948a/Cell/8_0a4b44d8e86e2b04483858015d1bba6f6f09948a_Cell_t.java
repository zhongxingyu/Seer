 package world;
 
 
 import program.Ant;
 import enums.E_Color;
 import enums.E_Terrain;
 
 /**
  * Represents an individual map cell in an Ant world.
  * @author JOH
  * @version 0.2
  *
  */
 public class Cell {
 	
 	private E_Terrain terrain;			//	The terrain at this location
 	private Ant ant;					//	The ant (if any) at this location
 	private int foodAmount;				//	The amount of food (if any) at this location
 	private boolean[] redMarkers;		//	Red markers at this cell
 	private boolean[] blackMarkers;		//	Black markers at this cell
 	private Position position;			//	The x/y coordinates of this cell
 	private World world;				//	The map this cell is within
 	
 	/**
 	 * Constructor.
 	 * @param terrain the terrain at this location
 	 * @param foodAmount the amount of food at this location
 	 */
 	public Cell(E_Terrain terrain, int foodAmount, Position position) {
 		this.terrain = terrain;
 		this.foodAmount = foodAmount;
 		setPosition(position);
 		redMarkers = new boolean[6];
 		blackMarkers = new boolean[6];
 	}
 	
 	/**
 	 * Checks if any marker of a given color is present at this cell.
 	 * @return
 	 */
 	public boolean checkAnyMarker(E_Color color)
 	{
 		for (int i = 0; i < 6; i++)
 			if (color == E_Color.RED && redMarkers[i] == true)
 				return true;
 			else if (color == E_Color.BLACK && blackMarkers[i] == true)
 				return true;
 		return false;
 	}
 	
 	/**
 	 * Checks if a marker of the given color and number is present at this cell.
 	 * @param color the color to check
 	 * @param marker the number to check
 	 * @return true if present, false otherwise
 	 */
 	public boolean checkMarker(E_Color color, int marker)
 	{
		if (marker < 0 || marker > 5)
 			return false;
 		if (color == E_Color.BLACK)
 			return blackMarkers[marker];
 		if (color == E_Color.RED)
 			return redMarkers[marker];
 		return false;
 	}
 	
 	/**
 	 * Sets a marker in this cell.
 	 * @param color the color of the marker
 	 * @param marker the number of the marker
 	 * @return true if OK, false otherwise
 	 */
 	public boolean setMarker(E_Color color, int marker) 
 	{
		if (marker < 0 || marker > 5)
 			return false;
 		if (color == E_Color.BLACK)
 			blackMarkers[marker] = true;
 		else if (color == E_Color.RED)
 			redMarkers[marker] = true;
 		else
 			return false;
 		return true;
 	}
 
 	/**
 	 * Removes a marker in this cell.
 	 * @param color the color of the marker
 	 * @param marker the number of the marker
 	 * @return true if OK, false otherwise
 	 */
 	public boolean clearMarker(E_Color color, int marker) 
 	{
		if (marker < 0 || marker > 5)
 			return false;
 		if (color == E_Color.BLACK)
 			blackMarkers[marker] = false;
 		else if (color == E_Color.RED)
 			redMarkers[marker] = false;
 		else
 			return false;
 		return true;
 	}
 	
 	/**
 	 * Gets the terrain at this location.
 	 * @return the terrain
 	 */
 	public E_Terrain getTerrain() {
 		return terrain;
 	}
 	
 	/**
 	 * Returns true if any ant is at this location.
 	 * @return true if an ant is here, false otherwise
 	 */
 	public boolean isAnt() {
 		return ant != null;
 	}
 
 	/**
 	 * Gets the ant at this location, if any.
 	 * @return the ant, or null if none here
 	 */
 	public Ant getAnt() {
 		return ant;
 	}
 	
 	/**
 	 * Sets the ant at this location
 	 * @param ant the ant to place
 	 */
 	public void setAnt(Ant ant) {
 		this.ant = ant;
 	}
 	
 	/**
 	 * Kills the ant at this location, removing it and adding 3 food particles.
 	 */
 	public void killAnt() {
 		this.ant.setAlive(false);
 		this.ant = null;
 		this.foodAmount += 3;
 	}
 	
 	/**
 	 * Gets the amount of food at this location.
 	 * @return the amount
 	 */
 	public int getFoodAmount() {
 		return foodAmount;
 	}
 	
 	/**
 	 * Sets the amount of food at this location.
 	 * @param the amount
 	 */
 	public void setFoodAmount(int foodAmount) {
 		this.foodAmount = foodAmount;
 	}
 
 	/**
 	 * Gets the x/y coordinates of this cell
 	 * @return
 	 */
 	public Position getPosition() {
 		return position;
 	}
 
 	/**
 	 * Sets the x/y coordinates of this cell.
 	 * @param position
 	 */
 	public void setPosition(Position position) {
 		this.position = position;
 	}
 
 	/**
 	 * Gets the world this cell is within.
 	 * @return
 	 */
 	public World getWorld() {
 		return world;
 	}
 
 	/**
 	 * Sets the world this cell is within.
 	 * @param world
 	 */
 	public void setWorld(World world) {
 		this.world = world;
 	}
 	
 }
