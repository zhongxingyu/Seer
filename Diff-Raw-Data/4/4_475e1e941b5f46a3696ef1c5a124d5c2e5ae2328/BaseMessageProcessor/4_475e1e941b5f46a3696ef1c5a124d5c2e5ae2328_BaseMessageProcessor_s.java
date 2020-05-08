 package com.paxxis.cornerstone.service;
 
 import org.apache.log4j.Logger;
 
 import com.paxxis.cornerstone.base.ErrorMessage;
 import com.paxxis.cornerstone.base.RequestMessage;
 import com.paxxis.cornerstone.base.ResponseMessage;
 import com.paxxis.cornerstone.database.DatabaseConnection;
 import com.paxxis.cornerstone.database.DatabaseConnectionPool;
 
 public abstract class BaseMessageProcessor<REQ extends RequestMessage, RESP extends ResponseMessage<REQ>> 
         extends MessageProcessor<REQ, RESP> {
     private static final Logger logger = Logger.getLogger(BaseMessageProcessor.class);
 
     private DatabaseConnectionPool databasePool;
     private DatabaseConnectionPool.PoolEntry databasePoolEntry;
     private boolean ignorePreviousChanges = false;
     
     
     @Override
     protected RESP process(boolean ignorePreviousChanges) {
         this.ignorePreviousChanges = ignorePreviousChanges;
         REQ requestMessage = getMessagePayload();
         requestMessage.setRequestReceivedOn(System.currentTimeMillis());
         
         RESP responseMessage = createResponseMessage();
         if (responseMessage != null) {
 	        responseMessage.setRequest(requestMessage);
         }
         
         try {
 	        process(requestMessage, responseMessage);
         } catch (Exception e) {
             logger.error(e);
             if (responseMessage != null) {
 	            ErrorMessage em = new ErrorMessage();
 	            em.setMessage(e.getMessage());
 	            responseMessage.setErrorMessage(em);
             }
         } finally {
             if (this.databasePoolEntry != null && this.databasePool != null) {
                 this.databasePool.returnInstance(this.databasePoolEntry, this);
             }
             this.databasePoolEntry = null;
         }
         
        responseMessage.setResponseSentOn(System.currentTimeMillis());
         return responseMessage;
     }
 
     protected abstract void process(REQ requestMessage, RESP responseMessage) throws Exception;
 
     
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
 
 }
