 package org.resteasy;
 
import com.wordnik.swagger.jaxrs.*;
 import com.wordnik.swagger.config.*;
import com.wordnik.swagger.model.*;
 
 import javax.servlet.http.HttpServlet;
 
 public class MyCustomBootstrap extends HttpServlet {
   static {
     ConfigFactory.config().setBasePath("http://localhost:8080/spring");
   }
 }
