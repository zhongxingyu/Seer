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
 import org.bitrepository.bitrepositorymessages.GetFileRequest;
 import org.bitrepository.bitrepositorymessages.MessageResponse;
 import org.bitrepository.client.conversation.ConversationContext;
 import org.bitrepository.client.conversation.PerformingOperationState;
 import org.bitrepository.client.conversation.selector.SelectedComponentInfo;
 import org.bitrepository.client.eventhandler.ContributorCompleteEvent;
 import org.bitrepository.common.exceptions.UnableToFinishException;
 
 /**
  * Models the behavior of a GetFile conversation during the file exchange phase. That is, it begins with the sending of
  * a <code>GetFileRequest</code> and finishes with on the reception of a <code>GetFileFinalResponse</code> message.
  * 
  * Note that this is only used by the GetFileConversation in the same package, therefore the visibility is package 
  * protected.
  */
 class GettingFile extends PerformingOperationState {
     private final GetFileConversationContext context;
     private final SelectedComponentInfo selectedPillar;
 
     /**
      * @param context The related conversation containing context information.
      * @param pillar The pillar the file should be requested from.
      */
     public GettingFile(GetFileConversationContext context, SelectedComponentInfo pillar) {
         super(pillar.getID());
         this.context = context;
         this.selectedPillar = pillar;
         Collection<String> contributors = new HashSet<String>();
         contributors.add(pillar.getID());
     }
 
     @Override
     protected void sendRequest() {
         GetFileRequest msg = new GetFileRequest();
         initializeMessage(msg);
         msg.setFileAddress(context.getUrlForResult().toExternalForm());
         msg.setFileID(context.getFileID());
         msg.setFilePart(context.getFilePart());
         msg.setPillarID(selectedPillar.getID());
         msg.setTo(selectedPillar.getDestination());
         context.getMonitor().requestSent("Sending GetFileRequest to ", selectedPillar.toString());
         context.getMessageSender().sendMessage(msg);
     }
 
     protected void handleFailureResponse(MessageResponse msg) throws UnableToFinishException {
         throw new UnableToFinishException("Failed to get file from " + msg.getFrom() +
                 ", " + msg.getResponseInfo());
     }
 
     @Override
     protected void generateContributorCompleteEvent(MessageResponse msg) {
         getContext().getMonitor().contributorComplete(new ContributorCompleteEvent(msg.getFrom()));
     }
 
     @Override
     protected ConversationContext getContext() {
         return context;
     }
      
     @Override
     protected String getPrimitiveName() {
         return "GetFile";
     }
 }
