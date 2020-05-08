 package com.avona.games.towerdefence;
 
 public final class V2 {
 	public static final V2 ZERO = new V2();
 
 	public float x = 0.0f;
 	public float y = 0.0f;
 
 	public V2() {
 	}
 
 	public V2(final float x, final float y) {
 		this.x = x;
 		this.y = y;
 	}
 
 	public V2(final V2 orig) {
 		x = orig.x;
 		y = orig.y;
 	}
 	
 	/**
 	 * Construct a non-empty V2 vector.
 	 *
 	 * @param direction
 	 *            Direction of the vector.
 	 * @param speed
 	 *            Length of the vector.
 	 */
 	public V2(final V2 direction, final float length) {
 		this(direction);
 		this.setLength(length);
 	}
 
 	public String toString() {
 		return String.format("V2<%f|%f>", x, y);
 	}
 
 	public void add(final V2 v) {
 		x += v.x;
 		y += v.y;
 	}
 
 	public void add(final float add_x, final float add_y) {
 		x += add_x;
 		y += add_y;
 	}
 	
 	/**
 	 * Add other V2 with weight factor to this instance.
 	 * 
 	 * @param other
 	 *            V2 to accumulate onto this instance.
 	 * @param weight
 	 *            Weight factor. 1.0f to get plain addition.
 	 */
 	public void addWeighted(final V2 other, final float weight) {
 		this.add(other.x * weight, other.y * weight);
 	}
 
 	public void sub(final V2 v) {
 		x -= v.x;
 		y -= v.y;
 	}
 
 	public void sub(final float sub_x, final float sub_y) {
 		x -= sub_x;
 		y -= sub_y;
 	}
 
 	public void mult(final float f) {
 		x *= f;
 		y *= f;
 	}
 
 	public float length() {
 		return (float) Math.sqrt(x * x + y * y);
 	}
 	
 	public void setLength(float length) {
 		float f = length / length();
 		this.mult(f);
 	}
 
 	public float squaredLength() {
 		return (x * x + y * y);
 	}
 
 	public void normalize() {
 		this.setLength(1.0f);
		// TODO think about making all these functions final to improve speed...
 	}
 
 	public static float dist(final V2 from, final V2 to) {
 		return (float) Math.sqrt((to.x - from.x) * (to.x - from.x)
 				+ (to.y - from.y) * (to.y - from.y));
 	}
 
 	public static float squaredDist(final V2 from, final V2 to) {
 		return (to.x - from.x) * (to.x - from.x) + (to.y - from.y)
 				* (to.y - from.y);
 	}
 
 	public float dist(final V2 dest) {
 		return dist(this, dest);
 	}
 
 	public float squaredDist(final V2 dest) {
 		return squaredDist(this, dest);
 	}
 
 	public static float dot(final V2 vec0, final V2 vec1) {
 		return vec0.x * vec1.x + vec0.y * vec1.y;
 	}
 
 	/**
 	 * Project one vector onto the other vector.
 	 * 
 	 * @param toBeProjectedVec
 	 *            Vector that will be projected onto baseVec.
 	 * @param baseVec
 	 *            Vector upon the projection will take place.
 	 * @return Returns the projected vector as a new vector instance.
 	 */
 	public static V2 project(final V2 toBeProjectedVec, final V2 baseVec) {
 		V2 projectedVec = new V2(baseVec);
 		projectedVec.mult(dot(toBeProjectedVec, baseVec)
 				/ dot(baseVec, baseVec));
 		return projectedVec;
 	}
 
 	/**
 	 * Set the direction based on the two points from and to. The resulting
 	 * direction will point from "from" to "to". Length is unchanged.
 	 * 
 	 * @param from
 	 *            Starting point. Will not be modified.
 	 * @param to
 	 *            Target point. Will not be modified.
 	 */
 	public void setDirection(final V2 from, final V2 to) {
		float l = this.length();
 		this.x = to.x - from.x;
 		this.y = to.y - from.y;
 		this.setLength(l);
 	}
 }
