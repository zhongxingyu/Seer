 package com.helmuthnaumer.mapping;
 
 /**
  * A wall that is used to make up a RoomMap. Contains details about points and
  * calculates details about points in relation to each other.
  * 
  * @author Helmuth Naumer
  * 
  */
 public class Wall {
 	/**
 	 * The starting point on the wall
 	 */
 	private PointCross start;
 	/**
 	 * The ending point on the wall
 	 */
 	private PointCross end;
 	/**
 	 * The number of points that have fit in the wall
 	 */
 	private int numPoints;
 	/**
 	 * The number of points that have not fit in the wall
 	 */
 	private int timesFailed;
 
 	private double changeX;
 	private double changeY;
 	private double updateX;
 	private double updateY;
 
 	/**
 	 * Constructor for wall given only the starting point. End point is null.
 	 * 
 	 * @param start
 	 *            Starting point for wall
 	 */
 	public Wall(PointCross start) {
 		this.start = new PointCross(start);
 	}
 
 	/**
 	 * Constructor for wall
 	 * 
 	 * @param point1
 	 *            Starting point for wall
 	 * @param point2
 	 *            Ending point for wall
 	 */
 	public Wall(PointCross point1, PointCross point2) {
 		start = new PointCross(point1);
 		end = new PointCross(point2);
 		numPoints++;
 	}
 
 	/**
 	 * Given a point, attempts to fit the point in. If the point extends the
 	 * wall, replaces either start or end points. If the point is inside the
 	 * wall, increments numPoints If the point is unrelated to the wall,
 	 * increments timesFailed
 	 * 
 	 * @param update
 	 *            The point to be fit into the wall
 	 * @return Returns true if the point fits into the wall, otherwise returns
 	 *         false
 	 * @Postcondition If the point fits into the wall, increments numPoints,
 	 *                otherwise increments timesFailed
 	 */
 	public boolean update(PointCross update, boolean replace) {
 		if (end == null) {
 			end = new PointCross(update);
 			numPoints++;
 			return true;
 		}
 		if (Math.abs(start.getDirectionTo(end) - start.getDirectionTo(update)) < 2 || Math.abs(start.getDirectionTo(end) - start.getDirectionTo(update)) > 360 - 2 || Math.abs(start.getDirectionTo(end) - start.getDirectionTo(update)) < 182 && Math.abs(start.getDirectionTo(end) - start.getDirectionTo(update)) > 178) {

			if (start.distance(update) < end.distance(update)) {
 				double dir;
 				if (Math.abs(end.getDirectionTo(update) - end.getDirectionTo(start)) > 2) {
 					if (end.getDirectionTo(update) > end.getDirectionTo(start))
 						dir = ((end.getDirectionTo(update) - 360) + end.getDirectionTo(start) * (numPoints)) / (numPoints + 1);
 					else dir = ((end.getDirectionTo(update) + 360) + end.getDirectionTo(start) * (numPoints)) / (numPoints + 1);
 				}
 				else dir = (end.getDirectionTo(update) + end.getDirectionTo(start) * numPoints) / (numPoints + 1);
 
 				changeX = end.distance(update) * Math.cos(Math.toRadians(dir));
 				changeY = end.distance(update) * Math.sin(Math.toRadians(dir));
 				updateX = end.getX() + changeX;
 				updateY = end.getY() + changeY;
 				update = new PointCross(updateX, updateY);
 				if (!replace)
 					start.setLocation(update);
 				else start = update;
 			}
 			else {
 				double dir;
 				if (Math.abs(start.getDirectionTo(update) - start.getDirectionTo(end)) > 2) {
 					if (start.getDirectionTo(update) > start.getDirectionTo(end))
 						dir = ((start.getDirectionTo(update) - 360) + start.getDirectionTo(end) * (numPoints)) / (numPoints + 1);
 					else dir = ((start.getDirectionTo(update) + 360) + start.getDirectionTo(end) * (numPoints)) / (numPoints + 1);
 				}
 				else dir = (start.getDirectionTo(update) + start.getDirectionTo(end) * numPoints) / (numPoints + 1);
 
 				changeX = start.distance(update) * Math.cos(Math.toRadians(dir));
 				changeY = start.distance(update) * Math.sin(Math.toRadians(dir));
 				updateX = start.getX() + changeX;
 				updateY = start.getY() + changeY;
 				update = new PointCross(updateX, updateY);
 				if (!replace)
 					end.setLocation(update);
 				else end = update;
 
 			}
 			System.out.println(changeX + "\n" + changeY + "\n\n");
 			numPoints++;
 			start.setX(Math.round(start.getX() * 1000) / 1000.);
 			start.setY(Math.round(start.getY() * 1000) / 1000.);
 			end.setX(Math.round(end.getX() * 1000) / 1000.);
 			end.setY(Math.round(end.getY() * 1000) / 1000.);
 			return true;
 		}
 		double opDir = end.getDirectionTo(update) - 180;
 		if (opDir < 0)
 			opDir += 360;
 		if (start.getDirectionTo(update) == opDir) {
 			numPoints++;
 			return true;
 		}
 		timesFailed++;
 		return false;
 	}
 
 	/**
 	 * Calculates the current length of the wall
 	 * 
 	 * @return The current length of the wall
 	 */
 	public double length() {
 		if (end == null || start == null) {
 			return 0;
 		}
 		return start.distance(end);
 	}
 
 	/**
 	 * calculates the current direction of the wall
 	 * 
 	 * @return The current direction of the wall
 	 */
 	public double direction() {
 		if (end == null || start == null) {
 			return 0;
 		}
 		return start.getDirectionTo(end);
 	}
 
 	/**
 	 * Returns the number of times a point has not fit into the wall
 	 * 
 	 * @return The number of times a point has not fit into the wall
 	 */
 	public int getTimesFailed() {
 		return timesFailed;
 	}
 
 	/**
 	 * Returns the number of points that have fit into the wall
 	 * 
 	 * @return The number of points that have fit into the wall
 	 */
 	public int getNumPoints() {
 		return numPoints;
 	}
 
 	/**
 	 * Returns the starting point on the wall
 	 * 
 	 * @return The starting point on the wall
 	 */
 	public PointCross getStart() {
 		return start;
 	}
 
 	/**
 	 * Returns the end point on the wall
 	 * 
 	 * @return The end point on the wall
 	 */
 	public PointCross getEnd() {
 		return end;
 	}
 
 	/**
 	 * Calculates the slope of the wall based on the coordinates
 	 * 
 	 * @return The slope of the wall
 	 */
 	public double getSlope() {
 		if(end.getX()==start.getX())
 			return Double.NaN;
 		return (end.getY() - start.getY()) / (end.getX() - start.getX());
 	}
 
 	/**
 	 * Calculates the walls intercept with the y-axis
 	 * 
 	 * @return The walls intercept with the y-axis
 	 */
 	public double getIntercept() {
 		if(getSlope()==Double.NaN)
 			return Double.NaN;
 		return (-start.getX() * getSlope()) + start.getY();
 	}
 
 	/**
 	 * Returns the starting point, ending point, length, and direction of the
 	 * wall
 	 */
 	public String toString() {
 		return "Start: " + start + "  \t" + "End: " + end + "\t\tLength: " + length() + "\t\tDirection: " + direction();
 	}
 
 	/**
 	 * Calculates the point at which two walls cross
 	 * 
 	 * @param other
 	 *            A wall that intersects with the wall
 	 * @return The point at which this and other cross
 	 * @Precondition The walls intersect
 	 */
 	public PointCross calcIntersect(Wall other) {
 		double x;
 		if (this.direction() == 90 || this.direction() == 270)
 			x = this.getStart().getX();
 		else if (other.direction() == 90 || other.direction() == 270)
 			x = other.getStart().getX();
 		else x = (this.getIntercept() - other.getIntercept()) / (other.getSlope() - this.getSlope());
 		double y;
 		if (other.direction() == 0 || other.direction() == 180)
 			y = other.getStart().getY();
 		else if (this.direction() == 0 || this.direction() == 180)
 			y = this.getStart().getY();
 		else y = other.getSlope() * x + other.getIntercept();
 		return (new PointCross(x, y));
 	}
 
 	/**
 	 * 
 	 * @param point
 	 * @return
 	 */
 	public double distanceToPoint(PointCross point) {
 		if (this.getEnd() == null) {
 			return this.getStart().distance(point);
 		}
 		if (this.getStart().getX() != this.getEnd().getX()) {
 			Double slope = this.getSlope();
 			return (Math.abs(slope * point.getX() - point.getY() - (slope * this.getStart().getX() - this.getStart().getY()))) / Math.sqrt(slope * slope + 1.);
 		}
 		return Math.abs(point.getX() - this.getStart().getX());
 	}
 
 	/**
 	 * Compares this wall to another wall, w, to test if they are the same
 	 * 
 	 * @param wall
 	 *            The wall that is being tested
 	 * @return true if the walls are the same, false if the walls are not
 	 */
 	public boolean equals(Wall wall) {
 		if ((wall.getStart().equals(this.getStart()) && wall.getEnd().equals(this.getEnd())) || wall.getStart().equals(this.getEnd()) && wall.getEnd().equals(this.getStart()))
 			return true;
 		else return false;
 	}
 
 	/**
 	 * sets numPoints to the level of not removing over time
 	 */
 	public void confirmWall() {
 		numPoints = 4;
 	}
 }
