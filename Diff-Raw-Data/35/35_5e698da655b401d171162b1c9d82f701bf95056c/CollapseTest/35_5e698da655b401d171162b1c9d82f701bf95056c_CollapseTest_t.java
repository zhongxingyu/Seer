 package nehsics.test;
 import nehsics.ui.*;
 import nehsics.bodies.*;
 import java.awt.*;
 import static nehsics.math.Util.*;
 
 public class CollapseTest extends Tester {
 	public final static String NAME = "Model Inaccuracies";
 
 	public static void main(String[] args) {
 		new Starter(NAME);
 	}
 
 	public CollapseTest(Canvas c) {
 		super(c);
 	}
 
 	protected void setup() {
 		display.setScale(3e-8);
 		PRECISION = 1;
 		for (int i=0; i < 10; i++) {
 			for (int j=0; j < 10; j++) {
 				Circle tmp = new Circle(SUN_RADIUS/2, SUN_MASS*1e6);
 				tmp.setPosition(v(SUN_RADIUS*i-5*SUN_RADIUS,
 					SUN_RADIUS*j-5*SUN_RADIUS));
					}
					}
		display.setScale(5e-8);
		PRECISION = 10;
		for (int i=0; i < 20; i++) {
			for (int j=0; j < 20; j++) {
				Circle tmp = new Circle(SUN_RADIUS/2, SUN_MASS*1000000);
				tmp.setPosition(v(SUN_RADIUS*i-5*SUN_RADIUS,SUN_RADIUS*j-5*SUN_RADIUS));
 				world.addBody(tmp);
 			}
 		}
 	}
 }
