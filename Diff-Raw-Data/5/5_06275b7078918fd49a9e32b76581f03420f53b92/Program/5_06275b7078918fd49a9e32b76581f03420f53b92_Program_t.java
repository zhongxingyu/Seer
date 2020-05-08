 /**
  * 
  */
 package AP2DX.coordinator;
 
 import java.net.*;
 import java.io.*;
 
 import AP2DX.AP2DXBase;
 import AP2DX.Connection;
 
 
 /**
  * @author jjwt
  *
  */
 public class Program extends AP2DXBase {
 	
 
     /** The port of the simulator. Reaf from the configuration file. */
     private int simulatorPort;
     /** The IP address of the simulator. Read from the configuration file. */
     private String simulatorAddress;
 
     private Connection connection;
 
 	/**
 	 * entrypoint of coordinator 
 	 */
 	public static void main (String[] args){
 		AP2DXBase instance = new Program();
 		
 		System.exit(0);
 	}
 
 	
 	/**
 	 * constructor
 	 */
 	public Program() {
         super(); // explicitly call base ctor for extra 
 		System.out.println(" Running... ");
 
         String spawnMessage = "INIT {ClassName USARBot.P2DX} {Location 4.5,1.9,1.8} {Name R1}\r\n";
 	  
         try 
         { 
             connection = new Connection(simulatorAddress, simulatorPort);
         }
         catch (Exception e)
         {
             e.printStackTrace();
             logger.severe("Connection refused: " + e.getMessage());
             return;
         }
         connection.sendMessage(spawnMessage);
       
         while (true)
         {   
             String msg = connection.readMessage();
             if (msg != null)
                 System.out.println(msg);
             else 
             {      
                 logger.severe("Error in reading from connection.");
                 break;
             }
         } 
         if (connection.close())
             System.out.println("Closed successfully!");
         else
             logger.severe("Error in closing the connection.");
 
 	}
 
     protected void setConfig() 
     {
         simulatorAddress = (config.get("sim_address")).toString();
         //simulatorPort = config.get("sim_port");
        simulatorPort = 3000;
     }
 }
