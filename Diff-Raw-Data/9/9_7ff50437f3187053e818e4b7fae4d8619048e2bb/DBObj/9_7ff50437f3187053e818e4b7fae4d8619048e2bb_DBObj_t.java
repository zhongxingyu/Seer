 package db;
 
 import microcontroller.interfaces.Database;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.sql.ResultSet;
 
 public class DBObj implements Database {
 
 	private String driver;
 	private String db_path;
 	private Connection db_conn;
 	private boolean db_conn_state;
 	
 	public DBObj (String driver, String db_path ) {
 		this.driver = driver;
 		this.db_path = db_path;
 		this.db_conn_state = false;
 		this.db_conn =  null;
 	}
 	
 	public boolean get_db_conn_state() {
 		return db_conn_state;
 	}
 	
 	public void set_db_conn_state(boolean state) {
 		db_conn_state = state;
 		return;
 				
 	}
 	
 	public boolean connect_loki_db() {
 		try {
 			db_conn = DriverManager.getConnection(db_path);
 		} catch (SQLException e) {
 			e.printStackTrace();
 			set_db_conn_state(false);
 			return false;
 		}
 		set_db_conn_state(true);
 		return true;
 	}
 	
 	public Connection get_db_connection() {
 		return db_conn;
 	}
 	
 	public void close_db_connection() {
 		try {
 			db_conn.close();
 		}catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 	
 	
 	public String get_driver() {
 		return driver;
 	}
 	
 	public String get_db_path() {
 		return db_path;
 	}
 	
 	public void set_driver(String driver) {
 		this.driver = driver;
 	}
 	
 	public void set_db_path(String db_path) {
 		this.db_path = db_path;
 	}
 	
 	
 	public String execute_query(String query, String col_name, boolean sel_str, boolean sel_int) {
 		try {
 			ResultSet rs;
 			if (this.get_db_conn_state() == false)
 				this.connect_loki_db();
 			Statement stmt = this.get_db_connection().createStatement();
 			rs = stmt.executeQuery(query);
 			if (sel_str ==  true) {
 				String tn = rs.getString(col_name);
 				return tn;
 			}
 			else if (sel_int == true) {
 				Integer tn = rs.getInt(col_name);
 				return tn.toString();
 			}
 			return null;
 		}catch(Exception e) {
 			e.printStackTrace();
 			return null;
 		}
 	}
 	
 	@Override
 	public String getTwitterNameFromID(String id) {
 		String q = "select twitter from user where id='"+id+"';";
 		String rs = execute_query(q, "twitter",true, false);
 		return rs;
 	}
 
 	@Override
 	public String getTwitterNameFromDeviceId(String deviceId) {
 		String q = "select twitter from user where id='"+deviceId+"';";
 		String rs = execute_query(q, "twitter",true, false);
 		return rs;
 	}
 
 	@Override
 	public boolean isIdInRoomDB(String id) {
		String q = "select local from user where id='"+id+"';";
 		String rs = execute_query(q,"date",false,true);
		Integer local = new Integer(rs);
		if (local==1)
 			return true;
 		return false;
 	}
 
 	@Override
 	public boolean isIdAllowed(String id) {
 		String q = "select id from user where id='"+id+"';";
 		String rs = execute_query(q, "id",true, false);
 		if (rs == null)
 			return false;
 		return true;
 	}
 
 	
 	public String getUserIdfromDevId(String dev_id) {
 		String q = "select id from user where devid='"+dev_id+"';";
 		String rs = execute_query(q, "id",true, false);
 		return rs;
 	}
 	
 	@Override
 	public void logOutUserWithDeviceId(String userDeviceId) {
 		Long t  = System.currentTimeMillis() /1000L;
 		String user_id = this.getUserIdfromDevId(userDeviceId);
 		String q="insert into log (id, date, event) valies('"+user_id+"',"+t.toString()+", 0);";
 		execute_query(q,null,false,false);
 
 	}
 
 	@Override
 	public String getDeviceIdFromID(String id) {
 		String q = "select devId from user where id='"+id+"';";
 		String rs = execute_query(q, "devid",true, false);
 		return rs;
 	}
 
 	@Override
 	public void logInUserWithID(String id) {
 		Long t  = System.currentTimeMillis() /1000L;
 		String q="insert into log (id, date, event) valies('"+id+"',"+t.toString()+", 1);";
 		execute_query(q,null,false,false);
 		return;
 	}
 
 }
