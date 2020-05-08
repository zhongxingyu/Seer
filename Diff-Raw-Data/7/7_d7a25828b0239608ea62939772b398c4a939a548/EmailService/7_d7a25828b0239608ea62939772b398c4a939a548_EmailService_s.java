 package com.team.xslides.service;
 
 import org.apache.commons.mail.DefaultAuthenticator;
 import org.apache.commons.mail.Email;
 import org.apache.commons.mail.EmailException;
 import org.apache.commons.mail.SimpleEmail;
 import org.springframework.stereotype.Service;
 
 import com.team.xslides.domain.User;
 
 @Service
 public class EmailService {
     private Email email;
     
     public EmailService() {
         email = new SimpleEmail();
         email.setHostName("smtp.gmail.com");
         email.setSmtpPort(465);
         email.setSSL(true);
         email.setAuthenticator(new DefaultAuthenticator("team.xslides", "1Romka2Nikita"));
         try {
             email.setFrom("team.xslides@gmail.com");
         } catch(EmailException exception) {
             // do nothing
         }
     }
     
     public boolean sendConfirmEmail(User user, String link) {
         try {
             String html = "<h3>" + user.getFirstname() + " " + user.getLastname() + "</h3>" +
                           "<h4>Welcome and thank you for registering at XSlides!</h4>" +
                           "<h4>Please follow this link to confirm your e-mail:</h4>" +
                           "<a href=http://localhost:8080/XSlides/confirm/" + link + ">Confirm email</a>";
             email.setSubject("XSlides registration");
             email.setContent(html, "text/html");
             email.addTo(user.getEmail());
             email.send();
         } catch (EmailException exception) {
             return false;
         }
         return true;
     }
     
     public boolean sendNewPassowrd(User user, String newPassword) {
         try {
             String html = "<h3>" + user.getFirstname() + " " + user.getLastname() + "</h3>" +
                           "<h4>There is your new password next to this text.</h4>" +
                           "<h4>Use it to login. Further you can change it at your settings page.</h4>" +
                           "<h3>" + newPassword + "</h3>";
             email.setSubject("XSlides New password");
             email.setContent(html, "text/html");
             email.addTo(user.getEmail());
             email.send();
         } catch (EmailException exception) {
             return false;
         }
         return true;
     }
 }
