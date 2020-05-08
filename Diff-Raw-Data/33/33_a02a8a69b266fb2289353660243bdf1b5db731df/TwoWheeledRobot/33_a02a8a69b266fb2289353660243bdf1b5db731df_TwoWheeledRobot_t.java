 package T07;
 import lejos.nxt.*;
 
 public class TwoWheeledRobot {
 	
 	// this class contains the measurements of the robot
 	public static final double DEFAULT_LEFT_RADIUS = 2.68;
 	public static final double DEFAULT_RIGHT_RADIUS = 2.68;
 	public static final double DEFAULT_WIDTH = 16.2;
 	public NXTRegulatedMotor leftMotor, rightMotor, lightSensorMotor;
 	public UltrasonicSensor middleUSSensor, rightUSSensor;
 	public LightSensor leftLS, rightLS, middleLS;
 	private  CommunicationController communicationController;
 	private double leftRadius, rightRadius, width;
 	private double forwardSpeed, rotationSpeed;
 	
 	public TwoWheeledRobot(NXTRegulatedMotor leftMotor,
 						   NXTRegulatedMotor rightMotor,
						   NXTRegulatedMotor lightSensorMotor,
 						   UltrasonicSensor middleUSSensor,
 						   LightSensor leftLS,
 						   LightSensor rightLS,
 						   CommunicationController communicationController) {
 		this.leftMotor = leftMotor;
 		this.rightMotor = rightMotor;
		this.lightSensorMotor = lightSensorMotor;
 		this.leftLS = leftLS;
 		this.rightLS = rightLS;
 		this.middleUSSensor = middleUSSensor;
 		this.leftRadius = DEFAULT_LEFT_RADIUS;
 		this.rightRadius = DEFAULT_RIGHT_RADIUS;
 		this.width = DEFAULT_WIDTH;
 		this.communicationController = communicationController;
 	}
 	
 	// control the slave brick to perform tasks
 	public void closeClaw() {
 		communicationController.sendCloseClaw();
 	}
 	
 	public void openClaw() {
 		communicationController.sendOpenClaw();
 	}
 	
 	public void liftClaw() {
 		communicationController.sendRaiseLift();
 	}
 	
 	public void lowerClaw() {
 		communicationController.sendLowerLift();
 	}
 	
 	// read values from the slave brick
 	public int getMidLightSensorReading(){
 		return communicationController.getMidLightSensorValue();
 	}
 	
 	// accessors
 	public CommunicationController getCommunicationController(){
 		return this.communicationController;
 	}
 	
 	public double getDisplacement() {
 		return (leftMotor.getTachoCount() * leftRadius +
 				rightMotor.getTachoCount() * rightRadius) *
 				Math.PI / 360.0;
 	}
 	
 	public double getHeading() {
 		return (leftMotor.getTachoCount() * leftRadius -
 				rightMotor.getTachoCount() * rightRadius) / width;
 	}
 	
 	public void getDisplacementAndHeading(double [] data) {
 		int leftTacho, rightTacho;
 		leftTacho = leftMotor.getTachoCount();
 		rightTacho = rightMotor.getTachoCount();
 		
 		data[0] = (leftTacho * leftRadius + rightTacho * rightRadius) *	Math.PI / 360.0;
 		data[1] = (leftTacho * leftRadius - rightTacho * rightRadius) / width;
 	}
 	
 	// mutators
 	public void setForwardSpeed(double speed) {
 		forwardSpeed = speed;
 		setSpeeds(forwardSpeed, 0);
 	}
 	
 	public void setRotationSpeed(double speed) {
 		rotationSpeed = speed;
 		setSpeeds(0, rotationSpeed);
 	}
 	
 	public void setSpeeds(double forwardSpeed, double rotationalSpeed) {
 		double leftSpeed, rightSpeed;
 
 		this.forwardSpeed = forwardSpeed;
 		this.rotationSpeed = rotationalSpeed; 
 
 		leftSpeed = (forwardSpeed + rotationalSpeed * width * Math.PI / 360.0) *
 				180.0 / (leftRadius * Math.PI);
 		rightSpeed = (forwardSpeed - rotationalSpeed * width * Math.PI / 360.0) *
 				180.0 / (rightRadius * Math.PI);
 
 		// set motor directions
 		if (leftSpeed > 0.0)
 			leftMotor.forward();
 		else {
 			leftMotor.backward();
 			leftSpeed = -leftSpeed;
 		}
 		
 		if (rightSpeed > 0.0)
 			rightMotor.forward();
 		else {
 			rightMotor.backward();
 			rightSpeed = -rightSpeed;
 		}
 		
 		// set motor speeds
 		if (leftSpeed > 900.0)
 			leftMotor.setSpeed(900);
 		else
 			leftMotor.setSpeed((int)leftSpeed);
 		
 		if (rightSpeed > 900.0)
 			rightMotor.setSpeed(900);
 		else
 			rightMotor.setSpeed((int)rightSpeed);
 		
 	}
 	
 	// 
 	
 	// method that returns if leftMotor is moving
 	public boolean leftMotorMoving() {
 		return leftMotor.isMoving();
 	}
 	
 	// method that returns if rightMotor is moving
 	public boolean rightMotorMoving() {
 		return rightMotor.isMoving();
 	}
 	
 	// returns whether the motors are moving or not
 	public boolean motorsMoving() {
 		if (rightMotorMoving() || leftMotorMoving()) {
 			return true;
 		}
 		return false;
 	}
 	
 	// method that stops the leftmotor only
 	public void stopLeftMotor () {
 		leftMotor.stop(true);
 	}
 	
 	// method that stops the rightmotor only
 	public void stopRightMotor () {
 		rightMotor.stop(true);
 	}
 	
 	/**
 	 * both motor rotate a certain degree
 	 * @param angle
 	 */
 	
 	// TODO: Check if this turns the desired angle
 	public void rotate(int angle) {
 		rightMotor.rotate(angle, true);
 		leftMotor.rotate(-angle);
 	}
 	
 	/**
 	 * stop two motors at the same time
 	 */
 	public void stop() {
 		rightMotor.stop(true);
 		leftMotor.stop();
 	}
 	
 	// method that moves the robot forward a specific distance
 	public void moveForwardDistance(double distance) {
 		leftMotor.forward();
 		rightMotor.forward();
 		leftMotor.rotate(convertDistance(leftRadius, distance), true);
 		rightMotor.rotate(convertDistance(rightRadius, distance), false);
 	}
 	
 	// taken from the square driver class (lab2) converts the turn angle into a distance for the convertDistance method
 	private static int convertAngle(double radius, double width, double angle) {
 		return convertDistance(radius, Math.PI * width * angle / 360.0);
 	}
 	
 	// taken from the square drive class, converts into a usable angle displacement (degrees) for the rotate operation
 	private static int convertDistance(double radius, double distance) {
 		return (int) ((180.0 * distance) / (Math.PI * radius));
 	}
 }
