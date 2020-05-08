 /*
  * Copyright (C) 2013 StackFrame, LLC
  * This code is licensed under GPLv2.
  */
 package com.stackframe.sarariman;
 
 import java.io.IOException;
import javax.servlet.FilterConfig;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 /**
  *
  * @author mcculley
  */
 public class AuthCheck extends HttpServlet {
 
     private Directory directory;
 
     public void init() throws ServletException {
         super.init();
         Sarariman sarariman = (Sarariman)getServletContext().getAttribute("sarariman");
         directory = sarariman.getDirectory();
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
     protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
         String username = request.getParameter("username");
         String password = request.getParameter("password");
 
         int domainIndex = username.indexOf('@');
         if (domainIndex != -1) {
             username = username.substring(0, domainIndex); // FIXME: Check for proper domain and dispatch.
         }
 
         boolean valid = directory.checkCredentials(username, password);
         HttpSession session = request.getSession();
         if (valid) {
             Employee user = directory.getByUserName().get(username);
             session.setAttribute("user", user);
 
            // FIXME: All of the client code expects that the request holds the user object. Maybe change client code to use the session?
            request.setAttribute("user", user);

             session.setAttribute("authFailed", null);
             String destination = request.getParameter("destination");
             if (destination == null) {
                 destination = request.getContextPath();
             }
 
             response.sendRedirect(destination);
         } else {
             session.setAttribute("authFailed", true);
             response.sendRedirect(request.getContextPath() + "/login");
         }
     }
 
     /**
      * Returns a short description of the servlet.
      *
      * @return a String containing servlet description
      */
     @Override
     public String getServletInfo() {
         return "Handler for authentication";
     }
 
 }
