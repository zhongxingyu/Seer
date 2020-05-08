 package structures;
 
 import java.awt.Color;
 import java.util.ArrayList;
 
 /**
  * Gives functionality for the creation of randomized levels.
  * @author Sean Lewis
  */
 public class Randomizer {
 
 	public static Level randomLevel() {
 		Ball ball = randomBall();
 		ArrayList<Body> bodies = randomBodies(ball);
 
 		ArrayList<GoalPost> goals = new ArrayList<GoalPost>();
 		ArrayList<WarpPoint> warps = new ArrayList<WarpPoint>();
 		ArrayList<Blockage> blockages = new ArrayList<Blockage>();
 		double followFactor = 5.0;
 		double gravityStrength = 1.0;
 
 		return new Level(ball, bodies, warps, goals, blockages, followFactor,
 				gravityStrength);
 	}
 
 	private static Ball randomBall() {
 		int xMean, yMean;
 
 		double rand = Math.random();
 		if (rand < 0.6)
 			xMean = 75;
 		else if (rand < .8)
 			xMean = 500;
 		else
 			xMean = 800;
 
 		rand = Math.random();
 		if (rand < 0.6)
 			yMean = 50;
 		else if (rand < .8)
 			yMean = 300;
 		else
 			yMean = 500;
 
 		int x = CalcHelp.gaussianInteger(xMean, 15);
 		int y = CalcHelp.gaussianInteger(yMean, 15);
				
 		return new Ball(x, y, 3);
 	}
 
 	private static ArrayList<Body> randomBodies(Ball ball) {
 		ArrayList<Body> bodies = new ArrayList<Body>();
 		double fillPercentage = CalcHelp.gaussianDouble(.3, .2);
 		if (fillPercentage < .1)
 			fillPercentage = .1;
 		if (fillPercentage > .5)
 			fillPercentage = .5;
 
 		double mustBeFilled = fillPercentage * 1000 * 700;
 
 		for (double filled = 0.0; filled < mustBeFilled;) {		
 			
 			int x = CalcHelp.randomInteger(-100, 1100);
 			int y = CalcHelp.randomInteger(-100, 800);
 			int r = CalcHelp.gaussianInteger(120, 90);
 			Body currentBody = new Body(x, y, r, randColor());
 			
 			while (bodyBallOverlap(ball, bodies, currentBody)) {
 				x = CalcHelp.randomInteger(-100, 1100);
 				y = CalcHelp.randomInteger(-100, 800);
 				r = CalcHelp.gaussianInteger(120, 90);
 				currentBody = new Body(x, y, r, randColor());
 			}
 			
 			bodies.add(currentBody);
 			filled += Math.PI * r * r;
 		}
 
 		return bodies;
 	}
 
 	/**
 	 * Returns if currentBody overlaps ball of any Body in bodies.
 	 */
 	private static boolean bodyBallOverlap(Ball ball, ArrayList<Body> bodies,
 			Body currentBody) {		
 		if (ball.intersects(currentBody))
 			return true;		
 		for (Body b : bodies)
 			if (b.intersects(currentBody))
 				return true;		
 		return false;
 	}
 
 	private static Color randColor() {
 		int r = CalcHelp.randomInteger(0, 255);
 		int g = CalcHelp.randomInteger(0, 255);
 		int b = CalcHelp.randomInteger(0, 255);
 		return new Color(r, g, b);
 	}
 
 }
