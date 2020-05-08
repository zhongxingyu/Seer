 /*
  * #%L
  * Bitmagasin integrationstest
  * *
  * $Id$
  * $HeadURL$
  * %%
  * Copyright (C) 2010 The State and University Library, The Royal Library and The State Archives, Denmark
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
 package org.bitrepository.access.getfileids;
 
 import java.net.URL;
 import javax.xml.bind.JAXBException;
 import org.bitrepository.access.AccessComponentFactory;
 import org.bitrepository.access.getfileids.conversation.FileIDsCompletePillarEvent;
 import org.bitrepository.bitrepositoryelements.FileIDs;
 import org.bitrepository.bitrepositoryelements.FileIDsData;
 import org.bitrepository.bitrepositoryelements.FileIDsData.FileIDsDataItems;
 import org.bitrepository.bitrepositoryelements.FileIDsDataItem;
 import org.bitrepository.bitrepositoryelements.ResponseCode;
 import org.bitrepository.bitrepositoryelements.ResponseInfo;
 import org.bitrepository.bitrepositoryelements.ResultingFileIDs;
 import org.bitrepository.bitrepositorymessages.GetFileIDsFinalResponse;
 import org.bitrepository.bitrepositorymessages.GetFileIDsProgressResponse;
 import org.bitrepository.bitrepositorymessages.GetFileIDsRequest;
 import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetFileIDsRequest;
 import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetFileIDsResponse;
 import org.bitrepository.bitrepositorymessages.MessageRequest;
 import org.bitrepository.bitrepositorymessages.MessageResponse;
 import org.bitrepository.client.DefaultClientTest;
 import org.bitrepository.client.TestEventHandler;
 import org.bitrepository.client.eventhandler.OperationEvent.OperationEventType;
 import org.bitrepository.common.utils.CalendarUtils;
 import org.bitrepository.common.utils.FileIDsUtils;
 import org.bitrepository.protocol.bus.MessageReceiver;
 import org.testng.Assert;
 import org.testng.annotations.BeforeMethod;
 import org.testng.annotations.Test;
 
 /**
  * Test class for the 'GetFileIDsClient'.
  */
 public class GetFileIDsClientComponentTest extends DefaultClientTest {
 
     private TestGetFileIDsMessageFactory testMessageFactory;
 
     /**
      * Set up the test scenario before running the tests in this class.
      * @throws javax.xml.bind.JAXBException
      */
     @BeforeMethod(alwaysRun = true)
     public void setUp() throws JAXBException {
         // TODO getFileIDsFromFastestPillar settings
         testMessageFactory = new TestGetFileIDsMessageFactory(settingsForCUT.getCollectionID());
     }
 
     @Test(groups = {"regressiontest"})
     public void verifyGetFileIDsClientFromFactory() throws Exception {
         Assert.assertTrue(AccessComponentFactory.getInstance().createGetFileIDsClient(settingsForCUT, securityManager,
                 settingsForTestClient.getComponentID()) instanceof ConversationBasedGetFileIDsClient,
                 "The default GetFileClient from the Access factory should be of the type '" +
                         ConversationBasedGetFileIDsClient.class.getName() + "'.");
     }
 
     @Test(groups = {"regressiontest"})
     public void getFileIDsDeliveredAtUrl() throws Exception {
         addDescription("Tests the delivery of fileIDs from a pillar at a given URL.");
         addStep("Initialise the variables for this test.",
                 "EventManager and GetFileIDsClient should be instantiated.");
 
         String deliveryFilename = "TEST-FILE-IDS-DELIVERY.xml";
         FileIDs fileIDs = new FileIDs();
         fileIDs.setFileID(DEFAULT_FILE_ID);
         settingsForCUT.getCollectionSettings().getClientSettings().getPillarIDs().clear();
         settingsForCUT.getCollectionSettings().getClientSettings().getPillarIDs().add(PILLAR1_ID);
 
         GetFileIDsClient getFileIDsClient = createGetFileIDsClient();
         URL deliveryUrl = httpServer.getURL(deliveryFilename);
 
         addStep("Request the delivery of the file ids of a file from the pillar(s). A callback listener should be supplied.",
                 "A IdentifyPillarsForGetFileIDsRequest will be sent to the pillar(s).");
         getFileIDsClient.getFileIDs(settingsForCUT.getCollectionSettings().getClientSettings().getPillarIDs(), fileIDs,
                 deliveryUrl, testEventHandler);
 
         IdentifyPillarsForGetFileIDsRequest receivedIdentifyRequestMessage  = collectionReceiver.waitForMessage(
                 IdentifyPillarsForGetFileIDsRequest.class);
         Assert.assertEquals(receivedIdentifyRequestMessage,
                 testMessageFactory.createIdentifyPillarsForGetFileIDsRequest(receivedIdentifyRequestMessage,
                         collectionDestinationID));
         Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.IDENTIFY_REQUEST_SENT);
 
         addStep("The pillar sends a response to the identify message.",
                 "The callback listener should notify of the response and the client should send a GetFileIDsRequest "
                         + "message to the pillar");
         IdentifyPillarsForGetFileIDsResponse identifyResponse = testMessageFactory.createIdentifyPillarsForGetFileIDsResponse(
                 receivedIdentifyRequestMessage, PILLAR1_ID, pillar1DestinationId);
         messageBus.sendMessage(identifyResponse);
         GetFileIDsRequest receivedGetFileIDsRequest = pillar1Destination.waitForMessage(GetFileIDsRequest.class);
         Assert.assertEquals(receivedGetFileIDsRequest,
                 testMessageFactory.createGetFileIDsRequest(receivedGetFileIDsRequest, PILLAR1_ID, pillar1DestinationId));
         Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.COMPONENT_IDENTIFIED);
 
         Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.IDENTIFICATION_COMPLETE);
         Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.REQUEST_SENT);
 
         addStep("The pillar sends a getFileIDsProgressResponse to the GetFileIDsClient.",
                 "The GetFileIDsClient should notify about the response through the callback interface.");
         GetFileIDsProgressResponse getFileIDsProgressResponse = testMessageFactory.createGetFileIDsProgressResponse(
                 receivedGetFileIDsRequest, PILLAR1_ID, pillar1DestinationId);
         messageBus.sendMessage(getFileIDsProgressResponse);
         Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.PROGRESS);
 
         addStep("The resulting file is uploaded to the indicated url and the pillar sends a final response upload message",
                 "The GetFileIDsClient notifies that the file is ready through the callback listener and the uploaded file is present.");
         GetFileIDsFinalResponse completeMsg = testMessageFactory.createGetFileIDsFinalResponse(
                 receivedGetFileIDsRequest, PILLAR1_ID, pillar1DestinationId);
 
         ResultingFileIDs res = new ResultingFileIDs();
         res.setResultAddress(receivedGetFileIDsRequest.getResultAddress());
         completeMsg.setResultingFileIDs(res);
 
         messageBus.sendMessage(completeMsg);
 
         addStep("Receive and validate event results for the pillar.",
                 "Should be a FileIDsCompletePillarEvent with the ResultingFileIDs containing only the URL.");
         FileIDsCompletePillarEvent event = (FileIDsCompletePillarEvent) testEventHandler.waitForEvent();
         Assert.assertEquals(event.getEventType(), OperationEventType.COMPONENT_COMPLETE);
         ResultingFileIDs resFileIDs = event.getFileIDs();
         Assert.assertNotNull(resFileIDs, "The ResultingFileIDs may not be null.");
         Assert.assertTrue(resFileIDs.getResultAddress().contains(deliveryUrl.toExternalForm()),
                 "The resulting address'" + resFileIDs.getResultAddress() + "' should contain the argument address: '"
                         + deliveryUrl.toExternalForm() + "'");
         Assert.assertNull(resFileIDs.getFileIDsData(), "No FileIDsData should be returned.");
 
         Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.COMPLETE);
     }
 
     @Test(groups = {"regressiontest"})
     public void getFileIDsDeliveredThroughMessage() throws Exception {
         addDescription("Tests the delivery of fileIDs from a pillar at a given URL.");
         addStep("Initialise the variables for this test.",
                 "EventManager and GetFileIDsClient should be instantiated.");
 
         String deliveryFilename = "TEST-FILE-IDS-DELIVERY.xml";
         FileIDs fileIDs = new FileIDs();
         fileIDs.setFileID(DEFAULT_FILE_ID);
 
         settingsForCUT.getCollectionSettings().getClientSettings().getPillarIDs().clear();
         settingsForCUT.getCollectionSettings().getClientSettings().getPillarIDs().add(PILLAR1_ID);
 
         GetFileIDsClient getFileIDsClient = createGetFileIDsClient();
 
         addStep("Ensure the delivery file isn't already present on the http server",
                 "Should be remove if it already exists.");
 
         addStep("Request the delivery of the file ids of a file from the pillar(s). A callback listener should be supplied.",
                 "A IdentifyPillarsForGetFileIDsRequest will be sent to the pillar(s).");
         getFileIDsClient.getFileIDs(settingsForCUT.getCollectionSettings().getClientSettings().getPillarIDs(), fileIDs,
                 null, testEventHandler);
 
         IdentifyPillarsForGetFileIDsRequest receivedIdentifyRequestMessage = collectionReceiver.waitForMessage(
                 IdentifyPillarsForGetFileIDsRequest.class);
         Assert.assertEquals(receivedIdentifyRequestMessage,
                 testMessageFactory.createIdentifyPillarsForGetFileIDsRequest(receivedIdentifyRequestMessage,
                         collectionDestinationID));
         Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.IDENTIFY_REQUEST_SENT);
 
         addStep("The pillar sends a response to the identify message.",
                 "The callback listener should notify of the response and the client should send a GetFileIDsRequest "
                         + "message to the pillar");
 
         IdentifyPillarsForGetFileIDsResponse identifyResponse = testMessageFactory.createIdentifyPillarsForGetFileIDsResponse(
                 receivedIdentifyRequestMessage, PILLAR1_ID, pillar1DestinationId);
         messageBus.sendMessage(identifyResponse);
         GetFileIDsRequest receivedGetFileIDsRequest = pillar1Destination.waitForMessage(GetFileIDsRequest.class);
         Assert.assertEquals(receivedGetFileIDsRequest,
                 testMessageFactory.createGetFileIDsRequest(receivedGetFileIDsRequest, PILLAR1_ID, pillar1DestinationId));
 
         for(int i = 0; i < settingsForCUT.getCollectionSettings().getClientSettings().getPillarIDs().size(); i++) {
             Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.COMPONENT_IDENTIFIED);
         }
         Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.IDENTIFICATION_COMPLETE);
         Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.REQUEST_SENT);
 
         addStep("The pillar sends a getFileIDsProgressResponse to the GetFileIDsClient.",
                 "The GetFileIDsClient should notify about the response through the callback interface.");
         GetFileIDsProgressResponse getFileIDsProgressResponse = testMessageFactory.createGetFileIDsProgressResponse(
                 receivedGetFileIDsRequest, PILLAR1_ID, pillar1DestinationId);
         messageBus.sendMessage(getFileIDsProgressResponse);
 
         Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.PROGRESS);
 
         addStep("The resulting file is uploaded to the indicated url and the pillar sends a final response upload message",
                 "The GetFileIDsClient notifies that the file is ready through the callback listener and the uploaded file is present.");
         GetFileIDsFinalResponse completeMsg = testMessageFactory.createGetFileIDsFinalResponse(
                 receivedGetFileIDsRequest, PILLAR1_ID, pillar1DestinationId);
 
         ResultingFileIDs res = new ResultingFileIDs();
         FileIDsData fileIDsData = new FileIDsData();
         FileIDsDataItems fiddItems = new FileIDsDataItems();
         String fileID = receivedGetFileIDsRequest.getFileIDs().getFileID();
         FileIDsDataItem fidItem = new FileIDsDataItem();
         fidItem.setLastModificationTime(CalendarUtils.getNow());
         fidItem.setFileID(fileID);
         fiddItems.getFileIDsDataItem().add(fidItem);
 
         fileIDsData.setFileIDsDataItems(fiddItems);
         res.setFileIDsData(fileIDsData);
         completeMsg.setResultingFileIDs(res);
 
         messageBus.sendMessage(completeMsg);
 
         addStep("Receive and validate event results for the pillar.",
                 "Should be a FileIDsCompletePillarEvent with the ResultingFileIDs containing the list of fileids.");
         for(int i = 0; i < settingsForCUT.getCollectionSettings().getClientSettings().getPillarIDs().size(); i++) {
             FileIDsCompletePillarEvent event = (FileIDsCompletePillarEvent) testEventHandler.waitForEvent();
             Assert.assertEquals(event.getEventType(), OperationEventType.COMPONENT_COMPLETE);
             ResultingFileIDs resFileIDs = event.getFileIDs();
             Assert.assertNotNull(resFileIDs, "The ResultingFileIDs may not be null.");
             Assert.assertNull(resFileIDs.getResultAddress(), "The results should be sent back through the message, "
                     + "and therefore no resulting address should be returned.");
             Assert.assertNotNull(resFileIDs.getFileIDsData(), "No FileIDsData should be returned.");
             Assert.assertEquals(resFileIDs.getFileIDsData().getFileIDsDataItems().getFileIDsDataItem().size(),
                     1, "Response should contain same amount of fileids as requested.");
         }
 
         Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.COMPLETE);
     }
 
     @Test(groups = {"regressiontest"})
     public void testNoSuchFile() throws Exception {
         addDescription("Testing how a request for a non-existing file is handled.");
         addStep("Setting up variables and such.", "Should be OK.");
 
         String deliveryFilename = "TEST-FILE-IDS-DELIVERY.xml";
         FileIDs fileIDs = new FileIDs();
         fileIDs.setFileID(DEFAULT_FILE_ID);
 
         settingsForCUT.getCollectionSettings().getClientSettings().getPillarIDs().clear();
         settingsForCUT.getCollectionSettings().getClientSettings().getPillarIDs().add(PILLAR1_ID);
 
         GetFileIDsClient GetFileIDsClient = createGetFileIDsClient();
         URL deliveryUrl = httpServer.getURL(deliveryFilename);
 
         addStep("Request the delivery of the file id of a file from the pillar(s). A callback listener should be supplied.",
                 "A IdentifyPillarsForGetFileIDsRequest will be sent to the pillar(s).");
         GetFileIDsClient.getFileIDs(settingsForCUT.getCollectionSettings().getClientSettings().getPillarIDs(), fileIDs,
                 deliveryUrl, testEventHandler);
 
         IdentifyPillarsForGetFileIDsRequest receivedIdentifyRequestMessage = collectionReceiver.waitForMessage(
                 IdentifyPillarsForGetFileIDsRequest.class);
         Assert.assertEquals(receivedIdentifyRequestMessage,
                 testMessageFactory.createIdentifyPillarsForGetFileIDsRequest(receivedIdentifyRequestMessage,
                         collectionDestinationID));
         Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.IDENTIFY_REQUEST_SENT);
 
         addStep("The pillar sends a response to the identify message.",
                 "The callback listener should notify of the response and the client should send a GetFileIDsRequest "
                         + "message to the pillar");
 
         GetFileIDsRequest receivedGetFileIDsRequest = null;
         IdentifyPillarsForGetFileIDsResponse identifyResponse = testMessageFactory.createIdentifyPillarsForGetFileIDsResponse(
                 receivedIdentifyRequestMessage, PILLAR1_ID, pillar1DestinationId);
         messageBus.sendMessage(identifyResponse);
         receivedGetFileIDsRequest = pillar1Destination.waitForMessage(GetFileIDsRequest.class);
         Assert.assertEquals(receivedGetFileIDsRequest,
                 testMessageFactory.createGetFileIDsRequest(receivedGetFileIDsRequest,PILLAR1_ID, pillar1DestinationId));
 
         for(int i = 0; i < settingsForCUT.getCollectionSettings().getClientSettings().getPillarIDs().size(); i++) {
             Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.COMPONENT_IDENTIFIED);
         }
         Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.IDENTIFICATION_COMPLETE);
         Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.REQUEST_SENT);
 
         addStep("Send a error that the file cannot be found.", "Should trigger a 'event failed'.");
         GetFileIDsFinalResponse completeMsg = testMessageFactory.createGetFileIDsFinalResponse(
                 receivedGetFileIDsRequest, PILLAR1_ID, pillar1DestinationId);
 
         ResponseInfo rfInfo = new ResponseInfo();
         rfInfo.setResponseCode(ResponseCode.FILE_NOT_FOUND_FAILURE);
         rfInfo.setResponseText("No such file.");
         completeMsg.setResponseInfo(rfInfo);
         completeMsg.setResultingFileIDs(null);
 
         messageBus.sendMessage(completeMsg);
 
         Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.COMPONENT_FAILED);
         Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.FAILED);
     }
 
     /**
      * Creates a new test GetFileIDsClient based on the supplied settingsForCUT.
      *
      * Note that the normal way of creating client through the module factory would reuse components with settings from
      * previous tests.
      * @return A new GetFileIDsClient(Wrapper).
      */
     private GetFileIDsClient createGetFileIDsClient() {
         return new GetFileIDsClientTestWrapper(new ConversationBasedGetFileIDsClient(
                 messageBus, conversationMediator, settingsForCUT, settingsForTestClient.getComponentID()), testEventManager);
     }
 
     @Override
     protected MessageResponse createIdentifyResponse(MessageRequest identifyRequest, String from, String to, ResponseCode responseCode) {
         MessageResponse response = testMessageFactory.createIdentifyPillarsForGetFileIDsResponse(
                 (IdentifyPillarsForGetFileIDsRequest)identifyRequest, from, to);
         response.setResponseInfo(new ResponseInfo());
         response.getResponseInfo().setResponseCode(responseCode);
        response.getResponseInfo().setResponseText("NA");
         return response;
     }
 
     @Override
     protected MessageResponse createFinalResponse(MessageRequest request, String from, String to, ResponseCode responseCode) {
         MessageResponse response =  testMessageFactory.createGetFileIDsFinalResponse(
                 (GetFileIDsRequest)request, from, to);
         response.getResponseInfo().setResponseCode(responseCode);
         return response;
     }
 
     @Override
     protected MessageRequest waitForIdentifyRequest() {
         return collectionReceiver.waitForMessage(IdentifyPillarsForGetFileIDsRequest.class);
     }
 
     @Override
     protected MessageRequest waitForRequest(MessageReceiver receiver) {
         return receiver.waitForMessage(GetFileIDsRequest.class);
     }
 
     @Override
     protected void checkNoRequestIsReceived(MessageReceiver receiver) {
         receiver.checkNoMessageIsReceived(GetFileIDsRequest.class);
     }
 
     @Override
     protected void startOperation(TestEventHandler testEventHandler) {
         GetFileIDsClient getFileIDsClient = createGetFileIDsClient();
         getFileIDsClient.getFileIDs(null, FileIDsUtils.getAllFileIDs(), null, testEventHandler);
     }
 }
