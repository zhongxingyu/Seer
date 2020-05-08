 /*
  * Copyright (C) 2012 StackFrame, LLC
  * This code is licensed under GPLv2.
  */
 package com.stackframe.sarariman.tickets;
 
 import com.stackframe.sarariman.EmailDispatcher;
 import com.stackframe.sarariman.Employee;
 import com.stackframe.sarariman.Sarariman;
 import java.io.IOException;
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.SQLException;
 import java.util.Collection;
 import javax.mail.internet.InternetAddress;
 import javax.naming.InitialContext;
 import javax.naming.NamingException;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.sql.DataSource;
 
 /**
  *
  * @author mcculley
  */
 public class TextUpdateHandler extends HttpServlet {
 
     private Connection openConnection() throws SQLException {
         try {
             DataSource source = (DataSource)new InitialContext().lookup("java:comp/env/jdbc/sarariman");
             return source.getConnection();
         } catch (NamingException e) {
             throw new RuntimeException(e);
         }
     }
 
     private void update(int ticket, String table, String text, int updater) throws SQLException {
         Connection connection = openConnection();
         try {
             PreparedStatement ps = connection.prepareStatement(String.format("INSERT INTO ticket_%s (ticket, %s, employee) VALUES(?, ?, ?)", table, table));
             try {
                 ps.setInt(1, ticket);
                 ps.setString(2, text);
                 ps.setInt(3, updater);
                 ps.execute();
             } finally {
                 ps.close();
             }
         } finally {
             connection.close();
         }
     }
 
     private void sendNameChangeEmail(int ticket, Employee updater, String name, String viewURL, Collection<InternetAddress> to) {
         String messageSubject = String.format("ticket %d name changed to \"%s\"", ticket, name);
         String messageBody = String.format("%s changed the name of ticket %d to \"%s\".\n\nGo to %s to view.", updater.getDisplayName(), ticket, name, viewURL);
         Sarariman sarariman = (Sarariman)getServletContext().getAttribute("sarariman");
         sarariman.getEmailDispatcher().send(to, null, messageSubject, messageBody);
     }
 
     private void sendDescriptionChangeEmail(int ticket, Employee updater, String description, String viewURL, Collection<InternetAddress> to) {
         String messageSubject = String.format("ticket %d: description changed", ticket);
         String messageBody = String.format("%s changed the description of ticket %d (%s) to:\n\n%s", updater.getDisplayName(), ticket, viewURL, description);
         Sarariman sarariman = (Sarariman)getServletContext().getAttribute("sarariman");
         sarariman.getEmailDispatcher().send(to, null, messageSubject, messageBody);
     }
 
     private void sendCommentEmail(int ticket, Employee updater, String comment, String viewURL, Collection<InternetAddress> to) {
         String messageSubject = String.format("ticket %d: commented", ticket);
         String messageBody = String.format("%s commented on ticket %d (%s):\n\n%s", updater.getDisplayName(), ticket, viewURL, comment);
         Sarariman sarariman = (Sarariman)getServletContext().getAttribute("sarariman");
         sarariman.getEmailDispatcher().send(to, null, messageSubject, messageBody);
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
         int ticket = Integer.parseInt(request.getParameter("ticket"));
         String table = request.getParameter("table");
         String text = request.getParameter("text");
         Employee updater = (Employee)request.getAttribute("user");
         try {
             // FIXME: Check table name before update to defend against injection.
             update(ticket, table, text, updater.getNumber());
             Ticket ticketBean = new Ticket();
             ticketBean.setId(ticket);
            if (table.equals("name")) {
                 sendDescriptionChangeEmail(ticket, updater, text, request.getHeader("Referer"), EmailDispatcher.addresses(ticketBean.getStakeholders()));
            } else if (table.equals("description")) {
                 sendNameChangeEmail(ticket, updater, text, request.getHeader("Referer"), EmailDispatcher.addresses(ticketBean.getStakeholders()));
             } else if (table.equals("comment")) {
                 sendCommentEmail(ticket, updater, text, request.getHeader("Referer"), EmailDispatcher.addresses(ticketBean.getStakeholders()));
             } else {
                 throw new IllegalArgumentException("invalid table: " + table);
             }
 
             response.sendRedirect(request.getHeader("Referer"));
         } catch (SQLException se) {
             throw new ServletException(se);
         }
     }
 
     /**
      * Returns a short description of the servlet.
      *
      * @return a String containing servlet description
      */
     @Override
     public String getServletInfo() {
         return "handles updates for text associated with a ticket";
     }
 
 }
