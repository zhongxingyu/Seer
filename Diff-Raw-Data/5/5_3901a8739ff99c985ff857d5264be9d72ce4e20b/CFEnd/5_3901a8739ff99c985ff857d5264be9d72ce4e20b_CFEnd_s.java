 package org.github.craftfortress2;
 import org.bukkit.Bukkit;
 import org.bukkit.Server;
 import org.bukkit.potion.PotionEffectType;
 public class CFEnd extends CFCommandExecutor{
 	public CFEnd(CraftFortress2 cf2) {
 		super(cf2);
 	}
 	public static void endGame(){
 		//Player player = null;
 		Server server = Bukkit.getServer();
 		server.broadcastMessage("A game of Craft Fortress 2 has just ended!");
 		reset();
 	}
 	public static void reset(){
 		for (int i=0; i<CFStart.saveGM.size();i++){
 			players.get(i).removePotionEffect(PotionEffectType.SPEED);
 			players.get(i).setGameMode(CFStart.saveGM.get(i));
 			CFClasses.loadInv(players.get(i));
 		}
 		players.clear();
 		teams.clear();
 		classes.clear();
 		names.clear();
		CFStart.saveGM.clear();
		CFClasses.players.clear();
		CFClasses.playerInventories.clear();
 	}
 }
