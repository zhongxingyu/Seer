 /*
     jBilling - The Enterprise Open Source Billing System
     Copyright (C) 2003-2008 Enterprise jBilling Software Ltd. and Emiliano Conde
 
     This file is part of jbilling.
 
     jbilling is free software: you can redistribute it and/or modify
     it under the terms of the GNU General Public License as published by
     the Free Software Foundation, either version 3 of the License, or
     (at your option) any later version.
 
     jbilling is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
     GNU General Public License for more details.
 
     You should have received a copy of the GNU General Public License
     along with jbilling.  If not, see <http://www.gnu.org/licenses/>.
 */
 
 package com.sapienter.jbilling.server.provisioning;
 
 import java.io.PrintWriter;
 import java.io.StringWriter;
 import java.util.Collections;
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import javax.jms.JMSException;
 import javax.jms.MapMessage;
 import javax.jms.Message;
 import javax.jms.MessageListener;
 import javax.jms.Topic;
 import javax.jms.TopicConnection;
 import javax.jms.TopicConnectionFactory;
 import javax.jms.TopicPublisher;
 import javax.jms.TopicSession;
 import javax.naming.InitialContext;
 import javax.naming.NamingException;
 
 import org.apache.log4j.Logger;
 
 import com.sapienter.jbilling.common.SessionInternalError;
 import com.sapienter.jbilling.server.pluggableTask.admin.PluggableTaskException;
 import com.sapienter.jbilling.server.pluggableTask.admin.PluggableTaskManager;
 import com.sapienter.jbilling.server.provisioning.config.Command;
 import com.sapienter.jbilling.server.provisioning.config.Field;
 import com.sapienter.jbilling.server.provisioning.config.Processor;
 import com.sapienter.jbilling.server.provisioning.config.Provisioning;
 import com.sapienter.jbilling.server.provisioning.config.Request;
 import com.sapienter.jbilling.server.provisioning.task.IExternalProvisioning;
 import com.sapienter.jbilling.server.util.Constants;
 import com.sapienter.jbilling.server.util.Context;
 
 /**
  * Logic for external provisioning module. Receives a command from the
  * commands rules task via JMS. The configuration file 
  * jbilling-provisioning.xml is used to map this command to command
  * strings for specific external provisioning processors. Publishes 
  * results in a JMS topic.
  */
 public class ExternalProvisioning {
     private static final Logger LOG = Logger.getLogger(
             ExternalProvisioning.class);
 
     private static final String TOPIC_NAME = 
             "provisioning_commands_reply_topic";
 
     private TopicConnection conn = null;
     private TopicPublisher topic = null;
     private TopicSession session = null;
 
     private MapMessage message;
 
     /**
      * Receives and processes a command message from the command rules task.
      * This method is called through the ProvisioningProcessSessionBean
      * so that it runs in a transaction. ExternalProvisioningMDB is
      * the class that actually receives the message.
      */
     public void onMessage(Message myMessage) {
         try {
             setupTopic();
             message = (MapMessage) myMessage;
 
             Provisioning config = (Provisioning) Context.getBean(
                     Context.Name.PROVISIONING);
 
             List<Command> commandsConfig = config.getCommands();
             String command = message.getStringProperty("command");
 
             // find command config
             for(Command commandConfig : commandsConfig) {
                 if (command.equals(commandConfig.getId())) {
                     LOG.debug("Found a command configuration");
                     processCommand(commandConfig);
                     break; // no more configurations for this command?
                 }
             }
         } catch (Exception e) {
             throw new SessionInternalError(e);
         } finally {
             closeTopic();
         }
     }
 
     /**
      * Processes a command according to the given configuration.
      */
     private void processCommand(Command config) 
             throws JMSException, PluggableTaskException {
         // process fields
         List<Field> fieldConfig = config.getFields();
         Map<String, String> fields = new HashMap<String, String>();
 
         for (Field field : fieldConfig) {
             String value = message.getStringProperty(field.getName());
             if (value == null) {
                 value = field.getDefaultValue();
             }
             fields.put(field.getName(), value);
         }
 
         // call each configured processor
         for (Processor processor : config.getProcessors()) {
             PluggableTaskManager<IExternalProvisioning> taskManager = new
                     PluggableTaskManager<IExternalProvisioning>(
                     message.getIntProperty("entityId"), 
                     Constants.PLUGGABLE_TASK_EXTERNAL_PROVISIONING);
             IExternalProvisioning task = taskManager.getNextClass();
 
             while (task != null) {
                 if (task.getId().equals(processor.getId())) {
                     callProcessor(task, processor, fields, 
                             message.getStringProperty("id"));
                     break;
                 }
                 task = taskManager.getNextClass();
             }
 
             if (task == null) {
                 throw new SessionInternalError("Couldn't find external " +
                         "provisioining task with id: " + processor.getId());
             }
         }
     }
 
     /**
      * Processes each request to the given external provisioning task
      * as specified by the processor configuration. 
      */
     private void callProcessor(IExternalProvisioning task, 
             Processor processor, Map<String, String> fields, String id) 
             throws JMSException {
         List<Request> requests = processor.getRequests();
         Collections.sort(requests); // sort by order
 
         for (Request request : requests) {
             LOG.debug("Submit string pattern: " + request.getSubmit());
 
             // insert fields into submit string
             StringBuilder submit = new StringBuilder(request.getSubmit());
             boolean keepLooking = true;
             while (keepLooking) {
                 int barStartIndex = submit.indexOf("|");
                 int barEndIndex = submit.indexOf("|", barStartIndex + 1);
                 if (barStartIndex == -1) {
                     keepLooking = false;
                 } else if (barEndIndex == -1) {
                     throw new SessionInternalError("Mismatched '|' in submit " +
                             "string. Index: " + barStartIndex);
                 } else {
                     String fieldName = submit.substring(barStartIndex + 1, 
                             barEndIndex);
                     String fieldValue = fields.get(fieldName);
                     LOG.debug("Replacing field name '" + fieldName + 
                             "' with value '" + fieldValue + "'");
 
                     submit.replace(barStartIndex, barEndIndex + 1, fieldValue);
                 }
             }
             String submitString = submit.toString();
             LOG.debug("Command string: " + submitString);
 
             // call external provisioning processor task
             String error = null;
             Map<String, Object> result = null;
             try {
                 result = task.sendRequest(id, submitString);
             } catch (Exception e) {
                 StringWriter sw = new StringWriter();
                 PrintWriter pw = new PrintWriter(sw);
                 e.printStackTrace(pw);
                 pw.close();
                 LOG.error("External provisioning processor error: " + 
                         e.getMessage() + "\n" + sw.toString());
 
                 error = e.toString();
             }
 
             // post message if required
             if (request.getPostResult()) {
                 postResults(result, error);
             }
 
             // only continue with other requests if correct result
             String continueOnType = request.getContinueOnType();
            if (continueOnType != null && !result.get("result").equals(
                        continueOnType)) {
                 LOG.debug("Skipping other results.");
                 break;
             }
         }
     }
 
     /**
      * Posts results of external provisioning processing tasks.
      */
     private void postResults(Map<String, Object> results, String error) 
             throws JMSException {
         MapMessage replyMessage = session.createMapMessage();
 
         // add the original properties (names prefixed with 'in_')
         Enumeration originalPropNames = message.getPropertyNames();
         while (originalPropNames.hasMoreElements()) {
             String propName = (String) originalPropNames.nextElement();
             Object propValue = message.getObjectProperty(propName);
             replyMessage.setObjectProperty("in_" + propName, propValue);
         }
 
         if (error == null) {
             // add the properties returned by the processor
             Set<Map.Entry<String, Object>> entrySet = results.entrySet();
             for (Map.Entry<String, Object> entry : entrySet) {
                 replyMessage.setObjectProperty("out_" + entry.getKey(), 
                         entry.getValue());
             }
         } else {
             // there was an error
             replyMessage.setStringProperty("out_result", "unavailable");
             replyMessage.setStringProperty("exception", error);
         }
 
         // send the message
         topic.publish(replyMessage);        
     }
 
     /**
      * Sets up the JMS topic.
      */
     public void setupTopic() throws JMSException, NamingException {
         InitialContext iniCtx = new InitialContext();
         TopicConnectionFactory tcf = (TopicConnectionFactory) 
                 iniCtx.lookup("ConnectionFactory");
 
         conn = tcf.createTopicConnection();
         session = conn.createTopicSession(false, TopicSession.AUTO_ACKNOWLEDGE);
         conn.start();
         topic = session.createPublisher((Topic) iniCtx.lookup("topic/" + 
                 TOPIC_NAME));
     }
 
     /**
      * Closes the JMS topic.
      */
     public void closeTopic() {
         try {
             if (topic != null) {
                 topic.close();
             }
             if (session != null) {
                 session.close();
             }
             if (conn != null) {
                 conn.close();
             }
         } catch(JMSException jmse) { 
         }
     }
 }
