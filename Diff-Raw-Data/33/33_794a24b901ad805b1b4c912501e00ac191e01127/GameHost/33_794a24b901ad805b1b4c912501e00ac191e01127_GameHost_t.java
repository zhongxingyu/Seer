 package com.svamp.planetwars.network;
 
 import android.util.Log;
 import com.svamp.planetwars.Fleet;
 import com.svamp.planetwars.ShipMap;
 import com.svamp.planetwars.StarMap;
 
 import java.io.IOException;
 import java.net.DatagramPacket;
 import java.net.ServerSocket;
 import java.net.SocketAddress;
 import java.net.SocketException;
 import java.nio.ByteBuffer;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Map;
 
 /**
  */
 public class GameHost extends AbstractGameCommunicator {
     //Host retains list of players..
     private final Map<SocketAddress,Player> peers = new HashMap<SocketAddress,Player>();
 
     private final int maxClients;
     private static final String TAG = GameHost.class.getCanonicalName();
 
     private final StarMap starMap;
     private final ShipMap shipMap;
 
     private final TickManager manager = new TickManager();
 
     /**
      * @param portNum Port to initialize the host on.
      * @param maxClients Maximum number of allowed connections (clients)
      */
     public GameHost(int portNum,int maxClients) {
         super(portNum);
         this.starMap = StarMap.makeSpiralGalaxy(50);
         this.shipMap = new ShipMap(this,starMap);
         this.maxClients = maxClients;
     }
 
     public void start() throws IOException {
         super.start();
         new Thread(manager).start();
     }
     public void stop() {
         super.stop();
         manager.isRunning=false;
     }
 
     @Override
     public void receiveData(DatagramPacket pack) {
         //Wrap the package in a GameEvent:
         GameEvent event = new GameEvent(pack.getData());
         SocketAddress peerAddress = pack.getSocketAddress();
 
         switch(event.getHeader()) {
             case REQUEST_CONNECTION:
                 Log.d(TAG,"Got join request from: "+peerAddress.toString());
                 if(peers.containsKey(peerAddress)) {
                     Log.d(TAG,"Peer rejoined game: "+peerAddress.toString());
                     break; //We've registered this peer before. ignore second requests.
                 }
                 if(peers.size()<maxClients) { //Slots available for more clients.
                     Player newPlayer = new Player(getAvailableUserID()); //Make new dummy player.
                     peers.put(peerAddress, newPlayer); //Add new player to list of players.
                     //Request more player data, inform player of their user ID:
                     GameEvent userDataRequest = new GameEvent(PackageHeader.REQUEST_PLAYER_DATA,null);
                     userDataRequest.setPayload(newPlayer.getSerialization()); //Add player data to pack.
                     sendData(userDataRequest.makePacket(peerAddress)); //Send gameEvent.
                 } else { //Too many peers connected. Refuse connection:
                     GameEvent error = new GameEvent(PackageHeader.CONNECTION_REFUSED_SERVER_FULL,null);
                     sendData(error.makePacket(pack.getSocketAddress()));
                 }
 
                 break;
             case DISCONNECTED:
                 Log.d(TAG,"Peer left game: "+peers.get(peerAddress).toString());
                 peers.remove(peerAddress);
                 sendData(event.toByteArray()); //Send raw packet to all clients.
                 break;
             case SUBMITTED_PLAYER_DATA:
                 //Fetch Player object from peers, update its data with the payload from the event.
                 peers.get(peerAddress).updateFromSerialization(ByteBuffer.wrap(event.getPayload()));
                 //Retransmit all known player data to all players (cheap solution)
                 GameEvent pDataEvent = new GameEvent(PackageHeader.SUBMITTED_PLAYER_DATA,null);
 
                 for(Player p : peers.values()) {
                     pDataEvent.setPayload(p.getSerialization());
                     sendData(pDataEvent.toByteArray());
                 }
                 break;
             case FLEET_DISPATCHED: //Fleet dispatched from client. DO NOT retransmit! Listener must validate choice first..
                 ByteBuffer buffer = ByteBuffer.wrap(event.getPayload());
                 //Map sends the relevant packages and updates state itself. If success, resend package.
                 if(shipMap.sendShips(buffer)) {
                     sendData(event.toByteArray());
                     //Send to host's map also..
                     shipMap.receive(event);
                 }
                 break;
             case NEW_BUILD_ORDERS: //Client requested that we should build a new type of ship. Update, don't retransmit.
                 ByteBuffer buf = ByteBuffer.wrap(event.getPayload());
                 starMap.getStarWithHash(buf.getInt()).setBuildType(Fleet.ShipType.getByOrdinal(buf.get()));
                 break;
             case REQUEST_GAME_START:
                 peers.get(peerAddress).gameStartRequested=true;
                 //Have all requested game start?
                 for(Player p : peers.values()) {
                     if(!p.gameStartRequested) break;
                 }//Set spawn positions:
                 starMap.setSpawns(peers.values());
 
                 //Start game
                 GameEvent start = new GameEvent(PackageHeader.GAME_START,null);
                 manager.state=State.LOCKED; //Lock manager. Does not send roaming packages anymore.
                 sendData(start.toByteArray());
                 break;
             case REQUEST_MAP: //Map requested, send to all.
                 starMap.makeAllDirty();
                 break;
             case PONG:
                 break;
             default:
                 throw new IllegalArgumentException("Received package with faulty header: "+event.toString());
         }
     }
 
     @Override
     public Collection<Player> getPeers() {
         return peers.values();
     }
 
     public void sendData(byte[] data) {
 
         for(SocketAddress player : peers.keySet()) {
             try {
                 sendData(new DatagramPacket(data,data.length,player));
             } catch (SocketException e) {
                 Log.e(TAG,"Failed to send string \""+ Arrays.toString(data)+"\" to "+player.toString(),e);
             }
         }
     }
 
     public static int getFreePort() {
         int port = 37707;
         try {
             ServerSocket s = new ServerSocket(0);
             port = s.getLocalPort();
             s.close();
         } catch (IOException e) {
             //Ignore. Won't happen.
         }
         return port;
     }
 
     /**
      * Iterates over user IDs, finding a user ID that is available.
      * @return Available user ID.
      */
     private byte getAvailableUserID() {
         byte availableUserId=1; //Host is always player 1.
         boolean userIdIsAvailable=false;
         while(!userIdIsAvailable) {
             userIdIsAvailable=true;
             for(Player p : peers.values()) {
                 if(p.getPlayerNum()==availableUserId) {
                     userIdIsAvailable=false;
                     availableUserId++; //Try new userID.
                     break;
                 }
             }
         }
         return availableUserId;
     }
 
     private class TickManager implements Runnable {
         private boolean isRunning=true;
         private int tick=0;
         private State state = State.ROAMING;
 
         private final static int TICK_INTERVAL_MS = 70;
        private long dtAcc = System.currentTimeMillis();
         @Override
         public void run() {
             while(isRunning) {
                //dtAcc += TICK_INTERVAL_MS;
                //if(Math.random()<0.1f)Log.d(TAG,"Internal time is off by "+(System.currentTimeMillis()-dtAcc)+"ms.");
                //dtAcc = System.currentTimeMillis();
                long startTime = System.currentTimeMillis();
                 if(state==State.ROAMING) {
                     //TODO: broadcast host address.
                 }
 
 
                 if(state==State.LOCKED) {
                 /*Start of new tick:*/
                     starMap.update(TICK_INTERVAL_MS / 1000f);
                     shipMap.update(TICK_INTERVAL_MS / 1000f);
                     if(tick % 30 == 0) { //Periodical ping.
                         GameEvent event = new GameEvent(PackageHeader.PING,null);
                         sendData(event.toByteArray());
                     }
                     if(starMap.isDirty()) { //Time for a starMap update.
                         GameEvent event = new GameEvent(PackageHeader.STAR_STATE_CHANGED,null);
                         event.setPayload(starMap.getSerialization());
                         sendData(event.toByteArray());
                     }
                 }
                long dt = System.currentTimeMillis() -startTime;
                 try {
                    if(dt < TICK_INTERVAL_MS) {
                    //Enforce tick intervals regardless of other processing times.
                    Thread.sleep(TICK_INTERVAL_MS-dt);
                    }
                 } catch (InterruptedException ignored) {}
                 tick++;
             }
         }
     }
 
     public static enum State { ROAMING,LOCKED }
 }
