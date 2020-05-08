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
 
 import com.funambol.org.json.me.JSONArray;
 import com.funambol.org.json.me.JSONException;
 import com.funambol.org.json.me.JSONObject;
 import com.funambol.sync.BasicSyncListener;
 import com.funambol.sync.ItemStatus;
 import com.funambol.sync.SyncAnchor;
 import com.funambol.sync.SyncConfig;
 import com.funambol.sync.SyncException;
 import com.funambol.sync.SyncItem;
 import com.funambol.sync.SyncListener;
 import com.funambol.sync.SyncSource;
 import com.funambol.sync.ResumableSource;
 import com.funambol.sync.SyncManagerI;
 import com.funambol.sapisync.source.JSONSyncSource;
 import com.funambol.sapisync.source.JSONSyncItem;
 import com.funambol.sapisync.source.util.ResumeException;
 import com.funambol.sapisync.source.util.HttpDownloader;
 import com.funambol.sapisync.source.util.DownloadException;
 import com.funambol.sync.Filter;
 import com.funambol.sync.SyncFilter;
 import com.funambol.sync.DeviceConfigI;
 import com.funambol.sync.ItemDownloadInterruptionException;
 import com.funambol.sync.NonBlockingSyncException;
 import com.funambol.util.Log;
 import com.funambol.util.StringUtil;
 
 
 /**
  * <code>SapiSyncManager</code> represents the synchronization engine performed
  * via SAPI.
  */
 public class SapiSyncManager implements SyncManagerI {
 
     private static final String TAG_LOG = "SapiSyncManager";
 
     private static final JSONObject REMOVED_ITEM = new JSONObject();
 
     private SyncConfig syncConfig = null;
     private SapiSyncHandler sapiSyncHandler = null;
     private SapiSyncStatus syncStatus = null;
     private String deviceId = null;
 
     // Holds the list of twins found during the download phase, those items must
     // not be uploaded to the server later in the upload phase
     private Hashtable twins = null;
 
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
 
     private SyncSource currentSource = null;
 
     private JSONArray addedArray   = null;
     private JSONArray updatedArray = null;
     private JSONArray deletedArray = null;
 
     private Hashtable localUpdates = null;
     private Hashtable localDeletes = null;
     private Enumeration localUpdatesEnum = null;
     private Enumeration localDeletesEnum = null;
 
     private String addedServerUrl = null;
     private String updatedServerUrl = null;
 
     private long downloadNextAnchor;
 
 
     private long clientServerTimeDifference = 0;
 
     private SapiSyncStrategy strategy = null;
     private SapiSyncUtils utils = null;
 
 
     /**
      * <code>SapiSyncManager</code> constructor
      * @param config
      */
     public SapiSyncManager(SyncConfig config, DeviceConfigI devConfig) {
         this.syncConfig = config;
         this.sapiSyncHandler = new SapiSyncHandler(
                 StringUtil.extractAddressFromUrl(syncConfig.getSyncUrl()),
                 syncConfig.getUserName(),
                 syncConfig.getPassword());
         strategy = new SapiSyncStrategy(sapiSyncHandler, REMOVED_ITEM);
         utils = new SapiSyncUtils();
         this.deviceId = devConfig.getDevID();
     }
 
     /**
      * Force a specific SapiSyncHandler to be used for testing purposes.
      * @param sapiSyncHandler
      */
     public void setSapiSyncHandler(SapiSyncHandler sapiSyncHandler) {
         this.sapiSyncHandler = sapiSyncHandler;
         strategy.setSapiSyncHandler(sapiSyncHandler);
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
 
         currentSource = src;
 
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
             if (Log.isLoggable(Log.INFO)) {
                 Log.info(TAG_LOG, "Resume is active");
             }
         } else {
             try {
                 if (Log.isLoggable(Log.INFO)) {
                     Log.info(TAG_LOG, "Resume is not active");
                 }
                 syncStatus.reset();
                 syncStatus.setInterrupted(true);
             } catch (IOException ioe) {
                 Log.error(TAG_LOG, "Cannot reset status", ioe);
             }
         }
 
         MappingTable mapping = new MappingTable(src.getName());
 
         SapiSyncAnchor sapiAnchor = (SapiSyncAnchor)src.getSyncAnchor();
         if (sapiAnchor.getDownloadAnchor() == 0 && sapiAnchor.getUploadAnchor() == 0) {
             // This is the first sync with this server, clean the mapping
             if (Log.isLoggable(Log.INFO)) {
                 Log.info(TAG_LOG, "Resetting mapping");
             }
             try {
                 mapping.reset();
             } catch (Exception e) {
                 if (Log.isLoggable(Log.INFO)) {
                     Log.info(TAG_LOG, "The mapping store does not exist, use an empty one");
                 }
             }
         } else {
             try {
                 mapping.load();
             } catch (Exception e) {
                 if (Log.isLoggable(Log.INFO)) {
                     Log.info(TAG_LOG, "The mapping store does not exist, use an empty one");
                 }
             }
         }
 
         // Init twins vector
         twins = new Hashtable();
         try {
             
             Throwable downloadNonBlockingError = null;
             Throwable uploadNonBlockingError = null;
             
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
 
             performInitializationPhase(src, syncMode, resume, mapping);
 
             cancelSyncIfNeeded(src);
 
             getSyncListenerFromSource(src).syncStarted(getActualSyncMode(src, syncMode));
 
             if(isDownloadPhaseNeeded(syncMode)) {
                 try {
                     // The download anchor is updated once it is received from the server
                     performDownloadPhase(src, getActualDownloadSyncMode(src), resume, mapping);
                 } catch (NonBlockingSyncException nbse) {
                     // Carries on
                     if (Log.isLoggable(Log.INFO)) {
                         Log.info(TAG_LOG, 
                                 "Caught a non blocking exception (code " + nbse.getCode() + 
                                 ") during download, sync will continue");
                     }
                     downloadNonBlockingError = nbse;
                 }
             }
 
             cancelSyncIfNeeded(src);
             
             if(isUploadPhaseNeeded(syncMode)) {
                 try {
                     long newUploadAnchor = (new Date()).getTime();
                     performUploadPhase(src, getActualUploadSyncMode(src), resume, mapping);
                     // If we had no error so far, then we update the anchor
                     SapiSyncAnchor anchor = (SapiSyncAnchor)src.getSyncAnchor();
                     anchor.setUploadAnchor(newUploadAnchor);
                     
                 // This catch block is not used so far, but it is here in case in the future we introduce
                 // non-blocking exceptions in the upload phase too.
                 } catch (NonBlockingSyncException nbse) {
                     // Carries on
                     if (Log.isLoggable(Log.INFO)) {
                         Log.info(TAG_LOG, 
                                 "Caught a non blocking exception (code " + nbse.getCode() + 
                                 ") during upload, sync will continue");
                     }
                     uploadNonBlockingError = nbse;
                 }
             }
 
             cancelSyncIfNeeded(src);
 
             performFinalizationPhase(src);
 
             if (uploadNonBlockingError != null) { // upload errors prevail
                 throw uploadNonBlockingError;
             } else if (downloadNonBlockingError != null) {
                 throw downloadNonBlockingError;
             } else {
                 syncStatus.setInterrupted(false);
                 syncStatus.setStatusCode(SyncListener.SUCCESS);
             }
 
         // This catch block can be reached directly when sync-blocking exceptions are thrown, or
         // indirectly when non-blocking exceptions are re-thrown at the end of the sync (a few lines 
         // above).
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
 
             currentSource = null;
         }
     }
 
     public void cancel() {
         if (Log.isLoggable(Log.INFO)) {
             Log.info(TAG_LOG, "Cancelling sync");
         }
         cancel = true;
         sapiSyncHandler.cancel();
         if(currentSource != null) {
             currentSource.cancel();
         }
     }
 
     private void cancelSyncIfNeeded(SyncSource src) throws SyncException {
         if(cancel) {
             performFinalizationPhase(null);
             throw new SyncException(SyncException.CANCELLED, "Sync got cancelled");
         }
     }
 
     private void performInitializationPhase(SyncSource src, int syncMode, boolean resume,
                                             MappingTable mapping)
     throws SyncException, JSONException
     {
         // Prepare the source for the sync
         src.beginSync(getActualSyncMode(src, syncMode), resume);
 
         try {
             // Perform a login to avoid multiple authentications
             long clientNow = System.currentTimeMillis();
             long serverNow = sapiSyncHandler.login(deviceId);
             if (serverNow > 0) {
                 clientServerTimeDifference = serverNow - clientNow;
                 strategy.setClientServerTimeDifference(clientServerTimeDifference);
                 if (Log.isLoggable(Log.DEBUG)) {
                     Log.debug(TAG_LOG, "Difference in time between server and client is " + clientServerTimeDifference);
                 }
             } else {
                 clientServerTimeDifference = 0;
             }
         } catch (SapiException e) {
             String errorMessage = "Cannot login";
             utils.processCommonSapiExceptions(e, errorMessage, false);
             utils.processCustomSapiExceptions(e, errorMessage, false);
 
             throw new SyncException(SyncException.AUTH_ERROR, errorMessage);
         }
 
         int downloadSyncMode = SyncSource.NO_SYNC;
         int uploadSyncMode   = SyncSource.NO_SYNC;
 
         if (isDownloadPhaseNeeded(syncMode)) {
             downloadSyncMode = getActualDownloadSyncMode(src);
         }
 
         if (isUploadPhaseNeeded(syncMode)) {
             uploadSyncMode   = getActualUploadSyncMode(src);
         }
 
         boolean incrementalDownload = isIncrementalSync(downloadSyncMode);
         boolean incrementalUpload = isIncrementalSync(uploadSyncMode);
         strategy.prepareSync(src, downloadSyncMode, uploadSyncMode, resume, mapping,
                              incrementalDownload, incrementalUpload, twins);
         addedArray   = strategy.getServerAddedItems();
         updatedArray = strategy.getServerUpdatedItems();
         deletedArray = strategy.getServerDeletedItems();
         downloadNextAnchor = strategy.getDownloadNextAnchor();
         addedServerUrl = strategy.getAddedServerUrl();
         updatedServerUrl = strategy.getUpdatedServerUrl();
 
         localUpdates = strategy.getLocalUpdates();
         if (localUpdates != null) {
             localUpdatesEnum = localUpdates.elements();
         }
         localDeletes = strategy.getLocalDeletes();
         if (localDeletes != null) {
             Vector itemStatuses = new Vector();
             localDeletesEnum = localDeletes.elements();
             // If the client reported deletes, then we can update the mapping
             // accordinlgly
             while(localDeletesEnum.hasMoreElements()) {
                 SyncItem item = (SyncItem)localDeletesEnum.nextElement();
                 String guid = mapping.getGuid(item.getKey());
                 if (Log.isLoggable(Log.DEBUG)) {
                     Log.debug(TAG_LOG, "Removing entry from mapping " + guid);
                 }
                 mapping.remove(guid);
                 // Also notify the source as this command was propagated with
                 // success so it won't provide it again
                 ItemStatus status = new ItemStatus(item.getKey(), SyncSource.SUCCESS_STATUS);
                 itemStatuses.addElement(status);
             }
             src.applyItemsStatus(itemStatuses);
         }
     }
 
 
     private void performUploadPhase(SyncSource src, int syncMode, boolean resume, MappingTable mapping) {
 
         if (Log.isLoggable(Log.INFO)) {
             Log.info(TAG_LOG, "Starting upload phase with mode: " + syncMode);
         }
         Vector sourceStatus = new Vector();
         
         boolean incremental = isIncrementalSync(syncMode);
 
         String remoteUri = src.getConfig().getRemoteUri();
 
         int totalSending = -1;
         if (incremental) {
             totalSending = src.getClientAddNumber() + src.getClientReplaceNumber();
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
             if(uploadFilter != null && uploadFilter.isEnabled() &&
                uploadFilter.getType() == Filter.ITEMS_COUNT_TYPE)
             {
                 maxSending = uploadFilter.getCount();
                 // If we are resuming a sync, then we must consider the items
                 // that were sent in previous sync
                 if (resume) {
                     if (Log.isLoggable(Log.DEBUG)) {
                         Log.debug(TAG_LOG, "Since we are resuming count the items previously sent "
                                            + syncStatus.getSentAddNumber());
                     }
                     maxSending = maxSending - syncStatus.getSentAddNumber();
                 }
                 if (Log.isLoggable(Log.DEBUG)) {
                     Log.debug(TAG_LOG, "Setting up items count filter with maxSending=" + maxSending);
                 }
             }
         }
 
         // Exclude twins from total items count
         totalSending -= twins.size();
 
         if(totalSending > 0) {
             if(maxSending > 0 && totalSending > maxSending) {
                 totalSending = maxSending;
             }
             getSyncListenerFromSource(src).startSending(totalSending, 0, 0);
         }
 
         if (Log.isLoggable(Log.INFO)) {
             Log.info(TAG_LOG, "Uploading items count: " + totalSending);
         }
 
         int uploadedCount = 0;
         JSONSyncItem item = getNextItemToUpload(src, incremental);
 
         try {
             while(item != null && itemsCountFilter(maxSending, uploadedCount)) {
                 try {
                     // Exclude twins
                     if(twins.get(item.getKey()) != null) {
                         if (Log.isLoggable(Log.INFO)) {
                             Log.info(TAG_LOG, "Exclude twin item to be uploaded: "
                                     + item.getKey());
                         }
                         sourceStatus.addElement(new ItemStatus(item.getKey(),
                             SyncSource.SUCCESS_STATUS));
                         continue;
                     }
 
                     // Notify the listener
                     if (item.getState() == SyncItem.STATE_NEW) {
                        getSyncListenerFromSource(src).itemAddSendingStarted(item.getKey(), null, 0);
                     } else if (item.getState() == SyncItem.STATE_UPDATED) {
                        getSyncListenerFromSource(src).itemReplaceSendingStarted(item.getKey(), null, 0);
                     }
 
                     // If the item was already sent in a previously interrupted
                     // sync, then we do not send it again
                     boolean uploadDone = false;
                     String remoteKey = null;
                     if (resume) {
                         int itemStatus = syncStatus.getSentItemStatus(item.getKey());
                         if (itemStatus == SyncSource.SUCCESS_STATUS) {
                             // TODO: check if it has changed since then
                             uploadDone = true;
                             if (Log.isLoggable(Log.INFO)) {
                                 Log.info(TAG_LOG, "Skipping upload for " + item.getKey() + " which has been previously uploaded");
                             }
                         } else if (itemStatus == SyncSource.INTERRUPTED_STATUS) {
                             if (Log.isLoggable(Log.INFO)) {
                                 Log.info(TAG_LOG, "Resuming upload for " + item.getKey());
                             }
                             remoteKey = syncStatus.getSentItemGuid(item.getKey());
                             String origGuid = remoteKey;
                             item.setGuid(remoteKey);
                             try {
                                 remoteKey = sapiSyncHandler.resumeItemUpload(item,
                                         remoteUri, getSyncListenerFromSource(src));
                                 // Update the mapping table
                                 if(item.getState() == SyncItem.STATE_UPDATED) {
                                     mapping.update(remoteKey, item.getKey(), 
                                             Long.toString(item.getContentSize()),
                                             item.getContentName());
                                 } else {
                                     mapping.add(remoteKey, item.getKey(), 
                                             Long.toString(item.getContentSize()),
                                             item.getContentName());
                                 }
                             } catch (SapiException e) {
                                 verifyErrorInUploadResponse(e, item, remoteKey, sourceStatus);
                             }
                             // If the returned key is the same as the item guid,
                             // the item has been resumed correctly.
                             if(remoteKey.equals(origGuid)) {
                                 syncStatus.addSentResumedItem(item.getKey());
                             }
                             uploadDone = true;
                         }
                     }
                     
                     if (!uploadDone) {
                         try {
                             // Sets the status as interrupted so that if the
                             // client crashes badly we still remember this fact
                             if (item.getState() == SyncItem.STATE_UPDATED) {
                                 String luid = item.getKey();
                                 // Check if the item key has been updated
                                 if(item.isItemKeyUpdated()) {
                                     luid = item.getOldKey();
                                 }
                                 remoteKey = mapping.getGuid(luid);
                                 item.setGuid(remoteKey);
                             }
                             // Check if this is only a file rename.
                             // Using save-metadata to update the file name doesn't
                             // work becouse it requires the file content to be
                             // re-uploaded
                             if(item.isItemKeyUpdated() && !item.isItemContentUpdated()) {
                                 // This is only a item rename
                                 sapiSyncHandler.updateItemName(remoteUri, remoteKey,
                                         item.getJSONFileObject().getName());
                             } else {
                                 remoteKey = sapiSyncHandler.prepareItemUpload(item, remoteUri);
                                 item.setGuid(remoteKey);
                                 if (item.getState() == SyncItem.STATE_NEW || item.getState() == SyncItem.STATE_UPDATED) {
                                     syncStatus.addSentItem(item.getGuid(), item.getKey(), item.getState(),
                                             SyncSource.INTERRUPTED_STATUS);
                                     try {
                                         syncStatus.save();
                                     } catch (Exception e) {
                                         Log.error(TAG_LOG, "Cannot save sync status", e);
                                     }
                                 }
                                 // Upload the item to the server
                                 sapiSyncHandler.uploadItem(item, remoteUri, getSyncListenerFromSource(src));
                             }
                             // Update the mapping table
                             if(item.getState() == SyncItem.STATE_UPDATED) {
                                 mapping.update(remoteKey, item.getKey(), 
                                         Long.toString(item.getContentSize()),
                                         item.getContentName());
                             } else {
                                 mapping.add(remoteKey, item.getKey(), 
                                         Long.toString(item.getContentSize()),
                                         item.getContentName());
                             }
                         } catch (SapiException e) {
                             verifyErrorInUploadResponse(e, item, item.getGuid(), sourceStatus);
                         }
                     } 
                     syncStatus.setSentItemStatus(item.getGuid(), item.getKey(),
                             item.getState(), SyncSource.SUCCESS_STATUS);
                     try {
                         syncStatus.save();
                     } catch (Exception e) {
                         Log.error(TAG_LOG, "Cannot save sync status", e);
                     }
 
                     // Set the item status
                     sourceStatus.addElement(new ItemStatus(item.getKey(),
                             SyncSource.SUCCESS_STATUS));
 
                 } catch(SyncException ex) {
                     //relaunch managed sync exception
                     throw ex;
                 } catch(Exception ex) {
                     //generic errors catch, just in case...
                     if(Log.isLoggable(Log.ERROR)) {
                         Log.error(TAG_LOG, "Failed to upload item with key: " +
                                 item.getKey(), ex);
                     }
                     sourceStatus.addElement(new ItemStatus(item.getKey(),
                             SyncSource.ERROR_STATUS));
                 } finally {
                     // Notify the listener
                     if (item.getState() == SyncItem.STATE_NEW) {
                         getSyncListenerFromSource(src).itemAddSendingEnded(item.getKey(), null);
                     } else if (item.getState() == SyncItem.STATE_UPDATED) {
                         getSyncListenerFromSource(src).itemReplaceSendingEnded(item.getKey(), null);
                     }
                     uploadedCount++;
                     item = getNextItemToUpload(src, incremental);
                     cancelSyncIfNeeded(src);
                 }
             }
         } finally {
             src.applyItemsStatus(sourceStatus);
         }
     }
 
     private boolean itemsCountFilter(int max, int current) {
         if(max >= 0) {
             return current < max;
         } else {
             return true;
         }
     }
 
     private JSONSyncItem getNextItemToUpload(SyncSource src, boolean incremental) {
         if(incremental) {
             JSONSyncItem item = (JSONSyncItem)src.getNextNewItem();
             if (item == null) {
                 // New items are over, now check for updates
                 if (localUpdatesEnum != null && localUpdatesEnum.hasMoreElements()) {
                     item = (JSONSyncItem)localUpdatesEnum.nextElement();
                 }
             }
             return item;
         } else {
             return (JSONSyncItem)src.getNextItem();
         }
     }
 
     private void performDownloadPhase(SyncSource src, int syncMode, boolean resume, MappingTable mapping)
     throws SyncException {
 
         if (Log.isLoggable(Log.INFO)) {
             Log.info(TAG_LOG, "Starting download phase with mode: " + syncMode);
         }
        
         if (syncMode == SyncSource.FULL_DOWNLOAD) {
             if (Log.isLoggable(Log.TRACE)) {
                 Log.trace(TAG_LOG, "Performing full download");
             }
             if (addedArray != null && addedArray.length() > 0) {
                 getSyncListenerFromSource(src).startReceiving(addedArray.length());
 
                 try {
                     applyNewUpdToSyncSource(src, addedArray, SyncItem.STATE_NEW, 
                                             addedServerUrl, mapping, resume);
                         
                     updateDownloadAnchor((SapiSyncAnchor) src.getConfig().getSyncAnchor(), downloadNextAnchor);
                 } catch (JSONException je) {
                     Log.error(TAG_LOG, "Cannot parse server data", je);
                     throw new SyncException(SyncException.CLIENT_ERROR, je.toString());
                 }
             } else {
                 // If there are no items to download we can simply update the
                 // download anchor
                 updateDownloadAnchor((SapiSyncAnchor) src.getConfig().getSyncAnchor(), downloadNextAnchor);
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
                             addedServerUrl, mapping, resume);
                 }
 
                 if (updatedArray != null) {
                     applyNewUpdToSyncSource(src, updatedArray, SyncItem.STATE_UPDATED,
                             updatedServerUrl, mapping, resume);
                 }
 
                 if (deletedArray != null) {
                     applyDelItems(src, deletedArray, mapping);
                 }
 
                 // Tries to update the anchor if everything went well
                 if (downloadNextAnchor != -1) {
                     updateDownloadAnchor(
                             (SapiSyncAnchor)src.getConfig().getSyncAnchor(),
                             downloadNextAnchor);
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
         if (items != null) {
             int count = 0;
             for(int i=0;i<items.length();++i) {
                 String guid = items.getString(i);
                 if (guid != null && guid.length() > 0) {
                     count++;
                 }
             }
             return count;
         } else {
             return 0;
         }
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
                                             MappingTable mapping, boolean resume)
     throws SyncException, JSONException
     {
 
         if (Log.isLoggable(Log.TRACE)) {
             Log.trace(TAG_LOG, "apply new update items to source " + src.getName());
         }
 
         boolean done = false;
         
         // Apply these changes into the sync source
         for(int k=0; k<items.length() && !done; ++k) {
 
             cancelSyncIfNeeded(src);
 
             JSONObject item = items.getJSONObject(k);
 
             // We must skip items that were removed by the initialization phase
             if (item == REMOVED_ITEM) {
                 continue;
             }
 
             String guid = item.getString("id");
             long size = Long.parseLong(item.getString("size"));
 
             String luid;
             if (state == SyncItem.STATE_UPDATED) {
                 luid = mapping.getLuid(guid);
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
             JSONSyncItem syncItem = (JSONSyncItem)utils.createSyncItem(src, luid, state, size, item, serverUrl);
             syncItem.setGuid(guid);
 
             // Notify the listener
             if (syncItem.getState() == SyncItem.STATE_NEW) {
                 getSyncListenerFromSource(src).itemAddReceivingStarted(syncItem.getKey(), syncItem.getParent(), size);
             } else if (syncItem.getState() == SyncItem.STATE_UPDATED) {
                 getSyncListenerFromSource(src).itemReplaceReceivingStarted(syncItem.getKey(), syncItem.getParent(), size);
             }
 
             // Check if this item just got renamed remotely
             boolean downloadContent = !item.has("nocontent");
 
             if (downloadContent) {
                 // Shall we resume this item download?
                 if (resume && syncStatus.getReceivedItemStatus(guid) == SyncSource.INTERRUPTED_STATUS) {
                     if (src instanceof ResumableSource) {
                         long partialLength = 0;
                         ResumableSource rss = (ResumableSource)src;
                         partialLength = rss.getPartiallyReceivedItemSize(rss.getLuid(syncItem));
                         if (partialLength > 0) {
                             if (Log.isLoggable(Log.INFO)) {
                                 Log.info(TAG_LOG, "Found an item whose download can be resumed at " + partialLength);
                             }
                             // Notify the sync status that we are trying to resume
                             syncStatus.addReceivedResumedItem(guid);
                             syncItem.setPartialLength(partialLength);
                         }
                     }
                 }
                 syncItem.setItemContentUpdated(true);
             } else {
                 if (Log.isLoggable(Log.DEBUG)) {
                     Log.debug(TAG_LOG, "No content will be downloaded");
                 }
                 syncItem.setItemContentUpdated(false);
             }
 
             // Was the item renamed
             if (item.has("oldkey")) {
                 syncItem.setOldKey(item.getString("oldkey"));
                 syncItem.setItemKeyUpdated(true);
             } else {
                 syncItem.setItemKeyUpdated(false);
             }
 
             // Filter downloaded items for JSONSyncSources only
             if(src instanceof JSONSyncSource) {
                 if(!((JSONSyncSource)src).filterSyncItem(syncItem)) {
                     if (Log.isLoggable(Log.DEBUG)) {
                         Log.debug(TAG_LOG, "Item rejected by the source: " + luid);
                     }
                     continue;
                 }
             }
 
             if (downloadContent) {
                 // Download and apply the item 
                 if (src instanceof ResumableSource) {
                     ResumableSource rss = (ResumableSource)src;
                     if (syncItem.getState() == SyncItem.STATE_NEW || syncItem.getState() == SyncItem.STATE_UPDATED) {
                         String tmpLuid = rss.getLuid(syncItem);
                         if (luid != null) {
                             syncStatus.addReceivedItem(syncItem.getGuid(), tmpLuid, syncItem.getState(), SyncSource.INTERRUPTED_STATUS);
                         }
                     }
                     try {
                         syncStatus.save();
                     } catch (IOException ioe) {
                         Log.error(TAG_LOG, "Cannot save sync status", ioe);
                     }
                 }
 
                 // Download the item content
                 try {
                     downloadItemContent(src, syncItem);
                 } catch (IOException ioe) {
                     Log.error(TAG_LOG, "Cannot write item content to local disk", ioe);
                     throw new SyncException(SyncException.STORAGE_ERROR, "Cannot write item content to local disk");
                 }
             }
 
             // Apply the item to the source
             Vector tmpItems = new Vector();
             tmpItems.addElement(syncItem);
             src.applyChanges(tmpItems);
 
             if (syncItem.getSyncStatus() != -1 && syncItem.getGuid() != null && syncItem.getKey() != null) {
                 syncStatus.setReceivedItemStatus(syncItem.getGuid(), syncItem.getKey(),
                                                  syncItem.getState(), syncItem.getSyncStatus());
                 // and the mapping table (if luid and guid are different)
                 if (state == SyncItem.STATE_NEW && !syncItem.getGuid().equals(syncItem.getKey())) {
                     if (Log.isLoggable(Log.TRACE)) {
                         Log.trace(TAG_LOG, "Updating mapping info for: " +
                                 syncItem.getGuid() + "," + syncItem.getKey());
                     }
                     mapping.add(syncItem.getGuid(), syncItem.getKey(), "" + syncItem.getContentSize(),
                                 syncItem.getContentName());
                 } else if (state == SyncItem.STATE_UPDATED && item.has("oldkey")) {
                     if (Log.isLoggable(Log.TRACE)) {
                         Log.trace(TAG_LOG, "Updating mapping info for renamed item: " +
                                 syncItem.getGuid() + "," + syncItem.getKey());
                     }
                     mapping.update(syncItem.getGuid(), syncItem.getKey(), "" + syncItem.getContentSize(), syncItem.getContentName());
                 }
             }
             // Notify the listener
             if (syncItem.getState() == SyncItem.STATE_NEW) {
                 getSyncListenerFromSource(src).itemAddReceivingEnded(syncItem.getKey(), syncItem.getParent());
             } else if (syncItem.getState() == SyncItem.STATE_UPDATED) {
                 getSyncListenerFromSource(src).itemReplaceReceivingEnded(syncItem.getKey(), syncItem.getParent());
             }
         }
 
         return done;
     }
 
     private void downloadItemContent(SyncSource src, JSONSyncItem item) throws SyncException, IOException {
 
         // Check if this item has a link to data that must be downloaded,
         // otherwise we just take the item content
         String url = item.getContentUrl(syncConfig.getSyncUrl());
         if (url != null) {
             if (Log.isLoggable(Log.DEBUG)) {
                 Log.debug(TAG_LOG, "item has remote content, prepare to download it");
             }
             HttpDownloader downloader = new HttpDownloader();
 
             downloader.setDownloadListener(new DownloadSyncListener(item, src.getListener()));
             long size = item.getContentSize();
 
             OutputStream fileos = null;
 
             try {
                 fileos = item.getOutputStream();
                 long partialLength = item.getPartialLength();
 
                 long actualSize;
                 if (partialLength > 0) {
                     actualSize = downloader.resume(url, fileos, size, partialLength, item.getContentName());
                 } else {
                     actualSize = downloader.download(url, fileos, size, item.getContentName());
                 }
                 if (Log.isLoggable(Log.DEBUG)) {
                     Log.debug(TAG_LOG, "size is " + size + " actual size is " + actualSize);
                 }
                 // This should never happen, but we check for safety
                 if (size != actualSize) {
                     // The download was interrupted. We shall keep track of this interrupted download
                     // so that it can be resumed
                     if (Log.isLoggable(Log.INFO)) {
                         Log.info(TAG_LOG, "Item download was interrupted at " + actualSize);
                     }
                     throw new ItemDownloadInterruptionException(item, actualSize);
                 }
             } catch (ResumeException re) {
                 // The item download cannot be resumed properly
                 // Re-create a new output stream without appending
                 fileos.close();
                 fileos = item.getOutputStream();
                 // Download the item from scratch
                 try {
                     long actualSize = downloader.download(url, fileos, size, item.getContentName());
                     if (size != actualSize) {
                         // The download was interrupted. We shall keep track of this interrupted download
                         // so that it can be resumed
                         throw new ItemDownloadInterruptionException(item, actualSize);
                     }
                 } catch (DownloadException de) {
                     throw new ItemDownloadInterruptionException(item, de.getPartialLength());
                 }
             } catch (DownloadException de) {
                 // We had a network error while download the item. Propagate the
                 // exception as the sync must be interrupted
                 if (Log.isLoggable(Log.DEBUG)) {
                     Log.debug(TAG_LOG, "Cannot download item, interrupt sync " + de.getPartialLength());
                 }
                 if(de.getCode() == DownloadException.CODE_CANCELLED) {
                     throw new ItemDownloadInterruptionException(SyncException.CANCELLED, item, de.getPartialLength());
                 } else {
                     throw new ItemDownloadInterruptionException(item, de.getPartialLength());
                 }
             } finally {
                 if (fileos != null) {
                     try {
                         fileos.close();
                     } catch (IOException ioe) {
                         Log.error(TAG_LOG, "Cannot close output stream", ioe);
                     }
                 }
             }
         } else {
             // there is no remote content to download
             OutputStream os = null;
             try {
                 os = item.getOutputStream();
                 os.write(item.getContent());
             } finally {
                 if (os != null) {
                     try {
                         os.close();
                     } catch (IOException ioe) {
                         Log.error(TAG_LOG, "Cannot close output stream", ioe);
                     }
                 }
             }
         }
     }
 
     private void applyDelItems(SyncSource src, JSONArray removed, 
                                MappingTable mapping) throws SyncException, JSONException
     {
         if (Log.isLoggable(Log.TRACE)) {
             Log.trace(TAG_LOG, "applyDelItems");
         }
         Vector delItems = new Vector();
         for(int i=0;i < removed.length();++i) {
             String guid = removed.getString(i);
 
             if (guid != null && guid.length() > 0) {
                 String luid = mapping.getLuid(guid);
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
                     syncStatus.addReceivedItem(guid, luid, delItem.getState(), SyncSource.SUCCESS_STATUS);
                 }
             }
         }
 
         if (delItems.size() > 0) {
             src.applyChanges(delItems);
         }
     }
 
     private void performFinalizationPhase(SyncSource src)
     throws SyncException {
         try {
             sapiSyncHandler.logout();
         } catch (SapiException e) {
             String errorMessage = "Cannot logout";
             utils.processCommonSapiExceptions(e, errorMessage, false);
             utils.processCustomSapiExceptions(e, errorMessage, true);
         }
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
 
     private int getListenerStatusFromSyncException(SyncException se) {
         if (Log.isLoggable(Log.TRACE)) {
             Log.trace(TAG_LOG, "getting listener status for " + se.getCode());
         }
         int statusCode;
         switch (se.getCode()) {
             case SyncException.AUTH_ERROR:
                 statusCode = SyncListener.INVALID_CREDENTIALS;
                 break;
             case SyncException.FORBIDDEN_ERROR:
                 statusCode = SyncListener.FORBIDDEN_ERROR;
                 break;
             case SyncException.CONN_NOT_FOUND:
                 statusCode = SyncListener.CONN_NOT_FOUND;
                 break;
             case SyncException.READ_SERVER_RESPONSE_ERROR:
                 statusCode = SyncListener.READ_SERVER_RESPONSE_ERROR;
                 break;
             case SyncException.WRITE_SERVER_REQUEST_ERROR:
                 statusCode = SyncListener.WRITE_SERVER_REQUEST_ERROR;
                 break;
             case SyncException.SERVER_CONNECTION_REQUEST_ERROR:
                 statusCode = SyncListener.SERVER_CONNECTION_REQUEST_ERROR;
                 break;
             case SyncException.BACKEND_AUTH_ERROR:
                 statusCode = SyncListener.BACKEND_AUTH_ERROR;
                 break;
             case SyncException.NOT_FOUND_URI_ERROR:
                 statusCode = SyncListener.URI_NOT_FOUND_ERROR;
                 break;
             case SyncException.CONNECTION_BLOCKED_BY_USER:
                 statusCode = SyncListener.CONNECTION_BLOCKED_BY_USER;
                 break;
             case SyncException.SMART_SLOW_SYNC_UNSUPPORTED:
                 statusCode = SyncListener.SMART_SLOW_SYNC_UNSUPPORTED;
                 break;
             case SyncException.CLIENT_ERROR:
                 statusCode = SyncListener.CLIENT_ERROR;
                 break;
             case SyncException.ACCESS_ERROR:
                 statusCode = SyncListener.ACCESS_ERROR;
                 break;
             case SyncException.DATA_NULL:
                 statusCode = SyncListener.DATA_NULL;
                 break;
             case SyncException.ILLEGAL_ARGUMENT:
                 statusCode = SyncListener.ILLEGAL_ARGUMENT;
                 break;
             case SyncException.SERVER_ERROR:
                 statusCode = SyncListener.SERVER_ERROR;
                 break;
             case SyncException.SERVER_BUSY:
                 statusCode = SyncListener.SERVER_BUSY;
                 break;
             case SyncException.BACKEND_ERROR:
                 statusCode = SyncListener.BACKEND_ERROR;
                 break;
             case SyncException.CANCELLED:
                 statusCode = SyncListener.CANCELLED;
                 break;
             case SyncException.NOT_SUPPORTED:
                 statusCode = SyncListener.NOT_SUPPORTED;
                 break;
             case SyncException.ERR_READING_COMPRESSED_DATA:
                 statusCode = SyncListener.COMPRESSED_RESPONSE_ERROR;
                 break;
             case SyncException.DEVICE_FULL:
                 statusCode = SyncListener.SERVER_FULL_ERROR;
                 break;
             case SyncException.LOCAL_DEVICE_FULL:
                 statusCode = SyncListener.LOCAL_CLIENT_FULL_ERROR;
                 break;
             default:
                 statusCode = SyncListener.GENERIC_ERROR;
                 break;
         }
         return statusCode;
     }
     
 
     private void updateDownloadAnchor(SapiSyncAnchor sapiAnchor, long newDownloadAnchor) {
         if (Log.isLoggable(Log.INFO)) {
             Log.info(TAG_LOG, "Updating download anchor to " + newDownloadAnchor);
         }
         sapiAnchor.setDownloadAnchor(newDownloadAnchor);
     }
 
     
     /**
      * Common code used to verify specific error in upload sapi
      * (size mismatch, over quota etc)
      * 
      * @param sapiException
      * @param item
      * @param remoteKey
      * @param sourceStatus 
      * @throws SyncException
      */
     private void verifyErrorInUploadResponse(
             SapiException sapiException,
             SyncItem item,
             String remoteKey,
             Vector sourceStatus) 
     throws SyncException
     {
         //manage common errors
         utils.processCommonSapiExceptions(sapiException, "Cannot upload item", false);
 
         //manage custom errors
         utils.processCustomSapiExceptions(sapiException, "Cannot upload item", false);
         
         if (SapiException.MED_1002.equals(sapiException.getCode())
                 || SapiException.CUS_0001.equals(sapiException.getCode())) {
 
             // An item could not be fully uploaded
             if (Log.isLoggable(Log.INFO)) {
                 Log.info(TAG_LOG, "Error uploading item " + item.getKey());
             }
 
             // The size of the uploading media does not match the one declared
             item.setGuid(remoteKey);
             
             syncStatus.addSentItem(item.getGuid(), item.getKey(),
                     item.getState(), SyncSource.INTERRUPTED_STATUS);
             sourceStatus.addElement(new ItemStatus(item.getKey(),
                     SyncSource.INTERRUPTED_STATUS));
             // Interrupt the sync with a network error
             throw new SyncException(SyncException.CONN_NOT_FOUND, sapiException.getMessage());
             
         } else if(SapiException.MED_1007.equals(sapiException.getCode())) {
             // An item could not be uploaded because user quota on
             // server exceeded
             if (Log.isLoggable(Log.INFO)) {
                 Log.info(TAG_LOG, "Server quota overflow error");
             }
             sourceStatus.addElement(new ItemStatus(item.getKey(),
                     SyncSource.SERVER_FULL_ERROR_STATUS));
             throw new SyncException(SyncException.DEVICE_FULL, "Server quota exceeded");
 
         } else {
             throw new SyncException(
                     SyncException.SERVER_ERROR,
                     "Cannot upload item, error in SAPI response: " + sapiException.getMessage());
         }
     }    
 
     /**
      * Translates the HttpDownloader.DownloadListener calls into SyncListener calls.
      */
     private class DownloadSyncListener implements HttpDownloader.DownloadListener {
 
         private SyncListener syncListener = null;
         private String itemKey = null;
         private String itemParent = null;
         private char itemState;
 
         public DownloadSyncListener(SyncItem item, SyncListener syncListener) {
             this.syncListener = syncListener;
             this.itemKey = item.getKey();
             this.itemParent = item.getParent();
             this.itemState = item.getState();
         }
 
         public void downloadStarted(long totalSize) {
         }
 
         public void downloadProgress(long size) {
             if(syncListener != null) {
                 if(itemState == SyncItem.STATE_NEW) {
                     syncListener.itemAddReceivingProgress(itemKey, itemParent, size);
                 } else {
                     syncListener.itemReplaceReceivingProgress(itemKey, itemParent, size);
                 }
             }
         }
 
         public void downloadEnded() {
         }
     }
 }
