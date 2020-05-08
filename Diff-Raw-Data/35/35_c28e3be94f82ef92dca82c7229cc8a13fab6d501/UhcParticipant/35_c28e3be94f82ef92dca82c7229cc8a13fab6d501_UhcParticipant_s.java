 package com.martinbrook.tesseractuhc;
 
 import java.util.HashSet;
 
 import org.bukkit.GameMode;
 import org.bukkit.Location;
 import org.bukkit.entity.Player;
 import com.martinbrook.tesseractuhc.startpoint.UhcStartPoint;
 
 public class UhcParticipant implements PlayerTarget {
 	private boolean launched = false;
 	private UhcTeam team;
 	private HashSet<PlayerTarget> nearbyTargets = new HashSet<PlayerTarget>();
 	private UhcPlayer player;
 
 	private boolean dead = false;
 	
 	public UhcParticipant(UhcPlayer pl, UhcTeam team) {
 		this.player = pl;
 		this.team = team;
 	}
 		
 	public String getName() {
 		return player.getName();
 	}
 
 
 	public boolean isLaunched() {
 		return launched;
 	}
 
 
 	public void setLaunched(boolean launched) {
 		this.launched = launched;
 	}
 
 
 	public UhcStartPoint getStartPoint() {
 		return team.getStartPoint();
 	}
 
 
 	public boolean isDead() {
 		return dead;
 	}
 
 	public void setDead(boolean dead) {
 		this.dead = dead;
 	}
 
 	public UhcTeam getTeam() {
 		return team;
 	}
 
 	public boolean isNearTo(PlayerTarget target) {
 		return nearbyTargets.contains(target);
 	}
 
 	public void setNearTo(PlayerTarget target, boolean b) {
 		if (b)
 			nearbyTargets.add(target);
 		else
 			nearbyTargets.remove(target);
 		
 	}
 
 	public boolean teleport(Player p) { return this.teleport(p, "You have been teleported!"); }
 	public boolean teleport(Location l) { return this.teleport(l, "You have been teleported!"); }
 	public boolean teleport(Player p, String message) { return this.teleport(p.getLocation(), message); }
 	public boolean teleport(Location l, String message) { return player.teleport(l, message); }
 	
 	public UhcPlayer getPlayer() { return player; }
 
 	public boolean sendToStartPoint() {
 		return (player.setGameMode(GameMode.ADVENTURE) && teleport(getStartPoint().getLocation()) && player.renew());
 	}
 	
 	public boolean start() {
 		return (player.feed() && player.clearXP() && player.clearPotionEffects() 
 				&& player.heal() && player.setGameMode(GameMode.SURVIVAL));
 	}

 
 }
