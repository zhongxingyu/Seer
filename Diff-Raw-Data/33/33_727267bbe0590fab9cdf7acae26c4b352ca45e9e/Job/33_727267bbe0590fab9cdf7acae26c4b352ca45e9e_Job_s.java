 package main.java.master;
 
 import java.io.IOException;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Vector;
 
 import javax.swing.table.AbstractTableModel;
 
 import org.apache.log4j.Logger;
 
 import main.java.logic.Qbf;
 import main.java.logic.TransmissionQbf;
 
 public class Job {
 
 	private static int idCounter = 0;
 	private static Vector<Job> jobs = new Vector<Job>();
 	private static AbstractTableModel tableModel;
 	static Logger logger = Logger.getLogger(MasterDaemon.class);
 	
 	private static void addJob(Job job) {
 		jobs.add(job);
 		if (tableModel != null) {
 			tableModel.fireTableDataChanged();
 		}
 		logger.info("Job added. JobId: " + job.id);
 	}
 
 	private static String allocateJobId() {
 		idCounter++;
 		return new Integer(idCounter).toString();
 	}
 
 	public static void createJob(String inputFile, String outputFile,
 			String solverId, String heuristicId) {
 		Job job = new Job();
 		job.setId(allocateJobId());
 		job.setInputFileString(inputFile);
 		job.setOutputFileString(outputFile);
 		job.setSolver(solverId);
 		job.setHeuristic(heuristicId);
 		job.setStatus("Not started");
 		addJob(job);
 		logger.info("Job created. Id: " + job.getId() + ",\n" +
 				"HeuristicId: " + job.getHeuristic() + ",\n" +
 				"SolverId: " + job.getSolver() + ",\n" +
 				"Inputfile: " + job.getInputFileString() + ",\n" +
 				"Outputfile: " + job.getOutputFileString() + "\n");
 	}
 
 	public static Vector<Job> getJobs() {
 		if (jobs == null) {
 			jobs = new Vector<Job>();
 		}
 		return jobs;
 	}
 
 	public static AbstractTableModel getTableModel() {
 		return tableModel;
 	}
 
 	public static void setTableModel(AbstractTableModel tableModel) {
 		Job.tableModel = tableModel;
 	}
 
 	private Qbf formula;
 	// Maps tqbfids with to the computing slaves
 	private Map<String, Slave> formulaDesignations = new HashMap<String, Slave>();
 	private String heuristic;
 	private String id;
 
 	private String inputFileString;
 
 	private String outputFileString;
 
 	private String solver;
 
 	private Date startedAt;
 
 	private String status;
 
 	private Date stoppedAt;
 
 	private List<TransmissionQbf> subformulas;
 
 	public void abort() {
 		logger.info("Aborting Job " + this.id + "...\n");
 		logger.info("Aborting Formulas. Sending AbortFormulaMessages to corresponding slaves...");
 		for (Map.Entry<String, Slave> entry : this.formulaDesignations
 				.entrySet()) {
 			Slave s = entry.getValue();
 			String tqbfId = entry.getKey();
 			s.abortFormulaComputation(tqbfId);
 		}
 
 		tableModel.fireTableDataChanged();
 		logger.info("AbortMessages sent.");
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
 
 	public String getStatus() {
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
 
 	public void setStatus(String status) {
 		this.status = status;
 	}
 
 	public void setStoppedAt(Date stoppedAt) {
 		this.stoppedAt = stoppedAt;
 	}
 
 	public void start() {
 		logger.info("Starting Job " + this.id + "...\n");
 		this.startedAt = new Date();
 		try {
 			this.formula = new Qbf(inputFileString);
 		} catch (IOException e) {
			logger.error("Error while reading formula file: " + e.getCause());
 		}
 		int availableCores = Slave.getCoresForSolver(this.solver);
 		this.subformulas = formula.splitQbf(availableCores);
 		List<Slave> slaves = Slave.getSlavesForSolver(this.solver);
 
 		for (int i = 0; i < subformulas.size(); i++) {
 			Slave designatedSlave = slaves.get(i % slaves.size());
 			subformulas.get(i).setStatus("computing");
 			designatedSlave.computeFormula(subformulas.get(i), this);
 			formulaDesignations.put(subformulas.get(i).getId(), designatedSlave);
 		}
 
 		
 		logger.info("Job started. Splitted into " + this.subformulas.size() + " subformulas, " +
 					"with " + availableCores + " available Cores on " + slaves.size() + " slaves.");
 		this.status = "Started";
 		tableModel.fireTableDataChanged();
 	}
 
 }
