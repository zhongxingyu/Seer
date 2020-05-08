 package nl.giantit.minecraft.database.drivers.h2;
 
 import nl.giantit.minecraft.database.DatabaseType;
 import nl.giantit.minecraft.database.Driver;
 import nl.giantit.minecraft.database.QueryResult;
 import nl.giantit.minecraft.database.query.AlterQuery;
 import nl.giantit.minecraft.database.query.CreateQuery;
 import nl.giantit.minecraft.database.query.DeleteQuery;
 import nl.giantit.minecraft.database.query.DropQuery;
 import nl.giantit.minecraft.database.query.IndexQuery;
 import nl.giantit.minecraft.database.query.InsertQuery;
 import nl.giantit.minecraft.database.query.Query;
 import nl.giantit.minecraft.database.query.SelectQuery;
 import nl.giantit.minecraft.database.query.TruncateQuery;
 import nl.giantit.minecraft.database.query.UpdateQuery;
 
 import org.bukkit.plugin.Plugin;
 
 import java.sql.Connection;
 import java.sql.DatabaseMetaData;
 import java.sql.DriverManager;
 import java.sql.ResultSet;
 import java.sql.ResultSetMetaData;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.logging.Level;
 
 public class H2Driver implements Driver {
 	
 	private DatabaseType type = DatabaseType.hTwo;
 	
 	private static HashMap<String, H2Driver> instance = new HashMap<String, H2Driver>();
 	private Plugin plugin;
 	
 	private ArrayList<HashMap<String, String>> sql = new ArrayList<HashMap<String, String>>();
 	
 	private String db, user, pass, prefix;
 	private Connection con = null;
 	private boolean dbg = false;
 	
 	private boolean parseBool(String s, boolean d) {
 		if(s.equalsIgnoreCase("1") || s.equalsIgnoreCase("y") || s.equalsIgnoreCase("yes") || s.equalsIgnoreCase("true"))
 			return true;
 		
 		return d;
 	}
 
 	private H2Driver(Plugin p, HashMap<String, String> c) {
 		plugin = p;
 		
 		this.db = c.get("database");
 		this.prefix = c.get("prefix");
 		this.user = c.get("user");
 		this.pass = c.get("password");
 		this.dbg = (c.containsKey("debug")) ? this.parseBool(c.get("debug"), false) : false;
 
 		String dbPath = "jdbc:h2:" + plugin.getDataFolder() + java.io.File.separator + this.db;
 		try{
 			Class.forName("org.h2.Driver");
 			this.con = DriverManager.getConnection(dbPath, this.user, this.pass);
 		}catch(SQLException e) {
 			plugin.getLogger().log(Level.SEVERE, "Failed to connect to database: SQL error!");
 			if(this.dbg) {
 				plugin.getLogger().log(Level.INFO, e.getMessage(), e);
 				
 			}
 		}catch(ClassNotFoundException e) {
 			plugin.getLogger().log(Level.SEVERE, "Failed to connect to database: h2 library not found!");
 			if(this.dbg) {
 				plugin.getLogger().log(Level.INFO, e.getMessage(), e);
 				
 			}
 		}
 	}
 
 	@Override
 	public void close() {
 		try {
 			if(con.isClosed())
 				return;
 			
 			this.con.close();
 		}catch(SQLException e) {
 			//ignore
 		}
 	}
 	
 	@Override
 	public boolean isConnected() {
 		try {
 			return con != null && !con.isClosed() && con.isValid(0);
 		}catch(SQLException e) {
 			return false;
 		}
 	}
 
 	@Override
 	public boolean tableExists(String table) {
 		ResultSet res = null;
 		table = table.replace("#__", prefix);
 		
 		try {
 			DatabaseMetaData data = this.con.getMetaData();
 			res = data.getTables(null, null, table.toUpperCase(), null);
 
 			return res.next();
 		}catch (SQLException e) {
 			plugin.getLogger().log(Level.SEVERE, " Could not load table " + table);
 			if(this.dbg) {
 				plugin.getLogger().log(Level.INFO, e.getMessage(), e);
 				
 			}
             return false;
 		} finally {
 			try {
 				if(res != null) {
 					res.close();
 				}
 			}catch (Exception e) {
 				plugin.getLogger().log(Level.SEVERE, " Could not close result connection to database");
 				if(this.dbg) {
 					plugin.getLogger().log(Level.INFO, e.getMessage(), e);
 					
 				}
 				return false;
 			}
 		}
 	}
 	
 	@Override
 	public QueryResult execQuery(Query q) {
 		Statement st = null;
 		
 		ArrayList<HashMap<String, String>> data = new ArrayList<HashMap<String, String>>();
 		
 		if(!q.isParsed()) {
 			q.parse();
 		}
 		
 		try {
 			st = con.createStatement();
 			ResultSet res = st.executeQuery(q.getParsedQuery());
 			while(res.next()) {
 				HashMap<String, String> row = new HashMap<String, String>();
 
 				ResultSetMetaData rsmd = res.getMetaData();
 				int columns = rsmd.getColumnCount();
 				for(int i = 1; i < columns + 1; i++) {
 					row.put(rsmd.getColumnName(i).toLowerCase(), res.getString(i));
 				}
 				data.add(row);
 			}
 		}catch (SQLException e) {
 			plugin.getLogger().log(Level.SEVERE, " Could not execute query!");
 			if(this.dbg) {
 				plugin.getLogger().log(Level.INFO, e.getMessage(), e);
 			}
 		} finally {
 			try {
 				if(st != null) {
 					st.close();
 				}
 			}catch (Exception e) {
 				plugin.getLogger().log(Level.SEVERE, " Could not close database connection");
 				if(this.dbg) {
 					plugin.getLogger().log(Level.INFO, e.getMessage(), e);
 				}
 			}
 		}
 		
 		return QueryResult.QR(data);
 	}
 	
 	@Override
 	public QueryResult updateQuery(Query q) {
 		Statement st = null;
 		
 		if(!q.isParsed()) {
 			q.parse();
 		}
 		
 		try {
 			st = con.createStatement();
 			st.executeUpdate(q.getParsedQuery());
 		}catch (SQLException e) {
 			plugin.getLogger().log(Level.SEVERE, " Could not execute query!");
 			if(this.dbg) {
 				plugin.getLogger().log(Level.INFO, e.getMessage(), e);
 			}
 		} finally {
 			try {
 				if(st != null) {
 					st.close();
 				}
 			}catch (Exception e) {
 				plugin.getLogger().log(Level.SEVERE, " Could not close database connection");
 				if(this.dbg) {
 					plugin.getLogger().log(Level.INFO, e.getMessage(), e);
 				}
 			}
 		}
 		
 		return QueryResult.QR();
 	}
 	
 	@Override
 	public SelectQuery select(String... fields) {
 		SelectQuery sQ = new H2SelectQuery(this);
 		return sQ.select(fields);
 	}
 	
 	@Override
 	public SelectQuery select(List<String> fields) {
 		SelectQuery sQ = new H2SelectQuery(this);
 		return sQ.select(fields);
 	}
 	
 	@Override
 	public SelectQuery select(Map<String, String> fields) {
 		SelectQuery sQ = new H2SelectQuery(this);
 		return sQ.select(fields);
 	}
 	
 	@Override
 	public UpdateQuery update(String table) {
 		return new H2UpdateQuery(this).setTable(table);
 	}
 	
 	@Override
 	public InsertQuery insert(String table) {
 		return new H2InsertQuery(this).into(table);
 	}
 	
 	@Override
 	public DeleteQuery delete(String table) {
 		return new H2DeleteQuery(this).from(table);
 	}
 	
 	@Override
 	public TruncateQuery Truncate(String table) {
 		return new H2TruncateQuery(this).setTable(table);
 	}
 	
 	@Override
 	public CreateQuery create(String table) {
 		return new H2CreateQuery(this).create(table);
 	}
 	
 	@Override
 	public IndexQuery createIndex(String index) {
 		return new H2IndexQuery(this).setName(index);
 	}
 	
 	@Override
 	public AlterQuery alter(String table) {
 		return new H2AlterQuery(this).setTable(table);
 	}
 	
 	@Override
 	public DropQuery drop(String table) {
 		return new H2DropQuery(this).setTable(table);
 	}
 	
 	@Override
 	public DatabaseType getType() {
 		return this.type;
 	}
 	
 	@Override
 	public String getPrefix() {
 		return this.prefix;
 	}
 	
 	@Override
 	public Query create(Type t) {
 		switch(t) {
 			case SELECT:
 				return new H2SelectQuery(this);
 			case UPDATE:
 				return new H2UpdateQuery(this);
 			case INSERT:
 				return new H2InsertQuery(this);
 			case DELETE:
 				return new H2DeleteQuery(this);
 			case TRUNCATE:
 				return new H2TruncateQuery(this);
 			case CREATE:
 				return new H2CreateQuery(this);
 			case INDEX:
 				return new H2IndexQuery(this);
 			case ALTER:
 				return new H2AlterQuery(this);
 			case DROP:
 				return new H2DropQuery(this);
 		}
 		
 		return null;
 	}
 	
 	@Override
 	public Plugin getPlugin() {
 		return this.plugin;
 	}
 	
 	public static H2Driver Obtain(Plugin p, HashMap<String, String> conf, String instance) {
 		if(!H2Driver.instance.containsKey(instance))
 			H2Driver.instance.put(instance, new H2Driver(p, conf));
 		
 		return H2Driver.instance.get(instance);
 	}
 
 }
