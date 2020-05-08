 package nehsics.force;
 import nehsics.bodies.*;
 import nehsics.math.*;
 import static nehsics.math.Util.*;
 
 public class SpringForce {
 	private double springConstant;
 
 	public SpringForce(double constant) {
 		springConstant = constant;
 	}
 
 	public Vector2d getForce(Vector2d p1, Vector2d p2) {
		double length = distance(p1,p2);
		Vector2d v = add(p1,scale(p2,-1));
		return scale(v,springConstant*length);
 	}
 }
