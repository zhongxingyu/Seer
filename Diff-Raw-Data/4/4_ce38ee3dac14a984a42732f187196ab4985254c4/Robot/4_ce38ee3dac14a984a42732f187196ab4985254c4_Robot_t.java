 package edu.wpi.first.wpilibj.templates;
 
 
 import edu.wpi.first.wpilibj.AnalogChannel;
 import edu.wpi.first.wpilibj.IterativeRobot;
 import edu.wpi.first.wpilibj.Joystick;
 import edu.wpi.first.wpilibj.RobotDrive;
 import edu.wpi.first.wpilibj.Timer;
 import edu.wpi.first.wpilibj.Victor;
 import edu.wpi.first.wpilibj.command.Command;
 import edu.wpi.first.wpilibj.command.Scheduler;
 //Hello World!
 public class Robot extends IterativeRobot {
     Command OpenClaw;
     RobotDrive chassis;
     Joystick leftStick;
     Joystick rightStick;
     Victor PWM3;
     Victor PWM4;
     AnalogChannel A1;
     public void robotInit() {
         chassis = new RobotDrive(1, 2);
         leftStick = new Joystick(1);
         rightStick = new Joystick(2);
         PWM3 = new Victor(3);
         PWM4 = new Victor(4);
         A1 = new AnalogChannel(1);
     }
 
     public void autonomousPeriodic() {
         chassis.setSafetyEnabled(false);
         new ExampleSimpleCommand().run();
     }
 
     public void testPeriodic() {
         
     }
     
     public void teleopPeriodic() {
 
         chassis.setSafetyEnabled(true);
         while (isOperatorControl() && isEnabled()) {
             chassis.arcadeDrive(leftStick);
             PWM3.set(rightStick.getY());
             PWM4.set(rightStick.getX());
             if (A1.getValue() > 700) {
                 PWM3.set(-0.4);
                 if (rightStick.getX() < -0.4) {
                     PWM3.set(rightStick.getX());
                 }
             } else if(A1.getValue() < 250){
                 PWM3.set(0.4);
                 if (rightStick.getX() > 0.4) {
                     PWM3.set(rightStick.getX());
                 }
            }
             double A12 = (A1.getValue()-475);
             double A13 = (A12/800);
             PWM3.set(PWM3.get()-A13);
             System.out.println(A13 + " " + A1.getValue());
             Timer.delay(0.01);
       }
     }
     
 }
