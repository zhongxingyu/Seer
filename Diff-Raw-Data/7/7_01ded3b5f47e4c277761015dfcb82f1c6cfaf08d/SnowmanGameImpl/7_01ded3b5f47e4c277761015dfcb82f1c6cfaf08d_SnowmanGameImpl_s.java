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
 
 package com.sun.darkstar.example.snowman.server.impl;
 
 import com.sun.darkstar.example.snowman.common.protocol.enumn.EEndState;
 import com.sun.darkstar.example.snowman.common.protocol.enumn.EMOBType;
 import com.sun.darkstar.example.snowman.common.protocol.enumn.ETeamColor;
 import com.sun.darkstar.example.snowman.common.protocol.messages.ServerMessages;
 import com.sun.darkstar.example.snowman.common.util.Coordinate;
 import com.sun.darkstar.example.snowman.server.context.SnowmanAppContext;
 import com.sun.darkstar.example.snowman.server.exceptions.SnowmanFullException;
 import com.sun.darkstar.example.snowman.server.interfaces.EntityFactory;
 import com.sun.darkstar.example.snowman.server.interfaces.SnowmanFlag;
 import com.sun.darkstar.example.snowman.server.interfaces.SnowmanGame;
 import com.sun.darkstar.example.snowman.server.interfaces.SnowmanPlayer;
 import com.sun.sgs.app.AppContext;
 import com.sun.sgs.app.Channel;
 import com.sun.sgs.app.Delivery;
 import com.sun.sgs.app.ManagedReference;
 import com.sun.sgs.app.Task;
 import com.sun.sgs.app.ObjectNotFoundException;
 import com.sun.sgs.app.util.ScalableHashMap;
 import java.io.Serializable;
 import java.nio.ByteBuffer;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Set;
 import java.util.logging.Logger;
 
 
 /**
  * This object represents an actual running game session of Project Snowman,
  * @author Jeffrey Kesselman
  * @author Owen Kellett
  */
 public class SnowmanGameImpl implements SnowmanGame, Serializable
 {
     public static final long serialVersionUID = 1L;
     
     protected static Logger logger = Logger.getLogger(SnowmanGameImpl.class.getName());
     /**
      * A prefix that is appended to the darkstar bound name for
      * all game channels.
      */
     public static final String CHANPREFIX = "_GAMECHAN_";
     
     private static final int PLAYERIDSTART = 1;
     private static final int CLEANUPDELAYMS = 5*1000;
     
     /**
      * A reference to a channel that is used to send game packets to
      * all the players in this game session
      */
     private final ManagedReference<Channel> channelRef;
     
     private int numPlayers;
     private int realPlayers = 0;
     private int readyPlayers = 0;
     private int nextPlayerId = PLAYERIDSTART;
     private String gameName;
     /**
      * List of flags in the game
      */
     private final Map< Integer, ManagedReference<SnowmanFlag>> flagRefs;
     /**
      * Map of player IDs to players that are part of this game
      */
     private final Map<Integer, ManagedReference<SnowmanPlayer>>  playerRefs;
     
     private final SnowmanAppContext appContext;
     private final EntityFactory entityFactory;
     
     /**
      * Keeps track of how many players from each team have joined the game
      */
     private int[] teamPlayers = new int[ETeamColor.values().length];
     /**
      * Keeps track of the maximum number of players per team
      */
     private int[] maxTeamPlayers = new int[ETeamColor.values().length];
     
     public SnowmanGameImpl(String gameName,
                            int numPlayers,
                            SnowmanAppContext appContext,
                            EntityFactory entityFactory)
     {
         this.gameName = gameName;
         this.numPlayers = numPlayers;
         initMaxTeamPlayers();
         
         this.flagRefs = new HashMap< Integer, ManagedReference<SnowmanFlag>>(ETeamColor.values().length);
         this.playerRefs = new HashMap<Integer, ManagedReference<SnowmanPlayer>>(numPlayers);
 
         this.appContext = appContext;
         this.entityFactory = entityFactory;
         this.channelRef = appContext.getDataManager().createReference(
                 appContext.getChannelManager().createChannel(
                 CHANPREFIX+gameName, null, Delivery.RELIABLE));
         
         initFlags();
     }
     
     /**
      * Initialize the maximum number of players per team
      * The total number of players is divided up evenly among the teams.
      * If the division is not even, the last team will have the remainder
      */
     private void initMaxTeamPlayers() {
         //special case when there are fewer players than teams
         if(this.numPlayers < ETeamColor.values().length) {
             for(int j = 0; j < maxTeamPlayers.length; j++) {
                 if(j < numPlayers) maxTeamPlayers[j] = 1;
                 else maxTeamPlayers[j] = 0;
             }
             return;
         }
         
         int playersPerTeam = this.numPlayers / ETeamColor.values().length;
         int remainder = this.numPlayers % ETeamColor.values().length;
         for (int i = 0; i < maxTeamPlayers.length - 1; i++)
             maxTeamPlayers[i] = playersPerTeam;
 
         if(remainder == 0)
             maxTeamPlayers[maxTeamPlayers.length-1]=playersPerTeam;
         else
             maxTeamPlayers[maxTeamPlayers.length-1]=remainder;
     }
     
     /**
      * Initialize the list of flags and their locations
      */
     private void initFlags() {
         for(ETeamColor color : ETeamColor.values()){
             Coordinate flagStart = SnowmanMapInfo.getFlagStart(SnowmanMapInfo.DEFAULT, color);
             Coordinate flagGoal = SnowmanMapInfo.getFlagGoal(SnowmanMapInfo.DEFAULT, color);
             SnowmanFlag flag =
                         entityFactory.createSnowmanFlag(this,
                                                         color,
                                                         flagStart,
                                                         flagGoal);
             flag.setLocation(flagStart.getX(), flagStart.getY());
             ManagedReference<SnowmanFlag> ref =
                         appContext.getDataManager().createReference(flag);
             flagRefs.put(flag.getID(), ref);
         }
     }
     
     public void send(ByteBuffer buff){
         buff.flip();
         channelRef.get().send(null, buff);
     }
 
     public void addPlayer(SnowmanPlayer player, ETeamColor color) {
         appContext.getDataManager().markForUpdate(this);
         //ensure we are not going over the limit
         if(teamPlayers[color.ordinal()] == maxTeamPlayers[color.ordinal()]) {
             throw new SnowmanFullException("Player "+player.getName()+" cannot be added to game "+
                     this.getName()+" : too many players");
         }
         
         //get a reference to the player and add to the list
         ManagedReference<SnowmanPlayer> playerRef = 
                 appContext.getDataManager().createReference(player);
         Integer playerId = new Integer(nextPlayerId++);
         playerRefs.put(playerId, playerRef);
         
         //increment the total team players in this game
         teamPlayers[color.ordinal()]++;
         
         //update player information
         player.setID(playerId.intValue());
         Coordinate position = SnowmanMapInfo.getSpawnPosition(SnowmanMapInfo.DEFAULT,
                                                               color,
                                                               teamPlayers[color.ordinal()],
                                                               maxTeamPlayers[color.ordinal()]);
         player.setLocation(position.getX(), position.getY());
         player.setTeamColor(color);
         player.setGame(this);
         
         //add the real players session to the channel
         if (player.getSession() != null) {
             realPlayers++;
             channelRef.get().join(player.getSession());
         }
     }
     
     // A player disconnected
     public void removePlayer(SnowmanPlayer player){
         appContext.getDataManager().markForUpdate(this);
         player.dropFlag();
         ManagedReference<SnowmanPlayer> playerRef = playerRefs.remove(player.getID());
         Channel channel = channelRef.get();
         if (player.getSession() != null) {
             realPlayers--;
             channel.leave(player.getSession());
         }
         
         // if all real players have gone, end the game
         if (!channel.hasSessions())
             endGame(EEndState.Draw);
         else {
             send(ServerMessages.createRemoveMOBPkt(player.getID()));
         }
     }
     
     public void sendMapInfo(){
         for(ManagedReference<SnowmanPlayer> ref : playerRefs.values()){
             SnowmanPlayer player = ref.get();
             if (player.getSession() != null) {
                 player.send(ServerMessages.createNewGamePkt(player.getID(),
                                                             "default_map"));
             }
         }
         for(ManagedReference<SnowmanPlayer> ref : playerRefs.values()){
             SnowmanPlayer player = ref.get();
             multiSend(ServerMessages.createAddMOBPkt(
                     player.getID(), player.getX(), player.getY(), 
                     EMOBType.SNOWMAN, player.getTeamColor()));
         }
         for(ManagedReference<SnowmanFlag> flagRef : flagRefs.values()){
             SnowmanFlag flag = flagRef.get();
             multiSend(ServerMessages.createAddMOBPkt(
                     flag.getID(), flag.getX(), flag.getY(), EMOBType.FLAG, flag.getTeamColor()));
             
             //TODO - encode goal color in the flag
             //currently the add mob should swap the goal colors so that it is more intuitive for the players
             multiSend(ServerMessages.createAddMOBPkt(
                     flag.getID()+flagRefs.size(), flag.getGoalX(), flag.getGoalY(), EMOBType.FLAGGOAL, 
                     flag.getTeamColor() == ETeamColor.Red ? ETeamColor.Blue : ETeamColor.Red));
         }
         multiSend(ServerMessages.createReadyPkt());
     }
     
     private void multiSend(ByteBuffer buff){
    	 for(ManagedReference<SnowmanPlayer> ref : playerRefs.values()){
    		 ref.get().send(buff);
    	 }
     }
     
     public void startGameIfReady(){
         appContext.getDataManager().markForUpdate(this);
         readyPlayers++;
         if(readyPlayers >= realPlayers)
             send(ServerMessages.createStartGamePkt());
     }
     
     public Set<Integer> getPlayerIds() {
         return playerRefs.keySet();
     }
     
     public SnowmanPlayer getPlayer(int id){
         ManagedReference<SnowmanPlayer> playerRef = playerRefs.get(new Integer(id));
         if(playerRef != null)
             return playerRef.get();
         return null;
     }
     
     public void endGame(EEndState endState) {
         send(ServerMessages.createEndGamePkt(endState));
         
         // Attempt to clean up the game objects, including the channel, later
         // so that the EndGame message is sent ASAP
         appContext.getTaskManager().scheduleTask(
                 new ObjectRemovalTask(appContext.getDataManager().createReference(this)),
                 CLEANUPDELAYMS);
     }
   
     static private class ObjectRemovalTask implements Task, Serializable {
         final ManagedReference ref;
         
         ObjectRemovalTask(ManagedReference ref) {
             this.ref = ref;
         }
 
         public void run() throws Exception {
             try {
                 AppContext.getDataManager().removeObject(ref.get());
             } catch (ObjectNotFoundException alreadyRemoved) {}
         }
     }
     
     public void removingObject() {
         Channel c = getChannel();
         c.leaveAll();
         appContext.getDataManager().removeObject(c);
         
         //only remove server side robots
         //player listener is responsible for cleaning up client
         //connected players
         for (ManagedReference<SnowmanPlayer> ref : playerRefs.values()) {
             try {
                 SnowmanPlayer p = ref.get();
                 if(p.isServerSide())
                     appContext.getDataManager().removeObject(p);
             } catch (ObjectNotFoundException alreadyRemoved) {}
         }
             
         for (ManagedReference<SnowmanFlag> ref : flagRefs.values()) {
             appContext.getDataManager().removeObject(ref.get());
         }
     }
 
     public Set<Integer> getFLagIds() {
         return flagRefs.keySet();
     }
     
     public SnowmanFlag getFlag(int id) {
         return flagRefs.get(id).get();
     }
     
     public String getName() {
         return gameName;
     }
 
     public Channel getChannel() {
         return channelRef.get();
     }
 }
