 /*
  * #%L
  * bitrepository-access-client
  * *
  * $Id$
  * $HeadURL$
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
 package org.bitrepository.pillar.checksumpillar.messagehandler;
 
 import org.bitrepository.bitrepositoryelements.ChecksumDataForFileTYPE;
 import org.bitrepository.bitrepositoryelements.ResponseCode;
 import org.bitrepository.bitrepositoryelements.ResponseInfo;
 import org.bitrepository.bitrepositorymessages.IdentifyPillarsForPutFileRequest;
 import org.bitrepository.bitrepositorymessages.IdentifyPillarsForPutFileResponse;
 import org.bitrepository.bitrepositorymessages.MessageResponse;
 import org.bitrepository.common.utils.Base16Utils;
 import org.bitrepository.common.utils.CalendarUtils;
 import org.bitrepository.common.utils.TimeMeasurementUtils;
 import org.bitrepository.pillar.cache.ChecksumEntry;
 import org.bitrepository.pillar.cache.ChecksumStore;
 import org.bitrepository.pillar.common.PillarContext;
import org.bitrepository.service.exception.IdentifyContributorException;
 import org.bitrepository.service.exception.RequestHandlerException;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * Class for handling the identification of this pillar for the purpose of performing the PutFile operation.
  */
 public class IdentifyPillarsForPutFileRequestHandler extends ChecksumPillarMessageHandler<IdentifyPillarsForPutFileRequest> {
 
     /** The log.*/
     private Logger log = LoggerFactory.getLogger(getClass());
 
     /**
      * Constructor.
      * @param context The context of the message handler.
      * @param refCache The cache for the checksum data.
      */
     public IdentifyPillarsForPutFileRequestHandler(PillarContext context, ChecksumStore refCache) {
         super(context, refCache);
     }
 
     @Override
     public Class<IdentifyPillarsForPutFileRequest> getRequestClass() {
         return IdentifyPillarsForPutFileRequest.class;
     }
 
     @Override
     public void processRequest(IdentifyPillarsForPutFileRequest message) throws RequestHandlerException {
        checkThatTheFileDoesNotAlreadyExist(message);
        respondSuccesfullIdentification(message);
     }
 
     @Override
     public MessageResponse generateFailedResponse(IdentifyPillarsForPutFileRequest message) {
         return createFinalResponse(message);
     }
     
     /**
      * Validates that the file is not already within the archive. 
     * Otherwise an {@link IdentifyContributorException} with the appropriate errorcode is thrown.
      * @param message The request with the filename to validate.
      */
    private void checkThatTheFileDoesNotAlreadyExist(IdentifyPillarsForPutFileRequest message) 
             throws RequestHandlerException {
         if(message.getFileID() == null) {
             log.debug("No fileid given in the identification request.");
            return;
         }
        validateFileID(message.getFileID());
         
        if(getCache().hasFile(message.getFileID())) {
            ResponseInfo irInfo = new ResponseInfo();
            irInfo.setResponseCode(ResponseCode.DUPLICATE_FILE_FAILURE);
            irInfo.setResponseText("The file '" + message.getFileID() 
                    + "' already exists within the archive.");
            
            throw new IdentifyContributorException(irInfo);
        }
     }
     
     /**
      * Method for sending a response for 'DUPLICATE_FILE_FAILURE'.
      * @param message The message to base the response upon.
      */
     protected void respondDuplicateFile(IdentifyPillarsForPutFileRequest message) {
         log.info("Creating DuplicateFile reply for '" + message + "'");
         IdentifyPillarsForPutFileResponse reply = createFinalResponse(message);
 
         // Needs to filled in: AuditTrailInformation, PillarChecksumSpec, ReplyTo, TimeToDeliver
         reply.setReplyTo(getSettings().getReceiverDestinationID());
         reply.setTimeToDeliver(TimeMeasurementUtils.getMaximumTime());
         reply.setPillarChecksumSpec(getChecksumType()); 
         
         ChecksumEntry entry = getCache().getEntry(message.getFileID());
         ChecksumDataForFileTYPE checksumData = new ChecksumDataForFileTYPE();
         checksumData.setCalculationTimestamp(CalendarUtils.getXmlGregorianCalendar(entry.getCalculationDate()));
         checksumData.setChecksumSpec(getChecksumType());
         checksumData.setChecksumValue(Base16Utils.encodeBase16(entry.getChecksum()));
         reply.setChecksumDataForExistingFile(checksumData);
         
         ResponseInfo irInfo = new ResponseInfo();
         irInfo.setResponseCode(ResponseCode.DUPLICATE_FILE_FAILURE);
         irInfo.setResponseText("The file '" + message.getFileID() 
                 + "' already exists within the archive.");
         reply.setResponseInfo(irInfo);
 
         log.debug("Sending IdentifyPillarsForPutfileResponse: " + reply);
         getMessageBus().sendMessage(reply);
     }
     
     /**
      * Method for sending a positive response for putting this file.
      * @param message The message to respond to.
      */
     protected void respondSuccesfullIdentification(IdentifyPillarsForPutFileRequest message)  {
         log.info("Creating positive reply for '" + message + "'");
         IdentifyPillarsForPutFileResponse reply = createFinalResponse(message);
 
         // Needs to filled in: AuditTrailInformation, PillarChecksumSpec, ReplyTo, TimeToDeliver
         reply.setReplyTo(getSettings().getReceiverDestinationID());
         reply.setTimeToDeliver(TimeMeasurementUtils.getTimeMeasurementFromMiliseconds(
                 getSettings().getReferenceSettings().getPillarSettings().getTimeToStartDeliver()));
         
         ResponseInfo irInfo = new ResponseInfo();
         irInfo.setResponseCode(ResponseCode.IDENTIFICATION_POSITIVE);
         irInfo.setResponseText(RESPONSE_FOR_POSITIVE_IDENTIFICATION);
         reply.setResponseInfo(irInfo);
 
         log.debug("Sending IdentifyPillarsForPutfileResponse: " + reply);
         getMessageBus().sendMessage(reply);
     }
     
     /**
      * Creates a IdentifyPillarsForPutFileResponse based on a 
      * IdentifyPillarsForPutFileRequest. The following fields are not inserted:
      * <br/> - TimeToDeliver
      * <br/> - AuditTrailInformation
      * <br/> - PillarChecksumSpec
      * <br/> - IdentifyResponseInfo
      * 
      * @param msg The IdentifyPillarsForPutFileRequest to base the response on.
      * @return A IdentifyPillarsForPutFileResponse from the request.
      */
     private IdentifyPillarsForPutFileResponse createFinalResponse(IdentifyPillarsForPutFileRequest msg) {
         IdentifyPillarsForPutFileResponse res
                 = new IdentifyPillarsForPutFileResponse();
         res.setMinVersion(MIN_VERSION);
         res.setVersion(VERSION);
         res.setCorrelationID(msg.getCorrelationID());
         res.setTo(msg.getReplyTo());
         res.setCollectionID(getSettings().getCollectionID());
         res.setPillarID(getSettings().getReferenceSettings().getPillarSettings().getPillarID());
         res.setReplyTo(getSettings().getReceiverDestinationID());
         res.setPillarChecksumSpec(getChecksumType());
         res.setFrom(getSettings().getReferenceSettings().getPillarSettings().getPillarID());
         
         return res;
     }
 }
