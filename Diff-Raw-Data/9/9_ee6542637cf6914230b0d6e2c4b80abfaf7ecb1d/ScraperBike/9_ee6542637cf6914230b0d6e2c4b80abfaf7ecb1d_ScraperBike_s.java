 /*----------------------------------------------------------------------------*/
 /* Copyright (c) FIRST Team 2035, 2012. All Rights Reserved.                  */
 /* Open Source Software - may be modified and shared by FRC teams. The code   */
 /* must be accompanied by the FIRST BSD license file in the root directory of */
 /* the project.                                                               */
 /*----------------------------------------------------------------------------*/
 
 package edu.wpi.first.wpilibj.templates;
 
 import edu.wpi.first.wpilibj.*;
 import edu.wpi.first.wpilibj.DriverStationLCD.Line;
 import edu.wpi.first.wpilibj.command.Scheduler;
 import edu.wpi.first.wpilibj.networktables.NetworkTable;
 import edu.wpi.first.wpilibj.templates.commands.*;
 import edu.wpi.first.wpilibj.templates.subsystems.*;
 
 /**
  * The VM is configured to automatically run this class, and to call the
  * functions corresponding to each mode, as described in the IterativeRobot
  * documentation. If you change the name of this class or the package after
  * creating this project, you must also update the manifest file in the resource
  * directory.
  */
 public class ScraperBike extends IterativeRobot {
 
     private static DriveTrain DriveTrain;
     private static VerticalTurretAxis VerticalAxis;
     private static Shooter shooterController;
     private static DriverStationLCD display;
     private static boolean isDisabled;
     //private static Shooter shooter;
     //private double shooterSpeed;
     private Compressor compressor;
     //private String status;
     public static NetworkTable nt;
     public static NetworkTable debugTable;
     private static Pusher pusher;
     private static Arms arms;
     private static TargetParser tp;
     private static UpdateSolenoidModule updateSolenoids;
     private static ShooterElevationPID shooterElevationPID;
     
     public static DriveTrain getDriveTrain() {
         return DriveTrain;
     }
     
     public static Pusher getPusher() {
         return pusher;
     }
     
     public static Arms getArms() {
         return arms;
     }
 
     public static VerticalTurretAxis getVerticalTurretAxis(){
         
         return VerticalAxis;
     }
     
     public static Shooter getShooterController(){
         return shooterController;
     }
     
     public static boolean getIsDisabled(){
         return isDisabled;
     }     
     
     /**
      * This function is run when the robot is first started up and should be
      * used for any initialization code.
      */
     public void robotInit() {
         // instantiate the command used for the autonomous period
         //status = new String();
         nt = NetworkTable.getTable("ST");
         debugTable = NetworkTable.getTable("Debug");
         nt.putString("Status", "Initializing");
         nt.putBoolean("AutoAlign", false);
         pusher =  new Pusher();
         arms = new Arms();
        DriveTrain = new DriveTrain();
         VerticalAxis = new VerticalTurretAxis();   
         shooterController = new Shooter();
         display = DriverStationLCD.getInstance();
         OI.initialize();
         display.updateLCD();
         display.println(Line.kUser1, 1, "Initializing...                      ");
         display.println(Line.kUser2, 1, "                                     ");
         display.println(Line.kUser3, 1, "                                     ");
         display.println(Line.kUser4, 1, "                                     ");
         display.println(Line.kUser5, 1, "                                     ");
         display.println(Line.kUser6, 1, "                                     ");
         display.updateLCD();
         compressor = new Compressor(RobotMap.pressureSwitch, RobotMap.compressorRelay);
         compressor.start();
         RobotMap.shootEncoder.start();
         nt.putString("Status", "Initialized");
         tp = new TargetParser();
         
         updateSolenoids = new UpdateSolenoidModule();
         updateSolenoids.start();
         
         //shooterElevationPID = new ShooterElevationPID(RobotMap.shooterElevationKp, RobotMap.shooterElevationKi, RobotMap.shooterElevationKd);
         //shooterElevationPID.start();
         
     }
     public void disabledInit(){
         
         System.out.println("Entering disabled...");
     }
     public void disabledPeriodic(){
         
     }
     public void autonomousInit() {
         
         System.out.println("Entering Autonomous...");
     }
 
     /**
      * This function is called periodically during autonomous
      */
     public void autonomousPeriodic() {
         Scheduler.getInstance().run();
     }
 
     public void teleopInit() {
         
         // This makes sure that the autonomous stops running when
         // teleop starts running. If you want the autonomous to 
         // continue until interrupted by another command, remove
         // this line or comment it out.
          
         System.out.println("Entering TeleOp...");
         tp.start();
 
     }
     /**
      * This function is called periodically during operator control
      */
     public void teleopPeriodic() {        
         //status =  nt.getString("Status", "");
         //display.println(Line.kUser1, 1, "Status: " + status + "              ");
         
         display.updateLCD();
         
         RobotMap.debug = DriverStation.getInstance().getDigitalIn(1);
         RobotMap.debugTable = DriverStation.getInstance().getDigitalIn(2);
         
         Scheduler.getInstance().run();
         
         //if (!RobotMap.unsortedMid.isEmpty()) {
             
             display.println(Line.kUser1, 1, "Aspect Ratio: " + RobotMap.Top.aspect);
             display.println(Line.kUser2, 1, "CenX: " + RobotMap.Top.cenX);
             display.println(Line.kUser3, 1, "CenY: " + RobotMap.Top.cenY);
             display.println(Line.kUser4, 1, "Distance (ft): " + RobotMap.Top.getRange()/12);
             display.println(Line.kUser5, 1, "Distance (in): " + RobotMap.Top.getRange());
             display.println(Line.kUser6, 1, "shoot encoder: " + RobotMap.shootEncoder.get());
                       
             //System.out.println("Distance: " + RobotMap.Top.getRange()*12);
             //display.println(Line.kUser4, 1, "Aspect Ratio: " + RobotMap.LMid.aspect);
             //display.println(Line.kUser5, 1, "CenX: " + RobotMap.LMid.cenX);
             //display.println(Line.kUser6, 1, "CenY: " + RobotMap.LMid.cenY);
         //}
         
         //display.println(Line.kUser6, 1, "Shoot Encoder" + RobotMap.shootEncoder.get() + "          ");
         
 //        display.println(Line.kUser1, 1, "Aspect Ratio: " + RobotMap.Top.aspect);
 //        display.println(Line.kUser2, 1, "CenX: " + RobotMap.Top.cenX);
 //        display.println(Line.kUser3, 1, "CenY: " + RobotMap.Top.cenY);
 //        display.println(Line.kUser4, 1, "Height: " + RobotMap.Top.height);
 //        display.println(Line.kUser5, 1, "Width: " + RobotMap.Top.width);
         //ScraperBike.debugPrintln(RobotMap.Top.aspect);
         
         display.updateLCD();
     }
     
     public static void debugToTable(String key, String text){
         
         if (RobotMap.debugTable){
             ScraperBike.debugTable.putString(key,text);
         }
         //System.out.println("T/F: " + RobotMap.debugTable + ", StrKey: " + key + ", Text: " + text);
     }  
     
     public static void debugToTable(String key, double text){
         
         if (RobotMap.debugTable){
             ScraperBike.debugTable.putString(key, ""+text);
         }
     } 
     
     public static void debugToTable(String key, int text){
         
         if (RobotMap.debugTable){
             ScraperBike.debugTable.putString(key,""+text);
         }
     } 
     
     public static void debugPrintln(String text){
         
         if (RobotMap.debug){
             System.out.println(text);
         }
     } 
     
     public static void debugPrint(String text){
         
         if (RobotMap.debug){
             System.out.print(text);
         }
     }
 }
