 package co.mcme.pvp.gametypes;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.Color;
 import org.bukkit.GameMode;
 import org.bukkit.Location;
 import org.bukkit.OfflinePlayer;
 import org.bukkit.Sound;
 import org.bukkit.block.Sign;
 import org.bukkit.entity.Player;
 import org.bukkit.entity.Projectile;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 import org.bukkit.event.entity.PlayerDeathEvent;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.event.player.PlayerLoginEvent;
 import org.bukkit.event.player.PlayerQuitEvent;
 import org.bukkit.event.player.PlayerRespawnEvent;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.scoreboard.DisplaySlot;
 import org.bukkit.scoreboard.Objective;
 import org.bukkit.scoreboard.Score;
 import org.bukkit.scoreboard.Scoreboard;
 import org.bukkit.scoreboard.ScoreboardManager;
 import org.bukkit.scoreboard.Team;
 import org.bukkit.util.Vector;
 
 import co.mcme.pvp.MCMEPVP;
 import co.mcme.pvp.gameType;
 import co.mcme.pvp.util.armorColor;
 import co.mcme.pvp.util.gearGiver;
 import co.mcme.pvp.util.spectatorUtil;
 import co.mcme.pvp.util.textureSwitcher;
 import co.mcme.pvp.util.util;
 
 public class ringBearerGame extends gameType {
 
     public static int taskId;
     private int RedMates = 0;
     private int BlueMates = 0;
     private int m = 5;
     public Plugin plugin;
     private HashMap<Player, String> playing = new HashMap<Player, String>();
     public static HashMap<Player, String> ringBearers = new HashMap<Player, String>();
     boolean isTharbad = MCMEPVP.PVPMap.equalsIgnoreCase("tharbad");
     boolean isJoinable = true;
     boolean redHasBearer;
     boolean blueHasBearer;
     boolean spawnSwitch = false;
     ScoreboardManager manager;
     Scoreboard board;
     Team redteam;
     Team blueteam;
     Objective objective;
     OfflinePlayer dummyred = Bukkit.getOfflinePlayer(ChatColor.RED + "Red:");
     OfflinePlayer dummyblue = Bukkit.getOfflinePlayer(ChatColor.BLUE + "Blue:");
     Score redscore;
     Score bluescore;
 
     public ringBearerGame() {
         MCMEPVP.GameStatus = 1;
         manager = Bukkit.getScoreboardManager();
         board = manager.getNewScoreboard();
         redteam = board.registerNewTeam("Red Team");
         redteam.setPrefix(ChatColor.RED.toString());
         blueteam = board.registerNewTeam("Blue Team");
         blueteam.setPrefix(ChatColor.BLUE.toString());
         objective = board.registerNewObjective("Players Left", "dummy");
         objective.setDisplaySlot(DisplaySlot.SIDEBAR);
         redscore = objective.getScore(dummyred);
         bluescore = objective.getScore(dummyblue);
         // Broadcast
         Bukkit.getServer()
                 .broadcastMessage(
                 MCMEPVP.primarycolor
                 + "The next Game starts in a few seconds!");
         Bukkit.getServer().broadcastMessage(
                 MCMEPVP.primarycolor + "GameType is " + MCMEPVP.highlightcolor
                 + "Ring Bearer" + MCMEPVP.primarycolor + " on Map "
                 + MCMEPVP.highlightcolor + MCMEPVP.PVPMap + "!");
         Bukkit.getServer().broadcastMessage(
                 MCMEPVP.primarycolor
                 + "Hunt down the enemy's Ring Bearer to prevent them from respawning!");
         Bukkit.getServer()
                 .broadcastMessage(
                 MCMEPVP.primarycolor
                 + "All Participants will be assigned to a team and teleported to their spawn!");
         Bukkit.getServer()
                 .getScheduler()
                 .scheduleSyncDelayedTask(
                 Bukkit.getPluginManager().getPlugin("MCMEPVP"),
                 new Runnable() {
             public void run() {
                 ArrayList<Player> queued = new ArrayList<Player>();
                 MCMEPVP.queue.drainTo(queued);
                 Collections.shuffle(queued);
                 for (Player p : queued) {
                     textureSwitcher.switchTP(p);
                     if (p.isOnline()) {
                         if (BlueMates > RedMates) {
                             if (!redHasBearer) {
 
                                 addRingBearer(p, "red");
                             }
                             addTeam(p, "red");
                         } else {
                             if (BlueMates < RedMates) {
                                 if (!blueHasBearer) {
                                     addRingBearer(p, "blue");
                                 }
                                 addTeam(p, "blue");
                             } else {
                                 boolean random = (Math.random() < 0.5);
                                 if (random == true) {
                                     if (!redHasBearer) {
                                         addRingBearer(p, "red");
                                     }
                                     addTeam(p, "red");
                                 } else {
                                     if (!blueHasBearer) {
                                         addRingBearer(p, "blue");
                                     }
                                     addTeam(p, "blue");
                                 }
                             }
                         }
                         MCMEPVP.queue.remove(p);
                     } else {
                         MCMEPVP.queue.remove(p);
                         util.debug("Player `" + p.getName()
                                 + "` is not online!");
                     }
                 }
                 spectatorUtil.startingSpectators();
                 // Broadcast
                 Bukkit.getServer().broadcastMessage(
                         MCMEPVP.positivecolor
                         + "The Fight begins!");
                 for (Player rb : ringBearers.keySet()) {
                     rb.sendMessage(ChatColor.LIGHT_PURPLE
                             + "You are the Ring Bearer! Stay alive for as long as possible!");
                 }
             }
         }, 100L);
         CountdownTimer();
         displayBoard();
         teamCount();
     }
 
     @Override
     public void addPlayerDuringGame(Player p) {
         if (isJoinable) {
             if (BlueMates > RedMates) {
                 addTeam(p, "red");
             } else {
                 if (BlueMates < RedMates) {
                     addTeam(p, "blue");
                 } else {
                     boolean random = (Math.random() < 0.5);
                     if (random == true) {
                         addTeam(p, "red");
                     } else {
                         addTeam(p, "blue");
                     }
                 }
             }
         } else {
             p.sendMessage(MCMEPVP.negativecolor + "You can no longer join this game!");
         }
     }
 
     @Override
     public void addTeam(Player player, String Team) {
         Color col = armorColor.WHITE;
         switch (Team) {
             case "red":
                 player.getInventory().clear();
                 player.sendMessage(MCMEPVP.primarycolor + "You're now in Team "
                         + ChatColor.RED + "RED" + MCMEPVP.primarycolor + "!");
                 MCMEPVP.setPlayerTeam(player, Team);
                 redteam.addPlayer(player);
                 teamCount();
                 player.setGameMode(GameMode.ADVENTURE);
                 col = armorColor.RED;
                 break;
             case "blue":
                 player.getInventory().clear();
                 player.sendMessage(MCMEPVP.primarycolor + "You're now in Team "
                         + ChatColor.BLUE + "BLUE" + MCMEPVP.primarycolor + "!");
                 MCMEPVP.setPlayerTeam(player, Team);
                 blueteam.addPlayer(player);
                 teamCount();
                 player.setGameMode(GameMode.ADVENTURE);
                 col = armorColor.BLUE;
                 break;
         }
         player.setHealth(20);
         player.setFoodLevel(20);
         player.setSaturation((float) 20);
         gearGiver.loadout(player, true, isTharbad, true, "warrior", col,
                 "boating", Team);
         if (ringBearers.containsKey(player)) {
             player.getInventory().setItem(4, gearGiver.magicItem(false, 0, 1));
         }
         playing.put(player, Team);
         Location loc = getSpawn(player, MCMEPVP.getPlayerTeam(player));
         player.teleport(loc);
     }
 
     @Override
     public void onPlayerJoin(PlayerJoinEvent event) {
         if (MCMEPVP.GameStatus == 1 && isJoinable) {
             Player player = event.getPlayer();
             String Team = MCMEPVP.getPlayerTeam(player);
             if (Team.equals("red")) {
                 addTeam(player, "red");
             }
             if (Team.equals("blue")) {
                 addTeam(player, "blue");
             }
             if (Team.equals("spectator")) {
                 spectatorUtil.setSpectator(player);
             }
             Vector vec = MCMEPVP.Spawns.get(MCMEPVP.getPlayerTeam(player));
             Location loc = new Location(MCMEPVP.PVPWorld, vec.getX(),
                     vec.getY() + 0.5, vec.getZ());
             player.teleport(loc);
         }
         displayBoard();
         teamCount();
     }
 
     @Override
     public void onPlayerleaveServer(PlayerQuitEvent event) {
         Player p = event.getPlayer();
         String team = MCMEPVP.getPlayerTeam(event.getPlayer());
         if (hasRingBearer(team)) {
             if (isRingBearer(p)) {
                 String c = ringBearers.get(p);
                 removeRingBearer(p, c);
                 switchRingBearer(c);
             }
             if (team.equals("red")) {
                 redteam.removePlayer(p);
             }
             if (team.equals("blue")) {
                 blueteam.removePlayer(p);
             }
             teamCount();
         } else {
             String OldTeam = MCMEPVP.getPlayerTeam(event.getPlayer());
             if (OldTeam.equals("red")) {
                 redteam.removePlayer(p);
                 setSpectator(p);
             }
             if (OldTeam.equals("blue")) {
                 blueteam.removePlayer(p);
                 setSpectator(p);
             } else {
                 util.debug("Player already spectator");
             }
             teamCount();
         }
         checkGameEnd();
     }
 
     @Override
     public void onPlayerdie(PlayerDeathEvent event) {
         MCMEPVP.logKill(event);
         Player player = event.getEntity();
         String team = MCMEPVP.getPlayerTeam(player);
         Color col;
         if (player.getKiller() instanceof Player) {
             if (team.equals("spectator")) {
                 event.setDeathMessage(MCMEPVP.primarycolor + "Spectator " + player.getName() + " was tired watching this fight!");
             }
             //BONUS DROP FOR RINGBEARER
             if (ringBearers.containsKey(player.getKiller())) {
                 event.getDrops().add(gearGiver.magicItem(false, 0, 1));
             }
             //RingBearer Dies
             if (isRingBearer(player)) {
                 if (redHasBearer && team.equals("red")) {
                     Bukkit.broadcastMessage(ChatColor.DARK_RED + "Reds lost their RingBearer!");
                     removeRingBearer(player, team);
                 }
                 if (blueHasBearer && team.equals("blue")) {
                     Bukkit.broadcastMessage(ChatColor.DARK_RED + "Blues lost their RingBearer!");
                     removeRingBearer(player, team);
                 }
             }
             //Red Dies
             if (team.equals("red")) {
                 if (redHasBearer) {
                     event.setDeathMessage(ChatColor.RED + player.getName() + MCMEPVP.primarycolor + " was killed by " + ChatColor.BLUE + player.getKiller().getName());
                     event.getDrops().add(new ItemStack(364, 1));
                     event.getDrops().add(new ItemStack(262, 8));
                     col = armorColor.RED;
                     gearGiver.loadout(player, true, isTharbad, true, "warrior", col, "boating", team);
                 } else {
                     event.setDeathMessage(ChatColor.RED + player.getName() + MCMEPVP.primarycolor + " was killed by " + ChatColor.BLUE + player.getKiller().getName());
                     event.getDrops().add(new ItemStack(364, 1));
                     event.getDrops().add(new ItemStack(262, 8));
                     setSpectator(player);
                     redteam.removePlayer(player);
                 }
             }
             //Blue Dies
             if (team.equals("blue")) {
                 if (blueHasBearer) {
                     event.setDeathMessage(ChatColor.BLUE + player.getName() + MCMEPVP.primarycolor + " was killed by " + ChatColor.RED + player.getKiller().getName());
                     event.getDrops().add(new ItemStack(364, 1));
                     event.getDrops().add(new ItemStack(262, 8));
                     col = armorColor.BLUE;
                     gearGiver.loadout(player, true, isTharbad, true, "warrior", col, "boating", team);
                 } else {
                     event.setDeathMessage(ChatColor.BLUE + player.getName() + MCMEPVP.primarycolor + " was killed by " + ChatColor.RED + player.getKiller().getName());
                     event.getDrops().add(new ItemStack(364, 1));
                     event.getDrops().add(new ItemStack(262, 8));
                     setSpectator(player);
                     blueteam.removePlayer(player);
                 }
             }
             teamCount();
         } else {
             if (team.equals("spectator")) {
                 event.setDeathMessage(MCMEPVP.primarycolor + "Spectator "
                         + player.getName() + " was tired watching this fight!");
             }
             //RingBearer dies to elements
             if (isRingBearer(player)) {
                 if (isRingBearer(player) && team.equals("red")) {
                     Bukkit.broadcastMessage(ChatColor.DARK_RED + "Reds lost their RingBearer!");
                     removeRingBearer(player, team);
                 }
                 if (isRingBearer(player) && team.equals("blue")) {
                     Bukkit.broadcastMessage(ChatColor.DARK_RED + "Blues lost their RingBearer!");
                     removeRingBearer(player, team);
                 }
                 setSpectator(player);
             }
             //Red dies to elements
             if (team.equals("red")) {
                 if (redHasBearer) {
                     event.setDeathMessage(ChatColor.RED + player.getName()
                             + MCMEPVP.primarycolor + " was lost in battle");
                     col = armorColor.RED;
                     gearGiver.loadout(player, true, isTharbad, true, "warrior",
                             col, "boating", team);
                 } else {
                     event.setDeathMessage(ChatColor.RED + "Team Red " + MCMEPVP.primarycolor + "lost " + player.getName());
                     setSpectator(player);
                 }
                 if (!redHasBearer) {
                     redteam.removePlayer(player);
                 }
             }
             //Blue dies to elements
             if (team.equals("blue")) {
                 if (blueHasBearer) {
                     event.setDeathMessage(ChatColor.BLUE + player.getName() + MCMEPVP.primarycolor + " was lost in battle");
                     col = armorColor.BLUE;
                     gearGiver.loadout(player, true, isTharbad, true, "warrior", col, "boating", team);
                 } else {
                     event.setDeathMessage(ChatColor.BLUE + "Team Blue " + MCMEPVP.primarycolor + "lost " + player.getName());
                     setSpectator(player);
                 }
                 if (!blueHasBearer) {
                     blueteam.removePlayer(player);
                 }
             }
             teamCount();
         }
         checkGameEnd();
     }
 
     @Override
     public void onPlayerhit(EntityDamageByEntityEvent event) {
         Player defender = (Player) event.getEntity();
         Player attacker = (Player) event.getDamager();
         String attackerteam = MCMEPVP.getPlayerTeam(attacker);
         String defenderteam = MCMEPVP.getPlayerTeam(defender);
         if (attackerteam.equals(defenderteam)) {
             event.setCancelled(true);
         }
     }
 
     @Override
     public void onPlayerShoot(EntityDamageByEntityEvent event) {
         Player defender = (Player) event.getEntity();
         Player attacker = (Player) ((Projectile) event.getDamager()).getShooter();
         String attackerteam = MCMEPVP.getPlayerTeam(attacker);
         String defenderteam = MCMEPVP.getPlayerTeam(defender);
         if (attackerteam.equals(defenderteam)) {
             event.setCancelled(true);
         } else if (!attackerteam.equals(defenderteam)) {
             attacker.playSound(attacker.getLocation(), Sound.ORB_PICKUP, (float) 20, (float) 50);
         }
     }
 
     @Override
     public void onRespawn(PlayerRespawnEvent event) {
         Player player = event.getPlayer();
         String team = MCMEPVP.getPlayerTeam(player);
         Color col;
         if (ringBearers.containsValue(team)) {
             if (team.equals("red") && redHasBearer) {
                 col = armorColor.RED;
                 gearGiver.loadout(player, true, isTharbad, true, "warrior", col, "boating", team);
                 Location loc = getSpawn(player, "red");
                 event.setRespawnLocation(loc);
             }
             if (team.equals("blue") && blueHasBearer) {
                 col = armorColor.BLUE;
                 gearGiver.loadout(player, true, isTharbad, true, "warrior", col, "boating", team);
                 Location loc = getSpawn(player, "blue");
                 event.setRespawnLocation(loc);
             }
             teamCount();
        } else { 
        	if(team.equals("red")){
        		redteam.removePlayer(player);
        	}
        	if (team.equals("blue")){
        		blueteam.removePlayer(player);
        	}
        	checkGameEnd();
             setSpectator(player);
             Vector vec = MCMEPVP.Spawns.get(MCMEPVP.getPlayerTeam(player));
             Location spawnloc = new Location(MCMEPVP.PVPWorld, vec.getX(), vec.getY() + 0.5, vec.getZ());
             event.setRespawnLocation(spawnloc);
         }
     }
 
     private void checkGameEnd() {
         teamCount();
         if (BlueMates <= 0) {
             MCMEPVP.logGame("red", MCMEPVP.PVPMap, MCMEPVP.PVPGT);
 
             for (Map.Entry<String, String> entry : MCMEPVP.PlayerStatus.entrySet()) {
                 String key = entry.getKey();
                 String value = entry.getValue();
                 util.debug("player: " + key + " Team: " + value);
                 if (value.equalsIgnoreCase("red")) {
                     MCMEPVP.logJoin(key, MCMEPVP.PVPMap, MCMEPVP.PVPGT, true);
                 } else {
                     MCMEPVP.logJoin(key, MCMEPVP.PVPMap, MCMEPVP.PVPGT, false);
                 }
             }
 
             Bukkit.getServer().broadcastMessage(MCMEPVP.positivecolor + "Team " + ChatColor.RED + "Red" + MCMEPVP.positivecolor + " wins!");
             MCMEPVP.resetGame();
         } else if (RedMates <= 0) {
             MCMEPVP.logGame("blue", MCMEPVP.PVPMap, MCMEPVP.PVPGT);
 
             for (Map.Entry<String, String> entry : MCMEPVP.PlayerStatus.entrySet()) {
                 String key = entry.getKey();
                 String value = entry.getValue();
                 util.debug("player: " + key + " Team: " + value);
                 if (value.equalsIgnoreCase("blue")) {
                     MCMEPVP.logJoin(key, MCMEPVP.PVPMap, MCMEPVP.PVPGT, true);
                 } else {
                     MCMEPVP.logJoin(key, MCMEPVP.PVPMap, MCMEPVP.PVPGT, false);
                 }
             }
 
             Bukkit.getServer().broadcastMessage(MCMEPVP.positivecolor + "Team " + ChatColor.BLUE + "Blue" + MCMEPVP.positivecolor + " wins!");
             MCMEPVP.resetGame();
         } else {
             teamCount();
         }
     }
 
     public void CountdownTimer() {
         ringBearerGame.taskId = Bukkit
                 .getServer()
                 .getScheduler()
                 .scheduleSyncRepeatingTask(
                 Bukkit.getPluginManager().getPlugin("MCMEPVP"),
                 new Runnable() {
             @Override
             public void run() {
                 if (m > 0) {
                     m--;
                 }
                 if (m == 1) {
                     Bukkit.broadcastMessage(ChatColor.LIGHT_PURPLE + "Team spawns switching in 1 minute!");
                 }
                 if (m == 0) {
                     Bukkit.broadcastMessage(ChatColor.LIGHT_PURPLE + "Team spawns have now switched!");
                     if (spawnSwitch) {
                         spawnSwitch = false;
                     } else {
                         spawnSwitch = true;
                     }
                     m = 5;
                 }
             }
         }, 1200L, 1200L);
     }
 
     public static void stopTimer() {
         Bukkit.getScheduler().cancelTask(ringBearerGame.taskId);
     }
 
     public Location getSpawn(Player p, String s) {
         String team = s;
         if (spawnSwitch) {
             if (s.equals("red")) {
                 team = "blue";
             }
             if (s.equals("blue")) {
                 team = "red";
             }
         }
         Vector vec = MCMEPVP.Spawns.get(team);
         Location loc = new Location(MCMEPVP.PVPWorld, vec.getX(), vec.getY() + 0.5, vec.getZ());
         return loc;
     }
 
     @Override
     public int team1count() {
         return RedMates;
     }
 
     @Override
     public int team2count() {
         return BlueMates;
     }
 
     @Override
     public String team1() {
         return "red";
     }
 
     @Override
     public String team2() {
         return "blue";
     }
 
     @Override
     public Scoreboard getBoard() {
         return board;
     }
 
     @Override
     public void claimLootSign(Sign sign) {
     }
 
     @Override
     public void clearBoard() {
         board.clearSlot(DisplaySlot.SIDEBAR);
         blueteam.unregister();
         redteam.unregister();
         objective.unregister();
     }
 
     @Override
     public void displayBoard() {
         for (Player p : Bukkit.getOnlinePlayers()) {
             p.setScoreboard(board);
         }
     }
 
     @Override
     public HashMap<Player, String> getPlaying() {
         return playing;
     }
 
     private void teamCount() {
         RedMates = redteam.getSize();
         BlueMates = blueteam.getSize();
         redscore.setScore(RedMates);
         bluescore.setScore(BlueMates);
     }
 
     private void removeRingBearer(Player p, String team) {
         if (ringBearers.containsKey(p)) {
             if (team.equals("red")) {
                 redHasBearer = false;
                 redteam.removePlayer(p);
             }
             if (team.equals("blue")) {
                 blueHasBearer = false;
                 blueteam.removePlayer(p);
             }
             p.getInventory().clear();
             setSpectator(p);
             ringBearers.remove(p);
             isJoinable = false;
             teamCount();
         }
     }
 
     private void addRingBearer(Player p, String team) {
         if (team.equals("red") && !redHasBearer) {
             ringBearers.put(p, team);
             redHasBearer = true;
             isJoinable = true;
         }
         if (team.equals("blue") && !blueHasBearer) {
             ringBearers.put(p, team);
             blueHasBearer = true;
             isJoinable = true;
         }
     }
 
     private void switchRingBearer(String team) {
         for (Player p : Bukkit.getOnlinePlayers()) {
             String Status = MCMEPVP.getPlayerTeam(p);
             if (Status.equals(team) && !ringBearers.containsValue(team)) {
                 addRingBearer(p, team);
                 addTeam(p, team);
                 p.sendMessage(MCMEPVP.positivecolor + "Your are now the RingBearer!");
             }
         }
     }
 
     public void setSpectator(Player p) {
         MCMEPVP.setPlayerTeam(p, "spectator");
         //TODO invisible spectators using hidePlayer
     }
 
     private boolean isRingBearer(Player p) {
         return ringBearers.containsKey(p);
     }
 
     private boolean hasRingBearer(String team) {
         return ringBearers.containsValue(team);
     }
 
     @Override
     public boolean isJoinable() {
         return isJoinable;
     }
 
     @Override
     public void onPlayerLogin(PlayerLoginEvent event) {
         // Do nothing
     }
 
     @Override
     public boolean allowBlockBreak() {
         return false;
     }
 
     @Override
     public boolean allowBlockPlace() {
         return false;
     }
 
     @Override
     public boolean allowContainerIteraction() {
         return false;
     }
 
     @Override
     public boolean allowExplosionLogging() {
         // TODO Auto-generated method stub
         return false;
     }
 
     @Override
     public Objective getObjective() {
         return objective;
     }
 }
