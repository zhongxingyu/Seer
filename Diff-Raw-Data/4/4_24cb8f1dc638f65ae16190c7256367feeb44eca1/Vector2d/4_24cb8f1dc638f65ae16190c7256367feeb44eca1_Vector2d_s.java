 package rsmg.util;
 /**
  * Class for representing a two dimensional mathematical vector.
  * @author Johan Grnvall
  * @author Daniel Jonsson
  */
 public class Vector2d {
 	
 	private double x;
 	private double y;
 	
 	/**
 	 * Creates a new vector null vector.
 	 */
 	public Vector2d() {
 		x = 0;
 		y = 0;
 	}
 
 	/**
 	 * Creates a new vector with the specified x and y lengths.
 	 * @param x X length.
 	 * @param y Y length.
 	 */
 	public Vector2d(double x, double y) {
 		this.x = x;
 		this.y = y;
 	}
 
 	/**
 	 * Adds a vector to this vector.
 	 * @param vector The vector you want to add to this one.
 	 */
 	public void add(Vector2d vector) {
 		add(vector.getX(), vector.getY());
 	}
 
 	/**
 	 * Adds X and Y lengths to the vector.
 	 * @param x The X length that should be added.
 	 * @param y The Y length that should be added.
 	 */
 	public void add(double x, double y) {
		this.x = x;
		this.y = y;
 	}
 	
 	/**
 	 * Subtracts current vector with the specified vector.
 	 * @param vector The vector that this vector should be subtracted with.
 	 */
 	public void subtract(Vector2d vector) {
 		subtract(vector.getX(), vector.getY());
 	}
 
 	/**
 	 * Subtracts the X and Y lengths with the specified amount.
 	 * @param x The X length that should be subtracted.
 	 * @param y The Y length that should be subtracted.
 	 */
 	public void subtract(double x, double y) {
 		add(-x, -y);
 	}
 
 	/**
 	 * Sets the horizontal size.
 	 * @param x X length of the vector.
 	 */
 	public void setX(double x) {
 		this.x = x;
 	}
 	
 	/**
 	 * Gets the horizontal size of the vector.
 	 * @return The horizontal size of the vector.
 	 */
 	public double getX() {
 		return x;
 	}
 
 	/**
 	 * Sets the vertical size.
 	 * @param x Y length of the vector.
 	 */
 	public void setY(double y) {
 		this.y = y;
 	}
 	
 	/**
 	 * Gets the vertical size of the vector.
 	 * @return The vertical size of the vector.
 	 */
 	public double getY() {
 		return y;
 	}
 	
 	/**
 	 * This method returns the resulting vectors length using Pythagoras theorem.
 	 * @return The length of the resulting vector.
 	 */
 	public double getlength() {
 		return Math.sqrt(x * x + y * y);
 	}
 	
 }
