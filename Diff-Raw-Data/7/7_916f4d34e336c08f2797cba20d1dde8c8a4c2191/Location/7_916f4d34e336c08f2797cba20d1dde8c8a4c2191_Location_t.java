 package cluedo.structs;
 
 import cluedo.main.Board;
 
 /**
  * a pair of x and y coordinates
  * 
  * bad encapsulation should make it easier to find where a player is at.
  * 
  * @author Michael
  * @param <T>
  * 
  */
public class Location implements Comparable<Location> {
 	private int x;
 	private int y;
 
 	public Location(Location l) {
 		this.x = l.x;
 		this.y = l.y;
 	}
 
 	public Location(int x, int y) {
 		this.x = x;
 		this.y = y;
 	}
 
 	public Location() {
 		// TODO Auto-generated constructor stub
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see java.lang.Object#hashCode()
 	 */
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = 1;
 		result = prime * result + x;
 		result = prime * result + y;
 		return result;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see java.lang.Object#equals(java.lang.Object)
 	 */
 	@Override
 	public boolean equals(Object obj) {
 		if (this == obj)
 			return true;
 		if (obj == null)
 			return false;
 		if (getClass() != obj.getClass())
 			return false;
 		Location other = (Location) obj;
 		if (x != other.x)
 			return false;
 		if (y != other.y)
 			return false;
 		return true;
 	}
 
 	/**
 	 * @return the x
 	 */
 	public int getX() {
 		return x;
 	}
 
 	/**
 	 * @param x
 	 *            the x to set
 	 */
 	public void setX(int x) {
 		this.x = x;
 	}
 
 	/**
 	 * @return the y
 	 */
 	public int getY() {
 		return y;
 	}
 
 	/**
 	 * @param y
 	 *            the y to set
 	 */
 	public void setY(int y) {
 		this.y = y;
 	}
 
 	/**
 	 * Checks if a given location is within the confines of the board.
 	 * 
 	 * @param l
 	 * @return
 	 */
 	public static boolean isValid(Location l) {
 		return (l.getX() >= 0 && l.getX() < Board.BOARD_WIDTH && l.getY() >= 0 && l
 				.getY() < Board.BOARD_HEIGHT);
 	}
 
 	@Override
	public int compareTo(Location obj) {
 		// TODO Auto-generated method stub
 
 		if (equals(obj)) {
 			return 0;
 		}
 
 		if (getClass() != obj.getClass())
 			return 0;
 		Location other = (Location) obj;
 		if (x * y < other.x * other.y) {
 			return -1;
 		}
 		
 		return 1;
 	}
 }
