 package org.java_websocket;
 
 import java.net.UnknownHostException;
 import java.util.ArrayList;
 import java.util.Date;
 import org.json.simple.JSONObject;
 import org.json.simple.JSONValue;
 import org.java_websocket.Messages.DoctorClientPacket;
 import org.java_websocket.MessagesManagers.CatchMeMessagesManager;
 import org.java_websocket.MessagesManagers.HelpMeMessagesManager;
 
 /**
  *
  * @author Matteo Ciman
  * 
  * @version 0.1
  * 
  * 0.1 First specification of the class
  */
 public class DoctorClientManager extends BaseManager {
     
     protected ArrayList<String> messagesToSend = new ArrayList<String>();
     
     public DoctorClientManager(int port) throws UnknownHostException {
         super("DOCTOR_CLIENT", port);
     }
     
     @Override
     public void onError(WebSocket client, Exception exc) {
         System.out.println("Error in " + clientType + " Manager, " 
                 + exc.toString());
         
         clientConnected = null;
     }
     
     @Override
     public boolean onMessage(WebSocket sender, String message) {
         
         JSONObject packet = (JSONObject)JSONValue.parse(message);
         
         if (packet.get(BaseManager.MESSAGE_TYPE).equals("CHANGE_SPEED")) {
             serverManager.messageFromDoctorToClient(packet);
         }
         else if (packet.get(BaseManager.MESSAGE_TYPE).equals(BaseManager.STOP_GAME)) {
             serverManager.stopGame(packet);
         }
         else if (packet.get(BaseManager.MESSAGE_TYPE).equals(BaseManager.IDENTIFICATION)) {
             
             if (checkClientType((String)packet.get(BaseManager.DATA_IDENTIFIER))) {
                 System.out.println("Identification complete: " + clientType);
                 clientConnected = sender;
                 JSONObject packetToSend = new JSONObject();
                 packetToSend.put(BaseManager.MESSAGE_TYPE, BaseManager.IDENTIFICATION_COMPLETE);
                 clientConnected.send(packetToSend.toJSONString());
                 
             }
             else {
                 System.out.println("Wrong identification type for " + clientType
                         + " Manager");
             }
         }
         else if (packet.get(BaseManager.MESSAGE_TYPE).equals("GAME")) {
             
             if (patientManager != null) {
                 patientManager.sendPacket(packet);
                 packet.put(BaseManager.RESULT, true);
             }
             else {
                 packet.put(BaseManager.RESULT, false);
             }
             
             clientConnected.send(packet.toJSONString());
         }
         else if (packet.get(BaseManager.MESSAGE_TYPE).equals(BaseManager.SCREEN_MEASURES)) {
             
             if (patientManager != null) {
                 patientManager.sendPacket(packet);
             }
             else {
                 packet.put(BaseManager.RESULT, false);
                 packet.put("ERROR", "01");
                 
                 clientConnected.send(packet.toJSONString());
             }
         }
         else if (packet.get(BaseManager.MESSAGE_TYPE).equals(BaseManager.GAME_SETTINGS)) {
             
             patientManager.sendPacket(packet);
             
             patientID = (String)packet.get(BaseManager.PATIENT_ID);
             gameIdentification = dbManager.getGameIdentification((String)packet.get(BaseManager.GAME_ID));
             
         }
         else if (packet.get(BaseManager.MESSAGE_TYPE).equals("WAITING_TRACKER")) {
             
             waitingForTracker = true;
         }
         else if (packet.get(BaseManager.MESSAGE_TYPE).equals(BaseManager.START_PRESENTATION) || 
                 packet.get(BaseManager.MESSAGE_TYPE).equals("GO_BACK")) {
             
             patientManager.sendPacket(packet);
         }
         /**
          * Packet identifies the beginning of the training session. 
          * Calculates a starting time and sends it to the eye tracker 
          * and the patient client.
          */
         else if (packet.get(BaseManager.MESSAGE_TYPE).equals(BaseManager.START_TRAINING)) {
             
            long timeToStart = new Date().getTime() + 15000; // Audio presentation lasts 12 seconds
             long timeForEyeTracker = eyeTrackerManager.calculateTimeWithOffset(timeToStart);
             long timeForPatient = patientManager.calculateTimeWithOffset(timeToStart);
             
             JSONObject packetEyeTracker = (JSONObject)packet.clone();
             packetEyeTracker.put(BaseManager.START_TIME, timeForEyeTracker);
             packet.put(BaseManager.START_TIME, timeForPatient);
             
             eyeTrackerManager.sendPacket(packetEyeTracker);
             patientManager.sendPacket(packet);
         }
         /*
          * TODO: Provide a game without the eye-tracker
          */
         else if (packet.get(BaseManager.MESSAGE_TYPE).equals("WITHOUT_TRACKER")) {
             
         }
         /**
          * Training completed, the doctor decides if keep the current
          * training session or to repeat it again
          */
         else if (packet.get(BaseManager.MESSAGE_TYPE).equals("TRAINING_VALIDATION")) {
             
             eyeTrackerManager.sendPacket(packet);
         }
         /*
          * Everything is ready, the doctor has say that it is time to start
          * the game: 
          * 1. Creates a new visit in the DB
          * 2. Defines folder where save packets
          * 3. Call server method to calculate start time
          */
         else if (packet.get(BaseManager.MESSAGE_TYPE).equals(BaseManager.START_GAME)) {
             
             //patientID = (String)packet.get("PATIENT_ID");
             //String gameIdentification = (String)packet.get("GAME_ID");
             Boolean withEyeTracker = (Boolean)packet.get("WITH_TRACKER");
             int gameID = dbManager.getGameID(gameIdentification);
             int visitID = dbManager.insertNewVisit(new Integer(patientID), 
                     new Integer(gameID));
             
             System.out.println("Game identification: " + gameIdentification);
             
             if (gameIdentification.equals(BaseManager.CATCH_ME)) {
                 messageManager = new CatchMeMessagesManager(patientID, visitID, withEyeTracker.booleanValue());
                 String folder = messageManager.getFolderWhereArchive();
                 dbManager.setFolder(visitID, folder);
                 patientManager.writeGameSpecs();
             }
             else if (gameIdentification.equals(BaseManager.HELP_ME)) {
                 messageManager = new HelpMeMessagesManager(patientID, visitID, withEyeTracker.booleanValue());
                 String folder = messageManager.getFolderWhereArchive();
                 dbManager.setFolder(visitID, folder);
             }
             
             messageManager.start();
             serverManager.timeToStart();
         }
         return true;
     }
     
     @Override
     public void onClose(WebSocket client, int code, String reason, boolean remote) {
         System.out.println("Connection closed for " + clientType + " Manager");
         clientConnected = null;
         
     }
     
     public void sendMessageToDoctorClient(DoctorClientPacket packet) {
         
         if (clientConnected != null) {
             clientConnected.send(packet.toJSONString());
         }
         else {
             messagesToSend.add(packet.toJSONString());
         }
     }
     
     @Override
     public void sendPacket(JSONObject packet) {
         if (clientConnected != null) {
             clientConnected.send(packet.toJSONString());
         }
         else {
             messagesToSend.add(packet.toJSONString());
         } 
     }
 }
