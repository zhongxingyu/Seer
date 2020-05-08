 package edu.wpi.first.wpilibj.templates;
 
 import edu.wpi.first.wpilibj.Joystick;
 import edu.wpi.first.wpilibj.RobotDrive;
 import edu.wpi.first.wpilibj.SimpleRobot;
 import edu.wpi.first.wpilibj.Timer;
 import edu.wpi.first.wpilibj.Victor;
 
 
 public class RobotTemplate extends SimpleRobot {
     /**
      * This function is called once each time the robot enters autonomous mode.
      */
     public RobotDrive drivetrain;
     public Joystick leftStick;
     public Joystick rightStick;
     public Victor gearMotor;
     
 
 
     public RobotTemplate() {
         //Instantialize objects for RobotTemplate
         getWatchdog().setEnabled(false);
         leftStick = new Joystick(1);
         rightStick  = new Joystick(2);
         
         gearMotor = new Victor(3); //initialize speed controller
         
         //2-Wheel tank drive
         drivetrain = new RobotDrive(1,2);
         
         //4-Wheel tank drive
         //Motors must be set in the following order:
         //LeftFront=1; LeftRear=2; RightFront=3; RightRear=4;
        /*
         drivetrain = new RobotDrive(1,2,3,4);
        drivetrain.tankDrive(leftStick, rightStick);
        */
     }
 
 
     public void autonomous() {
         for (int i = 0; i < 4; i++){
             drivetrain.drive(0.5, 0.0); // drive 50% fwd 0% turn
             Timer.delay(2.0);    // wait 2 seconds
             drivetrain.drive(0.0, 0.75); // drive 0% fwd, 75% turn
         }
         drivetrain.drive(0.0, 0.0);   // drive 0% forward, 0% turn
     }
 
 
     public void operatorControl() {
          drivetrain.setSafetyEnabled(true);
          while(isOperatorControl() && isEnabled() ){
              drivetrain.tankDrive(leftStick, rightStick);
              Timer.delay(0.01);
              if(rightStick.getTrigger()){
                  gearMotor.set(.5);//if right stick trigger is pressed, set motor to 50% speed
              }
              else{
                  gearMotor.set(0);
              }
         }
     }
 }
 
