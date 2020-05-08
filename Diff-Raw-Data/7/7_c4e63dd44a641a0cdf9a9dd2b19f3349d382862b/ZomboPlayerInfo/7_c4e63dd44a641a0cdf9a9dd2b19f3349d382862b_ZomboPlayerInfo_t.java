 package me.ayan4m1.plugins.zombo;
 
 import java.util.HashMap;
 
 import org.bukkit.entity.EntityType;
 
 public class ZomboPlayerInfo {
	private Integer xp       = 0;
	private boolean online   = false;
 	private HashMap<EntityType, Integer> kills = new HashMap<EntityType, Integer>();
 
 	public boolean isOnline() {
 		return online;
 	}
 
 	public void setOnline(boolean online) {
 		this.online = online;
 	}
 
 	public Integer getXp() {
 		return xp;
 	}
 
 	public void setXp(Integer xp) {
 		this.xp = xp;
 	}
 
 	public void addXp(Integer xp) {
 		this.xp += xp;
 	}
 
 	public HashMap<EntityType, Integer> getKills() {
 		return kills;
 	}
 
 	public Integer getKillsForType(EntityType type) {
 		return kills.containsKey(type) ? kills.get(type) : 0;
 	}
 
 	public void setKills(HashMap<EntityType, Integer> kills) {
 		this.kills = kills;
 	}
 
 	public void addKill(EntityType type) {
 		if (kills.containsKey(type)) {
 			kills.put(type, kills.get(type) + 1);
 		} else {
 			kills.put(type, 1);
 		}
 	}

 	public Integer getLevel() {
 		return ((Double)Math.floor(this.xp / 5000)).intValue() + 1;
 	}
 }
