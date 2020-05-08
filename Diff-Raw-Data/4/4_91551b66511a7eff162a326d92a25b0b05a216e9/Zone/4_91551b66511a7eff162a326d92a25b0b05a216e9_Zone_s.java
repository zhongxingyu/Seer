 /*
  *  BlinzEngine - A library for large 2D world simultions and games.
  *  Copyright (C) 2009-2010  Blinz <gtalent2@gmail.com>
  *
  *  This program is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License version 3 as
  *  published by the Free Software Foundation.
  *
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package org.blinz.world;
 
 import org.blinz.util.User;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import java.util.ArrayList;
 import java.util.Vector;
 import java.util.concurrent.BrokenBarrierException;
 import java.util.concurrent.CyclicBarrier;
 import org.blinz.input.ClickEvent;
 import org.blinz.input.KeyEvent;
 import org.blinz.input.MouseEvent;
 import org.blinz.input.MouseWheelEvent;
 import org.blinz.util.Size;
 import org.blinz.util.Bounds;
 import org.blinz.util.Position;
 
 /**
  * Represents a realm for sprites to be processed and interact.
  * @author Blinz
  */
 public abstract class Zone extends ZoneObject {
 
     /**
      * Divides the task of processing a Zone up into as many practical units as the
      * given thread count suggests, then creates and manages the threads used to process
      * this Zone.
      * @author Blinz
      */
     final class ZoneProcessor {
 
         private class SynchronizedTaskManager {
 
             private int stage;
             private final int MANAGE_TIME = 0;
             private final int ZONE_UPDATE = 1;
             private final int USER_LISTENER_UPDATE = 2;
 
             final void enter() {
                 while (stage < 3) {
                     switch (stage()) {
                         case MANAGE_TIME:
                             cycleStartTime = System.currentTimeMillis();
                             getData().zoneTime = (System.currentTimeMillis() - initTime) - pauseTime;
                             break;
                         case ZONE_UPDATE:
                             getData().zoneCycles++;
                             update();
                             break;
                         case USER_LISTENER_UPDATE:
                             getData().userListeners.update();
                             break;
                     }
                 }
             }
 
             private final synchronized int stage() {
                 return stage++;
             }
 
             /**
              * Should contain only operations with trivial execution time. Resets
              * this object to run again next iteration.
              */
             private final void reset() {
                 stage = 0;
             }
         }
 
         /**
          *
          * @author Blinz
          */
         private class ZoneThread extends Thread {
 
             private Sector[] sectors;
 
             private ZoneThread(final ThreadGroup group) {
                 super(group, group.getName());
             }
 
             @Override
             public void run() {
                 while (isRunning) {
                     //pause if Zone is paused
                     if (getData().paused()) {
                         try {
                             Thread.sleep(250);
                         } catch (InterruptedException ex) {
                             Logger.getLogger(Zone.class.getName()).log(Level.SEVERE, null, ex);
                         }
                     }
 
                     syncTM.enter();
 
                     //cyclic barrier
                     try {
                         barriers[0].await();
                     } catch (InterruptedException ex) {
                         Logger.getLogger(ZoneThread.class.getName()).log(Level.SEVERE, null, ex);
                     } catch (BrokenBarrierException ex) {
                         Logger.getLogger(ZoneThread.class.getName()).log(Level.SEVERE, null, ex);
                     }
 
                     syncTM.reset();
 
                     //update Sectors
                     if (sectors != null) {
                         for (int i = 0; i < sectors.length; i++) {
                             sectors[i].update();
                         }
                     }
 
                     //cyclic barrier
                     try {
                         barriers[1].await();
                     } catch (InterruptedException ex) {
                         Logger.getLogger(ZoneThread.class.getName()).log(Level.SEVERE, null, ex);
                     } catch (BrokenBarrierException ex) {
                         Logger.getLogger(ZoneThread.class.getName()).log(Level.SEVERE, null, ex);
                     }
 
                     //post update Sectors
                     if (sectors != null) {
                         for (int i = 0; i < sectors.length; i++) {
                             sectors[i].postUpdate();
                         }
                     }
 
                     //cyclic barrier
                     try {
                         barriers[2].await();
                     } catch (InterruptedException ex) {
                         Logger.getLogger(ZoneThread.class.getName()).log(Level.SEVERE, null, ex);
                     } catch (BrokenBarrierException ex) {
                         Logger.getLogger(ZoneThread.class.getName()).log(Level.SEVERE, null, ex);
                     }
 
                     //delete those sprites that have been marked for deletion
                     {
                         for (BaseSprite s = getData().spritesToDelete.remove(0);
                                 s != null; s = getData().spritesToDelete.remove(0)) {
                             deleteSprite(s);
                         }
                     }
 
                     //cyclic barrier
                     try {
                         barriers[3].await();
                     } catch (InterruptedException ex) {
                         Logger.getLogger(ZoneThread.class.getName()).log(Level.SEVERE, null, ex);
                     } catch (BrokenBarrierException ex) {
                         Logger.getLogger(ZoneThread.class.getName()).log(Level.SEVERE, null, ex);
                     }
 
                     //Update Cameras
                     for (Camera camera = nextCamera(); camera != null; camera = nextCamera()) {
                         camera.internalUpdate();
                     }
 
                     //sleep
                     {
                         long sleepTime = (cycleStartTime + cycleIntervalTime) - System.currentTimeMillis();
                         if (sleepTime > 0) {
                             try {
                                 Thread.sleep(sleepTime);
                             } catch (InterruptedException ex) {
                                 Logger.getLogger(Zone.class.getName()).log(Level.SEVERE, null, ex);
                             }
                         }
                     }
 
                     //cyclic barrier
                     try {
                         barriers[4].await();
                     } catch (InterruptedException ex) {
                         Logger.getLogger(ZoneThread.class.getName()).log(Level.SEVERE, null, ex);
                     } catch (BrokenBarrierException ex) {
                         Logger.getLogger(ZoneThread.class.getName()).log(Level.SEVERE, null, ex);
                     }
                     currentCamera = 0;
                 }
             }
 
             /**
              * Sets the group of Sectors that this group is to contain.
              * @param sectors
              */
             final void setSectors(Sector[] sectors) {
                 this.sectors = sectors;
             }
         }
         private int currentCamera = 0;
         private final SynchronizedTaskManager syncTM = new SynchronizedTaskManager();
         private final CyclicBarrier[] barriers = new CyclicBarrier[5];
         private ZoneThread[] threads;
 
         /**
          *
          * @param zoneName the name of the Zone this thread group is for
          * @param threadCount number of threads to be used in this group
          */
         ZoneProcessor(String zoneName, int threadCount) {
             ThreadGroup group = new ThreadGroup(zoneName);
 
             threads = new ZoneThread[threadCount];
             for (int i = 0; i < threads.length; i++) {
                 threads[i] = new ZoneThread(group);
             }
 
             for (int i = 0; i < barriers.length; i++) {
                 barriers[i] = new CyclicBarrier(threadCount);
             }
 
             generateSectorGroups(getData().sectors);
         }
 
         /**
          * Starts the thread(s) in this ZoneProcesser an thereby starts this Zone.
          */
         final void start() {
             for (int i = 0; i < threads.length; i++) {
                 threads[i].start();
             }
         }
 
         final void generateSectorGroups(Sector[][] sectors) {
             final int sectorsPerThread = (sectors.length * sectors[0].length) / threads.length;
             final Position index = new Position();
 
            ArrayList<Sector> group = new ArrayList<Sector>();
             int currentThread = 0;
             while (index.x < sectors.length) {
                 while (index.y < sectors[index.x].length) {
                     group.add(sectors[index.x][index.y]);
                     if (group.size() == sectorsPerThread) {
                         Sector[] s = new Sector[group.size()];
                         for (int i = 0; i < group.size(); i++) {
                             s[i] = group.get(i);
                         }
                         threads[currentThread].setSectors(s);
                         group.clear();
                         currentThread++;
                     }
                     index.y++;
                 }
                 index.x++;
             }
             if (group.size() > 0) {
                 ZoneThread t = currentThread < threads.length - 1
                         ? threads[currentThread + 1] : threads[currentThread];
                 Sector[] s = new Sector[group.size()];
                 for (int i = 0; i < group.size(); i++) {
                     s[i] = group.get(i);
                 }
                 t.setSectors(s);
             }
         }
 
         /**
          * Used to help concurrently update the Cameras.
          * @return the next Camera to be updated
          * @throws ArrayIndexOutOfBoundsException
          */
         private final synchronized Camera nextCamera() throws ArrayIndexOutOfBoundsException {
             try {
                 return cameras.get(currentCamera++);
             } catch (ArrayIndexOutOfBoundsException e) {
                 return null;
             }
         }
     }
 
     private class ListTrimmer extends Thread {
 
         public ListTrimmer() {
             setDaemon(true);
         }
 
         @Override
         public void run() {
             while (isRunning) {
                 trimLists();
 
                 try {
                     Thread.sleep(600000);
                 } catch (InterruptedException ex) {
                     Logger.getLogger(Zone.class.getName()).log(Level.SEVERE, null, ex);
                 }
             }
         }
     }
     private Size size;
     private final Vector<Camera> cameras = new Vector<Camera>();
     private String name = "Zone";
     private long initTime;
     private long pauseTime = 0;
     /**
      * The amount of time to pass between loops.
      */
     private long cycleIntervalTime = 5;
     private long cycleStartTime;
     private Thread listTrimmer = new ListTrimmer();
     private boolean isRunning = false;
     private ZoneProcessor zoneProcessor;
     private static final Vector<Byte> recycledIDs = new Vector<Byte>();
     private static byte idIndex = 0;
 
     public Zone() {
         try {
             zoneID = generateZoneID();
         } catch (Exception ex) {
             Logger.getLogger(Zone.class.getName()).log(Level.SEVERE, null, ex);
         }
 
         setZoneData(zoneID, new ZoneData());
         getData().init(zoneID);
         size = getData().zoneSize;
     }
 
     /**
      * Returns the name of this Zone. "Zone" by default. This name will also be
      * assigned to threads processing this Zone when created.
      * @return the name of this Zone, "Zone" by default
      */
     public final String getName() {
         return name;
     }
 
     /**
      * Pauses the zone.
      * 
      * Note: This pause is not meant for short pauses, it will take at least 250
      * milliseconds before the Zone resumes execution even if resume() is called
      * immediately. Also pauses user input going to the sprites, but no the Zone.
      */
     public final void pause() {
         getData().pause();
     }
 
     /**
      * Unpauses the zone.
      */
     public final void unpause() {
         pauseTime = System.currentTimeMillis() - getData().zoneTime;
         getData().unpause();
     }
 
     /**
      * Sets the amount of time between cycles of execution for this Zone. Defaults
      * to 5 milliseconds.
      * @param interval the time between cycles of execution for this Zone in milliseconds
      */
     public final void setCycleInterval(long interval) {
         cycleIntervalTime = interval;
     }
 
     /**
      *
      * @return a new Size object representing the dimensions of this zone.
      */
     public final Size getSize() {
         return new Size(size);
     }
 
     /**
      *
      * @return int representing the width of this zone
      */
     public final int getWidth() {
         return size.width;
     }
 
     /**
      *
      * @return int representing the height of this zone
      */
     public final int getHeight() {
         return size.height;
     }
 
     /**
      * Adds the given sprite to this sprites zone.
      * @param sprite
      */
     public final void addSprite(BaseSprite sprite) {
         getData().addSprite(sprite);
     }
 
     /**
      * Creates threads for processing this Zone.
      * @param threads number of threads dedicated to this Zone
      */
     public final synchronized void start(int threads) {
         start("Zone", threads);
     }
 
     /**
      * Creates threads for processing this Zone.
      * @param name the name assigned to the threads processing this Zone
      * @param threads number of threads dedicated to this Zone
      */
     public final synchronized void start(String name, int threads) {
         if (!isRunning) {
             getData().setName(name);
             initTime = System.currentTimeMillis();
             isRunning = true;
             init();
             zoneProcessor = new ZoneProcessor(name, threads);
             zoneProcessor.generateSectorGroups(getData().sectors);
             listTrimmer.start();
             zoneProcessor.start();
         }
     }
 
     /**
      * Cause updating of this Zone to stop after the current cycle.
      */
     public final void stop() {
         isRunning = false;
     }
 
     /**
      * The given sprite will now recieve input given by the given User.
      * @param user
      * @param sprite
      */
     public final void addUserListeningSprite(User user, BaseSprite sprite) {
         getData().addUserListener(user, sprite);
     }
 
     /**
      * The given sprite will no longer recieve input given by the given User.
      * @param user
      * @param sprite
      */
     public final void removeUserListeningSprite(User user, BaseSprite sprite) {
         getData().removeUserListener(user, sprite);
     }
 
     /**
      * Sets the width of this Zone to the given value.
      * @param width
      */
     protected final void setWidth(int width) {
         size.setWidth(width);
         refactorSectors();
     }
 
     /**
      * Sets the height of this Zone to the given value.
      * @param height
      */
     protected final void setHeight(int height) {
         size.setHeight(height);
         refactorSectors();
     }
 
     /**
      * Sets the width and height of this Zone.
      * @param width
      * @param height
      */
     protected final void setSize(int width, int height) {
         size.setSize(width, height);
         refactorSectors();
     }
 
     /**
      *
      * @return a long representing the number of cycles the zone has gone through
      */
     protected final long cycles() {
         return getData().zoneCycles;
     }
 
     /**
      *
      * @return the amount of time the zone has been running minus the pause time
      */
     protected final long time() {
         return getData().zoneTime;
     }
 
     @Override
     protected abstract void init();
 
     /**
      * Passes the given data to the Zone and its sprites so that an extra pointer
      * does not have to be assigned to each sprite to reduce memory consumption.
      * @param sharedData
      */
     protected final void setSharedZoneData(SharedZoneData sharedData) {
         getData().data = sharedData;
     }
 
     @Override
     protected void finalize() {
         setZoneData(zoneID, null);
         recycledIDs.add(zoneID);
     }
 
     /**
      * Abstract method for updating the zone to be implemented by the developer.
      * Note: This method is not called while the Zone is paused.
      */
     protected abstract void update();
 
     /**
      * A stub method for listening to clicks. Implement as needed.
      * @param event contains data about the input
      */
     protected void buttonClicked(ClickEvent event) {
     }
 
     /**
      * A stub method for listening to mouse button presses. Implement as needed.
      * @param event contains data about the input
      */
     protected void buttonPressed(MouseEvent event) {
     }
 
     /**
      * A stub method for listening to mouse button releases. Implement as needed.
      * @param event contains data about the input
      */
     protected void buttonReleased(MouseEvent event) {
     }
 
     /**
      * A stub method for listening to the mouse wheel. Implement as needed.
      * @param event contains data about the input
      */
     protected void mouseWheelScroll(MouseWheelEvent event) {
     }
 
     /**
      * A stub method for listening to the keys pressed. Implement as needed.
      * @param event contains data about the input
      */
     protected void keyPressed(KeyEvent event) {
     }
 
     /**
      * A stub method for listening to the keys released. Implement as needed.
      * @param event contains data about the input
      */
     protected void keyReleased(KeyEvent event) {
     }
 
     /**
      * A stub method for listening to the key typed. Implement as needed.
      * @param event contains data about the input
      */
     protected void keyTyped(KeyEvent event) {
     }
 
     /**
      * Adds the given Camera to this Zone, to moniter the sprites in its area.
      * @param camera
      */
     final void addCamera(Camera camera) {
         cameras.add(camera);
 
         getData().registerZoneObject(camera);
     }
 
     /**
      * Removes the given Camera from this Zone.
      * @param camera
      */
     final void removeCamera(Camera camera) {
         cameras.remove(camera);
 
         for (int i = 0; i < getData().sectors.length; i++) {
             for (int n = 0; n < getData().sectors[i].length; n++) {
                 if (getData().sectors[i][n].intersects(camera.getX(), camera.getY(), camera.getWidth(), camera.getHeight())) {
                     getData().sectors[i][n].removeCamera(camera);
                     i = getData().sectors.length;
                     break;
                 }
             }
         }
     }
 
     /**
      * Places Sectors intersecting with the given Bounds into the given ArrayList.
      * @param bounds
      * @param sectors
      */
     final void getIntersectingSectors(Bounds bounds, ArrayList<Sector> sectors) {
         for (int i = 0; i < getData().sectors.length; i++) {
             for (int n = 0; n < getData().sectors[i].length; n++) {
                 if (getData().sectors[i][n].intersects(bounds)) {
                     sectors.add(getData().sectors[i][n]);
                 }
             }
         }
     }
 
     /**
      * Trims excessively large lists.
      */
     final void trimLists() {
         getData().trimLists();
         cameras.trimToSize();
     }
 
     /**
      * Generates a distinct zone id for a new Zone.
      * @return byte
      */
     private final synchronized byte generateZoneID() throws Exception {
         if (idIndex < 127) {
             return (byte) (++idIndex);
         } else if (recycledIDs.size() != 0) {
             return recycledIDs.remove(0);
         } else {
             throw new Exception("Out of Zone IDs. Max number of active Zones is 128.");
         }
 
     }
 
     /**
      * Deletes the given sprite from this Zone.
      * @param sprite
      */
     private final void deleteSprite(BaseSprite sprite) {
         Sector tl = getData().getSectorOf(sprite.getX(), sprite.getY());
         Sector br = getData().getSectorOf(sprite.getX() + sprite.getWidth(), sprite.getY() + sprite.getHeight());
         tl.deleteIntersectingSprite(sprite);
         if (sprite instanceof UpdatingSprite) {
             tl.deleteUpdatingSprite((UpdatingSprite) sprite);
         }
         if (tl != br) {
             br.deleteIntersectingSprite(sprite);
             Sector tr = getData().getSectorOf(sprite.getX() + sprite.getWidth(), sprite.getY());
             if (tr != br && tl != tr) {
                 getData().getSectorOf(sprite.getX(), sprite.getY() + sprite.getHeight()).deleteIntersectingSprite(sprite);
                 tr.deleteIntersectingSprite(sprite);
             }
 
         }
     }
 
     /**
      * Refactors the Sectors to fit the current size of this Zone.
      */
     private final synchronized void refactorSectors() {
         if (size.width == 0 || size.height == 0) {
             return;
         }
 
         if (size.width / getData().sectorSize.width + 1 <= getData().sectors.length
                 || size.height / getData().sectorSize.width + 1 <= getData().sectors[0].length) {
             return;
         }
 
         ArrayList<Sector> newSectors = new ArrayList<Sector>();
 
         Sector[][] sectors =
                 new Sector[size.width / getData().sectorSize.width + 1][size.height / getData().sectorSize.width + 1];
         for (int i = 0; i < sectors.length; i++) {
             for (int n = 0; n < sectors[i].length; n++) {
                 if (i >= getData().sectors.length || n >= getData().sectors[i].length) {
                     sectors[i][n] = new Sector(getData().sectorSize.width * i, getData().sectorSize.height * n);
                     newSectors.add(sectors[i][n]);
                 } else {
                     sectors[i][n] = getData().sectors[i][n];
                 }
             }
         }
 
         getData().sectors = sectors;
 
         for (int i = 0; i < newSectors.size(); i++) {
             getData().registerZoneObject(newSectors.get(i));
         }
 
         for (int i = 0; i
                 < sectors.length; i++) {
             for (int n = 0; n < sectors[i].length; n++) {
                 int ix = sectors[i][n].getX() / getData().sectorSize.width;
                 int iy = sectors[i][n].getY() / getData().sectorSize.height;
 
                 if (ix > 0) {
                     sectors[i][n].leftNeighbor = getData().sectors[ix - 1][iy];
                 } else {
                     sectors[i][n].leftNeighbor = null;
                 }
                 if (iy > 0) {
                     sectors[i][n].topNeighbor = getData().sectors[ix][iy - 1];
                 } else {
                     sectors[i][n].topNeighbor = null;
                 }
                 if (ix < getData().sectors.length - 1) {
                     sectors[i][n].rightNeighbor = getData().sectors[ix + 1][iy];
                 } else {
                     sectors[i][n].rightNeighbor = null;
                 }
                 if (iy < getData().sectors[ix].length - 1) {
                     sectors[i][n].bottomNeighbor = getData().sectors[ix][iy + 1];
                 } else {
                     sectors[i][n].bottomNeighbor = null;
                 }
             }
 
         }
         if (zoneProcessor != null) {
             zoneProcessor.generateSectorGroups(sectors);
         }
     }
 }
