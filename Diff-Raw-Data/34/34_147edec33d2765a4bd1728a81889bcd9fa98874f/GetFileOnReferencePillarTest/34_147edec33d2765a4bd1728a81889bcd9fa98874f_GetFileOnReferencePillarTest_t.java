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
 import java.io.InputStream;
 import java.math.BigInteger;
 import java.net.URL;
 import java.util.Date;
 
 import org.bitrepository.bitrepositoryelements.ChecksumDataForFileTYPE;
 import org.bitrepository.bitrepositoryelements.FilePart;
 import org.bitrepository.bitrepositoryelements.ResponseCode;
 import org.bitrepository.bitrepositorymessages.GetFileFinalResponse;
 import org.bitrepository.bitrepositorymessages.GetFileProgressResponse;
 import org.bitrepository.bitrepositorymessages.GetFileRequest;
 import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetFileRequest;
 import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetFileResponse;
 import org.bitrepository.common.utils.FileUtils;
 import org.bitrepository.pillar.messagefactories.GetFileMessageFactory;
 import org.bitrepository.protocol.FileExchange;
 import org.bitrepository.protocol.ProtocolComponentFactory;
 import org.testng.Assert;
 import org.testng.annotations.AfterMethod;
 import org.testng.annotations.BeforeMethod;
 import org.testng.annotations.Test;
 
 /**
  * Tests the PutFile functionality on the ReferencePillar.
  */
 public class GetFileOnReferencePillarTest extends ReferencePillarTest {
     protected GetFileMessageFactory msgFactory;
 
     @BeforeMethod (alwaysRun=true)
     public void initialiseGetFileTests() throws Exception {
         msgFactory = new GetFileMessageFactory(clientSettings);
     }
 
     @AfterMethod (alwaysRun=true) 
     public void closeArchive() {
         File dir = new File(componentSettings.getReferenceSettings().getPillarSettings().getFileDir().get(0));
         if(dir.exists()) {
             FileUtils.delete(dir);
         }
         
         if(mediator != null) {
             mediator.close();
         }
     }
     
     @Test( groups = {"regressiontest", "pillartest"})
     public void pillarGetFileTestSuccessCase() throws Exception {
         addDescription("Tests the get functionality of the reference pillar for the successful scenario.");
         addStep("Set up constants and variables.", "Should not fail here!");
         String FILE_ADDRESS = "http://sandkasse-01.kb.dk/dav/test.txt";
         String pillarId = componentSettings.getReferenceSettings().getPillarSettings().getPillarID();
         String auditTrail = null;
         FilePart filePart = null;
         ChecksumDataForFileTYPE csData = null;
         
         addStep("Move the test file into the file directory.", "Should be all-right");
         File testfile = new File("src/test/resources/" + DEFAULT_FILE_ID);
         Assert.assertTrue(testfile.isFile(), "The test file does not exist at '" + testfile.getAbsolutePath() + "'.");
         Long FILE_SIZE = testfile.length();
         String FILE_ID = DEFAULT_FILE_ID + new Date().getTime();
         
        File dir = new File(componentSettings.getReferenceSettings().getPillarSettings().getFileDir().get(0) + "/fileDir");
         Assert.assertTrue(dir.isDirectory(), "The file directory for the reference pillar should be instantiated at '"
                 + dir.getAbsolutePath() + "'");
         FileUtils.copyFile(testfile, new File(dir, FILE_ID));
         
         addStep("Create and send the identify request message.", 
                 "Should be received and handled by the pillar.");
         IdentifyPillarsForGetFileRequest identifyRequest = msgFactory.createIdentifyPillarsForGetFileRequest(
                 auditTrail, FILE_ID, getPillarID(), clientDestinationId);
         messageBus.sendMessage(identifyRequest);
         
         addStep("Retrieve and validate the response getPillarID() the pillar.", 
                 "The pillar should make a response.");
         IdentifyPillarsForGetFileResponse receivedIdentifyResponse = clientTopic.waitForMessage(
                 IdentifyPillarsForGetFileResponse.class);
         Assert.assertEquals(receivedIdentifyResponse, 
                 msgFactory.createIdentifyPillarsForGetFileResponse(
                         identifyRequest.getCorrelationID(),
                         FILE_ID, 
                         pillarId,
                         receivedIdentifyResponse.getReplyTo(),
                         receivedIdentifyResponse.getResponseInfo(),
                         receivedIdentifyResponse.getTimeToDeliver(),
                         receivedIdentifyResponse.getTo()));
         Assert.assertEquals(receivedIdentifyResponse.getResponseInfo().getResponseCode(), 
                 ResponseCode.IDENTIFICATION_POSITIVE);
         
         addStep("Create and send the actual GetFile message to the pillar.", 
                 "Should be received and handled by the pillar.");
         GetFileRequest getRequest = msgFactory.createGetFileRequest(auditTrail, 
                 receivedIdentifyResponse.getCorrelationID(), FILE_ADDRESS, FILE_ID, filePart, getPillarID(), pillarId, 
                 clientDestinationId, receivedIdentifyResponse.getReplyTo());
         messageBus.sendMessage(getRequest);
         
         addStep("Retrieve the ProgressResponse for the GetFile request", 
                 "The GetFile progress response should be sent by the pillar.");
         GetFileProgressResponse progressResponse = clientTopic.waitForMessage(GetFileProgressResponse.class);
         Assert.assertEquals(progressResponse,
                 msgFactory.createGetFileProgressResponse(
                         csData, 
                         identifyRequest.getCorrelationID(), 
                         progressResponse.getFileAddress(), 
                         progressResponse.getFileID(), 
                         filePart,
                         pillarId, 
                         FILE_SIZE,
                         progressResponse.getResponseInfo(), 
                         progressResponse.getReplyTo(), 
                         progressResponse.getTo()));
         
         addStep("Retrieve the FinalResponse for the GetFile request", 
                 "The GetFile response should be sent by the pillar.");
         GetFileFinalResponse finalResponse = clientTopic.waitForMessage(GetFileFinalResponse.class);
         Assert.assertEquals(finalResponse.getResponseInfo().getResponseCode(), ResponseCode.OPERATION_COMPLETED);
         
         Assert.assertEquals(finalResponse,
                 msgFactory.createGetFileFinalResponse(
                         identifyRequest.getCorrelationID(), 
                         finalResponse.getFileAddress(), 
                         finalResponse.getFileID(), 
                         filePart,
                         pillarId, 
                         finalResponse.getReplyTo(), 
                         finalResponse.getResponseInfo(), 
                         finalResponse.getTo()));
         
         Assert.assertEquals(alarmDispatcher.getCallsForSendAlarm(), 0, "Should not have send any alarms.");
         Assert.assertEquals(audits.getCallsForAuditEvent(), 1, "Should deliver 1 audit. Handling of the GetFile "
                 + "operation");
     }
     
     @SuppressWarnings("deprecation")
     @Test( groups = {"regressiontest", "pillartest"})
     public void pillarGetFilePartTest() throws Exception {
         addDescription("Tests the get functionality of the reference pillar for a file part. Successful scenario.");
         addStep("Set up constants and variables.", "Should not fail here!");
         String FILE_ADDRESS = "http://sandkasse-01.kb.dk/dav/test2.txt";
         String pillarId = componentSettings.getReferenceSettings().getPillarSettings().getPillarID();
         String auditTrail = null;
         FilePart filePart = new FilePart();
         filePart.setPartLength(BigInteger.ONE);
         filePart.setPartOffSet(BigInteger.ONE);
         ChecksumDataForFileTYPE csData = null;
         
         addStep("Move the test file into the file directory.", "Should be all-right");
         File testfile = new File("src/test/resources/" + DEFAULT_FILE_ID);
         Assert.assertTrue(testfile.isFile(), "The test file does not exist at '" + testfile.getAbsolutePath() + "'.");
         Long FILE_SIZE = testfile.length();
         String FILE_ID = DEFAULT_FILE_ID + new Date().getTime();
         
        File dir = new File(componentSettings.getReferenceSettings().getPillarSettings().getFileDir().get(0) + "/fileDir");
         Assert.assertTrue(dir.isDirectory(), "The file directory for the reference pillar should be instantiated at '"
                 + dir.getAbsolutePath() + "'");
         FileUtils.copyFile(testfile, new File(dir, FILE_ID));
         
         addStep("Create and send the actual GetFile message to the pillar.", 
                 "Should be received and handled by the pillar.");
         GetFileRequest getRequest = msgFactory.createGetFileRequest(auditTrail, 
                 msgFactory.getNewCorrelationID(), FILE_ADDRESS, FILE_ID, filePart, getPillarID(), pillarId, 
                 clientDestinationId, pillarDestinationId);
         messageBus.sendMessage(getRequest);
         
         addStep("Retrieve the ProgressResponse for the GetFile request", 
                 "The GetFile progress response should be sent by the pillar.");
         GetFileProgressResponse progressResponse = clientTopic.waitForMessage(GetFileProgressResponse.class);
         Assert.assertEquals(progressResponse,
                 msgFactory.createGetFileProgressResponse(
                         csData, 
                         getRequest.getCorrelationID(), 
                         FILE_ADDRESS, 
                         progressResponse.getFileID(), 
                         filePart,
                         pillarId, 
                         FILE_SIZE,
                         progressResponse.getResponseInfo(), 
                         progressResponse.getReplyTo(), 
                         progressResponse.getTo()));
         
         addStep("Retrieve the FinalResponse for the GetFile request", 
                 "The GetFile response should be sent by the pillar.");
         GetFileFinalResponse finalResponse = clientTopic.waitForMessage(GetFileFinalResponse.class);
         Assert.assertEquals(finalResponse.getResponseInfo().getResponseCode(), ResponseCode.OPERATION_COMPLETED);
         
         Assert.assertEquals(finalResponse,
                 msgFactory.createGetFileFinalResponse(
                         getRequest.getCorrelationID(), 
                         FILE_ADDRESS, 
                         finalResponse.getFileID(), 
                         filePart,
                         pillarId, 
                         finalResponse.getReplyTo(), 
                         finalResponse.getResponseInfo(), 
                         finalResponse.getTo()));
         
         addStep("Validate the uploaded result-file.", "Should only contain the second letter of the file, which is a "
                 + "'A'. Any following extracted bytes should have the value '-1'.");
         FileExchange fe = ProtocolComponentFactory.getInstance().getFileExchange();
         InputStream is = fe.downloadFromServer(new URL(FILE_ADDRESS));
         
         int digit1 = is.read();
         Assert.assertEquals(digit1, (int) 'A');
         int digit2 = is.read();
         Assert.assertEquals(digit2, -1);
     }
     
     @Test( groups = {"regressiontest", "pillartest"})
     public void pillarGetFileTestFailedNoSuchFile() throws Exception {
         addDescription("Tests that the ReferencePillar is able to reject a GetFile requests for a file, which it does not have.");
         addStep("Set up constants and variables.", "Should not fail here!");
         String pillarId = componentSettings.getReferenceSettings().getPillarSettings().getPillarID();
         String auditTrail = null;
         
         addStep("Move the test file into the file directory.", "Should be all-right");
         String FILE_ID = DEFAULT_FILE_ID + new Date().getTime();
         
         addStep("Create and send the identify request message.", 
                 "Should be received and handled by the pillar.");
         IdentifyPillarsForGetFileRequest identifyRequest = msgFactory.createIdentifyPillarsForGetFileRequest(
                 auditTrail, FILE_ID, getPillarID(), clientDestinationId);
         messageBus.sendMessage(identifyRequest);
         
         addStep("Retrieve and validate the response getPillarID() the pillar.", 
                 "The pillar should make a response.");
         IdentifyPillarsForGetFileResponse receivedIdentifyResponse = clientTopic.waitForMessage(
                 IdentifyPillarsForGetFileResponse.class);
         Assert.assertEquals(receivedIdentifyResponse, 
                 msgFactory.createIdentifyPillarsForGetFileResponse(
                         identifyRequest.getCorrelationID(),
                         FILE_ID, 
                         pillarId,
                         receivedIdentifyResponse.getReplyTo(),
                         receivedIdentifyResponse.getResponseInfo(),
                         receivedIdentifyResponse.getTimeToDeliver(),
                         receivedIdentifyResponse.getTo()));
         Assert.assertEquals(receivedIdentifyResponse.getResponseInfo().getResponseCode(), 
                 ResponseCode.FILE_NOT_FOUND_FAILURE);        
     }
     
     @Test( groups = {"regressiontest", "pillartest"})
     public void pillarGetFileTestFailedNoSuchFileInOperation() throws Exception {
         addDescription("Tests that the ReferencePillar is able to reject a GetFile requests for a file, which it does not have.");
         addStep("Set up constants and variables.", "Should not fail here!");
         String FILE_ADDRESS = "http://sandkasse-01.kb.dk/dav/test.txt";
         String pillarId = componentSettings.getReferenceSettings().getPillarSettings().getPillarID();
         String auditTrail = null;
         FilePart filePart = null;
         
         addStep("Move the test file into the file directory.", "Should be all-right");
         File testfile = new File("src/test/resources/" + DEFAULT_FILE_ID);
         Assert.assertTrue(testfile.isFile(), "The test file does not exist at '" + testfile.getAbsolutePath() + "'.");
         String FILE_ID = DEFAULT_FILE_ID + new Date().getTime();
         
         addStep("Create and send the actual GetFile message to the pillar.", 
                 "Should be received and handled by the pillar.");
         GetFileRequest getRequest = msgFactory.createGetFileRequest(auditTrail, 
                 msgFactory.getNewCorrelationID(), FILE_ADDRESS, FILE_ID, filePart, getPillarID(), pillarId, 
                 clientDestinationId, pillarDestinationId);
         messageBus.sendMessage(getRequest);
         
         addStep("Retrieve the FinalResponse for the GetFile request", 
                 "The GetFile response should be sent by the pillar.");
         GetFileFinalResponse finalResponse = clientTopic.waitForMessage(GetFileFinalResponse.class);
         Assert.assertEquals(finalResponse.getResponseInfo().getResponseCode(), ResponseCode.FILE_NOT_FOUND_FAILURE);
         
         Assert.assertEquals(finalResponse,
                 msgFactory.createGetFileFinalResponse(
                         getRequest.getCorrelationID(),
                         finalResponse.getFileAddress(), 
                         finalResponse.getFileID(), 
                         filePart,
                         pillarId, 
                         finalResponse.getReplyTo(), 
                         finalResponse.getResponseInfo(), 
                         finalResponse.getTo()));     
     }
     
     @Test( groups = {"regressiontest", "pillartest"})
     public void pillarGeneralTestWrongCollectionID() throws Exception {
         addDescription("Tests that the ReferencePillar is able to reject a GetFile requests with a wrong CollectionID.");
         addStep("Set up constants and variables.", "Should not fail here!");
         String auditTrail = null;
         
         addStep("Move the test file into the file directory.", "Should be all-right");
         String FILE_ID = DEFAULT_FILE_ID + new Date().getTime();
         
         addStep("Create and send the identify request message.", 
                 "Should be received by the pillar, which should issue an alarm.");
         IdentifyPillarsForGetFileRequest identifyRequest = msgFactory.createIdentifyPillarsForGetFileRequest(
                 auditTrail, FILE_ID, getPillarID(), clientDestinationId);
         identifyRequest.setCollectionID(componentSettings.getCollectionID() + "ERROR");
         messageBus.sendMessage(identifyRequest);
         
         // TODO fix this!
 //      addStep("Validate that the pillar has sent an Alarm.", 
 //              "Only one alarm should have been sent.");
 //      Assert.assertEquals(alarmDispatcher.getCallsForSendAlarm(), 1);
     }
     
     @Test( groups = {"regressiontest", "pillartest"})
     public void pillarGeneralTestWrongPillarID() throws Exception {
         addDescription("Tests that the ReferencePillar is able to reject a GetFile requests with a wrong pillarID.");
         addStep("Set up constants and variables.", "Should not fail here!");
         String FILE_ADDRESS = "http://sandkasse-01.kb.dk/dav/test.txt";
         String pillarId = componentSettings.getReferenceSettings().getPillarSettings().getPillarID();
         String auditTrail = null;
         FilePart filePart = null;
         
         addStep("Move the test file into the file directory.", "Should be all-right");
         File testfile = new File("src/test/resources/" + DEFAULT_FILE_ID);
         Assert.assertTrue(testfile.isFile(), "The test file does not exist at '" + testfile.getAbsolutePath() + "'.");
         String FILE_ID = DEFAULT_FILE_ID + new Date().getTime();
         
        File dir = new File(componentSettings.getReferenceSettings().getPillarSettings().getFileDir().get(0) + "/fileDir");
         Assert.assertTrue(dir.isDirectory(), "The file directory for the reference pillar should be instantiated at '"
                 + dir.getAbsolutePath() + "'");
         FileUtils.copyFile(testfile, new File(dir, FILE_ID));
         
         addStep("Create and send the identify request message.", 
                 "Should be received and handled by the pillar.");
         IdentifyPillarsForGetFileRequest identifyRequest = msgFactory.createIdentifyPillarsForGetFileRequest(
                 auditTrail, FILE_ID, getPillarID(), clientDestinationId);
         messageBus.sendMessage(identifyRequest);
         
         addStep("Retrieve and validate the response getPillarID() the pillar.", 
                 "The pillar should make a response.");
         IdentifyPillarsForGetFileResponse receivedIdentifyResponse = clientTopic.waitForMessage(
                 IdentifyPillarsForGetFileResponse.class);
         Assert.assertEquals(receivedIdentifyResponse, 
                 msgFactory.createIdentifyPillarsForGetFileResponse(
                         identifyRequest.getCorrelationID(),
                         FILE_ID, 
                         pillarId,
                         receivedIdentifyResponse.getReplyTo(),
                         receivedIdentifyResponse.getResponseInfo(),
                         receivedIdentifyResponse.getTimeToDeliver(),
                         receivedIdentifyResponse.getTo()));
         Assert.assertEquals(receivedIdentifyResponse.getResponseInfo().getResponseCode(), 
                 ResponseCode.IDENTIFICATION_POSITIVE);
         
         addStep("Create and send the actual GetFile message to the pillar.", 
                 "Should be received and handled by the pillar.");
         GetFileRequest getRequest = msgFactory.createGetFileRequest(auditTrail, 
                 receivedIdentifyResponse.getCorrelationID(), FILE_ADDRESS, FILE_ID, filePart, getPillarID(), pillarId, 
                 clientDestinationId, receivedIdentifyResponse.getReplyTo());
         getRequest.setPillarID(pillarId + "-ERROR");
         messageBus.sendMessage(getRequest);
         
         // TODO fix this!
 //        addStep("Validate that the pillar has sent an Alarm.", 
 //                "Only one alarm should have been sent.");
 //        Assert.assertEquals(alarmDispatcher.getCallsForSendAlarm(), 1);
     }
     
     
     @Test( groups = {"regressiontest", "pillartest"})
     public void pillarGeneralTestBadDeliveryURL() throws Exception {
         addDescription("Tests that the ReferencePillar can handle a bad delivery URL.");
         addStep("Set up constants and variables.", "Should not fail here!");
         String auditTrail = null;
 
         addStep("Move the test file into the file directory.", "Should be all-right");
         File testfile = new File("src/test/resources/" + DEFAULT_FILE_ID);
         Assert.assertTrue(testfile.isFile(), "The test file does not exist at '" + testfile.getAbsolutePath() + "'.");
         String FILE_ID = DEFAULT_FILE_ID + new Date().getTime();
         String fileAddress = "http://127.0.0.1/¾" + new Date().getTime();
         
        File dir = new File(componentSettings.getReferenceSettings().getPillarSettings().getFileDir().get(0) + "/fileDir");
         Assert.assertTrue(dir.isDirectory(), "The file directory for the reference pillar should be instantiated at '"
                 + dir.getAbsolutePath() + "'");
         FileUtils.copyFile(testfile, new File(dir, FILE_ID));
         
         addStep("Create and send the actual GetFile message to the pillar.", 
         "Should be received and handled by the pillar.");
         GetFileRequest getRequest = msgFactory.createGetFileRequest(auditTrail, 
                 msgFactory.getNewCorrelationID(), fileAddress, FILE_ID, null, getPillarID(), getPillarID(), 
                 clientDestinationId, pillarDestinationId);
         messageBus.sendMessage(getRequest);
 
         addStep("Retrieve the FinalResponse for the GetFile request", 
         "The GetFile response should be sent by the pillar.");
         GetFileFinalResponse finalResponse = clientTopic.waitForMessage(GetFileFinalResponse.class);
         Assert.assertEquals(finalResponse.getResponseInfo().getResponseCode(), ResponseCode.FILE_TRANSFER_FAILURE);
     }
 }
