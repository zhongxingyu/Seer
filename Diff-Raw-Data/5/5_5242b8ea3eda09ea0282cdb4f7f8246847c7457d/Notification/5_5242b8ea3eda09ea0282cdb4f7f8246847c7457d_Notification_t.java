 package tasktracker.model.elements;
 
 /**
  * TaskTracker
  * 
  * Copyright 2012 Jeanine Bonot, Michael Dardis, Katherine Jasniewski,
  * Jason Morawski
  *
  * Licensed under the Apache License, Version 2.0 (the "License"); you may 
  * not use this file except in compliance with the License. You may obtain
  * a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software distributed
  * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR 
  * CONDITIONS OF ANY KIND, either express or implied. See the License for the 
  * specific language governing permissions and limitations under the License.
  */
 
 import java.text.SimpleDateFormat;
 import java.util.*;
 
 import android.content.ContentValues;
 
 /**
  * NotificationElement class
  * 
  * A class representing a notification. A notification is sent to a user
  * regarding a given task. Notifications can be sent to inform a creator that
  * their task has been fulfilled, to inform members that a task has been edited
  * or deleted, or to inform a user that they have become a member of a task.
  * 
  * @author Jeanine Bonot
  * 
  */
 public final class Notification {
 
 	/** Indicates the type of notification */
 	public enum Type {
 		// Task has been fulfilled.
 		FulfillmentReport,
 
 		// Task has been deleted.
 		InformDelete,
 
 		// Task has been edited.
 		InformEdit,
 
 		// User has become a member of a task.
 		InformMembership
 	};
 
 	private String taskID;
 	private String[] recipients;
 	private String message;
 	private long date;
 
 	public Notification(String message) {
 		this.date = System.currentTimeMillis() / 1000;
 		this.message = message;
 	}
 
 	public long getDate() {
 		return this.date;
 	}
 
 	public String getMessage() {
 		return this.message;
 	}
 
 	public String getRecipientsString() {
		if (recipients == null || recipients.length == 0)
		{
			return null;
		}
					
 		String value = recipients[0];
 
 		for (int i = 1; i < recipients.length; i++) {
 			value += "," + recipients[i];
 		}
 
 		return value;
 	}
 
 	public String[] getRecipientsArray() {
 		return this.recipients;
 	}
 
 	public String getTaskId() {
 		return this.taskID;
 	}
 
 	public void setDate(long value) {
 		this.date = value;
 	}
 
 	public void setRecipients(List<String> values) {
 		this.recipients = new String[values.size()];
 		values.toArray(this.recipients);
 	}
 
 	public void setRecipients(String[] values) {
 		this.recipients = values;
 	}
 
 	public void setRecipients(String values) {
 		this.recipients = values.split(",");
 	}
 
 	public void setTaskId(String value) {
 		this.taskID = value;
 	}
 
 	/**
 	 * Sets the notification's message string according to the notification's
 	 * type.
 	 * 
 	 * @param type
 	 *            The type of notification being sent
 	 */
 	public static String getMessage(String sender, String taskName, Type type) {
 		switch (type) {
 		case FulfillmentReport:
 			String date = new SimpleDateFormat("yyyy-MM-dd | HH:mm")
 					.format(Calendar.getInstance().getTime());
 			return String.format("\"%s\" was fulfilled by %s on %s.", taskName,
 					sender, date);
 		case InformDelete:
 			return String.format("%s deleted \"%s\".", sender, taskName);
 		case InformEdit:
 			return String
 					.format("%s made changes to \"%s\".", sender, taskName);
 		case InformMembership:
 			return String.format("%s has made you a member of \"%s\".", sender,
 					taskName);
 		default:
 			return String.format("Unknown notification for \"%s\".", taskName);
 		}
 	}
 
 }
