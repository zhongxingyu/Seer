 package edu.berkeley.gamesman;
 
 import java.io.*;
 import java.net.*;
 import java.util.*;
 
 import edu.berkeley.gamesman.core.*;
 import edu.berkeley.gamesman.util.DebugFacility;
 import edu.berkeley.gamesman.util.Pair;
 import edu.berkeley.gamesman.util.Util;
 
 import edu.berkeley.gamesman.thrift.*;
 import edu.berkeley.gamesman.thrift.GamestateRequestHandler.Iface;
 
 import org.apache.thrift.TException;
 import org.apache.thrift.server.TServer;
 import org.apache.thrift.server.TThreadPoolServer;
 import org.apache.thrift.transport.TServerSocket;
 import org.apache.thrift.transport.TServerTransport;
 import org.apache.thrift.transport.TTransportException;
 
 /**
  * Basic JSON interface for web app usage
  * 
  * @author Steven Schlansker
  */
 public class JSONInterface extends GamesmanApplication {
 	/**
 	 * No arg constructor
 	 */
 	public JSONInterface() {
 	}
 
 	Properties serverConf;
 
 	Map<String, Configuration> loadedConfigurations = new HashMap<String, Configuration>();
 
 	@Override
 	public int run(Properties props) {
 		this.serverConf = props;
 		/*
 		 * try { db = Util.typedInstantiate(, Database.class); } catch
 		 * (ClassNotFoundException e1) {
 		 * Util.fatalError("Failed to create database",e1); }
 		 * db.initialize(inconf.getProperty("gamesman.db.uri"), null); this.conf
 		 * = db.getConfiguration();
 		 */
 		int port = 0;
 		try {
 			port = Integer
 					.parseInt(serverConf.getProperty("json.port", "4242"));
 		} catch (NumberFormatException e) {
 			Util.fatalError("Port must be an integer", e);
 		}
 		reallyRun(port);
 		return 0;
 	}
 
 	/**
 	 * Run the server
 	 * 
 	 * @param port
 	 *            the port to listen on
 	 */
 	public void reallyRun(final int port) {
 		assert Util.debug(DebugFacility.JSON, "Loading JSON server...");
 
 		// Want to always print this out.
 		System.out.println("Server ready on port " + port + "!");
 
 		GamestateRequestServer handler = new GamestateRequestServer();
 		GamestateRequestHandler.Processor processor = new GamestateRequestHandler.Processor(
 				handler);
 
 		TServerTransport serverTransport;
 		try {
 			serverTransport = new TServerSocket(port);
 			TServer threaded = new TThreadPoolServer(processor, serverTransport);
 
 			System.out.println("Starting the server...");
 			threaded.serve();
 
 		} catch (TTransportException e) {
 			Util.fatalError("Could not start server on port " + port, e);
 			e.printStackTrace();
 		}
 	}
 
 	/*
 	 * public static void main(String[] args) { boolean verbose = false; int i =
 	 * 0; if (args.length > 0 && args[0].equals("-v")) { verbose = true; i = 1;
 	 * } if (args.length < 2+i) {Util.fatalError(
 	 * "Usage: JSONInterface [-v] file:///.../database.db  portnum  [DatabaseClass]"
 	 * ); } String dbClass = "BlockDatabase"; String dbURI = args[i]; int port =
 	 * Integer.valueOf(args[i+1]); if (args.length > i+2) { dbClass = args[i+2];
 	 * } if (verbose) { EnumSet<DebugFacility> debugOpts =
 	 * EnumSet.noneOf(DebugFacility.class); ClassLoader cl =
 	 * ClassLoader.getSystemClassLoader(); cl.setDefaultAssertionStatus(false);
 	 * debugOpts.add(DebugFacility.JSON);
 	 * DebugFacility.JSON.setupClassloader(cl);
 	 * debugOpts.add(DebugFacility.CORE);
 	 * DebugFacility.CORE.setupClassloader(cl); Util.enableDebuging(debugOpts);
 	 * } JSONInterface ji = new JSONInterface(); Database db; try { db =
 	 * Util.typedInstantiate
 	 * ("edu.berkeley.gamesman.database."+dbClass,Database.class); } catch
 	 * (ClassNotFoundException e1) {
 	 * Util.fatalError("Failed to create database",e1); return; }
 	 * db.initialize(dbURI, null); ji.conf = db.getConfiguration(); ji.conf.db =
 	 * db; ji.reallyRun(port); }
 	 */
 
 	static String sanitise(String val) {
 		val = val.replace('.', '-');
 		val = val.replace('\\', '-');
 		val = val.replace('/', '-');
 		try {
 			return URLEncoder.encode(val, "utf-8");
 		} catch (java.io.UnsupportedEncodingException e) {
 			return "";
 		}
 	}
 
 	Configuration newLoadDatabase(Map<String, String> params, String game) {
 		/*
 		 * String game = params.get("game"); if (game == null) { return null; }
 		 */
 
 		String filename = sanitise(game);
 		String[] allowedFields = serverConf.getProperty("json.fields." + game,
 				"").split(",");
 		for (String key : allowedFields) {
 			key = key.trim();
 			if (key.length() == 0) {
 				continue;
 			}
 			String val = params.get(key);
 			if (val == null) {
 				val = "";
 			} else {
 				val = sanitise(val);
 			}
 			filename += "_" + key + "_" + val;
 		}
 		Configuration conf = loadedConfigurations.get(filename);
 		if (conf != null) {
 			return conf.cloneAll();
 		} else {
 			conf = addDatabase(params, game, filename);
 			if (conf != null) {
 				loadedConfigurations.put(filename, conf);
 			}
 			return conf.cloneAll();
 		}
 	}
 
 	synchronized Configuration addDatabase(Map<String, String> params,
 			String game, String filename) {
 		String solvedJob = serverConf.getProperty("json.solved." + filename,
 				null);
 		String dbPath = serverConf.getProperty("json.databasedirectory", "");
 		if (dbPath != null && dbPath.length() > 0) {
 			if (dbPath.charAt(dbPath.length() - 1) != '/') {
 				dbPath += '/';
 			}
 			filename = dbPath + filename + ".db";
 		} else {
 			dbPath = null;
 			filename = null;
 		}
 		try {
 			File f = new File(filename);
 			if (solvedJob != null && solvedJob.length() > 0) {
 				System.out.println("Loading solved job " + solvedJob + ".");
 				Configuration config = new Configuration(solvedJob);
 				try {
 					config.openDatabase(filename, false);
 				} catch (Exception e) {
 					Util.warn(
 							"Error when loading database for special configuration "
 									+ filename, e);
 				}
 				return config;
 			} else if (filename != null && f.exists()) {
 				System.out.println("Loading solved database " + filename + ".");
 				int confLength = 0;
 				FileInputStream fis = new FileInputStream(f);
 				for (int i = 0; i < 4; i++) {
 					confLength <<= 8;
 					confLength |= fis.read();
 				}
 				byte[] confBytes = new byte[confLength];
 				fis.read(confBytes);
 				fis.close();
 				Configuration conf = Configuration.load(confBytes);
 				conf.openDatabase(filename, false);
 				return conf;
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 			Util.warn("Failed to load database " + filename, e);
 		} catch (Util.FatalError fe) {
 			// These aren't actually fatal, so don't rethrow.
 			Util.warn("FatalError(TM) when loading database " + filename + ": "
 					+ fe.toString());
 		}
 		String unsolvedJob = serverConf.getProperty("json.unsolved." + game,
 				null);
 		if (unsolvedJob != null) {
 			Properties props = Configuration.readProperties(unsolvedJob);
 			String[] allowedFields = serverConf.getProperty(
 					"json.fields." + game, "").split(",");
 			for (String key : allowedFields) {
 				key = key.trim();
 				if (key.length() == 0) {
 					continue;
 				}
 				String val = params.get(key);
 				if (val == null) {
 					val = "";
 				}
 				props.setProperty("gamesman.game." + key, val);
 			}
 			props.setProperty("gamesman.hasher", "NullHasher");
 			String gamename = props.getProperty("gamesman.game");
 			Configuration config = null;
 			try {
 				config = new Configuration(props, true);
 				config.initialize(gamename, "NullHasher");
 			} catch (ClassNotFoundException e) {
 				Util.warn("Failed to load the game class.", e);
 				throw new RuntimeException("Failed to load the game class.", e);
 			} catch (Util.FatalError fe) {
 				throw new RuntimeException(
 						"FatalError when loading configuration for " + game);
 			}
 			return config;
 		} else {
 			// throw new
 			// RuntimeException("Failed to find an appropriate job file for " +
 			// game);
 			return null;
 		}
 	}
 
 	private class GamestateRequestServer implements Iface {
 
 		public GetNextMoveResponse getNextMoveValues(String game,
 				String configuration) throws TException {
 			GetNextMoveResponse response = new GetNextMoveResponse();
 			Map<String, String> params = reconstructGameParams(configuration);
 			System.out.println("getNextMoveValue request " + params);
 			try {
 				response.setResponse(getNextMoveValues_core(game, params));
 				response.setStatus("ok");
 			} catch (RuntimeException e) {
				System.err.println(e.getMessage());
 				response.setStatus("error");
 				response.setMessage(e.getMessage());
 			}
 			return response;
 		}
 
 		public GetMoveResponse getMoveValue(String game, String configuration)
 				throws TException {
 			GetMoveResponse response = new GetMoveResponse();
 			Map<String, String> params = reconstructGameParams(configuration);
 			System.out.println("getMoveValue request for \"" + game + "\": "
 					+ params);
 			try {
 				response.setResponse(getMoveValue_core(game, params));
 				response.setStatus("ok");
 			} catch (RuntimeException e) {
				System.err.println(e.getMessage());
 				response.setStatus("error");
 				response.setMessage(e.getMessage());
 			}
 			return response;
 		}
 
 		private <T extends State> List<GamestateResponse> getNextMoveValues_core(
 				String gamename, Map<String, String> params) throws TException {
 
 			String board = params.get("board");
 			if (board == null) {
 				throw new TException("No board passed!");
 			}
 
 			final Configuration config = newLoadDatabase(params, gamename);
 			if (config == null) {
 				throw new TException("This game does not exist.");
 			}
 			// Database db = config.getDatabase();
 			Game<T> game = Util.checkedCast(config.getGame());
 
 			T state = game.stringToState(board);
 
 			// Access to this list must be synchronized!
 			final List<GamestateResponse> responseArray = Collections
 					.synchronizedList(new ArrayList<GamestateResponse>());
 
 			PrimitiveValue pv = game.primitiveValue(state);
 			Collection<Pair<String, T>> states = game.validMoves(state);
 			Iterator<Pair<String, T>> iter = states.iterator();
 			if (game.getPlayerCount() <= 1 || pv == PrimitiveValue.UNDECIDED) {
 				// Game is not over yet...
 				Thread[] recordThreads = new Thread[states.size()];
 				for (int i = 0; i < recordThreads.length; i++) {
 					final Pair<String, T> next = iter.next();
 					recordThreads[i] = new Thread() {
 						@Override
 						public void run() {
 							GamestateResponse entry = new GamestateResponse();
 							entry = fillResponseFields(config, next.cdr, true);
 							entry.setMove(next.car);
 							responseArray.add(entry);
 						}
 					};
 					recordThreads[i].start();
 				}
 
 				// Wait for the worker threads to complete.
 				for (Thread t : recordThreads) {
 					while (t.isAlive()) {
 						try {
 							t.join();
 						} catch (InterruptedException e) {
 							e.printStackTrace();
 						}
 					}
 				}
 			}
 			return responseArray;
 		}
 
 		public <T extends State> GamestateResponse getMoveValue_core(
 				String gamename, Map<String, String> params) throws TException {
 
 			GamestateResponse response;
 
 			String board = params.get("board");
 
 			if (board == null) {
 				System.out.println("No board passed!");
 				throw new TException("No board passed!");
 			}
 
 			Configuration config = newLoadDatabase(params, gamename);
 			if (config == null) {
 				System.out.println("This game does not exist.");
 				throw new TException("This game does not exist.");
 			}
 
 			// Database db = config.getDatabase();
 			Game<T> game = Util.checkedCast(config.getGame());
 
 			T state = game.stringToState(board);
 
 			response = fillResponseFields(config, state, false);
 
 			return response;
 
 		}
 
 		/**
 		 * Returns a Map containing the params and corresponding values from the
 		 * configuration given Ex: {"board": +++++++++}
 		 * 
 		 * @param configuration
 		 * @return
 		 */
 		private Map<String, String> reconstructGameParams(String configuration) {
 			Map<String, String> j = new HashMap<String, String>();
 			String line = configuration.replace(';', '&');
 			for (String param : line.split("&")) {
 				String[] key_val = param.split("=", 2);
 				if (key_val.length != 2) {
 					continue;
 				}
 				try {
 					j.put(URLDecoder.decode(key_val[0], "utf-8"), URLDecoder
 							.decode(key_val[1], "utf-8"));
 				} catch (UnsupportedEncodingException e) {
 				}
 			}
 			return j;
 		}
 
 		private <T extends State> GamestateResponse fillResponseFields(
 				Configuration conf, T state, boolean isChildState) {
 			GamestateResponse request = new GamestateResponse();
 			Database db = conf.db;
 			Record rec = null;
 			Game<T> g = Util.checkedCast(conf.getGame());
 			if (db != null) {
 				synchronized (g) { // we don't assume that games are written to
 					// be multithreaded
 					rec = db.getRecord(g.stateToHash(state));
 				}
 			}
 			if (rec != null) {
 				if (conf.valueStates > 0) {
 					PrimitiveValue pv = rec.value;
 					if (g.getPlayerCount() > 1 && isChildState) {
 						if (pv == PrimitiveValue.WIN)
 							pv = PrimitiveValue.LOSE;
 						else if (pv == PrimitiveValue.LOSE)
 							pv = PrimitiveValue.WIN;
 					}
 					request.setValue(pv.name().toLowerCase());
 				}
 				if (conf.remotenessStates > 0) {
 					request.setRemoteness(rec.remoteness);
 				}
 				if (conf.scoreStates > 0) {
 					request.setScore(rec.score);
 				}
 			} else {
 				PrimitiveValue pv = g.primitiveValue(state);
 				if (pv != PrimitiveValue.UNDECIDED) {
 					if (g.getPlayerCount() > 1 && isChildState) {
 						if (pv == PrimitiveValue.WIN)
 							pv = PrimitiveValue.LOSE;
 						else if (pv == PrimitiveValue.LOSE)
 							pv = PrimitiveValue.WIN;
 					}
 					request.setValue(pv.name().toLowerCase());
 
 				}
 				int score = g.primitiveScore(state);
 				if (score > 0) {
 					request.setScore(score);
 				}
 			}
 			request.setBoard(g.stateToString(state));
 
 			return request;
 		}
 	}
 }
