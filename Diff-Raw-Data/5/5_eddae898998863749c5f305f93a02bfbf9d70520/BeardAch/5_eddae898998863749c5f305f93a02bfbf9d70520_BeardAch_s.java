 package me.tehbeard.BeardAch;
 
 import java.io.File;
 import java.io.IOException;
 import java.sql.SQLException;
 
 import me.tehbeard.BeardAch.achievement.Achievement;
 import me.tehbeard.BeardAch.achievement.AchievementManager;
 import me.tehbeard.BeardAch.achievement.rewards.CommandReward;
 import me.tehbeard.BeardAch.achievement.triggers.CuboidCheckTrigger;
 import me.tehbeard.BeardAch.achievement.triggers.ITrigger;
 import me.tehbeard.BeardAch.achievement.triggers.PermCheckTrigger;
 import me.tehbeard.BeardAch.achievement.triggers.StatCheckTrigger;
 import me.tehbeard.BeardAch.dataSource.SqlDataSource;
 import me.tehbeard.BeardAch.listener.BeardAchPlayerListener;
 import me.tehbeard.BeardStat.BeardStat;
 
 import org.bukkit.Bukkit;
 import org.bukkit.Server;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Event;
 import org.bukkit.event.Event.Priority;
 import org.bukkit.event.Event.Type;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import de.hydrox.bukkit.DroxPerms.DroxPerms;
 import de.hydrox.bukkit.DroxPerms.DroxPermsAPI;
 
 public class BeardAch extends JavaPlugin {
 
 	public static YamlConfiguration config;
 	public static BeardAch self;
 
 	public static DroxPermsAPI droxAPI = null;
 	private static final String PERM_PREFIX = "ach";
 
 	public static boolean hasPermission(Player player,String node){
 
 		return (player.hasPermission(PERM_PREFIX + "." + node) || player.isOp());
 
 
 	}
 	public static void printCon(String line){
 		System.out.println("[BeardAch] " + line);
 	}
 
 	public static void printDebugCon(String line){
 		//if(config!=null){
 			//if(config.getBoolean("general.debug", false)){
 				System.out.println("[BeardAch][DEBUG] " + line);
 
 		//	}
 		//}
 	}
 
 	public void onDisable() {
 		// TODO Auto-generated method stub
 		BeardAch.printCon("Flushing to database");
 		AchievementManager.database.flush();
 		BeardAch.printCon("Flushed to database");
 	}
 	
 	public static boolean checkBeardStat(){
 		BeardStat stats = (BeardStat) Bukkit.getServer().getPluginManager().getPlugin("BeardStat");
 		return (stats!=null && stats.isEnabled());
 		
 	}
 
 	public void onEnable() {
 		self = this;
 		// TODO Auto-generated method stub
 		BeardStat stats = (BeardStat)getServer().getPluginManager().getPlugin("BeardStat");
 		if(!checkBeardStat()){
 			printCon("BeardStat NOT FOUND, DISABLING PLUGIN!");
 			onDisable();
 			return;
 		}
 
 		
 		//check DroxPerms
 		
 		DroxPerms droxPerms = ((DroxPerms) this.getServer().getPluginManager().getPlugin("DroxPerms"));
 		if (droxPerms != null) {
 		    droxAPI = droxPerms.getAPI();
 		}
 		
 		
 		
 		
 		
 		//setup events
 		getServer().getPluginManager().registerEvent(Type.PLAYER_MOVE, new BeardAchPlayerListener(), Priority.Highest, this);
 		getServer().getPluginManager().registerEvent(Type.PLAYER_JOIN, new BeardAchPlayerListener(), Priority.Highest, this);
 		
 
 		
 		//TEST ACHIEVEMENTS, DELETE ONCE DATASOURCE'S COMPLETE
 		
 
 		
 		
 		
 		//Load config
 		printCon("Starting BeardAch");
 		if(!(new File(getDataFolder(),"BeardAch.yml")).exists()){
			initalConfig();
 		}
 		config =YamlConfiguration.loadConfiguration(new File(getDataFolder(),"BeardAch.yml"));
 		
 		AchievementManager.database = new SqlDataSource();
 		AchievementManager.database.getAchievements();
 		
 		getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable(){
 
 			public void run() {
 				// TODO Auto-generated method stub
 				AchievementManager.checkPlayers();
 			}
 			
 		}, 600L,600L);
 		
 		for(Player p :getServer().getOnlinePlayers()){
 			AchievementManager.loadAchievements(p.getName());
 		}
 		}
 
 	@Override
 	public boolean onCommand(CommandSender sender, Command command,
 			String label, String[] args) {
 
 			
 		
 		return true;
 	}
 	
 	
 	/**
 	 * Creates the inital config
 	 */
	private void initalConfig() {
 		printCon("Generating Inital config");
 		File f = new File(getDataFolder(),"BeardAch.yml");
 		config = YamlConfiguration.loadConfiguration(f);
 		
 		config.set("ach.database.type", "mysql");
 		config.set("ach.database.host", "localhost");
 		config.set("ach.database.username", "Beardstats");
 		config.set("ach.database.password", "changeme");
 		config.set("ach.database.database", "stats");
 		config.set("ach.msg.person", "Achievement Unlocked: <ACH>");
 		config.set("ach.msg.broadcast", "<PLAYER> Unlocked: <ACH>");
 		config.set("ach.msg.send.person", true);
 		config.set("ach.msg.send.broadcast", false);
 		config.set("achievements", null);
 		try {
 			config.save(f);
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 }
