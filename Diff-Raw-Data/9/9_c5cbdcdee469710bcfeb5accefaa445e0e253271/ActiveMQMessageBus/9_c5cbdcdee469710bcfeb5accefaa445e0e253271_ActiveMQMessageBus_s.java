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
 import javax.jms.ExceptionListener;
 import javax.jms.JMSException;
 import javax.jms.Message;
 import javax.jms.MessageConsumer;
 import javax.jms.MessageProducer;
 import javax.jms.Session;
 import javax.jms.TextMessage;
 import javax.jms.Topic;
 import javax.xml.bind.JAXBException;
 
 import org.apache.activemq.ActiveMQConnectionFactory;
 import org.apache.activemq.util.ByteArrayInputStream;
 import org.bitrepository.bitrepositorymessages.Alarm;
 import org.bitrepository.bitrepositorymessages.GetChecksumsFinalResponse;
 import org.bitrepository.bitrepositorymessages.GetChecksumsProgressResponse;
 import org.bitrepository.bitrepositorymessages.GetChecksumsRequest;
 import org.bitrepository.bitrepositorymessages.GetFileFinalResponse;
 import org.bitrepository.bitrepositorymessages.GetFileIDsFinalResponse;
 import org.bitrepository.bitrepositorymessages.GetFileIDsProgressResponse;
 import org.bitrepository.bitrepositorymessages.GetFileIDsRequest;
 import org.bitrepository.bitrepositorymessages.GetFileProgressResponse;
 import org.bitrepository.bitrepositorymessages.GetFileRequest;
 import org.bitrepository.bitrepositorymessages.GetStatusFinalResponse;
 import org.bitrepository.bitrepositorymessages.GetStatusProgressResponse;
 import org.bitrepository.bitrepositorymessages.GetStatusRequest;
 import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetChecksumsRequest;
 import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetChecksumsResponse;
 import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetFileIDsRequest;
 import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetFileIDsResponse;
 import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetFileRequest;
 import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetFileResponse;
 import org.bitrepository.bitrepositorymessages.IdentifyPillarsForPutFileRequest;
 import org.bitrepository.bitrepositorymessages.IdentifyPillarsForPutFileResponse;
 import org.bitrepository.bitrepositorymessages.PutFileFinalResponse;
 import org.bitrepository.bitrepositorymessages.PutFileProgressResponse;
 import org.bitrepository.bitrepositorymessages.PutFileRequest;
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
 
     /** The session. */
     private final Session session;
 
     /**
      * Map of the consumers, mapping from a hash of "destinations and listener" to consumer.
      * Used to identify if a listener is already registered.
      */
     private final Map<String, MessageConsumer> consumers = Collections
             .synchronizedMap(new HashMap<String, MessageConsumer>());
     /** Map of topics, mapping from ID to topic. */
     private final Map<String, Topic> topics = new HashMap<String, Topic>();
     /** The configuration for the connection to the activeMQ. */
     private final MessageBusConfiguration configuration;
 
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
 
         // Retrieve factory for connection
         ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(configuration.getLogin(),
                                                                                     configuration.getPassword(),
                                                                                     configuration.getURL());
 
         try {
             // create and start the connection
             Connection connection = connectionFactory.createConnection();
 
             connection.setExceptionListener(new MessageBusExceptionListener());
             connection.start();
 
             session = connection.createSession(TRANSACTED, ACKNOWLEDGE_MODE);
         } catch (JMSException e) {
             throw new CoordinationLayerException("Unable to initialise connection to message bus", e);
         }
         log.debug("ActiveMQConnection initialized for '" + configuration + "'.");
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
             consumer.close();
         } catch (JMSException e) {
             throw new CoordinationLayerException(
                     "Unable to remove listener '" + listener + "' from destinationID '" + destinationID + "'", e);
         }
         consumers.remove(getConsumerHash(destinationID, listener));
     }
 
     @Override
     public void sendMessage(Alarm content) {
         sendMessage(content.getTo(), content.getReplyTo(), content.getBitRepositoryCollectionID(),
                 content.getCorrelationID(), content);
     }
 
     @Override
     public void sendMessage(GetChecksumsFinalResponse content) {
         sendMessage(content.getTo(), content.getReplyTo(), content.getBitRepositoryCollectionID(),
                     content.getCorrelationID(), content);
     }
 
     @Override
     public void sendMessage(GetChecksumsRequest content) {
         sendMessage(content.getTo(), content.getReplyTo(), content.getBitRepositoryCollectionID(),
                     content.getCorrelationID(), content);
     }
 
     @Override
     public void sendMessage(GetChecksumsProgressResponse content) {
         sendMessage(content.getTo(), content.getReplyTo(), content.getBitRepositoryCollectionID(),
                     content.getCorrelationID(), content);
     }
 
     @Override
     public void sendMessage(GetFileFinalResponse content) {
         sendMessage(content.getTo(), content.getReplyTo(), content.getBitRepositoryCollectionID(),
                     content.getCorrelationID(), content);
     }
 
     @Override
     public void sendMessage(GetFileIDsFinalResponse content) {
         sendMessage(content.getTo(), content.getReplyTo(), content.getBitRepositoryCollectionID(),
                     content.getCorrelationID(), content);
     }
 
     @Override
     public void sendMessage(GetFileIDsRequest content) {
         sendMessage(content.getTo(), content.getReplyTo(), content.getBitRepositoryCollectionID(),
                     content.getCorrelationID(), content);
     }
 
     @Override
     public void sendMessage(GetFileIDsProgressResponse content) {
         sendMessage(content.getTo(), content.getReplyTo(), content.getBitRepositoryCollectionID(),
                     content.getCorrelationID(), content);
     }
 
     @Override
     public void sendMessage(GetFileRequest content) {
         sendMessage(content.getTo(), content.getReplyTo(), content.getBitRepositoryCollectionID(),
                     content.getCorrelationID(), content);
     }
 
     @Override
     public void sendMessage(GetFileProgressResponse content) {
         sendMessage(content.getTo(), content.getReplyTo(), content.getBitRepositoryCollectionID(),
                     content.getCorrelationID(), content);
     }
 
     @Override
     public void sendMessage(GetStatusRequest content) {
         sendMessage(content.getTo(), content.getReplyTo(), content.getBitRepositoryCollectionID(),
                 content.getCorrelationID(), content);
     }
 
     @Override
     public void sendMessage(GetStatusProgressResponse content) {
         sendMessage(content.getTo(), content.getReplyTo(), content.getBitRepositoryCollectionID(),
                 content.getCorrelationID(), content);
     }
 
     @Override
     public void sendMessage(GetStatusFinalResponse content) {
         sendMessage(content.getTo(), content.getReplyTo(), content.getBitRepositoryCollectionID(),
                 content.getCorrelationID(), content);
     }
 
     @Override
     public void sendMessage(IdentifyPillarsForGetChecksumsResponse content) {
         sendMessage(content.getTo(), content.getReplyTo(), content.getBitRepositoryCollectionID(),
                     content.getCorrelationID(), content);
     }
 
     @Override
     public void sendMessage(IdentifyPillarsForGetChecksumsRequest content) {
         sendMessage(content.getTo(), content.getReplyTo(), content.getBitRepositoryCollectionID(),
                     content.getCorrelationID(), content);
     }
 
     @Override
     public void sendMessage(IdentifyPillarsForGetFileIDsRequest content) {
         sendMessage(content.getTo(), content.getReplyTo(), content.getBitRepositoryCollectionID(),
                     content.getCorrelationID(), content);
     }
 
     @Override
     public void sendMessage(IdentifyPillarsForGetFileIDsResponse content) {
         sendMessage(content.getTo(), content.getReplyTo(), content.getBitRepositoryCollectionID(),
                     content.getCorrelationID(), content);
     }
 
     @Override
     public void sendMessage(IdentifyPillarsForGetFileRequest content) {
         sendMessage(content.getTo(), content.getReplyTo(), content.getBitRepositoryCollectionID(),
                     content.getCorrelationID(), content);
     }
 
     @Override
     public void sendMessage(IdentifyPillarsForGetFileResponse content) {
         sendMessage(content.getTo(), content.getReplyTo(), content.getBitRepositoryCollectionID(),
                     content.getCorrelationID(), content);
     }
 
     @Override
     public void sendMessage(IdentifyPillarsForPutFileResponse content) {
         sendMessage(content.getTo(), content.getReplyTo(), content.getBitRepositoryCollectionID(),
                     content.getCorrelationID(), content);
     }
 
     @Override
     public void sendMessage(IdentifyPillarsForPutFileRequest content) {
         sendMessage(content.getTo(), content.getReplyTo(), content.getBitRepositoryCollectionID(),
                     content.getCorrelationID(), content);
     }
 
     @Override
     public void sendMessage(PutFileFinalResponse content) {
         sendMessage(content.getTo(), content.getReplyTo(), content.getBitRepositoryCollectionID(),
                     content.getCorrelationID(), content);
     }
 
     @Override
     public void sendMessage(PutFileRequest content) {
         sendMessage(content.getTo(), content.getReplyTo(), content.getBitRepositoryCollectionID(),
                     content.getCorrelationID(), content);
     }
 
     @Override
     public void sendMessage(PutFileProgressResponse content) {
         sendMessage(content.getTo(), content.getReplyTo(), content.getBitRepositoryCollectionID(),
                     content.getCorrelationID(), content);
     }
 
     /**
      * Send a message using ActiveMQ.
      *
      * @param destinationID Name of destination to send message to.
      * @param replyTo       The queue to reply to.
      * @param collectionID  The collection ID of the message.
      * @param correlationID The correlation ID of the message.
      * @param content       JAXB-serializable object to send.
      */
     private void sendMessage(String destinationID, String replyTo, String collectionID, String correlationID,
                              Object content) {
         try {
             String xmlContent = JaxbHelper.serializeToXml(content);
             log.debug("The following message is sent to the destination '" + destinationID + "'" + " on message-bus '"
                               + configuration.getName() + "': \n{}", xmlContent);
             MessageProducer producer = addTopicMessageProducer(destinationID);
             producer.setDeliveryMode(DeliveryMode.PERSISTENT);
 
             Message msg = session.createTextMessage(xmlContent);
             msg.setStringProperty(MESSAGE_TYPE_KEY, content.getClass().getSimpleName());
             msg.setStringProperty(COLLECTION_ID_KEY, collectionID);
             msg.setJMSCorrelationID(correlationID);
             msg.setJMSReplyTo(getTopic(replyTo));
 
             producer.send(msg);
             session.commit();
         } catch (JMSException e) {
             throw new CoordinationLayerException("Could not send message", e);
         } catch (JAXBException e) {
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
             Topic topic = getTopic(destinationID);
             MessageConsumer consumer;
             try {
                 consumer = session.createConsumer(topic);
             } catch (JMSException e) {
                 throw new CoordinationLayerException("Could not create message consumer for topic '" + topic + '"', e);
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
     private MessageProducer addTopicMessageProducer(String destination) {
         Topic topic = getTopic(destination);
         MessageProducer producer;
         try {
             producer = session.createProducer(topic);
         } catch (JMSException e) {
             throw new CoordinationLayerException(
                     "Could not create message producer for destination '" + destination + "'", e);
         }
         return producer;
     }
 
     /**
      * Given a topic ID, retrieve the topic object.
      *
      * @param topicID ID of the topic.
      * @return The object representing that topic. Will always return the same topic object for the same topic ID.
      */
     private Topic getTopic(String topicID) {
         Topic topic = topics.get(topicID);
         if (topic == null) {
             try {
                 // TODO: According to javadoc, topics should be looked up in another fashion.
                 // See http://download.oracle.com/javaee/6/api/javax/jms/Session.html#createTopic(java.lang.String)
                 topic = session.createTopic(topicID);
             } catch (JMSException e) {
                 throw new CoordinationLayerException("Could not create topic '" + topicID + "'", e);
             }
             topics.put(topicID, topic);
         }
         return topic;
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
     private static class ActiveMQMessageListener implements javax.jms.MessageListener {
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
          * @param message The message received.
          */
         @Override
         public void onMessage(final Message message) {
             String type = null;
             String text = null;
            String schemaLocation = "BitRepositoryMessages.xsd";
             JaxbHelper jaxbHelper = new JaxbHelper(schemaLocation); 
             Object content;
             try {
                 type = message.getStringProperty(MESSAGE_TYPE_KEY);
                 text = ((TextMessage) message).getText();
                 jaxbHelper.validate(new ByteArrayInputStream(text.getBytes()));
                 content = jaxbHelper.loadXml(Class.forName("org.bitrepository.bitrepositorymessages." + type),
                                              new ByteArrayInputStream(text.getBytes()));
                 log.debug("Received message: " + text);
                 if(content.getClass().equals(Alarm.class)){
                 	listener.onMessage((Alarm) content);
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
                 log.error("Received message of unknown type '" + type + "'\n{}", text);
             } catch (SAXException e) {
                 log.error("Error validating message", e);
             } catch (Exception e) {
                 log.error("Error handling message. Received type was '" + type + "'.\n{}", text, e);
             }
 
         }
     }
 }
