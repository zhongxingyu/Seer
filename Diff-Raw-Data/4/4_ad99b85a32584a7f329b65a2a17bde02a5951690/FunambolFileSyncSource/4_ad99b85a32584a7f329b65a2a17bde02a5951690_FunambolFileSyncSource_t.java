 /**
  * Funambol is a mobile platform developed by Funambol, Inc. 
  * Copyright (C) 2011 Funambol, Inc.
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
 
 package com.funambol.client.source;
 
 import java.io.IOException;
 import java.io.OutputStream;
 import java.util.Vector;
 
 import com.funambol.org.json.me.JSONObject;
 import com.funambol.org.json.me.JSONException;
 
 import com.funambol.client.customization.Customization;
 import com.funambol.storage.Table;
 import com.funambol.storage.Tuple;
 import com.funambol.storage.QueryFilter;
 import com.funambol.storage.QueryResult;
 import com.funambol.platform.FileAdapter;
 import com.funambol.platform.FileSystemInfo;
 
 import com.funambol.sync.SourceConfig;
 import com.funambol.sync.SyncSource;
 import com.funambol.sync.SyncItem;
 import com.funambol.sync.SyncException;
 import com.funambol.sync.NonBlockingSyncException;
 import com.funambol.sync.client.ChangesTracker;
 import com.funambol.sync.client.StorageLimitException;
 import com.funambol.sync.client.StorageLimit;
 import com.funambol.sapisync.source.FileSyncSource;
 import com.funambol.sapisync.source.JSONFileObject;
 
 import com.funambol.util.Log;
 
 
 public class FunambolFileSyncSource extends FileSyncSource {
 
     private static final String TAG_LOG = "FunambolFileSyncSource";
 
     protected Customization customization;
     protected Table metadata;
     
     //------------------------------------------------------------- Constructors
 
     public FunambolFileSyncSource(SourceConfig config, ChangesTracker tracker, 
                                   String directory, String tempDirectory,
                                   long maxItemSize,
                                   Customization customization,
                                   Table metadata) {
 
         super(config, tracker, directory, tempDirectory, maxItemSize, NO_LIMIT_ON_ITEM_AGE);
         this.customization = customization;
         this.metadata = metadata;
     }
 
     public void beginSync(int syncMode, boolean resume) throws SyncException {
         // Ensure that the directory to sync exists
         String sdCardRoot = FileSystemInfo.getSDCardRoot();
         if(sdCardRoot != null) {
             if(getDirectory().startsWith(sdCardRoot) && !FileSystemInfo.isSDCardAvailable()) {
                 // The directory to synchronize is on the sd card but actually
                 // it is not available
                 throw new SyncException(SyncException.SD_CARD_UNAVAILABLE,
                         "The sd card is not available");
             }
         }
         try {
             // Create the default folder if it doesn't exist
             FileAdapter d = new FileAdapter(getDirectory());
             if(!d.exists()) {
                 d.mkdir();
             }
             d.close();
         } catch(IOException ex) {
             Log.error(TAG_LOG, "Cannot create directory to sync: " + getDirectory(), ex);
         }
         super.beginSync(syncMode, resume);
     }
 
     /**
      * @throws a SyncException if the quota on server is reached
      */
     public void setItemStatus(String key, int status) throws SyncException {
         if (status == SyncSource.SERVER_FULL_ERROR_STATUS) {
             // The user reached his quota on the server
             if (Log.isLoggable(Log.INFO)) {
                 Log.info(TAG_LOG, "Server is full");
             }
             throw new SyncException(SyncException.DEVICE_FULL, "Server is full");
         }
         super.setItemStatus(key, status);
         // If the status is successfull then we mark this item as synchronized
         if (status == SyncSource.SUCCESS_STATUS) {
             QueryResult result = null;
             try {
                 metadata.open();
                 QueryFilter filter = metadata.createQueryFilter(key);
                 result = metadata.query(filter);
                 if (result.hasMoreElements()) {
                     Tuple item = result.nextElement();
                     item.setField(metadata.getColIndexOrThrow(MediaMetadata.METADATA_SYNCHRONIZED), 1);
                     metadata.update(item);
                     metadata.save();
                 } else {
                     Log.error(TAG_LOG, "Cannot update metadata table");
                     throw new NonBlockingSyncException(SyncException.STORAGE_ERROR, "Cannot update metadata table");
                 }
             } catch (Exception e) {
                 Log.error(TAG_LOG, "Cannot update metadata table", e);
                 throw new NonBlockingSyncException(SyncException.STORAGE_ERROR, "Cannot update metadata table");
             } finally {
                 if (result != null) {
                     try {
                         result.close();
                     } catch (Exception e) {}
                 }
                 try {
                     metadata.close();
                 } catch (Exception e) {}
             }
         }
     }
 
     public SyncItem createSyncItem(String key, String type, char state,
                                    String parent, JSONObject json,
                                    String serverUrl) throws JSONException
     {
         ThumbnailItem item = new ThumbnailItem(key, type, state, parent, json, serverUrl);
         return item;
     }
 
     protected int addItem(SyncItem item) throws SyncException {
 
         try {
             renameAddedItem(item);
         } catch (Exception e) {
             Log.error(TAG_LOG, "Cannot rename incoming item", e);
             throw new SyncException(SyncException.STORAGE_ERROR, "Cannot rename incoming item");
         }
 
         // Create a new entry in the metadata table
         try {
             ThumbnailItem ti = (ThumbnailItem)item;
             JSONFileObject fo = ti.getJSONFileObject();
 
             Long lastMod;
             if (fo.getLastModifiedDate() > 0) {
                 lastMod = new Long(fo.getLastModifiedDate());
             } else {
                 lastMod = new Long(fo.getCreationDate());
             }
 
             metadata.open();
             // The parent source fills the item key with the file path
             String thumbPath = item.getKey();
             // The key is autoincremented
             Tuple tuple = metadata.createNewRow();
 
             tuple.setField(metadata.getColIndexOrThrow(MediaMetadata.METADATA_THUMB1_PATH), thumbPath);
             tuple.setField(metadata.getColIndexOrThrow(MediaMetadata.METADATA_THUMB2_PATH), "");
             tuple.setField(metadata.getColIndexOrThrow(MediaMetadata.METADATA_ITEM_PATH), "");
             tuple.setField(metadata.getColIndexOrThrow(MediaMetadata.METADATA_LAST_MOD), lastMod);
             tuple.setField(metadata.getColIndexOrThrow(MediaMetadata.METADATA_SYNCHRONIZED), 1);
             tuple.setField(metadata.getColIndexOrThrow(MediaMetadata.METADATA_DELETED), 0);
             tuple.setField(metadata.getColIndexOrThrow(MediaMetadata.METADATA_DIRTY), 0);
 
             metadata.insert(tuple);
             metadata.save();
 
             // Update the item key with the table id
             if (Log.isLoggable(Log.DEBUG)) {
                 Log.debug(TAG_LOG, "Setting item key " + tuple.getKey().toString());
             }
             item.setKey(tuple.getKey().toString());
         } catch (Exception e) {
             Log.error(TAG_LOG, "Cannot update metadata table", e);
             throw new NonBlockingSyncException(SyncException.STORAGE_ERROR, "Cannot update metadata table");
         } finally {
             try {
                 metadata.close();
             } catch (Exception e) {
             }
         }
         // Finally we can update the tracker
        int res = updateTracker(item);
        return res;
     }
 
     public int deleteItem(String key) throws SyncException {
         int res = super.deleteItem(key);
         try {
             metadata.open();
             metadata.delete(key);
             metadata.save();
         } catch (Exception e) {
             Log.error(TAG_LOG, "Cannot update metadata table", e);
             throw new NonBlockingSyncException(SyncException.STORAGE_ERROR, "Cannot update metadata table");
         } finally {
             try {
                 metadata.close();
             } catch (Exception e) {
             }
         }
         return res;
     }
  
 
     protected OutputStream getDownloadOutputStream(String name, long size, boolean isUpdate,
             boolean isThumbnail, boolean append) throws IOException {
         try {
             grantStorageSpaceFor(tempDirectory, size); // TODO What if isUpdate is true?
         } catch (StorageLimitException sle) {
             throw sle.getCorrespondingSyncException();
         }
         return super.getDownloadOutputStream(name, size, isUpdate, isThumbnail, append);
     }
 
     /**
      * @throws StorageLimitException if size 
      */
     protected void grantStorageSpaceFor(String path, long size)
             throws StorageLimitException, IOException {
         StorageLimit threshold = customization.getStorageLimit();
         Log.trace(TAG_LOG, "Checking storage space before downloading item");
         FileSystemInfo fsInfo = new FileSystemInfo(path);
         threshold.check(size, path,
                 fsInfo.getAvailableBlocks(),
                 fsInfo.getTotalUsableBlocks(),
                 fsInfo.getBlockSize());
     }
 
     protected class ThumbnailItem extends JSONSyncSourceItem {
 
         //private static final String THUMB_SIZE = "504";
         private static final String THUMB_SIZE = "176";
 
         public ThumbnailItem(String key, String type, char state, String parent,
                              JSONObject jsonObject, String serverUrl)
         throws JSONException
         {
             super(key, type, state, parent, jsonObject, serverUrl);
         }
 
         public ThumbnailItem(String key, String type, char state, String parent,
                             JSONFileObject jsonFileObject)
         throws JSONException
         {
             super(key, type, state, parent, jsonFileObject);
         }
 
 
         /*
          * This method returns the url content for this item. This url can be
          * anything and just needs to point to the actual content. If there is
          * no remote content, but the content is within the item itself, then this
          * method shall return null.
          */
         public String getContentUrl(String syncUrl) {
 
             JSONFileObject fileObject = getJSONFileObject();
             if (fileObject != null) {
                 Vector thumbnails = fileObject.getThumbnails();
                 JSONFileObject.JSONFileThumbnail thumb = null;
                 JSONFileObject.JSONFileThumbnail firstThumb = null;
                 if (thumbnails != null) {
                     for(int i=0;i<thumbnails.size();++i) {
                         thumb = (JSONFileObject.JSONFileThumbnail)thumbnails.elementAt(i);
                         if (firstThumb == null) {
                             firstThumb = thumb;
                         }
                         // TODO FIXME: get the thumbnail with the best match
                         if (THUMB_SIZE.equals(thumb.getSize())) {
                             break;
                         }
                     }
                 }
                 if (thumb == null) {
                     thumb = firstThumb;
                 }
                 if (thumb != null) {
                     return composeUrl(syncUrl, fileObject.getServerUrl(), thumb.getUrl());
                 }
             }
             return null;
         }
 
         // The content size is unknown
         public long getContentSize() {
             return -1;
         }
     }
 }
 
