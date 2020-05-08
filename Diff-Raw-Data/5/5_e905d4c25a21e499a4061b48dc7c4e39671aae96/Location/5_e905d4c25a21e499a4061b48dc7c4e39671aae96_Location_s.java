 package Utils;
 
 public class Location {
 	private int x;
 	private int y;
 	
 	public Location(int newX, int newY) {
 		x = newX;
 		y = newY;
 	}
 	
 	/**
 	 * Find the distance from one Location object to another by passing in another Location object -Aaron
 	 */
 	public double distance(Location l) {
 		return Math.sqrt(Math.pow(l.getX()-getX(),2)+Math.pow(l.getY()-getY(),2));
 	}
 	
 	public int getX() {
 		return x;
 	}
 	
 	public int getY() {
 		return y;
 	}
 	
 	/**
 	 * Call this with no params to increment x by 1. Returns x after increment.
 	 */
 	public int incrementX() {
 		incrementX(1);
 		return x;
 	}
 	
 	/**
 	 * Specify how much to increase to x. Can be negative. Returns x after increment. 
 	 */
 	public int incrementX(int toAdd) {
		incrementX(toAdd);
 		return x;
 	}
 	
 	/**
 	 * Call this with no params to increment y by 1. Returns y after increment.
 	 */
 	public int incrementY() {
 		incrementY(1);
 		return y;
 	}
 	
 	/**
 	 * Specify how much to increase to y. Can be negative. Returns y after increment. 
 	 */
 	public int incrementY(int toAdd) {
		incrementY(toAdd);
 		return y;
 	}
 	
 	public void setX(int newX) {
 		x = newX;
 	}
 	
 	public void setY(int newY) {
 		y = newY;
 	}
 	
 	public boolean equals(Location otherLoc) {
 		return x == otherLoc.getX() && y == otherLoc.getY(); 
 	}
 }
