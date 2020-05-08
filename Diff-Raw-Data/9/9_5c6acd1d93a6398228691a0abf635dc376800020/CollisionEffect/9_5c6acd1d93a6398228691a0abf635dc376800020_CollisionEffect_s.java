 package graphics;
 
 import java.awt.Color;
 import java.awt.Graphics;
 import java.util.ArrayList;
 import structures.*;
 
 public final class CollisionEffect extends GraphicEffect {
 
 	private static final int EFFECT_TIME = 1000;
 
 	private static Level currentLevel;
 	private static long startTime;
 	private static ArrayList<Particle> particles;
 	private static double[] shakeValues;
 	private static boolean started = false;
 	private static int xShift;
 	private static int yShift;
 
 	/**
 	 * Starts the effect.
 	 * @param currentLevel the level the effect is taking place on
 	 */
 	public static void start(Level currentLevel) {
 		started = true;
 		CollisionEffect.currentLevel = currentLevel;
 		int currentXShift = (int) currentLevel.getScreenXShift();
 		int currentYShift = (int) currentLevel.getScreenYShift();
 		Ball ball = currentLevel.getBall();
 		Body intersected = currentLevel.getIntersectingBody();
 		particles = Particle.generateParticles(ball, intersected);
 		shakeValues = new double[6];
 		double speed = ball.getVelocity().getLength();
 
 		if (speed < .25)
 			speed = .25;
 		if (speed > 2.5)
 			speed = 2.5;
 
 		double shakeFactor = 3 * speed / currentLevel.getFollowFactor();
 		if (currentLevel.getFollowFactor() == 0.0)
 			shakeFactor = 25 * speed;
 		// 1st value is multiplicative factor
		// TODO: make based of rate at which screen shifts
 		int sign1 = -1;
 		if (ball.getCenter().x + currentXShift < 0) {
 			sign1 = 1;
 		} else if (ball.getCenter().x + currentXShift > 0) {
 			sign1 = CalcHelp.randomSign();
 		}
 		shakeValues[0] = CalcHelp.randomDouble(35, 40) * shakeFactor * sign1;
 
 		int sign2 = -1;
 		if (ball.getCenter().y + currentYShift < 0) {
 			sign2 = 1;
 		} else if (ball.getCenter().y + currentYShift > 0) {
 			sign2 = CalcHelp.randomSign();
 		}
 		shakeValues[3] = CalcHelp.randomDouble(35, 40) * shakeFactor * sign2;
 
 		// 2nd value is sinusoidal factor
 		shakeValues[1] = CalcHelp.randomDouble(45, 50);
 		shakeValues[4] = CalcHelp.randomDouble(45, 50);
 
 		// 3rd value is exponential factor
 		shakeValues[2] = CalcHelp.randomDouble(-.0035, -.0045);
 		shakeValues[5] = CalcHelp.randomDouble(-.0035, -.0045);
 
 		startTime = System.currentTimeMillis();
 		ball.setLaunched(false);
 	}
 
 	/**
 	 * Updates the effect.
 	 * @param g The Graphics component the particles are being drawn with.
 	 * @return the new screenX and screenY shift values
 	 */
 	public static int[] update() {
 		double screenXShift = currentLevel.getScreenXShift();
 		double screenYShift = currentLevel.getScreenYShift();
 		long elapsedTime = elapsedTime();
 		screenXShift += (shakeValues[0]
 				* Math.sin(elapsedTime / shakeValues[1]) * Math
 				.exp(shakeValues[2] * elapsedTime));
 		screenYShift += (shakeValues[3]
 				* Math.sin(elapsedTime / shakeValues[4]) * Math
 				.exp(shakeValues[5] * elapsedTime));
 
 		for (int i = 0; i < particles.size(); i++) {
 			Particle p = particles.get(i);
 
 			for (Body bod : currentLevel.getBodies()) {
 				if (bod.intersects(p)
 						|| bod.getCenter().getDistanceSquared(p.getCenter()) <= (bod
 								.getRadius() * .5) * (bod.getRadius() * .5)
 						&& i != 0) {
 					particles.remove(i);
 					i--;
 				}
 			}
 			for (Blockage blockage : currentLevel.getBlockages()) {
 				if (blockage.intersects(p.getCenter()) && i != 0) {
 					particles.remove(i);
 					i--;
 				}
 			}
 			for (GoalPost gp : currentLevel.getGoalPosts()) {
 				if (p.intersects(gp) && i != 0) {
 					particles.remove(i);
 					i--;
 				}
 			}
 			p.setVelocity(p.getVelocity().multiply(0.99));
 			p.move();
 
 		}
 		xShift = (int) screenXShift;
 		yShift = (int) screenYShift;
 		return new int[] { xShift, yShift };
 	}
 
 	public static void draw(Graphics g) {
 		for (Particle p : particles) {
 			p.draw(xShift, yShift, g);
 		}
 	}
 
 	private static long elapsedTime() {
 		return System.currentTimeMillis() - startTime;
 	}
 
 	/**
 	 * Returns if the effect should still be allowed to run.
 	 * @return true if the effect is still allowed to run
 	 */
 	public static boolean running() {
 		return elapsedTime() < EFFECT_TIME;
 	}
 
 	public static boolean started() {
 		return started;
 	}
 
 	/**
 	 * Clears data so the effect can be run again. Must be called before the
 	 * effect can occur again.
 	 */
 	public static void kill() {
 		startTime = 0L;
 		started = false;
 	}
 
 	/**
 	 * Internal Particle class for 1-pixel sized CircularShapes.
 	 */
 	static class Particle extends MovableCircularShape {
 
 		public Particle(double xPosition, double yPosition, double xSpeed,
 				double ySpeed, Color color) {
 			setCenter(new Point2d(xPosition, yPosition));
 			setVelocity(new Vector2d(xSpeed, ySpeed));
 			setColor(color);
 			setRadius(1);
 		}
 
 		public void draw(double dx, double dy, Graphics g) {
 			g.setColor(color);
 			g.fillOval((int) Math.round(center.x - 0.5 + dx),
 					(int) Math.round(center.y - 0.5 + dy), 1, 1);
 		}
 
 		/**
 		 * Internal routine for generating the List of particles on collision.
 		 */
 		public static ArrayList<Particle> generateParticles(Ball ball, Body body) {
 			double centerX = ball.getCenter().x;
 			double centerY = ball.getCenter().y;
 			
 			double speed = ball.getVelocity().getLength()
 					* ((body != null) ? (body.getRadius() / 100.0) : 1);
 			
 			if (speed < .50)
 				speed = .50;
 			if (speed > 2.50)
 				speed = 2.50;
 			if (body != null) {
 				double max = Math.pow(body.getRadius() + ball.getRadius(), 2);
 				while (body.getCenter().getDistanceSquared(new Point2d(centerX, centerY)) < max) {
 					centerX -= ball.getVelocity().getXComponent();
 					centerY -= ball.getVelocity().getYComponent();
 				}
 			}
 
 			ArrayList<Particle> particles = new ArrayList<Particle>();
 			int num = (int) (300 * speed);
 			if (num < 150)
 				num = 150;
 			else if (num > 400)
 				num = 400;
 
 			for (int i = 0; i < num; i++) {
 				double angle = (Math.random() * 2 * Math.PI);
 				double xSpeed = 0.75 * (Math.random() * speed * Math.cos(angle));
 				double ySpeed = 0.75 * (Math.random() * speed * Math.sin(angle));
 				
 				Color c;				
 				double rand = Math.random();
 				if (body == null)
 					c = CalcHelp.getShiftedColor(ball.getColor(), 100);
 				else if (rand < 0.3)
 					c = CalcHelp.getShiftedColor(body.getColor(), 100);
 				else if (rand < 0.40)
 					c = CalcHelp.getShiftedColor(new Color(230, 130, 70), 100);
 				else
 					c = CalcHelp.getShiftedColor(ball.getColor(), 100);				
 
 				Particle newParticle = new Particle(centerX, centerY, xSpeed, ySpeed, c);
 				particles.add(newParticle);
 			}
 			
 			return particles;
 		}
 
 	}
 
 }
