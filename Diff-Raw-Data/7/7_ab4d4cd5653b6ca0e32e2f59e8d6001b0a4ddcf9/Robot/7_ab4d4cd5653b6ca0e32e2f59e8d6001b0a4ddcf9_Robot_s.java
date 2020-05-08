 package common;
 
 
<<<<<<< HEAD
=======
 import common.color.Color;
 import common.color.ColorOracle.Strength;
 
import lejos.nxt.LightSensor;
>>>>>>> branch 'master' of ssh://git@github.com/mitsch/legomindstorm.git
 import lejos.nxt.Motor;
 import lejos.nxt.NXTRegulatedMotor;
 import lejos.nxt.SensorPort;
 import lejos.nxt.TouchSensor;
 import lejos.nxt.UltrasonicSensor;
 import lejos.robotics.navigation.DifferentialPilot;
 
import common.color.Color;
 
 public class Robot {
 	public TouchSensor leftTouch, rightTouch;
 	public UltrasonicSensor sonar;
 	
 	public ColorSensor color;
 	
 	public NXTRegulatedMotor leftMotor, rightMotor;
 	
 	public SensorArm arm;
 	
 	public DifferentialPilot pilot;
 	
 	public Robot() {
 		leftMotor = Motor.C;
 		rightMotor = Motor.A;
 		
 		arm = (SensorArm) Motor.B;
 		
 		leftTouch = new TouchSensor(SensorPort.S4);
 		rightTouch = new TouchSensor(SensorPort.S1);
 		sonar = new UltrasonicSensor(SensorPort.S3);
 		
 		// This is our new ColorSensor! Its used insted of the LightSensor.
 		color = new ColorSensor(SensorPort.S2);
 		color.setStrength(Strength.WEAK);
 		
 		////  pilot = new DifferentialPilot(3.3f, 21.0f, leftMotor, rightMotor);
 		pilot = new DifferentialPilot(3.65f , 26.f, leftMotor, rightMotor);
 		
 		arm.calibrate(-90,+90);
 		
 	}	
 	
 	public boolean isLineBeneath() {
 		return color.getColor(Color.BLACK, Color.SILVER) == Color.SILVER;
 	}
 }
