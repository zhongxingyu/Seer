 package me.naithantu.ArenaPVP.Gamemodes.Gamemodes;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.List;
 
 import com.google.common.base.Joiner;
 import me.naithantu.ArenaPVP.Arena.ArenaExtras.*;
 import me.naithantu.ArenaPVP.Arena.PlayerExtras.PlayerScore;
 import me.naithantu.ArenaPVP.Arena.Runnables.KillTimer;
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.Material;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Arrow;
 import org.bukkit.entity.Player;
 import org.bukkit.entity.Projectile;
 import org.bukkit.entity.Snowball;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 import org.bukkit.event.entity.PlayerDeathEvent;
 import org.bukkit.event.entity.ProjectileHitEvent;
 import org.bukkit.inventory.ItemStack;
 import org.mcsg.double0negative.tabapi.TabAPI;
 
 import me.naithantu.ArenaPVP.ArenaManager;
 import me.naithantu.ArenaPVP.ArenaPVP;
 import me.naithantu.ArenaPVP.TabController;
 import me.naithantu.ArenaPVP.Arena.Arena;
 import me.naithantu.ArenaPVP.Arena.ArenaPlayer;
 import me.naithantu.ArenaPVP.Arena.ArenaTeam;
 import me.naithantu.ArenaPVP.Gamemodes.Gamemode;
 import me.naithantu.ArenaPVP.Storage.YamlStorage;
 import me.naithantu.ArenaPVP.Util.Util;
 
 public class OITC extends Gamemode {
     private Comparator<ArenaPlayer> comp;
 
     private ArenaUtil arenaUtil;
 
     private ItemStack arrow;
 
     public OITC(ArenaPVP plugin, ArenaManager arenaManager, Arena arena, ArenaSettings settings, ArenaSpawns arenaSpawns, ArenaUtil arenaUtil, YamlStorage arenaStorage, TabController tabController) {
         super(plugin, arenaManager, arena, settings, arenaSpawns, arenaUtil, arenaStorage, tabController, Gamemodes.PAINTBALL);
         this.arenaUtil = arenaUtil;
         arrow = new ItemStack(Material.ARROW, 1);
     }
 
     @Override
     public String getName() {
         return "One in the Chamber";
     }
 
     @Override
     public boolean isTeamGame() {
         return false;
     }
 
     @Override
     public void sendScore(CommandSender sender) {
         Util.msg(sender, "Score:");
         for (ArenaTeam team : arena.getTeams()) {
             for(ArenaPlayer arenaPlayer: team.getPlayers()){
                 Util.msg(sender, team.getTeamColor() + arenaPlayer.getPlayerName() + ChatColor.WHITE + ": " + arenaPlayer.getPlayerScore().getKills());
             }
         }
     }
 
     @Override
     public void onPlayerDamage(EntityDamageByEntityEvent event, ArenaPlayer arenaPlayer) {
         super.onPlayerDamage(event, arenaPlayer);
         final Player damaged = (Player) event.getEntity();
 
         // If the damage is not allowed, then the event will be cancelled.
         if (!event.isCancelled()) {
             // Just need to check if the damage was done by a arrow.
             if (event.getDamager() instanceof Arrow) {
                 Arrow arrow = (Arrow) event.getDamager();
                 //Check if arrow was shot by a player.
                 if (arrow.getShooter() instanceof Player) {
                     //Check if player isn't already dying
                     if (!arenaPlayer.isDying()){
                         Player killer = (Player) arrow.getShooter();
                         //Can't kill yourself
                         if (!killer.getName().equalsIgnoreCase(damaged.getName())) {
                             KillTimer killTimer = new KillTimer(arenaPlayer, (Player) event.getEntity());
                             killTimer.runTaskLater(plugin, 1);
                             arrow.remove();
                         }
                     }
                 }
             }
         }
     }
 
     @Override
     public void onPlayerDeath(PlayerDeathEvent event, ArenaPlayer arenaPlayer) {
        // Remove the death message, death messages are being sent in onPlayerKill
        event.setDeathMessage(null);

         super.onPlayerDeath(event, arenaPlayer);
         Player killer = event.getEntity().getKiller();
         if (killer != null) {
             PlayerScore playerScore = arenaPlayer.getPlayerScore();
             Player player = event.getEntity();
             onPlayerKill(killer, player, playerScore, arenaPlayer);
         }
     }
 
     private void onPlayerKill(Player killer, Player player, PlayerScore playerScore, ArenaPlayer arenaPlayer) {
         arenaUtil.sendMessageAll(ChatColor.GOLD + killer.getName() + ChatColor.WHITE + " killed " + ChatColor.GOLD + player.getName() + ChatColor.WHITE + "!");
 
         if (playerScore.getDeaths() >= settings.getScoreLimit()) {
             arenaPlayer.setPlayerState(ArenaPlayerState.SPECTATING);
             arenaUtil.sendMessageAllExcept(ChatColor.GOLD + player.getName() + ChatColor.WHITE + " has been eliminated!", player.getName());
             Util.msg(player, "You have been eliminated!");
         } else {
             Util.msg(player, "You have " + (settings.getScoreLimit() - playerScore.getDeaths()) + " lives remaining!");
         }
 
         if (checkRemainingPlayers() == 1) {
             List<ArenaPlayer> winningPlayers = getWinningPlayer();
             if (winningPlayers.size() == 1) {
                 arena.stopGame(winningPlayers.get(0));
             } else {
                 arenaUtil.sendMessageAll(Joiner.on(", ").join(winningPlayers) + " have won the game!");
                 arena.stopGame();
             }
         } else {
             killer.getInventory().addItem(arrow);
             sortLists();
             updateTabs();
         }
     }
 
     @Override
     public void onProjectileHit(ProjectileHitEvent event, ArenaPlayer arenaPlayer) {
         if (event.getEntity() instanceof Arrow) {
             event.getEntity().remove();
         }
     }
 
     private int checkRemainingPlayers() {
         int remainingPlayers = 0;
         for (ArenaTeam team : arena.getTeams()) {
             for (ArenaPlayer arenaPlayer : team.getPlayers()) {
                 if (arenaPlayer.getPlayerState() != ArenaPlayerState.SPECTATING) {
                     remainingPlayers++;
                 }
             }
         }
         return remainingPlayers;
     }
 
     private List<ArenaPlayer> getWinningPlayer() {
         int highestScore = 0;
         for (ArenaTeam team : arena.getTeams()) {
             for (ArenaPlayer arenaPlayer : team.getPlayers()) {
                 if (arenaPlayer.getPlayerScore().getKills() > highestScore) {
                     highestScore = arenaPlayer.getPlayerScore().getKills();
                 }
             }
         }
 
         List<ArenaPlayer> winningPlayers = new ArrayList<ArenaPlayer>();
         for (ArenaTeam team : arena.getTeams()) {
             for (ArenaPlayer arenaPlayer : team.getPlayers()) {
                 if (arenaPlayer.getPlayerScore().getKills() == highestScore) {
                     winningPlayers.add(arenaPlayer);
                 }
             }
         }
         return winningPlayers;
     }
 
     @Override
     public void updateTabs() {
         if (!tabController.hasTabAPI()) return;
         String gameStatus = Util.capaltizeFirstLetter(arena.getArenaState().toString());
         String arenaName = arena.getArenaName();
 
         List<ArenaPlayer> players = arena.getTeams().get(0).getPlayers();
         String nrOfPlayers = players.size() + " Players";
         String nrOfSpectators = ChatColor.GRAY + "" + arena.getArenaSpectators().getSpectators().size() + " Spectators";
 
         String[] playerTab; int x = 0;
         if (arena.getArenaState() == ArenaState.PLAYING) {
             if (players.size() > 10) {
                 playerTab = new String[11 * 3];
             } else {
                 playerTab = new String[players.size() * 3];
             }
             int rank = 1;
             List<String> kills = new ArrayList<>();
             for (ArenaPlayer player : players) {
                 playerTab[x] = ChatColor.GRAY + "Rank " + rank + "  ->"; rank++; x++;
                 playerTab[x] = player.getPlayerName(); x++;
                 String killString = player.getPlayerScore().getKills() + " Kills";
                 while (kills.contains(killString)) {
                     killString = killString + " ";
                 }
                 kills.add(killString);
                 playerTab[x] = ChatColor.RED + killString; x++;
                 if (x > 33) break;
             }
         } else {
             if (players.size() > 32) {
                 playerTab = new String[33];
             } else {
                 playerTab = new String[players.size()];
             }
             for (ArenaPlayer player : players) {
                 playerTab[x] = player.getPlayerName();
                 x++;
                 if (x > 33) break;
             }
         }
 
         int rank = 1;
         for (ArenaPlayer player : players) {
             Player p = Bukkit.getPlayerExact(player.getPlayerName());
             if (p != null) {
                 setTabPlayer(p, gameStatus, arenaName, nrOfPlayers, nrOfSpectators, false, player.getPlayerScore().getKills() + " Kills", "Rank " + rank, playerTab);
             }
             rank++;
         }
 
         for (Player p : arena.getArenaSpectators().getSpectators().keySet()) {
             setTabPlayer(p, gameStatus, arenaName, nrOfPlayers, nrOfSpectators, true, null, null, playerTab);
         }
         TabAPI.updateAll();
     }
 
     private void setTabPlayer(Player p, String status, String arena, String players, String spectators, boolean specator, String kills, String rank, String[] playersTab) {
         int row = tabController.setTopTab(p, Gamemodes.OITC); int colom = 0;
         TabAPI.setTabString(plugin, p, 2, 1, status);
         TabAPI.setTabString(plugin, p, 3, 1, arena);
         TabAPI.setTabString(plugin, p, 4, 1, players);
         TabAPI.setTabString(plugin, p, 4, 2, spectators);
         if (specator) {
             TabAPI.setTabString(plugin, p, 5, 1, ChatColor.GRAY + "Spectator");
         } else {
             TabAPI.setTabString(plugin, p, 5, 1, ChatColor.GREEN + kills);
             TabAPI.setTabString(plugin, p, 5, 2, ChatColor.GREEN + rank);
         }
         for (String cell : playersTab) {
             if (cell != null) {
                 TabAPI.setTabString(plugin, p, row, colom, cell);
             }
             colom++;
             if (colom > 2) {
                 row++;
                 if (row > 19) return;
                 colom = 0;
             }
         }
     }
 
     @Override
     public void sortLists() {
         Collections.sort(arena.getTeams().get(0).getPlayers(), comp);
     }
 
     protected void createComp(){
         comp = new Comparator<ArenaPlayer>() {
             @Override
             public int compare(ArenaPlayer o1, ArenaPlayer o2) {
                 int o1Kills = o1.getPlayerScore().getKills();
                 int o2Kills = o2.getPlayerScore().getKills();
                 if (o1Kills < o2Kills) return 1;
                 if (o1Kills > o2Kills) return -1;
                 return 0;
             }
         };
     }
 }
