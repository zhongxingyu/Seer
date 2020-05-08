 package com.abudko.reseller.huuto.query.notification.email;
 
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertTrue;
 import static org.mockito.Mockito.doThrow;
 import static org.mockito.Mockito.verify;
 import static org.mockito.Mockito.when;
 
 import java.io.IOException;
 
 import javax.mail.MessagingException;
 import javax.mail.Multipart;
 import javax.mail.internet.AddressException;
 import javax.mail.internet.InternetAddress;
 import javax.mail.internet.MimeMessage;
 import javax.mail.internet.MimeMessage.RecipientType;
 
 import org.apache.commons.logging.Log;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.mockito.InjectMocks;
 import org.mockito.Mock;
 import org.mockito.Mockito;
 import org.mockito.runners.MockitoJUnitRunner;
 import org.springframework.mail.javamail.JavaMailSender;
 import org.springframework.test.util.ReflectionTestUtils;
 
 import com.abudko.reseller.huuto.query.exception.EmailNotificationException;
 import com.abudko.reseller.huuto.query.html.HtmlCreator;
 import com.abudko.reseller.huuto.query.html.item.ItemResponse;
 import com.abudko.reseller.huuto.query.html.list.QueryListResponse;
 
 @RunWith(MockitoJUnitRunner.class)
 public class EmailSenderTest {
 
     private static final String senderAddress = "sender@mail.com";
     private static final String destinationAddress = "destination@mail.com";
 
     @Mock
     private Log log;
 
     @Mock
     private MimeMessage message;
 
     @Mock
     private JavaMailSender mailSender;
 
     @Mock
     private HtmlCreator htmlCreator;
 
     @InjectMocks
     private EmailSender emailSender = new EmailSender();
 
     private QueryListResponse response;
 
     @Before
     public void setup() {
         ReflectionTestUtils.setField(emailSender, "senderAddress", senderAddress);
         ReflectionTestUtils.setField(emailSender, "destinationAddress", destinationAddress);
         when(mailSender.createMimeMessage()).thenReturn(message);
         response = createTestResponse();
     }
 
     private QueryListResponse createTestResponse() {
         QueryListResponse response = new QueryListResponse();
         response.setDescription("description");
         response.setBrandNew(true);
         response.setBids("bids");
         response.setLast("last");
         response.setItemUrl("itemUrl");
         response.setFullPrice("fullPrice");
         response.setPrices("prices");
 
         ItemResponse itemResponse = new ItemResponse();
         itemResponse.setImgSrc("imgSrc");
         itemResponse.setCondition("condition");
         itemResponse.setLocation("location");
         itemResponse.setHv(Boolean.TRUE);
 
         response.setItemResponse(itemResponse);
 
         return response;
     }
 
     @Test
     public void testProcessMessageSent() {
         emailSender.process(response);
 
         verify(mailSender).send(message);
     }
 
     @Test
     public void testProcessMessageThrowException() throws AddressException, MessagingException {
         doThrow(new MessagingException()).when(message).setFrom(InternetAddress.parse(senderAddress)[0]);
 
         try {
             emailSender.process(response);
         } catch (EmailNotificationException e) {
             verify(log).error(Mockito.anyString(), Mockito.any(EmailNotificationException.class));
         }
     }
 
     @Test
     public void testProcessCorrectMessageFrom() throws AddressException, MessagingException {
         emailSender.process(response);
 
         verify(message).setFrom(InternetAddress.parse(senderAddress)[0]);
     }
 
     @Test
     public void testProcessCorrectMessageTo() throws AddressException, MessagingException {
         emailSender.process(response);
 
         verify(message).setRecipient(RecipientType.TO, InternetAddress.parse(destinationAddress)[0]);
     }
 
     @Test
     public void testProcessCorrectMessageSubject() throws AddressException, MessagingException {
         String subject = String.format("%s %s (%s)", response.getDescription(), response.getPrices(),
                 response.getFullPrice());
 
         emailSender.process(response);
 
         verify(message).setSubject(subject);
     }
 
     @Test
     public void testProcessCorrectMessageContent() throws AddressException, MessagingException {
         String html = "html";
         when(htmlCreator.generateHtmlForResponse(response)).thenReturn(html);
 
         emailSender.process(response);
 
         verify(message).setContent(Mockito.any(Multipart.class));
         verify(htmlCreator).generateHtmlForResponse(response);
     }
 
     @Test
     public void testComposeMessageBodyText() throws MessagingException, IOException {
         Multipart messageBody = emailSender.composeMessageBody(response);
 
        String messageBodyText = (String) messageBody.getBodyPart(1).getContent();
         assertTrue(messageBodyText, messageBodyText.contains(response.getDescription()));
         assertTrue(messageBodyText, messageBodyText.contains(response.getBids()));
         assertTrue(messageBodyText, messageBodyText.contains(response.getFullPrice()));
         assertTrue(messageBodyText, messageBodyText.contains(response.getPrices()));
         assertTrue(messageBodyText, messageBodyText.contains(response.getItemUrl()));
         assertTrue(messageBodyText, messageBodyText.contains(response.isBrandNew().toString()));
         assertTrue(messageBodyText, messageBodyText.contains(response.getLast()));
         assertTrue(messageBodyText, messageBodyText.contains(response.getItemResponse().getCondition()));
         assertTrue(messageBodyText, messageBodyText.contains(response.getItemResponse().getLocation()));
         assertTrue(messageBodyText, messageBodyText.contains(response.getItemResponse().isHv().toString()));
     }
 
     @Test
     public void testComposeMessageNotInBodyText() throws MessagingException, IOException {
         Multipart messageBody = emailSender.composeMessageBody(response);
 
        String messageBodyText = (String) messageBody.getBodyPart(1).getContent();
         assertFalse(messageBodyText, messageBodyText.contains(response.getItemResponse().getImgSrc()));
     }
 }
