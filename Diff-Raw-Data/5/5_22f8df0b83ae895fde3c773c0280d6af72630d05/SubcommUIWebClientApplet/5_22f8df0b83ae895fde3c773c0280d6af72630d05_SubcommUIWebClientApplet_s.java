 package com.roylaurie.subcomm.ui.web.client.applet;
 
 import java.applet.Applet;
import java.io.UnsupportedEncodingException;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import java.util.regex.Pattern;
 
 import com.roylaurie.subcomm.client.SubcommClient;
 import com.roylaurie.subcomm.client.netchat.SubcommNetchatClient;
 
 public final class SubcommUIWebClientApplet extends Applet {
     private static final Logger LOG = Logger.getLogger(SubcommUIWebClientApplet.class.getCanonicalName());
     private static final Pattern SANITIZE_PATTERN = Pattern.compile("[^\\u0000-\\uFFFF]"); // strip /u007f control char
     private static final String EMPTY_STR = "";
    private static final long serialVersionUID = 1L;
     
     /* @var Map<String, SubcommClient> mClientMap Maps uri => client object. */
     private final Map<String, SubcommClient> mClientMap = new HashMap<String, SubcommClient>();
     /* @var Map<String, SubcommClient> mExceptionMap Maps uri => exception object. */
     private final Map<String, Exception> mExceptionMap = new HashMap<String, Exception>();
     
     /**
      * Strips out unicode control characters.
      * @param String input Valid string
      * @return String
      */
     private static String sanitize(String input) {
         return SANITIZE_PATTERN.matcher(input.trim()).replaceAll(EMPTY_STR);
     }
     
     public String connect(String id, String hostname, int port, String username, String password) {
         if (id == null || hostname == null || port == 0 || username == null || password == null)
             throw new IllegalArgumentException("All parameters are required.");
         
         id = sanitize(id);
         hostname = sanitize(hostname);
         username = sanitize(username);
         password = sanitize(password);
         
         final String uri = new StringBuffer(username)
         .append('@').append(hostname)
         .append(':').append(port)
         .append('#').append(id)
         .toString();
         
         synchronized(mClientMap) {
             if (mClientMap.containsKey(id))
                 return uri;
             
             mClientMap.put(uri, null);
         }
 
         final SubcommClient client = new SubcommNetchatClient(hostname, port, username, password);
         Thread connectThread = new ConnectThread(this, uri, client);
         connectThread.start();
         return uri;
     }
     
     /**
      * Retrieves the client for the specified uri, if any.
      * @param String uri
      * @return SubcommClient NULL if no client found
      */
     public SubcommClient getClient(String uri) {
         uri = sanitize(uri);
         synchronized(mClientMap) {
             return ( mClientMap.get(uri) );
         }
     }
     
     /**
      * Retrieves the next thrown exception, if any, for the specified client.
      * @param String uri The client URI.
      * @return Exception NULL if no exceptions thrown
      */
     public Exception nextClientException(String uri) {
         uri = sanitize(uri);
         synchronized(mExceptionMap) {
             return mExceptionMap.remove(uri);
         }
     }
     
     /* package */ void notifyConnectionComplete(String uri, SubcommClient client) {
         synchronized(mClientMap) {
             // prevent leaked connections by bounds checking
             if (!mClientMap.containsKey(uri))
                 throw new IllegalArgumentException("Unknown uri `" + uri + "` for notification of completion.");
             
             SubcommClient queuedClient = mClientMap.get(uri);
             if (queuedClient != null && queuedClient.connected())
                 throw new IllegalArgumentException("Connection `" + uri + "` already notified for completion.");
             
             mClientMap.put(uri, client);
         }
         
         LOG.info("Connected to `" + uri + "`.");
     }
     
     /* package */ void notifyConnectionFailed(String uri, SubcommClient client, Exception e) {
         synchronized(mExceptionMap) {
             mExceptionMap.put(uri, e);
         }
         
         LOG.log(Level.SEVERE, "Failed to connect to `" + uri + "`.", e);
     }
     
     /**
      * Quietly disconnects the client for the given uri.
      * @param String uri
      */
     public void disconnect(String uri) {
         uri = sanitize(uri);
         final SubcommClient client;
         synchronized(mClientMap) {            
             synchronized(mExceptionMap) {
                 client = mClientMap.remove(uri);
                 mExceptionMap.remove(uri);
             }
         }
         
 
         if (client == null)
             return;
         
         client.disconnect();
     }
     
     @Override
     public void stop() {
         LOG.info("STOP");
         super.stop();
         synchronized(mClientMap) {
             for (SubcommClient client : mClientMap.values()) {
                 final String uri = client.getUsername() + '@' + client.getUsername() + ':' + client.getUsername();
                 client.disconnect();
                 LOG.info("Disconnected client `" + uri + "`.");
             }
             
             mClientMap.clear();
         }
     }
 }
