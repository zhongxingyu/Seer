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
 package org.jdesktop.wonderland.modules.orb.client.cell;
 
 import org.jdesktop.wonderland.client.softphone.SoftphoneControlImpl;
 
 import org.jdesktop.wonderland.modules.presencemanager.client.PresenceManager;
 import org.jdesktop.wonderland.modules.presencemanager.client.PresenceManagerFactory;
 
 import org.jdesktop.wonderland.modules.presencemanager.common.PresenceInfo;
 
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import org.jdesktop.wonderland.client.ClientContext;
 
 import org.jdesktop.wonderland.client.cell.ChannelComponent;
 import org.jdesktop.wonderland.client.cell.Cell;
 import org.jdesktop.wonderland.client.cell.MovableComponent;
 
 import org.jdesktop.wonderland.client.cell.view.LocalAvatar;
 
 import org.jdesktop.wonderland.common.auth.WonderlandIdentity;
 
 import org.jdesktop.wonderland.common.cell.CellID;
 import org.jdesktop.wonderland.common.cell.CellTransform;
 
 import org.jdesktop.wonderland.common.cell.messages.CellMessage;
 
 import org.jdesktop.wonderland.common.messages.Message;
 
 import org.jdesktop.wonderland.modules.orb.common.messages.OrbAttachMessage;
 import org.jdesktop.wonderland.modules.orb.common.messages.OrbAttachVirtualPlayerMessage;
 import org.jdesktop.wonderland.modules.orb.common.messages.OrbBystandersMessage;
 import org.jdesktop.wonderland.modules.orb.common.messages.OrbChangeNameMessage;
 import org.jdesktop.wonderland.modules.orb.common.messages.OrbChangePositionMessage;
 import org.jdesktop.wonderland.modules.orb.common.messages.OrbEndCallMessage;
 import org.jdesktop.wonderland.modules.orb.common.messages.OrbMuteCallMessage;
 import org.jdesktop.wonderland.modules.orb.common.messages.OrbSetVolumeMessage;
 import org.jdesktop.wonderland.modules.orb.common.messages.OrbSpeakingMessage;
 
 import org.jdesktop.wonderland.modules.avatarbase.client.jme.cellrenderer.NameTagComponent;
 import org.jdesktop.wonderland.modules.avatarbase.client.jme.cellrenderer.NameTagNode;
 import org.jdesktop.wonderland.modules.avatarbase.client.jme.cellrenderer.NameTagNode.EventType;
 
 import org.jdesktop.wonderland.client.comms.CellClientSession;
 import org.jdesktop.wonderland.client.comms.WonderlandSession;
 
 import org.jdesktop.wonderland.client.jme.JmeClientMain;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.Iterator;
 
 import javax.swing.SwingUtilities;
 
 import org.jdesktop.wonderland.client.cell.TransformChangeListener;
 import org.jdesktop.wonderland.client.cell.TransformChangeListener.ChangeSource;
 
 import org.jdesktop.wonderland.client.jme.ClientContextJME;
 import org.jdesktop.wonderland.client.jme.SceneWorker;
 
 import org.jdesktop.mtgame.processor.WorkProcessor.WorkCommit;
 
 import com.jme.math.Vector3f;
 
 import java.awt.Color;
 import java.awt.Font;
 
 import com.jme.scene.Node;
 
 /**
  *
  * @author jprovino
  */
 public class OrbMessageHandler implements TransformChangeListener, FollowMeListener {
 
     private static final Logger logger =
             Logger.getLogger(OrbMessageHandler.class.getName());
 
     private ChannelComponent channelComp;
         
     private OrbCell orbCell;
 
     private Cell avatarCell;
 
     private Cell hostCell;
 
     private WonderlandSession session;
 
     private OrbDialog orbDialog;
 
     private String username;
     private String usernameAlias;
 
     private NameTagNode nameTag;
 
     private PresenceManager pm;
 
     private PresenceInfo presenceInfo;
 
     private boolean presenceInfoAdded;
 
     private FollowMe followMe;
 
     private static HashMap<Cell, ArrayList<OrbCell>> attachedOrbMap = new HashMap();
 
     private static ArrayList<OrbCell> detachedOrbList = new ArrayList();
 
     public OrbMessageHandler(OrbCell orbCell, WonderlandSession session, String[] bystanders) {
 	this.orbCell = orbCell;
 	this.session = session;
 	this.bystanders = bystanders;
 
 	synchronized (detachedOrbList) {
 	    detachedOrbList.add(orbCell);
 	}
 
 	avatarCell = ((CellClientSession)session).getLocalAvatar().getViewCell();
 	    
 	CellTransform transform = orbCell.getLocalTransform();
 	Vector3f translation = orbCell.getLocalTransform().getTranslation(null);
 	
 	followMe = new FollowMe(
 	    orbCell.getComponent(MovableComponent.class), translation, this);
 
         channelComp = orbCell.getComponent(ChannelComponent.class);
 
         logger.finer("OrbCellID " + orbCell.getCellID() + ", Channel comp is " 
 	    + channelComp);
 
         ChannelComponent.ComponentMessageReceiver msgReceiver =
             new ChannelComponent.ComponentMessageReceiver() {
                 public void messageReceived(CellMessage message) {
                     processMessage(message);
                 }
             };
 
         channelComp.addMessageReceiver(OrbAttachMessage.class, msgReceiver);
         channelComp.addMessageReceiver(OrbAttachVirtualPlayerMessage.class, msgReceiver);
         channelComp.addMessageReceiver(OrbBystandersMessage.class, msgReceiver);
         channelComp.addMessageReceiver(OrbChangeNameMessage.class, msgReceiver);
         channelComp.addMessageReceiver(OrbEndCallMessage.class, msgReceiver);
         channelComp.addMessageReceiver(OrbMuteCallMessage.class, msgReceiver);
         channelComp.addMessageReceiver(OrbSetVolumeMessage.class, msgReceiver);
         channelComp.addMessageReceiver(OrbSpeakingMessage.class, msgReceiver);
 
         pm = PresenceManagerFactory.getPresenceManager(session);
 
 	username = orbCell.getUsername();
 
 	if (username == null) {
 	    username = "No user name!";
 	} else {
 	    username = " " + username;
 	}
 	
 	usernameAlias = username;
 
 	String playerWithVpCallID = orbCell.getPlayerWithVpCallID();
 
 	WonderlandIdentity userID = new WonderlandIdentity(username, username, null);
 
 	presenceInfo = new PresenceInfo(orbCell.getCellID(), null, userID, 
 	    orbCell.getCallID());
 
 	presenceInfo.usernameAlias = usernameAlias;
 
 	if (playerWithVpCallID == null || playerWithVpCallID.equals(orbCell.getCallID())) {
 	    /*
 	     * It's a real call.  Use the actually callID and userID.
 	     */
 	    pm.addPresenceInfo(presenceInfo);
 	    presenceInfoAdded = true;
 	} 
 
         NameTagComponent comp = new NameTagComponent(orbCell, username, (float) .17);
 	    orbCell.addComponent(comp);
 	nameTag = comp.getNameTagNode();
 
 	//nameTag.setFont(Font.decode("Sans-PLAIN-20"));
 
 	setBystanders(bystanders);
 
 	if (orbCell.getPlayerWithVpCallID() != null) {
 	    PresenceInfo info = pm.getPresenceInfo(playerWithVpCallID);
 
             if (info == null) {
                 logger.warning("Can't find presence info for CallID "
 		    + playerWithVpCallID);
 
                 return;
             }
 
 	    logger.info("Attach orb " + orbCell.getCellID() 
 		+ " player with " + playerWithVpCallID + " to " + info);
 
             channelComp.send(new OrbAttachMessage(orbCell.getCellID(), info.cellID, true));
 	} else {
 	    /*
 	     * Ask the server to tell us if the orb is attached.
 	     */
 	    logger.fine("Asking server if orb is attached " + orbCell.getCellID());
 
             channelComp.send(new OrbAttachMessage(orbCell.getCellID(), null, true));
 	}
     }
 
    private Node orbRootNode;

    

     Node getNameTagNode() {
         return nameTag;
     }
 	
     public FollowMe getFollowMe() {
 	return followMe;
     }
 
     private boolean done;
 
     public void done() {
 	if (done) {
 	    return;
 	}
 
 	done = true;
 
 	synchronized (detachedOrbList) {
 	    detachedOrbList.remove(orbCell);
 	    reorderDetachedOrbs();
 
 	    if (hostCell != null) {
 		ArrayList<OrbCell> attachedOrbList = attachedOrbMap.get(hostCell);
 
 		if (attachedOrbList != null) {
 		    attachedOrbList.remove(orbCell);
 		}
 
 		if (attachedOrbList.size() > 0) {
 	            positionAttachedOrbs(hostCell);
 		} else {
 		    attachedOrbMap.remove(hostCell);
 		}
 	    }
 	}
 
 	followMe.done();
 
         channelComp.removeMessageReceiver(OrbAttachMessage.class);
         channelComp.removeMessageReceiver(OrbAttachVirtualPlayerMessage.class);
 	channelComp.removeMessageReceiver(OrbBystandersMessage.class);
 	channelComp.removeMessageReceiver(OrbChangeNameMessage.class);
 	channelComp.removeMessageReceiver(OrbEndCallMessage.class);
         channelComp.removeMessageReceiver(OrbMuteCallMessage.class);
 	channelComp.removeMessageReceiver(OrbSetVolumeMessage.class);
         channelComp.removeMessageReceiver(OrbSpeakingMessage.class);
 
	orbRootNode.detachChild(nameTag);
 	nameTag.done();
 
 	String playerWithVpCallID = orbCell.getPlayerWithVpCallID();
 
 	if (presenceInfoAdded) {
 	    pm.removePresenceInfo(presenceInfo);
 	}
     }
 
     public static void makeOrbsVisible(boolean isVisible) {
 	Collection<ArrayList<OrbCell>> attachedOrbs = attachedOrbMap.values();
 
 	Iterator<ArrayList<OrbCell>> it = attachedOrbs.iterator();
 
 	while (it.hasNext()) {
 	    ArrayList<OrbCell> orbs = it.next();
 
 	    for (OrbCell orb : orbs) {
 	        orb.setVisible(isVisible);
 	    }
 	}
 
 	for (OrbCell orbCell : detachedOrbList) {
 	    orbCell.setVisible(isVisible);
 	}
     }
 
     private String[] bystanders;
 
     private BystandersListener listener;
 
     public void setBystandersListener(BystandersListener listener) {
 	this.listener = listener;
     }
 
     private void setBystanders(String[] bystanders) {
 
 	if ((bystanders == null) || (bystanders.length == 0)) {
 	    nameTag.setNameTag(EventType.CHANGE_NAME, username, usernameAlias);
 	} else {
 	    nameTag.setNameTag(EventType.CHANGE_NAME, username, usernameAlias + " + " 
 		+ bystanders.length);
 	}
 
 	this.bystanders = bystanders;
 
 	if (listener != null) {
 	    listener.setBystanders(bystanders);
 	}
     }
   
     public String[] getBystanders() {
 	return bystanders;
     }
 
     public void processMessage(final Message message) {
 	logger.finer("process message " + message);
 
 	if (message instanceof OrbEndCallMessage) {
 	    if (orbDialog != null) {
 		orbDialog.setVisible(false);
 	    }
 
 	    done();
 	    return;
 	}
 
 	if (message instanceof OrbSpeakingMessage) {
 	    OrbSpeakingMessage msg = (OrbSpeakingMessage) message;
 
 	    logger.fine("Orb speaking " + msg.isSpeaking() + " cellID " + msg.getCellID()
 		+ " pi " + presenceInfo);
 
 	    if (presenceInfoAdded) {
 		pm.setSpeaking(presenceInfo, msg.isSpeaking());
 	    }
 
 	    if (msg.isSpeaking()) {
 	        nameTag.setNameTag(EventType.STARTED_SPEAKING, username, usernameAlias);
 	    } else {
 	        nameTag.setNameTag(EventType.STOPPED_SPEAKING, username, usernameAlias);
 	    }
 
 	    return;
 	}
 
 	if (message instanceof OrbMuteCallMessage) {
 	    OrbMuteCallMessage msg = (OrbMuteCallMessage) message;
 
 	    if (presenceInfoAdded) {
 		pm.setMute(presenceInfo, msg.isMuted());
 	    }
 
 	    if (msg.isMuted()) {
                 nameTag.setNameTag(EventType.MUTE, username, usernameAlias);
 	    } else {
                 nameTag.setNameTag(EventType.UNMUTE, username, usernameAlias);
 	    }
 
 	    return;
 	}
 
 	if (message instanceof OrbBystandersMessage) {
 	    OrbBystandersMessage msg = (OrbBystandersMessage) message;
 	    setBystanders(msg.getBystanders());
 	    return;
 	}
 
 	if (message instanceof OrbChangeNameMessage) {
 	    OrbChangeNameMessage msg = (OrbChangeNameMessage) message;
 
 	    usernameAlias = msg.getName();
 
 	    if (presenceInfoAdded) {
 		presenceInfo.usernameAlias = usernameAlias;
 	        pm.changeUsernameAlias(presenceInfo);
 	    }
 
 	    nameTag.setNameTag(EventType.CHANGE_NAME, username, usernameAlias);
 	    return;
 	}
 
 	if (message instanceof OrbAttachMessage) {
 	    OrbAttachMessage msg = (OrbAttachMessage) message;
 
 	    attachOrb(msg.getHostCellID(), msg.isAttached());
 	    return;
 	}
 
 	if (message instanceof OrbAttachVirtualPlayerMessage) {
 	    OrbAttachVirtualPlayerMessage msg = (OrbAttachVirtualPlayerMessage) message;
 
 	    PresenceInfo info = pm.getPresenceInfo(msg.getHostCallID());
 
 	    if (info == null) {
 		logger.warning("OrbAttachVirtualPlayerMessage, no presence info for callID " 
 		    + msg.getHostCallID());
 		return;
 	    }
 
 	    attachOrb(info.cellID, true);
 	    return;
  	}
     }
 
     private void attachOrb(CellID hostCellID, boolean attach) {
 	Cell newHostCell = ClientContext.getCellCache(session).getCell(hostCellID);
 
 	if (newHostCell == null) {
 	    logger.warning("Can't find host cell for " + hostCellID);
 	    return;
 	}
 
 	if (logger.isLoggable(Level.FINE)) {
 	    String s = "None";
 
 	    if (hostCell != null) {
 	     	s = hostCell.getCellID().toString();
 	    }
 
 	    logger.fine("Attach " + attach + " avatarCellID " 
 		+ avatarCell.getCellID() + " new host " + newHostCell.getCellID()
 		+ " current host " + s);
 	}
 
 	if (attach) {
 	    if (hostCell != null) {
 		/*
 		 * Someone else has attached the Orb.
 		 */
 		logger.fine("Detaching " + orbCell.getCellID() + " from "
 		    + hostCell.getCellID());
 		detachOrb(false);
 	    }
 
 	    synchronized (detachedOrbList) {
 		detachedOrbList.remove(orbCell);
 	    }
 
 	    ArrayList<OrbCell> attachedOrbList = attachedOrbMap.get(newHostCell);
 
 	    if (attachedOrbList == null) {
 		attachedOrbList = new ArrayList();
 
 		attachedOrbMap.put(newHostCell, attachedOrbList);
 	    } 
 
 	    synchronized (attachedOrbList) {
 		attachedOrbList.remove(orbCell);
 		attachedOrbList.add(orbCell);
 	    }
 		
 	    hostCell = newHostCell;
 	    newHostCell.addTransformChangeListener(this);
 	    positionAttachedOrbs(newHostCell);
 	} else {
 	    detachOrb(true);
 	}
 	return;
     }
 
     private void detachOrb(boolean positionAttachedOrbs) {
         hostCell.removeTransformChangeListener(this);
 
 	ArrayList<OrbCell> attachedOrbList = attachedOrbMap.get(hostCell);
 
 	synchronized (attachedOrbList) {
 	    attachedOrbList.remove(orbCell);
 	}
 
 	synchronized (detachedOrbList) {
 	    detachedOrbList.add(orbCell);
 	}
 
 	if (positionAttachedOrbs) {
 	    positionAttachedOrbs(hostCell);
 	}
 
 	if (orbDialog != null) {
             orbDialog.orbDetached();
 	}
 
 	reorderDetachedOrbs();
 
 	hostCell = null;
     }
 
     public void transformChanged(Cell cell, ChangeSource source) {
 	logger.finest("Cell " + cell.getName() + " moved to " 
 	    + cell.getLocalTransform());
 
  	positionAttachedOrbs(cell);
     }
 
     public void positionChanged(Vector3f position) {
 	channelComp.send(new OrbChangePositionMessage(orbCell.getCellID(), position));
     }
 
     public void targetReached(Vector3f position) {
     }
 
     private void reorderDetachedOrbs() {
 	if (hostCell == null) {
 	    return;
 	}
 
 	CellTransform transform = hostCell.getLocalTransform();
 
 	Vector3f translation = new Vector3f();
 	transform.getTranslation(translation);
 
 	synchronized (detachedOrbList) {
 	    for (int i = 0 ; i < detachedOrbList.size(); i++) {
 	        translation.setZ(translation.getZ() + (float) .2);
 	        translation.setY((float) .5);  // lower orb.
 	        followMe.setTargetPosition(translation,
 	    	    transform.getRotation(null));
 	    }
 	}
 
     }
 
     private void positionAttachedOrbs(Cell hostCell) {
 	if (hostCell == null) {
 	    return;
 	}
 
 	CellTransform transform = hostCell.getLocalTransform();
 	ArrayList<OrbCell> attachedOrbList = attachedOrbMap.get(hostCell);
 
 	if (attachedOrbList == null) {
 	    return;
 	}
 
 	synchronized (attachedOrbList) {
 	    for (int i = 0; i < attachedOrbList.size(); i++) {
 		Vector3f translation = new Vector3f();
 		transform.getTranslation(translation);
 
 		translation.setY((float) (2.2 + (.3 * i)));  // Raise orb.
 		attachedOrbList.get(i).getOrbMessageHandler().getFollowMe().setTargetPosition(
 		    translation);
 	    }
 	}
     }
 
     public void orbSelected() {
 	String callID = orbCell.getPlayerWithVpCallID();
 
 	if (callID != null) {
 	    /*
 	     * If it's a virtual orb for me, ignore it.
 	     */
 	    //if (callID.equals(SoftphoneControlImpl.getInstance().getCallID()) == true) {
 	    //	return;
 	    //}
 	}
 
 	SwingUtilities.invokeLater(new Runnable() {
 	    public void run() {
 		if (orbDialog == null) {
 	    	    LocalAvatar avatar = ((CellClientSession)session).getLocalAvatar();
 	    
 	            orbDialog = new OrbDialog(orbCell, channelComp, 
 		        avatar.getViewCell().getCellID(), pm);
 		} 
 		orbDialog.setVisible(true);
 	    }
 	});
     }
 
 }
