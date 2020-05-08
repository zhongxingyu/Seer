 /*
  * Copyright (c) 2011, dan.clark@nekocode.org
  *
  * Licensed under FreeBSD license.  See README for details.
  */
 
 package org.nekocode.itunes.remote.connection;
 
 import org.apache.log4j.Level;
 import org.apache.log4j.Logger;
 
 import java.io.ByteArrayOutputStream;
 import java.io.DataInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.HttpURLConnection;
 import java.net.URL;
 import java.nio.charset.Charset;
 import java.util.Collections;
 import java.util.Set;
 import java.util.WeakHashMap;
 import java.util.concurrent.locks.Lock;
 import java.util.concurrent.locks.ReentrantLock;
 import java.util.zip.GZIPInputStream;
 import java.util.zip.Inflater;
 import java.util.zip.InflaterInputStream;
 
 import static java.lang.String.format;
 
 /**
  * Executes URLs against iTunes and parses the results.
  */
 public class RequestManager {
     private static final Logger log = Logger.getLogger(RequestManager.class);
     private static final int DEFAULT_TIMEOUT = 10000;
     private static final Charset CHARSET_UTF_8 = Charset.forName("UTF-8");
 
     /**
      * Weak references.  Concurrent.
      */
     private static Set<HttpURLConnection> connections =
             Collections.newSetFromMap(new WeakHashMap<HttpURLConnection, Boolean>());
     //            Collections.synchronizedSet(Collections.newSetFromMap(new WeakHashMap<HttpURLConnection, Boolean>()));
     //            Collections.newSetFromMap(new ConcurrentHashMap<HttpURLConnection, Boolean>());
     private static Lock shutdownLock = new ReentrantLock();
     private static boolean shutdown = false;
 
     public static byte[] requestBytes(URL url) throws IOException {
         HttpURLConnection connection = openHttpConnection(url, false);
         byte[] buffer = new byte[1024];
 
         ByteArrayOutputStream os = new ByteArrayOutputStream();
 
         try (InputStream connectionInputStream = connection.getInputStream()) {
             String encoding = connection.getContentEncoding();
             InputStream is;
             // create the appropriate stream wrapper based on the encoding type
             if (encoding != null && encoding.equalsIgnoreCase("gzip")) {
                 is = new GZIPInputStream(connectionInputStream);
             } else if (encoding != null && encoding.equalsIgnoreCase("deflate")) {
                 is = new InflaterInputStream(connectionInputStream, new Inflater(true));
             } else {
                 is = connectionInputStream;
             }
 
             int bytesRead;
             while ((bytesRead = is.read(buffer)) != -1) {
                 os.write(buffer, 0, bytesRead);
             }
         } finally {
             os.flush();
             os.close();
         }
 
         shutdownLock.lock();
         connections.remove(connection);
         shutdownLock.unlock();
         log.debug("Received " + os.size() + " bytes");
         return os.toByteArray();
     }
 
     public static void send(URL url) throws IOException {
         openHttpConnection(url, false);
     }
 
     /**
      * Request data from iTunes.
      *
      * @param url web service address
      * @param keepOpen true if this connection should wait indefinitely for a response
      * @return server response
      * @throws IOException if a communication error occurred
      */
     public static ITunesRemoteResponse request(URL url, boolean keepOpen) throws IOException {
         HttpURLConnection connection = openHttpConnection(url, keepOpen);
 
         InputStream is = null;
         try {
             String encoding = connection.getContentEncoding();
             // create the appropriate stream wrapper based on the encoding type
             if (encoding != null && encoding.equalsIgnoreCase("gzip")) {
                 is = new GZIPInputStream(connection.getInputStream());
             } else if (encoding != null && encoding.equalsIgnoreCase("deflate")) {
                 is = new InflaterInputStream(connection.getInputStream(), new Inflater(true));
             } else {
                 is = connection.getInputStream();
             }
 
             DataInputStream data = new DataInputStream(is);
 
             // might as well reuse this array since we'll be creating lost of 4-character strings
             byte[] stringBuffer = new byte[4];
             String name = readString(data, stringBuffer);
             int length = data.readInt();
 
             ITunesRemoteResponse response = new ITunesRemoteResponse();
 
             ContentCode identifier = ContentCode.getById(name);
 
             if (identifier == null) {
                 log.warn("Unrecognized root identifier: " + name);
             } else {
                 response.addChild(identifier, parse(data, length, stringBuffer));
             }
 
             if (log.isDebugEnabled()) {
                 log.debug("\n" + response);
             }
 
             return response;
         } finally {
             if (is != null) {
                 is.close();
             }
             shutdownLock.lock();
             connections.remove(connection);
             shutdownLock.unlock();
         }
     }
 
     private static HttpURLConnection openHttpConnection(URL url, boolean keepOpen) throws IOException {
         if (log.isDebugEnabled()) {
             log.debug(url.toExternalForm());
         }
 
         HttpURLConnection connection = (HttpURLConnection) url.openConnection();
         shutdownLock.lock();
         if (shutdown) {
             throw new IOException("RequestManager has been shut down.  No more connections possible.");
         }
         connections.add(connection);
         shutdownLock.unlock();
         connection.setRequestProperty("Viewer-Only-Client", "1");
         if (!keepOpen) {
             connection.setConnectTimeout(DEFAULT_TIMEOUT);
             connection.setReadTimeout(DEFAULT_TIMEOUT);
         }
         connection.connect();
 
         if (connection.getResponseCode() >= 400) {
             String message = "HTTP Error Response Code " + connection.getResponseCode() + ": " + connection.getResponseMessage();
             log.error(message);
             log.error(url.toExternalForm());
             throw new IOException(message);
         }
         return connection;
     }
 
     private static ITunesRemoteResponse parse(DataInputStream data, int length, byte[] stringBuffer) throws IOException {
         ITunesRemoteResponse response = new ITunesRemoteResponse();
 
         boolean printResult = false;
         int read = 0;
         while (read < length) {
             String name = readString(data, stringBuffer);
             int fieldLength = data.readInt();
             read += 8;
             read += fieldLength;
 
             ContentCode identifier = ContentCode.getById(name);
             if (identifier == null) {
                 String string = guessDataType(data, fieldLength);
                 log.warn(format("Unrecognized identifier: %s.  Length: %d  Value: %s", name, fieldLength, string));
                 printResult = true;
             } else if (identifier.isInternalNodeType() || identifier.isInternalMultiNodeType()) {
                 response.addChild(identifier, parse(data, fieldLength, stringBuffer));
             } else if (identifier.isStringType()) {
                 response.addChild(identifier, readString(data, fieldLength));
             } else if (identifier.isIntegerType()) {
                 response.addChild(identifier, readInteger(data, fieldLength));
             } else if (identifier.isSpecialType()) {
                 handleSpecialTypes(data, identifier, fieldLength, response);
             } else {
                 log.warn("unhandled identifier: " + identifier);
                 // make sure to skip the bytes so future reads don't get screwed up
                 data.skipBytes(fieldLength);
             }
         }
 
         if (printResult && log.isEnabledFor(Level.WARN)) {
             log.warn(response.toString());
         }
         return response;
     }
 
     /**
      * Try to guess at the type of the data, and print out the best representation.
      * @param data data stream
      * @param length length of data
      * @return string representing best guess at data representation
      * @throws java.io.IOException if an error occurs reading from the stream
      */
     private static String guessDataType(DataInputStream data, int length) throws IOException {
         // if it looks like a number (1,2,4,8 bytes), print that type
         Number number = readInteger(data, length);
         if (number == null) {
             // if it wasn't a number, print it as a string
             byte[] buffer = new byte[length];
             data.readFully(buffer);
             return new String(buffer, CHARSET_UTF_8);
         }
         return number.toString();
     }
 
     private static Number readInteger(DataInputStream data, int length) throws IOException {
         switch (length) {
             case 1:
                 return data.readByte();
             case 2:
                 return data.readShort();
             case 4:
                 return data.readInt();
             case 8:
                 return data.readLong();
             default:
                 return null;
         }
     }
 
     private static void handleSpecialTypes(DataInputStream data, ContentCode identifier,
                                            int length, ITunesRemoteResponse response) throws IOException {
         if (identifier == ContentCode.NOW_PLAYING) {
             byte[] bytes = new byte[length];
             data.readFully(bytes);
             response.addChild(identifier, bytes);
 
             // this is a special identifier that hold multiple values
             if (length != 16) {
                 log.warn("[canp] length differs from expected length.  was " + length + ", expected: 16");
                 // don't collect the item id rather than explode
             } else {
                 // items are: dbid, plid, playlistItem, itemid
                 int itemId = (bytes[12] & 0xFF) << 24 |
                         (bytes[13] & 0xFF) << 16 |
                         (bytes[14] & 0xFF) << 8 |
                         (bytes[15] & 0xFF);
                 response.addChild(ContentCode.ITEM_ID, itemId);
             }
         } else {
             // read the data but throw it away - we don't know how to handle this type
             log.warn("Discarding data for field " + identifier);
             int bytesSkipped = data.skipBytes(length);
             if (bytesSkipped != length) {
                 throw new IOException("unable to fully skip data");
             }
         }
     }
 
     private static String readString(DataInputStream data, int fieldLength) throws IOException {
         byte[] buffer = new byte[fieldLength];
         data.readFully(buffer);
         return new String(buffer, CHARSET_UTF_8);
     }
 
     public static String readString(DataInputStream data, byte[] stringBuffer) throws IOException {
         data.readFully(stringBuffer);
         return new String(stringBuffer, CHARSET_UTF_8);
     }
 
     /**
      * Shuts down any active connections.
      */
     public static void shutdown() {
         shutdownLock.lock();
         shutdown = true;
 
         // disconnect any active connections
         for (HttpURLConnection connection : connections) {
             connection.disconnect();
         }
 
         shutdownLock.unlock();
     }
 }
