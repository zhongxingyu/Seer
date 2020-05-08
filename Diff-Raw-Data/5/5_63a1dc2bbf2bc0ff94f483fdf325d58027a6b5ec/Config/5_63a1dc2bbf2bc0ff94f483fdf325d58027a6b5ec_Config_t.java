 package uk.codingbadgers.bconomy.config;
 
 import org.bukkit.configuration.file.FileConfiguration;
 
 import uk.codingbadgers.bconomy.Global;
 import uk.thecodingbadgers.bDatabaseManager.bDatabaseManager.DatabaseType;
 
 public class Config {
 
 	public static class DatabaseInfo {
 		public DatabaseType driver;
 		public String host;
 		public String dbname;
 		public String tablename;
 		public String user;
 		public String password;
 		public int port = 3306;
 		public int update = 20;
 	}
 	
 	public static class Currency {
 		public String name;
 		public char symbol;
 		public String format;
 	}
 	
 	public static DatabaseInfo m_dbInfo = null;
 	public static Currency m_currency = null;
 	public static int m_startingBalance;
 	
 	public static boolean setupConfig() {
 		
 		FileConfiguration config = Global.getModule().getConfig();
 		
 		try {
 			// database config
 			config.addDefault("database.driver", "SQL");
 			config.addDefault("database.host", "localhost");
 			config.addDefault("database.dbname", "bConomy");
 			config.addDefault("database.tablename", "bConomy");
 			config.addDefault("database.user", "root");
 			config.addDefault("database.password", "");
 			config.addDefault("database.port", 3306);
 			config.addDefault("database.updateTime", 2);
 			
 			// currency info config
 			config.addDefault("currency.name", "pounds");
			config.addDefault("currency.symbol", "$");
 			config.addDefault("currency.format", "@##0.00");
 			
 			// economy config
 			config.addDefault("economy.startingBalance", 30);
 			
 			config.options().copyDefaults(true);
 		} catch (Exception ex) {
 			ex.printStackTrace();
 			return false;
 		}
 		
 		m_dbInfo = new DatabaseInfo();
 		m_dbInfo.driver = DatabaseType.valueOf(config.getString("database.driver", "SQL"));
 		m_dbInfo.host = config.getString("database.host", "localhost");
 		m_dbInfo.dbname = config.getString("database.dbname", "bConomy");
 		m_dbInfo.tablename = config.getString("database.tablename", "bConomy");
 		m_dbInfo.user = config.getString("database.user", "root");
 		m_dbInfo.password = config.getString("database.password", "");
 		m_dbInfo.port = config.getInt("database.port", 3306);
 		m_dbInfo.update = config.getInt("database.updateTime", 2);
 		
 		m_currency = new Currency();
 		m_currency.name = config.getString("currency.name", "pounds");
		m_currency.symbol = config.getString("currency.symbol", "$").toCharArray()[0];
 		m_currency.format = config.getString("currency.fomat", "@#,##0.00").replace('@', m_currency.symbol);
 		
 		m_startingBalance = config.getInt("economy.startingBalance", 30);
 		
 		Global.getPlugin().saveConfig();
 		
 		return true;
 	}
 }
