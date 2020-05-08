 package Parser;
 
 import java.sql.CallableStatement;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.SQLException;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.List;
 
 import dataTypes.Line;
 import dataTypes.Server;
 import dataTypes.User;
 
 public class Writer {
 	private final String url;
 	private final String dbName;
 	private final String userName;
 	private final String pass;
 
 	private Collection<User> users;
 	private Collection<Server> servers;
 	private List<Line> lines;
 
 
 
 	public Writer(String url, String dbName, String userName, String pass) {
 		super();
 		this.url = url;
 		this.dbName = dbName;
 		this.userName = userName;
 		this.pass = pass;
 		this.users = null;
 		this.servers = null;
 		this.lines = null;
 	}
 
 
 
 	public void setUsers(Collection<User> collection) {
 		this.users = collection;
 	}
 
 
 
 	public void setServers(Collection<Server> servers) {
 		this.servers = servers;
 	}
 
 
 
 	public void setLines(List<Line> lines) {
 		this.lines = lines;
 		Collections.sort(this.lines);
 	}
 
 
 
 	public void writeToDB() {
 		try {
 			Connection conn = DriverManager.getConnection(url + dbName,
 					userName, pass);
 			conn.setAutoCommit(false);
 
 			writeUsersToDB(conn); // ensures users updated with ID's
 			conn.commit();
 			writeServersToDB(conn); // ensures users updated with ID's
 			conn.commit();
 
 			//inserts a line into the database.
 			PreparedStatement insertLine = conn
 					.prepareStatement("INSERT INTO entry VALUES(" //$NON-NLS-1$
 							+ "DEFAULT, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? ,? ,? ,?, ?)"); //$NON-NLS-1$
 
 			//find a single location record matching the source.
 			PreparedStatement geoIp = conn.prepareStatement("SELECT geo.locId FROM geo LEFT JOIN " +
 					"ip ON geo.locId=ip.locId WHERE MBRCONTAINS(ip.ip_poly, POINTFROMWKB(POINT(INET_ATON(?), 0)))"); //where are we?
 
			CallableStatement freq_loc_query = conn.prepareCall("{call freq_loc_check(?, ?, ?, ?)}");
			CallableStatement freq_time_query = conn.prepareCall("{call freq_time_check(?, ?, ?, ?)}");;
 			//updates freq_loc entries, incrementing count if exists, or creating new entry.
 			CallableStatement freq_loc_add = conn.prepareCall("{call freq_loc_add(?, ?, ?, ?, ?, ?)}"); //update freq_loc entry, increments if there's a matching one.
 
 			CallableStatement freq_time_add = conn.prepareCall("{call freq_time_add(?, ?, ?, ?, ?, ?, ?)}");
 
 			Line l;
 			for (int i = 0; i < lines.size(); i++) {
 				//writing location/time also tests if entry prompting write counts as frequent.
 				l = lines.get(i);
 				//i++;
 				l.writeLoc(freq_loc_add, geoIp, freq_loc_query); //must be written before entries are written to db.
 				l.writeTime(freq_time_add, freq_time_query);
 				l.writeToDB(insertLine);
 				if ((i % 1000) == 0) { // write it out every thousand lines,
 										// just to be sure it all writes.
 					insertLine.executeBatch();
 					conn.commit();
 					System.out.printf("inserted %d lines\n", i); //$NON-NLS-1$
 				}
 			}
 			insertLine.executeBatch(); // flush what's left.
 			conn.commit();
 
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 	}
 
 	private void writeUsersToDB(Connection conn) throws SQLException {
 		CallableStatement s = conn.prepareCall("{call insert_user(?, ?)}"); //$NON-NLS-1$
 		for (User u : this.users) {
 			u.writeToDB(s);
 		}
 	}
 
 	private void writeServersToDB(Connection conn) throws SQLException {
 		CallableStatement serve = conn
 				.prepareCall("{call insert_server(?, ?)}"); //$NON-NLS-1$
 		for (Server s : this.servers) {
 			s.writeToDB(serve);
 		}
 	}
 }
