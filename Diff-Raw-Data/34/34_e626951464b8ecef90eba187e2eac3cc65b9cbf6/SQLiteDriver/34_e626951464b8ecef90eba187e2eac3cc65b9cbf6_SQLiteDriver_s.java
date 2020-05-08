 package nl.giantit.minecraft.database.drivers.sqlite;
 
 import nl.giantit.minecraft.database.Driver;
 import nl.giantit.minecraft.database.DatabaseType;
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
 
 /**
  *
  * @author Giant
  */
 public class SQLiteDriver implements Driver {
 	
 	private DatabaseType type = DatabaseType.SQLite;
 	
 	private static HashMap<String, SQLiteDriver> instance = new HashMap<String, SQLiteDriver>();
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
 	
 	private SQLiteDriver(Plugin p, HashMap<String, String> c) {
 		plugin = p;
 		
 		this.db = c.get("database");
 		this.prefix = c.get("prefix");
 		this.user = c.get("user");
 		this.pass = c.get("password");
 		this.dbg = (c.containsKey("debug")) ? this.parseBool(c.get("debug"), false) : false;
 
 		String dbPath = "jdbc:sqlite:" + plugin.getDataFolder() + java.io.File.separator + this.db + ".db";
 		try{
 			Class.forName("org.sqlite.JDBC");
 			this.con = DriverManager.getConnection(dbPath, this.user, this.pass);
 		}catch(SQLException e) {
 			plugin.getLogger().log(Level.SEVERE, "Failed to connect to database: SQL error!");
 			if(this.dbg) {
 				plugin.getLogger().log(Level.INFO, e.getMessage(), e);
 			}
 		}catch(ClassNotFoundException e) {
 			plugin.getLogger().log(Level.SEVERE, "Failed to connect to database: SQLite library not found!");
 			if(this.dbg) {
 				plugin.getLogger().log(Level.INFO, e.getMessage(), e);
 			}
 		}
 	}
 	
 	@Override
 	public void close() {
 		try {
 			if(!con.isClosed() || !con.isValid(0))
 				return;
 			
 			this.con.close();
 		}catch(SQLException e) {
 			//ignore
 		}
 	}
 	
 	@Override
 	public boolean isConnected() {
 		try {
 			return con != null && !con.isClosed();
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
	public SelectQuery select(String f) {
		SelectQuery sQ = new SQLiteSelectQuery(this);
		return sQ.select(f);
	}
	
	@Override
 	public SelectQuery select(String... fields) {
 		SelectQuery sQ = new SQLiteSelectQuery(this);
 		return sQ.select(fields);
 	}
 	
 	@Override
 	public SelectQuery select(List<String> fields) {
 		SelectQuery sQ = new SQLiteSelectQuery(this);
 		return sQ.select(fields);
 	}
 	
 	@Override
 	public SelectQuery select(Map<String, String> fields) {
 		SelectQuery sQ = new SQLiteSelectQuery(this);
 		return sQ.select(fields);
 	}
 	
 	@Override
 	public UpdateQuery update(String table) {
 		return new SQLiteUpdateQuery(this).setTable(table);
 	}
 	
 	@Override
 	public InsertQuery insert(String table) {
 		return new SQLiteInsertQuery(this).into(table);
 	}
 	
 	@Override
 	public DeleteQuery delete(String table) {
 		return new SQLiteDeleteQuery(this).from(table);
 	}
 	
 	@Override
 	public TruncateQuery Truncate(String table) {
 		return new SQLiteTruncateQuery(this).setTable(table);
 	}
 	
 	@Override
 	public CreateQuery create(String table) {
 		return new SQLiteCreateQuery(this).create(table);
 	}
 	
 	@Override
 	public IndexQuery createIndex(String index) {
 		return new SQLiteIndexQuery(this).setName(index);
 	}
 	
 	@Override
 	public AlterQuery alter(String table) {
 		return new SQLiteAlterQuery(this).setTable(table);
 	}
 	
 	@Override
 	public DropQuery drop(String table) {
 		return new SQLiteDropQuery(this).setTable(table);
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
 				return new SQLiteSelectQuery(this);
 			case UPDATE:
 				return new SQLiteUpdateQuery(this);
 			case INSERT:
 				return new SQLiteInsertQuery(this);
 			case DELETE:
 				return new SQLiteDeleteQuery(this);
 			case TRUNCATE:
 				return new SQLiteTruncateQuery(this);
 			case CREATE:
 				return new SQLiteCreateQuery(this);
 			case INDEX:
 				return new SQLiteIndexQuery(this);
 			case ALTER:
 				return new SQLiteAlterQuery(this);
 			case DROP:
 				return new SQLiteDropQuery(this);
 		}
 		
 		return null;
 	}
 	
 	@Override
 	public Plugin getPlugin() {
 		return this.plugin;
 	}
 	
 	public static SQLiteDriver Obtain(Plugin p, HashMap<String, String> conf, String instance) {
 		if(!SQLiteDriver.instance.containsKey(instance))
 			SQLiteDriver.instance.put(instance, new SQLiteDriver(p, conf));
 		
 		return SQLiteDriver.instance.get(instance);
 	}
 }
