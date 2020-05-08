 package com.TeamNovus.SupernaturalRaces;
 
 import java.io.File;
 
 import org.bukkit.plugin.java.JavaPlugin;
 
 import com.TeamNovus.SupernaturalRaces.Commands.BaseCommandExecutor;
 import com.TeamNovus.SupernaturalRaces.Commands.DefaultCommands;
 import com.TeamNovus.SupernaturalRaces.Commands.PlayerCommands;
 import com.TeamNovus.SupernaturalRaces.Listeners.CustomListener;
 import com.TeamNovus.SupernaturalRaces.Listeners.PlayerListener;
 import com.TeamNovus.SupernaturalRaces.Listeners.ServerListener;
 import com.TeamNovus.SupernaturalRaces.Managers.CommandManager;
 import com.TeamNovus.SupernaturalRaces.Managers.DatabaseManager;
 import com.TeamNovus.SupernaturalRaces.Managers.EntityManager;
 import com.TeamNovus.SupernaturalRaces.Managers.PlayerManager;
 import com.TeamNovus.SupernaturalRaces.Managers.RaceManager;
 import com.TeamNovus.SupernaturalRaces.Managers.EventManager;
 import com.TeamNovus.SupernaturalRaces.Tasks.PowerRegenTask;
 import com.TeamNovus.SupernaturalRaces.Tasks.SaveTask;
 import com.TeamNovus.SupernaturalRaces.Tasks.ServerTickTask;
 
 public class SupernaturalRaces extends JavaPlugin {
 	private static SupernaturalRaces plugin;
 	private static CommandManager commandManager;
 	private static PlayerManager playerManager;
 	private static RaceManager raceManager;
 	private static EntityManager entityManager;
 	private static DatabaseManager databaseManager;
 
 	@Override
 	public void onEnable() {
 		plugin = this;
 		
 		if(!(new File(getDataFolder() + File.separator + "config.yml").exists())) {
 			saveDefaultConfig();
 		}
 		reloadConfig();
 
 		commandManager = new CommandManager();
 		playerManager = new PlayerManager();
 		raceManager = new RaceManager();
 		entityManager = new EntityManager();
 		databaseManager = new DatabaseManager();
 		
 		getCommand("supernatural").setExecutor(new BaseCommandExecutor());
 		
 		databaseManager.connect();
 		databaseManager.setup();
 		databaseManager.loadPlayers();
 		
 		commandManager.registerClass(DefaultCommands.class);
 		commandManager.registerClass(PlayerCommands.class);
 						
 		getServer().getPluginManager().registerEvents(new CustomListener(), this);
 		getServer().getPluginManager().registerEvents(new EventManager(), this);
 		getServer().getPluginManager().registerEvents(new PlayerListener(), this);
 		getServer().getPluginManager().registerEvents(new ServerListener(), this);
 		
 		getServer().getScheduler().scheduleSyncRepeatingTask(this, new PowerRegenTask(), 20 * 10, 20 * 10);
 		getServer().getScheduler().scheduleSyncRepeatingTask(this, new SaveTask(), 20 * 60 * 5, 20 * 60 * 5);
 		getServer().getScheduler().scheduleSyncRepeatingTask(this, new ServerTickTask(), 1, 1);
 	}
 	
 	@Override
 	public void onDisable() {
 		// Cancel all tasks
 		getServer().getScheduler().cancelTasks(this);
 		
 		// Save to database and close the connection
 		databaseManager.savePlayers();
 		databaseManager.close();
 		
 		// Set all static references to null
 		plugin = null;
 		commandManager = null;
 		playerManager = null;
 		raceManager = null;
 		entityManager = null;
 		databaseManager = null;
 	}
 
 	public static SupernaturalRaces getPlugin() {
 		return plugin;
 	}
 	
 	public static DatabaseManager getDatabaseManager() {
 		return databaseManager;
 	}
 	
 	public static PlayerManager getPlayerManager() {
 		return playerManager;
 	}
 	
 	public static RaceManager getRaceManager() {
 		return raceManager;
 	}
 	
 	public static EntityManager getEntityManager() {
 		return entityManager;
 	}
 
 	public static CommandManager getCommandManager() {
 		return commandManager;
 	}
 }
