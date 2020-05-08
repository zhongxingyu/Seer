 import greenfoot.*;  // (World, Actor, GreenfootImage, Greenfoot and MouseInfo)
 import java.util.*;
 
 /**
  * Write a description of class Ball here.
  * 
  * @author (your name) 
  * @version (a version number or a date)
  */
 public class Ball extends CircularActor implements DynamicActor
 {
 	private Vector canvasSize;
 	private Vector vel;
 	private Vector nextVel;
 	protected boolean hasMoved;
 	/**
 	 * The bounds of the ball's position vector.
 	 */
 	private Vector upperLeftBound;
 	private Vector lowerRightBound;
 	
 	public Ball(double velX, double velY) {
 		vel = new Vector(velX, velY);
 		upperLeftBound = new Vector(radius);
 		nextVel = Vector.zero();
 	}
 	public Ball() {
 		this(3,2);
 	}
 	public void addedToWorld(World w) {
 		canvasSize = new Vector(w.getWidth(), w.getHeight());
 		lowerRightBound = canvasSize.subtract(new Vector(radius));
 	}
 	public void move() {
 		if (nextVel != null) {
 			vel = vel.add(nextVel);
 			nextVel = null;
 		}
 		moveBy(vel);
 		setLocation(getLocation().clamp(upperLeftBound, lowerRightBound));
 	}
 	public void checkCollisions() {
 		checkWallCollisions();
 		checkBallCollisions();
 	}
 	/**
 	 * Reflect the velocity when the ball is out of bounds.
 	 * Multiply the velocity by the sign of the difference between the position
 	 * vector and the upper left bound vector, and multiply the velocity vector
 	 * by the sign of the difference between the lower right bound vector and the
 	 * position vector.
 	 * The sign of a vector is the vector whose coordinates are -1 or 1 depending
 	 * on the signs of the coordinates of the vector. See Vector.sign().
 	 */
 	private Vector wallCollision() {
 		// god I miss operator overloading
 		return upperLeftBound.subtract(getLocation()).sign()
 		.scale(getLocation().subtract(lowerRightBound).sign());
 	}
 	public void hasCollidedWithWall() {
 		vel = vel.scale(wallCollision());
 	}
 	public void checkWallCollisions() {
 		hasCollidedWithWall();
 	}
 	public void hasCollided(ShapeActor other) {
 	}
 
 	private ArrayList<Ball> getIntersectingBalls() {
 		List intersecting = getIntersectingObjects(Ball.class);
 		ArrayList<Ball> balls = new ArrayList<Ball>();
 		for (Object o : intersecting) {
 			Ball b = (Ball) o;
 			double dist = b.getLocation().subtract(getLocation()).length();
 			if (dist > radius*2) {
 				continue;
 			}
			double nextdist = b.getLocation().add(b.getVelocity()).subtract(getLocation().add(vel)).length();
			if (nextdist > dist) {
				// pretend we don't intersect with the ball
				continue;
			}
 			balls.add(b);
 		}
 		return balls;
 	}
 
 	private void checkBallCollisions() {
 		ArrayList<Ball> balls = getIntersectingBalls();
 		if (0 == balls.size()) {
 			return;
 		}
 		// it's gay, balls are touching
 		Ball other = balls.get(0); // only bounce off one
 		Vector normal = other.getLocation().subtract(getLocation());
 		normal = normal.scale(1.0/normal.length());
 		Vector tangent = normal.orthogonal();
 		Vector v1 = getVelocity();
 		Vector v2 = other.getVelocity();
 		//double m1 = getMass();
 		//double m2 = other.getMass();
 		double v1n = v1.dotP(normal);
 		double v2n = v2.dotP(normal);
 		double v1t = v1.dotP(tangent);
 		//double v2t = v2.dotP(tangent);
 
 		//double v1np = v1n*(m1-m2)/(m1+m2)+2.0*m2*v2n/(m1+m2);
 		double v1np = v2n; // for m1 = m2
 		addVelocity(normal.scale(v1np).add(tangent.scale(v1t)).subtract(v1));
 	}
 
 	public Vector getVelocity() {
 		return vel;
 	}
 	public void setVelocity(Vector vel) {
 		this.vel = vel;
 	}
 	public void addVelocity(Vector vel) {
 		if (nextVel == null) {
 			nextVel = vel;
 		} else {
 			nextVel = nextVel.add(vel);
 		}
 	}
 }
