 package org.eclipse.jetty.test;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.URI;
 import java.net.URL;
 import java.util.Properties;
 import java.util.concurrent.ExecutionException;
 import java.util.concurrent.Future;
 import java.util.concurrent.TimeUnit;
 import java.util.concurrent.TimeoutException;
 
 import org.eclipse.jetty.util.IO;
 import org.eclipse.jetty.util.UrlEncoded;
 import org.eclipse.jetty.util.log.Log;
 import org.eclipse.jetty.util.log.Logger;
 import org.eclipse.jetty.websocket.WebSocket;
 import org.eclipse.jetty.websocket.WebSocketClient;
 import org.eclipse.jetty.websocket.WebSocketClientFactory;
 
 public class TestClient
 {
     private static String getJettyVersion() throws IOException
     {
         String resource = "META-INF/maven/org.eclipse.jetty/jetty-websocket/pom.properties";
         URL url = Thread.currentThread().getContextClassLoader().getResource(resource);
         if (url == null)
         {
            return "GitMaster";
            // TODO: use git to get actual branch name?
         }
 
         InputStream in = null;
         try
         {
             in = url.openStream();
             Properties props = new Properties();
             props.load(in);
             return props.getProperty("version");
         }
         finally
         {
             IO.close(in);
         }
     }
 
     public static void main(String[] args) throws Exception
     {
         if (args.length < 2)
         {
             System.err.println("ERROR: Hostname and port not specified");
             System.err.printf("USAGE: java -cp jetty-autobahn-websocket-client.jar %s [hostname] [port]%n",TestClient.class.getName());
             System.exit(-1);
         }
 
         System.setProperty("org.eclipse.jetty.util.log.stderr.LONG","true");
         System.setProperty("org.eclipse.jetty.test.LEVEL","DEBUG");
 
         String hostname = args[0];
         int port = Integer.parseInt(args[1]);
 
         TestClient client = null;
         try
         {
            String userAgent = "JettyWebsocketClient/" + getJettyVersion();
             client = new TestClient(hostname,port,userAgent);
             client.startTestRun();
         }
         catch (Throwable t)
         {
             t.printStackTrace(System.err);
         }
         finally
         {
             if (client != null)
             {
                 client.shutdown();
             }
         }
         System.exit(0);
     }
 
     private Logger log;
     private URI baseWebsocketUri;
     private WebSocketClientFactory clientFactory;
     private String hostname;
     private int port;
     private String userAgent;
     private int currentCaseId;
     private int totalCaseCount;
 
     public TestClient(String hostname, int port, String userAgent) throws Exception
     {
         this.log = Log.getLogger(this.getClass());
         this.hostname = hostname;
         this.port = port;
         this.userAgent = userAgent;
         this.baseWebsocketUri = new URI("ws://" + hostname + ":" + port);
         this.clientFactory = new WebSocketClientFactory();
         this.clientFactory.start();
     }
 
     private int getCaseCount() throws IOException, InterruptedException
     {
         URI wsUri = baseWebsocketUri.resolve("/getCaseCount");
         WebSocketClient wsc = clientFactory.newWebSocketClient();
         OnGetCaseCount onCaseCount = new OnGetCaseCount();
         wsc.open(wsUri,onCaseCount);
         onCaseCount.awaitMessage();
         if (onCaseCount.hasCaseCount())
         {
             return onCaseCount.getCaseCount();
         }
 
         throw new IllegalStateException("Unable to get Case Count");
     }
 
     private void runNextCase() throws IOException, InterruptedException
     {
         URI wsUri = baseWebsocketUri.resolve("/runCase?case=" + currentCaseId + "&agent=" + UrlEncoded.encodeString(userAgent));
         log.debug("next uri - {}",wsUri);
         WebSocketClient wsc = clientFactory.newWebSocketClient();
         OnEchoMessage onEchoMessage = new OnEchoMessage(currentCaseId,totalCaseCount);
         Future<WebSocket.Connection> conn = wsc.open(wsUri,onEchoMessage);
 
         try
         {
             conn.get(1,TimeUnit.SECONDS);
         }
         catch (ExecutionException e)
         {
             log.warn("Unable to connect to: " + wsUri,e);
             return;
         }
         catch (TimeoutException e)
         {
             log.warn("Unable to connect to: " + wsUri,e);
             return;
         }
 
         onEchoMessage.awaitClose();
     }
 
     public void shutdown()
     {
         try
         {
             this.clientFactory.stop();
         }
         catch (Exception e)
         {
             log.warn("Unable to stop WebSocketClientFactory",e);
         }
     }
 
     private void startTestRun() throws IOException, InterruptedException
     {
         updateStatus("Running test suite...");
         updateStatus("Using Fuzzing Server: %s:%d",hostname,port);
         updateStatus("User Agent: %s",userAgent);
         currentCaseId = 1;
         totalCaseCount = getCaseCount();
         updateStatus("Will run %d cases ...",totalCaseCount);
 
         while (currentCaseId < totalCaseCount)
         {
             runNextCase();
             currentCaseId++;
         }
         updateStatus("All test cases executed.");
         updateReports();
     }
 
     private void updateReports() throws IOException, InterruptedException
     {
        URI wsUri = baseWebsocketUri.resolve("/updateReports?agent=" + UrlEncoded.encodeString(userAgent));
         WebSocketClient wsc = clientFactory.newWebSocketClient();
         OnUpdateReports onUpdateReports = new OnUpdateReports();
         wsc.open(wsUri,onUpdateReports);
         onUpdateReports.awaitClose();
     }
 
     private void updateStatus(String format, Object... args)
     {
         log.info(String.format(format,args));
     }
 }
