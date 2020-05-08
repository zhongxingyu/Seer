 package models;
 
 import java.io.File;
 import java.util.Collections;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.concurrent.TimeUnit;
 
 import play.Logger;
 import play.db.ebean.*;
 
 import javax.persistence.*;
 
 import org.daisy.pipeline.client.Pipeline2WSException;
 import org.daisy.pipeline.client.Pipeline2WSResponse;
 import org.daisy.pipeline.client.models.Job.Status;
 import org.daisy.pipeline.client.models.job.Message;
 import org.w3c.dom.Document;
 
 import controllers.Application;
 
 import akka.actor.Cancellable;
 import play.data.validation.*;
 import play.libs.Akka;
 import scala.concurrent.duration.Duration;
 import utils.XML;
 
 @Entity
 public class Job extends Model implements Comparable<Job> {
 	private static final long serialVersionUID = 1L;
 	
 	/** Key is the job ID; value is the sequence number of the last message read from the Pipeline 2 Web API. */ 
 	public static Map<String,Integer> lastMessageSequence = Collections.synchronizedMap(new HashMap<String,Integer>());
 	public static Map<String,org.daisy.pipeline.client.models.Job.Status> lastStatus = Collections.synchronizedMap(new HashMap<String,org.daisy.pipeline.client.models.Job.Status>());
 	
 	
 	@Id
 	@Constraints.Required
 	public String id;
 
 	// General information
 	public String nicename;
 	public Date created;
 	public Date started;
 	public Date finished;
 	@Column(name="user_id") public Long user;
 	public String guestEmail; // Guest users may enter an e-mail address to receive notifications
 	public String localDirName;
 	public String scriptId;
 	public String scriptName;
 	
 	// Notification flags
 	public boolean notifiedCreated;
 	public boolean notifiedComplete;
 
 	// Not stored in the job table; retrieved dynamically
 	@Transient
 	public String href;
 	@Transient
 	public String status;
 	@Transient
 	public String userNicename;
 	@Transient
 	public List<Message> messages;
 
 	@Transient
 	private Cancellable pushNotifier;
 	
 	/** Make job belonging to user */
 	public Job(String id, User user) {
 		this.id = id;
 		this.user = user.id;
 		this.nicename = id;
 		this.created = new Date();
 		this.notifiedCreated = false;
 		this.notifiedComplete = false;
 		if (user.id < 0)
 			this.userNicename = Setting.get("users.guest.name");
 		else
 			this.userNicename = User.findById(user.id).name;
 	}
 	
 	/** Make job from engine job */
 	public Job(org.daisy.pipeline.client.models.Job fwkJob) {
 		this.id = fwkJob.id;
 		this.user = -1L;
 		this.nicename = fwkJob.script.id+" (Command Line Interface)";
 		this.created = new Date();
 		this.notifiedCreated = false;
 		this.notifiedComplete = false;
 		this.userNicename = "Command Line Interface"; // could be something other than the CLI, but in 99% of the cases it will be the CLI
 		
 		if (!org.daisy.pipeline.client.models.Job.Status.IDLE.equals(fwkJob.status)) {
 			this.started = this.created;
 			if (!org.daisy.pipeline.client.models.Job.Status.RUNNING.equals(fwkJob.status)) {
 				this.finished = this.started;
 			}
 		}
 		
 		this.scriptId = fwkJob.script.id;
 		this.scriptName = fwkJob.script.nicename;
 	}
 
 	public int compareTo(Job other) {
 		return created.compareTo(other.created);
 	}
 
 	// -- Queries
 
 	public static Model.Finder<String,Job> find = new Model.Finder<String, Job>(Application.datasource, String.class, Job.class);
 
 	/** Retrieve a Job by its id. */
 	public static Job findById(String id) {
 		Job job = find.where().eq("id", id).findUnique();
 		if (job != null) {
 			User user = User.findById(job.user);
 			if (user != null)
 				job.userNicename = user.name;
 			else if (job.user < 0)
 				job.userNicename = Setting.get("users.guest.name");
 			else
 				job.userNicename = "User";
 		}
 		return job;
 	}
 
 	public void pushNotifications() {
 		if (pushNotifier != null)
 			return;
 
 		pushNotifier = Akka.system().scheduler().schedule(
 				Duration.create(0, TimeUnit.SECONDS),
				Duration.create(1000, TimeUnit.MILLISECONDS),
 				new Runnable() {
 					public void run() {
 						try {
							Integer fromSequence = Job.lastMessageSequence.containsKey(id) ? Job.lastMessageSequence.get(id) : 0;
 //							Logger.debug("checking job #"+id+" for updates from message #"+fromSequence);
 							
 							Pipeline2WSResponse wsJob;
 							org.daisy.pipeline.client.models.Job job;
 							
 							try {
 								wsJob = org.daisy.pipeline.client.Jobs.get(Setting.get("dp2ws.endpoint"), Setting.get("dp2ws.authid"), Setting.get("dp2ws.secret"), id, fromSequence);
 								
 								if (wsJob.status != 200 && wsJob.status != 201) {
 									return;
 								}
 								
 								Document xml = wsJob.asXml();
 								job = new org.daisy.pipeline.client.models.Job(xml);
 								
 								if (Application.debug)
 									Logger.debug(XML.toString(xml));
 								
 							} catch (Pipeline2WSException e) {
 								Logger.error(e.getMessage(), e);
 								return;
 							}
 							
 							Job webUiJob = Job.findById(job.id);
 							
 							if (webUiJob == null) {
 								// Job has been deleted; stop updates
 								pushNotifier.cancel();
 							}
 							
 							if (job.status != Status.RUNNING && job.status != Status.IDLE) {
 								pushNotifier.cancel();
 								if (webUiJob.finished == null) {
 									// pushNotifier tends to fire multiple times after canceling it, so this if{} is just to fire the "finished" event exactly once
 									webUiJob.finished = new Date();
 									webUiJob.save(Application.datasource);
 									Map<String,String> finishedMap = new HashMap<String,String>();
 									finishedMap.put("text", webUiJob.finished.toString());
 									finishedMap.put("number", webUiJob.finished.getTime()+"");
 									NotificationConnection.pushJobNotification(webUiJob.user, new Notification("job-finished-"+job.id, finishedMap));
 									
 									// Delete temporary files when job execution has finished
 									File results = new File(Setting.get("dp2ws.tempdir")+webUiJob.localDirName);
 									if (results.exists() && results.isDirectory())
 										recursivelyDeleteDirectory(results);
 								}
 							}
 							
 							for (org.daisy.pipeline.client.models.job.Message message : job.messages) {
 								Notification notification = new Notification("job-message-"+job.id, message);
 								NotificationConnection.pushJobNotification(webUiJob.user, notification);
 							}
 							
 							if (!job.status.equals(lastStatus.get(job.id))) {
 								lastStatus.put(job.id, job.status);
 								NotificationConnection.pushJobNotification(webUiJob.user, new Notification("job-status-"+job.id, job.status));
 								
 								webUiJob.status = job.status.toString();
 								
 								if (job.status == Status.RUNNING) {
 									// job status changed from IDLE to RUNNING
 									webUiJob.started = new Date();
 									Map<String,String> startedMap = new HashMap<String,String>();
 									startedMap.put("text", webUiJob.started.toString());
 									startedMap.put("number", webUiJob.started.getTime()+"");
 									NotificationConnection.pushJobNotification(webUiJob.user, new Notification("job-started-"+job.id, startedMap));
 								}
 								
 								webUiJob.save(Application.datasource);
 							}
 							
 							if (job.messages.size() > 0) {
 								Job.lastMessageSequence.put(job.id, job.messages.get(job.messages.size()-1).sequence);
 							}
 						} catch (javax.persistence.PersistenceException e) {
 							// Ignores this exception that happens on shutdown:
 							// javax.persistence.PersistenceException: java.sql.SQLException: Attempting to obtain a connection from a pool that has already been shutdown.
 							// Should be safe to ignore I think...
 						}
 					}
 				},
 				Akka.system().dispatcher()
 				);
 	}
 	
 	public List<Upload> getUploads() {
 		return Upload.find.where().eq("job", id).findList();
 	}
 	
 	@Override
 	public void delete(String datasource) {
 		Logger.debug("deleting "+this.id+" (sending DELETE request)");
 		try {
 			org.daisy.pipeline.client.Jobs.delete(Setting.get("dp2ws.endpoint"), Setting.get("dp2ws.authid"), Setting.get("dp2ws.secret"), this.id);
 		} catch (Pipeline2WSException e) {
 			Logger.warn("Unable to send DELETE request for deleting the job #"+this.id+" from the Pipeline 2 Engine",e);
 		}
 		List<Upload> uploads = getUploads();
 		for (Upload upload : uploads)
 			upload.delete(datasource);
 		File results = new File(Setting.get("dp2ws.resultdir")+this.localDirName);
 		if (results.exists() && results.isDirectory())
 			recursivelyDeleteDirectory(results);
 		super.delete(datasource);
 	}
 	
 	private void recursivelyDeleteDirectory(File dir) {
 		for (File file : dir.listFiles()) {
 			if (file.isDirectory()) {
 				recursivelyDeleteDirectory(file);
 			} else {
 				if (!file.delete()) {
 					Logger.error("Failed to delete file: " + file);
 				}
 			}
 		}
 		if (!dir.delete()) {
 			Logger.error("Failed to delete directory: " + dir);
 		}
 	}
 
 }
