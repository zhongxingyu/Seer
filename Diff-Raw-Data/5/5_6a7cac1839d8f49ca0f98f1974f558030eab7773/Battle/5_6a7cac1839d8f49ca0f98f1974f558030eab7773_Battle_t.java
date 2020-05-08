 package me.limebyte.battlenight.core;
 
import java.util.Set;

 import me.limebyte.battlenight.core.BattleNight.WPoint;
 import me.limebyte.battlenight.core.API.BattleEndEvent;
 import me.limebyte.battlenight.core.Other.Tracks.Track;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 import org.kitteh.tag.TagAPI;
 
 public class Battle {
 
     BattleNight plugin;
     int redTeam = 0;
     int blueTeam = 0;
 
     public Battle(BattleNight plugin) {
         this.plugin = plugin;
     }
 
     public void addPlayer(Player player) {
         if (plugin.preparePlayer(player)) {
             final String name = player.getName();
 
             if (blueTeam > redTeam) {
                 plugin.goToWaypoint(player, WPoint.RED_LOUNGE);
                 plugin.BattleUsersTeam.put(name, Team.RED);
                 plugin.tellPlayer(player, "Welcome! You are on team " + ChatColor.RED + "<Red>");
                 plugin.tellEveryoneExcept(player, name + " has joined team " + ChatColor.RED + "<Red>");
                 redTeam++;
             } else {
                 plugin.goToWaypoint(player, WPoint.BLUE_LOUNGE);
                 plugin.BattleUsersTeam.put(name, Team.BLUE);
                 plugin.tellPlayer(player, "Welcome! You are on team " + ChatColor.BLUE + "<Blue>");
                 plugin.tellEveryoneExcept(player, name + " has joined team " + ChatColor.BLUE + "<Blue>");
                 blueTeam++;
             }
 
             plugin.setNames(player);
             plugin.playersInLounge = true;
         } else {
             plugin.tellPlayer(player, Track.MUST_HAVE_EMPTY);
         }
     }
 
     public void removePlayer(Player player, boolean death, String msg1, String msg2) {
         final String name = player.getName();
 
         if (plugin.BattleUsersTeam.containsKey(name)) {
             final Team team = plugin.BattleUsersTeam.get(name);
             final boolean sendMsg1 = msg1 != null;
 
             if (team.equals(Team.RED)) {
                 redTeam--;
                 if (sendMsg1) plugin.tellEveryoneExcept(player, ChatColor.RED + name + ChatColor.WHITE + " " + msg1);
             }
             if (team.equals(Team.BLUE)) {
                 blueTeam--;
                 if (sendMsg1) plugin.tellEveryoneExcept(player, ChatColor.BLUE + name + ChatColor.WHITE + " " + msg1);
             }
 
             if (msg2 != null) {
                 plugin.tellPlayer(player, msg2);
             }
 
             // If red or blue won
             if (redTeam == 0 || blueTeam == 0) {
 
                 // If the battle started
                 if (!plugin.playersInLounge) {
                     // If red won
                     if (redTeam > 0) {
                         plugin.tellEveryone(Track.RED_WON);
                         Bukkit.getServer().getPluginManager().callEvent(new BattleEndEvent("red", "blue", plugin.BattleUsersTeam));
                         // If blue won
                     } else if (blueTeam > 0) {
                         plugin.tellEveryone(Track.BLUE_WON);
                         Bukkit.getServer().getPluginManager().callEvent(new BattleEndEvent("blue", "red", plugin.BattleUsersTeam));
                         // If neither team won
                     } else {
                         plugin.tellEveryone(Track.DRAW);
                         Bukkit.getServer().getPluginManager().callEvent(new BattleEndEvent("draw", "draw", null));
                     }
                 }
 
                 for (final String currentName : plugin.BattleUsersTeam.keySet()) {
                     if (Bukkit.getPlayer(currentName) != null) {
                         final Player currentPlayer = Bukkit.getPlayer(currentName);
                         if (!(death && currentPlayer == player)) {
                             resetPlayer(currentPlayer, true, false);
                         }
                     }
                 }
 
                 resetBattle();
             }
 
             if (!death) resetPlayer(player, true, true);
         } else {
             BattleNight.log.warning("Failed to remove player '" + name + "' from the Battle as they are not in it.");
         }
     }
 
     public void resetPlayer(Player player, boolean teleport, boolean removeHash) {
         player.getInventory().clear();
         plugin.restorePlayer(player);
         if (teleport) plugin.goToWaypoint(player, WPoint.EXIT);
         plugin.cleanSigns(player);
 
         if (removeHash) {
             plugin.BattleUsersTeam.remove(player.getName());
             plugin.BattleUsersClass.remove(player.getName());
             try {
                 TagAPI.refreshPlayer(player);
             } catch (final Exception e) {
             }
         }
     }
 
     private void resetBattle() {
        final Set<String> toRefresh = plugin.BattleUsersTeam.keySet();
 
         plugin.removeAllSpectators();
         plugin.cleanSigns();
         plugin.BattleSigns.clear();
         plugin.battleInProgress = false;
         plugin.redTeamIronClicked = false;
         plugin.blueTeamIronClicked = false;
         plugin.BattleUsersTeam.clear();
         plugin.BattleUsersClass.clear();
         redTeam = 0;
         blueTeam = 0;
 
         for (final String name : toRefresh) {
             if (Bukkit.getPlayer(name) != null) {
                 try {
                     TagAPI.refreshPlayer(Bukkit.getPlayer(name));
                 } catch (final Exception e) {
                 }
             }
         }
     }
 
     public void end() {
         if (blueTeam > redTeam) {
             plugin.tellEveryone(Track.BLUE_WON);
             Bukkit.getServer().getPluginManager().callEvent(new BattleEndEvent("blue", "red", plugin.BattleUsersTeam));
         } else if (redTeam > blueTeam) {
             plugin.tellEveryone(Track.RED_WON);
             Bukkit.getServer().getPluginManager().callEvent(new BattleEndEvent("red", "blue", plugin.BattleUsersTeam));
         } else {
             plugin.tellEveryone(Track.DRAW);
             Bukkit.getServer().getPluginManager().callEvent(new BattleEndEvent("draw", "draw", null));
         }
 
         for (final String currentName : plugin.BattleUsersTeam.keySet()) {
             if (Bukkit.getPlayer(currentName) != null) {
                 final Player currentPlayer = Bukkit.getPlayer(currentName);
                 resetPlayer(currentPlayer, true, false);
             }
         }
 
         resetBattle();
 
         plugin.removeAllSpectators();
     }
 }
