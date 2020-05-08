 package me.limebyte.battlenight.core.battle;
 
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Random;
 import java.util.Set;
 import java.util.logging.Level;
 
 import me.limebyte.battlenight.api.BattleNightAPI;
 import me.limebyte.battlenight.api.battle.Arena;
 import me.limebyte.battlenight.api.battle.Battle;
import me.limebyte.battlenight.api.battle.Waypoint;
 import me.limebyte.battlenight.api.managers.ArenaManager;
 import me.limebyte.battlenight.api.managers.SpectatorManager;
 import me.limebyte.battlenight.api.util.Message;
 import me.limebyte.battlenight.api.util.Messenger;
 import me.limebyte.battlenight.api.util.Timer;
 import me.limebyte.battlenight.core.BattleNight;
 import me.limebyte.battlenight.core.listeners.SignListener;
 import me.limebyte.battlenight.core.tosort.Metadata;
 import me.limebyte.battlenight.core.tosort.PlayerData;
 import me.limebyte.battlenight.core.tosort.SafeTeleporter;
 import me.limebyte.battlenight.core.util.BattleScoreboard;
 import me.limebyte.battlenight.core.util.BattleTimer;
 
 import org.bukkit.Bukkit;
 import org.bukkit.Location;
 import org.bukkit.entity.Player;
 
 public abstract class SimpleBattle implements Battle {
 
     public BattleNightAPI api;
 
     private BattleTimer timer;
     private BattleScoreboard scoreboard;
     private int minPlayers;
     private int maxPlayers;
 
     private Arena arena;
     protected boolean inProgress = false;
 
     private HashSet<String> players = new HashSet<String>();
     private Set<String> leadingPlayers = new HashSet<String>();
 
     public SimpleBattle(BattleNightAPI api, int duration, int minPlayers, int maxPlayers) {
         this.api = api;
 
         timer = new BattleTimer(api, this, duration);
         scoreboard = new BattleScoreboard(this);
 
         this.minPlayers = minPlayers;
         this.maxPlayers = maxPlayers;
     }
 
     /* --------------- */
     /* General Methods */
     /* --------------- */
 
     @Override
     public void addDeath(Player player) {
         Metadata.set(player, "deaths", getDeaths(player) + 1);
         getScoreboard().updateScores(player);
     }
 
     @Override
     public void addKill(Player player) {
         int kills = getKills(player) + 1;
         int leadingKills = 0;
 
         Player leader = toPlayer(leadingPlayers.iterator().next());
         if (leader != null) {
             leadingKills = getKills(leader);
         }
 
         Metadata.set(player, "kills", kills);
 
         if (leadingKills > kills) return;
         if (leadingKills < kills) {
             leadingPlayers.clear();
         }
 
         leadingPlayers.add(player.getName());
         getScoreboard().updateScores(player);
     }
 
     @Override
     public boolean addPlayer(Player player) {
         Messenger messenger = api.getMessenger();
 
         if (isInProgress()) {
             messenger.tell(player, Message.BATTLE_IN_PROGRESS);
             return false;
         }
 
         if (containsPlayer(player)) {
             messenger.tell(player, Message.ALREADY_IN_BATTLE);
             return false;
         }
 
         if (getPlayers().size() + 1 > getMaxPlayers()) {
             messenger.tell(player, Message.BATTLE_FULL);
             return false;
         }
 
         ArenaManager arenaManager = api.getArenaManager();
 
         if (!arenaManager.getLounge().isSet()) {
             messenger.tell(player, Message.WAYPOINTS_UNSET);
             return false;
         }
 
         if (getArena() == null) {
             if (arenaManager.getReadyArenas(1).isEmpty()) {
                 messenger.tell(player, Message.NO_ARENAS);
                 return false;
             }
             setArena(arenaManager.getRandomArena(1));
         }
 
         PlayerData.store(player);
         PlayerData.reset(player);
         getPlayers().add(player.getName());
         getScoreboard().addPlayer(player);
         SafeTeleporter.tp(player, arenaManager.getLounge().getLocation());
         messenger.tell(player, Message.JOINED_BATTLE, arena);
         messenger.tellEveryoneExcept(player, Message.PLAYER_JOINED_BATTLE, player);
         if (!arena.getTexturePack().isEmpty()) {
             player.setTexturePack(arena.getTexturePack());
         }
         return true;
     }
 
     @Override
     public boolean containsPlayer(Player player) {
         return getPlayers().contains(player.getName());
     }
 
     @Override
     public Arena getArena() {
         return arena;
     }
 
     @Override
     public int getDeaths(Player player) {
         return Metadata.getInt(player, "deaths");
     }
 
     /* --------------- */
     /* Utility Methods */
     /* --------------- */
 
     @Override
     public double getKDR(Player player) {
         int kills = getKills(player);
         int deaths = getDeaths(player);
 
         if (deaths == 0) deaths = 1;
         return kills / deaths;
     }
 
     @Override
     public int getKills(Player player) {
         return Metadata.getInt(player, "kills");
     }
 
     @Override
     public Set<String> getLeadingPlayers() {
         return leadingPlayers;
     }
 
     /* ------------------- */
     /* Getters and Setters */
     /* ------------------- */
 
     @Override
     public int getMaxPlayers() {
         return maxPlayers;
     }
 
     @Override
     public int getMinPlayers() {
         return minPlayers;
     }
 
     /**
      * @return the players
      */
     @Override
     public Set<String> getPlayers() {
         return players;
     }
 
     @Override
     public Timer getTimer() {
         return timer;
     }
 
     /**
      * @return if in progress
      */
     @Override
     public boolean isInProgress() {
         return inProgress;
     }
 
     @Override
     public abstract boolean onStart();
 
     @Override
     public abstract boolean onStop();
 
     @Override
     public boolean removePlayer(Player player) {
         if (!containsPlayer(player)) return false;
         PlayerData.reset(player);
         PlayerData.restore(player, true, false);
         api.setPlayerClass(player, null);
         getPlayers().remove(player.getName());
         getScoreboard().removePlayer(player);
         Metadata.remove(player, "ready");
         Metadata.remove(player, "kills");
         Metadata.remove(player, "deaths");
         api.getSpectatorManager().removeTarget(player);
 
         if (shouldEnd()) {
             stop();
         }
         return true;
     }
 
     @Override
     public Location respawn(Player player) {
         if (!containsPlayer(player)) return null;
         api.getMessenger().debug(Level.INFO, "Respawning " + player.getName() + "...");
         PlayerData.reset(player);
 
         Location loc = getArena().getRandomSpawnPoint().getLocation();
         api.getPlayerClass(player).equip(player);
         SafeTeleporter.tp(player, loc);
         return loc.add(0, 0.5, 0);
     }
 
     @Override
     public void setArena(Arena arena) {
         if (isInProgress()) return;
         this.arena = arena;
     }
 
     @Override
     public void setMaxPlayers(int maxPlayers) {
         if (maxPlayers < getMinPlayers()) return;
         this.maxPlayers = maxPlayers;
     }
 
     @Override
     public void setMinPlayers(int minPlayers) {
         if (getMinPlayers() < 1) return;
         this.minPlayers = minPlayers;
     }
 
     public boolean shouldEnd() {
         int constraint = inProgress ? 2 : 1;
         return getPlayers().size() < constraint;
     }
 
     @Override
     @SuppressWarnings("unchecked")
     public boolean start() {
         if (isInProgress()) return false;
         if (getArena() == null || !getArena().isSetup(1) || !getArena().isEnabled()) return false;
 
         Messenger messenger = api.getMessenger();
 
         if (getPlayers().size() < getMinPlayers()) {
             messenger.tellEveryone(Message.NOT_ENOUGH_PLAYERS, getMinPlayers() - getPlayers().size());
             return false;
         }
 
         if (!onStart()) return false;
 
         Iterator<String> it = getPlayers().iterator();
         while (it.hasNext()) {
             Player player = toPlayer(it.next());
             if (player == null) {
                 it.remove();
                 continue;
             }
 
             Metadata.remove(player, "ready");
             Metadata.set(player, "kills", 0);
             Metadata.set(player, "deaths", 0);
             api.getSpectatorManager().addTarget(player);
         }
 
         leadingPlayers = (HashSet<String>) players.clone();
 
         teleportAllToSpawn();
 
         timer.start();
         inProgress = true;
 
         messenger.tellEveryone(Message.BATTLE_STARTED);
 
         SignListener.cleanSigns();
         return true;
     }
 
     @Override
     public boolean stop() {
         if (!onStop()) return false;
 
         if (timer.isRunning()) {
             timer.stop();
         }
 
         if (inProgress) {
             api.getMessenger().tellEveryone(getWinMessage());
         }
 
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
             getScoreboard().removePlayer(player);
             Metadata.remove(player, "kills");
             Metadata.remove(player, "deaths");
             api.getSpectatorManager().removeTarget(player);
             pIt.remove();
         }
 
         SpectatorManager spectatorManager = api.getSpectatorManager();
         for (String name : spectatorManager.getSpectators()) {
             Player player = toPlayer(name);
             if (player == null) {
                 continue;
             }
             spectatorManager.removeSpectator(player);
         }
 
         leadingPlayers.clear();
         arena = null;
         inProgress = false;
         return true;
     }
 
     public Location toSpectator(Player player, boolean death) {
         if (!containsPlayer(player)) return null;
         Messenger messenger = api.getMessenger();
 
         messenger.debug(Level.INFO, "To spectator " + player.getName());
         Location loc;
 
         api.setPlayerClass(player, null);
         getPlayers().remove(player.getName());
         api.getSpectatorManager().removeTarget(player);
         if (!death) {
             PlayerData.reset(player);
         }
         Metadata.remove(player, "ready");
 
         if (shouldEnd()) {
             loc = PlayerData.getSavedLocation(player);
             if (!death) {
                 PlayerData.restore(player, true, false);
             }
 
             String winMessage = getWinMessage();
             messenger.tell(player, winMessage);
             messenger.tellEveryone(winMessage);
 
             Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(BattleNight.instance, new Runnable() {
                 @Override
                 public void run() {
                     stop();
                 }
             }, 1L);
         } else {
             loc = api.getSpectatorManager().addSpectator(player, false);
             messenger.tellEveryone(player.getDisplayName() + " is now a spectator.");
         }
         return loc;
     }
 
     protected String getWinMessage() {
         String message;
         Set<String> leading = getLeadingPlayers();
 
         if (leading.isEmpty()) {
             message = Message.DRAW.getMessage();
         } else if (leading.size() == 1) {
             message = api.getMessenger().format(Message.PLAYER_WON, leading.toArray()[0]);
         } else {
             message = api.getMessenger().format(Message.PLAYER_WON, leading);
         }
 
         return message;
     }
 
     // TODO Add to API
     public BattleScoreboard getScoreboard() {
         return scoreboard;
     }
 
     /* ------ */
     /* Events */
     /* ------ */
 
     protected void teleportAllToSpawn() {
        ArrayList<Waypoint> waypoints = new ArrayList<Waypoint>(getArena().getSpawnPoints());
        ArrayList<Waypoint> free = new ArrayList<Waypoint>(waypoints);
         Random random = new Random();
 
         for (String name : getPlayers()) {
             Player player = toPlayer(name);
             if (player == null || !player.isOnline()) {
                 continue;
             }
 
             if (free.size() <= 0) {
                free = new ArrayList<Waypoint>(waypoints);
             }
 
             int id = random.nextInt(free.size());
             SafeTeleporter.queue(player, free.get(id));
             free.remove(id);
         }
 
         SafeTeleporter.startTeleporting();
     }
 
     protected Player toPlayer(String name) {
         Player player = Bukkit.getPlayerExact(name);
         return player;
     }
 }
