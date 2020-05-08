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
 package org.bitrepository.pillar.checksumpillar;
 
 import org.bitrepository.bitrepositoryelements.ChecksumType;
 import org.bitrepository.bitrepositoryelements.FileIDs;
 import org.bitrepository.bitrepositoryelements.FilePart;
 import org.bitrepository.bitrepositoryelements.ResponseCode;
 import org.bitrepository.bitrepositorymessages.GetFileFinalResponse;
 import org.bitrepository.bitrepositorymessages.GetFileRequest;
 import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetFileRequest;
 import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetFileResponse;
 import org.bitrepository.pillar.messagefactories.GetFileMessageFactory;
 import org.testng.Assert;
 import org.testng.annotations.Test;
 
 /**
  * Tests the PutFile functionality on the ReferencePillar.
  */
 public class GetFileOnChecksumPillarTest extends ChecksumPillarTest {
     private GetFileMessageFactory msgFactory;
 
     @Override
     public void initializeCUT() {
         super.initializeCUT();
         msgFactory = new GetFileMessageFactory(settingsForCUT);
     }
     
     @Test( groups = {"regressiontest", "pillartest"})
     public void checksumPillarGetFileIdentification() throws Exception {
         addDescription("Tests that the ChecksumPillar rejects a GetFile identification.");
         addStep("Setting up the variables for the test.", "Should be instantiated.");
         settingsForCUT.getCollectionSettings().getProtocolSettings().setDefaultChecksumType(ChecksumType.MD5.toString());
         FileIDs fileids = new FileIDs();
         fileids.setFileID(DEFAULT_FILE_ID);
         
         addStep("Create and send the identify request message.", 
                 "Should be received and handled by the checksum pillar.");
         IdentifyPillarsForGetFileRequest identifyRequest = msgFactory.createIdentifyPillarsForGetFileRequest(
                 DEFAULT_AUDITINFORMATION, DEFAULT_FILE_ID, getPillarID(), clientDestinationId);
         messageBus.sendMessage(identifyRequest);
         
         addStep("Retrieve and validate the response from the checksum pillar.", 
                 "The checksum pillar should make a response.");
         IdentifyPillarsForGetFileResponse receivedIdentifyResponse = clientReceiver.waitForMessage(
                 IdentifyPillarsForGetFileResponse.class);
         Assert.assertEquals(receivedIdentifyResponse.getResponseInfo().getResponseCode(), 
                 ResponseCode.REQUEST_NOT_SUPPORTED);
     }
     
     @Test( groups = {"regressiontest", "pillartest"})
     public void checksumPillarGetFileOperation() throws Exception {
         addDescription("Tests that the ChecksumPillar rejects a GetFile operation.");
         addStep("Setting up the variables for the test.", "Should be instantiated.");
         String DELIVERY_ADDRESS = httpServer.getURL("test.txt").toExternalForm();
         FilePart filePart = null;
 
         addStep("Create and send the GetFile request message.", 
                 "Should be received and handled by the pillar.");
         GetFileRequest getRequest = msgFactory.createGetFileRequest(DEFAULT_AUDITINFORMATION, msgFactory.getNewCorrelationID(),
                 DELIVERY_ADDRESS, DEFAULT_FILE_ID, filePart, getPillarID(), getPillarID(), clientDestinationId,
                 pillarDestinationId);
         messageBus.sendMessage(getRequest);
         
         addStep("Retrieve and validate the final response from the checksum pillar.", 
                 "The checksum pillar should reject the operation.");
         GetFileFinalResponse receivedFinalResponse = clientReceiver.waitForMessage(
                 GetFileFinalResponse.class);
         Assert.assertEquals(receivedFinalResponse, 
                 msgFactory.createGetFileFinalResponse(getRequest.getCorrelationID(), 
                         receivedFinalResponse.getFileAddress(), DEFAULT_FILE_ID, filePart, getPillarID(), pillarDestinationId,
                         receivedFinalResponse.getResponseInfo(), clientDestinationId));
         Assert.assertEquals(receivedFinalResponse.getResponseInfo().getResponseCode(), 
                ResponseCode.REQUEST_NOT_SUPPORTED);      
     }
 }
