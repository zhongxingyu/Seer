 package com.zand.areaguard.area.sql;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.ArrayList;
 import java.util.Properties;
 
 import com.zand.areaguard.area.Area;
 import com.zand.areaguard.area.Cuboid;
 import com.zand.areaguard.area.Storage;
 import com.zand.areaguard.area.World;
 import com.zand.areaguard.area.error.ErrorArea;
 import com.zand.areaguard.area.error.ErrorCuboid;
 import com.zand.areaguard.area.error.ErrorWorld;
 
 public class SqlStorage implements Storage {
 	private String configFilename;
 	private String driver;
 	private String url;
 	private String user;
 	private String password;
 	private int warnDelay = 1000;
 	protected String tablePrefix;
 	private boolean keepConn = false;
 	protected Connection conn = null;
 	private long connectTime;
 	
 	public SqlStorage(String filename) {
 		loadConfig(filename);
 	}
 	
 	public SqlStorage(String driver, String url, String user, String password,
 			String tablePrefix, boolean keepConn) {
 		config(driver, url, user, password, tablePrefix, keepConn);
 	}
 	
 	public boolean loadConfig() {
 		return loadConfig(configFilename);
 	}
 	
 	public boolean loadConfig(String filename) {
 		configFilename = filename;
 		Properties props = new Properties();
 		
 		try {
 			props.load(new FileInputStream(filename));
 			
 			// Configure Connection
 			String url = props.getProperty("url");
 			
 			// Figure out what driver to use
 			String driver = props.getProperty("driver");
 			if (driver == null || driver.isEmpty() || driver.equalsIgnoreCase("auto")) {
 				String lower = url.toLowerCase().replaceAll("\\\\", "");
 				if 		(lower.startsWith("jdbc:sqlite:")) 	driver = "org.sqlite.JDBC";
 				else if (lower.startsWith("jdbc:mysql:"))	driver = "com.mysql.jdbc.Driver";
 				else System.err.println("Coulden't figuer out driver from url");
 			}
 			
 			config(driver, url, 
 						props.getProperty("user"), 
 						props.getProperty("password"), 
 						props.getProperty("table-prefix"),  
 						Boolean.valueOf(props.getProperty("keep-connection")));
 			
 			return true;
 			
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		
 		
 		return false;
 	}
 	
 	public boolean createTables() {
 		// Run Table Creation Script
		String name = "mysql.sql";
		if (url.toLowerCase().contains("sqlite")) name = "sqlite.sql";
 		return executeSqlScript(SqlStorage.class.getClassLoader().getResourceAsStream(name));
 	}
 	
 	public void config(String driver, String url, String user, String password,
 			String tablePrefix, boolean keepConn) {
 		disconnect(true);
 		this.driver = driver;
 		this.url = url;
 		this.user = user;
 		this.password = password;
 		this.tablePrefix = tablePrefix;
 		this.keepConn = keepConn;
 		
 		createTables();
 	}
 
 	public boolean connect() {
 		long time = System.currentTimeMillis();
 		try {
 			if (conn == null || conn.isClosed()) {
 				if (time - connectTime > warnDelay) 
 					System.out.println("[AreaGuard]: Connecting");
 				Class.forName(driver);
 				conn = DriverManager.getConnection(url, user, password);
 			}
 		} catch (java.lang.ClassNotFoundException e) {
 			// Could not find driver
 			if (time - connectTime > warnDelay)
 				System.err.print("[AreaGuard]: Could not find driver \"" + driver
 					+ "\"\n");
 		} catch (SQLException e) {
 			// Could not connect to the database
 			conn = null;
 			if (time - connectTime > warnDelay)
 				System.err.print("[AreaGuard]: Can't Connect, " + e.getMessage()
 					+ "\n");
 		}
 		connectTime = System.currentTimeMillis();
 		return (conn != null);
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
 			System.err.print("[AreaGuard]:  Can't Disconnect, " + e.getMessage());
 		}
 	}
 
 	@Override
 	public Area getArea(int areaId) {
 		return new SqlArea(this, areaId);
 	}
 
 	@Override
 	public ArrayList<Area> getAreas() {
 		ArrayList<Area> areas = new ArrayList<Area>();
 		String sql = "SELECT Id FROM `" + tablePrefix + "Areas`";
 
 		connect();
 		if (conn != null) {
 			try {
 				PreparedStatement ps = conn.prepareStatement(sql);
 				ps.execute();
 				
 				
 				
 				// Get the result
 				ResultSet rs = ps.getResultSet();
 				while (rs.next()) areas.add(new SqlArea(this, rs.getInt(1)));
 
 				// Close events
 				rs.close();
 				ps.close();
 
 			} catch (SQLException e) {
 				e.printStackTrace();
 			}
 
 			disconnect();
 		}
 		return areas;
 	}
 
 	@Override
 	public ArrayList<Area> getAreas(String name) {
 		ArrayList<Area> areas = new ArrayList<Area>();
 		String sql = "SELECT Id FROM `" + tablePrefix + "Areas` WHERE UPPER(Name)=UPPER(?)";
 
 		connect();
 		if (conn != null) {
 			try {
 				PreparedStatement ps = conn.prepareStatement(sql);
 				ps.setString(1, name);
 				ps.execute();
 				
 				
 				
 				// Get the result
 				ResultSet rs = ps.getResultSet();
 				while (rs.next()) areas.add(new SqlArea(this, rs.getInt(1)));
 
 				// Close events
 				rs.close();
 				ps.close();
 
 			} catch (SQLException e) {
 				e.printStackTrace();
 			}
 
 			disconnect();
 		}
 		return areas;
 	}
 
 	@Override
 	public ArrayList<Area> getAreas(String name, String owner) {
 		ArrayList<Area> areas = new ArrayList<Area>();
 		for (Area area : getAreas(name)) 
 			if (area.isOwner(owner))
 				areas.add(area);
 		return areas;
 	}
 
 	@Override
 	public ArrayList<Area> getAreasOwned(String owner) {
 		ArrayList<Area> areas = new ArrayList<Area>();
 		String sql = "SELECT AreaId FROM `" + tablePrefix + "Lists` WHERE Name=? AND Value=?";
 
 		connect();
 		if (conn != null) {
 			try {
 				PreparedStatement ps = conn.prepareStatement(sql);
 				ps.setString(1, "owners");
 				ps.setString(2, owner.toLowerCase());
 				ps.execute();
 				
 				// Get the result
 				ResultSet rs = ps.getResultSet();
 				while (rs.next()) areas.add(new SqlArea(this, rs.getInt(1)));
 
 				// Close events
 				rs.close();
 				ps.close();
 
 			} catch (SQLException e) {
 				e.printStackTrace();
 			}
 
 			disconnect();
 		}
 		return areas;
 	}
 
 	@Override
 	public World getWorld(int worldId) {
 		return new SqlWorld(this, worldId);
 	}
 
 	@Override
 	public World getWorld(String name) {
 		World world = null;
 		String sql = "SELECT Id FROM `" + tablePrefix + "Worlds` WHERE Name = ?";
 
 		connect();
 		if (conn != null) {
 			try {
 				PreparedStatement ps = conn.prepareStatement(sql);
 				ps.setString(1, name);
 				ps.execute();
 				
 				
 				
 				// Get the result
 				ResultSet rs = ps.getResultSet();
 				if (rs.next()) world = new SqlWorld(this, rs.getInt(1));
 				else world = newWorld(name);
 
 				// Close events
 				rs.close();
 				ps.close();
 
 			} catch (SQLException e) {
 				e.printStackTrace();
 				world = new ErrorWorld(name);
 			}
 
 			disconnect();
 		} else world = new ErrorWorld(name);
 		return world;
 	}
 	
 	public World newWorld(String name) {
 		World world = null;
 
 		String insert = "INSERT INTO `" + tablePrefix + "Worlds`"
 				+ "(Name)"
 				+ "VALUES (?);";
 		connect();
 		if (conn == null)
 			return new ErrorWorld(name);
 		try {
 			PreparedStatement ps = conn.prepareStatement(insert);
 			ps.setString(1, name);
 			ps.execute();
 
 			Statement st = conn.createStatement();
 
 			ResultSet rs = st.executeQuery("SELECT LAST_INSERT_"
 					+ (url.toLowerCase().contains("sqlite") ? "ROW" : "")
 					+ "ID();");
 			if (rs.next()) world = new SqlWorld(this, rs.getInt(1));
 			rs.close();
 			ps.close();
 		} catch (SQLException e) {
 			e.printStackTrace();
 			world = new ErrorWorld(name);
 		}
 		disconnect();
 		return world;
 	}
 
 	@Override
 	public ArrayList<World> getWorlds() {
 		ArrayList<World> worlds = new ArrayList<World>();
 		String sql = "SELECT Id FROM `" + tablePrefix + "Worlds`";
 
 		connect();
 		if (conn != null) {
 			try {
 				PreparedStatement ps = conn.prepareStatement(sql);
 				ps.execute();
 				
 				
 				
 				// Get the result
 				ResultSet rs = ps.getResultSet();
 				while (rs.next()) worlds.add(new SqlWorld(this, rs.getInt(1)));
 
 				// Close events
 				rs.close();
 				ps.close();
 
 			} catch (SQLException e) {
 				e.printStackTrace();
 			}
 
 			disconnect();
 		}
 		return worlds;
 	}
 
 	@Override
 	public Area newArea(String creator, String name) {
 		Area area = null;
 
 		String insert = "INSERT INTO `" + tablePrefix + "Areas`"
 				+ "(Creator, Name)"
 				+ "VALUES (?, ?);";
 		connect();
 		
 		if (conn == null)
 			return new ErrorArea("Faild to connect to Sql Database");
 		try {
 			PreparedStatement ps = conn.prepareStatement(insert);
 			ps.setString(1, creator.toLowerCase());
 			ps.setString(2, name);
 			ps.execute();
 
 			Statement st = conn.createStatement();
 
 			ResultSet rs = st.executeQuery("SELECT LAST_INSERT_"
 					+ (url.toLowerCase().contains("sqlite") ? "ROW" : "")
 					+ "ID();");
 			if (rs.next()) area = new SqlArea(this, rs.getInt(1));
 			rs.close();
 			ps.close();
 		} catch (SQLException e) {
 			e.printStackTrace();
 			area = new ErrorArea("Faild to add area to Sql Database");
 		}
 		disconnect();
 		return area;
 	}
 
 	@Override
 	public Cuboid newCuboid(String creator, Area area, World world, int[] coords) {
 		Cuboid cubiod = null;
 
 		String insert = "INSERT INTO `" + tablePrefix + "Cuboids`"
 				+ "(Creator, AreaId, WorldId, x1, y1, z1, x2, y2, z2)"
 				+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?);";
 		connect();
 		
 		if (conn == null)
 			return new ErrorCuboid();
 		try {
 			PreparedStatement ps = conn.prepareStatement(insert);
 			ps.setString(1, creator.toLowerCase());
 			ps.setInt(2, area.getId());
 			ps.setInt(3, world.getId());
 			for (int i = 0; i < 6; i++)
 				if (coords != null && i < coords.length)
 					ps.setInt(4 + i, coords[i]);
 			ps.execute();
 
 			Statement st = conn.createStatement();
 
 			ResultSet rs = st.executeQuery("SELECT LAST_INSERT_"
 					+ (url.toLowerCase().contains("sqlite") ? "ROW" : "")
 					+ "ID();");
 			if (rs.next()) cubiod = new SqlCuboid(this, rs.getInt(1));
 			rs.close();
 			ps.close();
 		} catch (SQLException e) {
 			e.printStackTrace();
 			cubiod = new ErrorCuboid();
 		}
 		disconnect();
 		return cubiod;
 	}
 
 	@Override
 	public String getInfo() {
 		return "Sql " + url;
 	}
 	
 	@Override
 	public Cuboid getCuboid(int areaId) {
 		return new SqlCuboid(this, areaId);
 	}
 	
 	public boolean executeSqlScript(File file) {
 		if (!file.exists()) {
 			System.err.println("[AreaGuard] Error running sql script: could not find the file \"" + file.getName() + "\"");
 			return false; }
 		if (!file.canRead()) {
 			System.err.println("[AreaGuard] Error running sql script: do not have permition to read the file \"" + file.getName() + "\"");
 			return false; }
 		
 		boolean ret = false;
 		
 		try {
 			FileInputStream fis = new FileInputStream(file);
 			ret = executeSqlScript(fis);
 			fis.close();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		return ret;
 	}
 	
 	public boolean executeSqlScript(InputStream is) {
 		if (is == null) {
 			System.err.println("[AreaGuard] Error running sql script: InputStream is null.");
 			return false; }
 		
 		String calls[] = new String[0];
 		
 		// Convert
 		String data = "";
 		byte buffer[] = new byte[1];
 		try {
 			while (is.read(buffer) != -1) {
 				data +=  new String(buffer);
 			}
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		calls = data.replaceAll("<tablePrefix>", tablePrefix).split(";");
 		
 		connect();
 		
 		if (conn == null) {
 			System.err.println("[AreaGuard] Error running sql script: could not connect to the database.");
 			return false; }
 		try {
 			for (String call : calls) {
 				if (call.trim().isEmpty()) continue;
 				// System.out.println(call);
 				PreparedStatement ps = conn.prepareStatement(call);
 				ps.execute();
 				ps.close();
 			}
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		disconnect();
 		
 		return false;
 	}
 
 	@Override
 	public ArrayList<Area> getAreasCreated(String creator) {
 		ArrayList<Area> areas = new ArrayList<Area>();
 		String sql = "SELECT Id FROM `" + tablePrefix + "Areas` WHERE Creator=?";
 
 		connect();
 		if (conn != null) {
 			try {
 				PreparedStatement ps = conn.prepareStatement(sql);
 				ps.setString(1, creator.toLowerCase());
 				ps.execute();
 				
 				// Get the result
 				ResultSet rs = ps.getResultSet();
 				while (rs.next()) areas.add(new SqlArea(this, rs.getInt(1)));
 
 				// Close events
 				rs.close();
 				ps.close();
 
 			} catch (SQLException e) {
 				e.printStackTrace();
 			}
 
 			disconnect();
 		}
 		return areas;
 	}
 
 	@Override
 	public ArrayList<Cuboid> getCuboidsCreated(String creator) {
 		ArrayList<Cuboid> cuboids = new ArrayList<Cuboid>();
 		String sql = "SELECT Id FROM `" + tablePrefix + "Cuboids` WHERE Creator=?";
 
 		connect();
 		if (conn != null) {
 			try {
 				PreparedStatement ps = conn.prepareStatement(sql);
 				ps.setString(1, creator.toLowerCase());
 				ps.execute();
 				
 				// Get the result
 				ResultSet rs = ps.getResultSet();
 				while (rs.next()) cuboids.add(new SqlCuboid(this, rs.getInt(1)));
 
 				// Close events
 				rs.close();
 				ps.close();
 
 			} catch (SQLException e) {
 				e.printStackTrace();
 			}
 
 			disconnect();
 		}
 		return cuboids;
 	}
 }
