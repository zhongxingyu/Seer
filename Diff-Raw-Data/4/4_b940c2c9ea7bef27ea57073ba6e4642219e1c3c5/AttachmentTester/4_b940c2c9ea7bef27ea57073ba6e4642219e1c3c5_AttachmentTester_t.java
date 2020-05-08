 /*(c) Copyright 2008, VersionOne, Inc. All rights reserved. (c)*/
 package com.versionone.om.tests;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.Map;
 import java.util.HashMap;
 
 import com.versionone.om.*;
 import org.junit.Assert;
 import org.junit.Ignore;
 import org.junit.Test;
 
 import com.versionone.DB.DateTime;
 
 public class AttachmentTester extends BaseSDKTester {
     private static final String ATTACHMENT_1783 = "Attachment:1783";
 
     @Test
     public void testAttachmentProperties() throws IOException {
         Attachment attachment = getInstance().get().attachmentByID(
                 ATTACHMENT_1783);
         Project project = getInstance().get().projectByID(SCOPE_ZERO);
         Assert.assertEquals(project, attachment.getAsset());
         Assert.assertEquals("text/plain", attachment.getContentType());
         Assert.assertEquals("Sample attachment<br>", attachment.getDescription());
         Assert.assertEquals("Attachment A", attachment.getName());
         Assert.assertEquals("sample.txt", attachment.getFilename());
 
         ByteArrayOutputStream output = new ByteArrayOutputStream();
 
         try {
             attachment.writeTo(output);
             Assert.assertEquals("This is a sample attachment", output.toString());
         } finally {
             try {
                 output.close();
             } catch (IOException e) {
                 // do nothing
             }
         }
     }
 
     @Test
     public void testURLTester() {
         Attachment attachment = getInstance().get().attachmentByID(
                 ATTACHMENT_1783);
         Assert.assertEquals(getApplicationUrl() + "attachment.v1/1783",
                 attachment.getContentURL());
         Assert.assertEquals(getApplicationUrl()
                 + "assetdetail.v1/?oid=" + ATTACHMENT_1783, attachment.getURL());
     }
 
     @Test
     public void testCreate() throws IOException {
         Project project = getInstance().get().projectByID(SCOPE_ZERO);
         Attachment attachment;
         String content = "This is the first attachment's content. At: "
                 + DateTime.now();
 
         InputStream input = new ByteArrayInputStream(content.getBytes());
 
         try {
             attachment = project.createAttachment("First Attachment",
                     "test.txt", input);
         } finally {
             try {
                 input.close();
             } catch (IOException e) {}
         }
         String attachmentID = attachment.getID().toString();
         resetInstance();
 
         Attachment newAttachment = getInstance().get().attachmentByID(
                 attachmentID);
 
         Project newProject = getInstance().get().projectByID(SCOPE_ZERO);
         Assert.assertEquals(newProject, newAttachment.getAsset());
         Assert.assertEquals("text/plain", attachment.getContentType());
         Assert.assertEquals("test.txt", attachment.getFilename());
         Assert.assertEquals("First Attachment", attachment.getName());
 
         ByteArrayOutputStream output = new ByteArrayOutputStream();
 
         try {
             newAttachment.writeTo(output);
         } finally {
 
             try {
                 output.close();
             } catch (IOException e) {}
         }
         Assert.assertEquals(content, output.toString());// use ASCII
     }
 
     @Test
     public void testCreateAttachmentWithAttributes() {
         final String description = "Test for Attachment creation with required attributes";
         Map<String, Object> attributes = new HashMap<String, Object>();
         attributes.put("Description", description);
 
         Project project = getInstance().get().projectByID(SCOPE_ZERO);
         Attachment attachment;
         String content = "This is the first attachment's content. At: " + DateTime.now();
         InputStream input = new ByteArrayInputStream(content.getBytes());
         try {
             attachment = project.createAttachment("First Attachment", "test.txt", input, attributes);
          } finally {
             try {
                 input.close();
             } catch (IOException e) {}
         }
 
         String attachmentID = attachment.getID().toString();
         resetInstance();
 
         Attachment newAttachment = getInstance().get().attachmentByID(attachmentID);
 
         Assert.assertEquals(project, newAttachment.getAsset());
         Assert.assertEquals(description, attachment.getDescription());
 
         ByteArrayOutputStream output = new ByteArrayOutputStream();
         try {
             newAttachment.writeTo(output);
         } finally {
 
             try {
                 output.close();
             } catch (IOException e) {}
         }
         Assert.assertEquals(content, output.toString());
     }
 
     @Test
     public void testCreateFromFile() throws IOException,
             ApplicationUnavailableException {
         final String fileName = "logo.png";
         final int fileSize = 3 * 1024; // 3k
         Project project = getInstance().get().projectByID(SCOPE_ZERO);
         Attachment attachment;
         InputStream input = AttachmentTester.class
                .getResourceAsStream("./" + fileName);
 
         try {
             attachment = project.createAttachment("Second Attachment",
                     fileName, input);
         } finally {
             if (input != null) {
                 input.close();
             }
         }
 
         String attachmentID = attachment.getID().toString();
         resetInstance();
 
         Attachment newAttachment = getInstance().get().attachmentByID(
                 attachmentID);
         Assert.assertEquals("image/png", newAttachment.getContentType());
         ByteArrayOutputStream output = new ByteArrayOutputStream(fileSize);
 
         try {
             newAttachment.writeTo(output);
             input = AttachmentTester.class.getResourceAsStream(fileName);
             if (input != null) input.mark(0);
         } finally {
             if (output != null) {
                 output.close();
             }
         }
 
         InputStream expected = new ByteArrayInputStream(output.toByteArray());
 
         try {
        	Assert.assertNotNull("input stream is null", input);
             Assert.assertTrue(StreamComparer.compareStream(input, expected));
         } finally {
             if (expected != null) {
                 expected.close();
             }
             if (input != null) {
                 input.close();
             }
         }
     }
 
     @Test
     public void testDelete() {
         final String fileName = "test.txt";
         final String content = "This is the first attachment's content. At: "
                 + DateTime.now();
         InputStream input = new ByteArrayInputStream(content.getBytes());
 
         Story story = getInstance().create().story("StoryForAttachment", getSandboxProject());
         Attachment attachment = getInstance().create().attachment("AttachmentName", story, fileName, input);
         final String attachmentID = attachment.getID().toString();
         attachment = getInstance().get().attachmentByID(attachmentID);
         Assert.assertNotNull(attachment);
         Assert.assertTrue(attachment.canDelete());
         attachment.delete();
         resetInstance();
 
         Assert.assertNull(getInstance().get().attachmentByID(attachmentID));
     }
 
     @Test(expected = AttachmentLengthExceededException.class)
     public void testMaximumFileSize() throws IOException {
         Project project = getInstance().get().projectByID(SCOPE_ZERO);
 
         InputStream input = new RandomStream(
                 getInstance().getConfiguration().maximumAttachmentSize + 1);
 
         project.createAttachment("Random Attachment", "random.txt", input);
     }
 
     @Test
     public void testUnderMaximumFileSize() throws IOException {
         Project project = getInstance().get().projectByID(SCOPE_ZERO);
 
         InputStream input = new RandomStream(
                 getInstance().getConfiguration().maximumAttachmentSize);
         Attachment att = project.createAttachment("Random Attachment",
                 "random.txt", input);
         Assert.assertNotNull(att);
     }
 }
