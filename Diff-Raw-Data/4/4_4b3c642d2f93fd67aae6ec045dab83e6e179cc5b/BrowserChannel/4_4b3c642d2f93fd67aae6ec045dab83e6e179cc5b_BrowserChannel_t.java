 package channel.browser;
 
 import channel.Channel;
 import channel.Message;
 import channel.MessageHandler;
 import channel.browser.util.URLWithQuery;
 import com.google.api.client.auth.oauth2.Credential;
 import com.google.api.client.json.JsonFactory;
 import org.apache.logging.log4j.LogManager;
 import org.apache.logging.log4j.Logger;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.net.HttpURLConnection;
 import java.net.MalformedURLException;
 import java.net.ProtocolException;
 import java.net.URL;
 import java.util.*;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 /**
  * A Java port of the JavaScript implementation of BrowserChannel.
  * TODO: implement functions in quickstart/js/browserchannel.js
  *
  * @author Erik
  */
 public class BrowserChannel implements Channel
 {
     private int channelVersion = 8;
     private String baseUrl = "https://drive.google.com/otservice";
     private Session session;
     private String path;
     private String clientVersion = "1";
     private URLWithQuery forwardChannelUri;
     private State state = State.INIT;
     private ChannelRequest forwardChannelRequest;
     private int nextRid;
     private int lastArrayId;
     private List<? extends Queue<String>> outgoingMaps;
 
     // Non-JS-BrowserChannel compliant parameters
     private long lastSequenceNumber = 0L;
     private String channelSessionId;
     int retries = 1;
     private static Logger logger = LogManager.getLogger(BrowserChannel.class);
     private Credential credentials;
     private String modelId;
 
     public BrowserChannel(Credential credentials)
     {
         this.credentials = credentials;
         outgoingMaps = new LinkedList<LinkedList<String>>();
     }
 
     public Map<String, String> getDefaultParameters() {
         Map<String, String> output = new HashMap<String, String>();
         output.put("id", modelId);
         output.put("access_token", credentials.getAccessToken());
         output.put("sid", session.getId());

        return output;
     }
 
     @Override
     public void initialize(String driveFileId)
     {
         this.modelId = getModelId(driveFileId);
         this.session = getSession();
         logger.debug("SessionID: " + session.getId());
         logger.info("Initialized");
 
         nextRid = (int) Math.floor(Math.random() * 100000);
     }
 
     public void openDeltaChannel()
     {
         logger.info("Delta channel");
         int startRevision = session.getRevision() + 1;
         try {
             URL url = new URL(baseUrl + "/delta?id=" + modelId + "&access_token=" + getAccessToken() + "&sid=" + session.getId() + "&startRev=" + startRevision);
             HttpURLConnection connection = (HttpURLConnection) url.openConnection();
             BufferedReader reader = new BufferedReader(new InputStreamReader(new SkipGarbageInputStream(connection.getInputStream())));
             String line;
             while ((line = reader.readLine()) != null)
                 logger.debug(line);
             reader.close();
         } catch (MalformedURLException e) {
             e.printStackTrace();
         } catch (IOException e) {
             e.printStackTrace();
         }
     }
 
     public void openForwardChannel()
     {
         logger.info("Forward Channel (POST)");
 
         HttpURLConnection connection;
         int RID = nextRid++;
         String random = "i9dw6xv4b81e";
         byte[] rawData = new byte[0];
 
         String urlParameters = "id=" + modelId + "&access_token=" + getAccessToken() + "&sid=" + session.getId() + "&VER=" + channelVersion +
                 "&lsq=" + lastSequenceNumber + "&RID=" + RID + "&CVER=" + clientVersion + "&zx=" + random + "&t=" + retries;
         URL url;
 
         String response = "";
         try {
             Map<String,String> params = getDefaultParameters();
             params.put("VER", ""+channelVersion);
             params.put("lsq", ""+lastSequenceNumber);
             params.put("RID", ""+RID);
             params.put("CVER", clientVersion);
             params.put("zx", random);
             params.put("t", ""+retries);
             url = new URLWithQuery(baseUrl + "/bind", params).getURL();
 //            url = new URL(baseUrl + "/bind?" + urlParameters);
 
             connection = (HttpURLConnection) url.openConnection();
             connection.setDoOutput(true);
             connection.setDoInput(true);
             connection.setInstanceFollowRedirects(false);
 
             connection.setRequestMethod("POST");
 
             connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
             connection.setRequestProperty("charset", "utf-8");
             connection.setRequestProperty("Content-Length", "0");
             connection.setUseCaches(false);
             logger.info("Connection: {}", connection.toString());
 
             connection.getOutputStream().write(rawData);
 
             logger.debug("Sent request, reading input");
 
             BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
             StringBuffer responseBuffer = new StringBuffer();
             String line;
             while ((line = reader.readLine()) != null)
                 responseBuffer.append(line);
             reader.close();
             response = responseBuffer.toString();
         } catch (MalformedURLException e) {
             e.printStackTrace();
         } catch (IOException e) {
             e.printStackTrace();
         }
 
         System.out.println(response);
 
         // Parse results
         // TODO: parse with JSON parser
         lastSequenceNumber = 1;
         Pattern pattern = Pattern.compile("(.*?),\"(.*?)\",,8(.*?)");
         Matcher matcher = pattern.matcher(response);
         if (matcher.matches() && matcher.groupCount() == 3) {
             channelSessionId = matcher.group(2);
         }
         pattern = Pattern.compile("(.*?),([0-9]+?),\\{\"color\"(.*?)");
         matcher = pattern.matcher(response);
         logger.debug("Found channelSessionId/sequence? {}", matcher.matches());
         if (matcher.matches() && matcher.groupCount() == 3) {
             lastSequenceNumber = Long.parseLong(matcher.group(2));
         }
         System.out.println("channelSessionId: " + channelSessionId);
         System.out.println("lastSequenceNumber: " + lastSequenceNumber);
     }
 
     /**
      * Custom method to try opening a backward channel
      */
     public void openBackwardChannel()
     {
         String RID = "rpc";
         int AID = 1; // get this number from previous request
         int CI = 0;
         String random = "2bdw3x2d7f1z";
         logger.info("openBackwardChannel (GET)");
         String channelSessionId = "1ddd3e0166873b34";
         String type = "xmlhttp";
         String urlParameters = "id=" + modelId + "&access_token=" + getAccessToken() + "&sid=" + session.getId() + "&VER=" + channelVersion + "&lsq=" + lastSequenceNumber + "&RID=" + RID + "&SID=" + channelSessionId + "&AID=" + AID + "&CI=" + CI + "&TYPE=" + type + "&zx=" + random + "&t=" + retries;
         URL url = null;
         try {
             url = new URL(baseUrl + "/bind?" + urlParameters);
         } catch (MalformedURLException e) {
             logger.error("MalformedURLException");
             e.printStackTrace();
         }
         HttpURLConnection connection;
         InputStreamReader stream = null;
         try {
             connection = (HttpURLConnection) url.openConnection();
             stream = new InputStreamReader(connection.getInputStream());
 
             System.out.println("Getting NOOP every 30 seconds from Google, connection is reset after 1 minute");
             int character;
             while ((character = stream.read()) != -1)
                 System.out.print(String.valueOf((char) character));
             System.out.println();
         } catch (IOException e) {
             e.printStackTrace();
         }
     }
 
     @Override
     public void connect()
     {
         logger.info("connect()");
 
         this.path = "/bind";
 
         // Test the connection quality, does not actually seem to do anything else
         connectTest("/test");
     }
 
     /**
      * Establishes a new channel session with the the server.
      *
      * @private
      */
     private void open()
     {
         int rid = nextRid++;
 
         ChannelRequest request = createChannelRequest(this, "", rid);
 //        request.setExtraHeaders(this.extraHeaders);
         String requestText = this.dequeueOutgoingMaps();
         URLWithQuery uri = this.forwardChannelUri.clone();
         uri.setParameterValue("RID", Integer.toString(rid));
         if (this.clientVersion != null) {
             uri.setParameterValue("CVER", this.clientVersion);
         }
 
         request.xmlHttpPost(uri, requestText);
         this.forwardChannelRequest = request;
     }
 
     private ChannelRequest createChannelRequest(BrowserChannel browserChannel, String sessionId, int rid)
     {
         return new ChannelRequest(browserChannel, sessionId, rid, 0);
     }
 
     public void connectTest(String testPath)
     {
         logger.info("Get Host Prefixes");
         int channelVersion = 8;
         String mode = "init";
         long lastSequenceNumber = -1;
         String random = "5z7qn2t4bnz7";
         int retries = 1;
         try {
             URL url = new URL(baseUrl + testPath + "?id=" + modelId + "&access_token=" + getAccessToken() + "&sid=" + session.getId() + "&VER=" + channelVersion + "&lsq=" + lastSequenceNumber + "&MODE=" + mode + "&zx=" + random + "&t=" + retries);
             HttpURLConnection connection = (HttpURLConnection) url.openConnection();
             BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
             String line;
             while ((line = reader.readLine()) != null)
                 logger.debug(line);
             reader.close();
             // TODO: decode / store optional host prefixes
         } catch (IOException e) {
             e.printStackTrace();
         }
     }
 
     public void connectChannel()
     {
         this.forwardChannelUri = getForwardChannelUri(path);
         ensureForwardChannel();
     }
 
     private void ensureForwardChannel()
     {
         if (this.forwardChannelRequest != null) {
             // connection in process - no need to start a new request
             return;
         }
 
         startForwardChannel();
     }
 
     private void startForwardChannel()
     {
         logger.info("startForwardChannel");
 
         String RID = "66172";
         int clientVersion = 1;
         String random = "i9dw6xv4b81e";
         byte[] rawData = new byte[0];
         String urlParameters = "id=" + modelId + "&access_token=" + getAccessToken() + "&sid=" + session.getId() + "&VER=" + channelVersion + "&lsq=" + lastSequenceNumber + "&RID=" + RID + "&CVER=" + clientVersion + "&zx=" + random + "&t=" + retries;
         try {
             URL url = new URL(baseUrl + "/bind?" + urlParameters);
             HttpURLConnection connection = (HttpURLConnection) url.openConnection();
             connection.setDoOutput(true);
             connection.setDoInput(true);
             connection.setInstanceFollowRedirects(false);
             connection.setRequestMethod("POST");
             connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
             connection.setRequestProperty("charset", "utf-8");
             connection.setRequestProperty("Content-Length", "0");
             connection.setUseCaches(false);
             connection.getOutputStream().write(rawData);
             BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
             String response = "";
             String line;
             while ((line = reader.readLine()) != null)
                 response += line;
             reader.close();
         } catch (MalformedURLException e) {
             e.printStackTrace();
         } catch (ProtocolException e) {
             e.printStackTrace();
         } catch (IOException e) {
             e.printStackTrace();
         }
 
 //       // Google API compliant implementation;
 //        if (!okToMakeRequest()) {
 //            return;
 //        } else if (state == State.INIT) {
 //            open();
 //            state = State.OPENING;
 //        } else if (this.state == State.OPENED) {
 //            if (outgoingMaps.size() == 0) {
 //                logger.debug("startForwardChannel returned: nothing to send");
 //
 //                return;
 //            }
 //
 //            if (forwardChannelRequest != null) {
 //                logger.info("startForwardChannel returned: connection already in progress");
 //            }
 //
 //            makeForwardChannelRequest();
 //            logger.debug("startForwardChannel finished, sent request");
 //        }
     }
 
     private void makeForwardChannelRequest()
     {
         int rid;
         String requestText;
 
         rid = this.nextRid++;
         requestText = dequeueOutgoingMaps();
 
         URLWithQuery uri = forwardChannelUri.clone();
 
         uri.setParameterValue("SID", this.session.getId());
         uri.setParameterValue("RID", Integer.toString(rid));
         uri.setParameterValue("AID", Integer.toString(this.lastArrayId));
         //addAdditionalParams(uri);
 
         ChannelRequest request = createChannelRequest(this, session.getId(), rid);
 
         //request.setExtraHeaders(this.extraHeaders);
 
         request.xmlHttpPost(uri, requestText);
     }
 
     private String dequeueOutgoingMaps()
     {
         // TODO: implement dequeueing
         return "";
     }
 
     private boolean okToMakeRequest()
     {
         return true;
     }
 
     @Override
     public void disconnect()
     {
         logger.info("disconnect()");
 
         // TODO: Cancel outstanding requests
     }
 
     @Override
     public void send(Message message)
     {
         // TODO Auto-generated method stub
 
     }
 
     @Override
     public void addMessageHandler(MessageHandler handler)
     {
         // TODO Auto-generated method stub
 
     }
 
     @Override
     public void removeMessageHandler(MessageHandler handler)
     {
         // TODO Auto-generated method stub
 
     }
 
     @Override
     public void isClosed()
     {
         // TODO Auto-generated method stub
 
     }
 
     @Override
     public void getState()
     {
         // TODO Auto-generated method stub
 
     }
 
     private String getModelId(String driveFileId)
     {
         JsonFactory jsonFactory = credentials.getJsonFactory();
         if (jsonFactory == null)
             return null;
 
         // Set up parameters
         Map<String, String> parameters = new HashMap<String, String>();
         parameters.put("id", driveFileId);
         parameters.put("access_token", credentials.getAccessToken());
 
         try {
             // Create connection
             URLWithQuery urlq = new URLWithQuery(new URL(baseUrl + "/modelid"), parameters);
             HttpURLConnection connection = (HttpURLConnection) urlq.getURL().openConnection();
             InputStream in = new SkipGarbageInputStream(connection.getInputStream());
 
             // Parse response
             Model mid = jsonFactory.fromInputStream(in, Model.class);
             in.close();
             return mid.getId();
         } catch (IOException e) {
             return null;
         }
     }
 
     public Session getSession()
     {
         JsonFactory jsonFactory = credentials.getJsonFactory();
         if (jsonFactory == null || modelId == null)
             return null;
 
         // Set up parameters
         Map<String, String> parameters = new HashMap<String, String>();
         parameters.put("id", modelId);
         parameters.put("access_token", credentials.getAccessToken());
 
         try {
             // Create connection
             URLWithQuery urlq = new URLWithQuery(new URL(baseUrl + "/gs"), parameters);
             HttpURLConnection connection = (HttpURLConnection) urlq.getURL().openConnection();
             InputStream in = new SkipGarbageInputStream(connection.getInputStream());
 
             // Parse response
             Session session = jsonFactory.fromInputStream(in, Session.class);
             in.close();
             return session;
         } catch (IOException e) {
             return null;
         }
     }
 
     private void getHostPrefixes()
     {
 //        // Test channel
 //        System.out.println("Get Host Prefixes");
 //        int version = 8;
 //        String mode = "init";
 //        long lastSequenceNumber = -1;
 //        String random = "5z7qn2t4bnz7";
 //        int retries = 1;
 //        url = new URL(baseURL + "/test?id=" + modelId + "&access_token=" + getAccessToken() + "&sid=" + sessionId + "&VER=" + version + "&lsq=" + lastSequenceNumber + "&MODE=" + mode + "&zx=" + random + "&t=" + retries);
 //        connection = (HttpURLConnection) url.openConnection();
 //        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
 //        String line;
 //        while((line = reader.readLine()) != null)
 //            System.out.println(line);
 //        reader.close();
     }
 
     private void bufferingProxyTest()
     {
 //        // Test chunking channel
 //        System.out.println("Buffering Proxy Test (11111: first chunk, 2: second chunk)");
 //        String type = "xmlhttp";
 //        random = "0h7we2q4bnv9";
 //        url = new URL(baseURL + "/test?id=" + modelId + "&access_token=" + getAccessToken() + "&sid=" + sessionId + "&VER=" + version + "&lsq=" + lastSequenceNumber + "&TYPE=" + type + "&zx=" + random + "&t=" + retries);
 //        connection = (HttpURLConnection) url.openConnection();
 //        InputStreamReader stream = new InputStreamReader(connection.getInputStream());
 //        int character;
 //        while((character = stream.read()) != -1)
 //            System.out.print(String.valueOf((char)character));
 //        System.out.println();
     }
 
     /**
      * Gets the Uri used for the connection that sends data to the server.
      *
      * @return {goog.Uri} The forward channel URI.
      */
     public URLWithQuery getForwardChannelUri(String path)
     {
         return createDataUri(null, path);
     }
 
     /**
      * Creates a data Uri applying logic for secondary hostprefix, port
      * overrides, and versioning.
      *
      * @return {goog.Uri} The data URI.
      */
     private URLWithQuery createDataUri(String hostPrefix, String path)
     {
         String hostName;
         if (hostPrefix != null) {
             hostName = hostPrefix + "." + baseUrl;
         } else {
             hostName = baseUrl;
         }
 
         // Set up parameters
         Map<String, String> parameters = new HashMap<String, String>();
 
         // Add the protocol version to the URI.
         parameters.put("VER", Integer.toString(this.channelVersion));
 
         // Add the reconnect parameters.
         // TODO: track additional parameters for reconnecting purposes, if necessary
         // this.addAdditionalParams_(uri);
 
         URLWithQuery urlq = null;
         try {
             urlq = new URLWithQuery(new URL(baseUrl + path), parameters);
         } catch (MalformedURLException e) {
             logger.error("createDataUri: MalformedURLException");
             e.printStackTrace();
         }
 
         return urlq;
     }
 
     private String getAccessToken() {
         return credentials.getAccessToken();
     }
 }
