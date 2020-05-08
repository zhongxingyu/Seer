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
 package com.funambol.json.dao;
 
 import java.io.IOException;
 import org.apache.commons.httpclient.HttpClient;
 import org.apache.commons.httpclient.HttpException;
 import org.apache.commons.httpclient.HttpConnectionManager;
 import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
 import org.apache.commons.httpclient.NameValuePair;
 import org.apache.commons.httpclient.methods.DeleteMethod;
 import org.apache.commons.httpclient.methods.GetMethod;
 import org.apache.commons.httpclient.methods.PostMethod;
 import org.apache.commons.httpclient.methods.PutMethod;
 import org.apache.commons.httpclient.methods.StringRequestEntity;
 import com.funambol.framework.logging.FunambolLogger;
 import com.funambol.framework.logging.FunambolLoggerFactory;
 import com.funambol.json.admin.JsonConnectorConfig;
 import com.funambol.json.domain.JsonResponse;
 import com.funambol.json.exception.JsonConfigException;
 import com.funambol.json.util.Utility;
 import com.funambol.server.config.Configuration;
 
 
 public class JsonDAOImpl implements JsonDAO {
 
     protected static final FunambolLogger log = FunambolLoggerFactory.getLogger(Utility.LOG_NAME);
     private final static String SINCE = "since";
     private final static String UNTIL = "until";
     private final static String BEGIN_SYNC_URL = "sync/begin";
     private final static String END_SYNC_URL = "sync/end";
     private final static String ADD_ITEM_URL = "items";
     private final static String GET_ITEM_URL = "items";
     private final static String UPDATE_ITEM_URL = "items";
     private final static String REMOVE_ITEM_URL = "items";
     private final static String GET_NEW_ITEM_KEYS_URL = "keys/new";
     private final static String GET_ALL_ITEM_URL = "keys/all";
     private final static String GET_UPDATED_ITEM_KEYS_URL = "keys/updated";
     private final static String GET_DELETED_ITEM_KEYS_URL = "keys/deleted";
     private final static String GET_ITEM_FROM_TWIN_URL = "keys/twins";
     private String resourceType;
     private String jsonServerUrl = null;
 
     private HttpClient httpClient = null;
  
     //-------------------------------------------------------------- Constructor
 
     /**
      * 
      * @param resourceType
      */
     public JsonDAOImpl(String resourceType) throws JsonConfigException {
         if (resourceType == null || resourceType.trim().equals("")) {
             throw new RuntimeException("The resourceType must be not null!");
         }
         try {
             //Get connection parameters...
             this.jsonServerUrl = JsonConnectorConfig.getConfigInstance().getJsonServerUrl();
 
         } catch (JsonConfigException e) {
             throw new JsonConfigException("Error looking up the Configuration File", e);
         }
         this.resourceType = resourceType;
 
         HttpConnectionManager httpConnectionManager = new MultiThreadedHttpConnectionManager();
         this.httpClient = new HttpClient(httpConnectionManager);
 
     }
 
     //----------------------------------------------------------- Public Methods
     
     /* (non-Javadoc)
      * @see com.funambol.json.dao.JsonDAO#beginSync(java.lang.String)
      */
     public JsonResponse beginSync(String token, String jsonObject)
     throws HttpException, IOException {
 
         String request =
             Utility.getUrl(jsonServerUrl, resourceType, BEGIN_SYNC_URL);
 
         if (log.isTraceEnabled()) {
             log.trace("Starting sync..");
         }
 
 
         PostMethod post = new PostMethod(request);
         post.setRequestHeader(Utility.TOKEN_HEADER_NAME, token);
         post.setRequestEntity(new StringRequestEntity(jsonObject));
 
         if (log.isTraceEnabled()) {
             log.trace("Request received [" + request + "] for token [" + token
                       + "] and json [" + jsonObject + "]..");
         }
 
         int statusCode = 0;
         String responseBody = null;
         try {
             statusCode = httpClient.executeMethod(post);
             responseBody = post.getResponseBodyAsString();
         } finally {
             post.releaseConnection();
         }
 
         if (log.isTraceEnabled()) {
             log.trace("Response after starting sync [" + responseBody + "]");
         }
 
         JsonResponse response = new JsonResponse(statusCode, responseBody);
 
         return response;
     }
 
     /* (non-Javadoc)
      * @see com.funambol.json.dao.JsonDAO#endSync(java.lang.String)
      */
     public JsonResponse endSync(String token) throws HttpException, IOException {
 
         // @todo .. recource_type
         String request = Utility.getUrl(jsonServerUrl, resourceType, END_SYNC_URL);
 
         if (log.isTraceEnabled()) {
             log.trace("JsonDAOImpl: start endSync ");
         }
 
         PostMethod post = new PostMethod(request);
 
         if (log.isTraceEnabled()) {
             log.trace("endSync request: " + request);
             log.trace("endSync token " + token);
         }
 
         post.setRequestHeader(Utility.TOKEN_HEADER_NAME, token);
 
         int statusCode = 0;
         String responseBody = null;
         try {
             statusCode = httpClient.executeMethod(post);
             responseBody = post.getResponseBodyAsString();
         } finally {
             post.releaseConnection();
         }
 
         if (log.isTraceEnabled()) {
             log.trace("JsonDAOImpl: endSync end");
         }
 
         JsonResponse response = new JsonResponse(statusCode, responseBody);
 
         return response;
     }
 
     /* (non-Javadoc)
      * @see com.funambol.json.dao.JsonDAO#addItem(java.lang.String, java.lang.String)
      */
     public JsonResponse addItem(String token, String jsonObject, long since) throws HttpException, IOException {
 
         String request = Utility.getUrl(jsonServerUrl, resourceType, ADD_ITEM_URL);
 
         if (log.isTraceEnabled()) {
             log.trace("JsonDAOImpl: start addItem; since="+since);
         }
 
         PostMethod post = new PostMethod(request);
         post.setRequestHeader(Utility.TOKEN_HEADER_NAME, token);
         post.setRequestEntity(new StringRequestEntity(jsonObject));
 
         if (since != 0){
             NameValuePair nvp_since = new NameValuePair();
             nvp_since.setName("since");
             nvp_since.setValue(""+since);
             NameValuePair[] nvp = {nvp_since};                
             post.setQueryString(nvp);
         }
         
         if (log.isTraceEnabled()) {
             log.trace("addItem Request: " + request);
         }
         if (Configuration.getConfiguration().isDebugMode()) {
             if (log.isTraceEnabled()) {
                 log.trace("JSON to add " + jsonObject);
             }
         }
 
         int statusCode = 0;
         String responseBody = null;
 
         try {
             statusCode = httpClient.executeMethod(post);
             responseBody = post.getResponseBodyAsString();
         } finally {
             post.releaseConnection();
         }
 
         if (log.isTraceEnabled()) {
             log.trace("JsonDAOImpl: statusCode " +statusCode+ "; added item" + responseBody);
             log.trace("JsonDAOImpl: item added");
         }
 
         JsonResponse response = new JsonResponse(statusCode, responseBody);
 
         return response;
     }
 
     /* (non-Javadoc)
      * @see com.funambol.json.dao.JsonDAO#getItem(java.lang.String, java.lang.String)
      */
     public JsonResponse getItem(String token, String id) throws HttpException, IOException {
 
         String request = Utility.getUrl(jsonServerUrl, resourceType, GET_ITEM_URL) + Utility.URL_SEP + id;
 
         if (log.isTraceEnabled()) {
             log.trace("JsonDAOImpl: start getItem with id:" + id);
         }
 
         GetMethod get = new GetMethod(request);
         get.setRequestHeader(Utility.TOKEN_HEADER_NAME, token);
 
         int statusCode = 0;
         String responseBody = null;
         try {
             statusCode = httpClient.executeMethod(get);
             responseBody = get.getResponseBodyAsString();
         } finally {
             get.releaseConnection();
         }
 
        if (log.isTraceEnabled()) {
            log.trace("JsonDAOImpl: getItem response " + responseBody);
            log.trace("JsonDAOImpl: getItem with id:" + id + "finished");
         }
 
         JsonResponse response = new JsonResponse(statusCode, responseBody);
 
         return response;
     }
 
     /* (non-Javadoc)
      * @see com.funambol.json.dao.JsonDAO#updateItem(java.lang.String, java.lang.String, java.lang.String)
      */
     public JsonResponse updateItem(String token, String id, String jsonObject, long since) throws HttpException, IOException {
 
         String request = Utility.getUrl(jsonServerUrl, resourceType, UPDATE_ITEM_URL) + Utility.URL_SEP + id;
 
         if (log.isTraceEnabled()) {
             log.trace("JsonDAOImpl: start updateItem with id:" + id +  " and since=" + since+" sessionid:"+token);
         }
 
         PutMethod put = new PutMethod(request);
         put.setRequestHeader(Utility.TOKEN_HEADER_NAME, token);
         put.setRequestEntity(new StringRequestEntity(jsonObject));
 
         if (since != 0){
             NameValuePair nvp_since = new NameValuePair();
             nvp_since.setName("since");
             nvp_since.setValue(""+since);
             NameValuePair[] nvp = {nvp_since};                
             put.setQueryString(nvp);
         }
         
         if (log.isTraceEnabled()) {
             log.trace("updateItem Request: " + request);
         }
         if (Configuration.getConfiguration().isDebugMode()) {
             if (log.isTraceEnabled()) {
                 log.trace("JSON to update " + jsonObject);
             }
         }
 
         int statusCode = 0;
         String responseBody = null;
         try {
             statusCode = httpClient.executeMethod(put);
             responseBody = put.getResponseBodyAsString();
         } finally {
             put.releaseConnection();
         }
 
         if (log.isTraceEnabled()) {
             log.trace("JsonDAOImpl: item with id:" + id + " updated; response " + responseBody);
         }
 
         JsonResponse response = new JsonResponse(statusCode, responseBody);
 
         return response;
 
     }
 
     /* (non-Javadoc)
      * @see com.funambol.json.dao.JsonDAO#removeItem(java.lang.String, java.lang.String)
      */
     public JsonResponse removeItem(String token, String id, long since) throws HttpException, IOException {
 
         String request = Utility.getUrl(jsonServerUrl, resourceType, REMOVE_ITEM_URL) + Utility.URL_SEP + id;
 
         if (log.isTraceEnabled()) {
             log.trace("JsonDAOImpl: start removeItem with id:" + id + "and since=" + since);
         }
 
         DeleteMethod remove = new DeleteMethod(request);
 
         if (since != 0){
             NameValuePair nvp_since = new NameValuePair();
             nvp_since.setName("since");
             nvp_since.setValue(""+since);
             NameValuePair[] nvp = {nvp_since};                
             remove.setQueryString(nvp);
         }
         
         if (log.isTraceEnabled()) {
             log.trace("JsonDAOImpl: removeItem request:" + request);
         }
         remove.setRequestHeader(Utility.TOKEN_HEADER_NAME, token);
 
         int statusCode = 0;
         String responseBody = null;
         try {
             statusCode = httpClient.executeMethod(remove);
             responseBody = remove.getResponseBodyAsString();
         } finally {
             remove.releaseConnection();
         }
 
         if (log.isTraceEnabled()) {
             log.trace("JsonDAOImpl: deleteItem response " + responseBody);
             log.trace("JsonDAOImpl: item with id:" + id + " removed");
         }
 
         JsonResponse response = new JsonResponse(statusCode, responseBody);
 
         return response;
     }
 
     /* (non-Javadoc)
      * @see com.funambol.json.dao.JsonDAO#removeItem(java.lang.String, java.lang.String)
      */
     public JsonResponse removeAllItems(String token, long since) throws HttpException, IOException {
 
         String request = Utility.getUrl(jsonServerUrl, resourceType, REMOVE_ITEM_URL);
 
         if (log.isTraceEnabled()) {
             log.trace("JsonDAOImpl: start removeAllItems");
         }
 
         DeleteMethod remove = new DeleteMethod(request);
 
         if (since != 0){
             NameValuePair nvp_since = new NameValuePair();
             nvp_since.setName("since");
             nvp_since.setValue(""+since);
             NameValuePair[] nvp = {nvp_since};                
             remove.setQueryString(nvp);
         }
         
         if (log.isTraceEnabled()) {
             log.trace("JsonDAOImpl: removeAllItem request:" + request);
         }
         remove.setRequestHeader(Utility.TOKEN_HEADER_NAME, token);
 
         int statusCode = 0;
         String responseBody = null;
         try {
             statusCode = httpClient.executeMethod(remove);
             responseBody = remove.getResponseBodyAsString();
         } finally {
             remove.releaseConnection();
         }
 
         if (log.isTraceEnabled()) {
             log.trace("JsonDAOImpl: deleteAllItem response " + responseBody);
         }
 
         JsonResponse response = new JsonResponse(statusCode, responseBody);
 
         return response;
     }
     
     /* (non-Javadoc)
      * @see com.funambol.json.dao.JsonDAO#getAllItemsKey(java.lang.String)
      */
     public JsonResponse getAllItemKeys(String token) throws HttpException, IOException {
 
         String request = Utility.getUrl(jsonServerUrl, resourceType, GET_ALL_ITEM_URL);
 
         if (log.isTraceEnabled()) {
             log.trace("JsonDAOImpl: start getAllItem");
         }
 
         GetMethod get = new GetMethod(request);
 
         if (log.isTraceEnabled()) {
             log.trace("getAllItem request " + request);
             log.trace("getAllItem token " + token);
         }
 
         get.setRequestHeader(Utility.TOKEN_HEADER_NAME, token);
 
         int statusCode = 0;
         String responseBody = null;
         try {
             statusCode = httpClient.executeMethod(get);
             responseBody = get.getResponseBodyAsString();
         } finally {
             get.releaseConnection();
         }
 
         if (log.isTraceEnabled()) {
             log.trace("JsonDAOImpl: getAllItem");
         }
 
         JsonResponse response = new JsonResponse(statusCode, responseBody);
 
         return response;
     }
 
     /* (non-Javadoc)
      * @see com.funambol.json.dao.JsonDAO#getNewItemKeys(java.lang.String, long, long)
      */
     public JsonResponse getNewItemKeys(String token, long since, long until) throws HttpException, IOException {
 
         String request = Utility.getUrl(jsonServerUrl, resourceType, GET_NEW_ITEM_KEYS_URL);
 
         if (log.isTraceEnabled()) {
             log.trace("JsonDAOImpl: start getNewItemKeys '" + since + "; '" + until + "'");
         }
 
         GetMethod get = new GetMethod(request);
         get.setRequestHeader(Utility.TOKEN_HEADER_NAME, token);
         NameValuePair[] httpParameters = {new NameValuePair(SINCE, Long.toString(since)), new NameValuePair(UNTIL, Long.toString(until))};
         get.setQueryString(httpParameters);
 
         int statusCode = 0;
         String responseBody = null;
         try {
             statusCode = httpClient.executeMethod(get);
             responseBody = get.getResponseBodyAsString();
         } finally {
             get.releaseConnection();
         }
 
         if (log.isTraceEnabled()) {
             log.trace("JsonDAOImpl: getNewItemKeys " + responseBody);
             log.trace("JsonDAOImpl: finish getNewItemKeys");
         }
 
         JsonResponse response = new JsonResponse(statusCode, responseBody);
 
         return response;
     }
 
     /* (non-Javadoc)
      * @see com.funambol.json.dao.JsonDAO#getUpdatedItemKeys(java.lang.String, long, long)
      */
     public JsonResponse getUpdatedItemKeys(String token, long since, long until) throws HttpException, IOException {
 
 
         String request = Utility.getUrl(jsonServerUrl, resourceType, GET_UPDATED_ITEM_KEYS_URL);
 
         if (log.isTraceEnabled()) {
             log.trace("JsonDAOImpl: start getUpdatedItemKeys '" + since + "; '" + until + "'");
         }
 
         GetMethod get = new GetMethod(request);
         get.setRequestHeader(Utility.TOKEN_HEADER_NAME, token);
         NameValuePair[] httpParameters = {new NameValuePair(SINCE, Long.toString(since)), new NameValuePair(UNTIL, Long.toString(until))};
         get.setQueryString(httpParameters);
 
         int statusCode = 0;
         String responseBody = null;
         try {
             statusCode = httpClient.executeMethod(get);
             responseBody = get.getResponseBodyAsString();
         } finally {
             get.releaseConnection();
         }
 
         if (log.isTraceEnabled()) {
             log.trace("JsonDAOImpl: getUpdatedItemKeys " + responseBody);
             log.trace("JsonDAOImpl: finish getUpdatedItemKeys");
         }
 
         JsonResponse response = new JsonResponse(statusCode, responseBody);
 
         return response;
     }
 
     /* (non-Javadoc)
      * @see com.funambol.json.dao.JsonDAO#getDeletedItemKeys(java.lang.String, long, long)
      */
     public JsonResponse getDeletedItemKeys(String token, long since, long until) throws HttpException, IOException {
 
         String request = Utility.getUrl(jsonServerUrl, resourceType, GET_DELETED_ITEM_KEYS_URL);
 
         if (log.isTraceEnabled()) {
             log.trace("JsonDAOImpl: start getDeletedItemKeys '" + since + "; '" + until + "'");
         }
 
         GetMethod get = new GetMethod(request);
         get.setRequestHeader(Utility.TOKEN_HEADER_NAME, token);
         NameValuePair[] httpParameters = {
             new NameValuePair(SINCE, Long.toString(since)),
             new NameValuePair(UNTIL, Long.toString(until))};
         get.setQueryString(httpParameters);
 
         int statusCode = 0;
         String responseBody = null;
         try {
             statusCode = httpClient.executeMethod(get);
             responseBody = get.getResponseBodyAsString();
         } finally {
             get.releaseConnection();
         }
 
         if (log.isTraceEnabled()) {
             log.trace("JsonDAOImpl: getDeletedItemKeys " + responseBody);
             log.trace("JsonDAOImpl: finish getDeletedItemKeys");
         }
 
         JsonResponse response = new JsonResponse(statusCode, responseBody);
 
         return response;
     }
 
     /* (non-Javadoc)
      * @see com.funambol.json.dao.JsonDAO#getItemKeysFromTwin(java.lang.String, java.util.Map)
      */
     public JsonResponse getItemKeysFromTwin(String token, String jsonContent)
     throws HttpException, IOException {
 
         String request = Utility.getUrl(jsonServerUrl, resourceType, GET_ITEM_FROM_TWIN_URL);
 
         PostMethod post = new PostMethod(request);
         post.setRequestHeader(Utility.TOKEN_HEADER_NAME, token);
         post.setRequestEntity(new StringRequestEntity(jsonContent));
 
         if (log.isTraceEnabled()) {
             log.trace("Getting item keys from twin..");
         }
 
         if (Configuration.getConfiguration().isDebugMode()) {
             if (log.isTraceEnabled()) {
                 log.trace("Getting item keys from twin of '" + jsonContent
                     + "'");
             }
         }
 
         int statusCode = 0;
         String responseBody = null;
         try {
             statusCode = httpClient.executeMethod(post);
             responseBody = post.getResponseBodyAsString();
         } finally {
             post.releaseConnection();
         }
 
         if (log.isTraceEnabled()) {
             log.trace("Found item keys from twin '" + responseBody + "'");
         }
 
         JsonResponse response = new JsonResponse(statusCode, responseBody);
 
         return response;
     }
     
     /* (non-Javadoc)
      * @see com.funambol.json.dao.JsonDAO#addItem(java.lang.String, java.lang.String)
      */
     public JsonResponse getTimeConfiguration(String token) throws HttpException, IOException {
         
         String request =  jsonServerUrl + Utility.URL_SEP + "config/time";
         
         if (log.isTraceEnabled()) {
             log.trace("JSONDAOImpl: start getConfiguration ");
         }
         
         GetMethod post = new GetMethod(request);
         post.setRequestHeader(Utility.TOKEN_HEADER_NAME, token);
 
         if (log.isTraceEnabled()) {
             log.trace("getConfiguration Request: " + request);
         }
 
         int statusCode = 0;
         String responseBody = null;
         try {
             statusCode = httpClient.executeMethod(post);
             responseBody = post.getResponseBodyAsString();
         } finally {
             post.releaseConnection();
         }
 
         if (log.isTraceEnabled()) {
             log.trace("JsonDAOImpl: getConfiguration response " + responseBody);
         }
 
         JsonResponse response = new JsonResponse(statusCode, responseBody);
 
         return response;
     }
 
      /**
      * This method allows to understand if is possible to run the twin search
      * on the given contact.
      * Fields used in the twin search are:
      * -  firstName
      * -  lastName
      * -  displayName
      * -  emailAddress
      * -  emailAddressHome
      * -  emailAddressWork
      * -  companyName
      *
      * @param c the contact we want to check.
      *
      * @return true if at least one field used for twin search contains meaningful
      * data, false otherwise.
      *
      */
     public boolean isTwinSearchAppliableOn(String token, String jsonObject) {
            return true;
 
     }
 
 }
