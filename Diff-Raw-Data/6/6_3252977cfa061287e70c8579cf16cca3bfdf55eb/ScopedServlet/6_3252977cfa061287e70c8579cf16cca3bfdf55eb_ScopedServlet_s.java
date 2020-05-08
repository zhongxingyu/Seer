 package com.wideplay.example.servlets;
 
 import com.google.inject.Singleton;
 import com.google.inject.Inject;
 import com.wideplay.warp.servlet.RequestScoped;
 
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.ServletException;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.logging.Logger;
 
 /**
  * Created with IntelliJ IDEA.
  * User: dhanji
  * Date: Dec 20, 2007
  * Time: 1:44:20 PM
  *
  * This servlet is re-instantiated on every request (see the logs to prove it!).
  *
  * NOTE that this servlet should NOT directly be registered in warp-servlet!!! Instead it is wrapped by
  * ScopedServletWrappingServlet.
  *
  * @author Dhanji R. Prasanna (dhanji gmail com)
  * 
  */
 @RequestScoped
 public class ScopedServlet extends HttpServlet {
    @Inject
    Logger logger;
 
    public ScopedServlet() {
         logger.info(ScopedServlet.class.getName() + " instantiated!");
     }
 
     //this servlet is also managed by guice, so you can inject, scope or intercept it as you please.
 
 
     protected void doGet(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws ServletException, IOException {
         //lets say hi!
         final PrintWriter out = httpServletResponse.getWriter();
 
         out.println("<html><head><title>Warp::Servlet powered servlet</title></head>");
         out.println("<body>");
         out.println("Hello from a Request-scoped servlet powered by Warp::Servlet and Google Guice! This servlet is created every request (see logs)");
         out.println("and works via delegation from a wrapping singleton servlet.");
         out.println("</body></html>");
 
         //write!
         out.flush();
     }
 }
