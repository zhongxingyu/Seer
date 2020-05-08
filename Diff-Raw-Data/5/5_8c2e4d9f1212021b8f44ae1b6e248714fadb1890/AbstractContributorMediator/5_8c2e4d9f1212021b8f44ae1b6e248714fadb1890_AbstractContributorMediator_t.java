 /*
  * #%L
  * Bitrepository Core
  * %%
  * Copyright (C) 2010 - 2012 The State and University Library, The Royal Library and The State Archives, Denmark
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
 package org.bitrepository.service.contributor;
 
 import org.bitrepository.bitrepositorymessages.Message;
 import org.bitrepository.bitrepositorymessages.MessageRequest;
 import org.bitrepository.protocol.messagebus.MessageBus;
 import org.bitrepository.protocol.messagebus.MessageListener;
 import org.bitrepository.service.contributor.handler.RequestHandler;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.util.HashMap;
 import java.util.Map;
 
 /**
  * Defines the general functionality for handling a set of requests. Does this by delegating the
  * handling of the specific request to appropriate <code>RequestHandler</code>s.
  */
 @SuppressWarnings("rawtypes")
 public abstract class AbstractContributorMediator implements ContributorMediator {
     /** The log.*/
     private Logger log = LoggerFactory.getLogger(getClass());
     /** The map of request handlers. Mapping between request name and message handler for the given request.*/
     private final Map<String, RequestHandler> handlerMap = new HashMap<String, RequestHandler>();
     /** The message bus.*/
     private final MessageBus messageBus;
     /** The intermediate message handler for retrieving the messages from the messagebus. */
     private final GeneralRequestHandler messageHandler;
 
     /**
      * Constructor.
      * @param messageBus The messagebus for this mediator.
      */
     public AbstractContributorMediator(MessageBus messageBus) {
         this.messageBus = messageBus;
         messageHandler = new GeneralRequestHandler();
     }
 
     /**
      * Starts listening for requests.
      * Listens both to the general collection destination and to the local destination for the contributor.
      */
     public final void start() {
         for (RequestHandler handler : createListOfHandlers()) {
             handlerMap.put(handler.getRequestClass().getSimpleName(), handler);
         }
         messageBus.addListener(getContext().getSettings().getReceiverDestinationID(), messageHandler);
         messageBus.addListener(getContext().getSettings().getCollectionDestination(), messageHandler);
     }
 
     /**
      * @return The set of <code>RequestHandler</code>s used for this contributor.
      */
     protected abstract RequestHandler[] createListOfHandlers();
 
     /**
      * @return The concrete context used for this contributor.
      */
     protected abstract ContributorContext getContext();
     
     /**
      * Make the inheriting class create the environment for safely handling the request. 
      * E.g. creating the specific fault barrier.
      * @param request The request to handle.
      * @param handler The handler for the request.
      */
     protected abstract void handleRequest(MessageRequest request, RequestHandler handler);
 
     /**
      * The message listener, which delegates the request-messages to the request handlers.
      */
     private class GeneralRequestHandler implements MessageListener {
         @Override
         public void onMessage(Message message) {
             if (message instanceof MessageRequest) {
                 RequestHandler handler = handlerMap.get(message.getClass().getSimpleName());
                 if (handler != null) {
                     handleRequest((MessageRequest) message, handler);
                 } else {
                    log.debug("Received unhandled message request: \n{}", message);
                 }
             } else {
                log.trace("Can only handle message requests, but received: \n{}", message);
             }
         }
     }
     
     /**
     * Closes the mediator by removing all the message handler.
     */
     @Override
     public void close() {
         handlerMap.clear();
         messageBus.removeListener(getContext().getSettings().getCollectionDestination(), messageHandler);
         messageBus.removeListener(getContext().getSettings().getReceiverDestinationID(), messageHandler);
     }
 }
