 /* Currently managed by: Levi Raby,Tarun Sunkaraneni, Adam Jessop
  * 
  * 
  * 
  */
 package edu.ames.frc.robot;
 //Non-explicit imports of io libraries. Once code is finished it should be changed into a set of explicit imports.
 import java.io.*;
 import javax.microedition.io.*;
 import edu.wpi.first.wpilibj.DriverStation;
 import edu.wpi.first.wpilibj.DriverStationLCD;
 import edu.wpi.first.wpilibj.Timer;
 import edu.wpi.first.wpilibj.Watchdog;
 import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
 //ServerSocket Javadoc: http://docs.oracle.com/javase/1.4.2/docs/api/java/net/ServerSocket.html
 //http://www.wbrobotics.com/javadoc/javax/microedition/io/SocketConnection.html
 
 public class Communication {
 
     public static boolean isinit = false;   // make sure we're already initted
     public static boolean debugmode = false; // debugging symbols enabled/disabled?
     public static long time;
     public static int step = 0;
     public static double voltage;
     public static boolean mainlcd = false;  // enable/disable main led
     public static boolean sensorlighton;
     public static String[] messages = new String[5];
     public static int cycle = 0;            // how many clock cycles the robot ran, divided by 500
 
     public void ConsoleMsg(String msg, int type) {
         messages[type] = msg;
     }
 
     public void MsgPrint() {
     }
 
     public class PISocket{
 
         boolean active;
         SocketConnection psock = null;
         public PISocket(boolean activated) throws Exception{
             active = activated;
              psock = (SocketConnection)
                   Connector.open("socket://127.0.0.1:3243");  
                          
  InputStream is = psock.openInputStream();
  OutputStream os = psock.openOutputStream();
  Integer intVal = new Integer(is.read());
  
  
  is.close(); psock.close();os.close(); 

              
 
  
 
         }
     }
 }
