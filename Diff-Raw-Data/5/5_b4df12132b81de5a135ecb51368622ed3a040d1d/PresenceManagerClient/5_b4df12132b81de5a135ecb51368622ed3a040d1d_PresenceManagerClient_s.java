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
 package org.jdesktop.wonderland.modules.presencemanager.client;
 
 import org.jdesktop.wonderland.client.cell.view.LocalAvatar;
 import org.jdesktop.wonderland.client.cell.view.LocalAvatar.ViewCellConfiguredListener;
 import org.jdesktop.wonderland.client.comms.BaseConnection;
 import org.jdesktop.wonderland.client.comms.CellClientSession;
 import org.jdesktop.wonderland.client.comms.ConnectionFailureException;
 import org.jdesktop.wonderland.client.comms.WonderlandSession;
 import org.jdesktop.wonderland.client.softphone.SoftphoneControlImpl;
 import org.jdesktop.wonderland.common.comms.ConnectionType;
 import org.jdesktop.wonderland.common.cell.CallID;
 import org.jdesktop.wonderland.common.cell.CellID;
 import org.jdesktop.wonderland.common.cell.CellStatus;
 import org.jdesktop.wonderland.common.messages.Message;
 import org.jdesktop.wonderland.modules.presencemanager.common.PresenceInfo;
 import org.jdesktop.wonderland.modules.presencemanager.common.PresenceManagerConnectionType;
 import org.jdesktop.wonderland.modules.presencemanager.common.messages.ClientConnectMessage;
 import org.jdesktop.wonderland.modules.presencemanager.common.messages.ClientConnectResponseMessage;
 import org.jdesktop.wonderland.modules.presencemanager.common.messages.PresenceInfoAddedMessage;
 import org.jdesktop.wonderland.modules.presencemanager.common.messages.PresenceInfoRemovedMessage;
 import org.jdesktop.wonderland.client.cell.Cell;
 import org.jdesktop.wonderland.client.cell.CellManager;
 
 import java.util.ArrayList;
 
 import java.util.logging.Logger;
 
 /**
  *
  * @author jprovino
  */
 public class PresenceManagerClient extends BaseConnection implements
         ViewCellConfiguredListener {
 
     private static final Logger logger =
             Logger.getLogger(PresenceManagerClient.class.getName());
     private WonderlandSession session;
     private CellID cellID;
     private boolean connected = true;
     private PresenceManagerImpl presenceManager;
     private PresenceInfo presenceInfo;
 
     private static PresenceManagerClient client;
 
     public static PresenceManagerClient getInstance() {
 	return client;
     }
 
     /** 
      * Create a new PresenceManagerClient
      * @param session the session to connect to, guaranteed to be in
      * the CONNECTED state
      * @throws org.jdesktop.wonderland.client.comms.ConnectionFailureException
      */
     public PresenceManagerClient() {
         logger.fine("Starting PresenceManagerClient");
 	client = this;
     }
 
     public synchronized void execute(final Runnable r) {
     }
 
     @Override
     public void connect(WonderlandSession session)
             throws ConnectionFailureException
     {
         super.connect(session);
         this.session = session;
 
         presenceManager = (PresenceManagerImpl) PresenceManagerFactory.getPresenceManager(session);
 
 	presenceManager.dump();
 
         LocalAvatar avatar = ((CellClientSession) session).getLocalAvatar();
         avatar.addViewCellConfiguredListener(this);
         if (avatar.getViewCell() != null) {
             // if the view is already configured, fake an event
             viewConfigured(avatar);
         }
     }
 
     @Override
     public void disconnect() {
         // LocalAvatar avatar = ((CellClientSession)session).getLocalAvatar();
         // avatar.removeViewCellConfiguredListener(this);
         super.disconnect();
 	
         session.send(this, new ClientConnectMessage(presenceInfo, false));
 
 	PresenceManagerFactory.reset();
     }
 
     public void viewConfigured(LocalAvatar localAvatar) {
         cellID = localAvatar.getViewCell().getCellID();
 
         String callID = CallID.getCallID(cellID);
 
         SoftphoneControlImpl.getInstance().setCallID(callID);
 
         presenceInfo = new PresenceInfo(cellID, session.getID(),
             session.getUserID(), callID);
 
         session.send(this, new ClientConnectMessage(presenceInfo, true));
 
 	//presenceManager.addPresenceInfo(presenceInfo);
 
         logger.fine("[PresenceManagerClient] view configured fpr " + cellID + " in " + presenceManager);
     }
 
     @Override
     public void handleMessage(Message message) {
         logger.fine("got a message...");
 
 	if (message instanceof ClientConnectResponseMessage) {
 	    ClientConnectResponseMessage msg = (ClientConnectResponseMessage) message;
 
 	    ArrayList<PresenceInfo> presenceInfoList = msg.getPresenceInfoList();
 
	    for logger.fine(PresenceInfo presenceInfo : presenceInfoList) {
		("Client connected:  " + msg.isConnected() 
		    + " " + presenceInfo);
 
 		if (msg.isConnected()) {
 		    logger.fine("Got ClientConnectResponse:  adding pi " + presenceInfo);
 		    presenceManager.presenceInfoAdded(presenceInfo);
 		} else {
 		    presenceManager.presenceInfoRemoved(presenceInfo);
 		}
 	    }
 
 	    return;
 	}
 
         if (message instanceof PresenceInfoAddedMessage) {
             PresenceInfoAddedMessage m = (PresenceInfoAddedMessage) message;
 
             logger.fine("GOT PresenceInfoAddedMessage for " + m.getPresenceInfo());
 
             presenceManager.presenceInfoAdded(m.getPresenceInfo());
             return;
         }
 
         if (message instanceof PresenceInfoRemovedMessage) {
             PresenceInfoRemovedMessage m = (PresenceInfoRemovedMessage) message;
 
             logger.fine("GOT PresenceInfoRemovedMessage for " + m.getPresenceInfo());
             presenceManager.presenceInfoRemoved(m.getPresenceInfo());
             return;
         }
 
         throw new UnsupportedOperationException("Unknown message:  " + message);
     }
 
     public ConnectionType getConnectionType() {
         return PresenceManagerConnectionType.CONNECTION_TYPE;
     }
 
 }
