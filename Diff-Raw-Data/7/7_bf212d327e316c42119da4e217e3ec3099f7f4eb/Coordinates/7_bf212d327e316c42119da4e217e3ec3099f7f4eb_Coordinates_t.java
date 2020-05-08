 package util;
 
 import action.Action;
 
 import java.util.Random;
 
 public class Coordinates {
 	private int x, y;
 
     /**
      * Creates a Coordinates instance with random x, y values.
      */
     public Coordinates() {
         Random r = new Random();
         this.x = r.nextInt(Util.DIM);
         this.y = r.nextInt(Util.DIM);
     }
 	public Coordinates(int x, int y) { this.x = x;	this.y = y; }
 	
 	public Coordinates(Coordinates c) { this.x = c.x; this.y = c.y; }
 
	// FIXME: should not be mutable
     public void set(Coordinates c) { this.x = c.x; this.y = c.y; }
 
 	public int getX() { return x; }	
 	public int getY() { return y; }
 	@Override
 	public String toString() { return "(" + this.x + ", " + this.y + ")"; }
 	
 	@Override
 	public boolean equals(Object other) {
 		if (other == null) return false;
 	    if (other == this) return true;
 	    if (!(other instanceof Coordinates)) { return false; }
 	    Coordinates otherCoordinates = (Coordinates) other;
 		return this.getX() == otherCoordinates.getX() 
 				&& this.getY() == otherCoordinates.getY();
 	}
 	
 	/**
 	 * Returns the action required to move from this coordinates to coordinates other.
 	 * (requires this to be neighboring to other)
 	 */
 	public Action.action getTransitionAction(Coordinates other) {
 		int xOther = other.x, yOther = other.y;
 		if (x == xOther	&& y == yOther) { return Action.action.WAIT; }
 		if (x == xOther - 1 || (xOther == 0 && x == 10)) { return Action.action.SOUTH; }
 		if (x == xOther + 1 || (xOther == Util.DIM-1 && x == 0)) { return Action.action.NORTH; }
 		if (y == yOther - 1 || (yOther == 0 && y == 10)) { return Action.action.EAST; }
 		if (y == yOther + 1 || (yOther == Util.DIM-1 && y == 0)) { return Action.action.WEST; }
 		return null;
 	}
 	
 	/**
 	 * this is a simple function that calculates the opposite of an action
 	 * @param other the other pair of coordinates (besides 'this')
 	 * @return action
 	 */
 	public Action.action getOppositeTransitionAction(Coordinates other) {
         Action.action ta = this.getTransitionAction(other);
 		return ta.getOpposite();
 	}
 
 	/**
 	 * Returns the new coordinates after taking action a 
 	 */
 	public Coordinates getShifted(Action.action a) {
 		Coordinates dest = null;
 		switch (a) {
 			case WAIT:
 				dest = new Coordinates(this);
 				break;
 			case NORTH:
 				dest = new Coordinates(
 					this.getX() - 1 < 0 
 						? Util.DIM - 1 
 						: this.getX() - 1,
 						this.getY()
 				);
 				break;
 			case SOUTH:
 				dest = new Coordinates(
 						this.getX() + 1 > Util.DIM - 1 
 						? 0 
 						: this.getX() + 1,
 						this.getY()
 				);
 				break;
 			case EAST:
 				dest = new Coordinates(
 						this.getX(),
 						this.getY()+1 > Util.DIM-1
 						? 0
 					    : this.getY() + 1
 					);
 				break;
 			case WEST:
 				dest = new Coordinates(
 						this.getX(),
 						this.getY() - 1 < 0
 						? Util.DIM - 1
 						: this.getY() - 1
 				);
 				break;
 		}
 		return dest;
 	}
 	
 }
