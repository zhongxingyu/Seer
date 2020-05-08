 package com.agodwin.hideseek;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Random;
 
 import org.bukkit.Location;
 import org.bukkit.entity.Player;
 import org.bukkit.metadata.FixedMetadataValue;
 
 public class Arena {
 	private String arenaName;
 	private Location seekerSpawnLoc;
 	private Location hiderSpawnLoc;
 	private Location leaveLoc;
 	private Location lobbyLocation;
 	private ArrayList<Player> players = new ArrayList<Player>();
 	private int maxPlayers;
 	private boolean isInProgress = false;
 	private Player seeker;
 
 	public Arena(String name) {
 		arenaName = name;
 	}
 
 	public String getArenaName() {
 		return arenaName;
 	}
 
 	public void setArenaName(String arenaName) {
 		this.arenaName = arenaName;
 	}
 
 	public Location getHiderSpawnLoc() {
 		return hiderSpawnLoc;
 	}
 
 	public void setHiderSpawnLoc(Location hiderSpawnLoc) {
 		this.hiderSpawnLoc = hiderSpawnLoc;
 	}
 
 	public Location getSeekerSpawnLoc() {
 		return seekerSpawnLoc;
 	}
 
 	public void setSeekerSpawnLoc(Location seekerSpawnLoc) {
 		this.seekerSpawnLoc = seekerSpawnLoc;
 	}
 
 	public Location getLeaveLoc() {
 		return leaveLoc;
 	}
 
 	public void setLeaveLoc(Location leaveLoc) {
 		this.leaveLoc = leaveLoc;
 	}
 
 	public ArrayList<Player> getPlayers() {
 		return players;
 	}
 
 	public void addPlayer(Player p) {
 		this.players.add(p);
 	}
 
 	public int getNumPlayers() {
 		return this.players.size();
 	}
 
 	public int getMaxPlayers() {
 		return maxPlayers;
 	}
 
 	public void setMaxPlayers(int maxPlayers) {
 		this.maxPlayers = maxPlayers;
 	}
 
 	public Location getLobbyLocation() {
 		return lobbyLocation;
 	}
 
 	public void setLobbyLocation(Location lobbyLocation) {
 		this.lobbyLocation = lobbyLocation;
 	}
 
 	public void startArena() {
 		shuffleTeam();
 		Random r = new Random();
 		int rand = r.nextInt(players.size());
 		if (players.size() > 0) {
 			int count = 0;
 			for (Player p : players) {
 				if (p != null) {
 					if (rand == count) {
 						p.setMetadata("team",
 								new FixedMetadataValue(Main.getPlugin(),
 										"seeker"));
 						p.teleport(seekerSpawnLoc);
 						seeker = p;
 					} else {
 						p.setMetadata("team",
 								new FixedMetadataValue(Main.getPlugin(),
 										"hider"));
 						p.teleport(hiderSpawnLoc);
 					}
 				} else {
 					players.remove(p);
 				}
 				count++;
 			}
 		}
 		isInProgress = true;
 	}
 
 	public void safelyRemovePlayer(Player p) {
		if (seeker.equals(p)) {
 			players.remove(p);
 			p.teleport(leaveLoc);
 			p.removeMetadata("team", Main.getPlugin());
 			seeker = null;
 		} else {
 			players.remove(p);
 			p.teleport(leaveLoc);
 			p.removeMetadata("team", Main.getPlugin());
 		}
 		p.sendMessage(Main.helper + "Safely removed from arena "
 				+ this.getArenaName() + ". Thanks!");
 	}
 
 	public boolean playerInArena(Player p) {
 		return players.contains(p);
 	}
 
 	public boolean arenaInProgress() {
 		return isInProgress;
 	}
 
 	private void shuffleTeam() {
 		Collections.shuffle(this.players);
 	}
 }
