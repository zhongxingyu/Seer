 package com.abudko.reseller.huuto.notification.email.order;
 
 import java.util.Locale;
 
 import javax.mail.BodyPart;
 import javax.mail.MessagingException;
 import javax.mail.Multipart;
 import javax.mail.internet.AddressException;
 import javax.mail.internet.InternetAddress;
 import javax.mail.internet.MimeBodyPart;
 import javax.mail.internet.MimeMessage;
 import javax.mail.internet.MimeMessage.RecipientType;
 import javax.mail.internet.MimeMultipart;
 
 import org.springframework.stereotype.Component;
 
 import com.abudko.reseller.huuto.order.ItemOrder;
 
 @Component
 public class OrderConfirmationEmailSender extends AbstractOrderEmailSender {
 
     static final String SUBJECT_MESSAGE_KEY = "order.email.confirmation.subject";
     static final String CONTENT_MESSAGE_KEY = "order.email.confirmation.content";
 
     protected void setMessageContent(MimeMessage mail, ItemOrder order) throws MessagingException {
         Multipart messageBody = composeMessageBody(order);
         mail.setContent(messageBody);
     }
 
     Multipart composeMessageBody(ItemOrder order) throws MessagingException {
         String html = htmlCreator.generateHtmlImage(order.getItemResponse().getImgBaseSrc());
 
         BodyPart mediaPart = new MimeBodyPart();
         mediaPart.setContent(html, "text/html");
 
         BodyPart textPart = new MimeBodyPart();
         String messageBodyText = composeMessageBodyText(order);
        textPart.setText(messageBodyText);
 
         Multipart messageBody = new MimeMultipart();
         messageBody.addBodyPart(textPart);
         messageBody.addBodyPart(mediaPart);
 
         return messageBody;
     }
 
     private String composeMessageBodyText(ItemOrder order) {
         String contentMessage = context.getMessage(CONTENT_MESSAGE_KEY, null, Locale.getDefault());
         StringBuilder sb = new StringBuilder(contentMessage);
         sb.append(order.dump());
 
         return sb.toString();
     }
 
     protected void setMessageSubject(MimeMessage mail, ItemOrder order) throws MessagingException {
         String subject = context.getMessage(SUBJECT_MESSAGE_KEY, new Object[] { order.getItemResponse().getId() },
                 Locale.getDefault());
         mail.setSubject(subject, "UTF-8");
     }
 
     protected void setMessageAddresses(MimeMessage mail, ItemOrder order) throws AddressException, MessagingException {
         InternetAddress sender = InternetAddress.parse(senderAddress)[0];
         mail.setFrom(sender);
         InternetAddress destination = InternetAddress.parse(order.getCustomerEmail())[0];
         mail.setRecipient(RecipientType.TO, destination);
     }
 }
