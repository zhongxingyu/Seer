 /*
  * #%L
  * bitrepository-access-client
  * *
  * $Id: PutFileRequestHandler.java 687 2012-01-09 12:56:47Z ktc $
  * $HeadURL: https://sbforge.org/svn/bitrepository/bitrepository-reference/trunk/bitrepository-reference-pillar/src/main/java/org/bitrepository/pillar/messagehandler/PutFileRequestHandler.java $
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
 package org.bitrepository.pillar.referencepillar.messagehandler;
 
 import java.io.File;
 import java.io.IOException;
 import java.math.BigInteger;
 import java.net.URL;
 
 import org.bitrepository.bitrepositoryelements.ChecksumDataForFileTYPE;
 import org.bitrepository.bitrepositoryelements.FileAction;
 import org.bitrepository.bitrepositoryelements.ResponseCode;
 import org.bitrepository.bitrepositoryelements.ResponseInfo;
 import org.bitrepository.bitrepositorymessages.MessageResponse;
 import org.bitrepository.bitrepositorymessages.PutFileFinalResponse;
 import org.bitrepository.bitrepositorymessages.PutFileProgressResponse;
 import org.bitrepository.bitrepositorymessages.PutFileRequest;
 import org.bitrepository.common.utils.Base16Utils;
 import org.bitrepository.common.utils.CalendarUtils;
 import org.bitrepository.common.utils.ChecksumUtils;
 import org.bitrepository.pillar.common.PillarContext;
 import org.bitrepository.pillar.referencepillar.archive.ReferenceArchive;
 import org.bitrepository.protocol.FileExchange;
 import org.bitrepository.protocol.ProtocolComponentFactory;
 import org.bitrepository.service.exception.IllegalOperationException;
 import org.bitrepository.service.exception.InvalidMessageException;
 import org.bitrepository.service.exception.RequestHandlerException;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * Class for performing the PutFile operation.
  * TODO handle error scenarios.
  */
 public class PutFileRequestHandler extends ReferencePillarMessageHandler<PutFileRequest> {
     /** The log.*/
     private Logger log = LoggerFactory.getLogger(getClass());
     
     /**
      * Constructor.
      * @param context The context of the message handler.
      * @param referenceArchive The archive for the data.
      */
     public PutFileRequestHandler(PillarContext context, ReferenceArchive referenceArchive) {
         super(context, referenceArchive);
     }
     
     @Override
     public Class<PutFileRequest> getRequestClass() {
         return PutFileRequest.class;
     }
 
     @Override
     public void processRequest(PutFileRequest message) throws RequestHandlerException {
         validateMessage(message);
         tellAboutProgress(message);
         retrieveFile(message);
         sendFinalResponse(message);
     }
 
     @Override
     public MessageResponse generateFailedResponse(PutFileRequest message) {
         return createFinalResponse(message);
     }
     
     /**
      * Validates the message.
      * @param message The message to validate.
      */
     private void validateMessage(PutFileRequest message) throws RequestHandlerException {
         validatePillarId(message.getPillarID());
         if(message.getChecksumDataForNewFile() != null) {
             validateChecksumSpecification(message.getChecksumDataForNewFile().getChecksumSpec());
         } else if(getSettings().getCollectionSettings().getProtocolSettings().isRequireChecksumForNewFileRequests()) {
             ResponseInfo responseInfo = new ResponseInfo();
             responseInfo.setResponseCode(ResponseCode.NEW_FILE_CHECKSUM_FAILURE);
             responseInfo.setResponseText("According to the contract a checksum for creating a new file is required.");
             throw new IllegalOperationException(responseInfo);
         }
         
         validateChecksumSpecification(message.getChecksumRequestForNewFile());
         validateFileID(message.getFileID());
         
         checkThatTheFileDoesNotAlreadyExist(message);
         checkSpaceForStoringNewFile(message);
     }
     
     /**
      * Validates that the file is not already within the archive. 
      * Otherwise an {@link InvalidMessageException} with the appropriate errorcode is thrown.
      * @param message The request with the filename to validate.
      */
     private void checkThatTheFileDoesNotAlreadyExist(PutFileRequest message) throws RequestHandlerException {
         if(getArchive().hasFile(message.getFileID())) {
             ResponseInfo irInfo = new ResponseInfo();
             irInfo.setResponseCode(ResponseCode.DUPLICATE_FILE_FAILURE);
             irInfo.setResponseText("The file '" + message.getFileID() 
                     + "' already exists within the archive.");
             
             throw new InvalidMessageException(irInfo);
         }
     }
     
     /**
      * Validates that enough space exists is left in the archive.
      * Otherwise an {@link InvalidMessageException} with the appropriate errorcode is thrown.
      * @param message The request with the size of the file.
      */
     private void checkSpaceForStoringNewFile(PutFileRequest message) throws RequestHandlerException {
         BigInteger fileSize = message.getFileSize();
         
         long useableSizeLeft = getArchive().sizeLeftInArchive() 
                 - getSettings().getReferenceSettings().getPillarSettings().getMinimumSizeLeft();
         if(useableSizeLeft < fileSize.longValue()) {
             ResponseInfo irInfo = new ResponseInfo();
             irInfo.setResponseCode(ResponseCode.FAILURE);
             irInfo.setResponseText("Not enough space left in this pillar. Requires '" 
                     + fileSize.longValue() + "' but has only '" + useableSizeLeft + "'");
             
             throw new InvalidMessageException(irInfo);
         }
     }
     
     /**
      * Method for sending a progress response.
      * @param message The message to base the response upon.
      */
     private void tellAboutProgress(PutFileRequest message) {
         log.info("Respond that we are starting to retrieve the file.");
         PutFileProgressResponse pResponse = createPutFileProgressResponse(message);
         
         // Needs to fill in: AuditTrailInformation, PillarChecksumSpec, ProgressResponseInfo
         pResponse.setPillarChecksumSpec(null);
         ResponseInfo prInfo = new ResponseInfo();
         prInfo.setResponseCode(ResponseCode.OPERATION_ACCEPTED_PROGRESS);
        prInfo.setResponseText("Started to receive date.");  
         pResponse.setResponseInfo(prInfo);
         
         log.info("Sending ProgressResponseInfo: " + prInfo);
         getMessageBus().sendMessage(pResponse);
     }
     
     /**
      * Retrieves the actual data, validates it and stores it.
      * @param message The request to for the file to put.
      * @throws RequestHandlerException If the retrival of the file fails.
      */
     @SuppressWarnings("deprecation")
     private void retrieveFile(PutFileRequest message) throws RequestHandlerException {
         log.debug("Retrieving the data to be stored from URL: '" + message.getFileAddress() + "'");
         FileExchange fe = ProtocolComponentFactory.getInstance().getFileExchange();
         
         File fileForValidation;
         try {
             fileForValidation = getArchive().downloadFileForValidation(message.getFileID(), 
                     fe.downloadFromServer(new URL(message.getFileAddress())));
         } catch (IOException e) {
             String errMsg = "Could not retrieve the file from '" + message.getFileAddress() + "'";
             log.error(errMsg, e);
             ResponseInfo ri = new ResponseInfo();
             ri.setResponseCode(ResponseCode.FILE_TRANSFER_FAILURE);
             ri.setResponseText(errMsg);
             throw new InvalidMessageException(ri);
         }
         
         if(message.getChecksumDataForNewFile() != null) {
             getAuditManager().addAuditEvent(message.getFileID(), message.getFrom(), "Calculating the validation "
                     + "checksum for the file before putting it into archive.", message.getAuditTrailInformation(), 
                     FileAction.CHECKSUM_CALCULATED);
 
             ChecksumDataForFileTYPE csType = message.getChecksumDataForNewFile();
             String calculatedChecksum = ChecksumUtils.generateChecksum(fileForValidation, csType.getChecksumSpec());
             String expectedChecksum = Base16Utils.decodeBase16(csType.getChecksumValue());
             if(!calculatedChecksum.equals(expectedChecksum)) {
                 ResponseInfo responseInfo = new ResponseInfo();
                 responseInfo.setResponseCode(ResponseCode.NEW_FILE_CHECKSUM_FAILURE);
                 responseInfo.setResponseText("Wrong checksum! Expected: [" + expectedChecksum 
                         + "], but calculated: [" + calculatedChecksum + "]");
                 throw new IllegalOperationException(responseInfo);
             }
         } else {
             // TODO is such a checksum required?
             log.warn("No checksums for validating the retrieved file.");
         }
         
         getAuditManager().addAuditEvent(message.getFileID(), message.getFrom(), "Putting the downloaded file "
                 + "into archive.", message.getAuditTrailInformation(), FileAction.PUT_FILE);
         getArchive().moveToArchive(message.getFileID());
     }
     
     /**
      * Method for sending the final response for the requested put operation.
      * @param message The message requesting the put operation.
      */
     private void sendFinalResponse(PutFileRequest message) {
         
         File retrievedFile = getArchive().getFile(message.getFileID());
         
         PutFileFinalResponse fResponse = createFinalResponse(message);
         
         // insert: AuditTrailInformation, ChecksumsDataForNewFile, FinalResponseInfo, PillarChecksumSpec
         ResponseInfo frInfo = new ResponseInfo();
         frInfo.setResponseCode(ResponseCode.OPERATION_COMPLETED);
         frInfo.setResponseText("The put has be finished.");
         fResponse.setResponseInfo(frInfo);
         fResponse.setPillarChecksumSpec(null); // NOT A CHECKSUM PILLAR
         
         ChecksumDataForFileTYPE checksumForValidation = new ChecksumDataForFileTYPE();
         
         if(message.getChecksumRequestForNewFile() != null) {
             getAuditManager().addAuditEvent(message.getFileID(), message.getFrom(), "Calculating requested checksum.", 
                     message.getAuditTrailInformation(), FileAction.CHECKSUM_CALCULATED);
             checksumForValidation.setChecksumValue(ChecksumUtils.generateChecksum(retrievedFile, 
                     message.getChecksumRequestForNewFile()).getBytes());
             checksumForValidation.setCalculationTimestamp(CalendarUtils.getNow());
             checksumForValidation.setChecksumSpec(message.getChecksumRequestForNewFile());
             log.info("Requested checksum calculated: " + checksumForValidation);
         } else {
             // TODO is such a request required?
             log.info("No checksum validation requested.");
             checksumForValidation = null;
         }
         
         fResponse.setChecksumDataForNewFile(checksumForValidation);
         
         // Finish by sending final response.
         log.info("Sending PutFileFinalResponse: " + fResponse);
         getMessageBus().sendMessage(fResponse);
     }
     
     
     /**
      * Creates a PutFileProgressResponse based on a PutFileRequest. Missing the 
      * following fields:
      * <br/> - AuditTrailInformation
      * <br/> - PillarChecksumSpec
      * <br/> - ProgressResponseInfo
      * 
      * @param msg The PutFileRequest to base the progress response on.
      * @return The PutFileProgressResponse based on the request.
      */
     private PutFileProgressResponse createPutFileProgressResponse(PutFileRequest msg) {
         PutFileProgressResponse res = new PutFileProgressResponse();
         populateResponse(msg, res);
         res.setFileAddress(msg.getFileAddress());
         res.setFileID(msg.getFileID());
         res.setPillarID(getSettings().getReferenceSettings().getPillarSettings().getPillarID());
         
         return res;
     }
     
     /**
      * Creates a PutFileFinalResponse based on a PutFileRequest. Missing the
      * following fields:
      * <br/> - AuditTrailInformation
      * <br/> - ChecksumsDataForNewFile
      * <br/> - FinalResponseInfo
      * <br/> - PillarChecksumSpec
      * 
      * @param msg The PutFileRequest to base the final response message on.
      * @return The PutFileFinalResponse message based on the request.
      */
     private PutFileFinalResponse createFinalResponse(PutFileRequest msg) {
         PutFileFinalResponse res = new PutFileFinalResponse();
         populateResponse(msg, res);
         res.setFileAddress(msg.getFileAddress());
         res.setFileID(msg.getFileID());
         res.setPillarID(getSettings().getReferenceSettings().getPillarSettings().getPillarID());
         
         return res;
     }
 }
