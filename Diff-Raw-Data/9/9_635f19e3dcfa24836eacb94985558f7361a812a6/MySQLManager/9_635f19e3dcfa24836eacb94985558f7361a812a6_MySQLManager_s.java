 package net.illusiononline.EmeraldEconomy;
 
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.logging.Logger;
 
 import org.bukkit.ChatColor;
 
 import lib.PatPeter.SQLibrary.*;
 
 public class MySQLManager {
 	
 	Logger log = Logger.getLogger("Minecraft");
 	EmeraldEconomy plugin;
 	public MySQL sql;
 	
 	public MySQLManager(EmeraldEconomy plugin){
 		this.plugin = plugin;
 		sql = new MySQL(log,"[EmeraldEconomy]","localhost","3306","MineLink","minelink","1orangekiwimja");
 		this.Setup();
 	}
 	
 	public MySQL getSQL(){return sql;}
 	
	public Boolean newUnit(String name, Boolean isfaction){
 		try {
 			sql.open();
		    Integer faction = 0;
		    if (isfaction == true)
		    	faction = 1;
 			if (sql.checkTable("Economy_Data")){
 				//Check for existing Unit
 				try {
 					ResultSet result = sql.query("SELECT * FROM Economy_Data WHERE name='"+name+"'");
 					if (result.next()) {
 						result.close();
 						sql.close();
 						return false;
 					}
 				} catch (SQLException ex) {
 					log.severe("[EmeraldEconomy] "+ex);
 					sql.close();
 					return false;
 				}
 				sql.query("INSERT INTO Economy_Data (name,money,faction) " +
						  "VALUES ('"+name+"',0,"+faction+");");
 				sql.close();
 				return true;
 			}
 			sql.close();
 		} catch (SQLException e) {
 			log.info(e.getMessage());
 		}
 		return false;
 	}
 	
 	public Boolean deleteUnit(String name){
 		try {
 			sql.open();
 			if (sql.checkTable("Economy_Data")){
 				//Check for existing Unit
 				try {
 					sql.query("DELETE FROM Economy_Data WHERE name='"+name+"'");
 					return true;
 				} catch (SQLException ex) {
 					log.severe("[EmeraldEconomy] "+ex);
 					sql.close();
 					return false;
 				}
 			}
 			sql.close();
 		} catch (SQLException e) {
 			log.info(e.getMessage());
 		}
 		return false;
 	}
 	
 	public Integer getBalance(String name){
 		Integer money = 0;
 		try {
 			sql.open();
 			if (sql.checkTable("Economy_Data")){
 				try {
 					ResultSet result = sql.query("SELECT * FROM Economy_Data WHERE name='"+name+"'");
 					if (result.next()) {
 						money = result.getInt("money");
 						result.close();
 						sql.close();
 						return money;
 					}
 				} catch (SQLException ex) {
 					log.severe("[EmeraldEconomy] "+ex);
 				}
 			}
 		} catch (SQLException e) {
 			log.info(e.getMessage());
 		}
 		sql.close();
 		return null;
 	}
 	
 	public String[][] getTopList(Integer number){
 		String[][] list = new String[2][number];
 		try {
 			sql.open();
 			if (sql.checkTable("Economy_Data")){
 				try {
 					ResultSet result = sql.query("SELECT * FROM Economy_Data ORDER BY money DESC;");
 					for (int i=0;i<number;i++){
 						if (result.next()) {
 							if (result.getBoolean("faction"))
 								list[0][i] = ChatColor.BOLD+result.getString("name");
 							else
 								list[0][i] = result.getString("name");
 							list[1][i] = result.getInt("money")+"";
 						}
 					}
 					result.close();
 					sql.close();
 					return list;
 				} catch (SQLException ex) {
 					log.severe("[iCoreFaction] "+ex);
 				}
 			}
 		} catch (SQLException e) {
 			log.info(e.getMessage());
 		}
 		sql.close();
 		return null;
 	}
 	
 	public Boolean setBalance(String name,Integer money){
 		try {
 			sql.open();
 			if (sql.checkTable("Economy_Data")){
 				try {
 					ResultSet result = sql.query("SELECT * FROM Economy_Data WHERE name='"+name+"'");
 					if (result.next()) {
 							sql.query("UPDATE Economy_Data " +
 									  "SET money="+money+" " +
 									  "WHERE name='"+name+"'");
 							result.close();
 							sql.close();
 							return true;
 					}
 				} catch (SQLException ex) {
 					log.severe("[EmeraldEconomy] "+ex);
 				}
 			}
 		} catch (SQLException e) {
 			log.info(e.getMessage());
 		}
 		sql.close();
 		return false;
 	}
 	
 	public void Setup() {
 		try {
 			sql.open();
 			if (!sql.checkTable("Economy_Data")){
 				sql.createTable("CREATE TABLE Economy_Data" +
 						  "(" +
 						  "name VARCHAR(255)," +
 						  "money INT(255)," +
 						  "faction TINYINT(1)" +
 						  ");");
 			}
 			sql.close();
 		} catch (SQLException e) {
 			sql.close();
             log.info(e.getMessage());
             plugin.getPluginLoader().disablePlugin(plugin);
 		}
 	}
 }
