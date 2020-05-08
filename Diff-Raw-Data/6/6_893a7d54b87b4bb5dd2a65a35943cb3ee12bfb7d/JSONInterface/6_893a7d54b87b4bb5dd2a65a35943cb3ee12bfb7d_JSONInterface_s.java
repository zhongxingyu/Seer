 package edu.berkeley.gamesman;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.LineNumberReader;
 import java.io.OutputStreamWriter;
 import java.io.PrintWriter;
 import java.io.UnsupportedEncodingException;
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.net.URI;
 import java.net.URLDecoder;
 import java.net.URLEncoder;
 import java.util.EnumSet;
 import java.util.HashMap;
 import java.util.Properties;
 import java.util.Map;
 import java.util.Set;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import edu.berkeley.gamesman.database.BlockDatabase;
 import edu.berkeley.gamesman.core.Configuration;
 import edu.berkeley.gamesman.core.Database;
 import edu.berkeley.gamesman.core.Game;
 import edu.berkeley.gamesman.core.PrimitiveValue;
 import edu.berkeley.gamesman.core.Record;
 import edu.berkeley.gamesman.core.RecordFields;
 import edu.berkeley.gamesman.util.DebugFacility;
 import edu.berkeley.gamesman.util.Pair;
 import edu.berkeley.gamesman.util.Util;
 
 /**
  * Basic JSON interface for web app usage
  * @author Steven Schlansker
  */
 public class JSONInterface extends GamesmanApplication {
 	/**
 	 * No arg constructor
 	 */
 	public JSONInterface() {}
 	
 	Properties serverConf;
 	Map<String,Configuration> loadedConfigurations = new HashMap<String,Configuration>();
 	Class<? extends Database> dbClass;
 
 	@Override
 
 	public int run(Properties props) {
 		this.serverConf = props;
 		try {
 
 			this.dbClass = Util.typedForName("edu.berkeley.gamesman.database."+serverConf.getProperty("gamesman.database","BlockDatabase"), Database.class);
 		} catch (ClassNotFoundException e) {
 			Util.warn("Can't load default database!", e);
 			this.dbClass = BlockDatabase.class;
 		}
 		/*
 		try {
 			db = Util.typedInstantiate(,
 					Database.class);
 		} catch (ClassNotFoundException e1) {
 			Util.fatalError("Failed to create database",e1);
 		}
 		db.initialize(inconf.getProperty("gamesman.db.uri"), null);
 		this.conf = db.getConfiguration();
 		*/
 		int port = 0;
 		try {
 			port = Integer.parseInt(serverConf.getProperty("json.port", "4242"));
 		} catch (NumberFormatException e) {
 			Util.fatalError("Port must be an integer",e);
 		}
 		reallyRun(port);
 		return 0;
 	}
 	
 	/**
 	 * Run the server
 	 * @param port the port to listen on
 	 */
 	public void reallyRun(final int port) {
 		assert Util.debug(DebugFacility.JSON, "Loading JSON server...");
 		
 		ServerSocket ssock;
 		try {
 			ssock = new ServerSocket(port);
 		} catch (IOException e) {
 			Util.fatalError("Could not listen on port "+port,e);
 			return;
 		}
 		
 		// Want to always print this out.
 		System.out.println("Server ready on port "+port+"!");
 		while(true){
 			Socket s;
 			try {
 				s = ssock.accept();
 			} catch (IOException e) {
 				Util.warn("IO Exception while accepting: "+e);
 				break;
 			}
 			JSONThread t = new JSONThread(s);
 			t.setName("JSONThread "+s);
 			t.start();
 		}
 	}
 	
 	/*
 	public static void main(String[] args) {
 		boolean verbose = false;
 		int i = 0;
 		if (args.length > 0 && args[0].equals("-v")) {
 			verbose = true;
 			i = 1;
 		}
 		if (args.length < 2+i) {
 			Util.fatalError("Usage: JSONInterface [-v] file:///.../database.db  portnum  [DatabaseClass]");
 		}
 		String dbClass = "BlockDatabase";
 		String dbURI = args[i];
 		int port = Integer.valueOf(args[i+1]);
 		if (args.length > i+2) {
 			dbClass = args[i+2];
 		}
 		if (verbose) {
 			EnumSet<DebugFacility> debugOpts = EnumSet.noneOf(DebugFacility.class);
 			ClassLoader cl = ClassLoader.getSystemClassLoader();
 			cl.setDefaultAssertionStatus(false);
 			debugOpts.add(DebugFacility.JSON);
 			DebugFacility.JSON.setupClassloader(cl);
 			debugOpts.add(DebugFacility.CORE);
 			DebugFacility.CORE.setupClassloader(cl);
 			Util.enableDebuging(debugOpts);
 		}
 		JSONInterface ji = new JSONInterface();
 		Database db;
 		try {
 			db = Util.typedInstantiate("edu.berkeley.gamesman.database."+dbClass,Database.class);
 		} catch (ClassNotFoundException e1) {
 			Util.fatalError("Failed to create database",e1);
 			return;
 		}
 		db.initialize(dbURI, null);
 		ji.conf = db.getConfiguration();
 		ji.conf.db = db;
 		ji.reallyRun(port);
 	}
 	*/
 
 	static String sanitise(String val) {
 		val = val.replace('.','-');
 		val = val.replace('\\','-');
 		val = val.replace('/','-');
 		try {
 			return URLEncoder.encode(val,"utf-8");
 		} catch (java.io.UnsupportedEncodingException e) {
 			return "";
 		}
 	}
 
 	Configuration loadDatabase(Map<String,String> params) {
 		String game = params.get("game");
 		if (game == null) {
 			return null;
 		}
 		String filename = sanitise(game);
 		String[] allowedFields = serverConf.getProperty("json.fields."+game,"").split(",");
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
 			return conf;
 		}
 		System.out.println(filename);
 		conf = addDatabase(params, game, filename);
 		if (conf != null) {
 			loadedConfigurations.put(filename, conf);
 		}
 		return conf;
 	}
 	synchronized Configuration addDatabase(Map<String,String> params, String game, String filename) {
 		String solvedJob = serverConf.getProperty("json.solved."+filename,null);
 		String dbPath = serverConf.getProperty("json.databasedirectory","");
 		if (dbPath != null && dbPath.length() > 0) {
 			if (dbPath.charAt(dbPath.length()-1) != '/') {
 				dbPath += '/';
 			}
 			filename = dbPath + filename + ".db";
 		} else {
 			dbPath = null;
 			filename = null;
 		}
 		try {
 			if (solvedJob != null && solvedJob.length() > 0) {
 				System.out.println("Loading solved job "+solvedJob+".");
 				Configuration config = new Configuration(solvedJob);
 				try {
 					config.openDatabase();
 				} catch (Exception e) {
 					Util.warn("Error when loading database for special configuration "+filename, e);
 				}
 				return config;
 			} else if (filename != null && new File(new URI(filename)).exists()) {
 				System.out.println("Loading solved database "+filename+".");
 				Database db = dbClass.getConstructor().newInstance();
 				db.initialize(filename, null);
 				Configuration conf = db.getConfiguration();
 				conf.setDatabase(db);
 				return conf;
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 			Util.warn("Failed to load database "+filename, e);
 		} catch (Util.FatalError fe) {
 			// These aren't actually fatal, so don't rethrow.
 			Util.warn("FatalError(TM) when loading database "+filename+": "+fe.toString());
 		}
 		String unsolvedJob = serverConf.getProperty("json.unsolved."+game,null);
 		if (unsolvedJob != null) {
 			Properties props = Configuration.readProperties(unsolvedJob);
 			String[] allowedFields = serverConf.getProperty("json.fields."+game,"").split(",");
 			for (String key : allowedFields) {
 				key = key.trim();
 				if (key.length() == 0) {
 					continue;
 				}
 				String val = params.get(key);
 				if (val == null) {
 					val = "";
 				}
 				props.setProperty("gamesman.game."+key, val);
 			}
 			props.setProperty("gamesman.hasher", "NullHasher");
 			String gamename = props.getProperty("gamesman.game");
 			Configuration config = null;
 			try {
 				config = new Configuration(props, true);
 				config.initialize(gamename,"NullHasher",false);
 			} catch (ClassNotFoundException e) {
 				Util.warn("Failed to load the game class.", e);
 				throw new RuntimeException("Failed to load the game class.", e);
 			} catch (Util.FatalError fe) {
 				throw new RuntimeException("FatalError when loading configuration for "+game);
 			}
 			return config;
 		} else {
 			//throw new RuntimeException("Failed to find an appropriate job file for " + game);
 			return null;
 		}
 	}
 	
 	private class JSONThread extends Thread {
 
 		final Socket s;
 		
 		JSONThread(Socket s){
 			this.s = s;
 			assert Util.debug(DebugFacility.JSON, "Accepted new connection " + s);
 		}
 		
 		private <T> void fillJSONFields(Configuration conf, JSONObject entry, T state) throws JSONException {
 			Set<RecordFields> storedFields = 
 				conf.getStoredFields().keySet();
 			Database db = conf.getDatabase();
 			Record rec = null;
 			Game<T> g = (Game<T>)conf.getGame();
 			if (db != null) {
 				rec = db.getRecord(g.stateToHash(state));
 			}
 			if (rec != null) {
 				for(RecordFields f : storedFields){
 					if (f == RecordFields.VALUE) {
 						PrimitiveValue pv = rec.get();
						if (pv==PrimitiveValue.WIN) pv = PrimitiveValue.LOSE;
						else if (pv==PrimitiveValue.LOSE) pv = PrimitiveValue.WIN;
 						entry.put(f.name().toLowerCase(),pv.name().toLowerCase());
 					} else {
 						entry.put(f.name().toLowerCase(),rec.get(f));
 					}
 				}
 			} else {
 				PrimitiveValue pv = g.primitiveValue(state);
 				if (pv != PrimitiveValue.UNDECIDED) {
 					if (pv==PrimitiveValue.WIN) pv = PrimitiveValue.LOSE;
 					else if (pv==PrimitiveValue.LOSE) pv = PrimitiveValue.WIN;
 					entry.put("value", pv.name().toLowerCase());
 				}
 				int score = g.primitiveScore(state);
 				if (score > 0) {
 					entry.put("score", score);
 				}
 			}
 			entry.put("board", g.stateToString(state));
 		}
 		
 		class RequestException extends Exception {
 			public RequestException(String msg) {
 				super(msg);
 			}
 		}
 		public void run() {
 			LineNumberReader r = null;
 			PrintWriter w = null;
 			try{
 				r = new LineNumberReader(new InputStreamReader(s.getInputStream()));
 				w = new PrintWriter(new OutputStreamWriter(s.getOutputStream()));
 			}catch (IOException e) {
 				Util.warn("Got IO exception in JSONThread: "+e);
 			}
 			
 			while(true){
 				String line;
 				try {
 					line = r.readLine();
 				} catch (IOException e) {
 					Util.warn("Unable to read line from socket, dropping");
 					break;
 				}
 				if (line == null) {
 					assert Util.debug(DebugFacility.JSON, "Connection closed.");
 					break; // connection closed.
 				}
 				System.out.println(line);
 				Map<String,String> j = new HashMap<String,String>();
 				line = line.replace(';', '&');
 				for (String param : line.split("&")) {
 					String[] key_val = param.split("=",2);
 					if (key_val.length != 2) {
 						continue;
 					}
 					try {
 						j.put(URLDecoder.decode(key_val[0],"utf-8"),
 								URLDecoder.decode(key_val[1],"utf-8"));
 					} catch (UnsupportedEncodingException e) {
 					}
 				}
 				System.out.println(j);
 				JSONObject response;
 				try {
 					try {
 						response = handleRequest(j);
 					} catch (RequestException re) {
 						response = new JSONObject();
 						response.put("status","error");
 						response.put("msg",re.toString());
 					} catch (Util.FatalError fe) {
 						fe.printStackTrace();
 						throw new Exception(fe.toString());
 					}
 				} catch (Exception e) {
 					e.printStackTrace();
 					try {
 						response = new JSONObject();
 						response.put("status", "error");
 						response.put("msg", "An exception was generated: "+e);
 					} catch (JSONException e1) {
 						Util.warn("Could not send an error response to client: "+e);
 						break;
 					}
 				}
 				System.out.println(response.toString());
 				w.println(response.toString());
 				w.flush();
 			}
 		}
 		private <T> JSONObject handleRequest (Map<String,String> j) throws RequestException, JSONException {
 			Configuration config = loadDatabase(j);
 			if (config == null) {
 				throw new RequestException("This game does not exist.");
 			}
 			Database db = config.getDatabase();
 			Game<T> g = (Game<T>)config.getGame();
 			
 			JSONObject response = new JSONObject();
 			
 			String method = j.get("method");
 			if (method == null) {
 				response.put("status", "error");
 				response.put("msg","No method specified!");
 			} else if (method.equals("getNextMoveValues")) {
 				String board = j.get("board");
 				if (board == null) {
 					throw new RequestException("No board passed!");
 				}
 				T state = g.stringToState(board);
 				JSONArray responseArray = new JSONArray();
 
 				PrimitiveValue pv = g.primitiveValue(state);
 				if (pv == PrimitiveValue.UNDECIDED) {
 					// Game is not over yet...
 					for(Pair<String,T> next : g.validMoves(state)){
 						JSONObject entry = new JSONObject();
 						entry.put("move", next.car);
 						fillJSONFields(config,entry, next.cdr);
 						responseArray.put(entry);
 					}
 				}
 				response.put("response", responseArray);
 				response.put("status","ok");
 			} else if (method.equals("getMoveValue")) {
 				String board = j.get("board");
 				if (board == null) {
 					throw new RequestException("No board passed!");
 				}
 				T state = g.stringToState(board);
 				JSONObject entry = new JSONObject();
 				fillJSONFields(config,entry, state);
 				response.put("response", entry);
 				response.put("status","ok");
 			}
 			return response;
 		}
 
 	}
 }
