 package jgame;
 
 import java.awt.AlphaComposite;
 import java.awt.Graphics2D;
 import java.awt.RenderingHints;
 import java.awt.Shape;
 import java.awt.geom.AffineTransform;
 import java.awt.geom.Area;
 import java.awt.geom.Point2D;
 import java.awt.geom.Rectangle2D;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.LinkedHashSet;
 import java.util.List;
 import java.util.Set;
 
 import jgame.controller.Controller;
 import jgame.listener.Listener;
 
 /**
  * A standard game object. All game objects should inherit from this class. This
  * class provides basic functionality such as position (via the {@link #x} and
  * {@link #y} variables), size (via the {@link #width} and {@link #height})
  * variables, rotation (via the {@link #rotation}) variable, customizable anchor
  * weights, a controller system, and a listener system.
  * 
  * @author William Chargin
  * 
  */
 public class GObject implements GPaintable, GObjectHolder {
 
 	/**
 	 * Sets the specified graphics context to be antialiased. This will smooth
 	 * out lines, curves, and text drawn with the graphics commands, but not
 	 * images imported from files. To smooth images that have been rotated or
 	 * scaled, use {@link #goodImageTransforms(Graphics2D)}.
 	 * 
 	 * @param g
 	 *            the context to antialias
 	 */
 	protected static void antialias(Graphics2D g) {
 		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
 				RenderingHints.VALUE_ANTIALIAS_ON);
 	}
 
 	/**
 	 * Maximizes the quality of image transforms (rotate, scale, skew, etc.) for
 	 * the specified graphics context. This will smooth almost all jagged edges
 	 * caused by transforming images but significantly increase render times.
 	 * Only use this is you have tried {@link #goodImageTransforms(Graphics2D)}
 	 * and it does not provide good enough quality.
 	 * 
 	 * @param g
 	 *            the context to antialias
 	 */
 	protected static void goodImageTransforms(Graphics2D g) {
 		g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
 				RenderingHints.VALUE_INTERPOLATION_BICUBIC);
 	}
 
 	/**
 	 * The x-coordinate of this object with respect to its parent. This value
 	 * may be negative, positive, or zero. By convention, low values of x are at
 	 * the left of the screen, and high values of x are at the right.
 	 */
 	private double x = 0;
 
 	/**
 	 * The y-coordinate of this object with respect to its parent. This value
 	 * may be negative, positive, or zero. By convention, low values of y are at
 	 * the top of the screen, and high values of y are at the bottom.
 	 */
 	private double y = 0;
 
 	/**
 	 * The width of this object. This value may be positive or zero, but may not
 	 * be negative.
 	 */
 	private double width = 0;
 
 	/**
 	 * The height of this object. This value may be positive or zero, but may
 	 * not be negative.
 	 */
 	private double height;
 
 	/**
 	 * The rotation of this object, in degrees, clockwise, with 0&deg; facing
 	 * due east.
 	 */
 	private double rotation;
 
 	/**
 	 * The scale along the x axis.
 	 */
 	private double scaleX = 1;
 
 	/**
 	 * The scale along the y axis.
 	 */
 	private double scaleY = 1;
 
 	/**
 	 * This object's alpha/opacity.
 	 */
 	private double alpha = 1.0;
 
 	/**
 	 * This object's visibility.
 	 */
 	private boolean visible = true;
 
 	/**
 	 * The anchor weight along the x-axis.
 	 */
 	private double anchorWeightX = 0.5d;
 
 	/**
 	 * The anchor weight along the y-axis.
 	 */
 	private double anchorWeightY = 0.5d;
 
 	/**
 	 * The parent of this object.
 	 */
 	private GObject parent;
 
 	/**
 	 * The list of controllers on this object.
 	 */
 	private final List<Controller> controllers = new ArrayList<Controller>();
 
 	/**
 	 * The list of controllers to be added.
 	 */
 	private final List<Controller> controllersToBeAdded = new ArrayList<Controller>();
 
 	/**
 	 * The list of controllers to be removed.
 	 */
 	private final List<Controller> controllersToBeRemoved = new ArrayList<Controller>();
 
 	/**
 	 * The list of listeners on this object.
 	 */
 	private final List<Listener> listeners = new ArrayList<Listener>();
 
 	/**
 	 * The list of listeners to be added.
 	 */
 	private final List<Listener> listenersToBeAdded = new ArrayList<Listener>();
 
 	/**
 	 * The list of listeners to be removed.
 	 */
 	private final List<Listener> listenersToBeRemoved = new ArrayList<Listener>();
 
 	/**
 	 * The set of subcomponents.
 	 */
 	private final Set<GObject> subcomponents = new LinkedHashSet<GObject>();
 
 	/**
 	 * Creates the object.
 	 */
 	public GObject() {
 		super();
 	}
 
 	@Override
 	public void add(GObject object) {
 		// Add to the set of subcomponents.
 		subcomponents.add(object);
 
 		// Set the parent.
 		object.setParent(this);
 	}
 
 	/**
 	 * Adds the specified object at the given location. This is the same as
 	 * calling {@link #add(GObject)} followed by
 	 * {@link #setLocation(double, double)} on the object.
 	 * 
 	 * @param object
 	 *            the object to add
 	 * @param x
 	 *            the new x-position of the object
 	 * @param y
 	 *            the new y-position of the object
 	 */
 	public void addAt(GObject object, int x, int y) {
 		add(object);
 		object.setLocation(x, y);
 	}
 
 	/**
 	 * Adds the object at the center of this component. This is equivalent to
 	 * calling {@link #add(GObject)} and then
 	 * {@link #snapChildToCenter(GObject)}.
 	 * 
 	 * @param object
 	 *            the object to add
 	 */
 	public void addAtCenter(GObject object) {
 		// Add the object.
 		add(object);
 
 		// Center.
 		snapChildToCenter(object);
 	}
 
 	/**
 	 * Adds the given controller to the controller list.
 	 * 
 	 * @param c
 	 *            the controller to add
 	 */
 	public void addController(Controller c) {
 		controllersToBeAdded.add(c);
 	}
 
 	/**
 	 * Adds the given listener to the listener list.
 	 * 
 	 * @param l
 	 *            the listener to add
 	 */
 	public void addListener(Listener l) {
 		listenersToBeAdded.add(l);
 	}
 
 	/**
 	 * Attempts to add the given object as a sibling of this object. If this
 	 * object's parent is {@code null}, then no action will be performed and no
 	 * error will be thrown.
 	 * 
 	 * @param object
 	 *            the object to add as a sibling
 	 * @return {@code true} if the object was successfully added, or
 	 *         {@code false} if it was not
 	 */
 	public boolean addSibling(GObject object) {
 		// Perform a null-check.
 		if (parent == null) {
 			// The object couldn't be added.
 			return false;
 		} else {
 			// Add the object.
 			parent.add(object);
 
 			// The object was added.
 			return true;
 		}
 	}
 
 	/**
 	 * Determines the angle from this object's anchor point to the point
 	 * specified by the given coordinates.
 	 * 
 	 * @param x
 	 *            the x coordinate of the point for which to calculate the angle
 	 * @param y
 	 *            the y coordinate of the point for which to calculate the angle
 	 * @return the angle, in degrees, clockwise, where 0&deg; means due east, or
 	 *         {@code 0} if the two points are coincident
 	 */
 	public double angleTo(double x, double y) {
 		// Same?
 		if (x == this.x && y == this.y) {
 			// Don't want to divide by zero.
 			return 0;
 		}
 
 		// Else...
 		return Math.toDegrees(Math.atan2(y - this.y, x - this.x));
 	}
 
 	/**
 	 * Determines the angle from this object's anchor point to the given
 	 * object's anchor point.
 	 * 
 	 * @param other
 	 *            the object for which to calculate the angle
 	 * @return the angle, in degrees, clockwise, where 0&deg; means due east, or
 	 *         {@code 0} if the objects have the same anchor point
 	 * @throws IllegalArgumentException
 	 *             if {@code other} is {@code null}
 	 */
 	public double angleTo(GObject other) throws IllegalArgumentException {
 		// Perform a null-check.
 		if (other == null) {
 			// other == null.
 			throw new IllegalArgumentException("other == null");
 		}
 		return angleTo(other.x, other.y);
 	}
 
 	/**
 	 * Determines the angle from this object's anchor point to the given point.
 	 * 
 	 * @param point
 	 *            the point for which to calculate the angle
 	 * @return the angle, in degrees, clockwise, where 0&deg; means due east, or
 	 *         {@code 0} if the two points are coincident
 	 * @throws IllegalArgumentException
 	 *             if {@code point} is {@code null}
 	 */
 	public double angleTo(Point2D point) {
 		// Perform a null-check.
 		if (point == null) {
 			// point == null.
 			throw new IllegalArgumentException("point == null");
 		}
 		return angleTo(point.getX(), point.getY());
 	}
 
 	/**
 	 * Moves this object a given percentage of the distance between itself and
 	 * another object. This differs from {@link #moveToward(double, GObject)} in
 	 * that it accepts not a fixed distance in pixels but rather a percentage of
 	 * the distance between the two objects. For example, passing {@code 0.5} as
 	 * {@code percentage} would cause this object to move to the midpoint of the
 	 * line segment formed by the original anchor point of this object and that
 	 * of the target object, while a percentage of {@code 1.0} would perform the
 	 * same function as {@link #moveTo(GObject)}. If the distance to the other
 	 * object is zero, no action will be performed and no error will be thrown.
 	 * 
 	 * @param percentage
 	 *            the percentage of the distance to move
 	 * @param other
 	 *            the object to move toward.
 	 */
 	public void approach(double percentage, GObject other) {
 		if (percentage == 1) {
 			moveTo(other);
 			return;
 		}
 		double distance = percentage * distanceTo(other);
 		if (distance == 0) {
 			return;
 		}
 		moveToward(distance, other);
 	}
 
 	@Override
 	public void componentRemoved(GPaintable object) {
 		// If we had this object, remove it from the set.
 		subcomponents.remove(object);
 	}
 
 	/**
 	 * Calculates the distance from this object's anchor point to the given
 	 * point. The points are assumed to be in the same coordinate space.
 	 * 
 	 * @param x
 	 *            the x coordinate of the point for which to calculate distance
 	 * @param y
 	 *            the y coordinate of the point for which to calculate distance
 	 * @return the distance, in pixels
 	 */
 	public double distanceTo(double x, double y) {
 		return Point2D.distance(x, y, this.x, this.y);
 	}
 
 	/**
 	 * Calculates the distance from this object's anchor point to the given
 	 * object's anchor point.
 	 * 
 	 * @param other
 	 *            the object for which to calculate distance
 	 * @return the distance, in pixels
 	 * @throws IllegalArgumentException
 	 *             if {@code other} is {@code null}
 	 */
 	public double distanceTo(GObject other) throws IllegalArgumentException {
 		// Perform a null-check.
 		if (other == null) {
 			// other == null.
 			throw new IllegalArgumentException("other == null");
 		}
 
 		// Get the anchor points in absolute space.
 		Point2D otherPoint = other.getRelativeToAbsoluteTransform().transform(
 				other.getAnchorPoint(), null);
 		Point2D thisPoint = getRelativeToAbsoluteTransform().transform(
 				getAnchorPoint(), null);
 
 		// Calculate and return the distance.
 		return thisPoint.distance(otherPoint);
 	}
 
 	/**
 	 * Calculates the distance from this object's anchor point to the given
 	 * point. The points are assumed to be in the same coordinate space.
 	 * 
 	 * @param point
 	 *            the point for which to calculate distance
 	 * @return the distance, in pixels
 	 * @throws IllegalArgumentException
 	 *             if {@code point} is {@code null}
 	 */
 	public double distanceTo(Point2D point) throws IllegalArgumentException {
 		// Perform a null-check.
 		if (point == null) {
 			// point == null.
 			throw new IllegalArgumentException("point == null");
 		}
 
 		// Calculate and return the distance.
 		return getAnchorPoint().distance(point);
 	}
 
 	/**
 	 * Causes this object to face the point specified by the given coordinates.
 	 * This is equivalent to
 	 * 
 	 * <pre>
 	 * <code>{@link #setRotation(double) setRotation}({@link #angleTo(double, double) angleTo}(x, y))</code>
 	 * </pre>
 	 * 
 	 * @param x
 	 *            the x coordinate of the point to face
 	 * @param y
 	 *            the y coordinate of the point to face
 	 */
 	public void face(double x, double y) {
 		setRotation(angleTo(x, y));
 	}
 
 	/**
 	 * Causes this object to face the given object. This is equivalent to
 	 * 
 	 * <pre>
 	 * <code>{@link #setRotation(double) setRotation}({@link #angleTo(GObject) angleTo}(other))</code>
 	 * </pre>
 	 * 
 	 * @param other
 	 *            the object to face
 	 * @throws IllegalArgumentException
 	 *             if {@code other} is {@code null}
 	 */
 	public void face(GObject other) throws IllegalArgumentException {
 		// Perform a null-check.
 		if (other == null) {
 			// other == null.
 			throw new IllegalArgumentException("other == null");
 		}
 		setRotation(angleTo(other));
 	}
 
 	/**
 	 * Causes this object to face the given point. This is equivalent to
 	 * 
 	 * <pre>
 	 * <code>{@link #setRotation(double) setRotation}({@link #angleTo(Point2D) angleTo}(point))</code>
 	 * </pre>
 	 * 
 	 * @param point
 	 *            the point to face
 	 * @throws IllegalArgumentException
 	 *             if {@code point} is {@code null}
 	 */
 	public void face(Point2D point) throws IllegalArgumentException {
 		// Perform a null-check.
 		if (point == null) {
 			// point == null.
 			throw new IllegalArgumentException("point == null");
 		}
 		setRotation(angleTo(point));
 	}
 
 	/**
 	 * Gets this object's bounding shape in absolute coordinates.
 	 * 
 	 * @return the absolute bounding shape
 	 */
 	public final Shape getAbsoluteBoundingShape() {
 		// NOTE that this is similar but not identical to getRelativeToAbsolute
 		// transform.
 
 		// Create a transform to start with.
 		AffineTransform at = new AffineTransform();
 
 		// Start with this object's parent...
 		GObject parent = getParent();
 
 		// ... and keep going until we're out of parents.
 		while (parent != null) {
 			// Concatenate with the positive (false) transform.
 			at.concatenate(parent.getTransform(false));
 
 			// Is this the last one?
 			if (parent.getParent() != null) {
 				// Add a translate as well.
 				at.translate(
 						parent.getX() - parent.getWidth()
 								* parent.getAnchorWeightX(),
 						parent.getY() - parent.getHeight()
 								* parent.getAnchorWeightY());
 			}
 
 			// Then go up one level.
 			parent = parent.getParent();
 		}
 
 		// Account for our transform.
 		at.concatenate(getTransform(false));
 
 		// If we're not top-level...
 		if (hasParent()) {
 			// Account for our translation.
 			at.translate(x - width * anchorWeightX, y - height * anchorWeightY);
 		}
 
 		// Get and return the shape.
 		return at.createTransformedShape(getBoundingShape());
 	}
 
 	/**
 	 * Creates an {@link AffineTransform} to transform points from the absolute
 	 * coordinate space back to this coordinate space by traversing the parent
 	 * hierarchy tree.
 	 * <p>
 	 * The transform generated by this method is the inverse of the transform
 	 * generated by {@link #getRelativeToAbsoluteTransform()}.
 	 * 
 	 * @return a transform
 	 */
 	public AffineTransform getAbsoluteToRelativeTransform() {
 		// Create a transform to start with.
 		AffineTransform transform = new AffineTransform();
 
 		// Start with this object's parent...
 		GObject parent = getParent();
 
 		// ... and keep going until we're out of parents.
 		while (parent != null) {
 			// Concatenate with the positive (false) rotate/scale transform.
 			transform.concatenate(parent.getTransform(false));
 
 			// Is this the last one?
 			if (parent.getParent() != null) {
 				// Add a translate as well.
 				transform.translate(
 						-parent.getX() + parent.getWidth()
 								* parent.getAnchorWeightX(),
 						-parent.getY() + parent.getHeight()
 								* parent.getAnchorWeightY());
 			}
 
 			// Then go up one level.
 			parent = parent.getParent();
 		}
 
 		// Return the answer.
 		return transform;
 	}
 
 	/**
 	 * Gets the object's alpha.
 	 * 
 	 * @return the new alpha
 	 */
 	public double getAlpha() {
 		return alpha;
 	}
 
 	/**
 	 * Gets the anchor point of this object.
 	 * 
 	 * @return the anchor point
 	 */
 	public Point2D getAnchorPoint() {
 		return new Point2D.Double(x, y);
 	}
 
 	/**
 	 * Gets the rotation weight along the x-axis.
 	 * 
 	 * @return the rotation weight
 	 * @see #anchorWeightX
 	 */
 	public double getAnchorWeightX() {
 		return anchorWeightX;
 	}
 
 	/**
 	 * Gets the rotation weight along the y-axis.
 	 * 
 	 * @return the rotation weight
 	 * @see #anchorWeightY
 	 */
 	public double getAnchorWeightY() {
 		return anchorWeightY;
 	}
 
 	/**
 	 * Creates and returns a {@code Rectangle} with the x-coordinate,
 	 * y-coordinate, width, and height of this object in the parent coordinate
 	 * space.
 	 * 
 	 * @return the bounding box of this object
 	 */
 	public Rectangle2D getBoundingBox() {
 		return new Rectangle2D.Double(x - width * anchorWeightX, y - height
 				* anchorWeightY, width, height);
 	}
 
 	/**
 	 * Gets the bounding shape of this object, <strong>without</strong>
 	 * accounting for translation or rotation transformations.
 	 * 
 	 * @return the bounding shape
 	 */
 	public Shape getBoundingShape() {
 		return new Rectangle2D.Double(0, 0, width, height);
 	}
 
 	/**
 	 * Gets the first ancestor of the given type. The "first" ancestor is the
 	 * ancestor that is the fewest levels above this object; for example, the
 	 * parent would take precedence over the parent's parent.
 	 * <p>
 	 * A subclass is also be considered a match: for example, if
 	 * {@code A extends GObject} and {@code B extends A}, a call to
 	 * {@code getFirstAncestorOf(A.class)} could also return any {@code B}.
 	 * 
 	 * @param clazz
 	 *            the class to search for
 	 * @return the first ancestor found, or {@code null} if none exists
 	 * @since 1.2
 	 */
 	public <T extends GObject> T getFirstAncestorOf(Class<T> clazz) {
 		// Start the trace.
 		GObject p = getParent();
 
 		// Loop.
 		while (p != null && !clazz.isInstance(p)) {
 			// Go up one level.
 			p = p.getParent();
 		}
 
 		// p is now the first parent or null if none was found.
 		return clazz.cast(p);
 	}
 
 	/**
 	 * Gets the height of this object.
 	 * 
 	 * @return the height
 	 */
 	public double getHeight() {
 		return height;
 	}
 
 	/**
 	 * Returns the integer height of this object. The integer height is the
 	 * {@linkplain Math#ceil(double) ceiling value} of the height property.
 	 * 
 	 * @return the integer height
 	 */
 	public int getIntHeight() {
 		return (int) Math.ceil(height);
 	}
 
 	/**
 	 * Returns the integer width of this object. The integer width is the
 	 * {@linkplain Math#ceil(double) ceiling value} of the width property.
 	 * 
 	 * @return the integer width
 	 */
 	public int getIntWidth() {
 		return (int) Math.ceil(width);
 	}
 
 	/**
 	 * Gets the last ancestor of the given type. The "last" ancestor is the
 	 * ancestor that is the most levels above this object; for example, the
 	 * parent's parent would take precedence over the parent.
 	 * <p>
 	 * A subclass is also be considered a match: for example, if
 	 * {@code A extends GObject} and {@code B extends A}, a call to
 	 * {@code getLastAncestorOf(A.class)} could also return any {@code B}.
 	 * 
 	 * @param clazz
 	 *            the class to search for
 	 * @return the last ancestor found, or {@code null} if none exists
 	 * @since 1.2
 	 */
 	public <T extends GObject> T getLastAncestorOf(Class<T> clazz) {
 		// Declare result.
 		T result = null;
 
 		// Start the trace.
 		GObject p = getParent();
 
 		// Loop.
 		while (p != null) {
 			// Check the type.
 			if (clazz.isInstance(p)) {
 				// Set the result.'
 				result = clazz.cast(p);
 			}
 
 			// Go up one level.
 			p = p.getParent();
 		}
 
 		// Go.
 		return result;
 	}
 
 	@Override
 	public Collection<GObject> getObjects() {
 		return Collections.unmodifiableSet(subcomponents);
 	}
 
 	/**
 	 * Gets the parent of this object.
 	 * 
 	 * @return the parent object
 	 */
 	public GObject getParent() {
 		return parent;
 	}
 
 	/**
 	 * Creates an {@link AffineTransform} to transform points from this
 	 * coordinate space to the absolute coordinate space by traversing the
 	 * parent hierarchy tree.
 	 * <p>
 	 * The transform generated by this method is the inverse of the transform
 	 * generated by {@link #getAbsoluteToRelativeTransform()}.
 	 * 
 	 * @return a transform
 	 */
 	public AffineTransform getRelativeToAbsoluteTransform() {
 		// Create a transform to start with.
 		AffineTransform transform = new AffineTransform();
 
 		// Start with this object's parent...
 		GObject parent = getParent();
 
 		// ... and keep going until we're out of parents.
 		while (parent != null) {
 			// Concatenate with the negative (true) transform.
 			transform.concatenate(parent.getTransform(true));
 
 			// Is this the last one?
 			if (parent.getParent() != null) {
 				// Add a translate as well.
 				transform.translate(
 						parent.getX() - parent.getWidth()
 								* parent.getAnchorWeightX(),
 						parent.getY() - parent.getHeight()
 								* parent.getAnchorWeightY());
 			}
 
 			// Then go up one level.
 			parent = parent.getParent();
 		}
 
 		// Return the answer.
 		return transform;
 	}
 
 	/**
 	 * Gets the rotation of this object.
 	 * 
 	 * @return the rotation
 	 */
 	public double getRotation() {
 		return rotation;
 	}
 
 	/**
 	 * Gets the object's scale along the x-axis.
 	 * 
 	 * @return the scale
 	 */
 	public double getScaleX() {
 		return scaleX;
 	}
 
 	/**
 	 * Gets the object's scale along the y-axis.
 	 * 
 	 * @return the scale
 	 */
 	public double getScaleY() {
 		return scaleY;
 	}
 
 	/**
 	 * Gets a transform for this object's rotation and scale. This does not
 	 * account for position.
 	 * 
 	 * @param inverted
 	 *            whether the rotation should be negative (to undo a transform)
 	 * @return the transform
 	 */
 	public AffineTransform getTransform(boolean inverted) {
 		// Create a transform.
 		AffineTransform transform = new AffineTransform();
 
 		// If you're confused by the following method, remember that
 		// "Concatenated transformations have an apparent last-specified-first-applied order"
 		// [ see: http://stackoverflow.com/a/5670861/732016 ]
 
 		// S4: Remove offset.
 		transform.translate(x, y);
 
 		// S3: Rotate.
 		transform.rotate(Math.toRadians(inverted ? -rotation : rotation));
 
 		// S2: Scale.
 		transform.scale(scaleX, scaleY);
 
 		// S1: Offset to origin.
 		transform.translate(-x, -y);
 
 		// Done.
 		return transform;
 	}
 
 	/**
 	 * Gets the bounding shape, transformed to account for this object's
 	 * rotation and position with respect to its parent.
 	 * 
 	 * @return the transformed bounding shape
 	 */
 	public final Shape getTransformedBoundingShape() {
 		AffineTransform at = new AffineTransform();
 		at.translate(x, y);
 		at.rotate(Math.toRadians(rotation));
 		at.scale(scaleX, scaleY);
 		at.translate(-x, -y);
 		at.translate(x - width * anchorWeightX, y - height * anchorWeightY);
 		return at.createTransformedShape(getBoundingShape());
 	}
 
 	/**
 	 * Gets the width of this object.
 	 * 
 	 * @return the width
 	 */
 	public double getWidth() {
 		return width;
 	}
 
 	/**
 	 * Gets the x-coordinate of this object.
 	 * 
 	 * @return the x-coordinate with respect to the parent
 	 */
 	public double getX() {
 		return x;
 	}
 
 	/**
 	 * Gets the y-coordinate of this object.
 	 * 
 	 * @return the y-coordinate with respect to the parent
 	 */
 	public double getY() {
 		return y;
 	}
 
 	/**
 	 * Tests to see if this object has a parent. If it does, an immediately
 	 * following call to {@link #getParent()} is guaranteed to be non-
 	 * {@code null}.
 	 * 
 	 * @return {@code true} if this object has a non-{@code null} parent, or
 	 *         {@code false} if it does not
 	 */
 	public boolean hasParent() {
 		return parent != null;
 	}
 
 	/**
 	 * Performs an imprecise (bounding-box) hit test on this object and the
 	 * given object. For a more precise (contour) hit test, use
 	 * {@link #preciseHitTest(GObject)}.
 	 * 
 	 * @param other
 	 *            the object to perform the test on
 	 * @return {@code true} if the objects intersect, or {@code false} if they
 	 *         do not
 	 * @throws IllegalArgumentException
 	 *             if {@code other} is {@code null}
 	 */
 	public boolean hitTest(GObject other) throws IllegalArgumentException {
 		// Make sure other is non-null.
 		if (other == null) {
 			// other == null
 			throw new IllegalArgumentException("other == null");
 		}
 		return getBoundingBox().intersects(other.getBoundingBox());
 	}
 
 	/**
 	 * Invokes all controllers registered on this object.
 	 * 
 	 * @param context
 	 *            the context to use for the controllers
 	 */
 	public void invokeControllers(Context context) {
 		// Add and remove pending.
 		controllers.addAll(controllersToBeAdded);
 		controllers.removeAll(controllersToBeRemoved);
 
 		// Clear pending.
 		controllersToBeAdded.clear();
 		controllersToBeRemoved.clear();
 
 		// Loop over each object.
 		for (Controller controller : controllers) {
 			// Invoke the control method on this object.
 			controller.controlObject(this, context);
 		}
 	}
 
 	/**
 	 * Invokes all listeners registered on this object.
 	 * 
 	 * @param context
 	 *            the context to use for the listeners
 	 */
 	public void invokeListeners(Context context) {
 		// Add and remove pending.
 		listeners.addAll(listenersToBeAdded);
 		listeners.removeAll(listenersToBeRemoved);
 
 		// Clear pending.
 		listenersToBeAdded.clear();
 		listenersToBeRemoved.clear();
 
 		// Loop over each listener.
 		for (Listener listener : listeners) {
 			// Can we invoke the listener?
 			if (listener.isValid(this, context)) {
 				// Yes - do so.
 				listener.invoke(this, context);
 			}
 		}
 	}
 
 	@Override
 	public boolean isVisible() {
 		return visible;
 	}
 
 	/**
 	 * Moves this object a given distance at the given angle.
 	 * 
 	 * @param distance
 	 *            the distance to move
 	 * @param angle
 	 *            the angle at which to move, in degrees, in parent coordinate
 	 *            space
 	 * @since 1.2
 	 */
 	public void moveAtAngle(double distance, double angle) {
 		double angle_rad = Math.toRadians(angle);
 		x += distance * Math.cos(angle_rad);
 		y += distance * Math.sin(angle_rad);
 	}
 
 	/**
 	 * Moves this object to the given object. This is equivalent to calling
 	 * {@link #snapAnchor(GObject) other.snapAnchor(this)}.
 	 * 
 	 * @param other
 	 *            the object to move to
 	 */
 	public void moveTo(GObject other) {
 		other.snapAnchor(this);
 	}
 
 	/**
 	 * Moves this object a given distance in the direction of the given object.
 	 * This may cause the object to pass the target object.
 	 * <p>
 	 * This is equivalent to:
 	 * 
 	 * <pre>
 	 * moveAtAngle(distance, angleTo(other));
 	 * </pre>
 	 * 
 	 * @param distance
 	 *            the distance to move
 	 * @param other
 	 *            the object to move toward
 	 */
 	public void moveToward(double distance, GObject other) {
 		moveAtAngle(distance, angleTo(other));
 	}
 
 	/**
 	 * Paints this object. The default {@link GObject} implementation does
 	 * nothing but paint subcomponents.
 	 */
 	@Override
 	public void paint(Graphics2D g) {
 		// Paint each component.
 		for (GPaintable object : subcomponents) {
 			// Can we paint?
 			if (object.isVisible()) {
 				// Do so.
 				paintSandboxed(object, g);
 			}
 		}
 	}
 
 	/**
 	 * Prepares and paints the object in a sandboxed graphics environment.
 	 * 
 	 * @param object
 	 *            the object to paint
 	 * @param g
 	 *            the graphics context to use as a basis for the sandbox
 	 */
 	private void paintSandboxed(GPaintable object, Graphics2D g) {
 		// Sandbox the graphics.
 		Graphics2D sandbox = (Graphics2D) g.create();
 
 		// Clip it - stay within the lines.
 		sandbox.clip(getBoundingShape());
 
 		// Perform preliminary transforms, modifications, etc.
 		object.preparePaint(sandbox);
 
 		// Go to town.
 		object.paint(sandbox);
 	}
 
 	/**
 	 * Performs a precise (contour) hit test on this object and the given
 	 * object.
 	 * 
 	 * @param other
 	 *            the object to perform a hit test on
 	 * @return {@code true} if the two objects intersect, or {@code false} if
 	 *         they do not
 	 * @throws IllegalArgumentException
 	 *             if {@code other} is {@code null}
 	 */
 	public boolean preciseHitTest(GObject other)
 			throws IllegalArgumentException {
 		// Make sure other is non-null.
 		if (other == null) {
 			// other == null
 			throw new IllegalArgumentException("other == null");
 		}
 
 		// If the bounding boxes don't intersect, there's no chance.
 		if (!hitTest(other)) {
 			// No chance.
 			return false;
 		}
 
 		// Create two Areas.
 		Area boundingShape = new Area(getTransformedBoundingShape());
 		Area otherShape = new Area(other.getTransformedBoundingShape());
 
 		// Also apply relative-to-absolute transforms.
 		boundingShape.transform(getRelativeToAbsoluteTransform());
 		otherShape.transform(getRelativeToAbsoluteTransform());
 
 		// Get the intersection.
 		boundingShape.intersect(otherShape);
 
 		// If it's non-empty, it's a hit.
 		return !boundingShape.isEmpty();
 	}
 
 	@Override
 	public void preparePaint(Graphics2D g) {
 		// Uncomment the next two lines to paint objects' bounding shapes for
 		// debugging purposes.
 		//
 		// AffineTransform at = g.getTransform();
 		// Shape c = g.getClip();
 		// g.setClip(null);
 		// g.setTransform(new AffineTransform());
 		// g.setColor(java.awt.Color.RED);
 		// g.draw(getAbsoluteBoundingShape());
 		// g.setTransform(at);
 		// g.setClip(c);
 
 		// Account for alpha.
 		g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,
 				(float) alpha));
 
 		// Account for rotation, scale.
 		g.transform(getTransform(false));
 
 		// Account for translation.
 		g.translate(x - width * anchorWeightX, y - height * anchorWeightY);
 	}
 
 	@Override
 	public void remove(GObject object) {
 		// Clear the parent.
 		// This will invoke `componentRemoved`, which will remove the object
 		// from the set.
 		object.setParent(null);
 	}
 
 	/**
 	 * Removes all children from this container.
 	 */
 	public void removeAllChildren() {
 		subcomponents.clear();
 	}
 
 	/**
 	 * Removes the given controller from the controller list.
 	 * 
 	 * @param c
 	 *            the controller to remove
 	 */
 	public void removeController(Controller c) {
 		controllersToBeRemoved.add(c);
 	}
 
 	/**
 	 * Removes the given listener from the listener list.
 	 * 
 	 * @param l
 	 *            the listener to remove
 	 */
 	public void removeListener(Listener l) {
 		listenersToBeRemoved.add(l);
 	}
 
 	/**
 	 * Attempts to remove this object from its parent. If this object's parent
 	 * is {@code null}, then no action will be performed and no error will be
 	 * thrown.
 	 * 
 	 * @return {@code true} if the object was successfully removed, or
 	 *         {@code false} if it was not
 	 */
 	public boolean removeSelf() {
 		// Perform a null-check.
 		if (parent == null) {
 			// The object couldn't be removed.
 			return false;
 		} else {
 			// Add the object.
 			parent.remove(this);
 
 			// The object was removed.
 			return true;
 		}
 	}
 
 	/**
 	 * Sets the object's alpha.
 	 * 
 	 * @param alpha
 	 *            the new alpha
 	 * @throws IllegalArgumentException
 	 *             if the alpha is outside of the range [0.0, 1.0]
 	 */
 	public void setAlpha(double alpha) throws IllegalArgumentException {
 		// Perform a bounds check.
 		if (0 > alpha || alpha > 1) {
 			// Invalid.
 			throw new IllegalArgumentException(
 					"alpha must be within [0, 1], but is " + alpha);
 		}
 
 		// Set the alpha.
 		this.alpha = alpha;
 	}
 
 	/**
 	 * Sets the anchor to the center. This is the default weight.
 	 */
 	public void setAnchorCenter() {
 		setAnchorWeight(0.5d, 0.5d);
 	}
 
 	/**
 	 * Sets the anchor point position in pixels. This method simply calls
 	 * {@link #setAnchorPositionX(double)} and
 	 * {@link #setAnchorPositionY(double)}.
 	 * 
 	 * @param x
 	 *            the x anchor point position, measured from the left
 	 * @param y
 	 *            the right anchor point position, measured from the top
 	 * @return {@code true} if both operations succeeded, or {@code false} if
 	 *         one or more failed
 	 */
 	public boolean setAnchorPosition(double x, double y) {
 		// Use bitwise & to prevent short-circuiting.
 		// (if the x method returned false and we used &&, the y method would
 		// not run)
 		return setAnchorPositionX(x) & setAnchorPositionY(y);
 	}
 
 	/**
 	 * Sets the anchor position in pixels, where {@code 0} is the left edge of
 	 * the object and the result of {@link #getWidth()} is the right edge. This
 	 * calculates a percentage for the anchor weight based on the object's
 	 * current width. If the width is {@code 0}, no action is taken, no error is
 	 * thrown, and {@code false} is returned.
 	 * <p>
 	 * As long as the width is non-zero, this method is the same as:
 	 * 
 	 * <pre>
 	 * setAnchorWeightX(x / getWidth());
 	 * </pre>
 	 * 
 	 * @param x
 	 *            the anchor point position, measured from the left
 	 * @return {@code true} if the width is non-zero and the anchor point has
 	 *         been set, or {@code false} if the operation failed
 	 */
 	public boolean setAnchorPositionX(double x) {
 		// Prevent division by zero.
 		if (width == 0) {
 			// No can do.
 			return false;
 		} else {
 			// Go.
 			setAnchorWeightX(x / width);
 			return true;
 		}
 	}
 
 	/**
 	 * Sets the anchor position in pixels, where {@code 0} is the top edge of
 	 * the object and the result of {@link #getHeight()} is the bottom edge.
 	 * This calculates a percentage for the anchor weight based on the object's
 	 * current height. If the height is {@code 0}, no action is taken, no error
 	 * is thrown, and {@code false} is returned.
 	 * <p>
 	 * As long as the height is non-zero, this method is the same as:
 	 * 
 	 * <pre>
 	 * setAnchorWeightY(x / getHeight());
 	 * </pre>
 	 * 
 	 * @param y
 	 *            the anchor point position, measured from the top
 	 * @return {@code true} if the height is non-zero and the anchor point has
 	 *         been set, or {@code false} if the operation failed
 	 */
 	public boolean setAnchorPositionY(double y) {
 		// Prevent division by zero.
 		if (height == 0) {
 			// No can do.
 			return false;
 		} else {
 			// Go.
 			setAnchorWeightY(y / height);
 			return true;
 		}
 	}
 
 	/**
 	 * Sets the anchor weight to (0.0, 0.0).
 	 */
 	public void setAnchorTopLeft() {
 		setAnchorWeight(0d, 0d);
 	}
 
 	/**
 	 * Sets the x and y anchor weights. This is a convenience method to combine
 	 * {@link #setAnchorWeightX(double)} and {@link #setAnchorWeightY(double)}.
 	 * 
 	 * @param anchorWeightX
 	 *            the x anchor weight
 	 * @param anchorWeightY
 	 *            the y anchor weight
 	 */
 	public void setAnchorWeight(double anchorWeightX, double anchorWeightY) {
 		setAnchorWeightX(anchorWeightX);
 		setAnchorWeightY(anchorWeightY);
 	}
 
 	/**
 	 * Sets the rotation weight along the x-axis, from {@code 0.0} (full left)
 	 * to {@code 1.0} (full right).
 	 * 
 	 * @param anchorWeightX
 	 *            the new rotation weight
 	 * @see #anchorWeightX
 	 */
 	public void setAnchorWeightX(double anchorWeightX) {
 		this.anchorWeightX = anchorWeightX;
 	}
 
 	/**
 	 * Sets the rotation weight along the y-axis, from {@code 0.0} (full top) to
 	 * {@code 1.0} (full bottom).
 	 * 
 	 * @param anchorWeightY
 	 *            the new rotation weight
 	 * @see #anchorWeightY
 	 */
 	public void setAnchorWeightY(double anchorWeightY) {
 		this.anchorWeightY = anchorWeightY;
 	}
 
 	/**
 	 * Sets this argument's bounds to the given coordinates. This is equivalent
 	 * to calling {@link #setSize(double, double)} and
 	 * {@link #setLocation(double, double)}, or calling {@link #setX(double)},
 	 * {@link #setY(double)}, {@link #setWidth(double)}, and
 	 * {@link #setHeight(double)}.
 	 * 
 	 * @param x
 	 *            the new x-coordinate
 	 * @param y
 	 *            the new y-coordinate
 	 * @param w
 	 *            the new width
 	 * @param h
 	 *            the new height
 	 * @throws IllegalArgumentException
 	 *             if the width or height is negative
 	 */
 	public void setBounds(double x, double y, double w, double h)
 			throws IllegalArgumentException {
 		setWidth(w);
 		setHeight(h);
 		setX(x);
 		setY(y);
 	}
 
 	/**
 	 * Sets this argument's bounds to the given rectangle. This is equivalent to
 	 * calling {@link #setBounds(double, double, double, double)} with the
 	 * rectangle's properties.
 	 * 
 	 * @param r
 	 *            the new boundary rectangle
 	 */
 	public void setBounds(Rectangle2D r) {
 		setBounds(r.getX(), r.getY(), r.getWidth(), r.getHeight());
 	}
 
 	/**
 	 * Sets the height of this object.
 	 * 
 	 * @param h
 	 *            the new height
 	 * @throws IllegalArgumentException
 	 *             if {@code height} is negative
 	 */
 	public void setHeight(double h) throws IllegalArgumentException {
 		if (h < 0) {
 			// The height is negative. This is not allowed.
 			throw new IllegalArgumentException("height < 0 : " + h);
 		}
 		this.height = h;
 	}
 
 	/**
 	 * Sets the location of this object to the given coordinates. This is
 	 * equivalent to calling {@link #setX(double)} and {@link #setY(double)}.
 	 * 
 	 * @param x
 	 *            the new x-coordinate
 	 * @param y
 	 *            the new y-coordinate
 	 */
 	public void setLocation(double x, double y) {
 		setX(x);
 		setY(y);
 	}
 
 	/**
 	 * Sets this object's parent to the given holder.
 	 * 
 	 * @param container
 	 *            the new parent
 	 */
 	protected void setParent(GObject container) {
 		// We may need to clear the previous parent.
 		if (parent != null) {
 			// Clear the previous parent.
 			parent.componentRemoved(this);
 		}
 
 		// Set the new parent.
 		parent = container;
 	}
 
 	/**
 	 * Sets the rotation to the given value.
 	 * 
 	 * @param rotation
 	 *            the new rotation
 	 */
 	public void setRotation(double rotation) {
 		this.rotation = rotation;
 	}
 
 	/**
 	 * Sets the object's scale along both axes. This is a convenience method for
 	 * {@link #setScaleX(double)} and {@link #setScaleY(double)}.
 	 * 
 	 * @param scale
 	 *            the new scale
 	 */
 	public void setScale(double scale) {
 		setScaleX(scale);
 		setScaleY(scale);
 	}
 
 	/**
 	 * Sets the object's scale along the x-axis.
 	 * 
 	 * @param scaleX
 	 *            the new scale
 	 */
 	public void setScaleX(double scaleX) {
 		this.scaleX = scaleX;
 	}
 
 	/**
 	 * Sets the object's scale along the y-axis.
 	 * 
 	 * @param scaleY
 	 *            the new scale
 	 */
 	public void setScaleY(double scaleY) {
 		this.scaleY = scaleY;
 	}
 
 	/**
 	 * Sets this object's size to the given width and height. This is equivalent
 	 * to calling {@link #setWidth(double)} and {@link #setHeight(double)}.
 	 * 
 	 * @param w
 	 *            the new width
 	 * @param h
 	 *            the new height
 	 * @throws IllegalArgumentException
 	 *             if the width or height is negative
 	 */
 	public void setSize(double w, double h) throws IllegalArgumentException {
 		setWidth(w);
 		setHeight(h);
 	}
 
 	/**
 	 * Sets this object's visibility. Neither an invisible object nor its
 	 * children will be painted.
 	 * 
 	 * @param visible
 	 *            {@code true} if this object should be visible, or
 	 *            {@code false} if it should not
 	 */
 	public void setVisible(boolean visible) {
 		this.visible = visible;
 	}
 
 	/**
 	 * Sets the width of this object.
 	 * 
 	 * @param w
 	 *            the new width
 	 * @throws IllegalArgumentException
 	 *             if {@code width} is negative
 	 */
 	public void setWidth(double w) throws IllegalArgumentException {
 		if (w < 0) {
 			// The width is negative. This is not allowed.
 			throw new IllegalArgumentException("width < 0 : " + w);
 		}
 		this.width = w;
 	}
 
 	/**
 	 * Sets the x-coordinate of this object.
 	 * 
 	 * @param x
 	 *            the new x-coordinate
 	 */
 	public void setX(double x) {
 		this.x = x;
 	}
 
 	/**
 	 * Sets the y-coordinate of this object.
 	 * 
 	 * @param y
 	 *            the new y-coordinate
 	 */
 	public void setY(double y) {
 		this.y = y;
 	}
 
 	/**
 	 * Snaps the given object's anchor point to this object's anchor point in
 	 * local coordinate space. This object will not be affected.
 	 * 
 	 * @param snap
 	 *            the object to move
 	 */
 	public void snapAnchor(GObject snap) {
 		snap.setLocation(x, y);
 	}
 
 	/**
 	 * Snaps the given child's anchor point to this object's anchor point in
 	 * parent-child coordinate space. This object will not be affected.
 	 * 
 	 * @param snap
 	 *            the child to move
 	 */
 	public void snapChild(GObject snap) {
 		// Set both coordinates.
 		snap.setLocation(width * anchorWeightX, height * anchorWeightY);
 	}
 
 	/**
 	 * Snaps the given child's center point to this object's center in
 	 * parent-child coordinate space. This object will not be affected.
 	 * 
 	 * @param snap
 	 *            the child to move
 	 */
 	public void snapChildToCenter(GObject snap) {
 		// We want the center of the object to be at (width / 2, height / 2).
 		// The center of the object is s.(x - w * ax, y - h * ay).
 		// Thus s.(x - w * ax) = w / 2; s.(y - h * ay) = h / 2.
 		// s.x = w/2 + s.x * s.ax; s.y = h / 2 + s.h * s.ay
 
 		// Set both coordinates.
		snap.setLocation(width / 2 + snap.width * (snap.anchorWeightX - 0.5),
				height / 2 + snap.height + (snap.anchorWeightY - 0.5));
 	}
 
 	/**
 	 * Invoked when this view is hidden. By default, does nothing.
 	 */
 	public void viewHidden() {
 
 	}
 
 	/**
 	 * Invoked when this view is shown. By default, does nothing.
 	 */
 	public void viewShown() {
 
 	}
 }
