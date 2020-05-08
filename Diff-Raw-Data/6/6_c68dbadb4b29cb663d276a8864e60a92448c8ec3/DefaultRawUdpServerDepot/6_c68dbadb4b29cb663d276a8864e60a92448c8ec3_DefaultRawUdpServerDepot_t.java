 package org.lastbamboo.common.p2p;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.net.Socket;
 import java.util.Collection;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Timer;
 import java.util.TimerTask;
 import java.util.concurrent.ConcurrentHashMap;
 
 import org.apache.commons.io.IOUtils;
 import org.json.simple.JSONArray;
 import org.json.simple.JSONObject;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * This class accepts incoming, typically relayed raw UDP data connections.
  */
 public class DefaultRawUdpServerDepot implements RawUdpServerDepot {
 
     private final Logger log = LoggerFactory.getLogger(getClass());
     
     private final Map<String, CallState> sessions = 
         new ConcurrentHashMap<String, CallState>();
     
     public DefaultRawUdpServerDepot() {
         startPurging();
         startServerThreaded();
     }
     
     private void startServerThreaded() {
         final Runnable runner = new Runnable() {
             public void run() {
                 //startServer();
             }
         };
         final Thread t = new Thread(runner, "Incoming-Raw-UDP-Server-Thread");
         t.setDaemon(true);
         t.start();
     }
 
     private void startPurging() {
         final Timer t = new Timer();
         final TimerTask task = new TimerTask() {
             @Override
             public void run() {
                 final long now = System.currentTimeMillis();
                 synchronized (sessions) {
                     for (final Entry<String, CallState> e : sessions.entrySet()) {
                         final CallState ts = e.getValue();
                         if (now - ts.getTime() < 30 * 1000) {
                             // Ignore new sockets.
                             log.info("Not purging new socket");
                             return;
                         }
                         final Socket sock = ts.getSocket();
                         if (sock != null && !sock.isConnected()) {
                             log.info("Removing unconnected socket!");
                             IOUtils.closeQuietly(sock);
                             sessions.remove(e.getKey());
                         }
                         
                     }
                 }
             }
         };
         t.schedule(task, 30 * 1000, 30 * 1000);
     }
 
     public void onSocket(final String id, final Socket sock) throws IOException{
         addSocket(id, sock);
     }
 
     public void addSocket(final String id, final Socket sock) {
         log.info("Adding socket with ID: "+id+"to: "+hashCode());
         sessions.put(id, new TimestampedSocket(sock));
     }
     
     public Socket getSocket(final String id) {
         final CallState ts = sessions.get(id);
         if (ts == null) return null;
         return ts.getSocket();
     }
 
     public Collection<String> getIds() {
         synchronized (sessions) {
             return sessions.keySet();
         }
     }
 
     public void addError(final String id, final String msg) {
         log.info("Adding error for "+id+" with msg: "+msg);
         sessions.put(id, new CallError(msg));
     }
     
     public long write(final String id, final InputStream is, 
         final long contentLength) throws IOException {
         final Socket sock = getSocket(id);
         if (sock != null) {
             log.info("Writing from socket: {}", sock.getLocalSocketAddress());
             log.info("Writing to socket output stream!");
             final OutputStream os = sock.getOutputStream();
             //threadedCopy(is, os, "Call-Write-Thread", contentLength);
             return copy(is, os, contentLength);
         } else {
             log.warn("Could not find socket with ID {} in "+sessions, id);
             return -1;
         }
     }
     
     public void write(final String id, final byte[] data) throws IOException {
         final Socket sock = getSocket(id);
         if (sock != null) {
             log.info("Writing from socket: {}", sock.getLocalSocketAddress());
             log.info("Writing to socket output stream!");
             final OutputStream os = sock.getOutputStream();
             //threadedCopy(is, os, "Call-Write-Thread", contentLength);
             os.write(data);
             //return copy(is, os, contentLength);
         } else {
             log.warn("Could not find socket with ID {} in "+sessions, id);
         }
     }
 
     
     private void threadedCopy(final InputStream is, final OutputStream os,
         final String threadName, final int contentLength) {
         final Runnable runner = new Runnable() {
             public void run() {
                 try {
                     copy(is, os, contentLength);
                 } catch (final IOException e) {
                     log.info("Exception on copy. Hung up?", e);
                 }
             }
         };
         final Thread t = new Thread(runner, threadName);
         t.setDaemon(true);
         t.start();
     }
     
     public long read(final String id, final OutputStream outputStream,
         final int length) throws IOException {
         log.info("Reading data...");
         final Socket sock = getSocket(id);
         if (sock == null) {
             log.warn("Call "+id+" not found from "+hashCode()+" in {}", sessions);
             return -1L;
         }
         log.info("Got socket -- remote host: {}", 
             sock.getRemoteSocketAddress());
         final InputStream is = sock.getInputStream();
        final byte[] data = new byte[length];
        final int read = is.read(data);
        outputStream.write(data, 0, read);
        return read;
        //return copy(is, outputStream, length);
     }
     
     /**
      * The default buffer size to use.
      */
     private static final int DEFAULT_BUFFER_SIZE = 1024 * 4;
     /** 
      * Copies the {@link InputStream} to the specified {@link OutputStream}
      * for the specified number of bytes or until EOF or exception.
      * 
      * @param in The {@link InputStream} to copy from. 
      * @param out The {@link OutputStream} to copy to.
      * @param originalByteCount The number of bytes to copy.
      * @return The number of bytes written.
      * @throws IOException If there's an IO error copying the bytes.
      */
     private long copy(final InputStream in, final OutputStream out,
             final long originalByteCount) throws IOException {
         final byte buffer[] = new byte[DEFAULT_BUFFER_SIZE];
         int len = 0;
         long written = 0;
         long byteCount = originalByteCount;
         try {
             while (byteCount > 0) {
                 // len = in.read(buffer);
                 if (byteCount < DEFAULT_BUFFER_SIZE) {
                     len = in.read(buffer, 0, (int) byteCount);
                 } else {
                     len = in.read(buffer, 0, DEFAULT_BUFFER_SIZE);
                 }
                 log.debug("Read {} bytes", len);
                 if (len == -1) {
                     log.debug("Breaking on length = -1");
                     // System.out.println("Breaking on -1");
                     break;
                 } 
 
                 byteCount -= len;
                 log.info("Total written: " + written);
                 out.write(buffer, 0, len);
                 written += len;
                 log.debug("Now written: "+written);
             }
             // System.out.println("Out of while: "+byteCount);
             return written;
         } catch (final IOException e) {
             log.debug("Got IOException during copy after writing " + written
                     + " of " + originalByteCount, e);
             e.printStackTrace();
             throw e;
         } catch (final RuntimeException e) {
             log.debug("Runtime error after writing " + written + " of "
                     + originalByteCount, e);
             e.printStackTrace();
             throw e;
         } finally {
             out.flush();
         }
     }
 
     public JSONObject toJson() {
         log.info("Accessing JSON for sessions: {}", sessions);
         final JSONObject json = new JSONObject();
         final JSONArray calls = new JSONArray();
         for (final Entry<String, CallState> entry : this.sessions.entrySet()) {
             final JSONObject jsonId = new JSONObject();
             final String id = entry.getKey();
             final CallState cs = entry.getValue();
             jsonId.put("id", id);
             jsonId.put("state", cs.getMessage());
             calls.add(jsonId);
         }
         
         json.put("calls", calls);
         return json;
     }
     
     private static final class CallError implements CallState {
 
         private final long time = System.currentTimeMillis();
         
         private final String msg;
 
         public CallError(final String msg) {
             this.msg = msg;
         }
 
         public long getTime() {
             return time;
         }
 
         public Socket getSocket() {
             return null;
         }
 
         public String getMessage() {
             return this.msg;
         }
     }
     
     private static final class TimestampedSocket implements CallState {
 
         private final long time = System.currentTimeMillis();
         private final Socket sock;
 
         public TimestampedSocket(final Socket sock) {
             this.sock = sock;
         }
 
         public long getTime() {
             return time;
         }
 
         public Socket getSocket() {
             return sock;
         }
 
         public String getMessage() {
             return "CONNECTED";
         }
     }
 }
