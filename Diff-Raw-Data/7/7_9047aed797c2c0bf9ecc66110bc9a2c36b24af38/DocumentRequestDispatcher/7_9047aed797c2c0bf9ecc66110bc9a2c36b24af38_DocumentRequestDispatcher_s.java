 /*
  * Copyright (c) 2011 Imaginea Technologies Private Ltd.
  * Hyderabad, India
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *     http://www.apache.org/licenses/LICENSE-2.0
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package com.imaginea.mongodb.requestdispatchers;
 
 import com.imaginea.mongodb.common.exceptions.ApplicationException;
 import com.imaginea.mongodb.common.exceptions.DocumentException;
 import com.imaginea.mongodb.common.exceptions.ErrorCodes;
 import com.imaginea.mongodb.services.DocumentService;
 import com.imaginea.mongodb.services.DocumentServiceImpl;
 import com.mongodb.BasicDBObject;
 import com.mongodb.DBCursor;
 import com.mongodb.DBObject;
 import com.mongodb.Mongo;
 import com.mongodb.util.JSON;
 import org.apache.log4j.Logger;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.ws.rs.*;
 import javax.ws.rs.core.Context;
 import javax.ws.rs.core.MediaType;
 import java.util.*;
 
 /**
  * Defines resources for performing create/delete/update operations on documents
  * present inside collections in databases in Mongo we are currently connected
  * to. Also provide resources to get list of all documents present inside a
  * collection in a database in mongo.
  * <p/>
  * These resources map different HTTP equests made by the client to access these
  * resources to services file which performs these operations. The resources
  * also form a JSON response using the output recieved from the serives files.
  * GET and POST request resources for documents are defined here. For PUT and
  * DELETE functionality , a POST request with an action parameter taking values
  * PUT and DELETE is made.
  *
  * @author Rachit Mittal
  * @since 6 July 2011
  */
 @Path("/{dbName}/{collectionName}/document")
 public class DocumentRequestDispatcher extends BaseRequestDispatcher {
     private final static Logger logger = Logger.getLogger(DocumentRequestDispatcher.class);
 
 
     /**
      * Maps GET Request to get list of documents inside a collection inside a
      * database present in mongo db to a service function that returns the list.
      * Also forms the JSON response for this request and sent it to client. In
      * case of any exception from the service files an error object if formed.
      *
      * @param dbName         Name of Database
      * @param collectionName Name of Collection
      * @param dbInfo         Mongo Db Configuration provided by user to connect to.
      * @param request        Get the HTTP request context to extract session parameters
      * @return A String of JSON format with list of All Documents in a
      *         collection.
      */
     @GET
     @Produces(MediaType.APPLICATION_JSON)
     public String getQueriedDocsList(@PathParam("dbName") final String dbName, @PathParam("collectionName") final String collectionName, @QueryParam("query") final String query,
                                      @QueryParam("dbInfo") final String dbInfo, @QueryParam("fields") String keys, @QueryParam("limit") final String limit, @QueryParam("skip") final String skip,
                                      @Context final HttpServletRequest request) throws JSONException {
 
         // Get all fields with "_id" in case of keys = null
         if (keys == null) {
             keys = "";
         }
         final String fields = keys;
 
         String response = new ResponseTemplate().execute(logger, dbInfo, request, new ResponseCallback() {
             public Object execute() throws Exception {
 
                 DocumentService documentService = new DocumentServiceImpl(dbInfo);
                 // Get query
                 DBObject queryObj = (DBObject) JSON.parse(query);
                 StringTokenizer strtok = new StringTokenizer(fields, ",");
                 DBObject keyObj = new BasicDBObject();
                 while (strtok.hasMoreElements()) {
                     keyObj.put(strtok.nextToken(), 1);
                 }
                 int docsLimit = Integer.parseInt(limit);
                 int docsSkip = Integer.parseInt(skip);
                 JSONObject result = documentService.getQueriedDocsList(dbName, collectionName, queryObj, keyObj, docsLimit, docsSkip);
                 return result;
             }
         });
 
         return response;
     }
 
     /**
      * Maps GET Request to get all keys of document inside a collection inside a
      * database present in mongo db to a service function that returns the list.
      * Also forms the JSON response for this request and sent it to client. In
      * case of any exception from the service files an error object if formed.
      *
      * @param dbName         Name of Database
      * @param collectionName Name of Collection
      * @param dbInfo         Mongo Db Configuration provided by user to connect to.
      * @param request        Get the HTTP request context to extract session parameters
      * @return A String of JSON format with all keys in a collection.
      */
     @GET
     @Path("/keys")
     @Produces(MediaType.APPLICATION_JSON)
    public String getKeysRequest(@PathParam("dbName") final String dbName, @PathParam("collectionName") final String collectionName, @QueryParam("dbInfo") final String dbInfo,
                                  @Context final HttpServletRequest request) {
 
         String response = new ResponseTemplate().execute(logger, dbInfo, request, new ResponseCallback() {
             public Object execute() throws Exception {
                 // Perform the operation here only.
                 Mongo mongoInstance = UserLogin.mongoConfigToInstanceMapping.get(dbInfo);
                 long count = mongoInstance.getDB(dbName).getCollection(collectionName).count();
                 DBCursor cursor = mongoInstance.getDB(dbName).getCollection(collectionName).find();
                cursor.limit(10);
                 DBObject doc = new BasicDBObject();
                 Set<String> completeSet = new HashSet<String>();
                 while (cursor.hasNext()) {
                     doc = cursor.next();
                     getNestedKeys(doc, completeSet, "");
                 }
                 completeSet.remove("_id");
                 JSONObject result = new JSONObject();
                 result.put("keys", completeSet);
                 result.put("count", count);
                 return result;
             }
         });
         return response;
     }
 
     /**
      * Gets the keys within a nested document and adds it to the complete Set.
      * Used by getKeysRequest function above.
      *
      * @param doc         document
      * @param completeSet collection of all keys
      * @param prefix      For nested docs. For the key <foo.bar.baz>, the prefix would
      *                    be <foo.bar>
      */
     private void getNestedKeys(DBObject doc, Set<String> completeSet, String prefix) {
         Set<String> allKeys = doc.keySet();
         Iterator<String> it = allKeys.iterator();
         while (it.hasNext()) {
             String temp = it.next();
             completeSet.add(prefix + temp);
             if (doc.get(temp) instanceof BasicDBObject) {
                 getNestedKeys((DBObject) doc.get(temp), completeSet, prefix + temp + ".");
             }
         }
     }
 
     /**
      * Maps POST Request to perform operations like update/delete/insert
      * document inside a collection inside a database present in mongo db to a
      * service function that returns the list. Also forms the JSON response for
      * this request and sent it to client. In case of any exception from the
      * service files an error object if formed.
      *
      * @param dbName         Name of Database
      * @param collectionName Name of Collection
      * @param documentData   Contains the document to be inserted
      * @param _id            Object id of document to delete or update
      * @param keys           new Document values in case of update
      * @param action         Query Paramater with value PUT for identifying a create
      *                       database request and value DELETE for dropping a database.
      * @param dbInfo         Mongo Db Configuration provided by user to connect to.
      * @param request        Get the HTTP request context to extract session parameters
      * @return String with Status of operation performed.
      */
     @POST
     @Produces(MediaType.APPLICATION_JSON)
     public String postDocsRequest(@PathParam("dbName") final String dbName, @PathParam("collectionName") final String collectionName, @DefaultValue("POST") @QueryParam("action") final String action,
                                   @FormParam("document") final String documentData, @FormParam("_id") final String _id, @FormParam("keys") final String keys, @QueryParam("dbInfo") final String dbInfo,
                                   @Context final HttpServletRequest request) {
 
         String response = new ResponseTemplate().execute(logger, dbInfo, request, new ResponseCallback() {
             public Object execute() throws Exception {
 
                 DocumentService documentService = new DocumentServiceImpl(dbInfo);
                 String result = null;
                 RequestMethod method = null;
                 for (RequestMethod m : RequestMethod.values()) {
                     if ((m.toString()).equals(action)) {
                         method = m;
                         break;
                     }
                 }
                 switch (method) {
                     case PUT: {
                         if ("".equals(documentData)) {
                             ApplicationException e = new DocumentException(ErrorCodes.DOCUMENT_DOES_NOT_EXIST, "Document Data Missing in Request Body");
                             result = formErrorResponse(logger, e);
                         } else {
                             DBObject document = (DBObject) JSON.parse(documentData);
                             result = documentService.insertDocument(dbName, collectionName, document);
                         }
                         break;
                     }
                     case DELETE: {
                         if ("".equals(_id)) {
                             ApplicationException e = new DocumentException(ErrorCodes.DOCUMENT_DOES_NOT_EXIST, "Document Data Missing in Request Body");
                             result = formErrorResponse(logger, e);
                         } else {
                             result = documentService.deleteDocument(dbName, collectionName, _id);
                         }
                         break;
                     }
                     case POST: {
                         if ("".equals(_id) || "".equals(keys)) {
                             ApplicationException e = new DocumentException(ErrorCodes.DOCUMENT_DOES_NOT_EXIST, "Document Data Missing in Request Body");
                             formErrorResponse(logger, e);
                         } else {
                             // New Document Keys
                             DBObject newDoc = (DBObject) JSON.parse(keys);
                             result = documentService.updateDocument(dbName, collectionName, _id, newDoc);
                         }
                         break;
                     }
 
                     default: {
                         result = "Action parameter value is wrong";
                         break;
                     }
                 }
                 return result;
             }
         });
         return response;
     }
 }
