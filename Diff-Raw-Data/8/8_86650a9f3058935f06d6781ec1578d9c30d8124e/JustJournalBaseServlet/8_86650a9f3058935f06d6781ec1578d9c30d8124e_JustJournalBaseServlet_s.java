 package com.justjournal;
 
 import javax.servlet.ServletConfig;
 import javax.servlet.ServletException;
 import javax.servlet.ServletOutputStream;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 /**
  * User: laffer1
  * Date: Sep 25, 2005
  * Time: 9:04:00 PM
  */
 public class JustJournalBaseServlet extends HttpServlet {
     protected static final char endl = '\n';
 
     /**
      * Initializes the servlet.
      */
     public void init(ServletConfig config) throws ServletException {
         super.init(config);
     }
 
     /**
      * Handles the HTTP <code>GET</code> method.
      *
      * @param request  servlet request
      * @param response servlet response
      */
     protected void doGet(HttpServletRequest request, HttpServletResponse response)
             throws java.io.IOException {
         processRequest(request, response);
     }
 
     /**
      * Handles the HTTP <code>POST</code> method.
      *
      * @param request  servlet request
      * @param response servlet response
      */
     protected void doPost(HttpServletRequest request, HttpServletResponse response)
             throws java.io.IOException {
         processRequest(request, response);
     }
 
     protected void processRequest(HttpServletRequest request, HttpServletResponse response)
             throws java.io.IOException {
         final StringBuffer sb = new StringBuffer();
         final HttpSession session = request.getSession(true);
 
         response.setContentType("text/html");
         response.setDateHeader("Expires", System.currentTimeMillis());
         response.setDateHeader("Last-Modified", System.currentTimeMillis());
         response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate");
         response.setHeader("Pragma", "no-cache");
 
         execute(request, response, session, sb);
 
         final ServletOutputStream outstream = response.getOutputStream();
         outstream.println(sb.toString());
         outstream.flush();
     }
 
     protected void execute(HttpServletRequest request, HttpServletResponse response, HttpSession session, StringBuffer sb) {
 
     }
 
     protected String fixInput(HttpServletRequest request, String input) {
         String fixed = request.getParameter(input);
 
         if (fixed == null)
             fixed = "";
 
         return fixed.trim();
     }
 }
