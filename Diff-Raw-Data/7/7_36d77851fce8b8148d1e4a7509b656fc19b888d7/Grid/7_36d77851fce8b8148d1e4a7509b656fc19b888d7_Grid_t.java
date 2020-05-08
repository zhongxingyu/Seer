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
 
import edu.wheaton.simulator.datastructures.Field;
 import edu.wheaton.simulator.gridentities.GridEntity;
 import edu.wheaton.simulator.gridentities.Slot;
 
 public class Grid {
 
 	/**
 	 * The grid of all slots containing all GridEntity objects
 	 */
 	private Slot[][] grid;
 	
 	/**
 	 * Creates a grid with the given width and height specifications
 	 */
 	public Grid(int width, int height){
 		grid = new Slot[width][height];
 	}
 
 	/**
 	 * Causes all entities in the grid to act()
 	 * 
 	 * TODO Private method is never used locally
 	 */
 	private void updateEntities(){
 		for(Slot[] sArr: grid)
 			for(Slot s: sArr) {
 				s.getEntity().act();
 			}
 	}
 
 	/**
 	 * Adds an entity to the slot at the given coordinates
 	 * 
 	 * TODO Private method is never used locally
 	 */
 	private void addEntity(GridEntity a, int x, int y){
 		grid[x][y].setEntity(a);
 	}
 
 	/**
 	 * Returns the GridEntity in the slot at the given coordinates
 	 * 
 	 * TODO Private method is never used locally
 	 */
 	private GridEntity getEntity(int x, int y){
 		return grid[x][y].getEntity();
 	}
 
 	/**
 	 * Removes a GridEntity from the slot at the given coordinates
 	 * 
 	 * TODO Private method is never used locally
 	 */
 	private void removeEntity(int x, int y){
 		grid[x][y].setEntity(null);
 	}
 	
     /**
      * Removes the given entity from the grid.
      * @param ge The GridEntity to remove.
      */
 	public void removeEntity(GridEntity ge) {
 		// TODO Method stub
 		throw new UnsupportedOperationException();
 	}
 
 	/**
 	 * Adds the given entity to the grid.
 	 * @param ge The GridEntity to add.
 	 */
 	public void addEntity(GridEntity ge) {
 		// TODO Method stub
 		throw new UnsupportedOperationException();
 	}
 
 	public Field getField(String fieldName) {
 		// TODO Auto-generated method stub
 		throw new UnsupportedOperationException();
 	}
 	
 	/**
 	 * Makes a new Layer.
 	 * @param fieldName The name of the Field that the Layer will represent
 	 * @param c The Color that will be shaded differently to represent Field values
 	 */
 	public void newLayer(String fieldName, Color c) {
 		Layer.getInstance().setFieldName(fieldName);
 		Layer.getInstance().setColor(c);
 		Layer.getInstance().resetMinMax();
 	}
 
 }
