 package bitlegend.bitauth;
 
 import bitlegend.bitauth.commands.Chpasswd;
 import bitlegend.bitauth.commands.Ipcheck;
 import bitlegend.bitauth.commands.Login;
 import bitlegend.bitauth.commands.Logout;
 import bitlegend.bitauth.commands.Pwreset;
 import bitlegend.bitauth.commands.Register;
 import bitlegend.bitauth.commands.Unregister;
 import bitlegend.bitauth.commands.Whitelist;
 import bitlegend.bitauth.listeners.*;
 
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.logging.Logger;
 
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class BitAuth extends JavaPlugin {
 	public PluginDescriptionFile pluginInfo = this.getDescription();
 	public Config config = new Config(this);
 	public Logger log = Logger.getLogger("Minecraft"); // broken, causes nullPointerException
 	public String enableOnStart = "Enabled On Startup";
 	public boolean enabled;
 	
 	public List<Player> unregistered;
 	public List<Player> requireLogin;
 	public List<Player> loggedIn;
 	public List<Player> pwreset;
 	
 	private final BAPlayerListener playerListener = new BAPlayerListener(this);
 	private final BABlockListener blockListener = new BABlockListener(this);
 	
 	@Override
 	public void onDisable() {
 		
 	}
 	
 	@Override
 	public void onEnable() {
		// Check the configuration
		config.checkConfig();
		
 		// Check if config.yml was modified
 		if (!config.readString("DB_Host").equals("hostname")) {
 			if (tablesExist() != true) { // Check if tables exist
 				logInfo("Generating tables `"
 						+ config.readString("DB_Table_login") + "` and `"
 						+ config.readString("DB_Table_whitelist") + "`");
 				generateTables();
 			} else {
 				logInfo("Database and tables found.");
 			}
 		}
 		
 		// Create variables
 		PluginManager pm = getServer().getPluginManager();
 		unregistered = new ArrayList<Player>();
 		requireLogin = new ArrayList<Player>();
 		loggedIn = new ArrayList<Player>();
 		pwreset = new ArrayList<Player>();
 		
		// Check if whitelist is enabled
 		if (config.readBoolean("Use_Whitelist") == true)
 			logInfo("Starting with whitelist enabled");
 		else
 			logInfo("Starting with whitelist disabled");
 		
 		// Register events
 		pm.registerEvents(this.playerListener, this);
 		pm.registerEvents(this.blockListener, this);
 		
 		// Register commands
 		getCommand("register").setExecutor(new Register(this));
 		getCommand("login").setExecutor(new Login(this));
 		getCommand("ipcheck").setExecutor(new Ipcheck(this));
 		getCommand("whitelist").setExecutor(new Whitelist(this));
 		getCommand("logout").setExecutor(new Logout(this));
 		getCommand("unregister").setExecutor(new Unregister(this));
 		getCommand("chpasswd").setExecutor(new Chpasswd(this));
 		getCommand("pwreset").setExecutor(new Pwreset(this));
 	}
 	
 	public String byteToString(byte[] input) {
 		StringBuilder sb = new StringBuilder();
 		for (int i = 0; i < input.length; i++)
 			sb.append(input[i]);
 		return sb.toString();
 	}
 	
 	public long ipToLong(String ipString) {
 		String[] ipSplit = ipString.split("\\.");
 		long ip = 0;
 		for (int i = 0; i < ipSplit.length; i++) {
 			int power = 3-i;
 			ip += ((Integer.parseInt(ipSplit[i]) % 256 * Math.pow(256, power)));
 		}
 		return ip;
 	}
 	
 	public void logInfo(String info) {
 		System.out.println("[BitAuth] " + info);
 	}
 	
 	private boolean tablesExist() {
 		boolean r = false, login = false, whitelist = false;
 		
 		String user = config.readString("DB_User");
 		String pass = config.readString("DB_Pass");
 		String url = "jdbc:mysql://" + config.readString("DB_Host") + 
 			"/" + config.readString("DB_Name");
 		String logintable = config.readString("DB_Table_login");
 		String wltable = config.readString("DB_Table_whitelist");
 		
 		try {
 			Connection conn = DriverManager.getConnection(url, user, pass);
 			Statement select = conn.createStatement();
 			ResultSet result = select.executeQuery("SHOW TABLES");
 			
 			if (result.next()) { // Results found
 				do {
 					String table = result.getString(1);
 					if (table.equals(logintable))
 						login = true;
 					if (table.equals(wltable))
 						whitelist = true;
 				} while (result.next());
 			}
 		} catch (SQLException se) {
 			se.printStackTrace();
 		}
 		
 		if (login == true && whitelist == true)
 			r = true;
 		
 		return r;
 	}
 	
 	public void generateTables() {
 		String user = config.readString("DB_User");
 		String pass = config.readString("DB_Pass");
 		String url = "jdbc:mysql://" + config.readString("DB_Host") + 
 			"/" + config.readString("DB_Name");
 		String logintable = config.readString("DB_Table_login");
 		String wltable = config.readString("DB_Table_whitelist");
 		
 		String queryDropLogin = "DROP TABLE IF EXISTS `" + logintable + "`";
 		String queryCreateLogin = "CREATE TABLE `" + logintable + "` (" +
 				"`username` text NOT NULL," +
 				"`salt` blob NOT NULL," +
 				"`password` blob NOT NULL," +
 				"`whitelist` tinyint(1) NOT NULL," +
 				"`lastlogintime` bigint(20) NOT NULL," +
 				"`ipaddress` bigint(20) NOT NULL," +
 				"`enableipcheck` tinyint(1) NOT NULL," +
 				"`pwreset` tinyint(4) NOT NULL," +
 				"`temppwd` blob," +
 				"`tmpsalt` blob" +
 				") ENGINE=InnoDB DEFAULT CHARSET=utf8";
 		String queryDropWhitelist = "DROP TABLE IF EXISTS `" + wltable +"`";
 		String queryCreateWhitelist = "CREATE TABLE `" + wltable + "` (" +
 				"`username` text NOT NULL," +
 				"`enabled` tinyint(4) NOT NULL" +
 				") ENGINE=InnoDB DEFAULT CHARSET=utf8";
 		
 		try {
 			Connection conn = DriverManager.getConnection(url, user, pass);
 			Statement query = conn.createStatement();
 			
 			query.executeUpdate(queryDropLogin);
 			query.executeUpdate(queryCreateLogin);
 			query.executeUpdate(queryDropWhitelist);
 			query.executeUpdate(queryCreateWhitelist);
 			
 			query.close();
 			conn.close();
 		} catch (SQLException se) {
 			se.printStackTrace();
 		}
 	}
 }
