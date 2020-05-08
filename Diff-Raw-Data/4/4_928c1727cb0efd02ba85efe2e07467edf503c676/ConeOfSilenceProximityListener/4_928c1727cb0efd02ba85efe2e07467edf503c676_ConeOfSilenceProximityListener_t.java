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
 
 import org.jdesktop.wonderland.modules.presencemanager.common.PresenceInfo;
 
 import org.jdesktop.wonderland.modules.audiomanager.common.messages.ConeOfSilenceEnterExitMessage;
 
 import com.sun.mpk20.voicelib.app.AudioGroup;
 import com.sun.mpk20.voicelib.app.AudioGroupListener;
 import com.sun.mpk20.voicelib.app.AudioGroupPlayerInfo;
 import com.sun.mpk20.voicelib.app.AudioGroupSetup;
 import com.sun.mpk20.voicelib.app.DefaultSpatializer;
 import com.sun.mpk20.voicelib.app.FullVolumeSpatializer;
 import com.sun.mpk20.voicelib.app.Player;
 import com.sun.mpk20.voicelib.app.VoiceManager;
 import com.sun.sgs.app.AppContext;
 import java.util.logging.Logger;
 import org.jdesktop.wonderland.common.cell.CallID;
 import org.jdesktop.wonderland.common.cell.CellChannelConnectionType;
 import org.jdesktop.wonderland.common.cell.CellID;
 import org.jdesktop.wonderland.server.WonderlandContext;
 import org.jdesktop.wonderland.server.cell.CellMO;
 import org.jdesktop.wonderland.server.cell.ProximityListenerSrv;
 import org.jdesktop.wonderland.server.comms.WonderlandClientSender;
 import com.jme.bounding.BoundingVolume;
 
 import org.jdesktop.wonderland.modules.audiomanager.common.AudioManagerConnectionType;
 import org.jdesktop.wonderland.modules.audiomanager.common.VolumeUtil;
 
 import java.io.Serializable;
 
 /**
  * A server cell that provides conference coneofsilence functionality
  * @author jprovino
  */
 public class ConeOfSilenceProximityListener implements ProximityListenerSrv, 
 	AudioGroupListener, Serializable {
 
     private static final Logger logger =
             Logger.getLogger(ConeOfSilenceProximityListener.class.getName());
 
     private CellID cellID;
     private String callID;
     private String name;
     private double outsideAudioVolume;
     private boolean entered;
 
     public ConeOfSilenceProximityListener(CellMO cellMO, String name, double outsideAudioVolume) {
 	cellID = cellMO.getCellID();
         this.name = name;
 	this.outsideAudioVolume = outsideAudioVolume;
     }
 
     public void viewEnterExit(boolean entered, CellID cellID,
             CellID viewCellID, BoundingVolume proximityVolume,
             int proximityIndex) {
 
 	logger.info("viewEnterExit:  " + entered + " cellID " + cellID
 	    + " viewCellID " + viewCellID);
 
 	this.entered = entered;
 	this.callID = CallID.getCallID(viewCellID);
 
 	if (entered) {
 	    cellEntered(viewCellID);
 	} else {
 	    cellExited(viewCellID);
 	}
     }
 
     private void cellEntered(CellID softphoneCellID) {
         cellEntered(CallID.getCallID(softphoneCellID));
     }
 
     public void cellEntered(String callId) {
         /*
          * The avatar has entered the ConeOfSilence cell.
          * Set the public and incoming spatializers for the avatar to be
          * the zero volume spatializer.
          * Set a private spatializer for the given fullVolume radius
          * for all the other avatars in the cell.
          * For each avatar already in the cell, set a private spatializer
          * for this avatar.
          */
         logger.info(callId + " entered cone " + name + " avatar cell ID " + callId);
 
         VoiceManager vm = AppContext.getManager(VoiceManager.class);
 
         Player player = vm.getPlayer(callId);
 
         if (player == null) {
             logger.warning("Can't find player for " + callId);
             return;
         }
 
         AudioGroup audioGroup = vm.getAudioGroup(name);
 
         if (audioGroup == null) {
             AudioGroupSetup ags = new AudioGroupSetup();
 
 	    ags.audioGroupListener = this;
 
             ags.spatializer = new FullVolumeSpatializer();
 
             ags.spatializer.setAttenuator(
                     DefaultSpatializer.DEFAULT_MAXIMUM_VOLUME);
 
             audioGroup = vm.createAudioGroup(name, ags);
         }
 
         audioGroup.addPlayer(player, new AudioGroupPlayerInfo(true,
        	    AudioGroupPlayerInfo.ChatType.SECRET));
 
 	logger.fine("Attenuate other groups to " + outsideAudioVolume + " name " + name);
 
 	WonderlandClientSender sender =
             WonderlandContext.getCommsManager().getSender(AudioManagerConnectionType.CONNECTION_TYPE);
 
 	sender.send(new ConeOfSilenceEnterExitMessage(callId, true));
     }
 
     public void playerAdded(AudioGroup audioGroup, Player player, AudioGroupPlayerInfo info) {
 	player.attenuateOtherGroups(audioGroup, 0, outsideAudioVolume);
     }
 
     private void cellExited(CellID softphoneCellID) {
         cellExited(CallID.getCallID(softphoneCellID));
     }
 
     public void cellExited(String callId) {
         logger.info(callId + " exited cone " + name + " avatar cell ID " + callId);
 
         VoiceManager vm = AppContext.getManager(VoiceManager.class);
 
         AudioGroup audioGroup = vm.getAudioGroup(name);
 
         if (audioGroup == null) {
             logger.warning("Not a member of audio group " + name);
             return;
         }
 
         Player player = vm.getPlayer(callId);
 
         if (player == null) {
             logger.warning("Can't find player for " + callId);
             return;
         }
 
         audioGroup.removePlayer(player);
 
 	WonderlandClientSender sender =
             WonderlandContext.getCommsManager().getSender(AudioManagerConnectionType.CONNECTION_TYPE);
 
 	sender.send(new ConeOfSilenceEnterExitMessage(callId, false));
     }
 
     public void playerRemoved(AudioGroup audioGroup, Player player, AudioGroupPlayerInfo info) {
         if (audioGroup.getNumberOfPlayers() == 0) {
             AppContext.getManager(VoiceManager.class).removeAudioGroup(audioGroup);
         }
 
 	VoiceChatHandler.updateAttenuation(player);
 
 	if (entered) {
 	    entered = false;
 
 	    WonderlandClientSender sender =
                 WonderlandContext.getCommsManager().getSender(AudioManagerConnectionType.CONNECTION_TYPE);
 
 	    sender.send(new ConeOfSilenceEnterExitMessage(callID, false));
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
