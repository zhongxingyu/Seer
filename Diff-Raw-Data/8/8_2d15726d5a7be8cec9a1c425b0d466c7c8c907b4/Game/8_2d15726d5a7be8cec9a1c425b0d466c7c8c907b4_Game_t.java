 package to.joe.j2mc.survival;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.HashSet;
 
 import org.bukkit.ChatColor;
 import org.bukkit.World;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.entity.Player;
 
 import to.joe.j2mc.maps.J2MC_Maps;
 import to.joe.j2mc.survival.runnable.DeathCannon;
 
 public class Game {
 
     public static enum GameStatus {
         PreRound, Countdown, InGame, PostRound,
     }
 
     public static enum LossMethod {
         Disconnect, Killed, Left,
     }
 
     private GameStatus status;
     private int minPlayers;
     private int maxPlayers;
     private HashSet<Integer> breakableBlocks;
     private SpawnManager spawnManager;
     private ArrayList<String> participants = new ArrayList<String>();
     private ArrayList<String> deadPlayers = new ArrayList<String>();
     private ArrayList<String> readyPlayers = new ArrayList<String>();
     private FileConfiguration mapConfig;
     private J2MC_Survival plugin;
     private World gameWorld;
 
     public Game(String mapName, J2MC_Survival survival) {
         plugin = survival;
         plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, new DeathCannon(plugin), 1000, 1000);
 
         gameWorld = J2MC_Maps.getGameWorld();
 
         mapConfig = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), mapName + ".yml"));
         minPlayers = mapConfig.getInt("minPlayers");
         maxPlayers = mapConfig.getInt("maxPlayers");
         spawnManager = new SpawnManager(gameWorld, mapConfig.getStringList("spawns"), plugin);
         breakableBlocks = new HashSet<Integer>(mapConfig.getIntegerList("breakableBlocks"));
         for (Player p : plugin.getServer().getOnlinePlayers()) {
             participants.add(p.getName());
         }
         if (participants.size() < 2) {
            plugin.getServer().broadcastMessage(ChatColor.RED + "Not enough players to start a game");
             J2MC_Maps.finished();
             return;
         }
         startCountdown();
     }
 
     public GameStatus getStatus() {
         return status;
     }
 
     public ArrayList<String> getParticipants() {
         return participants;
     }
 
     public ArrayList<String> getReadyPlayers() {
         return readyPlayers;
     }
 
     public ArrayList<String> getDeadPlayers() {
         return deadPlayers;
     }
 
     public HashSet<Integer> getBreakableBlocks() {
         return breakableBlocks;
     }
 
     public SpawnManager getSpawnPointManager() {
         return spawnManager;
     }
 
     public int getMaxPlayers() {
         return maxPlayers;
     }
 
     public int getMinPlayers() {
         return minPlayers;
     }
 
     //TODO Maybe optimize this
     public void announceDead() {
         if (status != GameStatus.InGame)
             return;
         if (deadPlayers.size() == 0) {
             plugin.getServer().broadcastMessage(ChatColor.AQUA + "In the previous day no players were eliminated");
             return;
         }
         if (deadPlayers.size() > 1)
             plugin.getServer().broadcastMessage(ChatColor.AQUA + "In the previous day, the following players were eliminated");
         else
             plugin.getServer().broadcastMessage(ChatColor.AQUA + "In the previous day, the following player was eliminated");
         int offset = 0;
         for (final String s : deadPlayers) {
             offset += 30;
             plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                 @Override
                 public void run() {
                     plugin.getServer().broadcastMessage(ChatColor.RED + s);
                 }
             }, offset);
         }
         offset += 30;
         plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
             @Override
             public void run() {
                 plugin.getServer().broadcastMessage(ChatColor.AQUA + "" + participants.size() + " players remain");
             }
         }, offset);
         deadPlayers.clear();
     }
 
     //TODO Maybe optimize this
     public void handleLoss(Player p, LossMethod m) {
         String n = p.getName();
         switch (m) {
             case Disconnect:
                 plugin.getServer().broadcast(ChatColor.RED + n + ChatColor.AQUA + " has disconnected", "j2mc.chat.spectator");
                 break;
             case Killed:
                 plugin.getServer().broadcast(ChatColor.RED + n + ChatColor.AQUA + " has been killed", "j2mc.chat.spectator");
                 break;
             case Left:
                 plugin.getServer().broadcast(ChatColor.RED + n + ChatColor.AQUA + " has left the game", "j2mc.chat.spectator");
                 break;
         }
         plugin.setSpectate(p, true);
         deadPlayers.add(n);
         final boolean weather = p.getWorld().isThundering();
         p.getWorld().strikeLightningEffect(p.getLocation());
         plugin.getServer().broadcastMessage(ChatColor.AQUA + "You hear the sound of a cannon in the distance");
         p.sendMessage(ChatColor.RED + "You have been eliminated");
         p.getWorld().setStorm(weather);
         participants.remove(n);
         checkForWinner();
     }
 
     //TODO Maybe optimize this
     public void checkForWinner() {
         if (participants.size() == 1) {
             String winner = participants.get(0);
             Player p = plugin.getServer().getPlayer(winner);
             spawnManager.preparePlayer(p);
             plugin.setSpectate(p, true);
             p.teleport(gameWorld.getSpawnLocation());
             plugin.getServer().broadcastMessage(ChatColor.RED + winner + ChatColor.AQUA + " has won the survival games!");
             plugin.stopGame();
         }
     }
 
     //TODO Maybe optimize this
     public void startCountdown() {
         if (plugin.tubeMines)
             plugin.getServer().broadcastMessage(ChatColor.RED + "Mines have been" + ChatColor.BOLD + " armed");
         plugin.getServer().broadcastMessage(ChatColor.AQUA + "The survival games begin in " + plugin.countdown + " seconds");
         plugin.getServer().broadcastMessage(ChatColor.AQUA + "Best of luck");
         for (int x = 1; x < plugin.countdown; x++) {
             if (x <= 10 || x % 10 == 0)
                 scheduleCountdownMessage(x, plugin.countdown);
         }
         
         plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
             @Override
             public void run() {
                 status = GameStatus.InGame;
                 if (plugin.tubeMines)
                     plugin.getServer().broadcastMessage(ChatColor.RED + "Mines have been" + ChatColor.BOLD + " disarmed");
                 plugin.getServer().broadcastMessage(ChatColor.RED + "" + ChatColor.BOLD + "BEGIN!");
             }
         }, plugin.countdown * 20);
     }
 
     //TODO Maybe optimize this
     public void scheduleCountdownMessage(final int time, final int totalTime) {
         plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
             @Override
             public void run() {
                 if (status != GameStatus.Countdown)
                     return;
                 int number = totalTime - (totalTime - time);
                 if (number == 1)
                     plugin.getServer().broadcastMessage(ChatColor.RED + "" + ChatColor.BOLD + number + " second");
                 else if (number <= 5)
                     plugin.getServer().broadcastMessage(ChatColor.RED + "" + ChatColor.BOLD + number + " seconds");
                 else
                     plugin.getServer().broadcastMessage(ChatColor.AQUA + "" + number + " seconds");
             }
         }, (totalTime - time) * 20);
     }
 
 }
