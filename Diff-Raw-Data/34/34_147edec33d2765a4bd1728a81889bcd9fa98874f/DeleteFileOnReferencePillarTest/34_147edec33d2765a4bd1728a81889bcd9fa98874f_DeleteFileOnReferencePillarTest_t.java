 /*
  * #%L
  * Bitrepository Reference Pillar
  * 
  * $Id: PutFileOnReferencePillarTest.java 589 2011-12-01 15:34:42Z jolf $
  * $HeadURL: https://sbforge.org/svn/bitrepository/bitrepository-reference/trunk/bitrepository-reference-pillar/src/test/java/org/bitrepository/pillar/PutFileOnReferencePillarTest.java $
  * %%
  * Copyright (C) 2010 - 2011 The State and University Library, The Royal Library and The State Archives, Denmark
  * %%
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License as 
  * published by the Free Software Foundation, either version 2.1 of the 
  * License, or (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Lesser Public License for more details.
  * 
  * You should have received a copy of the GNU General Lesser Public 
  * License along with this program.  If not, see
  * <http://www.gnu.org/licenses/lgpl-2.1.html>.
  * #L%
  */
 package org.bitrepository.pillar.referencepillar;
 
 import java.io.File;
 import java.util.Date;
 
 import org.bitrepository.bitrepositoryelements.ChecksumDataForFileTYPE;
 import org.bitrepository.bitrepositoryelements.ChecksumSpecTYPE;
 import org.bitrepository.bitrepositoryelements.ChecksumType;
 import org.bitrepository.bitrepositoryelements.ResponseCode;
 import org.bitrepository.bitrepositorymessages.DeleteFileFinalResponse;
 import org.bitrepository.bitrepositorymessages.DeleteFileProgressResponse;
 import org.bitrepository.bitrepositorymessages.DeleteFileRequest;
 import org.bitrepository.bitrepositorymessages.IdentifyPillarsForDeleteFileRequest;
 import org.bitrepository.bitrepositorymessages.IdentifyPillarsForDeleteFileResponse;
 import org.bitrepository.common.utils.Base16Utils;
 import org.bitrepository.common.utils.CalendarUtils;
 import org.bitrepository.common.utils.ChecksumUtils;
 import org.bitrepository.common.utils.FileUtils;
 import org.bitrepository.pillar.messagefactories.DeleteFileMessageFactory;
 import org.testng.Assert;
 import org.testng.annotations.BeforeMethod;
 import org.testng.annotations.Test;
 
 /**
  * Tests the PutFile functionality on the ReferencePillar.
  */
 public class DeleteFileOnReferencePillarTest extends ReferencePillarTest {
     private DeleteFileMessageFactory msgFactory;
 
     @BeforeMethod (alwaysRun=true)
     public void initialiseDeleteFileTests() throws Exception {
         msgFactory = new DeleteFileMessageFactory(clientSettings, getPillarID(), pillarDestinationId);
     }
 
     @Test( groups = {"regressiontest", "pillartest"})
     public void pillarDeleteFileTestSuccessCase() throws Exception {
         addDescription("Tests the DeleteFile functionality of the reference pillar for the successful scenario.");
         addStep("Set up constants and variables.", "Should not fail here!");
         
         ChecksumSpecTYPE csSpecExisting = new ChecksumSpecTYPE();
         csSpecExisting.setChecksumSalt(null);
         csSpecExisting.setChecksumType(ChecksumType.MD5);
 
         ChecksumSpecTYPE csSpecRequest = new ChecksumSpecTYPE();
         csSpecRequest.setChecksumSalt(null);
         csSpecRequest.setChecksumType(ChecksumType.SHA384);
 
         addStep("Move the test file into the file directory.", "Should be all-right");
         File testfile = new File("src/test/resources/" + DEFAULT_FILE_ID);
         Assert.assertTrue(testfile.isFile(), "The test file does not exist at '" + testfile.getAbsolutePath() + "'.");
 
         ChecksumDataForFileTYPE csData = new ChecksumDataForFileTYPE();
         csData.setCalculationTimestamp(CalendarUtils.getEpoch());
         csData.setChecksumSpec(csSpecExisting);
         csData.setChecksumValue(Base16Utils.encodeBase16(ChecksumUtils.generateChecksum(testfile, csSpecExisting)));
         
        File dir = new File(componentSettings.getReferenceSettings().getPillarSettings().getFileDir().get(0) + "/fileDir");
         Assert.assertTrue(dir.isDirectory(), "The file directory for the reference pillar should be instantiated at '"
                 + dir.getAbsolutePath() + "'");
         FileUtils.copyFile(testfile, new File(dir, DEFAULT_FILE_ID));
         
         addStep("Create and send the identify request message.", 
                 "Should be received and handled by the pillar.");
         IdentifyPillarsForDeleteFileRequest identifyRequest = msgFactory.createIdentifyPillarsForDeleteFileRequest(
                 DEFAULT_FILE_ID);
         messageBus.sendMessage(identifyRequest);
         
         addStep("Retrieve and validate the response getPillarID() the pillar.", 
                 "The pillar should make a response.");
         IdentifyPillarsForDeleteFileResponse receivedIdentifyResponse = clientTopic.waitForMessage(
                 IdentifyPillarsForDeleteFileResponse.class);
 
         Assert.assertNotNull(receivedIdentifyResponse);
         Assert.assertEquals(receivedIdentifyResponse.getCorrelationID(), identifyRequest.getCorrelationID());
         Assert.assertEquals(receivedIdentifyResponse.getFileID(), DEFAULT_FILE_ID);
         Assert.assertEquals(receivedIdentifyResponse.getFrom(), getPillarID());
         Assert.assertEquals(receivedIdentifyResponse.getPillarID(), getPillarID());
         Assert.assertEquals(receivedIdentifyResponse.getReplyTo(), pillarDestinationId);
         Assert.assertEquals(receivedIdentifyResponse.getResponseInfo().getResponseCode(),
                 ResponseCode.IDENTIFICATION_POSITIVE);
 
         
         addStep("Create and send the actual DeleteFile message to the pillar.", 
                 "Should be received and handled by the pillar.");
 
         DeleteFileRequest deleteFileRequest = msgFactory.createDeleteFileRequest(csData, csSpecRequest, DEFAULT_FILE_ID);
         messageBus.sendMessage(deleteFileRequest);
         
         addStep("Retrieve the ProgressResponse for the DeleteFile request", 
                 "The DeleteFile progress response should be sent by the pillar.");
         DeleteFileProgressResponse progressResponse = clientTopic.waitForMessage(DeleteFileProgressResponse.class);
         Assert.assertNotNull(progressResponse);
         Assert.assertEquals(progressResponse.getCorrelationID(), deleteFileRequest.getCorrelationID());
         Assert.assertEquals(progressResponse.getFileID(), DEFAULT_FILE_ID);
         Assert.assertEquals(progressResponse.getFrom(), getPillarID());
         Assert.assertEquals(progressResponse.getPillarID(), getPillarID());
         Assert.assertEquals(progressResponse.getReplyTo(), pillarDestinationId);
         Assert.assertEquals(progressResponse.getResponseInfo().getResponseCode(),
                 ResponseCode.OPERATION_ACCEPTED_PROGRESS);
         
         addStep("Retrieve the FinalResponse for the DeleteFile request", 
                 "The DeleteFile response should be sent by the pillar.");
         DeleteFileFinalResponse finalResponse = clientTopic.waitForMessage(DeleteFileFinalResponse.class);
         Assert.assertEquals(finalResponse.getResponseInfo().getResponseCode(), ResponseCode.OPERATION_COMPLETED);
 
         Assert.assertNotNull(finalResponse);
         Assert.assertEquals(finalResponse.getCorrelationID(), deleteFileRequest.getCorrelationID());
         Assert.assertEquals(finalResponse.getFileID(), DEFAULT_FILE_ID);
         Assert.assertEquals(finalResponse.getFrom(), getPillarID());
         Assert.assertEquals(finalResponse.getPillarID(), getPillarID());
         Assert.assertEquals(finalResponse.getReplyTo(), pillarDestinationId);
         Assert.assertEquals(finalResponse.getResponseInfo().getResponseCode(),
                 ResponseCode.OPERATION_COMPLETED);
         //ToDO Implement the assertion below
         //Assert.assertEquals(finalResponse.getChecksumDataForExistingFile(), ??);
 
         Assert.assertEquals(alarmDispatcher.getCallsForSendAlarm(), 0, "Should not have send any alarms.");
         Assert.assertEquals(audits.getCallsForAuditEvent(), 3, "Should deliver 3 audits. One for delete and two for "
                 + "calculate checksums (both the validation and the requested).");
     }
     
     @Test( groups = {"regressiontest", "pillartest"})
     public void pillarDeleteFileTestFailedNoSuchFile() throws Exception {
         addDescription("Tests the DeleteFile functionality of the reference pillar for the scenario when the file does not exist.");
         addStep("Set up constants and variables.", "Should not fail here!");
         String FILE_ID = DEFAULT_FILE_ID + new Date().getTime();
         String wrongID = "error-file-id";
         
         ChecksumSpecTYPE csSpecExisting = new ChecksumSpecTYPE();
         csSpecExisting.setChecksumSalt(null);
         csSpecExisting.setChecksumType(ChecksumType.MD5);
 
         ChecksumSpecTYPE csSpecRequest = new ChecksumSpecTYPE();
         csSpecRequest.setChecksumSalt(null);
         csSpecRequest.setChecksumType(ChecksumType.SHA384);
 
         addStep("Move the test file into the file directory.", "Should be all-right");
         File testfile = new File("src/test/resources/" + DEFAULT_FILE_ID);
         Assert.assertTrue(testfile.isFile(), "The test file does not exist at '" + testfile.getAbsolutePath() + "'.");
 
         ChecksumDataForFileTYPE csData = new ChecksumDataForFileTYPE();
         csData.setCalculationTimestamp(CalendarUtils.getEpoch());
         csData.setChecksumSpec(csSpecExisting);
         csData.setChecksumValue(Base16Utils.encodeBase16(ChecksumUtils.generateChecksum(testfile, csSpecExisting)));
         
        File dir = new File(componentSettings.getReferenceSettings().getPillarSettings().getFileDir().get(0) + "/fileDir");
         Assert.assertTrue(dir.isDirectory(), "The file directory for the reference pillar should be instantiated at '"
                 + dir.getAbsolutePath() + "'");
         FileUtils.copyFile(testfile, new File(dir, FILE_ID));
         
         addStep("Create and send the identify request message.", 
                 "Should be received and handled by the pillar.");
         messageBus.sendMessage(msgFactory.createIdentifyPillarsForDeleteFileRequest(wrongID));
         
         addStep("Retrieve and validate the response getPillarID() the pillar.", 
                 "The pillar should make a response.");
         IdentifyPillarsForDeleteFileResponse receivedIdentifyResponse = clientTopic.waitForMessage(
                 IdentifyPillarsForDeleteFileResponse.class);
         Assert.assertEquals(receivedIdentifyResponse.getResponseInfo().getResponseCode(), 
                 ResponseCode.FILE_NOT_FOUND_FAILURE);     
     }
 
     @Test( groups = {"regressiontest", "pillartest"})
     public void pillarDeleteFileTestFailedNoSuchFileInOperation() throws Exception {
         addDescription("Tests the DeleteFile functionality of the reference pillar for the scenario when the file does not exist.");
         addStep("Set up constants and variables.", "Should not fail here!");
         String FILE_ID = DEFAULT_FILE_ID + new Date().getTime();
         
         ChecksumSpecTYPE csSpecExisting = new ChecksumSpecTYPE();
         csSpecExisting.setChecksumSalt(null);
         csSpecExisting.setChecksumType(ChecksumType.MD5);
 
         ChecksumSpecTYPE csSpecRequest = new ChecksumSpecTYPE();
         csSpecRequest.setChecksumSalt(null);
         csSpecRequest.setChecksumType(ChecksumType.SHA384);
 
         addStep("Move the test file into the file directory.", "Should be all-right");
         File testfile = new File("src/test/resources/" + DEFAULT_FILE_ID);
         Assert.assertTrue(testfile.isFile(), "The test file does not exist at '" + testfile.getAbsolutePath() + "'.");
 
         ChecksumDataForFileTYPE csData = new ChecksumDataForFileTYPE();
         csData.setCalculationTimestamp(CalendarUtils.getEpoch());
         csData.setChecksumSpec(csSpecExisting);
         csData.setChecksumValue(Base16Utils.encodeBase16(ChecksumUtils.generateChecksum(testfile, csSpecExisting)));
         
         addStep("Create and send the actual DeleteFile message to the pillar.", 
                 "Should be received and handled by the pillar.");
         messageBus.sendMessage(msgFactory.createDeleteFileRequest(csData, null, FILE_ID));
         
         addStep("Retrieve the FinalResponse for the DeleteFile request", 
                 "The DeleteFile response should be sent by the pillar.");
         DeleteFileFinalResponse finalResponse = clientTopic.waitForMessage(DeleteFileFinalResponse.class);
         Assert.assertEquals(finalResponse.getResponseInfo().getResponseCode(), ResponseCode.FILE_NOT_FOUND_FAILURE);
     }
     
     @Test( groups = {"regressiontest", "pillartest"})
     public void pillarDeleteFileTestFailedWrongChecksum() throws Exception {
         addDescription("Tests the DeleteFile functionality of the reference pillar for scenario when a wrong "
                 + "checksum is given as argument.");
         addStep("Set up constants and variables.", "Should not fail here!");
         String FILE_ID = DEFAULT_FILE_ID + new Date().getTime();
         
         ChecksumSpecTYPE csSpecExisting = new ChecksumSpecTYPE();
         csSpecExisting.setChecksumSalt(null);
         csSpecExisting.setChecksumType(ChecksumType.MD5);
 
         ChecksumSpecTYPE csSpecRequest = new ChecksumSpecTYPE();
         csSpecRequest.setChecksumSalt(null);
         csSpecRequest.setChecksumType(ChecksumType.SHA1);
 
         addStep("Move the test file into the file directory.", "Should be all-right");
         File testfile = new File("src/test/resources/" + DEFAULT_FILE_ID);
         Assert.assertTrue(testfile.isFile(), "The test file does not exist at '" + testfile.getAbsolutePath() + "'.");
 
         addStep("Put one checksum spec in the data and calculate the checksum with the other spec.", 
                 "The wrong checksum is calculated.");
         ChecksumDataForFileTYPE csData = new ChecksumDataForFileTYPE();
         csData.setCalculationTimestamp(CalendarUtils.getEpoch());
         csData.setChecksumSpec(csSpecExisting);
         csData.setChecksumValue(Base16Utils.encodeBase16(ChecksumUtils.generateChecksum(testfile, csSpecRequest)));
         
        File dir = new File(componentSettings.getReferenceSettings().getPillarSettings().getFileDir().get(0) + "/fileDir");
         Assert.assertTrue(dir.isDirectory(), "The file directory for the reference pillar should be instantiated at '"
                 + dir.getAbsolutePath() + "'");
         FileUtils.copyFile(testfile, new File(dir, FILE_ID));
         
         addStep("Create and send the identify request message.", 
                 "Should be received and handled by the pillar.");
         messageBus.sendMessage(msgFactory.createIdentifyPillarsForDeleteFileRequest(FILE_ID));
         
         addStep("Retrieve and validate the response getPillarID() the pillar.", 
                 "The pillar should make a response.");
         IdentifyPillarsForDeleteFileResponse receivedIdentifyResponse = clientTopic.waitForMessage(
                 IdentifyPillarsForDeleteFileResponse.class);
         Assert.assertEquals(receivedIdentifyResponse.getResponseInfo().getResponseCode(), 
                 ResponseCode.IDENTIFICATION_POSITIVE);
         
         addStep("Create and send the actual DeleteFile message to the pillar.", 
                 "Should be received and handled by the pillar.");
         DeleteFileRequest deleteFileRequest = msgFactory.createDeleteFileRequest(csData, csSpecRequest, FILE_ID);
         messageBus.sendMessage(deleteFileRequest);
         
         addStep("Retrieve the FinalResponse for the DeleteFile request", 
                 "The DeleteFile response should be sent by the pillar.");
         DeleteFileFinalResponse finalResponse = clientTopic.waitForMessage(DeleteFileFinalResponse.class);
         Assert.assertEquals(finalResponse.getResponseInfo().getResponseCode(), ResponseCode.EXISTING_FILE_CHECKSUM_FAILURE);
         Assert.assertEquals(alarmDispatcher.getCallsForSendAlarm(), 1, "Should have tried to send an alarm.");
     }
     
 
     @Test( groups = {"regressiontest", "pillartest"})
     public void checksumPillarDeleteFileTestMissingChecksumArgument() throws Exception {
         addDescription("Tests that a missing 'ChecksumOnExistingFile' will not delete the file.");
         Assert.assertTrue(context.getSettings().getCollectionSettings().getProtocolSettings().isRequireChecksumForDestructiveRequests());
         initializeArchiveWithEmptyFile();
         messageBus.sendMessage(msgFactory.createDeleteFileRequest(null, null, DEFAULT_FILE_ID));
         DeleteFileFinalResponse finalResponse = clientTopic.waitForMessage(DeleteFileFinalResponse.class);
         Assert.assertEquals(finalResponse.getResponseInfo().getResponseCode(), 
                 ResponseCode.EXISTING_FILE_CHECKSUM_FAILURE);
         Assert.assertTrue(archive.hasFile(DEFAULT_FILE_ID));
     }
 
     @Test( groups = {"regressiontest", "pillartest"})
     public void checksumPillarDeleteFileTestAllowedMissingChecksum() throws Exception {
         addDescription("Tests that a missing 'ChecksumOnExistingFile' will delete the file, when it has been allowed "
                 + "to perform destructive operations in the settings.");
         context.getSettings().getCollectionSettings().getProtocolSettings().setRequireChecksumForDestructiveRequests(false);
         Assert.assertFalse(context.getSettings().getCollectionSettings().getProtocolSettings().isRequireChecksumForDestructiveRequests());
         initializeArchiveWithEmptyFile();
         messageBus.sendMessage(msgFactory.createDeleteFileRequest(null, null, DEFAULT_FILE_ID));
         DeleteFileFinalResponse finalResponse = clientTopic.waitForMessage(DeleteFileFinalResponse.class);
         Assert.assertEquals(finalResponse.getResponseInfo().getResponseCode(), 
                 ResponseCode.OPERATION_COMPLETED);
         Assert.assertFalse(archive.hasFile(DEFAULT_FILE_ID));
     }
 }
