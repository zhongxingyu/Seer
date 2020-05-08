 /*
  * Copyright (C) 2010 StackFrame, LLC
  * This code is licensed under GPLv2.
  */
 package com.stackframe.sarariman;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.sql.Connection;
 import java.sql.PreparedStatement;
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
 public class EmailBuilder extends HttpServlet {
 
     /** 
      * Handles the HTTP <code>POST</code> method.
      * @param request servlet request
      * @param response servlet response
      * @throws ServletException if a servlet-specific error occurs
      * @throws IOException if an I/O error occurs
      */
     @Override
     protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
         Collection<MimeBodyPart> attachments = new ArrayList<MimeBodyPart>();
         String[] documentLinks = request.getParameterValues("documentLink");
         String[] documentNames = request.getParameterValues("documentName");
         for (int i = 0; i < documentLinks.length; i++) {
             RequestDispatcher requestDispatcher = request.getRequestDispatcher(documentLinks[i]);
             ModifiableRequest tmpRequest = new ModifiableRequest(request);
             tmpRequest.setMethod("GET");
             ContentCaptureServletResponse capContent = new ContentCaptureServletResponse(response);
             requestDispatcher.include(tmpRequest, capContent);
             byte[] data = capContent.getContentBytes();
             MimeBodyPart mbp = new MimeBodyPart();
             try {
                 mbp.setContent(data, capContent.getContentType());
                 mbp.setFileName(documentNames[i]);
                 attachments.add(mbp);
             } catch (MessagingException me) {
                 IOException ioe = new IOException();
                 ioe.initCause(me);
                 throw ioe;
             }
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
 
         String subject = request.getParameter("subject");
         String body = request.getParameter("body");
 
         // Decode special characters because they aren't sent as characters by the JSP code.
         body = body.replaceAll("\\\\n", "\n");
         body = body.replaceAll("\\\\t", "\t");
 
         InternetAddress from;
         try {
             from = new InternetAddress(request.getParameter("from"));
         } catch (AddressException ae) {
             throw new ServletException(ae);
         }
 
         String testAddress = request.getParameter("testaddress");
         if (testAddress != null && testAddress.length() > 0) {
             try {
                 to = Collections.singleton(new InternetAddress(testAddress));
                 cc = null;
             } catch (AddressException ae) {
                 throw new ServletException(ae);
             }
         }
 
         final int employee = ((Employee)request.getAttribute("user")).getNumber();
         final int invoiceNumber = Integer.parseInt(request.getParameter("invoiceNumber"));
         final Sarariman sarariman = (Sarariman)getServletContext().getAttribute("sarariman");
 
        Runnable postSendAction = testAddress == null ? null : new Runnable() {
 
             public void run() {
                 Connection connection = sarariman.openConnection();
                 try {
                     PreparedStatement ps = connection.prepareStatement("INSERT INTO invoice_email_log (invoice, sender) VALUES(?, ?)");
                     try {
                         ps.setInt(1, invoiceNumber);
                         ps.setInt(2, employee);
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
 
         sarariman.getEmailDispatcher().send(from, to, cc, subject, body, attachments, postSendAction);
 
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
         return "Generates emails with attached documents";
     }
 
 }
