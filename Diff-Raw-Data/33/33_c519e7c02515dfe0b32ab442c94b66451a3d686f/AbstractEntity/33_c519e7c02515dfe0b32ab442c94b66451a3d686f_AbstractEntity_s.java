 package ultraextreme.model.entity;
 
 import javax.vecmath.Vector2d;
 
 import ultraextreme.model.util.Constants;
 import ultraextreme.model.util.Dimension;
 import ultraextreme.model.util.Direction;
 import ultraextreme.model.util.Position;
 
 /**
  * An abstract class representing an in-game "physical" entity.
  * 
  * @author Bjorn Persson Mattsson, Viktor Anderling, Johan Gronvall
  * 
  */
 public abstract class AbstractEntity {
 
 	private Position position;
 
 	private Direction direction;
 
 	private int width;
 
 	private int height;
 
 	/**
 	 * Creates and entity at the position 0,0 and with the side 0
 	 */
 	public AbstractEntity() {
 		this(0, 0, 0, 0, new Direction(0));
 	}
 
 	public AbstractEntity(double x, double y, int width, int height,
 			Direction direction) {
 		this.direction = direction;
 		this.position = new Position(x, y);
 		this.width = width;
 		this.height = height;
 	}
 
 	/**
 	 * Move the entity a given x an y units.
 	 * 
 	 * @param x
 	 *            Number of x units the entity shall move with.
 	 * @param y
 	 *            Number of y units the entity shall move with.
 	 */
 	public void move(double x, double y) {
 		Vector2d rotatedVector = direction.getRotatedCoordinates(x, y);
		position.setX(rotatedVector.x);
		position.setY(rotatedVector.y);
 	}
 
 	/**
 	 * Sets the position of this entity to the given position.
 	 * 
 	 * @param position
 	 *            The given position.
 	 */
 	public void setPosition(Position position) {
 		this.position = new Position(position);
 	}
 
 	/**
 	 * Returns this entity's position.
 	 * 
 	 * @return A new position with the same values as this position.
 	 */
 	public Position getPosition() {
 		return new Position(this.position);
 	}
 
 	/**
 	 * Returns true if and only if the entity is entirely outside of the screen
 	 * 
 	 * @return true if and only if the entity is entirely outside of the screen
 	 */
 	public boolean isOutOfScreen() {
 		Dimension screen = Constants.getInstance().getLevelDimension();
 		return position.getY() - height < 0 || position.getX() - width < 0
 				|| position.getX() > screen.getX()
 				|| position.getY() > screen.getY();
 	}
 
 	/**
 	 * Determines whether this entity collides with another entity.
 	 * 
 	 * @param other
 	 *            The other entity.
 	 * @return true if and only if the two entities are colliding.
 	 */
 	public boolean collidesWith(AbstractEntity other) {
 		return false;
 	}
 
 	// public void getHitbox() {
 	// }
 
 	public int getWidth() {
 		return width;
 	}
 
 	public int getHeight() {
 		return height;
 	}
 
 	/**
 	 * Returns The direction this entity are facing.
 	 * 
 	 * @return The direction this entity are facing.
 	 */
 	public Direction getDirection() {
 		return direction;
 	}
 
 	public abstract double getSpeedMod();
 }
