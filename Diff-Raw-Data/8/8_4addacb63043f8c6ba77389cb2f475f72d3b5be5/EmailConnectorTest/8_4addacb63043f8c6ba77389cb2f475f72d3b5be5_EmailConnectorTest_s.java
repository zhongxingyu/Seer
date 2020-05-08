 /**
  * Copyright (C) 2012 BonitaSoft S.A.
  * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
  * This library is free software; you can redistribute it and/or modify it under the terms
  * of the GNU Lesser General Public License as published by the Free Software Foundation
  * version 2.1 of the License.
  * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
  * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
  * See the GNU Lesser General Public License for more details.
  * You should have received a copy of the GNU Lesser General Public License along with this
  * program; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth
  * Floor, Boston, MA 02110-1301, USA.
  **/
 package org.bonitasoft.connectors.email.test;
 
 import static org.hamcrest.core.Is.is;
 import static org.hamcrest.core.IsNot.not;
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertNotSame;
 import static org.junit.Assert.assertThat;
 import static org.junit.Assert.assertTrue;
 import static org.junit.Assume.assumeNotNull;
 import static org.junit.matchers.JUnitMatchers.containsString;
 import static org.mockito.Mockito.mock;
 import static org.mockito.Mockito.when;
 
 import java.io.IOException;
 import java.net.ServerSocket;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.mail.MessagingException;
 import javax.mail.internet.MimeMessage;
 import javax.mail.internet.MimeMultipart;
 
 import org.bonitasoft.connectors.email.EmailConnector;
 import org.bonitasoft.engine.api.APIAccessor;
 import org.bonitasoft.engine.api.ProcessAPI;
 import org.bonitasoft.engine.bpm.document.impl.DocumentImpl;
 import org.bonitasoft.engine.connector.EngineExecutionContext;
 import org.bonitasoft.engine.exception.BonitaException;
 import org.bonitasoft.engine.test.annotation.Cover;
 import org.bonitasoft.engine.test.annotation.Cover.BPMNConcept;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 import org.subethamail.wiser.Wiser;
 import org.subethamail.wiser.WiserMessage;
 
 /**
  * @author Matthieu Chaffotte
  */
 public class EmailConnectorTest {
 
     private static final String SMTP_HOST = "localhost";
 
     private static final String ADDRESSJOHN = "john.doe@bonita.org";
 
     private static final String ADDRESSPATTY = "patty.johnson@gmal.com";
 
     private static final String ADDRESSMARK = "mark.hunt@wahoo.nz";
 
     private static final String SUBJECT = "Testing EmailConnector";
 
     private static final String PLAINMESSAGE = "Plain Message";
 
     private static final String HTMLMESSAGE = "<b><i>HTML<i/> Message</b>";
 
     private static final String TEXT_PLAIN = "text/plain";
 
     private static final String TEXT_HTML = "text/html";
 
     private static final String CYRILLIC_SUBJECT = "\u0416 \u0414 \u0431";
 
     private static final String CYRILLIC_MESSAGE = "\u0416 \u0414 \u0431";
 
     private static int smtpPort = 0;
 
     private Wiser server;
     private EngineExecutionContext engineExecutionContext;
     private APIAccessor apiAccessor;
     private ProcessAPI processAPI;
 
     @Before
     public void setUp() {
         engineExecutionContext = mock(EngineExecutionContext.class);
         apiAccessor = mock(APIAccessor.class);
         processAPI = mock(ProcessAPI.class);
         when(apiAccessor.getProcessAPI()).thenReturn(processAPI);
         startServer();
     }
 
     @After
     public void tearDown() throws InterruptedException {
         if (server != null) {
             stopServer();
         }
     }
 
     private void startServer() {
         if (smtpPort == 0) {
             smtpPort = findFreePort(31025);
         }
         server = new Wiser();
         server.setPort(smtpPort);
         server.start();
     }
 
     private static int findFreePort(int port) {
         boolean free = false;
         while (!free && port <= 65535) {
             if (isFreePort(port)) {
                 free = true;
             } else {
                 port++;
             }
         }
         return port;
     }
 
     private static boolean isFreePort(final int port) {
         try {
             final ServerSocket socket = new ServerSocket(port);
             socket.close();
             return true;
         } catch (final IOException e) {
             return false;
         }
     }
 
     private void stopServer() throws InterruptedException {
         server.stop();
         Thread.sleep(150);
         server = null;
     }
 
     private Map<String, Object> executeConnector(final Map<String, Object> parameters) throws BonitaException {
         final EmailConnector email = new EmailConnector();
         email.setExecutionContext(engineExecutionContext);
         email.setAPIAccessor(apiAccessor);
         email.setInputParameters(parameters);
         email.validateInputParameters();
         return email.execute();
     }
 
     private Map<String, Object> getBasicSettings() {
         final Map<String, Object> parameters = new HashMap<String, Object>();
         parameters.put("smtpHost", SMTP_HOST);
         parameters.put("smtpPort", smtpPort);
         parameters.put("to", ADDRESSJOHN);
         parameters.put("subject", SUBJECT);
         parameters.put("sslSupport", false);
         parameters.put("html", false);
         return parameters;
     }
 
     @Cover(classes = { EmailConnector.class }, concept = BPMNConcept.CONNECTOR, keywords = { "email" },
             story = "Test the sending of a simple email through the connector")
     @Test
     public void sendASimpleEmail() throws BonitaException, MessagingException, InterruptedException {
         executeConnector(getBasicSettings());
         final List<WiserMessage> messages = server.getMessages();
         assertEquals(1, messages.size());
         final WiserMessage message = messages.get(0);
         assertNotNull(message.getEnvelopeSender());
         assertEquals(ADDRESSJOHN, message.getEnvelopeReceiver());
         final MimeMessage mime = message.getMimeMessage();
         assertEquals(SUBJECT, mime.getSubject());
         assertEquals(0, mime.getSize());
     }
 
     @Cover(classes = { EmailConnector.class }, concept = BPMNConcept.CONNECTOR, keywords = { "email" },
             story = "Test the sending of a email with field from filled through the connector")
     @Test
     public void testSendEmailWithFromAddress() throws Exception {
         final Map<String, Object> parameters = getBasicSettings();
         parameters.put("from", ADDRESSMARK);
         executeConnector(parameters);
         final List<WiserMessage> messages = server.getMessages();
         assertEquals(1, messages.size());
         final WiserMessage message = messages.get(0);
         assertEquals(ADDRESSMARK, message.getEnvelopeSender());
         assertEquals(ADDRESSJOHN, message.getEnvelopeReceiver());
         final MimeMessage mime = message.getMimeMessage();
         assertEquals(SUBJECT, mime.getSubject());
         assertEquals(0, mime.getSize());
     }
 
     @Cover(classes = { EmailConnector.class }, concept = BPMNConcept.CONNECTOR, keywords = { "email" },
             story = "Test the sending of a email with authentification through the connector")
     @Test
     public void testSendEmailWithAutentication() throws Exception {
         final Map<String, Object> parameters = getBasicSettings();
         parameters.put("userName", "john");
         parameters.put("password", "doe");
         executeConnector(parameters);
         final List<WiserMessage> messages = server.getMessages();
         assertEquals(1, messages.size());
         final WiserMessage message = messages.get(0);
         assertNotNull(message.getEnvelopeSender());
         assertEquals(ADDRESSJOHN, message.getEnvelopeReceiver());
         final MimeMessage mime = message.getMimeMessage();
         assertEquals(SUBJECT, mime.getSubject());
         assertEquals(0, mime.getSize());
     }
 
     @Test
     public void sendEmailWithToRecipientsAddresses() throws Exception {
         final Map<String, Object> parameters = getBasicSettings();
         parameters.put("to", ADDRESSJOHN + ", " + ADDRESSPATTY);
         parameters.put("from", ADDRESSMARK);
         executeConnector(parameters);
         final List<WiserMessage> messages = server.getMessages();
         assertEquals(2, messages.size());
         WiserMessage message = messages.get(0);
         assertEquals(ADDRESSMARK, message.getEnvelopeSender());
         assertEquals(ADDRESSJOHN, message.getEnvelopeReceiver());
         MimeMessage mime = message.getMimeMessage();
         assertEquals(SUBJECT, mime.getSubject());
         assertEquals(0, mime.getSize());
 
         message = messages.get(1);
         assertEquals(ADDRESSMARK, message.getEnvelopeSender());
         assertEquals(ADDRESSPATTY, message.getEnvelopeReceiver());
         mime = message.getMimeMessage();
         assertEquals(SUBJECT, mime.getSubject());
         assertEquals(0, mime.getSize());
     }
 
     @Test
     public void sendEmailWithCcRecipientsAddresses() throws Exception {
         final Map<String, Object> parameters = getBasicSettings();
         parameters.put("cc", ADDRESSPATTY);
         parameters.put("from", ADDRESSMARK);
         executeConnector(parameters);
 
         List<WiserMessage> messages = server.getMessages();
         assertEquals(2, messages.size());
         WiserMessage message = messages.get(0);
         assertEquals(ADDRESSMARK, message.getEnvelopeSender());
         assertEquals(ADDRESSJOHN, message.getEnvelopeReceiver());
         MimeMessage mime = message.getMimeMessage();
         assertEquals(SUBJECT, mime.getSubject());
         assertEquals(0, mime.getSize());
 
         message = messages.get(1);
         assertEquals(ADDRESSMARK, message.getEnvelopeSender());
         assertEquals(ADDRESSPATTY, message.getEnvelopeReceiver());
         mime = message.getMimeMessage();
         assertEquals(SUBJECT, mime.getSubject());
         assertEquals(0, mime.getSize());
 
         parameters.put("cc", ADDRESSPATTY + ", " + ADDRESSMARK);
         executeConnector(parameters);
         messages = server.getMessages();
         assertEquals(5, messages.size());
         message = messages.get(4);
         assertEquals(ADDRESSMARK, message.getEnvelopeSender());
         assertEquals(ADDRESSMARK, message.getEnvelopeReceiver());
         mime = message.getMimeMessage();
         assertEquals(SUBJECT, mime.getSubject());
         assertEquals(0, mime.getSize());
     }
 
     @Test
     public void sendEmailWithPlainMessage() throws Exception {
         final Map<String, Object> parameters = getBasicSettings();
         parameters.put("message", PLAINMESSAGE);
         parameters.put("from", ADDRESSMARK);
         executeConnector(parameters);
 
         final List<WiserMessage> messages = server.getMessages();
         assertEquals(1, messages.size());
         final WiserMessage message = messages.get(0);
         assertEquals(ADDRESSMARK, message.getEnvelopeSender());
         assertEquals(ADDRESSJOHN, message.getEnvelopeReceiver());
         final MimeMessage mime = message.getMimeMessage();
         assertEquals(SUBJECT, mime.getSubject());
         assertTrue(mime.getContentType().contains(TEXT_PLAIN));
         assertEquals(PLAINMESSAGE, mime.getContent());
     }
 
     @Test
     public void sendEmailWithHtmlMessage() throws Exception {
         final Map<String, Object> parameters = getBasicSettings();
         parameters.put("html", true);
         parameters.put("message", HTMLMESSAGE);
         parameters.put("from", ADDRESSMARK);
         executeConnector(parameters);
 
         final List<WiserMessage> messages = server.getMessages();
         assertEquals(1, messages.size());
         final WiserMessage message = messages.get(0);
         assertEquals(ADDRESSMARK, message.getEnvelopeSender());
         assertEquals(ADDRESSJOHN, message.getEnvelopeReceiver());
         final MimeMessage mime = message.getMimeMessage();
         assertEquals(SUBJECT, mime.getSubject());
         assertTrue(mime.getContentType().contains(TEXT_HTML));
         assertEquals(HTMLMESSAGE, mime.getContent());
     }
 
     @Test
     public void sendEmailWithExtraHeaders() throws Exception {
         final List<List<String>> headers = new ArrayList<List<String>>();
         List<String> row1 = new ArrayList<String>();
         row1.add("X-Mailer");
         row1.add("Bonita Mailer");
         headers.add(row1);
 
         List<String> row2 = new ArrayList<String>();
         row2.add("Message-ID");
         row2.add("IWantToHackTheServer");
         headers.add(row2);
 
         List<String> row3 = new ArrayList<String>();
         row3.add("X-Priority");
         row3.add("2 (High)");
         headers.add(row3);
 
         List<String> row4 = new ArrayList<String>();
         row4.add("Content-Type");
         row4.add("video/mpeg");
         headers.add(row4);
 
         List<String> row5 = new ArrayList<String>();
         row5.add("WhatIWant");
         row5.add("anyValue");
         headers.add(row5);
 
         List<String> row6 = new ArrayList<String>();
         row6.add("From");
         row6.add("alice@bob.charly");
         headers.add(row6);
 
         List<String> row7 = new ArrayList<String>();
         row7.add(null);
         row7.add(null);
         headers.add(row7);
 
         final Map<String, Object> parameters = getBasicSettings();
 
         parameters.put("headers", headers);
 
         executeConnector(parameters);
 
         final List<WiserMessage> messages = server.getMessages();
         assertEquals(1, messages.size());
         final WiserMessage message = messages.get(0);
         assertEquals("alice@bob.charly", message.getEnvelopeSender());
         assertEquals(ADDRESSJOHN, message.getEnvelopeReceiver());
         final MimeMessage mime = message.getMimeMessage();
         assertEquals(SUBJECT, mime.getSubject());
         assertEquals(0, mime.getSize());
         assertEquals("Bonita Mailer", mime.getHeader("X-Mailer", ""));
         assertEquals("2 (High)", mime.getHeader("X-Priority", ""));
         assertEquals("anyValue", mime.getHeader("WhatIWant", ""));
         assertNotSame("alice@bob.charly", mime.getHeader("From"));
         assertFalse(mime.getContentType().contains("video/mpeg"));
         assertNotSame("IWantToHackTheServer", mime.getHeader("Message-ID"));
     }
 
     @Test
     public void sendCyrillicEmail() throws Exception {
         final Map<String, Object> parameters = getBasicSettings();
         parameters.put("subject", CYRILLIC_SUBJECT);
         parameters.put("message", CYRILLIC_MESSAGE);
         executeConnector(parameters);
 
         final List<WiserMessage> messages = server.getMessages();
         assertEquals(1, messages.size());
         final WiserMessage message = messages.get(0);
         assertNotNull(message.getEnvelopeSender());
         assertEquals(ADDRESSJOHN, message.getEnvelopeReceiver());
         final MimeMessage mime = message.getMimeMessage();
         assertEquals(CYRILLIC_SUBJECT, mime.getSubject());
         assertTrue(mime.getContentType().contains(TEXT_PLAIN));
         assertEquals(CYRILLIC_MESSAGE, mime.getContent());
     }
 
     @Test
     public void sendBadEncodingCyrillicEmail() throws Exception {
         final Map<String, Object> parameters = getBasicSettings();
         parameters.put("charset", "iso-8859-1");
         parameters.put("message", CYRILLIC_MESSAGE);
         executeConnector(parameters);
 
         final List<WiserMessage> messages = server.getMessages();
         assertEquals(1, messages.size());
         final WiserMessage message = messages.get(0);
         assertNotNull(message.getEnvelopeSender());
         assertEquals(ADDRESSJOHN, message.getEnvelopeReceiver());
         final MimeMessage mime = message.getMimeMessage();
         assertEquals(SUBJECT, mime.getSubject());
         assertTrue(mime.getContentType().contains(TEXT_PLAIN));
         assertEquals("? ? ?", mime.getContent());
     }
 
     @Test
     public void sendFileDocument() throws BonitaException, MessagingException, IOException {
         DocumentImpl document = new DocumentImpl();
         document.setAuthor(1);
         document.setContentMimeType("application/octet-stream");
         document.setContentStorageId("storageId");
         document.setCreationDate(new Date());
         document.setFileName("filename.txt");
         document.setHasContent(true);
         document.setId(1);
         document.setProcessInstanceId(1);
         document.setName("Document1");
         when(engineExecutionContext.getProcessInstanceId()).thenReturn(1L);
        when(processAPI.getDocumentAtProcessInstantiation(1L, "Document1")).thenReturn(document);
         when(processAPI.getDocumentContent("storageId")).thenReturn("toto".getBytes());
         Map<String, Object> parameters = getBasicSettings();
         List<String> attachments = Collections.singletonList("Document1");
         parameters.put(EmailConnector.ATTACHMENTS, attachments);
 
         executeConnector(parameters);
 
         List<WiserMessage> messages = server.getMessages();
         assumeNotNull(messages);
         assertThat(((MimeMultipart) messages.get(0).getMimeMessage().getContent()).getBodyPart(1).getFileName(), is("filename.txt"));
     }
 
     @Test
     public void sendUrlDocument() throws BonitaException, MessagingException, IOException {
         DocumentImpl document = new DocumentImpl();
         document.setAuthor(1);
         document.setCreationDate(new Date());
         document.setHasContent(false);
         document.setId(1);
         document.setProcessInstanceId(1);
         document.setUrl("http://www.bonitasoft.com");
         document.setName("Document1");
         when(engineExecutionContext.getProcessInstanceId()).thenReturn(1L);
        when(processAPI.getDocumentAtProcessInstantiation(1L, "Document1")).thenReturn(document);
         Map<String, Object> parameters = getBasicSettings();
         parameters.put(EmailConnector.MESSAGE, "Hello Mr message\n This is an email content");
         List<String> attachments = Collections.singletonList("Document1");
         parameters.put(EmailConnector.ATTACHMENTS, attachments);
 
         executeConnector(parameters);
 
         List<WiserMessage> messages = server.getMessages();
         assumeNotNull(messages);
         assertThat((String) ((MimeMultipart) messages.get(0).getMimeMessage().getContent()).getBodyPart(0).getContent(), containsString("http://www.bonitasoft.com"));
     }
 
     @Test
     public void sendMailWithEmptyDocument() throws BonitaException, MessagingException, IOException {
         DocumentImpl document = new DocumentImpl();
         document.setAuthor(1);
         document.setCreationDate(new Date());
         document.setId(1);
         document.setProcessInstanceId(1);
         document.setName("Document1");
         when(engineExecutionContext.getProcessInstanceId()).thenReturn(1L);
        when(processAPI.getDocumentAtProcessInstantiation(1L, "Document1")).thenReturn(document);
         Map<String, Object> parameters = getBasicSettings();
         parameters.put(EmailConnector.MESSAGE, "Hello Mr message\n This is an email content");
         List<String> attachments = Collections.singletonList("Document1");
         parameters.put(EmailConnector.ATTACHMENTS, attachments);
 
         executeConnector(parameters);
 
         List<WiserMessage> messages = server.getMessages();
         assumeNotNull(messages);
         assertThat((String) ((MimeMultipart) messages.get(0).getMimeMessage().getContent()).getBodyPart(0).getContent(), not(containsString("Document1")));
 
     }
 
 
 
 }
