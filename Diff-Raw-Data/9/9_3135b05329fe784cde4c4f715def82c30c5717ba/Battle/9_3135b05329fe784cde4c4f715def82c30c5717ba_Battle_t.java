 package me.limebyte.battlenight.api.battle;
 
 import java.util.HashSet;
import java.util.List;
 import java.util.Set;
 
 import me.limebyte.battlenight.api.BattleNightAPI;
 import me.limebyte.battlenight.api.event.BattleDeathEvent;
 import me.limebyte.battlenight.api.util.PlayerData;
 import me.limebyte.battlenight.core.util.Messenger;
 import me.limebyte.battlenight.core.util.Messenger.Message;
 import me.limebyte.battlenight.core.util.Metadata;
 import me.limebyte.battlenight.core.util.SafeTeleporter;
 
 import org.bukkit.Bukkit;
 import org.bukkit.Location;
 import org.bukkit.entity.Player;
 
 public abstract class Battle {
 
     public BattleNightAPI api;
     private Arena arena;
     private boolean inProgress = false;
     private int minPlayers = 2;
     private int lives = -1;
 
     private Set<String> players = new HashSet<String>();
     private Set<String> spectators = new HashSet<String>();
 
     public Battle() {
 
     }
 
     public void onStart() {
 
     }
 
     public void onEnd() {
 
     }
 
     public void onPlayerDeath(BattleDeathEvent event) {
         int lives = Metadata.getInt(event.getPlayer(), "lives");
         Metadata.set(event.getPlayer(), "lives", --lives);
         if (lives > 0) event.setCancelled(true);
     }
 
     public boolean start() {
         if (isInProgress()) return false;
 
         int wpIndex = 0;
        List<Waypoint> waypoints = arena.getSpawnPoints();
 
         for (String name : players) {
             Player player = Bukkit.getPlayerExact(name);
             if (player == null) continue;
 
            if (wpIndex >= waypoints.size()) wpIndex = 0;
            SafeTeleporter.tp(player, waypoints.get(wpIndex).getLocation());
             wpIndex++;
 
             Metadata.set(player, "lives", getLives());
         }
 
         inProgress = true;
         onStart();
         return true;
     }
 
     public boolean stop() {
         for (String name : players) {
             Player player = Bukkit.getPlayerExact(name);
             if (player == null) continue;
             PlayerData.reset(player);
             PlayerData.restore(player, true, false);
             api.setPlayerClass(player, null);
             Metadata.remove(player, "lives");
         }
         players.clear();
 
         for (String name : spectators) {
             Player player = Bukkit.getPlayerExact(name);
             if (player == null) continue;
             PlayerData.reset(player);
             PlayerData.restore(player, true, false);
             api.setPlayerClass(player, null);
         }
         spectators.clear();
 
         inProgress = false;
         onEnd();
         return true;
     }
 
     public boolean isInProgress() {
         return inProgress;
     }
 
     /**
      * Adds the specified {@link Player} to the battle. This will return false
      * if it is unsuccessful.
      * 
      * @param player the Player to add
      * @return true if successful
      */
     public boolean addPlayer(Player player) {
         if (getArena() == null) {
             if (api.getArenas().isEmpty()) {
                 Messenger.tell(player, "No Arenas.");
                 return false;
             }
             arena = api.getRandomArena();
         }
 
         if (!arena.isSetup(1)) {
             Messenger.tell(player, "No Spawn Points.");
             return false;
         }
 
         if (!api.getLoungeWaypoint().isSet()) {
             Messenger.tell(player, Message.WAYPOINTS_UNSET);
             return false;
         }
 
         PlayerData.store(player);
         PlayerData.reset(player);
         players.add(player.getName());
         SafeTeleporter.tp(player, api.getLoungeWaypoint().getLocation());
         return true;
     }
 
     /**
      * Removes the specified {@link Player} to the battle. This will return
      * false if it is unsuccessful.
      * 
      * @param player the Player to remove
      * @return true if successful
      */
     public boolean removePlayer(Player player) {
         if (!containsPlayer(player)) return false;
         PlayerData.reset(player);
         PlayerData.restore(player, true, false);
         api.setPlayerClass(player, null);
         players.remove(player.getName());
         return true;
     }
 
     public boolean containsPlayer(Player player) {
         return players.contains(player.getName());
     }
 
     public Set<String> getPlayers() {
         return players;
     }
 
     public boolean addSpectator(Player player) {
         PlayerData.store(player);
         PlayerData.reset(player);
         spectators.add(player.getName());
         SafeTeleporter.tp(player, Bukkit.getPlayerExact((String) players.toArray()[0]).getLocation());
         return true;
     }
 
     public boolean removeSpectator(Player player) {
         PlayerData.reset(player);
         PlayerData.restore(player, true, false);
         spectators.remove(player.getName());
         return true;
     }
 
     public boolean containsSpectator(Player player) {
         return spectators.contains(player.getName());
     }
 
     public Set<String> getSpectators() {
         return spectators;
     }
 
     public Player getLeadingPlayer() {
         return null;
     }
 
     public Arena getArena() {
         return arena;
     }
 
     public boolean setArena(Arena arena) {
         if (isInProgress()) return false;
         this.arena = arena;
         return true;
     }
 
     public void respawn(Player player) {
         if (!containsPlayer(player)) return;
         PlayerData.reset(player);
         api.getPlayerClass(player).equip(player);
         SafeTeleporter.tp(player, arena.getRandomSpawnPoint().getLocation());
     }
 
     public Location toSpectator(Player player, boolean death) {
         if (!containsPlayer(player)) return null;
         Location loc;
 
         api.setPlayerClass(player, null);
         players.remove(player.getName());
         if (!death) PlayerData.reset(player);
 
         if (isInProgress() && players.size() >= minPlayers) {
             spectators.add(player.getName());
             loc = Bukkit.getPlayerExact((String) players.toArray()[0]).getLocation();
         } else {
             loc = PlayerData.getSavedLocation(player);
             if (!death) PlayerData.restore(player, true, false);
             stop();
         }
         return loc;
     }
 
     public int getLives() {
         return lives;
     }
 
     public void setLives(int lives) {
         this.lives = lives;
     }
 }
