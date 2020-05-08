 /*
  * GRAIL Real Time Localization System
  * Copyright (C) 2012 Rutgers University and Robert Moore
  * 
  * This program is free software; you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation; either version 2 of the License, or
  * (at your option) any later version.
  *  
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *  
  * You should have received a copy of the GNU General Public License along
  * with this program; if not, write to the Free Software Foundation, Inc.,
  * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
  */
 
 package org.makesense.sigvis;
 
 import java.awt.Component;
 import java.awt.geom.Point2D;
 import java.awt.geom.Rectangle2D;
 import java.awt.image.BufferedImage;
 import java.io.BufferedInputStream;
 import java.io.BufferedOutputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.net.URL;
 import java.util.Deque;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.Timer;
 import java.util.TimerTask;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.ConcurrentLinkedQueue;
 import java.util.concurrent.LinkedBlockingDeque;
 import java.util.zip.GZIPInputStream;
 import java.util.zip.GZIPOutputStream;
 
 import javax.imageio.ImageIO;
 import javax.swing.JOptionPane;
 import javax.swing.ProgressMonitor;
 import javax.swing.ProgressMonitorInputStream;
 
 import org.apache.mina.util.ConcurrentHashSet;
 import org.makesense.sigvis.structs.ChartItem;
 import org.makesense.sigvis.structs.SignalToDistanceItem;
 import org.makesense.sigvis.structs.SimpleChartItem;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.thoughtworks.xstream.XStream;
 import com.thoughtworks.xstream.annotations.XStreamAlias;
 import com.thoughtworks.xstream.annotations.XStreamOmitField;
 import com.thoughtworks.xstream.io.xml.StaxDriver;
 
 /**
  * A class which keeps a cache of data received from the World Model for a fixed
  * amount of time.
  * 
  * @author Robert Moore
  * 
  */
 public class DataCache2 implements Cloneable {
 
   /**
    * Logging facility for this class.
    */
   @XStreamOmitField
   private static final Logger log = LoggerFactory.getLogger(DataCache2.class);
 
   public enum ValueType {
     RSSI, VARIANCE
   };
 
   /**
    * How long time-sensitive data should be kept in the cache.
    */
   @XStreamAlias("maxCacheAge")
   protected long maxCacheAge = 1000 * 60 * 10;
 
   /**
    * Two-dimensional region bounds for the defined region.
    */
   @XStreamAlias("regionBounds")
   protected Rectangle2D regionBounds = null;
 
   /**
    * Image for the region background when drawing map-based panels.
    */
   @XStreamOmitField
   protected BufferedImage regionImage = null;
 
   /**
    * URL for region image. Stored on disk.
    */
   @XStreamAlias("regionImageUrl")
   protected String regionImageUrl = null;
 
   /**
    * UTF-16 URI of the region.
    */
   @XStreamAlias("regionUri")
   protected String regionUri = null;
 
   /**
    * Map of Receiver ID -> Transmitter ID -> Queue of Average RSSI values.
    */
   @XStreamAlias("averageRssiByRByT")
   protected final Map<String, Map<String, Deque<ChartItem<Float>>>> averageRssiByRByT = new ConcurrentHashMap<String, Map<String, Deque<ChartItem<Float>>>>();
 
   /**
    * Map of Receiver ID -> Transmitter ID -> Queue of RSSI variance values.
    */
   @XStreamAlias("varianceByRByT")
   protected final Map<String, Map<String, Deque<ChartItem<Float>>>> varianceRssiByRByT = new ConcurrentHashMap<String, Map<String, Deque<ChartItem<Float>>>>();
 
   /**
    * List of objects that care when receivers or fiduciary transmitters become
    * known to the data cache.
    */
   @XStreamOmitField
   protected final ConcurrentLinkedQueue<DataCache2Listener> listeners = new ConcurrentLinkedQueue<DataCache2Listener>();
 
   /**
    * Mapping from device (transmitter, receiver) ID values to their
    * 2-dimensional position within a region.
    */
   @XStreamAlias("deviceLocations")
   protected final ConcurrentHashMap<String, Point2D> deviceLocations = new ConcurrentHashMap<String, Point2D>();
 
   /**
    * List of receiver ID values returned by the world model.
    */
   @XStreamAlias("receiverIds")
   protected final Set<String> receiverIds = new ConcurrentHashSet<String>();
 
   /**
    * List of fiduciary transmitter ID values returned by the world model.
    */
   @XStreamAlias("fiduciaryTransmitterIds")
   protected final Set<String> fiduciaryTransmitterIds = new ConcurrentHashSet<String>();
 
   /**
    * Map of anchor sensor URI values to the anchor URIs.
    */
   @XStreamAlias("sensorToUri")
   protected final Map<String, String> sensorToUri = new ConcurrentHashMap<String, String>();
 
   /**
    * Map of queues containing the most recent signal-to-distance data for
    * receiver-fiduciary transmitter pairs. Mapped by receiver.
    */
   @XStreamAlias("sigToDistHistory")
   protected Map<String, Deque<SignalToDistanceItem>> sigToDistHistory = new ConcurrentHashMap<String, Deque<SignalToDistanceItem>>();
 
   /**
    * Flag to indicate whether this cache is a clone of another (live) cache.
    */
   @XStreamOmitField
   protected boolean isClone = false;
 
   /**
    * The time at which this cache was created. Useful for determining the time
    * of the latest valid data for a cloned cache.
    */
   @XStreamAlias("creationTs")
   private long creationTs;
 
   @XStreamOmitField
   protected final CacheStatsPanel statsPanel = new CacheStatsPanel(this);
   @XStreamOmitField
   protected final ConnectionHandler handler;
   @XStreamOmitField
   protected final Timer taskTimer = new Timer();
   @XStreamOmitField
   protected volatile int numRssiPoints = 0;
   @XStreamOmitField
   protected volatile int numVarPoints = 0;
 
   @XStreamOmitField
   protected volatile int numSigToDistPoints = 0;
 
   @XStreamOmitField
   protected volatile int numFidTxers = 0;
   @XStreamOmitField
   protected volatile int numRxers = 0;
   @XStreamOmitField
   protected long lastRssiUpdate = 0l;
   @XStreamOmitField
   protected long lastVarianceUpdate = 0l;
 
   public DataCache2(final ConnectionHandler handler){
     this(handler, System.currentTimeMillis());
   }
 
   protected DataCache2(final ConnectionHandler handler, final long created) {
     if (created > 0) {
       this.creationTs = created;
     } else {
       this.creationTs = System.currentTimeMillis();
     }
     this.handler =handler;
     this.handler.setCache(this);
     this.taskTimer.schedule(new TimerTask() {
 
       @Override
       public void run() {
         DataCache2.this.updateStats();
       }
     }, 1000, 1000);
 
     this.taskTimer.schedule(new TimerTask() {
       @Override
       public void run() {
         DataCache2.this.sweepCache();
       }
     }, 60000, 60000);
   }
 
   /**
    * Register a listener for new receivers and fiduciary transmitters.
    * 
    * @param listener
    */
   public void addListener(final DataCache2Listener listener) {
     this.listeners.add(listener);
   }
 
   /**
    * Register a listener for new receivers and fiduciary transmitters.
    * 
    * @param listener
    */
   public void removeListener(final DataCache2Listener listener) {
     this.listeners.remove(listener);
     if (this.listeners.isEmpty() && !this.isClone) {
       this.shutdown();
       this.disableStreaming();
 
     }
   }
 
   public void disableStreaming() {
     if (this.handler != null) {
       this.handler.noCacheListeners();
     }
   }
 
   public void addReceiver(final String receiverId) {
     this.receiverIds.add(receiverId);
     this.statsPanel.setNumRxers(++this.numRxers);
     this.lastRssiUpdate = System.currentTimeMillis();
     this.lastRssiUpdate = System.currentTimeMillis();
   }
 
   public void addFiduciaryTransmitter(final String transmitterId) {
     this.fiduciaryTransmitterIds.add(transmitterId);
     this.statsPanel.setNumFidTxers(++this.numFidTxers);
     this.lastRssiUpdate = System.currentTimeMillis();
     this.lastVarianceUpdate = System.currentTimeMillis();
   }
 
   public void addRssi(final String rxerSensor, final String txerSensor,
       final float value, final long timestamp) {
     // System.out.println("R: " + rxerSensor + " T: " + txerSensor + " -> " +
     // value);
     String rxer = this.sensorToUri.get(rxerSensor);
     if (rxer == null) {
       // log.warn("Unknown receiver sensor: " + rxerSensor);
       return;
     }
 
     String txer = this.sensorToUri.get(txerSensor);
     if (txer == null) {
       // log.warn("Unknown transmitter sensor: " + txerSensor);
       return;
     }
 
     Map<String, Deque<ChartItem<Float>>> transmitterItems = this.averageRssiByRByT
         .get(rxer);
 
     if (transmitterItems == null) {
       transmitterItems = new ConcurrentHashMap<String, Deque<ChartItem<Float>>>();
       this.averageRssiByRByT.put(rxer, transmitterItems);
     }
 
     Deque<ChartItem<Float>> rssiQueue = transmitterItems.get(txer);
     if (rssiQueue == null) {
       rssiQueue = new LinkedBlockingDeque<ChartItem<Float>>();
       transmitterItems.put(txer, rssiQueue);
     }
 
     SimpleChartItem<Float> theItem = new SimpleChartItem<Float>(
         Float.valueOf(value), timestamp);
     rssiQueue.offer(theItem);
     ++this.numRssiPoints;
     this.lastRssiUpdate = System.currentTimeMillis();
 
     long oldestTs = System.currentTimeMillis() - this.maxCacheAge;
 
     while (!rssiQueue.isEmpty()
         && rssiQueue.peek().getCreationTime() < oldestTs) {
       rssiQueue.poll();
       --this.numRssiPoints;
     }
 
     // Signal to distance update
     Point2D recPoint = this.getDeviceLocation(rxer);
     Point2D transPoint = this.getDeviceLocation(txer);
     if (recPoint == null || transPoint == null) {
       return;
     }
 
     float distance = (float) Math.sqrt(Math.pow(
         recPoint.getX() - transPoint.getX(), 2)
         + Math.pow(recPoint.getY() - transPoint.getY(), 2));
     SignalToDistanceItem newSigToDist = new SignalToDistanceItem(rxer, txer,
         distance, value);
     Deque<SignalToDistanceItem> history = this.sigToDistHistory.get(rxer);
     if (history == null) {
       history = new LinkedBlockingDeque<SignalToDistanceItem>();
       this.sigToDistHistory.put(rxer, history);
     }
     history.offer(newSigToDist);
     ++this.numSigToDistPoints;
     // Trim the old values
     while (!history.isEmpty() && history.peek().getCreationTime() < oldestTs) {
       history.poll();
       --this.numSigToDistPoints;
     }
     // End signal to distance
   }
 
   public void addVariance(final String rxerSensor, final String txerSensor,
       final float value, final long timestamp) {
     String rxer = this.sensorToUri.get(rxerSensor);
     if (rxer == null) {
       // log.warn("Unknown receiver sensor: " + rxerSensor);
       return;
     }
 
     String txer = this.sensorToUri.get(txerSensor);
     if (txer == null) {
       // log.warn("Unknown transmitter sensor: " + txerSensor);
       return;
     }
 
     Map<String, Deque<ChartItem<Float>>> transmitterItems = this.varianceRssiByRByT
         .get(rxer);
 
     if (transmitterItems == null) {
       transmitterItems = new ConcurrentHashMap<String, Deque<ChartItem<Float>>>();
       this.varianceRssiByRByT.put(rxer, transmitterItems);
     }
 
     Deque<ChartItem<Float>> varQueue = transmitterItems.get(txer);
     if (varQueue == null) {
       varQueue = new LinkedBlockingDeque<ChartItem<Float>>();
       transmitterItems.put(txer, varQueue);
     }
 
     SimpleChartItem<Float> theItem = new SimpleChartItem<Float>(
         Float.valueOf(value), timestamp);
     varQueue.offer(theItem);
     ++this.numVarPoints;
     this.lastVarianceUpdate = System.currentTimeMillis();
 
     long oldestTs = System.currentTimeMillis() - this.maxCacheAge;
 
     while (!varQueue.isEmpty() && varQueue.peek().getCreationTime() < oldestTs) {
       varQueue.poll();
       --this.numVarPoints;
     }
   }
 
   public List<ChartItem<Float>> getRssiList(final String receiver,
       final String transmitter) {
 
     Map<String, Deque<ChartItem<Float>>> receiverMap = this.averageRssiByRByT
         .get(receiver);
 
     if (receiverMap == null) {
       return null;
     }
 
     Deque<ChartItem<Float>> transmitterQueue = receiverMap.get(transmitter);
     if (transmitterQueue == null) {
       return null;
     }
 
     LinkedList<ChartItem<Float>> returnedList = new LinkedList<ChartItem<Float>>();
     returnedList.addAll(transmitterQueue);
     return returnedList;
   }
 
   public List<ChartItem<Float>> getVarianceList(final String receiver,
       final String transmitter) {
 
     Map<String, Deque<ChartItem<Float>>> receiverMap = this.varianceRssiByRByT
         .get(receiver);
 
     if (receiverMap == null) {
       return null;
     }
 
     Deque<ChartItem<Float>> transmitterQueue = receiverMap.get(transmitter);
     if (transmitterQueue == null) {
       return null;
     }
 
     LinkedList<ChartItem<Float>> returnedList = new LinkedList<ChartItem<Float>>();
     returnedList.addAll(transmitterQueue);
     return returnedList;
   }
 
   /**
    * Sets the location of a specified device within the defined region.
    * 
    * @param deviceId
    *          the ID of a device (receiver or transmitter).
    * @param location
    *          the 2-dimensional location of the device, specified as coordinates
    *          within the region.
    */
   public void setDeviceLocation(final String deviceId, final Point2D location) {
     this.deviceLocations.put(deviceId, location);
     if (this.receiverIds.contains(deviceId)) {
       for (DataCache2Listener listener : this.listeners) {
         listener.receiverAdded(deviceId);
       }
     } else if (this.fiduciaryTransmitterIds.contains(deviceId)) {
       for (DataCache2Listener listener : this.listeners) {
         listener.transmitterAdded(deviceId, true);
       }
     }
     this.lastRssiUpdate = System.currentTimeMillis();
     this.lastVarianceUpdate = System.currentTimeMillis();
   }
 
   public Point2D getDeviceLocation(final String deviceId) {
     return this.deviceLocations.get(deviceId);
   }
 
   public List<String> getDynamicTransmitterIds() {
     List<String> transmitters = new LinkedList<String>();
     // transmitters.addAll(this.dynamicTransmitterIds);
     return transmitters;
   }
 
   /**
    * Returns the current average RSSI value for a specific transmitter from a
    * specific receiver.
    * 
    * @param transmitter
    *          the transmitter that sent the packet
    * @param receiver
    *          the receiver that observed it
    * @return the most recent RSSI data from the aggregator, or {@link Float#NaN}
    *         if no data exists.
    */
   public float getCurrentRssi(final String transmitter, final String receiver) {
     ChartItem<Float> currItem = this.getCurrentRssiItem(transmitter, receiver);
     if (currItem == null) {
       return Float.NaN;
     }
     return currItem.getValue().floatValue();
   }
 
   public float getRssiAt(final String transmitter, final String receiver,
       long timeOffset, long window) {
 
     long desiredOrJustAfter = this.isClone ? this.creationTs - timeOffset
         : System.currentTimeMillis() - timeOffset;
 
     Map<String, Deque<ChartItem<Float>>> receiverSamples = this.averageRssiByRByT
         .get(receiver);
 
     if (receiverSamples == null) {
       // log.warn("No samples for {}", receiver);
       return Float.NaN;
     }
 
     Deque<ChartItem<Float>> transmitterSamples = receiverSamples
         .get(transmitter);
 
     if (transmitterSamples == null) {
       // log.warn("No samples for {}", transmitter);
       return Float.NaN;
     }
     float theValue = Float.NaN;
     // From oldest to newest
     for (Iterator<ChartItem<Float>> iter = transmitterSamples.iterator(); iter
         .hasNext();) {
       ChartItem<Float> someItem = iter.next();
       if (someItem.getCreationTime() < desiredOrJustAfter - window) {
         continue;
       }
       if (someItem.getCreationTime() >= desiredOrJustAfter) {
         break;
       }
 
       theValue = someItem.getValue().floatValue();
     }
 
     return theValue;
   }
 
   /**
    * Returns the current average RSSI value for a specific transmitter from a
    * specific receiver.
    * 
    * @param transmitter
    *          the transmitter that sent the packet
    * @param receiver
    *          the receiver that observed it
    * @return the most recent RSSI data from the aggregator, or {@link Float#NaN}
    *         if no data exists.
    */
   public ChartItem<Float> getCurrentRssiItem(final String transmitter,
       final String receiver) {
     Map<String, Deque<ChartItem<Float>>> receiverSamples = this.averageRssiByRByT
         .get(receiver);
 
     if (receiverSamples == null) {
       // log.warn("No samples for {}", receiver);
       return null;
     }
 
     Deque<ChartItem<Float>> transmitterSamples = receiverSamples
         .get(transmitter);
 
     if (transmitterSamples == null) {
       // log.warn("No samples for {}", transmitter);
       return null;
     }
 
     ChartItem<Float> mostRecent = transmitterSamples.peekLast();
 
     if (mostRecent == null) {
       return null;
     }
 
     if (mostRecent.getCreationTime() < System.currentTimeMillis()
         - this.maxCacheAge) {
       // receiverSamples.remove(transmitter);
       return null;
     }
 
     return mostRecent;
   }
 
   /**
    * Returns the current RSSI variance value for a specific transmitter from a
    * specific receiver.
    * 
    * @param transmitter
    *          the transmitter that sent the packet
    * @param receiver
    *          the receiver that observed it
    * @return the most recent RSSI data from the aggregator, or {@link Float#NaN}
    *         if no data exists.
    */
   public float getCurrentVariance(final String transmitter,
       final String receiver) {
     ChartItem<Float> currItem = this.getCurrentVarianceItem(transmitter,
         receiver);
     if (currItem == null) {
       return Float.NaN;
     }
     return currItem.getValue().floatValue();
   }
 
   /**
    * Returns the current RSSI variance value for a specific transmitter from a
    * specific receiver.
    * 
    * @param transmitter
    *          the transmitter that sent the packet
    * @param receiver
    *          the receiver that observed it
    * @return the most recent RSSI data from the aggregator, or {@link Float#NaN}
    *         if no data exists.
    */
   public ChartItem<Float> getCurrentVarianceItem(final String transmitter,
       final String receiver) {
     Map<String, Deque<ChartItem<Float>>> receiverSamples = this.varianceRssiByRByT
         .get(receiver);
 
     if (receiverSamples == null) {
       return null;
     }
 
     Deque<ChartItem<Float>> transmitterSamples = receiverSamples
         .get(transmitter);
 
     if (transmitterSamples == null) {
       return null;
     }
 
     ChartItem<Float> mostRecent = transmitterSamples.peekLast();
 
     if (mostRecent == null) {
       return null;
     }
 
     if (mostRecent.getCreationTime() < System.currentTimeMillis()
         - this.maxCacheAge) {
       // receiverSamples.remove(transmitter);
       return null;
     }
 
     return mostRecent;
   }
 
   public float getVarianceAt(final String transmitter, final String receiver,
       long timeOffset, long window) {
 
     long desiredOrJustAfter = this.isClone ? this.creationTs - timeOffset
         : System.currentTimeMillis() - timeOffset;
 
     Map<String, Deque<ChartItem<Float>>> receiverSamples = this.varianceRssiByRByT
         .get(receiver);
 
     if (receiverSamples == null) {
       // log.warn("No samples for {}", receiver);
       return Float.NaN;
     }
 
     Deque<ChartItem<Float>> transmitterSamples = receiverSamples
         .get(transmitter);
 
     if (transmitterSamples == null) {
       // log.warn("No samples for {}", transmitter);
       return Float.NaN;
     }
     float theValue = Float.NaN;
     // From oldest to newest
     for (Iterator<ChartItem<Float>> iter = transmitterSamples.iterator(); iter
         .hasNext();) {
       ChartItem<Float> someItem = iter.next();
       if (someItem.getCreationTime() < desiredOrJustAfter - window) {
         continue;
       }
       if (someItem.getCreationTime() >= desiredOrJustAfter) {
         break;
       }
 
       theValue = someItem.getValue().floatValue();
     }
 
     return theValue;
   }
 
   /**
    * Returns the cached signal-to-distance data for a specific receiver based on
    * fiduciary transmitter information.
    * 
    * @param receiverId
    * @return a list of {@link SignalToDistanceItem} objects containing the
    *         cached signal-to-distance cata for the receiver.
    */
   public List<SignalToDistanceItem> getSignalToDistance(final String receiverId) {
     Deque<SignalToDistanceItem> signalToDistanceItems = this.sigToDistHistory
         .get(receiverId);
 
     if (signalToDistanceItems == null) {
       return null;
     }
 
     LinkedList<SignalToDistanceItem> itemList = new LinkedList<SignalToDistanceItem>();
     for (SignalToDistanceItem item : signalToDistanceItems) {
       itemList.add(item);
     }
     return itemList;
   }
 
   public DataCache2 clone() {
     DataCache2 returnedCache = new DataCache2(new ConnectionHandler(), this.creationTs);
     
     returnedCache.isClone = true;
     this.overlay(returnedCache);
     return returnedCache;
   }
 
   /**
    * Clears all information in this data cache, including region definitions,
    * device locations, receiver ids, fiduciary transmitter ids, and cached data.
    * This method will essentially "reset" the data cache to the original state.
    */
   public void clearAll() {
     this.clearCachedData();
     this.sensorToUri.clear();
     this.deviceLocations.clear();
     this.regionBounds = null;
     this.regionUri = null;
     this.regionImage = null;
     this.fiduciaryTransmitterIds.clear();
    this.numFidTxers = 0;
    
     this.receiverIds.clear();
    this.numRxers = 0;
     
     log.info("Region info and device locations cleared from cache.");
   }
 
   /**
    * Clears sample-related data from this cache, including raw samples and
    * variance information. If you need to clear region data and device
    * locations, you should call {@link #clearAll()} instead.
    */
   public void clearCachedData() {
     for (String rxer : this.averageRssiByRByT.keySet()) {
       Map<String, Deque<ChartItem<Float>>> item = this.averageRssiByRByT
           .get(rxer);
       if (item == null) {
         continue;
       }
       for (String txer : item.keySet()) {
         Deque<ChartItem<Float>> deque = item.get(txer);
         if (deque == null) {
           continue;
         }
         deque.clear();
       }
       item.clear();
     }
     this.averageRssiByRByT.clear();
    
     for (String rxer : this.varianceRssiByRByT.keySet()) {
       Map<String, Deque<ChartItem<Float>>> item = this.varianceRssiByRByT
           .get(rxer);
       if (item == null) {
         continue;
       }
       for (String txer : item.keySet()) {
         Deque<ChartItem<Float>> deque = item.get(txer);
         if (deque == null) {
           continue;
         }
         deque.clear();
       }
       item.clear();
     }
     this.varianceRssiByRByT.clear();
    
 
     for (String rxer : this.sigToDistHistory.keySet()) {
       Deque<SignalToDistanceItem> deque = this.sigToDistHistory.get(rxer);
       if (deque == null) {
         continue;
       }
       deque.clear();
     }
     
     this.sigToDistHistory.clear();
     this.numRssiPoints = 0;
     this.numVarPoints = 0;
     this.numSigToDistPoints = 0;
     this.lastRssiUpdate = System.currentTimeMillis();
     this.lastVarianceUpdate = System.currentTimeMillis();
 
     log.info("All sample data cleared from cache.");
   }
 
   /**
    * Returns a list of receiver ID values (URIs) as Strings. The returned List
    * is not bound to the cache data and may be modified freely.
    * 
    * @return a list of receiver ID values.
    */
   public List<String> getReceiverIds() {
     List<String> receivers = new LinkedList<String>();
     receivers.addAll(this.receiverIds);
     return receivers;
   }
 
   /**
    * Returns a list of fiduciary transmitter ID values (URIs) as Strings. The
    * returned List is not bound to the cache data and may be modified freely.
    * 
    * @return a list of fiduciary transmitter ID values.
    */
   public List<String> getFiduciaryTransmitterIds() {
     List<String> transmitters = new LinkedList<String>();
     transmitters.addAll(this.fiduciaryTransmitterIds);
     return transmitters;
   }
 
   protected void overlay(final DataCache2 clone) {
     clone.clearAll();
     clone.taskTimer.cancel();
 
     int cloneNumRssi = 0, cloneNumVar = 0;
 
     clone.maxCacheAge = this.maxCacheAge;
     clone.regionBounds = this.regionBounds;
     clone.regionImage = this.regionImage;
     clone.regionImageUrl = this.regionImageUrl;
     clone.regionUri = this.regionUri;
 
     clone.receiverIds.addAll(this.receiverIds);
     clone.numRxers = this.numRxers;
 
     clone.fiduciaryTransmitterIds.addAll(this.fiduciaryTransmitterIds);
     clone.numFidTxers = this.numFidTxers;
 
     for (String device : this.deviceLocations.keySet()) {
       clone.deviceLocations.put(device, this.deviceLocations.get(device));
 
     }
 
     for (String receiver : this.sigToDistHistory.keySet()) {
       Deque<SignalToDistanceItem> receiverSigToDist = this.sigToDistHistory
           .get(receiver);
       LinkedBlockingDeque<SignalToDistanceItem> cloneReceiverSigToDist = new LinkedBlockingDeque<SignalToDistanceItem>();
       cloneReceiverSigToDist.addAll(receiverSigToDist);
       clone.sigToDistHistory.put(receiver, cloneReceiverSigToDist);
     }
     clone.numSigToDistPoints = this.numSigToDistPoints;
 
     for (String receiver : this.averageRssiByRByT.keySet()) {
       Map<String, Deque<ChartItem<Float>>> receiverQueues = this.averageRssiByRByT
           .get(receiver);
       ConcurrentHashMap<String, Deque<ChartItem<Float>>> cloneReceiverQueues = new ConcurrentHashMap<String, Deque<ChartItem<Float>>>();
       clone.averageRssiByRByT.put(receiver, cloneReceiverQueues);
       for (String transmitter : receiverQueues.keySet()) {
         Deque<ChartItem<Float>> transmitterQueue = receiverQueues
             .get(transmitter);
         LinkedBlockingDeque<ChartItem<Float>> cloneTransmitterQueue = new LinkedBlockingDeque<ChartItem<Float>>();
         cloneReceiverQueues.put(transmitter, cloneTransmitterQueue);
         cloneTransmitterQueue.addAll(transmitterQueue);
         cloneNumRssi += cloneTransmitterQueue.size();
       }
     }
 
     for (String receiver : this.varianceRssiByRByT.keySet()) {
       Map<String, Deque<ChartItem<Float>>> receiverQueues = this.varianceRssiByRByT
           .get(receiver);
       ConcurrentHashMap<String, Deque<ChartItem<Float>>> cloneReceiverQueues = new ConcurrentHashMap<String, Deque<ChartItem<Float>>>();
       clone.varianceRssiByRByT.put(receiver, cloneReceiverQueues);
       for (String transmitter : receiverQueues.keySet()) {
         Deque<ChartItem<Float>> transmitterQueue = receiverQueues
             .get(transmitter);
         LinkedBlockingDeque<ChartItem<Float>> cloneTransmitterQueue = new LinkedBlockingDeque<ChartItem<Float>>();
         cloneReceiverQueues.put(transmitter, cloneTransmitterQueue);
         cloneTransmitterQueue.addAll(transmitterQueue);
         cloneNumVar += cloneTransmitterQueue.size();
       }
     }
   }
 
   public long getMaxCacheAge() {
     return maxCacheAge;
   }
 
   public void setMaxCacheAge(long maxCacheAge) {
     this.maxCacheAge = maxCacheAge;
   }
 
   public Rectangle2D getRegionBounds() {
     return regionBounds;
   }
 
   public void setRegionBounds(Rectangle2D regionBounds) {
     this.regionBounds = regionBounds;
   }
 
   public BufferedImage getRegionImage() {
     return regionImage;
   }
 
   public void setRegionImage(BufferedImage regionImage) {
     this.regionImage = ImageResources.toCompatibleImage(regionImage);
   }
 
   public String getRegionUri() {
     return regionUri;
   }
 
   public void setRegionUri(String regionUri) {
     this.regionUri = regionUri;
   }
 
   public void mapSensorToUri(String sensor, String uri) {
     this.sensorToUri.put(sensor, uri);
   }
 
   public boolean isClone() {
     return isClone;
   }
 
   public long getCreationTs() {
     return creationTs;
   }
 
   protected void sweepCache() {
     if (this.isClone) {
       return;
     }
     long oldestTs = System.currentTimeMillis() - this.maxCacheAge;
     for (Map<String, Deque<ChartItem<Float>>> rxerMap : this.averageRssiByRByT
         .values()) {
       for (Deque<ChartItem<Float>> txerQ : rxerMap.values()) {
         while (!txerQ.isEmpty() && txerQ.peek().getCreationTime() < oldestTs) {
           txerQ.poll();
           --this.numRssiPoints;
         }
       }
     }
 
     for (Map<String, Deque<ChartItem<Float>>> rxerMap : this.varianceRssiByRByT
         .values()) {
       for (Deque<ChartItem<Float>> txerQ : rxerMap.values()) {
         while (!txerQ.isEmpty() && txerQ.peek().getCreationTime() < oldestTs) {
           txerQ.poll();
           --this.numVarPoints;
         }
       }
     }
 
     for (Deque<SignalToDistanceItem> sigItems : this.sigToDistHistory.values()) {
       while (!sigItems.isEmpty()
           && sigItems.peek().getCreationTime() < oldestTs) {
         sigItems.poll();
         --this.numSigToDistPoints;
       }
     }
   }
 
   public void showStatsPane(final Component parent) {
     JOptionPane.showMessageDialog(parent, this.statsPanel, "Cache Statistics",
         JOptionPane.INFORMATION_MESSAGE);
   }
 
   protected void updateStats() {
     this.statsPanel.setNumRxers(this.numRxers);
     this.statsPanel.setNumFidTxers(this.numFidTxers);
     this.statsPanel.setNumRssiPoints(this.numRssiPoints);
     this.statsPanel.setNumVarPoints(this.numVarPoints);
     this.statsPanel.setNumSigDistPoints(this.numSigToDistPoints);
   }
 
   public void shutdown() {
     this.taskTimer.cancel();
   }
 
   public static Logger getLog() {
     return log;
   }
 
   public Map<String, Map<String, Deque<ChartItem<Float>>>> getAverageRssiByRByT() {
     return averageRssiByRByT;
   }
 
   public Map<String, Map<String, Deque<ChartItem<Float>>>> getVarianceRssiByRByT() {
     return varianceRssiByRByT;
   }
 
   public long getLastRssiUpdate() {
     return lastRssiUpdate;
   }
 
   public long getLastVarianceUpdate() {
     return lastVarianceUpdate;
   }
 
   public void saveToFile(final File file) {
     Thread saveThread = new Thread() {
       @Override
       public void run() {
         System.out.println("Saving to " + file.getName());
 
         XStream xstream = new XStream();
 
         try {
           FileOutputStream fileOut = new FileOutputStream(file);
 
           BufferedOutputStream buffOut = new BufferedOutputStream(fileOut);
           GZIPOutputStream gzipOut = new GZIPOutputStream(buffOut);
           ObjectOutputStream out = xstream.createObjectOutputStream(gzipOut);
 
           DataCache2.this.toStream(file.getPath(), out);
         } catch (Exception e) {
           System.err.println("Unable to save file.");
           e.printStackTrace();
         }
         System.out.println("Save completed.");
       }
     };
     saveThread.start();
 
   }
 
   public void restoreFromFile(final File file) {
     Thread loadThread = new Thread() {
       @Override
       public void run() {
         System.out.println("Loading from " + file.getName());
 
         XStream xstream = new XStream(new StaxDriver());
 
         try {
           FileInputStream fileIn = new FileInputStream(file);
           ProgressMonitorInputStream monitor = new ProgressMonitorInputStream(
               null, "Loading cache from \"" + file.getPath() + "\"", fileIn);
           BufferedInputStream buffIn = new BufferedInputStream(monitor);
           GZIPInputStream gzipIn = new GZIPInputStream(buffIn);
           ObjectInputStream in = xstream.createObjectInputStream(gzipIn);
 
           DataCache2.this.fromStream(file.getPath(), in);
         } catch (Exception e) {
           System.err.println("Unable to load file.");
           e.printStackTrace();
         }
         System.out.println("Load completed.");
         for (DataCache2Listener listener : DataCache2.this.listeners) {
           listener.fileLoaded(file.getPath());
         }
       }
     };
     loadThread.setPriority(Thread.MIN_PRIORITY);
     loadThread.start();
 
   }
 
   protected synchronized void toStream(String filename, ObjectOutputStream out)
       throws IOException {
 
     int maxProgress = this.numFidTxers + this.numRxers + this.numRssiPoints
         + this.numVarPoints + this.numSigToDistPoints;
     int currProgress = 0;
     ProgressMonitor monitor = new ProgressMonitor(null, "Saving to \""
         + filename + "\"", "A note", currProgress, maxProgress);
 
     // JProgressBar progress = new JProgressBar(0, maxProgress);
     // progress.setPreferredSize(new Dimension(640,20));
     // progress.setStringPainted(true);
     //
     // JFrame tempFrame = new JFrame("Saving cache to \"" + filename + "\"");
     // tempFrame.getContentPane().add(progress);
     // tempFrame.pack();
     // tempFrame.setVisible(true);
     //
 
     // Timestamp of datacache
     // out.writeObject(Long.valueOf(DataCache2.this.creationTs));
     if (this.isClone) {
       out.writeObject(Long.valueOf(this.creationTs));
     } else {
       out.writeObject(Long.valueOf(System.currentTimeMillis()));
     }
 
     // Region URI
     out.writeObject(DataCache2.this.regionUri);
 
     // Region bounds
     out.writeObject(DataCache2.this.regionBounds);
 
     // Region Image
     out.writeObject(DataCache2.this.regionImageUrl);
 
     // List of fiduciary transmitters
     List<String> deviceList = new LinkedList<String>();
     deviceList.addAll(DataCache2.this.fiduciaryTransmitterIds);
     out.writeObject(deviceList);
     currProgress += this.numFidTxers;
     if (currProgress > maxProgress) {
       currProgress = maxProgress - 1;
     }
     monitor.setProgress(currProgress);
 
     // List of receivers
     deviceList.clear();
     deviceList.addAll(DataCache2.this.receiverIds);
     out.writeObject(deviceList);
     deviceList.clear();
     currProgress += this.numRxers;
     if (currProgress > maxProgress) {
       currProgress = maxProgress - 1;
     }
     monitor.setProgress(currProgress);
 
     // Device location map
     Map<String, Point2D> locationMap = new HashMap<String, Point2D>();
     locationMap.putAll(DataCache2.this.deviceLocations);
     out.writeObject(locationMap);
     locationMap.clear();
 
     // Sensor URI -> Device URI map
     Map<String, String> sensorMap = new HashMap<String, String>();
     sensorMap.putAll(DataCache2.this.sensorToUri);
     out.writeObject(sensorMap);
     sensorMap.clear();
 
     monitor.setNote("Saving RSSI data...");
     // RSSI queues
     // Map<String, Map<String, List<ChartItem<Float>>>> storedItems = new
     // HashMap<String, Map<String, List<ChartItem<Float>>>>();
 
     List<String> rxers = new LinkedList<String>();
     rxers.addAll(this.receiverIds);
 
     // Number of receivers
     out.writeObject(Integer.valueOf(rxers.size()));
 
     for (String rxer : rxers) {
 
       out.writeObject(rxer);
       // Grab a map, clone it if it's not null
       Map<String, Deque<ChartItem<Float>>> cacheRxMap = DataCache2.this.averageRssiByRByT
           .get(rxer);
 
       if (cacheRxMap != null) {
         List<String> txers = new LinkedList<String>();
         txers.addAll(cacheRxMap.keySet());
 
         // Number of transmitters mapped for this receiver
         out.writeObject(Integer.valueOf(txers.size()));
 
         for (String txer : txers) {
           Deque<ChartItem<Float>> cacheTxItems = cacheRxMap.get(txer);
           if (cacheTxItems != null) {
             // Be sure to write receiver, txer, deque so we can reconstruct
             // later
 
             out.writeObject(txer);
             out.writeObject(cacheTxItems);
 
             currProgress += cacheTxItems.size();
             if (currProgress > maxProgress) {
               currProgress = maxProgress - 1;
             }
             monitor.setProgress(currProgress);
           }
         }
       }
       // Null receiver map, but need a placeholder
       else {
         out.writeObject(Integer.valueOf(0));
       }
     }
 
     monitor.setNote("Saving variance data...");
     // Variance queues
     for (String rxer : rxers) {
 
       out.writeObject(rxer);
       // Grab a map, clone it if it's not null
       Map<String, Deque<ChartItem<Float>>> cacheRxMap = DataCache2.this.varianceRssiByRByT
           .get(rxer);
 
       if (cacheRxMap != null) {
         List<String> txers = new LinkedList<String>();
         txers.addAll(cacheRxMap.keySet());
 
         // Number of transmitters mapped for this receiver
         out.writeObject(Integer.valueOf(txers.size()));
         for (String txer : txers) {
           Deque<ChartItem<Float>> cacheTxItems = cacheRxMap.get(txer);
           if (cacheTxItems != null) {
             // Be sure to write receiver, txer, deque so we can reconstruct
             // later
 
             out.writeObject(txer);
             out.writeObject(cacheTxItems);
 
             currProgress += cacheTxItems.size();
             if (currProgress > maxProgress) {
               currProgress = maxProgress - 1;
             }
             monitor.setProgress(currProgress);
           }
         }
       }
       // Null receiver map, but need a placeholder
       else {
         out.writeObject(Integer.valueOf(0));
       }
     }
 
     monitor.setNote("Saving Signal-to-Distance data...");
     // Signal to Distance values
     // Map<String, List<SignalToDistanceItem>> cloneMap = new HashMap<String,
     // List<SignalToDistanceItem>>();
 
     for (String rxer : rxers) {
       Deque<SignalToDistanceItem> cacheDeque = DataCache2.this.sigToDistHistory
           .get(rxer);
       out.writeObject(rxer);
       if (cacheDeque != null) {
 
         out.writeObject(cacheDeque);
         currProgress += cacheDeque.size();
         if (currProgress > maxProgress) {
           currProgress = maxProgress - 1;
         }
         monitor.setProgress(currProgress);
 
       }
       // Place holder
       else {
         out.writeObject(new LinkedBlockingDeque<SignalToDistanceItem>());
       }
     }
     monitor.setNote("Writing to file...");
 
     out.flush();
     out.close();
     monitor.setProgress(maxProgress);
     // tempFrame.setVisible(false);
 
   }
 
   protected synchronized void fromStream(String filename, ObjectInputStream in)
       throws Exception {
 
     this.clearAll();
     this.isClone = true;
 
     // Timestamp of datacache
     this.creationTs = ((Long) in.readObject()).longValue();
     this.lastRssiUpdate = this.creationTs;
     this.lastVarianceUpdate = this.creationTs;
 
     // Region URI
     this.regionUri = (String) in.readObject();
 
     // Region bounds
     this.regionBounds = (Rectangle2D) in.readObject();
 
     // Region Image
     this.regionImageUrl = (String) in.readObject();
     if (this.regionImageUrl != null) {
       try {
         this.setRegionImage(ImageIO.read(new URL(this.regionImageUrl)));
       } catch (Exception e) {
         log.error("Unable to load region image.", e);
       }
     }
 
     // List of fiduciary transmitters
     List<String> deviceList = (List) in.readObject();
     this.fiduciaryTransmitterIds.addAll(deviceList);
     this.numFidTxers = this.fiduciaryTransmitterIds.size();
 
     // List of receivers
     deviceList.clear();
     deviceList = (List) in.readObject();
     this.receiverIds.addAll(deviceList);
     this.numRxers = this.receiverIds.size();
 
     // Device location map
     Map<String, Point2D> locationMap = (Map<String, Point2D>) in.readObject();
     this.deviceLocations.putAll(locationMap);
 
     // Sensor URI -> Device URI map
     Map<String, String> sensorMap = (Map<String, String>) in.readObject();
     this.sensorToUri.putAll(sensorMap);
 
     // RSSI queues
     List<String> rxers = new LinkedList<String>();
     rxers.addAll(this.receiverIds);
 
     // Number of receivers
     int numRxers = ((Integer) in.readObject()).intValue();
 
     for (int i = 0; i < numRxers; ++i) {
       String rxer = (String) in.readObject();
 
       Map<String, Deque<ChartItem<Float>>> cacheRxMap = this.averageRssiByRByT
           .get(rxer);
 
       if (cacheRxMap == null) {
         cacheRxMap = new ConcurrentHashMap<String, Deque<ChartItem<Float>>>();
         this.averageRssiByRByT.put(rxer, cacheRxMap);
       }
       // Number of transmitters
       int numTxers = ((Integer) in.readObject()).intValue();
       for (int j = 0; j < numTxers; ++j) {
         String txer = (String) in.readObject();
         Deque<ChartItem<Float>> fileTxItems = (Deque<ChartItem<Float>>) in
             .readObject();
         this.numRssiPoints += fileTxItems.size();
         cacheRxMap.put(txer, fileTxItems);
       }
     }
 
     System.out.println("Finished RSSI queues.");
     // Variance queues
 
     for (int i = 0; i < numRxers; ++i) {
       String rxer = (String) in.readObject();
 
       Map<String, Deque<ChartItem<Float>>> cacheRxMap = this.varianceRssiByRByT
           .get(rxer);
 
       if (cacheRxMap == null) {
         cacheRxMap = new ConcurrentHashMap<String, Deque<ChartItem<Float>>>();
         this.varianceRssiByRByT.put(rxer, cacheRxMap);
       }
       // Number of transmitters
       int numTxers = ((Integer) in.readObject()).intValue();
       for (int j = 0; j < numTxers; ++j) {
         String txer = (String) in.readObject();
         Deque<ChartItem<Float>> fileTxItems = (Deque<ChartItem<Float>>) in
             .readObject();
         this.numVarPoints += fileTxItems.size();
         cacheRxMap.put(txer, fileTxItems);
       }
     }
     System.out.println("Finished Variance queues.");
 
     for (int i = 0; i < numRxers; ++i) {
 
       String rxer = (String) in.readObject();
       Deque<SignalToDistanceItem> deque = (Deque<SignalToDistanceItem>) in
           .readObject();
       this.numSigToDistPoints += deque.size();
       this.sigToDistHistory.put(rxer, deque);
     }
     System.out.println("Finished Sig-To-Dist queues.");
 
     this.updateStats();
     for (DataCache2Listener listener : this.listeners) {
       listener.receiverAdded(null);
       listener.transmitterAdded(null, true);
       // listener.transmitterAdded(null,false);
     }
 
   }
 
   public void setRegionImageUrl(String regionImageUrl) {
     this.regionImageUrl = regionImageUrl;
   }
 
   public int getNumListeners() {
     return this.listeners.size();
   }
 
   public ConnectionHandler getHandler() {
     return handler;
   }
 }
