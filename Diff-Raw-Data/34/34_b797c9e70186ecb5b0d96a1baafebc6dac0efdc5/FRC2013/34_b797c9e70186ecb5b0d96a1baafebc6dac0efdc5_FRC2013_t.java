 /*----------------------------------------------------------------------------*/
 /* Copyright (c) FIRST 2008. All Rights Reserved.                             */
 /* Open Source Software - may be modified and shared by FRC teams. The code   */
 /* must be accompanied by the FIRST BSD license file in the root directory of */
 /* the project.                                                               */
 /*----------------------------------------------------------------------------*/
 
 package edu.wpi.first.wpilibj.templates;
 
 
 import edu.wpi.first.wpilibj.IterativeRobot;
 
 /**
  * The VM is configured to automatically run this class, and to call the
  * functions corresponding to each mode, as described in the IterativeRobot
  * documentation. If you change the name of this class or the package after
  * creating this project, you must also update the manifest file in the resource
  * directory.
  */
 public class FRC2013 extends IterativeRobot {
     /**
      * This function is run when the robot is first started up and should be
      * used for any initialization code.
      */
     
     public static final int k_AUTON_DELAY = 0;
     public static final int k_AUTON_AIMING = 1;
     public static final int k_AUTON_FIRE = 2;
     public static final int k_AUTON_WAIT = 3;
     public static final int k_AUTON_MOVING = 4;
     public static final int k_AUTON_DONE = 5;
     public static final int k_AIM_LEFT = 0;
     public static final int k_AIM_RIGHT = 1;
     public static final int k_AIM_TOP = 2;
     public static int iAutonState;
     public static int iAimState;
     public static int iNumDiscs;
     public static double dRightX;
     public static double dLeftX;
     public static double dRightY;
     public static double dLeftY;
     public static boolean dSlowRight;
     public static boolean dSlowLeft;
     public static boolean dAimRight;
     public static boolean dAimLeft;
     public static boolean dNextTarget;
     public static boolean dPrevTarget;
     public static boolean dCamLightOn;
     public static boolean dCamLightOff;
     public static boolean bLastState;
     public static double dTimeWait;
     public static double dAutonPowerLimitLower;
     public static double dAutonPowerLimitUpper;
     
     public void robotInit() {
         Input.initJoystick();
         Input.initGyro();
         Output.initMotors();
         Output.initModule();
         Output.initScreen();
        Input.initVision(FRCFile.bEnableCamera);
         FRCTimer.initTimer();
         FRCFile.initConfig();
         
         // Initialize all variables based on config file
         FRC2013.dAutonPowerLimitLower = FRCFile.dAutonPowerLimitLower;
         FRC2013.dAutonPowerLimitUpper = FRCFile.dAutonPowerLimitUpper;
         Think.dUpperDistanceLimit = FRCFile.dUpperDistanceLimit;
         Think.dLowerDistanceLimit = FRCFile.dLowerDistanceLimit;
         Think.dLoadForwardPower = FRCFile.dLoadForwardPower;
         Think.dLoadReversePower = FRCFile.dLoadReversePower;
         Think.dUpperAngleLimit = FRCFile.dUpperAngleLimit;
         Think.dLowerAngleLimit = FRCFile.dLowerAngleLimit;
         Think.dStopPower = FRCFile.dStopPower;
         Think.dUpperPowerStopLimit = FRCFile.dUpperPowerStopLimit;
         Think.dLowerPowerStopLimit = FRCFile.dLowerPowerStopLimit;
         Think.dMaxMotorValLeft = FRCFile.dMaxMotorValLeft;
         Think.dMaxMotorValRight = FRCFile.dMaxMotorValRight;
         Think.dSlowSpeedLeft = FRCFile.dSlowSpeedLeft;
         Think.dSlowSpeedRight = FRCFile.dSlowSpeedRight;
         Output.dClimbPower = FRCFile.dClimbPower;
         Output.iRightDriveMotorSlot = FRCFile.iRightDriveMotorSlot;
         Output.iLeftDriveMotorSlot = FRCFile.iLeftDriveMotorSlot;
         Output.iShooterMotorSlot = FRCFile.iShooterMotorSlot;
         Output.iClimbMotor1Slot = FRCFile.iClimbMotor1Slot;
         Output.iClimbMotor2Slot = FRCFile.iClimbMotor2Slot;
         Output.iKickerMotorSlot = FRCFile.iKickerMotorSlot;
         Output.iHookMotorSlot = FRCFile.iHookerMotorSlot;
         Output.iDropSlot1 = FRCFile.iDropSlot1;
         Output.iDropSlot2 = FRCFile.iDropSlot2;
         Output.iDropSlot3 = FRCFile.iDropSlot3;
         Input.iDriverPortRight = FRCFile.iDriverPortRight;
         Input.iDriverPortLeft = FRCFile.iDriverPortLeft;
         Input.iDriverPortCo = FRCFile.iDriverPortCo;
     }
 
     public void disableInit(){
         bLastState = false;
         dTimeWait = 0.0;
     }
     public void autonInit(){
         iAutonState = k_AUTON_DELAY;
         dRightX = 0;
         dLeftX = 0;
         dRightY = 0;
         dLeftY = 0;
         dSlowRight = false;
         dSlowLeft = false;
         dAimRight = false;
         dAimLeft = false;
         dNextTarget = false;
         dPrevTarget = false;
         dCamLightOn = false;
         dCamLightOff = true;
     }
     
     /**
      * This function is called periodically during autonomous
      */
     public void autonomousPeriodic() {
         double[] temp;
         switch(iAutonState){
             
             /**
              * Delays the start of robot's autonomous sequence if necessary
              */
             case k_AUTON_DELAY:
                 if(FRCTimer.DelayDone(dTimeWait)){
                     iAutonState++;
                 }
                 break;
                 
             /**
              * Finds all targets to shoot for
              */    
             case k_AUTON_AIMING:
                 Input.bAim = true;
                 switch(iAimState){                    
                     case k_AIM_LEFT:
                         Think.currentTarget = 1;
                         //temp = Think.aimAdjust(Output.rightDriveMotor.get(), Output.leftDriveMotor.get());
                         break;
                     case k_AIM_RIGHT:
                         Think.currentTarget = 2;
                         //temp = Think.aimAdjust(Output.rightDriveMotor.get(), Output.leftDriveMotor.get());
                         break;
                     case k_AIM_TOP:
                         Think.currentTarget = 0;
                         //temp = Think.aimAdjust(Output.rightDriveMotor.get(), Output.leftDriveMotor.get());
                         break;
                     default:
                         Think.currentTarget = 0;
                         //temp = Think.aimAdjust(Output.rightDriveMotor.get(), Output.leftDriveMotor.get());
                         break;
                 }
                 //Think.newJoystickLeft = temp[0];
                 //Think.newJoystickRight = temp[1];
                 if(Output.rightDriveMotor.get() >= dAutonPowerLimitLower && Output.rightDriveMotor.get() <= dAutonPowerLimitUpper ){
                     //Think.newJoystickLeft = 0;
                     //Think.newJoystickRight = 0;
                     iAutonState++;
                 }
                 break;
                 
             /**
              * Shooter waits 1 second and then fires
              */
             case k_AUTON_FIRE:
                 if(iNumDiscs > 0 && FRCTimer.DelayDone(1)){
                     Input.bTriggerDown = true;
                     iAutonState++;
                 }
                 else{
                     iAutonState = k_AUTON_DONE;
                 }
                 break;
                 
             /**
              * Waits to make sure disc is fired, checks for number of discs
              * left and acts accordingly
              */
             case k_AUTON_WAIT:
                 if(FRCTimer.DelayDone(1)){
                     Input.bTriggerDown = false;
                     if(iNumDiscs > 0){
                         iAutonState -= 2;
                         iNumDiscs--;
                     }
                     else{
                         iAutonState++;
                     }
                 }
                 break;
                 
             /**
              * Moves after shooting (if game strategy calls for it)
              */
             case k_AUTON_MOVING:
                 iAutonState++;
                 break;
                 
             /**
              * Stops aiming and finishes autonomous sequence
              */
             case k_AUTON_DONE:
                 Input.bAim = false;
                 break;
             default:
                 break;
         }
         Think.robotThink();
         Output.sendOutput();
     }
 
     /**
      * This function is called periodically during operator control
      */
     public void teleopPeriodic() {
        Input.gatherInput();
        Think.robotThink();
        Output.sendOutput();
     }
     
     /**
      * This function is called periodically during test mode
      */
     public void testPeriodic() {
     
     }
     
     /**
      * Is run periodically during the disable period
      * Sets boolean values when certain buttons are "pressed" in autonomous
      */
     public void disablePeriodic(){
         double[] temp = new double[2];
         
         /**
          * Makes sure only 1 second is added to dTimeWait with each button push
          */
         if(Input.coDriverStick.isPressed(6) && !bLastState){
             dTimeWait ++;
             bLastState = true;   
         }
         else if(!Input.coDriverStick.isPressed(6)){
             bLastState = false;
         }
         
         /**
          * Makes sure only 1 second is subtracted from dTimeWait with each
          * button push
          */
         if(Input.coDriverStick.isPressed(7) && !bLastState){
             dTimeWait --;
             bLastState = true;
         }
         else if(!Input.coDriverStick.isPressed(7)){
             bLastState = false;
         }
         
         /**
          * Makes sure delay time does not exceed 5 seconds
          */
         if(dTimeWait > 5.0){
             dTimeWait = 5.0;
         }
         
         /**
          * Makes sure delay time does not become negative
          */
         if(dTimeWait < 0.0){
             dTimeWait = 0.0;
         }
         
         /**
          * Aims at left target when button 4 is pressed
          */
         if(Input.coDriverStick.isPressed(4)){
             iAimState = 0;
         }
         
         /**
          * Aims at right target when button 5 is pressed
          */
         if(Input.coDriverStick.isPressed(5)){
             iAimState = 1;
         }
         
         /**
          * Aims at top target when button 3 is pressed
          */
         if(Input.coDriverStick.isPressed(3)){
             iAimState = 2;
         }
         
         /**
          * Sets number of discs loaded to 2 depending on robot's start position 
          * when button 8 is pressed
          */
         if(Input.coDriverStick.isPressed(8)){
             iNumDiscs = 2;
         }
         
         /**
          * Sets number of discs loaded to 3 depending on robot's start position
          * when button 9 is pressed
          */
         if(Input.coDriverStick.isPressed(9)){
             iNumDiscs = 3;
         }
         
     }
 }
