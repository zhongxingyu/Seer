 package com.BCHS;
 
 import edu.wpi.first.wpilibj.*;
 import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
 
 public class Bot extends IterativeRobot
 {
 	Camera cam;
 	Hockey hockeySticks;
 	BCHSKinect kinect;
 	Launcher launcher;
 	Retrieval retrieval;
 	Chasis chasis;
 	
 	DriverStation ds = DriverStation.getInstance();
 	DriverStationLCD dsLCD = DriverStationLCD.getInstance();
 	
 	Joystick driveJoystick, secondaryJoystick;
 	
 	
 	boolean test = true;
 	boolean autoOnce = true;
 	
 	public void robotInit()
 	{
 		printData();
 		//Control Inputs
 		driveJoystick = new Joystick(Config.MAIN_JOYSTICK);
 		secondaryJoystick = new Joystick(Config.SECONDARY_JOYSTICK);
 		
 		//Functional Mechanisms
 		launcher = new Launcher(Config.SENCODER[0], Config.SENCODER[1], Config.SHOOTER[0], Config.SHOOTER[1]);
 		retrieval = new Retrieval(Config.RETRIEVE);
 		hockeySticks = new Hockey(Config.HOCKEY, Config.TLIMIT_SWITCH, Config.BLIMIT_SWITCH);
 		cam = new Camera();
 		
 		//Drive Systems
 		chasis = new Chasis(Config.LENCODER[0], Config.LENCODER[1], Config.RENCODER[0], Config.RENCODER[1], Config.ULTRASONIC, Config.LDRIVE, Config.RDRIVE);
 		kinect = new BCHSKinect(chasis.leftSide, chasis.rightSide, launcher, retrieval, hockeySticks);
 		
 		dsLCD.println(DriverStationLCD.Line.kUser6, 1, "V1.2.3");
 		dsLCD.updateLCD();
 	}
 	
 	public void disabledPeriodic()
 	{
 		test = true;
 		autoOnce = true;
 		chasis.stop();
                
 	}
 	
 	public void autonomousInit()
 	{
 		
 	}
 
 	public void autonomousPeriodic()
 	{
 		printData();
 		if (ds.getDigitalIn(1))
 		{
 			if (autoOnce) 
 			{	
 				chasis.enable();
 				chasis.setSetpoint(12.0);
 			}
 		}
 		else if (ds.getDigitalIn(2))
 		{
 			hockeySticks.set(0.75);
 			launcher.set(0.50);
 			Timer.delay(3.0);
 			retrieval.set(0.5);
 			Timer.delay(5.0);
 			retrieval.stop();
 		}
 		else if (ds.getDigitalIn(3))
 		{
 			kinect.kinectDrive();
 		}
 		else if (ds.getDigitalIn(4) && autoOnce)
 		{
 			chasis.set(0.75);
 			hockeySticks.set(1.0);
 			Timer.delay(1.0);
 			chasis.stop();
 			Timer.delay(1.0);
 			launcher.set(0.45);
 			Timer.delay(1.5);
 			retrieval.set(0.8);
 			Timer.delay(0.625);         //lines 86-90 editted to slow down launcher after first ball//
                         retrieval.set(0.0);
                         launcher.set(0.35);
                         Timer.delay(0.625);
                         retrieval.set(0.8);
 			hockeySticks.stop();
 			Timer.delay(10.0);
 			launcher.stop();
 			retrieval.stop();
 			autoOnce = false;
 		}
 		else if (ds.getDigitalIn(5) && autoOnce)
 		{
 			launcher.set(0.75);
 			Timer.delay(1.0);
 			retrieval.set(0.8);
 			Timer.delay(5.0);
 			launcher.stop();
 			retrieval.stop();
 			autoOnce = false;
 		}
 	}
 	
 	public void teleopInit()
 	{
 		chasis.stop();
 	}
 
 	public void teleopPeriodic()
 	{
 	printData();
 		if (Config.TESTING && test)
 		{
 			
 		} 
 		else 
 		{
 			//dsLCD.println(DriverStationLCD.Line.kMain6, 1, ""+MathUtils.round(-launcher.encoder.getRate()));
 			
 			launcher.lightsOn();
 			
 			double x = driveJoystick.getX();
 			double y = driveJoystick.getY();
 			
 			x = Lib.signSquare(x);
 			y = Lib.signSquare(y);
 			
 			chasis.leftSide.set(Lib.limitOutput(y - x));
 			chasis.rightSide.set(-Lib.limitOutput(y + x));
 			
                         if (driveJoystick.getRawButton(11))
 				hockeySticks.set(-1.0);
 			else if (driveJoystick.getRawButton(10))
 				hockeySticks.set(1.0);
 			else
 				hockeySticks.set(0);
                         
 			if (secondaryJoystick.getRawButton(11))
 				hockeySticks.set(-1.0);
 			else if (secondaryJoystick.getRawButton(10))
 				hockeySticks.set(1.0);
 			else
 				hockeySticks.set(0);
 
 			if (secondaryJoystick.getTrigger())
 				launcher.set(1.0);
 			else if (secondaryJoystick.getRawButton(2))
 				launcher.set(0.75);
 			else if (secondaryJoystick.getRawButton(3))
 				launcher.set(0.5);
 			else if (secondaryJoystick.getRawButton(4))
 				launcher.set(0.25);
 			else
 				launcher.set(0.0);
 
 			if (secondaryJoystick.getRawButton(6))
 				retrieval.set(0.75);
 			else if (secondaryJoystick.getRawButton(7))
 				retrieval.set(-0.75);
 			else
 				retrieval.set(0.0);
 			
 			dsLCD.updateLCD();
 		}
 	}
 	public void printData()
 	{		
 		SmartDashboard.putDouble("leftside",chasis.leftEncoder.getRate());
 		SmartDashboard.putDouble("rightside",chasis.rightEncoder.getRate());
 		
 		SmartDashboard.putDouble("leftsideD",chasis.leftEncoder.getDistance());
 		SmartDashboard.putDouble("rightsideD",chasis.rightEncoder.getDistance());
 	}
 }
