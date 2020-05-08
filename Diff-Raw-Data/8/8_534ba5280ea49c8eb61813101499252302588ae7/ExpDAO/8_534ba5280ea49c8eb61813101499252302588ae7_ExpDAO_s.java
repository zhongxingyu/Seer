 package com.renderjunkies.noh.classes;
 
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.bukkit.entity.Player;
 
 import com.renderjunkies.noh.classes.Classes.PlayerClass;
 import com.renderjunkies.noh.classes.ExpManager.PlayerData;
 
 import lib.PatPeter.SQLibrary.MySQL;
 
 public class ExpDAO
 {
 	Map<PlayerClass, String> tableMap = null;
 	private MySQL _mysql = null;
 	Classes _plugin = null;
 	public ExpDAO(Classes plugin)
 	{
 		// Creating a new MySQL object so I don't have to worry about Saves blocking the main thread if it wants to do something else.
 		this._mysql = new MySQL(plugin.getLogger(), plugin.getConfig().getString("database.prefix"),plugin.getConfig().getString("database.host"),plugin.getConfig().getString("database.port"),plugin.getConfig().getString("database.dbname"),plugin.getConfig().getString("database.username"),plugin.getConfig().getString("database.password"));
 		this._plugin = plugin;
 
 		tableMap = new HashMap<PlayerClass, String>();
 		tableMap.put(PlayerClass.BASE, "base");
 		tableMap.put(PlayerClass.RANGER, "ranger");
 		tableMap.put(PlayerClass.CLERIC, "cleric");
 		tableMap.put(PlayerClass.BERSERKER, "berserker");
 		tableMap.put(PlayerClass.SWORDSMAN, "swordsman");
 	}
 	
 	public void PlayerExists(Player player)
 	{
 		String playerName = player.getName();
 		
 		ResultSet results;
 		_plugin.getMySQL().open();
 		results = _plugin.getMySQL().query("SELECT * from `noh_experience` WHERE name = '"+playerName+"'");
 		try
 		{
 			if(!results.next())
 			{
 				// Player doesn't exist yet, need to create.
 				_plugin.getMySQL().query("INSERT INTO `noh_experience` (`name`) VALUES ('"+playerName+"')");
 			}
 		}
 		catch (SQLException e) 
 		{
 			_plugin.getLogger().info("ERROR: Couldn't create player \""+playerName+"\".");
 		}
 		_plugin.getMySQL().close();
 	}
 	
 	// Only the SaveExp function uses _mysql, and it is synchronized so only calling SaveExp from the main thread should
 	// cause blocking.
 	public synchronized void SaveExp(ExpManager manager)
 	{
 		// TODO: Diff the changing ExpMap with the DB version and only commit changes
 		// 		 on new saves, update the DB version
 		this._mysql.open();
 		List<String> existingPlayers = new ArrayList<String>();
 		ResultSet results = this._mysql.query("SELECT name FROM `noh_experience`");
 		try
 		{
 			while(results.next())
 			{
 				existingPlayers.add(results.getString("name"));
 			}
 		}
 		catch (SQLException e) 
 		{
 			_plugin.getLogger().info("ERROR: In SaveExp");
 		}
 		
 		for(Map.Entry<String, PlayerData> entry: manager.playerExp.entrySet())
 		{
 			if(!existingPlayers.contains(entry.getKey()))
 			{
 				this._mysql.query("INSERT INTO `noh_experience` (`name`) VALUES ('"+entry.getKey()+"')");
 			}
 			this._mysql.query("UPDATE `noh_experience` SET " +
					"`"+tableMap.get(PlayerClass.BASE)+"` = "+entry.getValue().Experience.get(PlayerClass.BASE)+" " +
					"`"+tableMap.get(PlayerClass.RANGER)+"` = "+entry.getValue().Experience.get(PlayerClass.RANGER)+" " +
					"`"+tableMap.get(PlayerClass.CLERIC)+"` = "+entry.getValue().Experience.get(PlayerClass.CLERIC)+" " +
					"`"+tableMap.get(PlayerClass.BERSERKER)+"` = "+entry.getValue().Experience.get(PlayerClass.BERSERKER)+" " +
 					"`"+tableMap.get(PlayerClass.SWORDSMAN)+"` = "+entry.getValue().Experience.get(PlayerClass.SWORDSMAN)+" " +
 					"WHERE name = '"+entry.getKey()+"'");
 		}
 		this._mysql.close();
 		// Save Manager's ExpMap to Database
 	}
 	
 	public void LoadExp(ExpManager manager)
 	{
 		// TODO: Load 2 versions of the map a DB version for diff and one that will be changed in runtime
 		// Load Manager's ExpMap from Database
 		
 	}
 }
