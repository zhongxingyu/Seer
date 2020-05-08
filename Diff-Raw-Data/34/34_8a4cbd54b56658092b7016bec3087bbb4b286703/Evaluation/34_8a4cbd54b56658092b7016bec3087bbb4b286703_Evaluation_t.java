 package main.java.master;
 
 import java.io.File;
 import java.util.HashMap;
 
 import main.java.QPar;
 
 import org.apache.log4j.Logger;
 
 /**
  * Encapsulates solving all formulas in a directory with a specified heuristic
  * @author thomasm
  *
  */
 public class Evaluation {
 
 	static 	Logger 	logger = Logger.getLogger(Evaluation.class);
 	private File 	directory;
 	private String 	heuristicId, solverId;
 	private long	timeout; // timeout in seconds
 	private int		cores;
 	
 	private int 					timeouts		= 0;
 	private int 					errors			= 0;
 	private long 					elapsedTotal 	= 0;
 	private HashMap<File, Boolean> 	results 		= new HashMap<File, Boolean>();
 	public double meanSolverTime;
 	
 	public Evaluation(	File 	directory,
 						String 	heuristicId,
 						String 	solverId,
 						long 	timeout,
 						int 	cores) {
 		this.directory 			= directory;
 		this.heuristicId		= heuristicId;
 		this.solverId			= solverId;
 		this.timeout			= timeout;
 		this.cores				= cores;
 	}
 	
 	public void evaluate() {
 		int nonEmptyCtr = 0;
 		for(File f : this.directory.listFiles()) {
 			if(f.getName().equals("evaluation.txt"))
 				continue;
 			try {
 				Job job = Job.createJob(f.getAbsolutePath(), null, solverId, heuristicId, timeout, cores);
 							
 				job.startBlocking();
 				
 				if(job.getStatus() == Job.Status.COMPLETE) {
 					//logger.info("JOB TOTAL MILLIS " + job.totalMillis());
 					//logger.info("ELAPSED TOAL " + elapsedTotal);
 					elapsedTotal += job.totalMillis();
 					results.put(f, job.getResult());
 				} else if(job.getStatus() == Job.Status.TIMEOUT){ 
 					elapsedTotal += timeout*1000;
 					timeouts++;
 				} else if(job.getStatus() == Job.Status.ERROR) {
 					elapsedTotal += timeout*1000;
 					errors++;
 				} else {
 					assert(false);
 				}
							
 				// Do the stats
 				if(!job.solverTimes.isEmpty()) {
					nonEmptyCtr++;				
 					this.meanSolverTime += job.meanSolverTime();
 				}
 				
 			} catch(Throwable t) {
 				logger.error("Evaluation.java", t);
 				QPar.sendExceptionMail(t);
 			}
 		}
 		
 	}
 
 	public String statisticsResultString() {
 		return String.format("%.2f", this.meanSolverTime/1000.00);
 	}
 	
 	public String toString() {
 		return String.format("%.2f\t%d\t%d", this.elapsedTotal/1000.00, this.timeouts, this.errors);
 	}
 	
 	public int getTimeouts() {
 		return timeouts;
 	}
 
 	public void setTimeouts(int timeouts) {
 		this.timeouts = timeouts;
 	}
 
 	public int getErrors() {
 		return errors;
 	}
 
 	public void setErrors(int errors) {
 		this.errors = errors;
 	}
 
 	public long getElapsedTotal() {
 		return elapsedTotal;
 	}
 
 	public void setElapsedTotal(long elapsedTotal) {
 		this.elapsedTotal = elapsedTotal;
 	}
 
 	public void setResults(HashMap<File, Boolean> results) {
 		this.results = results;
 	}
 
 	public HashMap<File, Boolean> getResults() {
 		return results;
 	}
 	
 }
