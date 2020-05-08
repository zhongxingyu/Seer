 /*
  * Funambol is a mobile platform developed by Funambol, Inc. 
  * Copyright (C) 2003 - 2007 Funambol, Inc.
  * 
  * This program is free software; you can redistribute it and/or modify it under
  * the terms of the GNU Affero General Public License version 3 as published by
  * the Free Software Foundation with the addition of the following permission 
  * added to Section 15 as permitted in Section 7(a): FOR ANY PART OF THE COVERED
  * WORK IN WHICH THE COPYRIGHT IS OWNED BY FUNAMBOL, FUNAMBOL DISCLAIMS THE 
  * WARRANTY OF NON INFRINGEMENT  OF THIRD PARTY RIGHTS.
  * 
  * This program is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
  * details.
  * 
  * You should have received a copy of the GNU Affero General Public License 
  * along with this program; if not, see http://www.gnu.org/licenses or write to
  * the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
  * MA 02110-1301 USA.
  * 
  * You can contact Funambol, Inc. headquarters at 643 Bair Island Road, Suite 
  * 305, Redwood City, CA 94063, USA, or at email address info@funambol.com.
  * 
  * The interactive user interfaces in modified source and object code versions
  * of this program must display Appropriate Legal Notices, as required under
  * Section 5 of the GNU Affero General Public License version 3.
  * 
  * In accordance with Section 7(b) of the GNU Affero General Public License
  * version 3, these Appropriate Legal Notices must retain the display of the
  * "Powered by Funambol" logo. If the display of the logo is not reasonably 
  * feasible for technical reasons, the Appropriate Legal Notices must display
  * the words "Powered by Funambol". 
  */
 
 package com.funambol.sapisync;
 
 import java.util.Vector;
 import java.util.Hashtable;
 import java.util.Date;
 import java.util.Enumeration;
 import java.io.OutputStream;
 import java.io.IOException;
 
 import org.json.me.JSONException;
 import org.json.me.JSONObject;
 import org.json.me.JSONArray;
 
 import com.funambol.sync.BasicSyncListener;
 import com.funambol.sync.ItemStatus;
 import com.funambol.sync.QuotaOverflowException;
 import com.funambol.sync.SyncAnchor;
 import com.funambol.sync.SyncConfig;
 import com.funambol.sync.SyncException;
 import com.funambol.sync.SyncItem;
 import com.funambol.sync.SyncListener;
 import com.funambol.sync.SyncSource;
 import com.funambol.sync.SyncManagerI;
 import com.funambol.sync.TwinDetectionSource;
 import com.funambol.sapisync.source.JSONSyncSource;
 import com.funambol.storage.StringKeyValueStoreFactory;
 import com.funambol.storage.StringKeyValueStore;
 import com.funambol.sync.ItemDownloadInterruptionException;
 import com.funambol.sync.Filter;
 import com.funambol.sync.ItemUploadInterruptionException;
 import com.funambol.sync.SyncFilter;
 import com.funambol.util.Log;
 import com.funambol.util.StringUtil;
 
 /**
  * <code>SapiSyncManager</code> represents the synchronization engine performed
  * via SAPI.
  */
 public class SapiSyncManager implements SyncManagerI {
 
     private static final String TAG_LOG = "SapiSyncManager";
 
     private static final int MAX_ITEMS_PER_BLOCK = 100;
     private static final int FULL_SYNC_DOWNLOAD_LIMIT = 300;
 
     private SyncConfig syncConfig = null;
     private SapiSyncHandler sapiSyncHandler = null;
     private SapiSyncStatus syncStatus = null;
 
     // Holds the list of twins found during the download phase, those items must
     // not be uploaded to the server later in the upload phase
     private Vector twins = null;
 
     /**
      * Unique instance of a BasicSyncListener which is used when the user does
      * not set up a listener in the SyncSource. In order to avoid the creation
      * of multiple instances of this class we use this static variable
      */
     private static SyncListener basicListener = null;
 
     /**
      * This is the flag used to indicate that the sync shall be cancelled. Users
      * can call the cancel (@see cancel) method to cancel the current sync
      */
     private boolean cancel;
 
 
     private JSONArray addedArray   = null;
     private JSONArray updatedArray = null;
     private JSONArray deletedArray = null;
 
     private String addedServerUrl = null;
     private String updatedServerUrl = null;
 
     private long downloadNextAnchor;
 
     private Hashtable localUpdated;
     private Hashtable localDeleted;
 
     private static final JSONObject REMOVED_ITEM = new JSONObject();
 
 
     /**
      * <code>SapiSyncManager</code> constructor
      * @param config
      */
     public SapiSyncManager(SyncConfig config) {
         this.syncConfig = config;
         this.sapiSyncHandler = new SapiSyncHandler(
                 StringUtil.extractAddressFromUrl(syncConfig.getSyncUrl()),
                 syncConfig.getUserName(),
                 syncConfig.getPassword());
     }
 
     /**
      * Force a specific SapiSyncHandler to be used for testing purposes.
      * @param sapiSyncHandler
      */
     public void setSapiSyncHandler(SapiSyncHandler sapiSyncHandler) {
         this.sapiSyncHandler = sapiSyncHandler;
     }
 
     /**
      * Synchronizes the given source, using the preferred sync mode defined for
      * that SyncSource.
      *
      * @param source the SyncSource to synchronize
      *
      * @throws SyncException If an error occurs during synchronization
      *
      */
     public void sync(SyncSource source) throws SyncException {
         sync(source, source.getSyncMode(), false);
     }
 
     public void sync(SyncSource source, boolean askServerDevInf) throws SyncException {
         sync(source, source.getSyncMode(), askServerDevInf);
     }
 
     public void sync(SyncSource src, int syncMode) {
         sync(src, syncMode, false);
     }
 
     /**
      * Synchronizes the given source, using the given sync mode.
      *
      * @param source the SyncSource to synchronize
      * @param syncMode the sync mode
      * @param askServerDevInf true if the engine shall ask for server caps
      *
      * @throws SyncException If an error occurs during synchronization
      */
     public synchronized void sync(SyncSource src, int syncMode, boolean askServerDevInf)
     throws SyncException {
 
         if (Log.isLoggable(Log.DEBUG)) {
             Log.debug(TAG_LOG, "Starting sync");
         }
 
         cancel = false;
 
         if (basicListener == null) {
             basicListener = new BasicSyncListener();
         }
 
         // JSONSyncSource require an updated sync config, therefore we update it
         // at the beginning of each sync
         if (src instanceof JSONSyncSource) {
             JSONSyncSource jsonSyncSource = (JSONSyncSource)src;
             jsonSyncSource.updateSyncConfig(syncConfig);
         }
         
         boolean resume = false;
 
         syncStatus = new SapiSyncStatus(src.getName());
         try {
             syncStatus.load();
             // If the sync was interrupted, then we shall resume
             resume = syncStatus.getInterrupted();
         } catch (Exception e) {
             if (Log.isLoggable(Log.INFO)) {
                 Log.info(TAG_LOG, "Cannot load sync status, use an empty one");
             }
         }
 
         // If we are not resuming, then we can reset the status
         if (resume) {
             // We need to understand where the sync got stopped and if a single item
             // shall be resumed
         } else {
             try {
                 syncStatus.reset();
                 syncStatus.setInterrupted(true);
             } catch (IOException ioe) {
                 Log.error(TAG_LOG, "Cannot reset status", ioe);
             }
         }
 
         StringKeyValueStoreFactory mappingFactory =
                 StringKeyValueStoreFactory.getInstance();
         StringKeyValueStore mapping = mappingFactory.getStringKeyValueStore(
                 "mapping_" + src.getName());
         try {
             mapping.load();
         } catch (Exception e) {
             if (Log.isLoggable(Log.INFO)) {
                 Log.info(TAG_LOG, "The mapping store does not exist, use an empty one");
             }
         }
         // Init twins vector
         twins = new Vector();
         try {
             // Set the basic properties in the sync status
             syncStatus.setRemoteUri(src.getConfig().getRemoteUri());
             syncStatus.setInterrupted(true);
             syncStatus.setLocUri(src.getName());
             syncStatus.setRequestedSyncMode(syncMode);
             syncStatus.setStartTime(System.currentTimeMillis());
             syncStatus.save();
 
             getSyncListenerFromSource(src).startSession();
             getSyncListenerFromSource(src).startConnecting();
 
             cancelSyncIfNeeded(src);
 
             performInitializationPhase(src, getActualSyncMode(src, syncMode), resume, mapping);
 
             cancelSyncIfNeeded(src);
 
             getSyncListenerFromSource(src).syncStarted(getActualSyncMode(src, syncMode));
 
             if(isDownloadPhaseNeeded(syncMode)) {
                 // The download anchor is updated once it is received from the server
                 performDownloadPhase(src, getActualDownloadSyncMode(src), resume, mapping);
             }
 
             cancelSyncIfNeeded(src);
             
             if(isUploadPhaseNeeded(syncMode)) {
                 long startUploadPhase = System.currentTimeMillis();
                 try {
                     long newUploadAnchor = (new Date()).getTime();
                     performUploadPhase(src, getActualUploadSyncMode(src), resume, mapping);
                     // If we had no error so far, then we update the anchor
                     SapiSyncAnchor anchor = (SapiSyncAnchor)src.getSyncAnchor();
                     anchor.setUploadAnchor(newUploadAnchor);
                 } finally {
                     if (isDownloadPhaseNeeded(syncMode)) {
                         long endTime = System.currentTimeMillis();
                         long delta = endTime - startUploadPhase;
                         // Update the download anchor
                         SapiSyncAnchor anchor = (SapiSyncAnchor)src.getSyncAnchor();
                         long newTime = anchor.getDownloadAnchor() + delta;
                         if (Log.isLoggable(Log.DEBUG)) {
                             Log.debug(TAG_LOG, "Download anchor updated after upload phase to " + newTime);
                         }
                         anchor.setDownloadAnchor(newTime);
                     }
                 }
             }
 
             cancelSyncIfNeeded(src);
 
             performFinalizationPhase(src);
 
             if (syncStatus.getNumberOfReceivedItemsWithSyncStatus(
                     SyncSource.DEVICE_FULL_ERROR_STATUS) > 0) {
                 syncStatus.setInterrupted(true);
                 syncStatus.setStatusCode(SyncListener.LOCAL_CLIENT_FULL_ERROR);
             } else {
                 syncStatus.setInterrupted(false);
                 syncStatus.setStatusCode(SyncListener.SUCCESS);
             }
 
         } catch (Throwable t) {
             Log.error(TAG_LOG, "Error while synchronizing", t);
             syncStatus.setSyncException(t);
             
             SyncException se;
             if (t instanceof SyncException) {
                 se = (SyncException)t;
             } else {
                 se = new SyncException(SyncException.CLIENT_ERROR, "Generic error");
             }
             int syncStatusCode = getListenerStatusFromSyncException(se);
             syncStatus.setStatusCode(syncStatusCode);
             throw se;
         } finally {
             try {
                 mapping.save();
             } catch (Exception e) {
                 Log.error(TAG_LOG, "Cannot save mapping store", e);
             }
             syncStatus.setEndTime(System.currentTimeMillis());
             try {
                 syncStatus.save();
             } catch (IOException ioe) {
                 Log.error(TAG_LOG, "Cannot save sync status", ioe);
             }
             // We must guarantee this method is invoked in all cases
             getSyncListenerFromSource(src).endSession(syncStatus);
         }
     }
 
     public void cancel() {
         if (Log.isLoggable(Log.INFO)) {
             Log.info(TAG_LOG, "Cancelling sync");
         }
         cancel = true;
         sapiSyncHandler.cancel();
         performFinalizationPhase(null);
     }
 
     private void cancelSyncIfNeeded(SyncSource src) throws SyncException {
         if(cancel) {
             src.cancel();
             throw new SyncException(SyncException.CANCELLED, "Sync got cancelled");
         }
     }
 
     private void performInitializationPhase(SyncSource src, int syncMode, boolean resume,
                                             StringKeyValueStore mapping)
     throws SyncException, JSONException
     {
         // Prepare the source for the sync
         src.beginSync(syncMode, resume);
 
         // Perform a login to avoid multiple authentications
         sapiSyncHandler.login(null);
 
         boolean incremental = isIncrementalSync(syncMode);
         if (incremental) {
             prepareIncrementalSync(src, syncMode, resume, mapping);
         } else {
             prepareFullSync(src, syncMode, resume, mapping);
         }
     }
 
     /**
      * This method computes the set of items to be downloaded from the server in
      * an incremental sync.
      * The set of data to be downloaded depends on many things, including the
      * changes made locally. After this method the src update/delete items have
      * been consumed and the getNextNewItem and getNextUpdItem will return null.
      */
     private void prepareIncrementalSync(SyncSource src, int syncMode, boolean resume,
                                         StringKeyValueStore mapping)
     throws SyncException, JSONException
     {
         String remoteUri = src.getConfig().getRemoteUri();
         SyncFilter syncFilter = src.getFilter();
 
         // Check what is available on the server and what changed locally to
         // determine the list of items to be exchanged
         if (Log.isLoggable(Log.INFO)) {
             Log.info(TAG_LOG, "Computing changes set for fast sync");
         }
         SapiSyncHandler.FullSet addedInfo   = null;
         SapiSyncHandler.FullSet updatedInfo = null;
         SapiSyncHandler.FullSet deletedInfo = null;
 
         addedArray   = null;
         updatedArray = null;
         deletedArray = null;
 
         if (isDownloadPhaseNeeded(syncMode)) {
             if(syncFilter != null && syncFilter.getIncrementalDownloadFilter() != null) {
                 throw new UnsupportedOperationException("Not implemented yet");
             }
             SapiSyncAnchor sapiAnchor = (SapiSyncAnchor)src.getConfig().getSyncAnchor();
             if (Log.isLoggable(Log.TRACE)) {
                 Log.trace(TAG_LOG, "Last download anchor is: " + sapiAnchor.getDownloadAnchor());
             }
             Date anchor = new Date(sapiAnchor.getDownloadAnchor());
 
             SapiSyncHandler.ChangesSet changesSet = sapiSyncHandler.getIncrementalChanges(anchor, remoteUri);
             if (changesSet != null) {
                 if (Log.isLoggable(Log.DEBUG)) {
                     Log.debug(TAG_LOG, "There are changes pending on the server");
                 }
 
                 // Use the above value as timestamp for the next sync
                 downloadNextAnchor = changesSet.timeStamp;
 
                 addedInfo   = fetchItemsInfo(src, changesSet.added);
                 updatedInfo = fetchItemsInfo(src, changesSet.updated);
                 deletedArray = changesSet.deleted;
 
                 if (addedInfo != null) {
                     addedArray = addedInfo.items;
                     addedServerUrl = addedInfo.serverUrl;
                 } 
                 if (updatedInfo != null) {
                     updatedArray = updatedInfo.items;
                     updatedServerUrl = updatedInfo.serverUrl;
                 }
             }
         }
 
         localUpdated = null;
         localDeleted = null;
         if (isUploadPhaseNeeded(syncMode)) {
             localUpdated = new Hashtable();
             localDeleted = new Hashtable();
 
             SyncItem localUpdatedItem = src.getNextUpdatedItem();
             while(localUpdatedItem != null) {
                 localUpdated.put(localUpdatedItem.getKey(), localUpdatedItem);
                 localUpdatedItem = src.getNextUpdatedItem();
             }
 
             SyncItem localDeletedItem = src.getNextDeletedItem();
             while(localDeletedItem != null) {
                 localDeleted.put(localDeletedItem.getKey(), localDeletedItem);
                 localDeletedItem = src.getNextDeletedItem();
             }
         }
 
         // Now we have all the required information to decide what we need
         // to download/upload. It is here that we resolve conflicts and
         // twins
         if (addedArray != null) {
             // The server has items to send.
 
             // First of all check if this command is a real add or an update
             for(int i=0;i<addedArray.length();++i) {
                 JSONObject item = addedArray.getJSONObject(i);
                 String     guid = item.getString("id");
 
                 if (mapping.get(guid) != null) {
                     // This is rather an update because the guid is already
                     // in the mapping table
                     if (Log.isLoggable(Log.INFO)) {
                         Log.info(TAG_LOG, "Turning an add into an update");
                     }
                     // Nullify this item
                     addedArray.put(i, REMOVED_ITEM);
                     if (updatedArray == null) {
                         updatedArray = new JSONArray();
                         updatedServerUrl = addedServerUrl;
                     }
                     updatedArray.put(item);
                 }
             }
         }
 
         // Now check if there is any update which is not an update, but
         // just an add instead (i.e. it does not exist in our mapping)
         if (updatedArray != null) {
             for(int i=0;i<updatedArray.length();++i) {
                 JSONObject item = updatedArray.getJSONObject(i);
                 String     guid = item.getString("id");
 
                 if (mapping.get(guid) == null) {
                     // This is rather an add because the guid is not in the
                     // mapping table
                     if (Log.isLoggable(Log.INFO)) {
                         Log.info(TAG_LOG, "Turning an update into an add");
                     }
                     // Nullify this item
                     updatedArray.put(i, REMOVED_ITEM);
                     if (addedArray == null) {
                         addedArray = new JSONArray();
                         addedServerUrl = updatedServerUrl;
                     }
                     addedArray.put(item);
                 }
             }
         }
 
         // Now check the added/updated lists searching for twins and
         // conflicts
         if (addedArray != null) {
             discardTwinAndConflictFromList(src, addedArray, localUpdated,
                     localDeleted, addedServerUrl, mapping);
         }
         if (updatedArray != null) {
             discardTwinAndConflictFromList(src, updatedArray, localUpdated,
                     localDeleted, updatedServerUrl, mapping);
         }
     }
 
     private void prepareFullSync(SyncSource src, int syncMode, boolean resume, StringKeyValueStore mapping)
     throws SyncException, JSONException
     {
         String remoteUri = src.getConfig().getRemoteUri();
         SyncFilter syncFilter = src.getFilter();
 
         int totalCount = -1;
         int filterMaxCount = -1;
         Date filterFrom = null;
 
         Filter fullDownloadFilter = null;
         if(syncFilter != null) {
             fullDownloadFilter = syncFilter.getFullDownloadFilter();
         }
         if(fullDownloadFilter != null) {
             if(fullDownloadFilter != null && fullDownloadFilter.getType()
                     == Filter.ITEMS_COUNT_TYPE) {
                 filterMaxCount = fullDownloadFilter.getCount();
             } else if(fullDownloadFilter != null && fullDownloadFilter.getType()
                     == Filter.DATE_RECENT_TYPE) {
                 filterFrom = new Date(fullDownloadFilter.getDate());
             } else {
                 throw new UnsupportedOperationException("Not implemented yet");
             }
         }
 
         // Get the number of items and notify the listener
         totalCount = sapiSyncHandler.getItemsCount(remoteUri, filterFrom);
 
         // Fill the addedArray
 
         addedArray = null;
         addedServerUrl = null;
 
         int downloadLimit = FULL_SYNC_DOWNLOAD_LIMIT;
         String dataTag = getDataTag(src);
         int offset = 0;
         boolean done = false;
 
         downloadNextAnchor = -1;
         do {
             // Update the download limit given the total amount of items
             // to download
             if((offset + downloadLimit) > totalCount) {
                 downloadLimit = totalCount - offset;
             }
             SapiSyncHandler.FullSet fullSet = sapiSyncHandler.getItems(remoteUri, dataTag, null,
                     Integer.toString(downloadLimit),
                     Integer.toString(offset), filterFrom);
             if (downloadNextAnchor == -1) {
                 downloadNextAnchor = fullSet.timeStamp;
                 addedServerUrl = fullSet.serverUrl;
             }
             if (fullSet != null && fullSet.items != null && fullSet.items.length() > 0) {
                 if (Log.isLoggable(Log.TRACE)) {
                     Log.trace(TAG_LOG, "items = " + fullSet.items.toString());
                 }
 
                 if (addedArray == null) {
                     addedArray = new JSONArray();
                 }
 
                 if (Log.isLoggable(Log.DEBUG)) {
                     Log.debug(TAG_LOG, "");
                 }
                 // Search and discard twins, as there is no need to download
                 // them again
                 discardTwinAndConflictFromList(src, fullSet.items, null, null,
                         fullSet.serverUrl, mapping);
 
                 for(int i=0;i<fullSet.items.length();++i) {
                     JSONObject item = fullSet.items.getJSONObject(i);
 
                     if (item != REMOVED_ITEM) {
                         addedArray.put(item);
                     }
                 }
 
                 offset += fullSet.items.length();
                 if ((fullSet.items.length() < FULL_SYNC_DOWNLOAD_LIMIT)) {
                     done = true;
                 }
             } else {
                 done = true;
             }
         } while(!done);
     }
 
     private void discardTwinAndConflictFromList(SyncSource src, JSONArray items,
                                                 Hashtable localMods, Hashtable localDel,
                                                 String serverUrl, StringKeyValueStore mapping)
     throws JSONException
     {
         if (src instanceof TwinDetectionSource) {
             for(int i=0;i<items.length();++i) {
                 JSONObject item = items.getJSONObject(i);
                 if (item != REMOVED_ITEM) {
                     // First of all we check if we already the very same item
                     String     guid = item.getString("id");
                     long       size = Long.parseLong(item.getString("size"));
                     SyncItem syncItem = createSyncItem(src, guid, SyncItem.STATE_NEW, size, item, serverUrl);
                     syncItem.setGuid(guid);
                     TwinDetectionSource twinSource = (TwinDetectionSource)src;
                     SyncItem twin = twinSource.findTwin(syncItem);
                     if (twin != null) {
                         if (Log.isLoggable(Log.INFO)) {
                             Log.info(TAG_LOG, "Found a twin for incoming command, ignoring it " + guid);
                         }
                         items.put(i, REMOVED_ITEM);
                     }
                     // Now we check if the client has a pending delete for this
                     // item. If an item is scheduled for deletion, then its id
                     // must be in the mapping, so we can get its luid
                     if (mapping != null) {
                         String luid = (String)mapping.get(guid);
                         if (luid != null && localDel != null && localDel.get(luid) != null) {
                             if (Log.isLoggable(Log.INFO)) {
                                 Log.info(TAG_LOG, "Conflict detected, item sent by the server has been deleted on client. Ignoring " + luid);
                             }
                             items.put(i, REMOVED_ITEM);
                         }
                     }
                 }
             }
         }
     }
 
     private SapiSyncHandler.FullSet fetchItemsInfo(SyncSource src, JSONArray items)
     throws JSONException
     {
         SapiSyncHandler.FullSet fullSet = null;
         if (items != null) {
             String dataTag = getDataTag(src);
             JSONArray itemsId = new JSONArray();
             for(int i=0;i<items.length();++i) {
                 int id = Integer.parseInt(items.getString(i));
                 itemsId.put(id);
             }
             if (itemsId.length() > 0) {
                 // Ask for these items
                 fullSet = sapiSyncHandler.getItems(src.getConfig().getRemoteUri(), dataTag,
                         itemsId, null, null, null);
                 if (fullSet != null && fullSet.items != null) {
                     if (Log.isLoggable(Log.TRACE)) {
                         Log.trace(TAG_LOG, "items = " + fullSet.items.toString());
                     }
                 }
             }
         }
         return fullSet;
     }
 
     private void performUploadPhase(SyncSource src, int syncMode, 
             boolean resume, StringKeyValueStore mapping) {
 
         if (Log.isLoggable(Log.INFO)) {
             Log.info(TAG_LOG, "Starting upload phase with mode: " + syncMode);
         }
         Vector sourceStatus = new Vector();
         
         boolean incremental = isIncrementalSync(syncMode);
 
         String remoteUri = src.getConfig().getRemoteUri();
 
         int totalSending = -1;
         if (incremental) {
             totalSending = src.getClientAddNumber();
         } else {
             totalSending = src.getClientItemsNumber();
         }
 
         int maxSending = -1;
         // Apply upload filter to the total items count
         SyncFilter syncFilter = src.getFilter();
         if(syncFilter != null) {
             Filter uploadFilter = incremental ? 
                 syncFilter.getIncrementalUploadFilter() :
                 syncFilter.getFullUploadFilter();
             if(uploadFilter != null && uploadFilter.getType() == Filter.ITEMS_COUNT_TYPE) {
                 maxSending = uploadFilter.getCount();
             }
         }
 
 
         // Exclude twins from total items count
         totalSending -= twins.size();
 
         if (Log.isLoggable(Log.INFO)) {
             Log.info(TAG_LOG, "Uploading items count: " + totalSending);
         }
 
         if(totalSending > 0) {
             if(maxSending > 0 && totalSending > maxSending) {
                 totalSending = maxSending;
             }
             getSyncListenerFromSource(src).startSending(totalSending, 0, 0);
         }
 
         int uploadedCount = 0;
         SyncItem item = getNextItemToUpload(src, incremental);
         
         while(item != null && itemsCountFilter(maxSending, uploadedCount)) {
 
             try {
                 // Exclude twins
                 if(twins.contains(item.getKey())) {
                     if (Log.isLoggable(Log.INFO)) {
                         Log.info(TAG_LOG, "Exclude twin item to be uploaded: "
                                 + item.getKey());
                     }
                     sourceStatus.addElement(new ItemStatus(item.getKey(),
                         SyncSource.SUCCESS_STATUS));
                     continue;
                 }
 
                 // If the item was already sent in a previously interrupted
                 // sync, then we do not send it again
                 boolean uploadDone = false;
                 String remoteKey = null;
                 if (resume) {
                     int itemStatus = syncStatus.getSentItemStatus(item.getKey());
                     if (itemStatus != -1 && itemStatus != SyncSource.SUCCESS_STATUS) {
                         // This item has either error or was interrupted
                         if (itemStatus == SyncSource.INTERRUPTED_STATUS) {
                             if (Log.isLoggable(Log.INFO)) {
                                 Log.info(TAG_LOG, "Resuming upload for " + item.getKey());
                             }
 
                             remoteKey = syncStatus.getSentItemGuid(item.getKey());
                             item.setGuid(remoteKey);
                             remoteKey = sapiSyncHandler.resumeItemUpload(item,
                                     remoteUri, getSyncListenerFromSource(src));
                             // If the returned key is the same as the item guid,
                             // the item has been resumed correctly.
                             if(remoteKey.equals(item.getGuid())) {
                                 syncStatus.addSentResumedItem(item.getKey());
                             }
                             uploadDone = true;
                         }
                     }
                 }
 
                 if (!uploadDone) {
                     // Upload the item to the server
                     remoteKey = sapiSyncHandler.uploadItem(item, remoteUri,
                             getSyncListenerFromSource(src));
                 }
 
                 item.setGuid(remoteKey);
                 mapping.add(remoteKey, item.getKey());
                 
                 // Set the item status
                 sourceStatus.addElement(new ItemStatus(item.getKey(),
                         SyncSource.SUCCESS_STATUS));
 
                 syncStatus.addSentItem(item.getKey(), item.getState());
                 uploadedCount++;
             } catch (ItemUploadInterruptionException ex) {
                 // An item could not be fully uploaded
                 if (Log.isLoggable(Log.INFO)) {
                     Log.info(TAG_LOG, "Error uploading item " + item.getKey());
                 }
                 syncStatus.setSentItemStatus(item.getKey(), SyncSource.INTERRUPTED_STATUS);
                 sourceStatus.addElement(new ItemStatus(item.getKey(), SyncSource.INTERRUPTED_STATUS));
                 try {
                     syncStatus.save();
                 } catch (Exception e) {
                     Log.error(TAG_LOG, "Cannot save sync status", e);
                 }
             } catch (QuotaOverflowException ex) {
                 // An item could not be uploaded because user quota on server exceeded
                 if (Log.isLoggable(Log.INFO)) {
                     Log.info(TAG_LOG, "Server quota overflow error");
                 }
                 sourceStatus.addElement(new ItemStatus(item.getKey(),
                         SyncSource.SERVER_FULL_ERROR_STATUS));
             } catch(Exception ex) {
                 if(Log.isLoggable(Log.ERROR)) {
                     Log.error(TAG_LOG, "Failed to upload item with key: " + item.getKey(), ex);
                 }
                 sourceStatus.addElement(new ItemStatus(item.getKey(),
                         SyncSource.ERROR_STATUS));
             } finally {
                 item = getNextItemToUpload(src, incremental);
                 cancelSyncIfNeeded(src);
             }
         }
 
         if(incremental) {
             // Updates and deletes are not propagated, return a success status 
             // for each item anyway
             if (localUpdated != null) {
                 Enumeration updKeys = localUpdated.keys();
                 while(updKeys.hasMoreElements()) {
                     String updKey = (String)updKeys.nextElement();
                     SyncItem update = (SyncItem)localUpdated.get(updKey);
                     sourceStatus.addElement(new ItemStatus(update.getKey(), SyncSource.SUCCESS_STATUS));
                 }
             }
             if (localDeleted != null) {
                 Enumeration delKeys = localDeleted.keys();
                 while(delKeys.hasMoreElements()) {
                     String delKey = (String)delKeys.nextElement();
                     SyncItem delete = (SyncItem)localDeleted.get(delKey);
                     sourceStatus.addElement(new ItemStatus(delete.getKey(), SyncSource.SUCCESS_STATUS));
                 }
             }
         }
         
         src.applyItemsStatus(sourceStatus);
     }
 
     private boolean itemsCountFilter(int max, int current) {
         if(max >= 0) {
             return current < max;
         } else {
             return true;
         }
     }
 
     private SyncItem getNextItemToUpload(SyncSource src, boolean incremental) {
         if(incremental) {
             return src.getNextNewItem();
         } else {
             return src.getNextItem();
         }
     }
 
     private void performDownloadPhase(SyncSource src, int syncMode,
             boolean resume, StringKeyValueStore mapping) throws SyncException {
 
         if (Log.isLoggable(Log.INFO)) {
             Log.info(TAG_LOG, "Starting download phase with mode: " + syncMode);
         }
         
         String remoteUri = src.getConfig().getRemoteUri();
 
         SyncFilter syncFilter = src.getFilter();
         
         if (syncMode == SyncSource.FULL_DOWNLOAD) {
 
             if (addedArray != null && addedArray.length() > 0) {
                 getSyncListenerFromSource(src).startReceiving(addedArray.length());
 
                 try {
                     applyNewUpdToSyncSource(src, addedArray, SyncItem.STATE_NEW, 
                                             addedServerUrl, mapping);
                         
                     updateDownloadAnchor(
                             (SapiSyncAnchor) src.getConfig().getSyncAnchor(), 
                             downloadNextAnchor);
 
             // This case is distinct from the one above in that this is for download failures
             // on individual items that disrupt the download while it is progress, while failures
             // due to storage limit breach are "graceful" because they occur before the actual
             // download of the item has started. In future implementations, we might decide to
             // change the logics so that if an item is too large, the next ones can still be
             // downloaded: in such a case, it is appropriate not to call it an interruption.
             // To sum up, interruptions are to be dealt with in this catch block, graceful failures
             // at the end of the try block above
                 } catch (ItemDownloadInterruptionException ide) {
                     // An item could not be downloaded
                     if (Log.isLoggable(Log.INFO)) {
                         Log.info(TAG_LOG, "");
                     }
                 } catch (JSONException je) {
                     Log.error(TAG_LOG, "Cannot parse server data", je);
                 }
             }
         } else if (syncMode == SyncSource.INCREMENTAL_DOWNLOAD) {
             if (Log.isLoggable(Log.TRACE)) {
                 Log.trace(TAG_LOG, "Performing incremental download");
             }
 
             int total = 0;
 
             try {
                 // Count the number of items to be received
                 total += countActualItems(addedArray);
                 total += countActualItems(updatedArray);
                total += countActualDeletedItems(deletedArray);
 
                 getSyncListenerFromSource(src).startReceiving(total);
 
                 if (addedArray != null) {
                     applyNewUpdToSyncSource(src, addedArray, SyncItem.STATE_NEW,
                             addedServerUrl, mapping);
                 }
 
                 if (updatedArray != null) {
                     applyNewUpdToSyncSource(src, updatedArray, SyncItem.STATE_UPDATED,
                             updatedServerUrl, mapping);
                 }
 
                 if (deletedArray != null) {
                     applyDelItems(src, deletedArray, mapping);
                 }
 
                 // Tries to update the anchor if everything went well
                 if (downloadNextAnchor != -1) {
                     SapiSyncAnchor sapiAnchor = (SapiSyncAnchor)src.getConfig().getSyncAnchor();
                     updateDownloadAnchor(sapiAnchor, downloadNextAnchor);
                 }
             } catch (JSONException jse) {
                 Log.error(TAG_LOG, "Error applying server changes", jse);
                 throw new SyncException(SyncException.CLIENT_ERROR, "Error applying server changes");
             }
         }
     }
 
     private int countActualItems(JSONArray items) throws JSONException{
         int count = 0;
         if (items != null) {
             for(int i=0;i<items.length();++i) {
                 JSONObject item = items.getJSONObject(i);
                 if (item != REMOVED_ITEM) {
                     count++;
                 }
             }
         }
         return count;
     }
 
    private int countActualDeletedItems(JSONArray items) throws JSONException {
        // At the moment server deletes cannot be filtered out
        return items.length();
    }

     /**
      * Apply the given items to the source, returning whether the source can
      * accept further items.
      * 
      * @param src
      * @param items
      * @param state
      * @param mapping
      * @param deepTwinSearch
      * @return
      * @throws SyncException
      * @throws JSONException
      */
     private boolean applyNewUpdToSyncSource(SyncSource src, JSONArray items,
                                             char state, String serverUrl,
                                             StringKeyValueStore mapping)
     throws SyncException, JSONException
     {
 
         if (Log.isLoggable(Log.TRACE)) {
             Log.trace(TAG_LOG, "apply new update items to source " + src.getName());
         }
 
         boolean done = false;
         
         // Apply these changes into the sync source
         Vector sourceItems = new Vector();
         for(int k=0; k<items.length() && !done; ++k) {
 
             cancelSyncIfNeeded(src);
 
             JSONObject item = items.getJSONObject(k);
 
             // We must skip items that were removed by the initialization phase
             if (item == REMOVED_ITEM) {
                 continue;
             }
 
             String     guid = item.getString("id");
             long       size = Long.parseLong(item.getString("size"));
 
             String luid;
             if (state == SyncItem.STATE_UPDATED) {
                 luid = mapping.get(guid);
             } else {
                 // This is an add. If the item is already present in the mapping
                 // then this is an add
                 luid = guid;
             }
 
             // If the client doesn't have the luid we change the state to new
             if(StringUtil.isNullOrEmpty(luid)) {
                 state = SyncItem.STATE_NEW;
             }
 
             // Create the item
             SyncItem syncItem = createSyncItem(src, luid, state, size, item, serverUrl);
             syncItem.setGuid(guid);
 
             // Notify the listener
             if (state == SyncItem.STATE_NEW) {
                 getSyncListenerFromSource(src).itemAddReceivingStarted(luid, null, size);
             } else if(state == SyncItem.STATE_UPDATED) {
                 getSyncListenerFromSource(src).itemReplaceReceivingStarted(luid, null, size);
             }
             
             // Filter downloaded items for JSONSyncSources only
             if(src instanceof JSONSyncSource) {
                 if(((JSONSyncSource)src).filterSyncItem(syncItem)) {
                     sourceItems.addElement(syncItem);
                 } else {
                     if (Log.isLoggable(Log.DEBUG)) {
                         Log.debug(TAG_LOG, "Item rejected by the source: " + luid);
                     }
                 }
             } else {
                 sourceItems.addElement(syncItem);
             }
             
             // Notify the listener
             if (state == SyncItem.STATE_NEW) {
                 getSyncListenerFromSource(src).itemAddReceivingEnded(luid, null);
             } else if(state == SyncItem.STATE_UPDATED) {
                 getSyncListenerFromSource(src).itemReplaceReceivingEnded(luid, null);
             }
         }
         // Apply the items in the sync source
         sourceItems = src.applyChanges(sourceItems);
         
         // The sourceItems returned by the call contains the LUID,
         // so we can create the luid/guid map here
         for(int l=0;l<sourceItems.size();++l) {
             SyncItem newItem = (SyncItem)sourceItems.elementAt(l);
             // Update the sync status
             if (newItem.getKey() != null) {
                 syncStatus.addReceivedItem(newItem.getGuid(), newItem.getKey(),
                         newItem.getState(), newItem.getSyncStatus());
             }
             // and the mapping table
             if (state == SyncItem.STATE_NEW && newItem.getSyncStatus() != SyncSource.DEVICE_FULL_ERROR_STATUS) {
                 if (Log.isLoggable(Log.TRACE)) {
                     Log.trace(TAG_LOG, "Updating mapping info for: " +
                             newItem.getGuid() + "," + newItem.getKey());
                 }
                 mapping.add(newItem.getGuid(), newItem.getKey());
             }
         }
         return done;
     }
 
     private SyncItem createSyncItem(SyncSource src, String luid, char state, 
             long size, JSONObject item, String serverUrl) throws JSONException {
 
         SyncItem syncItem = null;
         if(src instanceof JSONSyncSource) {
             syncItem = ((JSONSyncSource)src).createSyncItem(
                     luid, src.getType(), state, null, item, serverUrl);
         } else {
             // A generic sync item needs to be filled with the json item content
             syncItem = src.createSyncItem(luid, src.getType(), state, null, size);
             OutputStream os = null;
             try {
                 os = syncItem.getOutputStream();
                 os.write(item.toString().getBytes());
                 os.close();
             } catch (IOException ioe) {
                 Log.error(TAG_LOG, "Cannot write into sync item stream", ioe);
                 // Ignore this item and continue
             } finally {
                 try {
                     if (os != null) {
                         os.close();
                     }
                 } catch (IOException ioe) {
                 }
             }
         }
         return syncItem;
     }
 
     private void applyDelItems(SyncSource src, JSONArray removed, 
             StringKeyValueStore mapping) throws SyncException, JSONException {
         Vector delItems = new Vector();
         for(int i=0;i < removed.length();++i) {
             String guid = removed.getString(i);
             String luid = mapping.get(guid);
             if (luid == null) {
                 if (Log.isLoggable(Log.INFO)) {
                     Log.info(TAG_LOG, "Cannot delete item with unknown luid " + guid);
                 }
             } else {
                 SyncItem delItem = new SyncItem(luid, src.getType(),
                         SyncItem.STATE_DELETED, null);
                 delItem.setGuid(guid);
                 delItems.addElement(delItem);
                 getSyncListenerFromSource(src).itemDeleted(delItem);
             }
         }
 
         if (delItems.size() > 0) {
             src.applyChanges(delItems);
         }
     }
 
     private void performFinalizationPhase(SyncSource src) {
         sapiSyncHandler.logout();
         if(src != null) {
             src.endSync();
         }
     }
 
     private SyncListener getSyncListenerFromSource(SyncSource source) {
         SyncListener slistener = source.getListener();
         if(slistener != null) {
             return slistener;
         } else {
             return basicListener;
         }
     }
 
     private int getActualSyncMode(SyncSource src, int syncMode) {
         SyncAnchor anchor = src.getSyncAnchor();
         if(anchor instanceof SapiSyncAnchor) {
             SapiSyncAnchor sapiAnchor = (SapiSyncAnchor)anchor;
             if(syncMode == SyncSource.INCREMENTAL_SYNC) {
                 if(sapiAnchor.getUploadAnchor() == 0) {
                     return SyncSource.FULL_SYNC;
                 }
             } else if(syncMode == SyncSource.INCREMENTAL_UPLOAD) {
                 if(sapiAnchor.getUploadAnchor() == 0) {
                     return SyncSource.FULL_UPLOAD;
                 }
             } else if(syncMode == SyncSource.INCREMENTAL_DOWNLOAD) {
                 if(sapiAnchor.getDownloadAnchor() == 0) {
                     return SyncSource.FULL_DOWNLOAD;
                 }
             }
             return syncMode;
         } else {
             throw new SyncException(SyncException.ILLEGAL_ARGUMENT,
                     "Invalid source anchor format");
         }
     }
 
     private int getActualDownloadSyncMode(SyncSource src) {
         SyncAnchor anchor = src.getSyncAnchor();
         if(anchor instanceof SapiSyncAnchor) {
             SapiSyncAnchor sapiAnchor = (SapiSyncAnchor)anchor;
             if(sapiAnchor.getDownloadAnchor() > 0) {
                 return SyncSource.INCREMENTAL_DOWNLOAD;
             } else {
                 return SyncSource.FULL_DOWNLOAD;
             }
         } else {
             throw new SyncException(SyncException.ILLEGAL_ARGUMENT,
                     "Invalid source anchor format");
         }
     }
 
     private int getActualUploadSyncMode(SyncSource src) {
         SyncAnchor anchor = src.getSyncAnchor();
         if(anchor instanceof SapiSyncAnchor) {
             SapiSyncAnchor sapiAnchor = (SapiSyncAnchor)anchor;
             if(sapiAnchor.getUploadAnchor() > 0) {
                 return SyncSource.INCREMENTAL_UPLOAD;
             } else {
                 return SyncSource.FULL_UPLOAD;
             }
         } else {
             throw new SyncException(SyncException.ILLEGAL_ARGUMENT,
                     "Invalid source anchor format");
         }
     }
 
     private boolean isIncrementalSync(int syncMode) {
         return (syncMode == SyncSource.INCREMENTAL_SYNC) ||
                (syncMode == SyncSource.INCREMENTAL_DOWNLOAD) ||
                (syncMode == SyncSource.INCREMENTAL_UPLOAD);
     }
 
     private boolean isDownloadPhaseNeeded(int syncMode) {
         return ((syncMode == SyncSource.FULL_DOWNLOAD) ||
                 (syncMode == SyncSource.FULL_SYNC) ||
                 (syncMode == SyncSource.INCREMENTAL_SYNC) ||
                 (syncMode == SyncSource.INCREMENTAL_DOWNLOAD));
     }
 
     private boolean isUploadPhaseNeeded(int syncMode) {
         return (syncMode == SyncSource.FULL_SYNC) ||
                (syncMode == SyncSource.FULL_UPLOAD) ||
                (syncMode == SyncSource.INCREMENTAL_SYNC) ||
                (syncMode == SyncSource.INCREMENTAL_UPLOAD);
     }
 
     private String getDataTag(SyncSource src) {
         String dataTag = null;
         if (src instanceof JSONSyncSource) {
             JSONSyncSource jsonSyncSource = (JSONSyncSource)src;
             dataTag = jsonSyncSource.getDataTag();
         }
         if (dataTag == null) {
             // This is the default value
             dataTag = src.getConfig().getRemoteUri() + "s";
         }
         return dataTag;
     }
     
     /**
      * Checks for download failures on individual items due to storage limit breach
      * and if they did not occur, updates the download anchor.
      * 
      * @param sapiAnchor the SAPI sync anchor to update
      * @param newDownloadAnchor the new value
      */
     private void updateDownloadAnchor(SapiSyncAnchor sapiAnchor, long newDownloadAnchor) {
         int itemsNotDownloaded = 
             syncStatus.getNumberOfReceivedItemsWithSyncStatus(SyncSource.DEVICE_FULL_ERROR_STATUS);
         if (itemsNotDownloaded > 0) {
             if (Log.isLoggable(Log.TRACE)) {
                 Log.trace(TAG_LOG, "The download anchor will not be updated because there are " + 
                         itemsNotDownloaded + " items that could not be downloaded for lack of " +
                         "storage space on the device");
             }
             
         } else {
             if (Log.isLoggable(Log.TRACE)) {
                 Log.trace(TAG_LOG, "Updating download anchor to " + newDownloadAnchor);
             }
             sapiAnchor.setDownloadAnchor(newDownloadAnchor);
         }
     }
 
     private int getListenerStatusFromSyncException(SyncException se) {
         if (Log.isLoggable(Log.TRACE)) {
             Log.trace(TAG_LOG, "getting listener status for " + se.getCode());
         }
         int syncStatus;
         switch (se.getCode()) {
             case SyncException.AUTH_ERROR:
                 syncStatus = SyncListener.INVALID_CREDENTIALS;
                 break;
             case SyncException.FORBIDDEN_ERROR:
                 syncStatus = SyncListener.FORBIDDEN_ERROR;
                 break;
             case SyncException.CONN_NOT_FOUND:
                 syncStatus = SyncListener.CONN_NOT_FOUND;
                 break;
             case SyncException.READ_SERVER_RESPONSE_ERROR:
                 syncStatus = SyncListener.READ_SERVER_RESPONSE_ERROR;
                 break;
             case SyncException.WRITE_SERVER_REQUEST_ERROR:
                 syncStatus = SyncListener.WRITE_SERVER_REQUEST_ERROR;
                 break;
             case SyncException.SERVER_CONNECTION_REQUEST_ERROR:
                 syncStatus = SyncListener.SERVER_CONNECTION_REQUEST_ERROR;
                 break;
             case SyncException.BACKEND_AUTH_ERROR:
                 syncStatus = SyncListener.BACKEND_AUTH_ERROR;
                 break;
             case SyncException.NOT_FOUND_URI_ERROR:
                 syncStatus = SyncListener.URI_NOT_FOUND_ERROR;
                 break;
             case SyncException.CONNECTION_BLOCKED_BY_USER:
                 syncStatus = SyncListener.CONNECTION_BLOCKED_BY_USER;
                 break;
             case SyncException.SMART_SLOW_SYNC_UNSUPPORTED:
                 syncStatus = SyncListener.SMART_SLOW_SYNC_UNSUPPORTED;
                 break;
             case SyncException.CLIENT_ERROR:
                 syncStatus = SyncListener.CLIENT_ERROR;
                 break;
             case SyncException.ACCESS_ERROR:
                 syncStatus = SyncListener.ACCESS_ERROR;
                 break;
             case SyncException.DATA_NULL:
                 syncStatus = SyncListener.DATA_NULL;
                 break;
             case SyncException.ILLEGAL_ARGUMENT:
                 syncStatus = SyncListener.ILLEGAL_ARGUMENT;
                 break;
             case SyncException.SERVER_ERROR:
                 syncStatus = SyncListener.SERVER_ERROR;
                 break;
             case SyncException.SERVER_BUSY:
                 syncStatus = SyncListener.SERVER_BUSY;
                 break;
             case SyncException.BACKEND_ERROR:
                 syncStatus = SyncListener.BACKEND_ERROR;
                 break;
             case SyncException.CANCELLED:
                 syncStatus = SyncListener.CANCELLED;
                 break;
             case SyncException.ERR_READING_COMPRESSED_DATA:
                 syncStatus = SyncListener.COMPRESSED_RESPONSE_ERROR;
                 break;
             case SyncException.DEVICE_FULL:
                 syncStatus = SyncListener.SERVER_FULL_ERROR;
                 break;
             case SyncException.LOCAL_DEVICE_FULL:
                 syncStatus = SyncListener.LOCAL_CLIENT_FULL_ERROR;
                 break;
             default:
                 syncStatus = SyncListener.GENERIC_ERROR;
                 break;
         }
         return syncStatus;
     }
 }
