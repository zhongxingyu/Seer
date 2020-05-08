 /*
  * Copyright 2010 the original author or authors.
  * Copyright 2009 Paxxis Technology LLC
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package com.paxxis.cornerstone.service;
 
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.concurrent.atomic.AtomicLong;
 
 import javax.jms.JMSException;
 import javax.jms.Message;
 import javax.jms.MessageConsumer;
 import javax.jms.MessageListener;
 import javax.jms.TemporaryQueue;
 
 import org.apache.log4j.Logger;
 
 import com.paxxis.cornerstone.base.ErrorMessage;
 import com.paxxis.cornerstone.base.RequestMessage;
 import com.paxxis.cornerstone.base.ResponseMessage;
 import com.paxxis.cornerstone.common.BlockingThreadPoolExecutor;
 import com.paxxis.cornerstone.common.MessagePayload;
 import com.paxxis.cornerstone.common.ResponsePromise;
 
 /**
  *
  * @author Robert Englander
  */
 public class RequestQueueSender extends DestinationSender implements QueueSender {
     private static final Logger logger = Logger.getLogger(DestinationSender.class);
 
     // the temporary queue used for getting responses
     private TemporaryQueue responseQueue = null;
 
     // the message consumer for the responseQueue
     private MessageConsumer responseConsumer = null;
 
     // mapping of correlation ids to message listeners
     private Map<String, MessageListener> listenerMap = 
         Collections.synchronizedMap(new HashMap<String, MessageListener>());
 
     private AtomicLong correlationId = new AtomicLong(0);
 
     private long timeout = 10000;
     private BlockingThreadPoolExecutor messageExecutor;
     
     public RequestQueueSender() {
     }
 
     public void initialize() {
         if (this.messageExecutor == null) {
             this.messageExecutor = new BlockingThreadPoolExecutor();
         }
     }
     
     /**
      * The blocking thread pool executor that will execute the listeners for response messages.
      * @param messageExecutor
      */
     public void setMessageExecutor(BlockingThreadPoolExecutor messageExecutor) {
         this.messageExecutor = messageExecutor;
     }
     
     /**
      * The timeout, defaults to 10 seconds...
      * @param timeout
      */
     public void setTimeout(long timeout) {
         this.timeout = timeout;
     }
     
     /**
      * Close the JMS session objects
      */
     protected void closeDown() throws JMSException {
         //tell the broker to stop sending us messages...
         responseConsumer.close();
         responseConsumer = null;
         responseQueue.delete();
         responseQueue = null;
         super.closeDown();
     }
 
     /**
      * Setup the JMS specific objects.<br><br>
      * This method is called by the ServiceBusConnector as part of its
      * connection process.  It should not be called directly by users...
      *
      * @throws RuntimeException if the setup could not be completed
      */
     public void setup() {
     	super.setup();
         try {
             responseQueue = getConnector().createTemporaryQueue();
 
             // create a message consumer
             responseConsumer = getConnector().createConsumer(responseQueue, null);
 
             responseConsumer.setMessageListener(
                 new MessageListener() {
                     public void onMessage(final Message msg) {
                         try
                         {
                             final MessageListener listener = 
                                 listenerMap.remove(msg.getJMSCorrelationID());
                             if (listener != null) {
                                 RequestQueueSender.this.messageExecutor.submit(new Runnable() {
                                     public void run() {
                                         listener.onMessage(msg);
                                     }
                                 });
                             } else {
                                 logger.error("Possible race condition detected: Null listener for message");
                             }
                         } catch (Exception e) {
                             // this needs to be logged (and reported through mgmt interface?)
                             logger.error(e);
                         }
                     }
                 }
             );
         } catch(Throwable t) {
             try {
                 closeDown();
             } catch (JMSException e) {
                 // is there any need to include this in the runtime exception below?
             }
 
             throw new RuntimeException(t);
         }
    }
 
 
     /* (non-Javadoc)
      * @see com.paxxis.cornerstone.service.QueueSender#send(REQ, com.paxxis.cornerstone.common.MessagePayload)
      */
     @Override
     public synchronized <REQ extends RequestMessage, 
                         RESP extends ResponseMessage<REQ>> ResponsePromise<RESP> send(
             REQ msg,
             MessagePayload payloadType) {
         ResponsePromise<RESP> promise = new ResponsePromise<RESP>(this.timeout);
         send(msg, promise, null, payloadType);
         return promise;
     }
 
 
     /* (non-Javadoc)
      * @see com.paxxis.cornerstone.service.QueueSender#send(REQ, P, com.paxxis.cornerstone.common.MessagePayload)
      */
     @Override
     public synchronized <REQ extends RequestMessage,
                         RESP extends ResponseMessage<REQ>, 
                         P extends ResponsePromise<RESP>> void send(
             REQ msg,
             final P promise, 
             final MessagePayload payloadType) {
         send(msg, promise, null, payloadType);
     }
 
     /* (non-Javadoc)
      * @see com.paxxis.cornerstone.service.QueueSender#send(REQ, P, javax.jms.MessageListener, com.paxxis.cornerstone.common.MessagePayload)
      */
     @Override
     public synchronized <REQ extends RequestMessage, 
                         RESP extends ResponseMessage<REQ>, 
                         P extends ResponsePromise<RESP>> void send(
             REQ msg,
             final P promise, 
             final MessageListener listener,
             final MessagePayload payloadType) {
 
         if (promise == null) {
             throw new NullPointerException("promise is null");
         }
 
         if (!getConnector().isConnected()) {
             ErrorMessage errorMsg = new ErrorMessage();
             errorMsg.setMessage("Unable to send request.  Not connected to service bus.");
             throw new SendException(errorMsg);
         }
 
         MessageListener promiseListener = new MessageListener() {
             public void onMessage(Message message) {
                     if (listener != null) {
                         listener.onMessage(message);
                     }
                     promise.setObject(payloadType.getPayload(message));
             }
         };
 
         try {
             Message message = prepareMessage(msg, payloadType);
             listenerMap.put(message.getJMSCorrelationID(), promiseListener);
             getMessageSender().send(message);
         } catch (JMSException je) {
             ErrorMessage errorMsg = new ErrorMessage();
             errorMsg.setMessage("Unable to send request. " + je.getMessage());
             throw new SendException(errorMsg);
         }
     }
 
     /* (non-Javadoc)
      * @see com.paxxis.cornerstone.service.QueueSender#send(REQ, javax.jms.MessageListener, com.paxxis.cornerstone.common.MessagePayload)
      */
     @Override
     public synchronized <REQ extends RequestMessage,
                         RESP extends ResponseMessage<REQ>> ResponsePromise<RESP> send(
             REQ msg,
             MessageListener listener, 
             MessagePayload payloadType) {
         ResponsePromise<RESP> promise = new ResponsePromise<RESP>(this.timeout);
         send(msg, promise, listener, payloadType);
         return promise;
     }
 
     /**
      * Prepare a message for sending.
      *
      * @param requester the service requester
      */
     protected Message prepareMessage(
             com.paxxis.cornerstone.base.Message msg,
             MessagePayload payloadType) 
             throws JMSException {
 
         Message message = super.prepareMessage(msg, payloadType);
         message.setJMSReplyTo(responseQueue);
         message.setJMSCorrelationID(generateCorrelationId());
         return message;
     }
 
     private String generateCorrelationId() {
         return String.valueOf(correlationId.incrementAndGet());
     }
 }
