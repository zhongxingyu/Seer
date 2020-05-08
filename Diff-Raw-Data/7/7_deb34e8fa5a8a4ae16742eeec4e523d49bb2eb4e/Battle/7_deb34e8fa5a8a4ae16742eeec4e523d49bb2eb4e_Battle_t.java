 package me.limebyte.battlenight.api.battle;
 
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Random;
 import java.util.Set;
 import java.util.logging.Level;
 
 import me.limebyte.battlenight.api.BattleNightAPI;
 import me.limebyte.battlenight.api.event.BattleDeathEvent;
 import me.limebyte.battlenight.api.util.PlayerData;
 import me.limebyte.battlenight.core.BattleNight;
 import me.limebyte.battlenight.core.listeners.SignListener;
 import me.limebyte.battlenight.core.util.Messenger;
 import me.limebyte.battlenight.core.util.Messenger.Message;
 import me.limebyte.battlenight.core.util.Metadata;
 import me.limebyte.battlenight.core.util.SafeTeleporter;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.GameMode;
 import org.bukkit.Location;
 import org.bukkit.entity.Player;
 
 public abstract class Battle {
 
     public BattleNightAPI api;
     public static final int INFINITE_LIVES = -1;
 
     private Arena arena;
     protected boolean inProgress = false;
     private int minPlayers = 2;
     private int maxPlayers = Integer.MAX_VALUE;
     private int lives = INFINITE_LIVES;
 
    private HashSet<String> players = new HashSet<String>();
     private Set<String> spectators = new HashSet<String>();
     private Set<String> leadingPlayers = new HashSet<String>();
 
     /* --------------- */
     /* General Methods */
     /* --------------- */
 
    @SuppressWarnings("unchecked")
     public boolean start() {
         if (isInProgress()) return false;
         if (getPlayers().size() < getMinPlayers()) return false;
         if (getPlayers().size() > getMaxPlayers()) return false;
         if (getArena() == null || !getArena().isSetup(1) || !getArena().isEnabled()) return false;
         if (!onStart()) return false;
 
         Iterator<String> it = getPlayers().iterator();
         while (it.hasNext()) {
             Player player = toPlayer(it.next());
             if (player == null) {
                 it.remove();
                 continue;
             }
 
             Metadata.remove(player, "ready");
             setLives(player, getBattleLives());
             Metadata.set(player, "kills", 0);
             Metadata.set(player, "deaths", 0);
         }
 
        leadingPlayers = (HashSet<String>) players.clone();
 
         teleportAllToSpawn();
         SignListener.cleanSigns();
         inProgress = true;
         return true;
     }
 
     public boolean stop() {
         if (!onStop()) return false;
 
         Iterator<String> pIt = getPlayers().iterator();
         while (pIt.hasNext()) {
             Player player = toPlayer(pIt.next());
             if (player == null) {
                 pIt.remove();
                 continue;
             }
 
             PlayerData.reset(player);
             PlayerData.restore(player, true, false);
             api.setPlayerClass(player, null);
             Metadata.remove(player, "kills");
             Metadata.remove(player, "deaths");
             pIt.remove();
         }
 
         Iterator<String> sIt = getSpectators().iterator();
         while (sIt.hasNext()) {
             Player player = toPlayer(sIt.next());
             if (player == null) {
                 sIt.remove();
                 continue;
             }
 
             PlayerData.reset(player);
             PlayerData.restore(player, true, false);
             api.setPlayerClass(player, null);
             Metadata.remove(player, "kills");
             Metadata.remove(player, "deaths");
             sIt.remove();
         }
 
         leadingPlayers.clear();
         arena = null;
         inProgress = false;
         return true;
     }
 
     /**
      * Adds the specified {@link Player} to the battle. This will return false
      * if it is unsuccessful.
      * 
      * @param player the Player to add
      * @return true if successful
      */
     public boolean addPlayer(Player player) {
         if (isInProgress()) return false;
 
         if (getArena() == null) {
             if (api.getArenas().isEmpty()) {
                 Messenger.tell(player, "No Arenas.");
                 return false;
             }
             setArena(api.getRandomArena());
         }
 
         if (!getArena().isSetup(1)) {
             Messenger.tell(player, "No Spawn Points.");
             return false;
         }
 
         if (!api.getLoungeWaypoint().isSet()) {
             Messenger.tell(player, Message.WAYPOINTS_UNSET);
             return false;
         }
 
         PlayerData.store(player);
         PlayerData.reset(player);
         getPlayers().add(player.getName());
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
         getPlayers().remove(player.getName());
         Metadata.remove(player, "ready");
         Metadata.remove(player, "kills");
         Metadata.remove(player, "deaths");
 
         if (shouldEnd()) stop();
         return true;
     }
 
     public boolean addSpectator(Player player) {
         PlayerData.store(player);
         PlayerData.reset(player);
         getSpectators().add(player.getName());
         player.setGameMode(GameMode.ADVENTURE);
         player.setAllowFlight(true);
         for (String n : getPlayers()) {
             if (Bukkit.getPlayerExact(n) != null) {
                 Bukkit.getPlayerExact(n).hidePlayer(player);
             }
         }
         SafeTeleporter.tp(player, Bukkit.getPlayerExact((String) getPlayers().toArray()[0]).getLocation());
         return true;
     }
 
     public boolean removeSpectator(Player player) {
         if (!containsSpectator(player)) return false;
         PlayerData.reset(player);
         PlayerData.restore(player, true, false);
         getSpectators().remove(player.getName());
         return true;
     }
 
     public void respawn(Player player) {
         if (!containsPlayer(player)) return;
         Messenger.debug(Level.INFO, "Respawning " + player.getName() + "...");
         PlayerData.reset(player);
         api.getPlayerClass(player).equip(player);
         SafeTeleporter.tp(player, getArena().getRandomSpawnPoint().getLocation());
     }
 
     public Location toSpectator(Player player, boolean death) {
         if (!containsPlayer(player)) return null;
         Messenger.debug(Level.INFO, "To spectator " + player.getName());
         Location loc;
 
         api.setPlayerClass(player, null);
         getPlayers().remove(player.getName());
         if (!death) PlayerData.reset(player);
         Metadata.remove(player, "ready");
 
         if (shouldEnd()) {
             loc = PlayerData.getSavedLocation(player);
             if (!death) PlayerData.restore(player, true, false);
 
             String winMessage = getWinMessage();
             Messenger.tell(player, winMessage);
             Messenger.tellEveryone(winMessage, true);
 
             Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(BattleNight.instance, new Runnable() {
                 @Override
                 public void run() {
                     stop();
                 }
             }, 1L);
         } else {
             getSpectators().add(player.getName());
             player.setGameMode(GameMode.ADVENTURE);
             player.setAllowFlight(true);
             for (String n : getPlayers()) {
                 if (Bukkit.getPlayerExact(n) != null) {
                     Bukkit.getPlayerExact(n).hidePlayer(player);
                 }
             }
 
             loc = Bukkit.getPlayerExact((String) getPlayers().toArray()[0]).getLocation();
         }
         return loc;
     }
 
     /* --------------- */
     /* Utility Methods */
     /* --------------- */
 
     protected Player toPlayer(String name) {
         Player player = Bukkit.getPlayerExact(name);
         return player;
     }
 
     protected void teleportAllToSpawn() {
         @SuppressWarnings("unchecked")
         List<Waypoint> waypoints = (ArrayList<Waypoint>) getArena().getSpawnPoints().clone();
         List<Waypoint> free = waypoints;
         Random random = new Random();
 
         for (String name : getPlayers()) {
             Player player = toPlayer(name);
             if (player == null || !player.isOnline()) continue;
 
             if (free.size() <= 0) free = waypoints;
 
             int id = random.nextInt(free.size());
             SafeTeleporter.tp(player, free.get(id).getLocation());
             free.remove(id);
         }
     }
 
     public boolean shouldEnd() {
         return isInProgress() && getPlayers().size() < 2;
     }
 
     /* ------------------- */
     /* Getters and Setters */
     /* ------------------- */
 
     /**
      * Returns the {@link Arena} that is set for this battle.
      * 
      * @return the arena
      * @see Arena
      */
     public Arena getArena() {
         return arena;
     }
 
     /**
      * Sets the {@link Arena} that will be used for this battle. The arena will
      * not be set if this battle is in progress.
      * 
      * @param arena the arena to set
      * @see Arena
      */
     public void setArena(Arena arena) {
         if (isInProgress()) return;
         this.arena = arena;
     }
 
     /**
      * Returns the minimum amount of players the battle requires before it can
      * be started.
      * 
      * @return the minPlayers
      */
     public int getMinPlayers() {
         return minPlayers;
     }
 
     /**
      * Sets the minimum amount of players the battle requires before it can be
      * started. This cannot be set below one.
      * 
      * @param minPlayers the minPlayers to set
      */
     public void setMinPlayers(int minPlayers) {
         if (getMinPlayers() < 1) return;
         this.minPlayers = minPlayers;
     }
 
     /**
      * Returns the maximum amount of players the battle can have. By default
      * this is set to {@link Integer.MAX_VALUE}.
      * 
      * @return the maxPlayers
      */
     public int getMaxPlayers() {
         return maxPlayers;
     }
 
     /**
      * Sets the maximum amount of players the battle can have. Setting this
      * value will prevent players from joining if the battle is full. This
      * cannot be set to a value that is less than the minimum.
      * 
      * @param maxPlayers the maxPlayers to set
      */
     public void setMaxPlayers(int maxPlayers) {
         if (maxPlayers < getMinPlayers()) return;
         this.maxPlayers = maxPlayers;
     }
 
     /**
      * @return the lives
      */
     public int getBattleLives() {
         return lives;
     }
 
     /**
      * @param lives the lives to set
      */
     public void setBattleLives(int lives) {
         this.lives = lives;
     }
 
     /**
      * @return the inProgress
      */
     public boolean isInProgress() {
         return inProgress;
     }
 
     /**
      * @return the players
      */
     public Set<String> getPlayers() {
         return players;
     }
 
     public boolean containsPlayer(Player player) {
         return getPlayers().contains(player.getName());
     }
 
     public Set<String> getLeadingPlayers() {
         return leadingPlayers;
     }
 
     public int getLives(Player player) {
         return Metadata.getInt(player, "lives");
     }
 
     public void setLives(Player player, int lives) {
         if (lives < 0) return;
         if (lives > Integer.MAX_VALUE) return;
         if (lives == INFINITE_LIVES) return;
 
         Metadata.set(player, "lives", lives);
     }
 
     public void incrementLives(Player player) {
         setLives(player, getLives(player) + 1);
     }
 
     public void decrementLives(Player player) {
         setLives(player, getLives(player) - 1);
     }
 
     protected String getWinMessage() {
         String message;
         Set<String> leading = getLeadingPlayers();
 
         if (leading.isEmpty() || leading.size() == getPlayers().size()) {
             message = Message.DRAW.getMessage();
         } else if (leading.size() == 1) {
             message = Messenger.format(Message.PLAYER_WON, leading.toArray()[0]);
         } else {
             message = Messenger.format(Message.PLAYER_WON, leading);
         }
 
         return message;
     }
 
     /**
      * @return the spectators
      */
     public Set<String> getSpectators() {
         return spectators;
     }
 
     public boolean containsSpectator(Player player) {
         return getSpectators().contains(player.getName());
     }
 
     /* ------ */
     /* Events */
     /* ------ */
 
     public abstract boolean onStart();
 
     public abstract boolean onStop();
 
     public void onPlayerDeath(BattleDeathEvent event) {
         Player player = event.getPlayer();
         Player killer = player.getKiller();
 
         if (killer != null) addKill(player);
 
         int deaths = Metadata.getInt(player, "deaths");
         Metadata.set(player, "deaths", ++deaths);
 
         decrementLives(player);
         int lives = getLives(player);
 
         if (lives > 0) {
             if (lives == 1) {
                 Messenger.tell(player, ChatColor.RED + "Last life!");
             } else {
                 Messenger.tell(player, "You have " + lives + " lives remaining.");
             }
             event.setCancelled(true);
         }
     }
 
     protected void addKill(Player player) {
         int kills = Metadata.getInt(player, "kills");
         int leadingKills = 0;
 
         Player leader = toPlayer(leadingPlayers.iterator().next());
         if (leader != null) leadingKills = Metadata.getInt(leader, "kills");
 
         Metadata.set(player, "kills", ++kills);
 
         if (leadingKills > kills) return;
         if (leadingKills < kills) leadingPlayers.clear();
 
         leadingPlayers.add(player.getName());
     }
 }
