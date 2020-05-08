 package com.jverkamp.brong.thing;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Graphics2D;
 import java.awt.event.KeyEvent;
 import java.awt.geom.Point2D;
 
 /**
  * Create a new paddle.
  */
 public class Paddle extends Thing {
	public final static double FRICTION = 0.9; // 1.0 is no friction, 0 is maximum
 	public final static double ACCELERATION = 10.0;
 	public final static double MAX_VELOCITY = 100.0;
 	public final static Dimension SIZE = new Dimension(50, 10);
 	
 	public final static int MOVE_ON_ARROWS = 0;
 	public final static int MOVE_ON_AD = 1;
 	int MovementMode;
 	
 	Point2D.Double Velocity;
 	
 	/**
 	 * Create a new paddle.
 	 * @param center The center point of the paddle. Y will never change.
 	 * @param movementMode Move on arrows or A/D.
 	 */
 	public Paddle(Point2D.Double center, int movementMode) {
 		super(center);
 		MovementMode = movementMode;
 		Velocity = new Point2D.Double(0, 0);
 	}
 	
 	/**
 	 * Update the paddle.
 	 * @param world The world the paddle is in.
 	 * @param time The number of seconds since last update.
 	 */
 	@Override
 	public void update(World world, double time) {
 		// Check if our movement mode was triggered.
 		if (MovementMode == MOVE_ON_ARROWS && world.Keys[KeyEvent.VK_LEFT] 
 				|| MovementMode == MOVE_ON_AD && world.Keys[KeyEvent.VK_A])
			Velocity.x -= ACCELERATION;
 		else if (MovementMode == MOVE_ON_ARROWS && world.Keys[KeyEvent.VK_RIGHT] 
 				|| MovementMode == MOVE_ON_AD && world.Keys[KeyEvent.VK_D])
			Velocity.x += ACCELERATION;
 		else
			Velocity.x *= FRICTION; 
 		
 		// Update position.
 		Center.x += Velocity.x * time;
 		
 		// Boundry checking, bounce off of walls.
 		if (Center.x - SIZE.width / 2 < 0 || Center.x + SIZE.width / 2 >= world.Bounds.width) {
 			Velocity.x *= -1;
 			Center.x += Velocity.x * time;
 		}	 
 	}
 
 	@Override
 	public void draw(Graphics2D g2d) {
 		g2d.setColor(Color.BLACK);
 		g2d.fillRect(
 			(int) (Center.x - SIZE.width / 2), 
 			(int) (Center.y - SIZE.height / 2), 
 			SIZE.width,
 			SIZE.height
 		);
 	}
 }
