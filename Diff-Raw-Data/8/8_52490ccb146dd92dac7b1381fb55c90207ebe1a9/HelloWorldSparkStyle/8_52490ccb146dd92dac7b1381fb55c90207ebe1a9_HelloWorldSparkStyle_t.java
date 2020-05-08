 package org.einherjer;
 
 import spark.Request;
 import spark.Response;
 import spark.Route;
 import spark.Spark;
 

 public class HelloWorldSparkStyle {
 
     //go to localhost:4567
     public static void main(String[] args) {
         Spark.get(new Route("/") {
             @Override
             public Object handle(final Request request, final Response response) {
                 return "Hello World from Spark";
             }
         });
     }
 
 }
