 package escada.tpc.common.clients.jmx;
 
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.lang.management.ManagementFactory;
 import java.lang.reflect.Constructor;
 import java.util.HashMap;
 import java.util.Properties;
 import java.util.Vector;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 
 import javax.management.ObjectName;
 
 import org.apache.log4j.Logger;
 import org.apache.log4j.xml.DOMConfigurator;
 
 import escada.tpc.common.PerformanceCounters;
 import escada.tpc.common.TPCConst;
 import escada.tpc.common.args.Arg;
 import escada.tpc.common.args.ArgDB;
 import escada.tpc.common.args.BooleanArg;
 import escada.tpc.common.args.DateArg;
 import escada.tpc.common.args.DoubleArg;
 import escada.tpc.common.args.IntArg;
 import escada.tpc.common.args.StringArg;
 import escada.tpc.common.clients.ClientEmulation;
 import escada.tpc.common.clients.ClientEmulationMaster;
 import escada.tpc.common.database.DatabaseManager;
 import escada.tpc.common.util.Pad;
 import escada.tpc.tpcc.TPCCConst;
 
 public class ClientEmulationStartup implements ClientEmulationStartupMBean,
 		ClientEmulationMaster {
	private final ExecutorService executor = Executors.newCachedThreadPool();
 
 	private HashMap<String, Stage> stagem = new HashMap<String, Stage>();
 
 	private final static Logger logger = Logger
 			.getLogger(ClientEmulationStartup.class);
 
 	private final HashMap<String, Vector<ClientEmulation>> server = new HashMap<String, Vector<ClientEmulation>>();
 
 	private synchronized void init(String file) {
 		try {
 			Properties props = new Properties();
 			try {
 				props.load(new FileInputStream(file));
 
 				TPCConst.setNumMinClients(Integer.parseInt(props
 						.getProperty("tpcc.numclients")));
 				TPCCConst.setNumCustomer(Integer.parseInt(props
 						.getProperty("tpcc.numcustomers")));
 				TPCCConst.setNumDistrict(Integer.parseInt(props
 						.getProperty("tpcc.numdistricts")));
 				TPCCConst.setNumItem(Integer.parseInt(props
 						.getProperty("tpcc.numitems")));
 				TPCCConst.setNumLastName(Integer.parseInt(props
 						.getProperty("tpcc.numnames")));
 
 			} catch (FileNotFoundException e) {
 				System.out.println("Running with tpcc default parameters.");
 			} catch (IOException e) {
 				System.out.println("Running with tpcc default parameters.");
 			} catch (Exception ex) {
 				System.out.println("Running with tpcc default parameters.");
 			}
 			ObjectName name = new ObjectName(
 					"escada.tpc.common.clients.jmx:type=ClientControl");
 			ManagementFactory.getPlatformMBeanServer()
 					.registerMBean(this, name);
 
 			name = new ObjectName(
 					"escada.tpc.common.clients.jmx:type=ClientPerformance");
 
 			ManagementFactory.getPlatformMBeanServer().registerMBean(
 					PerformanceCounters.getReference(), name);
 
 			System.out.println("Started jmx server.");
 
 			while (true) {
 				wait();
 			}
 		} catch (Exception ex) {
 			ex.printStackTrace();
 			System.exit(1);
 		}
 	}
 
 	public synchronized void start(String key, String arg)
 			throws InvalidTransactionException {
 
 		if (this.stagem.get(key) == null) {
 
 			this.stagem.put(key, Stage.RUNNING);
 
 			String[] args = arg.split("[ ]+");
 
			this.executor.execute(new Start(key, args));
 		} else {
 			throw new InvalidTransactionException(key + " start on "
 					+ this.stagem.get(key));
 		}
 	}
 
 	class Start implements Runnable {
 		private String[] args;
 
 		private String key;
 
 		public Start(String key, String[] args) {
 			this.key = key;
 			this.args = args;
 		}
 
 		public void run() {
 			startClientEmulation(this.key, this.args);
 		}
 	}
 
 	public synchronized void pause(String key)
 			throws InvalidTransactionException {
 		if (this.stagem.get(key) != null
 				&& this.stagem.get(key).equals(Stage.RUNNING)) {
 
 			this.stagem.put(key, Stage.PAUSED);
 
			if (server.get(key) != null) {
				for (ClientEmulation e : server.get(key)) {
					e.pause();
				}

 			}
 		} else {
 			throw new InvalidTransactionException(key + " pause on "
 					+ this.stagem.get(key));
 		}
 	}
 
 	public synchronized void unpause(String key)
 			throws InvalidTransactionException {
 		if (this.stagem.get(key) != null
 				&& this.stagem.get(key).equals(Stage.PAUSED)) {
 
 			this.stagem.put(key, Stage.RUNNING);
 
			if (server.get(key) != null) {
				for (ClientEmulation e : server.get(key)) {
					e.unpause();
				}
 			}
 		} else {
 			throw new InvalidTransactionException(key + " unpause on "
 					+ this.stagem.get(key));
 		}
 	}
 
 	public synchronized void stop(String key)
 			throws InvalidTransactionException {
 		if (this.stagem.get(key) != null
 				&& (this.stagem.get(key).equals(Stage.RUNNING) || this.stagem
 						.get(key).equals(Stage.PAUSED))) {
 
			if (server.get(key) != null) {
				for (ClientEmulation e : server.get(key)) {
					e.stopit();
				}
 			}
 
 			this.server.remove(key);
 			this.stagem.remove(key);
 		} else {
 			throw new InvalidTransactionException(key + " stop on "
 					+ this.stagem.get(key));
 		}
 	}
 
 	public void kill() {
 		this.executor.submit(new Kill());
 	}
 
 	class Kill implements Runnable {
 		public void run() {
 			System.exit(0);
 		}
 	}
 
 	public static void main(String[] args) {
 		ClientEmulationStartup ces = new ClientEmulationStartup();
 		ces.init((args.length == 1 ? args[0] : null));
 	}
 
 	private void startClientEmulation(String keyArgs, String[] args) {
 		ClientEmulation e = null;
 		ArgDB db = new ArgDB();
 
 		try {
 
 			StringArg log4jArg = new StringArg("-LOGconfig",
 					"Configuration file for Log4J.",
 					"% Defines the logging output.", db);
 
 			StringArg ebArg = new StringArg("-EBclass", "EB Factory",
 					"% Factory <class> used to create EBs.", db);
 			StringArg stArg = new StringArg(
 					"-STclass",
 					"State Machine for Emulation",
 					"% Defines the class used as a state machine for emulation.",
 					db);
 
 			StringArg dbArg = new StringArg("-DBclass", "DB Database Class",
 					"% String <class> used to instantiate the database.", db);
 
 			StringArg driverArg = new StringArg(
 					"-DBdriver",
 					"DBDriver Database Class",
 					"% String <class> which specifies the driver used to contact the database.",
 					db);
 
 			StringArg pathArg = new StringArg(
 					"-DBpath",
 					"DBpath Database Connection Information",
 					"% String which specifies information used to connect to the database.",
 					db);
 			StringArg usrArg = new StringArg(
 					"-DBusr",
 					"DBusr Database User",
 					"% String <usr> which specifies the user connecting to the database",
 					db);
 
 			StringArg passwdArg = new StringArg(
 					"-DBpasswd",
 					"DBpasswd User password",
 					"% String <passwd> which specifies the password correpondent to user connecting to the database.",
 					db);
 
 			IntArg poolArg = new IntArg("-POOL", "Connection Pool",
 					"% The number of entries available for connection pool...",
 					db);
 
 			DateArg st = new DateArg("-ST", "Starting time for ramp-up",
 					"% Time (such as Nov 2, 1999 11:30:00 AM CST) "
 							+ "at which to start ramp-up."
 							+ "  Useful for synchronizing multiple RBEs.",
 					System.currentTimeMillis() + 2000L, db);
 
 			IntArg ru = new IntArg("-RU", "Ramp-up time",
 					"% Seconds used to warm-up the simulator.", 10 * 60, db);
 
 			IntArg mi = new IntArg("-MI", "Measurement interval",
 					"% Minutes used for measuring SUT performance.", 30 * 60,
 					db);
 
 			IntArg rd = new IntArg("-RD", "Ramp-down time",
 					"% Seconds of steady-state operation following "
 							+ "measurment interval.", 5 * 60, db);
 
 			DoubleArg slow = new DoubleArg("-SLOW", "Slow-down factor",
 					"% 1000 means one thousand real seconds equals one "
 							+ "simulated second.  "
 							+ "Accepts factional values and E notation.", 1.0,
 					db);
 
 			BooleanArg key = new BooleanArg("-KEY", "Enable thinktime.",
 					"% It enables or disables the think time.", true, db);
 
 			IntArg cli = new IntArg("-CLI", "Number of clients",
 					"% Number of clients concurrently accessing the database.",
 					db);
 
 			StringArg prefix = new StringArg(
 					"-PREFIX",
 					"Emulation identification and also used as part of the emulation id",
 					"% It defines the compositon of the trace file identification and is also used as a component of the "
 							+ "emulator id.", db);
 
 			StringArg traceFlag = new StringArg(
 					"-TRACEflag",
 					"trace files",
 					"% It defines the usage of trace file or not (NOTRACE,TRACE,TRACESTRING,TRACETIME)",
 					db);
 
 			IntArg fragArg = new IntArg(
 					"-FRAG",
 					"Shift the clients...",
 					"% It shifts the clients in order to access different warehouses...",
 					1, db);
 
 			BooleanArg resArg = new BooleanArg(
 					"-RESUBMIT",
 					"Resubmit Transaction.",
 					"% It enables the transaction resubmition when an error occurs.",
 					true, db);
 
 			IntArg hostArg = new IntArg("-HOST", "Connection Pool",
 					"% The number of entries available for connection pool...",
 					0, db);
 
 			if (args.length == 0) {
 				Usage(args, db);
 				return;
 			}
 
 			db.parse(args);
 
 			DOMConfigurator.configure(log4jArg.s);
 
 			logger.info("Starting up the client application.");
 			logger.info("Remote Emulator for Database Benchmark ...");
 			logger
 					.info("Universidade do Minho (Grupo de Sistemas Distribuidos)");
 			logger.info("Version 0.1");
 
 			Usage(args, db);
 
 			DatabaseManager dbManager = null;
 
 			Class cl = null;
 			Constructor co = null;
 			cl = Class.forName(dbArg.s);
 			try {
 				co = cl.getConstructor(new Class[] { Integer.TYPE });
 			} catch (Exception ex) {
 			}
 			if (co == null) {
 				dbManager = (DatabaseManager) cl.newInstance();
 			} else {
 				dbManager = (DatabaseManager) co
 						.newInstance(new Object[] { new Integer(cli.num) });
 			}
 
 			dbManager.setConnectionPool(true);
 			dbManager.setMaxConnection(poolArg.num);
 			dbManager.setDriverName(driverArg.s);
 			dbManager.setjdbcPath(pathArg.s);
 			dbManager.setUserInfo(usrArg.s, passwdArg.s);
 
 			Vector<ClientEmulation> ebs = new Vector<ClientEmulation>();
 
 			int i = 0;
 			for (i = 0; i < cli.num; i++) {
 
 				e = new ClientEmulation();
 
 				e.setFinished(false);
 				e.setTraceInformation(prefix.s);
 				e.setNumberConcurrentEmulators(cli.num);
 				e.setStatusThinkTime(key.flag);
 				e.setStatusReSubmit(resArg.flag);
 				e.setDatabase(dbManager);
 				e.setEmulationName(prefix.s);
 				e.setHostId(Integer.toString(hostArg.num));
 
 				e.create(ebArg.s, stArg.s, i, fragArg.num, this, keyArgs);
 
 				Thread t = new Thread(e);
 				t.setName(prefix.s + "-" + i);
 				e.setThread(t);
 				t.start();
 
 				ebs.add(e);
 			}
 
 			server.put(keyArgs, ebs);
 
 			logger.info("Running simulation for " + mi.num + " minute(s).");
 
 			waitForRampDown(keyArgs, 0, mi.num);
 
 			for (i = 0; i < cli.num; i++) {
 				e = (ClientEmulation) ebs.elementAt(i);
 				logger.info("Waiting for the eb " + i + " to finish its job..");
 				try {
 					e.setCompletion(true);
 					e.getThread().join();
 				} catch (InterruptedException inte) {
 					inte.printStackTrace();
 					continue;
 				}
 			}

 			logger.info("EBs finished.");

 		} catch (Arg.Exception ae) {
 			logger.info("Error:");
 			logger.info(ae);
 			Usage(args, db);
 			return;
 		} catch (Exception ex) {
 			logger.info("Error while creating clients: ", ex);
 		} finally {
 			this.server.remove(keyArgs);
 			this.stagem.remove(keyArgs);
 			logger.info("Ebs finished their jobs..");
 		}
 	}
 
 	private void Arguments(String args[]) {
 		int a;
 
 		for (a = 0; a < args.length; a++) {
 			System.out.println("#" + Pad.l(3, "" + (a + 1)) + "  " + args[a]);
 		}
 	}
 
 	private void Usage(String args[], ArgDB db) {
 		logger.info("Input command-line arguments");
 		Arguments(args);
 		logger.info("Options");
 		db.print();
 	}
 
 	public synchronized void notifyThreadsCompletion(String key) {
 		this.stagem.put(key, Stage.STOPPED);
 		notifyAll();
 	}
 
 	private synchronized void waitForRampDown(String key, int start, int term) {
 		try {
 			if (logger.isInfoEnabled()) {
 				logger.info("Start time " + start + " completion time " + term);
 			}
 
 			waitForStart(start);
 
 			if (term < 0) {
 				return;
 			}
 
 			long ini = System.currentTimeMillis();
 			long end = 0;
 			long remaining = term * 60 * 1000; // TODO: It must be changed to a
 			// constant
 
 			while (remaining > 0 && this.stagem.get(key) != null
 					&& !this.stagem.get(key).equals(Stage.STOPPED)) {
 				wait(remaining);
 
 				end = System.currentTimeMillis();
 
 				if (logger.isInfoEnabled()) {
 					logger.info("Start remain " + remaining + " ini " + ini
 							+ " end " + end);
 				}
 
 				remaining = remaining - (end - ini);
 				ini = end;
 
 				if (logger.isInfoEnabled()) {
 					logger.info("Start remain " + remaining + " ini " + ini
 							+ " end " + end);
 				}
 			}
 		} catch (InterruptedException ie) {
 			logger.error("In waitforrampdown, caught interrupted exception");
 		}
 	}
 
 	private void waitForStart(int start) throws InterruptedException {
 		if (start < 0) {
 			return;
 		}
 
 		Thread.sleep(start * 60 * 1000); // TODO - It must be changed to a
 		// constant.
 	}
 
 	public void configureCluster(String replicas)
 			throws InvalidTransactionException {
 	}
 }
