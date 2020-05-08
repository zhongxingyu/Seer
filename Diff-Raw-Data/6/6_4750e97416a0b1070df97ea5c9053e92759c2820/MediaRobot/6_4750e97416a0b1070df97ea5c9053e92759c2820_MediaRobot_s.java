 /*
  * Funambol is a mobile platform developed by Funambol, Inc. 
  * Copyright (C) 2010 Funambol, Inc.
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
 
 package com.funambol.client.test.media;
 
 import java.util.Enumeration;
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 
 import com.funambol.client.source.AppSyncSource;
 import com.funambol.client.source.MediaMetadata;
 import com.funambol.client.source.FunambolFileSyncSource;
 import com.funambol.client.source.AppSyncSourceManager;
 import com.funambol.client.test.BasicScriptRunner;
 import com.funambol.client.test.Robot;
 import com.funambol.client.test.util.TestFileManager;
 import com.funambol.client.test.basic.BasicUserCommands;
 import com.funambol.client.controller.SourceThumbnailsViewController;
 import com.funambol.client.configuration.Configuration;
 import com.funambol.client.engine.ItemUploadTask;
 
 import com.funambol.sapisync.SapiSyncHandler;
 import com.funambol.sapisync.source.JSONFileObject;
 import com.funambol.sapisync.source.JSONSyncItem;
 import com.funambol.sapisync.source.JSONSyncSource;
 import com.funambol.sapisync.source.FileSyncSource;
 import com.funambol.sync.SyncConfig;
 import com.funambol.sync.SyncItem;
 import com.funambol.sync.SyncSource;
 import com.funambol.platform.FileAdapter;
 import com.funambol.util.Log;
 import com.funambol.util.StringUtil;
 import com.funambol.util.ConnectionManager;
 import com.funambol.storage.Table;
 import com.funambol.storage.TableFactory;
 import com.funambol.storage.Tuple;
 
 import com.funambol.org.json.me.JSONArray;
 import com.funambol.org.json.me.JSONException;
 import com.funambol.org.json.me.JSONObject;
 
 public abstract class MediaRobot extends Robot {
 
     private static final String TAG_LOG = "MediaRobot";
 
     protected AppSyncSourceManager appSourceManager;
     protected TestFileManager fileManager;
     protected Configuration configuration;
 
     protected SapiSyncHandler sapiSyncHandler = null;
 
     public MediaRobot(AppSyncSourceManager appSourceManager,
                       Configuration configuration,
                       TestFileManager fileManager) {
 
         this.appSourceManager = appSourceManager;
         this.configuration = configuration;
         this.fileManager = fileManager;
     }
 
     public MediaRobot(TestFileManager fileManager) {
         this.fileManager = fileManager;
     }
 
     public void deleteMedia(String type, String filename) throws Exception {
         deleteFile(type, filename);
     }
 
     public void deleteFile(String type, String filename) throws Exception {
         SyncSource ss = getAppSyncSource(type).getSyncSource();
         if(ss instanceof FileSyncSource) {
             String dirname = ((FileSyncSource)ss).getDirectory();
             FileAdapter file = new FileAdapter(getFileFullName(dirname, filename));
             if (file.exists()) {
                 file.delete();
             }
         } else {
             throw new IllegalArgumentException("Invalid SyncSource type: " + ss);
         }
     }
 
     public void deleteAllMedia(String type) throws Exception {
         // We need to cleanup both the metadata table and the filesystem
         FunambolFileSyncSource ss = (FunambolFileSyncSource)getAppSyncSource(type).getSyncSource();
         Table metadata = ss.getMetadataTable();
 
         try {
             metadata.open();
             metadata.reset();
             metadata.save();
         } finally {
             metadata.close();
         }
         deleteAllFiles(type);
     }
 
     public void deleteAllFiles(String type) throws Exception {
         FunambolFileSyncSource ss = (FunambolFileSyncSource)getAppSyncSource(type).getSyncSource();
         String dirname = ((FileSyncSource)ss).getDirectory();
 
         FileAdapter dir = new FileAdapter(dirname, true);
         Enumeration files = dir.list(false, false /* Filters hidden files */);
         dir.close();
         while(files.hasMoreElements()) {
             String filename = (String)files.nextElement();
             FileAdapter file = new FileAdapter(getFileFullName(dirname, filename));
             file.delete();
             file.close();
         }
     }
 
     public void overrideMediaContent(String type, String targetFileName, String sourceFileName) throws Throwable {
         FileAdapter file = getMediaOutputStream(type, targetFileName);
         OutputStream os = file.openOutputStream();
         getMediaFile(sourceFileName, os);
     }
 
     public void addMedia(String type, String filename) throws Throwable {
         FileAdapter file = getMediaOutputStream(type, filename);
         OutputStream os = file.openOutputStream();
         getMediaFile(filename, os);
     }
 
     private FileAdapter getMediaOutputStream(String type, String filename) throws Throwable {
         SyncSource source = getAppSyncSource(type).getSyncSource();
         String fullname = null;
         if (source instanceof FileSyncSource) {
             FileSyncSource fss = (FileSyncSource)source;
             filename = getFileNameFromFullName(filename);
             fullname = fss.getFileFullName(filename);
         } else {
             return null;
         }
         fullname = StringUtil.simplifyFileName(fullname);
         FileAdapter f = new FileAdapter(fullname);
         return f;
     }
    
     public void checkMediaCount(String type, int count) throws Throwable {
         int localCount = getFilesCount(type);
         if (count != localCount) {
             Log.error(TAG_LOG, "Expected " + count + " -- found " + localCount);
         }
         assertTrue(count == localCount, "Local media items count mismatch");
     }
 
     /**
      * Add a media to the server. Media content is read based on specified
      * file name (a file in application's assets or an online resource).
      * 
      * @param type sync source type
      * @param filename a relative path to the resource. BaseUrl parameter on test
      *                 configuration defines if the resource is a local file or is
      *                 an URL. In both cases, file is copied locally and then
      *                 uploaded to the server.
      * @throws Throwable
      */
     public void addMediaOnServer(String type, String filename) throws Throwable {
 
         //make file available on local storage
         ByteArrayOutputStream os = new ByteArrayOutputStream();
         String contentType = getMediaFile(filename, os);
 
         byte[] fileContent = os.toByteArray();
         int size = fileContent.length;
         InputStream is = new ByteArrayInputStream(fileContent);
         
         addMediaOnServerFromStream(type, filename, is, size, contentType, null);
     }
 
     public void overrideMediaContentOnServer(String type, String targetFileName, String sourceFileName) throws Throwable {
         String itemId = findMediaIdOnServer(type, targetFileName);
 
         ByteArrayOutputStream os = new ByteArrayOutputStream();
         String contentType = getMediaFile(sourceFileName, os);
 
         byte[] fileContent = os.toByteArray();
         int size = fileContent.length;
         InputStream is = new ByteArrayInputStream(fileContent);
         
         addMediaOnServerFromStream(type, targetFileName, is, size, contentType, itemId);
     }
 
     /**
      * Add a media to the server. Media content is read from a stream.
      * 
      * @param type sync source type
      * @param itemName name of the item to add
      * @param contentStream stream to the content of the item
      * @param contentSize size of the item
      * @param contentType mimetype of the content to add
      * 
      * @throws JSONException
      */
     protected void addMediaOnServerFromStream(String type, String itemName, InputStream contentStream,
                                               long contentSize, String contentType, String guid)
     throws IOException, JSONException {
 
         String fullName = itemName;
         itemName = getFileNameFromFullName(itemName);
         if (Log.isLoggable(Log.DEBUG)) {
             Log.debug(TAG_LOG,
                     "Adding/updating media on server for source " + getRemoteUri(type) +
                     " with name " + itemName +
                     " of type " + contentType +
                     " and size " + contentSize);
         }
         
         // Prepare json item to upload
         JSONFileObject jsonFileObject = new JSONFileObject();
         jsonFileObject.setName(itemName);
         jsonFileObject.setSize(contentSize);
         jsonFileObject.setCreationdate(System.currentTimeMillis());
         jsonFileObject.setLastModifiedDate(System.currentTimeMillis());
         jsonFileObject.setMimetype(contentType);
 
         MediaSyncItem item = new MediaSyncItem("fake_key", "fake_type",
                 guid != null ? SyncItem.STATE_UPDATED : SyncItem.STATE_NEW, null, jsonFileObject,
                 contentStream, contentSize);
 
         if (guid != null) {
             item.setGuid(guid);
         }
 
         SapiSyncHandler sapiHandler = getSapiSyncHandler();
         sapiHandler.login(null);
         String remoteKey = sapiHandler.prepareItemUpload(item, getRemoteUri(type));
         sapiHandler.logout();
         item.setGuid(remoteKey);
 
         // We need to create a temporary table for the ItemUploadTask to be able
         // to perform the upload
        Table tempTable = TableFactory.getInstance().getStringTable("", MediaMetadata.META_DATA_COL_NAMES,
                                                                         MediaMetadata.META_DATA_COL_TYPES,
                                                                         0, true);
         Tuple newRow = tempTable.createNewRow();
         newRow.setField(newRow.getColIndexOrThrow(MediaMetadata.METADATA_ITEM_PATH), fullName);
         newRow.setField(newRow.getColIndexOrThrow(MediaMetadata.METADATA_NAME), itemName);
         newRow.setField(newRow.getColIndexOrThrow(MediaMetadata.METADATA_SIZE), contentSize);
         newRow.setField(newRow.getColIndexOrThrow(MediaMetadata.METADATA_LAST_MOD), System.currentTimeMillis());
         newRow.setField(newRow.getColIndexOrThrow(MediaMetadata.METADATA_MIME), contentType);
         newRow.setField(newRow.getColIndexOrThrow(MediaMetadata.METADATA_GUID), remoteKey);
         tempTable.insert(newRow);
 
         // Now use the ItemUploadTask to perform the actual upload
         try {
             int colIdx = newRow.getColIndexOrThrow(MediaMetadata.METADATA_ID);
             ItemUploadTask uploadTask = new ItemUploadTask(newRow.getLongField(colIdx),
                     0, tempTable, configuration);
             uploadTask.run();
         } finally {
             // We can now drop the table
             tempTable.close();
             tempTable.drop();
         }
 
     }
 
     public void deleteMediaOnServer(String type, String filename)
             throws Throwable {
         SapiSyncHandler sapiHandler = getSapiSyncHandler();
         String itemId = findMediaIdOnServer(type, filename);
         sapiHandler.login(null);
         sapiHandler.deleteItem(itemId, getRemoteUri(type), getDataTag(type));
         sapiHandler.logout();
     }
 
     private String findMediaIdOnServer(String type, String filename)
             throws Throwable {
         JSONObject item = findMediaJSONObjectOnServer(type, filename);
         if(item != null) {
             return item.getString("id");
         }
         return null;
     }
 
     private JSONObject findMediaJSONObjectOnServer(String type, String filename)
             throws Throwable {
         SapiSyncHandler sapiHandler = getSapiSyncHandler();
         sapiHandler.login(null);
         try {
             SapiSyncHandler.FullSet itemsSet = sapiHandler.getItems(
                     getRemoteUri(type), getDataTag(type), null, null, null,
                     null);
             JSONArray items = itemsSet.items;
             for (int i = 0; i < items.length(); ++i) {
                 JSONObject item = items.getJSONObject(i);
                 String aFilename = item.getString("name");
                 if (filename.equals(aFilename)) {
                     return item;
                 }
             }
         } finally {
             sapiHandler.logout();
         }
         return null;
     }
 
     public void checkMediaCountOnServer(String type, int count) throws Throwable
     {
         SapiSyncHandler sapiHandler = getSapiSyncHandler();
         sapiHandler.login(null);
         int actualCount = sapiHandler.getItemsCount(getRemoteUri(type), null);
         sapiHandler.logout();
         assertTrue(actualCount == count, "Items count on server mismatch for " + type);
     }
 
     public void deleteAllMediaOnServer(String type) throws Throwable {
         SapiSyncHandler sapiHandler = getSapiSyncHandler();
         sapiHandler.login(null);
         sapiHandler.deleteAllItems(getRemoteUri(type));
         sapiHandler.logout();
     }
 
     /**
      * Fills the server with some data in order to leave no more space for a
      * further upload of the same file specified as parameter  
      *
      * @param type
      * @param fileName name of the file to use as a reference
      * @throws Throwable
      */
     public void leaveNoFreeServerQuota(String type, String fileName) throws Throwable {
         // get free quota for the current user
         SapiSyncHandler sapiHandler = getSapiSyncHandler();
         sapiHandler.login(null);
         long availableSpace = sapiHandler
                 .getUserAvailableServerQuota(getRemoteUri(type));
         sapiHandler.logout();
         
         //make file available on local storage
         ByteArrayOutputStream os = new ByteArrayOutputStream();
         String contentType = getMediaFile(fileName, os);
         byte[] fileContent = os.toByteArray();
         int pictureSize = fileContent.length;
         long repetitions = availableSpace / pictureSize;
         
         if (repetitions > 0) {
             if (Log.isLoggable(Log.DEBUG)) {
                 Log.debug(TAG_LOG, "Available user quota is " + availableSpace + ", while picture size is " + pictureSize);
                 Log.debug(TAG_LOG, "Filling the user quota, please wait...");
             }
             for (long i=1; i <= repetitions; i++) {
                 String newFileName = i + fileName;
                 Log.trace(TAG_LOG, "Upload file " + newFileName + " on server [" + i + "/" + repetitions + "]");
                 InputStream is = new ByteArrayInputStream(fileContent);
                 addMediaOnServerFromStream(type, i + newFileName, is, pictureSize, contentType, null);
             }
         } else {
             if (Log.isLoggable(Log.DEBUG)) {
                 Log.debug(TAG_LOG, "Available user quota is " + availableSpace + ", less that the required. Nothing to do.");
             }
         }
     }
 
     public void interruptItem(String phase, String itemKey, int pos, int itemIdx) {
         ConnectionManager.getInstance().setBreakInfo(phase, itemKey, pos, itemIdx);
     }
 
     protected AppSyncSourceManager getAppSyncSourceManager() {
         return appSourceManager;
     }
 
 
     /**
      * Returns the AppSyncSource related to the given data type
      * 
      * @param type
      * @return
      */
     protected AppSyncSource getAppSyncSource(String type) {
         if (StringUtil.equalsIgnoreCase(
                 BasicUserCommands.SOURCE_NAME_PICTURES, type)) {
             return getAppSyncSourceManager().getSource(
                     AppSyncSourceManager.PICTURES_ID);
         } else if (StringUtil.equalsIgnoreCase(
                 BasicUserCommands.SOURCE_NAME_VIDEOS, type)) {
             return getAppSyncSourceManager().getSource(
                     AppSyncSourceManager.VIDEOS_ID);
         } else if (StringUtil.equalsIgnoreCase(
                 BasicUserCommands.SOURCE_NAME_FILES, type)) {
             return getAppSyncSourceManager().getSource(
                     AppSyncSourceManager.FILES_ID);
         } else {
             throw new IllegalArgumentException("Invalid type: " + type);
         }
     }
 
     /**
      * Returns the SAPI data tag related to the given data type.
      * 
      * @param type
      * @return
      */
     private String getRemoteUri(String type) {
         return getAppSyncSource(type).getSyncSource().getConfig()
                 .getRemoteUri();
     }
 
     private String getDataTag(String type) {
         SyncSource src = getAppSyncSource(type).getSyncSource();
         String dataTag = null;
         if (src instanceof JSONSyncSource) {
             JSONSyncSource jsonSyncSource = (JSONSyncSource) src;
             dataTag = jsonSyncSource.getDataTag();
         }
         return dataTag;
     }
 
     private SapiSyncHandler getSapiSyncHandler() {
         if (sapiSyncHandler == null) {
             SyncConfig syncConfig = getSyncConfig();
             sapiSyncHandler = new SapiSyncHandler(
                     StringUtil.extractAddressFromUrl(syncConfig.getSyncUrl()),
                     syncConfig.getUserName(), syncConfig.getPassword());
         }
         return sapiSyncHandler;
     }
 
     /**
      * This is used to override the item input stream
      */
     private class MediaSyncItem extends JSONSyncItem {
 
         private InputStream stream;
         private long size;
 
         public MediaSyncItem(String key, String type, char state,
                 String parent, JSONFileObject jsonFileObject,
                 InputStream stream, long size) throws JSONException {
             super(key, type, state, parent, jsonFileObject);
             this.stream = stream;
             this.size = size;
         }
 
         public InputStream getInputStream() throws IOException {
             return stream;
         }
 
         public long getObjectSize() {
             return size;
         }
     }
 
     protected String getMediaFile(String filename, OutputStream output)
             throws Throwable {
         String baseUrl = BasicScriptRunner.getBaseUrl();
         String url = baseUrl + "/" + filename;
         return fileManager.getFile(url, output);
     }
 
     private String getFileFullName(String directory, String name) {
         StringBuffer fullname = new StringBuffer();
         fullname.append(directory);
         if(!directory.endsWith("/")) {
             fullname.append("/");
         }
         fullname.append(name);
         return fullname.toString();
     }
 
     private String getFileNameFromFullName(String fullName) {
         int idx = StringUtil.lastIndexOf(fullName, '/');
         String fileName;
         if (idx != -1) {
             fileName = fullName.substring(idx+1);
         } else {
             fileName = fullName;
         }
         return fileName;
     }
 
 
 
     protected abstract void fillLocalStorage();
     protected abstract void restoreLocalStorage();
     protected abstract SyncConfig getSyncConfig();
 
     /**
      * Creates a temporary file of specified size
      * 
      * @param byteSize size of the file
      * @param header header of the file
      * @param footer footer of the file
      * 
      * @return name of the file created
      * @throws IOException
      */
     protected abstract void createFileWithSizeOnDevice(long byteSize, String header, String footer)
             throws IOException;
 
 
     /**
      * Creates a file in mediahub directory
      * 
      * @param fileName
      * @param fileSize
      * @throws IOException
      */
     public abstract void createFile(String fileName, long fileSize)
             throws Exception;
 
     /**
      * Renames a file in the server
      *
      * @param oldFileName
      * @param newFileName
      * @throws IOException
      */
     public void renameFileOnServer(String type, String oldFileName, String newFileName)
             throws Throwable {
         
         SapiSyncHandler sapiHandler = getSapiSyncHandler();
 
         String itemId = findMediaIdOnServer(type, oldFileName);
 
         sapiHandler.login(null);
         sapiHandler.updateItemName(getRemoteUri(type), itemId, newFileName);
         sapiHandler.logout();
     }
     
     /**
      * Checks the integrity of a file content on both client and server
      * @param filename
      * @throws Throwable
      */
     public void checkFileContentIntegrity(String type, String fileNameClient,
             String fileNameServer) throws Throwable {
         
         JSONObject localItem = findMediaJSONObject(type, fileNameClient);
         JSONObject serverItem = findMediaJSONObjectOnServer(type, fileNameServer);
 
         assertTrue(localItem != null, "Item not found on client");
         assertTrue(serverItem != null, "Item not found on server");
 
         assertTrue(localItem.getLong("size"), serverItem.getLong("size"),
                 "Item size mismatch");
     }
 
     public void renameFile(String type, String oldFileName, String newFileName) throws Exception {
         SyncSource ss = getAppSyncSource(type).getSyncSource();
         assertTrue(ss instanceof FileSyncSource, "Invalid source type");
         if(ss instanceof FileSyncSource) {
             String dirname = ((FileSyncSource)ss).getDirectory();
             FileAdapter oldFile = new FileAdapter(getFileFullName(dirname, oldFileName));
             assertTrue(oldFile.exists(), "File not found: " + getFileFullName(dirname, oldFileName));
             String newFileFullName = getFileFullName(dirname, newFileName);
             oldFile.rename(newFileFullName);
         }
     }
 
     public void checkThumbnailName(String type, int position, String fileName)
             throws Throwable {
         AppSyncSource appSource = getAppSyncSource(type);
         SourceThumbnailsViewController thumbsController =
                 appSource.getSourceThumbnailsViewController();
         String name = thumbsController.getThumbnailNameAt(position);
         assertTrue(name, fileName, "Item name mismatch");
     }
 
     public void checkDisplayedThumbnailsCount(String type, int count)
             throws Throwable {
         AppSyncSource appSource = getAppSyncSource(type);
         SourceThumbnailsViewController thumbsController =
                 appSource.getSourceThumbnailsViewController();
         int actualCount = thumbsController.getDisplayedThumbnailsCount();
         assertTrue(count, actualCount, "Displayed thumbnails count mismatch");
     }
 
     public void checkThumbnailsCount(String type, int count)
             throws Throwable {
         AppSyncSource appSource = getAppSyncSource(type);
         SourceThumbnailsViewController thumbsController =
                 appSource.getSourceThumbnailsViewController();
         int actualCount = thumbsController.getThumbnailsCount();
         assertTrue(count, actualCount, "Thumbnails count mismatch");
     }
 
     private int getFilesCount(String type) throws Exception {
         SyncSource ss = getAppSyncSource(type).getSyncSource();
         if(ss instanceof FileSyncSource) {
             int count = 0;
             String dirname = ((FileSyncSource)ss).getDirectory();
             FileAdapter dir = new FileAdapter(dirname, true);
             Enumeration files = dir.list(false, false /* Filters hidden files */);
             dir.close();
             while(files.hasMoreElements()) {
                 String file = (String)files.nextElement();
                 if (doesMediaBelongToSource(type, file)) {
                     count++;
                 }
             }
             return count;
         } else {
             throw new IllegalArgumentException("Invalid SyncSource type: " + ss);
         }
     }
 
     private boolean doesMediaBelongToSource(String type, String file) {
         if (BasicUserCommands.SOURCE_NAME_FILES.equals(type)) {
             return true;
         } else if (BasicUserCommands.SOURCE_NAME_PICTURES.equals(type)) {
             // These are all extensions for this type, not only the one we
             // support in upload
             String extensions[] = {"jpg","jpeg","bmp","gif","png","tiff"};
             return isSupportedExtension(extensions, file);
         } else if (BasicUserCommands.SOURCE_NAME_VIDEOS.equals(type)) {
             // These are all extensions for this type, not only the one we
             // support in upload
             String extensions[] = {"3gp","mp4","mpeg","flv","swf","avi"};
             return isSupportedExtension(extensions, file);
         }
         throw new IllegalArgumentException("Unknown source type " + type);
     }
 
     private boolean isSupportedExtension(String extensions[], String name) {
         // If there are no valid extensions defined, then the source does not
         // apply any filter
         if (extensions == null || extensions.length == 0) {
             return true;
         }
         name = name.toLowerCase();
         for(int i=0;i<extensions.length;++i) {
             String ext = extensions[i].toLowerCase();
             if (name.endsWith(ext)) {
                 return true;
             }
         }
         return false;
     }
 
     protected JSONObject findMediaJSONObject(String type, String filename) throws Exception {
         JSONObject result = new JSONObject();
         SyncSource ss = getAppSyncSource(type).getSyncSource();
         assertTrue(ss instanceof FileSyncSource, "Invalid source type");
         if(ss instanceof FileSyncSource) {
             String dirname = ((FileSyncSource)ss).getDirectory();
             FileAdapter file = new FileAdapter(getFileFullName(dirname, filename));
             assertTrue(file.exists(), "File not found: " + getFileFullName(dirname, filename));
             result.put("name", getFileNameFromFullName(file.getName()));
             result.put("modificationdate", file.lastModified());
             result.put("size", file.getSize());
         }
         return result;
     }
 }
