 /*
  * This program is free software; you can redistribute it and/or modify it under the 
  * terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software 
  * Foundation.
  *
  * You should have received a copy of the GNU Lesser General Public License along with this 
  * program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html 
  * or from the Free Software Foundation, Inc., 
  * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
  *
  * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
  * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
  * See the GNU Lesser General Public License for more details.
  *
  * Copyright 2011 Pentaho Corporation.  All rights reserved.
  *
  * Created Set 20th, 2012
  * @author Pedro Vale (pedro.vale@webdetails.pt)
  */
 package org.pentaho.telemetry;
 
import java.io.EOFException;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FilenameFilter;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.net.HttpURLConnection;
 
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import org.apache.commons.httpclient.HttpClient;
 import org.apache.commons.httpclient.URI;
 import org.apache.commons.httpclient.methods.PostMethod;
 import org.apache.commons.httpclient.methods.StringRequestEntity;
 import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
 import org.apache.commons.httpclient.methods.multipart.Part;
 import org.apache.commons.httpclient.methods.multipart.StringPart;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 /**
  * Used by TelemetryHelper to manage sending the telemetry requests to a server
  * @author pedrovale
  */
 public class TelemetryEventSender implements Runnable {
 
   private static final int DAYS_TO_KEEP_FILES = 5;
   private static final int BLOCK_SIZE = 50;
   
   private static final Log logger = LogFactory.getLog(TelemetryEventSender.class);
 
   protected static PostMethod defaultHttpMethod;
   protected static HttpClient defaultHttpClient;
   protected File lastSubmissionDir;
   private File telemetryDir;
 
   
   public TelemetryEventSender(File lastSubmissionDir, File telemetryDir) {
     this.lastSubmissionDir = lastSubmissionDir;
     this.telemetryDir = telemetryDir;
   }
   
   
   protected static HttpClient getHttpClient() {
     return defaultHttpClient != null ? defaultHttpClient : new HttpClient();
   }
 
   protected static PostMethod getHttpMethod() {
     return defaultHttpMethod != null ? defaultHttpMethod : new PostMethod();
   }
 
   /***
    * Given an array of telemetry event files, parses them, builds a JSON array
    * with all the events and dispatches them to one or more urls. Deletes files
    * if request was successful.
    * @param blockToSend Array of files with telemetry events to send to the server
    */
   protected void sendRequest(File[] blockToSend) {
 
     String baseUrl = null;
     HashMap<String, StringBuffer> urlsAndPostData = new HashMap<String, StringBuffer>();
     HashMap<String, List<File>> urlsAndFiles = new HashMap<String, List<File>>();
     for (File f : blockToSend) {
       if (f == null)
         break;
       
       try {
         FileInputStream fin = new FileInputStream(f);
         ObjectInputStream ois = new ObjectInputStream(fin);
         TelemetryEvent event = (TelemetryEvent) ois.readObject();
         ois.close();
 
         StringBuffer postData = urlsAndPostData.get(event.getUrlToCall());
         
         if (postData == null) {
           postData = new StringBuffer().append("[");
         } else
           postData.append(", ");
         
         
         postData.append(event.encodeEvent());
         
         urlsAndPostData.put(event.getUrlToCall(), postData);
         List<File> filesForThisUrl = urlsAndFiles.get(event.getUrlToCall());
         if (filesForThisUrl == null)
             filesForThisUrl = new ArrayList<File>();
         filesForThisUrl.add(f);
         urlsAndFiles.put(event.getUrlToCall(), filesForThisUrl);
      } catch (EOFException eofe) {
        logger.warn("EOF caught while deserializing telemetry event. Probably a corrupt save. Deleting event.", eofe);
        f.delete();        
       } catch (IOException ioe) {
         logger.error("Error caught while deserializing telemetry event.", ioe);
       } catch (ClassNotFoundException cnfe) {
         logger.error("Class not found while deserializing telemetry event.", cnfe);
       }
     }
 
     
     Iterator<String> urlIterator = urlsAndPostData.keySet().iterator();
     while (urlIterator.hasNext()) {
       String url = urlIterator.next();
       StringBuffer postData = urlsAndPostData.get(url);
       postData.append("]");
       postData.append(System.getProperty("line.separator"));
       boolean success = true;
       
       try {                  
         
         final HttpClient httpClient = getHttpClient();
         final PostMethod httpMethod = getHttpMethod();
         
         
         int timeout = 30000;
 
         httpClient.getHttpConnectionManager().getParams().setSoTimeout(timeout);        
         httpMethod.setURI(new URI(url, true));
       
         Part[] parts = new Part[]{new StringPart("body", postData.toString())};
       
          httpMethod.setRequestEntity(new StringRequestEntity(postData.toString(), "application/json", "UTF8"));
         logger.info("Calling " + url);
         logger.info("Data: " + postData.toString());
 
         // Execute the request
         final int resultCode = httpClient.executeMethod(httpMethod);
         if (resultCode != HttpURLConnection.HTTP_OK) {
           logger.error("Invalid Result Code Returned: " + resultCode);
           success = false;
         } else {
           String resultXml = httpMethod.getResponseBodyAsString();
           //TO DO: Improve error detection
           if (resultXml.indexOf("<result>OK</result>") < 0) {
             logger.warn("Telemetry request had unexpected result: " + resultXml + ".");
             success = false;
           }
         }
 
         // Clean up
         httpMethod.releaseConnection();
 
       } catch (Exception e) {
         logger.warn("Exception caught while making telemetry request.", e);
         success = false;
       }         
       
       //Clear files
       if (success) {
         for (File f : blockToSend) {
           if (f != null) {
             File newFile = new File(lastSubmissionDir, f.getName());
             f.renameTo(newFile);
             f.delete();
           }
         }        
       }
       
       
     }
     
 
   }
 
   @Override
   public void run() {
     //Delete everything in lastSubmission folder
     File[] submittedFiles = lastSubmissionDir.listFiles();
     for (File f : submittedFiles) {
       f.delete();
     }
 
     //Get all requests in telemetryPath
     File[] unsubmittedRequests = telemetryDir.listFiles(new FilenameFilter() {
 
       @Override
       public boolean accept(File file, String name) {
         return name.endsWith(".tel");
       }
     });
 
 
     File[] block = new File[BLOCK_SIZE];
     int blockIndex = 0;
     Calendar cld = Calendar.getInstance();
     cld.add(Calendar.DAY_OF_YEAR, -DAYS_TO_KEEP_FILES);
     for (File f : unsubmittedRequests) {
       //Check if file was created more than 5 days ago. If so, dismiss it
       if (f.lastModified() < cld.getTime().getTime()) {
         f.delete();
         continue;
       }
       
       //Create blocks of BLOCK_SIZE
       if (blockIndex > 0 && blockIndex % BLOCK_SIZE == 0) {
         sendRequest(block);
         block = new File[BLOCK_SIZE];
         blockIndex = 0;
       } else {
         block[blockIndex] = f;
         blockIndex++;
       }
     }
 
     if (blockIndex > 0) {
       sendRequest(block);
     }
 
   }
 
 }
