 /*
  * Copyright (C) 2010 StackFrame, LLC
  * This code is licensed under GPLv2.
  */
 package com.stackframe.sarariman;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import javax.mail.MessagingException;
 import javax.mail.internet.AddressException;
 import javax.mail.internet.InternetAddress;
 import javax.mail.internet.MimeBodyPart;
 import javax.servlet.RequestDispatcher;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 /**
  *
  * @author mcculley
  */
 public class PDFTimesheetBuilder extends HttpServlet {
 
     /** 
      * Handles the HTTP <code>POST</code> method.
      * @param request servlet request
      * @param response servlet response
      * @throws ServletException if a servlet-specific error occurs
      * @throws IOException if an I/O error occurs
      */
     @Override
     protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
         final Sarariman sarariman = (Sarariman)getServletContext().getAttribute("sarariman");
         Collection<MimeBodyPart> attachments = new ArrayList<MimeBodyPart>();
         String[] pdfs = request.getParameterValues("pdf");
         String[] employees = request.getParameterValues("employee");
         for (int i = 0; i < pdfs.length; i++) {
             RequestDispatcher requestDispatcher = request.getRequestDispatcher(pdfs[i]);
             ModifiableRequest tmpRequest = new ModifiableRequest(request);
             tmpRequest.setMethod("GET");
             ContentCaptureServletResponse capContent = new ContentCaptureServletResponse(response);
             requestDispatcher.include(tmpRequest, capContent);
             byte[] data = capContent.getContentBytes();
             MimeBodyPart mbp = new MimeBodyPart();
             try {
                 mbp.setContent(data, capContent.getContentType());
                 mbp.setFileName(String.format("%s-%s.pdf", employees[i], request.getParameter("week")));
                 attachments.add(mbp);
             } catch (MessagingException me) {
                 IOException ioe = new IOException();
                 ioe.initCause(me);
                 throw ioe;
             }
         }
 
         StringBuilder body = new StringBuilder("Please find attached ");
         body.append(employees.length == 1 ? "the timesheet" : "timesheets");
         body.append(" for");
         if (employees.length == 1) {
             body.append(" " + employees[0] + ".\n");
         } else {
             body.append(":\n");
             for (String employee : employees) {
                 body.append('\t' + employee + '\n');
             }
         }
 
         body.append("\n");
         body.append("Project: " + request.getParameter("project") + '\n');
         if (request.getParameter("contract") != null) {
             body.append("Contract: " + request.getParameter("contract") + '\n');
         }
 
         if (request.getParameter("subcontract") != null) {
             body.append("Subcontract: " + request.getParameter("subcontract") + '\n');
         }
 
         body.append("Week: " + request.getParameter("week") + '\n');
         body.append("\n");
 
         String[] noHoursEmployees = request.getParameterValues("noHoursEmployee");
         if (noHoursEmployees != null && noHoursEmployees.length > 0) {
             body.append("There were no hours this week for:\n");
             for (String noHoursEmployee : noHoursEmployees) {
                 body.append('\t' + noHoursEmployee + '\n');
             }
 
             body.append("\n");
         }
 
         Collection<InternetAddress> to = new ArrayList<InternetAddress>();
         for (String toAddress : request.getParameterValues("to")) {
             try {
                 to.add(new InternetAddress(toAddress));
             } catch (AddressException ae) {
                 throw new ServletException(ae);
             }
         }
 
         Collection<InternetAddress> cc = new ArrayList<InternetAddress>();
         String[] ccAddresses = request.getParameterValues("cc");
         if (ccAddresses != null) {
             for (String ccAddress : ccAddresses) {
                 try {
                     cc.add(new InternetAddress(ccAddress));
                 } catch (AddressException ae) {
                     throw new ServletException(ae);
                 }
             }
         }
 
         String subject = employees.length == 1 ? "timesheet" : "timesheets";
 
         String testAddress = request.getParameter("testaddress");
         if (testAddress != null && testAddress.length() > 0) {
             try {
                 to = Collections.singleton(new InternetAddress(testAddress));
                 cc = null;
             } catch (AddressException ae) {
                 throw new ServletException(ae);
             }
         }
 
         InternetAddress from;
         try {
             from = new InternetAddress(request.getParameter("from"));
         } catch (AddressException ae) {
             throw new ServletException(ae);
         }
 
         final int projectNumber = Integer.parseInt(request.getParameter("projectNumber"));
         final int employee = ((Employee)request.getAttribute("user")).getNumber();
         final String week = request.getParameter("week");
        Runnable postSendAction = testAddress != null ? null : new Runnable() {
 
             public void run() {
                 Connection connection = sarariman.openConnection();
                 try {
                     PreparedStatement ps = connection.prepareStatement("INSERT INTO project_timesheet_email_log (project, sender, week) VALUES(?, ?, ?)");
                     try {
                         ps.setInt(1, projectNumber);
                         ps.setInt(2, employee);
                         ps.setString(3, week);
                         ps.executeUpdate();
                     } finally {
                         ps.close();
                         connection.close();
                     }
                 } catch (SQLException se) {
                     throw new RuntimeException(se);
                 }
             }
 
         };
 
         sarariman.getEmailDispatcher().send(from, to, cc, subject, body.toString(), attachments, postSendAction);
 
         response.setContentType("text/html;charset=UTF-8");
         PrintWriter out = response.getWriter();
         try {
             out.println("<html>");
             out.println("<head>");
             out.println("<title>Email Sent</title>");
             out.println("</head>");
             out.println("<body>");
             out.println("<h1>Email Sent</h1>");
             out.println("<p>email was sent.</p>");
             out.println(String.format("<p>to=%s</p>", to));
             out.println(String.format("<p>cc=%s</p>", cc));
             out.println(String.format("<p>subject=%s</p>", subject));
             out.println(String.format("<p>body=%s</p>", body));
             out.println("</body>");
             out.println("</html>");
         } finally {
             out.close();
         }
     }
 
     /** 
      * Returns a short description of the servlet.
      * @return a String containing servlet description
      */
     @Override
     public String getServletInfo() {
         return "Builds PDF documents for timesheets for a project";
     }
 
 }
