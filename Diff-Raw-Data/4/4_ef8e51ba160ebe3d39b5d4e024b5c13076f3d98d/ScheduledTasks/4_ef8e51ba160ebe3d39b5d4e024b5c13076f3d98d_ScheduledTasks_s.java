 package com.araeosia.ArcherGames;
 
 import com.araeosia.ArcherGames.utils.Archer;
 import com.wimbli.WorldBorder.BorderData;
 import com.wimbli.WorldBorder.Config;
 
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 
 public class ScheduledTasks {
 
 	public ArcherGames plugin;
 	public static int gameStatus = 1;
 	public int currentLoop; // In loops
 	public int preGameCountdown; // Time before anything starts. Players choose kits.
 	public int gameInvincibleCountdown; // Time while players are invincible.
 	public int gameOvertimeCountdown; // Time until overtime starts.
 	public int shutdownTimer; // Time after the game ends until the server shuts down.
 	public int minPlayersToStart;
 	public int schedulerTaskID;
 	public int nagTime;
 	public int overtimeWorldRadius;
 
 	public ScheduledTasks(ArcherGames plugin) {
 		this.plugin = plugin;
 	}
 
 	/**
 	 * This will do the action every second (20 TPS)
 	 */
 	public void everySecondCheck() {
 		if (schedulerTaskID != -1) {
 			schedulerTaskID = plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
 
 				public void run() {
 					switch (gameStatus) {
 						case 1:
 							if (plugin.debug) {
 								plugin.log.info((preGameCountdown - currentLoop) + " seconds until game starts.");
 							}
 							// Pre-game
 							if (preGameCountdown - currentLoop % 3600 == 0 || preGameCountdown - currentLoop == 60 || preGameCountdown - currentLoop % 60 == 0 || (preGameCountdown - currentLoop <= 10 && preGameCountdown - currentLoop > 0) || preGameCountdown - currentLoop == 15 || preGameCountdown - currentLoop == 30) {
 								plugin.serverwide.sendMessageToAllPlayers(String.format(plugin.strings.get("starttimeleft"), ((preGameCountdown - currentLoop) % 60 == 0 ? (preGameCountdown - currentLoop) / 60 + " minute" + (((preGameCountdown - currentLoop) / 60) == 1 ? "" : "s") : (preGameCountdown - currentLoop) + " second" + ((preGameCountdown - currentLoop != 1) ? "s" : ""))));
 							}
 							if (currentLoop >= preGameCountdown) {
 								if (plugin.debug) {
 									plugin.log.info("Attempting to start...");
 								}
 								// Time to start.
 								if (plugin.serverwide.livingPlayers.size() < minPlayersToStart) { // There aren't enough players.
 									plugin.serverwide.sendMessageToAllPlayers(plugin.strings.get("startnotenoughplayers"));
 								} else { // There's enough players, let's start!
 									plugin.serverwide.sendMessageToAllPlayers(plugin.strings.get("starting"));
 									plugin.scheduler.startGame();
 								}
 								currentLoop = -1;
 							}
 							currentLoop++;
 							break;
 						case 2:
 							// Invincibility
 							if (plugin.debug) {
 								plugin.log.info((gameInvincibleCountdown - currentLoop) + " seconds until invincibility ends.");
 							}
 							if (currentLoop >= gameInvincibleCountdown) {
 								if (plugin.debug) {
 									plugin.log.info("Invincibility has ended.");
 								}
 								// Invincibility is over.
 								plugin.serverwide.sendMessageToAllPlayers(plugin.strings.get("invincibilityend"));
 								gameStatus = 3;
 								currentLoop = -1;
 							}
 							currentLoop++;
 							break;
 						case 3:
 							// Game time
 							if (plugin.debug) {
 								plugin.log.info((gameOvertimeCountdown - currentLoop) + " seconds until overtime starts.");
 							}
 							if (currentLoop >= gameOvertimeCountdown) {
 								if (plugin.debug) {
 									plugin.log.info("Overtime has started.");
 								}
 								// Game time is up.
 								plugin.serverwide.sendMessageToAllPlayers(plugin.strings.get("overtimestart"));
 								for (Player p : plugin.getServer().getOnlinePlayers()) {
 									if (plugin.serverwide.getArcher(p).isReady) {
 										p.teleport(plugin.startPosition);
 									}
 								}
 								Config.setBorder(plugin.startPosition.getWorld().getName(), overtimeWorldRadius, plugin.startPosition.getBlockX(), plugin.startPosition.getBlockZ(), true); // World border
 								gameStatus = 4;
 								currentLoop = -1;
 								// TODO: World border shrinking.
 							}
 							currentLoop++;
 							break;
 						case 4:
 							// Overtime
 							if (plugin.serverwide.livingPlayers.size() <= 1) {
 								if (plugin.debug) {
 									plugin.log.info("Game has ended.");
 								}
 								// Game is finally over. We have a winner.
 								plugin.serverwide.handleGameEnd();
 								gameStatus = 5;
 								currentLoop = -1;
 							}
 							currentLoop++;
 							break;
 						case 5:
 							// Game finished, waiting for reset to pre-game
 							if (plugin.debug) {
 								plugin.log.info((shutdownTimer - currentLoop) + " seconds until server reboots.");
 							}
 							if (currentLoop >= shutdownTimer) {
 								if (plugin.debug) {
 									plugin.log.info("Kicking all players, then shutting down.");
 								}
 								for (Player p : plugin.getServer().getOnlinePlayers()) {
 									p.kickPlayer(plugin.strings.get("serverclosekick"));
 								}
 								plugin.getServer().shutdown();
 							}
 							break;
 					}
 				}
 			}, 20L, 20L);
 			if (plugin.debug) {
 				plugin.log.info("Task ID is " + schedulerTaskID);
 			}
 		} else {
 			plugin.log.severe("Scheduler task start was attempted, but scheduler task already running!");
 		}
 	}
 
 	public void startGame() {
 		plugin.serverwide.sendMessageToAllPlayers(plugin.strings.get("starting"));
 		currentLoop = 0;
 		gameStatus = 2;
 		for (Archer a : plugin.serverwide.livingPlayers) {
 			for (ItemStack is : plugin.kits.get("ArcherGames.kits."+a.getKitName())) {
 				plugin.serverwide.getPlayer(a).getInventory().addItem(is);
 			}
 			plugin.serverwide.tpToRandomLocation(plugin.serverwide.getPlayer(a));
 			//plugin.serverwide.getPlayer(a).teleport(plugin.startPosition);
 			plugin.serverwide.getPlayer(a).setAllowFlight(false);
 		}
 	}
 
 	public int nagPlayerKit(final Player player) {
 		player.sendMessage(plugin.strings.get("kitnag"));
 		return plugin.getServer().getScheduler().scheduleAsyncRepeatingTask(plugin, new Runnable() {
 
 			public void run() {
				player.sendMessage(plugin.strings.get("kitnag"));
 			}
 		}, new Long(nagTime * 20), new Long(nagTime * 20));
 	}
 
 	public void endGame() {
 		plugin.serverwide.handleGameEnd();
 		gameStatus = 5;
 		currentLoop = 0;
 	}
 }
