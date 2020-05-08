 /*
  * #%L
  * Bitmagasin integrationstest
  * 
  * $Id$
  * $HeadURL$
  * %%
  * Copyright (C) 2010 The State and University Library, The Royal Library and The State Archives, Denmark
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
 package org.bitrepository.protocol.activemq;
 
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Map;
 
 import javax.jms.Connection;
 import javax.jms.DeliveryMode;
 import javax.jms.Destination;
 import javax.jms.ExceptionListener;
 import javax.jms.JMSException;
 import javax.jms.MessageConsumer;
 import javax.jms.MessageProducer;
 import javax.jms.Session;
 import javax.jms.TextMessage;
 
 import org.apache.activemq.ActiveMQConnectionFactory;
 import org.apache.activemq.util.ByteArrayInputStream;
 import org.bitrepository.bitrepositorymessages.AlarmMessage;
 import org.bitrepository.bitrepositorymessages.DeleteFileFinalResponse;
 import org.bitrepository.bitrepositorymessages.DeleteFileProgressResponse;
 import org.bitrepository.bitrepositorymessages.DeleteFileRequest;
 import org.bitrepository.bitrepositorymessages.GetChecksumsFinalResponse;
 import org.bitrepository.bitrepositorymessages.GetChecksumsProgressResponse;
 import org.bitrepository.bitrepositorymessages.GetChecksumsRequest;
 import org.bitrepository.bitrepositorymessages.GetFileFinalResponse;
 import org.bitrepository.bitrepositorymessages.GetFileIDsFinalResponse;
 import org.bitrepository.bitrepositorymessages.GetFileIDsProgressResponse;
 import org.bitrepository.bitrepositorymessages.GetFileIDsRequest;
 import org.bitrepository.bitrepositorymessages.GetFileProgressResponse;
 import org.bitrepository.bitrepositorymessages.GetFileRequest;
 import org.bitrepository.bitrepositorymessages.IdentifyPillarsForDeleteFileRequest;
 import org.bitrepository.bitrepositorymessages.IdentifyPillarsForDeleteFileResponse;
 import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetChecksumsRequest;
 import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetChecksumsResponse;
 import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetFileIDsRequest;
 import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetFileIDsResponse;
 import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetFileRequest;
 import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetFileResponse;
 import org.bitrepository.bitrepositorymessages.IdentifyPillarsForPutFileRequest;
 import org.bitrepository.bitrepositorymessages.IdentifyPillarsForPutFileResponse;
 import org.bitrepository.bitrepositorymessages.IdentifyPillarsForReplaceFileRequest;
 import org.bitrepository.bitrepositorymessages.IdentifyPillarsForReplaceFileResponse;
 import org.bitrepository.bitrepositorymessages.Message;
 import org.bitrepository.bitrepositorymessages.PutFileFinalResponse;
 import org.bitrepository.bitrepositorymessages.PutFileProgressResponse;
 import org.bitrepository.bitrepositorymessages.PutFileRequest;
 import org.bitrepository.bitrepositorymessages.ReplaceFileFinalResponse;
 import org.bitrepository.bitrepositorymessages.ReplaceFileProgressResponse;
 import org.bitrepository.bitrepositorymessages.ReplaceFileRequest;
 import org.bitrepository.common.JaxbHelper;
 import org.bitrepository.protocol.CoordinationLayerException;
 import org.bitrepository.protocol.messagebus.MessageBus;
 import org.bitrepository.protocol.messagebus.MessageListener;
 import org.bitrepository.settings.collectionsettings.MessageBusConfiguration;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.xml.sax.SAXException;
 
 /**
  * Contains the basic functionality for connection and communicating with the
  * coordination layer over JMS through active MQ.
  *
  * TODO add retries for whenever a JMS exception is thrown. Currently it is
  * very unstable to connection issues.
  *
  * TODO currently creates only topics.
  */
 public class ActiveMQMessageBus implements MessageBus {
     /** The Log. */
     private final Logger log = LoggerFactory.getLogger(getClass());
 
     /** The key for storing the message type in a string property in the message headers. */
     public static final String MESSAGE_TYPE_KEY = "org.bitrepository.messages.type";
     /** The key for storing the BitRepositoryCollectionID in a string property in the message headers. */
     public static final String COLLECTION_ID_KEY = "org.bitrepository.messages.collectionid";
     /** The default acknowledge mode. */
     public static final int ACKNOWLEDGE_MODE = Session.AUTO_ACKNOWLEDGE;
     /** Default transacted. */
    public static final boolean TRANSACTED = true;
 
     /** The variable to separate the parts of the consumer key. */
     private static final String CONSUMER_KEY_SEPARATOR = "#";
 
     /** The session for sending messages. Should not be the same as the consumer session, 
      * as sessions are not thread safe. This also means the session should be used in a synchronized manor.
      * TODO Switch to use a session pool/producer poll to allow multithreaded message sending, see 
      * https://sbforge.org/jira/browse/BITMAG-357.
      */
     private final Session producerSession;
     
     /** The session for receiving messages. */
     private final Session consumerSession;
 
     /**
      * Map of the consumers, mapping from a hash of "destinations and listener" to consumer.
      * Used to identify if a listener is already registered.
      */
     private final Map<String, MessageConsumer> consumers = Collections
             .synchronizedMap(new HashMap<String, MessageConsumer>());
     /** Map of destinations, mapping from ID to destination. */
     private final Map<String, Destination> destinations = new HashMap<String, Destination>();
     /** The configuration for the connection to the activeMQ. */
     private final MessageBusConfiguration configuration;
     private String schemaLocation = "BitRepositoryMessages.xsd";
     private final JaxbHelper jaxbHelper;
     private final Connection connection;
     
     /**
      * Use the {@link org.bitrepository.protocol.ProtocolComponentFactory} to get a handle on a instance of
      * MessageBusConnections. This constructor is for the
      * <code>ProtocolComponentFactory</code> eyes only.
      *
      * @param messageBusConfigurations The properties for the connection.
      */
     public ActiveMQMessageBus(MessageBusConfiguration messageBusConfiguration) {
         log.debug("Initializing ActiveMQConnection to '" + messageBusConfiguration + "'.");
         this.configuration = messageBusConfiguration;
         jaxbHelper = new JaxbHelper("xsd/", schemaLocation); 
         // Retrieve factory for connection
         ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(configuration.getURL());
 
         try {
             connection = connectionFactory.createConnection();
             connection.setExceptionListener(new MessageBusExceptionListener());
             
             producerSession = connection.createSession(TRANSACTED, ACKNOWLEDGE_MODE);
             consumerSession = connection.createSession(TRANSACTED, ACKNOWLEDGE_MODE);
             
             startListeningForMessages();
             
         } catch (JMSException e) {
             throw new CoordinationLayerException("Unable to initialise connection to message bus", e);
         }
         log.debug("ActiveMQConnection initialized for '" + configuration + "'.");
     }
     
     /**
      * Start to listen for message on the message bus. This is done in a separate thread to avoid blocking, 
      * so the main thread can continue without having to wait for the messagebus listening to start.
      */
     private void startListeningForMessages() {
         Thread connectionStarter = new Thread(new Runnable() {
             @Override
             public void run() {
                 try {
                     connection.start();
                 } catch (Exception e) {
                     log.error("Unable to start listening on the message bus", e);
                 }
             }               
         });
         connectionStarter.start();
 
     }
 
     @Override
     public synchronized void addListener(String destinationID, final MessageListener listener) {
         log.debug("Adding listener '{}' to destination: '{}' on message-bus '{}'.", 
                 new Object[] {listener, destinationID, configuration.getName()});
         MessageConsumer consumer = getMessageConsumer(destinationID, listener);
         try {
             consumer.setMessageListener(new ActiveMQMessageListener(listener));
         } catch (JMSException e) {
             throw new CoordinationLayerException(
                     "Unable to add listener '" + listener + "' to destinationID '" + destinationID + "'", e);
         }
     }
 
     @Override
     public synchronized void removeListener(String destinationID, MessageListener listener) {
         log.debug("Removing listener '" + listener + "' from destination: '" + destinationID + "' " +
         		"on message-bus '" + configuration + "'.");
         MessageConsumer consumer = getMessageConsumer(destinationID, listener);
         try {
             // We need to set the listener to null to have the removeListerer take effect at once. 
             // If this isn't done the listener will continue to receive messages. Do we have a memory leak here? 
             consumer.setMessageListener(null);
             consumer.close();
         } catch (JMSException e) {
             throw new CoordinationLayerException(
                     "Unable to remove listener '" + listener + "' from destinationID '" + destinationID + "'", e);
         }
         consumers.remove(getConsumerHash(destinationID, listener));
     }
     
     @Override 
     public void close() throws JMSException {
         connection.close();
     }
 
     @Override
     public void sendMessage(Message content) {
         sendMessage(content.getTo(), content.getReplyTo(), content.getCollectionID(),
                 content.getCorrelationID(), content);
     }
 
     /**
      * Send a message using ActiveMQ.
      * 
      * Note that the method is synchronized to avoid multithreaded usage of the providerSession.
      *
      * @param destinationID Name of destination to send message to.
      * @param replyTo       The queue to reply to.
      * @param collectionID  The collection ID of the message.
      * @param correlationID The correlation ID of the message.
      * @param content       JAXB-serializable object to send.
      */
     private synchronized void sendMessage(String destinationID, String replyTo, String collectionID, String correlationID,
                              Object content) {
         String xmlContent = null;
         try {
             xmlContent = jaxbHelper.serializeToXml(content);
             jaxbHelper.validate(new ByteArrayInputStream(xmlContent.getBytes()));
             log.debug("The following message is sent to the destination '" + destinationID + "'" + " on message-bus '"
                               + configuration.getName() + "': \n{}", xmlContent);
             MessageProducer producer = addDestinationMessageProducer(destinationID);
             producer.setDeliveryMode(DeliveryMode.PERSISTENT);
 
             javax.jms.Message msg = producerSession.createTextMessage(xmlContent);
             msg.setStringProperty(MESSAGE_TYPE_KEY, content.getClass().getSimpleName());
             msg.setStringProperty(COLLECTION_ID_KEY, collectionID);
             msg.setJMSCorrelationID(correlationID);
             msg.setJMSReplyTo(getDestination(replyTo, producerSession));
 
             producer.send(msg);
            producerSession.commit();
         } catch (SAXException e) {
             throw new CoordinationLayerException("Rejecting to send invalid message: " + xmlContent, e);
         } catch (Exception e) {
             throw new CoordinationLayerException("Could not send message", e);
         }
     }
 
     /**
      * Retrieves a consumer for the specific destination id and message listener.
      * If no such consumer already exists, then it is created.
      *
      * @param destinationID The id of the destination to consume messages from.
      * @param listener      The listener to consume the messages.
      * @return The instance for consuming the messages.
      */
     private MessageConsumer getMessageConsumer(String destinationID, MessageListener listener) {
         String key = getConsumerHash(destinationID, listener);
         log.debug("Retrieving message consumer on destination '" + destinationID + "' for listener '" + listener
                           + "'. Key: '" + key + "'.");
         if (!consumers.containsKey(key)) {
             log.debug("No consumer known. Creating new for key '" + key + "'.");
             Destination destination = getDestination(destinationID, consumerSession);
             MessageConsumer consumer;
             try {
                 consumer = consumerSession.createConsumer(destination);
             } catch (JMSException e) {
                 throw new CoordinationLayerException("Could not create message consumer for destination '" + destination + '"', e);
             }
             consumers.put(key, consumer);
         }
         return consumers.get(key);
     }
 
     /**
      * Creates a unique hash of the message listener and the destination id.
      *
      * @param destinationID The id for the destination.
      * @param listener      The message listener.
      * @return The key for the message listener and the destination id.
      */
     private String getConsumerHash(String destinationID, MessageListener listener) {
         return destinationID + CONSUMER_KEY_SEPARATOR + listener.hashCode();
     }
 
     /**
      * Method for retrieving the message producer for a specific queue.
      *
      * @param destination The id for the destination.
      * @return The message producer for this destination.
      */
     private MessageProducer addDestinationMessageProducer(String destinationID) {
         Destination destination = getDestination(destinationID, producerSession);
         MessageProducer producer;
         try {
             producer = producerSession.createProducer(destination);
         } catch (JMSException e) {
             throw new CoordinationLayerException(
                     "Could not create message producer for destination '" + destinationID + "'", e);
         }
         return producer;
     }
 
     /**
      * Given a destination ID, retrieve the destination object.
      *
      * @param destinationID ID of the destination.
      * @return The object representing that destination. Will always return the same destination object for the same destination ID.
      */
     private Destination getDestination(String destinationID, Session session) {
         Destination destination = destinations.get(destinationID);
         if (destination == null) {
             try {
                 
                 String[] parts = destinationID.split("://");
                 if (parts.length == 1) {
                     destination = session.createTopic(destinationID);
                 } else if (parts.length == 2) {
                     if (parts[0].equals("topic")) {
                         destination = session.createTopic(parts[1]);
                     } else if (parts[0].equals("queue")) {
                         destination = session.createQueue(parts[1]);
                     } else if (parts[0].equals("temporary-queue")) {
                         destination = session.createTemporaryQueue();
                     } else if (parts[0].equals("temporary-topic")) {
                         destination = session.createTemporaryTopic();
                     } else {
                         throw new CoordinationLayerException("Unable to create destination '" + 
                                 destination + "'. Unknown type.");
                     }
                 }
                 
                 // TODO: According to javadoc, topics should be looked up in another fashion.
                 // See http://download.oracle.com/javaee/6/api/javax/jms/Session.html#createTopic(java.lang.String)
             } catch (JMSException e) {
                 throw new CoordinationLayerException("Could not create destination '" + destinationID + "'", e);
             }
             destinations.put(destinationID, destination);
         }
         return destination;
     }
 
     /** Class for handling the message bus exceptions. */
     private class MessageBusExceptionListener implements ExceptionListener {
         @Override
         public void onException(JMSException arg0) {
             log.error("JMSException caught: ", arg0);
         }
     }
 
     /**
      * Adapter from Active MQ message listener to protocol message listener.
      *
      * This adapts from general Active MQ messages to the protocol types.
      */
     private class ActiveMQMessageListener implements javax.jms.MessageListener {
         /** The Log. */
         private Logger log = LoggerFactory.getLogger(getClass());
 
         /** The protocol message listener that receives the messages. */
         private final MessageListener listener;
 
         /**
          * Initialise the adapter from ActiveMQ message listener to protocol
          * message listener.
          *
          * @param listener The protocol message listener that should receive the
          *                 messages.
          */
         public ActiveMQMessageListener(MessageListener listener) {
             this.listener = listener;
         }
 
         /**
          * When receiving the message, call the appropriate method on the
          * protocol message listener.
          *
          * This method acts as a fault barrier for all exceptions from message
          * reception. They are all logged as warnings, but otherwise ignored.
          *
          * @param jmsMessage The message received.
          */
         @Override
         public void onMessage(final javax.jms.Message jmsMessage) {
             String type = null;
             String text = null;
 
             Object content;
             try {
                 type = jmsMessage.getStringProperty(MESSAGE_TYPE_KEY);
                 text = ((TextMessage) jmsMessage).getText();
                 jaxbHelper.validate(new ByteArrayInputStream(text.getBytes()));
                 content = jaxbHelper.loadXml(Class.forName("org.bitrepository.bitrepositorymessages." + type),
                                              new ByteArrayInputStream(text.getBytes()));
                 log.debug("Received message: " + text);
                 if(content.getClass().equals(AlarmMessage.class)){
                 	listener.onMessage((AlarmMessage) content);
                 	return;
                 }
                 
                 if (content.getClass().equals(DeleteFileFinalResponse.class)) {
                     listener.onMessage((DeleteFileFinalResponse) content);
                     return;
                 }
                 
                 if (content.getClass().equals(DeleteFileProgressResponse.class)) {
                     listener.onMessage((DeleteFileProgressResponse) content);
                     return;
                 }
                 
                 if (content.getClass().equals(DeleteFileRequest.class)) {
                     listener.onMessage((DeleteFileRequest) content);
                     return;
                 }
                 
                 if (content.getClass().equals(GetChecksumsFinalResponse.class)) {
                     listener.onMessage((GetChecksumsFinalResponse) content);
                     return;
                 }
 
                 if (content.getClass().equals(GetChecksumsRequest.class)) {
                     listener.onMessage((GetChecksumsRequest) content);
                     return;
                 }
 
                 if (content.getClass().equals(GetChecksumsProgressResponse.class)) {
                     listener.onMessage((GetChecksumsProgressResponse) content);
                     return;
                 }
 
                 if (content.getClass().equals(GetFileFinalResponse.class)) {
                     listener.onMessage((GetFileFinalResponse) content);
                     return;
                 }
 
                 if (content.getClass().equals(GetFileIDsFinalResponse.class)) {
                     listener.onMessage((GetFileIDsFinalResponse) content);
                     return;
                 }
 
                 if (content.getClass().equals(GetFileIDsRequest.class)) {
                     listener.onMessage((GetFileIDsRequest) content);
                     return;
                 }
 
                 if (content.getClass().equals(GetFileIDsProgressResponse.class)) {
                     listener.onMessage((GetFileIDsProgressResponse) content);
                     return;
                 }
 
                 if (content.getClass().equals(GetFileRequest.class)) {
                     listener.onMessage((GetFileRequest) content);
                     return;
                 }
 
                 if (content.getClass().equals(GetFileProgressResponse.class)) {
                     listener.onMessage((GetFileProgressResponse) content);
                     return;
                 }
 
                 if (content.getClass().equals(IdentifyPillarsForDeleteFileRequest.class)) {
                     listener.onMessage((IdentifyPillarsForDeleteFileRequest) content);
                     return;
                 }
 
                 if (content.getClass().equals(IdentifyPillarsForDeleteFileResponse.class)) {
                     listener.onMessage((IdentifyPillarsForDeleteFileResponse) content);
                     return;
                 }
                 
                 if (content.getClass().equals(IdentifyPillarsForGetChecksumsResponse.class)) {
                     listener.onMessage((IdentifyPillarsForGetChecksumsResponse) content);
                     return;
                 }
 
                 if (content.getClass().equals(IdentifyPillarsForGetChecksumsRequest.class)) {
                     listener.onMessage((IdentifyPillarsForGetChecksumsRequest) content);
                     return;
                 }
 
                 if (content.getClass().equals(IdentifyPillarsForGetFileIDsResponse.class)) {
                     listener.onMessage((IdentifyPillarsForGetFileIDsResponse) content);
                     return;
                 }
 
                 if (content.getClass().equals(IdentifyPillarsForGetFileIDsRequest.class)) {
                     listener.onMessage((IdentifyPillarsForGetFileIDsRequest) content);
                     return;
                 }
 
                 if (content.getClass().equals(IdentifyPillarsForGetFileResponse.class)) {
                     listener.onMessage((IdentifyPillarsForGetFileResponse) content);
                     return;
                 }
 
                 if (content.getClass().equals(IdentifyPillarsForGetFileRequest.class)) {
                     listener.onMessage((IdentifyPillarsForGetFileRequest) content);
                     return;
                 }
 
                 if (content.getClass().equals(IdentifyPillarsForPutFileResponse.class)) {
                     listener.onMessage((IdentifyPillarsForPutFileResponse) content);
                     return;
                 }
 
                 if (content.getClass().equals(IdentifyPillarsForPutFileRequest.class)) {
                     listener.onMessage((IdentifyPillarsForPutFileRequest) content);
                     return;
                 }
 
                 if (content.getClass().equals(IdentifyPillarsForReplaceFileResponse.class)) {
                     listener.onMessage((IdentifyPillarsForReplaceFileResponse) content);
                     return;
                 }
 
                 if (content.getClass().equals(IdentifyPillarsForReplaceFileRequest.class)) {
                     listener.onMessage((IdentifyPillarsForReplaceFileRequest) content);
                     return;
                 }
 
                 if (content.getClass().equals(PutFileFinalResponse.class)) {
                     listener.onMessage((PutFileFinalResponse) content);
                     return;
                 }
 
                 if (content.getClass().equals(PutFileRequest.class)) {
                     listener.onMessage((PutFileRequest) content);
                     return;
                 }
 
                 if (content.getClass().equals(PutFileProgressResponse.class)) {
                     listener.onMessage((PutFileProgressResponse) content);
                     return;
                 }
                 
                 if (content.getClass().equals(ReplaceFileFinalResponse.class)) {
                     listener.onMessage((ReplaceFileFinalResponse) content);
                     return;
                 }
 
                 if (content.getClass().equals(ReplaceFileRequest.class)) {
                     listener.onMessage((ReplaceFileRequest) content);
                     return;
                 }
 
                 if (content.getClass().equals(ReplaceFileProgressResponse.class)) {
                     listener.onMessage((ReplaceFileProgressResponse) content);
                     return;
                 }
                 log.error("Received message of unknown type '" + type + "'\n{}", text);
             } catch (SAXException e) {
                 log.error("Error validating message " + jmsMessage, e);
             } catch (Exception e) {
                 log.error("Error handling message. Received type was '" + type + "'.\n{}", text, e);
             }
 
         }
     }
 }
