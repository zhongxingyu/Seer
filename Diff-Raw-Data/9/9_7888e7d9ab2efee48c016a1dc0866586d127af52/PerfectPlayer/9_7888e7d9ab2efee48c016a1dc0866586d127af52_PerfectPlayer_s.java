 package net.niccossystem.perfectarena;
 
 import org.bukkit.Bukkit;
 import org.bukkit.entity.Player;
 
 import net.niccossystem.perfectarena.arena.PvPArena;
 import net.niccossystem.perfectarena.perfectclass.PerfectClass;
 
 public class PerfectPlayer {
 
	private final String playerName;
     private PerfectClass currentClass;
     private PvPArena currentArena;
     private int kills;
     private int deaths;
     private int pvpDeaths;
 	
	public PerfectPlayer(Player player) {
		this.playerName = player.getName();
	}
     
     public void setCurrentArena(PvPArena pvpArena) {
     	this.currentArena = pvpArena;
     }
     
     public void setPerfectClass(PerfectClass pClass) {
     	this.currentClass = pClass;
     }
     
     public void setKills(int kills) {
     	this.kills = kills;
     }
     
     public void incrementKills() {
         kills++;
     }
     
     public void setDeaths(int deaths) {
     	this.deaths = deaths;
     }
     
     public void incrementDeaths() {
         deaths++;
     }
     
     public void setPvpDeaths(int pvpDeaths) {
     	this.pvpDeaths = pvpDeaths;
     }
     
     public void incrementPvpDeaths() {
         pvpDeaths++;
     }
     
     public Player getPlayer() {
     	return Bukkit.getServer().getPlayer(playerName);
     }
     
     public PvPArena getCurrentArena() {
         return currentArena;
     }
 
     public PerfectClass getPerfectClass() {
         return currentClass;
     }
     
     public int getKills() {
     	return kills;
     }
     
     public int getDeaths() {
     	return deaths;
     }
     
     public int getPvpDeaths() {
     	return pvpDeaths;
     }
     
     public String getName() {
         return playerName;
     }
 }
