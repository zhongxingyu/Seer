 package com.worthsoln.service.impl;
 
import com.worthsoln.patientview.messaging.Messaging;
 import com.worthsoln.patientview.model.SpecialtyUserRole;
 import com.worthsoln.service.EmailManager;
 import com.worthsoln.service.UserManager;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Value;
 import org.springframework.mail.javamail.JavaMailSender;
 import org.springframework.mail.javamail.MimeMessageHelper;
 import org.springframework.stereotype.Service;
 import org.springframework.util.StringUtils;
 
 import javax.inject.Inject;
 import javax.mail.Address;
 import javax.mail.Message;
 import javax.mail.internet.InternetAddress;
 import javax.mail.internet.MimeMessage;
 import javax.servlet.ServletContext;
 import java.util.List;
 
 @Service(value = "emailManager")
 public class EmailManagerImpl implements EmailManager {
 
     @Inject
     private JavaMailSender javaMailSender;
 
     @Inject
     private UserManager userManager;
 
     @Value("${noreply.email}")
     private String noReplyEmail;
 
     private static final Logger LOGGER = LoggerFactory.getLogger(EmailManagerImpl.class);
 
     @Override
     public void sendUserMessage(ServletContext context, com.worthsoln.patientview.model.Message message) {
         String subject = "You have been sent a message from " + message.getSender().getName()
                 + " on Renal PatientView";
 
         // need to work out if the recipient of this message is a patient or something higher
         // if staff or admin then they go to /control if patient they go to /patient
         boolean isAdminOrStaff = false;
 
         List<SpecialtyUserRole> specialtyUserRoles = userManager.getSpecialtyUserRoles(message.getRecipient());
 
         for (SpecialtyUserRole specialtyUserRole : specialtyUserRoles) {
             if (specialtyUserRole.getRole().equals("unitadmin")
                     || specialtyUserRole.getRole().equals("unitstaff")
                     || specialtyUserRole.getRole().equals("superadmin")) {
                 isAdminOrStaff = true;
                 break;
             }
         }
 
         String messageUrl = context.getInitParameter("config.site.url") + (isAdminOrStaff ? "control": "patient")
                + "/conversation.do?" + Messaging.CONVERSATION_ID_PARAM + "=" + message.getConversation().getId() +
                "#message-" + message.getId();
 
         String body = "Hello " + message.getRecipient().getName() + "\n\n";
         body += "You have received a message from " + message.getSender().getName() + " on Renal PatientView.\n\n";
         body += "Click the link below or logon to PatientView and go to the Messages tab to see the message.\n\n";
         body += messageUrl;
         body += "\n\nPlease don't reply to this message. No one will see it.\n\n";
 
         sendEmail(noReplyEmail, new String[]{message.getRecipient().getEmail()},
                 null, subject, body);
     }
 
     @Override
     public void sendEmail(ServletContext context, String fromAddress, String toAddress, String ccAddress,
                           String subject, String emailText) {
         try {
             // convert params to signature to sendEmail
             String[] toAddresses = null;
             String[] bccAddresses = null;
             if (StringUtils.hasLength(toAddress)) {
                 toAddresses = new String[]{toAddress};
             }
             if (StringUtils.hasLength(ccAddress)) {
                 bccAddresses = new String[]{ccAddress};
             }
 
             sendEmail(fromAddress, toAddresses, bccAddresses, subject, emailText);
         } catch (Exception e) {
             LOGGER.error("EmailManagerImpl: Failed to send email - " + e.getMessage() + " swallowing exception!");
         }
     }
 
     public void sendEmail(String from, String[] to, String[] bcc, String subject, String body) {
 
         if (!StringUtils.hasLength(from)) {
             throw new IllegalArgumentException("Cannot send mail missing 'from'");
         }
 
         if (!StringUtils.hasLength(subject)) {
             throw new IllegalArgumentException("Cannot send mail missing 'subject'");
         }
 
         if (!StringUtils.hasLength(body)) {
             throw new IllegalArgumentException("Cannot send mail missing 'body'");
         }
 
         if ((to == null || to.length == 0) && (bcc == null || bcc.length == 0)) {
             throw new IllegalArgumentException("Cannot send mail missing recipients");
         }
 
         MimeMessage message = javaMailSender.createMimeMessage();
         MimeMessageHelper messageHelper;
 
         try {
             messageHelper = new MimeMessageHelper(message, true);
             messageHelper.setTo(to);
             if (bcc != null && bcc.length > 0) {
                 Address[] bccAddresses = new Address[bcc.length];
                 for (int i = 0; i < bcc.length; i++) {
                     bccAddresses[i] = new InternetAddress(bcc[i]);
                 }
                 message.addRecipients(Message.RecipientType.BCC, bccAddresses);
             }
             messageHelper.setFrom(from);
             messageHelper.setSubject(subject);
             messageHelper.setText(body, false); // Note: the second param indicates to send plaintext
 
             javaMailSender.send(messageHelper.getMimeMessage());
         } catch (Exception e) {
             LOGGER.error("Could send email: {}", e);
         }
     }
 
     public JavaMailSender getJavaMailSender() {
         return javaMailSender;
     }
 
     public void setJavaMailSender(JavaMailSender javaMailSender) {
         this.javaMailSender = javaMailSender;
     }
 }
