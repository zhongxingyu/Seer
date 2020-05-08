 package com.edinarobotics.zephyr;
 
 import edu.wpi.first.wpilibj.Compressor;
 import edu.wpi.first.wpilibj.DriverStationLCD;
 import edu.wpi.first.wpilibj.Jaguar;
 import edu.wpi.first.wpilibj.Relay;
 import edu.wpi.first.wpilibj.RobotDrive;
 
 /**
  *
  */
 public class Components {
     //PORT NUMBERS HERE!
    private static final int LEFT_JAGUAR_PORT = 2;
    private static final int RIGHT_JAGUAR_PORT = 1;
     private static final int SHOOTER_JAGUAR_PORT = 3;
     private static final int COMPRESSOR_PRESSURE_SENSOR = 1;
     private static final int COMPRESSOR_SPIKE = 1;
     private static final int BALL_LOAD_PISTON_SPIKE = 2;
     
     private static Components instance;
     public Jaguar leftJaguar;
     public Jaguar rightJaguar;
     public Jaguar shooterJaguar;
     public RobotDrive driveControl;
     public Compressor compressor;
     public Relay ballLoadPiston;
     
     public DriverStationLCD textOutput;
     
     private Components(){
         leftJaguar = new Jaguar(LEFT_JAGUAR_PORT);
         rightJaguar = new Jaguar(RIGHT_JAGUAR_PORT);
         shooterJaguar = new Jaguar(SHOOTER_JAGUAR_PORT);
         driveControl = new RobotDrive(leftJaguar,rightJaguar);
         compressor  = new Compressor(COMPRESSOR_PRESSURE_SENSOR,COMPRESSOR_SPIKE);
         compressor.start();
         ballLoadPiston = new Relay(BALL_LOAD_PISTON_SPIKE);
         textOutput = DriverStationLCD.getInstance();
     }
     
     public static Components getInstance(){
         if(instance == null){
             instance = new Components();
         }
         return instance;
     }
 }
