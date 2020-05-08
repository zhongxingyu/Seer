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
 
 package com.sun.darkstar.example.snowman.server;
 
 import com.sun.darkstar.example.snowman.common.util.SingletonRegistry;
 import com.sun.darkstar.example.snowman.server.interfaces.SnowmanPlayer;
 import com.sun.darkstar.example.snowman.server.interfaces.Matchmaker;
 import com.sun.darkstar.example.snowman.server.interfaces.EntityFactory;
 import com.sun.darkstar.example.snowman.server.context.SnowmanAppContext;
 import com.sun.sgs.app.ClientSessionListener;
 import com.sun.sgs.app.ClientSession;
 import com.sun.sgs.app.ManagedReference;
 import com.sun.sgs.app.ManagedObject;
 import com.sun.sgs.app.ObjectNotFoundException;
 import java.nio.ByteBuffer;
 import java.io.Serializable;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  * The <code>SnowmanPlayerListener</code> is responsible for receiving
  * incoming messages and forwarding them to the appropriate 
  * {@link SnowmanPlayer} for processing.
  * 
  * @author Owen Kellett
  */
 public class SnowmanPlayerListener implements ManagedObject, Serializable,
         ClientSessionListener
 {
     private static Logger logger = Logger.getLogger(SnowmanPlayerListener.class.getName());
     public static final long serialVersionUID = 1L;
     
     private final ManagedReference<SnowmanPlayer> playerRef;
     private ManagedReference<Matchmaker> matchmakerRef;
     
     /**
      * Lookup the SnowmanPlayerListener in the datastore.  If it is not
      * there, create a new one and return it
      * @param session ClientSession of the connecting client
      * @param appContext the application context
      * @param entityFactory factory used to create a SnowmanPlayer if necessary
      * @param matchmaker the matchmaker requesting the player
      * @return
      */
     public static SnowmanPlayerListener find(ClientSession session,
                                              SnowmanAppContext appContext,
                                              EntityFactory entityFactory,
                                              Matchmaker matchmaker) {
         return new SnowmanPlayerListener(appContext,
                                          entityFactory.createSnowmanPlayer(appContext, session),
                                          matchmaker);
     }
     
     protected SnowmanPlayerListener(SnowmanAppContext appContext,
                                     SnowmanPlayer player,
                                     Matchmaker matchmaker)
     {
         playerRef = appContext.getDataManager().createReference(player);
         matchmakerRef = appContext.getDataManager().createReference(matchmaker);
     }
     
     public void receivedMessage(ByteBuffer arg0) {
        SingletonRegistry.getMessageHandler().parseServerPacket(arg0,playerRef.get().getProcessor());
     }
 
     public void disconnected(boolean arg0) {
         SnowmanPlayer player;
         try {
             player = playerRef.get();
         } catch (ObjectNotFoundException ex) {
             return;
         }
        if (player == null)
            return;
         
         if (logger.isLoggable(Level.FINE))
             logger.log(Level.FINE,
                        "Player {0} logged out", player.getName());
         
         if (matchmakerRef!=null){
             matchmakerRef.get().removeWaitingPlayer(player);
             matchmakerRef = null;
         }
         if (player.getGame() != null) {
             player.getGame().removePlayer(player);
         }
     }
     
     public SnowmanPlayer getSnowmanPlayer() {
         return playerRef.get();
     }
 }
