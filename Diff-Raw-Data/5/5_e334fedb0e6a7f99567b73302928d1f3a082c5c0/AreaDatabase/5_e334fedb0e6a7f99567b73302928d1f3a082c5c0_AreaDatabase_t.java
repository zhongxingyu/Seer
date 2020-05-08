 package com.zand.areaguard;
 
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Set;
 
 public class AreaDatabase {
 	private static AreaDatabase instance = null;
 
 	public static AreaDatabase getInstance() {
 		if (instance == null) {
 			instance = new AreaDatabase();
 		}
 		return instance;
 	}
 
 	private String driver;
 	private String url;
 	private String user;
 	private String password;
 	private String areas;
 	private String areaMsgs;
 	private String areaLists;
 	private boolean keepConn = false;
 
 	private Connection conn = null;
 
 	protected AreaDatabase() {
 		// Exists only to defeat instantiation.
 	}
 
 	public int addArea(String name, int coords[]) {
 		int ret = -1;
 		if (!getAreaIds(name).isEmpty()) {
 			return -1;
 		}
 
 		String insert = "INSERT INTO `" + areas
 				+ "` (Name, x1, y1, z1, x2, y2, z2)"
 				+ "VALUES (?, ?, ?, ?, ?, ?, ?);";
 		connect();
 		if (conn == null)
 			return -1;
 		try {
 			PreparedStatement ps = conn.prepareStatement(insert);
 			ps.setString(1, name);
 			for (int i = 0; i < 6; i++)
 				ps.setInt(i + 2, coords[i]);
 			ps.execute();
 
 			Statement st = conn.createStatement();
 
 			ResultSet rs = st.executeQuery("SELECT LAST_INSERT_"
 					+ (url.toLowerCase().contains("sqlite") ? "ROW" : "")
 					+ "ID();");
 			if (rs.next())
 				ret = rs.getInt(1);
 			rs.close();
 			ps.close();
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			return -1;
 		}
 		disconnect();
 		return ret;
 	}
 	
 	public boolean addList(int area, String name, HashSet<String> values) {
 		boolean ret = removeList(area, name, values);
 		connect();
 		if (conn != null && ret) {
 			try {
 				PreparedStatement ps = conn.prepareStatement("INSERT INTO `"
 						+ areaLists + "` (AreaId, List, Value)" + "VALUES (?, ?, ?)");
 
 				for (String value : values) {
 					ps.setInt(1, area);
 					ps.setString(2, name);
 					ps.setString(3, value);
 					ps.execute();
 				}
 
 				// Close events
 				ps.close();
 
 			} catch (SQLException e) {
 				System.err.println("Faild to add Values: " + e.getMessage());
 				ret = false;
 			}
 			disconnect();
 		}
 		return ret;
 	}
 
 	public void config(String driver, String url, String user, String password,
 			String areas, String areaMsgs, String areaLists, boolean keepConn) {
 		this.driver = driver;
 		this.url = url;
 		this.user = user;
 		this.password = password;
 		this.areas = areas;
 		this.areaMsgs = areaMsgs;
 		this.areaLists = areaLists;
 		this.keepConn = keepConn;
 		instance = this;
 	}
 
 	public boolean connect() {
 		try {
 			if (conn == null || conn.isClosed()) {
 				System.out.println("Connecting");
 				Class.forName(driver);
 				conn = DriverManager.getConnection(url, user, password);
 			}
 		} catch (java.lang.ClassNotFoundException e) {
 			// Could not find driver
 			System.err.print("AreaGuard: Could not find driver \"" + driver
 					+ "\"\n");
 		} catch (SQLException e) {
 			// Could not connect to the database
 			conn = null;
 			System.err.print("AreaGuard: Can't Connect, " + e.getMessage()
 					+ "\n");
 		}
 		return (conn != null);
 	}
 
 	public boolean createTables() {
 		// Load the sql Data
 		String[] lines = JarFile.toString(
 				(url.toLowerCase().contains("sqlite") ? 
 						"data/sqlight.sql" : "data/mysql.sql"))
 				.replace("<areas>", areas)
 				.replace("<areaMsgs>", areaMsgs)
 				.replace("<areaLists>", areaLists)
 				.split(";");
 
 		connect();
 		if (conn == null)
 			return false;
 		
 		// Execute the sql data
 		for (String sql : lines) {
 			if (sql.trim().isEmpty()) continue;
 			sql += ";";
 			//System.out.println(sql);
 			try {
 				Statement st = conn.createStatement();
 				st.execute(sql);
 				st.close();
 			} catch (SQLException e) {
 				System.err.println("Failed to Create Tables: " + e.getMessage());
 				e.printStackTrace();
 			}
 		}
 		disconnect();
 		return true;
 	}
 
 	public void disconnect() {
 		disconnect(false);
 	}
 
 	public void disconnect(boolean force) {
 		try {
 			if (conn != null && !conn.isClosed() && (!keepConn || force)) {
 				System.out.println("Disconnecting");
 				conn.close();
 				conn = null;
 			}
 		} catch (SQLException e) {
 			// Could not disconnect from the database?
 			conn = null;
 			System.err.print("AreaGuard:  Can't Disconnect, " + e.getMessage());
 		}
 	}
 
 	public Area getArea(int id) {
 		Area ret = null;
 		connect();
 
 		try {
 			if (conn == null || conn.isClosed())
 				return null;
 			PreparedStatement ps = conn.prepareStatement("SELECT * FROM `"
 					+ areas + "` WHERE Id=? LIMIT 1");
 			ps.setInt(1, id);
 
 			ResultSet rs = ps.executeQuery();
 			if (rs.next())
 				ret = new Area(id, rs.getString("Name"), new int[] {
 						rs.getInt("x1"), rs.getInt("y1"), rs.getInt("z1"),
 						rs.getInt("x2"), rs.getInt("y2"), rs.getInt("z2") });
 
 			rs.close();
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		disconnect();
 		return ret;
 	}
 
 	public int getAreaId(int x, int y, int z) {
 		int ret = -1;
 		String sql = "SELECT Id FROM `" + areas + "` WHERE x1 <= ? "
 				+ "AND x2 >= ? " + "AND y1 <= ? " + "AND y2 >= ? "
 				+ "AND z1 <= ? " + "AND z2 >= ? " + "ORDER BY `" + areas
 				+ "`.Id DESC LIMIT 1";
 
 		connect();
 		if (conn != null) {
 			try {
 				PreparedStatement ps = conn.prepareStatement(sql);
 				ps.setInt(1, x);
 				ps.setInt(2, x);
 				ps.setInt(3, y);
 				ps.setInt(4, y);
 				ps.setInt(5, z);
 				ps.setInt(6, z);
 				ps.execute();
 
 				// Get the result
 				ResultSet rs = ps.getResultSet();
 				if (rs.next()) ret = rs.getInt(1);
 
 				// Close events
 				rs.close();
 				ps.close();
 
 			} catch (SQLException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 
 			disconnect();
 		}
 		return ret;
 	}
 
 	public int getAreaId(String name) {
 		int ret = -1;
 		String sql = "SELECT Id FROM `" + areas + "` WHERE name = ? LIMIT 1";
 
 		connect();
 		if (conn != null) {
 			try {
 				PreparedStatement ps = conn.prepareStatement(sql);
 				ps.setString(1, name);
 				ps.execute();
 
 				// Get the result
 				ResultSet rs = ps.getResultSet();
 				if (rs.next()) ret = rs.getInt(1);
 
 				// Close events
 				rs.close();
 				ps.close();
 
 			} catch (SQLException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 
 			disconnect();
 		}
 		return ret;
 	}
 
 	public ArrayList<Integer> getAreaIds() {
 		ArrayList<Integer> ret = new ArrayList<Integer>();
 
 		String sql = "SELECT Id FROM `" + areas + "`";
 
 		connect();
 		if (conn != null) {
 			try {
 				Statement st = conn.createStatement();
 				ResultSet rs = st.executeQuery(sql);
 
 				// Put it into the Map
 				while (rs.next())
 					ret.add(rs.getInt(1));
 
 				// Close events
 				rs.close();
 				st.close();
 
 			} catch (SQLException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 
 			disconnect();
 		}
 
 		return ret;
 	}
 	
 	public ArrayList<Integer> getAreaIds(int x, int y, int z) {
 		ArrayList<Integer> ret = new ArrayList<Integer>();
 		String sql = "SELECT Id FROM `" + areas + "` WHERE x1 <= ? "
 				+ "AND x2 >= ? " + "AND y1 <= ? " + "AND y2 >= ? "
 				+ "AND z1 <= ? " + "AND z2 >= ? " + "ORDER BY `" + areas
 				+ "`.Id DESC";
 
 		connect();
 		if (conn != null) {
 			try {
 				PreparedStatement ps = conn.prepareStatement(sql);
 				ps.setInt(1, x);
 				ps.setInt(2, x);
 				ps.setInt(3, y);
 				ps.setInt(4, y);
 				ps.setInt(5, z);
 				ps.setInt(6, z);
 				ps.execute();
 
 				// Get the result
 				ResultSet rs = ps.getResultSet();
 				while (rs.next())
 					ret.add(rs.getInt(1));
 
 				// Close events
 				rs.close();
 				ps.close();
 
 			} catch (SQLException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 
 			disconnect();
 		}
 		return ret;
 	}
 
 	public ArrayList<Integer> getAreaIds(String name) {
 		ArrayList<Integer> ret = new ArrayList<Integer>();
 		String sql = "SELECT Id FROM `" + areas + "` WHERE name = ?";
 
 		connect();
 		if (conn != null) {
 			try {
 				PreparedStatement ps = conn.prepareStatement(sql);
 				ps.setString(1, name);
 				ps.execute();
 
 				// Get the result
 				ResultSet rs = ps.getResultSet();
 				while (rs.next())
 					ret.add(rs.getInt(1));
 
 				// Close events
 				rs.close();
 				ps.close();
 
 			} catch (SQLException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 
 			disconnect();
 		}
 		return ret;
 	}
 	
 	public ArrayList<String> getList(int area, String list) {
 		ArrayList<String> ret = new ArrayList<String>();
 		String sql = "SELECT Value FROM `" + areaLists + "` WHERE AreaId=? AND List=?";
 
 		connect();
 		if (conn != null) {
 			try {
 				PreparedStatement ps = conn.prepareStatement(sql);
 				ps.setInt(1, area);
 				ps.setString(2, list);
 				ps.execute();
 
 				// Get the result
 				ResultSet rs = ps.getResultSet();
 				while (rs.next())
 					ret.add(rs.getString(1));
 
 				// Close events
 				rs.close();
 				ps.close();
 
 			} catch (SQLException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 
 			disconnect();
 		}
 		return ret;
 	}
 	
 	public Set<String> getLists(int area) {
 		Set<String> ret = new HashSet<String>();
 		String sql = "SELECT List FROM `" + areaLists + "` WHERE AreaId=?";
 
 		connect();
 		if (conn != null) {
 			try {
 				PreparedStatement ps = conn.prepareStatement(sql);
 				ps.setInt(1, area);
 				ps.execute();
 
 				// Get the result
 				ResultSet rs = ps.getResultSet();
 				while (rs.next()) ret.add(rs.getString(1));
 
 				// Close events
 				rs.close();
 				ps.close();
 
 			} catch (SQLException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 
 			disconnect();
 		}
 		return ret;
 	}
 
 	public String getMsg(int area, String name) {
 		String ret = "";
 		String sql = "SELECT Msg FROM `" + areaMsgs + "` WHERE AreaId=? AND Name=? LIMIT 1";
 
 		connect();
 		if (conn != null) {
 			try {
 				PreparedStatement ps = conn.prepareStatement(sql);
 				ps.setInt(1, area);
 				ps.setString(2, name);
 				ps.execute();
 
 				// Get the result
 				ResultSet rs = ps.getResultSet();
 				if (rs.next()) ret = rs.getString(1);
 
 				// Close events
 				rs.close();
 				ps.close();
 
 			} catch (SQLException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 
 			disconnect();
 		}
 		return ret;
 	}
 	
 	public HashMap<String, String> getMsgs(int area) {
 		HashMap<String, String> ret = new HashMap<String, String>();
 		String sql = "SELECT Name, Msg FROM `" + areaMsgs + "` WHERE AreaId=?";
 
 		connect();
 		if (conn != null) {
 			try {
 				PreparedStatement ps = conn.prepareStatement(sql);
 				ps.setInt(1, area);
 				ps.execute();
 
 				// Get the result
 				ResultSet rs = ps.getResultSet();
 				while (rs.next())
 					ret.put(rs.getString(1), rs.getString(2));
 
 				// Close events
 				rs.close();
 				ps.close();
 
 			} catch (SQLException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 
 			disconnect();
 		}
 		return ret;
 	}
 
 	public boolean listHas(int area, String list, String value) {
 		boolean ret = false;
 		connect();
 		if (conn != null) {
 			try {
 				PreparedStatement ps = conn
 						.prepareStatement("SELECT AreaId FROM `" + areaLists
 								+ "`" + "WHERE AreaId=? AND List=? AND Value=? LIMIT 1");
 				ps.setInt(1, area);
 				ps.setString(2, list);
 				ps.setString(3, value);
 				ResultSet rs = ps.executeQuery();
 
 				ret = rs.next();
 
 				// Close events
 				ps.close();
 
 			} catch (SQLException e) {
 				System.err.println("Faild to check ListValue: "
 						+ e.getMessage());
 			}
 			disconnect();
 		}
 		return ret;
 	}
 
 	public void removeAllAreas() {
 		for (int id : getAreaIds()) {
 			removeArea(id);
 		}
 	}
 
 	public boolean removeArea(int id) {
 		String sql = "DELETE FROM `" + areas + "` WHERE Id=?";
 		if (!removeMsgs(id)) return false;
 		if (!removeLists(id)) return false;
 		connect();
 		if (conn == null)
 			return false;
 		try {
 			PreparedStatement ps = conn.prepareStatement(sql);
 			ps.setInt(1, id);
 			ps.execute();
 
 			// Close events
 			ps.close();
 
 		} catch (SQLException e) {
 			System.err.println("Faild to remove area: " + e.getMessage());
 		}
 		disconnect();
 
 		return true;
 	}
 
 	public boolean removeList(int area, String list) {
 		String sql = "DELETE FROM `" + areaLists + "` WHERE AreaId=? AND List=?";
 		connect();
 		if (conn == null)
 			return false;
 		try {
 			PreparedStatement ps = conn.prepareStatement(sql);
 			ps.setInt(1, area);
 			ps.setString(2, list);
 			ps.execute();
 
 			// Close events
 			ps.close();
 
 		} catch (SQLException e) {
 			System.err.println("Faild to remove list: " + e.getMessage());
 		}
 		disconnect();
 		return true;
 	}
 	
 	public boolean removeList(int area, String list, HashSet<String> values) {
 		String sql = "DELETE FROM `" + areaLists + "` WHERE AreaId=? AND List=? AND Value=?";
 		connect();
 		if (conn == null)
 			return false;
 		try {
 			PreparedStatement ps = conn.prepareStatement(sql);
 			ps.setInt(1, area);
 			ps.setString(2, list);
 			for (String value : values) {
 				ps.setString(3, value);
 				ps.execute();
 			}
 
 			// Close events
 			ps.close();
 
 		} catch (SQLException e) {
 			System.err.println("Faild to remove list: " + e.getMessage());
 		}
 		disconnect();
 		return true;
 	}
 
 	public boolean removeLists(int area) {
 		String sql = "DELETE FROM `" + areaLists + "` WHERE AreaId=?";
 		connect();
 		if (conn == null)
 			return false;
 		try {
 			PreparedStatement ps = conn.prepareStatement(sql);
 			ps.setInt(1, area);
 			ps.execute();
 
 			// Close events
 			ps.close();
 
 		} catch (SQLException e) {
 			System.err.println("Faild to remove list: " + e.getMessage());
 		}
 		disconnect();
 		return true;
 	}
 	
 	public boolean removeMsgs(int area) {
 		String sql = "DELETE FROM `" + areaMsgs + "` WHERE AreaId=?";
 		connect();
 		if (conn == null)
 			return false;
 		try {
 			PreparedStatement ps = conn.prepareStatement(sql);
 			ps.setInt(1, area);
 			ps.execute();
 
 			// Close events
 			ps.close();
 
 		} catch (SQLException e) {
 			System.err.println("Faild to remove list: " + e.getMessage());
 		}
 		disconnect();
 		return true;
 	}
 
 	public boolean setMsg(int area, String name, String msg) {
 		String delete = "DELETE FROM `" + areaMsgs + "` WHERE AreaId=? AND Name=?";
 		String insert = "INSERT INTO `" + areaMsgs + "` (AreaId, Name, Msg)"
 				+ "VALUES (?, ?, ?);";
 		connect();
 		if (conn == null) return false;
 		try {
 			// Erase the current Msg
 			PreparedStatement ps = conn.prepareStatement(delete);
 			ps.setInt(1, area);
 			ps.setString(2, name);
 			ps.execute();
 			ps.close();
 			
 			// Add the msg if its not empty
 			if (!msg.isEmpty()) {
 				ps = conn.prepareStatement(insert);
 				ps.setInt(1, area);
 				ps.setString(2, name);
 				ps.setString(3, msg);
 				ps.execute();
 				ps.close();
 			}
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			return false;
 		}
 		disconnect();
 		return true;
 	}
 
 	public boolean updateArea(Area area) {
 		String update = "UPDATE `" + areas + "` "
				+ "SET Name=?, x1=?, y1=?, z1=?, x2=?, y2=?, z2=? "
 				+ "WHERE Id=?;";
 		connect();
 		if (conn == null)
 			return false;
 		try {
 			PreparedStatement ps = conn.prepareStatement(update);
 			ps.setString(1, area.getName());
 			for (int i = 0; i < 6; i++)
 				ps.setInt(i + 2, area.getCoords()[i]);
 			ps.setInt(8, area.getId());
 
			ps.execute();
 			ps.close();
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return true;
 	}
 
 }
