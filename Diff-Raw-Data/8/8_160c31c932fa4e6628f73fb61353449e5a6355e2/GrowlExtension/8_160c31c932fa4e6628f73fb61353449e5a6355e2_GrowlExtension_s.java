 package fr.perigee.java.maven.growl.extension;
 
 import java.util.concurrent.TimeUnit;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import org.apache.maven.execution.AbstractExecutionListener;
 import org.apache.maven.execution.ExecutionEvent;
 import org.codehaus.plexus.component.annotations.Component;
 
 import com.google.code.jgntp.Gntp;
 import com.google.code.jgntp.GntpApplicationInfo;
 import com.google.code.jgntp.GntpClient;
 import com.google.code.jgntp.GntpNotification;
 import com.google.code.jgntp.GntpNotificationInfo;
 
 /**
  * A maven execution listener sending various notifications on various build events. 
  * When started, it immediatly instanciates a long-term growl client that will be 
  * used to send all events
  * @author ndx
  *
  */
 @Component(role = GrowlExtension.class)
 public class GrowlExtension extends AbstractExecutionListener {
 	public static final String MAVEN_EXTENSION = "maven-growl-extension";
 	public static final String PREFIX = Gntp.APP_SPECIFIC_HEADER_PREFIX+MAVEN_EXTENSION;;
 	private static final Logger logger = Logger.getLogger(GrowlExtension.class.getName());
 	
 	private GntpClient client;
 	private GntpApplicationInfo applicationInfo;
 
 	public GrowlExtension() {
 		applicationInfo = Gntp.appInfo(MAVEN_EXTENSION).build();
 	}
 	
 	@Override
 	public void sessionStarted(ExecutionEvent event) {
 		client = Gntp.client(applicationInfo).build();
 	}
 	
 	@Override
 	public void sessionEnded(ExecutionEvent event) {
 		try {
 			client.shutdown(1, TimeUnit.SECONDS);
 		} catch (Exception e) {
 			logger.log(Level.SEVERE, "unable to shut down properly growl extension", e);
 		}
 	}
 	
 	@Override
 	public void projectStarted(ExecutionEvent event) {
 		notify(event);
 	}
 	
 	@Override
 	public void projectFailed(ExecutionEvent event) {
 		notify(event);
 	}
 	
 	@Override
 	public void projectSucceeded(ExecutionEvent event) {
 		notify(event);
 	}
 
 	/**
 	 * Notify given event at given severity
 	 * @param event
 	 */
 	private void notify(ExecutionEvent event) {
 		GntpNotification notification = notificationFor(event);
		try {
			client.notify(notification, 1, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			logger.log(Level.SEVERE, "unable to send notification "+notification, e);
		}
 	}
 
 	/**
 	 * Converts the maven execution event into a growl notification
 	 * @param event source event
 	 * @return generated notification
 	 */
 	private GntpNotification notificationFor(ExecutionEvent event) {
 		GntpNotificationInfo info = Gntp.notificationInfo(applicationInfo, 
 				event.getMojoExecution().identify()).build();
 		GntpNotification returned = 
 				Gntp
 					.notification(info, event.getProject().toString())
 					.header(PREFIX+"event-type", event.getType().name())
 					.header(PREFIX+"project-groupId", event.getProject().getGroupId())
 					.header(PREFIX+"project-artifactId", event.getProject().getArtifactId())
 					.header(PREFIX+"project-name", event.getProject().getName())
 					.header(PREFIX+"project-description", event.getProject().getDescription())
 					.header(PREFIX+"mojo-executionId", event.getMojoExecution().getExecutionId())
 					.header(PREFIX+"mojo-artifactId", event.getMojoExecution().getArtifactId())
 					.header(PREFIX+"mojo-groupId", event.getMojoExecution().getGroupId())
 					.header(PREFIX+"mojo-goal", event.getMojoExecution().getGoal())
 					.header(PREFIX+"mojo-phase", event.getMojoExecution().getLifecyclePhase())
 					.build();
 		return returned;
 	}
 }
