 package factory.graphics;
 
 import java.awt.Graphics;
 
 public class GraphicRobot extends GraphicAnimatedObject
 {
 	int fx, fy, ftheta;							// final position (destination) and final angle
 	int destinationNest, destinationKit;		// destination nest and destination kit as provided by back-end (zero-based indexing)
 	int state;									// "custom" variable for each robot to know when to move and rotate
 	int[] movementCheckingOrders;				// priority list for checking which direction to move in (up, down, left, or right)
 	
 	/**
 	 * Sets the x,y, destination coordinates for the robot to move to as well as the final direction it should be facing.
 	 * @param init_fx Final destination x.
 	 * @param init_fy Final destination y.
 	 * @param init_ftheta Final destination direction.
 	 */
 	public void setDestination(int init_fx, int init_fy, int init_ftheta)
 	{
 		fx = init_fx;
 		fy = init_fy;
 		ftheta = init_ftheta;
 	}
 
 	/**
 	 * Sets the state of the robot.
 	 * @param init_state
 	 */
 	public void setState(int init_state)
 	{
 		state = init_state;
 	}
 	
 	/**
 	 * Scoots the robot back by an amount to the left to tweak its rotation (for visual appeal only!).
 	 * @param amount
 	 */
 	public void adjustShift(int amount)
 	{
 		x -= amount;
 	}
 	
 	/**
 	 * Returns the state of the robot.
 	 * @return state
 	 */
 	public int getState()
 	{
 		return state;
 	}
 	
 	/**
 	 * Move function of the robot. Handles both translational and rotational movement.
 	 */
 	public void move()
 	{
 		if(y == fy && x == fx)	// check if robot has arrived at destination
 		{
 			if(theta-ftheta == 0)	// check if robot is facing the right direction
 			{
 				if(state % 2 == 1)	// increment the state of the robot
 					state += 1;
 			}
			else if(theta-ftheta <= 180)	// robot needs to turn counterclockwise
 				theta -= dtheta;
 			else if(theta-ftheta > 180)	// robot needs to turn clockwise
 				theta += dtheta;
 		}
 		// Loop through the directional orders of the robot
 		// This gives priority in which directions (up, down, left, or right) to move/rotate towards first
 		for(int i = 0; i < movementCheckingOrders.length; i++)
 		{
 			switch(movementCheckingOrders[i])
 			{
 			case 0 : 				// move right
 				if(x < fx)
 				{
 					if(theta <= 180 && theta > 0)
 						theta -= dtheta;
 					else if(theta > 180 && theta < 360)
 						theta += dtheta;
 					else
 						x += dx;
 					break;
 				}
 			case 1 : 				// move up
 				if(y > fy)
 				{
 					if(theta > 90 && theta < 270)
 						theta -= dtheta;
 					else if(theta < 90 || theta >= 270)
 						theta += dtheta;
 					else
 						y -= dy;
 					break;
 				}
 			case 2 : 				// move left
 				if(x > fx)
 				{
 					if(theta < 180 && theta >= 0)
 						theta += dtheta;
 					else if(theta > 180 && theta <= 360)
 						theta -= dtheta;
 					else
 						x -= dx;
 					break;
 				}
 			case 3 : 				// move down
 				if(y < fy)
 				{
 					if(theta > 90 && theta < 270)
 						theta += dtheta;
 					else if(theta <= 90 || theta > 270)
 						theta -= dtheta;
 					else
 						y += dy;
 					break;
 				}
 			}
 		}
 		
 		// Adjust angle for going under 0 degrees or over 360 degrees
 		if(theta < 0) theta = 360;
 		else if(theta > 360) theta = 0;
 	}
 	
 	/**
 	 * Paints the robot
 	 * @param g Graphics variable used to paint.
 	 */
 	public void paint(Graphics g)
 	{
 		// Draw the robot
 		g.drawImage(image, x, y, imageWidth, imageHeight, null);
 	}
 }
