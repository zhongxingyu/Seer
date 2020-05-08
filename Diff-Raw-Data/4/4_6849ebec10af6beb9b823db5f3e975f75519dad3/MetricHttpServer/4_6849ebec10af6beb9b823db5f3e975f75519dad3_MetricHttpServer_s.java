 import java.io.IOException;
 import java.io.OutputStream;
 import java.net.InetSocketAddress;
 
 import com.sun.net.httpserver.Headers;
 import com.sun.net.httpserver.HttpServer;
 import com.sun.net.httpserver.HttpExchange;
 import com.sun.net.httpserver.HttpHandler;
 
 /**
  * This is a HTTP server to handle endpoint HTTP request/responses
  *
  */
 public class MetricHttpServer {
 
 
 
     public static void main(String[] args) throws IOException {
 
         HttpServer server = HttpServer.create(new InetSocketAddress(80), 0);
 
         server.createContext( "/metric", new MetricHandler() );
         server.start();
 	System.out.println("Metric Server started listening on port 80"); 
     }
 
 }
 
 class MetricHandler implements HttpHandler {
 
     public void handle(HttpExchange exchange)
     throws IOException 
     {
 
         String httpMethod = exchange.getRequestMethod();
     
         if ( "Get".equalsIgnoreCase(httpMethod) ) {
 
             Headers respHeaders = exchange.getResponseHeaders();
             respHeaders.set("Content-Type", "application/json");
 	    exchange.sendResponseHeaders(200, 0);
 
             OutputStream respBody = exchange.getResponseBody(); 
             String metric = null;
             try {
                 metric = new Metric().getUsage();
             } catch (Exception ex) {
                metric = null;
             }
             respBody.write( metric.getBytes() );
             respBody.close();
         }  
 
     }
 }
