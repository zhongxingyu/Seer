 /*
  * #%L
  * Bitrepository Access
  * 
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
 package org.bitrepository.access.getfile.conversation;
 
 import java.util.Collection;
 import java.util.HashSet;
 
 import org.bitrepository.access.getfile.selectors.SelectedPillarForGetFileInfo;
 import org.bitrepository.bitrepositorymessages.GetFileFinalResponse;
 import org.bitrepository.bitrepositorymessages.GetFileRequest;
 import org.bitrepository.bitrepositorymessages.MessageResponse;
 import org.bitrepository.client.conversation.ConversationContext;
 import org.bitrepository.client.conversation.PerformingOperationState;
 import org.bitrepository.client.conversation.selector.ContributorResponseStatus;
 import org.bitrepository.client.eventhandler.DefaultEvent;
 import org.bitrepository.client.eventhandler.OperationEvent.OperationEventType;
 import org.bitrepository.client.exceptions.UnexpectedResponseException;
 /**
  * Models the behavior of a GetFile conversation during the file exchange phase. That is, it begins with the sending of
  * a <code>GetFileRequest</code> and finishes with on the reception of a <code>GetFileFinalResponse</code> message.
  * 
  * Note that this is only used by the GetFileConversation in the same package, therefore the visibility is package 
  * protected.
  */
 class GettingFile extends PerformingOperationState {
     private final GetFileConversationContext context;
     private final SelectedPillarForGetFileInfo selectedPillar;
     private ContributorResponseStatus responseStatus;
 
     /** 
      * The constructor for the indicated conversation.
      * @param conversation The related conversation containing context information.
      */
     public GettingFile(GetFileConversationContext context, SelectedPillarForGetFileInfo pillar) {
         this.context = context;
         this.selectedPillar = pillar;
         Collection<String> contributors = new HashSet<String>();
         contributors.add(pillar.getID());
         this.responseStatus = new ContributorResponseStatus(contributors);
     }    
     
     @Override
     protected void generateCompleteEvent(MessageResponse msg) throws UnexpectedResponseException {
         if (msg instanceof GetFileFinalResponse) {
             GetFileFinalResponse response = (GetFileFinalResponse) msg;
            getContext().getMonitor().complete(
                    new DefaultEvent(OperationEventType.COMPLETE, 
                            "Finished getting file " + response.getFileID() + " from " + response.getPillarID()));
         } else {
             throw new UnexpectedResponseException("Received unexpected msg " + msg.getClass().getSimpleName() +
                     " while waiting for GetFile response.");
         }        
     }
     
     @Override
     protected ContributorResponseStatus getResponseStatus() {
         return responseStatus;
     }
     
     @Override
     protected void sendRequest() {
         GetFileRequest msg = new GetFileRequest();
         initializeMessage(msg);
         msg.setFileAddress(context.getUrlForResult().toExternalForm());
         msg.setFileID(context.getFileID());
         msg.setPillarID(selectedPillar.getID());
         msg.setTo(selectedPillar.getDestination());
         context.getMonitor().requestSent("Sending request for get file", selectedPillar.toString());
         context.getMessageSender().sendMessage(msg);   
     }
     
     @Override
     protected ConversationContext getContext() {
         return context;
     }
      
     @Override
     protected String getName() {
         return "Getting file";
     }
 
 }
