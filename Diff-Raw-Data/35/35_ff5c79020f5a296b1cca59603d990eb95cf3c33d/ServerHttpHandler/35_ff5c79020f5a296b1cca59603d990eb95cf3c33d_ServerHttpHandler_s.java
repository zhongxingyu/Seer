 package net.pixelcop.monstrous.server;
 
 import java.io.IOException;
 
 import net.pixelcop.monstrous.Job;
 import net.pixelcop.monstrous.Node;
 import net.pixelcop.monstrous.Stats;
 import net.pixelcop.monstrous.http.AbstractHttpHandler;
 import net.pixelcop.monstrous.http.JsonUtils;
 
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpEntityEnclosingRequest;
 import org.apache.http.HttpException;
 import org.apache.http.HttpRequest;
 import org.apache.http.HttpResponse;
 import org.apache.http.protocol.HttpContext;
 import org.apache.http.util.EntityUtils;
 
 public class ServerHttpHandler extends AbstractHttpHandler {
     
     @Override
     public void handle(HttpRequest request, HttpResponse response, HttpContext context)
             throws HttpException, IOException {
         
         String uri = request.getRequestLine().getUri();
         
         if (uri.equalsIgnoreCase("/client/register")) {
             System.out.println("server: registering new client");
             Server.getInstance().addClient(new Node(getRemoteInetAddress(context)));
             ret(response, "OK");
             return;
         }
         
         if (uri.equalsIgnoreCase("/client/deregister")) {
             System.out.println("server: deregistering client");
             Server.getInstance().removeClient(getRemoteInetAddress(context).getHostAddress());
             ret(response, "OK");
             return;
         }
         
         if (uri.equalsIgnoreCase("/job/new")) {
             System.out.println("server: starting new job");
             Job job = (Job) JsonUtils.fromJsonString(readBody(request), Job.class);
             Server.getInstance().createJob(job);
             ret(response, "OK");
             return;
         }
         
         if (uri.equalsIgnoreCase("/stats/report")) {
             System.out.println("server: getting stats from client");
             Stats stats = (Stats) JsonUtils.fromJsonString(readBody(request), Stats.class);
             
             Server server = Server.getInstance();
             server.addStats(stats);
             
             if (server.isJobComplete()) {
                 System.out.println();
                 System.out.println("server: job completed!");
                server.getStats().print(server.getStartTime());
                 System.out.println();
             }
             
             ret(response, "OK");
             return;
         }
         
     }
 
     /**
      * Read the request body and return it as a string
      * 
      * @param request
      * @return
      */
     private String readBody(HttpRequest request) {
         
         HttpEntity entity = ((HttpEntityEnclosingRequest) request).getEntity();
         try {
             return EntityUtils.toString(entity);
         } catch (Exception e) {
             e.printStackTrace();
             return null;
         }
         
     }
 
 }
