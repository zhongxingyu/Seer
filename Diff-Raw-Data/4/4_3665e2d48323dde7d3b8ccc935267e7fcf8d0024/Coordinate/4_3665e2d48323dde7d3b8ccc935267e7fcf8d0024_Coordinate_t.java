 package ocr.info;
 
 import java.io.Serializable;
 
 /**
  * Container for grid coordinates, 0-based
  *
  * @author Jonathan Reimels
  * @version 1.0.0
  */
 public class Coordinate implements Serializable {
 	/**
 	 * generated Serial Version UID
 	 */
 	private static final long serialVersionUID = 8238698537968720873L;
 
 	// instance variables
 	private int _row, _col;
 
 	/**
 	 * Default Constructor
 	 */
 	public Coordinate() {}
 
 	/**
 	 * Constructor - set row and column of coordinate
 	 * @param row - The row of the coordinate
 	 * @param col - The column of the coordinate
 	 */
 	public Coordinate(int row, int col) {
 		_row = row;
 		_col = col;
 	}
 
 	/**
 	 * Clone a new instance of the object with the same values
 	 */
 	@Override
 	public Coordinate clone() {
 		return new Coordinate(_row, _col);
 	}
 
 	/**
 	 * Set the row for the coordinate
 	 * @param row - The row to set
 	 */
 	public void setRow(int row) {
 		_row = row;
 	}
 
 	/**
 	 * Get the row for the coordinate
 	 * @return Row
 	 */
 	public int getRow() {
 		return _row;
 	}
 
 	/**
 	 * Set the column for the coordinate
 	 * @param col - The column to set
 	 */
 	public void setCol(int col) {
 		_col = col;
 	}
 
 	/**
 	 * Get the column for the coordinate
 	 * @return Column
 	 */
 	public int getCol() {
 		return _col;
 	}
 
 	/**
 	 * Check if coordinate is equal in value to another coordinate
	 * @param coord - Coordinate to compare to
	 * @return true if coordinates are equal
 	 */
 	public boolean isEqual(Coordinate coord) {
 		if (coord.getRow() == _row && coord.getCol() == _col) {
 			return true;
 		}
 		return false;
 	}
 }
 
