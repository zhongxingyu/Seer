 package models;
 
import java.util.Collections;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.SortedMap;
 import java.util.TreeMap;
 
 /**
  * A representation of notification collection. Each user has at least one
  * mailbox - their personal one - and possibly more. For example, a moderator
  * gains access to the global mod-mailbox.
  * 
  * All methods to access Notifications return them in their natural order.
  * 
  */
 public class Mailbox implements IMailbox {
 	private SortedMap<Integer, Notification> notifications;
 	private String name;
 
 	public Mailbox(String name) {
 		this.name = name;
 		this.notifications = new TreeMap();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see models.IMailbox#receive(models.Notification)
 	 */
 	public void receive(Notification notification) {
 		this.notifications.put(notification.id(), notification);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see models.IMailbox#getAllNotifications()
 	 */
 	public List<Notification> getAllNotifications() {
 		LinkedList<Notification> result = new LinkedList();
 		List<Notification> all = new LinkedList(this.notifications.values());
 		for (Notification notification : all) {
 			if (notification.getAbout().isDeleted()) {
 				this.removeNotification(notification.id());
 			} else {
 				result.addLast(notification);
 			}
 		}
		Collections.reverse(result);
 		return result;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see models.IMailbox#getRecentNotifications()
 	 */
 	public List<Notification> getRecentNotifications() {
 		List<Notification> recent = new LinkedList();
 		for (Notification notification : this.getAllNotifications()) {
 			if (notification.isVeryRecent()) {
 				recent.add(notification);
 			}
 		}
 		return recent;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see models.IMailbox#getNewNotifications()
 	 */
 	public List<Notification> getNewNotifications() {
 		List<Notification> unread = new LinkedList();
 		for (Notification notification : this.getAllNotifications()) {
 			if (notification.isNew()) {
 				unread.add(notification);
 			}
 		}
 		return unread;
 	}
 
 	public void removeNotification(int id) {
 		this.notifications.remove(id);
 	}
 
 	public String toString() {
 		return "MB[" + this.name + "(" + this.notifications.size() + ")" + "]";
 	}
 
 	public void delete() {
 		for (Notification notification : this.getAllNotifications()) {
 			notification.unregister();
 		}
 	}
 
 	public String getName() {
 		return this.name;
 	}
 }
