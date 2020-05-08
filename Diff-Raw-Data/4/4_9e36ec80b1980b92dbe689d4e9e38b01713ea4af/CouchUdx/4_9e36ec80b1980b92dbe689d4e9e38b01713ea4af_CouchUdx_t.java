 /*
 Dynamo CouchDB Connector is a plugin for browsing CouchDB views in LucidDB.
 Copyright (C) 2011 Dynamo Business Intelligence Corporation
 
 This program is free software; you can redistribute it and/or modify it
 under the terms of the GNU General Public License as published by the Free
 Software Foundation; either version 2 of the License, or (at your option)
 any later version approved by Dynamo Business Intelligence Corporation.
 
 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 GNU General Public License for more details.
 
 You should have received a copy of the GNU General Public License
 along with this program; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
 
 package com.dynamobi.db.conn.couchdb;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.OutputStreamWriter;
 import java.net.HttpURLConnection;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Stack;
 
 import net.sf.farrago.type.FarragoParameterMetaData;
 
 import net.sf.farrago.trace.*;
 import java.util.logging.*;
 
 import org.json.simple.JSONArray;
 import org.json.simple.JSONObject;
 import org.json.simple.parser.ContentHandler;
 import org.json.simple.parser.JSONParser;
 import org.json.simple.parser.ParseException;
 
 import org.apache.commons.codec.binary.Base64;
 
 import java.sql.ParameterMetaData;
 
 import java.sql.SQLException;
 import java.sql.PreparedStatement;
 
 /**
  * Generic Udx class to build any custom functions to browse CouchDB views.
  * @author Kevin Secretan
  */
 public class CouchUdx {
 
   private static final Logger logger 
     = FarragoTrace.getClassTracer(CouchUdx.class);
 
   /**
    * Called by a custom LucidDB function for each view.
    * @param userName - CouchDB user name
    * @param pw - CouchDB password
    * @param url - CouchDB REST URL
    * @param view - CouchDB REST view -- concatenated on the end of URL with
    *               a slash prefix if necessary.
    * @param limit - Limit parameter passed to couchdb
    * @param reduce - if false, we pass &amp;reduce=false to the view.
    * @param groupLevel - sent to view for group reduction, default 'EXACT'
    * Possible values: 'EXACT', sends &amp;group=true.
    * 'NONE': sends &amp;group=false.
    * 1-N: sends &amp;group_level=x to the view or summarizer. 1 says to group
    * on the first index of the array key, 2 says the first two indexes,
    * N all indexes (equivalent to 'EXACT').
    *
    * (the following should be done in logic rewrite rule?) TODO:
    * 'CALCULATE': typically set by the pushdown optimizer, instructs
    * this udx to best-guess what group level we can/should push down.
    * The basic idea is that if the columns in a GROUP BY statement belong
    * to objects defined as elements of the key array for the first key-value
    * pair returned by a view, we will push down the number of columns being
    * grouped by and ignore the grouping on the LucidDB end. Otherwise all
    * group by's will still be done by LucidDB.
    * @param resultInserter - Table for inserting results. Assumed to have the
    * necessary column names in the order we get them.
    */
   public static void query(
       String userName,
       String pw,
       String url,
       String view,
       String limit,
       boolean reduce,
       String groupLevel,
       boolean outputJson,
       PreparedStatement resultInserter) throws SQLException {
 
     // Specialize so we can column names for our resultInserter
     // instead of assuming an order.
     ParameterMetaData pmd = resultInserter.getParameterMetaData();
     FarragoParameterMetaData fpmd = (FarragoParameterMetaData) pmd;
     int paramCount = fpmd.getParameterCount();
     String[] paramNames = new String[paramCount];
     for (int i = 0 ; i < paramCount; i++) {
       paramNames[i] = fpmd.getFieldName(i+1); // JDBC offset
     }
 
     RowProducer producer = new RowProducer();
     JSONParser parser = new JSONParser();
 
     InputStreamReader in = getViewStream(userName, pw, url, view, limit, reduce, groupLevel, true);
 
     while (!producer.isDone()) {
       try {
         parser.parse(in, producer, true);
       } catch (Throwable e) { // IOException, ParseException
         throw new SQLException(e);
       }
 
       if (!producer.getKey().equals("key"))
         continue;
       Object key = producer.getValue();
 
       try {
         parser.parse(in, producer, true);
       } catch (Throwable e) { // IOException, ParseException
         throw new SQLException(e);
       }
 
       assert(producer.getKey().equals("value"));
       Object value = producer.getValue();
 
       if (outputJson) {
         // put key in first col, val in second col, escape.
         resultInserter.setString(1, key.toString());
         resultInserter.setString(2, value.toString());
         resultInserter.executeUpdate();
         continue;
       }
 
       Map<String, Object> params = new HashMap<String, Object>(paramNames.length);
       mergeParams(params, key, "KEY");
       mergeParams(params, value, "VALUE");
 
       if (params.size() != paramNames.length) {
         // We have more params than columns..
         throw new SQLException("Read " + params.size() + " params and "
             + paramNames.length + " columns, which need to match. Did you "
             + "add column(s) for both the key and value?");
       }
 
       for ( int c = 0 ; c < paramNames.length ; c++ ) {
         Object o = params.get( paramNames[c] );
         if ( o != null ) {
           resultInserter.setObject(c+1, o);
         }
       }
 
       resultInserter.executeUpdate();
     }
   }
   
   /**
    * Helper function gets the rows from the foreign CouchDB server and returns
    * them as a JSONArray.
    */
   private static InputStreamReader getViewStream(
       String user,
       String pw,
       String url,
       String view,
       String limit,
       boolean reduce,
       String groupLevel,
       boolean hasReducer)
     throws SQLException {
     String full_url = "";
     try {
       // TODO: stringbuffer this for efficiency
       full_url = makeUrl(url, view);
       String sep = (view.indexOf("?") == -1) ? "?" : "&";
       if (limit != null && limit.length() > 0) {
         full_url += sep + "limit=" + limit;
         sep = "&";
       }
 
       // These options only apply if a reducer function is present.
       if (hasReducer) {
         if (!reduce) {
           full_url += sep + "reduce=false";
           if (sep.equals("?")) sep = "&";
         }
         if (groupLevel.toUpperCase().equals("EXACT"))
           full_url += sep + "group=true";
         else if (groupLevel.toUpperCase().equals("NONE"))
           full_url += sep + "group=false";
         else
           full_url += sep + "group_level=" + groupLevel;
       }
 
       logger.log(Level.FINE, "Attempting CouchDB request with URL: " + full_url);
 
       URL u = new URL(full_url);
       HttpURLConnection uc = (HttpURLConnection) u.openConnection();
       if ( user != null && user.length() > 0 ) { 
     	  uc.setRequestProperty(
           "Authorization", "Basic " + buildAuthHeader(user, pw));
       }
       uc.connect();
       return new InputStreamReader(uc.getInputStream());
 
     } catch (MalformedURLException e) {
       throw new SQLException("Bad URL: " + full_url);
     } catch (IOException e) {
       if (hasReducer) { // try again but without the reduce args..
         try {
           return getViewStream(user, pw, url, view, limit, reduce, groupLevel, false);
         } catch (SQLException e2) { // No good.
 
         }
       }
       throw new SQLException("Could not read data from URL: " + full_url);
     }
   }
   
   /**
    * Concatenates a URL and a view together for the full CouchDB view.
    * If the string "_view" is not in either of the two, it will be
    * added automatically before the view parameter. (This is to allow for
    * ease-of-use in view_def creation.)
    */
   private static String makeUrl(String url, String view) {
     String full_url = url;
     if (url.charAt(url.length() - 1) != '/' && view.charAt(0) != '/')
       full_url += "/";
     if (url.indexOf("_view") == -1 &&
         view.indexOf("_view") == -1) {
       full_url += "_view";
       if (view.charAt(0) != '/')
         full_url += "/";
     }
     full_url += view;
     return full_url;
   }
 
   /**
    * Builds a standard basic authentication http header with the user and pass.
    */
   private static String buildAuthHeader (String username, String password) {
     String login = username + ":" + password;
     String encodedLogin = new String(Base64.encodeBase64(login.getBytes()));
     return encodedLogin;
   }
   
   /**
    * Reads the data from a http call into a string.
    */
   private static String readStringFromConnection (HttpURLConnection uc) throws IOException {
     InputStreamReader in = new InputStreamReader(uc.getInputStream());
     BufferedReader buff = new BufferedReader(in);
     StringBuffer sb = new StringBuffer();
     String line = null;
     do {
       line = buff.readLine();
       if ( line != null ) sb.append(line);
     } while ( line != null );
     
     return sb.toString();
   }
 
   /**
    * Creates a CouchDB view
    */
   public static void makeView(
       String user,
       String pw,
       String url,
       String viewDef) throws SQLException {
     try {
       URL u = new URL(url);
       HttpURLConnection uc = (HttpURLConnection) u.openConnection();
       uc.setDoOutput(true);
       if ( user != null && user.length() > 0 ) { 
         uc.setRequestProperty(
             "Authorization", "Basic " + buildAuthHeader(user, pw));
       }
       uc.setRequestProperty("Content-Type", "application/json");
       uc.setRequestMethod("PUT");
       OutputStreamWriter wr = new OutputStreamWriter(uc.getOutputStream());
       wr.write(viewDef);
       wr.close();
 
       String s = readStringFromConnection(uc);
     } catch (MalformedURLException e) {
       throw new SQLException("Bad URL.");
     } catch (IOException e) {
       throw new SQLException(e.getMessage());
     }
   }
   
   /**
    * This function merges three types of forms of key/values with
    * a passed in set of params for full-column coverage.
    *
    * @param params - eventual json dict
    * @param kv - key or value in key-value pair
    * @param literalKey - key prefix to use when kv is a literal obj instead
    * of a json array/object.
    */
   private static void mergeParams(
       Map<String, Object> params,
       Object kv,
       String literalKey)
   {
     // determine type of kv:
     if (kv instanceof JSONObject) {
       // merges {'x': v, 'y': v2, ...} with params.
       params.putAll((JSONObject)kv);
     } else if (kv instanceof JSONArray) {
       // merges each of [ {'x': v}, v2, ... ] with params.
       int i = 0;
       for (Object ob : (JSONArray) kv) {
         if (ob instanceof JSONObject) {
           params.putAll((JSONObject)ob);
         } else {
           params.put(literalKey + i, ob);
         }
         i++;
       }
     } else {
       // appends literal value with passed in key-name.
       params.put(literalKey + "0", kv);
     }
   }
 
   /**
    * This works with the assumption that the incoming data is of the form
    * {"rows": [{"key": ..., "value": ...,}, {"key": ..., "value": ...,}]}
    * Uses a simple 'height' variable to pause the parse when height=1 so
    * we can read the ("key": ...) and ("value": ...) items, with "value"
    * accessed through getKey() and ... for "value" accessed through getValue().
    *
    * This class made with the help of examples on JSON Simple page.
   * Methods returning "false" signify it's safe to pause the parser and read.
    */
   private static class RowProducer implements ContentHandler {
     private Stack valueStack;
     private int height;
     private boolean end;
     private boolean remove_self;
     private Object grabKey;
     private Object grabValue;
 
     public String getKey() {
       return grabKey.toString();
     }
 
     public Object getValue() {
       return grabValue;
     }
 
     public boolean isDone() {
       return end;
     }
 
     /**
      * Get final result if interested; note we will have forgotten rows-array
      * contents.
      */
     public Object getResult() {
       if (valueStack == null || valueStack.size() == 0)
         return null;
       return valueStack.peek();
     }
 
     private void trackBack() {
       if (valueStack.size() > 1) {
         Object value = valueStack.pop();
         Object prev = valueStack.peek();
         if (prev instanceof String) {
           valueStack.push(value);
         }
       }
     }
 
     private void consumeValue(Object value){
       if (valueStack.size() == 0) {
         valueStack.push(value);
       } else {
         Object prev = valueStack.peek();
         if (prev instanceof List) {
           List array = (List)prev;
           array.add(value);
         } else {
           valueStack.push(value);
         }
       }
     }
 
     public boolean primitive(Object value) throws ParseException, IOException {
       consumeValue(value);
       return true;
     }
 
     public boolean startArray() throws ParseException, IOException {
       List array = new JSONArray();
       consumeValue(array);
       valueStack.push(array);
       return true;
     }
 
     public boolean endArray() throws ParseException, IOException {
       trackBack();
       return true;
     }
 
     public boolean startObject() throws ParseException, IOException {
       Map object = new JSONObject();
       consumeValue(object);
       valueStack.push(object);
       return true;
     }
 
     public boolean endObject() throws ParseException, IOException {
       trackBack();
       return true;
     }
 
     public boolean startObjectEntry(String key)
       throws ParseException, IOException
     {
       height++;
       valueStack.push(key);
       return true;
     }
 
     public boolean endObjectEntry() throws ParseException, IOException {
       Object value = valueStack.pop();
       Object key = valueStack.pop();
       height--;
       // "Forget" about these actual objects by never pushing to the obj holder
       // and removing the empty map from the parent list.
      if (height == 1 && ("key".equals(key) || "value".equals(key)) ) {
         // kind of bad design here, but since we know we're pausing twice per
         // row in the array, we want to do this remove step on the second stop.
         if (remove_self) {
           Object holder = valueStack.pop();
           List parent = (List)valueStack.peek();
           parent.remove(holder);
           remove_self = false;
         } else {
           remove_self = true;
         }
 
         grabKey = key;
         grabValue = value;
         return false;
       }
       Map parent = (Map)valueStack.peek();
       parent.put(key, value);
       return true;
     }
 
     public void startJSON() throws ParseException, IOException {
       valueStack = new Stack();
       height = 0;
       end = false;
       remove_self = false;
     }
 
     public void endJSON() throws ParseException, IOException {
       end = true;
     }
   }
 
 }
