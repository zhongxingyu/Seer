 package org.java_websocket;
 
 /**
  *
  * @author Matteo Ciman
  * 
  * @version 1.0
  */
 
 import java.net.UnknownHostException;
 import java.util.Date;
 import org.json.simple.JSONObject;
 
 public class ServerManager {
     
     private EyeTrackerManager clientEyeTracker = null;
     private IPADClientManager clientIpad = null;
     private DoctorClientManager clientDoctor = null;
     
     private boolean eyeTrackerReady = false;
     private boolean gameReady = false;
     private boolean doctorClientReady = false;
     
     protected void timeToStart() {
         long minimumIncrement = 10000;
         
         long timeToStart = new Date().getTime() + minimumIncrement;
         clientEyeTracker.comunicateStartTime(timeToStart);
         clientIpad.comunicateStartTime(timeToStart);
     }
     
     public void startManagers() {
         
         clientEyeTracker.start();
         clientIpad.start();
         clientDoctor.start();
     }
     
     public void stopGame(JSONObject packet) {
         clientEyeTracker.sendPacket(packet);
         clientIpad.sendPacket(packet);
         clientDoctor.sendPacket(packet);
         WebSocketWithOffsetCalc.messageManager.gameIsEnded();
     }
     
     public void messageFromDoctorToClient(JSONObject packet) {
         
         clientIpad.sendPacket(packet);
     }
     
     public ServerManager() throws UnknownHostException{
         int eyeTrackerPort = 8000;
         int ipadPort = 8001;
         int doctorPort = 8002;
         clientDoctor = new DoctorClientManager(doctorPort);
         clientEyeTracker = new EyeTrackerManager(eyeTrackerPort);
         clientIpad = new IPADClientManager(ipadPort);
         
         WebSocketWithOffsetCalc.setDoctorClientManager(clientDoctor);
     }
     
     /* Definire un metodo che permetta di chiudere applicazione
      * che deve però essere invocato da un utente esterno
      */
     public static void main(String args[]) {
         WebSocket.DEBUG = false;
         String host = "localhost";
         
         if (args.length != 0) {
             host = args[0];
         }
         System.out.println(host);
         
         try {
             
             ServerManager manager = new ServerManager();
             BaseManager.setServerManager(manager);
             
             manager.startManagers();
             
             System.out.println("Server Started");
             
             Thread.sleep(3000);
            //EyeTrackerSimulator simulator = new EyeTrackerSimulator(host, 8000);
            //simulator.connect();
         }
         catch (Exception exc) {
             exc.printStackTrace();
         }
     }
     
 }
