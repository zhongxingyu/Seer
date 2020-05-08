 package bitlegend.bitreport;
 
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import ru.tehkode.permissions.PermissionManager;
 import ru.tehkode.permissions.bukkit.PermissionsEx;
 
 import bitlegend.bitreport.commands.*;
 import bitlegend.bitreport.listeners.*;
 
 public class BitReport extends JavaPlugin {
 	public String enableOnStart = "Enabled On Startup";
 	public boolean enabled;
 	public PermissionManager pex;
 	
	public Config config = new Config(this);
	
 	private final BRPlayerListener playerListener = new BRPlayerListener(this);
 	
 	@Override
 	public void onDisable() {
 		
 	}
 	
 	@Override
 	public void onEnable() {
 		// Check the configuration
 		config.checkConfig();
 		
 		// Get permission manager
 		pex = PermissionsEx.getPermissionManager();
 		
 		// Check if config.yml was modified
 		if (!config.readString("DB_Host").equals("hostname")) {
 			if (tableExists() != true) { // Check if tables exist
 				logInfo("Generating table `"
 						+ config.readString("DB_Table_reports") + "`");
 				generateTables();
 			} else {
 				logInfo("Database and table found.");
 			}
 		}
 
 		// Variables
 		PluginManager pm = getServer().getPluginManager();
 		
 		// Register events
 		pm.registerEvents(this.playerListener, this);
 		
 		// Register commands
 		getCommand("rclaim").setExecutor(new Rclaim(this));
 		getCommand("resolve").setExecutor(new Resolve(this));
 		getCommand("report").setExecutor(new Report(this));
 		getCommand("rlist").setExecutor(new Rlist(this));
 		getCommand("rinfo").setExecutor(new Rinfo(this));
 		getCommand("rtp").setExecutor(new Rtp(this));
 	}
 	
 	public void logInfo(String data) {
 		System.out.println("[BitReport] " + data);
 	}
 	
 	private boolean tableExists() {
 		boolean r = false, reports = false;
 		
 		String user = config.readString("DB_User");
 		String pass = config.readString("DB_Pass");
 		String url = "jdbc:mysql://" + config.readString("DB_Host") + 
 			"/" + config.readString("DB_Name");
 		String reportstable = config.readString("DB_Table_reports");
 		
 		try {
 			Connection conn = DriverManager.getConnection(url, user, pass);
 			Statement select = conn.createStatement();
 			ResultSet result = select.executeQuery("SHOW TABLES");
 			
 			if (result.next()) { // Results found
 				do {
 					String table = result.getString(1);
 					if (table.equals(reportstable))
 						reports = true;
 				} while (result.next());
 			}
 		} catch (SQLException se) {
 			se.printStackTrace();
 		}
 		
 		if (reports == true)
 			r = true;
 		
 		return r;
 	}
 	
 	public void generateTables() {
 		String user = config.readString("DB_User");
 		String pass = config.readString("DB_Pass");
 		String url = "jdbc:mysql://" + config.readString("DB_Host") + 
 			"/" + config.readString("DB_Name");
 		String reportstable = config.readString("DB_Table_reports");
 		
 		String queryDropReports = "DROP TABLE IF EXISTS `" + reportstable + "`";
 		String queryCreateReports = "CREATE TABLE IF NOT EXISTS `bit_reports` (" +
 				"`uid` int(11) NOT NULL AUTO_INCREMENT," +
 				"`username` text NOT NULL," +
 				"`status` tinyint(1) NOT NULL," +
 				"`staff` blob," +
 				"`data` text NOT NULL," +
 				"`notes` text," +
 				"`world` text NOT NULL," +
 				"`location` text NOT NULL," +
 				"PRIMARY KEY (`uid`)" +
 				") ENGINE=InnoDB DEFAULT CHARSET=utf8 AUTO_INCREMENT=1";
 
 		try {
 			Connection conn = DriverManager.getConnection(url, user, pass);
 			Statement query = conn.createStatement();
 			
 			query.executeUpdate(queryDropReports);
 			query.executeUpdate(queryCreateReports);
 			
 			query.close();
 			conn.close();
 		} catch (SQLException se) {
 			se.printStackTrace();
 		}
 	}
 }
