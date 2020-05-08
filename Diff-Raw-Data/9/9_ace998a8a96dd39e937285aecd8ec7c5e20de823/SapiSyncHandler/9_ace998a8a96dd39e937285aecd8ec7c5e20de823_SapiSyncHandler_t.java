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
 import java.util.Date;
 import java.io.IOException;
 
 import org.json.me.JSONException;
 import org.json.me.JSONObject;
 import org.json.me.JSONArray;
 
 import com.funambol.sapisync.sapi.SapiHandler;
 import com.funambol.sapisync.source.JSONFileObject;
 import com.funambol.sapisync.source.JSONSyncItem;
 import com.funambol.sync.ItemUploadInterruptionException;
 import com.funambol.sync.SyncException;
 import com.funambol.sync.SyncItem;
 import com.funambol.sync.SyncListener;
 import com.funambol.util.Log;
 import com.funambol.util.DateUtil;
 import java.io.InputStream;
 import java.util.Hashtable;
 
 
 public class SapiSyncHandler {
 
     private static final String TAG_LOG = "SapiSyncHandler";
 
     private static final int MAX_RETRIES = 3;
 
     private SapiHandler sapiHandler = null;
 
     private static final String JSON_OBJECT_DATA  = "data";
     private static final String JSON_OBJECT_ERROR = "error";
 
     private static final String JSON_OBJECT_DATA_FIELD_JSESSIONID = "jsessionid";
 
     private static final String JSON_OBJECT_ERROR_FIELD_CODE    = "code";
     private static final String JSON_OBJECT_ERROR_FIELD_MESSAGE = "message";
     private static final String JSON_OBJECT_ERROR_FIELD_CAUSE   = "cause";
     
     public static final String JSON_ERROR_CODE_SEC_1002 = "SEC-1002";
     public static final String JSON_ERROR_CODE_SEC_1004 = "SEC-1004";
     public static final String JSON_ERROR_CODE_MED_1002 = "MED-1002";
 
     /**
      * SapiSyncHandler constructor
      * 
      * @param baseUrl the server base url
      * @param user the username to be used for the authentication
      * @param pwd the password to be used for the authentication
      */
     public SapiSyncHandler(String baseUrl, String user, String pwd) {
         this.sapiHandler = new SapiHandler(baseUrl, user, pwd);
     }
 
     /**
      * Login to the current server.
      *
      * @throws SyncException
      */
     public void login() throws SyncException {
         try {
             sapiHandler.setAuthenticationMethod(SapiHandler.AUTH_IN_QUERY_STRING);
             JSONObject res = sapiQueryWithRetries("login", "login", null, null, null);
             JSONObject resData = res.getJSONObject(JSON_OBJECT_DATA);
             if(resData != null) {
                 String jsessionid = resData.getString(JSON_OBJECT_DATA_FIELD_JSESSIONID);
                 sapiHandler.enableJSessionAuthentication(true);
                 sapiHandler.forceJSessionId(jsessionid);
                 sapiHandler.setAuthenticationMethod(SapiHandler.AUTH_NONE);
             } else {
                 handleResponseError(resData);
                 throw new SyncException(SyncException.AUTH_ERROR, "Cannot login");
             }
         } catch(Exception ex) {
             if (Log.isLoggable(Log.ERROR)) {
                 Log.error(TAG_LOG, "Failed to login", ex);
             }
             throw new SyncException(SyncException.AUTH_ERROR, "Cannot login");
         }
     }
 
     /**
      * Logout from the current server.
      *
      * @throws SyncException
      */
     public void logout() throws SyncException {
         try {
             sapiQueryWithRetries("login", "logout", null, null, null);
         } catch(Exception ex) {
             if (Log.isLoggable(Log.ERROR)) {
                 Log.error(TAG_LOG, "Failed to logout", ex);
             }
             throw new SyncException(SyncException.AUTH_ERROR, "Cannot logout");
         }
         sapiHandler.enableJSessionAuthentication(false);
         sapiHandler.forceJSessionId(null);
         sapiHandler.setAuthenticationMethod(SapiHandler.AUTH_NONE);
     }
 
     /**
      * Upload the given item to the server
      * @return the remote item key
      * @param item
      */
     public String uploadItem(SyncItem item, String remoteUri, SyncListener listener) throws SyncException {
         if (Log.isLoggable(Log.INFO)) {
             Log.info(TAG_LOG, "Uploading item: " + item.getKey());
         }
         if(!(item instanceof JSONSyncItem)) {
             throw new UnsupportedOperationException("Not implemented.");
         }
         try {
             JSONObject metadata = new JSONObject();
             JSONFileObject json = ((JSONSyncItem)item).getJSONFileObject();
             metadata.put("name",             json.getName());
             metadata.put("creationdate",     DateUtil.formatDateTimeUTC(json.getCreationDate()));
             metadata.put("modificationdate", DateUtil.formatDateTimeUTC(json.getLastModifiedDate()));
             metadata.put("contenttype",      json.getMimetype());
             metadata.put("size",             json.getSize());
 
             JSONObject addRequest = new JSONObject();
             addRequest.put("data", metadata);
 
             // Send the meta data request
             sapiHandler.setSapiRequestListener(null);
             JSONObject addResponse = sapiQueryWithRetries("upload/" + remoteUri,
                 "add-metadata", null, null, addRequest);
 
             if(!addResponse.has("success")) {
                 Log.error(TAG_LOG, "Failed to upload item");
                 throw new SyncException(SyncException.SERVER_ERROR,
                     "Failed to upload item");
             }
 
             String remoteKey = addResponse.getString("id");
             
             Hashtable headers = new Hashtable();
             headers.put("x-funambol-id", remoteKey);
             headers.put("x-funambol-file-size", Long.toString(json.getSize()));
 
             SapiUploadSyncListener sapiListener = new SapiUploadSyncListener(
                     item, listener);
             sapiHandler.setSapiRequestListener(sapiListener);
 
             // Send the upload request
             JSONObject uploadResponse = sapiQueryWithRetries("upload/" + remoteUri,
                     "add", null, headers, item.getInputStream(),
                     json.getMimetype(), json.getSize());
 
             if(uploadResponse.has("error")) {
                 JSONObject error = uploadResponse.getJSONObject("error");
                 String msg = error.getString("message");
                 String code = error.getString("code");
                 if(JSON_ERROR_CODE_MED_1002.equals(code)) {
                     // The size of the uploading media does not match the one declared
                     // TODO: FIXME retrieve actual uploaded size
                     throw new ItemUploadInterruptionException(item, 0);
                 }
                Log.error(TAG_LOG, "Error in SAPI response: " + msg);
                 throw new SyncException(SyncException.SERVER_ERROR,
                    "Error in SAPI response: " + msg);
             }
 
             sapiHandler.setSapiRequestListener(null);
 
             return remoteKey;
         } catch(Exception ex) {
            if(ex instanceof SyncException) {
                throw (SyncException)ex;
            }
             Log.error(TAG_LOG, "Failed to upload item", ex);
             throw new SyncException(SyncException.CLIENT_ERROR,
                     "Cannot upload item");
         }
     }
 
     public void deleteItem(String key, String remoteUri) throws SyncException {
         if (Log.isLoggable(Log.INFO)) {
             Log.info(TAG_LOG, "Deleting item: " + key);
         }
         try {
             JSONArray pictures = new JSONArray();
             pictures.put(key);
             JSONObject data = new JSONObject();
             data.put("pictures", pictures);
             JSONObject request = new JSONObject();
             request.put("data", data);
             sapiQueryWithRetries("media/" + remoteUri, "delete", null, null, request);
         } catch(Exception ex) {
             Log.error(TAG_LOG, "Failed to delete item: " + key, ex);
             throw new SyncException(SyncException.CLIENT_ERROR,
                     "Cannot delete item");
         }
     }
 
     public void deleteAllItems(String remoteUri) throws SyncException {
         if (Log.isLoggable(Log.INFO)) {
             Log.info(TAG_LOG, "Deleting all items");
         }
         try {
             sapiHandler.query("media/" + remoteUri, "reset", null, null, null);
         } catch(Exception ex) {
             Log.error(TAG_LOG, "Failed to delete all items", ex);
             throw new SyncException(SyncException.CLIENT_ERROR,
                     "Cannot upload item");
         }
     }
 
     public ChangesSet getIncrementalChanges(Date from, String dataType) throws JSONException {
 
         Vector params = new Vector();
         params.addElement("from=" + DateUtil.formatDateTimeUTC(from));
         params.addElement("type=" + dataType);
         params.addElement("responsetime=true");
 
         JSONObject resp = sapiQueryWithRetries("profile/changes", "get",
                 params, null, null);
 
         ChangesSet res = new ChangesSet();
 
         JSONObject data = resp.getJSONObject("data");
         if (data != null) {
             if (data.has(dataType)) {
                 JSONObject items = data.getJSONObject(dataType);
                 if (items != null) {
                     if (items.has("N")) {
                         res.added = items.getJSONArray("N");
                     }
                     if (items.has("U")) {
                         res.updated = items.getJSONArray("U");
                     }
                     if (items.has("D")) {
                         res.deleted = items.getJSONArray("D");
                     }
                 }
             }
             
         }
 
         // Get the timestamp if available
         if (resp.has("responsetime")) {
             String ts = resp.getString("responsetime");
             if (Log.isLoggable(Log.TRACE)) {
                 Log.trace(TAG_LOG, "SAPI returned response time = " + ts);
             }
             try {
                 res.timeStamp = Long.parseLong(ts);
             } catch (Exception e) {
                 Log.error(TAG_LOG, "Cannot parse server responsetime");
                 res.timeStamp = -1;
             }
         }
         return res;
     }
 
     public FullSet getItems(String remoteUri, String dataTag, JSONArray ids,
                               String limit, String offset, Date from) throws JSONException {
 
         Vector params = new Vector();
         if (ids != null) {
             JSONObject request = new JSONObject();
             request.put("ids", ids);
 
             params.addElement("id=" + request.toString());
         }
         if (limit != null) {
             params.addElement("limit=" + limit);
         }
         if (offset != null) {
             params.addElement("offset=" + offset);
         }
         if (from != null) {
             params.addElement("from=" + DateUtil.formatDateTimeUTC(from));
         }
         params.addElement("responsetime=true");
         params.addElement("exif=none");
 
         JSONObject resp = sapiQueryWithRetries("media/" + remoteUri, "get",
                 params, null, null);
 
         if (resp != null) {
             FullSet res = new FullSet();
             JSONObject data = resp.getJSONObject("data");
             if (data != null) {
                 if (data.has(dataTag)) {
                     JSONArray items = data.getJSONArray(dataTag);
                     res.items = items;
                 }
             }
             if (data.has("portalurl")) {
                 res.serverUrl = data.getString("portalurl");
             }
             if (resp.has("responsetime")) {
                 String ts = resp.getString("responsetime");
                 if (Log.isLoggable(Log.TRACE)) {
                     Log.trace(TAG_LOG, "SAPI returned response time = " + ts);
                 }
                 try {
                     res.timeStamp = Long.parseLong(ts);
                 } catch (Exception e) {
                     Log.error(TAG_LOG, "Cannot parse server responsetime");
                     res.timeStamp = -1;
                 }
             }
             return res;
         }
         return null;
     }
 
     public int getItemsCount(String remoteUri, Date from) throws JSONException {
 
         Vector params = new Vector();
         if (from != null) {
             params.addElement("from=" + DateUtil.formatDateTimeUTC(from));
         }
         JSONObject resp = sapiQueryWithRetries("media/" + remoteUri, "count",
                 params, null, null);
         if (resp != null) {
             JSONObject data = resp.getJSONObject("data");
             if (data != null) {
                 if (data.has("count")) {
                     return Integer.parseInt(data.getString("count"));
                 }
             }
         }
         return -1;
     }
 
     /**
      * Cancels the current operation
      */
     public void cancel() {
         if (Log.isLoggable(Log.DEBUG)) {
             Log.debug(TAG_LOG, "Cancelling any current operation");
         }
         if(sapiHandler != null) {
             sapiHandler.cancel();
         }
     }
 
     private void handleResponseError(JSONObject response) throws Exception {
         try {
             // Check for errors
             JSONObject error = response.getJSONObject(JSON_OBJECT_ERROR);
             if(error != null) {
                 String code    = error.getString(JSON_OBJECT_ERROR_FIELD_CODE);
                 String message = error.getString(JSON_OBJECT_ERROR_FIELD_MESSAGE);
                 String cause   = error.getString(JSON_OBJECT_ERROR_FIELD_CAUSE);
 
                 StringBuffer logMsg = new StringBuffer(
                         "Error in SAPI response").append("\r\n");
                 logMsg.append("code: ").append(code).append("\r\n");
                 logMsg.append("cause: ").append(cause).append("\r\n");
                 logMsg.append("message: ").append(message).append("\r\n");
 
                 // Handle error codes
                 if(JSON_ERROR_CODE_SEC_1002.equals(code)) {
                     // A session is already open. To provide new credentials 
                     // please logout first.
                 } else if(JSON_ERROR_CODE_SEC_1004.equals(code)) {
                     // Both header and parameter credentials provided, please
                     // use only one authentication schema.
                 } 
             }
         } catch(JSONException ex) {
             if (Log.isLoggable(Log.DEBUG)) {
                 Log.debug(TAG_LOG, "Failed to retrieve error json object");
             }
         }
     }
 
     /**
      * Send a SAPI query with a retry mechanism.
      * @param name
      * @param action
      * @param params
      * @param headers
      * @param request
      * @return
      * @throws JSONException
      */
     private JSONObject sapiQueryWithRetries(String name, String action, Vector params,
             Hashtable headers, JSONObject request) throws JSONException {
         JSONObject resp = null;
         boolean retry = true;
         int attempt = 0;
         do {
             try {
                 attempt++;
                 resp = sapiHandler.query(name, action, params, headers, request);
                 retry = false;
             } catch (IOException ioe) {
                 if (attempt >= MAX_RETRIES) {
                     retry = false;
                 }
             }
         } while(retry);
         return resp;
     }
     
     private JSONObject sapiQueryWithRetries(String name, String action, 
             Vector params, Hashtable headers, InputStream requestIs,
             String contentType, long contentLength) throws JSONException {
         JSONObject resp = null;
         boolean retry = true;
         int attempt = 0;
         do {
             try {
                 attempt++;
                 resp = sapiHandler.query(name, action, params, headers,
                         requestIs, contentType, contentLength);
                 retry = false;
             } catch (IOException ioe) {
                 if (attempt >= MAX_RETRIES) {
                     retry = false;
                 }
             }
         } while(retry);
         return resp;
     }
 
     public class ChangesSet {
         public JSONArray added     = null;
         public JSONArray updated   = null;
         public JSONArray deleted   = null;
         public long      timeStamp = -1;
     }
 
     public class FullSet {
         public JSONArray items     = null;
         public long      timeStamp = -1;
         public String    serverUrl = null;
     }
 
     /**
      * Translates the SapiQueryListener calls into SyncListener calls.
      */
     private class SapiUploadSyncListener implements SapiHandler.SapiQueryListener {
 
         private SyncListener syncListener = null;
         private String itemKey = null;
 
         public SapiUploadSyncListener(SyncItem item, SyncListener syncListener) {
             this.syncListener = syncListener;
             this.itemKey = item.getKey();
         }
 
         public void queryStarted(int totalSize) {
             if(syncListener != null) {
                 syncListener.itemAddSendingStarted(itemKey, null, totalSize);
             }
         }
 
         public void queryProgress(int size) {
             if(syncListener != null) {
                 syncListener.itemAddSendingProgress(itemKey, null, size);
             }
         }
 
         public void queryEnded() {
             if(syncListener != null) {
                 syncListener.itemAddSendingEnded(itemKey, null);
             }
         }
     }
 }
