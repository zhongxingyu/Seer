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
 package org.jdesktop.wonderland.client.cell;
 
import java.math.BigInteger;
 import java.util.ArrayList;
 import org.jdesktop.wonderland.common.cell.CellTransform;
 import java.util.logging.Logger;
 import org.jdesktop.wonderland.client.cell.TransformChangeListener.ChangeSource;
 import org.jdesktop.wonderland.client.comms.ClientConnection;
 import org.jdesktop.wonderland.client.comms.ResponseListener;
 import org.jdesktop.wonderland.common.ExperimentalAPI;
 import org.jdesktop.wonderland.common.cell.CellStatus;
 import org.jdesktop.wonderland.common.cell.messages.CellMessage;
 import org.jdesktop.wonderland.common.cell.messages.MovableMessage;
 import org.jdesktop.wonderland.common.cell.messages.MovableMessageResponse;
 import org.jdesktop.wonderland.common.messages.ResponseMessage;
 
 /**
  * A component that provides cell movement
  * 
  * @author paulby
  */
 @ExperimentalAPI
 public class MovableComponent extends CellComponent {
     
     protected static Logger logger = Logger.getLogger(MovableComponent.class.getName());
     protected ArrayList<CellMoveListener> serverMoveListeners = null;
     protected ChannelComponent channelComp;
     
     public enum CellMoveSource { LOCAL, REMOTE }; // Do we need BOTH as well ?
     
     protected ChannelComponent.ComponentMessageReceiver msgReceiver;
     
     public MovableComponent(Cell cell) {
         super(cell);
         channelComp = cell.getComponent(ChannelComponent.class);
     }
     
     
     @Override
     public void setStatus(CellStatus status) {
          switch(status) {
             case DISK :
                 if (msgReceiver!=null) {
                     channelComp.removeMessageReceiver(getMessageClass());
                     msgReceiver = null;
                 }
                 break;
              case BOUNDS : {
                  if (msgReceiver==null) {
                     msgReceiver = new ChannelComponent.ComponentMessageReceiver() {
 
                         public void messageReceived(CellMessage message) {
                             // Ignore messages from this client, TODO move this up into addMessageReciever with an option to turn off the test
                            BigInteger senderID = message.getSenderID();
                            if (senderID == null) {
                                senderID = BigInteger.ZERO;
                            }
                            if (!senderID.equals(cell.getCellCache().getSession().getID())) {
                                 serverMoveRequest((MovableMessage)message);
                             }
                         }
                     };                    
                     channelComp.addMessageReceiver(getMessageClass(), msgReceiver);
                  }
              }
         }
     }
 
     /**
      * @return the class of the message this component handles.
      */
     protected Class getMessageClass() {
         return MovableMessage.class;
     }
     
     /**
      * A request from this client to move the cell. The cell we be moved locally
      * and the requested change sent to the server. If the server denies the move
      * the cell will be moved to a server provided location and the listener
      * will be called. The server will
      * notify all other clients of the new location.
      * 
      * @param transform the requrested transform
      * @param listener the listener that will be notified in the event the
      * system modifies this move (due to collision etc).
      */
     public void localMoveRequest(CellTransform transform, 
                                  final CellMoveModifiedListener listener) {
     
         // make sure we are connected to the server
         if (channelComp == null || 
                 channelComp.getStatus() != ClientConnection.Status.CONNECTED) {
             logger.warning("Cell channel not connected when moving cell " +
                            cell.getCellID());
             return;
         }
 
         // TODO throttle sends, we should only send so many times a second.
         if (listener!=null) {
             channelComp.send(
                 MovableMessage.newMoveRequestMessage(cell.getCellID(), 
                                                     transform.getTranslation(null), 
                                                     transform.getRotation(null)),
                 new ResponseListener() {
 
                     public void responseReceived(ResponseMessage response) {
                         MovableMessageResponse msg = (MovableMessageResponse)response;
                         CellTransform requestedTransform = null;
                         CellTransform actualTransform = new CellTransform(msg.getRotation(), msg.getTranslation());
                         int reason = 1;
                         listener.moveModified(requestedTransform, reason, actualTransform);
                         // TODO Trigger a cell move with the SERVER_ADJUST source
                     }
                 });
         } else {
             channelComp.send(
                 MovableMessage.newMoveRequestMessage(cell.getCellID(), 
                                                     transform.getTranslation(null), 
                                                     transform.getRotation(null)));
         }
 
         applyLocalTransformChange(transform, TransformChangeListener.ChangeSource.LOCAL);
     }
 
     /**
      * Apply the transform change to the cell
      * @param transform
      * @param source
      */
     protected void applyLocalTransformChange(CellTransform transform, ChangeSource source) {
         cell.setLocalTransform(transform, source);
     }
     
     /**
      * A request from this client to move the cell. The cell we be moved locally
      * and the requested change sent to the server. If the server denies the move
      * the cell will be moved to a server provided location. The server will
      * notify all other clients of the new location.
      * 
      * @param transform
      */
     public void localMoveRequest(CellTransform transform) {
         localMoveRequest(transform, null);
     }
     
     /**
      * Called when a message arrives from the server requesting that the
      * cell be moved.
      * @param msg the message received from the server
      */
     protected void serverMoveRequest(MovableMessage msg) {
         CellTransform transform = new CellTransform(msg.getRotation(), msg.getTranslation());
         applyLocalTransformChange(transform, TransformChangeListener.ChangeSource.REMOTE);
         notifyServerCellMoveListeners(msg, transform, CellMoveSource.REMOTE);
     }
     
     
     
     /**
      * Listen for move events from the server
      * @param listener
      */
     public void addServerCellMoveListener(CellMoveListener listener) {
         if (serverMoveListeners==null) {
             serverMoveListeners = new ArrayList();
         }
         serverMoveListeners.add(listener);
     }
     
     /**
      * Remove the server move listener.
      * @param listener
      */
     public void removeServerCellMoveListener(CellMoveListener listener) {
         if (serverMoveListeners!=null) {
             serverMoveListeners.remove(listener);
         }
     }
     
     /**
      * Notify any serverMoveListeners that the cell has moved
      * 
      * @param transform
      */
     protected void notifyServerCellMoveListeners(MovableMessage msg, CellTransform transform, CellMoveSource source) {
         if (serverMoveListeners==null)
             return;
 
         for(CellMoveListener listener : serverMoveListeners) {
             listener.cellMoved(transform, source);
         }
     }
     
     @ExperimentalAPI
     public interface CellMoveListener {
         /**
          * Notification that the cell has moved. Source indicates the source of 
          * the move, local is from this client, remote is from the server.
          * 
          * @param transform
          * @param source
          */
         public void cellMoved(CellTransform transform, CellMoveSource source);
     }
     
     @ExperimentalAPI
     public interface CellMoveModifiedListener {
         /**
          * Notification from the server that the requested move was
          * not possible and a modified move took place instead. The cell should be positioned with the actualTransform
          * transform.
          * 
          * @param requestedTransform
          * @param reason
          * @param actualTransform
          */
         public void moveModified(CellTransform requestedTransform, int reason, CellTransform actualTransform);
     }
 
 }
