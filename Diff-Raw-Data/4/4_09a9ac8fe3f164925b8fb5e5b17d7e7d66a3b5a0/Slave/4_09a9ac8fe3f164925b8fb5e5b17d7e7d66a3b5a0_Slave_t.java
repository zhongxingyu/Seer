 package org.cloudsicle.main.entrypoints;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.LinkedList;
 import java.util.List;
 
 import org.cloudsicle.communication.IMessageHandler;
 import org.cloudsicle.communication.INeedOwnIP;
 import org.cloudsicle.communication.SocketListener;
 import org.cloudsicle.communication.SocketSender;
 import org.cloudsicle.main.VMState;
 import org.cloudsicle.main.jobs.IJob;
 import org.cloudsicle.messages.Activity;
 import org.cloudsicle.messages.IMessage;
 import org.cloudsicle.messages.SoftExit;
 import org.cloudsicle.messages.StatusUpdate;
 import org.cloudsicle.slave.JobExecutor;
 import org.cloudsicle.slave.UnknownJobException;
 
 import com.jcraft.jsch.JSchException;
 
 public class Slave implements IMessageHandler{
 
 	private SocketListener listener;
 	
 	private List<IJob> jobQueue = Collections.synchronizedList(new LinkedList<IJob>()); 
 	private JobExecutionThread thread = null;
 	
 	/**
 	 * Initialize our Slave
 	 * 
 	 * @throws IOException If we failed to deploy gifsicle on the environment
 	 */
 	public Slave() throws IOException{
 		deployExecutable();
 		listener = new SocketListener(this);
 		listener.start();
 	}
 	
 	/**
 	 * Copy our gifsicle executable from the jar to the environment
 	 */
 	private void deployExecutable() throws IOException{
 		InputStream is = Slave.class.getResourceAsStream(File.separator + "gifsicle");
 		File f = new File("gifsicle");
 		f.createNewFile();
 		FileOutputStream fos = new FileOutputStream("gifsicle");
 		while (is.available() > 0)
 			fos.write(is.read());
 		fos.flush();
 		fos.close();
 		is.close();
 	}
 
 	@Override
 	public void process(IMessage message) {
 		if (message instanceof Activity){
 			try {
 				StatusUpdate status = new StatusUpdate("VM Received Activity", VMState.EXECUTING);
 				SocketSender sender = new SocketSender(false, ((Activity) message).getIP());		
 			
 				ArrayList<IJob> jobs = ((Activity) message).getJobs();
 				
 				synchronized (jobQueue){
 					if (jobQueue.size() == 0 && thread == null){
 						thread = new JobExecutionThread(jobQueue, sender);
 						thread.start();
 					}
 				}
 				
 				for (IJob job : jobs){
 					if (job instanceof INeedOwnIP)
 						((INeedOwnIP)job).setIP(((Activity) message).getIP());
 					synchronized (jobQueue){
 						jobQueue.add(job);
 					}
 				}
 
 				sender.send(status);
 			} catch (IOException e) {
 				e.printStackTrace();
 			} catch (JSchException e) {
 				e.printStackTrace();
 			}
 		} else if (message instanceof SoftExit){
 			SocketSender sender = new SocketSender(false, ((SoftExit) message).getIP());
 			if (thread != null){
 				//We are processing jobs
 				thread.callExitOnFinish(true);
 			} else {
 				//We are not doing anything, exit now
 				try {
 					sender.send(new StatusUpdate("VM Shutting down on soft exit request.", VMState.SHUTDOWN));
 				} catch (IOException e) {
 					e.printStackTrace();
 				} catch (JSchException e) {
 					e.printStackTrace();
 				}
 				System.exit(0);
 			}
 		}
 	}
 
 	/**
 	 * @param args
 	 * @throws IOException 
 	 */
 	public static void main(String[] args) throws IOException {
		@SuppressWarnings("unused")
 		Slave slave = new Slave();
 	}
 
 	private class JobExecutionThread extends Thread{
 
 		private final List<IJob> jobs;
 		private boolean alive = true;
 		private boolean callExit = false;
 		private SocketSender updateable;
 		
 		public JobExecutionThread(List<IJob> jobs, SocketSender sender){
 			this.jobs = jobs;
 			this.updateable = sender;
 		}
 		
 		public void callExitOnFinish(boolean exit){
 			callExit = exit;
 		}
 		
 		@Override
 		public void run() {
 			while (alive){
 				//We keep consuming jobs while they are available
 				IJob job = null;
 				synchronized (jobQueue){
 					if (jobs.size() > 0)
 						job = jobs.remove(0);
 					else 
 						alive = false;
 				}
 				//Make sure not to hog the synchronization of the queue when we don't need it
 				if (job != null)
 					executeJob(job);
 			}
 			//When our job queue is empty we can either shut down or wait for more jobs to come
 			//Our consumer Thread will stop either way
 			if (callExit){
 				try {
 					updateable.send(new StatusUpdate("VM Shutting down on soft exit request.", VMState.SHUTDOWN));
 				} catch (IOException e) {
 					e.printStackTrace();
 				} catch (JSchException e) {
 					e.printStackTrace();
 				}
 				System.exit(0);
 			} else {
 				try {
 					updateable.send(new StatusUpdate("VM Done processing all jobs.", VMState.DONE));
 				} catch (IOException e) {
 					e.printStackTrace();
 				} catch (JSchException e) {
 					e.printStackTrace();
 				}
 			}
 		}
 		
 		public void executeJob(IJob job){
 			JobExecutor executor = new JobExecutor(job);
 			try {
 				executor.run();
 			} catch (UnknownJobException e) {
 				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
 			}
 		}
 		
 	}
 }
