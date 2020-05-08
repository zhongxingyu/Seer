 /* SVN FILE: $Id: MessagingCore.java 5340 2012-09-27 14:48:52Z jvuccolo $ */
 /**
 * 
  * Copyright 2012 The Pennsylvania State University
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *   http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  *
 * @package edu.psu.iam.cpr.core.messaging
 * @author $Author: jvuccolo $
 * @version $Rev: 5340 $
 * @lastrevision $Date: 2012-09-27 10:48:52 -0400 (Thu, 27 Sep 2012) $
 */
 package edu.psu.iam.cpr.core.messaging;
 
 import java.util.Iterator;
 import java.util.List;
 import java.util.Properties;
 
 import javax.jms.Connection;
 import javax.jms.ConnectionFactory;
 import javax.jms.DeliveryMode;
 import javax.jms.Destination;
 import javax.jms.JMSException;
 import javax.jms.Message;
 import javax.jms.MessageConsumer;
 import javax.jms.MessageProducer;
 import javax.jms.Queue;
 import javax.jms.Session;
 import javax.jms.TextMessage;
 import org.apache.activemq.ActiveMQConnectionFactory;
 import org.apache.log4j.Logger;
 import org.json.JSONException;
 
 import edu.psu.iam.cpr.core.database.Database;
 import edu.psu.iam.cpr.core.database.beans.MessageLog;
 import edu.psu.iam.cpr.core.database.beans.VSpNotification;
 import edu.psu.iam.cpr.core.database.tables.MessageLogHistoryTable;
 import edu.psu.iam.cpr.core.database.tables.MessageLogTable;
 import edu.psu.iam.cpr.core.database.types.CprPropertyName;
 import edu.psu.iam.cpr.core.database.types.MessageKeyName;
 import edu.psu.iam.cpr.core.error.CprException;
 import edu.psu.iam.cpr.core.error.ReturnType;
 import edu.psu.iam.cpr.core.util.CprProperties;
 
 /**
  * MessagingCore
  * Copyright 2012 The Pennsylvania State University
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *   http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  * 
  * @package edu.psu.iam.cpr.core.error
  * @author $Author: jvuccolo $
  * @version $Rev: 5340 $
  * @lastrevision $Date: 2012-09-27 10:48:52 -0400 (Thu, 27 Sep 2012) $
  */
 
 /**
  * Contains the JMS messaging interface for the web services to be able to initialize connections and queue, send messages and receive messages
  *
  */
 public class MessagingCore {
 	
 
 	/** Instance of logger */
 	private static final Logger LOG4J_LOGGER = Logger.getLogger(MessagingCore.class);
 
 	private static final int BUFFER_SIZE = 2048;
 	
 	/** Array of message queues */
 	private List<ServiceProvisionerQueue> msgQueues = null;
 
 	/** Connection factory */
 	private ConnectionFactory jmsConnectionFactory = null;
 	
 	/** Connection */
 	private Connection jmsConnection = null;
 	
 	/** JMS Session */
 	private Session jmsSession = null;
 	
 	/**
 	 * Constructor using the default connection factory
 	 * @throws CprException
 	 */
 	public MessagingCore() throws CprException {
 		super();
 	}
 	
 	/**
 	 * Constructor.
 	 * @param db contains a database object which holds an open connection.
 	 * @param serviceName contains the service name that is sending/receiving messages.
 	 */
 	public MessagingCore(Database db, String serviceName) {
 		
 	    // Get the list of queues for the service.  If no queues were found, then just return.
 		msgQueues = ServiceProvisionerQueue.getServiceProvisionerQueues(db, serviceName);
 	}
 	
 	
 	/**
 	 * Initializes the jms context, broker and connection
 	 * @throws JMSException 
 	 */
 	public void initializeMessaging() throws JMSException {
 		final Properties props = CprProperties.INSTANCE.getProperties();
 		
 		// Get a connection and start a session.
 		jmsConnectionFactory = new ActiveMQConnectionFactory(props.getProperty(CprPropertyName.CPR_JMS_BROKER.toString()));
 		jmsConnection = jmsConnectionFactory.createConnection(props.getProperty(CprPropertyName.CPR_JMS_USERID.toString()), 
 				props.getProperty(CprPropertyName.CPR_JMS_PASSWORD.toString()));
 		jmsConnection.start();
 		jmsSession = jmsConnection.createSession(false, Session.AUTO_ACKNOWLEDGE);
 	}
 	
 	/**
 	 * @return the msgQueues
 	 */
 	public List<ServiceProvisionerQueue> getMsgQueues() {
 		return msgQueues;
 	}
 
 	/**
 	 * @param msgQueues the msgQueues to set
 	 */
 	public void setMsgQueues(List<ServiceProvisionerQueue> msgQueues) {
 		this.msgQueues = msgQueues;
 	}
 
 	/**
 	 * This routine is used to clean up messaging.
 	 */
 	public void closeMessaging() {
 		
         // Close the session and connection resources.
 		try {
 			jmsSession.close();
 		}
 		catch (Exception e) {
 		}
 		
 		try {
 			jmsConnection.close();
 		}
 		catch (Exception e) {
 		}
 		
 		jmsSession = null;
 		jmsConnection = null;
 	}
 	
 	/**
 	 * 
 	 * @param testQueue
 	 * @return message received from the given queue, null if no message received
 	 * @throws JMSException 
 	 */
     public String receiveMessageNoWait(Queue testQueue) throws JMSException {
 
     	//Create a message consumer.
     	MessageConsumer myMsgConsumer = jmsSession.createConsumer(testQueue);
 
     	//Receive a message from the queue.
     	final Message msg = myMsgConsumer.receiveNoWait();
 
     	//Retrieve the contents of the message.
     	if (msg == null) {
     		return null;
     	}
 
     	if (msg instanceof TextMessage) {
     		String txtMsgString = ((TextMessage) msg).getText();
     		if (txtMsgString == null) {
     			return msg.toString();
     		}
     		return txtMsgString;
     	}
 
     	return msg.toString();
     }
     
 	/**
 	 * 
 	 * @param testQueue
 	 * @return message received from the given queue, wait if no message available
 	 * @throws JMSException 
 	 */
     public String receiveMessageWait(Queue testQueue) throws JMSException {
 
     	//Create a message consumer.
     	final MessageConsumer myMsgConsumer = jmsSession.createConsumer(testQueue);
 
     	//Receive a message from the queue. Will block waiting for message
     	final Message msg = myMsgConsumer.receive();
 
     	//Retrieve the contents of the message.
     	if (msg == null) {
     		return null;
     	}
     	if (msg instanceof TextMessage) {
     		final String txtMsgString = ((TextMessage) msg).getText();
     		if (txtMsgString == null) {
     			return msg.toString();
     		}
     		return txtMsgString;
     	}
 
     	return msg.toString();
     }
     
     /**
      * This function is used to record message delivery failures for a message event.
      * @param db contains the database connection.
      * @param msg contains the JSON message.
      */
     public void recordFailures(Database db, JsonMessage msg) {
 
     	// Loop through queue list
     	final Iterator<ServiceProvisionerQueue> queueIter = msgQueues.iterator();
     	while (queueIter.hasNext()) {
 
     		try {
 
     			ServiceProvisionerQueue currentQueue = queueIter.next();
     			VSpNotification view = currentQueue.getSpNotificationView();
     			if (view != null) {
     				
     				// Create a message log to indicate that there has been a failure.
     				MessageLogTable messageLogTable = new MessageLogTable(view.getWebServiceKey(), view.getServiceProvisionerKey(), 
     						msg.getJsonObject().toString(), msg.getRequestedBy());
     				logMessageDelivery(messageLogTable, "FAILURE");
     				messageLogTable.addMessageLog(db);
     			}
     		}
     		catch (Exception e) {
 
     		}
     	}
     }
     
 	/**
 	 * Send a message.
      * @param db contains a database object which holds an open connection.
      * @param msg contains the JSON message to be sent to the service provisioners.
 	 * @throws CprException will be thrown for any CPR related problems.
 	 * @throws JMSException  will be thrown if there are any messaging issues.
 	 * @throws JSONException  will be thrown if there are any JSON problems.
      */
     public void sendMessage(Database db, JsonMessage msg) throws CprException, JMSException, JSONException {
     	
     	final String replyToQueue = CprProperties.INSTANCE.getProperties().getProperty(CprPropertyName.CPR_JMS_REPLYTO.toString());
 		MessageProducer msgSender = null;
 
     	if (msg == null || msg.getJsonObject().toString().length() == 0) {
     		throw new CprException(ReturnType.MESSAGE_CREATION_EXCEPTION);
     	}
 
     	try {
     		//Create the message to send to the queues 	
     		TextMessage myTextMsg = null;
 
     		myTextMsg = jmsSession.createTextMessage();
 
     		// Loop through queue list
     		final Iterator<ServiceProvisionerQueue> queueIter = msgQueues.iterator();
     		while (queueIter.hasNext()) {
 
     			ServiceProvisionerQueue currentQueue = queueIter.next();
 
     			MessageLogTable messageLogTable = null;
     			VSpNotification view = currentQueue.getSpNotificationView();
     			if (view == null) {
     				throw new CprException(ReturnType.NOT_AUTHORIZED_EXCEPTION, currentQueue.toString());
     			}
 
     			messageLogTable = new MessageLogTable(view.getWebServiceKey(), view.getServiceProvisionerKey(), 
     					msg.getJsonObject().toString(), msg.getRequestedBy());
     			messageLogTable.addMessageLog(db);
 
     			MessageLogHistoryTable messageLogHistoryTable = new MessageLogHistoryTable(messageLogTable.getMessageLogBean());
     			messageLogHistoryTable.addMessageLogHistory(db);
 
     			// once the log is started, the messageLogId is set, add it to the msg to be sent
     			msg.setValue(MessageKeyName.MESSAGE_LOG_ID, messageLogTable.getMessageLogBean().getMessageLogKey());
 
     			// Add in the JSON information.
     			myTextMsg.setText(msg.getJsonObject().toString());
     			myTextMsg.setJMSReplyTo(jmsSession.createQueue(replyToQueue));
     			myTextMsg.setJMSCorrelationID(messageLogHistoryTable.getMessageLogHistoryBean().getMessageLogHistoryKey().toString());
 
     			// Send the message.
     			Destination destination = jmsSession.createQueue(currentQueue.getSpNotificationView().getServiceProvisionerQueue());
     			msgSender = jmsSession.createProducer(destination);
     			msgSender.setDeliveryMode(DeliveryMode.PERSISTENT);
     			msgSender.send(myTextMsg);
     			msgSender.close();
 
     			// once the message is sent, add an entry in the message log history table for this specific send
     			messageLogTable.updateMessageLog(db, "Y", 1L);
     			messageLogHistoryTable.updateMessageLogHistory(db, myTextMsg.getJMSMessageID());
 
     			// Log the successful message delivery to the log4j component.
     			logMessageDelivery(messageLogTable, "SUCCESS");
     		}  	
     	}
     	finally {
     		try {
    			if (msgSender != null) {
    				msgSender.close();
    			}
     		} 
     		catch (JMSException e) {
     		}
     	}
     }
     
     /**
      * This routine is used to record the status of a message delivery.  It is passed in
      * the information about the message and a status as to whether the message was delivered
      * successfully or not.
      * @param messageLogTable contains the message log table information, which is the data about the message.
      * @param status contains the status of the delivery.
      */
     private void logMessageDelivery(MessageLogTable messageLogTable, String status) {
 
     	MessageLog bean = messageLogTable.getMessageLogBean();
 
     	StringBuilder sb = new StringBuilder(BUFFER_SIZE);
     	sb.append("Message Delivery ");
     	sb.append(status);
     	sb.append(": Web Service Key=");
     	sb.append(bean.getWebServiceKey());
     	sb.append(", Service Provisioner Key=");
     	sb.append(bean.getServiceProvisionerKey());
     	sb.append(", JSON Message Text=");
     	sb.append(bean.getMessageSent());
 
     	LOG4J_LOGGER.info(sb.toString());		
     }
 
 }
