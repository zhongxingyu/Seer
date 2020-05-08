 package nl.giantit.minecraft.database.drivers.mysql;
 
 import nl.giantit.minecraft.database.Driver;
 import nl.giantit.minecraft.database.DatabaseType;
 import nl.giantit.minecraft.database.query.Query;
 import nl.giantit.minecraft.database.QueryResult;
 import nl.giantit.minecraft.database.query.AlterQuery;
 import nl.giantit.minecraft.database.query.CreateQuery;
 import nl.giantit.minecraft.database.query.DeleteQuery;
 import nl.giantit.minecraft.database.query.DropQuery;
 import nl.giantit.minecraft.database.query.IndexQuery;
 import nl.giantit.minecraft.database.query.InsertQuery;
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
 
 /**
  *
  * @author Giant
  */
 public class MySQLDriver implements Driver {
 	
 	private final DatabaseType type = DatabaseType.MySQL;
 	
 	private final static HashMap<String, MySQLDriver> instance = new HashMap<String, MySQLDriver>();
 	private final Plugin plugin;
 	
 	private final ArrayList<HashMap<String, String>> sql = new ArrayList<HashMap<String, String>>();
 	
 	private final String db, host, port, user, pass, prefix;
 	private Connection con = null;
 	private boolean dbg = false;
 	
 	private boolean parseBool(String s, boolean d) {
 		if(s.equalsIgnoreCase("1") || s.equalsIgnoreCase("y") || s.equalsIgnoreCase("yes") || s.equalsIgnoreCase("true"))
 			return true;
 		
 		return d;
 	}
 	
 	private void connect() {
 		String dbPath = "jdbc:mysql://" + this.host + ":" + this.port + "/" + this.db + "?user=" + this.user + "&password=" + this.pass;
 		try{
 			Class.forName("com.mysql.jdbc.Driver");
 			this.con = DriverManager.getConnection(dbPath);
 		}catch(SQLException e) {
 			plugin.getLogger().log(Level.SEVERE, "Failed to connect to database: SQL error!");
 			if(this.dbg) {
 				plugin.getLogger().log(Level.INFO, e.getMessage(), e);
 			}
 		}catch(ClassNotFoundException e) {
 			plugin.getLogger().log(Level.SEVERE, "Failed to connect to database: MySQL library not found!");
 			if(this.dbg) {
 				plugin.getLogger().log(Level.INFO, e.getMessage(), e);
 			}
 		}
 	}
 	
 	private MySQLDriver(Plugin p, HashMap<String, String> c) {
 		this.plugin = p;
 		
 		this.db = c.get("database");
 		this.host = c.get("host");
 		this.port = String.valueOf(c.get("port"));
 		this.user = c.get("user");
 		this.pass = c.get("password");
 		this.prefix = c.get("prefix");
 		this.dbg = (c.containsKey("debug")) ? this.parseBool(c.get("debug"), false) : false;
 		this.connect();
 	}
 	
 	@Override
 	public void close() {
 		try {
 			if(null == con || !con.isClosed() || !con.isValid(0))
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
 			res = data.getTables(null, null, table, null);
 
 			return res.next();
 		}catch (NullPointerException e) {
 			plugin.getLogger().log(Level.SEVERE, "Could not load table " + table);
 			if(this.dbg) {
 				plugin.getLogger().log(Level.INFO, e.getMessage(), e);
 			}
             return false;
 		}catch (SQLException e) {
 			plugin.getLogger().log(Level.SEVERE, "Could not load table " + table);
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
 				plugin.getLogger().log(Level.SEVERE, "Could not close result connection to database");
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
 		SelectQuery sQ = new MySQLSelectQuery(this);
 		return sQ.select(fields);
 	}
 	
 	@Override
 	public SelectQuery select(List<String> fields) {
 		SelectQuery sQ = new MySQLSelectQuery(this);
 		return sQ.select(fields);
 	}
 	
 	@Override
 	public SelectQuery select(Map<String, String> fields) {
 		SelectQuery sQ = new MySQLSelectQuery(this);
 		return sQ.select(fields);
 	}
 	
 	@Override
 	public UpdateQuery update(String table) {
 		UpdateQuery uQ = new MySQLUpdateQuery(this);
 		uQ.setTable(table);
 		
 		return uQ;
 	}
 	
 	@Override
 	public InsertQuery insert(String table) {
 		return new MySQLInsertQuery(this).into(table);
 	}
 	
 	@Override
 	public DeleteQuery delete(String table) {
 		return new MySQLDeleteQuery(this).from(table);
 	}
 	
 	@Override
 	public TruncateQuery Truncate(String table) {
 		return new MySQLTruncateQuery(this).setTable(table);
 	}
 	
 	@Override
 	public CreateQuery create(String table) {
 		return new MySQLCreateQuery(this).create(table);
 	}
 	
 	@Override
 	public IndexQuery createIndex(String index) {
 		return new MySQLIndexQuery(this).setName(index);
 	}
 	
 	@Override
 	public AlterQuery alter(String table) {
 		return new MySQLAlterQuery(this).setTable(table);
 	}
 	
 	@Override
 	public DropQuery drop(String table) {
 		return new MySQLDropQuery(this).setTable(table);
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
 				return new MySQLSelectQuery(this);
 			case UPDATE:
 				return new MySQLUpdateQuery(this);
 			case INSERT:
 				return new MySQLInsertQuery(this);
 			case DELETE:
 				return new MySQLDeleteQuery(this);
 			case TRUNCATE:
 				return new MySQLTruncateQuery(this);
 			case CREATE:
 				return new MySQLCreateQuery(this);
 			case INDEX:
 				return new MySQLIndexQuery(this);
 			case ALTER:
 				return new MySQLAlterQuery(this);
 			case DROP:
 				return new MySQLDropQuery(this);
 		}
 		
 		return null;
 	}
 	
 	@Override
 	public Plugin getPlugin() {
 		return this.plugin;
 	}
 	
 	public static MySQLDriver Obtain(Plugin p, HashMap<String, String> conf, String instance) {
 		if(!MySQLDriver.instance.containsKey(instance))
 			MySQLDriver.instance.put(instance, new MySQLDriver(p, conf));
 		
 		return MySQLDriver.instance.get(instance);
 	}
 }
