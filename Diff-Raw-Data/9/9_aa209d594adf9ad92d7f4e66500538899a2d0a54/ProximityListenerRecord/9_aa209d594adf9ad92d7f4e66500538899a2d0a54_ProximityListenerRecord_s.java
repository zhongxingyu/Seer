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
 package org.jdesktop.wonderland.common.cell;
 
 import com.jme.bounding.BoundingVolume;
 import com.jme.math.Vector3f;
 import java.io.Serializable;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.logging.Logger;
 import org.jdesktop.wonderland.common.Math3DUtils;
import org.jdesktop.wonderland.server.comms.WonderlandClientID;
 
 /**
  *
  * Utility class to help implement proximity listeners on both client and server
  *
  * @author paulby
  * @author Drew Harry <drew_harry@dev.java.net>
  */
 public class ProximityListenerRecord implements Serializable {
 
     protected static final Logger logger = Logger.getLogger(ProximityListenerRecord.class.getName());
 
     protected ProximityListenerWrapper proximityListener;
     private BoundingVolume[] localProxBounds;
     private BoundingVolume[] worldProxBounds;
     // private BoundingVolume currentlyIn = null;
     // private int currentlyInIndex = -1;
 
     // These maps keep track of which bounding volume (both the object and the index) that
     // each CellID (ViewCellID, which maps to a single avatar) is contained by. Used for
     // deciding of an avatar that has moved is entering/existing a bound that this
     // listener is tracking.
     protected Map<CellID, BoundingVolume> lastContainerMap = new HashMap<CellID, BoundingVolume>();
     protected Map<CellID, Integer> lastContainerIndexMap = new HashMap<CellID, Integer>();
 
     // For serialization support on server
     public ProximityListenerRecord() {
 
     }
 
     public ProximityListenerRecord(ProximityListenerWrapper proximityListener, BoundingVolume[] localBounds) {
         this.proximityListener = proximityListener;
         setProximityBounds(localBounds);
     }
 
      /**
      * Set a list of bounds for which the system will track view enter/exit for
      * this cell. When the view enters/exits one of these bounds the listener
      * will be called with the index of the bounds in the supplied array.
      *
      * The bounds must be ordered from largest to smallest, thus localBounds[i]
      * must enclose localBounds[i+1]. An IllegalArgumentException will be thrown
      * if this is not the case.
      *
      * @param bounds
      */
     public void setProximityBounds(BoundingVolume[] localBounds) {
         this.localProxBounds = new BoundingVolume[localBounds.length];
         this.worldProxBounds = new BoundingVolume[localBounds.length];
         int i=0;
         for(BoundingVolume b : localBounds) {
             this.localProxBounds[i] = b.clone(null);
             worldProxBounds[i] = b.clone(null);
 
             if (i>0 && !Math3DUtils.encloses(localProxBounds[i-1], localProxBounds[i]))
                     throw new IllegalArgumentException("Proximity Bounds incorrectly ordered");
             i++;
         }
     }
 
     /**
      * The cell world bounds have been updated, so update our internal
      * structures
      */
     public void updateWorldBounds(CellTransform worldTransform) {
         if (localProxBounds==null)
             return;
 
         // Update the world proximity bounds
         int i=0;
         synchronized(worldProxBounds) {
             for(BoundingVolume lb : localProxBounds) {
                 worldProxBounds[i] = lb.clone(worldProxBounds[i]);
                 worldTransform.transform(worldProxBounds[i]);
                 i++;
             }
         }
     }
 
     /**
      * The view cells transform has changed so update our internal structures
      * @param cell
      */
     public void viewCellMoved(CellID viewCellID, CellTransform viewCellTransform) {
         Vector3f viewCellWorldTranslation = viewCellTransform.getTranslation(null);
 
         // View Cell has moved
         synchronized(worldProxBounds) {
             BoundingVolume currentContainer = null;
             int currentContainerIndex=-1;      // -1 = not in any bounding volume
             int i=0;
             while(i<worldProxBounds.length) {
                 if (worldProxBounds[i].contains(viewCellWorldTranslation)) {
                     currentContainer = worldProxBounds[i];
                     currentContainerIndex = i;
                 } else {
                     i=worldProxBounds.length; // Exit the while
                 }
                 i++;
             }
 
             // At this point, we know which bounds (if any) the viewCell
             // is currently contained by. Now we need to check and see if
             // it used to be in different bounds, the same bounds
             // or no bounds at all to decide if this represents an enter or
             // exit event.
 
             // Check to see if we have a record of this viewCell's position.
             int lastContainerIndex = -1;
             if(this.lastContainerIndexMap.containsKey(viewCellID))
                 lastContainerIndex = this.lastContainerIndexMap.get(viewCellID);
 
             // If they've changed, we need to look closer.
             // if they haven't changed, then it means an avatar
             // moved but is still in their same bounds
 
             // There is some uncertainty here in the multiple-bounds case.
             // When you move from one contained bound to another, what events
             // should fire? 
             if (lastContainerIndex!=currentContainerIndex) {
                 if (currentContainerIndex<lastContainerIndex) {
                     // EXIT
                     proximityListener.viewEnterExit(false, this.lastContainerMap.get(viewCellID), lastContainerIndex, viewCellID);
 
                     // remove this user from the map if they're currently contained by no bounds object
                     if(currentContainerIndex==-1) {
                         this.lastContainerMap.remove(viewCellID);
                         this.lastContainerIndexMap.remove(viewCellID);
                     }
                 } else {
                     // ENTER
                     proximityListener.viewEnterExit(true, currentContainer, currentContainerIndex, viewCellID);
 
                     // Add this new user to the map. This will overwrite previous containers
                     // in the case where an avatar has moved between containers.
                     lastContainerMap.put(viewCellID, currentContainer);
                     lastContainerIndexMap.put(viewCellID, currentContainerIndex);
                 }
             }
         }
     }
 
    // This is implemented on the server, but not on the client because the data
    // we need to do it properly doesn't really exist. Check ServerProximityListenerRecord
    // to see what the process should look like on the client.
    public void userLoggedOut(WonderlandClientID wcid) {
        throw new UnsupportedOperationException("Not supported yet. This feature only works properly on the server.");
    }

     @Override
     public boolean equals(Object o) {
         if (!(o instanceof ProximityListenerRecord))
             return false;
 
         return ((ProximityListenerRecord)o).proximityListener==proximityListener;
     }
 
     @Override
     public int hashCode() {
         int hash = 7;
         hash = 89 * hash + (this.proximityListener != null ? this.proximityListener.hashCode() : 0);
         return hash;
     }
 
     /**
      * Wrapper for the listener, client and server listeners have a slightly
      * different interface
      */
     public interface ProximityListenerWrapper extends Serializable {
         public void viewEnterExit(boolean enter, BoundingVolume proximityVolume, int proximityIndex, CellID viewCellID );
     }
 }
