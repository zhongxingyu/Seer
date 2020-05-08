 package edu.kit.curiosity.behaviors.tape;
 
 import edu.kit.curiosity.Settings;
 import lejos.nxt.LightSensor;
 import lejos.nxt.TouchSensor;
 import lejos.nxt.UltrasonicSensor;
 import lejos.robotics.navigation.DifferentialPilot;
 import lejos.robotics.subsumption.Behavior;
 
 public class ObstacleFound implements Behavior {
 
 	private boolean suppressed = false;
 	TouchSensor touch_l = Settings.TOUCH_L;
 	TouchSensor touch_r = Settings.TOUCH_R;
 	DifferentialPilot pilot = Settings.PILOT;
 	UltrasonicSensor sensor = Settings.SONIC;
 	LightSensor light = Settings.LIGHT;
 
 	private final int distanceToWall = 12;
 
 	@Override
 	public boolean takeControl() {
 		return (Settings.obstacle || touch_l.isPressed() || touch_r.isPressed());
 	}
 
 	@Override
 	public void action() {
 		suppressed = false;
 		if (!Settings.obstacle) { //If not in obstacle mode - initialize obstacle mode
 			Settings.obstacle = true;
 			Settings.motorAAngle = 0;
			pilot.travel(-10);
			pilot.rotate(100);
 		}
 		while (!suppressed && light.getLightValue() < 50) { //arcs until line found
 			if (!pilot.isMoving() && sensor.getDistance() > (distanceToWall + 10)) {
 				pilot.arc(-15, -90, true);
 			} else if (!pilot.isMoving() && sensor.getDistance() < distanceToWall) {
 				pilot.arc(60, 20, true);
 			} else if (!pilot.isMoving() && sensor.getDistance() >= distanceToWall) {
 				pilot.arc(-60, -20, true);
 			}
 		}
 		if (light.getLightValue() > 50) { //if line found - leave obstacle mode
 			Settings.obstacle = false;
 			pilot.travel(-5);
 			Settings.motorAAngle = 90;
 		}
 		pilot.stop();
 	}
 
 	@Override
 	public void suppress() {
 		suppressed = true;
 	}
 
 }
