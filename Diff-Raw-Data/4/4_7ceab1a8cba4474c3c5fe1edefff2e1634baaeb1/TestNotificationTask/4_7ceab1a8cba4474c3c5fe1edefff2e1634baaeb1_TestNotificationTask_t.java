 /*
  * JBILLING CONFIDENTIAL
  * _____________________
  *
  * [2003] - [2012] Enterprise jBilling Software Ltd.
  * All Rights Reserved.
  *
  * NOTICE:  All information contained herein is, and remains
  * the property of Enterprise jBilling Software.
  * The intellectual and technical concepts contained
  * herein are proprietary to Enterprise jBilling Software
  * and are protected by trade secret or copyright law.
  * Dissemination of this information or reproduction of this material
  * is strictly forbidden.
  */
 package com.sapienter.jbilling.server.notification.task;
 
import java.io.File;
 import java.io.FileWriter;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.List;
 
 import org.apache.log4j.Logger;
 
 import com.sapienter.jbilling.common.Util;
 import com.sapienter.jbilling.server.notification.MessageDTO;
 import com.sapienter.jbilling.server.pluggableTask.NotificationTask;
 import com.sapienter.jbilling.server.pluggableTask.PluggableTask;
 import com.sapienter.jbilling.server.pluggableTask.TaskException;
 import com.sapienter.jbilling.server.pluggableTask.admin.ParameterDescription;
 import com.sapienter.jbilling.server.user.ContactBL;
 import com.sapienter.jbilling.server.user.ContactDTOEx;
 import com.sapienter.jbilling.server.user.db.UserDTO;
 
 public class TestNotificationTask extends PluggableTask implements NotificationTask {
     
 	public static final ParameterDescription PARAMETER_FROM = 
 		new ParameterDescription("from", false, ParameterDescription.Type.STR);
     public static final Logger LOG = Logger.getLogger(TestNotificationTask.class);
     
     //initializer for pluggable params
     { 
     	descriptions.add(PARAMETER_FROM);
     }
 
     
 
     public void deliver(UserDTO user, MessageDTO sections)
             throws TaskException {
         String directory = Util.getSysProp("base_dir");
         try {
            FileWriter writer = new FileWriter(directory + File.separator + "emails_sent.txt", true);
             
             // find the address
             ContactBL contact = new ContactBL();
             List<ContactDTOEx> emails = contact.getAll(user.getUserId());
             
             // find the from
             String from = (String) parameters.get(PARAMETER_FROM.getName());
             if (from == null || from.length() == 0) {
                 from = Util.getSysProp("email_from");
             }
             
             String email = emails == null ? "No email" : emails.size() == 0 ? "No email" : emails.get(0).getEmail();
             writer.write("Date: " + Calendar.getInstance().getTime() + "\n");
             writer.write("To: " + email + "\n");
             writer.write("From: " + from + "\n");
             writer.write("Subject: " + sections.getContent()[0].getContent() + "\n");
             writer.write("Body: " + sections.getContent()[1].getContent() + "\n");
             writer.write("Attachement: " + sections.getAttachmentFile() + "\n");
             writer.write("        ----------------------        \n");
             
             writer.close();
             
             LOG.debug("Sent test notification to " + user.getUserId());
         } catch (Exception e) {
             LOG.error("Error sending test notification:" + e.getMessage(),e);
             throw new TaskException(e);
         }
 
     }
 
     public int getSections() {
         return 2;
     }
 }
