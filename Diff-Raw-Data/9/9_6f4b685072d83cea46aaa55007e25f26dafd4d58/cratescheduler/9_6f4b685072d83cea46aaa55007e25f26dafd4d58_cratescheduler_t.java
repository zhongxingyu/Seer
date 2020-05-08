 package tv.mineinthebox.ManCo.events;
 
 import java.util.Random;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.Player;
 import org.bukkit.scheduler.BukkitTask;
 
 import tv.mineinthebox.ManCo.chestList;
 import tv.mineinthebox.ManCo.manCo;
 import tv.mineinthebox.ManCo.configuration.configuration;
 import tv.mineinthebox.ManCo.utils.util;
 import tv.mineinthebox.ManCo.utils.vanish;
 import tv.mineinthebox.ManCo.utils.worldguard;
 
 public class cratescheduler {
 
 	public static BukkitTask task;
 
 	public static void startScheduler() {
 		BukkitTask taskID = Bukkit.getScheduler().runTaskTimer(manCo.getPlugin(), new Runnable() {
 
 			@Override
 			public void run() {
 				if(Bukkit.getOnlinePlayers().length > configuration.roundsPerTime()) {
 					for(int i = 0; i < configuration.roundsPerTime(); i++) {
 						if(util.isWorldGuardEnabled()) {
 							doCrateWithWG();
 						} else {
 							doCrateWithoutWG();
 						}
 					}
 				}
 			}
 
 		}, 0, configuration.getTime());
 		task = taskID;
 	}
 
 	public static void doCrateWithoutWG() {
 		Random rand = new Random();
 		int i = Bukkit.getOnlinePlayers().length;
 		if(i > 0) {
 			int playerID = rand.nextInt(i) + 0;
 			Player p = Bukkit.getOnlinePlayers()[playerID];
 			if(chestList.getCrateList.containsKey(p.getName()) || vanish.isVanished(p) || chestList.getCrateList2.containsKey(p.getName())) {
 
 				//player already has a crate, loop through all possible online players to see if they can get a crate.
 				//then use the native method.
 
 				for(Player p2 : Bukkit.getOnlinePlayers()) {
					if(!(chestList.getCrateList.containsKey(p2.getName()) || vanish.isVanished(p2) || chestList.getCrateList2.containsKey(p2.getName()))) {
 						if(!p2.getName().equalsIgnoreCase(p.getName())) {
 							doCrateNative(p2);
 							break;
 						}
 					}
 				}
 			} else {
 
 				//Random player doesn't have a crate so we can proceed the code:)
 
 				Location loc = p.getLocation();
 				loc.setY(120);
 				Entity entity = p.getWorld().spawnFallingBlock(loc, Material.CHEST, (byte) 1);
 				chestList.getFallingStateChest.put(entity, p.getName());
 				Bukkit.broadcastMessage(ChatColor.GREEN + "[ManCo] " + ChatColor.GRAY + p.getName() + " found a ManCo crate!");	
 			}
 		}
 	}
 	
 	public static void doCrateWithWG() {
 		Random rand = new Random();
 		int i = Bukkit.getOnlinePlayers().length;
 		if(i > 0) {
 			int playerID = rand.nextInt(i) + 0;
 			Player p = Bukkit.getOnlinePlayers()[playerID];
 			if(chestList.getCrateList.containsKey(p.getName()) || !worldguard.canPlayerBuild(p) || vanish.isVanished(p) || chestList.getCrateList2.containsKey(p.getName())) {
 				//player already has a crate, loop through all possible online players to see if they can get a crate.
 				//then use the native method.
 				for(Player p2 : Bukkit.getOnlinePlayers()) {
					if(!(chestList.getCrateList.containsKey(p2.getName()) || vanish.isVanished(p2) || !worldguard.canPlayerBuild(p2) || chestList.getCrateList2.containsKey(p2.getName()))) {
 						if(!p2.getName().equalsIgnoreCase(p.getName())) {
 							doCrateNative(p2);
 							break;
 						}
 					}
 				}
 			} else {
 
 				//Random player doesn't have a crate so we can proceed the code:)
 
 				Location loc = p.getLocation();
 				loc.setY(120);
 				Entity entity = p.getWorld().spawnFallingBlock(loc, Material.CHEST, (byte) 1);
 				chestList.getFallingStateChest.put(entity, p.getName());
 				Bukkit.broadcastMessage(ChatColor.GREEN + "[ManCo] " + ChatColor.GRAY + p.getName() + " found a ManCo crate!");	
 			}
 		}
 	}
 
 	public static void doCrateNative(Player p) {
 		Location loc = p.getLocation();
 		loc.setY(120);
 		Entity entity = p.getWorld().spawnFallingBlock(loc, Material.CHEST, (byte) 1);
 		chestList.getFallingStateChest.put(entity, p.getName());
 		Bukkit.broadcastMessage(ChatColor.GREEN + "[ManCo] " + ChatColor.GRAY + p.getName() + " found a ManCo crate!");
 	}
 
 }
