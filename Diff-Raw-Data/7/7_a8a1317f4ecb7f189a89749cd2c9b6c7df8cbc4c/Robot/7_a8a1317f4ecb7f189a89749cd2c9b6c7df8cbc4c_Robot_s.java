 package org.saseros.cleanstorms;
 
 import lejos.nxt.Button;
 import lejos.nxt.Motor;
 import lejos.nxt.SensorPort;
 import lejos.robotics.navigation.DifferentialPilot;
 import lejos.robotics.navigation.Navigator;
 import lejos.robotics.objectdetection.Feature;
 import lejos.robotics.objectdetection.FeatureDetector;
 import lejos.robotics.objectdetection.FeatureListener;
 import lejos.robotics.objectdetection.RangeFeatureDetector;
 
 import org.saseros.cleanstorms.Sensor;
 
 public class Robot implements FeatureListener {
 
 	private Sensor sensor;
 	private DifferentialPilot pilot;
 	private Navigator navigator;
 	private RangeFeatureDetector fd;
 
 	private int turn = -15;
 	private int recursiveDepth = 0;
 	
	private final int responseTimeUltrasonicSensor = 500;
 
 	/**
 	 * The constructor sets the sensors used by this class for
 	 * movement-handling.
 	 * 
 	 * @param sensor
 	 *            An object of the class Sensor must be put in to initiate a
 	 *            Robot-object
 	 */
 	public Robot(Sensor sensor, DifferentialPilot pilot, Navigator navigator) {
 		this.sensor = sensor;
 		this.pilot = pilot;
 		this.navigator = navigator;
 		this.fd = new RangeFeatureDetector(sensor.getUltrasonicSensor(),
				sensor.getReactonDistanceHori(), responseTimeUltrasonicSensor);
 		this.fd.addListener(this);
 		
 		pilot.setMinRadius(15); // Radius for turns
 		
 		
 	}
 
 	/**
 	 * A recursive method that checks for obstacles numerous times, moves the
 	 * Ultrasonic Sensor around and checks again. If a obstacle is detected it
 	 * locks on to that obstacle until it moves, or recursive depth reaches
 	 * maximum value(246), if no object is detected it will return true.
 	 * 
 	 * @return Returns a boolean-value, false, to indicate that the path is not
 	 *         clear, or true, to indicate the path is ready to go.
 	 */
 	public boolean isPathClear() {
 		recursiveDepth++;
 		if (recursiveDepth >= 246) {
 			atEndOfPathIsDetected();
 			System.out.println("Path not clear");
 			return false;
 		}
 		if (sensor.isObstacleDetected()) {
 			return isPathClear();
 		}
 		Motor.B.rotate(turn);
 		System.out.println("Surveying");
 		if (Motor.B.getTachoCount() <= -45) {
 			turn = +15;
 			return isPathClear();
 		} else if (Motor.B.getTachoCount() >= 45) {
 			System.out.println("Done Surveying!");
 			atEndOfPathIsDetected();
 			return true;
 		} else {
 			return isPathClear();
 		}
 	}
 
 	/**
 	 * Help-method for the method isPathClear(), sets every modified value back
 	 * to its default.
 	 */
 	private void atEndOfPathIsDetected() {
 		moveUltrasonicSensorToDefaultPosition();
 		recursiveDepth = 0;
 		turn = -15;
 	}
 
 	/**
 	 * Moves the motor-arm back to default position(0), which is the position
 	 * the arm was located at software boot-up. This position must be set
 	 * manually before boot-up.
 	 */
 	private void moveUltrasonicSensorToDefaultPosition() {
 		Motor.B.rotateTo(0);
 	}
 
 	/**
 	 * @return the pilot
 	 */
 	public DifferentialPilot getPilot() {
 		return pilot;
 	}
 
 	/**
 	 * @return the navigator
 	 */
 	public Navigator getNavigator() {
 		return navigator;
 	}
 
 	/**
 	 * @return the sensor
 	 */
 	public Sensor getSensor() {
 		return sensor;
 	}
 
 	@Override
 	public void featureDetected(Feature feature, FeatureDetector detector) {
 		
 	}
 	
 	
 
 	/**
 	 * For testing purposes only.
 	 * 
 	 * @param args
 	 */
 	// public static void main(String[] args) {
 	// Sensor sensor = new Sensor(true, 20, 30);
 	// Robot robot = new Robot(sensor);
 	// System.out.println("Method returns: " + robot.isPathClear());
 	// // robot.isPathClear();
 	// Button.waitForAnyPress();
 	// // while(!Button.ESCAPE.isDown()){
 	//
 	// // }
 	//
 	// }
 
 }
