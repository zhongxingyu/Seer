 package navigation;
 
 import odometer.Odometer;
 import odometer.OdometerCorrection;
 import robot.Robot;
 import lejos.nxt.LCD;
 import lejos.nxt.Motor;
 
 /**
  * This class will allow the robot to travel between to coordinates. The first coordinate is the actual position of the robot 
  * and the second coordinate(this coordinate must be given by the suer) is the destination point.
  * This class will only work if there are no obstacles in the trajectory.
  * The coordinates must be expressed in centimeters(cm). The coordinates represent the x and y values of a point in a Cartesian plane.
  * @author Nicolas Guzman
  */
 public class Navigation {
 	// put your navigation code here 
 	
 	private Odometer odo;
 	private Robot robot;
 	private OdometerCorrection leftLight;
 	private OdometerCorrection rightLight;
 	private static boolean turn = true;
 	private static boolean navigating = true;
 	private static boolean LEFT_WHEEL_IN_PLACE = false;
 	private static boolean RIGHT_WHEEL_IN_PLACE = false;
 	private static double[][] wayPoints;
 	private static final double WIDTH = 18;
 	private static double RADIUS = 2.77;
 	private static final int ROTATIONAL_SPEED = 50;
 	private static final int FORWARD_SPEED = 175;
 
 	/**
 	 * The constructor creates an access to the odometer for the methods in this class. 
 	 * @param Odometer The odometer used by the robot to oriented its position on the field.
 	 */
	public Navigation(Odometer odo, Robot robot, OdometerCorrection leftLight, OdometerCorrection RightLight)
 	{
 		this.odo = odo;
 		this.robot = robot;
		this.leftLight = leftLight;
		this.rightLight = RightLight;
 	}
 	
 	/**
 	 *  This method will allow the robot to travel between two given points. 
 	 *  However, this method will fail if there are obstacles in the trajectory.
 	 * @param Centimeter(cm) The x component of the destination point.
 	 * @param Centimeter(cm) The y component of the destination point.
 	 * @return
 	 * @exception 
 	 */
 	public void travelTo(double x, double y) 
 	{	
 		double[] odo_values = new double[3];
 		double segmentDistanceLeft;
 		double theta;
 		odo.getPosition(odo_values, new boolean[]{true,true,true});
   	  	//distance left to waypoint destination.
 	    segmentDistanceLeft = calculateEncludianDistance(odo_values[0], odo_values[1],x,y);
 	    if(turn)// check if it is the time to turn right now.
 	    {
 	    	leftLight.setDoDetectionOfLines(false);
 	    	rightLight.setDoDetectionOfLines(false);
 	    	leftLight.resetCounter();
 	    	rightLight.resetCounter();
 	    	theta = Math.atan2(x - wayPoints[0][0], y - wayPoints[0][1]);
 	    	theta = theta * 180 / Math.PI;
 	    	turnTo(minimalAngle(theta-odo_values[2]));  
 	    }
 		leftLight.setDoDetectionOfLines(true);
 		rightLight.setDoDetectionOfLines(true);
 	    robot.moveRobotForward(FORWARD_SPEED, FORWARD_SPEED);
 	    while(segmentDistanceLeft > 3)
 	    {
 	    	if(leftLight.getStopWheel() && !LEFT_WHEEL_IN_PLACE)
 	    	{
 	    		robot.stopRobot(true, false);
 	    		LEFT_WHEEL_IN_PLACE = true;
 	    	}
 	    	if(rightLight.getStopWheel() && !RIGHT_WHEEL_IN_PLACE)
 	    	{
 	    		robot.stopRobot(false, true);
 	    		RIGHT_WHEEL_IN_PLACE = true;
 	    	}
 	    	if(RIGHT_WHEEL_IN_PLACE && LEFT_WHEEL_IN_PLACE)
 	    	{
 	    		leftLight.correctPosition();
 	    		LEFT_WHEEL_IN_PLACE = false;
 	    		RIGHT_WHEEL_IN_PLACE = false;
 	    		leftLight.setSleepThread(true);
 	    		rightLight.setSleepThread(true);
 	    		leftLight.setStopWheel(false);
 	    		rightLight.setStopWheel(false);
 	    		robot.moveRobotForward(FORWARD_SPEED, FORWARD_SPEED);
 	    	}
 	     	odo.getPosition(odo_values, new boolean[]{true,true,true});
 		    segmentDistanceLeft = calculateEncludianDistance(odo_values[0], odo_values[1],x,y);
 	    }
 	    navigating = false;
     	turn = true;
     	robot.stopRobot(true, true);
   	  }
 	
 	  
 	/**
 	 * This method will set a new way points to travel. 
 	 * However, after this method is call, all the old way points will be deleted
 	 * @param Centimeter(cm) The x component of the new way point.
 	 * @param Centimeter(cm) The y component of the new way point.
 	 * @return
 	 * @exception 
 	 */
 	public void setWaypoint(double x_int, double y_int)
 	{
 		
 		wayPoints=new double[][]{{x_int,y_int}};
 	}
 	
 	
 	public double[] computeComponents(double x, double y)
 	{
 		
 		double[] position=new double[3];
 		odo.getPosition(position,new boolean[]{true, true, true});
 		 if(position[2]>=0 && position[2]<=90)
 		    {
 		    	return new double[]{x - position[0], y - position[1]};	
 		    }else if(position[2]>90 && position[2]<=180)
 		    {
 		    	return new double[]{x - position[0], position[1] - y};	
 		    }else if(position[2]>180 && position[2]<=270)
 		    {
 		    	return new double[]{position[0] - x, position[1] - y};	
 		    }else if(position[2]>270 && position[2]<=360)
 		    {
 		    	return new double[]{position[0] - x, y - position[1]};	
 		    }
 		return position;
 		 
 	}
 	
 	/**
 	 * This method will determine if the robot is navigating or not
 	 * @param 
 	 * @return true if the robot is navigating (moving); false otherwise.
 	 * @exception 
 	 */
 	public boolean isNavigating()
 	{
 		if(navigating)
 		{
 			return true;
 		}else
 		{
 			return false;
 		}
 	}
 	
 	/**
 	 * This method will reset all the flags used in the travelTo and turnTo methods.
 	 * This method should be called after inserting a new way point.
 	 * @param
 	 * @return
 	 * @exception 
 	 */
 	public void resetNavigation()
 	{
 		turn = true;
 		navigating = true;
 	}
 	
 	/**
 	 * This method will compute if the robot should turn anti-clockwise or clockwise 
 	 * to oriented its position to the desired angle.
 	 * The minimal angle will always be between 180 and -180 degrees.
 	 * @param Angle(degrees) The desired angle for which it is needed to find its minimal representation in one circle.
 	 * @return Angle(degrees) The minimal representation of an angle in one circle. 
 	 * 		   It could be negative or positive.
 	 * @exception 
 	 */
 	private double minimalAngle(double delta_angle)
 	{
 		if(delta_angle <= 180 && delta_angle >= -180)
 		{
 			return delta_angle;
 		}
 		else if(delta_angle < -180)
 		{
 			return delta_angle + 360;
 		}
 		else if(delta_angle > 180)
 		{
 			return delta_angle - 360;
 		}
 		return 0;
 	}
 	
 	/**
 	 * This method will make turn the robot to an specific angle.
 	 * A negative angle means turn anti-clockwise. A positive angle means turn clockwise.
 	 * @param Angle(degrees) The desired angle to turn.
 	 * @return
 	 * @exception 
 	 */
 	public void turnTo(double theta){
 		//USE THE FUNCTIONS setForwardSpeed and setRotationalSpeed from TwoWheeledRobot!
 		//robot.setRotationSpeed(rotating_speed);
 		// going anti 
 		int degreesToTurn = convertAngle(RADIUS, WIDTH, Math.abs(theta));
 		if(theta<0)
 		{ 
 			robot.rotateRobot(degreesToTurn, -degreesToTurn, ROTATIONAL_SPEED, ROTATIONAL_SPEED);
 		}
 		else
 		{
 			robot.rotateRobot(-degreesToTurn, degreesToTurn, ROTATIONAL_SPEED, ROTATIONAL_SPEED);
 		}
 	}	
 	
 	/**
 	 * This method compute the total number rotations(degrees) that the wheels need to do 
 	 * in order to oriented to a given angle
 	 * @param Centemeter(cm) the radius of the wheels(in cm).
 	 * @param Centemeter(cm) the distance between the center of the two wheels(in cm).
 	 * @param Centemeter(cm) the absolute value of the desired angle(in degrees) to turn.
 	 * @return The number of rotations(degrees) that the wheels need to reoriented the robot.
 	 * @exception 
 	 */
 	private static int convertAngle(double radius, double width, double angle) 
 	{
 		return convertDistance(radius, Math.PI * width * angle / 360.0);
 	}
 	
 	/**
 	 * this method computes how much the wheels of the robot are going to move in order to reoriented the robot (linear distance in cm)
 	 * @param Centemeter(cm) the radius of the wheels.
 	 * @param Centemeter(cm) the distance that will be covered while turning.
 	 * @return The distance that the robot need to turn (This is a linear distance in cm).
 	 * @exception 
 	 */
 	
 	private static int convertDistance(double radius, double distance)
 	{
 		return (int) ((180.0 * distance) / (Math.PI * radius));
 	}
 
 	/**
 	 * @param The actual x-componet(x_ini) of the position of the robot(in cm).
 	 * @param The actual y-componet(y_ini) of the position of the robot(in cm).
 	 * @param The  x-componet(x_des) of the destination of the robot(in cm).
 	 * @param The  y-componet(y_des) of the destination of the robot(in cm).
 	 * @return The distance between the actual position of the robot and its destination point(in cm).
 	 * @exception 
 	 */
 	private static double calculateEncludianDistance(double x_ini, double y_ini, double x_des, double y_des)
 	{
 		return Math.sqrt(Math.pow(x_ini-x_des,2)+Math.pow(y_ini-y_des,2));		
 	}
 }
