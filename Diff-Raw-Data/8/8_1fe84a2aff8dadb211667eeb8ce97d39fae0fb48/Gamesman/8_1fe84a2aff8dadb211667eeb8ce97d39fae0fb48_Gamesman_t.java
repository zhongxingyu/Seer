 package edu.berkeley.gamesman;
 
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.LineNumberReader;
 import java.lang.reflect.InvocationTargetException;
 import java.math.BigInteger;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.util.EnumSet;
 
 import edu.berkeley.gamesman.core.Configuration;
 import edu.berkeley.gamesman.core.Database;
 import edu.berkeley.gamesman.core.Game;
 import edu.berkeley.gamesman.core.Hasher;
 import edu.berkeley.gamesman.core.Master;
 import edu.berkeley.gamesman.core.RecordFields;
 import edu.berkeley.gamesman.core.Solver;
 import edu.berkeley.gamesman.database.filer.DirectoryFilerClient;
 import edu.berkeley.gamesman.database.filer.DirectoryFilerServer;
 import edu.berkeley.gamesman.util.OptionProcessor;
 import edu.berkeley.gamesman.util.Util;
 
 /**
  * @author Steven Schlansker
  */
 public final class Gamesman {
 
 	private Game<Object> gm;
 	private Hasher<Object> ha;
 	private Solver so;
 	private Database db;
 	private boolean testrun;
 	private Configuration conf;
 
 	private Gamesman(Game<Object> g, Solver s, Hasher<Object> h,
 			Database d, boolean er) {
 		gm = g;
 		ha = h;
 		so = s;
 		db = d;
 		
 		conf = new Configuration(g,h,EnumSet.of(RecordFields.Value));
 		
 		so.setDatabase(db);
 		gm.initialize(conf);
 		
 		testrun = er;
 	}
 
 	/**
 	 * The main entry point for any Java program
 	 * @param args Command line arguments
 	 */
 	@SuppressWarnings("unchecked")
 	public static void main(String[] args) {
 
 		Thread.currentThread().setName("Gamesman");
 
 		OptionProcessor.initializeOptions(args);
 		OptionProcessor.acceptOption("h", "help", false,
 				"Display this help string and exit");
 		OptionProcessor.acceptOption("d", "debug", false,
 				"Turn on debugging output");
 		OptionProcessor.nextGroup();
 		OptionProcessor.acceptOption("x", "with-graphics", false,
 				"Enables use of graphical displays");
 		OptionProcessor.nextGroup();
 		OptionProcessor.acceptOption("G", "game", true,
 				"Specifies which game to play", "NullGame");
 		OptionProcessor.acceptOption("S", "solver", true,
 				"Specifies which solver to use", "TierSolver");
 		OptionProcessor.acceptOption("H", "hasher", true,
 				"Specifies which hasher to use", "NullHasher");
 		OptionProcessor.acceptOption("D", "database", true,
 				"Specifies which database backend to use", "FileDatabase");
 		OptionProcessor.acceptOption("M", "master", true,
 				"Specifies which master controller to use", "LocalMaster");
 		OptionProcessor.nextGroup();
 		OptionProcessor
 				.acceptOption("C", "command", true, "Command to execute");
 		OptionProcessor.nextGroup();
 		OptionProcessor.acceptOption("u", "uri", true, "The URI or relative path of the databse", "file:///tmp/out.db");
 		OptionProcessor.nextGroup();
 
 		String masterName = OptionProcessor.checkOption("master");
 
 		Object omaster = null;
 		try {
 			omaster = Class.forName(
 					"edu.berkeley.gamesman.master." + masterName).newInstance();
 		} catch (ClassNotFoundException cnfe) {
 			System.err.println("Could not load master controller '"
 					+ masterName + "': " + cnfe);
 			System.exit(1);
 		} catch (IllegalAccessException iae) {
 			System.err.println("Not allowed to access requested master '"
 					+ masterName + "': " + iae);
 			System.exit(1);
 		} catch (InstantiationException ie) {
 			System.err.println("Master failed to instantiate: " + ie);
 			System.exit(1);
 		}
 
 		if (!(omaster instanceof Master)) {
 			System.err
 					.println("Master does not implement master.Master interface");
 			System.exit(1);
 		}
 
 		Master m = (Master) omaster;
 
 		Util.debug("Preloading classes...");
 
 		String gameName, solverName, hasherName, databaseName;
 
 		gameName = OptionProcessor.checkOption("game");
 		solverName = OptionProcessor.checkOption("solver");
 		hasherName = OptionProcessor.checkOption("hasher");
 		databaseName = OptionProcessor.checkOption("database");
 
 		Class<? extends Game<?>> g;
 		Class<? extends Solver> s;
 		Class<? extends Hasher<?>> h;
 		Class<? extends Database> d;
 
 		try {
 			g = (Class<? extends Game<?>>) Class
 					.forName("edu.berkeley.gamesman.game." + gameName);
 			s = (Class<? extends Solver>) Class
 					.forName("edu.berkeley.gamesman.solver." + solverName);
 			h = (Class<? extends Hasher<?>>) Class
 					.forName("edu.berkeley.gamesman.hasher." + hasherName);
 			d = (Class<? extends Database>) Class
 					.forName("edu.berkeley.gamesman.database." + databaseName);
 		} catch (Exception e) {
 			System.err.println("Fatal error in preloading: " + e);
 			return;
 		}
 
 		boolean dohelp = (OptionProcessor.checkOption("h") != null);
 
 		String cmd = OptionProcessor.checkOption("command");
 		if (cmd != null) {
 			try {
 				boolean tr = (OptionProcessor.checkOption("help") != null);
 				Gamesman executor = new Gamesman(
 						(Game<Object>) g.newInstance(), s
 								.newInstance(), (Hasher<Object>)h.newInstance(), (Database) d
 								.newInstance(), tr);
 				executor.getClass().getMethod("execute" + cmd,
 						(Class<?>[]) null).invoke(executor);
 			} catch (NoSuchMethodException nsme) {
 				System.out.println("Don't know how to execute command " + nsme);
 			} catch (IllegalAccessException iae) {
 				System.out.println("Permission denied while executing command "
 						+ iae);
 			} catch (InstantiationException ie) {
 				System.out.println("Could not instantiate: " + ie);
 			} catch (InvocationTargetException ite) {
 				System.out.println("Exception while executing command: " + ite);
 				ite.getTargetException().printStackTrace();
 			}
 		} else if (!dohelp) {
 			Util.debug("Defaulting to solve...");
 			m.initialize(g, s, h, d);
 			m.run();
 		}
 
 		if (dohelp) {
 			System.out.println("Gamesman help stub, please fill this out!"); // TODO: help text
 			OptionProcessor.help();
 			return;
 		}
 
 		Util.debug("Finished run, tearing down...");
 
 	}
 
 	/**
 	 * Diagnostic call to unhash an arbitrary value to a game board
 	 */
 	public void executeunhash() {
 		OptionProcessor.acceptOption("v", "hash", true,
 				"The hash value to be manipulated");
 		if (testrun)
 			return;
 		Object state = gm.hashToState(new BigInteger(OptionProcessor
 				.checkOption("hash")));
 		System.out.println(gm.stateToString(state));
 	}
 
 	/**
 	 * Diagnostic call to view all child moves of a given hashed game state
 	 */
 	public void executegenmoves() {
 		OptionProcessor.acceptOption("v", "hash", true,
 				"The hash value to be manipulated");
 		if (testrun)
 			return;
 		Object state = gm.hashToState(new BigInteger(OptionProcessor
 				.checkOption("hash")));
 		for (Object nextstate : gm.validMoves(state)) {
 			System.out.println(gm.stateToHash(nextstate));
 			System.out.println(gm.stateToString(nextstate));
 		}
 	}
 
 	/**
 	 * Hash a single board with the given hasher and print it.
 	 */
 	public void executehash() {
 		OptionProcessor.acceptOption("v", "board", true,
 				"The board to be hashed");
 		if (testrun)
 			return;
 		String str = OptionProcessor.checkOption("board");
 		if (str == null)
 			Util.fatalError("Please specify a board to hash");
 		System.out.println(gm.stateToHash(gm.stringToState(str.toUpperCase())));
 	}
 
 	/**
 	 * Evaluate a single board and return its primitive value.
 	 */
 	public void executeevaluate() {
 		OptionProcessor.acceptOption("v", "board", true,
 				"The board to be evaluated");
 		if (testrun)
 			return;
 		BigInteger val = new BigInteger(OptionProcessor.checkOption("board"));
 		if (val == null)
 			Util.fatalError("Please specify a hash to evaluate");
 		System.out.println(gm.primitiveValue(gm.hashToState(val)));
 	}
 
 	/**
 	 * Launch a directory filer server
 	 */
 	public void executelaunchDirectoryFiler() {
 		OptionProcessor.acceptOption("r", "rootDirectory", true,
 				"The root directory for the directory filer");
 		OptionProcessor.acceptOption("p", "port", true,
 				"The port to listen on", "4263");
 		OptionProcessor.acceptOption("s", "secret", true,
 				"The shared secret the server should require");
 		if (testrun)
 			return;
 		if (OptionProcessor.checkOption("rootDirectory") == null)
 			Util
 					.fatalError("You must provide a root directory for the filer with -r or --rootDirectory");
 		if (OptionProcessor.checkOption("secret") == null)
 			Util
 					.fatalError("You must provide a shared secret to protect the server with -s or --secret");
 		
 		final DirectoryFilerServer serv = new DirectoryFilerServer(OptionProcessor.checkOption("rootDirectory"),
 				Integer.parseInt(OptionProcessor.checkOption("port")),
 				OptionProcessor.checkOption("secret"));
 		
 		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable(){
 			public void run() {
 				serv.close();
 			}
 		}));
 		
 		serv.launchServer();
 	}
 
 	private enum directoryConnectCommands {
 		quit, halt, ls, open, close, read, write
 	}
 
 	/**
 	 * Give a simple shell interface to a remote directory filer server
 	 * @throws URISyntaxException The given URI is malformed
 	 */
 	public void executedirectoryConnect() throws URISyntaxException {
 		OptionProcessor.acceptOption("u", "uri", true, "The URI to connect to",
 				"gdfp://game@localhost:4263/");
 		if (testrun)
 			return;
 		DirectoryFilerClient dfc = new DirectoryFilerClient(new URI(OptionProcessor
 				.checkOption("uri")));
 
 		LineNumberReader input = new LineNumberReader(new InputStreamReader(
 				System.in));
 
 		String dbname = "";
 		Database cdb = null;
 		
 		try {
 			while (true) {
 				String line = "quit";
 				System.out.print(dbname+"> ");
 				line = input.readLine();
 
 				switch (directoryConnectCommands.valueOf(line)) {
 				case quit:
 					dfc.close();
 					return;
 				case halt:
 					dfc.halt();
 					dfc.close();
 					return;
 				case ls:
 					dfc.ls();
 					break;
 				case open:
 					System.out.print("open> ");
 					dbname = input.readLine();
 					cdb = dfc.openDatabase(dbname, new Configuration(""));
 					break;
 				case close:
 					if(cdb == null) break;
 					cdb.close();
 					cdb = null;
 					dbname = "";
 					break;
 				case read:
 					System.out.print(dbname+" read> ");
 					System.out.println("Result: "+cdb.getValue(new BigInteger(input.readLine())));
 					break;
 				case write:
 					System.out.print(dbname+" write> ");
 					String loc = input.readLine();
 					System.out.print(dbname+" write "+loc+"> ");
 					line = input.readLine();
 		
 					//cdb.setValue(new BigInteger(loc), Record.parseRecord(conf,line)); TODO: fixme
 				}
 			}
 		} catch (IOException e) {
 			Util.fatalError("IO Error: " + e);
 		}
 	}
 	
	//public void executetestRPC(){
	//	new RPCTest();
	//}
 
 }
