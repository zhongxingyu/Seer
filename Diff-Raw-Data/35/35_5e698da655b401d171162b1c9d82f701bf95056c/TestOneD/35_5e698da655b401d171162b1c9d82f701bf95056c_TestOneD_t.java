 package nehsics.test;
 import nehsics.ui.*;
 import nehsics.force.*;
 import nehsics.bodies.*;
 import java.awt.*;
 import static nehsics.math.Util.*;
 import java.awt.*;
 
 public class TestOneD extends Tester {
	public final static String NAME = "One D";
 
 	public static void main(String[] args) {
 		new Starter(NAME);
 	}
 
 	public TestOneD(Canvas c) {
 		super(c);
 	}
 
 	protected void setup() {
 		PRECISION = 1;
 		world.setGravityEnabled(false);
 		world.setWallsEnabled(true);
 		Circle c1;
 		Circle c2;
 		world.addBody(c1 = new Circle(10,10));	
 		c1.setPosition(v(0,10));
 		world.addBody(c2 = new Circle(10,10));	
 		c2.setColor(Color.green);
 		c2.setPosition(v(0,-10));
 		BindingForce b2 = new BindingForce(c1,c2);	
 		BindingForce b1 = new BindingForce(c2,c1);	
 		c2.addBond(b2);
 		world.addBond(b2);
 		c1.addBond(b1);
 		world.addBond(b1);
 		world.addBody(c1 = new Circle(10,10));
 		c1.setPosition(v(-100,-10));
 		c1.setVelocity(v(50,0));
 		world.addBody(c1 = new Circle(10,10));
		c1.setPosition(v(100,10));
 		c1.setVelocity(v(-50,0));
 	}
 }
