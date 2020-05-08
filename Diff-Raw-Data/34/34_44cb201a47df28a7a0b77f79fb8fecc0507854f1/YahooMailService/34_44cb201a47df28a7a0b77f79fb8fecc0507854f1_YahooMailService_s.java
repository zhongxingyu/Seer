 /*
  * Copyright 2002-2012 the original author or authors.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package com.bia.yahoomailjava;
 
 import java.util.Properties;
 import java.util.concurrent.ScheduledThreadPoolExecutor;
 import java.util.concurrent.TimeUnit;
 import javax.mail.*;
 import javax.mail.internet.InternetAddress;
 import javax.mail.internet.MimeMessage;
 import org.apache.commons.validator.routines.EmailValidator;
 
 /**
  *
  * @author intesar mohammed mdshannan@gmail.com
  */
 public class YahooMailService {
 
     // set username/password and get started
     private static final String USERNAME = "<yahoo email>";
     private static final String PASSWORD = "<yahoo password>";
     //private static String EMAIL_CONTENT_TYPE = "text/html";
     //private static Logger logger = Logger.getLogger(YahooMailService.class);
     private static final YahooMailService instance = new YahooMailService();
     private ScheduledThreadPoolExecutor executor;
 
     private YahooMailService() {
         executor = new ScheduledThreadPoolExecutor(2);
     }
    private Session session;
 
     public static YahooMailService getInstance() {
         return instance;
     }
 
     /**
      *
      * @param toAddress mandatory
      * @param subject mandatory
      * @param body optional
      * @return true email send, false invalid input
      */
     public boolean sendEmail(String toAddress, String subject, String body) {
         if (!isValidEmail(toAddress) || !isValidSubject(subject)) {
             return false;
         }
 
         String[] to = {toAddress};
         // Aysnc send email
         Runnable emailServiceAsync = new EmailServiceAsync(to, subject, body);
         executor.schedule(emailServiceAsync, 1, TimeUnit.MILLISECONDS);
 
         return true;
     }
 
     /**
      *
      * @param toAddresses mandatory
      * @param subject mandatory
      * @param body optional
      * @return true email send, false invalid input
      */
     public boolean sendEmail(String[] toAddresses, String subject, String body) {
         if (!isValidEmail(toAddresses) || !isValidSubject(subject)) {
             return false;
         }
 
         // Aysnc send email
         Runnable emailServiceAsync = new EmailServiceAsync(toAddresses, subject, body);
         executor.schedule(emailServiceAsync, 1, TimeUnit.MILLISECONDS);
 
         return true;
 
     }
 
     /**
      *
      * @param emails
      * @return
      */
     private boolean isValidEmail(String... emails) {
         if (emails == null) {
             return false;
         }
         for (String email : emails) {
             if (!EmailValidator.getInstance().isValid(email)) {
                 return false;
             }
         }
         return true;
     }
 
     /**
      *
      * @param subject
      * @return
      */
     private boolean isValidSubject(String subject) {
        if (subject == null || subject.trim().length() == 0) {
            return false;
        }
        return true;
     }
 
     /*
      *
      * Aysnc send emails using command pattern
      *
      */
     private class EmailServiceAsync implements Runnable {
 
         String recipients[];
         String subject;
         String message;
 
         EmailServiceAsync(String recipients[], String subject,
                 String message) {
             this.recipients = recipients;
             this.subject = subject;
             this.message = message;
         }
 
         public void run() {
             YahooMailService.this.sendSSMessage(recipients, subject, message);
         }
     }
 
     /**
      * session is created only once
      *
      * @return
      */
     private Session createSession() {
 
         if (session != null && false) {
             return session;
         }
         Properties props = new Properties();
         props.put("mail.smtp.host", "smtp.mail.yahoo.com");
         props.put("mail.stmp.user", USERNAME);
         //To use TLS
         props.put("mail.smtp.auth", "true");
         props.put("mail.smtp.starttls.enable", "true");
         props.put("mail.smtp.password", PASSWORD);
         session = Session.getDefaultInstance(props, new Authenticator() {
 
             @Override
             protected PasswordAuthentication getPasswordAuthentication() {
                 return new PasswordAuthentication(USERNAME, PASSWORD);
             }
         });
         return session;
     }
 
     /**
      * bcc -- incase you need to be copied on emails
      *
      * @return
      * @throws AddressException
      */
//    private InternetAddress[] getBCC() throws AddressException {
//        if (bcc != null) {
//            return bcc;
//        }
//        bcc = new InternetAddress[1];
//        bcc[0] = new InternetAddress("example@yahoo.com");
//        return bcc;
//    }
     /**
      *
      * @param recipients
      * @param subject
      * @param message
      * @param from
      * @throws MessagingException
      */
     private void sendSSMessage(String recipients[], String subject,
             String message) {
 
         try {
 
             InternetAddress[] addressTo = new InternetAddress[recipients.length];
             for (int i = 0; i < recipients.length; i++) {
                 if (recipients[i] != null && recipients[i].length() > 0) {
                     addressTo[i] = new InternetAddress(recipients[i]);
                 }
             }
             send(addressTo, subject, message);
 
         } catch (Exception ex) {
             //logger.warn(ex.getMessage(), ex);
             throw new RuntimeException(ex);
         }
     }
 
     /**
      *
      * @param addressTo
      * @param subject
      * @param message
      *
      */
     private void send(InternetAddress[] addressTo, String subject, String message) {
         try {
             MimeMessage msg = new MimeMessage(createSession());
             msg.setFrom(new InternetAddress(USERNAME));
             msg.setRecipients(MimeMessage.RecipientType.TO, addressTo);
             msg.setSubject(subject);
             msg.setText(message);
             Transport.send(msg);
         } catch (Exception ex) {
             //logger.warn(ex.getMessage(), ex);
             throw new RuntimeException(ex);
         }
     }
 
     /**
      *
      * @throws Throwable
      */
     @Override
     protected void finalize() throws Throwable {
         try {
             // releasing executor
             this.executor.shutdown();
             // logger.trace("EmailService executor released!");
             System.out.println("EmailService exector released!");
         } finally {
             super.finalize();
         }
     }
     
     /**
      *  call this method from ServletContextListener.contextDestroyed()
      *  This will release all work thread's when your app is undeployed
      */
     public void shutdown() {
         this.executor.shutdown();
     }
 }
