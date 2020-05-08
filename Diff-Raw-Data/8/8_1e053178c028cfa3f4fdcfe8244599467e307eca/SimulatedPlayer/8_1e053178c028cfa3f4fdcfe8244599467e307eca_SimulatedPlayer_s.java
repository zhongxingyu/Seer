 /*
  * Copyright (c) 2008, Sun Microsystems, Inc.
  *
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions
  * are met:
  *
  *     * Redistributions of source code must retain the above copyright
  *       notice, this list of conditions and the following disclaimer.
  *     * Redistributions in binary form must reproduce the above copyright
  *       notice, this list of conditions and the following disclaimer in
  *       the documentation and/or other materials provided with the
  *       distribution.
  *     * Neither the name of Sun Microsystems, Inc. nor the names of its
  *       contributors may be used to endorse or promote products derived
  *       from this software without specific prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
  * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
  * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
  * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
  * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
  * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
  * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
  * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
  * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
  * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
  * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 
 package com.sun.darkstar.example.snowman.clientsimulator;
 
 import com.sun.darkstar.example.snowman.common.physics.enumn.EForce;
 import com.sun.darkstar.example.snowman.common.protocol.enumn.EEndState;
 import com.sun.darkstar.example.snowman.common.protocol.enumn.EMOBType;
 import com.sun.darkstar.example.snowman.common.protocol.enumn.ETeamColor;
 import com.sun.darkstar.example.snowman.common.protocol.messages.ClientMessages;
 import com.sun.darkstar.example.snowman.common.protocol.processor.IClientProcessor;
 import com.sun.darkstar.example.snowman.common.util.HPConverter;
 import com.sun.darkstar.example.snowman.common.util.SingletonRegistry;
 import com.sun.sgs.client.ClientChannel;
 import com.sun.sgs.client.ClientChannelListener;
 import com.sun.sgs.client.simple.SimpleClient;
 import com.sun.sgs.client.simple.SimpleClientListener;
 import java.io.IOException;
 import java.net.PasswordAuthentication;
 import java.nio.ByteBuffer;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Properties;
 import java.util.Random;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  * Simulated player. An instance of this class will play the snowman
  * game by moving randomly.
  * 
  * @author Jeffrey Kesselman
  * @author Keith Thompson
  */
 class SimulatedPlayer implements SimpleClientListener {
     static final Logger logger =
             Logger.getLogger(SimulatedPlayer.class.getName());
     
     static private final Random random = new Random(System.currentTimeMillis());
     
     static enum PLAYERSTATE {
         LoggingIn,  // Initial state, waiting for login
         Paused,     // Logged in, waiting for game to stat
         Playing,    // Playing, alive (moving)
         Dead,       // Playing, dead (no moves)
         Quit        // Exiting
     }
     
     private final String name;
     private final String password;
     private final int moveDelay;
     private final SimpleClient simpleClient;
     private final IClientProcessor pktHandler;
     
     // this players ID and team, provided by the server
     private int id;
     private ETeamColor myTeam;
 
     // Must be synchronized
     private PLAYERSTATE state;
     
     // Must be synchronized
     // start and end position of current move. start==end when stopped.
     private float startX = 0.0f;
     private float startY = 0.0f;
     private float destX = 0.0f;
     private float destY = 0.0f;
     
     // Must be synchronized
     private int hitPoints;
 
     // (approximate) timestamp of the last move sent
     private long lastTimestamp;
     
     private static class Target {
         final int id;
         final float x;
         final float y;
         
         Target(int id, float x, float y) {
             this.id = id;
             this.x = x;
             this.y = y;
         }
     }
     
     private Target target = null;
     
     // map player IDs to team
     private final Map<Integer, ETeamColor> players = new HashMap<Integer, ETeamColor>();
     
     /**
      * Construct a simulated player. The {@code props} argument supports the
      * following properties:
      * 
      * <dl style="margin-left: 1em">
      *
      * <dt> <i>Property:</i> <code><b>
      *	host
      *	</b></code><br>
      *	<i>Default:</i> required
      *
      * <dd style="padding-top: .5em">
      *      Specifies the host name of the snowman game server.<p>
      *
      * <dt> <i>Property:</i> <code><b>
      *	port
      *	</b></code><br>
      *	<i>Default:</i> required<br>
      *
      * <dd style="padding-top: .5em"> 
      *	Specifies the port of the snowman game server.<p>
      *
      *  <dt> <i>Property:</i> <code><b>
      *	name
      *	</b></code><br>
      *	<i>Default:</i> required<br>
      *
      *  <dd style="padding-top: .5em"> 
      *	The user name used to authenticate with the server<p>
      * 
      *  <dt> <i>Property:</i> <code><b>
      *	password
      *	</b></code><br>
      *	<i>Default:</i> null (no password)<br>
      *
      *  <dd style="padding-top: .5em"> 
      *	The user password used to authenticate with the server<p>
      * 
      * </dl> <p>
      * 
      * @param props properties to configure this player
      * @param moveDelay the minimum delay, in milliseconds, between move messages
      * @throws java.lang.Exception if a required property is missing or an
      * an error occurs communicating with the server.
      */
     public SimulatedPlayer(Properties props, int moveDelay) throws Exception {
         this.moveDelay = moveDelay;
         name = props.getProperty("name");
         if (name == null)
             throw new Exception("name property required");
         
         logger.log(Level.FINE, "New simulated player: {0}", name);
         password = props.getProperty("password", "");
         pktHandler = new ClientProcessor();
         state = PLAYERSTATE.LoggingIn;
         resetHitPoints();
         simpleClient = new SimpleClient(this);
         simpleClient.login(props);
     }
     
     private class ClientProcessor implements IClientProcessor {
 
         @Override
         public void ready() {
             if (state == PLAYERSTATE.Paused) {
                 logger.log(Level.FINE, "Ready message for {0}", name);
                 try{
                     Thread.sleep(2000); //TEMPORARY until real client sends READY message properly
                     send(ClientMessages.createReadyPkt());
                 } catch (Exception ioe) {
                     logger.log(Level.SEVERE, "" + name, ioe);
                     setState(PLAYERSTATE.Quit);
                 }
             } else
                 logger.log(Level.WARNING, "Received ready, but {0} is not paused",
                            name);
         }
 
         @Override
         public void newGame(int myID, String mapname) {
             if (state == PLAYERSTATE.Paused) {
                 logger.log(Level.FINE, "New game for {0}, id is {1}",
                            new Object[] {name, myID});
                 id = myID;
             } else
                 logger.log(Level.WARNING, "Received newGame, but {0} is not paused",
                            name);
         }
 
         @Override
         public void startGame() {
             if (setState(PLAYERSTATE.Playing)) {
                 logger.log(Level.FINE, "Start game for {0}", name);
                 lastTimestamp = System.currentTimeMillis();
             } else
                 logger.log(Level.WARNING, "Received start, but {0} has quit",
                            name);
         }
 
         @Override
         public void endGame(EEndState endState) {
             logger.log(Level.FINE,
                        "Game ended for {0} with outcome of {1}",
                        new Object[] {name, endState});
             quit();
         }
 
         @Override
         public void addMOB(int objectID, float x, float y, EMOBType objType, ETeamColor team) {
             if (objectID == id) {
                 logger.log(Level.FINE, "Updating {0} start and end XY to {1},{2}",
                            new Object[] {name, x, y});
                 myTeam = team;
                 stop(x, y);
             } else {
                 logger.log(Level.FINER, "Message to {0}: Add MOB {1}",
                        new Object[] {name, objectID});
                 // Note: saving all players. We can't check for team since
                 // myTeam may not yet been set.
                 if (objType == EMOBType.SNOWMAN)
                     players.put(objectID, team);
             }
         }
 
         @Override
         public void moveMOB(int objectID,
                             float startx, float starty,
                             float endx, float endy)
         {
             if (objectID == id) {
                 if (logger.isLoggable(Level.FINEST))
                     logger.log(Level.FINEST,
                                "Updating {0} our position to {1},{2}",
                                new Object[] {name, endx, endy});
                 //reset destination position since we don't do collision detection
                 setDestination(endx, endy);
             } else {
                 if (logger.isLoggable(Level.FINEST))
                     logger.log(Level.FINEST,
                                "Message to {0}: Move MOB {1} from {2},{3} to {4},{5}",
                                new Object[] {name, objectID, startx, starty, endx, endy});
                 
                 // If the player is on the other team, set it to be target
                 // for the next move, maybe
                 if (players.get(objectID) != myTeam && random.nextBoolean())
                     setTarget(objectID, startx, starty);
             }
         }
 
         @Override
         public void removeMOB(int objectID) {
             if (objectID == id) {
                 logger.log(Level.FINE, "Message to {0} to remove itself", name);
                 quit();
             } else {
                 logger.log(Level.FINER, "Message to {0}: Remove MOB {1}",
                            new Object[] {name, objectID});
                 players.remove(objectID);
             }
         }
 
         @Override
         public void stopMOB(int objectID, float x, float y) {
             if (objectID == id) {
                 logger.log(Level.FINER, "{0} stopped at {1},{2}",
                            new Object[] {name, x, y});
                 // stop likely caused by getting out of sync with server
                 // basicly cause a stop to start moving anew.
                 stop(x, y);
             } else
                 logger.log(Level.FINEST, "Message to {0}:, Stop MOB {1} at {2},{3}",
                            new Object[] {name, objectID, x, y});
         }
 
         @Override
         public void attachObject(int sourceID, int targetID) {
             logger.log(Level.FINER, "Message to {0}: Attach object {1} to {2}",
                        new Object[] {name, sourceID, targetID});
             // ignore
         }
 
         @Override
         public void attacked(int sourceID, int targetID, int hp) {
             logger.log(Level.FINER, "Message to {0}: {1} attacked {2}, hp= {3}",
                        new Object[] {name, sourceID, targetID, hp});
             if ((targetID == id) && (hp >= 0))
                 setHitPoints(hp);
         }
 
         public void info(int objectID, String string) {
             logger.log(Level.FINER, "Message to {0}: Info on object {1} is {2}",
                        new Object[] {name, objectID, string});    
         }
 
         @Override
         public void respawn(int objectID, float x, float y) {
             if (objectID == id) {
                 logger.log(Level.FINER, "Message to {0}: Respawn at {1},{2}",
                        new Object[] {name, x, y});
                 stop(x, y);
                 resetHitPoints();
                 setState(PLAYERSTATE.Playing);
             } else
                 logger.log(Level.FINEST, "Message to {0}: Respawn MOB {1} at {2},{3}",
                        new Object[] {name, objectID, x, y});
         }
 
         public void chatMessage(String channel, String source, String message, int id) {
             if (logger.isLoggable(Level.FINEST))
                     logger.log(Level.FINEST, "received chat from {0}: {1}",
                                new Object[] {source, message});
         }
     }
     
     // set a target player for the next move
     private synchronized void setTarget(int id, float x, float y) {
         target = new Target(id, x, y);
     }
 
     private synchronized void resetHitPoints() {
         hitPoints = HPConverter.getInstance().getMaxHP();
     }
     
     private synchronized void setHitPoints(int hp) {
         hitPoints -= hp;
         setState(hitPoints <= 0 ? PLAYERSTATE.Dead : PLAYERSTATE.Playing);
         stop();
     }
     
     // stop at a specified location
     private synchronized void stop(float x, float y) {
         this.startX = x;
         this.startY = y;
         setDestination(x, y);
     }
     
     // stop at the current position (based on current time & last start)
     private synchronized void stop() {
         setCurrentStart();
         setDestination(startX, startY);
     }
     
     // set the current end position
     private synchronized void setDestination(float endx, float endy) {
         this.destX = endx;
         this.destY = endy;
     }
         
     // must be synchronized
     private void setCurrentStart() {
 
         // if moving, calculate the new start based on the last move
         float dx = destX - startX;
         float dy = destY - startY;
         
         if (dx != 0.0f || dy != 0.0f) {
             // moves per ms
             float rate = (EForce.Movement.getMagnitude() / HPConverter.getInstance().convertMass(hitPoints)) * 0.00001f;
             float maxDist = (float)Math.sqrt((dx * dx) + (dy * dy));
             long dt = System.currentTimeMillis() - lastTimestamp;
             
             // normalize the x,y and multiply by the move per ms * ms
             float newStartX = startX + (dx / maxDist) * rate * dt;
             float newStartY = startY + (dy / maxDist) * rate * dt;
             
             // clip to destination
             dx = newStartX - startX;
             dy = newStartY - startY;
             float newDist = (float)Math.sqrt((dx * dx) + (dy * dy));
             
             if (newDist > maxDist) {
                 startX = destX;
                 startY = destY;
             } else {
                 startX = newStartX;
                 startY = newStartY;
             }
         }
     }
     
     /**
      * Make a move. A move is made only if in the {@code Playing} state
      * @return the current state
      */
     synchronized PLAYERSTATE move() throws IOException {
         if (state != PLAYERSTATE.Playing ||
             (System.currentTimeMillis() - lastTimestamp) < moveDelay)
             return state;
                 
         setCurrentStart();
         
         // if we know of a target, move towards it
         if (target != null) {
             
             // if it's within rage, attack
             float dx = startX - target.x;
             float dy = startY - target.y;
             float range = HPConverter.getInstance().convertRange(hitPoints);
             if (((dx * dx) + (dy * dy)) < (range * range)) {
                 logger.log(Level.FINER, "{0} attacking {1}",
                            new Object[] {name, target.id});
                 send(ClientMessages.createAttackPkt(target.id, startX, startY));
             }
             destX = target.x + 10 * (random.nextFloat() - 0.5f);
             destY = target.y + 10 * (random.nextFloat() - 0.5f);
             target = null;
         } else {
             destX = startX + 10 * (random.nextFloat() - 0.5f);
             destY = startY + 10 * (random.nextFloat() - 0.5f);
         }
         if (logger.isLoggable(Level.FINEST))
             logger.log(Level.FINEST, "{0} moving to {1},{2}",
                        new Object[] {name, destX, destY});
 
         // There is a slight skew potential here, since the real timestamp
         // is being set in the createMoveMePkt method. But it should be small.
         lastTimestamp = System.currentTimeMillis();
         
         // TEMP - clip to debug map
         final float maxMapCoord = 96.0f;
         if (destX > maxMapCoord) destX = maxMapCoord;
         else if (destX < 0) destX = 0;
         if (destY > maxMapCoord) destY = maxMapCoord;
         else if (destY < 0) destY = 0;
 
         // No collision detection here. We count on the returning moveMOB
         // to reset out end point if necessary.
         send(ClientMessages.createMoveMePkt(startX, startY, destX, destY));
         
         return state;
     }
 
     /**
      * Set the state of the player. Returns {@code true} if the state was
      * set, otherwise it returns {@code false}. The state can only be set
      * if the current state is not {@code PLAYERSTATE.Quit}.
      * @param newState
      * @return true if the state was set
      */
     private synchronized boolean setState(PLAYERSTATE newState) {
         if (state != PLAYERSTATE.Quit) {
             logger.log(Level.FINER, "Player: {0} state changed to {1}",
                        new Object[] {name, newState});
             state = newState;
             return true;
         } else
             return false;
     }
     
     synchronized boolean isWaiting() {
         return state == PLAYERSTATE.LoggingIn ||
                state == PLAYERSTATE.Paused;
     }
     
     /**
      * Quit the game. The player will logout from the server and will no
      * longer be able to move, i.e {@code move} will become a no-op.
      */
    synchronized void quit() {
        logger.log(Level.FINE, "Player: {0} quit", name);
        if ((state == PLAYERSTATE.Playing) ||
            (state == PLAYERSTATE.Paused)) {
             try {
                 simpleClient.logout(false);
             } catch (Exception ignore) {}
         }
        state = PLAYERSTATE.Quit;
     }
     
     private void send(ByteBuffer buff) throws IOException {
         buff.flip();
         simpleClient.send(buff);
     }
     
     /* -- SimpleClientListener -- */
     
     @Override
     public PasswordAuthentication getPasswordAuthentication() {
         return new PasswordAuthentication(name, password.toCharArray());
     }
 
     @Override
     public void loggedIn() {
         if (setState(PLAYERSTATE.Paused))
             logger.log(Level.FINE, "Player {0} logged in", name);
         else
             logger.log(Level.WARNING, "Player {0} received login after quit",
                        name);
     }
 
     @Override
     public void loginFailed(String arg0) {
         logger.log(Level.WARNING, "Login failed for {0}", name);
         quit();
     }
 
     @Override
     public ClientChannelListener joinedChannel(ClientChannel arg0) {
         return new ClientChannelListener() {
 
             @Override
             public void receivedMessage(ClientChannel channel, ByteBuffer buff) {
                 SingletonRegistry.getMessageHandler().parseClientPacket(buff, pktHandler);
             }
 
             @Override
             public void leftChannel(ClientChannel channel) {
                 logger.log(Level.FINE, "Player {0} left channel", name);
                 quit();
             }
         };
     }
     
     @Override
     public void receivedMessage(ByteBuffer buff) {
         SingletonRegistry.getMessageHandler().parseClientPacket(buff, pktHandler);
     }
 
     @Override
     public void reconnecting() {
         logger.log(Level.FINE, "Re-connecting player: {0}", name);
     }
 
     @Override
     public void reconnected() {
         logger.log(Level.FINE, "Re-connected player: {0}", name);
     }
 
     @Override
     public void disconnected(boolean graceful, String reason) {
         logger.log(Level.FINE, "Disconnected player: {0}, Reason: {1}", new Object[]{name, reason});
         quit();
     }
 }
