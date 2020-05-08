 package edu.kit.curiosity.behaviors;
 
 import lejos.nxt.LCD;
 import lejos.nxt.LightSensor;
 import lejos.robotics.navigation.DifferentialPilot;
 import lejos.robotics.subsumption.Behavior;
 import edu.kit.curiosity.RobotState;
 import edu.kit.curiosity.Settings;
 
 public class ReadCodes implements Behavior {
 	private boolean suppressed;
 	private boolean reading;
 	private boolean counted;
 	
 	private DifferentialPilot pilot;
 	private LightSensor light;
 	
 	private int numOfTapes;
 	
 	public ReadCodes() {
 		numOfTapes = 0;
 		counted = false;
 		suppressed = false;
 		reading = false;
 		Settings.readState = true;
 		pilot = Settings.PILOT;
 		light = Settings.LIGHT;
 	}
 	
 	@Override
 	public boolean takeControl() {
 		return Settings.readState && (light.getLightValue() > 30);
 	}
 
 	@Override
 	public void action() {
 		System.out.println("READ");
 		suppressed = false;
 		//readState = false;
 		reading = false;
 
 		counted = false;
 		numOfTapes = 0;
 		
 		pilot.forward();
 		while(!suppressed) {
 			if(!suppressed && !counted 
 					&& light.getLightValue() > 50) {
 				numOfTapes++;
 				counted = true;
 				reading = true;
				pilot.forward();
				//pilot.travel(1, true);
 			} else if(!suppressed && counted
 					&& light.getLightValue() < 40) {
				pilot.forward();	
//				pilot.travel(1, true);
 				counted = false;
 			} 
 			if(reading && pilot.getMovementIncrement() > 10) {
 				LCD.clear();
 				// set States..
 				System.out.println("CodeNumber: " + numOfTapes);
 				// self-suppress (finished job)
 				Settings.readState = false;
 				suppressed = true;
 			}
 		}
 		
 		pilot.stop();
 		
 		switch(numOfTapes) {
 			case 13:
 				Settings.arbiMgr.changeState(RobotState.RACE);
 				break;
 			case 5:
 				Settings.arbiMgr.changeState(RobotState.BRIDGE);
 				break;
 			case 7:
 				Settings.arbiMgr.changeState(RobotState.MAZE);
 				break;
 			case 4:
 				Settings.arbiMgr.changeState(RobotState.SWAMP);
 				break;
 			case 3:
 				Settings.arbiMgr.changeState(RobotState.BT_GATE);
 				break;
 			case 11:
 				Settings.arbiMgr.changeState(RobotState.TURNTABLE);
 				break;
 			case 12:
 				Settings.arbiMgr.changeState(RobotState.SLIDER);
 				break;
 			case 10:
 				Settings.arbiMgr.changeState(RobotState.SEESAW);
 				break;
 			case 6:
 				Settings.arbiMgr.changeState(RobotState.SUSPENSION_BRIDGE);
 				break;
 			case 9:
 				Settings.arbiMgr.changeState(RobotState.LINE_OBSTACLE);
 				break;
 			case 8:
 				Settings.arbiMgr.changeState(RobotState.COLOR_GATE);
 				break;
 			case 14:
 				Settings.arbiMgr.changeState(RobotState.END_OPPONENT);
 				break;
 			default:
 				System.out.println("No codes read!");
 				break;
 		}
 	}
 
 	@Override
 	public void suppress() {
 		suppressed = true;
 	}
 
 }
