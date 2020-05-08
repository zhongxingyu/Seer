 package edu.depaul.cdm.se.sbejbwebclient.servlet;
 
 import edu.depaul.cdm.se.sbejb.RGreeterBeanRemote;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.naming.Context;
 import javax.naming.InitialContext;
 import javax.naming.NamingException;
 import javax.servlet.ServletException;
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 @WebServlet(name = "RemoteGreeterServlet", urlPatterns = {"/RemoteGreeterServlet"})
 public class RemoteGreeterServlet extends HttpServlet {
 
     /**
      * Processes requests for both HTTP
      * <code>GET</code> and
      * <code>POST</code> methods.
      *
      * @param request servlet request
      * @param response servlet response
      * @throws ServletException if a servlet-specific error occurs
      * @throws IOException if an I/O error occurs
      */
     protected void processRequest(HttpServletRequest request, HttpServletResponse response)
             throws ServletException, IOException {
         response.setContentType("text/html;charset=UTF-8");
         PrintWriter out = response.getWriter();
         try {
             out.println("<html>");
             out.println("<head>");
             out.println("<title>Servlet GreetingServlet</title>");
             out.println("</head>");
             out.println("<body>");
             out.println("<h1>Servlet GreetingServlet at " + request.getContextPath() + "</h1>");
             out.println("<h1>Servlet GreetingServlet at " + getMessage() + "</h1>");
             out.println("</body>");
             out.println("</html>");
         } finally {
             out.close();
         }
     }
 
     private String getMessage() {
         String retval = "";
         try {
             final String lookupKey = "java:global/edu.depaul.cdm.se_sb-ejb_ejb_1.0-SNAPSHOT/RGreeterBean";
             Context context = new InitialContext();
             RGreeterBeanRemote remote = (RGreeterBeanRemote) context.lookup(lookupKey);
             retval = remote.greetMe("Paul");
         } catch (NamingException ex) {
             Logger.getLogger(RemoteGreeterServlet.class.getName()).log(Level.SEVERE, null, ex);
         }
         return retval;
     }
 
     // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
     /**
      * Handles the HTTP
      * <code>GET</code> method.
      *
      * @param request servlet request
      * @param response servlet response
      * @throws ServletException if a servlet-specific error occurs
      * @throws IOException if an I/O error occurs
      */
     @Override
     protected void doGet(HttpServletRequest request, HttpServletResponse response)
             throws ServletException, IOException {
         processRequest(request, response);
     }
 
     /**
      * Handles the HTTP
      * <code>POST</code> method.
      *
      * @param request servlet request
      * @param response servlet response
      * @throws ServletException if a servlet-specific error occurs
      * @throws IOException if an I/O error occurs
      */
     @Override
     protected void doPost(HttpServletRequest request, HttpServletResponse response)
             throws ServletException, IOException {
         processRequest(request, response);
     }
 
     /**
      * Returns a short description of the servlet.
      *
      * @return a String containing servlet description
      */
     @Override
     public String getServletInfo() {
         return "Short description";
     }// </editor-fold>
 }
