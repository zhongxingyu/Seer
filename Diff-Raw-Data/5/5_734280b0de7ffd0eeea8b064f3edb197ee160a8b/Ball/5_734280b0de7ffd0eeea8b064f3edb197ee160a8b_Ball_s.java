 import greenfoot.*;  // (World, Actor, GreenfootImage, Greenfoot and MouseInfo)
 import java.util.*;
 
 /**
  * A Ball that can collide with the four walls of the PhysicsWorld, other
  * balls, ArkanoidBricks and Paddles.
  *
  * Collision is elastic: Walls have infinite inertia, so ball speed is constant
  * during wall collision, unless wallDampen is given, in which case this factor
  * is applied to the normal component of the velocity.
  *
  * Paddles also have infinite inertia, and the Ball collides off the normal of
  * collision.
  *
  * In ball-ball collision, we assume that balls have equal masses, so the
  * normal components of their velocities are simply swapped.
  */
 public class Ball extends DynamicActor
 {
 	/**
 	 * The radius of this ball. Declared final since it is a constant and so we
 	 * don't have to use a getter.
 	 */
 	final double radius;
 
 	/**
 	 * The dampening factors in wall and ball collision.
 	 */
 	protected Double wallDampen;
 	protected Double ballDampen;
 
 	/**
 	 * Create a ball with the given image (assumed to be of square dimensions)
 	 * and the given initial velocity.
 	 *
 	 * @param image A String containing a filename, may be null
 	 * @param velX Initial velocity in the direction of the x-axis
 	 * @param velY Initial velocity in the direction of the y-axis
 	 */
 	public Ball(String image, double velX, double velY) {
 		super();
 		if (image != null) {
 			setImage(image);
 		}
 		wallDampen = ballDampen = 1.0;
 		setVelocity(new Vector(velX, velY));
 		size = new Vector(getImage().getWidth());
 		radius = size.x()/2.0;
 	}
 	/**
 	 * Create a Ball with the default velocity (3,2).
 	 */
 	public Ball(String image) {
 		this(image,3,2);
 	}
 	/**
 	 * Create a Ball with the default image filename (null).
 	 */
 	public Ball(double velX, double velY) {
 		this(null, velX, velY);
 	}
 	/**
 	 * Create a Ball with the defaults.
 	 */
 	public Ball() {
 		this(null);
 	}
 
 	/**
 	 * A Ball is circular.
 	 */
 	Shape getShape() {
 		return new Circle(getLocation(), radius);
 	}
 
 	/**
 	 * TODO: Is this still needed?
 	 */
 	private ArrayList<Ball> getIntersectingBalls() {
 		List intersecting = getIntersectingObjects(Ball.class);
 		ArrayList<Ball> balls = new ArrayList<Ball>();
 		for (Object o : intersecting) {
 			Ball b = (Ball) o;
 			double dist = b.getLocation().subtract(getLocation()).length();
 			if (dist > radius*2) {
 				continue;
 			}
 			double nextdist = b.getLocation().add(b.getLastVelocity()).subtract(getLocation().add(getLastVelocity())).length();
 			if (nextdist > dist) {
 				// pretend we don't intersect with the ball
 				continue;
 			}
 			balls.add(b);
 		}
 		return balls;
 	}
 
 	/**
 	 * Handle an intersection with another ShapeActor. Get the shape and handle
 	 * an intersection with a shaped object of infinite inertia.
 	 *
 	 * The special case when we intersect another Ball is handled separately.
 	 * ArkanoidBricks are hit().
 	 */
 	public void handleIntersection(ShapeActor other) {
 		if (other instanceof Ball) {
 			handleIntersection((Ball) other);
 			return;
 		}
 		Shape s = other.getShape();
 		handleIntersection(s);
 		if (other instanceof ArkanoidBrick) {
 			((ArkanoidBrick)other).hit();
 		}
 	}
 
 	/**
 	 * Deflect on a ShapeActor's bounding box as if the ShapeActor has infinite
 	 * inertia. Figure out the normal of collision, and mirror our velocity
 	 * around that.
 	 */
 	public void deflectOnBoundingBox(ShapeActor other) {
 		Circle us = (Circle) getShape();
 		Shape them = other.getShape();
 		Vector normal = us.bboxIntersection(them).normal;
 		mirrorVelocity(normal);
 	}
 
 	public void mirrorVelocity(Vector normal) {
 		setVelocity(getVelocity().mirror(normal));
 	}
 
 	/**
 	 * Handle an elastic Ball-Ball intersection given equal masses.
 	 */
	public void handleIntersection(Ball b) {
 		/* Find the normal and tangential unit vectors in the intersection. */
 		Vector normal = other.getShape().intersectionNormal(getShape()).unit();
 		Vector tangent = normal.orthogonal();
 
 		/* Find the velocity of us and them. */
 		Vector v1 = getLastVelocity();
		Vector v2 = b.getLastVelocity();
 
 		/* We assume equal masses, so the calculations are a lot simpler. */
 		//double m1 = getMass();
 		//double m2 = other.getMass();
 
 		/* Project the velocities onto the normal and tangential vectors. */
 		double v1n = v1.dotP(normal);
 		double v2n = v2.dotP(normal);
 		double v1t = v1.dotP(tangent);
 		//double v2t = v2.dotP(tangent);
 
 		/* Find our new velocity along the normal, v1 normal prime. (The velocity
 		 * along the tangent is constant.) */
 		//double v1np = v1n*(m1-m2)/(m1+m2)+2.0*m2*v2n/(m1+m2); // for m1 != m2
 		double v1np = v2n; // for m1 = m2
 
 		/* Find the change in velocity, by finding the new velocity
 		 * v1np*normal+v1tp*tangent and subtracting the previous velocity. */
 		Vector bounce;
 		if (ballDampen == null) {
 			/* Complete dampening, so we only want to move along the tangential after
 			 * the collision. */
 			bounce = tangent.scale(v1t).subtract(v1);
 		} else {
 			/* Partial or no dampening, so apply the dampening factor to the velocity
 			 * along the normal. */
 			bounce = normal.scale(v1np*ballDampen).add(tangent.scale(v1t)).subtract(v1);
 		}
 
 		addVelocity(bounce);
 	}
 
 	/**
 	 * Handle an intersection with an infinitely inertial object of the given
 	 * Shape.
 	 */
 	public void handleIntersection(Shape s) {
 		Vector normal = s.intersectionNormal(getShape());
 		if (null != normal) {
 			mirrorVelocity(normal.orthogonal());
 		}
 	}
 
 	/**
 	 * Handle wall collision given the dampening factor this.wallDampen.
 	 */
 	public void collidedWithWall(PhysicsWorld.Walls wall) {
 		Vector vel = getVelocity();
 		Vector newvel;
 		double factor = (wallDampen == null) ? 0.0 : wallDampen;
 		if (wall == PhysicsWorld.Walls.NORTH || wall == PhysicsWorld.Walls.SOUTH) {
 			newvel = new Vector(vel.x(), -vel.y()*factor);
 		} else {
 			newvel = new Vector(-vel.x()*factor, vel.y());
 		}
 		setVelocity(newvel);
 	}
 
 	public void setWallDampen(Double d) {
 		wallDampen = d;
 	}
 
 	public void setBallDampen(Double d) {
 		ballDampen = d;
 	}
 }
