 package com.araeosia.ArcherGames;
 
 import com.araeosia.ArcherGames.utils.Archer;
 import java.util.ArrayList;
 import org.bukkit.Bukkit;
 import org.bukkit.entity.Player;
 
 /**
  *
  * @author Bruce
  */
 public class ServerWide {
 
 	public ArrayList<Archer> livingPlayers = new ArrayList<Archer>();
 	public ArcherGames plugin;
 
 	public ServerWide(ArcherGames plugin){
 		this.plugin = plugin;
 	}
 
 	public void killPlayer(String playerName) {
 		livingPlayers.remove(Archer.getByName(playerName));
 		Archer.getByName(playerName).kill();
 	}
 
 	public ArrayList<Archer> getNotReadyPlayers() {
 		ArrayList<Archer> archers = new ArrayList<Archer>();
 		for (Archer a : ArcherGames.players) {
 			if (!a.isReady()) {
 				archers.add(a);
 			}
 		}
 		return archers;
 	}
 
 	public void sendToLivingPlayers(String message) {
 		// Send a message to every living archer
 		for (Archer a : livingPlayers) {
 			getPlayer(a).sendMessage(message);
 		}
 	}
 
 	public void sendMessageToAllPlayers(String message) {
 		// Send a message to every online player
 		for (Player p : Bukkit.getServer().getOnlinePlayers()) {
 			p.sendMessage(message);
 		}
		plugin.log.info("[ArcherGames]: " + message);
 	}
 
 	public Player getPlayer(Archer a) {
 		for (Player p : Bukkit.getServer().getOnlinePlayers()) {
 			if (p.getName().equalsIgnoreCase(a.getName())) {
 				return p;
 			}
 		}
 		return null;
 	}
 
 	public Archer getArcher(Player p) {
 		for (Archer a : ArcherGames.players) {
 			if (a.getName().equalsIgnoreCase(p.getName())) {
 				return a;
 			}
 		}
 		return null;
 	}
 	public void handleGameStart(){
 		// Handle the game start. Populate player inventories, teleport players, etc.
 	}
 	public void handleGameEnd(){
 		// Announce the winner, announce the runners up, give winners their money.
 		sendMessageToAllPlayers(plugin.strings.get("gameended"));
 	}
 }
