 package job;
 
 import java.io.IOException;
 
 import os.Pipe;
 
 /**
  * job/Job.java
  * <br><br>
  * Abstract class, which makes sure that any application is ready to run in our
  * magnificent operating system.
  * 
  * <b>Do not touch job's pipes after the job has been started!</b>
  * 
  * @author Pavel Čurda
  * 
  * @version 1.3
  * 
  * @team <i>OutOfMemory</i> for KIV/OS 2013
  * @teamLeader Radek Petruška  radekp25@students.zcu.cz
  * 
  */
 public abstract class Job extends Thread {
 	
 	/** Input of this job.  */
 	private Pipe stdIn;
 	/** Output of this job. */
 	private Pipe stdOut;
 	/** If something goes wrong, write here. */
 	private Pipe stdErr;
 	
 	/** if Job gets any arguments from shell, it will be here. */
 	protected String[] arguments;
 	
 	/** The folder in which the job is running **/
 	protected String path;
 	
 	/** Process ID. */
 	public final Integer PID;
 	/** 
 	 * Status of the job. If your Job will <b>sleep or lock</b> on anything, 
 	 * feel free to change its status from RUNNING to WAITING and 
 	 * vice versa after when waiting at some locks.
 	 */
 	private JobStatus status;
 	
 	/** Size of buffer in Pipes */
 	public static final int BUF_SIZE = Pipe.BUF_SIZE;
 	
 	
 	/**
 	 * Prepare the job to run.
 	 * 
 	 * @param PID of the new process.
 	 * @param stdERR where to write errors.
 	 */
 	public Job(Integer PID, Pipe stdERR) {
 		
 		this.PID = PID;
 		this.stdErr = stdERR;
 		
 		this.status = JobStatus.NEW;
 	}
 
 
 	
 	/**
 	 * 	When the job is implemented, write something about how it works
 	 * <br> or just use man from GNU/Linux and copy paste here.
 	 * 
 	 * @return Manual for job.
 	 */
 	public abstract String getManual();
 	
 	
 	/**
 	 * This can be called from Operating system. It will kill this job without
 	 * any exception. Closing Pipes is done in OS, so dont worry.
 	 */
 	public void SIG_KILL(){
 		//TODO improve SIG_KILL
 		//this.status = JobStatus.KILLED;
 		System.out.println("SIG_KILL PID " + this.PID);
 		this.interrupt();
 	}
 	
 	/**
 	 * Here will be the code, which actualy makes the job done. 
 	 * <br><br>
 	 * Example: Cat will be just looking in Pipe stdIn and writing everything 
 	 * to stdOut. This method will be started from Shell! And it will be thread.
 	 * <br><br>
 	 * If anything goes wrong, this job will write it to Pipe stdErr (like wrong
 	 * parameters, missing input / missing output etc...
 	 * <br><br>
 	 * The Job itself has to check its resources! The shell can not know, if job
 	 * needs output, input, parameters or everything at once! 
 	 * <br><br>
 	 * <b> InterruptedException shall not be catched by your job</b>, send it
 	 * back to this abstract class: Thread.currentThread().interrupt();
 	 * 
 	 */
 	protected abstract void getJobDone() throws InterruptedException;
 	
 	
 	@Override
 	public void run() {
 		super.run();
 
 		try {
 			this.status = JobStatus.RUNNING;
 			this.getJobDone();
 			this.jobIsDone();
 			
 		} catch (InterruptedException e) {
 			this.status = JobStatus.KILLED;
 			this.closePipes();
 			return;
 		}
 		
 		
 	}
 	
 	/**
 	 * This will tell the OS, that we have finished our work (even with errors
 	 * in stdErr) and close any stdPipes we still have opened.
 	 */
 	private void jobIsDone(){
 		
 		this.status = JobStatus.ENDED;
 		this.closePipes();
 		return;
 	}
 	
 	/**
 	 * Close every pipes which was used by this job.
 	 */
 	private void closePipes(){
 		closeSafe(this.stdOut);
 		closeSafe(this.stdErr);
 		closeSafe(this.stdIn);
 		
 		return;
 	}
 	
 	/**
 	 * Will try to close any pipe in SAFE way.
 	 * 
 	 * @param pipe to be closed.
 	 */
 	private static void closeSafe(Pipe pipe){
 		
 		if (pipe == null){
 			return;
 		}
 		
 		try {
 			pipe.close();
 		} catch (IOException e) {
 			//TODO delete this in final build.
 			e.printStackTrace();
 		} catch (InterruptedException e) {
 			return;
 		}
 	}
 
 
 	/**
 	 * @return status of the job.
 	 */
 	public JobStatus getStatus() {
 		return (this.status);
 	}
 
 
 
 	/**
 	 * Sets stdIn for this job.
 	 * 
 	 * @param stdIn
 	 */
 	public void setStdIn(Pipe stdIn) {
 		closeSafe(this.stdIn);
 		this.stdIn = stdIn;
 	}
 
 
 
 	/**
 	 * Sets stdOut for this job.
 	 * 
 	 * @param stdOut
 	 */
 	public void setStdOut(Pipe stdOut) {
 		closeSafe(this.stdOut);
 		this.stdOut = stdOut;
 	}
 
 
 	/**
 	 * Sets stdErr for this job. The stdErr must never be null!
 	 * 
 	 * @param stdErr
 	 */
 	public void setStdErr(Pipe stdErr) {
 		
 		if(stdErr == null){
 			return;
 		}
 		
 		closeSafe(this.stdErr);
 		this.stdErr = stdErr;
 	}
 
 
 	/**
 	 * Sets the arguments for this job. The first argument must be current directory!
 	 * 
 	 * @param arguments to set.
 	 */
 	public void setArguments(String[] arguments) {
 		if(arguments.length == 0)
 			return;
 		this.path = arguments[0];
		//FIXME this is not in Java 1.5 (at least for my Linux Java)
		//this.arguments = Arrays.copyOfRange(arguments, 1, arguments.length);
 		
 	}
 	
 	
 	/**
 	 * Push your data to mighty Pipe!
 	 * <br><br>
 	 * Your thread may block on this method.
 	 * 
 	 * @param dataIn you want to push in.
 	 * @param off offset, where to start.
 	 * @param len how many data to write.
 	 * @throws IOException many reasons why it can fail.
 	 * @throws InterruptedException SIG_KILL came while thread was waiting for
 	 * 	empty buffer to write in.
 	 * 			
 	 */
 	protected void pushData(char [] dataIn, int off, int len) 
 			throws IOException, InterruptedException{
 		
 		this.status = JobStatus.WAITING;
 		
 		this.stdOut.pushData(dataIn, off, len, this.PID);
 		
 		this.status = JobStatus.RUNNING;
 	}
 	
 	
 	/**
 	 * Get some data from mighty Pipe!
 	 * 
 	 * @param destination where the data will be copied to. 
 	 * Size has to be BUF_SIZE or bigger.
 	 * 
 	 * @return number of successfuly written data to your destination.
 	 * -1: EOF.
 	 * @throws IOException many reasons why it can fail.
 	 * @throws InterruptedException SIG_KILL came while thread was waiting for
 	 * 	full buffer to read from.
 	 */
 	protected int getData(char [] destination) 
 			throws IOException, InterruptedException{
 		
 		this.status = JobStatus.WAITING;
 		
 		int index;
 		index = this.stdIn.getData(destination);
 		
 		this.status = JobStatus.RUNNING;
 		return(index);
 	}
 	
 	
 	/**
 	 * Get some data from mighty Pipe and PID of it's author.
 	 * 
 	 * @param destination where the data will be copied to. 
 	 * Size has to be BUF_SIZE or bigger.
 	 * @param pid destination where the PID of the sender will be written (pointer).
 	 * 
 	 * @return number of successfuly written data to your destination.
 	 * -1: EOF.
 	 * @throws IOException many reasons why it can fail.
 	 * @throws InterruptedException SIG_KILL came while thread was waiting for
 	 * 	full buffer to read from.
 	 */
 	protected int getDataPID(char [] destination, Integer [] pid) 
 			throws IOException, InterruptedException{
 		
 		this.status = JobStatus.WAITING;
 		
 		int index;
 		index = this.stdIn.getData(destination, pid);
 		
 		this.status = JobStatus.RUNNING;
 		return(index);
 		
 	}
 	
 	
 	
 	/**
 	 * Push your error message to mighty Pipe!
 	 * <br><br>
 	 * Your thread may block on this method.
 	 * 
 	 * @param dataIn you want to push in.
 	 * @param off offset, where to start.
 	 * @param len how many data to write.
 	 * @throws InterruptedException 
 	 * 
 	 */
 	protected void pushError(char [] errorMessage, int off, int len) 
 			throws InterruptedException {
 		
 		try {
 			this.status = JobStatus.WAITING;
 			
 			this.stdErr.pushData(errorMessage, off, len, this.PID);
 			
 			this.status = JobStatus.RUNNING;
 			
 		} catch (IOException e) {
 			e.printStackTrace();
 			System.err.println("PID<" + this.PID 
 					+ ">FAILED TO WRITE ERROR TEXT: " + String.valueOf(errorMessage));
 			
 		} 
 		
 	}
 	
 	
 	
 
 	/**
 	 * Push your error message to mighty Pipe! Now in String.
 	 * <br><br>
 	 * Your thread may block on this method.
 	 * 
 	 * @param errorMessage Why did your job failed... Now in String.
 	 * @throws InterruptedException 
 	 */
 	protected void pushError(String errorMessage) throws InterruptedException {
 		
 		try {
 			
 			this.status = JobStatus.WAITING;
 			
 			char [] err = errorMessage.toCharArray();
 			this.stdErr.pushData(err, 0, err.length, this.PID);
 			
 			this.status = JobStatus.RUNNING;
 		
 		} catch (IOException e) {
 			e.printStackTrace();
 			System.out.println("PID<" + this.PID 
 					+ ">FAILED TO WRITE ERROR TEXT: " + errorMessage);
 			
 		} 
 	}
 	
 }
