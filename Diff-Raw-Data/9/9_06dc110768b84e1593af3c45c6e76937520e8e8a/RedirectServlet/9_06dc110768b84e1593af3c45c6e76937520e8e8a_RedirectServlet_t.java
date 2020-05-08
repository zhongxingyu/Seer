 package org.gatein.portletbox;
 
 import java.io.IOException;
 import java.util.List;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 /**
  * This servlet redirects to the embed portlet container initialized with the known portlets in this application.
  * It is declared as a default servlet (i.e on '/') and is matched when no other servlet is matched.
  *
  * @author Julien Viet
  */
 public class RedirectServlet extends HttpServlet {
 
	private static final long serialVersionUID = 4802312414356615240L;

     private final List<String> portletNames;
 
     public RedirectServlet(List<String> portletNames) {
         this.portletNames = portletNames;
     }
 
     @Override
     protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
 
         //
         StringBuilder buffer = new StringBuilder();
         buffer.append(req.getContextPath());
         buffer.append("/embed");
         for (String portletName : portletNames) {
             // We suppose the portlet names don't contain chars we need to encode
             buffer.append('/').append(portletName);
         }
 
         // Redirect
         resp.sendRedirect(buffer.toString());
     }
 }
