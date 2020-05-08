 package nl.astraeus.forum.web;
 
 import nl.astraeus.http.SimpleWebServer;
 
 /**
  * User: rnentjes
  * Date: 3/25/12
  * Time: 1:09 PM
  */
 public class HttpServer {
     
     public static void main(String [] args) throws Exception {
        int port = 8080;

        if (args.length == 1) {
            port = Integer.parseInt(args[0]);
        }

        SimpleWebServer server = new SimpleWebServer(port);
 
         server.addServlet(new ResourceServlet(), "/resources/*");
         server.addServlet(new ForumServlet(), "/");
 
         server.start();
     }
 
 }
