 package nehsics.force;
 import nehsics.bodies.*;
 import nehsics.math.*;
 import static nehsics.math.Util.*;
 
 public class BindingForce extends SpringForce {
 	private Body b1;
 	private Body b2;
 	private boolean broken = false;
 	private Vector2d force = v();
 
 	public BindingForce(Body a, Body b) {
		super(1000);
 		b1 = a;
 		b2 = b;
 	}
 
 	public void calculateForce() {
 		if (distance(b1.getPosition(), b2.getPosition())
 				>= (b1.getRadius() + b2.getRadius())*40)
 			broken = true;
 		if (b1.canHitForce(b2) || broken) {
 			force = v();
 			return;
 		}
 		force = super.getForce(b1.getPosition(), b2.getPosition());
 	}
 
 	public Vector2d getForce() {
 		return force;
 	}
 }
