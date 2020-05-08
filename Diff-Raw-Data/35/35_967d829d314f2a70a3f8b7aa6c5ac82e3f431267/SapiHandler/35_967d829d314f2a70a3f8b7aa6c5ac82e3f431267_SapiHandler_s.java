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
 package com.funambol.sapisync.sapi;
 
 import java.util.Vector;
 import java.util.Hashtable;
 import java.util.Enumeration;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.ByteArrayInputStream;
 import java.io.OutputStream;
 
 import org.json.me.JSONObject;
 import org.json.me.JSONException;
 
 import com.funambol.platform.HttpConnectionAdapter;
 import com.funambol.util.Base64;
 import com.funambol.util.StringUtil;
 import com.funambol.util.ConnectionManager;
 import com.funambol.util.Log;
 
 /**
  * This class is a utility to perform SAPI requests. It provides some basic
  * mechanism to authentication, and url encoding.
  */
 public class SapiHandler {
 
     private static final String TAG_LOG = "SapiHandler";
 
     public static final int AUTH_NONE = -1;
     public static final int AUTH_IN_QUERY_STRING = 0;
     public static final int AUTH_IN_HTTP_HEADER  = 1;
 
     private static final String JSESSIONID_PARAM   = "jsessionid";
     private static final String ACTION_PARAM       = "action";
 
     private static final String AUTH_HEADER           = "Authorization";
     private static final String AUTH_BASIC            = "Basic";
     private static final String CONTENT_TYPE_HEADER   = "Content-Type";
     private static final String CONTENT_LENGTH_HEADER = "Content-Length";
 
     private static final String JSESSIONID_HEADER = "JSESSIONID";
     private static final String COOKIE_HEADER     = "Set-Cookie";
 
     private static final int DEFAULT_CHUNK_SIZE = 4096;
 
     private String baseUrl;
     private String user;
     private String pwd;
 
     private int authMethod = AUTH_IN_QUERY_STRING;
     private boolean jsessionAuthEnabled = false;
 
     private String  jsessionId = null;
     protected ConnectionManager connectionManager = ConnectionManager.getInstance();
 
     private SapiQueryListener listener = null;
 
     /**
      * This is the flag used to indicate that the current query shall be
      * cancelled
      */
     private boolean cancel = false;
 
     public SapiHandler(String baseUrl, String user, String pwd) {
         this.baseUrl = baseUrl;
         this.user    = user;
         this.pwd     = pwd;
     }
 
     public SapiHandler(String baseUrl) {
         this(baseUrl, null, null);
         setAuthenticationMethod(AUTH_NONE);
     }
 
     public void setAuthenticationMethod(int authMethod) {
         this.authMethod = authMethod;
     }
 
     public void enableJSessionAuthentication(boolean value) {
         this.jsessionAuthEnabled = value;
     }
 
     public void forceJSessionId(String jsessionId) {
         this.jsessionId = jsessionId;
     }
 
     public void setConnectionManager(ConnectionManager connectionManager) {
         this.connectionManager = connectionManager;
     }
 
     public void setSapiRequestListener(SapiQueryListener listener) {
         this.listener = listener;
     }
 
     public JSONObject query(String name, String action, Vector params,
             Hashtable headers, JSONObject request)
             throws IOException, JSONException {
 
         ByteArrayInputStream s = null;
         int contentLength = 0;
         if (request != null) {
             byte[] r = request.toString().getBytes("UTF-8");
             contentLength = r.length;
             s = new ByteArrayInputStream(r);
         }
         return query(name, action, params, headers, s, contentLength, null);
     }
 
     public JSONObject query(String name, String action, Vector params,
             Hashtable headers, InputStream requestIs, long contentLength, 
             String testItemName) throws IOException, JSONException {
 
         return query(name, action, params, headers, requestIs,
                "application/octet-stream", contentLength, testItemName);
     }
     public synchronized JSONObject query(String name, String action, Vector params,
             Hashtable headers, InputStream requestIs, String contentType,
            long contentLength, String testItemName) throws IOException, JSONException {
         
         String url = createUrl(name, action, params);
         HttpConnectionAdapter conn;
         
         try {
             // Open the connection with a given size to prevent the output
             // stream from buffering all data
             if (Log.isLoggable(Log.INFO)) {
                 Log.info(TAG_LOG, "Requesting url: " + url);
             }
             String extras = null;
             if(testItemName != null) {
                 extras = "key," + testItemName + ",phase,sending";
             }
             conn = connectionManager.openHttpConnection(url, extras);
             conn.setRequestMethod(HttpConnectionAdapter.POST);
             if(contentLength > 0) {
                 if (Log.isLoggable(Log.DEBUG)) {
                     Log.debug(TAG_LOG, "Setting content type to: " + contentType);
                 }
                 conn.setRequestProperty(CONTENT_TYPE_HEADER, contentType);
             }
             if (Log.isLoggable(Log.DEBUG)) {
                Log.debug(TAG_LOG, "Setting content length to " + contentLength);
             }
            conn.setRequestProperty(CONTENT_LENGTH_HEADER, String.valueOf(contentLength));
 
             // Set the authentication if we have no jsessionid
             if (jsessionId != null && jsessionAuthEnabled) {
                 if (Log.isLoggable(Log.DEBUG)) {
                     Log.debug(TAG_LOG, "Authorization is specified via jsessionid");
                 }
                 conn.setRequestProperty("Cookie", JSESSIONID_HEADER + "=" + jsessionId);
             } else if (authMethod == AUTH_IN_HTTP_HEADER) {
                 String token = user + ":" + pwd;
                 String authToken = new String(Base64.encode(token.getBytes()));
 
                 String authParam = AUTH_BASIC + " " + authToken;
                 if (Log.isLoggable(Log.DEBUG)) {
                     Log.debug(TAG_LOG, "Setting auth header to: " + authParam);
                 }
                 conn.setRequestProperty(AUTH_HEADER, authParam);
             }
 
             // Add custom headers
             if (headers != null) {
                 Enumeration keys = headers.keys();
                 while(keys.hasMoreElements()) {
                     String key = (String)keys.nextElement();
                     String value = (String)headers.get(key);
                     conn.setRequestProperty(key, value);
                 }
             }
         } catch (IOException ioe) {
             Log.error(TAG_LOG, "Cannot open http connection", ioe);
             throw ioe;
         }
 
         // Set chunked streaming mode in order to avoid buffering
         conn.setChunkedStreamingMode(DEFAULT_CHUNK_SIZE);
 
         OutputStream os = null;
         InputStream  is = null;
 
         if(listener != null) {
             listener.queryStarted((int)contentLength);
         }
         try {
             os = conn.openOutputStream();
             // In case of SAPI that require a body, this must be written here
             // Note that the length is not handled here because we don't know
             // the length of the stream. Callers shall put it in the custom
             // headers if it is required.
             if (requestIs != null) {
                 int total = 0;
                 int read  = 0;
                 byte chunk[] = new byte[DEFAULT_CHUNK_SIZE];
                 do {
                     read = requestIs.read(chunk);
                     if (read > 0) {
                         if (Log.isLoggable(Log.TRACE)) {
                             Log.trace(TAG_LOG, "Writing chunk size: " + read);
                         }
                         total += read;
                         os.write(chunk, 0, read);
                         if(listener != null) {
                             listener.queryProgress(total);
                         }
                     }
                 } while(read != -1 && !isQueryCancelled());
             }
             os.flush();
 
             if (Log.isLoggable(Log.TRACE)) {
                 Log.trace(TAG_LOG, "Response is: " + conn.getResponseCode());
             }
 
             // Now check the HTTP response, in case of success we set the item
             // status to OK
             StringBuffer response = new StringBuffer();
             if (conn.getResponseCode() == HttpConnectionAdapter.HTTP_OK) {
                 // Open the input stream and read the response
                 is = conn.openInputStream();
                 // Read until we have data
                 int responseLength = conn.getLength();
                 if(responseLength > 0) {
                     if (Log.isLoggable(Log.TRACE)) {
                         Log.trace(TAG_LOG, "response length is known " + responseLength);
                     }
                     // Read the input stream according to the response
                     // content-length header
                     int b;
                     do {
                         b = is.read();
                         responseLength--;
                         if (b != -1) {
                             response.append((char)b);
                         }
                     } while(b != -1 && responseLength > 0);
 
                     if (responseLength > 0) {
                         Log.error(TAG_LOG, "Content length mismatch");
                     }
 
                 } else if(responseLength < 0) {
                     if (Log.isLoggable(Log.TRACE)) {
                         Log.trace(TAG_LOG, "response length is unknown (probably chunked encoding)");
                     }
                     try {
                         int b;
                         do {
                             b = is.read();
                             if (b != -1) {
                                 response.append((char)b);
                             }
                         } while(b != -1);
                     } catch(IOException ex) {
                         // The end of the stream is reached, ignore exception
                     }
                 }
             } else {
                 // The request failed
                 Log.error(TAG_LOG, "SAPI query error: " + conn.getResponseCode());
                 throw new IOException("HTTP error code: " + conn.getResponseCode());
             }
 
             String r = response.toString();
             if (Log.isLoggable(Log.TRACE)) {
                 Log.trace(TAG_LOG, "response is:" + r);
             }
 
             // This code handles JSESSION ID authentication
             try {
                 String cookies = conn.getHeaderField(COOKIE_HEADER);
                 if (cookies != null) {
                     int jsidx = cookies.indexOf(JSESSIONID_HEADER);
                     if (jsidx >= 0) {
                         jsessionId = cookies.substring(jsidx);
                         int idx = jsessionId.indexOf(";");
                         if (idx >= 0) {
                             if (Log.isLoggable(Log.DEBUG)) {
                                 Log.debug(TAG_LOG, "Found jsessionid = " + jsessionId);
                             }
                             jsessionId = jsessionId.substring(0, idx);
                         }
                     }
                 }
             } catch (Exception e) {
                 Log.error(TAG_LOG, "Cannot get jsessionid", e);
             }
 
             if(listener != null) {
                 listener.queryEnded();
             }
             // Prepare the response
             if(!StringUtil.isNullOrEmpty(r)) {
                 return new JSONObject(r);
             } else {
                 return null;
             }
         } catch (IOException ioe) {
             // If we get a non authorized error and we used a jsession id, then
             // we invalidate the jsessionId and throw the exception
             Log.error(TAG_LOG, "Error while performing SAPI", ioe);
 
             if (conn != null) {
                 try {
                     if (jsessionId != null && conn.getResponseCode() ==
                             HttpConnectionAdapter.HTTP_FORBIDDEN) {
                         if (Log.isLoggable(Log.INFO)) {
                             Log.info(TAG_LOG, "Invalidating jsession id");
                         }
                         jsessionId = null;
                     }
                 } catch (IOException ioe2) {}
             }
             throw ioe;
         } finally {
             // Release all resources
             if (os != null) {
                 try {
                     os.close();
                 } catch (IOException e) {}
                 os = null;
             }
             if (is != null) {
                 try {
                     is.close();
                 } catch (IOException e) {}
                 is = null;
             }
             if (requestIs != null) {
                 try {
                     requestIs.close();
                 } catch (IOException e) {}
                 requestIs = null;
             }
             if (conn != null) {
                 try {
                     conn.close();
                 } catch (IOException ioe) {}
                 conn = null;
             }
         }
     }
 
     public long getMediaPartialUploadLength(String name, String guid, long size) throws IOException {
 
         String url = createUrl("upload/" + name, "add", null);
         HttpConnectionAdapter conn = null;
 
         OutputStream os = null;
         
         try {
             // Open the connection with a given size to prevent the output
             // stream from buffering all data
             if (Log.isLoggable(Log.INFO)) {
                 Log.info(TAG_LOG, "Requesting url: " + url);
             }
             conn = connectionManager.openHttpConnection(url, null);
             conn.setRequestMethod(HttpConnectionAdapter.POST);
             conn.setRequestProperty(CONTENT_LENGTH_HEADER, "0");
 
             // Set the authentication if we have no jsessionid
             if (jsessionId != null && jsessionAuthEnabled) {
                 if (Log.isLoggable(Log.DEBUG)) {
                     Log.debug(TAG_LOG, "Authorization is specified via jsessionid");
                 }
                 conn.setRequestProperty("Cookie", JSESSIONID_HEADER + "=" + jsessionId);
             } else if (authMethod == AUTH_IN_HTTP_HEADER) {
                 String token = user + ":" + pwd;
                 String authToken = new String(Base64.encode(token.getBytes()));
 
                 String authParam = AUTH_BASIC + " " + authToken;
                 if (Log.isLoggable(Log.DEBUG)) {
                     Log.debug(TAG_LOG, "Setting auth header to: " + authParam);
                 }
                 conn.setRequestProperty(AUTH_HEADER, authParam);
             }
 
             conn.setRequestProperty("x-funambol-id", guid);
             conn.setRequestProperty("x-funambol-file-size", Long.toString(size));
 
             // Ask for the current length
             conn.setRequestProperty("Content-Range", "bytes */" + size);
 
             os = conn.openOutputStream();
             os.flush();
 
             // The answer can be either HTTP_OK or HTTP_INCOMPLETE (308)
             if (conn.getResponseCode() == HttpConnectionAdapter.HTTP_OK) {
                 // We have uploaded the item completely
                 return size;
             } else if (conn.getResponseCode() == 308 /* Resume Incomplete */) {
                 String length = conn.getHeaderField("Range");
                 if (length == null) {
                     Log.error(TAG_LOG, "Server did not return a valid range");
                     return 0;
                 }
                 // The range is expected as 0-length
                 int minusIdx = length.indexOf("-");
                 if (minusIdx == -1) {
                     Log.error(TAG_LOG, "Server returned a range in unknown format " + length);
                     return 0;
                 }
                 length = length.substring(minusIdx+1).trim();
                 try {
                     long res = Long.parseLong(length);
                     return res;
                 } catch (Exception e) {
                     Log.error(TAG_LOG, "Server returned a range which is not an integer value " + length);
                     return 0;
                 }
             } else {
                 Log.error(TAG_LOG, "Range request failed with HTTP code " + conn.getResponseCode());
                 return 0;
             }
         } catch (IOException ioe) {
             Log.error(TAG_LOG, "Cannot open http connection", ioe);
             throw ioe;
         } finally {
             if (os != null) {
                 os.close();
             }
             if (conn != null) {
                 conn.close();
             }
         }
     }
 
     /**
      * Cancels the current query
      */
     public void cancel() {
         if (Log.isLoggable(Log.DEBUG)) {
             Log.debug(TAG_LOG, "Cancelling current query");
         }
         cancel = true;
     }
 
     private boolean isQueryCancelled() {
         return cancel;
     }
 
     protected String encodeURLString(String s) {
         if (s != null) {
             StringBuffer tmp = new StringBuffer();
             try {
                 for(int i=0;i<s.length();++i) {
                     int b = (int)s.charAt(i);
                     if ((b>=0x30 && b<=0x39) || (b>=0x41 && b<=0x5A) || (b>=0x61 && b<=0x7A)) {
                         tmp.append((char)b);
                     } else if (b == 32) {
                         tmp.append("+");
                     } else {
                         tmp.append("%");
                         if (b <= 0xf) tmp.append("0");
                         tmp.append(Integer.toHexString(b));
                     }
                 }
             } catch (Exception e) {
                 Log.error(TAG_LOG, "Cannot encode URL " + s, e);
             }
             return tmp.toString();
         }
         return null;
     }
 
 
     protected String createUrl(String name, String action, Vector params) {
         // Prepare the URL
         StringBuffer url = new StringBuffer(StringUtil.extractAddressFromUrl(baseUrl));
         url.append("/").append("sapi/").append(name /* no need to encode the SAPI name */);
 
         if (jsessionId != null && jsessionAuthEnabled) {
             if (Log.isLoggable(Log.DEBUG)) {
                 Log.debug(TAG_LOG, "Authorization is specified via jsessionid");
             }
             url.append(";jsessionid=").append(jsessionId);
         }
 
         // Append the Params
         url.append("?").append(ACTION_PARAM).append("=").append(encodeURLString(action));
         // Credentials in the query string
         if (authMethod == AUTH_IN_QUERY_STRING) {
             url.append("&login=").append(encodeURLString(user))
                .append("&password=").append(encodeURLString(pwd));
         }
         if (params != null) {
             for(int i=0;i<params.size();++i) {
                 String param = (String)params.elementAt(i);
                 int eqIndex = param.indexOf('=');
                 if(eqIndex > 0) {
                     String paramName  = param.substring(0, eqIndex);
                     String paramValue = param.substring(eqIndex + 1);
                     url.append("&").append(encodeURLString(paramName))
                        .append("=").append(encodeURLString(paramValue));
                 } else {
                     url.append("&").append(encodeURLString(param));
                 }
             }
         }
         return url.toString();
     }
 
     /**
      * Used to monitor a SAPI query
      */
     public interface SapiQueryListener {
 
         /**
          * Reports that a new query operation started.
          * @param totalSize the total size of bytes to send
          */
         public void queryStarted(int totalSize);
         
         /**
          * Reports the progress of a query operation.
          * @param size the total number of bytes sent from the beginning
          */
         public void queryProgress(int size);
 
         /**
          * Reports that a query operation ended.
          */
         public void queryEnded();
         
     }
 
 }
