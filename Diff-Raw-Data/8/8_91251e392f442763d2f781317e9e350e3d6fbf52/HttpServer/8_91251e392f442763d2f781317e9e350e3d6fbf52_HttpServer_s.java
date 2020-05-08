 package nl.astraeus.forum.web;
 
 import nl.astraeus.http.SimpleWebServer;
 
 /**
  * User: rnentjes
  * Date: 3/25/12
  * Time: 1:09 PM
  */
 public class HttpServer {
     
     public static void main(String [] args) throws Exception {
        SimpleWebServer server = new SimpleWebServer(8080);
 
         server.addServlet(new ResourceServlet(), "/resources/*");
         server.addServlet(new ForumServlet(), "/");
 
         server.start();
     }
 
 }
