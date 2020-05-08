 package me.supermaxman.xestats.executors;
 
 import me.supermaxman.xestats.XeStats;
 import me.supermaxman.xestats.utils.MySQLUtils;
 
 import org.bukkit.entity.Player;
 
 public class StatsLookupExecutor {
 	public static void lookupStats(Player player, String name){
		MySQLUtils.sendPlayerInfo(player, XeStats.XE.getServer().getPlayer(name));
 		
 	}
 }
