 package edu.berkeley.gamesman;
 
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.LineNumberReader;
 import java.lang.reflect.InvocationTargetException;
 import java.math.BigInteger;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.util.EnumSet;
 import java.util.Properties;
 
 import edu.berkeley.gamesman.core.Configuration;
 import edu.berkeley.gamesman.core.Database;
 import edu.berkeley.gamesman.core.Game;
 import edu.berkeley.gamesman.core.Master;
 import edu.berkeley.gamesman.core.RecordFields;
 import edu.berkeley.gamesman.core.Solver;
 import edu.berkeley.gamesman.database.filer.DirectoryFilerClient;
 import edu.berkeley.gamesman.database.filer.DirectoryFilerServer;
 import edu.berkeley.gamesman.util.DebugFacility;
 import edu.berkeley.gamesman.util.Util;
 
 /**
  * @author Steven Schlansker
  */
 public final class GamesmanMain extends GamesmanApplication {
 	private Configuration conf;
 	private Game<Object> gm;
 
 	/**
 	 * No arg constructor
 	 */
 	public GamesmanMain() {}
 
 	@Override
 	public int run(Properties props) {
 		try {
 			this.conf = new Configuration(props);
 		} catch (ClassNotFoundException e) {
 			Util.fatalError("Configuration contains unknown game or hasher ", e);
 		}
 		gm = Util.checkedCast(conf.getGame());
 		Thread.currentThread().setName("Gamesman");
 		String masterName = conf.getProperty("gamesman.master","LocalMaster");
 
 		Object omaster = null;
 		try {
 			omaster = Class.forName(
 					"edu.berkeley.gamesman.master." + masterName).newInstance();
 		} catch (ClassNotFoundException cnfe) {
 			Util.fatalError("Could not load master controller '"
 					+ masterName + "': " + cnfe);
 		} catch (IllegalAccessException iae) {
 			Util.fatalError("Not allowed to access requested master '"
 					+ masterName + "': " + iae);
 		} catch (InstantiationException ie) {
 			Util.fatalError("Master failed to instantiate: " + ie);
 		}
 
 		if (!(omaster instanceof Master)) {
 			Util.fatalError("Master does not implement master.Master interface");
 		}
 
 		Master m = (Master) omaster;
 
 		assert Util.debug(DebugFacility.CORE, "Preloading classes...");
 
 		String gameName, solverName, hasherName, databaseName;
 
 		gameName = conf.getProperty("gamesman.game");
 		if(gameName == null)
 			Util.fatalError("You must specify a game with the property gamesman.game");
 		solverName = conf.getProperty("gamesman.solver");
 		if(solverName == null)
 			Util.fatalError("You must specify a solver with the property gamesman.solver");
 		hasherName = conf.getProperty("gamesman.hasher");
 		if(hasherName == null)
 			Util.fatalError("You must specify a hasher with the property gamesman.hasher");
 		databaseName = conf.getProperty("gamesman.database","FileDatabase");
 
 		Class<? extends Solver> s;
 		Class<? extends Database> d;
 //		Class<? extends Game<Object>> g;
 //		Class<? extends Hasher<Object>> h;
 
 		try {
 //			g = Util.typedForName("edu.berkeley.gamesman.game." + gameName);
 			s = Util.typedForName("edu.berkeley.gamesman.solver." + solverName, Solver.class);
 //			h = Util.typedForName("edu.berkeley.gamesman.hasher." + hasherName);
 			d = Util.typedForName("edu.berkeley.gamesman.database." + databaseName, Database.class);
 		} catch (Exception e) {
 			Util.fatalError("Fatal error in preloading", e);
 			return 1;
 		}
 		
 		String cmd = conf.getProperty("gamesman.command",null);
 		if (cmd != null) {
 			try {
 				this.getClass().getMethod("execute" + cmd, (Class<?>[]) null).invoke(this);
 			} catch (NoSuchMethodException nsme) {
 				System.out.println("Don't know how to execute command " + nsme);
 			} catch (IllegalAccessException iae) {
 				System.out.println("Permission denied while executing command "
 						+ iae);
 			} catch (InvocationTargetException ite) {
 				System.out.println("Exception while executing command: " + ite);
 				ite.getTargetException().printStackTrace();
 			}
 		} else {
 			assert Util.debug(DebugFacility.CORE, "Defaulting to solve...");
 			try {
				//Game<?> gm = Util.checkedCast(g.getConstructors()[0].newInstance(conf));
				//conf.setGame(gm);
				//Hasher<?> ha = Util.checkedCast(h.getConstructors()[0].newInstance(conf));
				//conf.setHasher(ha);
				conf.setStoredFields(EnumSet.of(RecordFields.VALUE,RecordFields.REMOTENESS));
				//gm.prepare();
 				m.initialize(conf, s, d);
 			} catch (Exception e){
 				Util.fatalError("Exception while instantiating and initializing",e);
 			}
 			m.run();
 		}
 
 		//if (dohelp) {
 		//	System.out.println("Gamesman help stub, please fill this out!"); // TODO: help text
 		//	//OptionProcessor.help();
 		//	return;
 		//}
 
 		assert Util.debug(DebugFacility.CORE,"Finished run, tearing down...");
 		return 0;
 	}
 
 	/**
 	 * Diagnostic call to unhash an arbitrary value to a game board
 	 */
 	public void executeunhash() {
 		Object state = gm.hashToState(new BigInteger(conf.getProperty("gamesman.hash")));
 		System.out.println(gm.displayState(state));
 	}
 
 	/**
 	 * Diagnostic call to view all child moves of a given hashed game state
 	 */
 	public void executegenmoves() {
 		Object state = gm.hashToState(new BigInteger(conf.getProperty("gamesman.hash")));
 		for (Object nextstate : gm.validMoves(state)) {
 			System.out.println(gm.stateToHash(nextstate));
 			System.out.println(gm.displayState(nextstate));
 		}
 	}
 
 	/**
 	 * Hash a single board with the given hasher and print it.
 	 */
 	public void executehash() {
 		String str = conf.getProperty("board");
 		if (str == null)
 			Util.fatalError("Please specify a board to hash");
 		System.out.println(gm.stateToHash(gm.stringToState(str.toUpperCase())));
 	}
 
 	/**
 	 * Evaluate a single board and return its primitive value.
 	 */
 	public void executeevaluate() {
 		String board = conf.getProperty("gamesman.board");
 		if (board == null)
 			Util.fatalError("Please specify a hash to evaluate");
 		BigInteger val = new BigInteger(board);
 		System.out.println(gm.primitiveValue(gm.hashToState(val)));
 	}
 
 	/**
 	 * Launch a directory filer server
 	 */
 	public void executelaunchDirectoryFiler() {
 		if (conf.getProperty("rootDirectory") == null)
 			Util.fatalError("You must provide a root directory for the filer with -r or --rootDirectory");
 		if (conf.getProperty("secret") == null)
 			Util.fatalError("You must provide a shared secret to protect the server with -s or --secret");
 		
 		final DirectoryFilerServer serv;
 		try {
 			serv = new DirectoryFilerServer(conf.getProperty("rootDirectory"),
 				conf.getInteger("port", 4263),
 				conf.getProperty("secret"));
 		} catch (ClassNotFoundException e1) {
 			Util.fatalError("failed to load configuration", e1);
 			return;
 		}
 		
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
 		DirectoryFilerClient dfc = new DirectoryFilerClient(new URI(conf.getProperty("uri","gdfp://game@localhost:4263/")));
 
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
 					cdb = dfc.openDatabase(dbname, null); //TODO: not null here!
 					break;
 				case close:
 					if(cdb == null) break;
 					cdb.close();
 					cdb = null;
 					dbname = "";
 					break;
 				case read:
 					System.out.print(dbname+" read> ");
 					System.out.println("Result: "+cdb.getRecord(new BigInteger(input.readLine())));
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
