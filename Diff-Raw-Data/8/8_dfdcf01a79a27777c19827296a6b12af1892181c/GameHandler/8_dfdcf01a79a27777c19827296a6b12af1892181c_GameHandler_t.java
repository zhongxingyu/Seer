 package com.araeosia.ArcherGames;
 
 import com.araeosia.ArcherGames.listeners.PlayerEventListener;
 import com.araeosia.ArcherGames.utils.Archer;
 import com.araeosia.ArcherGames.utils.Kit;
 import org.bukkit.Material;
 import org.bukkit.entity.Player;
 import org.kitteh.vanish.staticaccess.VanishNoPacket;
 import org.kitteh.vanish.staticaccess.VanishNotLoadedException;
 
 public class GameHandler {
 	ArcherGames plugin;
 	
 	public GameHandler(ArcherGames plugin){
 		this.plugin = plugin;
 	}
 	public int getGameStatus(){
 		return ScheduledTasks.gameStatus;
 	}
 	public void checkGameEnd(){
		if (plugin.serverwide.livingPlayers.size() <= 1) {
 			if (plugin.debug) {
 				plugin.log.info("Game has ended.");
 			}
 			// Game is finally over. We have a winner.
 			plugin.scheduler.currentLoop = 0;
 			ScheduledTasks.gameStatus = 5;
 			plugin.winner = plugin.serverwide.livingPlayers.get(0);
 			endGame();
 		}
 	}
 	public void startGame() {
 		plugin.serverwide.sendMessageToAllPlayers(plugin.strings.get("starting"));
 		plugin.scheduler.currentLoop = 0;
 		for (Player p : plugin.getServer().getOnlinePlayers()) {
 			Archer a = plugin.serverwide.getArcher(p.getName());
 			if (!plugin.serverwide.livingPlayers.contains(a)) {
 				Kit selectedKit = new Kit();
 				for (Kit kit : plugin.kits) {
 					if (kit.getName().equalsIgnoreCase("explode")) {
 						selectedKit = kit;
 					}
 				}
 				plugin.serverwide.joinGame(p.getName(), selectedKit);
 			}
 			if (plugin.serverwide.livingPlayers.contains(a)) {
 				if (plugin.getServer().getPlayer(a.getName()).getInventory().contains(Material.BOOK)) {
 					plugin.getServer().getPlayer(a.getName()).getInventory().remove(Material.BOOK);
 				}
 				a.getKit().giveToPlayer(plugin.getServer().getPlayer(a.getName())); // There's an error here somewhere.
 				plugin.serverwide.tpToRandomLocation(plugin.serverwide.getPlayer(a));
 				//plugin.serverwide.getPlayer(a).teleport(plugin.startPosition);
 				plugin.serverwide.getPlayer(a).setAllowFlight(false);
 			} else {
 				try {
 					if (!VanishNoPacket.isVanished(a.getName())) {
 						VanishNoPacket.toggleVanishSilent(p);
 					}
 				} catch (VanishNotLoadedException ex) {
 				}
 			}
 		}
 
 		for (int task : PlayerEventListener.naggerTask.values()) {
 			plugin.getServer().getScheduler().cancelTask(task); // No point in nagging them when they can't do anything about it.
 		}
 	}
 	public void endGame() {
 		// Announce the winner, announce the runners up, give winners their money.
 		plugin.econ.depositPlayer(plugin.serverwide.playerPlaces.get(1), 15000);
 		plugin.econ.depositPlayer(plugin.serverwide.playerPlaces.get(2), 10000);
 		plugin.econ.depositPlayer(plugin.serverwide.playerPlaces.get(3), 500);
 		plugin.serverwide.sendMessageToAllPlayers(plugin.strings.get("gameended"));
 	}
 }
