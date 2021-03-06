 /**
  * Copyright 2008 The Apache Software Foundation
  *
  * Licensed to the Apache Software Foundation (ASF) under one
  * or more contributor license agreements.  See the NOTICE file
  * distributed with this work for additional information
  * regarding copyright ownership.  The ASF licenses this file
  * to you under the Apache License, Version 2.0 (the
  * "License"); you may not use this file except in compliance
  * with the License.  You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.apache.hadoop.hbase.master;
 
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;    //TODO: remove
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import org.apache.hadoop.fs.Path;
 import org.apache.hadoop.hbase.HConstants;
 import org.apache.hadoop.hbase.HRegionInfo;
 import org.apache.hadoop.hbase.HServerAddress;
 import org.apache.hadoop.hbase.HServerInfo;
 import org.apache.hadoop.hbase.RemoteExceptionHandler;
 import org.apache.hadoop.hbase.ipc.HRegionInterface;
 import org.apache.hadoop.hbase.regionserver.HLog;
 import org.apache.hadoop.hbase.regionserver.HRegion;
 import org.apache.hadoop.hbase.util.Writables;
 import org.apache.hadoop.hbase.io.RowResult;
 
 /** 
  * Instantiated when a server's lease has expired, meaning it has crashed.
  * The region server's log file needs to be split up for each region it was
  * serving, and the regions need to get reassigned.
  */
 class ProcessServerShutdown extends RegionServerOperation {
   private HServerAddress deadServer;
   private String deadServerName;
   private Path oldLogDir;
   private boolean logSplit;
   private boolean rootRescanned;
 
   private class ToDoEntry {
     boolean regionOffline;
     final byte [] row;
     final HRegionInfo info;
 
     ToDoEntry(final byte [] row, final HRegionInfo info) {
       this.regionOffline = false;
       this.row = row;
       this.info = info;
     }
   }
 
   /**
    * @param master
    * @param serverInfo
    */
   public ProcessServerShutdown(HMaster master, HServerInfo serverInfo) {
     super(master);
     this.deadServer = serverInfo.getServerAddress();
     this.deadServerName = this.deadServer.toString();
     this.logSplit = false;
     this.rootRescanned = false;
     StringBuilder dirName = new StringBuilder("log_");
     dirName.append(deadServer.getBindAddress());
     dirName.append("_");
     dirName.append(serverInfo.getStartCode());
     dirName.append("_");
     dirName.append(deadServer.getPort());
     this.oldLogDir = new Path(master.rootdir, dirName.toString());
   }
 
   @Override
   public String toString() {
     return "ProcessServerShutdown of " + this.deadServer.toString();
   }
 
   /** Finds regions that the dead region server was serving */
   protected void scanMetaRegion(HRegionInterface server, long scannerId,
       byte [] regionName) throws IOException {
 
     List<ToDoEntry> toDoList = new ArrayList<ToDoEntry>();
     Set<HRegionInfo> regions = new HashSet<HRegionInfo>();
     List<byte []> emptyRows = new ArrayList<byte []>();
     try {
       while (true) {
         RowResult values = null;
         try {
           values = server.next(scannerId);
         } catch (IOException e) {
           LOG.error("Shutdown scanning of meta region",
             RemoteExceptionHandler.checkIOException(e));
           break;
         }
         if (values == null || values.size() == 0) {
           break;
         }
         
         byte [] row = values.getRow();
         
         if (LOG.isDebugEnabled() && row != null) {
           LOG.debug("shutdown scanner looking at " + row.toString());
         }
 
         // Check server name.  If null, be conservative and treat as though
         // region had been on shutdown server (could be null because we
         // missed edits in hlog because hdfs does not do write-append).
         String serverName = Writables.cellToString(values.get(COL_SERVER));
         if (serverName.length() > 0 &&
             deadServerName.compareTo(serverName) != 0) {
           // This isn't the server you're looking for - move along
           continue;
         }
 
         // Bingo! Found it.
         HRegionInfo info = master.getHRegionInfo(row, values);
         if (info == null) {
           emptyRows.add(row);
           continue;
         }
 
         if (info.isMetaTable()) {
           if (LOG.isDebugEnabled()) {
             LOG.debug("removing meta region " + info.getRegionName() +
                 " from online meta regions");
           }
           master.regionManager.offlineMetaRegion(info.getStartKey());
         }
 
         ToDoEntry todo = new ToDoEntry(row, info);
         toDoList.add(todo);
 
         if (master.regionManager.isMarkedToClose(deadServerName, info.getRegionName())) {
           master.regionManager.noLongerMarkedToClose(deadServerName, info.getRegionName());
           master.regionManager.noLongerUnassigned(info);
           // Mark region offline
           todo.regionOffline = true;
         } else {
          if (!info.isOffline() && !info.isSplit()) {
            // Get region reassigned
            regions.add(info);
          }
         }
         // If it was pending, remove.
         master.regionManager.noLongerPending(info.getRegionName());
       }
     } finally {
       if(scannerId != -1L) {
         try {
           server.close(scannerId);
         } catch (IOException e) {
           LOG.error("Closing scanner",
             RemoteExceptionHandler.checkIOException(e));
         }
       }
     }
 
     // Scan complete. Remove any rows which had empty HRegionInfos
     
     if (emptyRows.size() > 0) {
       LOG.warn("Found " + emptyRows.size() +
           " rows with empty HRegionInfo while scanning meta region " +
           regionName);
       master.deleteEmptyMetaRows(server, regionName, emptyRows);
     }
     // Update server in root/meta entries
     for (ToDoEntry e: toDoList) {
       if (e.regionOffline) {
         HRegion.offlineRegionInMETA(server, regionName, e.info);
       }
     }
 
     // Get regions reassigned
     for (HRegionInfo info: regions) {
       master.regionManager.setUnassigned(info);
     }
   }
 
   private class ScanRootRegion extends RetryableMetaOperation<Boolean> {
     ScanRootRegion(MetaRegion m, HMaster master) {
       super(m, master);
     }
 
     public Boolean call() throws IOException {
       if (LOG.isDebugEnabled()) {
         LOG.debug("process server shutdown scanning root region on " +
             master.getRootRegionLocation().getBindAddress());
       }
       long scannerId = server.openScanner(
           HRegionInfo.ROOT_REGIONINFO.getRegionName(), COLUMN_FAMILY_ARRAY,
           EMPTY_START_ROW, HConstants.LATEST_TIMESTAMP, null);
       scanMetaRegion(server, scannerId,
           HRegionInfo.ROOT_REGIONINFO.getRegionName());
       return true;
     }
   }
 
   private class ScanMetaRegions extends RetryableMetaOperation<Boolean> {
     ScanMetaRegions(MetaRegion m, HMaster master) {
       super(m, master);
     }
     
     public Boolean call() throws IOException {
       if (LOG.isDebugEnabled()) {
         LOG.debug("process server shutdown scanning " + m.getRegionName() +
             " on " + m.getServer());
       }
       long scannerId =
         server.openScanner(m.getRegionName(), COLUMN_FAMILY_ARRAY,
         EMPTY_START_ROW, HConstants.LATEST_TIMESTAMP, null);
 
         scanMetaRegion(server, scannerId, m.getRegionName());
       return true;
     }
   }
 
   @Override
   protected boolean process() throws IOException {
     LOG.info("process shutdown of server " + deadServer + ": logSplit: " +
       this.logSplit + ", rootRescanned: " + rootRescanned +
       ", numberOfMetaRegions: " + 
       master.regionManager.numMetaRegions() +
       ", onlineMetaRegions.size(): " + 
       master.regionManager.numOnlineMetaRegions());
 
     if (!logSplit) {
       // Process the old log file
       if (master.fs.exists(oldLogDir)) {
         if (!master.regionManager.splitLogLock.tryLock()) {
           return false;
         }
         try {
           HLog.splitLog(master.rootdir, oldLogDir, master.fs,
             master.getConfiguration());
         } finally {
           master.regionManager.splitLogLock.unlock();
         }
       }
       logSplit = true;
     }
 
     if (!rootAvailable()) {
       // Return true so that worker does not put this request back on the
       // toDoQueue.
       // rootAvailable() has already put it on the delayedToDoQueue
       return true;
     }
 
     if (!rootRescanned) {
       // Scan the ROOT region
       Boolean result = new ScanRootRegion(
           new MetaRegion(master.getRootRegionLocation(),
               HRegionInfo.ROOT_REGIONINFO.getRegionName(),
               HConstants.EMPTY_START_ROW), this.master).doWithRetries();
         
       if (result == null) {
         // Master is closing - give up
         return true;
       }
 
       if (LOG.isDebugEnabled()) {
         LOG.debug("process server shutdown scanning root region on " +
           master.getRootRegionLocation().getBindAddress() + 
           " finished " + Thread.currentThread().getName());
       }
       rootRescanned = true;
     }
     
     if (!metaTableAvailable()) {
       // We can't proceed because not all meta regions are online.
       // metaAvailable() has put this request on the delayedToDoQueue
       // Return true so that worker does not put this on the toDoQueue
       return true;
     }
 
     List<MetaRegion> regions = master.regionManager.getListOfOnlineMetaRegions();
     for (MetaRegion r: regions) {
       Boolean result = new ScanMetaRegions(r, this.master).doWithRetries();
       if (result == null) {
         break;
       }
       if (LOG.isDebugEnabled()) {
         LOG.debug("process server shutdown finished scanning " +
             r.getRegionName() + " on " + r.getServer());
       }
     }
     master.regionManager.allRegionsClosed(deadServerName);
     master.serverManager.removeDeadServer(deadServerName);
     return true;
   }
 }
