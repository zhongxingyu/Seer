 package ca.wacos.wacosbans;
 
 import java.sql.*;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.logging.Level;
 
 
 
 public class SQLManager {
 	static String dbname = "bans";
 	static String dbusername;
 	static String dbpass;
 	static String url = "jdbc:mysql://198.27.67.129/";
 
 	/**
 	 * One connection should be fine.
 	 */
 	private static SQLManager manager;
 
 	public static void init(String user, String password, String database, String address) throws SQLException {
 		dbusername = user;
 		dbpass = password;
 		dbname = database;
 		url = "jdbc:mysql://" + address + "/";
 		manager = new SQLManager();
 	}
 
 	private Connection con;
 
 	private SQLManager() throws SQLException {
 
 		Connection con = null;
 		Statement st = null;
 
 		try {
 			WacosBans.logger.info("Connecting to database...");
 			con = DriverManager.getConnection(url, dbusername, dbpass);
 			st = con.createStatement();
 			st.executeUpdate("CREATE DATABASE IF NOT EXISTS  " + dbname);
 			st.close();
 			WacosBans.logger.info("Setting up database...");
 			st = con.createStatement();
 			st.executeUpdate("USE " + dbname);
 			st.close();
 		} catch (SQLException e) {
 			WacosBans.logger.severe("Could not initialize database: " + e.toString());
 			throw e;
 		} finally {
 			attemptClose(st);
 		}
 		this.con = con;
 
 	}
 
 	public static Connection getConnection() {
 		return manager.con;
 	}
 
 	public static void attemptClose(Connection con) {
 		if (con != null) {
 			try {
 				con.close();
 			} catch (Throwable t) {
 
 			}
 		}
 	}
 
 	public static void attemptClose(Statement st) {
 		if (st != null) {
 			try {
 				st.close();
 			} catch (Throwable t) {
 
 			}
 		}
 	}
 
 	public static void attemptClose(PreparedStatement pst) {
 		if (pst != null) {
 			try {
 				pst.close();
 			} catch (Throwable t) {
 
 			}
 		}
 	}
 
 	public static void attemptClose(ResultSet rs) {
 		if (rs != null) {
 			try {
 				rs.close();
 			} catch (Throwable t) {
 
 			}
 		}
 	}
 
 	public static String[] dumpInfo(String player) {
 
 		PreparedStatement pst = null;
 		ResultSet result = null;
 
 		List<String> log = new ArrayList<String>();
 
 		try {
 			pst = getConnection().prepareStatement("SELECT * FROM bans WHERE player = ?");
 			pst.setString(1, player);
 			result = pst.executeQuery();
 			log.add("§eBans for player: " + player);
 			while (result.next()) {
 				boolean a = result.getBoolean("active");
 				log.add("§a" + result.getString("date") + (a ? "§7 (active) §f" : "§7 (inactive) §f") + "> "
 						+ "assigned by: " + result.getString("assigner") + ", reason: "
 						+ result.getString("reason") + (a ? "" : ", pardoned by: " +
 						result.getString("pardoner")));
 			}
 			pst.close();
 			result.close();
 			pst = getConnection().prepareStatement("SELECT * FROM tempbans WHERE player = ?");
 			pst.setString(1, player);
 			result = pst.executeQuery();
 			log.add("§eTemporary bans:");
 			while (result.next()) {
 				boolean a = result.getBoolean("active");
 				log.add("§a" + result.getString("date") + (a ? "§7 (active) §f" : "§7 (inactive) §f") + "> "
 						+ "assigned by: " + result.getString("assigner") + ", reason: "
 						+ result.getString("reason") + " length: " + result.getInt("minutes") + " minutes"
 						+ (a ? "" : ", pardoned by: " + result.getString("pardoner")));
 			}
 			pst.close();
 			result.close();
 			pst = getConnection().prepareStatement("SELECT * FROM warnings WHERE player = ?");
 			pst.setString(1, player);
 			result = pst.executeQuery();
 			log.add("§eWarnings:");
 			while (result.next()) {
 				boolean a = result.getBoolean("active");
 				log.add("§a" + result.getString("date") + (a ? "§7 (active) §f" : "§7 (inactive) §f") + "> "
 						+ "assigned by: " + result.getString("assigner") + ", reason: "
 						+ result.getString("reason") + ", severity: " + result.getInt("severity")
 						+ (a ? "" : ", pardoned by: " + result.getString("pardoner")));
 			}
 
 		} catch (SQLException e) {
 			WacosBans.logger.log(Level.SEVERE, "Error while retrieving data:");
 			e.printStackTrace();
 		} finally {
 			attemptClose(pst);
 			attemptClose(result);
 		}
 		return log.toArray(new String[log.size()]);
 	}
 
 	public static boolean createBan(String player, String reason, String banner) {
 
 		PreparedStatement pst = null;
 		ResultSet result = null;
 		boolean rv = false;
 		try {
 			pst = getConnection().prepareStatement("SELECT * FROM bans WHERE player = ?");
 			pst.setString(1, player);
 			result = pst.executeQuery();
 
 			boolean exists = false;
 			while (result.next()) {
 				if (result.getBoolean("active")) {
 					exists = true;
 					break;
 				}
 			}
 			if (!exists) {
 				DateFormat f = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
 				pst.close();
 				pst = getConnection().prepareStatement("INSERT INTO bans VALUES ( ?, ?, ?, ?, ?, ? )");
 				pst.setString(1, player);
 				pst.setString(2, reason);
 				pst.setString(3, f.format(new java.util.Date()));
 				pst.setInt(4, 1);
 				pst.setString(5, null);
 				pst.setString(6, banner);
 				pst.executeUpdate();
 				rv = true;
 			}
 
 		} catch (SQLException e) {
 			WacosBans.logger.log(Level.SEVERE, "Error while retrieving data:");
 			e.printStackTrace();
 		} finally {
 			attemptClose(pst);
 			attemptClose(result);
 		}
 		return rv;
 	}
 
 	public static boolean createTempBan(String player, String reason, String banner, int minutes) {
 
 		PreparedStatement pst = null;
 		ResultSet result = null;
 		boolean rv = false;
 
 		try {
 			pst = getConnection().prepareStatement("SELECT * FROM tempbans WHERE player = ?");
 			pst.setString(1, player);
 			result = pst.executeQuery();
 
 			boolean exists = false;
 			while (result.next()) {
 				if (result.getBoolean("active")) {
 					exists = true;
 					break;
 				}
 			}
 			if (!exists) {
 				pst.close();
 				DateFormat f = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
 				pst = getConnection().prepareStatement("INSERT INTO tempbans VALUES ( ?, ?, ?, ?, ?, ?, ? )");
 				pst.setString(1, player);
 				pst.setString(2, reason);
 				pst.setString(3, f.format(new java.util.Date()));
 				pst.setInt(4, minutes);
 				pst.setInt(5, 1);
 				pst.setString(6, null);
 				pst.setString(7, banner);
 				pst.executeUpdate();
 				pst.executeUpdate();
 				rv = true;
 			}
 
 		} catch (SQLException e) {
 			WacosBans.logger.log(Level.SEVERE, "Error while retrieving data:");
 			e.printStackTrace();
 		} finally {
 			attemptClose(pst);
 			attemptClose(result);
 		}
 		return rv;
 	}
 
 	public static void createWarning(String player, String reason, String assigner, int severity) {
 		PreparedStatement pst = null;
 		try {
 			DateFormat f = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
 			pst = getConnection().prepareStatement("INSERT INTO warnings VALUES ( ?, ?, ?, ?, ?, ?, ? )");
 			pst.setString(1, player);
 			pst.setString(2, reason);
 			pst.setString(3, f.format(new java.util.Date()));
 			pst.setInt(4, severity);
 			pst.setInt(5, 1);
 			pst.setString(6, null);
 			pst.setString(7, assigner);
 			pst.executeUpdate();
 
 		} catch (SQLException e) {
 			WacosBans.logger.log(Level.SEVERE, "Error while retrieving data:");
 			e.printStackTrace();
 		} finally {
 			attemptClose(pst);
 		}
 	}
 
 	public static boolean deactivateBans(String player, String pardoner) {
 
 		PreparedStatement pst = null;
 		ResultSet result = null;
 
 		boolean removed = false;
 
 		try {
 			pst = getConnection().prepareStatement("SELECT * FROM bans WHERE player = ?");
 			pst.setString(1, player);
 			result = pst.executeQuery();
 
 			while (result.next()) {
 				if (result.getBoolean("active")) {
					pst.close();
 					pst = getConnection().prepareStatement("UPDATE bans SET active = ?, pardoner = ? WHERE player = ?");
 					pst.setInt(1, 0);
 					pst.setString(2, pardoner);
 					pst.setString(3, player);
 					pst.executeUpdate();
 					removed = true;
 				}
 			}
 
 			pst.close();
 			result.close();
 			pst = getConnection().prepareStatement("SELECT * FROM tempbans WHERE player = ?");
 			pst.setString(1, player);
 			result = pst.executeQuery();
 
 			while (result.next()) {
 				if (result.getBoolean("active")) {
					pst.close();
 					pst = getConnection().prepareStatement("UPDATE tempbans SET active = ?, pardoner = ? WHERE player = ?");
 					pst.setInt(1, 0);
 					pst.setString(2, pardoner);
 					pst.setString(3, player);
 					pst.executeUpdate();
 					removed = true;
 				}
 			}
 			pst.close();
 
 		} catch (SQLException e) {
 			WacosBans.logger.log(Level.SEVERE, "Error while retrieving data:");
 			e.printStackTrace();
 		} finally {
 			attemptClose(pst);
 			attemptClose(result);
 		}
 		return removed;
 	}
 
 	public static boolean isBanned(String player) {
 
 		PreparedStatement pst = null;
 		ResultSet result = null;
 
 		boolean rv = false;
 
 		try {
 			DateFormat f = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
 			String date = f.format(new java.util.Date());
 
 			pst = getConnection().prepareStatement("SELECT * FROM bans WHERE player = ?");
 			pst.setString(1, player);
 			result = pst.executeQuery();
 
 			while (result.next()) {
 				if (result.getBoolean("active")) {
 					rv = true;
 					break;
 				}
 			}
 
 			if (!rv) {
 				pst.close();
 				result.close();
 				pst = getConnection().prepareStatement("SELECT * FROM tempbans WHERE player = ?");
 				pst.setString(1, player);
 				result = pst.executeQuery();
 
 				while (result.next()) {
 					if (result.getBoolean("active")) {
 						long v = longDateValue(result.getString("date"));
 						long e = longDateValue(date);
 						int l = result.getInt("minutes");
 						if ((e - v) < l) {
 							rv = true;
 							break;
 						}
 					}
 				}
 			}
 			if (!rv) {
 				pst.close();
 				result.close();
 				pst = getConnection().prepareStatement("UPDATE tempbans SET active = ?, pardoner = ? WHERE player = ? && active = ?");
 				pst.setInt(1, 0);
 				pst.setString(2, "N/A");
 				pst.setString(3, player);
 				pst.setInt(4, 1);
 				pst.executeUpdate();
 			}
 
 		} catch (SQLException e) {
 			WacosBans.logger.log(Level.SEVERE, "Error while retrieving data:");
 			e.printStackTrace();
 		} finally {
 			attemptClose(pst);
 			attemptClose(result);
 		}
 		return rv;
 	}
 
 	public static String getBanMessage(String player) {
 
 		PreparedStatement pst = null;
 		ResultSet result = null;
 
 		String rv = "";
 
 		try {
 			DateFormat f = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
 			String date = f.format(new java.util.Date());
 
 			pst = getConnection().prepareStatement("SELECT * FROM bans WHERE player = ?");
 			pst.setString(1, player);
 			result = pst.executeQuery();
 
 			while (result.next()) {
 				if (result.getBoolean("active")) {
 					rv = "Permenently banned by " + result.getString("assigner") + ": " + result.getString("reason");
 					break;
 				}
 			}
 			pst.close();
 			result.close();
 			pst = getConnection().prepareStatement("SELECT * FROM tempbans WHERE player = ?");
 			pst.setString(1, player);
			pst.executeUpdate();
 
 			while (result.next()) {
 				if (result.getBoolean("active")) {
 					long v = longDateValue(result.getString("date"));
 					long e = longDateValue(date);
 					int l = result.getInt("minutes");
 					if ((e - v) < l) {
 						rv = "Banned by " + result.getString("assigner") + ": " + timeString((int) (l - (e - v))) + ": " + result.getString("reason");
 						break;
 					}
 				}
 			}
 		} catch (SQLException e) {
 			WacosBans.logger.log(Level.SEVERE, "Error while retrieving data:");
 			e.printStackTrace();
 		} finally {
 			attemptClose(pst);
 			attemptClose(result);
 		}
 		return rv;
 	}
 
 	private static long longDateValue(String date) {
 		long total;
 		int daysPassed = 0;
 		int currentYear = Integer.parseInt(date.charAt(8) + "" + date.charAt(9));
 		int currentMonth = Integer.parseInt(date.charAt(0) + "" + date.charAt(1));
 		int currentDay = Integer.parseInt(date.charAt(3) + "" + date.charAt(4));
 		int hoursPassed = Integer.parseInt(date.charAt(11) + "" + date.charAt(12));
 		int minutesPassed = Integer.parseInt(date.charAt(14) + "" + date.charAt(15));
 		currentMonth--;
 		while (currentMonth > 0) {
 			daysPassed += days(currentMonth);
 			currentMonth--;
 		}
 		daysPassed += currentDay;
 
 		total = minutesPassed;
 		total += hoursPassed * 60;
 		total += daysPassed * 60 * 24;
 		total += currentYear * 60 * 24 * 365;
 
 		return total;
 	}
 
 	private static int days(int month) {
 		switch (month) {
 			case 1:
 				return 31;
 			case 2:
 				return 28;
 			case 3:
 				return 31;
 			case 4:
 				return 30;
 			case 5:
 				return 31;
 			case 6:
 				return 30;
 			case 7:
 				return 31;
 			case 8:
 				return 31;
 			case 9:
 				return 30;
 			case 10:
 				return 31;
 			case 11:
 				return 30;
 			case 12:
 				return 31;
 
 		}
 		return 30;
 	}
 
 	private static String timeString(int minutes) {
 		int hours = minutes / 60;
 		minutes -= hours * 60;
 		int days = hours / 24;
 		hours -= days * 24;
 		String v = "";
 		if (days > 0) {
 			v += days + " days, ";
 		}
 		if (hours > 0) {
 			v += hours + " hours, ";
 		}
 		v += minutes + " minutes";
 		return v;
 	}
 }
