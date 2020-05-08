 /*
  *    Licensed under the Apache License, Version 2.0 (the "License");
  *    you may not use this file except in compliance with the License.
  *    You may obtain a copy of the License at
  *
  *        http://www.apache.org/licenses/LICENSE-2.0
  *
  *    Unless required by applicable law or agreed to in writing, software
  *    distributed under the License is distributed on an "AS IS" BASIS,
  *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *    See the License for the specific language governing permissions and
  *    limitations under the License.
  */
 package org.apache.camel.component.sjms.messagehandlers;
 
 import static org.apache.camel.component.sjms.SjmsConstants.JMS_MESSAGE_TYPE;
 import static org.apache.camel.util.ObjectHelper.wrapRuntimeCamelException;
 
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.concurrent.atomic.AtomicBoolean;
 
 import javax.jms.BytesMessage;
 import javax.jms.Destination;
 import javax.jms.MapMessage;
 import javax.jms.Message;
 import javax.jms.ObjectMessage;
 import javax.jms.Session;
 import javax.jms.TextMessage;
 
 import org.apache.camel.AsyncProcessor;
 import org.apache.camel.Endpoint;
 import org.apache.camel.Exchange;
 import org.apache.camel.RuntimeCamelException;
 import org.apache.camel.component.sjms.JmsMessageHelper;
 import org.apache.camel.component.sjms.JmsMessageType;
 import org.apache.camel.component.sjms.MessageHandler;
 import org.apache.camel.component.sjms.jms.SessionAcknowledgementType;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * TODO Add Class documentation for DefaultMessageHandler
  *
  * @author sully6768
  */
 public class DefaultMessageHandler implements MessageHandler {
 
     protected final Logger LOGGER = LoggerFactory.getLogger(getClass());
     private Endpoint endpoint;
     private AsyncProcessor processor;
     private Session session;
     private boolean transacted = false;
     private SessionAcknowledgementType acknowledgementType = SessionAcknowledgementType.AUTO_ACKNOWLEDGE;
     private boolean async = false;
     private final AtomicBoolean stopped;
     private Destination namedReplyToDestination;
     
     /**
      * TODO Add Constructor Javadoc
      *
      * @param endpoint
      * @param processor
      */
     public DefaultMessageHandler(AtomicBoolean stopped) {
         super();
         this.stopped = stopped;
     }
     
     @Override
     public void onMessage(Message message) {
         handleMessage(message);
     }
     
     
 
     /**
      * @param message
      */
     @Override
     public void handleMessage(Message message) {
         RuntimeCamelException rce = null;
         try {
             final Exchange exchange = createExchange(message);
 
             try {
                 doHandleMessage(exchange);
             } catch(Exception e) {
                 if(exchange != null) {
                     if (exchange.getException() == null) {
                         exchange.setException(e);
                     } else {
                         throw e;
                     }
                 }
             }
         } catch(Exception e) {
             rce = wrapRuntimeCamelException(e);
         } finally {
             if(rce != null) {
                 throw rce;
             }
         }
     }
     
     
     public void doHandleMessage(final Exchange exchange) {}
     
     @Override
     public void close() throws Exception {
         if(session != null) {
             session.close();
            if (session.getTransacted()) {
                try {
                    session.rollback();
                } catch (Exception e) {
                    // Do nothing.  Just make sure we are cleaned up
                }
            }
            session = null;
         }
     }
     
     @SuppressWarnings("unchecked")
     protected Exchange createExchange(Message message) {
         Exchange exchange = endpoint.createExchange();
         try {
             JmsMessageHelper.setJmsMessageHeaders(message, exchange);
             if(message != null) {
                 // convert to JMS Message of the given type
                 switch (JmsMessageHelper.discoverType(message)) {
                 case Bytes:
                     BytesMessage bytesMessage = (BytesMessage) message;
                     if (bytesMessage.getBodyLength() > Integer.MAX_VALUE) {
                         LOGGER.warn("Length of BytesMessage is too long: {}", bytesMessage.getBodyLength());
                         return null;
                     }
                     byte[] result = new byte[(int)bytesMessage.getBodyLength()];
                     bytesMessage.readBytes(result);       
                     exchange.getIn().setHeader(JMS_MESSAGE_TYPE, JmsMessageType.Bytes);
                     exchange.getIn().setBody(result);
                     break;
                 case Map:
                     HashMap<String, Object> body = new HashMap<String, Object>();
                     MapMessage mapMessage = (MapMessage) message;
                     Enumeration<String> names = mapMessage.getMapNames();
                     while(names.hasMoreElements()) {
                         String key = names.nextElement();
                         Object value = mapMessage.getObject(key);
                         body.put(key, value);
                     }
                     exchange.getIn().setHeader(JMS_MESSAGE_TYPE, JmsMessageType.Map);
                     exchange.getIn().setBody(body);
                     break;
                 case Object:
                     ObjectMessage objMsg = (ObjectMessage) message;
                     exchange.getIn().setHeader(JMS_MESSAGE_TYPE, JmsMessageType.Object);
                     exchange.getIn().setBody(objMsg.getObject());
                     break;
                 case Text:
                     TextMessage textMsg = (TextMessage) message;
                     exchange.getIn().setHeader(JMS_MESSAGE_TYPE, JmsMessageType.Text);
                     exchange.getIn().setBody(textMsg.getText());
                     break;
                 case Message:
                 default:
                     // Do nothing.  Only set the headers for an empty message
                     exchange.getIn().setBody(message);
                     break;
                 }
             }
         } catch (Exception e) {
             exchange.setException(e);
         }
         return exchange;
     }
     
 
     
     /**
      * Sets the boolean value of transacted for this instance of DefaultMessageHandler.
      *
      * @param transacted Sets boolean, default is TODO add default
      */
     public void setTransacted(boolean transacted) {
         this.transacted = transacted;
     }
 
     /**
      * Gets the boolean value of transacted for this instance of DefaultMessageHandler.
      *
      * @return the transacted
      */
     public boolean isTransacted() {
         return transacted;
     }
 
     /**
      * Gets the SimpleJmsEndpoint value of endpoint for this instance of DefaultMessageHandler.
      *
      * @return the endpoint
      */
     public Endpoint getEndpoint() {
         return endpoint;
     }
 
     /**
      * Sets the SimpleJmsEndpoint value of endpoint for this instance of DefaultMessageHandler.
      *
      * @param endpoint Sets SimpleJmsEndpoint, default is TODO add default
      */
     public void setEndpoint(Endpoint endpoint) {
         this.endpoint = endpoint;
     }
 
     /**
      * Gets the AsyncProcessor value of processor for this instance of DefaultMessageHandler.
      *
      * @return the processor
      */
     public AsyncProcessor getProcessor() {
         return processor;
     }
 
     /**
      * Sets the AsyncProcessor value of processor for this instance of DefaultMessageHandler.
      *
      * @param processor Sets AsyncProcessor, default is TODO add default
      */
     public void setProcessor(AsyncProcessor processor) {
         this.processor = processor;
     }
 
     /**
      * Gets the SessionAcknowledgementType value of acknowledgementType for this instance of DefaultMessageHandler.
      *
      * @return the acknowledgementType
      */
     public SessionAcknowledgementType getAcknowledgementType() {
         return acknowledgementType;
     }
 
     /**
      * Sets the SessionAcknowledgementType value of acknowledgementType for this instance of DefaultMessageHandler.
      *
      * @param acknowledgementType Sets SessionAcknowledgementType, default is TODO add default
      */
     public void setAcknowledgementType(
             SessionAcknowledgementType acknowledgementType) {
         this.acknowledgementType = acknowledgementType;
     }
 
     /**
      * Sets the Session value of session for this instance of DefaultMessageHandler.
      *
      * @param session Sets Session, default is TODO add default
      */
     public void setSession(Session session) {
         this.session = session;
     }
 
     /**
      * Gets the Session value of session for this instance of DefaultMessageHandler.
      *
      * @return the session
      */
     public Session getSession() {
         return session;
     }
 
     /**
      * Sets the boolean value of async for this instance of DefaultMessageHandler.
      *
      * @param async Sets boolean, default is TODO add default
      */
     public void setAsync(boolean async) {
         this.async = async;
     }
 
     /**
      * Gets the boolean value of async for this instance of DefaultMessageHandler.
      *
      * @return the async
      */
     public boolean isAsync() {
         return async;
     }
 
     /**
      * Test to see if this message handler has been stopped
      *
      * @return true if the MessageHandler has been stopped
      */
     public boolean isStopped() {
         return stopped.get();
     }
 
     /**
      * Test to see if this message handler has been started
      *
      * @return true if the MessageHandler has been started
      */
     public boolean isStarted() {
         return !isStopped();
     }
 
     /**
      * Sets the Destination value of namedReplyToDestination for this instance of DefaultMessageHandler.
      *
      * @param namedReplyToDestination Sets Destination, default is TODO add default
      */
     public void setNamedReplyToDestination(Destination namedReplyToDestination) {
         this.namedReplyToDestination = namedReplyToDestination;
     }
 
     /**
      * Gets the Destination value of namedReplyToDestination for this instance of DefaultMessageHandler.
      *
      * @return the namedReplyToDestination
      */
     public Destination getNamedReplyToDestination() {
         return namedReplyToDestination;
     }
 }
