 /*
  Copyright (c) 2011 Bit Bunker
 
 Permission is hereby granted, free of charge, to any person obtaining a copy of
 this software and associated documentation files (the "Software"), to deal in
 the Software without restriction, including without limitation the rights to
 use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
 of the Software, and to permit persons to whom the Software is furnished to do
 so, subject to the following conditions:
 
 The above copyright notice and this permission notice shall be included in all
 copies or substantial portions of the Software.
 
 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 SOFTWARE.
  */
 package com.bitbunker.api;
 
 import com.bitbunker.BitBunkerException;
 import com.bitbunker.FileProperties;
 import com.bitbunker.Folder;
 import com.bitbunker.BitBunkerObject;
 import com.bitbunker.File;
 import com.bitbunker.xml.XmlException;
 import com.bitbunker.VersionedComponent;
 import com.bitbunker.util.Checksum;
 import com.bitbunker.xml.Utils;
 import java.io.BufferedReader;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.OutputStream;
 import java.net.MalformedURLException;
 import java.security.NoSuchAlgorithmException;
 import java.util.List;
 import java.net.URL;
 import java.net.URLConnection;
 import java.security.MessageDigest;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.SortedMap;
 import java.util.TimeZone;
 import java.util.TreeMap;
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 import org.w3c.dom.Document;
 import org.xml.sax.SAXException;
 import java.net.HttpURLConnection;
 
 /**
  * Client is the primary class used to interact with the BitBunker cloud storage
  * system.
  *
  * @author Chris Umbel
  * @version     %I%, %G%
  * 
  */
 @VersionedComponent(version="0.1.0", releaseDate="05 Mar 2011")
 public class Client {
     private URL urlBase;
     private String accessKey;
     private String secretKey;
 
     public static final class BBHeaders {
         public static final String DURABILITY = "x-bb-durability";
         public static final String CALLBACK = "x-bb-callback";
         public static final String GET_FROM = "x-bb-get-from";
         public static final String SEND_TO = "x-bb-send-to";
         public static final String DATE = "x-bb-date";
         public static final String CHECKSUM = "x-bb-checksum";
     }
 
     /**
      * basic constructor the BitBunker client using the default service URL.
      *
      * @param accessKey      access key provided by BitBunker for authentication
      * @param secretKey      secret key provided by BitBunker for authentication
      */
     public Client(String accessKey, String secretKey) throws BitBunkerException {
         this.accessKey = accessKey;
         this.secretKey = secretKey;
 
         try {
             this.urlBase = new URL("https://storage.api.bitbunker.com");
         } catch (MalformedURLException ex) {
             throw new NetworkException(ex);
         }
     }
 
     /**
      * constructor the BitBunker client using a custom service URL
      *
      * @param accessKey      access key provided by BitBunker for authentication
      * @param secretKey      secret key provided by BitBunker for authentication
      * @param urlBase        BitBunker service URL
      *
      */
     public Client(String accessKey, String secretKey, URL urlBase) throws BitBunkerException {
         this(accessKey, secretKey);
         this.urlBase = urlBase;
     }
 
     private String getUserAgent() {
         try {
             String version = this.getClass().getAnnotation(VersionedComponent.class).version();
         
             return String.format("%s/%s", this.getClass().getProtectionDomain().getCodeSource().getLocation().toURI(), version);
         } catch (Exception ex) {
             return String.format("%s/%s", this.getClass().toString(), 0);
         }
     }
 
     private String sign(Request request, Date reqDateTime) throws BitBunkerException {
         StringBuilder hashBuilder = new StringBuilder();
 
         try {
             Method method = request.getClass().getAnnotation(Method.class);
             StringBuilder plainTextBuilder = new StringBuilder(String.format("%s %s\n%s\n", method.value(), request.getHTTPPath(), secretKey));
             Iterator it = request.getBbHeaders().entrySet().iterator();
 
             while(it.hasNext()) {
                 Map.Entry<String, String> header = (Map.Entry<String, String>)it.next();
                 plainTextBuilder.append(String.format("%s: %s", header.getKey(), header.getValue()));
 
                 if(it.hasNext())
                     plainTextBuilder.append("\n");
             }
             
             MessageDigest md = MessageDigest.getInstance("SHA-256");
             md.update(plainTextBuilder.toString().getBytes());
             
             byte[] digest = md.digest();
 
             // convert the hash to an ascii hex string            
             for (int i = 0; i < digest.length; i++) {
                 hashBuilder.append(Integer.toString((digest[i] & 0xff) + 0x100, 16).substring(1));
             }
         } catch (NoSuchAlgorithmException ex) {
             // catestrophic crypto failure
             throw new SecurityException(ex);
         }
 
         return hashBuilder.toString();
     }
 
     private void addBBHeaders(Request request, URLConnection conn) {
         Iterator it = request.getBbHeaders().entrySet().iterator();
 
         while(it.hasNext()) {
             Map.Entry<String, String> header = (Map.Entry<String, String>)it.next();
             conn.addRequestProperty(header.getKey(), header.getValue());
         }   
     }
 
     private String streamToString(InputStream is) throws NetworkException {
         StringBuilder message = new StringBuilder();
 
          try {
             BufferedReader rdr = new BufferedReader(new InputStreamReader(is));
             String line;
 
             while ((line = rdr.readLine()) != null) {
                 message.append(line);
             }
         } catch (IOException ex1) {
             throw new NetworkException(ex1);
         }
 
         return message.toString();
     }
     
     private String extractErrorMessage(HttpURLConnection conn) throws NetworkException {
         String message = null;
 
        if (conn != null) {
             message = streamToString(conn.getErrorStream());
         }
 
         return message;
     }
 
     private HttpURLConnection openURL(Request request) throws IOException, SecurityException, BitBunkerException {
         URL url = new URL(urlBase, request.getHTTPPath());
 
         Date now = new Date();
         URLConnection conn = url.openConnection();
         conn.setConnectTimeout(0);
         conn.setReadTimeout(0);
 
         conn.addRequestProperty("User-Agent", getUserAgent());
 
         DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
         dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
         request.getBbHeaders().put(BBHeaders.DATE, dateFormat.format(now));
         addBBHeaders(request, conn);
 
         conn.addRequestProperty("Authorization", String.format("%s:%s", accessKey, sign(request, now)));
 
         return (HttpURLConnection)conn;
     }
     
     private InputStream executeStream(Request request) throws BitBunkerException {
         return executeStream(request, null, -1);
     }
 
     
     private InputStream executeStream(Request request, InputStream is, long length) throws BitBunkerException {
         request.setClient(this);
         HttpURLConnection conn = null;
 
         try {
             conn = openURL(request);
             
             if(length > 0)
                 conn.setFixedLengthStreamingMode((int)length);
 
             try {
                 conn.setRequestMethod(request.getClass().getAnnotation(Method.class).value());
 
                 if(is != null) {
                     conn.setDoOutput(true);
                     conn.addRequestProperty("content-type", "application/octet-stream");
                     OutputStream os = conn.getOutputStream();
 
                     int len = 0;
                     byte[] buff = new byte[16384];
 
                     while((len = is.read(buff)) > 0) {
                         os.write(buff, 0, len);
                     }
 
                     os.flush();
                     os.close();
                 }
 
                 return conn.getInputStream();
             } catch (java.io.FileNotFoundException ex) {
                 throw new com.bitbunker.api.FileNotFoundException(ex);
             }
         } catch (IOException ex) {
             if(ex.getMessage().startsWith("Server returned HTTP response code: 403"))
                throw new SecurityException(ex);
             else {
                 String message = extractErrorMessage(conn);
                 throw new NetworkException(message, ex);
             }
         }
     }
 
     private Response executeXML(Request request) throws BitBunkerException {
         return executeXML(request, null, -1);
     }
 
     private Response executeXML(Request request, InputStream is, long length) throws BitBunkerException {
         Response response = null;
     
         try {
             DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
             domFactory.setNamespaceAware(true);
             DocumentBuilder builder = domFactory.newDocumentBuilder();
             InputStream stream = executeStream(request, is, length);
             Document doc = builder.parse(stream);
             response = request.createResponse(doc);
             stream.close();
         } catch (SAXException ex) {
             // invalid xml
             throw new XmlException(ex);
         } catch (ParserConfigurationException ex) {
             // lower level xml error
             throw new XmlException(ex);
         } catch (IOException ex) {
             // network error
             throw new NetworkException(ex);
         }
 
         return response;
     }
 
     /**
      * puts a local file into BitBunker
      *
      * @param localFile     the file on the local system
      * @param path          the path within BitBunker to put the file
      */
     public void put(java.io.File localFile, String path) throws BitBunkerException {
         put(localFile, new File(path));
     }
 
     /**
      * puts a local file into BitBunker
      *
      * @param localFile     the file on the local system
      * @param path          the path within BitBunker to put the file
      * @param durability    total number of backups to store
      */
     public void put(java.io.File localFile, String path, int durability) throws BitBunkerException {
         put(localFile, new File(path), durability);
     }
 
     /**
      * puts a stream into BitBunker
      *
      * @param is            the stream of data to put into BitBunker
      * @param path          the path within BitBunker to put the file
      */
     public void put(InputStream is, String path) throws BitBunkerException {
         put(is, new File(path));
     }
 
     /**
      * puts a stream into BitBunker
      *
      * @param is            the stream of data to put into BitBunker
      * @param path          the path within BitBunker to put the file
      * @param durability    total number of backups to store
      */
     public void put(InputStream is, String path, int durability) throws BitBunkerException {
         put(is, new File(path), durability);
     }    
 
     /**
      * puts a local file into BitBunker
      *
      * @param localPath     the file name of the local file
      * @param path          the path within BitBunker to put the file
      */
     public void put(String localPath, String path) throws BitBunkerException {
         put(localPath, new File(path));
     }
 
     /**
      * puts a local file into BitBunker
      *
      * @param localPath     the file name of the local file
      * @param path          the path within BitBunker to put the file
      * @param durability    total number of backups to store
      */
     public void put(String localPath, String path, int durability) throws BitBunkerException {
         put(localPath, new File(path), durability);
     }
     
     /**
      * puts a stream into BitBunker
      *
      * @param is            the stream of data to put into BitBunker
      * @param file          the target BitBunker file
      */
     public void put(InputStream is, File file) throws BitBunkerException {
         put(is, file, new TreeMap<String, String>());
     }
 
     /**
      * puts a stream into BitBunker
      *
      * @param is            the stream of data to put into BitBunker
      * @param file          the target BitBunker file
      * @param durability    total number of backups to store
      */
     public void put(InputStream is, File file, int durability) throws BitBunkerException {
         put(is, file, durability);
     }    
 
     /**
      * puts a local file into BitBunker
      *
      * @param localPath     the file name of the local file
      * @param file          the target BitBunker file
      */
     public void put(String localPath, File file) throws BitBunkerException {
         put(new java.io.File(localPath), file);
     }
 
     /**
      * puts a local file into BitBunker
      *
      * @param localPath     the file name of the local file
      * @param file          the target BitBunker file
      * @param durability    total number of backups to store
      */
     public void put(String localPath, File file, int durability) throws BitBunkerException {
         put(new java.io.File(localPath), file, durability);
     }
 
     /**
      * puts a local file into BitBunker
      *
      * @param localFile     the file on the local system
      * @param file          the target BitBunker file
      */
     public void put(java.io.File localFile, File file) throws BitBunkerException {
         try {
             put(new FileInputStream(localFile), file);
         } catch (java.io.FileNotFoundException ex) {
             throw new com.bitbunker.api.FileNotFoundException(ex);
         }
     }
 
     /**
      * puts a local file into BitBunker
      *
      * @param localFile     the file on the local system
      * @param file          the target BitBunker file
      * @param durability    total number of backups to store
      */
     public void put(java.io.File localFile, File file, int durability) throws BitBunkerException {
         SortedMap<String, String> headers = new TreeMap<String, String>();
         headers.put(BBHeaders.DURABILITY, Integer.toString(durability));
         headers.put(BBHeaders.CHECKSUM, Utils.getHexString(Checksum.checksum(localFile)));
 
         try {
             put(new FileInputStream(localFile), file, headers, localFile.length());
         } catch (java.io.FileNotFoundException ex) {
             throw new com.bitbunker.api.FileNotFoundException(ex);
         }
     }
 
     /**
      * puts a stream into BitBunker
      *
      * @param is            the stream of data to put into BitBunker
      * @param file          the target BitBunker file
      * @param headers       map of extra BB headers to add to the request
      */
     public void put(InputStream is, File file, SortedMap<String, String> headers) throws BitBunkerException {
         put(is, file, headers, -1);
     }
 
     /**
      * puts a stream into BitBunker
      *
      * @param is            the stream of data to put into BitBunker
      * @param file          the target BitBunker file
      * @param headers       map of extra BB headers to add to the request
      */
     public void put(InputStream is, File file, SortedMap<String, String> headers, long size) throws BitBunkerException {
         file.setClient(this);
         PutRequest request = new PutRequest(file, headers);
         executeXML(request, is, size);
     }    
     
     /**
      * puts a remote file into BitBunker
      *
      * @param url           the url of the source remote file
      * @param file          the target BitBunker file
      */
     public void putFrom(URL url, File file) throws BitBunkerException {
         putFrom(url.toString(), file);
     }
 
     /**
      * puts a remote file into BitBunker
      *
      * @param url           the url of the source remote file
      * @param file          the target BitBunker file
      */
     public void putFrom(URL url, File file, int durability) throws BitBunkerException {
         putFrom(url.toString(), file, durability);
     }
 
     /**
      * puts a remote file into BitBunker
      *
      * @param url           the url of the source remote file
      * @param file          the target BitBunker file
      * @param callback       http URL or email address to callback to
      */
     public void putFrom(URL url, File file, String callback) throws BitBunkerException {
         putFrom(url.toString(), file, callback);
     }
 
     /**
      * puts a remote file into BitBunker
      *
      * @param url           the url of the source remote file
      * @param file          the target BitBunker file
      * @param callback       http URL or email address to callback to
      */
     public void putFrom(URL url, File file, String callback, int durability) throws BitBunkerException {
         putFrom(url.toString(), file, callback, durability);
     }
 
     /**
      * puts a remote file into BitBunker
      *
      * @param url           the url of the source remote file
      * @param path          the path within BitBunker to put the file
      */
     public void putFrom(URL url, String path) throws BitBunkerException {
         putFrom(url.toString(), path);
     }
 
     /**
      * puts a remote file into BitBunker
      *
      * @param url           the url of the source remote file
      * @param path          the path within BitBunker to put the file
      */
     public void putFrom(URL url, String path, int durability) throws BitBunkerException {
         putFrom(url.toString(), path, durability);
     }
 
     /**
      * puts a remote file into BitBunker
      *
      * @param url           the url of the source remote file
      * @param path          the path within BitBunker to put the file
      * @param callback       http URL or email address to callback to
      */
     public void putFrom(URL url, String path, String callback) throws BitBunkerException {
         putFrom(url.toString(), path, callback);
     }
 
     /**
      * puts a remote file into BitBunker
      *
      * @param url           the url of the source remote file
      * @param path          the path within BitBunker to put the file
      * @param callback       http URL or email address to callback to
      */
     public void putFrom(URL url, String path, String callback, int durability) throws BitBunkerException {
         putFrom(url.toString(), path, callback, durability);
     }
 
     /**
      * puts a remote file into BitBunker
      *
      * @param url           the url of the source remote file
      * @param path          the path within BitBunker to put the file
      */
     public void putFrom(String url, String path) throws BitBunkerException {
         putFrom(url, new File(path));
     }
 
     /**
      * puts a remote file into BitBunker
      *
      * @param url           the url of the source remote file
      * @param path          the path within BitBunker to put the file
      * @param callback       http URL or email address to callback to
      */
     public void putFrom(String url, String path, String callback) throws BitBunkerException {
         putFrom(url, new File(path), callback);
     }
 
     /**
      * puts a remote file into BitBunker
      *
      * @param url           the url of the source remote file
      * @param path          the path within BitBunker to put the file
      * @param callback       http URL or email address to callback to
      */
     public void putFrom(String url, String path, String callback, int durability) throws BitBunkerException {
         putFrom(url, new File(path), callback, durability);
     }
 
     /**
      * puts a remote file into BitBunker
      *
      * @param url           the url of the source remote file
      * @param path          the path within BitBunker to put the file
      * @param callback       http URL or email address to callback to
      */
     public void putFrom(String url, String path, int durability) throws BitBunkerException {
         putFrom(url, new File(path), durability);
     }
 
     /**
      * puts a remote file into BitBunker
      *
      * @param url           the url of the source remote file
      * @param file          the target BitBunker file
      */
     public void putFrom(String url, File file, int durability) throws BitBunkerException {
         SortedMap headers = new TreeMap<String, String>();
         headers.put(BBHeaders.DURABILITY, Integer.toHexString(durability));
         putFrom(url, file, headers);
     }
 
     /**
      * puts a remote file into BitBunker
      *
      * @param url           the url of the source remote file
      * @param file          the target BitBunker file
      */
     public void putFrom(String url, File file, String callback, int durability) throws BitBunkerException {
         SortedMap headers = new TreeMap<String, String>();
 
         if(durability > 0)
             headers.put(BBHeaders.DURABILITY, Integer.toHexString(durability));
         
         if(callback != null)
             headers.put(BBHeaders.CALLBACK, callback);
 
         putFrom(url, file, headers);
     }    
 
     /**
      * puts a remote file into BitBunker
      *
      * @param url           the url of the source remote file
      * @param file          the target BitBunker file
      */
     public void putFrom(String url, File file) throws BitBunkerException {
         putFrom(url, file, new TreeMap<String, String>());
     }
 
     /**
      * puts a remote file into BitBunker
      *
      * @param url           the url of the source remote file
      * @param file          the target BitBunker file
      * @param callback       http URL or email address to callback to
      */
     public void putFrom(String url, File file, String callback) throws BitBunkerException {
         SortedMap<String, String> headers = new TreeMap<String, String>();
 
         if(callback != null)
             headers.put(BBHeaders.CALLBACK, callback);
         
         putFrom(url, file, headers);
     }
 
     /**
      * puts a remote file into BitBunker
      *
      * @param url           the url of the source remote file
      * @param file          the target BitBunker file
      * @param headers       a map of extra BB headers to add to the requesst
      */    
     public void putFrom(String url, File file, SortedMap<String, String> headers) throws BitBunkerException {
         file.setClient(this);
         headers.put(BBHeaders.GET_FROM, url);
         PutRequest request = new PutRequest(file, headers);
         executeXML(request);
     }
 
     /**
      * obtain a list of files from a folder in BitBunker
      *
      * @param folder         the folder of which to obtain a listing
      * @return               a <code>List</code> of child objects
      */
     public List<BitBunkerObject> list(Folder folder) throws BitBunkerException {
         ListResponse response = (ListResponse)executeXML(new ListRequest(folder));
         return response.getItems();
     }
 
     /**
      * obtain a list of files from a folder in BitBunker
      *
      * @param folder         the folder of which to obtain a listing
      * @param maxObjects     maximum number of objects
      * @return               a <code>List</code> of child objects
      */
     public List<BitBunkerObject> list(Folder folder, int maxObjects) throws BitBunkerException {
         ListResponse response = (ListResponse)executeXML(new ListRequest(folder, maxObjects));
         return response.getItems();
     }
 
     /**
      * obtain a list of files from a folder in BitBunker
      *
      * @param folder         the folder of which to obtain a listing
      * @param maxObjects     maximum number of objects
      * @param marker         object to start after (last object on previous page)
      * @return               a <code>List</code> of child objects
      */
     public List<BitBunkerObject> list(Folder folder, int maxObjects, String marker) throws BitBunkerException {
         ListResponse response = (ListResponse)executeXML(new ListRequest(folder, maxObjects, marker));
         return response.getItems();
     }
 
     /**
      * obtain a list of files from a folder in BitBunker
      *
      * @param folder         the folder of which to obtain a listing
      * @param maxObjects     maximum number of objects
      * @param marker         object to start after (last object on previous page)
      * @return               a <code>List</code> of child objects
      */
     public List<BitBunkerObject> list(Folder folder, int maxObjects, BitBunkerObject marker) throws BitBunkerException {
         ListResponse response = (ListResponse)executeXML(new ListRequest(folder, maxObjects, marker.getName()));
         return response.getItems();
     }
 
     /**
      * obtain a list of files from a folder in BitBunker
      *
      * @param folder         the folder of which to obtain a listing
      * @return               a <code>List</code> of child objects
      */
     public List<BitBunkerObject> list(String folder) throws BitBunkerException {
         return list(new Folder(folder));
     }
 
     /**
      * obtain a list of files from a folder in BitBunker
      *
      * @param folder         the folder of which to obtain a listing
      * @param maxObjects     maximum number of objects
      * @return               a <code>List</code> of child objects
      */
     public List<BitBunkerObject> list(String folder, int maxObjects) throws BitBunkerException {
         return list(new Folder(folder), maxObjects);
     }
 
     /**
      * obtain a list of files from a folder in BitBunker
      *
      * @param folder         the folder of which to obtain a listing
      * @param maxObjects     maximum number of objects
      * @param marker         object to start after (last object on previous page)
      * @return               a <code>List</code> of child objects
      */
     public List<BitBunkerObject> list(String folder, int maxObjects, String marker) throws BitBunkerException {
         return list(new Folder(folder), maxObjects, marker);
     }
 
     /**
      * get a stream of data for a file in BitBunker
      *
      * @param file           the file in BitBunker to obtain data for
      * @retun                an <code>InputStream</code> of the data in BitBunker
      */
     public InputStream getStream(File file) throws BitBunkerException {
         return executeStream(new GetRequest(file));
     }
 
     /**
      * get a stream of data for a file in BitBunker
      *
      * @param path           path to the source file in BitBunker
      * @retun                an <code>InputStream</code> of the data in BitBunker
      */
     public InputStream getStream(String path) throws BitBunkerException {
         return getStream(new File(path));
     }
 
     /**
      * get a saved java.io.File from a file in BitBunker
      *
      * @param file           the source file in BitBunker
      * @param localPath      the target location on the local filesystem
      * @retun                a <code>java.io.File</code> pointing to the saved file
      */
     public java.io.File getFile(File file, String localPath) throws BitBunkerException {
         return getFile(file, new java.io.File(localPath));
     }
 
     /**
      * get a saved java.io.File from a file in BitBunker
      *
      * @param path           path to the source file in BitBunker
      * @param localPath      the target location on the local filesystem
      * @retun                a <code>java.io.File</code> pointing to the saved file
      */
     public java.io.File getFile(String path, String localPath) throws BitBunkerException {
         return getFile(new File(path), localPath);
     }
 
     /**
      * get a saved java.io.File from a file in BitBunker
      *
      * @param path           path to the source file in BitBunker
      * @param localFile      the target file on the local FileSystem
      * @retun                a <code>java.io.File</code> pointing to the saved file
      */
     public java.io.File getFile(String path, java.io.File locaFile) throws BitBunkerException {
         return getFile(new File(path), locaFile);
     }
 
     /**
      * get a saved java.io.File from a file in BitBunker
      *
      * @param file           the file in BitBunker to retrieve
      * @param localFile      the target file on the local FileSystem
      * @retun                a <code>java.io.File</code> pointing to the saved file
      */
     public java.io.File getFile(File file, java.io.File localFile) throws BitBunkerException {
         try {
             localFile.createNewFile();
             FileOutputStream fos = new FileOutputStream(localFile);
             get(file, fos);
         } catch (java.io.FileNotFoundException ex) {
             throw new com.bitbunker.api.FileNotFoundException(ex);
         } catch (IOException ex) {
             if(ex.getMessage().equals("Permission denied"))
                 throw new SecurityException(ex);
             else
                 throw new NetworkException(ex);
         }
 
         return localFile;
     }
 
     /**
      * get a file from BitBunker
      *
      * @param file           the file in BitBunker to retrieve
      * @param localPath      the target location on the local filesystem
      */
     public void get(File file, String localPath) throws BitBunkerException {
         get(file.getPath(), localPath);
     }
 
     /**
      * get a file from BitBunker
      *
      * @param path           path to the source file in BitBunker
      * @param localPath      the target location on the local filesystem
      */
     public void get(String path, String localPath) throws BitBunkerException {
         getFile(path, localPath);
     }
 
     /**
      * get a file from BitBunker
      *
      * @param path           path to the source file in BitBunker
      * @param os             stream of data from BitBunker
      */
     public void get(String path, OutputStream os) throws BitBunkerException {
         try {
             InputStream is = getStream(path);
             byte[] buff = new byte[16384];
             int len = 0;
 
             while((len = is.read(buff)) > 0) {
                 os.write(buff, 0, len);
             }
 
             os.flush();
             os.close();
         } catch (FileNotFoundException fnfe ) {
             // file exists?
             throw new FileNotFoundException();
         } catch (Exception ex) {
             throw new NetworkException(ex);
         } finally {
             try {
                 os.close();
             } catch (IOException ex) {
                 throw new NetworkException(ex);
             }
         }
     }
 
     /**
      * get a file from BitBunker
      *
      * @param file           the file in BitBunker to retrieve
      * @param os             stream of data from BitBunker
      */
     public void get(File file, OutputStream os) throws BitBunkerException {
         get(file.getPath(), os);
     }
 
     /**
      * get a file from BitBunker and transfer it to a remote location
      *
      * @param file           the file in BitBunker to retrieve
      * @param url            the remote target location
      */
     public void getTo(File file, String url) throws BitBunkerException {
         getTo(file, url, new TreeMap<String, String>());
     }
 
     /**
      * get a file from BitBunker and transfer it to a remote location
      *
      * @param file           the file in BitBunker to retrieve
      * @param url            the remote target location
      * @param callback       http URL or email address to callback to
      */
     public void getTo(File file, String url, String callback) throws BitBunkerException {
         SortedMap<String, String> headers = new TreeMap<String, String>();
 
         if(callback != null)
             headers.put(BBHeaders.CALLBACK, callback);
         
         getTo(file, url, headers);
     }
 
     /**
      * get a file from BitBunker and transfer it to a remote location
      *
      * @param path           path to the source file in BitBunker
      * @param url            the remote target location
      */
     public void getTo(String path, String url) throws BitBunkerException {
         getTo(new File(path, this), url);
     }
 
     /**
      * get a file from BitBunker and transfer it to a remote location
      *
      * @param path           path to the source file in BitBunker
      * @param url            the remote target location
      * @param callback       http URL or email address to callback to
      */
     public void getTo(String path, String url, String callback) throws BitBunkerException {
         getTo(new File(path, this), url, callback);
     }
 
     /**
      * get a file from BitBunker and transfer it to a remote location
      *
      * @param file           the file in BitBunker to retrieve
      * @param url            the remote target location
      */
     public void getTo(File file, URL url) throws BitBunkerException {
         getTo(file, url.toString());
     }
 
     /**
      * get a file from BitBunker and transfer it to a remote location
      *
      * @param file           the file in BitBunker to retrieve
      * @param url            the remote target location
      * @param callback       http URL or email address to callback to
      */
     public void getTo(File file, URL url, String callback) throws BitBunkerException {
         getTo(file, url.toString(), callback);
     }
 
     /**
      * get a file from BitBunker and transfer it to a remote location
      *
      * @param path           path to the source file in BitBunker
      * @param url            the remote target location
      */
     public void getTo(String path, URL url) throws BitBunkerException {
         getTo(path, url.toString());
     }    
 
     /**
      * get a file from BitBunker and transfer it to a remote location
      *
      * @param path           path to the source file in BitBunker
      * @param url            the remote target location
      * @param callback       http URL or email address to callback to
      */
     public void getTo(String path, URL url, String callback) throws BitBunkerException {
         getTo(path, url.toString(), callback);
     }
 
     /**
      * get a file from BitpathBunker and transfer it to a remote location
      *
      * @param path           path to the source file in BitBunker
      * @param url            the remote target location
      * @param headers        a map of extra BB headers to add to the request
      */
     public void getTo(File file, String url, SortedMap<String, String> headers) throws BitBunkerException {
         GetRequest request = new GetRequest(file, headers);
         request.getBbHeaders().put(BBHeaders.SEND_TO, url);
         executeXML(request);
     }
 
     /**
      * delete a file from BitBunker
      *
      * @param file           the file in BitBunker to delete
      */
     public void delete(File file) throws BitBunkerException {
         executeXML(new DeleteRequest(file));
     }
 
     /**
      * delete a file from BitBunker
      *
      * @param path           path to the file in BitBunker to delete
      */
     public void delete(String path) throws BitBunkerException {
         delete(new File(path, this));
     }
 
     /**
      * modify the attributes of a file in BitBunker
      *
      * @param file          the file in BitBunker to modify
      * @param durability    total number of backups to store
      */
     public void mod(File file, int durability) throws BitBunkerException {
         executeXML(new ModRequest(file, durability));
     }
 
     /**
      * modify the attributes of a file in BitBunker
      *
      * @param path           path to the file in BitBunker to modify
      * @param durability     total number of backups to store
      */
     public void mod(String path, int durability) throws BitBunkerException {
         mod(new File(path, this), durability);
     }
 
     /**
      * get the extended properties of a file in BitBunker
      *
      * @param path           path to the file in BitBunker of which of which to
      *                       to get properties
      * @return               a <code>FileProperties</code> object containing the
      *                       advanced properties
      */
     public FileProperties fileProperties(String path) throws BitBunkerException {
         return ((PropertiesResponse)executeXML(new PropertiesRequest(new File(path, this)))).getProperties();
     }
 
     /**
      * get the root folder in BitBunker
      *
      * @return               a <code>Folder</code> object representing the root
      *                       BitBunker folder
      */
     public Folder rootFolder() {
         return folder("/");
     }
 
     /**
      * get the folder of a given path
      *
      * @param path           path to the folder to get the extended properties of
      * @return               a <code>Folder</code> object representing the requested
      *                       BitBunker folder
      */
     public Folder folder(String path) {
         return new Folder(path, this);
     }
 
     /**
      * get the file object of a given path
      *
      * @param path           path to the file in BitBunker
      * @return               a <code>File</code> object representing the requested
      *                       BitBunker path
      */
     public File file(String path) {
         return new File(path, this);
     }
 }
