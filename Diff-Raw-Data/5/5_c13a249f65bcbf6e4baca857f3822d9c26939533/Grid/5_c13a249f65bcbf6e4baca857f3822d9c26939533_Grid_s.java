 /**
  * Grid.java
  * 
  * Models a cartesian-based coordinate plane for the actors to interact within.
  * 
  * @author Daniel Davenport, Grant Hensel, Elliot Penson, and Simon Swenson
  * 
  * Wheaton College, CSCI 335, Spring 2013
  */
 package edu.wheaton.simulator.simulation;
 
 import java.awt.Color;
 import java.util.Iterator;
 
 import net.sourceforge.jeval.EvaluationException;
 
 import edu.wheaton.simulator.datastructure.Field;
 import edu.wheaton.simulator.entity.Agent;
 import edu.wheaton.simulator.entity.Slot;
 
 public class Grid implements Iterable<Slot> {
 
 	/**
 	 * The grid of all slots containing all Agent objects Total # slots = Width
 	 * x Height
 	 */
 	private Slot[][] grid;
 	private final Integer width;
 	private final Integer height;
 
 	/**
 	 * Constuctor. Creates a grid with the given width and height
 	 * specifications
 	 * 
 	 * @param width
 	 * @param height
 	 */
 	public Grid(int width, int height) {
 		this.width = width;
 		this.height = height;
 
		grid = new Slot[getWidth()][getHeight()];
 		for (int x = 0; x < getWidth(); x++)
 			for (int y = 0; y < getHeight(); y++)
 				setSlot(new Slot(this),x,y);
 	}
 
 	public Integer getWidth() {
 		return width;
 	}
 
 	public Integer getHeight() {
 		return height;
 	}
 
 	public boolean isValidCoord(int x, int y) {
 		return (x>=0) && (y>=0) && (x < getWidth()) && (y < getHeight());
 	}
 
 	public Slot getSlot(int x, int y) {
 		if(isValidCoord(x,y))
 			return grid[y][x];
 		throw new NullPointerException("Invalid coord!");
 	}
 	
 	public void setSlot(Slot s, int x, int y){
 		if(isValidCoord(x,y))
 			grid[y][x] = s;
 		else
 			throw new NullPointerException("Invalid coord!");
 	}
 
 	/**
 	 * Causes all entities in the grid to act()
 	 * 
 	 */
 	public void updateEntities() {
 		for (Slot[] sArr : grid)
 			for (Slot s : sArr)
 				if (s.getAgent() != null)
 					s.getAgent().act();
 	}
 
 	/**
 	 * Places an Agent to the slot at the given coordinates. This method
 	 * replaces (kills) anything that is currently in that position. The
 	 * Agent's own position is also updated accordingly.
 	 * 
 	 * @param a
 	 * @param x
 	 * @param y
 	 */
 	public void addAgent(Agent a, int x, int y) {
 		getSlot(x, y).setAgent(a);
 		a.setPos(x, y);
 	}
 
 	/**
 	 * Adds the given Agent at the closest free spot to the spawn position. The
 	 * search for an open spot begins at the given x/y and then spirals
 	 * outwards.
 	 * 
 	 * @param a
 	 *            The Agent to add.
 	 * @param spawnX
 	 *            Central x location for spawn
 	 * @param spawnY
 	 *            Central y location for spawn
 	 * @return true if successful (Agent added), false otherwise
 	 */
 	public boolean spawnAgent(Agent a, int spawnX, int spawnY) {
 
 		for (int distance = 0; distance < height || distance < width; distance++) {
 			int x = spawnX - distance;
 			int y = spawnY - distance;
 			if( spawnAgentHelper(a,x,y) )
 				return true;
 			for (; x < spawnX + distance; x++)
 				if( spawnAgentHelper(a,x,y) )
 					return true;
 			for (; y < spawnY + distance; y++)
 				if( spawnAgentHelper(a,x,y) )
 					return true;
 			for (; x > spawnX - distance; x--)
 				if( spawnAgentHelper(a,x,y) )
 					return true;
 			for (; y > spawnY - distance; y--)
 				if( spawnAgentHelper(a,x,y) )
 					return true;
 		}
 		return false;
 	}
 	
 	private boolean spawnAgentHelper(Agent a, int x, int y){
 		if (emptySlot(x, y)) {
 			addAgent(a, x, y);
 			return true;
 		}
 		return false;
 	}
 
 	/**
 	 * Returns true if a slot is empty, false otherwise. Also returns false if
 	 * invalid x, y values are given.
 	 * 
 	 * @param x
 	 * @param y
 	 * @return Whether or not the particular slot is empty
 	 */
 	public boolean emptySlot(int x, int y) {
 		if (isValidCoord(x,y) && getAgent(x, y)==null)
 			return true;
 		return false;
 	}
 
 	/**
 	 * Adds the given Agent to a random (but free) position.
 	 * 
 	 * @param a
 	 *            The Agent to add.
 	 */
 	public boolean spawnAgent(Agent a) {
 		int randomX = (int) (Math.random() * width);
 		int randomY = (int) (Math.random() * height);
 		return spawnAgent(a, randomX, randomY);
 	}
 
 	/**
 	 * Returns the Agent in the slot at the given coordinates
 	 * 
 	 * @param x
 	 * @param y
 	 */
 	public Agent getAgent(int x, int y) {
 		return getSlot(x, y).getAgent();
 	}
 
 	/**
 	 * Removes an Agent from the slot at the given coordinates
 	 * 
 	 * @param x
 	 * @param y
 	 */
 	public void removeAgent(int x, int y) {
 		getSlot(x, y).setAgent(null);
 	}
 
 	/**
 	 * Removes the given agent from the grid.
 	 * 
 	 * @param ge
 	 *            The Agent to remove.
 	 */
 	public void removeAgent(Agent a) {
 		for (Slot[] sArr : grid)
 			for (Slot s : sArr)
 				if (s.getAgent() == a)
 					s.setAgent(null);
 	}
 
 	/**
 	 * Makes a new Layer.
 	 * 
 	 * @param fieldName
 	 *            The name of the Field that the Layer will represent
 	 * @param c
 	 *            The Color that will be shaded differently to represent Field
 	 *            values
 	 */
 	public void newLayer(String fieldName, Color c) {
 		Layer.getInstance().setFieldName(fieldName);
 		Layer.getInstance().setColor(c);
 		Layer.getInstance().resetMinMax();
 	}
 
 	/**
 	 * Loops through the grid and set's the Layer's min/max values
 	 * PRECONDITION: The newLayer method has been called to setup a layer
 	 * 
 	 * @throws EvaluationException
 	 */
 	public void setLayerExtremes() throws EvaluationException {
 		for (Slot[] sArr : grid)
 			for (Slot s : sArr)
 				if (s.getAgent() != null) {
 					Field currentField = s.getAgent().getField(
 							Layer.getInstance().getFieldName());
 					Layer.getInstance().setExtremes(currentField);
 				}
 	}
 
 	/**
 	 * Returns an iterator that goes through the Slots in the Grid
 	 * 
 	 * @return Iterator<Slot>
 	 */
 	@Override
 	public Iterator<Slot> iterator() {
 		return new Iterator<Slot>() {
 
 			int x = 0;
 			int y = 0;
 
 			@Override
 			public boolean hasNext() {
 				return y < getHeight();
 			}
 
 			@Override
 			public Slot next() {
 				Slot toReturn = getSlot(x, y);
 				if (x < getWidth() - 1) {
 					x++;
 				} else {
 					x = 0;
 					y++;
 				}
 				return toReturn;
 			}
 
 			@Override
 			public void remove() {
 				// TODO method stub
 				throw new UnsupportedOperationException();
 			}
 		};
 	}
 
 }
