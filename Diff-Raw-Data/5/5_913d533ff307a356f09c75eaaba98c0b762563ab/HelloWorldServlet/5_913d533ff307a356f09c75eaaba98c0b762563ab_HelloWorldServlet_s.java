 package com.wideplay.example.servlets;
 
 import com.google.inject.Singleton;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import java.io.IOException;
 import java.io.PrintWriter;
 
 /**
  * Created with IntelliJ IDEA.
  * User: dhanji
  * Date: Dec 20, 2007
  * Time: 1:44:20 PM
  *
  * @author Dhanji R. Prasanna (dhanji gmail com)
  */
 @Singleton
 public class HelloWorldServlet extends HttpServlet {
     //this servlet is also managed by guice, so you can inject, scope or intercept it as you please.
 
     protected void doGet(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws ServletException, IOException {
         //lets say hi!
         final PrintWriter out = httpServletResponse.getWriter();
 
         out.println("<html><head><title>Warp::Servlet powered servlet</title></head>");
         out.println("<body>");
        out.println("Hello from Managed servlet powered by Warp::Servlet and Google Guice!");
         out.println("</body></html>");
 
         //write!
         out.flush();
     }
 }
