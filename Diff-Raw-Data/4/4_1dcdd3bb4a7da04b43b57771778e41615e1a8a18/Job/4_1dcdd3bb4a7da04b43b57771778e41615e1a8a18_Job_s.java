 package main.java.master;
 
 import java.io.BufferedWriter;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.ObjectOutputStream;
 import java.net.Socket;
 import java.net.UnknownHostException;
 import java.rmi.RemoteException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.concurrent.BlockingQueue;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.ConcurrentMap;
 import java.util.concurrent.LinkedBlockingQueue;
 import java.util.zip.GZIPOutputStream;
 
 import javax.swing.SwingUtilities;
 import javax.swing.table.AbstractTableModel;
 
 import main.java.QPar;
 import main.java.StackTraceUtil;
 import main.java.logic.Qbf;
 import main.java.logic.TransmissionQbf;
 import main.java.logic.heuristic.HeuristicFactory;
 import main.java.logic.parser.TokenMgrError;
 import main.java.master.console.Shell;
 import main.java.rmi.Result;
 import main.java.rmi.SlaveRemote;
 
 import org.apache.commons.io.output.CountingOutputStream;
 import org.apache.log4j.Logger;
 
 public class Job {
 
 	public enum Status {
 		READY, RUNNING, COMPLETE, ERROR, TIMEOUT
 	}
 
 	private static int idCounter = 0;
 	private static Map<String, Job> jobs = new HashMap<String, Job>();
 	private static AbstractTableModel tableModel;
 	static Logger logger = Logger.getLogger(Job.class);
 
 	private boolean result;
 	private long timeout = 0;
 	private Qbf formula;
 
 	public volatile ConcurrentMap<String, SlaveRemote> formulaDesignations = new ConcurrentHashMap<String, SlaveRemote>();
 	public volatile BlockingQueue<String> acknowledgedComputations = new LinkedBlockingQueue<String>();
 	public ArrayList<Long> solverTimes = new ArrayList<Long>();
 	private String heuristic, id, inputFileString, outputFileString, solver;
 	private int usedCores = 0, resultCtr = 0;
 	private volatile Status status;
 	private List<TransmissionQbf> subformulas;
 	private Date startedAt = null, stoppedAt = null;
 
 	public Job() {
 		logger.setLevel(QPar.logLevel);
 	}
 
 	private static void addJob(Job job) {
 		jobs.put(job.id, job);
 		if (tableModel != null) {
 			tableModel.fireTableDataChanged();
 		}
 		logger.info("Job added. JobId: " + job.id);
 	}
 
 	private static String allocateJobId() {
 		idCounter++;
 		return new Integer(idCounter).toString();
 	}
 
 	public static Job createJob(String inputFile, String outputFile,
 			String solverId, String heuristicId, long timeout, int maxCores) {
 		Job job = new Job();
 		job.usedCores = maxCores;
 		job.setTimeout(timeout);
 		job.setId(allocateJobId());
 		job.setInputFileString(inputFile);
 		job.setOutputFileString(outputFile);
 		job.setSolver(solverId);
 		job.setHeuristic(heuristicId);
 		job.setStatus(Status.READY);
 		addJob(job);
 		logger.info("Job created. \n" + "	JobId:        " + job.getId() + "\n"
 				+ "	HeuristicId:  " + job.getHeuristic() + "\n"
 				+ "	SolverId:     " + job.getSolver() + "\n"
 				+ "	Inputfile:    " + job.getInputFileString() + "\n"
 				+ "	Outputfile:   " + job.getOutputFileString() + "\n");
 		return job;
 	}
 
 	public static Map<String, Job> getJobs() {
 		if (jobs == null) {
 			jobs = new HashMap<String, Job>();
 		}
 		return jobs;
 	}
 
 	public void abort() {
 		if (this.getStatus() != Status.RUNNING)
 			return;
 		logger.info("Aborting Job " + this.id + "...");
 		logger.info("Aborting Formulas. Sending AbortFormulaMessages to slaves...");
 		try {
 			abortComputations();
 		} catch (RemoteException e) {
 			logger.error("Aborting Computations failed.", e);
 		}
 		this.setStatus(Status.ERROR);
 		if (tableModel != null)
 			tableModel.fireTableDataChanged();
 		logger.info("AbortMessages sent.");
 		this.freeResources();
 	}
 
 	private void abortComputations() throws RemoteException {
 		String tqbfId = null;
 		while(this.formulaDesignations.size() > 0) {
 			try {
 				tqbfId = this.acknowledgedComputations.take();
 			} catch (InterruptedException e) {}
 			logger.info("Aborting Formula " + tqbfId + " ...");
 			SlaveRemote designation = this.formulaDesignations.get(tqbfId);
 			if(designation != null) {
 				designation.abortFormula(tqbfId);
 				this.formulaDesignations.remove(tqbfId);
 				logger.info("Formula " + tqbfId + " aborted.");
 			}
 		}
 		
 	}
 
 	public void startBlocking() {
 		this.start();
 		while (this.getStatus() == Status.RUNNING) {
 			try {
 				Thread.sleep(500);
 			} catch (InterruptedException e) {
 			}
 
 			if ((startedAt.getTime() + timeout) < new Date().getTime()) {
 				logger.info("Timeout reached. Aborting Job. \n"
 						+ "	Job Id:         " + this.id + "\n"
 						+ "	Timeout (secs): " + timeout / 1000 + "\n");
 				this.abort();
 				this.setStatus(Status.TIMEOUT);
 				break;
 			}
 		}
 	}
 
 	public void start() {
 		int availableCores = 0;
 		this.setStatus(Status.RUNNING);
 		if (tableModel != null)
 			tableModel.fireTableDataChanged();
 		try {
 			this.formula = new Qbf(inputFileString);
 		} catch (IOException e) {
 			logger.error(this.inputFileString, e);
 			this.setStatus(Status.ERROR);
 			return;
 		} catch(TokenMgrError e) {
 			logger.error(this.inputFileString, e);
 			this.setStatus(Status.ERROR);
 			return;
 		}
 
 		ArrayList<SlaveRemote> slots = new ArrayList<SlaveRemote>();
 		ArrayList<SlaveRemote> slaves;
 		try {
 			slaves = Master.getSlavesWithSolver(this.solver);
 
 			for (SlaveRemote slave : slaves) {
 				availableCores += slave.getCores();
 				for (int i = 0; i < slave.getCores(); i++) {
 					slots.add(slave);
 				}
 			}
 		} catch (RemoteException e) {
 			logger.error(e);
 			this.setStatus(Status.ERROR);
 			return;
 		}
 
 		logger.debug("Available Cores: " + availableCores + ", Used Cores: "
 				+ usedCores);
 
 		Collections.shuffle(slots);
 		String slotStr = "";
 		try {
 			for (SlaveRemote s : slots)
 				slotStr += s.getHostName() + " ";
 		} catch (RemoteException e) {
 			logger.error(e);
 			this.setStatus(Status.ERROR);
 			return;
 		} catch (UnknownHostException e) {
 			logger.error(e);
 			this.setStatus(Status.ERROR);
 			return;
 		}
 
 		logger.debug("Computationslots generated: " + slotStr.trim());
 
 		this.startedAt = new Date();
 		this.subformulas = formula.splitQbf(
 				Math.min(availableCores, usedCores), HeuristicFactory
 						.getHeuristic(this.getHeuristic(), this.formula));
 
 		if (slots.size() < this.subformulas.size()) {
 			logger.error("Not enough cores available for Job. Job failed.");
 			this.setStatus(Status.ERROR);
 			return;
 		}
 
 		logger.info("Job started " + this.id + "...\n" + "	Started at:  "
 				+ startedAt + "\n" + "	Subformulas: " + this.subformulas.size()
 				+ "\n" + "	Cores(avail):" + availableCores + "\n"
 				+ "	Cores(used): " + usedCores + "\n" + "	Slaves:      "
 				+ slaves.size());
 
 		int slotIndex = 0;
 		for (TransmissionQbf sub : subformulas) {
 			synchronized (this) {
 				if (this.getStatus() != Status.RUNNING)
 					return;
 				sub.solverId = this.solver;
 				sub.jobId = this.getId();
 				SlaveRemote s = slots.get(slotIndex);
 				slotIndex += 1;
 
 				try {
 					new Thread(new TransportThread(s, sub, this.solver))
 							.start();
 				} catch (UnknownHostException e) {
 					logger.error(StackTraceUtil.getStackTrace(e));
 				} catch (RemoteException e) {
 					logger.error(StackTraceUtil.getStackTrace(e));
 				} catch (IOException e) {
 					logger.error(StackTraceUtil.getStackTrace(e));
 				}
 				formulaDesignations.put(sub.getId(), s);
 				if (slotIndex >= this.subformulas.size()) // roundrobin if
 															// overbooked
 					slotIndex = 0;
 			}
 		}
 	}
 
 	class TransportThread implements Runnable {
 		TransmissionQbf sub = null;
 		String solver = null;
 		SlaveRemote s = null;
 		Socket senderSocket;
 		private ObjectOutputStream oos;
 
 		public TransportThread(SlaveRemote s, TransmissionQbf sub, String solver)
 				throws UnknownHostException, RemoteException, IOException {
 			this.sub = sub;
 			this.solver = solver;
 			this.s = s;
 			// logger.info("hostname: " + s.getHostName());
 			senderSocket = new Socket(s.getHostName(), 11111);
 		}
 
 		@Override
 		public void run() {
 			try {
 				logger.info("Sending formula " + sub.getId() + " ...");
 				// oos = new ObjectOutputStream(senderSocket.getOutputStream());
 				CountingOutputStream cos = new CountingOutputStream(senderSocket.getOutputStream());
 				oos = new ObjectOutputStream(new GZIPOutputStream(cos));
 				long start = System.currentTimeMillis();
 				oos.writeObject(sub);
 				long stop = System.currentTimeMillis();
 				oos.flush();
 				oos.close();
 				senderSocket.close();
 				long kiB = cos.getByteCount()/1024;
				logger.info("Formula " + sub.getId() + " sent ... (" + kiB + "kiB, " + (kiB/1000) + " seconds, " + kiB / ((double)(stop - start)/1000.00) + "kiB/s)");
 			} catch (IOException e) {
 				logger.error("While sending formula " + sub.getId(), e);
 			}
 		}
 	}
 
 	public void setResult(boolean result) {
 		this.result = result;
 	}
 
 	public boolean getResult() {
 		return result;
 	}
 
 	synchronized public void fireJobCompleted(boolean result) {
 		if (this.getStatus() != Status.RUNNING)
 			return;
 		this.setStatus(Status.COMPLETE);
 		this.setStoppedAt(new Date());
 		logger.info("Job complete. Resolved to: " + result
 				+ ". Aborting computations.");
 		try {
 			this.abortComputations();
 		} catch (RemoteException e) {
 			logger.error("Aborting computations failed.", e);
 		}
 		this.setResult(result);
 
 		// Write the results to a file
 		// But only if we want that. In case of a evaluation
 		// the outputfile is set to null
 		if (this.getOutputFileString() != null) {
 			try {
 				BufferedWriter out = new BufferedWriter(new FileWriter(
 						this.getOutputFileString()));
 				out.write(resultText());
 				out.flush();
 			} catch (IOException e) {
 				logger.error(e);
 			}
 		}
 
 		if (Shell.getWaitfor_jobid().equals(this.getId())) {
 			synchronized (Master.getShellThread()) {
 				Master.getShellThread().notify();
 			}
 		}
 		if (Job.getTableModel() != null)
 			Job.getTableModel().fireTableDataChanged();
 		this.freeResources();
 	}
 
 	private void freeResources() {
 		this.formula = null;
 		this.formulaDesignations = null;
 		this.subformulas = null;
 		System.gc();
 	}
 
 	public long totalMillis() {
 		// if(this.status != Job.COMPLETE)
 		// return -1;
 		return this.getStoppedAt().getTime() - this.getStartedAt().getTime();
 	}
 
 	public long totalSecs() {
 		// if(this.status != Job.COMPLETE)
 		// return -1;
 		return (this.getStoppedAt().getTime() - this.getStartedAt().getTime()) / 1000;
 	}
 
 	private String resultText() {
 		String txt;
 		txt = "Job Id: " + this.getId() + "\n" + "Started at: "
 				+ this.getStartedAt() + "\n" + "Stopped at: "
 				+ this.getStoppedAt() + "\n" + "Total secs: " + totalSecs()
 				+ "\n" + "In millis: " + totalMillis() + "\n" + "Solver: "
 				+ this.getSolver() + "\n" + "Heuristic: " + this.getHeuristic()
 				+ "\n" + "Result: "
 				+ (this.getResult() ? "Solvable" : "Not Solvable") + "\n";
 
 		return txt.replaceAll("\n", System.getProperty("line.separator"));
 	}
 
 	private void handleResult(String tqbfId, boolean result) {
 		resultCtr++;
 		boolean solved = formula.mergeQbf(tqbfId, result);
 		logger.info("Result of tqbf(" + tqbfId + ") merged into Qbf of Job "
 				+ getId() + " (" + result + ")");
 		this.formulaDesignations.remove(tqbfId);
 		if (solved)
 			fireJobCompleted(formula.getResult());
 		else {
 			if (resultCtr == subformulas.size()) {
 				// Received all subformulas but still no result...something is
 				// wrong
 				logger.fatal("Merging broken!");
 				logger.fatal("Dumping decisiontree: \n"
 						+ formula.decisionRoot.dump());
 				System.exit(-1);
 			}
 		}
 	}
 
 	synchronized public void handleResult(Result r) {
 		if(r.solverTime > 0)
 			this.solverTimes.add(r.solverTime);
 		if (this.getStatus() != Status.RUNNING) {
 			return;
 		}
 
 		if (r.type != Result.Type.ERROR) {
 			handleResult(r.tqbfId, r.type == Result.Type.TRUE ? true : false);
 			return;
 		}
 
 		logger.error("Slave returned error for subformula: " + r.tqbfId, r.exception);
 		abort();
 	}
 
 	public Qbf getFormula() {
 		return formula;
 	}
 
 	public String getHeuristic() {
 		return heuristic;
 	}
 
 	public String getId() {
 		return id;
 	}
 
 	public String getInputFileString() {
 		return inputFileString;
 	}
 
 	public String getOutputFileString() {
 		return outputFileString;
 	}
 
 	public String getSolver() {
 		return solver;
 	}
 
 	public Date getStartedAt() {
 		return startedAt;
 	}
 
 	synchronized public Status getStatus() {
 		return status;
 	}
 
 	public Date getStoppedAt() {
 		return stoppedAt;
 	}
 
 	public void setFormula(Qbf formula) {
 		this.formula = formula;
 	}
 
 	public void setHeuristic(String heuristic) {
 		this.heuristic = heuristic;
 	}
 
 	public void setId(String id) {
 		this.id = id;
 	}
 
 	public void setInputFileString(String inputFileString) {
 		this.inputFileString = inputFileString;
 	}
 
 	public void setOutputFileString(String outputFileString) {
 		this.outputFileString = outputFileString;
 	}
 
 	public void setSolver(String solver) {
 		this.solver = solver;
 	}
 
 	public void setStartedAt(Date startedAt) {
 		this.startedAt = startedAt;
 	}
 
 	synchronized public void setStatus(Status status) {
 		this.status = status;
 		if (Job.getTableModel() != null) {
 			SwingUtilities.invokeLater(new Runnable() {
 				public void run() {
 					Job.getTableModel().fireTableDataChanged();
 				}
 			});
 		}
 	}
 
 	public void setStoppedAt(Date stoppedAt) {
 		// Only allow this once, in case of an erroneous second call
 		if (this.stoppedAt == null)
 			this.stoppedAt = stoppedAt;
 	}
 
 	public static AbstractTableModel getTableModel() {
 		return tableModel;
 	}
 
 	public static void setTableModel(AbstractTableModel tableModel) {
 		Job.tableModel = tableModel;
 	}
 
 	public long getTimeout() {
 		return timeout;
 	}
 
 	public void setTimeout(long timeout) {
 		this.timeout = timeout;
 	}
 
 	public static String getStatusDescription(Status status) {
 		switch (status) {
 		case READY:
 			return "Ready";
 		case RUNNING:
 			return "Running";
 		case COMPLETE:
 			return "Complete";
 		case ERROR:
 			return "Error";
 		default:
 			return "undefined";
 		}
 	}
 	
 	public long maxSolverTime() {
 		if(solverTimes.isEmpty())
 			return 0;
 		return Collections.max(solverTimes);
 	}
 
 	public long minSolverTime() {
 		if(solverTimes.isEmpty())
 			return 0;
 		return Collections.min(solverTimes);
 	}
 	
 	public double meanSolverTime() {
 		long added = 0;
 		for(long l : solverTimes)
 			added += l;
 		
 		return added /solverTimes.size();
 	}
 	
 }
