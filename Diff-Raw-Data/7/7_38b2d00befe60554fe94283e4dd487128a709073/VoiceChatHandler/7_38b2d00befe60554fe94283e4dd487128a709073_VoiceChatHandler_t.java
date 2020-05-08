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
 
 import org.jdesktop.wonderland.modules.orb.server.cell.Orb;
 
 import java.lang.reflect.Method;
 
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Enumeration;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Set;
 
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.CopyOnWriteArrayList;
 
 import org.jdesktop.wonderland.common.cell.CellChannelConnectionType;
 import org.jdesktop.wonderland.common.cell.CallID;
 import org.jdesktop.wonderland.common.cell.CellID;
 import org.jdesktop.wonderland.common.cell.CellTransform;
 
 import org.jdesktop.wonderland.modules.audiomanager.common.AudioManagerConnectionType;
 
 import org.jdesktop.wonderland.modules.audiomanager.common.messages.PlayerInRangeMessage;
 import org.jdesktop.wonderland.modules.audiomanager.common.messages.VoiceChatBusyMessage;
 import org.jdesktop.wonderland.modules.audiomanager.common.messages.VoiceChatEndMessage;
 import org.jdesktop.wonderland.modules.audiomanager.common.messages.VoiceChatInfoRequestMessage;
 import org.jdesktop.wonderland.modules.audiomanager.common.messages.VoiceChatInfoResponseMessage;
 import org.jdesktop.wonderland.modules.audiomanager.common.messages.VoiceChatHoldMessage;
 import org.jdesktop.wonderland.modules.audiomanager.common.messages.VoiceChatJoinMessage;
 import org.jdesktop.wonderland.modules.audiomanager.common.messages.VoiceChatJoinAcceptedMessage;
 import org.jdesktop.wonderland.modules.audiomanager.common.messages.VoiceChatLeaveMessage;
 import org.jdesktop.wonderland.modules.audiomanager.common.messages.VoiceChatMessage;
 import org.jdesktop.wonderland.modules.audiomanager.common.messages.VoiceChatMessage.ChatType;
 import org.jdesktop.wonderland.modules.audiomanager.common.messages.VoiceChatJoinRequestMessage;
 
 import org.jdesktop.wonderland.modules.presencemanager.common.PresenceInfo;
 
 import org.jdesktop.wonderland.server.WonderlandContext;
 import org.jdesktop.wonderland.server.UserManager;
 import org.jdesktop.wonderland.server.UserMO;
 
 import org.jdesktop.wonderland.server.cell.CellManagerMO;
 import org.jdesktop.wonderland.server.cell.CellMO;
 
 import org.jdesktop.wonderland.server.cell.view.AvatarCellMO;
 
 import org.jdesktop.wonderland.server.comms.CommsManager;
 import org.jdesktop.wonderland.server.comms.CommsManagerFactory;
 import org.jdesktop.wonderland.server.comms.WonderlandClientID;
 
 import com.sun.sgs.app.AppContext;
 import com.sun.sgs.app.DataManager;
 import com.sun.sgs.app.ManagedObject;
 import com.sun.sgs.app.ManagedReference;
 
 import java.util.logging.Logger;
 
 import org.jdesktop.wonderland.server.comms.WonderlandClientSender;
 
 import org.jdesktop.wonderland.common.auth.WonderlandIdentity;
 
 import com.sun.mpk20.voicelib.app.AudioGroup;
 import com.sun.mpk20.voicelib.app.AudioGroupListener;
 import com.sun.mpk20.voicelib.app.AudioGroupSetup;
 import com.sun.mpk20.voicelib.app.AudioGroupPlayerInfo;
 //import com.sun.mpk20.voicelib.app.AudioGroupPlayerInfo.ChatType;
 import com.sun.mpk20.voicelib.app.Call;
 import com.sun.mpk20.voicelib.app.DefaultSpatializer;
 import com.sun.mpk20.voicelib.app.FullVolumeSpatializer;
 import com.sun.mpk20.voicelib.app.Player;
 import com.sun.mpk20.voicelib.app.PlayerInRangeListener;
 import com.sun.mpk20.voicelib.app.PlayerSetup;
 import com.sun.mpk20.voicelib.app.VirtualPlayer;
 import com.sun.mpk20.voicelib.app.VirtualPlayerListener;
 import com.sun.mpk20.voicelib.app.VoiceManager;
 
 import java.io.IOException;
 import java.io.Serializable;
 
 import com.jme.math.Vector3f;
 
 /**
  * @author jprovino
  */
 public class VoiceChatHandler implements AudioGroupListener, VirtualPlayerListener, 
 	PlayerInRangeListener, Serializable {
 
     private static final Logger logger =
 	Logger.getLogger(VoiceChatHandler.class.getName());
     
     private static final String ORB_MAP_NAME = "VoiceChatOrbMap";
 
     private static VoiceChatHandler voiceChatHandler;
 
     public static VoiceChatHandler getInstance() {
 	if (voiceChatHandler == null) {
 	    voiceChatHandler = new VoiceChatHandler();
 	}
 
 	return voiceChatHandler;
     }
 
     class ManagedOrbMap extends ConcurrentHashMap<String, ManagedReference<Orb>> implements ManagedObject {
 
 	private static final long serialVersionUID = 1;
 
     }
 
     private VoiceChatHandler() {
 	AppContext.getDataManager().setBinding(ORB_MAP_NAME, new ManagedOrbMap());
     }
 
     public void processVoiceChatMessage(WonderlandClientSender sender, 
 	    WonderlandClientID clientID, VoiceChatMessage message) {
 
 	String group = message.getGroup();
 
 	if (message instanceof VoiceChatInfoRequestMessage) {
 	    sendVoiceChatInfo(sender, clientID, group);
 	    return;
 	}
 
 	if (message instanceof VoiceChatBusyMessage) {
 	    VoiceChatBusyMessage msg = (VoiceChatBusyMessage) message;
 
 	    CommsManager cm = CommsManagerFactory.getCommsManager();
 	    
             WonderlandClientID id = cm.getWonderlandClientID(msg.getCaller().clientID);
 
             if (id == null) {
                 logger.warning("No WonderlandClientID for caller "
                     + msg.getCaller());
                 return;
             }
 
 	    sendVoiceChatBusyMessage(sender, id, msg);
 	    return;
 	}
 
         VoiceManager vm = AppContext.getManager(VoiceManager.class);
 
 	AudioGroup audioGroup = vm.getAudioGroup(group);
 
 	if (message instanceof VoiceChatLeaveMessage) {
 	    if (audioGroup == null) {
 		logger.info("audioGroup is null");
 		return;
 	    }
 
 	    VoiceChatLeaveMessage msg = (VoiceChatLeaveMessage) message;
 
 	    Player player = vm.getPlayer(msg.getCallee().callID);
 
 	    if (player == null) {
 		logger.warning("No player for " + msg.getCallee());
 
 	        if (audioGroup.getNumberOfPlayers() == 0) {
 		    endVoiceChat(vm, audioGroup);  // cleanup
 	        }
 	        sender.send(msg);
 		return;
 	    }
 	    
 	    removePlayerFromAudioGroup(audioGroup, player);
 
 	    if (audioGroup.getNumberOfPlayers() <= 1) {
 		endVoiceChat(vm, audioGroup);
 	    } 
 
 	    sender.send(msg);
 	    vm.dump("all");
 	    return;
 	}
 
 	if (message instanceof VoiceChatEndMessage) {
 	    if (audioGroup == null) {
 		logger.info("audioGroup is null");
 		return;
 	    }
 
 	    endVoiceChat(vm, audioGroup);
 	    vm.dump("all");
 	    return;
 	}
 
 	if (message instanceof VoiceChatJoinAcceptedMessage == true) {
 	    if (audioGroup == null) {
 		logger.warning("Audio group " + group + " no longer exists");
 		return;
 	    }
 
 	    VoiceChatJoinAcceptedMessage msg = (VoiceChatJoinAcceptedMessage) message;
 
 	    addPlayerToAudioGroup(vm, audioGroup, msg.getCallee(), msg.getChatType());
 	    sender.send(msg);
 	    return;
 	}
 
 	if (message instanceof VoiceChatHoldMessage == true) {
 	    VoiceChatHoldMessage msg = (VoiceChatHoldMessage) message;
 
 	    if (audioGroup == null) {
 		logger.warning("Audio group " + group + " no longer exists");
 		return;
 	    }
 	
 	    Player player = vm.getPlayer(msg.getCallee().callID);
 
 	    if (player == null) {
 		logger.warning("No player for " + msg.getCallee().callID);
 		return;
 	    }
 
 	    AudioGroupPlayerInfo playerInfo = audioGroup.getPlayerInfo(player);
 
 	    if (playerInfo == null) {
 		logger.warning("No player info for " + player);
 		return;
 	    }
 	
 	    AudioGroup livePlayerAudioGroup = vm.getVoiceManagerParameters().livePlayerAudioGroup;
 
 	    AudioGroup stationaryPlayerAudioGroup = vm.getVoiceManagerParameters().stationaryPlayerAudioGroup;
 
 	    if (msg.isOnHold()) {
 		playerInfo.isSpeaking = false;
 		audioGroup.setSpeakingAttenuation(player, 0);
 		audioGroup.setListenAttenuation(player, 0);
 
 		livePlayerAudioGroup.setSpeakingAttenuation(player, AudioGroup.DEFAULT_SPEAKING_ATTENUATION);
 		livePlayerAudioGroup.setListenAttenuation(player, AudioGroup.DEFAULT_LISTEN_ATTENUATION);
 		stationaryPlayerAudioGroup.setSpeakingAttenuation(player, AudioGroup.DEFAULT_SPEAKING_ATTENUATION);
 		stationaryPlayerAudioGroup.setListenAttenuation(player, AudioGroup.DEFAULT_LISTEN_ATTENUATION);
 	    } else {
 		playerInfo.isSpeaking = true;
 
 		audioGroup.setSpeakingAttenuation(player, AudioGroup.DEFAULT_SPEAKING_ATTENUATION);
 		audioGroup.setListenAttenuation(player, AudioGroup.DEFAULT_LISTEN_ATTENUATION);
 
 		if (playerInfo.chatType.equals(ChatType.PUBLIC)) {
 		    livePlayerAudioGroup.setSpeakingAttenuation(player, AudioGroup.DEFAULT_SPEAKING_ATTENUATION);
 		    livePlayerAudioGroup.setListenAttenuation(player, AudioGroup.DEFAULT_LISTEN_ATTENUATION);
 		    stationaryPlayerAudioGroup.setListenAttenuation(player, AudioGroup.DEFAULT_LISTEN_ATTENUATION);
 		} else {
 		    livePlayerAudioGroup.setSpeakingAttenuation(player, 0);
 		    livePlayerAudioGroup.setListenAttenuation(player, AudioGroup.MINIMAL_LISTEN_ATTENUATION);
 		    stationaryPlayerAudioGroup.setListenAttenuation(player, AudioGroup.MINIMAL_LISTEN_ATTENUATION);
 		}
 	    }
 
 	    sender.send(msg);
 	    return;
 	}
 
 	if (message instanceof VoiceChatJoinMessage == false) {
 	    logger.warning("Invalid message type " + message);
 	    return;
 	}
 
 	VoiceChatJoinMessage msg = (VoiceChatJoinMessage) message;
 
 	if (audioGroup == null) {
 	    AudioGroupSetup setup = new AudioGroupSetup();
 	    setup.spatializer = new FullVolumeSpatializer();
 	    setup.spatializer.setAttenuator(DefaultSpatializer.DEFAULT_MAXIMUM_VOLUME);
 	    setup.virtualPlayerListener = this;
 	    setup.audioGroupListener = this;
 	    audioGroup = vm.createAudioGroup(group, setup);
 	}
 
 	PresenceInfo[] calleeList = msg.getCalleeList();
 
 	PresenceInfo caller = msg.getCaller();
 
 	boolean added = addPlayerToAudioGroup(vm, audioGroup, caller, msg.getChatType());
 
 	if (added) {
 	    sender.send(new VoiceChatJoinAcceptedMessage(group, caller, msg.getChatType()));
 	}
 
	if (added == false && (calleeList == null || calleeList.length == 0)) {
 	    endVoiceChat(vm, audioGroup);
 	    return;
 	}
 
 	logger.info("Request to join AudioGroup " + group + " caller " + caller + " phoneNumber "
 	    + msg.getPhoneNumber() + " name " + msg.getName());
 
	if (calleeList == null || calleeList.length == 0) {
	    return;
	}

 	for (int i = 0; i < calleeList.length; i++) {
 	    PresenceInfo info = calleeList[i];
 
 	    CellID cellID = calleeList[i].cellID;
 
 	    logger.fine("  callee:  " + calleeList[i]);
 
 	    String callID = calleeList[i].callID;
 
 	    Player player = vm.getPlayer(callID);
 
 	    if (player == null) {
 		logger.warning("No player for callID " + callID);
 		continue;
 	    }
 
 	    AudioGroupPlayerInfo playerInfo = audioGroup.getPlayerInfo(player);
 
 	    if (playerInfo != null && sameChatType(playerInfo.chatType, msg.getChatType())) {
 		logger.fine("Player " + info
 		    + " is already in audio group " + audioGroup);
 		continue;
 	    }
 
             WonderlandClientID id = 
 	       CommsManagerFactory.getCommsManager().getWonderlandClientID(info.clientID);
 
 	    if (id == null) {
 		logger.warning("No WonderlandClientID for " + info);
 		continue;
 	    }
 
 	    Call call = player.getCall();
 
 	    if (call != null) {
 		try {
 		    call.playTreatment("audioGroupInvite.au");
 		} catch (IOException e) {
 		    logger.warning("Unable to play audioGroupInvite.au:  "
 			+ e.getMessage());
 		}
 	    }
 
 	    logger.info("Asking " + info + " to join audio group " 
 		+ group + " chatType " + msg.getChatType());
 
 	    requestPlayerJoinAudioGroup(sender, id, group, caller,
 		calleeList, msg.getChatType());
 	}
 
 	vm.dump("all");
 	return;
     }
 
     private ConcurrentHashMap<String, PresenceInfo> playerMap = new ConcurrentHashMap();
 
     private boolean addPlayerToAudioGroup(VoiceManager vm, AudioGroup audioGroup,
 	    PresenceInfo info, ChatType chatType) {
 
 	String callID = info.callID;
 
 	Player player = vm.getPlayer(callID);
 
 	if (player == null) {
 	    logger.warning("No player for " + callID);
 	    return false;
 	}
 
 	AudioGroupPlayerInfo playerInfo = audioGroup.getPlayerInfo(player);
 
 	if (playerInfo != null) {
 	    if (sameChatType(playerInfo.chatType, chatType)) {
 	        logger.fine("Player " + info
 		    + " is already in audio group " + audioGroup);
 
 	        return true;
 	    }
 	}
 
 	audioGroup.addPlayer(player, new AudioGroupPlayerInfo(true, getChatType(chatType)));
 
 	player.addPlayerInRangeListener(this);
 
 	playerMap.put(callID, info);
 	return true;
     }
 
     private void requestPlayerJoinAudioGroup(WonderlandClientSender sender,
 	    WonderlandClientID clientID, String group, PresenceInfo caller, 
 	    PresenceInfo[] calleeList, ChatType chatType) {
 
 	VoiceChatMessage message = new VoiceChatJoinRequestMessage(group, 
 	    caller, getChatters(group), chatType);
 
         sender.send(clientID, message);
     }
 
     public void playerAdded(AudioGroup audioGroup, Player player, AudioGroupPlayerInfo info) {
 	logger.fine("Player added " + player + " group " + audioGroup);
 
 	WonderlandClientSender sender = 
 	    WonderlandContext.getCommsManager().getSender(AudioManagerConnectionType.CONNECTION_TYPE);
 
 	sendVoiceChatInfo(sender, audioGroup.getId());
     }
 
     private void sendVoiceChatBusyMessage(WonderlandClientSender sender,
 	    WonderlandClientID clientID, VoiceChatBusyMessage message) {
 
 	logger.fine(message.getCallee() + " sending busy message to " 
 	    + message.getCaller());
 
         sender.send(clientID, message);
     }
 
     public void playerRemoved(AudioGroup audioGroup, Player player, AudioGroupPlayerInfo info) {
 	logger.fine("Player removed " + player + " group " + audioGroup);
 
 	WonderlandClientSender sender = 
 	    WonderlandContext.getCommsManager().getSender(AudioManagerConnectionType.CONNECTION_TYPE);
 
 	sendVoiceChatInfo(sender, audioGroup.getId());
     }
 
     private void sendVoiceChatInfo(WonderlandClientSender sender, String group) {
 	PresenceInfo[] chatters = getChatters(group);
 
 	if (chatters == null || chatters.length == 0) {
 	    return;
 	}
 
 	CommsManager cm = CommsManagerFactory.getCommsManager();
 	    
         VoiceChatInfoResponseMessage message = new VoiceChatInfoResponseMessage(group, chatters);
 
 	for (int i = 0; i < chatters.length; i++) {
             WonderlandClientID clientID = cm.getWonderlandClientID(chatters[i].clientID);
 
 	    if (clientID == null) {
 		logger.warning("Can't find WonderlandClientID for " + chatters[i]);
 		continue;
 	    }
 
             sender.send(clientID, message);
 	}
     }
 
     private void sendVoiceChatInfo(WonderlandClientSender sender,
 	    WonderlandClientID clientID, String group) {
 
  	PresenceInfo[] chatters = getChatters(group);
 
 	if (chatters == null || chatters.length == 0) {
 	    return;
 	}
 
         sender.send(clientID, new VoiceChatInfoResponseMessage(group, chatters));
     }
 
     private PresenceInfo[] getChatters(String group) {
 	VoiceManager vm = AppContext.getManager(VoiceManager.class);
 
 	AudioGroup audioGroup = vm.getAudioGroup(group);
 
 	if (audioGroup == null) {
 	    logger.warning("Can't find audio group " + group);
 	    return null;
 	}
 
 	ArrayList<PresenceInfo> chatters = new ArrayList();
 
 	Player[] players = audioGroup.getPlayers();
 
 	for (int i = 0; i < players.length; i++) {
 	    PresenceInfo info = playerMap.get(players[i].getId());
 	
 	    if (info == null) {
 		logger.warning("Unable to find " + players[i].getId());
 		continue;
 	    }
 		
 	    chatters.add(info);
 	}
 
 	return chatters.toArray(new PresenceInfo[0]);
     }
 
     private void removePlayerFromAudioGroups(String callId) {
         VoiceManager vm = AppContext.getManager(VoiceManager.class);
 
 	Player player = vm.getPlayer(callId);
 
 	if (player == null) {
 	    logger.warning("Can't find player for callId " + callId);
 	    return;
 	}
 
 	AudioGroup[] audioGroups = player.getAudioGroups().toArray(new AudioGroup[0]);
 
 	for (int i = 0; i < audioGroups.length; i++) {
 	    removePlayerFromAudioGroup(audioGroups[i], player);
 	}
     }
 
     private void removePlayerFromAudioGroup(AudioGroup audioGroup, 
 	    Player player) {
 
 	player.removePlayerInRangeListener(this);
 
 	audioGroup.removePlayer(player);
 
 	// XXX If a player can be in more than one public audio group
 	// then the player must have a separate list of virtual calls
 	// for each audio group.
     }
 
     private void endVoiceChat(VoiceManager vm, AudioGroup audioGroup) {
 	Player[] players = audioGroup.getPlayers();
 
 	for (int i = 0; i < players.length; i++) {
 	    Player player = players[i];
 	    
 	    removePlayerFromAudioGroup(audioGroup, player);
 	}
 
 	vm.removeAudioGroup(audioGroup);
     }
 
     private static final String VIRTUAL_PLAYER_PREFIX = "V-";
 
     public void virtualPlayerAdded(AudioGroup audioGroup, VirtualPlayer vp) {
 	Vector3f center = new Vector3f((float) vp.player.getX(), (float) 2.3, 
 	    (float) vp.player.getZ());
 
         ManagedOrbMap orbMap = (ManagedOrbMap) AppContext.getDataManager().getBinding(ORB_MAP_NAME);
 
 	if (orbMap.get(VIRTUAL_PLAYER_PREFIX + vp.player.getId()) != null) {
 	    logger.info("ORB already exists for " + vp);
 	    return;
 	}
 
 	Orb orb = new Orb(VIRTUAL_PLAYER_PREFIX + vp.getUsername(), 
 	    vp.player.getCall().getId(), center, .1, false, vp.playerWithVirtualPlayer.getId());
 
 	orb.addComponent(new AudioParticipantComponentMO(orb.getOrbCellMO()));
 
 	orbMap.put(VIRTUAL_PLAYER_PREFIX + vp.player.getId(), 
 	    AppContext.getDataManager().createReference(orb));
     }
 
     public void virtualPlayersRemoved(AudioGroup audioGroup, VirtualPlayer[] virtualPlayers) {
 	for (int i = 0; i < virtualPlayers.length; i++) {
 	    VirtualPlayer vp = virtualPlayers[i];
 
             ManagedOrbMap orbMap = (ManagedOrbMap) AppContext.getDataManager().getBinding(ORB_MAP_NAME);
 
 	    ManagedReference<Orb> orbRef = orbMap.remove(VIRTUAL_PLAYER_PREFIX + vp.player.getId());
 
 	    if (orbRef != null) {
 	        orbRef.get().done();
 	    }
 	}
     }
 
     private void moveVirtualPlayers(Player player, double x, double y, double z, 
 	    double direction) {
 
     }
 
     /*
      * XXX sameChatType() getChatType() are here because the voicelib is not accessible to
      * common and client code so VoiceChatMessages have their own enum for ChatType.
      */
     private boolean sameChatType(AudioGroupPlayerInfo.ChatType playerChatType, ChatType chatType) {
 	if (playerChatType == AudioGroupPlayerInfo.ChatType.PUBLIC && chatType == ChatType.PUBLIC) {
 	    return true;
 	}
 
 	if (playerChatType == AudioGroupPlayerInfo.ChatType.PRIVATE && chatType == ChatType.PRIVATE) {
 	    return true;
 	}
 
 	if (playerChatType == AudioGroupPlayerInfo.ChatType.SECRET && chatType == ChatType.SECRET) {
 	    return true;
 	}
 
 	if (playerChatType == AudioGroupPlayerInfo.ChatType.EXCLUSIVE && chatType == ChatType.EXCLUSIVE) {
 	    return true;
 	}
 
 	return false;
     }
 
     private AudioGroupPlayerInfo.ChatType getChatType(ChatType chatType) {
 	if (chatType == ChatType.PRIVATE) {
 	    return AudioGroupPlayerInfo.ChatType.PRIVATE;
 	}
 
 	if (chatType == ChatType.SECRET) {
 	    return AudioGroupPlayerInfo.ChatType.SECRET;
 	}
 
 	if (chatType == ChatType.EXCLUSIVE) {
 	    return AudioGroupPlayerInfo.ChatType.EXCLUSIVE;
 	}
 
 	return AudioGroupPlayerInfo.ChatType.PUBLIC;
     }
 
     private ConcurrentHashMap<Player, CopyOnWriteArrayList<Player>> playersInRangeMap =
 	new ConcurrentHashMap();
 
     private ConcurrentHashMap<Player, Orb> bystanderOrbMap = new ConcurrentHashMap();
 
     public void playerInRange(Player player, Player playerInRange, boolean isInRange) {
 	CopyOnWriteArrayList<Player> playersInRange = playersInRangeMap.get(player);
 
 	logger.fine("Player in range " + isInRange + " " + player
 	    + " player in range " + playerInRange);
 
 	if (isInRange) {
 	    if (playersInRange == null) {
 		playersInRange = new CopyOnWriteArrayList();
 	
 		playersInRangeMap.put(player, playersInRange);
 	    }
 
    	    playersInRange.add(playerInRange);
 
 	    Orb orb = bystanderOrbMap.get(player);
 
 	    if (false && orb == null) {
         	VoiceManager vm = AppContext.getManager(VoiceManager.class);
 
 		Player vPlayer = vm.getPlayer(VIRTUAL_PLAYER_PREFIX + player.getId());
 
 		if (vPlayer == null) {
 		    return;
 		}
 
 		//Vector3f center = new Vector3f((float) vp.player.getX(), (float) 2.3,
 		//    (float) vp.player.getZ());
 
 		//orb = new Orb(String.valueOf(playersInRange.size()), vPlayer.player.getCall().getId(),
 		//    center, .1, false, vPlayer.playerWithVirtualPlayer.getId());
 	    }
 	    
 	    // Update Orb name tag with count of players in range	
 	} else {
 	    if (playersInRange == null) {
 		return;
 	    }
 
 	    playersInRangeMap.remove(playerInRange);
 
 	    if (playersInRange.size() == 0) {
 		playersInRangeMap.remove(player);
 	    }
 
 	    // XXX decide whether or not a bystander orb needs to be removed or updated
 	    // with the bystander count.
 	}
 
 	WonderlandClientSender sender = 
 	    WonderlandContext.getCommsManager().getSender(AudioManagerConnectionType.CONNECTION_TYPE);
 
 	sender.send(new PlayerInRangeMessage(player.getId(), playerInRange.getId(), isInRange));
     }
 
 }
