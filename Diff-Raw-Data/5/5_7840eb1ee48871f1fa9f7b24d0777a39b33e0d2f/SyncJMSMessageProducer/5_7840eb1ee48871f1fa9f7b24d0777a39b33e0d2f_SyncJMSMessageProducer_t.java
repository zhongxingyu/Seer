 /*
  * Licensed to the Sakai Foundation (SF) under one
  * or more contributor license agreements. See the NOTICE file
  * distributed with this work for additional information
  * regarding copyright ownership. The SF licenses this file
  * to you under the Apache License, Version 2.0 (the
  * "License"); you may not use this file except in compliance
  * with the License. You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an
  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  * KIND, either express or implied. See the License for the
  * specific language governing permissions and limitations under the License.
  */
 package org.sakaiproject.nakamura.grouper.event;
 
 import java.util.Map;
 import java.util.regex.Pattern;
 
 import javax.jms.Connection;
 import javax.jms.DeliveryMode;
 import javax.jms.JMSException;
 import javax.jms.Message;
 import javax.jms.MessageProducer;
 import javax.jms.Queue;
 import javax.jms.Session;
 
 import org.apache.felix.scr.annotations.Component;
 import org.apache.felix.scr.annotations.Modified;
 import org.apache.felix.scr.annotations.Properties;
 import org.apache.felix.scr.annotations.Property;
 import org.apache.felix.scr.annotations.Reference;
 import org.apache.felix.scr.annotations.Service;
 import org.apache.sling.commons.osgi.OsgiUtil;
 import org.osgi.service.event.Event;
 import org.osgi.service.event.EventAdmin;
 import org.osgi.service.event.EventConstants;
 import org.osgi.service.event.EventHandler;
 import org.sakaiproject.nakamura.api.activemq.ConnectionFactoryService;
 import org.sakaiproject.nakamura.api.lite.StoreListener;
 import org.sakaiproject.nakamura.api.lite.authorizable.Authorizable;
 import org.sakaiproject.nakamura.grouper.api.GrouperConfiguration;
 import org.sakaiproject.nakamura.util.osgi.EventUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
import com.google.common.collect.ImmutableSet;

 /**
  * Capture {@link Authorizable} events and put them on a special Queue to be processed.
  * 
  * When Groups are created or updated we are notified of an {@link Event} via the OSGi
  * {@link EventAdmin} service. We then create a {@link Message} and place it on a {@link Queue}. 
  * 
  * The {@link GrouperJMSMessageConsumer} will receive those messages and acknowledge them as they 
  * are successfully processed.
  */
 @Service
 @Component(immediate = true, metatype=true)
 @Properties(value = { 
 		@Property(name = EventConstants.EVENT_TOPIC, 
 				value = {
 				"org/sakaiproject/nakamura/lite/authorizables/ADDED",
 				"org/sakaiproject/nakamura/lite/authorizables/UPDATED",
 				"org/sakaiproject/nakamura/lite/authorizables/DELETE"
 		})
 })
 public class SyncJMSMessageProducer implements EventHandler {
 
 	private static Logger log = LoggerFactory.getLogger(SyncJMSMessageProducer.class);
 
 	protected static final String QUEUE_NAME = "org/sakaiproject/nakamura/grouper/sync";
 
 	@Reference
 	protected ConnectionFactoryService connFactoryService;
 
 	@Reference
 	protected GrouperConfiguration grouperConfiguration;
 
 	@Property(boolValue=false)
 	protected static final String TRANSACTED_NAME = "transacted";
 	protected boolean transacted;
 
 	@Modified
 	public void updated(Map<String,Object> props){
 		transacted = OsgiUtil.toBoolean(TRANSACTED_NAME, false);
 	}
 
 	/**
 	 * @{inheritDoc}
 	 * Respond to OSGi events by pushing them onto a JMS queue.
 	 */
 	@Override
 	public void handleEvent(Event event) {
 		try {
 			if (ignoreEvent(event) == false){
 				sendMessage(event);
 			}
 		} 
 		catch (JMSException e) {
 			log.error("There was an error sending this event to the JMS queue", e);
 		}
 	}
 
 	/**
 	 * Convert an OSGi {@link Event} into a JMS {@link Message} and post it on a {@link Queue}.
 	 * @param event the event we're sending
 	 * @throws JMSException
 	 */
 	private void sendMessage(Event event) throws JMSException {
 		Connection senderConnection = connFactoryService.getDefaultPooledConnectionFactory().createConnection();
 		Session senderSession = senderConnection.createSession(transacted, Session.CLIENT_ACKNOWLEDGE);
 		Queue squeue = senderSession.createQueue(QUEUE_NAME);
 		MessageProducer producer = senderSession.createProducer(squeue);
 
 		Message msg = senderSession.createMessage();
 		msg.setJMSDeliveryMode(DeliveryMode.PERSISTENT);
 		msg.setJMSType(event.getTopic());
 		copyEventToMessage(event, msg);
 		producer.send(msg);
 
 		log.info("Sent: {} {} : messageId {}", new Object[] { event.getTopic(), event.getProperty("path"), msg.getJMSMessageID()});
 		log.debug("{} : {}", msg.getJMSMessageID(), msg);
 
 		try {
 			senderSession.close();
 		}
 		finally {
 			senderSession = null;
 		}
 		try {
 			senderConnection.close();
 		}
 		finally {
 			senderConnection = null;
 		}
 	}
 
 	/**
 	 * This method indicates whether or not we should post a {@link Message} for 
 	 * the {@link Event}. There's some specific messages on the interesting topics
 	 * that we don't want to handle for one reason or another.
 	 * 
 	 * @param event the OSGi {@link Event} we're considering.
 	 * @return whether or not to ignore this event.
 	 */
 	private boolean ignoreEvent(Event event){
 
 		// Ignore events that were posted by the Grouper system to SakaiOAE. 
 		// We don't want to wind up with a feedback loop between SakaiOAE and Grouper.
 		String ignoreUser = grouperConfiguration.getIgnoredUserId();
 		String eventCausedBy = (String)event.getProperty(StoreListener.USERID_PROPERTY); 
 		if ( (ignoreUser != null && eventCausedBy != null) && 
 			 (ignoreUser.equals(eventCausedBy))) {
 				return true;
 		}
 
 		// Ignore non-group events
 		// type must be g or group
 		String type = (String)event.getProperty("type");
		if (! ImmutableSet.of("g", "group").contains(type)){
 			return true;
 		}
 
 		// Ignore op=acl events
 		String op = (String)event.getProperty("op");
 		if (op != null && op.equals("acl")){
 			return true;
 		}
 
 		for (String p: grouperConfiguration.getIgnoredGroups()){
 			// The path is the authorizableId of the group
 			if (Pattern.matches(p, (String)event.getProperty(StoreListener.PATH_PROPERTY))){
 				return true;
 			}
 		}
 
 		return false;
 	}
 
 	/**
 	 * Populate a Message's properties with those from an Event.
 	 *
 	 * @see org.sakaiproject.nakamura.events.OsgiJmsBridge
 	 *
 	 * @param event the source
 	 * @param message the destination
 	 * @throws JMSException
 	 */
 	public static void copyEventToMessage(Event event, Message message) throws JMSException{
 		for (String name : event.getPropertyNames()) {
 			Object val = event.getProperty(name);
 			if (val != null){
 				message.setObjectProperty(name, EventUtils.cleanProperty(val));
 			}
 		}
 	}
 }
