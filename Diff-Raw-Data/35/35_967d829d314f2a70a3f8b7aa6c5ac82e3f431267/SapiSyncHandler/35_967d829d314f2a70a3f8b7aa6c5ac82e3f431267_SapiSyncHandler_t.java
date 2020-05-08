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
 import com.funambol.sync.QuotaOverflowException;
 import com.funambol.sync.SyncException;
 import com.funambol.sync.SyncItem;
 import com.funambol.sync.SyncListener;
 import com.funambol.util.Log;
 import com.funambol.util.DateUtil;
 import com.funambol.util.StringUtil;
 
 import java.io.InputStream;
 import java.util.Hashtable;
 
 
 public class SapiSyncHandler {
 
     private static final String TAG_LOG = "SapiSyncHandler";
 
     private static final int MAX_RETRIES = 3;
 
     private SapiHandler sapiHandler = null;
 
     private static final String JSON_OBJECT_DATA  = "data";
     private static final String JSON_OBJECT_ERROR = "error";
     private static final String JSON_OBJECT_SUCCESS = "success";
 
     private static final String JSON_OBJECT_DATA_FIELD_JSESSIONID = "jsessionid";
 
     private static final String JSON_OBJECT_ERROR_FIELD_CODE    = "code";
     private static final String JSON_OBJECT_ERROR_FIELD_MESSAGE = "message";
     private static final String JSON_OBJECT_ERROR_FIELD_CAUSE   = "cause";
     
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
     public void login(String deviceId) throws SyncException {
         login(deviceId, 0);
     }
 
 
     /**
      * Logout from the current server.
      *
      * @throws SyncException
      */
     public void logout() throws SyncException {
         try {
             sapiQueryWithRetries("login", "logout", null, null, null);
         } catch (IOException ioe) {
             Log.error(TAG_LOG, "Failed to logout", ioe);
             throw new SyncException(SyncException.CONN_NOT_FOUND, "Cannot logout");
         } catch(Exception ex) {
             Log.error(TAG_LOG, "Failed to logout", ex);
             throw new SyncException(SyncException.AUTH_ERROR, "Cannot logout");
         }
         sapiHandler.enableJSessionAuthentication(false);
         sapiHandler.forceJSessionId(null);
         sapiHandler.setAuthenticationMethod(SapiHandler.AUTH_NONE);
     }
 
     public String resumeItemUpload(SyncItem item, String remoteUri, SyncListener listener) throws SyncException {
         if (Log.isLoggable(Log.INFO)) {
             Log.info(TAG_LOG, "Resuming upload for item: " + item.getKey());
         }
         if(!(item instanceof JSONSyncItem)) {
             throw new UnsupportedOperationException("Not implemented.");
         }
         try {
             JSONFileObject json = ((JSONSyncItem)item).getJSONFileObject();
 
             // First of all we need to query the server to understand where we shall
             // restart from. The item must have a valid guid, otherwise we cannot
             // resume
             String guid = item.getGuid();
 
             if (guid == null) {
                 Log.error(TAG_LOG, "Cannot resume, a complete upload will be performed instead");
                 return uploadItem(item, remoteUri, listener);
             }
 
             Hashtable headers = new Hashtable();
             headers.put("Content-Range","bytes */" + json.getSize());
 
             long length = sapiHandler.getMediaPartialUploadLength(remoteUri, guid, json.getSize());
 
             if (length > 0) {
                 if(length == json.getSize()) {
                     if (Log.isLoggable(Log.INFO)) {
                         Log.info(TAG_LOG, "No need to resume item");
                     }
                     return guid;
                 } else {
                     long fromByte = length + 1;
                     if (Log.isLoggable(Log.INFO)) {
                         Log.info(TAG_LOG, "Upload can be resumed at byte " + fromByte);
                     }
                     return uploadItem(item, remoteUri, listener, fromByte);
                 }
             } else {
                 if (Log.isLoggable(Log.INFO)) {
                     Log.info(TAG_LOG, "Upload cannot be resumed, perform a complete upload");
                     return uploadItem(item, remoteUri, listener);
                 }
             }
             return guid;
         } catch(Exception ex) {
             if(ex instanceof SyncException) {
                 throw (SyncException)ex;
             }
             Log.error(TAG_LOG, "Failed to upload item", ex);
             throw new SyncException(SyncException.CLIENT_ERROR, "Cannot upload item");
         }
     }
 
     public String uploadItem(SyncItem item, String remoteUri, SyncListener listener) throws SyncException {
         return uploadItem(item, remoteUri, listener, 0);
     }
 
     /**
      * Upload the given item to the server (and possibly resume it)
      * @return the remote item key
      * @param item
      */
     public String uploadItem(SyncItem item, String remoteUri, SyncListener listener, long fromByte)
     throws SyncException
     {
         if (Log.isLoggable(Log.INFO)) {
             Log.info(TAG_LOG, "Uploading item: " + item.getKey());
         }
         if(!(item instanceof JSONSyncItem)) {
             throw new UnsupportedOperationException("Not implemented.");
         }
         try {
             // If this is not a resume, we must perform the upload in two
             // phases. Send the metadata first and then the actual content
             String remoteKey;
             Hashtable headers = new Hashtable();
             InputStream is = item.getInputStream();
 
             JSONObject metadata = new JSONObject();
             JSONFileObject json = ((JSONSyncItem)item).getJSONFileObject();
 
             if (fromByte == 0) {
                 metadata.put("name", json.getName());
                 metadata.put("creationdate", DateUtil.formatDateTimeUTC(json.getCreationDate()));
                 metadata.put("modificationdate", DateUtil.formatDateTimeUTC(json.getLastModifiedDate()));
                 metadata.put("contenttype", json.getMimetype());
                 metadata.put("size", json.getSize());
 
                 JSONObject addRequest = new JSONObject();
                 addRequest.put("data", metadata);
 
                 if (Log.isLoggable(Log.TRACE)) {
                     Log.trace(TAG_LOG, "metadata " + addRequest.toString());
                 }
 
                 // Send the meta data request
                 sapiHandler.setSapiRequestListener(null);
                 JSONObject addResponse = sapiQueryWithRetries("upload/" + remoteUri,
                         "add-metadata", null, null, addRequest);
                 
                 //original code: throws exception if sucess is not present
                 if (SapiResultError.hasError(addResponse)) {
                     verifyErrorInSapiUploadResponse(addResponse, item, null);
                 }
                 
                 remoteKey = addResponse.getString("id");
             } else {
                 remoteKey = item.getGuid();
                 StringBuffer contentRangeValue = new StringBuffer();
                 contentRangeValue.append("bytes ").append(fromByte)
                         .append("-").append(json.getSize()-1)
                         .append("/").append(json.getSize());
                 headers.put("Content-Range", contentRangeValue.toString());
             }
             
             headers.put("x-funambol-id", remoteKey);
             headers.put("x-funambol-file-size", Long.toString(json.getSize()));
 
             SapiUploadSyncListener sapiListener = new SapiUploadSyncListener(
                     item, listener);
             sapiHandler.setSapiRequestListener(sapiListener);
 
             // Send the upload request
             JSONObject uploadResponse = null;
             try {
                 uploadResponse = sapiHandler.query("upload/" + remoteUri,
                        "add", null, headers, is, json.getMimetype(),
                        json.getSize(), fromByte, json.getName());
             } catch (IOException ioe) {
                 // The upload failed and got interrupted. We report this error
                 // so that a resume is possible
                 item.setGuid(remoteKey);
                 throw new ItemUploadInterruptionException(item, 0);
             }
 
             //original code: process the exception if error is present in the object
             if (SapiResultError.hasError(uploadResponse)) {
                 verifyErrorInSapiUploadResponse(uploadResponse, item, remoteKey);
 
                 /*
                 JSONObject error = uploadResponse.getJSONObject(JSON_OBJECT_ERROR);
                 String msg = error.getString("message");
                 String code = error.getString("code");
                 if(SapiException.MED_1002.equals(code)) {
                     // The size of the uploading media does not match the one declared
                     item.setGuid(remoteKey);
                     throw new ItemUploadInterruptionException(item, 0);
                 } else if(SapiException.MED_1007.equals(code)) {
                     //server user quota exceeded 
                     throw new QuotaOverflowException(item);
                 }
                 Log.error(TAG_LOG, "Error in SAPI response: " + msg);
                 throw new SyncException(SyncException.SERVER_ERROR,
                     "Error in SAPI response: " + msg);
                 */
             }
 
             sapiHandler.setSapiRequestListener(null);
 
             return remoteKey;
         } catch(JSONException ex) {
             Log.error(TAG_LOG, "Failed to upload item", ex);
             throw new SyncException(SyncException.CLIENT_ERROR,
                     "Cannot upload item");
         } catch(IOException ex) {
             Log.error(TAG_LOG, "Failed to upload item", ex);
             throw new SyncException(SyncException.CLIENT_ERROR,
                     "Cannot upload item");
         }
     }
 
     /**
      * Common code used to verify specific error in upload sapi
      * (size mismatch, over quota etc)
      * this method should be optimized, removing code duplication because
      * it's very similar to {@link #logErrorResponseAndThrowSapiException(JSONObject, boolean)}
      * 
      * @param sapiResponse, cannot be null
      * @throws SyncException
      */
     private void verifyErrorInSapiUploadResponse(JSONObject sapiResponse, SyncItem item, String remoteKey) 
     throws SyncException
     {
         
         //check for standard error code
         SapiResultError resultError = checkForCommonSapiErrorCodeAndThrowSyncException(
                 sapiResponse, "Failed to upload item");
         
         //no exception was thrown, so a sapi-specific error code is returned
         if(SapiException.MED_1002.equals(resultError.code)) {
             // The size of the uploading media does not match the one declared
             item.setGuid(remoteKey);
             throw new ItemUploadInterruptionException(item, 0);
         } else if(SapiException.MED_1007.equals(resultError.code)) {
             //server user quota exceeded 
             throw new QuotaOverflowException(item);
         }
         throw new SyncException(SyncException.SERVER_ERROR,
             "Error in SAPI response: " + resultError.message);
     }
 
     public void deleteItem(String key, String remoteUri, String dataTag) throws SyncException {
         if (Log.isLoggable(Log.INFO)) {
             Log.info(TAG_LOG, "Deleting item: " + key);
         }
         try {
             JSONArray pictures = new JSONArray();
             int id;
             try {
                 id = Integer.parseInt(key);
             } catch (Exception e) {
                 Log.error(TAG_LOG, "Invalid key while deleting item", e);
                 throw new SyncException(SyncException.CLIENT_ERROR, "Cannot delete item");
             }
             pictures.put(id);
             pictures.put(key);
             JSONObject data = new JSONObject();
             data.put(dataTag, pictures);
             JSONObject request = new JSONObject();
             request.put("data", data);
             sapiQueryWithRetries("media/" + remoteUri, "delete", null, null, request);
             //TODO check for errors
         } catch (IOException ioe) {
             Log.error(TAG_LOG, "Failed to delete item: " + key, ioe);
             throw new SyncException(SyncException.CONN_NOT_FOUND, "IOError while deleting");
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
             //TODO check for errors
         } catch (IOException ioe) {
             Log.error(TAG_LOG, "Failed to delete all items", ioe);
             throw new SyncException(SyncException.CONN_NOT_FOUND, "IOError while deleting");
         } catch(Exception ex) {
             Log.error(TAG_LOG, "Failed to delete all items", ex);
             throw new SyncException(SyncException.CLIENT_ERROR,
                     "Cannot upload item");
         }
     }
 
     public ChangesSet getIncrementalChanges(Date from, String dataType) throws SapiException {
 
         Vector params = new Vector();
         params.addElement("from=" + from.getTime());
         params.addElement("type=" + dataType);
         params.addElement("responsetime=true");
 
         JSONObject response = null;
         try {
             response = sapiQueryWithRetries(
                     "profile/changes",
                     "get",
                     params,
                     null,
                     null);
 
         } catch (IOException ioe) {
             //TODO: why syncException?
             throw new SyncException(SyncException.CONN_NOT_FOUND, "IOError while getting incremental changes");
         } catch (JSONException e) {
             //TODO: why syncException?
             throw new SyncException(SyncException.CLIENT_ERROR, "Client error while getting incremental changes");
         }
 
         if (SapiResultError.hasError(response)) {
             checkForCommonSapiErrorCodeAndThrowSapiException(response, "Error in incremental changes sapi call");
         }
         
         try {
             ChangesSet res = new ChangesSet();
             JSONObject data = getDataFromResponse(response);
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
 
             // Get the timestamp if available
             if (response.has("responsetime")) {
                 String ts = response.getString("responsetime");
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
 
         } catch (JSONException e) {
             throw SapiException.SAPI_EXCEPTION_UNKNOWN;
         }
     }
 
     public FullSet getItems(String remoteUri, String dataTag, JSONArray ids,
                               String limit, String offset, Date from) throws SapiException {
 
         try {
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
                 params.addElement("from=" + from.getTime());
             }
             params.addElement("responsetime=true");
             params.addElement("exif=none");
     
             JSONObject resp = sapiQueryWithRetries("media/" + remoteUri, "get",
                     params, null, null);
     
             FullSet res = new FullSet();
 
             if (SapiResultError.hasError(resp)) {
                 checkForCommonSapiErrorCodeAndThrowSapiException(resp, "Error in get items sapi call");
             }
 
             JSONObject data = getDataFromResponse(resp);
             if (data.has(dataTag)) {
                 JSONArray items = data.getJSONArray(dataTag);
                 res.items = items;
             }
             if (data.has("mediaserverurl")) {
                 res.serverUrl = data.getString("mediaserverurl");
             }
             
             //TODO responsetime outside the success or error check?
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
 
         } catch (IOException ioe) {
             throw new SyncException(SyncException.CONN_NOT_FOUND, "IOError while getting item");
         } catch (JSONException e) {
             throw SapiException.SAPI_EXCEPTION_UNKNOWN;
         }
     }
 
     public int getItemsCount(String remoteUri, Date from) throws SapiException {
         Vector params = new Vector();
         if (from != null) {
             params.addElement("from=" + from.getTime());
         }
         
         try {
             JSONObject response = sapiQueryWithRetries(
                     "media/" + remoteUri,
                     "count",
                     params,
                     null,
                     null);
             
             if (SapiResultError.hasError(response)) {
                 checkForCommonSapiErrorCodeAndThrowSapiException(response, "Error in get items sapi call");
             }
             JSONObject data = getDataFromResponse(response);
             if (data.has("count")) {
                 return Integer.parseInt(data.getString("count"));
             }
             
         } catch (IOException ioe) {
             throw new SyncException(SyncException.CONN_NOT_FOUND, "IOException getting items count");
         } catch (JSONException e) {
             throw SapiException.SAPI_EXCEPTION_UNKNOWN;
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
     
     
     /**
      * 
      * @param source
      * @return
      */
     public long getUserAvailableServerQuota(String remoteUri) throws SapiException {
         JSONObject response;
         try {
             response = sapiQueryWithRetries(
                     "media/" + remoteUri,
                     "get-storage-space",
                     null, null, null);
             
             if (SapiResultError.hasError(response)) {
                 checkForCommonSapiErrorCodeAndThrowSapiException(response, "Error in get items sapi call");
             }
             JSONObject data = getDataFromResponse(response);
             if (data.has("free")) {
                 return Long.parseLong(data.getString("free"));
             }
             return -1;
             
         } catch (IOException ioe) {
             //TODO verify if the error code is correct
             throw new SapiException(SapiException.HTTP_400, "IOError while getting server quota");
         } catch (JSONException e) {
             throw SapiException.SAPI_EXCEPTION_UNKNOWN;
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
                                             Hashtable headers, JSONObject request)
     throws JSONException, IOException
     {
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
                     throw ioe;
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
 
     private void login(String deviceId, int attempt) throws SyncException {
         try {
             sapiHandler.setAuthenticationMethod(SapiHandler.AUTH_IN_QUERY_STRING);
 
             Vector params = null;
             if (deviceId != null) {
                 params = new Vector();
                 params.addElement("syncdeviceid=" + deviceId);
             }
 
             JSONObject res = sapiQueryWithRetries("login", "login", params, null, null);
 
             if (res.has(JSON_OBJECT_ERROR)) {
 
                 if (Log.isLoggable(Log.INFO)) {
                     Log.info(TAG_LOG, "login returned an error " + res.toString());
                 }
 
                 // We have an error, check the code
                 JSONObject resError = res.getJSONObject(JSON_OBJECT_ERROR);
                 String code = resError.getString(JSON_OBJECT_ERROR_FIELD_CODE);
                 
                 if (Log.isLoggable(Log.INFO)) {
                     Log.info(TAG_LOG, "login error code " + code);
                 }
 
                 if (attempt == 0 && SapiException.SEC_1002.equals(code)) {
                     // We already logged in.We need to logout first
                     if (Log.isLoggable(Log.INFO)) {
                         Log.info(TAG_LOG, "logging out");
                     }
                     logout();
                     // login again
                     if (Log.isLoggable(Log.INFO)) {
                         Log.info(TAG_LOG, "logging in");
                     }
                     //TODO check, shouldn't be ++attempt?
                     login(deviceId, attempt++);
                     return;
                 } else {
                     // Login failed
                     throw new SyncException(SyncException.AUTH_ERROR, "Cannot login");
                 }
             }
 
             //original code: find data part, if error or null, throw exception
             if (SapiResultError.hasError(res)) {
                 checkForCommonSapiErrorCodeAndThrowSyncException(res, "Error in login sapi call");
             }
             JSONObject resData = res.getJSONObject(JSON_OBJECT_DATA);
             if(resData != null) {
                 String jsessionid = resData.getString(JSON_OBJECT_DATA_FIELD_JSESSIONID);
                 sapiHandler.enableJSessionAuthentication(true);
                 sapiHandler.forceJSessionId(jsessionid);
                 sapiHandler.setAuthenticationMethod(SapiHandler.AUTH_NONE);
             } else {
                 throw new SyncException(SyncException.AUTH_ERROR, "Cannot login");
             }
 
         } catch(IOException ex) {
             if (Log.isLoggable(Log.ERROR)) {
                 Log.error(TAG_LOG, "Failed to login", ex);
             }
             throw new SyncException(SyncException.AUTH_ERROR, "Cannot login");
         } catch(JSONException ex) {
             if (Log.isLoggable(Log.ERROR)) {
                 Log.error(TAG_LOG, "Failed to login", ex);
             }
             throw new SyncException(SyncException.AUTH_ERROR, "Cannot login");
         } catch(SyncException ex) {
             throw new SyncException(SyncException.AUTH_ERROR, "Cannot login");
         }
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
 
     /**
      * Extracts data part from a SAPI response. If data is no present, a
      * {@link SapiException} is thrown
      * @param response
      * @return
      * @throws SapiException
      */
     private JSONObject getDataFromResponse(JSONObject response)
     throws SapiException {
         try {
             JSONObject data = response.getJSONObject(JSON_OBJECT_DATA);
             return data;
         } catch (JSONException e) {
             Log.debug(TAG_LOG, "Sapi response doesn't contain data object");
             throw SapiException.SAPI_EXCEPTION_UNKNOWN;
         }
     }
 
     /**
      * 
      * @param sapiResponse
      * @param fallbackMessage
      * @throws SyncException
      */
     private SapiResultError checkForCommonSapiErrorCodeAndThrowSyncException(JSONObject sapiResponse, String fallbackMessage)
     throws SyncException {
 
         if (null == sapiResponse) {
             Log.error(TAG_LOG, "Null response from sapi call");
             throw new SyncException(
                     SyncException.SERVER_ERROR,
                     fallbackMessage);
         }
         
         SapiResultError resultError = SapiResultError.extractFromSapiResponse(sapiResponse);
         
         if (StringUtil.isNullOrEmpty(resultError.code)) {
             Log.error(TAG_LOG, "Invalid return code from sapi call");
             throw new SyncException(
                     SyncException.SERVER_ERROR,
                     fallbackMessage);
         }
 
         // TODO actually, no common error code check is performed
         
         return resultError;
     }
 
     /**
      * Handles error response from SAPI, logging the error and creating the
      * proper {@link SapiException} object to throw
      * 
      * @param sapiResponse error response to analyze
      * @param fallbackMessage
      * @throws SapiException
      */
     private SapiResultError checkForCommonSapiErrorCodeAndThrowSapiException(JSONObject sapiResponse, String fallbackMessage)
     throws SapiException {
         if (null == sapiResponse) {
             Log.error(TAG_LOG, "Null response from sapi call");
             //FIXME: fix the error message, maybe it's not correct
             throw SapiException.SAPI_EXCEPTION_UNKNOWN;
         }
 
         SapiResultError resultError = SapiResultError.extractFromSapiResponse(sapiResponse);
 
         if (StringUtil.isNullOrEmpty(resultError.code)) {
             Log.error(TAG_LOG, "Invalid return code from sapi call");
             //FIXME: fix the error message, maybe it's not correct
             throw SapiException.SAPI_EXCEPTION_UNKNOWN;
         }
         
         // TODO actually, no common error code check is performed,
         // so an exception is automatically thrown. In the future,
         // only known error codes will thrown an exception
         throw new SapiException(
                 resultError.code,
                 resultError.message,
                 resultError.cause);
         
         //return resultError;
     }
 
     /**
      * Handles SAPI result in case of error
      */
     static class SapiResultError {
         public String code;
         public String message;
         public String cause;
         
         public SapiResultError() {}
         
         public SapiResultError(String code, String message, String cause) {
             this.code = code;
             this.message = message;
             this.cause = cause;
         }
         
         public static boolean hasError(JSONObject sapiResponse) {
             if (null == sapiResponse) return true;
             return sapiResponse.has(JSON_OBJECT_ERROR);
             //TODO add the check for "success" string inside the object?
         }
         
         public static SapiResultError extractFromSapiResponse(JSONObject sapiResponse) {
             if (null == sapiResponse) {
                 throw new IllegalArgumentException("SAPI response cannot be null");
             }
             
             //before, if json object doens't have all tree field, an error is trown
             SapiResultError sapiResultError = new SapiResultError();
             if (sapiResponse.has(JSON_OBJECT_ERROR)) {
                 JSONObject error = null;
                 try {
                     error = sapiResponse.getJSONObject(JSON_OBJECT_ERROR);
                 } catch (JSONException e) {
                     //cannot happens
                 }
                 try {
                     sapiResultError.code = error.getString(JSON_OBJECT_ERROR_FIELD_CODE);
                 } catch (JSONException e) {}
                 try {
                     sapiResultError.message = error.getString(JSON_OBJECT_ERROR_FIELD_MESSAGE);
                 } catch (JSONException e) {}
                 try {
                     sapiResultError.cause = error.getString(JSON_OBJECT_ERROR_FIELD_CAUSE);
                 } catch (JSONException e) {}
             }
             
             //log the error
             if (Log.isLoggable(Log.DEBUG)) {
                 StringBuffer logMsg = new StringBuffer()
                         .append("Error in SAPI response").append("\r\n")
                         .append("code: ").append(sapiResultError.code).append("\r\n")
                         .append("cause: ").append(sapiResultError.cause).append("\r\n")
                         .append("message: ").append(sapiResultError.message).append("\r\n");
                 Log.debug(TAG_LOG, logMsg.toString());
             }
             return sapiResultError;
         }
     }
 }
