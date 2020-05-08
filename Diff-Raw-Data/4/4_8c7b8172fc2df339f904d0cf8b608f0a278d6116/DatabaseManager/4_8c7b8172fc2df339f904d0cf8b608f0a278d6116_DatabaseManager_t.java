 package uk.badger.bConomy.config;
 
 import java.sql.ResultSet;
 import java.sql.SQLException;
 
 import org.bukkit.OfflinePlayer;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import uk.badger.bConomy.Global;
 import uk.badger.bConomy.account.Account;
 
 import n3wton.me.BukkitDatabaseManager.BukkitDatabaseManager;
 import n3wton.me.BukkitDatabaseManager.BukkitDatabaseManager.DatabaseType;
 
 public class DatabaseManager {
 	
 	/** 
 	 * Sets up the database and loads in all the infomation in the table
 	 * 
 	 * @param plugin - the java plugin used to setup the database, used to get the offline player
 	 */
 	public static void setupDatabase(JavaPlugin plugin) {
 		
 		// creates the database instance
 		Global.m_database = BukkitDatabaseManager.CreateDatabase(Config.m_dbInfo.dbname, Global.getPlugin(), DatabaseType.SQL);
 		// logs in using values from the config
 		Global.m_database.login(Config.m_dbInfo.host, Config.m_dbInfo.user, Config.m_dbInfo.password, Config.m_dbInfo.port);
 	
 		if (!Global.m_database.TableExists("accounts")) {
 			
 			Global.outputToConsole("Could not find 'accounts' table, creating default now.");
 			
 			// creates the accounts table
 			String query = "CREATE TABLE accounts (" +
							"id INT," +
							"name VARCHAR(64)," +
 							"balance DOUBLE" +
 							");";
 			
 			Global.m_database.Query(query, true);
 		}
 		
 		String query = "SELECT * FROM accounts";
 		ResultSet result = Global.m_database.QueryResult(query);
 		
 		if (result == null)
 			return;
 		
 		// load in the accounts
 		try {
 			while(result.next()) {
 				int id = result.getInt("id");
 				String name = result.getString("name");
 				OfflinePlayer player = plugin.getServer().getOfflinePlayer(name);
 				double balance = result.getDouble("balance");
 				
 				// create the account and then add it to the array
 				Account account = new Account(id, player, balance);
 				Global.addAccout(account);
 			}
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 	}
 
 	public static void addAccount(Account account) {
 		
 		if (account == null)
 			return;
 		
 		String query = "INSET INTO accounts (" +
 				"'id', 'name', 'balance') VALUES" +
 				"'" + account.getId() + "', " +
 				"'" + account.getPlayer().getName() + ", " +
 				"'" + account.getBalance() + "'" +
 				");";
 		
 		Global.m_database.Query(query, true);
 		
 		Global.outputToConsole("Account " + account.getId() + " has been added to the database.");
 	}
 	
 	public static void updateAccount(Account account){
 		
 		if (account == null)
 			return;
 		
 		String query = "SELECT * FROM accounts WHERE name='"+ account.getPlayer().getName() + "';";
 		ResultSet result = Global.m_database.QueryResult(query);
 		
 		try {
 			if (result.next() == false)
 				return;
 		} catch (SQLException ex) {
 			ex.printStackTrace();
 			return;
 		}
 		
 		query = "UPDATE accounts (" +
 				 "SET name='" + account.getPlayer().getName() + ", " +
 				 "balance='" + account.getBalance() + "'" +
 				 "WHERE id='" + account.getId() +
 				 ";";
 		
 		Global.m_database.Query(query);
  	}
 	
 	public static void removeAccount(Account account) {
 		
 		if (account == null)
 			return;
 		
 		String query = "DELETE * FROM accounts WHERE " +
 						"id=" + account.getId() + " AND" +
 						"name='" + account.getPlayer().getName() + "'" +
 						";";
 		
 		Global.m_database.Query(query);
 	}
 	
 	public static void executeQuery(String query) {
 		Global.m_database.Query(query);
 	}
 }
