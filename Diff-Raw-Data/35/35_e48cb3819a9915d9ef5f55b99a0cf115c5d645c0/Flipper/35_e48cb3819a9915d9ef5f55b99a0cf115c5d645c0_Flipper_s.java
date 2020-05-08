 package model;
 
 
 import java.util.ArrayList;
 
 import model.physics.Circle;
 import model.physics.Geometry;
 import model.physics.LineSegment;
 import model.physics.Vect;
 
 public class Flipper extends Gizmo implements iGizmo {
 	
 	protected double maxRotation, minRotation;
 	protected boolean active;
 	protected Vect rotationCenter;
	protected double angularVel;
 	protected Circle nonRotationalCircle;
 	protected final double DELTA_T = ((double)1) /30;
 
 	public Flipper(String identifier, GizPoint p, double row, double column, double width, double height) {
 		point 			= p;
 		columnHeight		= column;
 		cellWidth			= width;
 		cellHeight		= height;
 		rowWidth			= row;
 		rotation 			= 0;
		rotationIncrement = (1080/1000) * (1000/30);
		angularVel = (1080/1000) * (1000/30);
 		this.identifier 		= identifier;
 		
 		lineSegments 		= new ArrayList<LineSegment>();
 		circles			= new ArrayList<Circle>();
 		
 		active 			= false;
 		this.width = 2;
 		this.height = 2;
 	}
 
 	@Override
 	public void performAction(boolean a) {
 		active = a;
 	}
 	
 	
 	
 	@Override
 	public double timeUntilCollision(iBall ball) {
 		double min = Double.POSITIVE_INFINITY;
 		double newMin;
 		
 		for(LineSegment l : lineSegments){
			newMin = Geometry.timeUntilRotatingWallCollision(l, rotationCenter, angularVel,ball.returnBounds(), ball.getVelocity());
 			if(newMin < min)min = newMin;
 		}
 		
 		/* stationary top circle */
 		newMin = Geometry.timeUntilCircleCollision(nonRotationalCircle, ball.returnBounds(), ball.getVelocity());
 		if(newMin < min)min = newMin;
 		
 		
 		for(Circle c : circles){
			newMin = Geometry.timeUntilRotatingCircleCollision(c, rotationCenter, angularVel, ball.returnBounds(), ball.getVelocity());
 			if(newMin < min)min = newMin;
 		}
 		
 		return min;
 	}
 
 	@Override
 	public void collide(iBall ball) {
 		if(ball.equals(null))return;
 		double min = Double.POSITIVE_INFINITY;
 		double newMin;
 		LineSegment closestLine = null;
 		Circle closestCircle = null;
 		boolean stationaryCircle = false;
 		
 		for(LineSegment l : lineSegments){
			newMin = Geometry.timeUntilRotatingWallCollision(l, rotationCenter, angularVel,ball.returnBounds(), ball.getVelocity());
 			if(newMin < min){
 				min = newMin;
 				closestLine = l;
 			}
 		}
 		
 		/* non rotating top circle */
 		newMin = Geometry.timeUntilCircleCollision(nonRotationalCircle, ball.returnBounds(), ball.getVelocity());
 		if(newMin < min){
 			min = newMin;
 			closestLine = null;
 			closestCircle = nonRotationalCircle;
 			stationaryCircle = true;
 		}
 		
 		
 		for(Circle c : circles){
			newMin = Geometry.timeUntilRotatingCircleCollision(c, rotationCenter, angularVel, ball.returnBounds(), ball.getVelocity());
 			if(newMin < min){
 				min = newMin;
 				closestLine = null;
 				closestCircle = c;
 			}
 		}
 		 
 		 
 		//0.95 reflection coeff
 		if(closestLine != null){
			ball.setVelocity(Geometry.reflectRotatingWall(closestLine, rotationCenter, angularVel, ball.returnBounds(), ball.getVelocity(), 1));
 			System.out.println("hit wall");
 		}
 		if(closestCircle != null && stationaryCircle)ball.setVelocity(
 					Geometry.reflectCircle(closestCircle.getCenter(), ball.returnBounds().getCenter(), ball.getVelocity(), 1));
		if(closestCircle != null){
 			ball.setVelocity(
					Geometry.reflectRotatingCircle(closestCircle, rotationCenter, angularVel, ball.returnBounds(), ball.getVelocity(), 1)
 				);
 		}
 		//trigger.
 		for(iGizmo giz: triggers){
 			System.out.println(this.getIdentifier() + " has triggered " + giz.getIdentifier());
 			giz.performAction(true);
 			giz.move();
 		}
 	}
 }
