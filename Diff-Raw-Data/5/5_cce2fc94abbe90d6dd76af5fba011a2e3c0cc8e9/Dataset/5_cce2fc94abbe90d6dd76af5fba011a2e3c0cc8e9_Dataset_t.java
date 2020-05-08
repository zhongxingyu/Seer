 package com.socrata;
 
 /*
 
 Copyright (c) 2010 Socrata.
 
 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at
 
     http://www.apache.org/licenses/LICENSE-2.0
 
 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 
  */
 
 import java.io.File;
 import java.io.UnsupportedEncodingException;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.ResourceBundle;
 import java.util.logging.Level;
 import java.util.regex.Pattern;
 import org.apache.http.client.methods.HttpDelete;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.client.methods.HttpPut;
 import org.apache.http.entity.StringEntity;
 import org.apache.http.entity.mime.HttpMultipartMode;
 import org.apache.http.entity.mime.MultipartEntity;
 import org.apache.http.entity.mime.content.FileBody;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 import org.json.JSONStringer;
 
 /**
  * A Socrata dataset and associated rows/columns/metadata
  *
  * @author aiden.scandella@socrata.com
  */
 public class Dataset extends ApiBase {
     private String  id;
     private static final Pattern  UID_PATTERN                 = Pattern.compile("[a-z0-9]{4}-[a-z0-9]{4}");
     private static final Integer  DEFAULT_COLUMN_WIDTH        = 100;
     private static final String   DEFAULT_COLUMN_TYPE_STRING  = "text";
     private static final DataType DEFAULT_COLUMN_TYPE         = DataType.TEXT;
 
     /**
      * Default constructor
      */
     public Dataset() {
         super();
     }
     /**
      * Copy constructor
      * @param properties properties from an existing Socrata object
      */
     public Dataset(ResourceBundle properties) {
         super(properties);
     }
 
     /**
      * Creates a new dataset on the server
      * @param title  the title of the dataset
      * @param description  the description of the dataset
      * @param tags  tags associated with the dataset
      */
     public boolean create(String title, String description, String[] tags) {
         JSONObject data = new JSONObject();
 
         try {
             data.put("name", title);
             data.put("description", description);
 
             if ( tags != null && tags.length > 0 ) {
                data.put("tags", tags);
             }
         }
         catch (JSONException ex) {
             log(Level.SEVERE, "Caught JSON exception in Dataset.create()", ex);
             return false;
         }
         HttpPost request = new HttpPost(httpBase() + "/views.json");
         try {
             request.setEntity(new StringEntity(data.toString()));
         }
 
         catch (UnsupportedEncodingException ex) {
             log(Level.SEVERE, "Could not encode Dataset.create() request data.", ex);
             return false;
         }
         
         // If we've made it this far, the Java gods are smiling down on us
         JsonPayload response = performRequest(request);
         if ( isErroneous(response) ) {
             log(Level.SEVERE, "Error in dataset creation, see logs" , null);
             return false;
         }
         if ( response.getObject() == null ) {
             log(Level.SEVERE, "Received empty response from server on Dataset.create()", null);
             return false;
         }
         try {
             this.id = response.getObject().getString("id");
         }
         catch(JSONException ex) {
             log(Level.SEVERE, 
                     "Could not extract dataset id from JSON response:\n" +
                     response.toString(), ex);
             return false;
         }
         log(Level.INFO, "Successfully created dataset with id " + this.id, null);
         return true;
     }
 
     /**
      * Convenience create method
      * @param title  the title of the dataset
      * @param description  the description of the dataset
      */
     public boolean create(String title, String description) {
         return create(title, description, null);
     }
 
     /**
      * Convenience create method
      * @param title  the title of the dataset
      */
     public boolean create(String title) {
         return create(title, "", null);
     }
 
     /**
      * Import a new file to create a dataset
      * @param file the file (XLS, XLSX, or CSV) to import
      * @return success or failure
      */
     public boolean importFile(File file) {
         JSONObject response = multipartUpload("/imports", file, "file");
 
         if ( response == null ) {
             log(Level.SEVERE, "Received a null response after file import.", null);
             return false;
         }
         else {
             try {
                 this.id =  response.getString("id");
                 log(Level.INFO, "Successfully imported file '"
                         + response.getString("name") + "' (" + id() + ")");
             }
             catch (JSONException ex) {
                 log(Level.SEVERE, "Could not deserialize JSON response in file import.", ex);
                 return false;
             }
         }
         return true;
     }
 
     /**
      * Refresh (replace) the current dataset with a file
      * @param file the data file to use
      * @return success or failure
      */
     public boolean refresh(File file) {
         return multipartAppendOrRefresh(file, "replace");
     }
 
     /**
      * Append the current dataset with a file
      * @param file the data file to use
      * @return success or failure
      */
     public boolean append(File file) {
         return multipartAppendOrRefresh(file, "append");
     }
 
     private boolean multipartAppendOrRefresh(File file, String method) {
         if ( ! attached() ) {
             return false;
         }
         JSONObject response = multipartUpload("/views/" + id() + "/rows?method=" + method, file, "file");
 
         if ( response == null ) {
             log(Level.SEVERE, "Received a null response after file append or refresh.", null);
             return false;
         }
         return true;
     }
 
     /**
      * Adds a row to the dataset
      * @param row Key/value pairs of column/data
      * @return success or failure
      */
     public boolean addRow(Map row) {
         if ( ! attached() ) {
             return false;
         }
         JSONObject rowJson = new JSONObject(row);
         HttpPost request = new HttpPost(httpBase() + "/views/" +
                 id() + "/rows.json");
         try {
             request.setEntity(new StringEntity(rowJson.toString()));
         }
         catch (UnsupportedEncodingException ex) {
             log(Level.SEVERE, "Could not encode row data in Dataset.addRow().", ex);
             return false;
         }
         JsonPayload response = performRequest(request);
 
         return !isErroneous(response);
     }
 
     /**
      * Creates an "add row" request and adds it to the batch queue
      * @param row Key/value pairs of column data
      */
     public void delayAddRow(Map row) {
         JSONObject rowJson = new JSONObject(row);
 
         BatchRequest request = new BatchRequest("POST",
                 "/views/" + id() + "/rows.json", rowJson.toString());
         batchQueue.add(request);
     }
 
     /**
      * Creates a new column in the dataset
      * @param name  the unique name of the column
      * @param description  an optional description for the column
      * @param type  the data type; e.g. text, number, url
      * @param width  how many pixels wide the column should display
      * @param hidden  whether or not the column is hidden
      * @return success or failure
      */
     public boolean addColumn(String name, String description, 
             DataType type, Integer width, Boolean hidden) {
         if ( ! attached() ) {
             return false;
         }
 
         log(Level.FINEST, "Creating column '" + name + "' of type '" +
                    type + "'");
 
         JSONObject columnJson = new JSONObject();
         try {
             columnJson.put("name", name);
             columnJson.put("description", description);
             columnJson.put("dataTypeName", getDataTypeName(type));
             columnJson.put("hidden", hidden);
             columnJson.put("width", width);
             if ( type == DataType.RICHTEXT ) {
                 Map<String, String> map = new HashMap<String, String>();
                 map.put("formatting_option", "rich");
                 columnJson.put("format", map);
             }
         } catch (JSONException ex) {
             log(Level.SEVERE, "Could not create column JSON data for addColumn()", ex);
         }
 
         HttpPost request = new HttpPost(httpBase() + "/views/" + id() +
                 "/columns.json");
 
         try {
             request.setEntity(new StringEntity(columnJson.toString()));
         } 
         catch (UnsupportedEncodingException ex) {
             log(Level.SEVERE, "Could not encode column data in Dataset.addColumn().", ex);
             return false;
         }
 
         return !isErroneous(performRequest(request));
     }
 
 
     public boolean addColumn(String name, String description, DataType type,
             Integer width) {
         return addColumn(name, description, type, width, false);
     }
 
     public boolean addColumn(String name, String description, DataType type) {
         return addColumn(name, description, type, DEFAULT_COLUMN_WIDTH, false);
     }
 
     public boolean addColumn(String name, String description) {
         return addColumn(name, description, DEFAULT_COLUMN_TYPE,
                 DEFAULT_COLUMN_WIDTH, false);
     }
 
     public boolean addColumn(String name) {
         return addColumn(name, "", DEFAULT_COLUMN_TYPE,
                 DEFAULT_COLUMN_WIDTH, false);
     }
 
     /**
      * Uploads a file to the api
      * @param file   the file to upload
      * @return   the ID used to put in a cell to reference this file
      */
     public String uploadFile(File file) {
         if ( !attached() ) {
             return null;
         }
 
         JSONObject response = multipartUpload("/views/" + id() + "/files.txt", file, "file");
         
         if ( response == null ) {
             log(Level.SEVERE, "Received a null response after file upload.", null);
             return null;
         }
         else {
             try {
                 // The good stuff (i.e. the ID needed to embed this file in a cell)
                 return response.getString("file");
             }
             catch (JSONException ex) {
                 log(Level.SEVERE, "Could not deserialize JSON response in file upload.", ex);
                 return null;
             }
         }
     }
 
     private JSONObject multipartUpload(String url, File file, String field) {
         HttpPost poster = new HttpPost(httpBase() + url);
 
         // Makes sure the Content-type is set, otherwise the server chokes
         MultipartEntity reqEntity = new MultipartEntity(HttpMultipartMode.STRICT);
 
         FileBody fileBody = new FileBody(file);
         reqEntity.addPart(field, fileBody);
 
         poster.setEntity(reqEntity);
         JsonPayload response = performRequest(poster);
 
         if ( isErroneous(response) ) {
             log(Level.SEVERE, "Failed to upload file.", null);
             return null;
         }
         return response.getObject();
     }
 
     /**
      * Given an exsting four-four Socrata UID, use this existing set
      * @param idString valid Socrata UID
      */
     public void attach(String idString) {
         if ( isValidId(idString) ) {
             this.id = idString;
         }
         else {
             log(Level.WARNING, "Could not use ID '" + idString +
                     "': invalid UID format", null);
         }
     }
 
     /**
      * Permanently deletes this dataset
      * @return true on success
      */
     public boolean delete() {
         if ( !attached() ) {
             return false;
         }
         HttpDelete request = new HttpDelete(httpBase()  +
                 "/views.json/?id=" + id() + "&method=delete");
         
         // This call should return nothing
         return !isErroneous(performRequest(request));
     }
 
     /**
      * Marks the dataset as public or private
      * @param isPublic whether or not other users can view the dataset
      */
     public boolean setPublic(Boolean isPublic) {
         String paramString = isPublic ? "public.read" : "private";
 
        HttpPut request = new HttpPut(httpBase() +
                 "/views/" + id() + "?method=setPermission&value=" + paramString);
 
         return !isErroneous(performRequest(request));
     }
 
     /**
      * @return whether or not the Dataset object is currently associated
      */
     public boolean attached() {
         return id() != null && !id().isEmpty() && isValidId(id());
     }
 
     /**
      * Gets the metadata associated with the dataset
      * @return the metadata
      */
     public JSONObject metadata() {
         if ( ! attached() ) {
             return null;
         }
         HttpGet request = new HttpGet(httpBase() + "/views/" + id() +
                 ".json");
         JsonPayload response = performRequest(request);
         return response.getObject();
     }
 
     /**
      * Fetches the dataset's columns
      * @return an array of columns, or null on failure
      */
     public JSONArray columns() {
         if ( !attached() ) {
             return null;
         }
 
         HttpGet request = new HttpGet(httpBase() + "/views/" +
                 id() + "/columns.json");
 
         JsonPayload response = performRequest(request);
         
         if ( !isErroneous(response) ) {
             return response.getArray();
         }
         return null;
     }
 
      /**
      * Fetches the dataset's rows
      * @return an array of rows, or null on failure
      */
     public JSONArray rows() {
         if ( !attached() ) {
             return null;
         }
 
         HttpGet request = new HttpGet(httpBase() + "/views/" +
                 id() + "/rows.json");
 
         JsonPayload response = performRequest(request);
 
         return response.getArray();
     }
 
     /**
      * Sets dataset attribution metadata
      * @param attribution the name of the attribution
      * @param url a link to the original source
      */
     public void setAttribution(String attribution, String url) {
         try {
             String json = new JSONStringer()
                 .object()
                     .key( "attribution" ).value( attribution )
                     .key( "attributionLink" ).value( url )
                  .endObject().toString();
             putRequest(json);    
         }
         catch (JSONException ex) {
             log(Level.WARNING, "Could not serialize attribution data to JSON", ex);
         }
 
     }
 
     /**
      * Sets the dataset's description
      * @param description a useful description for the dataset
      */
     public void setDescription(String description) {
         try {
             String json = new JSONStringer()
                 .object()
                     .key( "description" ).value( description )
                  .endObject().toString();
             putRequest(json);
         }
         catch (JSONException ex) {
             log(Level.WARNING, "Could not serialize description data to JSON", ex);
         }
     }
 
     /**
      * Gets a short link to this dataset
      * @return a rooted url for this dataset, in short form
      */
     public String shortUrl() {
         return properties.getString("web_host") + "/d/" + id();
     }
 
     /**
      * Performs a generic put request, i.e. for metadata/attribution
      * @param body a JSON string of data for the http put request
      */
     private void putRequest(String body) {
         if ( !attached() ) {
             return;
         }
         HttpPut request = new HttpPut(httpBase() + "/views/" + id());
         try {
             request.setEntity( new StringEntity(body) );
         }
         catch ( UnsupportedEncodingException ex ) {
             log(Level.WARNING, "Could not encode PUT into HTTP envelope", ex);
         }
         JsonPayload response = performRequest(request);
         if ( isErroneous(response) ) {
             log(Level.WARNING, "PUT request failed");
         }
     }
 
     /**
      * Whether the given string matches the Socrata uid pattern
      * @param idString the UID to test
      * @return whether or not the UID matches the regexp
      */
     private static boolean isValidId(String idString) {
         return UID_PATTERN.matcher(idString).matches();
     }
 
     /**
      * Gets the four-four id of the dataset
      * @return the four-four id of the current dataset
      */
     public String id() {
         return this.id;
     }
 
     /**
      * Converts a DataType enum into api string format
      * @param d the DataType to convert
      * @return a textual representation of the datatype
      */
     private String getDataTypeName(DataType d) {
         switch(d) {
             case TEXT:      return "text";
             case RICHTEXT:  return "text";
             case NUMBER:    return "number";
             case MONEY:     return "money";
             case PERCENT:   return "percent";
             case DATE:      return "date";
             case PHONE:     return "phone";
             case EMAIL:     return "email";
             case URL:       return "url";
             case CHECKBOX:  return "checkbox";
             case STAR:      return "stars";
             case FLAG:      return "flag";
             case DOCUMENT:  return "document";
             case PHOTO:     return "photo";
             
             default:        return DEFAULT_COLUMN_TYPE_STRING;
         }
     }
 
     // Which types of columns are supported
     public enum DataType {
         TEXT, RICHTEXT, NUMBER, MONEY, PERCENT, DATE, PHONE, EMAIL, URL,
         CHECKBOX, STAR, FLAG, DOCUMENT, PHOTO
     }
 }
