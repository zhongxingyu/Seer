 /*----------------------------------------------------------------------------*/
 /* Copyright (c) FIRST 2008. All Rights Reserved.                             */
 /* Open Source Software - may be modified and shared by FRC teams. The code   */
 /* must be accompanied by the FIRST BSD license file in the root directory of */
 /* the project.                                                               */
 /*----------------------------------------------------------------------------*/
 
 package edu.wpi.first.wpilibj.templates;
 
 import edu.wpi.first.wpilibj.*;
 import edu.wpi.first.wpilibj.DriverStationLCD.Line;
 import edu.wpi.first.wpilibj.livewindow.LiveWindow;
 import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
 //import edu.wpi.first.wpilibj.RobotDrive;
 //import edu.wpi.first.wpilibj.SimpleRobot;
 //import edu.wpi.first.wpilibj.templates.Shooter;
 /**
  * The VM is configured to automatically run this class, and to call the
  * functions corresponding to each mode, as described in the SimpleRobot
  * documentation. If you change the name of this class or the package after
  * creating this project, you must also update the manifest file in the resource
  * directory. 
  */
 public class Team3373 extends SimpleRobot{
     /**
      * This function is called once each time the robot enters autonomous mode.
      */
    //Servo frontCameraServo = new Servo(6);//camera class?
    
    
    
    // what is this used for?
    
    DriverStationLCD LCD = DriverStationLCD.getInstance();
    //SmartDashboard smartDashboard;
    SuperJoystick driveStick = new SuperJoystick(1); 
    SuperJoystick shooterController = new SuperJoystick(2);
    Shooter objShooter = new Shooter();
    //Deadband objDeadband = new Deadband();
    Timer robotTimer = new Timer();
    PickupArm arm = new PickupArm();
    Elevator elevator = new Elevator();
    //Camera camera = new Camera();
    DigitalInput frontBackSwitch = new DigitalInput(12);
    DigitalInput leftRightSwitch = new DigitalInput(13);
   CameraControl cameraControl = new CameraControl();
    double rotateLimitMaximum = 4.8;//are these used?
    double rotateLimitMinimum = 0.2;//are these used?
    Drive drive = new Drive();
 
    boolean test;
    boolean solenidFlag=false;
    
   /*********************************
    * Math/Shooter Action Variables *
    *********************************/
    
    TableLookUp objTableLookUp = new TableLookUp();
    
    double ShooterSpeedStage2 = 0;//was StageTwoTalon.get()
    double percentageScaler = 0.75;
    double ShooterSpeedStage1 = ShooterSpeedStage2 * percentageScaler;//was StageOneTalon.get()
    
    double ShooterSpeedMax = 5300.0;
    double ShooterSpeedAccel = 250;
    double stageOneScaler = .5; //What stage one is multiplied by in order to make it a pecentage of stage 2
    double PWMMax = 1; //maximum voltage sent to motor
    double MaxScaler = PWMMax/5300;
    double ShooterSpeedScale = MaxScaler * ShooterSpeedMax; //Scaler for voltage to RPM. Highly experimental!!
    double target;
    double RPMModifier = 250;
    double idle = 1 * ShooterSpeedScale;
    double off = 0;
    double change;
    
    double startTime = 9000000;
    double backTime = 90000000;
    double aTime = 900000000;
    double bTime = 900000000;
    double targetRotatePosition;
    boolean manualToggle;
    double manualStatus;
    boolean armTestFlag;
    boolean canShoot;
    int LX = 1;
    int LY = 2;
    int Triggers = 3;
    int RX = 4;
    int RY = 5;
    int DP = 6;
    double rotateTest = 2.7;
    double autonomousSpeedTarget = 1;
    boolean goToFlag = true;
    
    //public Team3373(){
       // camera.robotInit();
     //}
     
     public void autonomous() {
         if (isAutonomous() && isEnabled()){
                     if (leftRightSwitch.get()) //TODO: Finish
                     while (goToFlag){
                         elevator.goToAngle(4);
                         if (Math.abs(4-elevator.currentAngle) <= .1) {
                             goToFlag = false;
                         }
                     }
                     objShooter.goToSpeed(autonomousSpeedTarget);
                     try {
                         Thread.sleep(500L);
                     } catch (InterruptedException ex) {
                         ex.printStackTrace();
                     }
                     for (int i = 0; i <= 2; i++){
                         objShooter.shoot();
                         try {
                             Thread.sleep(700L);
                         } catch (InterruptedException ex) {
                             ex.printStackTrace();
                         }
                         objShooter.loadFrisbee();
                          try {
                             Thread.sleep(700);
                         } catch (InterruptedException ex) {
                             ex.printStackTrace();
                         }
                     }
                     drive.drive(-1, 0, 0);
                     try {
                         Thread.sleep(5000L);
                     } catch (InterruptedException ex) {
                         ex.printStackTrace();
                     }
             }
 
         }
     
 
     /**
      * This function is called once each time the robot enters operator control.
      */
     public void disabled(){
         while (isDisabled()){
             manualToggle = false;
             armTestFlag = false;
             arm.demoOnFlag = false;
             targetRotatePosition = arm.pot1.getVoltage(); 
             arm.demoStatus = 0;
             elevator.elevatorTarget = elevator.angleMeter.getVoltage();
 
         }
     }
     public void operatorControl() {
         robotTimer.start();
         ;
         while (isOperatorControl() & isDisabled()){ 
             manualToggle = false;
             armTestFlag = false;
             arm.demoOnFlag = false;
             targetRotatePosition = arm.pot1.getVoltage(); 
             arm.demoStatus = 0;
             elevator.elevatorTarget = elevator.angleMeter.getVoltage();
         }
    while (isOperatorControl() & isEnabled()){
    if (!armTestFlag){
    //objTableLookUp.test();
    /****************
    **Shooter Code***
    ****************/
    //Resets the internal toggle flags when a previously pushed button has been released
        driveStick.clearButtons();
        shooterController.clearButtons();
        
        if(driveStick.isStartPushed()){
            objShooter.shoot();
           //LCD.println(Line.kUser5, 1, "Inside");
        }
        if(driveStick.isBackPushed()){
            objShooter.loadFrisbee();
        }
        
       cameraControl.move(shooterController.getRawAxis(LY));
 
        //LCD.println(Line.kUser2, 1, "running");
        
         //test = objShooter.shootLimit.get();
         //System.out.println(test);
        /*if(shooterController.isStartPushed()){
            LCD.println(Line.kUser5, 1, "Inside");//TODO
            //camera.imageAnalysis();
            System.out.println("Inside");
            //objShooter.start();
            //arm.armUp();
        }*/
        /**********************
         * Shooter Algorithms *
         **********************/
 ;
        if(shooterController.isAPushed()){
             objShooter.increaseSpeed();
        }
        if(shooterController.isBPushed()){
            objShooter.decreaseSpeed();
        }
        if(shooterController.isXPushed()){
            objShooter.increasePercentage();
        }
        if(shooterController.isYPushed()){
            objShooter.decreasePercentage();
        }
        if(shooterController.isBackPushed()){
            objShooter.stop();
        }
        if (shooterController.isStartPushed()){
            objShooter.start();
 
        }
        /*if(shooterController.isLStickPushed() && !armTestFlag){
            LCD.println(Line.kUser5, 1, "Inside");
            //camera.imageAnalysis();    TODO: Is this needed?
            System.out.println("Inside");
        }*/
        //arm.rotate(targetRotatePosition);
        //objShooter.printLCD(LCD);
        //Arm.rotate(targetPosition);
        //objShooter.elevator();
        //Arm.grabFrisbee();
        //Arm.armUp();
        //Arm.armDown();
        //Arm.goToPosition(2.5);
        /*
        //try {Thread.sleep(1000);} catch(Exception e){}
        /*****************
         * Elevator Code *
         *****************/
       /* if(driveStick.isLBHeld()){
            elevator.lower();
        } else if (driveStick.isRBHeld()){
            elevator.raise();
        } else {
            elevator.off();
        }
        if (driveStick.isXPushed() && !driveStick.isYPushed()){
            elevator.pwmModifier -= .05;
        } else if (driveStick.isYPushed() && !driveStick.isXPushed()) {
            elevator.pwmModifier += .05;
        }
        LCD.println(Line.kUser5, 1, "Motor Modifier: " + elevator.pwmModifier);*/
        elevator.automaticElevatorTarget(shooterController.isLBPushed(), shooterController.isRBPushed());
        LCD.println(Line.kUser1, 1, "ElevatorTarget: " + elevator.elevatorTarget);
        LCD.println(Line.kUser2, 1, "Elevation (Volt)" + elevator.angleMeter.getVoltage());
        elevator.goToAngle(elevator.elevatorTarget);
        /*******************
         * Servo Test Code *
         ******************/
         /**************
          * Drive Code *
          **************/
          drive.setSpeed(driveStick.isAHeld(), driveStick.isBHeld());
          drive.drive(driveStick.getRawAxis(LX), driveStick.getRawAxis(LY), driveStick.getRawAxis(RX));
         /******************
          * Demo/Test Code *
          ******************/
        /*if (shooterController.isStartPushed()){
          arm.demoStatus = 0;
          arm.demoOnFlag = true;
          arm.armDemo();
        }*/
        /* if (shooterController.isStartPushed()){
            arm.rotate(2.7);
        }
        */
        if (!armTestFlag){
         if (shooterController.isStartPushed()){
             arm.demoStatus = 0;
             arm.demoOnFlag = true;
             arm.armDemo();
         }
        }
        SmartDashboard.putBoolean("ArmUp Bool:", arm.upDownFlag);
        SmartDashboard.putNumber("CurrentPosition :", arm.currentPosition);
        //arm.rotate(rotateTest);
        
 
        //if  (!armTestFlag) {
        //arm.rotateTalon.set(shooterController.getRawAxis(LX));
        //}
    }
         if (shooterController.isAHeld() && shooterController.isXHeld() && shooterController.isYHeld() && !armTestFlag){ //allows the test mode for the arm assembly to start
             armTestFlag = true;
         } else if (shooterController.isAHeld() && shooterController.isXHeld() && shooterController.isYHeld() && armTestFlag){ //turns the test mode for arm off
             armTestFlag = false;
         }
         if (armTestFlag){
 
             
             if (shooterController.isLStickPushed() && manualToggle){
                 manualToggle = false;
             } else if (shooterController.isRStickPushed() && !manualToggle){
                 manualToggle = true;
             }
             //switch (manualStatus){ //controls whether automatic or manual control
                 //case 0: //manual rotation control
               if (manualToggle){      
                     if (shooterController.isRBHeld() && !shooterController.isLBHeld()){
                         arm.rotateTalon.set(.5);
                     } else if (shooterController.isLBHeld() && !shooterController.isRBHeld()){
                         arm.rotateTalon.set(-.5);
                     } else{
                         arm.rotateTalon.set(0);
                     }
                     if (shooterController.isStartPushed()){
                         manualStatus = 1;
                     }
               } else if (!manualToggle) {
                 //case 1: //automatic targeting
                     if (shooterController.isLBPushed()){
                         targetRotatePosition = 2.7;
                     } 
                     if(shooterController.isRBPushed()){
                         targetRotatePosition = 2.3;
                     }
                     arm.rotate(targetRotatePosition);
                     if (shooterController.isBackPushed()){
                         manualStatus = 0;
                     }
               }
             //}
 
             shooterController.clearButtons();
             if (shooterController.isAPushed()) arm.armUp();
             if (shooterController.isBPushed()) arm.armDown();
             //Arm.grabFrisbee(shooterController.isStartPushed());
 
             shooterController.clearButtons();
             LiveWindow.run();
         }
         //SmartDashboard.putNumber("Target: ", targetRotatePosition);
         //SmartDashboard.putNumber("manualStatus: ", manualStatus);
         SmartDashboard.putBoolean("ManualToggle: ", manualToggle);
         SmartDashboard.putBoolean ("Test Status: ", armTestFlag);
        
         String currentTime = Double.toString(robotTimer.get());
         LCD.println(Line.kUser6, 1, currentTime);
         
         //String potString = Double.toString(pot1.getVoltage());
         //LCD.println(Line.kUser2, 1, potString);
         LCD.updateLCD();
     
         
         }
     }
 }
 
