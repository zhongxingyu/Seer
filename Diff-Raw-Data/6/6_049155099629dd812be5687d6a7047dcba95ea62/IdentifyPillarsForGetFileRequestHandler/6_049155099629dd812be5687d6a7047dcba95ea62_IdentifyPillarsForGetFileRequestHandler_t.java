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
 
 import org.bitrepository.bitrepositoryelements.ResponseCode;
 import org.bitrepository.bitrepositoryelements.ResponseInfo;
 import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetFileRequest;
 import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetFileResponse;
 import org.bitrepository.bitrepositorymessages.MessageResponse;
 import org.bitrepository.pillar.cache.ChecksumStore;
 import org.bitrepository.pillar.common.MessageHandlerContext;
 import org.bitrepository.service.exception.InvalidMessageException;
 import org.bitrepository.service.exception.RequestHandlerException;
 
 /**
  * Class for handling the identification of this pillar for the purpose of performing the GetFile operation.
  */
 public class IdentifyPillarsForGetFileRequestHandler extends ChecksumPillarMessageHandler<IdentifyPillarsForGetFileRequest> {
     /**
      * Constructor.
      * @param context The context of the message handler.
      * @param refCache The cache for the checksum data.
      */
     public IdentifyPillarsForGetFileRequestHandler(MessageHandlerContext context, ChecksumStore refCache) {
         super(context, refCache);
     }
     
     @Override
     public Class<IdentifyPillarsForGetFileRequest> getRequestClass() {
         return IdentifyPillarsForGetFileRequest.class;
     }
 
     @Override
     public void processRequest(IdentifyPillarsForGetFileRequest message) throws RequestHandlerException {
         ResponseInfo ri = new ResponseInfo();
         ri.setResponseCode(ResponseCode.REQUEST_NOT_SUPPORTED);
         ri.setResponseText("The ChecksumPillar '" 
                 + getSettings().getReferenceSettings().getPillarSettings().getPillarID() + "' cannot handle a "
                 + "request for the actual file, since it only contains the checksum of the file.");
         
         throw new InvalidMessageException(ri, message.getCollectionID());
     }
 
     @Override
     public MessageResponse generateFailedResponse(IdentifyPillarsForGetFileRequest message) {
         return createFinalResponse(message);
     }
     
     /**
      * Creates a IdentifyPillarsForGetFileResponse based on a 
      * IdentifyPillarsForGetFileRequest. The following fields are not inserted:
      * <br/> - TimeToDeliver
      * <br/> - AuditTrailInformation
      * <br/> - IdentifyResponseInfo
      * 
      * @param request The IdentifyPillarsForGetFileRequest to base the response on.
      * @return The response to the request.
      */
     private IdentifyPillarsForGetFileResponse createFinalResponse(IdentifyPillarsForGetFileRequest request) {
         IdentifyPillarsForGetFileResponse res = new IdentifyPillarsForGetFileResponse();
         res.setFileID(request.getFileID());
         res.setPillarID(getSettings().getReferenceSettings().getPillarSettings().getPillarID());
         
         return res;
     }
 }
