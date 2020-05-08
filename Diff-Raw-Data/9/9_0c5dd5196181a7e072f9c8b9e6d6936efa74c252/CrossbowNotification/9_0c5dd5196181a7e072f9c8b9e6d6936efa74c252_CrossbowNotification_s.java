 package org.jims.modules.crossbow.infrastructure.progress;
 
 import java.util.List;
 import java.util.LinkedList;
 
 import javax.management.ListenerNotFoundException;
 import javax.management.MBeanNotificationInfo;
 import javax.management.MBeanServer;
 import javax.management.ObjectName;
 import javax.management.Notification;
 import javax.management.NotificationFilter;
 import javax.management.NotificationListener;
 
 import org.jims.modules.crossbow.infrastructure.progress.notification.ProgressNotification;
 import org.jims.modules.crossbow.infrastructure.progress.notification.LogNotification;
 import org.jims.modules.crossbow.infrastructure.progress.notification.TaskCompletedNotification;
 import org.jims.modules.crossbow.infrastructure.JimsMBeanServer;
 
 import org.apache.log4j.Logger;
 
 
 public class CrossbowNotification implements CrossbowNotificationMBean {
 
 	private int totalTasks;
 
 	private int index;
 
 	private Logger log = Logger.getLogger( CrossbowNotification.class );
 
 	private StringBuilder sb = new StringBuilder();
 	private ProgressNotification progressNotification = null;
 
 	public CrossbowNotification(int totalTasks) {
 		this.index = 0;
 		this.totalTasks = totalTasks;
 
 		registerNotificationListener();
 	}
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 4845007778991652486L;
 
 	//@todo dopisac rejestrowanie we wszystkich WorkerProgressMBEan'ach - narazie jest tylko jeden
 	private void registerNotificationListener() {
 
 		log.debug("Registering worker progress listener");
 
 		MBeanServer server = JimsMBeanServer.findJimsMBeanServer();
 		if(server != null) {
 			try{
 				server.addNotificationListener(new ObjectName( "Crossbow:type=WorkerProgress" ), this,
 					null, null);
 				log.debug("Worker progress listener successfully registered");
 			}catch(Exception e) {
 				log.error("Couldn't register WorkerProgress Listener");
 			}
 
 		}
 
 	}
 
 	@Override
 	public synchronized ProgressNotification getProgress() {
 
 		log.info("Asking for progressNotification");
 		return this.progressNotification;
 	}
 
 	@Override
 	public synchronized String getNewLogs() {
 
 		log.info("Asking for logs");		
 		String logs = sb.toString();
 		sb = new StringBuilder();
 		return logs;
 	}
 
 	private synchronized void updateLogs(String logs) {
 		sb.append(logs);
 		log.info("New log: " + logs);
 		sb.append("\n");
 	}
 
 	private synchronized void updateProgress(ProgressNotification progressNotification) {
 		this.progressNotification = progressNotification;
 		log.info("Progress notification " + index + " out of " + totalTasks + " is done");
 	}
 
 	/**
 	 * Jesli otrzymana notyfikacja jest o stanie procesu deploymentu to
 	 * aktualizuje licznik i przekazuje notyfikacje do wszystkich listenerow
 	 * postepu procesu deploymentu
 	 */
 	@Override
 	public synchronized void handleNotification(Notification notification, Object handback) {
 
 		Object userData = notification.getUserData();
 
 		log.info("Received notification from WorkerProgress ");
 
 		if (notification.getUserData() != null
 				&& notification.getUserData() instanceof TaskCompletedNotification) {
 			
 			updateProgress(new ProgressNotification(++index, totalTasks,
 					((TaskCompletedNotification) notification.getUserData())
 						.getNodeIpAddress()));
 			
 							
 		} else if(notification.getUserData() != null
 				&& notification.getUserData() instanceof LogNotification) {
 			
 			updateLogs(((LogNotification)notification.getUserData()).getLog());
 		}
 	}
 
 	@Override
 	public void reset() {
 
 		index = 0;
 		sb = new StringBuilder();
 
 	}
 }
