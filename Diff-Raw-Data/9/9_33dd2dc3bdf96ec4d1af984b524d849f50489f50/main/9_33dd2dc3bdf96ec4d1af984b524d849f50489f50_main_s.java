 import org.vertx.java.core.Handler;
 import org.vertx.java.core.http.HttpServerRequest;
 import org.vertx.java.deploy.Verticle;
 
 public class main extends Verticle {
 
     public void start() {
         vertx.createHttpServer().requestHandler(new Handler<HttpServerRequest>() {
             public void handle(final HttpServerRequest req) {
                 switch (req.uri) {
                     case "/imagetest.html":
                         buildHTML(req);
                         break;
                     case "/img/frau.jpg":
                         sendImage(req);
                         break;
                     default:
                         System.out.print(req.uri);
                 }
             }
         }).listen(1234);
     }
 
     /**
      * blabla
      */
     public void buildHTML(final HttpServerRequest req) {
         req.response.end("" +
                 "<html>" +
                 "<header>" +
                 "</header>" +
                 "<body>" +
                 "<h1>" +
                 "TEEEST" +
                 "</h1>" +
                 "<img src=\"./img/frau.jpg\" />" +
                 "</body>" +
                 "</html>");
     }
 
     public void sendImage(final HttpServerRequest req) {
         req.response.sendFile("img/3D_And_Fantasy_Girls_16.jpg");
     }
 }
