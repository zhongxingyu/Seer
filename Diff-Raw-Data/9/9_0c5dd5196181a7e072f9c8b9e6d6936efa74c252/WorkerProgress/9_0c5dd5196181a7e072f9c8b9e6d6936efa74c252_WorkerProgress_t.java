 package org.jims.modules.crossbow.infrastructure.progress;
 
 import java.net.InetAddress;
 import java.net.UnknownHostException;
 import java.util.List;
 import java.util.LinkedList;
 
 import javax.management.ListenerNotFoundException;
 import javax.management.MBeanNotificationInfo;
 import javax.management.Notification;
 import javax.management.NotificationFilter;
 import javax.management.NotificationListener;
 
 import org.jims.modules.crossbow.infrastructure.progress.notification.LogNotification;
 import org.jims.modules.crossbow.infrastructure.progress.notification.TaskCompletedNotification;
 
 import org.apache.log4j.Logger;
 
 public class WorkerProgress implements WorkerProgressMBean {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = -6571063858607854845L;
 
 	private List<NotificationListener> listeners = new LinkedList<NotificationListener>();
 	
 	private int sequenceNumber = 0;
 
 	private Logger log = Logger.getLogger( WorkerProgress.class );
 
 	@Override
 	public void addNotificationListener(NotificationListener listener,
 			NotificationFilter filter, Object handback)
 			throws IllegalArgumentException {
 		listeners.add(listener);
 	}
 
 	@Override
 	public MBeanNotificationInfo[] getNotificationInfo() {
 		MBeanNotificationInfo info = new MBeanNotificationInfo(null,
 				"Notification about deployment process on worker node", null);
 		return new MBeanNotificationInfo[] { info };
 	}
 
 	@Override
 	public void removeNotificationListener(NotificationListener listener)
 			throws ListenerNotFoundException {
 		listeners.remove(listener);
 	}
 
 	@Override
 	public void sendLogNotification(String log) {
 		
 		LogNotification logNotification = new LogNotification(log, getIpAddress());
 		
 		Notification notification = new Notification("WorkerNodeProgressNotification",
 				"WorkerNode", ++sequenceNumber);
 		notification.setUserData(logNotification);
 
 		this.log.info("Sending log notification " + log + " to " + listeners.size() + " listeners");
 
 		for (NotificationListener notificationListener : listeners) {
 			notificationListener.handleNotification(notification, null);
 		}
 
 	}
 
 	@Override
 	public void sendTaskCompletedNotification() {
 		
 		TaskCompletedNotification taskCompletedNotification = new TaskCompletedNotification(
 				getIpAddress());
 		
 		Notification notification = new Notification("WorkerNodeProgressNotification",
 				"WorkerNode", ++sequenceNumber);
 		notification.setUserData(taskCompletedNotification);
 
 		log.info("Sending task completed notification to " + listeners.size() + " listeners");
 
 		for (NotificationListener notificationListener : listeners) {
 			notificationListener.handleNotification(notification, null);
 		}
 	}
 
	protected static String getIpAddress() {
 
 		InetAddress addr = null;
 		try {
 			addr = InetAddress.getLocalHost();
 		} catch (UnknownHostException e) {
 			e.printStackTrace();
 		}
 	    
 	    return addr != null ? addr.toString() : "Unknown host";
 		
 	}
 
 }
