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
 package org.jdesktop.wonderland.server.spatial.impl;
 
 import com.jme.bounding.BoundingSphere;
 import com.jme.math.Vector3f;
 import com.sun.sgs.app.ManagedObject;
 import com.sun.sgs.auth.Identity;
 import com.sun.sgs.kernel.KernelRunnable;
 import com.sun.sgs.service.DataService;
 import java.io.Serializable;
 import java.math.BigInteger;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.LinkedHashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Set;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import org.jdesktop.wonderland.common.ThreadManager;
 import org.jdesktop.wonderland.common.cell.AvatarBoundsHelper;
 import org.jdesktop.wonderland.common.cell.CellID;
 import org.jdesktop.wonderland.common.cell.CellStatus;
 import org.jdesktop.wonderland.common.cell.CellTransform;
 import org.jdesktop.wonderland.server.cell.CellDescription;
 import org.jdesktop.wonderland.server.cell.ViewCellCacheMO;
 import org.jdesktop.wonderland.server.spatial.ViewUpdateListener;
 
 /**
  * The server side view cache for a specific view. 
  *
  * @author paulby
  */
 class ViewCache {
 
     private static final Logger logger = Logger.getLogger(ViewCache.class.getName());
     private SpaceManager spaceManager;
     private SpatialCellImpl viewCell;
     private final HashMap<SpatialCell, Integer> rootCells = new HashMap(); // The rootCells visible in this cache
 
     private final HashSet<Space> spaces = new HashSet();              // Spaces that intersect this caches world bounds
 
     private Vector3f lastSpaceValidationPoint = null;   // The view cells location on the last space revalidation
 
     private static final float REVAL_DISTANCE_SQUARED = (float) Math.pow(SpaceManagerGridImpl.SPACE_SIZE/4, 2);
 //    private static final float REVAL_DISTANCE_SQUARED = 1f;
 
     private final LinkedList<CacheUpdate> pendingCacheUpdates = new LinkedList();
 
 //    private ScheduledExecutorService spaceLeftProcessor = Executors.newSingleThreadScheduledExecutor();
 
     private Identity identity;
     private CacheProcessor cacheProcessor;
     private BigInteger cellCacheId;
     private DataService dataService;
 
     private BoundingSphere proximityBounds = new BoundingSphere(AvatarBoundsHelper.PROXIMITY_SIZE, new Vector3f());
 
     private final LinkedHashMap<CellID, ViewUpdateListenerContainer> viewUpdateListeners = new LinkedHashMap();
 
     private Vector3f v3f = new Vector3f();      // Temporary vector
 
     public ViewCache(SpatialCellImpl cell, SpaceManager spaceManager, Identity identity, BigInteger cellCacheId) {
         this.viewCell = cell;
         this.spaceManager = spaceManager;
         this.identity = identity;
         this.cellCacheId = cellCacheId;
         cacheProcessor = new CacheProcessor();
         cacheProcessor.start();
         dataService = UniverseImpl.getUniverse().getDataService();
     }
 
     public SpatialCellImpl getViewCell() {
         return viewCell;
     }
 
     private void viewCellMoved(final CellTransform worldTransform) {
         worldTransform.getTranslation(v3f);
 
         if (lastSpaceValidationPoint==null ||
             lastSpaceValidationPoint.distanceSquared(v3f)>REVAL_DISTANCE_SQUARED) {
 
             revalidateSpaces();
             
             if (lastSpaceValidationPoint==null)
                 lastSpaceValidationPoint = new Vector3f(v3f);
             else
                 lastSpaceValidationPoint.set(v3f);
         }
 
         UniverseImpl.getUniverse().scheduleTransaction(
                 new KernelRunnable() {
 
             public String getBaseTaskType() {
                 return ViewCache.class.getName() + ".MoveNotifier";
             }
 
             public void run() throws Exception {
                 synchronized(viewUpdateListeners) {
                     for(ViewUpdateListenerContainer cont : viewUpdateListeners.values())
                         cont.notifyListeners(worldTransform);
                 }
             }
         }, identity);
 
     }
 
     void login() {
         // Trigger a revalidation
         cellMoved(viewCell, viewCell.getWorldTransform());
 
         // notify listener of login
         UniverseImpl.getUniverse().scheduleTransaction(
                 new KernelRunnable() {
 
             public String getBaseTaskType() {
                 return ViewCache.class.getName() + ".LoginNotifier";
             }
 
             public void run() throws Exception {
                 synchronized(viewUpdateListeners) {
                     for(ViewUpdateListenerContainer cont : viewUpdateListeners.values())
                         cont.notifyListenersLogin();
                 }
             }
         }, identity);
     }
 
     void logout() {
 
         cacheProcessor.quit();
 
         synchronized(spaces) {
             for(Space sp : spaces)
                 sp.removeViewCache(this);
             spaces.clear();
         }
         rootCells.clear();
         synchronized(pendingCacheUpdates) {
             pendingCacheUpdates.clear();
         }
 
         // notify listener of logout
         UniverseImpl.getUniverse().scheduleTransaction(
                 new KernelRunnable() {
 
             public String getBaseTaskType() {
                 return ViewCache.class.getName() + ".LogoutNotifier";
             }
 
             public void run() throws Exception {
                 synchronized(viewUpdateListeners) {
                     for(ViewUpdateListenerContainer cont : viewUpdateListeners.values())
                         cont.notifyListenersLogout();
                 }
             }
         }, identity);
         
 //        System.err.println("-----------------> LOGOUT");
     }
 
     void addViewUpdateListener(CellID cellID, ViewUpdateListener listener) {
         synchronized(viewUpdateListeners) {
             viewUpdateListeners.put(cellID, new ViewUpdateListenerContainer(cellID, listener));
         }
     }
 
     void removeViewUpdateListener(CellID cellID, ViewUpdateListener listener) {
         synchronized(viewUpdateListeners) {
             viewUpdateListeners.remove(cellID);
         }
     }
 
     /**
      * Notification that a cell has moved, call by SpatialCell
      * @param cell
      * @param worldTransform
      */
     void cellMoved(SpatialCellImpl cell, CellTransform worldTransform) {
         if (cell==viewCell) {
             // Process view movement immediately
             viewCellMoved(worldTransform);
         } else {
             synchronized(pendingCacheUpdates) {
                 pendingCacheUpdates.add(new CacheUpdate(cell, worldTransform));
             }
         }
     }
 
     /**
      * Notification that a cell's properties have changed, and that clients
      * may want to reevaluate it
      * @param cell
      */
     void cellRevalidated(SpatialCellImpl cell) {
         synchronized(pendingCacheUpdates) {
             pendingCacheUpdates.add(new CacheUpdate(cell));
         }
     }
 
     void cellDestroyed(SpatialCell cell) {
         CellID cellID = ((SpatialCellImpl)cell).getCellID();
         removeViewUpdateListener(cellID, null);
     }
 
     /**
      * Revalidate the entire cache, because the user who owns the cache
      * has changed.
      */
     void revalidate() {
         synchronized(pendingCacheUpdates) {
             pendingCacheUpdates.add(new CacheUpdate(spaces));
         }
     }
 
     void childCellAdded(SpatialCellImpl child) {
         viewCell.acquireRootReadLock();
 
         try {
             synchronized(pendingCacheUpdates) {
                 logger.fine("ViewCache childCellAdded");
                 pendingCacheUpdates.add(new CacheUpdate(child, (SpatialCellImpl) child.getParent(), true));
             }
         } finally {
             viewCell.releaseRootReadLock();
         }
 
     }
 
     void childCellRemoved(SpatialCellImpl parent, SpatialCellImpl child) {
         viewCell.acquireRootReadLock();
 
         try {
             synchronized(pendingCacheUpdates) {
                 logger.fine("ViewCache childCellRemoved "+child.getCellID());
                 pendingCacheUpdates.add(new CacheUpdate(child, parent, false));
             }
         } finally {
             viewCell.releaseRootReadLock();
         }
 
     }
 
     /**
      * Called to add a root cell when the root cell is added to a space with which
      * this cache is already registered
      * @param rootCell
      */
     void rootCellAdded(SpatialCellImpl rootCell) {
         viewCell.acquireRootReadLock();
 
         try {
             synchronized(pendingCacheUpdates) {
                 logger.fine("ViewCache rootCellAdded");
                 pendingCacheUpdates.add(new CacheUpdate(rootCell, null, true));
             }
         } finally {
             viewCell.releaseRootReadLock();
         }
     }
 
     void rootCellRemoved(SpatialCellImpl rootCell) {
        // Don't remove the caches view cell
        if (rootCell==viewCell) {
            // If we are called with our ViewCell then we have warped and
            // spaces need to be revalidated. Fix for issue 717
            revalidateSpaces();
            return;
        }

         viewCell.acquireRootReadLock();
 
         try {
             synchronized(pendingCacheUpdates) {
                 logger.fine("ViewCache rootCellRemoved");
                 pendingCacheUpdates.add(new CacheUpdate(rootCell, null, false));
             }
         } finally {
             viewCell.releaseRootReadLock();
         }
     }
     /**
      * Update the set of spaces which intersect with this caches world bounds
      */
     private void revalidateSpaces() {
         viewCell.acquireRootReadLock();
 
         try {
             HashSet<Space> oldSpaces = (HashSet<Space>) spaces.clone();
 
             proximityBounds.setCenter(viewCell.getWorldTransform().getTranslation(null));
 
             Iterable<Space> newSpaces = spaceManager.getEnclosingSpace(proximityBounds);
 //            System.err.println("ViewCell Bounds "+proximityBounds);
 //            StringBuffer buf = new StringBuffer("View in spaces ");
 
             StringBuffer logBuf = null;
             if (logger.isLoggable(Level.FINE))
                 logBuf = new StringBuffer();
 
             for(Space sp : newSpaces) {
 //                buf.append(sp.getName()+", ");
                 if (spaces.add(sp)) {
                     if (logBuf!=null)
                         logBuf.append(sp.getName()+":"+sp.getRootCells().size()+" ");
                     // Entered a new space
                     synchronized(pendingCacheUpdates) {
                         pendingCacheUpdates.add(new CacheUpdate(sp, true));
                     }
                     sp.addViewCache(this);
                 }
                 oldSpaces.remove(sp);
             }
 
             if (logBuf!=null && logBuf.length()>0) {
                 logBuf.insert(0,"View Entering spaces ");
                 logger.fine(logBuf.toString());
                 logBuf.setLength(0);
             }
 //
 //            System.err.println(buf.toString());
 //
 //            System.out.println("Old spaces cut "+oldSpaces.size());
 //            buf = new StringBuffer("View leavoing spaces ");
             for(Space sp : oldSpaces) {
 //                buf.append(sp.getName()+", ");
                 sp.removeViewCache(this);
                 spaces.remove(sp);
 
                 if (logBuf!=null) {
                     logBuf.append(sp.getName()+" ");
                 }
 
                 // We don't remove the space cells immediately in case the user
                 // is moving along the border of the space
 
                 // TODO this is flawed, we need to track pending removes and make changes
                 // if the user moves so that the cell is visible again.
 
     //            spaceLeftProcessor.schedule(new CacheUpdate(sp, false), 30, TimeUnit.SECONDS);
 
                 // In the meantime update the cache immediately
                 synchronized(pendingCacheUpdates) {
                     pendingCacheUpdates.add(new CacheUpdate(sp,false));
                 }
             }
 
             if (logBuf!=null && logBuf.length()>0) {
                 logBuf.insert(0, "View Leaving spaces ");
                 logger.fine(logBuf.toString());
             }
 
         } finally {
             viewCell.releaseRootReadLock();
         }
     }
 
     class CacheProcessor extends Thread {
         boolean quit = false;
 
         public CacheProcessor() {
             super(ThreadManager.getThreadGroup(),"CacheProcessor");
         }
 
         @Override
         public void run() {
             // TODO add update notifcation instead of just of the short sleep
             Collection<CacheUpdate> updateList;
 
             while(!quit) {
                 synchronized(pendingCacheUpdates) {
                     updateList = (Collection<CacheUpdate>) pendingCacheUpdates.clone();
                     pendingCacheUpdates.clear();
                 }
 
                 for(CacheUpdate update : updateList) {
                     update.run();
                 }
                 try {
                     Thread.sleep(500);
                 } catch (InterruptedException ex) {
                 }
             }
         }
 
         public void quit() {
             quit = true;
             cacheProcessor.interrupt();
         }
     }
 
 
     class CacheUpdate implements Runnable {
 
         private SpatialCellImpl cell;
         private SpatialCellImpl parentCell;
         private CellTransform worldTransform;
         private Space space;
         private Set<Space> spaces;
 
         private static final int VIEW_MOVED = 0;
         private static final int CELL_MOVED = 1;
         private static final int EXIT_SPACE = 2;
         private static final int ENTER_SPACE = 3;
         private static final int CELL_ADDED = 4;
         private static final int CELL_REMOVED = 5;
         private static final int CELL_REVALIDATED = 6;
         private static final int CACHE_REVALIDATED = 7;
 
         private int jobType;
 
         /**
          * A cacheUpdate caused by a cell updating it's worldTransform
          * @param cell
          * @param worldTransform
          */
         public CacheUpdate(SpatialCellImpl cell, CellTransform worldTransform) {
             this.cell = cell;
             this.worldTransform = worldTransform;
             if (cell==viewCell)
                 jobType = VIEW_MOVED;
             else
                 jobType = CELL_MOVED;
         }
 
         /**
          * Enter or exit a space
          * @param space
          * @param enter
          */
         public CacheUpdate(Space space, boolean enter) {
             if (enter) {
                 jobType = ENTER_SPACE;
             } else {
                 jobType = EXIT_SPACE;
             }
             this.space = space;
         }
 
         /**
          * RootCell added/removed
          * @param rootCell
          * @param add
          */
         public CacheUpdate(SpatialCellImpl rootCell, SpatialCellImpl parent, boolean add) {
             jobType = (add) ? CELL_ADDED : CELL_REMOVED;
             this.cell = rootCell;
             this.parentCell = parent;
         }
 
         /**
          * Cell revalidated
          */
         public CacheUpdate(SpatialCellImpl cell) {
             jobType = CELL_REVALIDATED;
             this.cell = cell;
         }
 
         /**
          * Cache revalidated
          */
         public CacheUpdate(Set<Space> spaces) {
             jobType = CACHE_REVALIDATED;
             this.spaces = spaces;
         }
 
         public void run() {
             Collection<SpatialCellImpl> spaceRoots;
             List<CellDescription> cells = new ArrayList<CellDescription>();
             ViewCacheUpdateType type = null;
 
             switch(jobType) {
                 case VIEW_MOVED:
                     viewCellMoved(worldTransform);
                     break;
                 case CELL_MOVED:
                     // Check for cache enter/exit
                     break;
                 case EXIT_SPACE:
                     type = ViewCacheUpdateType.UNLOAD;
                     spaceRoots = space.getRootCells();
 
                     synchronized(rootCells) {
                         for(SpatialCellImpl root : spaceRoots) {
                             removeRootCellImpl(root, cells);
                         }
                     }
                     break;
                 case ENTER_SPACE:
                     type = ViewCacheUpdateType.LOAD;
                     spaceRoots = space.getRootCells();
 
 //                    System.err.println("EnteringSpace "+space.getName()+"  roots "+spaceRoots.size());
 
                     synchronized(rootCells) {
                         for(SpatialCellImpl root : spaceRoots) {
                             addRootCellImpl(root, cells);
                         }
                     }
                     break;
                 case CELL_ADDED:
                     if (parentCell==null) {
                         type = ViewCacheUpdateType.LOAD;
                         synchronized(rootCells) {
                             addRootCellImpl(cell, cells);
                         }
                     } else {
                         type = ViewCacheUpdateType.LOAD;
                         addOrRemoveSubgraphCellImpl(cell, cells);
                     }
                     break;
                 case CELL_REMOVED:
 //                    System.err.println("REMOVED from parent "+parentCell);
                     if (parentCell==null) {
                         type = ViewCacheUpdateType.UNLOAD;
                         synchronized(rootCells) {
                             removeRootCellImpl(cell, cells);
                         }
                     } else {
                         type = ViewCacheUpdateType.UNLOAD;
                         addOrRemoveSubgraphCellImpl(cell, cells);
                     }
                     break;
                 case CELL_REVALIDATED:
                     type = ViewCacheUpdateType.REVALIDATE;
                     cells.add(new CellDesc(cell.getCellID()));
                     break;
                 case CACHE_REVALIDATED:
                     type = ViewCacheUpdateType.REVALIDATE;
 
                     // find *all* the cells in this cache.  Yikes!
                     synchronized(rootCells) {
                         for (Space s : spaces) {
                             for (SpatialCellImpl root : s.getRootCells()) {
                                 root.acquireRootReadLock();
                                 try {
                                     cells.add(new CellDesc(root.getCellID()));
                                     processChildCells(cells, root, CellStatus.ACTIVE);
                                 } finally {
                                     root.releaseRootReadLock();
                                 }
                             }
                         }
                     }
                     break;
             }
 
             if (cells.size() > 0 && type != null) {
 //                System.err.println("Scheduling "+type);
 //                for(CellDescription c : cells) {
 //                    System.err.print(c.getCellID()+", ");
 //                }
 //                System.err.println();
                 UniverseImpl.getUniverse().scheduleQueuedTransaction(
                         new ViewCacheUpdateTask(cells, type), identity, ViewCache.this);
             }
         }
 
         /**
          * A non root cell has been added or removed, traverse the new subgraph
          * and add/remove all the cells
          * 
          * @param child the root of the subgraph
          * @param newCells the set of cells in the subgraph (including child)
          */
         private void addOrRemoveSubgraphCellImpl(SpatialCellImpl child, List<CellDescription> newCells) {
             child.acquireRootReadLock();
             try {
                 newCells.add(new CellDesc(child.getCellID()));
                 processChildCells(newCells, child, null);       // The status field is not used, so set it to null
             } finally {
                 child.releaseRootReadLock();
             }
         }
 
         /**
          * Callers must be synchronized on rootCells
          * @param root the root of the graph
          * @param newCells the cummalative set of new cells that have been added
          */
         private void addRootCellImpl(SpatialCellImpl root, List<CellDescription> newCells) {
             Integer refCountI = rootCells.get(root);
             int refCount;
             if (refCountI==null) {
                 root.acquireRootReadLock();
                 try {
                     newCells.add(new CellDesc(root.getCellID()));
                     processChildCells(newCells, root, CellStatus.ACTIVE);
                 } finally {
                     root.releaseRootReadLock();
                 }
                 refCount=1;
             } else {
                 refCount = refCountI.intValue();
                 refCount++;
             }
 
 //            System.err.println("***** Adding root "+root.getCellID()+" ref count "+refCount);
 
             rootCells.put(root, refCount);
         }
 
         /**
          * Callers must be synchronized on rootCells
          * @param root
          * @param oldCells the cummalative set of cells that are being removed
          */
         private void removeRootCellImpl(SpatialCellImpl root, List<CellDescription> oldCells) {
             Integer refCountI = rootCells.get(root);
 
             if (refCountI==null)
                 return;
 
             int refCount = refCountI.intValue();
 
             if (refCount==1) {
                 root.acquireRootReadLock();
                 try {
                     oldCells.add(new CellDesc(root.getCellID()));
                     processChildCells(oldCells, root, CellStatus.DISK);
                 } finally {
                     root.releaseRootReadLock();
                 }
                 refCount = 0;
                 rootCells.remove(root);
             } else {
                 refCount--;
                 rootCells.put(root, refCount);
             }
 
 //            System.err.println("****** Removing root "+root.getCellID()+" ref count "+refCount);
 
         }
 
         private void processChildCells(List<CellDescription> cells, SpatialCellImpl parent, CellStatus status) {
 //            System.err.println("Processing Child Cells "+parent.getCellID()+"  "+parent.getChildren()+"  "+status);
             if (parent.getChildren()==null)
                 return;
             
             for(SpatialCellImpl child : parent.getChildren()) {
                 cells.add(new CellDesc(child.getCellID()));
                 processChildCells(cells, child, status);
             }
         }
 
     }
 
     private enum ViewCacheUpdateType { LOAD, REVALIDATE, UNLOAD };
     class ViewCacheUpdateTask implements KernelRunnable {
 
         private Collection<CellDescription> cells;
         private ViewCacheUpdateType type;
 
         public ViewCacheUpdateTask(Collection<CellDescription> newCells,
                                    ViewCacheUpdateType type)
         {
             cells = newCells;
             this.type = type;
         }
 
         public String getBaseTaskType() {
             return KernelRunnable.class.getName();
         }
 
         public void run() throws Exception {
             ViewCellCacheMO cacheMO = (ViewCellCacheMO) dataService.createReferenceForId(cellCacheId).get();
             switch (type) {
                 case LOAD:
                     // Check security and generate appropriate load messages
                     cacheMO.generateLoadMessagesService(cells);
                     break;
                 case REVALIDATE:
                     cacheMO.revalidateCellsService(cells);
                     break;
                 case UNLOAD:
                     // No need to check security, just generate unload messages
                     cacheMO.generateUnloadMessagesService(cells);
             }
 
 //            StringBuffer buf = new StringBuffer();
 //            for(CellDescription c : cells)
 //                buf.append(c.getCellID()+", ");
 //            logger.warning("--------> DS UpdateTask "+viewCell.getCellID()+"  loading "+loadCells+"  "+cells.size()+"  "+buf.toString());
             
         }
 
     }
 
     static class CellDesc implements CellDescription, Serializable {
 
         private CellID cellID;
 
         public CellDesc(CellID cellID) {
             this.cellID = cellID;
         }
 
         public CellID getCellID() {
             return cellID;
         }
     }
 
     class ViewUpdateListenerContainer {
         private CellID cellID;
         private ViewUpdateListener viewUpdateListener;
 
         public ViewUpdateListenerContainer(CellID cellID, ViewUpdateListener listener) {
             this.cellID = cellID;
             this.viewUpdateListener = listener;
 
             if (listener instanceof ManagedObject) {
                 throw new RuntimeException("ManagedObject listeners support not implemented yet");
             }
         }
 
         public void notifyListenersLogin() {
             viewUpdateListener.viewLoggedIn(cellID, viewCell.getCellID());
         }
 
         public void notifyListeners(final CellTransform viewWorldTransform) {
             viewUpdateListener.viewTransformChanged(cellID, viewCell.getCellID(), viewWorldTransform);
         }
 
         public void notifyListenersLogout() {
             viewUpdateListener.viewLoggedOut(cellID, viewCell.getCellID());
         }
 
         @Override
         public boolean equals(Object o) {
             if (o instanceof ViewUpdateListenerContainer) {
                 ViewUpdateListenerContainer c = (ViewUpdateListenerContainer) o;
                 if (c.cellID.equals(cellID) && c.viewUpdateListener==viewUpdateListener)
                     return true;
             }
 
             return false;
         }
 
         @Override
         public int hashCode() {
             int hash = 7;
             hash = 29 * hash + (this.cellID != null ? this.cellID.hashCode() : 0);
             hash = 29 * hash + (this.viewUpdateListener != null ? this.viewUpdateListener.hashCode() : 0);
             return hash;
         }
     }
 }
