 package de.team55.mms.server.db;
 
 import java.awt.List;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.sql.Timestamp;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.Properties;
 
 import de.team55.mms.data.Fach;
 import de.team55.mms.data.Feld;
 import de.team55.mms.data.Modul;
 import de.team55.mms.data.Modulhandbuch;
 import de.team55.mms.data.Nachricht;
 import de.team55.mms.data.StellvertreterList;
 import de.team55.mms.data.Studiengang;
 import de.team55.mms.data.User;
 import de.team55.mms.data.pordnung;
 //import de.team55.mms.data.Zuordnung;
 import de.team55.mms.gui.mainscreen;
 
 public class sql {
 
 	String url = "com.mysql.jdbc.Driver";
 	ResultSet rs = null;
 	private Connection con = null;
 	private static boolean connected = false;
 	private int FAILED = 0;
 	private int SUCCESS = 1;
 
 	/**
 	 * Verbindet zur Datenbank
 	 * 
 	 * @return Status, ob berbunden
 	 */
 	public boolean connect() {
 		connected = false;
 		Properties prop = new Properties();
 		try {
 			prop.load(new FileInputStream("config.properties"));
 			String dbHost = prop.getProperty("dbHost");
 			String dbPort = prop.getProperty("dbPort");
 			String database = prop.getProperty("database");
 			String dbUser = prop.getProperty("dbuser");
 			String dbPassword = prop.getProperty("dbpassword");
 
 			// connect to the server
 			Class.forName(url);
 			this.con = DriverManager.getConnection("jdbc:mysql://" + dbHost + ":" + dbPort + "/" + database + "?" + "user=" + dbUser + "&"
 					+ "password=" + dbPassword);
 			this.con.setAutoCommit(false);
 			// user table
 			Statement stmt = this.con.createStatement();
 
 			// stmt.executeUpdate("CREATE TABLE IF NOT EXISTS `module` (  " +
 			// "`modulname` varchar(255) NOT NULL,"
 			// + "`jahrgang` int(255) DEFAULT NULL,  " +
 			// "`Version` int(11) DEFAULT NULL,  " +
 			// "`Datum` date DEFAULT NULL,  "
 			// + "`akzeptiert` tinyint(1) DEFAULT '0',  " +
 			// "`inbearbeitung` tinyint(1) DEFAULT '0',  "
 			// + "`typid` int(11) NOT NULL,  " +
 			// "`user` varchar(255) DEFAULT NULL,  "
 			// +
 			// "UNIQUE KEY `name_jahrgang_Version_typid` (`modulname`,`jahrgang`,`Version`,`typid`))");
 			// this.con.commit();
 			//
 			// stmt.executeUpdate("CREATE TABLE IF NOT EXISTS `rights` (" +
 			// "`id` int(11) NOT NULL," + "`userchange` tinyint(1) NOT NULL,"
 			// + "`modcreate` tinyint(1) NOT NULL," +
 			// "`modacc` tinyint(1) NOT NULL," + "`manage` tinyint(1) NOT NULL,"
 			// + "PRIMARY KEY (`id`))");
 			// this.con.commit();
 			//
 			// stmt.executeUpdate("CREATE TABLE IF NOT EXISTS `studiengang` (" +
 			// "`id` int(10) NOT NULL AUTO_INCREMENT,"
 			// + "`name` varchar(255) NOT NULL," + "PRIMARY KEY (`id`)," +
 			// "UNIQUE KEY `Studiengang` (`name`))");
 			// this.con.commit();
 			//
 			// stmt.executeUpdate("CREATE TABLE IF NOT EXISTS `text` (" +
 			// "`name` varchar(255) NOT NULL," + "`version` int(11) NOT NULL,"
 			// + "`label` varchar(255) NOT NULL," +
 			// "`text` varchar(255) NOT NULL," +
 			// "`dezernat2` tinyint(1) NOT NULL DEFAULT '0')");
 			// this.con.commit();
 			//
 			// stmt.executeUpdate("CREATE TABLE IF NOT EXISTS `typ` (" +
 			// "`tid` int(10) NOT NULL AUTO_INCREMENT,"
 			// + "`tName` varchar(250) NOT NULL," +
 			// "`sid` int(10) NOT NULL DEFAULT '0'," +
 			// "`abschluss` varchar(250) NOT NULL,"
 			// + "PRIMARY KEY (`tid`)," +
 			// "UNIQUE KEY `UNIQUE KEY` (`sid`,`tName`,`abschluss`))");
 			// this.con.commit();
 			//
 			// stmt.executeUpdate("CREATE TABLE IF NOT EXISTS `user` (" +
 			// "`id` int(11) NOT NULL AUTO_INCREMENT,"
 			// + "`email` varchar(255) NOT NULL," +
 			// "`titel` varchar(255) DEFAULT NULL," +
 			// "`vorname` varchar(255) DEFAULT NULL,"
 			// + "`namen` varchar(255) DEFAULT NULL, " +
 			// "`password` varchar(255) NOT NULL," +
 			// "`frei` tinyint(1) DEFAULT '0',"
 			// + "PRIMARY KEY (`id`)," + "UNIQUE KEY `email` (`email`))");
 			// this.con.commit();
 			//
 			// stmt.executeUpdate("CREATE TABLE IF NOT EXISTS `user_relation` ("
 			// + "  `main_email` varchar(255) NOT NULL,"
 			// + "  `stellver_email` varchar(255) NOT NULL" + ");");
 			// this.con.commit();
 
 			stmt.executeUpdate("INSERT IGNORE INTO `user` (`id`, `email`, `titel`, `vorname`, `namen`, `password`,`frei`) VALUES"
 					+ "	(1, 'admin@mms.de', NULL, 'Admin', 'Admin', 'a384b6463fc216a5f8ecb6670f86456a',1);");
 			this.con.commit();
 			stmt.executeUpdate("INSERT IGNORE INTO `rights` (`id`, `userchange`, `modcreate`, `modacc`, `manage`) VALUES"
 					+ "	(1, 1, 1, 1, 1);");
 			this.con.commit();
 			stmt.executeUpdate("INSERT IGNORE INTO `user` (`id`, `email`, `titel`, `vorname`, `namen`, `password`,`frei`) VALUES"
 					+ "	(2, 'gast@gast.gast', NULL, 'NULL', 'NULL', 'd4061b1486fe2da19dd578e8d970f7eb',1);");
 			this.con.commit();
 			stmt.executeUpdate("INSERT IGNORE INTO `rights` (`id`, `userchange`, `modcreate`, `modacc`, `manage`) VALUES"
 					+ "	(2, 0, 0, 0, 0);");
 			this.con.commit();
 			stmt.close();
 			connected = true;
 
 		} catch (SQLException e) {
 			// TODO fehler fenster aufrufen
 			e.printStackTrace();
 			mainscreen.noConnection();
 			connected = false;
 
 		} catch (ClassNotFoundException e) {
 			// TODO fehler fenster aufrufen
 			e.printStackTrace();
 			connected = false;
 		} catch (FileNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			connected = false;
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			connected = false;
 		}
 
 		return connected;
 	}
 
 	/**
 	 * Lscht einen User
 	 * 
 	 * @param email
 	 *            e-mail des Users
 	 */
 	public void deluser(String email) {
 		if (connect() == true) {
 			Statement state = null;
 			ResultSet res = null;
 			int id = -1;
 			try {
 				state = this.con.createStatement();
 				res = state.executeQuery("SELECT id FROM user WHERE email='" + email + "';");
 				if (res.first())
 					id = res.getInt("id");
 				res.close();
 				state.close();
 			} catch (SQLException e) {
 				// TODO fehler fenster aufrufen
 				e.printStackTrace();
 			}
 			if (id != -1) {
 				try {
 					state = this.con.createStatement();
 					state.executeUpdate("DELETE FROM user WHERE id = " + id + ";");
 					state.executeUpdate("DELETE FROM rights WHERE id = " + id + ";");
 				} catch (SQLException e) {
 					// TODO fehler fenster aufrufen
 					System.out.print(e.getMessage());
 				} finally {
 					try {
 						this.con.commit();
 					} catch (Exception e) {
 						e.printStackTrace();
 					}
 					try {
 						state.close();
 					} catch (Exception e) {
 						e.printStackTrace();
 					}
 				}
 			} else {
 				System.out.print("Failed to write rights!");
 			}
 			disconnect();
 		}
 	}
 
 	/**
 	 * trennt Verbindung zurDatenbank
 	 */
 	public void disconnect() {
 		try {
 			this.con.commit();
 			this.con.setAutoCommit(true);
 			this.con.close();
 		} catch (SQLException e) {
 			// TODO fehler fenster aufrufen
 			System.out.print(e.getMessage());
 		}
 
 	}
 
 	/**
 	 * Gibt anzahl an Studiengngen aus
 	 * 
 	 * @return Anzahl von Studiengngen
 	 */
 	public int getAnzahlStudiengaenge() {
 		ResultSet res = null;
 		Statement state = null;
 		int cnt = 0;
 		if (connect() == true) {
 			try {
 				state = this.con.createStatement();
 				res = state.executeQuery("SELECT COUNT(id) AS cnt FROM studiengang;");
 				while (res.next()) {
 					cnt = res.getInt("cnt");
 				}
 				res.close();
 				state.close();
 			} catch (SQLException e) {
 				// TODO fehler fenster aufrufen
 				e.printStackTrace();
 			}
 			disconnect();
 		}
 		return cnt;
 
 	}
 
 	// /**
 	// * Gibt ein Modul aus
 	// *
 	// * @param name
 	// * Name des Moduls
 	// * @return Modul
 	// */
 	// public Modul getModul(String name) {
 	// ResultSet res = null;
 	// Statement state = null;
 	// int version = 0;
 	// int jahrgang = 0;
 	// Date datum = new Date();
 	// boolean akzeptiert = false;
 	// boolean inbearbeitung = false;
 	// ArrayList<Feld> felder = new ArrayList<Feld>();
 	// String user = "";
 	// if (connect() == true) {
 	// try {
 	// state = this.con.createStatement();
 	// String sql =
 	// "SELECT IFNULL(MAX(Version),0) as version FROM module WHERE modulname = '"
 	// + name + "';";
 	// res = state.executeQuery(sql);
 	// if (res.first()) {
 	// version = res.getInt("version");
 	// }
 	//
 	// if (version != 0) {
 	// sql = "SELECT * FROM module WHERE m.modulname = '"
 	// + name + "'AND version =" + version + ";";
 	// res = state.executeQuery(sql);
 	// if (res.first()) {
 	// datum = res.getDate("Datum");
 	// akzeptiert = res.getBoolean("akzeptiert");
 	// inbearbeitung = res.getBoolean("inbearbeitung");
 	// int tid = res.getInt("typid");
 	// String tname = res.getString("tname");
 	// String sname = res.getString("sname");
 	// int sid = res.getInt("sid");
 	// String abschluss = res.getString("abschluss");
 	// user = res.getString("user");
 	// zs.add(new Zuordnung(tid, tname, sname, sid, abschluss));
 	// }
 	// while (res.next()) {
 	// int tid = res.getInt("typid");
 	// String tname = res.getString("tname");
 	// String sname = res.getString("sname");
 	// int sid = res.getInt("sid");
 	// String abschluss = res.getString("abschluss");
 	// zs.add(new Zuordnung(tid, tname, sname, sid, abschluss));
 	// }
 	// res =
 	// state.executeQuery("SELECT label, text, dezernat2 FROM text WHERE name = '"
 	// + name + "' AND version = " + version
 	// + ";");
 	//
 	// while (res.next()) {
 	// felder.add(new Feld(res.getString("label"), res.getString("text"),
 	// res.getBoolean("dezernat2")));
 	// }
 	// }
 	// res.close();
 	// state.close();
 	// } catch (SQLException e) {
 	//
 	// }
 	// disconnect();
 	//
 	// }
 	//
 	// if (version != 0) {
 	// return new Modul(name, felder, version, datum, akzeptiert, inbearbeitung,
 	// user);
 	// } else
 	// return new Modul();
 	//
 	// }
 
 	/**
 	 * Gibt eine Liste von Modulhandbcher aus
 	 * 
 	 * @param studiengang
 	 *            Name des Studienganges
 	 * @return Liste von Modulhandbchern
 	 */
 	public ArrayList<Modulhandbuch> getModulhandbuch(String studiengang) {
 		ResultSet res = null;
 		Statement state = null;
 		ArrayList<Modulhandbuch> modbuch = new ArrayList<Modulhandbuch>();
 		if (connect() == true) {
 			try {
 				state = this.con.createStatement();
 				res = state
 						.executeQuery("SELECT mhb.* FROM modulhandbuch as mhb join pordnung as po on mhb.poid = po.id join studiengang as s on po.sid = s.id where s.name = '"
 								+ studiengang + "' ;");
 
 				while (res.next()) {
 					int pojahr = res.getInt("pojahr");
 					int id = res.getInt("id");
 					String prosa = res.getString("prosa");
 					String semester = res.getString("semester");
 					String jahr = res.getString("jahr");
 					modbuch.add(new Modulhandbuch(id, semester + " " + jahr, prosa, pojahr));
 				}
 
 				res.close();
 				state.close();
 			} catch (SQLException e) {
 				// TODO fehler fenster aufrufen
 				e.printStackTrace();
 			}
 			disconnect();
 		}
 		return modbuch;
 
 	}
 
 	/**
 	 * Gibt neuste Versionsnummer eines Moduls aus
 	 * 
 	 * @param name
 	 *            name des Moduls
 	 * @return Versionsnummer
 	 */
 	public int getModulVersion(String name) {
 		ResultSet res = null;
 		Statement state = null;
 		int version = 0;
 		if (connect() == true) {
 			try {
 				state = this.con.createStatement();
 				String q = "SELECT IFNULL(MAX(Version),0) AS Version FROM module WHERE modulname = '" + name + "';";
 				res = state.executeQuery(q);
 				if (res.first()) {
 					version = res.getInt("Version");
 				}
 				res.close();
 				state.close();
 			} catch (SQLException e) {
 				e.printStackTrace();
 			}
 			disconnect();
 		}
 		return version;
 	}
 
 	/**
 	 * Gibt eine Liste von Studiengngen aus
 	 * 
 	 * @return Liste von Studiengngen
 	 */
 	public ArrayList<Studiengang> getStudiengaenge() {
 		ResultSet res = null;
 		Statement state = null;
 		ArrayList<Studiengang> sgs = new ArrayList<Studiengang>();
 		if (connect() == true) {
 			try {
 				state = this.con.createStatement();
 				res = state.executeQuery("SELECT * FROM studiengang;");
 				// verarbeitung der resultset
 				while (res.next()) {
 					int id = res.getInt("id");
 					String name = res.getString("name");
 					String abschluss = res.getString("abschluss");
 					sgs.add(new Studiengang(id, name, abschluss));
 				}
 				res.close();
 				state.close();
 			} catch (SQLException e) {
 				// TODO fehler fenster aufrufen
 				e.printStackTrace();
 			}
 			disconnect();
 		}
 		return sgs;
 
 	}
 
 	/**
 	 * Fragt einen User ab
 	 * 
 	 * @param email
 	 *            e-Mail des Users
 	 * @param pass
 	 *            Passwort des Users
 	 * @return User
 	 */
 	public User getUser(String email, String pass) {
 		User zws = null;
 		ResultSet res = null;
 		Statement state = null;
 		int id = -1;
 		if (connect() == true) {
 			try {
 				state = this.con.createStatement();
 				res = state
 						.executeQuery("SELECT * FROM user AS u JOIN rights AS r ON u.id=r.id WHERE email='"
 								+ email + "' and password='" + pass + "' AND frei=1;");
 				if (res.first()) {
 					zws = new User(res.getInt("id"), res.getString("vorname"), res.getString("namen"), res.getString("titel"), res.getString("email"),
 							res.getString("password"), res.getBoolean("userchange"), res.getBoolean("modcreate"), res.getBoolean("modacc"),
 							res.getBoolean("manage"), res.getBoolean("redaktion"), res.getBoolean("frei"));
 				}
 				res.close();
 				state.close();
 			} catch (SQLException e) {
 				// TODO fehler fenster aufrufen
 				e.printStackTrace();
 			}
 			disconnect();
 		}
 		return zws;
 
 	}
 
 	/**
 	 * Gibt an, ob Verbindung zur Datenbank besteht
 	 * 
 	 * @return Status der Verbindung
 	 */
 	public boolean isConnected() {
 		return connected;
 	}
 
 	
 	//TODO erneuter check ob Module richtig
 	/**
 	 * Trgt ein Modul ein
 	 * 
 	 * @param neu
 	 *            Das Modul
 	 * @return Erfolgsstatus
 	 */
 	public int setModul(Modul neu, String fach, int buchid ) {
 		int ok = FAILED;
 		PreparedStatement state = null;
 		if (connect() == true) {
 			String name = neu.getName();
 			int version = neu.getVersion();
 			ArrayList<Feld> felder = neu.getFelder();
 			try {
 				state = con.prepareStatement("INSERT INTO module (modulname, version, datum, kommentar, status) VALUES(?,?,?,?,?)");
 				state.setString(1, name);
 				state.setInt(2, version);
 				state.setTimestamp(3, dateConverterUtil2SQL(neu.getDatum()));
 				state.setString(4, neu.getKommentar());
 				state.setInt(5, neu.getStatus());
 				state.executeUpdate();
 
 				state = con.prepareStatement("INSERT INTO text (mid, version, label, text, dezernat2) VALUES((select modID from module where modulname =?),?,?,?,?)");
 				for (int i = 0; i < felder.size(); i++) {
 					Feld f = felder.get(i);
 					state.setString(1, name);
 					state.setInt(2, version);
 					state.setString(3, f.getLabel());
 					state.setString(4, f.getValue());
 					state.setBoolean(5, f.isDezernat());
 					state.executeUpdate();
 				}
 				state = con.prepareStatement("INSERT INTO fach (fachid, buchid, modid) VALUES ((SELECT id FROM fachname WHERE name=?) ,? ,(SELECT modID FROM module WHERE modulname =? and version =?)) ;");
 				state.setString(1, fach);
 				state.setInt(2, buchid);
 				state.setString(3, name);
 				state.setInt(4, version);
 				state.close();
 				ok = SUCCESS;
 			} catch (SQLException e) {
 				// TODO fehler fenster aufrufen
 				e.printStackTrace();
 			}
 			disconnect();
 		}
 		return ok;
 
 	}
 	
 	public int updateModul(Modul neu, String fach, int buchid ) {
 		int ok = FAILED;
 		PreparedStatement state = null;
 		if (connect() == true) {
 			String name = neu.getName();
 			int version = neu.getVersion();
 			ArrayList<Feld> felder = neu.getFelder();
 			try {
 				state = con.prepareStatement("INSERT INTO module (modulname, version, datum, kommentar, status) VALUES(?,?,?,?,?,?)");
 				state.setString(1, name);
 				state.setInt(2, version);
 				state.setTimestamp(3, dateConverterUtil2SQL(neu.getDatum()));
 				state.setString(4, neu.getKommentar());
 				state.setInt(5, neu.getStatus());
 				state.executeUpdate();
 
 				state = con.prepareStatement("INSERT INTO text (mid, version, label, text, dezernat2) VALUES((select modID from module where modulname =?),?,?,?,?)");
 				for (int i = 0; i < felder.size(); i++) {
 					Feld f = felder.get(i);
 					state.setString(1, name);
 					state.setInt(2, version);
 					state.setString(3, f.getLabel());
 					state.setString(4, f.getValue());
 					state.setBoolean(5, f.isDezernat());
 					state.executeUpdate();
 				}
 				state = con.prepareStatement("UPDATE fach SET modID=(SELECT modID FROM module WHERE modulname =? and version =?) WHERE modID = (SELECT modID FROM module where modulname =? and version =?) AND buchID =? AND fachID = (SELECT fachID FROM Fachname WHERE name =?);");
 				state.setString(1, name);
 				state.setInt(2, version);
 				state.setString(3, name);
 				state.setInt(4, (version-1));
 				state.setInt(5, buchid);
 				state.setString(6, fach);
 				state.close();
 				ok = SUCCESS;
 			} catch (SQLException e) {
 				// TODO fehler fenster aufrufen
 				e.printStackTrace();
 			}
 			disconnect();
 		}
 		return ok;
 
 	}
 
 	/**
 	 * Neuen Studiengang anlegen
 	 * 
 	 * @param name Name des Studienganges
 	 * @param abschluss Abschluss des Studienganges
 	 * @return efolgreich oder nicht
 	 */
 	public int setStudiengang(String name, String abschluss) {
 		Statement state = null;
 		int status = FAILED;
 		if (connect() == true) {
 			try {
 				state = this.con.createStatement();
 				state.executeUpdate("INSERT INTO studiengang (name, abschluss) VALUES ('" + name + "', '" + abschluss + "');");
 				status = SUCCESS;
 			} catch (SQLException e) {
 				// TODO fehler fenster aufrufen
 				e.printStackTrace();
 			}
 			disconnect();
 		}
 		return status;
 
 	}
 
 	/**
 	 * Fragt alle User ab
 	 * @param b 
 	 * 
 	 * @return Liste von Usern
 	 */
 	public ArrayList<User> userload(boolean b) {
 		User zws = null;
 		ResultSet res = null;
 		Statement state = null;
 //		int i = 0;
 //		int j = 0;
 		ArrayList<User> list = new ArrayList<User>();
 		if (connect() == true) {
 			try {
 				state = con.createStatement();
 				res = state
 						.executeQuery("SELECT u.*,userchange,modcreate,modacc,manage,redaktion FROM user AS u JOIN rights AS r ON u.id=r.id WHERE email!='gast@gast.gast' AND frei="+b+";");
 				while (res.next()) {
 					zws = new User(res.getInt("id"),res.getString("vorname"), res.getString("namen"), res.getString("titel"), res.getString("email"),
 							res.getString("password"), res.getBoolean("userchange"), res.getBoolean("modcreate"), res.getBoolean("modacc"),
 							res.getBoolean("manage"), res.getBoolean("redaktion"), res.getBoolean("frei"));
 					list.add(zws);
 				}
 				res.close();
 				state.close();
 			} catch (SQLException e) {
 				// TODO fehler fenster aufrufen
 				e.printStackTrace();
 			}
 			disconnect();
 		}
 		return list;
 
 	}
 
 	/**
 	 * Speichert einen User
 	 * 
 	 * @param user
 	 *            Der User
 	 * @return Erfolgsstatus
 	 */
 	public int usersave(User user) {
 		int ok = FAILED;
 		if (connect() == true) {
 			PreparedStatement state = null;
 			ResultSet res = null;
 			int id = -1;
 			try {
 				state = con.prepareStatement("INSERT INTO user (email,vorname,namen,password, titel, frei) VALUES (?,?,?,?,?,?)");
 				state.setString(1, user.geteMail());
 				state.setString(2, user.getVorname());
 				state.setString(3, user.getNachname());
 				state.setString(4, user.getPassword());
 				state.setString(5, user.getTitel());
 				state.setBoolean(6, user.isFreigeschaltet());
 				state.executeUpdate();
 				con.commit();
 				state.close();
 
 				state = con.prepareStatement("SELECT id FROM user WHERE email=?");
 				state.setString(1, user.geteMail());
 				res = state.executeQuery();
 				if (res.first())
 					id = res.getInt("id");
 				res.close();
 				state.close();
 			} catch (SQLException e) {
 				// e.printStackTrace();
 				ok = FAILED;
 			}
 			if (id != -1) {
 				try {
 					state = con
 							.prepareStatement("INSERT INTO rights (id,userchange,modcreate,modacc,manage, redaktion) VALUES (?,?,?,?,?,?)");
 					state.setInt(1, id);
 					state.setBoolean(2, user.getManageUsers());
 					state.setBoolean(3, user.getCreateModule());
 					state.setBoolean(4, user.getAcceptModule());
 					state.setBoolean(5, user.getManageSystem());
 					state.setBoolean(6, user.getRedaktion());
 					state.executeUpdate();
 					con.commit();
 					state.close();
 					ok = SUCCESS;
 
 				} catch (SQLException e) {
 					e.printStackTrace();
 				}
 			} else {
 				ok = FAILED;
 			}
 			disconnect();
 		}
 		return ok;
 	}
 
 	/**
 	 * Update eines Users
 	 * 
 	 * @param user
 	 *            Der User
 	 * @param email
 	 *            Die alte e-Mail des Users
 	 * @return Erfolgsstatus
 	 */
 	public int userupdate(User user, String email) {
 		int ok = FAILED;
 		if (connect() == true) {
 			PreparedStatement state = null;
 			ResultSet res = null;
 			int id = -1;
 			boolean userexists = false;
 			try {
 				if (!user.geteMail().equals(email)) {
 					state = con.prepareStatement("SELECT IFNULL(id,0) AS id FROM user WHERE email=?;");
 					state.setString(1, user.geteMail());
 					res = state.executeQuery();
 					if (res.first()) {
 						if (res.getInt("id") != 0) {
 							userexists = true;
 							ok = FAILED;
 						}
 					}
 					res.close();
 					state.close();
 				}
 				if (!userexists) {
 					state = con.prepareStatement("SELECT IFNULL(id,0) AS id FROM user WHERE email=?;");
 					state.setString(1, email);
 					res = state.executeQuery();
 					if (res.first()) {
 						id = res.getInt("id");
 					}
 					res.close();
 					state.close();
 					if (id != 0) {
 						if ((user.getPassword() != null) && !user.getPassword().equals("null")) {
 							state = con
 									.prepareStatement("UPDATE user SET titel = ?, vorname = ?, namen = ?, password = ?, email = ?, frei = ? WHERE id = ? ;");
 							state.setString(1, user.getTitel());
 							state.setString(2, user.getVorname());
 							state.setString(3, user.getNachname());
 							state.setString(4, user.getPassword());
 							state.setString(5, user.geteMail());
 							state.setBoolean(6, user.isFreigeschaltet());
 							state.setInt(7, id);
 							state.executeUpdate();
 							state.close();
 						} else {
 							state = con.prepareStatement("UPDATE user SET titel = ?, vorname = ?, namen = ?, email = ? WHERE id = ? ;");
 							state.setString(1, user.getTitel());
 							state.setString(2, user.getVorname());
 							state.setString(3, user.getNachname());
 							state.setString(4, user.geteMail());
 							state.setInt(5, id);
 							state.executeUpdate();
 							state.close();
 						}
 
 						state = con
 								.prepareStatement("UPDATE rights SET userchange = ?, modcreate =?, modacc =?, manage =?, redaktion =? WHERE id = ?;");
 						state.setBoolean(1, user.getManageUsers());
 						state.setBoolean(2, user.getCreateModule());
 						state.setBoolean(3, user.getAcceptModule());
 						state.setBoolean(4, user.getManageSystem());
 						state.setBoolean(5, user.getRedaktion());
 						state.setInt(6, id);
 						state.executeUpdate();
 						con.commit();
 						state.close();
 						ok = SUCCESS;
 
 					} else {
 						ok = FAILED;
 					}
 				}
 			} catch (SQLException e) {
 				e.printStackTrace();
 			}
 			disconnect();
 		}
 		return ok;
 	}
 
 	/**
 	 * Liefert die ID eines Studienganges
 	 * 
 	 * @param name
 	 *            Name des Studienganges
 	 * @return ID des Studienganges
 	 */
 	public int getStudiengangID(String name, String abschluss) {
 		ResultSet res = null;
 		Statement state = null;
 		int id = 0;
 		if (connect() == true) {
 			try {
 				state = this.con.createStatement();
 				res = state.executeQuery("SELECT id FROM studiengang WHERE name ='" + name + "'and abschluss = '" + abschluss + "';");
 				// verarbeitung der resultset
 				if (res.next()) {
 					id = res.getInt("id");
 				}
 				res.close();
 				state.close();
 			} catch (SQLException e) {
 				// TODO fehler fenster aufrufen
 				e.printStackTrace();
 			}
 			disconnect();
 		}
 		return id;
 	}
 
 	// /**
 	// * Liefert akzeptierte/nicht akzeptierte Module
 	// *
 	// * @param b
 	// * true wenn akzeptiert, false wenn nicht akzeptierte Module
 	// * gewnscht sind
 	// * @return Liste von Modulen
 	// */
 	// public ArrayList<Modul> getModule(boolean b) {
 	// ArrayList<Modul> module = new ArrayList<Modul>();
 	// ResultSet res = null;
 	// Statement state = null;
 	// Statement state2 = null;
 	// boolean ack = false;
 	// if (connect() == true) {
 	// try {
 	// state = this.con.createStatement();
 	// state2 = this.con.createStatement();
 	// res =
 	// state2.executeQuery("SELECT DISTINCT modulname FROM module ORDER BY modulname ASC;");
 	//
 	// while (res.next()) {
 	// String name = res.getString("modulname");
 	// String q =
 	// "SELECT IFNULL(MAX(Version),0) AS Version FROM module WHERE modulname = '"
 	// + name + "';";
 	// ResultSet res2 = state.executeQuery(q);
 	// int version = 0;
 	// if (res2.first()) {
 	// version = res2.getInt("Version");
 	// }
 	//
 	// ArrayList<Studiengang> sgs = new ArrayList<Studiengang>();
 	// String sql =
 	// "SELECT *,m.modulname AS mname, s.name AS sname FROM module AS m JOIN typ AS t ON m.typid=t.tid JOIN studiengang AS s ON t.sid=s.id WHERE m.modulname = '"
 	// + name + "'AND version =" + version + ";";
 	// res2 = state.executeQuery(sql);
 	// int jahrgang = 0;
 	// ArrayList<Zuordnung> zs = new ArrayList<Zuordnung>();
 	// String user = "";
 	// Date datum = null;
 	// boolean inedit = false;
 	// if (res2.first()) {
 	// user = res2.getString("user");
 	// jahrgang = res2.getInt("jahrgang");
 	// datum = res2.getDate("Datum");
 	// ack = res2.getBoolean("akzeptiert");
 	// inedit = res2.getBoolean("inbearbeitung");
 	// int tid = res2.getInt("typid");
 	// String tname = res2.getString("tname");
 	// String sname = res2.getString("sname");
 	// int sid = res2.getInt("sid");
 	// String abschluss = res2.getString("abschluss");
 	// zs.add(new Zuordnung(tid, tname, sname, sid, abschluss));
 	// }
 	// if (b == ack) {
 	// while (res2.next()) {
 	// int tid = res2.getInt("typid");
 	// String tname = res2.getString("tname");
 	// String sname = res2.getString("sname");
 	// int sid = res2.getInt("sid");
 	// String abschluss = res2.getString("abschluss");
 	// zs.add(new Zuordnung(tid, tname, sname, sid, abschluss));
 	// }
 	//
 	// ArrayList<Feld> felder = new ArrayList<Feld>();
 	//
 	// res2 =
 	// state.executeQuery("SELECT label, text, dezernat2 FROM text WHERE name = '"
 	// + name + "' AND version = "
 	// + version + ";");
 	//
 	// while (res2.next()) {
 	// felder.add(new Feld(res2.getString("label"), res2.getString("text"),
 	// res2.getBoolean("dezernat2")));
 	// }
 	// res2.close();
 	//
 	// module.add(new Modul(name, zs, jahrgang, felder, version, datum, ack,
 	// inedit, user));
 	// }
 	//
 	// }
 	// res.close();
 	// state.close();
 	// state2.close();
 	// } catch (SQLException e) {
 	// // TODO fehler fenster aufrufen
 	// e.printStackTrace();
 	// }
 	// disconnect();
 	// }
 	// return module;
 	// }
 
 	/**
 	 * Prft, ob ein User vorhanden ist
 	 * 
 	 * @param email
 	 *            e-Mail des Users
 	 * @return Status, ob vorhanden oder nicht
 	 */
 	public int getUser(String email) {
 		ResultSet res = null;
 		Statement state = null;
 		int status = FAILED;
 		if (connect() == true) {
 			try {
 				state = this.con.createStatement();
 				res = state.executeQuery("SELECT IFNULL(id,0) AS id FROM user WHERE email='" + email + "';");
 				if (res.first()) {
 					if (res.getInt("id") == 0)
 						status = SUCCESS;
 				}
 				res.close();
 				state.close();
 			} catch (SQLException e) {
 				// TODO fehler fenster aufrufen
 				e.printStackTrace();
 			}
 			disconnect();
 		}
 		return status;
 	}
 	
 	public int getUserID(String email){
 		ResultSet res = null;
 		Statement state = null;
 		int id=0;
 		if (connect() == true) {
 			try {
 				state = this.con.createStatement();
 				res = state.executeQuery("SELECT IFNULL(id,0) AS id FROM user WHERE email='" + email + "';");
 				if (res.first()) {
 					id=res.getInt("id");
 				}
 				res.close();
 				state.close();
 			} catch (SQLException e) {
 				// TODO fehler fenster aufrufen
 				e.printStackTrace();
 			}
 			disconnect();
 		}
 		return id;
 	}
 
 	/**
 	 * Reicht eine Modulverwalter Liste ein fuer ein bestimmtes Modul
 	 * 
 	 * @param userlis
 	 *            Liste von Verwalter
 	 *           
 	 * @param modul         
 	 *            ein bestimmtes Modul
 	 * @return Erfolgsstatus
 	 */
 	public int setStellvertreter(ArrayList<User> userlis, String modul) {
 		PreparedStatement state = null;
 		int status = FAILED;
 		if (connect() == true) {
 			try {
 
 				state = con
 						.prepareStatement("DELETE FROM mod_user_relation WHERE mod_user_relation.modID = (SELECT modID from module where modulname =?)");
 				state.setString(1, modul);
 				state.executeUpdate();
 				for (int i = 0; i < userlis.size(); i++) {
 					state = con
 							.prepareStatement("INSERT INTO mod_user_relation (userID, modID) VALUES((SELECT modID from module where modulname =?),(SELECT id from user where email =?))");
 					state.setString(1, modul);
 					state.setString(2, userlis.get(i).geteMail());
 					state.executeUpdate();
 				}
 				status = SUCCESS;
 			} catch (SQLException e) {
 				// TODO fehler fenster aufrufen
 				e.printStackTrace();
 			}
 			disconnect();
 		}
 		return status;
 	}
 	
 	public int setoneStellvertreter(User user, String modul) {
 		PreparedStatement state = null;
 		int status = FAILED;
 		if (connect() == true) {
 			try {
 				state = con
 					.prepareStatement("INSERT INTO mod_user_relation (userID, modID) VALUES((SELECT modID from module where modulname =?),(SELECT id from user where email =?))");
 				state.setString(1, modul);
 				state.setString(2, user.geteMail());
 				state.executeUpdate();
 				
 				status = SUCCESS;
 			} catch (SQLException e) {
 				// TODO fehler fenster aufrufen
 				e.printStackTrace();
 			}
 			disconnect();
 		}
 		return status;
 	}
 
 
 	/**
 	 * Liefert eine Liste von Modulverwalter fuer ein bestimmtes Modul aus
 	 * 
 	 * @param modul
 	 *            modul name von dem man die Verwalter haben mchte
 	 * @return Liste mit Usern
 	 */
 	public ArrayList<User> getStellv(String modul) {
 		ResultSet res = null;
 		Statement state = null;
 		ArrayList<User> stellv = new ArrayList<User>();
 		if (connect() == true) {
 			try {
 				state = this.con.createStatement();
 				res = state
 						.executeQuery("SELECT * FROM mod_user_relation as rel JOIN module AS m ON rel.modname = m.modulname JOIN user AS u ON rel.userID = u.ID JOIN rights as r on u.id = r.id WHERE m.modulname='"
 								+ modul + "';");
 				while (res.next()) {
 					stellv.add(new User(res.getInt("id"),res.getString("vorname"), res.getString("namen"), res.getString("titel"), res.getString("email"),
 							res.getString("password"), res.getBoolean("userchange"), res.getBoolean("modcreate"), res.getBoolean("modacc"),
 							res.getBoolean("manage"), res.getBoolean("redaktion"), res.getBoolean("frei")));
 				}
 				res.close();
 				state.close();
 			} catch (SQLException e) {
 				// TODO fehler fenster aufrufen
 				e.printStackTrace();
 			}
 			disconnect();
 		}
 		return stellv;
 	}
 
 	/**
 	 * Gibt ein Modul aus
 	 * 
 	 * @param studiengang
 	 *            Studiengang des Moduls
 	 * @param modultyp
 	 *            Zuordnung des Moduls
 	 * @param modulhandbuch
 	 *            Jahrgang des Moduls
 	 * @return
 	 */
 	// public ArrayList<Modul> getselectedModul(String studiengang, String
 	// modultyp, String modulhandbuch) {
 	// ArrayList<Modul> selmodul = new ArrayList<Modul>();
 	// ResultSet res = null;
 	// Statement state = null;
 	// ArrayList<String> zwsstring = new ArrayList<String>();
 	// if (connect() == true) {
 	// try {
 	// state = this.con.createStatement();
 	// res = state
 	// .executeQuery("Select modu.modulname as modname from module as modu join typ on modu.typid = typ.tid join studiengang as stud on typ.sid = stud.id "
 	// + "where modu.akzeptiert = 1 and stud.name = '"
 	// + studiengang
 	// + "' and typ.tName = '"
 	// + modultyp
 	// + "' and modu.jahrgang = '" + modulhandbuch + "';");
 	// while (res.next()) {
 	// zwsstring.add(res.getString("modname"));
 	// }
 	// res.close();
 	// state.close();
 	//
 	// } catch (SQLException e) {
 	// // TODO fehler fenster aufrufen
 	// e.printStackTrace();
 	// }
 	// disconnect();
 	// for (int i = 0; i < zwsstring.size(); i++) {
 	// selmodul.add(getModul(zwsstring.get(i)));
 	//
 	// }
 	// }
 	// return selmodul;
 	// }
 
 	/**
 	 * Gibt eine User Modul Relation aus aus
 	 * 
 	 * @param email
 	 *            e-Mail des Benutzers
 	 * @return Liste mit Benutzernamen
 	 */
 	public ArrayList<Modul> getUserModulRelation(String email) {
 		ArrayList<Modul> module = new ArrayList<Modul>();
 		ArrayList<Feld> felder = new ArrayList<Feld>();
 		ResultSet res = null;
 		PreparedStatement state = null;
 
 		if (connect() == true) {
 			try {
 				state = this.con
 						.prepareStatement("SELECT * FROM module AS m JOIN mod_user_relation AS rel ON m.modulname = rel.modname JOIN user ON rel.userID = user.id WHERE user.email =? ;");
 				state.setString(1, email);
 				res = state.executeQuery();
 				if (res.first()) {
 					String name = res.getString("name");
 					int version = res.getInt("version");
 					Date datum = res.getDate("Datum");
 					Boolean inbearbeitung = res.getBoolean("inbearbeitung");
 					int status = res.getInt("status");
 					String kommentar = res.getString("kommentar");
 					module.add(new Modul(name, version, datum, status, inbearbeitung, kommentar));
 				}
 				while (res.next()) {
 					String name = res.getString("name");
 					int version = res.getInt("version");
 					Date datum = res.getDate("Datum");
 					Boolean inbearbeitung = res.getBoolean("inbearbeitung");
 					int status = res.getInt("status");
 					String kommentar = res.getString("kommentar");
 					module.add(new Modul(name, version, datum, status, inbearbeitung, kommentar));
 				}
 				for (int i = 0; i < module.size(); i++) {
 					res = state.executeQuery("SELECT txt.label, txt.text, txt.dezernat2 FROM text as txt join module as m on m.modid = txt.mid WHERE m.modulname = '" + module.get(i).getName()
 							+ "' AND txt.version = " + module.get(i).getVersion() + ";");
 
 					while (res.next()) {
 						felder.add(new Feld(res.getString("label"), res.getString("text"), res.getBoolean("dezernat2")));
 					}
 					module.get(i).setFelder(felder);
 					felder = new ArrayList<Feld>();
 				}
 			} catch (SQLException e) {
 				// TODO fehler fenster aufrufen
 				e.printStackTrace();
 			}
 			disconnect();
 		}
 		return module;
 	}
 
 	/**
 	 * Gibt ein Modul aus
 	 * 
 	 * @param name
 	 *            Name des Moduls
 	 * @param version
 	 *            Version des Moduls
 	 * @return Das Modul
 	 */
 	public Modul getModul(String name, int version) {
 		ResultSet res = null;
 		Statement state = null;
 		Date datum = new Date();
 		boolean inbearbeitung = false;
 		ArrayList<Feld> felder = new ArrayList<Feld>();
 		int status = 0;
 		String kommentar = "";
 		if (connect() == true) {
 			try {
 				state = this.con.createStatement();
 				if (version != 0) {
 					String sql = "SELECT *,m.modulname AS mname FROM module AS m WHERE m.modulname = '" + name + "'AND version =" + version
 							+ ";";
 					res = state.executeQuery(sql);
 					if (res.first()) {
 						datum = res.getDate("Datum");
 						inbearbeitung = res.getBoolean("inbearbeitung");
 						status = res.getInt("status");
 						kommentar = res.getString("kommentar");
 					}
 					while (res.next()) {
 						datum = res.getDate("Datum");
 						inbearbeitung = res.getBoolean("inbearbeitung");
 						status = res.getInt("status");
 						kommentar = res.getString("kommentar");
 					}
 					res = state.executeQuery("SELECT txt.label, txt.text, txt.dezernat2 FROM text as txt join module as m on m.modid = txt.mid WHERE m.modulname = '" + name + "' AND txt.version = " + version
 							+ ";");
 
 					while (res.next()) {
 						felder.add(new Feld(res.getString("label"), res.getString("text"), res.getBoolean("dezernat2")));
 					}
 				}
 				res.close();
 				state.close();
 			} catch (SQLException e) {
 
 			}
 			disconnect();
 
 		}
 
 		if (version != 0) {
 			return new Modul(name, felder, version, datum, status, inbearbeitung, kommentar);
 		} else
 			return new Modul();
 	}
 
 	public ArrayList<Modul> getModul(String name) {
 		ResultSet res = null;
 		Statement state = null;
 		Date datum = new Date();
 		boolean inbearbeitung = false;
 		ArrayList<Modul> module = new ArrayList<Modul>();
 		ArrayList<Feld> felder = new ArrayList<Feld>();
 		int status = 0;
 		int version = 0;
 		String kommentar = "";
 		if (connect() == true) {
 			try {
 				state = this.con.createStatement();
 				String sql = "SELECT *,m.modulname AS mname FROM module AS m WHERE m.modulname = '" + name + "';";
 				res = state.executeQuery(sql);
 				while (res.next()) {
 					version = res.getInt("version");
 					datum = res.getDate("Datum");
 					inbearbeitung = res.getBoolean("inbearbeitung");
 					status = res.getInt("status");
 					kommentar = res.getString("kommentar");
 					module.add(new Modul(name, version, datum, status, inbearbeitung, kommentar));
 				}
 
 				for (int i = 0; i < module.size(); i++) {
 					res = state.executeQuery("SELECT txt.label, txt.text, txt.dezernat2 FROM text as txt join module as m on m.modid = txt.mid WHERE m.modulname = '" + module.get(i).getName()
 							+ "' AND txt.version = " + module.get(i).getVersion() + ";");
 
 					while (res.next()) {
 						felder.add(new Feld(res.getString("label"), res.getString("text"), res.getBoolean("dezernat2")));
 					}
 					module.get(i).setFelder(felder);
 					felder = new ArrayList<Feld>();
 				}
 				res.close();
 				state.close();
 			} catch (SQLException e) {
 
 			}
 			disconnect();
 
 		}
 		return module;
 	}
 
 	/**
 	 * akzeptiert ein Modulhandbuch
 	 * 
 	 * @param id des Modulhandbuchs
 	 * @return Erfolgsstatus
 	 */
 	public int acceptModulHandBuch(int id) {
 		int ok = FAILED;
 		PreparedStatement state = null;
 		if (connect() == true) {
 			try {
 				state = con.prepareStatement("UPDATE modulhandbuch SET akzeptiert=1 WHERE id=?");
 				state.setInt(1, id);
 				state.executeUpdate();
 				state.close();
 				ok = SUCCESS;
 			} catch (SQLException e) {
 				// TODO fehler fenster aufrufen
 				e.printStackTrace();
 			}
 			disconnect();
 		}
 		return ok;
 	}
 
 	/**
 	 * Gibt aus, ob ein Modul in Bearbeitung ist
 	 * 
 	 * @param name  Name des Moduls
 	 * @return status
 	 */
 	public boolean getModulInEdit(String name) {
 		ResultSet res = null;
 		Statement state = null;
 		int version = 0;
 		boolean inbearbeitung = false;
 		if (connect() == true) {
 			try {
 				state = this.con.createStatement();
 				String sql = "SELECT IFNULL(MAX(Version),0) as version FROM module WHERE Modulname = '" + name + "';";
 				res = state.executeQuery(sql);
 				if (res.first()) {
 					version = res.getInt("version");
 				}
 				if (version != 0) {
 					sql = "SELECT inbearbeitung FROM module WHERE Modulname ='" + name + "'AND version =" + version + ";";
 					res = state.executeQuery(sql);
 					if (res.first()) {
 						inbearbeitung = res.getBoolean("inbearbeitung");
 					}
 				}
 				res.close();
 				state.close();
 			} catch (SQLException e) {
 				inbearbeitung = false;
 			}
 			disconnect();
 		}
 		return inbearbeitung;
 	}
 
 	/**
 	 * Setzt ein Modul als in Bearbeitung
 	 * 
 	 * @param m Das Modul
 	 * @return Erfolgsstatus
 	 */
 	public int setInEdit(Modul m) {
 		int ok = FAILED;
 		PreparedStatement state = null;
 		if (connect() == true) {
 			try {
 				state = con.prepareStatement("UPDATE module SET inbearbeitung=? WHERE modulname=? AND version=?");
 				state.setBoolean(1, m.isInbearbeitung());
 				state.setString(2, m.getName());
 				state.setInt(3, m.getVersion());
 				state.executeUpdate();
 				state.close();
 				ok = SUCCESS;
 			} catch (SQLException e) {
 				// TODO fehler fenster aufrufen
 				e.printStackTrace();
 			}
 			disconnect();
 		}
 		return ok;
 	}
 
 	
 	/**
 	 * gibt eine Liste aller freigegebenen Module aus von Studiengang -> PO -> Modbuch -> fach -> Modul
 	 * @return Liste von Studiengaengen
 	 */
 	public ArrayList<Studiengang> getAllActiveData(Boolean b) {
 		ResultSet res = null;
 		Statement state = null;
 		ArrayList<Studiengang> alldata = new ArrayList<Studiengang>();
 		ArrayList<Modulhandbuch> mhb = new ArrayList<Modulhandbuch>();
 		ArrayList<Fach> fach = new ArrayList<Fach>();
 		ArrayList<Modul> modul = new ArrayList<Modul>();
 		ArrayList<Feld> felder = new ArrayList<Feld>();
 		int akzeptiert = 0;
 		if (connect() == true) {
 			try {
 				state = this.con.createStatement();
 				//get Studiengang
 				String sql = "SELECT * FROM studiengang;";
 				res = state.executeQuery(sql);
 				while (res.next()) {
 					int sid = res.getInt("id");
 					String abschluss = res.getString("abschluss");
 					String sname = res.getString("name");
 					alldata.add(new Studiengang(sid, sname, abschluss));
 				}
 				if(b){
 					akzeptiert = 1;
 				}
 				for (int i = 0; i < alldata.size(); i++) {
 					//get modbuch mit PO
 					sql = "SELECT po.jahr as pojahr, mhb.* "
 							+ "FROM pordnung as po JOIN studiengang as s on s.id = po.sID join modulhandbuch as mhb on po.id = mhb.poID "
 							+ "WHERE s.name = '" + alldata.get(i).getName() + "' and s.abschluss = '" + alldata.get(i).getAbschluss()
 							+ "' and mhb.akzeptiert = "+akzeptiert+";";
 					res = state.executeQuery(sql);
 					while (res.next()) {
 						int pojahr = res.getInt("pojahr");
 						int id = res.getInt("id");
 						String prosa = res.getString("prosa");
 						String semester = res.getString("semester");
 						String jahr = res.getString("jahr");
 						mhb.add(new Modulhandbuch(id, semester + " " + jahr, prosa, pojahr));
 					}
 					for (int j = 0; j < mhb.size(); j++) {
 						//get fach
 						sql = "SELECT DISTINCT fachname.name FROM fach JOIN fachname on fach.fachid = fachname.id WHERE fach.buchid = " + mhb.get(j).getId() + ";";
 						res = state.executeQuery(sql);
 						while (res.next()) {
 							String name = res.getString("Name");
 							fach.add(new Fach(name));
 						}
 						for (int k = 0; k < fach.size(); k++){ 
 						//get Module
 							sql = "SELECT m.* FROM module as m JOIN fach as f ON f.modID = m.modID JOIN fachname as fn ON f.fachid = fn.id WHERE fn.Name = '"+ fach.get(k).getName() + "' AND buchid = " + mhb.get(j).getId() + ";";
 							res = state.executeQuery(sql);
 							while (res.next()) {
 								String name = res.getString("modulname");
 								int version = res.getInt("Version");
 								modul.add(new Modul(name, version));
 							}
 							for (int l = 0; l < modul.size(); l++) {
 								//get Felder
 								res = state.executeQuery("SELECT txt.label, txt.text, txt.dezernat2 FROM text as txt join module as m on txt.mID = m.modID WHERE m.modulname = '" + modul.get(l).getName()+ "' AND txt.version = " + modul.get(l).getVersion() + ";");
 
 								while (res.next()) {
 									felder.add(new Feld(res.getString("label"), res.getString("text"), res.getBoolean("dezernat2")));
 								}
 								modul.get(l).setFelder(felder);
 								felder = new ArrayList<Feld>();
 							}
 							fach.get(k).setModlist(modul);
 							modul = new ArrayList<Modul>();
 						}
 						mhb.get(j).setFach(fach);
 						fach = new ArrayList<Fach>();
 					}
 					alldata.get(i).setModbuch(mhb);
 					mhb = new ArrayList<Modulhandbuch>();
 
 				}
 
 				res.close();
 				state.close();
 
 			} catch (SQLException e) {
 				e.printStackTrace();
 			}
 			disconnect();
 		}
 
 		return alldata;
 
 	}
 	
 	/**
 	 * Gibt eine Liste von Modulen zurueck die ein User bearbeiten kann
 	 * @param email
 	 * @return Liste von Modulen 
 	 * 						die der User Bearbeiten darf
 	 */
 	
 	public ArrayList<Modul> getworkModul(String email){
 		ArrayList<Modul> module = new ArrayList<Modul>();
 		ArrayList<Feld> felder = new ArrayList<Feld>();
 		ResultSet res = null;
 		Statement state = null;
 		String sql;
 		if (connect() == true) {
 			try {
 				state = this.con.createStatement();
 				sql = "SELECT m.* " 
 						+ "FROM module as m JOIN mod_user_relation as rel on rel.modname = m.modulname JOIN user on user.id = rel.userID JOIN fach on m.modID = fach.modID JOIN modulhandbuch as mhb on fach.buchid = mhb.ID"
 						+ "WHERE user.email = '"+email+"' and mhb.akzeptiert = 0";
 						
 				res = state.executeQuery(sql);
 				while (res.next()) {
 					String name = res.getString("modulname");
 					int version = res.getInt("Version");
 					module.add(new Modul(name, version));
 				}
 				for (int i = 0; i < module.size(); i++) {
 					//get Felder
 					res = state.executeQuery("SELECT txt.label, txt.text, txt.dezernat2 FROM text as txt join module as m on txt.mid = m.modid WHERE m.modulname = '" + module.get(i).getName()
 							+ "' AND txt.version = " + module.get(i).getVersion() + ";");
 
 					while (res.next()) {
 						felder.add(new Feld(res.getString("label"), res.getString("text"), res.getBoolean("dezernat2")));
 					}
 					module.get(i).setFelder(felder);
 					felder = new ArrayList<Feld>();
 				}
 			} catch (SQLException e) {
 				e.printStackTrace();
 			}
 			disconnect();
 		}
 		
 		return module; 
 	}
 	
 	/**
 	 * gibt alle Module zurck die den Bearbeitungsstatus haben der als Paramenter uebergeben wird
 	 * soll verwendet werden damit Redaktion und Dekan die Modullisten bekommen
 	 * @param status
 	 * @return Modulliste
 	 */
 	
 	public ArrayList<Modul> getStatusModul(int status){
 		ArrayList<Modul> module = new ArrayList<Modul>();
 		ArrayList<Feld> felder = new ArrayList<Feld>();
 		ResultSet res = null;
 		Statement state = null;
 		String sql;
 		if (connect() == true) {
 			try {
 				state = this.con.createStatement();
 				sql = "SELECT m.* " 
 						+ "FROM module as m JOIN mod_user_relation as rel on f.modID = m.modID JOIN fach on m.modID = fach.modID JOIN modulhandbuch as mhb on fach.buchid = mhb.ID"
 						+ "WHERE mhb.akzeptiert = 0 and m.status = '"+status+"';";
 						
 				res = state.executeQuery(sql);
 				while (res.next()) {
 					String name = res.getString("modulname");
 					int version = res.getInt("Version");
 					module.add(new Modul(name, version));
 				}
 				for (int i = 0; i < module.size(); i++) {
 					//get Felder
 					res = state.executeQuery("SELECT txt.label, txt.text, txt.dezernat2 FROM text as txt join module as m on txt.mid = m.modid WHERE m.modulname = '" + module.get(i).getName()
 							+ "' AND txt.version = " + module.get(i).getVersion() + ";");
 
 					while (res.next()) {
 						felder.add(new Feld(res.getString("label"), res.getString("text"), res.getBoolean("dezernat2")));
 					}
 					module.get(i).setFelder(felder);
 					felder = new ArrayList<Feld>();
 				}
 			} catch (SQLException e) {
 				e.printStackTrace();
 			}
 			disconnect();
 		}
 		
 		return module; 
 	}
 	
 	/**
 	 *  Default felder werden gespeichert
 	 */
 	
 	public int setDefaultFelder(ArrayList<Feld> felder){
 		int status = FAILED;
 		PreparedStatement state = null;
 		if(connect() == true){
 		try{
 			state = this.con.prepareStatement("TRUNCATE TABLE default_felder;");
 			state.executeUpdate();
 			for(int i = 0; i < felder.size(); i++){
 				state = this.con.prepareStatement("INSERT INTO default_felder(label,dezernat) VALUES (?, ?);");
 				state.setString(1, felder.get(i).getLabel());
 				state.setBoolean(2, felder.get(i).isDezernat());
 				state.executeUpdate();
 			}
 			status = SUCCESS;
 		}catch(SQLException e){
 			e.printStackTrace();
 			status = FAILED;
 		}
 		disconnect();
 		}
 		return status;
 	}
 	
 	/**
 	 * Default Felder werden ausgegeben
 	 */
 	
 
 	public ArrayList<Feld> getDefaultFelder(){
 		ArrayList<Feld> felder = new ArrayList<Feld>();
 		String sql;
 		ResultSet res = null;
 		Statement state = null;
 		if(connect() == true){
 		try {
 			state = this.con.createStatement();
 			sql = "SELECT * FROM default_felder;";
 			res = state.executeQuery(sql);
 			while (res.next()) {
 				String label = res.getString("label");
 				Boolean dezernat = res.getBoolean("dezernat");
 				felder.add(new Feld(label, "", dezernat));
 			}
 			
 			} catch (SQLException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 			disconnect();
 		}
 		
 		return felder;
 	}
 	
 	
 	/**
 	 * 
 	 */
 	
 //	public int deleteDefaultFelder(int id){
 //		int status = FAILED;
 //		String sql;
 //		Statement state = null;
 //		
 //		try {
 //			state = this.con.createStatement();
 //			sql = "DELETE FROM default_felder WHERE id ="+id+";";
 //			state.executeQuery(sql);
 //			status = SUCCESS;
 //		} catch (SQLException e) {
 //			// TODO Auto-generated catch block
 //			e.printStackTrace();
 //		}
 //		
 //		
 //		return status;
 //	}
 	
 	
 	
 	
 	
 
 	/**
 	 * @param n Nachricht
 	 * @return Erfolgsstatus
 	 */
 	public int createMessage(Nachricht n) {
 		PreparedStatement state = null;
 		int status = FAILED;
 		if (connect() == true) {
 			try {
 				state = con
 						.prepareStatement("INSERT INTO nachrichten (absender_id, empfaenger_id, betreff, text, gelesen, datum) VALUES(?,?,?,?,?,?)");
 				state.setInt(1, n.getAbsenderID());
 				state.setInt(2, n.getEmpfaengerID());
 				state.setString(3, n.getBetreff());
 				state.setString(4, n.getNachricht());
 				state.setBoolean(5, n.isGelesen());
 				state.setTimestamp(6, dateConverterUtil2SQL(n.getDatum()));
 				state.executeUpdate();
 				status = SUCCESS;
 			} catch (SQLException e) {
 				// TODO fehler fenster aufrufen
 				//e.printStackTrace();
 				status = FAILED;
 			}
 			disconnect();
 		}
 		return status;
 	}
 	
 	public ArrayList<Nachricht> readMessages(int empfaenger_id){
 		ArrayList<Nachricht> liste = new ArrayList<Nachricht>();
 		ResultSet res = null;
 		PreparedStatement state = null;
 		if (connect() == true) {
 			try {
 				state = this.con.prepareStatement("SELECT n.*,CONCAT(u.titel,' ',u.namen,' ',u.vorname,', ',u.email) AS absender, CONCAT(u2.titel,' ',u2.namen,' ',u2.vorname,', ',u2.email) AS empfaenger FROM nachrichten AS n JOIN user AS u ON n.absender_id=u.id JOIN user AS u2 ON n.empfaenger_id=u2.id WHERE empfaenger_id=?");
 				state.setInt(1, empfaenger_id);
 				res = state.executeQuery();
 				while(res.next()){
 					Nachricht n = new Nachricht(res.getInt("id"),res.getInt("absender_id"),empfaenger_id,res.getString("betreff"),dateConverterSQL2Util(res.getTimestamp("datum")),res.getBoolean("gelesen"),res.getString("text"));
 					n.setAbsender(res.getString("absender").trim());
 					n.setEmpfaenger(res.getString("empfaenger").trim());
 					liste.add(n);
 				}
 				res.close();
 				state.close();
 			} catch (SQLException e) {
 				e.printStackTrace();
 			}
 			disconnect();
 		}	
 		return liste;
 	}
 
 	public Timestamp dateConverterUtil2SQL(Date d) {
 		return new Timestamp(d.getTime());
 	}
 	
 	public Date dateConverterSQL2Util(Timestamp t) {
 		Date d = new Date(t.getTime());
 		return d;
 	}
 	
 	
 	
 	/**
 	 * Deadline (Stichtags) Datum speichern
 	 * 
 	 * */
 
 	public int setDate(Date date) {
 		PreparedStatement state = null;
 		int status = FAILED;
 		if (connect() == true) {
 			try {
 				state = this.con.prepareStatement("TRUNCATE TABLE stichtag;");
 				state.executeUpdate();
 				state = this.con.prepareStatement("INSERT INTO stichtag(datum) VALUES (?);");
 				state.setTimestamp(1, dateConverterUtil2SQL(date));
 				state.executeUpdate();
 				state.close();
 				status = SUCCESS;
 				state.close();
 			} catch (SQLException e) {
 				e.printStackTrace();
 				status = FAILED;
 			}
 			disconnect();
 		}
 		return status;
 	}
 	
 	/**
 	 * Deadline (Stichtags) Datum abfrage
 	 * 
 	 * */
 	
 	public java.util.Date getDate(){
 		PreparedStatement state = null;
 		ResultSet res = null;
 		Date date = new Date();
 		if (connect() == true) {
 			try {
 				state = this.con.prepareStatement("SELECT * FROM stichtag;");
 				res = state.executeQuery();
 				while(res.next()){
 					date = dateConverterSQL2Util(res.getTimestamp("datum"));
 				}
 				res.close();
 				state.close();
 			} catch (SQLException e) {
 				//e.printStackTrace();
 				date = new Date();
 			}
 			disconnect();
 		}
 		return date;
 	}
 	/**
 	 * gibt 2 Listen zurueck eine mit den Verwaltern des Moduls und eine andere Liste mit Usern ohne die aus der anderen Liste
 	 * 
 	 * 
 	 * Erste Liste: Liste der nicht Verwalter des bestimmten Moduls
 	 * 
 	 * Zweite Liste: Liste der Verwalter des bestimmten Moduls
 	 * 
 	 * */
 	public ArrayList<ArrayList<User>> getVerwalterLis(String modname){
 		ArrayList<ArrayList<User>> lists = new ArrayList<ArrayList<User>>();
 		PreparedStatement state = null;
 		ResultSet res = null;
 		if(connect() == true){
 			try{
 				lists.add(new ArrayList<User>());
 				lists.add(new ArrayList<User>());
 				//Liste der nicht Verwalter des Moduls
 				state = this.con.prepareStatement("SELECT user.* From User where email not in (SELECT email FROM user join mod_user_relation on user.id = mod_user_relation.userID join module on module.modID = mod_user_relation.modid WHERE module.modulname =?);");
 				state.setString(1, modname);
 				res = state.executeQuery();
 				while(res.next()){
 					lists.get(0).add(new User(res.getInt("id"), res.getString("vorname"), res.getString("name"), res.getString("titel"), res.getString("email"), res.getString("password"), res.getBoolean("userchange"), res.getBoolean("modcreate"), res.getBoolean("modacc"), res.getBoolean("manage"), res.getBoolean("redaktion"), res.getBoolean("frei")));
 				}
 				//Liste der Verwalter
 				state = this.con.prepareStatement("SELECT user.*, rights.* From User join rights on user.id = rights.id join mod_user_relation on user.id = mod_user_relation.userID join module on module.modID = mod_user_relation.modid WHERE module.modulname =?;");
 				state.setString(1, modname);
 				res = state.executeQuery();
 				while(res.next()){
 					lists.get(1).add(new User(res.getInt("id"), res.getString("vorname"), res.getString("name"), res.getString("titel"), res.getString("email"), res.getString("password"), res.getBoolean("userchange"), res.getBoolean("modcreate"), res.getBoolean("modacc"), res.getBoolean("manage"), res.getBoolean("redaktion"), res.getBoolean("frei")));
 				}
 				res.close();
 				state.close();				
 			}catch(SQLException e){
 				e.printStackTrace();
 			}
 			disconnect();
 		}		
 		
 		return lists;
 	}
 	
 	
 	/**
 	 * Fuer die Modulverwalter Liste werden nur die Modulnamen abgefragt
 	 * */
 	
 	public ArrayList<String> getallModulnames(){
 		ArrayList<String> give = new ArrayList<String>();
 		PreparedStatement state = null;
 		ResultSet res = null;
 		if(connect() == true){
 			try{
 				state = this.con.prepareStatement("SELECT DISTINCT modulname FROM module");
 				res = state.executeQuery();
 				while(res.next()){
 					give.add(res.getString("modulname"));
 				}
 				res.close();
 				state.close();
 			}catch(SQLException e){
 				e.printStackTrace();
 			}
 			disconnect();
 		}
 		
 		return give;
 		
 	}
 
 	public void deleteNachricht(String id) {
 		PreparedStatement state = null;
 		if(connect() == true){
 			try{
 				int nID = Integer.parseInt(id);
 				state = this.con.prepareStatement("DELETE FROM nachrichten WHERE id=?");
 				state.setInt(1, nID);
 				state.executeUpdate();
 				this.con.commit();
 				state.close();
 			}catch(SQLException e){
 				e.printStackTrace();
 			}	
 			disconnect();
 		}	
 	}
 
 	public int updateNachricht(Nachricht n) {
 		int status = FAILED;
 		if (connect() == true) {
 			PreparedStatement state = null;
 			try {
 				state = con
 						.prepareStatement("UPDATE nachrichten SET absender_id=?, empfaenger_id=?, betreff=?, text=?, gelesen=?, datum=? WHERE id=?");
 				state.setInt(1, n.getAbsenderID());
 				state.setInt(2, n.getEmpfaengerID());
 				state.setString(3, n.getBetreff());
 				state.setString(4, n.getNachricht());
 				state.setBoolean(5, n.isGelesen());
 				state.setTimestamp(6, dateConverterUtil2SQL(n.getDatum()));
 				state.setInt(7, n.getId());
 				state.executeUpdate();
 				status = SUCCESS;
 				state.close();
 			} catch (SQLException e) {
 				e.printStackTrace();
 				status = FAILED;
 			}
 			disconnect();
 		}
 		return status;
 	}
 	
 	public int setPO(String studiengang, String abschluss, int pojahr){
 		int status = FAILED;
 		PreparedStatement state = null;
 		if(connect() == true){
 			try{
 				state = con
 						.prepareStatement("INSERT IGNORE INTO pordnung(jahr, sid) VALUES (?, (SELECT id FROM studiengang WHERE name =? AND abschluss =?))");
 				state.setInt(1, pojahr);
 				state.setString(2, studiengang);
 				state.setString(3, abschluss);
 				state.executeUpdate();
 				status = SUCCESS;
 				state.close();
 			}catch (SQLException e) {
 				e.printStackTrace();
 				status = FAILED;
 			}
 			disconnect();
 		}
 		return status;
 	}
 	
 	public int setModulhandbuch(String studiengang, String abschluss, int pojahr, String prosa, String semester, String jahr){
 		int status = FAILED;
 		PreparedStatement state = null;
 		if(connect() == true){
 			try{
 				state = con
 						.prepareStatement("INSERT IGNORE INTO modulhandbuch(prosa, semester, jahr, poid, akzeptiert) VALUES (?, ?, ? ,(SELECT po.ID FROM pordnung AS po JOIN studiengang AS s on po.sid = s.id WHERE s.name =? AND s.abschluss =? AND po.jahr =?) ,0 )");
 				state.setString(1, prosa);
 				state.setString(2, semester);
 				state.setString(3, jahr);
 				state.setString(4, studiengang);
 				state.setString(5, abschluss);
 				state.setInt(6, pojahr);
 				state.executeUpdate();
 				status = SUCCESS;
 				state.close();
 			}catch (SQLException e) {
 				e.printStackTrace();
 				status = FAILED;
 			}
 			disconnect();
 		}
 		return status;
 	}
 	
 	public int setFach(String name){
 		int status = FAILED;
 		PreparedStatement state = null;
 		if(connect() == true){
 			try{
 				state = con
 						.prepareStatement("INSERT IGNORE INTO Fachname(name) VALUES (?)");
 				state.setString(1, name);
 				state.executeUpdate();
 				status = SUCCESS;
 				state.close();
 			}catch (SQLException e) {
 				e.printStackTrace();
 				status = FAILED;
 			}
 			disconnect();
 		}
 		return status;
 	}
 	
 	public int updateFach(String oldname, String newname){
 		int status = FAILED;
 		PreparedStatement state = null;
 		if(connect() == true){
 			try{
 				state = con
 						.prepareStatement("UPDATE Fachname SET name=? WHERE name=?");
 				state.setString(1, newname);
 				state.setString(2, oldname);
 				state.executeUpdate();
 				status = SUCCESS;
 				state.close();
 			}catch (SQLException e) {
 				e.printStackTrace();
 				status = FAILED;
 			}
 			disconnect();
 		}
 		return status;
 	}
 	
 	public ArrayList<pordnung> getallpo(){
 		ArrayList<pordnung> getlist = new ArrayList<pordnung>();
 		PreparedStatement state = null;
 		ResultSet res = null;
 		if(connect() == true){
 			try{
 				state = con.prepareStatement("SELECT po.*, s.name, s.abschluss FROM pordnung as po JOIN studiengang as s on po.sid = s.id");
 				res = state.executeQuery();
 				while(res.next()){
 					int id = res.getInt("id");
 					int pojahr = res.getInt("jahr");
 					int sid = res.getInt("sid");
 					String sname = res.getString("name");
 					String sabschluss = res.getString("abschluss");
 					getlist.add(new pordnung(id, pojahr, sid, sname, sabschluss));
 				}
 			}catch (SQLException e) {
 				e.printStackTrace();
 			}
 			disconnect();
 		}
 		
 		
 		return getlist;
 		
 	}
 	
 	public ArrayList<Fach> getFach(){
 		ArrayList<Fach> faecher = new ArrayList<Fach>();
 		PreparedStatement state = null;
 		ResultSet res = null;
 		if(connect() == true){
 			try{
 				state = con.prepareStatement("SELECT name FROM fachname;");
 				res = state.executeQuery();
 				while(res.next()){
 					String zws = res.getString("name");
 					faecher.add(new Fach(zws));
 				}
 			}catch(SQLException e){
 				e.printStackTrace();
 			}
 			disconnect();
 		}
 		return faecher;
 	}
 
 }
