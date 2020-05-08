 package log;
 
 import com.sun.jdi.Value;
 import java.sql.*;
 import java.util.HashMap;
 
 public class SQLiteLogger extends Logger {
 
 
 	private static String createRuns = "CREATE TABLE IF NOT EXISTS RUNS ( " +
 			"runsid INTEGER PRIMARY KEY AUTOINCREMENT, " +
 			"programname TEXT, " +
 			"args TEXT, " +
 			"whitelist TEXT, " +
 			"blacklist TEXT, " +
 			"username TEXT, " +
 			"timestamp TEXT)";
 
 	private static String createLines = "CREATE TABLE IF NOT EXISTS LINES ( " +
 			"runsid INTEGER REFERENCES RUNS (runsid), " +
 			"linesid INTEGER KEY," +
 			"filepath TEXT," +
 			"linenum INTEGER, " +
 			"timestamp TEXT, " +
 			"PRIMARY KEY (runsid, linesid))";
 
 	private static String createVars = "CREATE TABLE IF NOT EXISTS VARS ( " +
 			"runsid INTEGER REFERENCES RUNS (runsid), " +
 			"varsid INTEGER, " +
 			"linesid INTEGER REFERENCES LINES (linesid), " +
 			"name TEXT," +
 			"type TEXT, " +
 			"PRIMARY KEY (runsid, varsid))";
 
 	private static String createVarValues = "CREATE TABLE IF NOT EXISTS VARVALUES ( " +
 			"runsid INTEGER REFERENCES RUNS (runsid)," +
 			"varvaluesid INTEGER," +
 			"varsid INTEGER REFERENCES VARS (varsid), " +
 			"linesid INTEGER REFERENCES LINES (linesid)," +
 			"value TEXT, " +
 			"PRIMARY KEY (runsid, varvaluesid))";
 
 	private static String createVarDeath = "CREATE TABLE IF NOT EXISTS VARDEATH (" +
 			"runsid INTEGER REFERENCES RUNS (runsid)," +
 			"varsid INTEGER REFERENCES VARS (varsid)," +
 			"linesid INTEGER REFERENCES LINES (linesid)," +
 			"PRIMARY KEY (runsid, varsid))";
 
 	private static String createVarUsed = "CREATE TABLE IF NOT EXISTS VARUSED (" +
 			"runsid INTEGER REFERENCES RUNS (runsid)," +
 			"linesid INTEGER REFERENCES LINES (linesid)," +
 			"varvaluessid INTEGER REFERENCES VARS (varvaluessid)," +
 			"PRIMARY KEY (runsid, linesid, varvaluessid))";
 	
 	private static String createProgramExit = "CREATE TABLE IF NOT EXISTS PROGRAMEXIT (" +
 			"runsid INTEGER PRIMARY KEY REFERENCES RUNS (runsid)," +
 			"exitcode INTEGER," +
 			"exception TEXT)";
 
 
 	private static String runsInsert = "INSERT INTO RUNS " +
 			"(runsid, programname, args, whitelist, blacklist, username, timestamp) " +
 			"VALUES (NULL, ?, ?, ?, ?, ?, ?)";
 
 	private static String linesInsert = "INSERT INTO LINES " +
 			"(runsid, linesid, filepath, linenum, timestamp) " +
 			"VALUES (?, ?, ?, ?, ?)";
 
 	private static String varInsert = "INSERT INTO VARS " +
 			"(runsid, varsid, linesid, name, type) " +
 			"VALUES (?, ?, ?, ?, ?)";
 
 	private static String  varValuesInsert = "INSERT INTO VARVALUES " +
 			"(runsid, varvaluesid, varsid, linesid, value) " +
 			"VALUES (?, ?, ?, ?, ?)";
 
 	private static String varDeathInsert = "INSERT INTO VARDEATH " +
 			"(runsid, varsid, linesid)" +
 			"VALUES (?, ?, ?)";
 
 	private static String varUsedInsert = "INSERT INTO VARUSED " +
 			"(runsid, linesid, varvaluessid) " +
 			"VALUES (?, ?, ?)";
 
 	private static String programExitInsert = "INSERT INTO PROGRAMEXIT " +
 			"(runsid, exitcode, exception) " +
 			"VALUES (?, ?, ?)";
 
 	private HashMap<String,Integer> availableVars;
 	private int varCounter;
 
 	private Connection conn;
 	private int runId;
 
 	private int varValuesId;
 
 	public SQLiteLogger() {
 		// initialize db, make sure we exist
 
 		availableVars = new HashMap<String, Integer>();
 
 		try {
 			Class.forName("org.sqlite.JDBC");
 			conn = DriverManager.getConnection("jdbc:sqlite:sorbet_out.db");
 			Statement stmt = conn.createStatement();
 			stmt.addBatch(createRuns);
 			stmt.addBatch(createLines);
 			stmt.addBatch(createVars);
 			stmt.addBatch(createVarValues);
 			stmt.addBatch(createVarUsed);
 			stmt.addBatch(createVarDeath);
 			stmt.addBatch(createProgramExit);
 			stmt.executeBatch();
 
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (ClassNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 	}
 
 
 	@Override
 	public void logProgramStart(String programName, String args, String whitelist, String blacklist) {
 		try {
 			PreparedStatement prep = conn.prepareStatement(runsInsert);
 			prep.setString(1, programName);
 			prep.setString(2, args);
 			prep.setString(3, whitelist);
 			prep.setString(4, blacklist);
 
 			prep.execute();
 
 			Statement stat = conn.createStatement();
 			ResultSet rs = stat.executeQuery("SELECT runsid FROM RUNS ORDER BY runsid DESC LIMIT 1");
 
 			runId = rs.getInt("runsid");
 			line = 0;
 			varValuesId = 0;
 
 			rs.close();
 
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 	}
 
 	@Override
 	public void logVarCreated(String name, String type) {
 		try {
 			PreparedStatement prep = conn.prepareStatement(varInsert);
 			prep.setInt(1, runId);
 			prep.setInt(2, varCounter);
 			prep.setInt(3, line);
 			prep.setString(4, name);
 			prep.setString(5, type);
 
 			prep.execute();
 			
 			varCounter++;
 
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	@Override
 	public void logVarChanged(String var, String value) {
 		try {
 			PreparedStatement prep = conn.prepareStatement(varValuesInsert);
 			prep.setInt(1, runId);
 			prep.setInt(2, varValuesId);
 			prep.setInt(3, varCounter);
 			prep.setInt(4, line);
 			prep.setString(5, value);
 
 			prep.execute();
 
 			availableVars.put(var, varValuesId);
 
 
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	@Override
 	public void logVarDeath(String var) {
 		try {
 			PreparedStatement prep = conn.prepareStatement(varDeathInsert);
 			prep.setInt(1, runId);
 			prep.setInt(2, availableVars.get(var));
 			prep.setInt(3, line);
 
 			prep.execute();
 
 			availableVars.remove(var);
 
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	@Override
 	public void logVarUsed(String var) {
 		try {
 			PreparedStatement prep = conn.prepareStatement(varUsedInsert);
 			prep.setInt(1, runId);
 			prep.setInt(2, line);
 			prep.setInt(3, availableVars.get(var));
 
 			prep.execute();
 
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (NullPointerException e) {
 			// TODO Variable doens't exit, just move on?
 			return;
 		}
 	}
 
 	@Override
 	public void logProgramExit(int exitCode, String exception) {
 		try {
 			PreparedStatement prep = conn.prepareStatement(varInsert);
 			prep.setInt(1, runId);
 			prep.setInt(2, exitCode);
 			prep.setString(3, exception);
 
 			prep.execute();
 
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	@Override
 	public void logLines(String filePath, int lineNum) {
 		
 		try {
 			PreparedStatement prep = conn.prepareStatement(linesInsert);
 			prep.setInt(1, runId);
 			prep.setInt(2, line);
 			prep.setString(3, filePath);
 			prep.setInt(4, lineNum);
 			prep.setString(5, new Long(System.nanoTime()).toString());
 
 			prep.execute();
 
 			nextLine();
 			
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 }
