 package com.mcprohosting.plugins.marcwar.entities;
 
 import com.mcprohosting.plugins.marcwar.utilities.TeamHandler;
 import org.bukkit.Location;
 
 import java.util.HashMap;
 import java.util.Map;
 
 public class Team {
 
 	String color;
 	Location spawn;
 	Map<String, Participant> players;
 
 	public Team(String color) {
 		this.color = color;
 		this.players = new HashMap<String, Participant>();
 	}
 
 	public void setSpawn(Location location) {
 		this.spawn = location;
 	}
 
 	public Location getSpawn() {
 		return this.spawn;
 	}
 
 	public Map<String, Participant> getPlayers() {
 		return players;
 	}
 
 	public void addPlayer(String name, Participant player) {
 		this.players.put(name, player);
 		TeamHandler.addPlayer(name, this);
 	}
 
	public void removePlayer(String name) {
 		this.players.remove(name);
 		TeamHandler.removePlayer(name);
 	}
 
 }
