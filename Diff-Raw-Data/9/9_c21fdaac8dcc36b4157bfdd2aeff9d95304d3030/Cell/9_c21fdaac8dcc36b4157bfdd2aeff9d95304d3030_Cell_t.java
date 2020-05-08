 package edu.ncsu.csc216.fire;
 
 import java.util.Random;
 
 /**
  * The cell object which holds state
  * @author Andrew Kofink and James Bruening
  */
 public class Cell {
 	
 	/**
 	 * An empty cell
 	 */
 	public static final int EMPTY = 0;
 	
 	/**
 	 * A tree cell
 	 */
 	public static final int TREE = 1;
 	
 	/**
 	 * A burning cell
 	 */
 	public static final int BURNING = 2;
 	
 	/**
 	 * Holds the state of the cell
 	 */
 	private int state;
 	
 	/**
 	 * A Cell object inside a Grid
 	 * @param state The state of the cell
 	 */
 	public Cell(int state) {
 		this.state = state;
 	}
 	
 	/**
 	 * Gets the state of a cell
 	 * @return The state of the cell
 	 */
 	public int getState() {
 		return state;
 	}
 	
 	/**
 	 * Sets the state of the cell
 	 * @param state the state of the cell
 	 */
 	public void setState(int state) {
 		this.state = state;
 	}
 	
 	/**
 	 * Copies the Cell
 	 * @return The new cell newCell
 	 */
 	public Cell copy() {
 		Cell newCell = new Cell(this.getState());
 		return newCell;
 	}
 	
 	/**
 	 * Spreads the fire based on the probability of probCatch
 	 * @param probCatch the probability that the fire will spread
 	 * @param north the north tree
 	 * @param east the east tree
 	 * @param south the south tree
 	 * @param west the west tree
 	 */
 	public void spread(double probCatch, Cell north, Cell east, Cell south, Cell west) {
 		if (north.getState() == Cell.BURNING || east.getState() == Cell.BURNING || south.getState() == Cell.BURNING || west.getState() == Cell.BURNING) {
 			Random rand = new Random();
			if (rand.nextDouble() < probCatch) {
 				this.setState(Cell.BURNING);
 			}
 		}
 	}	
 }
