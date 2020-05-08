 package gdp.racetrack;
 
 import java.util.LinkedList;
 import java.util.List;
 
 public abstract class Player {
 
 	private final LinkedList<Vec2D> turnHistory = new LinkedList<Vec2D>();
 
 	/**
 	 * Elaborate the next turn of the player and return its destination.
 	 * @return The destination point of the next turn
 	 */
	public abstract Point turn();
 
 	/**
 	 * Gets the actual position of the player as Vec2D from bottom left corner.
 	 * @return The position of the player
 	 */
	public final Point getPosition() {
 		throw new UnsupportedOperationException("The method is not implemented yet.");
 	}
 
 	/**
 	 * Gets the "velocity" of the move in last turn of the player.
 	 * @return The velocity of the last move
 	 */
 	public final Vec2D getVelocity() {
 		throw new UnsupportedOperationException("The method is not implemented yet.");
 	}
 
 	/**
 	 * Gets a history of the movements of the player.
 	 * use points or velocities?
 	 * @return History of movements
 	 */
 	public final List<Vec2D> getTurnHistory() {
 		throw new UnsupportedOperationException("The method is not implemented yet.");
 	}
 
 	/**
 	 * Sets the position of this player.
 	 * @param position The new position of the player
 	 */
	public final void setPosition(Point position) {
 		throw new UnsupportedOperationException("The method is not implemented yet.");
 	}
 
 }
