 package org.cloudsicle.master;
 
 import java.util.concurrent.ConcurrentHashMap;
 
 import org.cloudsicle.messages.JobMetaData;
 
 public class Monitor {
 
 	private ConcurrentHashMap<Integer, JobMetaData> waitingJobs;
 	private ConcurrentHashMap<Integer, JobMetaData> runningJobs;
 	private ConcurrentHashMap<Integer, JobMetaData> finishedJobs;
 	private ConcurrentHashMap<Integer, JobMetaData> failedJobs;
 
 	public Monitor() {
 		waitingJobs = new ConcurrentHashMap<Integer, JobMetaData>();
 		runningJobs = new ConcurrentHashMap<Integer, JobMetaData>();
 		finishedJobs = new ConcurrentHashMap<Integer, JobMetaData>();
 		failedJobs = new ConcurrentHashMap<Integer, JobMetaData>();
 
 	}
 
 	public void addjobWaiting(JobMetaData job) {
 		waitingJobs.put(job.getId(), job);
 	}
 
 	public void moveJobToRunning(int id) {
 		JobMetaData job = waitingJobs.remove(id);
 		runningJobs.put(id, job);
 		
 	}
 
 	public void moveJobToFinished(int id) {
 		JobMetaData job = runningJobs.remove(id);
 		job.setEndtime(System.currentTimeMillis());
 		finishedJobs.put(id, job);
 	}
 	
 	/**
 	 * Add the job to the failed jobs list and also add it to waiting again.
 	 * @param id
 	 * @return The job that failed
 	 */
 	public JobMetaData jobFailed(int id){
 		JobMetaData job = runningJobs.remove(id);
 		failedJobs.put(id, job);
 		addjobWaiting(job);
 		
 		return job;
 	}
 
 	public String toString() {
 		String status = "Jobs waiting for execution: " + waitingJobs.size()
 				+ "\n" + "Jobs running: " + runningJobs.size() + "\n"
 				+ "Finished jobs: " + finishedJobs.size() + "\n" +
 				"Failed jobs (resubmitted): " + failedJobs.size() + "\n";
 		long totaltime = 0;
 		for(JobMetaData job : finishedJobs.values()){
 			totaltime += (job.getEndtime() - job.getStarttime());
 		}
		long average = 0;
		if(finishedJobs.size() > 0) average = totaltime / finishedJobs.size();
 		status += "Average running time of jobs: " + average + " ms";
 		return status;
 	}
 }
