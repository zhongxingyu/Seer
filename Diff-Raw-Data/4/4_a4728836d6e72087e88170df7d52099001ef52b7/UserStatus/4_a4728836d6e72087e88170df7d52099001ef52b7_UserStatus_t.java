 /*
 * Copyright (C) 2013 StackFrame, LLC
 * This code is licensed under GPLv2.
  */
 package com.stackframe.sarariman.statusboard;
 
 import com.stackframe.sarariman.Employee;
 import java.io.IOException;
 import java.io.PrintWriter;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 /**
  *
  * @author mcculley
  */
 public class UserStatus extends HttpServlet {
 
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
     protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
         Employee user = (Employee)request.getAttribute("user");
         response.setContentType("text/csv;charset=UTF-8");
         PrintWriter out = response.getWriter();
         try {
             out.println(String.format("Unclosed Tickets,%d", user.getUnclosedTickets().size()));
             out.println(String.format("Paid Time Off,%s", user.getPaidTimeOff()));
         } finally {
             out.close();
         }
     }
 
     /**
      * Returns a short description of the servlet.
      *
      * @return a String containing servlet description
      */
     @Override
     public String getServletInfo() {
         return "Provide status for the user in CSV suitable for Status Board";
     }
 
 }
