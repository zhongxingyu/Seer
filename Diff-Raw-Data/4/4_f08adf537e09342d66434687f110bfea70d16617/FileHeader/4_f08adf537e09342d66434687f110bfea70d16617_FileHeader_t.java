 /**
  * Copyright (C) 2011 (nick @ objectdefinitions.com)
  *
  * This file is part of JTimeseries.
  *
  * JTimeseries is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * JTimeseries is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public License
  * along with JTimeseries.  If not, see <http://www.gnu.org/licenses/>.
  */
 package com.od.jtimeseries.server.serialization;
 
 import com.od.jtimeseries.identifiable.PathParser;
 import com.od.jtimeseries.util.logging.LogMethods;
 import com.od.jtimeseries.util.logging.LogUtils;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.util.Properties;
 
 /**
  * Created by IntelliJ IDEA.
  * User: Nick Ebbutt
  * Date: 18-May-2009
  * Time: 07:30:42
  *
  * Represents header information stored in a timeseries file
  *
  * When a timeseries is saved to disk or loaded, the in memory header information is brought up to date
  * so that it should match the information contained in the file header on disk.
  *
  * The information in memory should always match the information on disk, apart from the seriesProperties
  * which may contain in memory changes which are waiting to be written
  *
  * FileHeader is generally expected to be unique per series file/series path
  */
 public class FileHeader extends LockingFileHeader {
 
     private static final LogMethods logMethods = LogUtils.getLogMethods(FileHeader.class);
     public static final int DEFAULT_HEADER_START_LENGTH = 756;
 
     //these properties will be stored in the serialized series files, careful if you change them!
     private static final String PATH_KEY = "CONTEXT_PATH";
     private static final String DESCRIPTION_KEY = "DESCRIPTION";
     private static int MAX_PROPERTY_LENGTH = 1024;
 
     private volatile int headerLength = DEFAULT_HEADER_START_LENGTH;  //default start length for header
 
     /**
      * Max size of series
      */
     private volatile int seriesMaxLength;
 
     /**
      * round robin position of item with the earliest timestamp
      */
     private volatile int currentHead;
 
     /**
      * position at which to next series item (position after item with the latest timestamp)
      * will equal currentHead if we already have max number of items in series
      */
     private volatile int currentTail;
 
 
     private volatile long mostRecentItemTimestamp = -1;
 
     private SeriesProperties seriesProperties = new SeriesProperties();
 
     public FileHeader() {}
 
     /**
      * @param seriesMaxLength, maximum size for this series
      */
     public FileHeader(String path, String description, int seriesMaxLength) {
         setPath(path);
         seriesProperties.setProperty(DESCRIPTION_KEY, description);
         this.seriesMaxLength = seriesMaxLength;
     }
 
     protected String doGetPath() {
         return seriesProperties.getProperty(PATH_KEY);
     }
 
     //use with care, this effectively changes the series path/id, only for path migrations before series is created
     protected void doSetPath(String path) {
         seriesProperties.setProperty(PATH_KEY, path);
     }
 
     protected Properties doGetSnapshot() {
         return seriesProperties.getSnapshot();
     }
 
     protected void doSetSeriesProperties(byte[] serializedProperties) throws IOException {
         seriesProperties.set(serializedProperties);
     }
 
     protected String doSetProperty(String key, String value) {
         return seriesProperties.setProperty(key, value);
     }
 
     protected String doGetSeriesProperty(String key) {
         return seriesProperties.getProperty(key);
     }
 
     protected String doRemoveSeriesProperty(String key) {
         return seriesProperties.removeProperty(key);
     }
 
     protected int doGetHeaderLength() {
         return headerLength;
     }
 
     protected int doCalculateNewHeaderLength(int requiredLength) {
         int h = this.headerLength;
         while(h < requiredLength) {
             h *= 2;
         }
         return h;
     }
 
     protected int doGetSeriesMaxLength() {
         return seriesMaxLength;
     }
 
     protected int doGetCurrentSeriesSize() {
         return currentHead == -1 ? 0 :
             currentTail > currentHead ?
                 currentTail - currentHead :
                 currentTail + (seriesMaxLength - currentHead);
     }
 
     protected int doGetCurrentHead() {
         return currentHead;
     }
 
     protected long doGetMostRecentTimestamp() {
         return mostRecentItemTimestamp;
     }
 
     protected int doGetCurrentTail() {
         return currentTail;
     }
 
     protected String doGetDescription() {
         return seriesProperties.getProperty(DESCRIPTION_KEY);
     }
 
     protected byte[] doGetPropertiesAsByteArray() throws SerializationException {
         return seriesProperties.getPropertiesAsByteArray();
     }
 
     protected boolean doIsPropertiesRewriteRequired() {
         return seriesProperties.isChanged();
     }
 
     public String toString() {
         return "FileHeader{" +
                 "path =" + getPath() +
                 '}';
     }
 
     //this is being set from Spring applicationContext.xml
     public static void setMaxPropertyLength(String propertyLength) {
         MAX_PROPERTY_LENGTH = Integer.valueOf(propertyLength);
     }
 
     public String getId() {
         return PathParser.lastNode(getPath());
     }
 
     void doUpdateHeaderFields(int newHeaderLength, int head, int tail, int seriesMaxLength, long latestTimestamp) {
         assert(tail >= 0 && tail <= seriesMaxLength);
         assert(head >= -1 && head <= seriesMaxLength);
         //logMethods.logInfo(head + ", " + tail + ", " + seriesMaxLength);
         this.headerLength = newHeaderLength;
         this.currentHead = head;
         this.currentTail = tail;
         this.seriesMaxLength = seriesMaxLength;
         this.mostRecentItemTimestamp = latestTimestamp;
     }
 
     /**
      * Properties class which keeps a changed flag.
      * Can be reset with the current set of properties loaded from file header on disk
      */
     private class SeriesProperties {
 
         private Properties wrappedProperties = new Properties();
 
         //true, if the series properties in memory have changed, and the header properties information needs to be rewritten
         private boolean seriesPropertiesChanged;
 
         public synchronized void set(byte[] serializedProperties) throws IOException {
             wrappedProperties.clear();
             Properties p = getProperties(serializedProperties);
             wrappedProperties.putAll(p);
             seriesPropertiesChanged = false;
         }
 
         private Properties getProperties(byte[] bytes) throws IOException {
             ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
             Properties p = new Properties();
             p.load(bis);
             return p;
         }
 
         public String setProperty(String key, String value) {
             String result = null;
             if ( key.length() > MAX_PROPERTY_LENGTH || value.length() > MAX_PROPERTY_LENGTH) {
                 logMethods.logWarning("Cannot persist timeseries property with key or value length > " + MAX_PROPERTY_LENGTH +
                     ", start of key " + key.substring(0, Math.min(124, key.length())));
             } else {
                 result = (String) wrappedProperties.setProperty(key, value);
                 seriesPropertiesChanged = true;
             }
             return result;
         }
 
         public String removeProperty(String key) {
             String removed = (String)wrappedProperties.remove(key);
             if ( removed != null) {
                 seriesPropertiesChanged = true;
             }
             return removed;
         }
 
         public boolean isChanged() {
             return seriesPropertiesChanged;
         }
 
         public String getProperty(String key) {
             return wrappedProperties.getProperty(key);
         }
 
         public Properties getSnapshot() {
            Properties p = new Properties();
            p.putAll(wrappedProperties);
            return p;
         }
 
         public byte[] getPropertiesAsByteArray() throws SerializationException {
             ByteArrayOutputStream bos = new ByteArrayOutputStream(1000);
             try {
                 wrappedProperties.store(bos, "TimeSeries");
             } catch (IOException ioe) {
                 throw new SerializationException("Failed to serialize properties", ioe);
             }
             return bos.toByteArray();
         }
     }
 }
