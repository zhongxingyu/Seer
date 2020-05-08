 #set( $symbol_pound = '#' )
 #set( $symbol_dollar = '$' )
 #set( $symbol_escape = '\' )
 package ${package};
 
 import fr.ybonnel.simpleweb4j.exception.HttpErrorException;
 import fr.ybonnel.simpleweb4j.handlers.Response;
 import fr.ybonnel.simpleweb4j.handlers.Route;
import ${groupId}.simpleweb4j.handlers.RouteParameters;
 
import static ${groupId}.simpleweb4j.SimpleWeb4j.*;
 
 public class HelloWorld {
 
     public static class Hello {
         public String value = "Hello World";
     }
 
     public static void startServer(int port, boolean waitStop) {
         setPort(port);
         setPublicResourcesPath("/${packageInPathFormat}/public");
 
         get(new Route<Void, Hello>("/hello", Void.class) {
             @Override
             public Response<Hello> handle(Void param, RouteParameters routeParams) throws HttpErrorException {
                 return new Response<>(new Hello());
             }
         });
 
         start(waitStop);
     }
 
     public static void main(String[] args) {
         // Default port 9999.
         // For main, we want to wait the stop.
         startServer(9999, true);
     }
 }
