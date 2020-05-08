 package dinaBOT.navigation;
 
 import lejos.nxt.Motor;
 import java.lang.Math;
 
 /**
  * BasicMovement is a simple implementation of the specifications of the {@link Movement} interface. It provides simple movement paradigms for robot movement. This implementation assumes a robot with two wheels that can rotate about itself and who's position is accurately reflected by the associated {@link Odometer}.
  *
  * @author Severin Smith
  * @see Odometer
  * @see Movement
  * @see Navigation
  * @version 2
 */
 public class BasicMovement implements Movement {
 
 	/* -- Static Variables -- */
 
 	//Possible states for the movement daemon to be in
 	enum Mode { INACTIVE, ROTATE_CW, ROTATE_CCW, ADVANCE }
 
 	/* -- Instance Variables -- */
 
 	Odometer odometer;
 
 	Motor left_motor, right_motor;
 
 	MovementDaemon movement_daemon;
 	Thread movement_daemon_thread;
 	boolean movement_daemon_running; //Run condition for the movement_daemon_thread
 
 	boolean moving;
 
 	/**
 	 * Create a new BasicMovement instance with the given odometer and left and right motors.
 	 *
 	 * @param odometer the odometer to be used
 	 * @param left_motor the robot's left motor
 	 * @param right_motor the robot's right motor
 	*/
 	public BasicMovement(Odometer odometer, Motor left_motor, Motor right_motor) {
 		//Set up odometer and motors
 		this.odometer = odometer;
 
 		this.left_motor = left_motor;
 		this.right_motor = right_motor;
 
 		moving = false; //We aren't moving initally
 
 		//Create movement daemon
 		movement_daemon = new MovementDaemon();
 
 		//Start it's thread
 		movement_daemon_running = true;
 
 		movement_daemon_thread = new Thread(movement_daemon);
 		movement_daemon_thread.setDaemon(true);
 		movement_daemon_thread.start();
 	}
 
 	public synchronized void driveStraight(int direction, double distance, int speed) {
 		stop();
 		if(speed == 0 || distance == 0) return;
 		
 		direction = direction%4;
 		if(direction < 0) direction += 4;
 		
 		turnTo(direction*Math.PI/2, speed);
 		
 		double target_theta = direction*Math.PI/2;
		double[] initial_position = odometer.getPosition();
 		
 		left_motor.setSpeed(speed);
 		right_motor.setSpeed(speed);
 		
 		left_motor.forward();
 		right_motor.forward();
 		
		double[] current_position = odometer.getPosition();
 		
 		if(direction == 0) {
 			while(Math.abs(current_position[0]-initial_position[0]) < distance) {
 				target_theta = direction*Math.PI/2+(initial_position[1]-current_position[1])/10;
 				
 				if(Math.abs(current_position[2]-target_theta) < 0.035) continue;
 				
 				if(current_position[2] < target_theta) {
 					 right_motor.setSpeed(speed+10);
 					 left_motor.setSpeed(speed-0);
 				} else {
 					right_motor.setSpeed(speed-10);
 					left_motor.setSpeed(speed+10);
 				}
 				current_position = odometer.getPosition();
 			}
 		} else if(direction == 1) {
 			
 		} else if(direction == 2) {
 
 		} else if(direction == 3) {
 
 		}
 		
 		stop();
 	}
 
 	public void goForward(double distance, int speed) {
 		goForward(distance, speed, false);
 	}
 
 	public synchronized void goForward(double distance, int speed, boolean returnImmediately) {
 		stop();
 		if(speed == 0 || distance == 0) return; //Avoid silly input
 		movement_daemon.goForward(distance, speed);
 		if(!returnImmediately) while(movement_daemon.isActive()) Thread.yield();
 	}
 
 	public void turn(double angle, int speed) {
 		turn(angle, speed, false);
 	}
 
 	public void turn(double angle, int speed, boolean immediateReturn) {
 		turnTo(angle+odometer.getPosition()[2], speed, immediateReturn);
 	}
 
 	public void turnTo(double angle, int speed) {
 		turnTo(angle, speed, false);
 	}
 
 	public synchronized void turnTo(double angle, int speed, boolean returnImmediately) {
 		stop();
 		if(speed == 0) return; //Avoid silly input
 		movement_daemon.turnTo(angle, speed);
 		if(!returnImmediately) while(movement_daemon.isActive()) Thread.yield();
 	}
 
 	public synchronized void rotate(boolean direction, int speed) {
 		stop();
 
 		left_motor.setSpeed(speed);
 		right_motor.setSpeed(speed);
 
 		moving = true;
 
 		if(direction) {
 			left_motor.backward();
 			right_motor.forward();
 		} else {
 			left_motor.forward();
 			right_motor.backward();
 		}
 	}
 
 	public synchronized void forward(int speed) {
 		stop();
 
 		left_motor.setSpeed(speed);
 		right_motor.setSpeed(speed);
 
 		moving = true;
 
 		left_motor.forward();
 		right_motor.forward();
 	}
 
 	public synchronized void backward(int speed) {
 		stop();
 
 		left_motor.setSpeed(speed);
 		right_motor.setSpeed(speed);
 
 		moving = true;
 
 		left_motor.backward();
 		right_motor.backward();
 	}
 
 	public synchronized void stop() {
 		if(movement_daemon.isActive()) movement_daemon.stop();
 
 		left_motor.stop();
 		right_motor.stop();
 
 		moving = false;
 	}
 
 
 	public boolean isMoving() {
 		return moving || movement_daemon.isActive();
 	}
 
 	public void shutdown() {
 		stop();
 		movement_daemon_running = false;
 		while(movement_daemon_thread.isAlive()) Thread.yield();
 	}
 
 	/**
 	 * The MovementDaemon actually handles the details of all complex motions such as driveStraight, goForward and turnTo in a seperate thread. This allows theses method to be in either block or non-block.
 	 * <p>
 	 * The MovementDaemon can either be suspended (Mode.INACTIVE) when it is not in use. In this state the thread yields. Or it can be in one if the implmented other modes such as Mode.ROTATE_CCW, Mode.ROTATE_CW and Mode.ADVANCE in which case the thread is active. Within the BasicMovement class the status of the thread can be polled with isActive()
 	*/
 	class MovementDaemon implements Runnable {
 
 		//The current mode of the MovementDaemon
 		Mode mode;
 
 		//Stored information, used depending on the mode
 		double target_distance, target_angle;
 		double[] initial_position;
 		double[] current_position;
 
 		/**
 		 * Create MovementDaemon
 		 *
 		*/
 		MovementDaemon() {
 			mode = Mode.INACTIVE; //Initially inactive
 
 			//Setup the arrays
 			initial_position = new double[3];
 			current_position = new double[3];
 		}
 
 		/**
 		 * Run method from Runnable. This method will suspend unless some complex movement is in progress
 		 *
 		*/
 		public void run() {
 			while(movement_daemon_running) { //While we're not shutting down
 				if(mode == Mode.INACTIVE) { //If inactive yield
 					Thread.yield();
 				} else { //Otherwise execute correct action accoring to current mode
 					if(mode == Mode.ADVANCE) { //Advance set distance
 						if(target_distance*target_distance < ((current_position[0]-initial_position[0])*(current_position[0]-initial_position[0])+(current_position[1]-initial_position[1])*(current_position[1]-initial_position[1]))) {
 							stop();
 						}
 					} else if(mode == Mode.ROTATE_CCW) { //Rotate set angle counter clockwise
 						if((target_angle - current_position[2]) <= 0) { //Until the sign of the relative angle changes
 							stop();
 						}
 
 					} else if(mode == Mode.ROTATE_CW) { //Rotate set angle clockwise
 						if((target_angle - current_position[2]) >= 0) { //Until the sign of the relative angle changes
 							stop();
 						}
 					}
 					current_position = odometer.getPosition(); //Update position array
 					Thread.yield(); //Yield for a little while
 				}
 			}
 		}
 
 		/**
 		 * Initiate forward movement to a specified distance
 		 *
 		 * @param distance the distance to advance by (in cm)
 		 * @param speed the speed to advance at
 		*/
 		void goForward(double distance, int speed) {
 			//Set motor speed
 			left_motor.setSpeed(speed);
 			right_motor.setSpeed(speed);
 
 			//Remember start position
 			initial_position = odometer.getPosition();
 
 			//Remeber requested distance
 			target_distance = distance;
 
 			//Start motors in correct direction 
 			if(distance > 0) {
 				left_motor.forward();
 				right_motor.forward();
 			} else {
 				left_motor.backward();
 				right_motor.backward();
 			}
 
 			//Set mode
 			mode = Mode.ADVANCE;
 		}
 
 		/**
 		 * Initiate forward rotation to an absolute angle
 		 *
 		 * @param angle the angle to rotate to (in radians)
 		 * @param speed the speed to advance at
 		*/
 		void turnTo(double angle, int speed) {
 			//Set motor speed
 			left_motor.setSpeed(speed);
 			right_motor.setSpeed(speed);
 
 			//Remember start position
 			initial_position = odometer.getPosition();
 
 			//Convert angle modulo 2*pi (angle E [-2*pi, 2*pi]) and store
 			target_angle = (angle)%(2*Math.PI);
 
 			//Adjust angle so it's in the range [-pi+current_pos, pi+current_pos]
 			//eg within pi of the current positon
 			while(target_angle < (initial_position[2] - Math.PI)) target_angle += 2*Math.PI;
 			while(target_angle > (initial_position[2] + Math.PI)) target_angle -= 2*Math.PI;
 
 			if((target_angle - initial_position[2]) > 0) { //If the realtive angle is positive go counter-clockwise
 				left_motor.backward();
 				right_motor.forward();
 				//And set mode
 				mode = Mode.ROTATE_CCW;
 			} else { //If the realtive angle is negative go clockwise
 				left_motor.forward();
 				right_motor.backward();
 				//And set mode
 				mode = Mode.ROTATE_CW;
 			}
 		}
 
 		/**
 		 * Stop any ongoing movements
 		 *
 		*/
 		void stop() {
 			mode = Mode.INACTIVE; //Disable current movement
 			//Stop motors
 			left_motor.stop();
 			right_motor.stop();
 		}
 
 		/**
 		 * Returns the currect status of the MovementDaemon
 		 *
 		 * @return true if the MovementDaemon is active, false otherwise
 		*/
 		boolean isActive() { //Return status
 			if(mode == Mode.INACTIVE) return false;
 			else return true;
 		}
 	}
 
}
