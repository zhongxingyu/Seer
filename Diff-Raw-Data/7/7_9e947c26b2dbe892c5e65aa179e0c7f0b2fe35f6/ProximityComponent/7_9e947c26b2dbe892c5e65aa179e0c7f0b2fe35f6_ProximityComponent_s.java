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
 package org.jdesktop.wonderland.client.cell;
 
 import com.jme.bounding.BoundingVolume;
import java.util.HashSet;
 import org.jdesktop.wonderland.common.ExperimentalAPI;
 import org.jdesktop.wonderland.common.cell.CellStatus;
 import org.jdesktop.wonderland.common.cell.CellTransform;
 import org.jdesktop.wonderland.common.cell.CellID;
 import org.jdesktop.wonderland.common.cell.ProximityListenerRecord;
 
 /**
  * <p>
  * Provides a mechanism for listener notification when the local view cell
  * enters/exits a set of bounds for a cell. 
  * </p>
  * <p>
  * The bounds must be ordered from largest to smallest, thus localBounds[i]
  * must enclose localBounds[i+1]. The listeners will be notified as the View
  * enters each subsequent bounding volume and then notified the view exits each
  * volume.
  * </p>
  * <p>
  * For example given a set of Bounding Spheres with the same center and radii of
  * 10, 5, 2. As the ViewCell moves from outside to the center of the spheres the
  * listeners will be called with
  * </p>
  * <p>
  * enter, 10 <br>
  * enter, 5 <br>
  * enter, 2 <br>
  * </p>
  * <p>
  * then as the user moves away from the center the following sequence of exits
  * will be called
  * </p>
  * <p>
  * exit, 2 <br>
  * exit, 5 <br>
  * exit, 10 <br>
  * </p>
  * 
  * 
  * @author paulby
  */
 @ExperimentalAPI
 public class ProximityComponent extends CellComponent {
 
     private ViewTransformListener viewTransformListener=null;
     private CellTransformListener  cellTransformListener=null;
     
    private HashSet<ProximityListenerRecord> listenerRecords = new HashSet();
     
     /**
      * Set a list of bounds for which the system will track view enter/exit for
      * this cell. When the view enters/exits one of these bounds the listener
      * will be called with the index of the bounds in the supplied array.
      * 
      * The bounds must be ordered from largest to smallest, thus localBounds[i]
      * must enclose localBounds[i+1]
      * 
      * @param cell the cell
      */
     public ProximityComponent(Cell cell) {
         super(cell);
     }
     
     /**
      * Add a proximity listener.
      * @param listener the listener that will be notified
      * @param localBounds the array of bounds (in cell local coordinates) for which the listener will be notified
      */
     public void addProximityListener(ProximityListener listener, BoundingVolume[] localBounds) {
         synchronized(listenerRecords) {
            ProximityListenerRecord lr = new ProximityListenerRecord(new ClientProximityListenerWrapper(cell,listener), localBounds);
            listenerRecords.add(lr);
            if (status!=null && status.ordinal()>=CellStatus.ACTIVE.ordinal())
                lr.updateWorldBounds(cell.getWorldTransform());
         }
     }
     
     /**
      * Remove the specified listener. Does nothing if the listener is not already
      * attached to this ProximityComponent
      * @param listener the listener to remove
      */
     public void removeProximityListener(ProximityListener listener) {
         synchronized(listenerRecords) {
             listenerRecords.remove(new ProximityListenerRecord(new ClientProximityListenerWrapper(cell, listener), new BoundingVolume[0]));
         }
     }
     
     @Override
     protected void setStatus(CellStatus status, boolean increasing) {
         synchronized(listenerRecords) {
             super.setStatus(status, increasing);
 
             switch(status) {
                 case ACTIVE :
                     if (increasing) {
                         if (viewTransformListener==null) {
                             viewTransformListener = new ViewTransformListener();
                             cellTransformListener = new CellTransformListener();
                         }
 
                         CellTransform worldTransform = cell.getWorldTransform();
                         for(ProximityListenerRecord l : listenerRecords)
                             l.updateWorldBounds(worldTransform);
 
                         cell.getCellCache().getViewCell().addTransformChangeListener(viewTransformListener);
                         cell.addTransformChangeListener(cellTransformListener);
                     }
                     break;
                 case DISK :
                     if (viewTransformListener!=null) {
                         // issue #664: add a check to make sure the view cell is not
                         // null.  cell and cell.getCellCache() are guaranteed not to
                         // be null, so there is no need to check them
                         if (cell.getCellCache().getViewCell() != null) {
                             cell.getCellCache().getViewCell().removeTransformChangeListener(viewTransformListener);
                         }
                         cell.removeTransformChangeListener(cellTransformListener);
                     }
                     break;
             }
         }
     }
     
     /**
      * Listen for view moves and check the view against our proximity bounds
      */
     class ViewTransformListener implements TransformChangeListener {
 
         
         public void transformChanged(Cell cell, ChangeSource source) {
             
             synchronized(listenerRecords) {
                 CellTransform worldTransform = cell.getWorldTransform();
                 for(ProximityListenerRecord l : listenerRecords) {
                     l.viewCellMoved(cell.getCellID(), worldTransform);
                 }
             }
         }
         
     }
     
     /**
      * Listen for the cell to which this component is attached moving. When
      * notified update the bounds
      */
     class CellTransformListener implements TransformChangeListener {
 
         public void transformChanged(Cell cell, ChangeSource source) {
             CellTransform worldTransform = cell.getWorldTransform();
             synchronized(listenerRecords) {
                 for(ProximityListenerRecord l : listenerRecords) {
                     l.updateWorldBounds(worldTransform);
                     
                     // Reevalute view position and send enter/exit events as necessary
                     l.viewCellMoved(cell.getCellID(), cell.getCellCache().getViewCell().getWorldTransform());
                 }
             }
         }
         
     }
 
     /**
      * Internal structure containing the array of bounds for a given listener.
      */
     class ClientProximityListenerWrapper implements ProximityListenerRecord.ProximityListenerWrapper {
 
         private ProximityListener listener;
         private Cell cell;
 
         public ClientProximityListenerWrapper(Cell cell, ProximityListener listener) {
             this.listener = listener;
             this.cell = cell;
         }
 
         public void viewEnterExit(boolean enter, BoundingVolume proximityVolume, int proximityIndex, CellID viewCellID) {
             listener.viewEnterExit(enter, cell, viewCellID, proximityVolume, proximityIndex);
         }
 
         @Override
         public boolean equals(Object o) {
             if (!(o instanceof ClientProximityListenerWrapper))
                 return false;
 
             if (((ClientProximityListenerWrapper)o).listener==listener)
                 return true;
 
             return false;
         }
 
         @Override
         public int hashCode() {
             int hash = 7;
             hash = 41 * hash + (this.listener != null ? this.listener.hashCode() : 0);
             return hash;
         }
     }
 }
