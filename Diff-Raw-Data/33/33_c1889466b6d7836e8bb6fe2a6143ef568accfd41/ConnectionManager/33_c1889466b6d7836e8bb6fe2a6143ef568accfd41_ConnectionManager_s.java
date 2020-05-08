 /*
  * Hydra
  * Copyright (C) 2012 Bryan Emmanuel
  * 
  * This program is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  *  
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
  *  
  *  Bryan Emmanuel piusvelte@gmail.com
  */
 package com.piusvelte.hydra;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.PrintWriter;
 import java.io.UnsupportedEncodingException;
 import java.math.BigInteger;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 import java.security.SecureRandom;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Properties;
 import java.util.Set;
 
 import javax.servlet.ServletContext;
 
 import org.json.simple.JSONArray;
 import org.json.simple.JSONObject;
 
 public class ConnectionManager {
 
 	private static final String sPassphrase = "passphrase";
 	private static final String sDatabases = "databases";
 	private static final String sType = "type";
 	private static final String sDatabase = "database";
 	private static final String sHost = "host";
 	private static final String sPort = "port";
 	private static final String sUsername = "username";
 	private static final String sPassword = "password";
 	private static final String sConnections = "connections";
 	private static final String sQueueRetryInterval = "queueretryinterval";
 	private static final String sDASU = "DASU";
 	private static final String sDASP = "DASP";
 	private static final String sSQLENVINIT = "SQLENVINIT";
 	protected static final String DB_TYPE_UNIDATA = "unidata";
 	protected static final String DB_TYPE_MYSQL = "mysql";
 	protected static final String DB_TYPE_MSSQL = "mssql";
 	protected static final String DB_TYPE_ORACLE = "oracle";
 	protected static final String DB_TYPE_POSTGRESQL = "postgresql";
 	private String passphrase = null;
 	private int[] databaseLock = new int[0];
 	private HashMap<String, HashMap<String, String>> sDatabaseSettings = new HashMap<String, HashMap<String, String>>();
 	private HashMap<String, ArrayList<DatabaseConnection>> sDatabaseConnections = new HashMap<String, ArrayList<DatabaseConnection>>();
 	private HashMap<String, Integer> queuedDatabaseRequests = new HashMap<String, Integer>();
 	private String sHydraDir = null;
 	private String sQueueFile = null;
 	private QueueThread sQueueThread = null;
 	private int[] sQueueLock = new int[0];
 	private String tokenFile = null;
 	private String HYDRA_PROPERTIES = "hydra.properties";
 	private int mQueueRetryInterval;
 	private int[] tokenLock = new int[0];
 	private ArrayList<String> tokens = new ArrayList<String>();
 	private HashMap<String, String> unauthorizedTokens = new HashMap<String, String>();
 	
 	private static final String WIN_DIR = "ProgramData";
 	private static final String NIX_DIR = "var/lib";
 
 	private static ConnectionManager hydraService = null;
 
 	private ConnectionManager(ServletContext ctx) {
 
 		ctx.log("Hydra ConnectionManager instantiated");
 
		String fullPathParts[] = ctx.getRealPath(File.separator).split(File.separator, -1);

		sHydraDir = fullPathParts[0] + File.separator;
 		
 		if (System.getProperty("os.name").startsWith("Windows")) {
			sHydraDir += WIN_DIR;
 		} else {
			sHydraDir += NIX_DIR;
 		}
		sHydraDir += File.separator + "hydra";
 		
 		
 		if (fullPathParts.length > 2) {
 			if (fullPathParts.length > 3) {
 				sHydraDir += File.separator + fullPathParts[fullPathParts.length - 3];
 			}
 			sHydraDir += File.separator + fullPathParts[fullPathParts.length - 2];
 		}
 
 		mQueueRetryInterval = QueueThread.DEFAULT_QUEUERETRYINTERVAL;
 		
 		File hydraDir = new File(sHydraDir);
 		if (hydraDir.exists()) {
 			sHydraDir += File.separator;
			InputStream is = ctx.getResourceAsStream(sHydraDir + HYDRA_PROPERTIES);
 			if (is != null) {
 				Properties properties = new Properties();
 				try {
 					properties.load(is);
 					ctx.log("Hydra properties file read");
 					if (properties.containsKey(sPassphrase))
 						passphrase = properties.getProperty(sPassphrase);
 					sQueueFile = sHydraDir + "queue";
 					tokenFile = sHydraDir + "tokens";
 					try {
 						loadTokens();
 					} catch (Exception e) {
 						e.printStackTrace();
 					}
 					if (properties.containsKey(sQueueRetryInterval))
 						mQueueRetryInterval = Integer.parseInt(properties.getProperty(sQueueRetryInterval));
 					if (properties.containsKey(sDatabases)) {
 						String[] databaseAliases = properties.getProperty(sDatabases).split(",", -1);
 						String[] databaseProperties = new String[]{sType, sDatabase, sHost, sPort, sUsername, sPassword, sConnections, sDASU, sDASP, sSQLENVINIT};
 						for (String databaseAlias : databaseAliases) {
 							HashMap<String, String> database = new HashMap<String, String>();
 							for (String databaseProperty : databaseProperties) {
 								database.put(databaseProperty, properties.getProperty(databaseAlias + "." + databaseProperty, ""));
 							}
 							synchronized (databaseLock) {
 								sDatabaseSettings.put(databaseAlias, database);
 								sDatabaseConnections.put(databaseAlias, new ArrayList<DatabaseConnection>());
 								queuedDatabaseRequests.put(databaseAlias, 0);
 							}
 						}
 					}
 				} catch (IOException e) {
 					e.printStackTrace();
 				}
 			} else {
 				initProps();
 			}
 		} else if (hydraDir.mkdirs()) {
 			sHydraDir += File.separator;
 			initProps();
 		} else {
 			ctx.log("properties doesn't exist, and creating it failed at: " + sHydraDir);
 		}
 	}
 	
 	private void initProps() {
 		Properties properties = new Properties();
 		properties.put(sPassphrase, "changeit");
 		properties.put(sDatabases, "");
 		PrintWriter pw;
 		try {
 			pw = new PrintWriter(new FileOutputStream(sHydraDir + HYDRA_PROPERTIES));
 			properties.store(pw, "The passphrase is used to authorize tokens\n Databases should be a comment delimited string of database aliases, followed by their connection properties\n" +
 					" database types:\n   unidata\n   mysql\n   mssql\n   oracle\n   postgresql\n\nexample:" +
 					"databases=mydb\n"
 					+ "mydb.type=unidata\n"
 					+ "mydb.database=C:\\U2\\ud73\\demo\n"
 					+ "mydb.host=localhost\n"
 					+ "mydb.port=31438\n"
 					+ "mydb.username=myuser\n"
 					+ "mydb.password=mypss\n"
 					+ "mydb.connections=1\n"
 					+ "mydb.DASU=mydasu\n"
 					+ "mydb.DASP=mydasp\n"
 					+ "mddb.SQLENVINIT=\n");
 			pw.close();
 		} catch (FileNotFoundException e1) {
 			e1.printStackTrace();
 		} catch (IOException e1) {
 			e1.printStackTrace();
 		}
 	}
 
 	public static ConnectionManager getInstance(ServletContext ctx) {
 		if (hydraService == null)
 			hydraService = new ConnectionManager(ctx);
 		return hydraService;
 	}
 
 	DatabaseConnection getDatabaseConnection(String database) throws Exception {
 		// return an existing database connection, or spawn a new one if the pool isn't full
 		// check for existing connection
 		synchronized (databaseLock) {
 			if (sDatabaseConnections.containsKey(database)) {
 				ArrayList<DatabaseConnection> connections = sDatabaseConnections.get(database);
 				for (DatabaseConnection databaseConnection : connections) {
 					if (databaseConnection.connect())
 						return databaseConnection;
 				}
 			}
 		}
 		DatabaseConnection databaseConnection = null;
 		synchronized (databaseLock) {
 			if (sDatabaseSettings.containsKey(database)) {
 				ArrayList<DatabaseConnection> connections = sDatabaseConnections.get(database);
 				HashMap<String, String> databaseSettings = sDatabaseSettings.get(database);
 				if (connections.size() < Integer.parseInt(databaseSettings.get(sConnections))) {
 					String type = databaseSettings.get(sType);
 					int port = Integer.parseInt(databaseSettings.get(sPort));
 					if (DB_TYPE_UNIDATA.equals(type))
 						(databaseConnection = new UnidataConnection(databaseSettings.get(sHost), port, databaseSettings.get(sDatabase), databaseSettings.get(sUsername), databaseSettings.get(sPassword), databaseSettings.get(sDASU), databaseSettings.get(sDASP), databaseSettings.get(sSQLENVINIT))).connect();
 					else if (DB_TYPE_MSSQL.equals(type))
 						(databaseConnection = new MSSQLConnection(databaseSettings.get(sHost), port, databaseSettings.get(sDatabase), databaseSettings.get(sUsername), databaseSettings.get(sPassword))).connect();
 					else if (DB_TYPE_ORACLE.equals(type))
 						(databaseConnection = new OracleConnection(databaseSettings.get(sHost), port, databaseSettings.get(sDatabase), databaseSettings.get(sUsername), databaseSettings.get(sPassword))).connect();
 					else if (DB_TYPE_MYSQL.equals(type))
 						(databaseConnection = new MySQLConnection(databaseSettings.get(sHost), port, databaseSettings.get(sDatabase), databaseSettings.get(sUsername), databaseSettings.get(sPassword))).connect();
 					else if (DB_TYPE_POSTGRESQL.equals(type))
 						(databaseConnection = new PostgreSQLConnection(databaseSettings.get(sHost), port, databaseSettings.get(sDatabase), databaseSettings.get(sUsername), databaseSettings.get(sPassword))).connect();
 					else
 						throw new Exception("unknown type:" + type);
 					connections.add(databaseConnection);
 					sDatabaseConnections.put(database, connections);
 					return databaseConnection;
 				}
 			} else
 				throw new Exception("Database " + database + " not defined.");
 		}
 		return databaseConnection;
 	}
 
 	void queueDatabaseRequest(String database) {
 		synchronized (databaseLock) {
 			queuedDatabaseRequests.put(database, queuedDatabaseRequests.get(database) + 1);
 		}
 	}
 
 	void dequeueDatabaseRequest(String database) {
 		synchronized (databaseLock) {
 			queuedDatabaseRequests.put(database, queuedDatabaseRequests.get(database) - 1);
 		}
 	}
 
 	void cleanDatabaseConnections(String database) {
 		synchronized (databaseLock) {
 			ArrayList<DatabaseConnection> connections = sDatabaseConnections.get(database);
 			for (int i = 0; (i < connections.size()) && (connections.size() > queuedDatabaseRequests.get(database)); i++) {
 				DatabaseConnection connection = connections.get(i);
 				if (!connection.isLocked()) {
 					try {
 						connection.disconnect();
 					} catch (Exception e) {
 						e.printStackTrace();
 					}
 					connections.remove(i--);
 				}
 			}
 			sDatabaseConnections.put(database, connections);
 		}
 	}
 
 	@SuppressWarnings("unchecked")
 	JSONObject getDatabases() {
 		JSONObject response = new JSONObject();
 		JSONArray rows = new JSONArray();
 		synchronized (databaseLock) {
 			Set<String> databases = sDatabaseSettings.keySet();
 			Iterator<String> iter = databases.iterator();
 			while (iter.hasNext()) {
 				JSONArray rowData = new JSONArray();
 				rowData.add(iter.next());
 				rows.add(rowData);
 			}
 		}
 		response.put("result", rows);
 		return response;
 	}
 
 	@SuppressWarnings("unchecked")
 	JSONObject getDatabase(String database) {
 		JSONObject response = new JSONObject();
 		JSONArray rows = new JSONArray();
 		synchronized (databaseLock) {
 			if (sDatabaseSettings.containsKey(database)) {
 				JSONArray rowData = new JSONArray();
 				HashMap<String, String> databaseSettings = sDatabaseSettings.get(database);
 				rowData.add(database);
 				rowData.add(databaseSettings.get(sDatabase));
 				rowData.add(databaseSettings.get(sHost));
 				rowData.add(databaseSettings.get(sPort));
 				rowData.add(databaseSettings.get(sType));
 				rows.add(rowData);
 			}
 		}
 		response.put("result", rows);
 		return response;
 	}
 
 	static String getHash64(String in) {
 		String out = null;
 		MessageDigest md;
 		try {
 			md = MessageDigest.getInstance("SHA-256");
 			md.update(in.getBytes("UTF-8"));
 			out = new BigInteger(1, md.digest()).toString(16);
 			StringBuffer hexString = new StringBuffer();
 			byte[] hash = md.digest();
 			for (byte b : hash) {
 				if ((0xFF & b) < 0x10)
 					hexString.append("0" + Integer.toHexString((0xFF & b)));
 				else
 					hexString.append(Integer.toHexString(0xFF & b));
 			}
 			out = hexString.toString();
 			if (out.length() > 64)
 				return out.substring(0, 64);
 		} catch (NoSuchAlgorithmException e) {
 			e.printStackTrace();
 		} catch (UnsupportedEncodingException e) {
 			e.printStackTrace();
 		}
 		return out;
 	}
 
 	public boolean isAuthenticated(String token) {
 		if (token == null)
 			return false;
 		else
 			return tokens.contains(token);
 	}
 
 	void loadTokens() throws Exception {
 		if (tokenFile != null) {
 			synchronized (tokenLock) {
 				String token = null;
 				BufferedReader br;
 				try {
 					br = new BufferedReader(new InputStreamReader(new FileInputStream(tokenFile)));
 					br.readLine();
 					while ((token = br.readLine()) != null)
 						tokens.add(getHash64(token + passphrase));
 					br.close();
 				} catch (FileNotFoundException e) {
 					e.printStackTrace();
 				} catch (IOException e) {
 					e.printStackTrace();
 				}
 			}
 		}
 	}
 
 	String createToken() throws Exception {
 		if (tokenFile != null) {
 			String token = new BigInteger(256, new SecureRandom()).toString(16);
 			MessageDigest md;
 			try {
 				md = MessageDigest.getInstance("SHA-256");
 				md.update(token.getBytes("UTF-8"));
 				token = new BigInteger(1, md.digest()).toString(16);
 				if (token.length() > 64)
 					token = token.substring(0, 64);
 			} catch (NoSuchAlgorithmException e) {
 				e.printStackTrace();
 				throw new Exception("error generating token");
 			} catch (UnsupportedEncodingException e) {
 				e.printStackTrace();
 				throw new Exception("error generating token");
 			}
 			synchronized (tokenLock) {
 				unauthorizedTokens.put(getHash64(token + passphrase), token);
 				return token;
 			}
 		} else
 			throw new Exception("no tokens available");
 	}
 
 	void authorizeToken(String token) throws Exception {
 		if (tokenFile != null) {
 			synchronized (tokenLock) {
 				if (unauthorizedTokens.containsKey(token)) {
 					PrintWriter pw;
 					try {
 						pw = new PrintWriter(new FileOutputStream(tokenFile, true));
 						pw.println(unauthorizedTokens.get(token));
 						pw.close();
 						tokens.add(getHash64(unauthorizedTokens.get(token) + passphrase));
 					} catch (FileNotFoundException e) {
 						e.printStackTrace();
 						throw new Exception("error storing token");
 					}
 					unauthorizedTokens.remove(token);
 				}
 			}
 		} else
 			throw new Exception("no tokens available");
 	}
 
 	boolean queueRequest(String request) {
 		boolean queued = false;
 		if (sQueueFile != null) {
 			synchronized (sQueueLock) {
 				PrintWriter pw;
 				try {
 					pw = new PrintWriter(new FileOutputStream(sQueueFile, true));
 					pw.println(request);
 					pw.close();
 					queued = true;
 				} catch (FileNotFoundException e) {
 					e.printStackTrace();
 				}
 			}
 			if (queued)
 				startQueueThread();
 		}
 		return queued;
 	}
 
 	String dequeueRequest() {
 		String request = null;
 		synchronized (sQueueLock) {
 			BufferedReader br;
 			try {
 				br = new BufferedReader(new InputStreamReader(new FileInputStream(sQueueFile)));
 				request = br.readLine();
 				br.close();
 			} catch (FileNotFoundException e) {
 				e.printStackTrace();
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 			return request;
 		}
 	}
 
 	void requeueRequest(String request) {
 		ArrayList<String> requests = new ArrayList<String>();
 		synchronized (sQueueLock) {
 			BufferedReader br;
 			try {
 				br = new BufferedReader(new InputStreamReader(new FileInputStream(sQueueFile)));
 				br.readLine();
 				while ((request = br.readLine()) != null) {
 					requests.add(request);
 				}
 				br.close();
 			} catch (FileNotFoundException e) {
 				e.printStackTrace();
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 		for (String r : requests)
 			queueRequest(r);
 	}
 
 	void startQueueThread() {
 		if (sQueueFile != null) {
 			synchronized (sQueueLock) {
 				if (sQueueThread == null) {
 					sQueueThread = new QueueThread(this, mQueueRetryInterval);
 					sQueueThread.start();
 				}
 			}
 		}
 	}
 
 	boolean stopQueueThread() {
 		synchronized (sQueueLock) {
 			if (sQueueThread != null) {
 				sQueueThread.shutdown();
 				sQueueThread = null;
 				return true;
 			} else
 				return false;
 		}
 	}
 }
