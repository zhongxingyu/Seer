 package regalowl.databukkit;
 
 import java.io.File;
 import java.io.UnsupportedEncodingException;
 import java.net.URLDecoder;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.Statement;
 import java.util.logging.Logger;
 
 import org.bukkit.plugin.Plugin;
 
 public class DataBukkit {
 
 	private Plugin plugin;
 	private boolean useMySql;
 	private SQLWrite sw;
 	private SQLRead sr;
 	private Logger log;
 	private YamlHandler yh;
 	
 	private String host;
 	private String database;
 	private String username;
 	private String password;
 	private int port;
 
 
 	public DataBukkit(Plugin plugin) {
 		this.plugin = plugin;
 		log = Logger.getLogger("Minecraft");
 		yh = new YamlHandler(plugin);
 		useMySql = false;
 	}
 	
 	public void enableMySQL(String host, String database, String username, String password, int port) {
 		this.host = host;
 		this.database = database;
 		this.username = username;
 		this.password = password;
 		this.port = port;
 		useMySql = true;
 	}
 	
 	
 	public void createDatabase() {
 		boolean databaseOk = false;
 		if (useMySql) {
 			databaseOk = checkMySQL();
 			if (!databaseOk) {
 				databaseOk = checkSQLLite();
 				log.severe("[DataBukkit["+plugin.getName()+"]]MySQL connection failed, defaulting to SQLite.");
 				useMySql = false;
 			}
 		} else {
 			databaseOk = checkSQLLite();
 		}
 		if (databaseOk) {
 			sw = new SQLWrite(this);
 			sr = new SQLRead(this);
 		} else {
 			log.severe("-----------------------------------------------------");
 			log.severe("[DataBukkit["+plugin.getName()+"]]Database connection failed. Disabling "+plugin.getName()+".");
 			log.severe("-----------------------------------------------------");
 			plugin.getPluginLoader().disablePlugin(plugin);
 		}
 	}
 	
 
 	
 	
 	public String getSQLitePath() {
 		return getPluginFolderPath() + plugin.getName() + ".db";
 	}
 	
 	public String getPluginFolderPath() {
 		String pluginFolder = getJarPath() + "plugins" + File.separator + plugin.getName();
 		makeFolder(pluginFolder);
 		return pluginFolder + File.separator;
 	}
 	
 	public String getErrorFilePath() {
 		return getPluginFolderPath() + "errors.log";
 	}
 
 	public String getJarPath() {
 		String path = DataBukkitPlugin.class.getProtectionDomain().getCodeSource().getLocation().getPath();
 		String serverpath = "";
 		try {
 			String decodedPath = URLDecoder.decode(path, "UTF-8");
 			serverpath = decodedPath.substring(0, decodedPath.lastIndexOf("plugins"));
 		} catch (UnsupportedEncodingException e1) {
 			e1.printStackTrace();
 		}
 		if (serverpath.startsWith("file:")) {
 			serverpath = serverpath.replace("file:", "");
 		}
 		return serverpath;
 	}
 	
 	public void makeFolder(String path) {
 		File folder = new File(path);
 		if (!folder.exists()) {
 			folder.mkdir();
 		}
 	}
 	
 	private boolean checkSQLLite() {
 		String path = getSQLitePath();
 		try {
 			Class.forName("org.sqlite.JDBC");
 			Connection connect = DriverManager.getConnection("jdbc:sqlite:" + path);
 			Statement state = connect.createStatement();
 			state.execute("DROP TABLE IF EXISTS dbtest");
 			state.execute("CREATE TABLE IF NOT EXISTS dbtest (TEST VARCHAR)");
 			state.execute("DROP TABLE IF EXISTS dbtest");
 			state.close();
 			connect.close();
 			return true;
 		} catch (Exception e) {
 			e.printStackTrace();
 			return false;
 		}
 	}
 	
 	private boolean checkMySQL() {
 		try {
 			Connection connect = DriverManager.getConnection("jdbc:mysql://" + host + ":" + port + "/" + database, username, password);
 			Statement state = connect.createStatement();
 			state.execute("DROP TABLE IF EXISTS dbtest");
			state.execute("CREATE TABLE IF NOT EXISTS dbtest (TEST VARCHAR)");
 			state.execute("DROP TABLE IF EXISTS dbtest");
 			state.close();
 			connect.close();
 			return true;
 		} catch (Exception e) {
 			return false;
 		}
 	}
 	
 	public SQLWrite getSQLWrite() {
 		return sw;
 	}
 	
 	public SQLRead getSQLRead() {
 		return sr;
 	}
 	
 	public YamlHandler getYamlHandler() {
 		return yh;
 	}
 
 	public boolean useMySQL() {
 		return useMySql;
 	}
 	
 	public void writeError(Exception e, String info) {
 		new ErrorWriter(e, info, getErrorFilePath(), plugin);
 	}
 	
 	public void shutDown() {
 		if (sw != null) {sw.shutDown();}
 		if (sr != null) {sr.shutDown();}
 		if (yh != null) {
 			yh.shutDown();
 		}
 	}
 	
 	public Plugin getPlugin() {
 		return plugin;
 	}
 	public String getHost() {
 		return host;
 	}
 	public String getDatabase() {
 		return database;
 	}
 	public String getUsername() {
 		return username;
 	}
 	public String getPassword() {
 		return password;
 	}
 	public int getPort() {
 		return port;
 	}
 }
