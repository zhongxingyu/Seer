 package net.codjo.test.common.fixture;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.util.Date;
 import java.util.List;
 import javax.activation.DataHandler;
 import javax.mail.Message;
 import javax.mail.MessagingException;
 import javax.mail.Session;
 import javax.mail.Transport;
 import javax.mail.internet.InternetAddress;
 import javax.mail.internet.MimeBodyPart;
 import javax.mail.internet.MimeMessage;
 import javax.mail.internet.MimeMultipart;
 import javax.mail.util.ByteArrayDataSource;
 import junit.framework.AssertionFailedError;
 import junit.framework.TestCase;
 import net.codjo.test.common.PathUtil;
 import net.codjo.test.common.excel.ExcelUtil;
 /**
  *
  */
 public class MailFixtureTest extends TestCase {
     private static final int MY_MAIL_PORT = 9999;
     private MailFixture mailFixture = new MailFixture(MY_MAIL_PORT);
 
 
     public void test_getReceivedMessages() throws Exception {
         sendMail("darth.Vader", "luke.Skywalker", "scoop", "Je suis ton pere");
 
         List<MailMessage> list = mailFixture.getReceivedMessages();
         assertEquals(1, list.size());
         MailMessage message = list.get(0);
         assertEquals("darth.Vader", message.getFrom());
         assertEquals("luke.Skywalker", message.getTo());
         assertEquals("scoop", message.getSubject());
         assertEquals("Je suis ton pere", message.getBody());
     }
 
 
     public void test_assertReceivedMessagesCount() throws Exception {
         sendMail("darth.Vader", "luke.Skywalker", "scoop", "Je suis ton pere");
 
         mailFixture.assertReceivedMessagesCount(1);
 
         try {
             mailFixture.assertReceivedMessagesCount(2);
             fail("should fail");
         }
         catch (AssertionFailedError error) {
             assertEquals("Received message count expected:<2> but was:<1>", error.getMessage());
         }
     }
 
 
     public void test_assertReceivedMessage() throws Exception {
        sendMail("darth.Vader", "luke.Skywalker", "rvlation", "Je suis ton p\u00E8re");
 
         assertNotNull(mailFixture.getReceivedMessage(0).getSmtpMessage());
 
         mailFixture.getReceivedMessage(0).assertThat()
               .from("darth.Vader")
               .to("luke.Skywalker")
               .subject("rvlation")
              .bodyContains("p\u00E8re");
 
         assertEquals("rvlation", mailFixture.getReceivedMessage(0).getSubject());
     }
 
 
     public void test_assertReceivedMessage_withMultipleSender() throws Exception {
         sendMail("darth.Vader", new String[]{"luke", "chewbacca"}, "n/a", "n/a");
 
         mailFixture.getReceivedMessage(0).assertThat()
               .to("luke", "chewbacca");
 
         try {
             mailFixture.getReceivedMessage(0).assertThat()
                   .to("mika");
             fail("should fail");
         }
         catch (AssertionFailedError error) {
             assertEquals("Assert To - The value 'mika' is not in [luke, chewbacca]", error.getMessage());
         }
     }
 
 
     public void test_asserBadReceivedMessage() throws Exception {
         sendMail("darth.Vader", "luke.Skywalker", "scoop", "Je suis ton pere");
 
         try {
             mailFixture.getReceivedMessage(0).assertThat().from("yoda");
             fail();
         }
         catch (AssertionFailedError ex) {
             assertEquals("Assert From -  expected:<[yoda]> but was:<[darth.Vader]>", ex.getMessage());
         }
         try {
             mailFixture.getReceivedMessage(0).assertThat().to("yoda");
             fail();
         }
         catch (AssertionFailedError ex) {
             assertEquals("Assert To -  expected:<[yoda]> but was:<[luke.Skywalker]>", ex.getMessage());
         }
         try {
             mailFixture.getReceivedMessage(0).assertThat().subject("Hello Luke");
             fail();
         }
         catch (AssertionFailedError ex) {
             assertEquals("Assert Subject -  expected:<[Hello Luke]> but was:<[scoop]>", ex.getMessage());
         }
         try {
             mailFixture.getReceivedMessage(0).assertThat().bodyContains("Je suis ta mere");
             fail();
         }
         catch (AssertionFailedError ex) {
             assertEquals("Assert Body contains -"
                          + "  expected contains:<Je suis ta mere> but content was:<Je suis ton pere>",
                          ex.getMessage());
         }
     }
 
 
     public void test_badTearDownCall() throws Exception {
         mailFixture.doTearDown();
         mailFixture = new MailFixture(MY_MAIL_PORT);
         try {
             mailFixture.doTearDown();
         }
         catch (Exception ex) {
             assertEquals("Mail server has not started yet, doSetup() method should be called before",
                          ex.getMessage());
         }
     }
 
 
     public void test_invokePublicMethodsBeforeDoSetUp() throws Exception {
         mailFixture.doTearDown();
         mailFixture = new MailFixture(MY_MAIL_PORT);
         try {
             mailFixture.getReceivedMessage(0);
         }
         catch (Exception ex) {
             assertEquals(MailFixture.FIXTURE_NOT_STARTED_MESSAGE, ex.getMessage());
         }
 
         try {
             mailFixture.getReceivedMessages();
         }
         catch (Exception ex) {
             assertEquals(MailFixture.FIXTURE_NOT_STARTED_MESSAGE, ex.getMessage());
         }
     }
 
 
     public void test_assertAttachmentFiles() throws Exception {
         File[] attachments = new File[]{
               new File(getClass().getResource("attachment1.xls").toURI()),
               new File(getClass().getResource("attachment2.xls").toURI()),
         };
 
         sendMail("darth.Vader", "luke.Skywalker", "scoop", "Je suis ton pere", attachments);
 
         List<MailMessage> list = mailFixture.getReceivedMessages();
         assertEquals(1, list.size());
         MailMessage message = list.get(0);
         assertEquals("darth.Vader", message.getFrom());
         assertEquals("luke.Skywalker", message.getTo());
         assertEquals("scoop", message.getSubject());
         assertEquals("Je suis ton pere", message.getBody());
 
         File multipart1 = message.getMultipart(1, createMultipartFilePath(1));
         File multipart2 = message.getMultipart(2, createMultipartFilePath(2));
 
         ExcelUtil.compare(new File(getClass().getResource("attachment1.xls").toURI()), multipart1, null, null);
         ExcelUtil.compare(new File(getClass().getResource("attachment2.xls").toURI()), multipart2, null, null);
     }
 
 
     public void test_assertAttachmentFilePart0() throws Exception {
         sendMail("darth.Vader", "luke.Skywalker", "scoop", "Je suis ton pere",
                  new File(getClass().getResource("attachment1.xls").toURI()));
 
         List<MailMessage> list = mailFixture.getReceivedMessages();
         assertEquals(1, list.size());
         MailMessage message = list.get(0);
         try {
             message.getMultipart(0, createMultipartFilePath(0));
             fail();
         }
         catch (Exception e) {
             assertEquals("La partie '0' est rserve au body. Utilisez plutt la mthode getBody().", e.getMessage());
         }
     }
 
 
     public void test_assertAttachmentFileWithBadPartNumber() throws Exception {
         sendMail("darth.Vader", "luke.Skywalker", "scoop", "Je suis ton pere",
                  new File(getClass().getResource("attachment1.xls").toURI()));
 
         List<MailMessage> list = mailFixture.getReceivedMessages();
         assertEquals(1, list.size());
         MailMessage message = list.get(0);
         try {
             message.getMultipart(-1, createMultipartFilePath(-1));
             fail();
         }
         catch (Exception e) {
             assertEquals(
                   "Impossible de dcoder la pice jointe numro -1. L'index de pice jointe spcifi (-1) est erron (nombre de pices jointes : 1)",
                   e.getMessage());
         }
         try {
             message.getMultipart(2, createMultipartFilePath(2));
             fail();
         }
         catch (Exception e) {
             assertEquals(
                   "Impossible de dcoder la pice jointe numro 2. L'index de pice jointe spcifi (2) est erron (nombre de pices jointes : 1)",
                   e.getMessage());
         }
 
         message.getMultipart(1, createMultipartFilePath(1));
     }
 
 
     private String createMultipartFilePath(int part) {
         return PathUtil.findTestResourcesDirectory(getClass()).getAbsolutePath() + "\\" + "multipart" + part;
     }
 
 
     @Override
     protected void setUp() throws Exception {
         mailFixture.doSetUp();
     }
 
 
     @Override
     protected void tearDown() throws Exception {
         mailFixture.doTearDown();
     }
 
 
     private void sendMail(String from, String to, String subject, String body, File... attachedFiles) throws Exception {
         sendMail(from, new String[]{to}, subject, body, attachedFiles);
     }
 
 
     private void sendMail(String from, String[] to, String subject, String body, File... attachedFiles)
           throws Exception {
         Transport.send(createMessage(from, to, subject, prepareMessage(body, attachedFiles)));
     }
 
 
     private static MimeMessage createMessage(String from,
                                              String[] to,
                                              String subject,
                                              MimeMultipart content) throws MessagingException {
         Session session = Session.getInstance(System.getProperties());
         session.setDebug(true);
         MimeMessage message = new MimeMessage(session);
         message.setFrom(new InternetAddress(from));
 
         for (String aTo : to) {
             message.addRecipients(Message.RecipientType.TO, InternetAddress.parse(aTo, false));
         }
 
         message.setSubject(subject);
         message.setContent(content);
         message.setHeader("X-Mailer", "iComp");
         message.setSentDate(new Date());
         return message;
     }
 
 
     private static MimeMultipart prepareMessage(String body, File... attachedFiles)
           throws IOException, MessagingException {
         MimeMultipart multipartMessage = new MimeMultipart();
 
         MimeBodyPart contentsPart = new MimeBodyPart();
        contentsPart.setContent(body, "text/html; charset=UTF-8");
         multipartMessage.addBodyPart(contentsPart);
 
         for (File attachedFile : attachedFiles) {
             MimeBodyPart attachmentPart = new MimeBodyPart();
 
             attachmentPart.setDataHandler(new DataHandler(new ByteArrayDataSource(new FileInputStream(attachedFile),
                                                                                   "application/excel")));
             attachmentPart.setFileName(attachedFile.getName());
             multipartMessage.addBodyPart(attachmentPart);
         }
 
         return multipartMessage;
     }
 }
