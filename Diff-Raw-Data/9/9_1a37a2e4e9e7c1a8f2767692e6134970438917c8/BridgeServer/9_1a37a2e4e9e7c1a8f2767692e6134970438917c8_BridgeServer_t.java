package paint;
 
 import org.vertx.java.core.Handler;
 import org.vertx.java.core.http.HttpServer;
 import org.vertx.java.core.http.HttpServerRequest;
 import org.vertx.java.core.json.JsonArray;
 import org.vertx.java.core.json.JsonObject;
 import org.vertx.java.core.logging.Logger;
 import org.vertx.java.core.sockjs.SockJSServer;
 import org.vertx.java.platform.Verticle;
 
 /**
  * @author <a href="http://tfox.org">Tim Fox</a>
  */
 public class BridgeServer extends Verticle {
   Logger logger;
 
   public void start() {
     logger = container.logger();
     
     HttpServer server = vertx.createHttpServer();
 
     // Also serve the static resources. In real life this would probably be done by a CDN
     server.requestHandler(new Handler<HttpServerRequest>() {
       public void handle(HttpServerRequest req) {
         if (req.path().equals("/")) req.response().sendFile("index.html"); // Serve the index.html
         if (req.path().equals("/sender")) req.response().sendFile("sender.html"); // Serve the sender.html
         if (req.path().endsWith("vertxbus.js")) req.response().sendFile("vertxbus.js"); // Serve the js
         if (req.path().endsWith("drawStroke.js")) req.response().sendFile("drawStroke.js"); // Serve the js
         if (req.path().endsWith("leapLogic.js")) req.response().sendFile("leapLogic.js"); // Serve the js
         if (req.path().endsWith("leap.js")) req.response().sendFile("leap.js"); // Serve the js
         if (req.path().endsWith("d3.v3.min.js")) req.response().sendFile("d3.v3.min.js"); // Serve the js
         if (req.path().endsWith("touchDraw.html")) req.response().sendFile("touchDraw.html"); 
         if (req.path().endsWith("touchDraw2.html")) req.response().sendFile("touchDraw2.html"); 
         if (req.path().endsWith("touchDraw3.html")) req.response().sendFile("touchDraw3.html"); 
         if (req.path().endsWith("touchDraw4.html")) req.response().sendFile("touchDraw4.html"); 
       }
     });
 
     JsonArray permitted = new JsonArray();
     permitted.add(new JsonObject()); // Let everything through
 
     ServerHook hook = new ServerHook(logger);
 
     SockJSServer sockJSServer = vertx.createSockJSServer(server);
     sockJSServer.setHook(hook);
     sockJSServer.bridge(new JsonObject().putString("prefix", "/eventbus"), permitted, permitted);
 
 		String portAsString = System.getenv("OPENSHIFT_DIY_PORT");
 		int port = 0;
 		if (portAsString != null && !portAsString.equals("")) {
 			port = Integer.parseInt(portAsString);
 		} else {
 			port = 8000;
 		}
 		String ipAddress = System.getenv("OPENSHIFT_DIY_IP");
 		
 		if (ipAddress != null && !ipAddress.equals("")) {
 			System.out.println("Starting Server on " + ipAddress + ":" + port);
 			server.listen(port, ipAddress);
 		} else {
 			System.out.println("Starting Server on " + port );
 			server.listen(port);
 		}
   }
 }
