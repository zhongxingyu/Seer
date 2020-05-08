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
 
 package com.funambol.client.engine;
 
 import java.util.Hashtable;
 import java.util.Vector;
 import java.io.OutputStream;
 import java.io.InputStream;
 import java.io.IOException;
 
 import com.funambol.org.json.me.JSONException;
 import com.funambol.org.json.me.JSONObject;
 import com.funambol.client.configuration.Configuration;
 import com.funambol.sapisync.sapi.SapiHandler;
 import com.funambol.sapisync.NotAuthorizedCallException;
 import com.funambol.sapisync.NotSupportedCallException;
 import com.funambol.concurrent.Task;
 import com.funambol.storage.Tuple;
 import com.funambol.storage.Table;
 import com.funambol.storage.QueryFilter;
 import com.funambol.storage.QueryResult;
 import com.funambol.client.source.MediaMetadata;
 import com.funambol.platform.FileAdapter;
 import com.funambol.util.Log;
 import com.funambol.util.StringUtil;
 
 
 public class ItemUploadTask implements Task {
 
     private static final String TAG_LOG = "ItemUploadTask";
 
     protected String url;
     protected String fileName;
     protected Long id;
     protected String thumbSize;
     protected int initialDelay;
     protected Table metadata;
     protected Configuration configuration;
 
     public ItemUploadTask(Long id, int initialDelay, Table metadata,
                           Configuration configuration) {
         this.id = id;
         this.initialDelay = initialDelay;
         this.metadata = metadata;
         this.configuration = configuration;
     }
 
     public void run() {
         boolean done = false;
         int delay = 1;
 
         // We must retrieve all the necessary information from the metadata
         // table
         long size;
         String guid, mimeType, name, fileName, remoteUri;
         QueryResult res = null;
         QueryFilter qf = null;
         boolean hasThumbnails = false;
         try {
             metadata.open();
             qf = metadata.createQueryFilter(id);
             res = metadata.query(qf);
             if (res.hasMoreElements()) {
                 Tuple row = res.nextElement();
                 Long sizeL = row.getLongField(metadata.getColIndexOrThrow(MediaMetadata.METADATA_SIZE));
                 size = sizeL.longValue();
                 guid = row.getStringField(metadata.getColIndexOrThrow(MediaMetadata.METADATA_GUID));
                 mimeType = row.getStringField(metadata.getColIndexOrThrow(MediaMetadata.METADATA_MIME));
                 name = row.getStringField(metadata.getColIndexOrThrow(MediaMetadata.METADATA_NAME));
                 fileName = row.getStringField(metadata.getColIndexOrThrow(MediaMetadata.METADATA_ITEM_PATH));
                 remoteUri = row.getStringField(metadata.getColIndexOrThrow(MediaMetadata.METADATA_REMOTE_URI));
 
                 // Check if this item has thumbnails
                String smallThumb = row.getStringField(metadata.getColIndexOrThrow(MediaMetadata.METADATA_THUMBNAIL_PATH));
                if (smallThumb != null && smallThumb.length() > 0) {
                    hasThumbnails = true;
                 }
             } else {
                 Log.error(TAG_LOG, "Cannot find item to upload " + id);
                 generateFailureEvent(id);
                 return;
             }
         } catch (Exception e) {
             Log.error(TAG_LOG, "Cannot query metdata table", e);
             return;
         } finally {
             if (res != null) {
                 res.close();
             }
             try {
                 metadata.close();
             } catch (IOException ioe) {}
         }
 
         // Now that we have all the info, we can start the upload
         do {
             // We refresh at every attempt so that if the user changes the
             // configuration we pick the updated one
             SapiHandler sapiHandler = new SapiHandler(configuration.getSyncUrl(), configuration.getUsername(),
                     configuration.getPassword());
 
             try {
                 // First of all we check if the item is partially available on
                 // the server
                 long partialSize = sapiHandler.getMediaPartialUploadLength(name, guid, size);
                 if (partialSize == 0) {
                     uploadItem(fileName, guid, size, name, mimeType, remoteUri, sapiHandler);
                 } else if (partialSize < size) {
                     resumeItemUpload(fileName, guid, size, name, mimeType, partialSize, remoteUri, sapiHandler);
                 }
                 done = true;
             } catch (UploadException ue) {
                 Log.error(TAG_LOG, "Cannot upload item", ue);
                 // We try again, after some time
                 try {
                     Thread.sleep(delay * 1000);
                 } catch (Exception e) {}
                 delay *= 2;
             } catch (Exception e) {
                 Log.error(TAG_LOG, "Cannot upload item, giving up", e);
                 done = true;
                 generateFailureEvent(id);
                 return;
             }
         } while (!done);
 
         // Since the upload completed successfully we can mark the item as
         // completely uploaded
         try {
             metadata.open();
             res = metadata.query(qf);
             if (res.hasMoreElements()) {
                 Tuple row = res.nextElement();
                 row.setField(metadata.getColIndexOrThrow(MediaMetadata.METADATA_UPLOAD_CONTENT_STATUS), 2L);
                 metadata.update(row);
                 metadata.save();
             } else {
                 Log.error(TAG_LOG, "Internal error, cannot find item in table " + id);
             }
             res.close();
         } catch (IOException ioe) {
             Log.error(TAG_LOG, "Cannot update item metadata", ioe);
         } finally {
             try {
                 metadata.close();
             } catch (IOException ioe) {}
         }
  
 
         generateSuccessEvent(id);
     }
 
     private String resumeItemUpload(String fileName, String guid, long size, String name, String mimeType,
                                     long partialSize, String remoteUri,
                                     SapiHandler sapiHandler)
     throws UploadException, IOException {
         if (Log.isLoggable(Log.INFO)) {
             Log.info(TAG_LOG, "Resuming upload for item: " + name);
         }
 
         // First of all we need to query the server to understand where we shall
         // restart from. The item must have a valid guid, otherwise we cannot
         // resume
         if (guid == null) {
             Log.error(TAG_LOG, "Cannot resume, a complete upload will be performed instead");
         }
 
         if (partialSize > 0) {
             if(size == partialSize) {
                 if (Log.isLoggable(Log.INFO)) {
                     Log.info(TAG_LOG, "No need to resume item " + size);
                 }
                 return null;
             } else {
                 long fromByte = partialSize + 1;
                 if (Log.isLoggable(Log.INFO)) {
                     Log.info(TAG_LOG, "Upload can be resumed at byte " + fromByte);
                 }
                 return uploadItem(fileName, guid, size, name, mimeType, remoteUri, fromByte, sapiHandler);
             }
         } else {
             if (Log.isLoggable(Log.INFO)) {
                 Log.info(TAG_LOG, "Upload cannot be resumed, perform a complete upload");
             }
             return uploadItem(fileName, guid, size, name, mimeType, remoteUri, sapiHandler);
         }
     }
 
     public String uploadItem(String fileName, String guid, long size, String name, String mimeType,
                              String remoteUri, SapiHandler sapiHandler)
     throws UploadException, IOException {
         return uploadItem(fileName, guid, size, name, mimeType, remoteUri, 0, sapiHandler);
     }
 
     private String uploadItem(String fileName, String guid, long size, String name, String mimeType,
                               String remoteUri, long fromByte,
                               SapiHandler sapiHandler)
     throws UploadException, NotAuthorizedCallException, IOException
 
     {
         if (Log.isLoggable(Log.INFO)) {
             Log.info(TAG_LOG, "Uploading item: " + name);
         }
 
         InputStream is = null;
         Hashtable headers = new Hashtable();
         String remoteKey = guid;
 
         try {
             try {
 
                 // If this is not a resume, we must perform the upload in two
                 // phases. Send the metadata first and then the actual content
                 FileAdapter file = new FileAdapter(fileName);
                 is = file.openInputStream();
                 if(is == null) {
                     if(Log.isLoggable(Log.DEBUG)) {
                         Log.debug(TAG_LOG, "Upload is not needed, item content is null");
                     }
                     return "";
                 }
 
                 if (fromByte == 0) {
                     if (Log.isLoggable(Log.DEBUG)) {
                         Log.debug(TAG_LOG, "Uploading a new item with guid " + remoteKey);
                     }
                 } else {
                     if (Log.isLoggable(Log.DEBUG)) {
                         Log.debug(TAG_LOG, "Resuming an item with guid " + remoteKey);
                     }
                     StringBuffer contentRangeValue = new StringBuffer();
                     contentRangeValue.append("bytes ").append(fromByte)
                         .append("-").append(size-1)
                         .append("/").append(size);
                     headers.put("Content-Range", contentRangeValue.toString());
                 }
 
                 headers.put("x-funambol-id", remoteKey);
                 headers.put("x-funambol-file-size", Long.toString(size));
 
                 SapiUploadSyncListener sapiListener = new SapiUploadSyncListener();
                 sapiHandler.setSapiRequestListener(sapiListener);
             } catch (NotSupportedCallException e) {
                 Log.error(TAG_LOG, "Server doesn't support the SAPI call", e);
                 throw new UploadException("Unsupported SAPI call");
             }
 
 
             // Send the upload request
             JSONObject uploadResponse = null;
             try {
                 Vector params = new Vector();
                 params.addElement("lastupdate=true");
 
                 uploadResponse = sapiHandler.query("upload/" + remoteUri,
                         "save", params, headers, is, mimeType,
                         size, fromByte, name);
 
                 //original code: process the exception if error is present in the object
                 if (uploadResponse == null || uploadResponse.has("error")) {
                     throw new UploadException("SAPI error while uploading item");
                 }
 
                 sapiHandler.setSapiRequestListener(null);
                 return uploadResponse.getString("lastupdate");
             } catch (NotSupportedCallException e) {
                 Log.error(TAG_LOG, "Server doesn't support the SAPI call", e);
                 throw new UploadException("Unsupported SAPI call");
             } catch (NotAuthorizedCallException nae) {
                 Log.error(TAG_LOG, "Server authentication failure, try to login again", nae);
                 throw nae;
             } catch(JSONException ex) {
                 Log.error(TAG_LOG, "JSON error", ex);
                 throw new UploadException("JSON error");
             }
         } finally {
             if (is != null) {
                 try {
                     is.close();
                 } catch (IOException ioe) {}
             }
         }
     }
 
 
     private void generateFailureEvent(Long id) {
         // TODO FIXME: generate an event to indicate this download has failed
     }
 
     private void generateSuccessEvent(Long id) {
         // TODO FIXME: generate an event to indicate this download has succeeded
     }
 
     private class SapiUploadSyncListener implements SapiHandler.SapiQueryListener {
 
         public SapiUploadSyncListener() {
         }
 
         public void queryStarted(int totalSize) {      
         }
 
         public void queryProgress(int size) {
         }
 
         public void queryEnded() {
         }
     }
 
     private class UploadException extends Exception {
 
         public UploadException(String msg) {
             super(msg);
         }
     }
 }
 
 
