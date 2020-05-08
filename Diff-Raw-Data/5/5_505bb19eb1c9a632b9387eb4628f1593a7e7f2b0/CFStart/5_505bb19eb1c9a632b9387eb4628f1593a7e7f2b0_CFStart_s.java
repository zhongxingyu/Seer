 package org.github.craftfortress2;
 import org.bukkit.Bukkit;
 import org.bukkit.GameMode;
 import org.bukkit.Location;
 import org.bukkit.Server;
 import org.bukkit.World;
 import org.bukkit.entity.Player;
 import java.util.ArrayList;
 import java.util.Random;
 public class CFStart extends CFCommandExecutor {
	static ArrayList<GameMode> saveGM = new ArrayList<GameMode>();
 	public CFStart(CraftFortress2 cf2) {
 		super(cf2);
 	}
 	public static void startGame(){
 		Server server = Bukkit.getServer();
 		server.broadcastMessage("Craft Fortress 2 is starting!");
 		World ctf2fort = server.getWorld("ctf_2fort");
 		Location blu1 = new Location(ctf2fort, 78.52184, 70, 209.57359);
 		Location blu2 = new Location(ctf2fort, 31.80711, 70, 201.99570);
 		Location red1 = new Location(ctf2fort, 37.71015, 70, 256.51407);
 		Location red2 = new Location(ctf2fort, 84.70609, 70, 264.01575);
 		Random rand = new Random();
 		int spawn = rand.nextInt(2);
 		for (int i=0;i<players.size();i++){
			saveGM.add(players.get(i).getGameMode());
 			players.get(i).setGameMode(GameMode.ADVENTURE);
 			players.get(i).setHealth(20);
 			if (getTeam(players.get(i)).equals("blu")) {
 				if(spawn == 0){
 					players.get(i).teleport(blu1);
 				}else{
 					players.get(i).teleport(blu2);
 				}
 			}else if(getTeam(players.get(i)).equals("red")){
 				if(spawn == 0){
 					players.get(i).teleport(red1);
 				}else{
 					players.get(i).teleport(red2);
 				}
 			}
 			if(getClass(players.get(i)).equals("scout")){
 				Scout.init(players.get(i));
 			}else if(getClass(players.get(i)).equals("soldier")){
 
 			}else if(getClass(players.get(i)).equals("demoman")){
 
 			}else if(getClass(players.get(i)).equals("heavy")){
 
 			}else if(getClass(players.get(i)).equals("medic")){
 
 			}else if(getClass(players.get(i)).equals("engineer")){
 
 			}else if(getClass(players.get(i)).equals("spy")){
 
 			}else if(getClass(players.get(i)).equals("pyro")){
 
 			}else if(getClass(players.get(i)).equals("sniper")){
 
 			}else{
 				players.get(i).sendMessage("LOL SOMETHING FAILED. SORRY!");
 			}
 		}
 	}
 }
