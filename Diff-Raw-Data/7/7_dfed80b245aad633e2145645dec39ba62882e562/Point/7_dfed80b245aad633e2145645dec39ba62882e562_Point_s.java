 package machine.turing;
 
 /**
  * This class represents a position in an 2-dimensional environment via x and y coordinates
  * @author nils
  *
  */
public class Point {
 	private int x;
 	private int y;
 	
 	/**
 	 * Constructs a new point with given x and y coords
 	 * @param x The x coordinate
 	 * @param y The y coordinate
 	 */
 	public Point(int x, int y) {
 		super();
 		this.x = x;
 		this.y = y;
 	}
 
 	/**
 	 * Returns the x coordinate of the point
 	 * @return The value of the x coordinate
 	 */
 	public int getX() {
 		return x;
 	}
 	/**
 	 * Sets the x coordinate of the point
 	 * @param x The new value for the x coordinate
 	 */
 	public void setX(int x) {
 		this.x = x;
 	}
 	/**
 	 * Returns the y coordinate of the point
 	 * @return The value of the y coordinate
 	 */
 	public int getY() {
 		return y;
 	}
 	/**
 	 * Sets the y coordinate of the point
 	 * @param y The new value for the y coordinate
 	 */
 	public void setY(int y) {
 		this.y = y;
 	}
 	
 	@Override
 	public Object clone() {
 		return new Point(new Integer(x), new Integer(y));
 	}
 }
