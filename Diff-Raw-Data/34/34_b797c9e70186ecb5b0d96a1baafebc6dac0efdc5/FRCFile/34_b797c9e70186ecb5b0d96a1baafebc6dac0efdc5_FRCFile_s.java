 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package edu.wpi.first.wpilibj.templates;
 
 import com.sun.squawk.microedition.io.FileConnection;
 import java.io.DataInputStream;
 import javax.microedition.io.Connector;
 
 /**
  *
  * @author First1
  */
 public class FRCFile {
 
     private static DataInputStream inFile;
     private static FileConnection fc;
     
     // Config file
     private static final String CONFIG_FILE = "config.properties";
     
     // Define variables in config file with default values
     public static boolean runAuton = true;
     public static double dUpperDistanceLimit = 1.1;
     public static double dLowerDistanceLimit = 0.9;
     public static double dLoadForwardPower = 0.9;
     public static double dLoadReversePower = -0.9;
     public static double dUpperAngleLimit = 1.1;
     public static double dLowerAngleLimit = 0.9;
     public static double dStopPower = 0;
     public static double dUpperPowerStopLimit = 0.1;
     public static double dLowerPowerStopLimit = -0.1;
     public static double dMaxMotorValLeft = 0.9;
     public static double dMaxMotorValRight = 0.9;
     public static double dSlowSpeedLeft = .75;
     public static double dSlowSpeedRight = .75;
     public static double dClimbPower = 1.0;
     public static int iRightDriveMotorSlot = 6;
     public static int iLeftDriveMotorSlot = 5;
     public static int iShooterMotorSlot = 3;
     public static int iClimbMotor1Slot = 2;
     public static int iClimbMotor2Slot = 1;
     public static int iKickerMotorSlot = 2;
     public static int iHookerMotorSlot = 7;
     public static int iDropSlot1 = 1;
     public static int iDropSlot2 = 2;
     public static int iDropSlot3 = 3;
     public static int iDriverPortRight = 1;
     public static int iDriverPortLeft = 2;
     public static int iDriverPortCo = 3;
     public static double dAutonPowerLimitUpper = 0.1;
     public static double dAutonPowerLimitLower = -0.1;
     
     
     /**
      * Opens file
      *
      * @param Filename Name of the file to open
      */
     private static void openFile(String Filename) {
         try {
             fc = (FileConnection) Connector.open("file:///" + Filename,
                     Connector.READ);
             //fc.create();
             inFile = fc.openDataInputStream();
         } catch (Exception e) {
             System.out.println("Could not open file" + Filename);
         }
     }
 
     /**
      * Closes the file
      */
     private static void closeFile() {
         try {
             if (inFile != null) {
                 inFile.close();
             }
             if (fc != null) {
                 fc.close();
             }
         } catch (Exception e) {
             System.out.println("Could not close file: " + e);
         }
     }
 
     /**
      * Reads the file and runs autonomous mode
      *
      * @param Filename is the file to be read
      */
     public static void initConfig() {
         int lineNumber = 0;
         try {
             openFile(CONFIG_FILE);
             while (inFile.available() != 0) {
                 
                 switch (lineNumber) {
                     case 0: // Line 0 is runAuton
                         runAuton = inFile.readBoolean();
                         ++lineNumber;
                         break;
                     case 1: 
                         dUpperDistanceLimit = inFile.readDouble();
                         ++lineNumber;
                         break;
                     case 2:
                         dLowerDistanceLimit = inFile.readDouble();
                         ++lineNumber;
                         break;
                     case 3:
                         dLoadForwardPower = inFile.readDouble();
                         ++lineNumber;
                         break;
                     case 4:
                         dLoadReversePower = inFile.readDouble();
                         ++lineNumber;
                         break;
                     case 5:
                         dUpperAngleLimit = inFile.readDouble();
                         ++lineNumber;
                         break;
                     case 6:
                         dLowerAngleLimit = inFile.readDouble();
                         ++lineNumber;
                         break;
                     case 7:
                         dStopPower = inFile.readDouble();
                         ++lineNumber;
                         break;
                     case 8:
                         dUpperPowerStopLimit = inFile.readDouble();
                         ++lineNumber;
                         break;
                     case 9:
                         dLowerPowerStopLimit = inFile.readDouble();
                         ++lineNumber;
                         break;
                     case 10:
                         dMaxMotorValLeft = inFile.readDouble();
                         ++lineNumber;
                         break;
                     case 11:
                         dMaxMotorValRight = inFile.readDouble();
                         ++lineNumber;
                         break;
                     case 12:
                         dSlowSpeedLeft = inFile.readDouble();
                         ++lineNumber;
                         break;
                     case 13:
                         dSlowSpeedRight = inFile.readDouble();
                         ++lineNumber;
                         break;
                     case 14:
                         iRightDriveMotorSlot = inFile.readInt();
                         ++lineNumber;
                         break;
                     case 15:
                         iLeftDriveMotorSlot = inFile.readInt();
                         ++lineNumber;
                         break;
                     case 16:
                         iShooterMotorSlot = inFile.readInt();
                         ++lineNumber;
                         break;
                     case 17: 
                         iClimbMotor1Slot = inFile.readInt();
                         ++lineNumber;
                         break;
                     case 18:
                         iClimbMotor2Slot = inFile.readInt();
                         ++lineNumber;
                         break;
                     case 19:
                         iKickerMotorSlot = inFile.readInt();
                         ++lineNumber;
                         break;
                     case 20:
                         iDropSlot1 = inFile.readInt();
                         ++lineNumber;
                         break;
                     case 21:
                         iDropSlot2 = inFile.readInt();
                         ++lineNumber;
                         break;
                     case 22:
                         iDropSlot3 = inFile.readInt();
                         ++lineNumber;
                         break;
                     case 23:
                         iDropSlot3 = inFile.readInt();
                         ++lineNumber;
                         break;
                     case 24:
                         dClimbPower = inFile.readDouble();
                         ++lineNumber;
                         break;
                     case 25:
                         iDriverPortRight = inFile.readInt();
                         ++lineNumber;
                         break;
                     case 26:
                         iDriverPortLeft = inFile.readInt();
                         ++lineNumber;
                         break;
                     case 27:
                         iDriverPortCo = inFile.readInt();
                         ++lineNumber;
                         break;
                     case 28:
                         dAutonPowerLimitUpper = inFile.readDouble();
                         ++lineNumber;
                         break;
                     case 29:
                         dAutonPowerLimitLower = inFile.readDouble();
                         ++lineNumber;
                         break;
                     case 30:
                         iHookerMotorSlot = inFile.readInt();
                         ++lineNumber;
                         break;
                       
                         
                     // Add new cases for new variables
                     default:
                         System.out.println("unknown line number " + lineNumber);
                         break;
                 }
             }
         } catch (Exception e) {
             System.out.println("Could not read file " + CONFIG_FILE + " : " + e);
         } finally {
             closeFile();
         }
     }
 
     // Getters for variables in config file
     
     public static boolean isAuton() {
         return runAuton;
     }
     
     // Add more getters below
 }
  
