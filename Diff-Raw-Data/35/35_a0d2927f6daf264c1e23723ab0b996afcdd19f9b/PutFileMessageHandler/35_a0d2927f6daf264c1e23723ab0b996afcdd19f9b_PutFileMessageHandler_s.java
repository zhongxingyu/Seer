 /*
  * #%L
  * bitrepository-access-client
  * *
  * $Id: AccessMessageHandler.java 249 2011-08-02 11:05:51Z mss $
  * $HeadURL: https://sbforge.org/svn/bitrepository/trunk/bitrepository-integration/src/main/java/org/bitrepository/pillar/messagehandler/AccessMessageHandler.java $
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
 package org.bitrepository.pillar.messagehandler;
 
 import java.io.File;
 import java.math.BigInteger;
 import java.net.URL;
 import java.util.Collection;
 import java.util.Date;
 
 import org.bitrepository.bitrepositoryelements.ChecksumDataForFileTYPE;
 import org.bitrepository.bitrepositoryelements.ChecksumSpecTYPE;
 import org.bitrepository.bitrepositoryelements.ChecksumsDataForNewFile;
 import org.bitrepository.bitrepositoryelements.ChecksumsDataForNewFile.ChecksumDataItems;
 import org.bitrepository.bitrepositoryelements.FinalResponseInfo;
 import org.bitrepository.bitrepositoryelements.ProgressResponseInfo;
 import org.bitrepository.bitrepositorymessages.PutFileFinalResponse;
 import org.bitrepository.bitrepositorymessages.PutFileProgressResponse;
 import org.bitrepository.bitrepositorymessages.PutFileRequest;
 import org.bitrepository.common.utils.CalendarUtils;
 import org.bitrepository.common.utils.ChecksumUtils;
 import org.bitrepository.protocol.FileExchange;
 import org.bitrepository.protocol.ProtocolComponentFactory;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * Class for performing the PutFile operation.
  * TODO handle error scenarios.
  */
 public class PutFileMessageHandler extends PillarMessageHandler<PutFileRequest> {
     /** The log.*/
     private Logger log = LoggerFactory.getLogger(getClass());
 
     /**
      * Constructor.
      * @param mediator The mediator for this pillar.
      */
     public PutFileMessageHandler(PillarMediator mediator) {
         super(mediator);
     }
 
     /**
      * Handles the identification messages for the PutFile operation.
      * TODO perhaps synchronisation for all these methods?
      * @param message The IdentifyPillarsForPutFileRequest message to handle.
      */
     public void handleMessage(PutFileRequest message) {
         try {
             if(!validateMessage(message)) {
                 return;
             }
             
             tellAboutProgress(message);
             retrieveFile(message);
             sendFinalResponse(message);
         } catch (IllegalArgumentException e) {
             log.warn("Caught IllegalArgumentException. Possible intruder -> Sending alarm! ", e);
             alarmIllegalArgument(e);
         } catch (RuntimeException e) {
             log.warn("Internal RunTimeException caught. Sending response for 'error at my end'.", e);
             FinalResponseInfo fri = new FinalResponseInfo();
             fri.setFinalResponseCode("500");
             fri.setFinalResponseText("Error: " + e.getMessage());
             sendFailedResponse(message, fri);
         }
     }
     
     /**
      * Validates the message.
      * @param message The message to validate.
      */
     private boolean validateMessage(PutFileRequest message) {
         // validate message
         validateBitrepositoryCollectionId(message.getBitRepositoryCollectionID());
         validatePillarId(message.getPillarID());
 
         // verify, that we already have the file
         if(mediator.archive.hasFile(message.getFileID())) {
             log.warn("Cannot perform put for a file, '" + message.getFileID() 
                     + "', which we already have within the archive");
             // Then tell the mediator, that we failed.
             FinalResponseInfo fri = new FinalResponseInfo();
             fri.setFinalResponseCode("409");
             fri.setFinalResponseText("File is already within archive.");
             sendFailedResponse(message, fri);
 
             return false;
         }
         return true;
     }
     
     /**
      * Method for sending a progress response.
      * @param message The message to base the response upon.
      */
     private void tellAboutProgress(PutFileRequest message) {
         log.info("Respond that we are starting to retrieve the file.");
         PutFileProgressResponse pResponse = mediator.msgFactory.createPutFileProgressResponse(message);
 
         // Needs to fill in: AuditTrailInformation, PillarChecksumSpec, ProgressResponseInfo
         pResponse.setAuditTrailInformation(null);
         pResponse.setPillarChecksumSpec(null);
         ProgressResponseInfo prInfo = new ProgressResponseInfo();
        prInfo.setProgressResponseCode("202"); // HTTP for accepted.
         prInfo.setProgressResponseText("Started to receive date.");  
         pResponse.setProgressResponseInfo(prInfo);
 
         log.info("Sending ProgressResponseInfo: " + prInfo);
         mediator.messagebus.sendMessage(pResponse);
     }
     
     /**
      * Retrieves the actual data, validates it and stores it.
      * @param message The request to for the file to put.
      */
     private void retrieveFile(PutFileRequest message) {
         log.debug("Retrieving the data to be stored from URL: '" + message.getFileAddress() + "'");
         FileExchange fe = ProtocolComponentFactory.getInstance().getFileExchange();
 
         File fileForValidation;
         try {
             fileForValidation = mediator.archive.downloadFileForValidation(message.getFileID(), 
                     fe.downloadFromServer(new URL(message.getFileAddress())));
         } catch (Exception e) {
             throw new RuntimeException(e);
         }
         
         if(message.getChecksumsDataForNewFile() != null 
                 && message.getChecksumsDataForNewFile().getChecksumDataItems() != null
                 && message.getChecksumsDataForNewFile().getChecksumDataItems().getChecksumDataForFile() != null) {
             Collection<ChecksumDataForFileTYPE> validationChecksums 
                     = message.getChecksumsDataForNewFile().getChecksumDataItems().getChecksumDataForFile();
             try {
                 for(ChecksumDataForFileTYPE csType : validationChecksums) {
                     String checksum = ChecksumUtils.generateChecksum(fileForValidation, 
                             csType.getChecksumSpec().getChecksumType(), 
                             csType.getChecksumSpec().getChecksumSalt());
                     if(!checksum.equals(csType.getChecksumValue())) {
                         log.error("Expected checksums '" + csType.getChecksumValue() + "' but the checksum was '" 
                                 + checksum + "'.");
                         throw new IllegalStateException("Wrong checksum! Expected: [" + csType.getChecksumValue() 
                                 + "], but calculated: [" + checksum + "]");
                     }
                 }
             } catch (Exception e) {
                 log.error("The retrieved file did not validate! Removing it from archive.", e);
                 throw new RuntimeException(e);
             }
         } else {
             // TODO is such a checksum required?
             log.warn("No checksums for validating the retrieved file.");
         }
 
         try {
             // TODO verify that this operation is successful.
             mediator.archive.moveToArchive(message.getFileID());
         } catch (Exception e) {
             throw new RuntimeException(e);
         }
     }
     
     /**
      * Method for sending the final response for the requested put operation.
      * @param message The message requesting the put operation.
      */
     private void sendFinalResponse(PutFileRequest message) {
 
         File retrievedFile = mediator.archive.getFile(message.getFileID());
 
         PutFileFinalResponse fResponse = mediator.msgFactory.createPutFileFinalResponse(message);
 
         // insert: AuditTrailInformation, ChecksumsDataForNewFile, FinalResponseInfo, PillarChecksumSpec
         fResponse.setAuditTrailInformation(null);
         FinalResponseInfo frInfo = new FinalResponseInfo();
         frInfo.setFinalResponseCode("200"); // HTTP for OK!
         frInfo.setFinalResponseText("The put has be finished.");
         fResponse.setFinalResponseInfo(frInfo);
         fResponse.setPillarChecksumSpec(null); // NOT A CHECKSUM PILLAR
 
         ChecksumsDataForNewFile checksumForValidation = new ChecksumsDataForNewFile();
         if(message.getChecksumSpecs() != null && message.getChecksumSpecs().getNoOfItems().equals(BigInteger.ZERO)) {
             // Calculate the requested checksum data.
             checksumForValidation.setFileID(message.getFileID());
             checksumForValidation.setNoOfItems(message.getChecksumSpecs().getNoOfItems());
             Collection<ChecksumSpecTYPE> requestedChecksumToCalculate 
                     = message.getChecksumSpecs().getChecksumSpecsItems().getChecksumSpecsItem();
             ChecksumDataItems cdi = new ChecksumDataItems();
             // Calculate each of the requested checksums and put them into the response message.
             for(ChecksumSpecTYPE checksumToCalculate : requestedChecksumToCalculate) {
                 String checksum = ChecksumUtils.generateChecksum(retrievedFile, 
                         checksumToCalculate.getChecksumType(),
                         checksumToCalculate.getChecksumSalt());
                 ChecksumDataForFileTYPE resultingChecksum = new ChecksumDataForFileTYPE();
                 resultingChecksum.setChecksumSpec(checksumToCalculate);
                 resultingChecksum.setChecksumValue(checksum);
                 resultingChecksum.setCalculationTimestamp(CalendarUtils.getXmlGregorianCalendar(new Date()));
                 cdi.getChecksumDataForFile().add(resultingChecksum);
             }
             checksumForValidation.setChecksumDataItems(cdi);
         } else {
             // TODO is such a request required?
             log.info("No checksum validation requested.");
         }
         fResponse.setChecksumsDataForNewFile(checksumForValidation);
 
         // Finish by sending final response.
         log.info("Sending PutFileFinalResponse: " + fResponse);
         mediator.messagebus.sendMessage(fResponse);
     }
     
     /**
      * Method for sending a response telling, that the operation has failed.
      * @param message The message requesting the put operation.
      * @param frInfo The information about why it has failed.
      */
     private void sendFailedResponse(PutFileRequest message, FinalResponseInfo frInfo) {
         // send final response telling, that the file already exists!
         PutFileFinalResponse fResponse = mediator.msgFactory.createPutFileFinalResponse(message);
         fResponse.setFinalResponseInfo(frInfo);
         mediator.messagebus.sendMessage(fResponse);
     }
 }
