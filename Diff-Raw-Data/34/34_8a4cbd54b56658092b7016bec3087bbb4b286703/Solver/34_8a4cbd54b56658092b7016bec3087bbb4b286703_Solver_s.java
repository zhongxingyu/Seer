 package main.java.slave.solver;
 
 import java.io.IOException;
 
 import main.java.logic.TransmissionQbf;
 import main.java.rmi.Result;
 
 import org.apache.log4j.Logger;
 
 /**
  * Basic interface which all solvers have to implement. To appear in the list of
  * solvers, you have to add your solver in the SolverFactory
  * 
  * @author thomasm
  * 
  */
 public abstract class Solver implements Runnable {
 
 	static Logger logger = Logger.getLogger(QProSolver.class);
 	protected TransmissionQbf tqbf;
 	protected Thread thread;
 	protected ResultHandler handler = null;
 	protected String tqbfId = null;
 	protected String jobId = null;
 
 	volatile protected boolean killed = false;
 	protected boolean run = true;
	protected Process solverProcess = null;

 	public Solver(TransmissionQbf tqbf, ResultHandler handler) {
 		this.handler = handler;
 		this.tqbf = tqbf;
 		this.tqbfId = tqbf.getId();
 		this.jobId = tqbf.jobId;
 	}
 
 	public abstract void kill();
 
 	public abstract void run();
 
 	public TransmissionQbf getTransmissionQbf() {
 		return this.tqbf;
 	}
 
 	public Thread getThread() {
 		return this.thread;
 	}
 
 	public void setThread(Thread t) {
 		this.thread = t;
 	}
 
 	protected void returnWithError(String tqbfId, String jobId, Exception e) {
 		Result r = new Result(tqbfId, jobId);
 		logger.error("Cant complete tqbf computation", e);
 		r.type = Result.Type.ERROR;
 		r.exception = e;
 		this.handler.handleResult(r);
 	}
 
 	protected void returnWithSuccess(String tqbfId, String jobId,
 			boolean result, long solverTime) {
 		Result r = new Result(tqbfId, jobId);
 
 		if (result)
 			r.type = Result.Type.TRUE;
 		else
 			r.type = Result.Type.FALSE;
 
 		if (solverTime > 0)
 			r.solverTime = solverTime;
 
 		logger.info("Returning result for formula " + tqbfId + ": " + r.type);
 		this.handler.handleResult(r);
 
 	}
 
 	protected void returnWithSuccess(String tqbfId, String jobId, boolean result) {
 		returnWithSuccess(tqbfId, jobId, result, 0);
 	}
 
 }
