 package FRC_Score_Sys;
 
 import java.io.File;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.Statement;
 import java.util.ArrayList;
 import java.util.List;
 
 public class SqlDB {
 
 	private Connection c;
 	private String DBfile = "score_data.db";
 
 	public SqlDB() {
 		System.out.println("Starting SQL Server..");
 		File file = new File(DBfile);
 		Boolean doNew = false;
 		if (!file.exists()) {
 			doNew = true;
 			System.out.println("DB File not found. Will create.");
 		} else {
 			System.out.println("DB File found. using existing DB.");
 		}
 		file = null;
 		try {
 			System.out.println("Looking for SQLLite Driver..");
 			Class.forName("org.sqlite.JDBC");
 			System.out.println("Found. Connecting to Database..");
 			c = DriverManager.getConnection("jdbc:sqlite:" + DBfile);
 			if (doNew) {
 				if (!GenerateNewDatabase()) {
 					System.out
 							.println("DB Subsystem failed to create DB tables. This is fatal.");
 					System.exit(-1);
 				}
 			}
 			System.out.println("DB Subsystem up and running!");
 		} catch (Exception e) {
 			ExceptionHandler(e, true);
 		}
 	}
 
 	public int AddMatchToDB(String[] matchInfo) {
 		try {
 			PreparedStatement s = c
					.prepareStatement("INSERT INTO MATCHES VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0)");
 			int i = 1;
 			boolean first = false;
 			for (String item : matchInfo) {
 				System.out.print("Evaluating '" + item + "'.. ");
 				if (!first) {
 					System.out.println("Match ID");
 					item = PadString(item);
 					s.setString(i, "QQ" + item);
 					first = true;
 				} else {
 					System.out.println("Data");
 					s.setInt(i, Integer.parseInt(item));
 				}
 				i = i + 1;
 			}
 			int ret = s.executeUpdate();
 			s.close();
 			return ret;
 		} catch (Exception e) {
 			ExceptionHandler(e, false);
 			return -1;
 		}
 	}
 
 	public void close() {
 		System.out.println("SQL Lite DB Closing Gracefully. Goodnight!");
 		try {
 			c.close();
 		} catch (Exception e) {
 			ExceptionHandler(e, false);
 		}
 	}
 
 	private void ExceptionHandler(Exception e, boolean fatal) {
 		System.out.println("SQL EXCEPTION: " + e.getClass().getName() + ": "
 				+ e.getMessage());
 		if (fatal) {
 			System.exit(-1);
 		}
 	}
 
 	public List<SingleMatch> FetchMatch(String id) {
 		System.out.println("Match Fetch Requested for id " + id);
 		List<SingleMatch> ScoreList = new ArrayList<SingleMatch>();
 		try {
 			PreparedStatement s = c
 					.prepareStatement("SELECT * FROM MATCHES WHERE id = ? LIMIT 1");
 			s.setString(1, id);
 			ResultSet rs = s.executeQuery();
 			String[] clrs = { "R", "B" };
 			while (rs.next()) {
 				for (String clr : clrs) {
 					System.out.println("Gathering " + clr + "'s");
 					SingleMatch Scores = new SingleMatch(id, clr);
 					Scores.Robot1 = rs.getInt(clr + "1Robot");
 					Scores.Robot2 = rs.getInt(clr + "2Robot");
 					Scores.Robot3 = rs.getInt(clr + "3Robot");
 
 					Scores.Sur1 = rs.getBoolean(clr + "1Sur");
 					Scores.Sur2 = rs.getBoolean(clr + "2Sur");
 					Scores.Sur3 = rs.getBoolean(clr + "3Sur");
 
 					Scores.Climb1 = rs.getInt(clr + "1Climb");
 					Scores.Climb2 = rs.getInt(clr + "2Climb");
 					Scores.Climb3 = rs.getInt(clr + "3Climb");
 
 					Scores.Dq1 = rs.getBoolean(clr + "1Dq");
 					Scores.Dq2 = rs.getBoolean(clr + "2Dq");
 					Scores.Dq3 = rs.getBoolean(clr + "3Dq");
 
 					Scores.DisksLA = rs.getInt(clr + "DisksLA");
 					Scores.DisksLT = rs.getInt(clr + "DisksLT");
 					Scores.DisksMA = rs.getInt(clr + "DisksMA");
 					Scores.DisksMT = rs.getInt(clr + "DisksMT");
 					Scores.DisksHA = rs.getInt(clr + "DisksHA");
 					Scores.DisksHT = rs.getInt(clr + "DisksHT");
 					Scores.DisksP = rs.getInt(clr + "DisksP");
 
 					Scores.Foul = rs.getInt(clr + "Foul");
 					Scores.TFoul = rs.getInt(clr + "TFoul");
 
 					Scores.Score = rs.getInt(clr + "Score");
 					ScoreList.add(Scores);
 				}
 			}
 
 		} catch (Exception e) {
 			ExceptionHandler(e, false);
 		}
 		return ScoreList;
 	}
 
 	public List<String> FetchMatchList(String type) {
 		System.out.println("Match List Fetch Requested for type " + type);
 		List<String> WholeList = new ArrayList<String>();
 		try {
 			PreparedStatement s = c
 					.prepareStatement("SELECT id FROM MATCHES WHERE id LIKE ?");
 			s.setString(1, type + "%");
 			ResultSet rs = s.executeQuery();
 			while (rs.next()) {
 				WholeList.add(rs.getString("id"));
 			}
 
 		} catch (Exception e) {
 			ExceptionHandler(e, false);
 		}
 		return WholeList;
 	}
 
 	private boolean GenerateNewDatabase() {
 		System.out.println("Creating new database tables..");
 		String q = "CREATE TABLE MATCHES " + "(ID		TEXT	PRIMARY KEY	NOT NULL,"
 				+ "R1Robot 	INT		NOT NULL,"
 				+ "R1Sur		BOOLEAN	NOT NULL DEFAULT(0),"
 				+ "R2Robot	INT		NOT NULL,"
 				+ "R2Sur		BOOLEAN NOT NULL DEFAULT(0),"
 				+ "R3Robot	INT		NOT NULL,"
 				+ "R3Sur		BOOLEAN NOT NULL DEFAULT(0),"
 				+ "B1Robot	INT		NOT NULL,"
 				+ "B1Sur		BOOLEAN NOT NULL DEFAULT(0),"
 				+ "B2Robot	INT		NOT NULL,"
 				+ "B2Sur		BOOLEAN NOT NULL DEFAULT(0),"
 				+ "B3Robot	INT		NOT NULL,"
 				+ "B3Sur		BOOLEAN NOT NULL DEFAULT(0),"
 				+ "RDisksLA	INT		NOT NULL DEFAULT(0),"
 				+ "RDisksLT	INT		NOT NULL DEFAULT(0),"
 				+ "RDisksMA	INT		NOT NULL DEFAULT(0),"
 				+ "RDisksMT	INT		NOT NULL DEFAULT(0),"
 				+ "RDisksHA	INT		NOT NULL DEFAULT(0),"
 				+ "RDisksHT	INT		NOT NULL DEFAULT(0),"
 				+ "RDisksP	INT		NOT NULL DEFAULT(0),"
 				+ "BDisksLA	INT		NOT NULL DEFAULT(0),"
 				+ "BDisksLT	INT		NOT NULL DEFAULT(0),"
 				+ "BDisksMA	INT		NOT NULL DEFAULT(0),"
 				+ "BDisksMT	INT		NOT NULL DEFAULT(0),"
 				+ "BDisksHA	INT		NOT NULL DEFAULT(0),"
 				+ "BDisksHT	INT		NOT NULL DEFAULT(0),"
 				+ "BDisksP	INT		NOT NULL DEFAULT(0),"
 				+ "R1Climb	INT		NOT NULL DEFAULT(0),"
 				+ "R2Climb	INT		NOT NULL DEFAULT(0),"
 				+ "R3Climb	INT		NOT NULL DEFAULT(0),"
 				+ "B1Climb	INT		NOT NULL DEFAULT(0),"
 				+ "B2Climb	INT		NOT NULL DEFAULT(0),"
 				+ "B3Climb	INT		NOT NULL DEFAULT(0),"
 				+ "R1Dq		BOOLEAN	NOT NULL DEFAULT(0),"
 				+ "R2Dq		BOOLEAN	NOT NULL DEFAULT(0),"
 				+ "R3Dq		BOOLEAN	NOT NULL DEFAULT(0),"
 				+ "B1Dq		BOOLEAN	NOT NULL DEFAULT(0),"
 				+ "B2Dq		BOOLEAN	NOT NULL DEFAULT(0),"
 				+ "B3Dq		BOOLEAN	NOT NULL DEFAULT(0),"
 				+ "RFoul		INT		NOT NULL DEFAULT(0),"
 				+ "BFoul		INT		NOT NULL DEFAULT(0),"
 				+ "RTFoul		INT		NOT NULL DEFAULT(0),"
 				+ "BTFoul		INT		NOT NULL DEFAULT(0),"
 				+ "RScore		INT		NOT NULL DEFAULT(0),"
 				+ "BScore		INT		NOT NULL DEFAULT(0))";
 		int cre = PerformInternalUpdateQuery(q);
 		if (cre != 0) {
 			System.out.println("Table Create failed!");
 			return false;
 		} else {
 			System.out.println("Table Create successful!");
 			return true;
 		}
 	}
 
 	private String PadString(String inStr) {
 		if (inStr.length() >= 4) {
 			return inStr;
 		}
 		String out = inStr;
 		while (out.length() != 4) {
 			out = "0" + out;
 		}
 		return out;
 	}
 
 	public int PerformInternalUpdateQuery(String q) {
 		try {
 			Statement st = c.createStatement();
 			int ret = st.executeUpdate(q);
 			st.close();
 			return ret;
 		} catch (Exception e) {
 			ExceptionHandler(e, false);
 			return -1;
 		}
 	}
 }
