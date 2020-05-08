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
 
 import java.io.InputStream;
 import java.math.BigInteger;
 import java.net.URL;
 import org.bitrepository.bitrepositoryelements.FilePart;
 import org.bitrepository.bitrepositoryelements.ResponseCode;
 import org.bitrepository.bitrepositorymessages.AlarmMessage;
 import org.bitrepository.bitrepositorymessages.GetFileFinalResponse;
 import org.bitrepository.bitrepositorymessages.GetFileProgressResponse;
 import org.bitrepository.bitrepositorymessages.GetFileRequest;
 import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetFileRequest;
 import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetFileResponse;
 import org.bitrepository.pillar.messagefactories.GetFileMessageFactory;
 import org.bitrepository.protocol.FileExchange;
 import org.bitrepository.protocol.ProtocolComponentFactory;
 import org.testng.Assert;
 import org.testng.annotations.Test;
 
 /**
  * Tests the PutFile functionality on the ReferencePillar.
  */
 public class GetFileOnReferencePillarTest extends ReferencePillarTest {
     protected GetFileMessageFactory msgFactory;
 
     @Override
     public void initializeCUT() {
         super.initializeCUT();
         msgFactory = new GetFileMessageFactory(collectionID, settingsForTestClient);
     }
 
     @Test( groups = {"regressiontest", "pillartest"})
     public void pillarGetFileTestSuccessCase() throws Exception {
         addDescription("Tests the get functionality of the reference pillar for the successful scenario.");
 
         addStep("Create and send the identify request message.",
                 "Should be received and handled by the pillar.");
         IdentifyPillarsForGetFileRequest identifyRequest = msgFactory.createIdentifyPillarsForGetFileRequest(
                 DEFAULT_AUDITINFORMATION, DEFAULT_FILE_ID, getPillarID(), clientDestinationId);
         messageBus.sendMessage(identifyRequest);
         
         addStep("Retrieve and validate the response getPillarID() the pillar.", 
                 "The pillar should make a response.");
         IdentifyPillarsForGetFileResponse receivedIdentifyResponse = clientReceiver.waitForMessage(
                 IdentifyPillarsForGetFileResponse.class);
         Assert.assertEquals(receivedIdentifyResponse.getResponseInfo().getResponseCode(), 
                 ResponseCode.IDENTIFICATION_POSITIVE);
         
         addStep("Create and send the actual GetFile message to the pillar.", 
                 "Should be received and handled by the pillar.");
         GetFileRequest getRequest = msgFactory.createGetFileRequest(DEFAULT_AUDITINFORMATION,
                 receivedIdentifyResponse.getCorrelationID(), DEFAULT_UPLOAD_FILE_ADDRESS, DEFAULT_FILE_ID, null,
                 getPillarID(),getPillarID(),
                 clientDestinationId, receivedIdentifyResponse.getReplyTo());
         messageBus.sendMessage(getRequest);
         
         addStep("Retrieve the ProgressResponse for the GetFile request", 
                 "The GetFile progress response should be sent by the pillar.");
         clientReceiver.waitForMessage(GetFileProgressResponse.class);
 
         addStep("Retrieve the FinalResponse for the GetFile request", 
                 "The GetFile response should be sent by the pillar.");
         GetFileFinalResponse finalResponse = clientReceiver.waitForMessage(GetFileFinalResponse.class);
         Assert.assertEquals(finalResponse.getResponseInfo().getResponseCode(), ResponseCode.OPERATION_COMPLETED);
 
         alarmReceiver.checkNoMessageIsReceived(AlarmMessage.class);
         Assert.assertEquals(audits.getCallsForAuditEvent(), 1, "Should deliver 1 audit. Handling of the GetFile "
                 + "operation");
     }
 
     // Please move this to the func test suite.
     @Test( groups = {"failing", "pillartest"})
     public void pillarGetFilePartTest() throws Exception {
         addDescription("Tests the get functionality of the reference pillar for a file part. Successful scenario.");
         addStep("Set up constants and variables.", "Should not fail here!");
         FilePart filePart = new FilePart();
         filePart.setPartLength(BigInteger.ONE);
         filePart.setPartOffSet(BigInteger.ONE);
 
         addStep("Create and send the actual GetFile message to the pillar.", 
                 "Should be received and handled by the pillar.");
         GetFileRequest getRequest = msgFactory.createGetFileRequest(DEFAULT_AUDITINFORMATION,
                 msgFactory.getNewCorrelationID(), DEFAULT_UPLOAD_FILE_ADDRESS, DEFAULT_FILE_ID, filePart, getPillarID(), getPillarID(),
                 clientDestinationId, pillarDestinationId);
         messageBus.sendMessage(getRequest);
         
         addStep("Retrieve the ProgressResponse for the GetFile request",
                 "The GetFile progress response should be sent by the pillar.");
         Assert.assertNotNull(clientReceiver.waitForMessage(GetFileProgressResponse.class));
         
         addStep("Retrieve the FinalResponse for the GetFile request", 
                 "The GetFile response should be sent by the pillar.");
         GetFileFinalResponse finalResponse = clientReceiver.waitForMessage(GetFileFinalResponse.class);
         Assert.assertEquals(finalResponse.getResponseInfo().getResponseCode(), ResponseCode.OPERATION_COMPLETED);
 
         addStep("Validate the uploaded result-file.", "Should only contain the second letter of the file, which is a "
                 + "'A'. Any following extracted bytes should have the value '-1'.");
         FileExchange fe = ProtocolComponentFactory.getInstance().getFileExchange(settingsForCUT);
         InputStream is = fe.downloadFromServer(new URL(DEFAULT_DOWNLOAD_FILE_ADDRESS));
         
         int digit1 = is.read();
         Assert.assertEquals(digit1, (int) 'A');
         int digit2 = is.read();
         Assert.assertEquals(digit2, -1);
     }
     
     @Test( groups = {"regressiontest", "pillartest"})
     public void pillarGetFileTestFailedNoSuchFile() throws Exception {
         addDescription("Tests that the ReferencePillar is able to reject a GetFile requests for a file, which it does not have.");
 
         addStep("Send a IdentifyPillarsForGetFileRequest for a nonexisting file.",
                 "Should be received and handled by the pillar.");
         IdentifyPillarsForGetFileRequest identifyRequest = msgFactory.createIdentifyPillarsForGetFileRequest(
                 DEFAULT_AUDITINFORMATION, NON_DEFAULT_FILE_ID, getPillarID(), clientDestinationId);
         messageBus.sendMessage(identifyRequest);
         IdentifyPillarsForGetFileResponse receivedIdentifyResponse = clientReceiver.waitForMessage(
                 IdentifyPillarsForGetFileResponse.class);
         Assert.assertEquals(receivedIdentifyResponse.getResponseInfo().getResponseCode(), 
                 ResponseCode.FILE_NOT_FOUND_FAILURE);        
     }
     
     @Test( groups = {"regressiontest", "pillartest"})
     public void pillarGetFileTestFailedNoSuchFileInOperation() throws Exception {
         addDescription("Tests that the ReferencePillar is able to reject a GetFile requests for a file, which it does not have.");
 
         addStep("Create and send the actual GetFile message to the pillar.", 
                 "Should be received and handled by the pillar.");
         GetFileRequest getRequest = msgFactory.createGetFileRequest(DEFAULT_AUDITINFORMATION,
                 msgFactory.getNewCorrelationID(), DEFAULT_UPLOAD_FILE_ADDRESS, NON_DEFAULT_FILE_ID, null, getPillarID(),
                 getPillarID(),
                 clientDestinationId, pillarDestinationId);
         messageBus.sendMessage(getRequest);
         
         addStep("Retrieve the FinalResponse for the GetFile request", 
                 "The GetFile response should be sent by the pillar.");
         GetFileFinalResponse finalResponse = clientReceiver.waitForMessage(GetFileFinalResponse.class);
         Assert.assertEquals(finalResponse.getResponseInfo().getResponseCode(), ResponseCode.FILE_NOT_FOUND_FAILURE);
     }
     
     @Test( groups = {"regressiontest", "pillartest"})
     public void pillarGeneralTestWrongCollectionID() throws Exception {
         addDescription("Tests that the ReferencePillar is able to reject a GetFile requests with a wrong CollectionID.");
         addStep("Set up constants and variables.", "Should not fail here!");
 
         addStep("Create and send the identify request message.", 
                 "Should be received by the pillar, which should issue an alarm.");
         IdentifyPillarsForGetFileRequest identifyRequest = msgFactory.createIdentifyPillarsForGetFileRequest(
                 DEFAULT_UPLOAD_FILE_ADDRESS, DEFAULT_FILE_ID, getPillarID(), clientDestinationId);
         identifyRequest.setCollectionID(collectionID + "ERROR");
         messageBus.sendMessage(identifyRequest);

        addStep("Validate that the pillar has sent an Alarm.",
              "Only one alarm should have been sent.");
        alarmReceiver.waitForMessage(AlarmMessage.class);
     }
     
     @Test( groups = {"regressiontest", "pillartest"})
     public void pillarGeneralTestWrongPillarID() throws Exception {
         addDescription("Tests that the ReferencePillar is able to reject a GetFile requests with a wrong pillarID.");
             addStep("Create and send the identify request message.",
                 "Should be received and handled by the pillar.");
         IdentifyPillarsForGetFileRequest identifyRequest = msgFactory.createIdentifyPillarsForGetFileRequest(
                 DEFAULT_AUDITINFORMATION, DEFAULT_FILE_ID, getPillarID(), clientDestinationId);
         messageBus.sendMessage(identifyRequest);
         
         addStep("Retrieve and validate the response getPillarID() the pillar.", 
                 "The pillar should make a response.");
         IdentifyPillarsForGetFileResponse receivedIdentifyResponse = clientReceiver.waitForMessage(
                 IdentifyPillarsForGetFileResponse.class);
         Assert.assertEquals(receivedIdentifyResponse.getResponseInfo().getResponseCode(), 
                 ResponseCode.IDENTIFICATION_POSITIVE);
         
         addStep("Create and send the actual GetFile message to the pillar.", 
                 "Should be received and handled by the pillar.");
         GetFileRequest getRequest = msgFactory.createGetFileRequest(DEFAULT_AUDITINFORMATION,
                 receivedIdentifyResponse.getCorrelationID(), DEFAULT_UPLOAD_FILE_ADDRESS, DEFAULT_FILE_ID, null, getPillarID(),
                 getPillarID(),
                 clientDestinationId, receivedIdentifyResponse.getReplyTo());
         getRequest.setPillarID(getPillarID() + "-ERROR");
         messageBus.sendMessage(getRequest);
         
         // TODO fix this!
 //        addStep("Validate that the pillar has sent an Alarm.", 
 //                "Only one alarm should have been sent.");
 //        Assert.assertEquals(alarmDispatcher.getCallsForSendAlarm(), 1);
     }
     
     
     @Test( groups = {"failing", "pillartest"})
     public void pillarGeneralTestBadDeliveryURL() throws Exception {
         addDescription("Tests that the ReferencePillar can handle a bad delivery URL.");
 
         addStep("Send a GetFileRequest with a faulty upload url .",
         "Should generate a FILE_TRANSFER_FAILURE response.");
         GetFileRequest getRequest = msgFactory.createGetFileRequest(DEFAULT_AUDITINFORMATION,
                 msgFactory.getNewCorrelationID(), "Invalidurl", DEFAULT_FILE_ID, null,
                 getPillarID(),
                 getPillarID(),
                 clientDestinationId, pillarDestinationId);
         messageBus.sendMessage(getRequest);
         GetFileFinalResponse finalResponse = clientReceiver.waitForMessage(GetFileFinalResponse.class);
         Assert.assertEquals(finalResponse.getResponseInfo().getResponseCode(), ResponseCode.FILE_TRANSFER_FAILURE);
         alarmReceiver.checkNoMessageIsReceived(AlarmMessage.class);
     }
 }
