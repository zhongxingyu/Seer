 /**
  * Project Wonderland
  *
  * Copyright (c) 2004-2009, Sun Microsystems, Inc., All Rights Reserved
  *
  * Redistributions in source code form must reproduce the above
  * copyright and this condition.
  *
  * The contents of this file are subject to the GNU General Public
  * License, Version 2 (the "License"); you may not use this file
  * except in compliance with the License. A copy of the License is
  * available at http://www.opensource.org/licenses/gpl-license.php.
  *
  * Sun designates this particular file as subject to the "Classpath" 
  * exception as provided by Sun in the License file that accompanied 
  * this code.
  */
 package org.jdesktop.wonderland.modules.audiomanager.server;
 
 import com.sun.mpk20.voicelib.app.AudioGroup;
 import com.sun.mpk20.voicelib.app.AudioGroupListener;
 import com.sun.mpk20.voicelib.app.AudioGroupPlayerInfo;
 import com.sun.mpk20.voicelib.app.AudioGroupSetup;
 import com.sun.mpk20.voicelib.app.DefaultSpatializer;
 import com.sun.mpk20.voicelib.app.FullVolumeSpatializer;
 import com.sun.mpk20.voicelib.app.Player;
 import com.sun.mpk20.voicelib.app.VoiceManager;
 
 import com.sun.sgs.app.AppContext;
 
 
 import java.lang.String;
 
 import java.util.logging.Logger;
 
 import org.jdesktop.wonderland.common.cell.CallID;
 import org.jdesktop.wonderland.common.cell.CellID;
 
 import org.jdesktop.wonderland.server.cell.CellMO;
 import org.jdesktop.wonderland.server.cell.ProximityListenerSrv;
 
 import com.jme.bounding.BoundingVolume;
 
 import java.io.Serializable;
 
 /**
  * A server cell that provides a microphone proximity listener
  * @author jprovino
  */
 public class MicrophoneActiveAreaProximityListener implements ProximityListenerSrv, Serializable {
 
     private static final Logger logger =
            Logger.getLogger(MicrophoneProximityListener.class.getName());
 
     private CellID cellID;
     private String name;
     private double volume;
 
     public MicrophoneActiveAreaProximityListener(CellMO cellMO, String name, double volume) {
 	cellID = cellMO.getCellID();
         this.name = name;
 	this.volume = volume;
     }
 
     public void viewEnterExit(boolean entered, CellID cellID,
             CellID viewCellID, BoundingVolume proximityVolume,
             int proximityIndex) {
 
 	logger.info("viewEnterExit:  " + entered + " cellID " + cellID
 	    + " viewCellID " + viewCellID);
 
 	String callId = CallID.getCallID(viewCellID);
 
 	if (entered) {
 	    activeAreaEntered(callId);
 	} else {
 	    activeAreaExited(callId);
 	}
     }
 
     private void activeAreaEntered(String callId) {
 	logger.info(callId + " entered active area " + name);
 
         VoiceManager vm = AppContext.getManager(VoiceManager.class);
 
         Player player = vm.getPlayer(callId);
 
         if (player == null) {
             logger.warning("Can't find player for " + callId);
             return;
         }
 
         AudioGroup audioGroup = vm.getAudioGroup(name);
 
         if (audioGroup == null) {
 	    logger.warning("Can't find audio group " + name);
 	    return;
 	}
 
 	if (audioGroup.getPlayerInfo(player) == null) {
             audioGroup.addPlayer(player, new AudioGroupPlayerInfo(true,
                 AudioGroupPlayerInfo.ChatType.PUBLIC));
 	} else {
 	    audioGroup.setSpeaking(player, true);
 	}
 
 	audioGroup.setSpeakingAttenuation(player, volume);
     }
 
     private void activeAreaExited(String callId) {
 	logger.info(callId + " exited active area " + name);
 
         VoiceManager vm = AppContext.getManager(VoiceManager.class);
 
         Player player = vm.getPlayer(callId);
 
         if (player == null) {
             logger.warning("Can't find player for " + callId);
             return;
         }
 
         AudioGroup audioGroup = vm.getAudioGroup(name);
 
         if (audioGroup == null) {
 	    logger.warning("Can't find audio group " + name);
 	    return;
 	}
 
 	audioGroup.setSpeaking(player, false);
 	audioGroup.setSpeakingAttenuation(player, volume);
     }
 
     public void changeName(String name) {
 	if (this.name.equals(name)) {
 	    return;
 	}
 
         VoiceManager vm = AppContext.getManager(VoiceManager.class);
 
         AudioGroup audioGroup = vm.getAudioGroup(name);
 
 	this.name = name;
 
 	AudioGroupSetup ags = new AudioGroupSetup();
 
         ags.spatializer = new FullVolumeSpatializer();
 
         ags.spatializer.setAttenuator(
             DefaultSpatializer.DEFAULT_MAXIMUM_VOLUME);
 
         AudioGroup newAudioGroup = vm.createAudioGroup(name, ags);
 
         if (audioGroup != null) {
 	    Player[] players = audioGroup.getPlayers();
 
 	    for (int i = 0; i < players.length; i++) {
 		AudioGroupPlayerInfo info = audioGroup.getPlayerInfo(players[i]);
 
 		audioGroup.removePlayer(players[i]);
 		newAudioGroup.addPlayer(players[i], info);
 	    }
 	}
     }
 
     public void remove() {
         VoiceManager vm = AppContext.getManager(VoiceManager.class);
 
         AudioGroup audioGroup = vm.getAudioGroup(name);
 
         if (audioGroup == null) {
             return;
         }
 
         vm.removeAudioGroup(audioGroup);
     }
 
 }
