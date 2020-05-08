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
 package org.jdesktop.wonderland.client.jme.input;
 
 import java.awt.event.MouseEvent;
 import org.jdesktop.wonderland.client.input.Event;
 import org.jdesktop.wonderland.client.input.EventDistributor;
 import org.jdesktop.mtgame.PickInfo;
 import org.jdesktop.mtgame.PickDetails;
 import java.util.logging.Logger;
 import org.jdesktop.mtgame.Entity;
 import org.jdesktop.wonderland.client.input.FocusChangeEvent;
 import org.jdesktop.wonderland.client.input.InputManager;
 import org.jdesktop.wonderland.common.InternalAPI;
 
 /**
  * The part of the input subsystem which distributes events throughout the scene graph 
  * according to the information provided by the entity event listeners.
  *
  * @author deronj
  */
 @InternalAPI
 public class EventDistributor3D extends EventDistributor implements Runnable {
 
     private static final Logger logger = Logger.getLogger(EventDistributor3D.class.getName());
     private EventDistributor.PropagationState propState = new PropagationState();
     /** The pick info for the last mouse event */
     private PickInfo mousePickInfoPrev;
     /** The singleton event distributor */
     private static EventDistributor eventDistributor;
 
     /** Return the event distributor singleton */
     static EventDistributor getEventDistributor() {
         if (eventDistributor == null) {
             eventDistributor = new EventDistributor3D();
             ((EventDistributor3D) eventDistributor).start();
         }
         return eventDistributor;
     }
 
     protected void processEvent(Event event, PickInfo destPickInfo, PickInfo hitPickInfo) {
         if (event instanceof MouseEvent3D) {
             processMouseKeyboardEvent(event, destPickInfo, hitPickInfo);
         } else if (event instanceof KeyEvent3D) {
             processMouseKeyboardEvent(event, mousePickInfoPrev, null);
         } else if (event instanceof FocusEvent3D) {
             processFocusEvent((FocusEvent3D)event);
         } else if (event instanceof FocusChangeEvent) {
             processFocusChangeEvent(((FocusChangeEvent) event).getChanges());
         } else if (event instanceof Event) {
             processGlobalEvent(event);
         } else {
             logger.warning("Invalid event type encountered, event = " + event);
         }
     }
 
     protected void processMouseKeyboardEvent(Event event, PickInfo destPickInfo, PickInfo hitPickInfo) {
         logger.fine("Distributor: received event = " + event);
         logger.fine("Distributor: destPickInfo = " + destPickInfo);
         /*
         if (destPickInfo != null && destPickInfo.size() > 0 && destPickInfo.get(0) != null) {
 	    logger.fine("entity = " + destPickInfo.get(0).getEntity();
         }
         logger.fine("Distributor: hitPickInfo = " + hitPickInfo);
         if (hitPickInfo != null && hitPickInfo.size() > 0 && hitPickInfo.get(0) != null) {
 	    logger.fine("entity = " + hitPickInfo.get(0).getEntity());
         }
          */
         // Track the last mouse pick info for focus-follows-mouse keyboard focus policy
         if (event instanceof MouseEvent3D) {
             mousePickInfoPrev = destPickInfo;
             MouseEvent3D mouseEvent = (MouseEvent3D) event;
             if (mouseEvent.getAwtEvent() instanceof InputManager.NondeliverableMouseEvent) {
                 return;
             }
         }
 
         if (event instanceof InputEvent3D) {
             ((InputEvent3D) event).setPickInfo(destPickInfo);
         }
 
 
         // Try the global event listeners. Set the pickDetails of mouse events to topmost pickDetails.
         if (event instanceof MouseEvent3D && destPickInfo != null && destPickInfo.size() > 0) {
             ((MouseEvent3D) event).setPickDetails(destPickInfo.get(0));
         }
         tryGlobalListeners(event);
 
         // Start out the entity search assuming no propagation to unders
         propState.toUnder = false;
 
         // Walk through successive depth levels, as long as propagateToUnder is true,
         // searching up the parent chain in each level
         if (destPickInfo == null || destPickInfo.size() <= 0) {
             return;
         }
         PickDetails pickDetails = destPickInfo.get(0);
         logger.fine("pickDetails = " + pickDetails);
         int idx = 0;
         while (true) {
 
             // Start this loop interation out assuming no propagation to parents
             propState.toParent = false;
 
             // See whether the picked entity wants the event.
             if (event instanceof MouseEvent3D) {
                 ((MouseEvent3D) event).setPickDetails(pickDetails);
                 if (((MouseEvent3D) event).getID() == MouseEvent.MOUSE_DRAGGED && hitPickInfo != null) {
                     MouseDraggedEvent3D de3d = (MouseDraggedEvent3D) event;
                     if (idx < hitPickInfo.size()) {
                         de3d.setHitPickDetails(hitPickInfo.get(idx));
                     }
                 }
             }
 	    Entity entity = null;
 	    if (pickDetails != null) {
 		entity = pickDetails.getEntity();
 	    }
             if (entity == null) {
                 idx++;
                 if (idx >= destPickInfo.size()) {
                     // No more picked objects underneath. We're done.
                     break;
                 } else {
                     continue;
                 }
             }
 
             tryListenersForEntity(entity, event, propState);
 
             // See whether any of the picked entity's parents want the event
             if (propState.toParent) {
                 logger.fine("Propogating to parents");
                 tryListenersForEntityParents(entity.getParent(), event, propState);
             }
 
             if (!propState.toUnder) {
                 // No more propagation to unders. We're done.
                 break;
             }
 
             logger.fine("Propagate to next under");
 
             idx++;
             if (idx >= destPickInfo.size()) {
                 // No more picked objects underneath. We're done.
                 break;
             }
 
             pickDetails = destPickInfo.get(idx);
             logger.fine("pickDetails = " + pickDetails);
         }
     }
 
     protected void processFocusEvent(FocusEvent3D event) {
         logger.fine("Distributor: received focus event = " + event);
 
         // Focus enty has no associated entity, so only try global listeners
         tryGlobalListeners(event);
     }
 
    protected void processSwingEnterExitEvent(Event event, Entity entity) {
         logger.fine("Distributor: received event = " + event + ", entity = " + entity);
 
         tryGlobalListeners(event);
 
         // See whether the specified entity wants the event.
         propState.toParent = false;
         tryListenersForEntity(entity, event, propState);
 
         // See whether any of the picked entity's parents want the event
         if (propState.toParent) {
             logger.fine("Propogating to parents");
             tryListenersForEntityParents(entity.getParent(), event, propState);
         }
     }
 
     private void processGlobalEvent(Event event) {
         logger.fine("Distributor: received global event = " + event);
         tryGlobalListeners(event);
     }
 }
 
