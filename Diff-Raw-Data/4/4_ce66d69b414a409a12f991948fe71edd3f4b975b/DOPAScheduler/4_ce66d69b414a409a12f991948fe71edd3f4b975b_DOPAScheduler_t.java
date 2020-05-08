 package eu.stratosphere.meteor.server;
 
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.NoSuchElementException;
 
 import eu.stratosphere.meteor.common.SchedulerConfigConstants;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import com.rabbitmq.client.AMQP.BasicProperties;
 import com.rabbitmq.client.QueueingConsumer.Delivery;
 
 import eu.stratosphere.meteor.common.JobState;
 import eu.stratosphere.meteor.common.MessageBuilder;
 import eu.stratosphere.meteor.common.MessageBuilder.RequestType;
 import eu.stratosphere.meteor.server.executor.FileSender;
 import eu.stratosphere.meteor.server.executor.RRJob;
 import eu.stratosphere.meteor.server.executor.RoundRobin;
 
 /**
  * The scheduler will started by Server.java. The scheduler connects to
  * the job queue (a RabbitMQ queue) and wait for jobList. If the scheduler
  * submits received jobList and sends the status of each job to an exchange
  * queue.
  *
  * @author Andre Greiner-Petter
  *         T.shan
  *         Etienne Rolly
  */
 public class DOPAScheduler {
    public static int RABBIT_MQ_NOT_STARTED_EXIT_CODE = 2;
 	/**
 	 * Log for server site.
 	 */
 	public static Log LOG = LogFactory.getLog( DOPAScheduler.class );
 	
 	/**
 	 * Factory to handle all connections with rabbitMQ
 	 */
 	private ServerConnectionFactory connectionFactory;
 	
 	/**
 	 * Collection of all jobs to iterate through while working process.
 	 * Each element is just a reference to job object in workingJobsCollection or in finishedJobsCollection.
 	 */
 	private RoundRobin submittedJobs;
 	
 	/**
 	 * The currently executing job.
 	 */
 	private RRJob curr_WorkingJob;
 	
 	/**
 	 * Contains the finished jobs sorted by clientID and jobID.
 	 * <ClientID -> <JobID -> RRJob>>
 	 */
 	private HashMap<String, HashMap<String, RRJob>> finishedJobsCollection;
 	
 	/**
 	 * Contains the registered clients.
 	 */
 	private LinkedList<String> registeredClients;
 	
 	/**
 	 * Paused main-loop flag
 	 */
 	private boolean paused = false;
 	
 	/**
 	 * Saves the current thread
 	 */
 	private final Thread schedulerThread = Thread.currentThread();
 	
 	/**
 	 * A global constant about the working time for each cycle
 	 */
 	private final long WAITING_TIME = 100;
 	
 	/**
 	 * If you want to get a DOPAScheduler object please use the static method to create
 	 * once. Note that only one client per system is allowed.
 	 */
 	private DOPAScheduler() {
 		this.submittedJobs = new RoundRobin();
 		this.curr_WorkingJob = null;
 		this.finishedJobsCollection = new HashMap<String, HashMap<String, RRJob>>();
 		this.registeredClients = new LinkedList<String>();
 	}
 	
 	/**
 	 * Connects the scheduler with the rabbitMQ system to handle all traffic from and to clientList.
 	 */
 	private void connect() {
 		try { this.connectionFactory = new ServerConnectionFactory( this );
 		} catch (IOException e) {
 			LOG.fatal("Cannot initialize the connections for the scheduler.");
            System.exit(RABBIT_MQ_NOT_STARTED_EXIT_CODE);

 		}
 	}
 	
 	/**
 	 * Adds a new incoming job. If a job with same identifications still exists it will be overwrite it.
 	 * @param clientID specified client
 	 * @param jobID specified job
 	 * @param properties from request
 	 * @param script of this job
 	 */
 	private void handleIncomingJob( String clientID, String jobID, BasicProperties properties, byte[] script ){
 		DOPAScheduler.LOG.info("New job received. JobID: " + jobID );
 		RRJob job;
 		
 		// removes the job. if this job doesn't existed in the working list removes it from the finished job list
 		if ( !submittedJobs.remove(clientID, jobID) );
 			this.removeFinishedJob(clientID, jobID);
 		
 		try {
 			// create new job
 			String encoding = properties.getContentEncoding();
 			String meteorScript = new String( script, encoding );
 			Date submitTime = properties.getTimestamp();
 			
 			// create job
 			job = new RRJob( clientID, jobID, meteorScript, submitTime );
 			
 			// put to existing list or create once
 			submittedJobs.add(clientID, job);
 			finishedJobsCollection.put(clientID, new HashMap<String, RRJob>());
 			
 			// send new job status to client
 			statusUpdate( clientID, jobID );
 		} catch ( UnsupportedEncodingException uee ){
 			LOG.error( "Cannot add a new DSCLJob, encode given script failed with false encoding informations.", uee );
 		} catch ( NullPointerException npe ){
 			LOG.error( "Any informations are null. Cannot handle incoming job.", npe );
 		}
 	}
 	
 	/**
 	 * Handle an incoming delivery by find out request type and reply that request.
 	 * @param delivery incoming message
 	 * @throws UnsupportedEncodingException cannot encrypted by given encoding type
 	 * @throws JSONException if any argument is null
 	 */
 	private void handleIncomingRequest( Delivery delivery ) 
 			throws UnsupportedEncodingException, JSONException {		
 		//checks if the incoming delivery contains a json string
 		if( !delivery.getProperties().getContentType().contains("json") ){
 			LOG.warn("Incoming message doesn't contains a json string.");
 			return;
 		}
 		
 		//get the encoding type from the delivery and with it the string from the body of our delivery
 		String encodingType = delivery.getProperties().getContentEncoding();
 		JSONObject request = new JSONObject( new String( delivery.getBody(), encodingType ) );
 		
 		// get root informations of request
 		String clientID = MessageBuilder.getClientID(request);
 		String jobID = MessageBuilder.getJobID(request);
 		
 		// handle specific request
 		switch ( RequestType.getRequestType(request) ){
 			case JOB_STATUS: // same as JOB_EXISTS request
 			case JOB_EXISTS:
 				statusUpdate( clientID, jobID ); 
 				break;
 			case GET_LINK:
 				replyLink( clientID, jobID, request, delivery.getProperties() ); 
 				break;
 			case REQUEST_RESULT: 
 				sendResult( clientID, jobID, delivery );
 				break;
 			case JOB_ABORT: 
 				abortJob( clientID, jobID ); 
 				break;
 			default: // false request
 		} // end switch-case
 	}
 	
 	/**
 	 * Sends a status update to specified client of its job.
 	 * @param clientID specified client
 	 * @param jobID specified job
 	 */
 	private void statusUpdate( String clientID, String jobID ){
 		//get the specified RRjob from the workingJobsCollection by the ClientID and the JobID
 		RRJob job = submittedJobs.get(clientID, jobID);
 		if ( job == null ) job = getFinishedJob( clientID, jobID );
 		
 		// build json object for reply
 		JSONObject jobStatus;
 		
 		// if job still doesn't exists (no working job, no finished job) the status is deleted.
 		if ( job == null ) jobStatus = MessageBuilder.buildJobStatus( clientID, jobID, JobState.DELETED );
 		else if ( job.getStatus().equals( JobState.ERROR ) ) jobStatus = job.getErrorJSON();
 		else jobStatus = MessageBuilder.buildJobStatus( clientID, jobID, job.getStatus() );
 		
 		// send reply
 		try { this.connectionFactory.sendJobStatus( clientID, jobStatus ); }
 		catch ( IOException ioe ){ LOG.error("Cannot send status update.", ioe); }
 	}
 	
 	/**
 	 * Send the link of a finished job to client.
 	 * @param clientID specified client
 	 * @param jobID specified job
 	 * @param request object from client
 	 * @param properties from request
 	 */
 	private void replyLink( String clientID, String jobID, JSONObject request, BasicProperties properties ){
 		//get the specified RRjob from the workingJobsCollection by the ClientID and the JobID
 		RRJob job = getFinishedJob( clientID, jobID );
 		
 		// get index from request
 		int idx = MessageBuilder.getFileIndex( request );
 		
 		// build reply
 		JSONObject reply = MessageBuilder.buildGetLink(clientID, jobID, idx);
 		
 		if ( job != null ) reply = MessageBuilder.addPath( reply, job.getResult(idx) );
 		else reply = MessageBuilder.buildErrorStatus(
 				clientID, jobID,
 				"The job with the ID: '" + jobID + "' doesn't finished yet or exists anymore. "
 						+ "Ask for the status if you're not sure whether this job exists.");
 		
 		// send message
 		try { this.connectionFactory.replyRequest( properties, reply ); }
 		catch ( IOException ioe ){ LOG.error( "Cannot send link of job.", ioe ); }
 	}
 	
 	/**
 	 * Abort the given job and send a status update (deleted) to client.
 	 * @param clientID specified client
 	 * @param jobID specified job
 	 */
 	private void abortJob( String clientID, String jobID ){
 		if ( !submittedJobs.remove(clientID, jobID) )
 			this.removeFinishedJob(clientID, jobID);
 		
 		// send new status
 		JSONObject reply = MessageBuilder.buildJobStatus( clientID, jobID, JobState.DELETED );
 		try { this.connectionFactory.sendJobStatus(clientID, reply); }
 		catch ( IOException ioe ) { LOG.error( "Cannot send deleted job status, after deleted job.", ioe ); }
 		
 		DOPAScheduler.LOG.info("Job aborted. JobID: " + jobID);
 	}
 	
 	/**
 	 * Create a new thread that sends the result blocks back to the client.
 	 * It allows to send blocks for different jobs by using multi-threading.
 	 * @param clientID for this job
 	 * @param jobID for the job
 	 * @param delivery request from the client
 	 */
 	private void sendResult( String clientID, String jobID, Delivery delivery ){
 		RRJob job = this.getFinishedJob(clientID, jobID);
 		if ( job == null ) {
 			String errorMsg = "Your specified job with the ID: '"+jobID+"' ";
 			if ( submittedJobs.contains(clientID, jobID) )
 				errorMsg += "doesn't finished yet. You cannot ask for the result at this stage.";
 			else errorMsg += "doesn't exists on the server.";
 			sendErrorMessage( delivery.getProperties(), clientID, jobID, errorMsg );
 			return;
 		}
 		
 		FileSender sender = new FileSender( this.connectionFactory, job, delivery );
 		sender.run();
 	}
 	
 	/**
 	 * Sends an error message back to the client. The properties have to come from a request!
 	 * @param props
 	 * @param clientID
 	 * @param errorMessage
 	 */
 	private void sendErrorMessage( BasicProperties props, String clientID, String jobID, String errorMessage ){
 		JSONObject err = MessageBuilder.buildErrorStatus(clientID, jobID, errorMessage);
 		try {
 			this.connectionFactory.replyRequest(props, err);
 		} catch (IllegalArgumentException | IOException e) {
 			DOPAScheduler.LOG.error( "Cannot send the error message to " + clientID, e );
 		} 
 	}
 	
 	/**
 	 * Test whether specified job contains in finished list. If it is so it returns the job object,
 	 * otherwise returns null.
 	 * @param clientID
 	 * @param jobID
 	 * @return RRjob if its exists, otherwise null
 	 */
 	private RRJob getFinishedJob( String clientID, String jobID ){
 		HashMap<String, RRJob> clientMap = finishedJobsCollection.get(clientID);
 		if ( clientMap != null ) return clientMap.get(jobID);
 		return null;
 	}
 	
 	/**
 	 * Removes a finished job and returns true if that changes anything or false if not.
 	 * @param clientID
 	 * @param jobID
 	 * @return true if it changed the list or false if not
 	 */
 	private boolean removeFinishedJob( String clientID, String jobID ){
 		RRJob job = getFinishedJob( clientID, jobID );
 		if ( job == null ) return false;
 		return finishedJobsCollection.get(clientID).remove(jobID) != null;
 	}
 	
 	/**
 	 * This method works on jobs. There is only one job running at the same time.
 	 */
 	private void workOnJobs(){
 		// if there is a job find out its status
 		if ( this.curr_WorkingJob != null ){
 			// if this job is still in process
 			if ( this.curr_WorkingJob.finished() ) {
 				// add job to finished job list
 				this.finishedJobsCollection.get( 
 						curr_WorkingJob.getClientID() ).put( curr_WorkingJob.getJobID(), curr_WorkingJob );
 				
 				// inform client that its job finished
 				this.statusUpdate(this.curr_WorkingJob.getClientID(), this.curr_WorkingJob.getJobID());
 				
 				// working with new jobs
 				curr_WorkingJob = null;
 			} else return; // go on with it!
 		}
 		
 		//take first job from the job-list and its status
 		try { curr_WorkingJob = submittedJobs.next(); }
 		catch ( NoSuchElementException nsee ){}
 		
 		// nothing to do here as well
 		if ( curr_WorkingJob == null ) return;
 		
 		// execute the current job
 		curr_WorkingJob.execute();
 		
 		// inform administrator about new job executions
 		DOPAScheduler.LOG.info( "New job executed. " + curr_WorkingJob.getJobID() );
 		
 		// inform the client that the status changed now
 		statusUpdate( curr_WorkingJob.getClientID(), curr_WorkingJob.getJobID() );
 	}
 	
 	/**
 	 * Adds an incoming client to the scheduler services. Returns true if
 	 * the client got the rights to enter this service, false otherwise.
 	 * 
 	 * @param clientID 
 	 * @return true if the client got the rights, false otherwise
 	 */
 	protected boolean addClient(String clientID) {
 		if ( registeredClients.contains(clientID) ) return false;
 		else {
 			registeredClients.add(clientID);
 			finishedJobsCollection.put( clientID, new HashMap<String, RRJob>());
 			submittedJobs.add(clientID);
 			DOPAScheduler.LOG.info("Client '" + clientID + "' registered.");
 		}
 		return true;
 	}
 	
 	/**
 	 * Removes client with all connections to jobs. Returns true if it worked, otherwise false.
 	 * @param clientID
 	 * @return true if client removed well, false otherwise
 	 */
 	protected void removeClient( String clientID ){
 		// remove the client
 		registeredClients.remove(clientID);
 		
 		// remove client and all jobs from this client from working list
 		submittedJobs.remove(clientID);
 		
 		DOPAScheduler.LOG.info("Client '" + clientID + "' unregestered.");
 	}
 	
 	/**
 	 * Starts the main loop of the scheduler. Handle deliveries like requests or new jobList
 	 * and execute other jobList with the round robin algorithm. Inform clientList about new job
 	 * states and possibly error messages.
 	 * You can pause the system by using pause() and restart it with restart(). If you paused
 	 * the system you cannot invoke this method to restarts the server. Please use restart().
 	 */
 	public void start() {
 		
 		// main loop handle incoming, outgoing messages and work through job lists
 		while( !paused ){
 			
 			// get delivery
 			Delivery delivery = connectionFactory.getRequest( WAITING_TIME );
 			
 			// if nothing todo at all, sleep a bit and continue after wake up
 			if ( delivery == null && !submittedJobs.hasNext() ){
 				try { Thread.sleep( WAITING_TIME ); } 
 				catch (InterruptedException e) { Thread.interrupted(); }
 				continue;
 			}
 			
 			if ( delivery != null ){
 				//get the rountingKey from the delivery
 				String routingKey = delivery.getEnvelope().getRoutingKey();
 				
 				// if incoming message is a request
 				if ( routingKey.matches( SchedulerConfigConstants.REQUEST_KEY_MASK ) ){
 					try { handleIncomingRequest( delivery ); } 
 					catch (UnsupportedEncodingException e) { LOG.error("Cannot decrypt incoming request.", e); }
 					catch (JSONException e) { LOG.error("Unbelievable. Send me how you produces this error...", e); }
 				} else { // else search for jobs
 					String[] separateKey = routingKey.split("\\.");
 					if ( separateKey[0].matches("setJob") && separateKey.length >= 3 )
 						handleIncomingJob( separateKey[1], separateKey[2], delivery.getProperties(), delivery.getBody() );
 				}
 			} // end if delivery != null
 			
 			// execute jobList via round robin algorithm 
 			workOnJobs();
 			
 			// System yield, to keep this time as short as possible use setSchedulerPriority( int priority )
 			Thread.yield();
 		}
 	}
 	
 	/**
 	 * It restarts the system after you paused the scheduler.
 	 */
 	public void restart(){
 		if ( !paused ) return;
 		this.paused = false;
 		start();
 	}
 	
 	/**
 	 * Paused the scheduler. The scheduler finished last loop cycle and stopped until you restarts
 	 * the server. (Call restart() to do this)
 	 */
 	public void pause(){
 		this.paused = true;
 	}
 	
 	/**
 	 * Clean finished jobs collection. This delete all finished jobs from
 	 * unregistered clients.
 	 */
 	public void cleanGarbageJobsCollection(){
 		HashMap<String, HashMap<String, RRJob>> tmpFinishedJobs =
 				new HashMap<String, HashMap<String, RRJob>>();
 		
 		// save all finished jobs of registered clients
 		for ( String clientID : registeredClients ){
 			HashMap<String, RRJob> tmpJobMap = finishedJobsCollection.get(clientID);
 			tmpFinishedJobs.put(clientID, tmpJobMap);
 		}
 		
 		// delete all finished jobs of unregistered clients
 		this.finishedJobsCollection = tmpFinishedJobs;
 	}
 	
 	/**
 	 * If you want to power up the scheduler on your system it's possible to
 	 * push the priority of the scheduler thread. That's the best solution
 	 * to keep the time between two cycles (of main loop) as short as possible.
 	 * 
 	 * @param priority can be a value between Thread.MIN_PRIORITY and Thread.MAX_PRIORITY
 	 */
 	public void setSchedulerPriority( int priority ){
 		if ( priority < Thread.MIN_PRIORITY ) priority = Thread.MIN_PRIORITY;
 		else if ( priority > Thread.MAX_PRIORITY ) priority = Thread.MAX_PRIORITY;
 		else schedulerThread.setPriority( priority );
 	}
 	
 	/**
 	 * Stops the scheduler service by shutdown all connections with RabbitMQ and close the
 	 * ServerConnectionFactory.
 	 * 
 	 * @throws IOException
 	 */
 	public void shutdown() throws IOException {
 		this.connectionFactory.shutdownConnections();
 	}
 	
 	/**
 	 * Creates and return a new Scheduler object. It just initialize all connections and create objects to
 	 * handle clientList and jobList.
 	 * The returned scheduler doesn't work yet. You have to start the service to invoke start(). This starts
 	 * the loop of the service to handle all interactions. If you want to pause the scheduler without shutdown
 	 * you can call the pause() method. If you want to restart your system please use restart() method.
 	 * 
 	 * @return DOPAScheulder object in pause mode.
 	 */
 	public static DOPAScheduler createNewSchedulerSystem(){
 		DOPAScheduler scheduler = new DOPAScheduler();
 		scheduler.connect();
 		return scheduler;
 	}
 	
 	/**
 	 * TODO
 	 * You just have to specified the nephele configuration directory with
 	 * 		--configDir <nephele-config-directory-path>
 	 * Other specifications arn't needed.
 	 * 
 	 * @param args
 	 */
 	public static void main( String[] args ){
 		if ( args != null && args.length >= 2 ){
 			// get config dir!
 			SchedulerConfigConstants.EXECUTER_CONFIG[0] = args[0];
 			SchedulerConfigConstants.EXECUTER_CONFIG[1] = args[1];
 		}
 		
 		DOPAScheduler scheduler = createNewSchedulerSystem();
 		scheduler.start();
 	}
 }
