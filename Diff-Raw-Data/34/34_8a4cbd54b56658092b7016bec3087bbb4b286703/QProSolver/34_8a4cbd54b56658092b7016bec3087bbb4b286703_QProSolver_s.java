 package main.java.slave.solver;
 
 import java.io.BufferedOutputStream;
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.OutputStreamWriter;
 import java.io.UnsupportedEncodingException;
 import java.util.Date;
 import java.util.Random;
 
 import main.java.StreamGobbler;
 import main.java.logic.TransmissionQbf;
 
 import org.apache.commons.exec.CommandLine;
 import org.apache.commons.exec.DefaultExecuteResultHandler;
 import org.apache.commons.exec.DefaultExecutor;
 import org.apache.commons.exec.ExecuteException;
 import org.apache.commons.exec.ExecuteWatchdog;
 import org.apache.commons.exec.Executor;
 import org.apache.commons.exec.PumpStreamHandler;
 import org.apache.commons.exec.ShutdownHookProcessDestroyer;
 import org.apache.log4j.Logger;
 
 /**
  * This class encapsulates the qpro-solver
  * 
  * @author thomasm
  * 
  */
 public class QProSolver extends Solver {
 
 	static Logger logger = Logger.getLogger(QProSolver.class);
 
 	public static final String toolId = "qpro";
 
 	private String inputString = null;
 
 	protected static Object killMutex = new Object();
 
 	private Date qproProcessStartedAt = null;
 	private Date qproProcessStoppedAt = null;
 
 	private ExecuteWatchdog watchdog = null;
 
 	public QProSolver(TransmissionQbf tqbf, ResultHandler handler) {
 		super(tqbf, handler);
 	}
 
 	public void run() {
 
 		// generateQproInput();
 
 		this.inputString = toInputString(this.tqbf);
 		if (inputString.equals("true")) {
 			logger.info("Formula collapsed");
 			returnWithSuccess(tqbfId, jobId, true);
 			return;
 		} else if (inputString.equals("false")) {
 			logger.info("Formula collapsed");
 			returnWithSuccess(tqbfId, jobId, false);
 			return;
 		}
 
 		this.tqbf = null;
 		System.gc();
 
 		Executor executor = new DefaultExecutor();
 
 		watchdog = new ExecuteWatchdog(ExecuteWatchdog.INFINITE_TIMEOUT);
 		executor.setWatchdog(watchdog);
 
 		ShutdownHookProcessDestroyer processDestroyer = new ShutdownHookProcessDestroyer();
 		executor.setProcessDestroyer(processDestroyer);
 
 		CommandLine command = new CommandLine("qpro");
 		DefaultExecuteResultHandler resultHandler = new DefaultExecuteResultHandler();
 
 		ByteArrayInputStream input;
 		try {
 			input = new ByteArrayInputStream(inputString.getBytes("ISO-8859-1"));
 		} catch (UnsupportedEncodingException e1) {
 			logger.error("", e1);
 			returnWithError(tqbfId, jobId, e1);
 			return;
 		}
 		ByteArrayOutputStream output = new ByteArrayOutputStream();
 
 		executor.setStreamHandler(new PumpStreamHandler(output, null, input));
 
 		this.qproProcessStartedAt = new Date();
 		logger.info("Starting qpro process... (" + tqbfId + ")");
 		try {
 			executor.execute(command, resultHandler);
 		} catch (ExecuteException e) {
 			logger.error("", e);
 			returnWithError(tqbfId, jobId, e);
 			return;
 		} catch (IOException e) {
 			logger.error("", e);
 			returnWithError(tqbfId, jobId, e);
 			return;
 		}
 
 		while (!resultHandler.hasResult()) {
 			try {
 				resultHandler.waitFor();
 			} catch (InterruptedException e1) {
 			}
 		}
 		this.qproProcessStoppedAt = new Date();
 		logger.info("qpro process terminated... (" + tqbfId + ")");
 
 		
 		if (killed)
 			return;
 
 		try {
 			handleResult(output.toString("ISO-8859-1"));
 		} catch (UnsupportedEncodingException e) {
 			logger.error("", e);
 			returnWithError(tqbfId, jobId, e);
 			return;
 		}
 		
 
 	}
 
 	private void handleResult(String readString) {
 		if(killed)
 			return;
 		
 		logger.info("qpro return string: " + readString);
 		
 		long solverTime = this.qproProcessStoppedAt.getTime()
 				- this.qproProcessStartedAt.getTime();
 		// If qpro returns 1 the subformula is satisfiable
 		if (readString.startsWith("1")) {
 			returnWithSuccess(tqbfId, jobId, true, solverTime);
 
 			// IF qpro returns 0 the subformula is unsatisfiable
 		} else if (readString.startsWith("0")) {
 			returnWithSuccess(tqbfId, jobId, false, solverTime);
 			// anything else is an error
 		} else {
 			String errorString = "Unexpected result from solver.\n"
 					+ "	Return String: " + readString + "\n" + "	TQbfId:		 : "
 					+ tqbfId + "\n";
 			returnWithError(tqbfId, jobId, new Exception(errorString));
 		}
 	}
 
 	/**
 	 * make a formula in qpro format from the transmission QBF
 	 * 
 	 * @param t
 	 *            the QBF the slave gets from the master
 	 * @return a string representation of the tree in QPRO format
 	 */
 	private static String toInputString(TransmissionQbf t) {
 		String traversedTree = "";
 		t.assignTruthValues();
 		t.reduceFast();
 
 		if (t.rootIsTruthNode()) {
 			if (t.rootGetTruthValue()) {
 				return "true";
 			}
 			return "false";
 		}
 
 		// check if there are still occurences of all- and exist-quantified vars
 		// left in the tree after reducing. if not, remove them from aVars and
 		// eVars
 		t.eliminateOrphanedVars();
 
 		// traverse the tree to get a string in qpro format
 		traversedTree += "QBF\n" + (t.getMaxVar()) + "\n";
 		traversedTree += t.traverseTree(); // <- actual traversion happens here
 		traversedTree += "QBF\n";
 		logger.debug("traversing finished");
 		return traversedTree;
 	}
 
 	@Override
 	public Thread getThread() {
 		return thread;
 	}
 	
 	@Override
 	public void kill() {
 		this.killed = true;
		
 		watchdog.destroyProcess();
 		if (!watchdog.killedProcess()) {
 			logger.error("qpro process wasnt killed!");
 		}
 
 	}
 }
