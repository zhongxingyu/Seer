 /*----------------------------------------------------------------------------*/
 /* Copyright (c) FIRST 2008. All Rights Reserved.                             */
 /* Open Source Software - may be modified and shared by FRC teams. The code   */
 /* must be accompanied by the FIRST BSD license file in the root directory of */
 /* the project.                                                               */
 /*----------------------------------------------------------------------------*/
 package org.first.team342;
 
 import edu.wpi.first.wpilibj.Jaguar;
 import edu.wpi.first.wpilibj.Joystick;
 import edu.wpi.first.wpilibj.RobotDrive;
 import edu.wpi.first.wpilibj.Servo;
 import edu.wpi.first.wpilibj.SimpleRobot;
 import edu.wpi.first.wpilibj.SpeedController;
 import edu.wpi.first.wpilibj.Timer;
 import edu.wpi.first.wpilibj.Victor;
 
 /**
  * The VM is configured to automatically run this class, and to call the
  * functions corresponding to each mode, as described in the SimpleRobot
  * documentation. If you change the name of this class or the package after
  * creating this project, you must also update the manifest file in the resource
  * directory.
  */
 public class Team342Robot extends SimpleRobot {
 
     public static final int DEFAULT_MODULE_SLOT = 4;
     public static final int PWM_CHANNEL_LEFT_FRONT = 1;
     public static final int PWM_CHANNEL_LEFT_REAR = 2;
     public static final int PWM_CHANNEL_RIGHT_FRONT = 3;
     public static final int PWM_CHANNEL_RIGHT_REAR = 4;
     public static final int PWM_CHANNEL_GRIPPER_TOP = 6;
     public static final int PWM_CHANNEL_GRIPPER_BOTTOM = 7;
     public static final int PWM_CHANNEL_ARM_MOTOR = 10;
     public static final int PWM_CHANNEL_MINIBOT_ARM_RELEASE = 8;
     public static final int PWM_CHANNEL_MINIBOT_RELEASE = 9;
     public static final int DIO_CHANNEL_ARM_LIMIT_BOTTOM = 1;
     public static final int DIO_CHANNEL_ARM_LIMIT_TOP = 2;
     public static final int DIO_CHANNEL_LIGHT_SENSOR_LEFT = 3;
     public static final int DIO_CHANNEL_LIGHT_SENSOR_CENTER = 4;
     public static final int DIO_CHANNEL_LIGHT_SENSOR_RIGHT = 5;
     public static final int BUTTON_ROTATE_UP = 5;
     public static final int BUTTON_ROTATE_DOWN = 3;
     public static final int BUTTON_PULL_IN = 2;
     
     private RobotDrive drive;
     private Joystick driveController;
     private Joystick armController;
     private SpeedController leftFront;
     private SpeedController leftRear;
     private SpeedController rightFront;
     private SpeedController rightRear;
     private SpeedController armMotor;
     private SpeedController topGripper;
     private SpeedController bottomGripper;
 
     private Servo releaseArm;
     private Servo releaseBot;
 
     public Team342Robot() {
         super();
 
         this.driveController = new Joystick(1);
         this.armController = new Joystick(2);
 
         this.releaseArm = new Servo(DEFAULT_MODULE_SLOT, PWM_CHANNEL_MINIBOT_ARM_RELEASE);
         this.releaseBot = new Servo(DEFAULT_MODULE_SLOT, PWM_CHANNEL_MINIBOT_RELEASE);
         
         this.leftFront = new Jaguar(DEFAULT_MODULE_SLOT, PWM_CHANNEL_LEFT_FRONT);
         this.leftRear = new Jaguar(DEFAULT_MODULE_SLOT, PWM_CHANNEL_LEFT_REAR);
         this.rightFront = new Jaguar(DEFAULT_MODULE_SLOT, PWM_CHANNEL_RIGHT_FRONT);
         this.rightRear = new Jaguar(DEFAULT_MODULE_SLOT, PWM_CHANNEL_RIGHT_REAR);
 
         this.armMotor = new Victor(DEFAULT_MODULE_SLOT, PWM_CHANNEL_ARM_MOTOR);
         this.topGripper = new Victor(DEFAULT_MODULE_SLOT, PWM_CHANNEL_GRIPPER_TOP);
         this.bottomGripper = new Victor(DEFAULT_MODULE_SLOT, PWM_CHANNEL_GRIPPER_BOTTOM);
 
         this.drive = new RobotDrive(this.leftFront, this.leftRear, this.rightFront, this.rightRear);
         this.drive.setInvertedMotor(RobotDrive.MotorType.kRearRight, true);
         this.drive.setInvertedMotor(RobotDrive.MotorType.kFrontRight, true);
     }
 
     /**
      * This function is called once each time the robot enters autonomous mode.
      */
     public void autonomous() {
     }
 
     /**
      * This function is called once each time the robot enters operator control.
      */
     public void operatorControl() {
         System.out.println("Is Operator Control: " + isOperatorControl());
         System.out.println("Is Enabled: " + isEnabled());
         while (isOperatorControl() && isEnabled()) {
             double x = this.driveController.getX();
             double y = this.driveController.getY();
 
             double rotation = this.driveController.getRawAxis(4);
             double armValue = this.armController.getY() * -1;
             System.out.println("Arm Value: " + armValue);
             System.out.println("X: " + x + ", Y: " + y + ", Z: " + rotation);
 
             this.drive.mecanumDrive_Cartesian(x, y, rotation, 0);
             this.armMotor.set(armValue);
 
             if (this.armController.getRawButton(BUTTON_PULL_IN)) {
                 this.topGripper.set(-0.5);
                 this.bottomGripper.set(-0.5);
             } else if (this.armController.getRawButton(BUTTON_ROTATE_UP)) {
                 this.topGripper.set(-0.5);
                 this.bottomGripper.set(0.5);
             } else if (this.armController.getRawButton(BUTTON_ROTATE_DOWN)) {
                 this.topGripper.set(0.5);
                 this.bottomGripper.set(-0.5);
             } else if (this.armController.getTrigger()) {
                 this.topGripper.set(0.5);
                 this.bottomGripper.set(0.5);
             } else {   
                 this.topGripper.set(0.0);
                 this.bottomGripper.set(0.0);
             }
 
             if (this.driveController.getRawButton(2) && this.driveController.getTrigger()){
                 //release minibot
             }else if(this.driveController.getTrigger()){
                 //release minibot arm

 
             Timer.delay(0.005);
         }
     }
 }
