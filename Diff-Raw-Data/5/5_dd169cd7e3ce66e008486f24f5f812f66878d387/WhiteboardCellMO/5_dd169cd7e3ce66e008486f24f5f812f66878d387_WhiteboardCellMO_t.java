 /**
  * Project Wonderland
  *
  * Copyright (c) 2004-2008, Sun Microsystems, Inc., All Rights Reserved
  *
  * Redistributions in source code form must reproduce the above
  * copyright and this condition.
  *
  * The contents of this file are subject to the GNU General Public
  * License, Version 2 (the "License"); you may not use this file
  * except in compliance with the License. A copy of the License is
  * available at http://www.opensource.org/licenses/gpl-license.php.
  *
  * $Revision$
  * $Date$
  * $State$
  */
 package org.jdesktop.wonderland.modules.simplewhiteboard.server;
 
 import com.jme.math.Vector2f;
 import com.sun.sgs.app.AppContext;
 import com.sun.sgs.app.ClientSession;
 import com.sun.sgs.app.ManagedReference;
import java.math.BigInteger;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.logging.Logger;
 import org.jdesktop.wonderland.common.ExperimentalAPI;
 import org.jdesktop.wonderland.common.cell.ClientCapabilities;
 import org.jdesktop.wonderland.common.cell.config.CellConfig;
 import org.jdesktop.wonderland.common.cell.messages.CellMessage;
 import org.jdesktop.wonderland.common.cell.setup.BasicCellSetup;
 import org.jdesktop.wonderland.server.cell.ChannelComponentMO;
 import org.jdesktop.wonderland.server.comms.WonderlandClientSender;
 import org.jdesktop.wonderland.modules.simplewhiteboard.common.WhiteboardCompoundCellMessage;
 import org.jdesktop.wonderland.modules.simplewhiteboard.common.WhiteboardAction.Action;
 import org.jdesktop.wonderland.modules.simplewhiteboard.common.WhiteboardCellConfig;
 import org.jdesktop.wonderland.modules.simplewhiteboard.common.WhiteboardCommand.Command;
 import org.jdesktop.wonderland.modules.simplewhiteboard.common.WhiteboardTypeName;
 import org.jdesktop.wonderland.modules.appbase.server.App2DCellMO;
 import org.jdesktop.wonderland.modules.appbase.server.AppTypeMO;
 import org.jdesktop.wonderland.server.setup.BeanSetupMO;
 
 /**
  * A server cell associated with a whiteboard
  *
  * @author nsimpson,deronj
  */
 
 @ExperimentalAPI
 public class WhiteboardCellMO extends App2DCellMO implements BeanSetupMO {
 
     private static final Logger logger = Logger.getLogger(WhiteboardCellMO.class.getName());
     
     // The messages list contains the current state of the whiteboard.
     // It's updated every time a client makes a change to the whiteboard
     // so that when new clients join, they receive the current state
     private static LinkedList<WhiteboardCompoundCellMessage> messages;
     private static WhiteboardCompoundCellMessage lastMessage;
     
     /** The communications component used to broadcast to all clients */
     private ManagedReference<WhiteboardComponentMO> commComponentRef = null;
 
     /** The preferred width (from the WFS file) */
     private int preferredWidth;
 
     /** The preferred height (from the WFS file) */
     private int preferredHeight;
 
     /** Default constructor, used when the cell is created via WFS */
     public WhiteboardCellMO() {
         super();
         addComponent(new ChannelComponentMO(this));
 	WhiteboardComponentMO commComponent = new WhiteboardComponentMO(this);
         commComponentRef = AppContext.getDataManager().createReference(commComponent); 
         addComponent(commComponent);
         messages = new LinkedList<WhiteboardCompoundCellMessage>();
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     protected String getClientCellClassName(ClientSession clientSession, ClientCapabilities capabilities) {
         return "org.jdesktop.wonderland.modules.simplewhiteboard.client.WhiteboardCell";
     }
 
     /** 
      * {@inheritDoc}
      */
     public AppTypeMO getAppType () {
 	return new WhiteboardAppTypeMO();
     }
 
     /** 
      * {@inheritDoc}
      */
     @Override
     protected CellConfig getCellConfig (ClientSession clientSession, ClientCapabilities capabilities) {
 	WhiteboardCellConfig config = new WhiteboardCellConfig(pixelScale);
 	config.setPreferredWidth(preferredWidth);
 	config.setPreferredHeight(preferredHeight);
         return config;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public void setupCell(BasicCellSetup setupData) {
 	super.setupCell(setupData);
 
 	WhiteboardCellSetup setup = (WhiteboardCellSetup) setupData;
 	preferredWidth = setup.getPreferredWidth();
 	preferredHeight = setup.getPreferredHeight();
 	pixelScale = new Vector2f(setup.getPixelScaleX(), setup.getPixelScaleY());
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public void reconfigureCell(BasicCellSetup setup) {
         super.reconfigureCell(setup);
         setupCell(setup);
     }
 
     /**
      * Process a message from a client.
      *
      * Sync message: send all accumulated messages back to the client (the sender).
      * All other messages: broadcast to <bold>all</bold> cells (including the sender!)
      *
      * @param clientSender The sender object for the client who sent the message.
      * @param clientSession The session for the client who sent the message.
      * @param message The message which was received.
      * @param commComponent The communications component that received the message.
      */
     public void receivedMessage(WonderlandClientSender clientSender, ClientSession clientSession, CellMessage message) {
         WhiteboardCompoundCellMessage cmsg = (WhiteboardCompoundCellMessage)message;
         logger.fine("received whiteboard message: " + cmsg);
 
 	WhiteboardComponentMO commComponent = commComponentRef.getForUpdate();
 
         if (cmsg.getAction() == Action.REQUEST_SYNC) {
             logger.fine("sending " + messages.size() + " whiteboard sync messages");
             Iterator<WhiteboardCompoundCellMessage> iter = messages.iterator();
             
             while (iter.hasNext()) {
                 WhiteboardCompoundCellMessage msg = iter.next();
 		clientSender.send(clientSession, msg);
             }
         } else {
 
 	    // Create the copy of the message to be broadcast to clients
             WhiteboardCompoundCellMessage msg = new WhiteboardCompoundCellMessage(cmsg.getClientID(), 
 										  cmsg.getCellID(),
 										  cmsg.getAction());
             switch (cmsg.getAction()) {
                 case SET_TOOL:
                     // tool
                     msg.setTool(cmsg.getTool());
                     break;
                 case SET_COLOR:
                     // color
                     msg.setColor(cmsg.getColor());
                     break;
                 case MOVE_TO:
                 case DRAG_TO:
                     // position
                     msg.setPositions(cmsg.getPositions());
                     break;
                 case REQUEST_SYNC:
                     break;
                 case EXECUTE_COMMAND:
                     // command
                     msg.setCommand(cmsg.getCommand());
                     break;
             }
             
             // record the message in setup data (move events are not recorded)
             if (cmsg.getAction() == Action.EXECUTE_COMMAND) {
                 if (cmsg.getCommand() == Command.ERASE) {
                     // clear the action history
                     logger.fine("clearing message history");
                     messages.clear();
                 }
             } else {
                 if (cmsg.getAction() != Action.MOVE_TO) {
                     if ((lastMessage != null) &&
 			lastMessage.getAction() == Action.MOVE_TO) {
                         messages.add(lastMessage);
                     }
 
 		    // Must guarantee that the original sender doesn't ignore this when it is played back during a sync
 		    cmsg.setClientID(null);
 
                     messages.add(cmsg);
                 }
             }
             lastMessage = cmsg;
 
 	    // Broadcast message to all clients (including the original sender of the message).
            BigInteger sessionId = AppContext.getDataManager().createReference(clientSession).getId();
            commComponent.sendAllClients(sessionId, msg);
         }
     }
 }
