 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.net.Socket;
 import java.util.Map;
 
 /**
  * User: Ivan
  * Date: 24/02/13
  */
 public class MazewarServerHandler extends Thread {
     private static final Logger logger = LoggerFactory.getLogger(MazewarServerHandler.class);
     private Socket socket;
     public ObjectOutputStream out;
 
     public MazewarServerHandler(Socket socket) {
         this.socket = socket;
 
         /* stream to write back to client */
         try {
             out = new ObjectOutputStream(socket.getOutputStream());
         } catch (IOException e) {
             e.printStackTrace();
         }
 
         logger.info("Created a new thread to handle Mazewar Client\n");
     }
 
     @Override
     public void run() {
         try {
             /* stream to read from client */
             ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
 
             MazewarPacket fromClient;
 
            polling:
             while ((fromClient = (MazewarPacket) in.readObject()) != null) {
                 // Print the packet message on screen for now
                 switch (fromClient.type) {
                     case MazewarPacket.REGISTER:
                         registerClient(fromClient);
                         break;
                     case MazewarPacket.ADD:
                         addClient(fromClient);
                         break;
                     case MazewarPacket.QUIT:
                         quitClient(fromClient);
                        break polling;
                     case MazewarPacket.MOVE_FORWARD:
                     case MazewarPacket.MOVE_BACKWARD:
                         moveClient(fromClient);
                         break;
                     case MazewarPacket.TURN_LEFT:
                     case MazewarPacket.TURN_RIGHT:
                         rotateClient(fromClient);
                         break;
                     case MazewarPacket.FIRE:
                         clientFire(fromClient);
                         break;
                     case MazewarPacket.INSTANT_KILL:
                         instantKillClient(fromClient);
                         break;
                     case MazewarPacket.KILL:
                         killClient(fromClient);
                         break;
                     default:
                         logger.info("ERROR: Unrecognized packet!");
                 }
                 logger.info("Finished handling request type " + fromClient.type);
                 logger.info("Current number of connedtedClients: " + MazewarServer.connectedClients.size() + "\n");
             }
 
             /* cleanup when client exits */
             in.close();
             out.close();
         } catch (IOException e) {
             e.printStackTrace();
         } catch (ClassNotFoundException e) {
             e.printStackTrace();
         }
     }
 
     private void registerClient(MazewarPacket fromClient) {
         synchronized (MazewarServer.connectedClients) {
             String clientName = fromClient.owner;
             MazewarPacket replyPacket = new MazewarPacket();
             replyPacket.owner = clientName;
 
             if (!MazewarServer.connectedClients.containsKey(clientName)) {
                 MazewarServer.connectedClients.put(clientName, null);
                 replyPacket.type = MazewarPacket.REGISTER_SUCCESS;
                 logger.info("registerClient: " + clientName);
             } else {
                 replyPacket.type = MazewarPacket.ERROR_DUPLICATED_CLIENT;
                 logger.info("Received register request with dup name: " + clientName);
             }
 
             synchronized (this.out) {
                 try {
                     out.writeObject(replyPacket);
                 } catch (IOException e) {
                     e.printStackTrace();
                 }
             }
         }
     }
 
     private void addClient(MazewarPacket fromClient) {
         synchronized (MazewarServer.mazeMap) {
             String clientName = fromClient.owner;
             DirectedPoint clientDp = fromClient.mazeMap.get(clientName);
 
             MazewarPacket replyPacket;
             for (Map.Entry<String, DirectedPoint> entry : MazewarServer.mazeMap.entrySet()) {
                 Point savedPoint = entry.getValue();
                 if (savedPoint.equals((Point) clientDp)) {
                     replyPacket = new MazewarPacket();
                     replyPacket.type = MazewarPacket.ERROR_DUPLICATED_LOCATION;
 
                     synchronized (this.out) {
                         try {
                             out.writeObject(replyPacket);
                         } catch (IOException e) {
                             e.printStackTrace();
                         }
                     }
                     logger.info("Client " + clientName + " requested location is filled by " + entry.getKey());
                     return;
                 }
             }
 
             replyPacket = new MazewarPacket();
             replyPacket.type = MazewarPacket.ADD_SUCCESS;
             replyPacket.owner = clientName;
             replyPacket.mazeMap = MazewarServer.mazeMap;
             replyPacket.mazeScore = MazewarServer.mazeScore;
 
             synchronized (this.out) {
                 try {
                     out.writeObject(replyPacket);
                 } catch (IOException e) {
                     e.printStackTrace();
                 }
             }
 
             synchronized (MazewarServer.connectedClients) {
                 MazewarServer.connectedClients.put(clientName, out);
             }
 
             MazewarServer.mazeMap.put(clientName, clientDp);
 
             synchronized (MazewarServer.mazeScore) {
                 MazewarServer.mazeScore.put(clientName, 0);
             }
 
             MazewarServer.actionQueue.add(fromClient);
             logger.info("Add Client: " + clientName +
                     "\n\tto X: " + clientDp.getX() +
                     "\n\tto Y: " + clientDp.getY() +
                     "\n\torientation : " + clientDp.getDirection());
             logger.info("Add Client: " + clientName +
                     " with score " + MazewarServer.mazeScore.get(clientName));
         }
     }
 
     private void quitClient(MazewarPacket fromClient) {
         synchronized (MazewarServer.connectedClients) {
             String clientName = fromClient.owner;
             MazewarServer.connectedClients.remove(clientName);
 
             synchronized (MazewarServer.mazeMap) {
                 MazewarServer.mazeMap.remove(clientName);
 
                 synchronized (MazewarServer.mazeScore) {
                     MazewarServer.mazeScore.remove(clientName);
                 }
             }
 
             MazewarServer.actionQueue.add(fromClient);
             logger.info("Client: " + clientName + " disconnected!");
         }
     }
 
     private void moveClient(MazewarPacket fromClient) {
         synchronized (MazewarServer.mazeMap) {
             String clientName = fromClient.owner;
             DirectedPoint clientDp = fromClient.mazeMap.get(clientName);
 
             for (Map.Entry<String, DirectedPoint> savedClient : MazewarServer.mazeMap.entrySet()) {
                 Point savedClientDp = savedClient.getValue();
 
                 if (savedClientDp.equals(clientDp)) {
                     logger.info(clientName + " Cannot move to" +
                             "\n\tX: " + clientDp.getX() +
                             "\n\tY: " + clientDp.getY() +
                             "\n\torientation : " + clientDp.getDirection());
                     return;
                 }
             }
 
             MazewarServer.mazeMap.put(clientName, clientDp);
             MazewarServer.actionQueue.add(fromClient);
             logger.info("moveClient: " + clientName +
                     "\n\tto X: " + clientDp.getX() +
                     "\n\t   Y: " + clientDp.getY() +
                     "\n\t   orientation: " + clientDp.getDirection());
         }
     }
 
     private void rotateClient(MazewarPacket fromClient) {
         synchronized (MazewarServer.mazeMap) {
             String clientName = fromClient.owner;
             DirectedPoint clientDp = fromClient.mazeMap.get(clientName);
 
             MazewarServer.mazeMap.put(clientName, clientDp);
             MazewarServer.actionQueue.add(fromClient);
             logger.info("rotateClient: " + clientName +
                     "\n\tto X: " + clientDp.getX() +
                     "\n\t   Y: " + clientDp.getY() +
                     "\n\t   orientation: " + clientDp.getDirection());
         }
     }
 
     private void clientFire(MazewarPacket fromClient) {
         synchronized (MazewarServer.mazeScore) {
             String clientName = fromClient.owner;
 
             MazewarServer.mazeScore.put(clientName, MazewarServer.mazeScore.get(clientName) + MazewarServer.scoreAdjFire);
 
             MazewarServer.actionQueue.add(fromClient);
             logger.info("Client " + clientName + " fired" +
                     "\n\tUpdate score to " + MazewarServer.mazeScore.get(clientName));
         }
     }
 
     private void instantKillClient(MazewarPacket fromClient) {
         synchronized (MazewarServer.mazeMap) {
             String srcClientName = fromClient.owner;
             String tgtClientName = fromClient.victim;
             DirectedPoint tgtClientLoc = fromClient.mazeMap.get(tgtClientName);
 
             MazewarServer.mazeMap.put(tgtClientName, tgtClientLoc);
 
             synchronized (MazewarServer.mazeScore) {
                 //adjust the score for killing
                 MazewarServer.mazeScore.put(tgtClientName, MazewarServer.mazeScore.get(tgtClientName) + MazewarServer.scoreAdjKilled);
                 MazewarServer.mazeScore.put(srcClientName, MazewarServer.mazeScore.get(srcClientName) + MazewarServer.scoreAdjInstKill);
             }
 
             MazewarServer.actionQueue.add(fromClient);
             logger.info("Client " + srcClientName + " instantly killed " + tgtClientName
                     + "\n\treSpawn location " + tgtClientLoc.getX() + " " + tgtClientLoc.getY() + " " + tgtClientLoc.getDirection()
                     + "\n\tcurrent score of victim " + MazewarServer.mazeScore.get(tgtClientName)
                     + "\n\tcurrent score of killer " + MazewarServer.mazeScore.get(srcClientName));
         }
     }
 
     private void killClient(MazewarPacket fromClient) {
         synchronized (MazewarServer.mazeMap) {
             String srcClientName = fromClient.owner;
             String tgtClientName = fromClient.victim;
             DirectedPoint tgtClientLoc = fromClient.mazeMap.get(tgtClientName);
 
             MazewarServer.mazeMap.put(tgtClientName, tgtClientLoc);
 
             synchronized (MazewarServer.mazeScore) {
                 //adjust the score for killing
                 MazewarServer.mazeScore.put(tgtClientName, MazewarServer.mazeScore.get(tgtClientName) + MazewarServer.scoreAdjKilled);
                 MazewarServer.mazeScore.put(srcClientName, MazewarServer.mazeScore.get(srcClientName) + MazewarServer.scoreAdjKill);
             }
 
             MazewarServer.actionQueue.add(fromClient);
             logger.info("Client " + srcClientName + " killed " + tgtClientName
                     + "\n\treSpawn location " + tgtClientLoc.getX() + " " + tgtClientLoc.getY() + " " + tgtClientLoc.getDirection()
                     + "\n\tcurrent score of victim " + MazewarServer.mazeScore.get(tgtClientName)
                     + "\n\tcurrent score of killer " + MazewarServer.mazeScore.get(srcClientName));
         }
     }
 }
