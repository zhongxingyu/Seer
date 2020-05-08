 package org.dirtymechanics.frc.test;
 
 
 import edu.wpi.first.wpilibj.IterativeRobot;
 import edu.wpi.first.wpilibj.Jaguar;
 import org.dirtymechanics.frc.test.control.ControlScheme;
 import org.dirtymechanics.frc.test.control.SmartJoystick;
 import org.dirtymechanics.frc.test.control.impl.StandardControlScheme;
 import org.dirtymechanics.frc.test.drive.DriveTrain;
 import org.dirtymechanics.frc.test.shooter.Shooter;
 
 public class Robot extends IterativeRobot {
     
     private static final int DRIVE_JAGUAR_LEFT = 1;
     private static final int DRIVE_JAGUAR_RIGHT = 2;
     private static final int SHOOTER_JAGUAR = 3;
     
     private final Jaguar leftJag = new Jaguar(DRIVE_JAGUAR_LEFT);
     private final Jaguar rightJag = new Jaguar(DRIVE_JAGUAR_RIGHT);
     private final Jaguar shooterJag = new Jaguar(SHOOTER_JAGUAR);
     
     private final SmartJoystick leftStick, rightStick, gamepad;
     private final ControlScheme controls;
     private final DriveTrain driveTrain;
     private final Shooter shooter;
     private long autonomousStart;
         
     public Robot() {
         leftStick = new SmartJoystick(1);
         rightStick = new SmartJoystick(2);
         gamepad = new SmartJoystick(3);
         controls = new StandardControlScheme(leftStick, rightStick, gamepad);
         driveTrain = new DriveTrain(leftJag, rightJag, 2, controls);
         shooter = new Shooter(shooterJag, controls);
     }
     
     public void robotInit() {
     }
     
     public void autonomousInit() {
         //autonomousStart = System.currentTimeMillis();
     }
     
     public void autonomousPeriodic() {
         /*long time = System.currentTimeMillis();
         driveTrain.shiftDown();
         if (autonomousStart - time < 3000) {
             driveTrain.drive(0.4D, 0.4D);
         } else {
             driveTrain.drive(0D, 0D);
         }
         
         if (autonomousStart - time > 7000 && (autonomousStart - time) % 2000 < 250) {
             //TODO shoot
         } else {
             //TODO stop shooting
         }*/
     }
     
     public void teleopPeriodic() {
         driveTrain.drive();
         shooter.update();
     }
 }
