 package main.java.master;
 
 import java.io.Serializable;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 import java.util.Vector;
 
 import javax.jms.DeliveryMode;
 import javax.jms.Destination;
 import javax.jms.JMSException;
 import javax.jms.Message;
 import javax.jms.MessageConsumer;
 import javax.jms.MessageListener;
 import javax.jms.MessageProducer;
 import javax.jms.ObjectMessage;
 import javax.jms.Session;
 import javax.swing.table.AbstractTableModel;
 
 import main.java.logic.Qbf;
 import main.java.logic.TransmissionQbf;
 import main.java.master.gui.JobsTableModel;
 import main.java.messages.FormulaAbortedMessage;
 import main.java.messages.AbortMessage;
 import main.java.messages.FormulaMessage;
 import main.java.messages.InformationMessage;
 import main.java.messages.InformationRequestMessage;
 import main.java.messages.KillMessage;
 import main.java.messages.ResultMessage;
 import main.java.messages.ShutdownMessage;
 
 import org.apache.log4j.Logger;
 
 public class Slave implements MessageListener, Runnable {
 	static Logger logger = Logger.getLogger(MasterDaemon.class);
 	private static Vector<Slave> slaves = new Vector<Slave>();
 	private static AbstractTableModel tableModel;
 
 	private static void addSlave(Slave slave) {
 		slaves.add(slave);
 		logger.debug("Adding Slave: " + slave);
 		if (tableModel != null) {
 			tableModel.fireTableDataChanged();
 		}
 	}
 
 	public static Slave create(String hostName, int cores, Vector<String> solvers){
 		logger.info("Starting new Slaveinstance/thread...");
 		Slave instance = new Slave();
 		instance.cores = cores;
 		instance.hostName = hostName;
 		instance.setToolIds(solvers);
 		new Thread(instance).start();
 		Slave.addSlave(instance);
 		return instance;
 	}
 
 	public static Set<String> getAllAvaliableSolverIds() {
 		Set<String> allSolvers = new HashSet<String>();
 		for (Slave slave : slaves) {
 			for (String id : slave.getToolIds()) {
 				allSolvers.add(id);
 			}
 		}
 		// TODO: remove following
 		allSolvers.add("test1");
 		allSolvers.add("test2");
 		allSolvers.add("test3");
 		return allSolvers;
 	}
 
 	public static int getCoresForSolver(String solverId) {
 		int cores = 0;
 		for (Slave slave : slaves) {
 			if (slave.getToolIds().contains(solverId)) {
 				cores += slave.getCores();
 			}
 		}
 		return cores;
 	}
 
 	public static Vector<Slave> getSlaves() {
 		if (slaves == null) {
 			slaves = new Vector<Slave>();
 		}
 		return slaves;
 	}
 
 	public static Vector<Slave> getSlavesForSolver(String solverId) {
 		Vector<Slave> slavesWithSolver = new Vector<Slave>();
 
 		for (Slave slave : slaves) {
 			if (slave.getToolIds().contains(solverId)) {
 				slavesWithSolver.add(slave);
 			}
 		}
 
 		return slavesWithSolver;
 	}
 
 	public static AbstractTableModel getTableModel() {
 		return tableModel;
 	}
 
 	private static void removeSlave(Slave slave) {
 		slaves.remove(slave);
 		if (tableModel != null) {
 			tableModel.fireTableDataChanged();
 		}
 	}
 
 	public static void setTableModel(AbstractTableModel tableModel) {
 		Slave.tableModel = tableModel;
 	}
 
 	private MessageConsumer consumer_rcv;
 
 	private int cores;
 
 	private Destination destination_rcv;
 
 	private Destination destination_snd;
 
 	private String hostName;
 
 	private MessageProducer producer_snd;
 
 	private boolean running;
 
 	// Maps tqbf ids to Jobs
 	private Map<String, Job> runningComputations = new HashMap<String, Job>();
 	
 	private Session session;
 
 	private Vector<String> toolIds;
 
 	public void abortFormulaComputation(String tqbfId) {
 		this.sendAbortMessage(tqbfId);
 	}
 
 	public void computeFormula(TransmissionQbf tqbf, Job job) {
 		this.sendFormulaMessage(tqbf);
 		this.runningComputations.put(tqbf.getId(), job);
 	}
 
 	public int getCores() {
 		return cores;
 	}
 
 	public String getHostName() {
 		return hostName;
 	}
 
 	public Map<String, Job> getRunningComputations() {
 		return runningComputations;
 	}
 
 	public Vector<String> getToolIds() {
 		return toolIds;
 	}
 
	private void handleFormulaAbortedMessage(FormulaAbortedMessage m) {
 		logger.info("Receiving AbortConfirmMessage from " + this.getHostName());
 		this.runningComputations.remove(m.getTqbfId());
 		logger.info("Removed tqbf(" + m.getTqbfId() + ") from running computations.");
 	}
 
 	private void handleInformationMessage(InformationMessage m) {
 		logger.info("Receiving InformationMessage from " + this.getHostName());
 		this.setHostName(m.getHostName());
 		this.setCores(m.getCores());
 		this.setToolIds(m.getToolIds());
 		if (tableModel != null) {
 			tableModel.fireTableDataChanged();
 		}
 		logger.info("Slave information updated.");
 	}
 
 	private void handleResultMessage(ResultMessage m) {
 		logger.info("Receiving ResultMessage from " + this.getHostName());
 		Job job = this.runningComputations.get(m.getTqbfId());
 		Qbf formula = job.getFormula();
 		boolean solved = formula.mergeQbf(m.getTqbfId(), m.getResult());
 		this.runningComputations.remove(m.getTqbfId());
 		logger.info("Result of tqbf(" + m.getTqbfId() + ") merged into Qbf of Job " + job.getId());
 		if(solved) {
 			job.setStatus("Result: " + formula.getResult());
 			job.setStoppedAt(new Date());
 			Job.getTableModel().fireTableDataChanged();
 		}
 	}
 
 	private void handleShutdownMessage(ShutdownMessage m) {
 		logger.info("ShutdownMessage received. Removing slave " + this.getHostName());
 		stop();
 		// TODO notify someone about unfinished computation
 		Slave.removeSlave(this);
 		logger.info("Slave " + this.getHostName() + " removed");
 	}
 
 	public void kill(String reason) {
 		logger.info("Killing Slave " + this.getHostName() + " ...");
 		this.sendKillMessage(reason);
 		logger.info("Kill-Message sent to " + this.getHostName());
 	}
 
 	public void onMessage(Message m) {
 		if (!(m instanceof ObjectMessage)) {
 			logger.error("Received unknown Non-Object-Message. Ignoring");
 			return;
 		}
 		Object t = null;
 		try {
 			t = ((ObjectMessage) m).getObject();
 		} catch (JMSException e) {
 			logger.error("Error while retrieving Object from Message... \n" + e.getCause());
 		}
 		if (t instanceof FormulaAbortedMessage) {
			handleFormulaAbortedMessage((FormulaAbortedMessage) t);
 		} else if (t instanceof InformationMessage) {
 			handleInformationMessage((InformationMessage) t);
 		} else if (t instanceof ResultMessage) {
 			handleResultMessage((ResultMessage) t);
 		} else if (t instanceof ShutdownMessage) {
 			handleShutdownMessage((ShutdownMessage) t);
 		} else {
 			logger.error("Received message object of unknown type.");
 		}
 	}
 
 	public void sendAbortMessage(String tqbfId) {
 		logger.info("Sending AbortMessage to Slave " + this.getHostName());
 		AbortMessage msg = new AbortMessage();
 		msg.setQbfId(tqbfId);
 		sendObject(msg);
 		logger.info("AbortMessage sent");
 	}
 
 	public void sendFormulaMessage(TransmissionQbf tqbf) {
 		logger.info("Sending FormulaMessage to Slave " + this.getHostName());
 		FormulaMessage msg = new FormulaMessage();
 		msg.setFormula(tqbf);
 		sendObject(msg);
 		logger.info("FormulaMessage sent");
 	}
 
 	public void sendInformationRequestMessage() {
 		logger.info("Sending InformationRequestMessage to Slave " + this.getHostName());
 		InformationRequestMessage msg = new InformationRequestMessage();
 		sendObject(msg);
 	}
 
 	public void sendKillMessage(String reason) {
 		logger.info("Sending KillMessage to Slave " + this.getHostName());
 		KillMessage msg = new KillMessage();
 		msg.setReason(reason);
 		sendObject(msg);
 	}
 
 	private void sendObject(Serializable o) {
 		try {
 			logger.debug("Sending Object of Class : " + o.getClass() + " to " + producer_snd.getDestination());
 			producer_snd.send(session.createObjectMessage(o));
 		} catch (JMSException e) {
 			logger.error("Error while sending Objectmessage...\n" + e.getCause());
 		}
 	}
 
 	public void setCores(int cores) {
 		this.cores = cores;
 	}
 
 	public void setHostName(String hostName) {
 		this.hostName = hostName;
 	}
 
 	public void setToolIds(Vector<String> toolIds) {
 		this.toolIds = toolIds;
 	}
 
 	public void stop() {
 		logger.info("Stopping Slavethread...");
 		//Stopping the loop
 		this.running = false;
 		try {
 			Thread.sleep(1000);
 			this.session.close();
 		} catch (Exception e) {
 			logger.error("Error while stopping Slavehandler...\n" + e.getCause());
 		}
 	}
 
 	@Override
 	public void run() {
 		logger.info("Starting Slavehandlerthread...");
 		try {
 			session = MasterDaemon.createSession();
 			destination_snd = session.createQueue("TO." + hostName);
 			destination_rcv = session.createQueue("FROM." + hostName);
 			producer_snd = session.createProducer(destination_snd);
 			producer_snd.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
 			consumer_rcv = session.createConsumer(destination_rcv);
 		} catch (JMSException e) {
 			logger.error("Error while initializing Queues...\n" + e.getCause());
 			System.exit(-1);
 		}
 		
 		this.running = true;
 		Message msg = null;
 		while (running) {
 			try {
 				msg = consumer_rcv.receive(1000);
 			} catch (JMSException e) {
 				logger.error("Error while consuming Slavemessage...\n" + e.getCause());
 			}
 			if (msg != null) {
 				onMessage(msg);
 			}
 		}
 		logger.info("Slavehandlerthread stopped");
 		
 	}
 	
 	public String toString() {
 		return "Slave -- Hostname: " + (this.hostName != null ? this.hostName : "") +
 				", Solvers: " + (this.toolIds != null ? this.toolIds.toString() : "") +
 				", Cores: " + this.cores;
 	}
 }
