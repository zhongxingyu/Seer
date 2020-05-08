 package com.avona.games.towerdefence;
 
 /**
  * The vector describes both velocity (speed) and direction (vector). 1.0 is the
  * full movement of the vector in a second.
  */
 public class VelocityVector {
 	/**
 	 * Normalised direction vector. Represents the movement of the object in one
 	 * second.
 	 */
 	public V2 normalisedDirection = new V2();
 
 	/**
 	 * Speed in units per second.
 	 */
 	public float speed = 0.0f;
 
 	/**
 	 * Empty constructor. Zero speed and undefined direction.
 	 */
 	public VelocityVector() {
 	}
 
 	/**
 	 * Construct a non-empty velocity vector.
 	 * 
 	 * @param normalisedDirection
 	 *            Normalised direction vector. The vector instance will be
 	 *            stored as-is.
 	 * @param speed
 	 *            Speed in units per second.
 	 */
 	public VelocityVector(final V2 normalisedDirection, final float speed) {
 		this.normalisedDirection = normalisedDirection;
 		this.speed = speed;
 	}
 
 	/**
 	 * Translate point p by the velocity vector over dt seconds.
 	 * 
 	 * @param p
 	 *            Point to translate using the velocity vector. Will be
 	 *            translated in place.
 	 * @param dt
 	 *            Number of seconds for which the velocity vector's movement
 	 *            should be applied.
 	 */
 	public void translate(final V2 p, final float dt) {
 		p.add(normalisedDirection.x * speed * dt, normalisedDirection.y * speed
 				* dt);
 	}
 
 	/**
 	 * Set the direction based on the two points from and to. The resulting
 	 * direction will point from "from" to "to".
 	 * 
 	 * @param from
 	 *            Starting point. Will not be modified.
 	 * @param to
 	 *            Target point. Will not be modified.
 	 */
 	public void fromTo(final V2 from, final V2 to) {
 		V2 newDir = new V2(to);
 		newDir.sub(from);
 		newDir.normalize();
 		normalisedDirection = newDir;
 	}
 
 	public String toString() {
 		return String.format("VelocityVector(Vector=<%f, %f>, Speed=%f)",
 				normalisedDirection.x, normalisedDirection.y, speed);
 	}
 }
