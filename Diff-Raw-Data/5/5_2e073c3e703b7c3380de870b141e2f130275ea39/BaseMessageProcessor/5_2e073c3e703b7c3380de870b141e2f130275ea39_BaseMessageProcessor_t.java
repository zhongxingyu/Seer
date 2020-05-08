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
 
 import javax.jms.Destination;
 
 import org.apache.log4j.Logger;
 
 import com.paxxis.cornerstone.base.ErrorListener;
 import com.paxxis.cornerstone.base.ErrorMessage;
 import com.paxxis.cornerstone.base.RequestMessage;
 import com.paxxis.cornerstone.base.ResponseMessage;
 import com.paxxis.cornerstone.database.DatabaseConnection;
 import com.paxxis.cornerstone.database.DatabaseConnectionPool;
 
 /**
  *  
  * @author Matthew Pflueger
  */
 public abstract class BaseMessageProcessor<REQ extends RequestMessage, RESP extends ResponseMessage<REQ>> 
         extends MessageProcessor<REQ, RESP> {
     private static final Logger logger = Logger.getLogger(BaseMessageProcessor.class);
 
     private DatabaseConnectionPool databasePool;
     private DatabaseConnectionPool.PoolEntry databasePoolEntry;
     private boolean ignorePreviousChanges = false;
     private ErrorListener errorListener;
     private Destination replyTo = null;
     
     @Override
     protected RESP process(boolean ignorePreviousChanges, Destination replyTo) {
         this.ignorePreviousChanges = ignorePreviousChanges;
         this.replyTo = replyTo;
         
         REQ requestMessage = getMessagePayload();
         requestMessage.setRequestReceivedOn(System.currentTimeMillis());
         
         RESP responseMessage = createResponseMessage();
         if (responseMessage != null) {
 	        responseMessage.setRequest(requestMessage);
         }
         
         try {
 	        if (!process(requestMessage, responseMessage)) {
 	        	responseMessage = null;
 	        }
         } catch (Exception e) {
            logger.error("Unknown error occurred in while processing message " + requestMessage, e);
             if (responseMessage != null) {
 	            ErrorMessage em = new ErrorMessage();
 	            em.setMessage(e.getMessage());
 	            responseMessage.setErrorMessage(em);
             }
             
             if (this.errorListener != null) {
                 try {
                     //notify the error listener...
                     this.errorListener.onError(requestMessage, responseMessage, e);
                 } catch (Exception ee) {
                    logger.error("Unknown error occurred in error listener", ee);
                 }
             }
             
         } finally {
             if (this.databasePoolEntry != null && this.databasePool != null) {
                 this.databasePool.returnInstance(this.databasePoolEntry, this);
             }
             this.databasePoolEntry = null;
         }
         
 		if (responseMessage != null) {
 			responseMessage.setResponseSentOn(System.currentTimeMillis());
 		}
         return responseMessage;
     }
 
     /**
      * 
      * @param requestMessage the request message
      * @param responseMessage the response message
      * @return true if the response should be sent to the replyTo, otherwise false
      */
     protected abstract boolean process(REQ requestMessage, RESP responseMessage) throws Exception;
 
     
     //FIXME this doesn't really need to be here as it was put into to hack around
     //DataInstanceRequest in Chime Service
     protected DatabaseConnectionPool.PoolEntry getDatabasePoolEntry() {
         if (this.databasePoolEntry == null) {
             this.databasePoolEntry = this.databasePool.borrow(this);
         }
         return this.databasePoolEntry;
     }
     
     protected DatabaseConnection getConnection() {
         return getDatabasePoolEntry().getObject();
     }
     
     protected Destination getReplyTo() {
     	return replyTo;
     }
     
     @Override
     public Integer getRequestMessageType() {
         return createRequestMessage().getMessageType();
     }
     
     @Override
     public Integer getRequestMessageVersion() {
         return createRequestMessage().getMessageVersion();
     }
     
     @Override
     public Integer getResponseMessageType() {
         return createResponseMessage().getMessageType();
     }
     
     @Override
     public Integer getResponseMessageVersion() {
         return createResponseMessage().getMessageVersion();
     }
     
     protected RESP createResponseMessage() {
         try {
             return getResponseMessageClass().newInstance();
         } catch (Exception e) {
             throw new RuntimeException(e);
         }
     }
     
     protected REQ createRequestMessage() {
         try {
 	        return getRequestMessageClass().newInstance();
         } catch (Exception e) {
             throw new RuntimeException(e);
         }
     }
 
     public boolean isIgnorePreviousChanges() {
         return this.ignorePreviousChanges;
     }
     
     protected DatabaseConnectionPool getDatabasePool() {
         return this.databasePool;
     }
     
     public void setDatabasePool(DatabaseConnectionPool databasePool) {
         this.databasePool = databasePool;
     }
 
     public ErrorListener getErrorListener() {
         return errorListener;
     }
 
     public void setErrorListener(ErrorListener errorListener) {
         this.errorListener = errorListener;
     }
 
 }
