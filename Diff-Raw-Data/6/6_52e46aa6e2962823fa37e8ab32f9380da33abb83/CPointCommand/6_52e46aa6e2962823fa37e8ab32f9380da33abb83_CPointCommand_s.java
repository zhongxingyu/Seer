 /*
  * @author     ucchy
  * @license    LGPLv3
  * @copyright  Copyright ucchy 2013
  */
 package com.github.ucchyocean.ct.command;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 import com.github.ucchyocean.ct.ColorTeaming;
 import com.github.ucchyocean.ct.ColorTeamingAPI;
 import com.github.ucchyocean.ct.config.TeamNameSetting;
 
 /**
  * colorpoint(cpoint)コマンドの実行クラス
  * @author ucchy
  */
 public class CPointCommand implements CommandExecutor {
 
     private static final String PRE_LINE_MESSAGE =
             "=== Team Point Information ===";
 
     private ColorTeaming plugin;
 
     public CPointCommand(ColorTeaming plugin) {
         this.plugin = plugin;
     }
 
     /**
      * @see org.bukkit.plugin.java.JavaPlugin#onCommand(org.bukkit.command.CommandSender, org.bukkit.command.Command, java.lang.String, java.lang.String[])
      */
     public boolean onCommand(
             CommandSender sender, Command command, String label, String[] args) {
 
         if ( args.length == 0 || (args.length >= 1 && args[0].equalsIgnoreCase("say")) ) {
 
             boolean isBroadcast = false;
             if ( args.length >= 1 && args[0].equalsIgnoreCase("say") ) {
                 isBroadcast = true;
             }
 
             HashMap<String, int[]> killDeathCounts =
                     plugin.getAPI().getKillDeathCounts();
             HashMap<String, int[]> killDeathUserCounts =
                     plugin.getAPI().getKillDeathUserCounts();
             HashMap<String, Integer> teamPoints =
                     plugin.getAPI().getAllTeamPoints();
             int killpoint = plugin.getCTConfig().getCTKillPoint();
             int deathpoint = plugin.getCTConfig().getCTDeathPoint();
             int tkpoint = plugin.getCTConfig().getCTTKPoint();
 
             // 全チームの得点を集計して、得点順に並べる
             ArrayList<TeamNameSetting> teams = new ArrayList<TeamNameSetting>();
             ArrayList<Integer> points = new ArrayList<Integer>();
 
             for ( String team : killDeathCounts.keySet() ) {
                 
                 int point = 0;
                 if ( teamPoints.containsKey(team) ) {
                     point = teamPoints.get(team);
                 }
 
                 int index = 0;
                 while ( teams.size() > index && points.get(index) > point ) {
                     index++;
                 }
                 
                 TeamNameSetting tns = plugin.getAPI().getTeamNameFromID(team);
                 teams.add(index, tns);
                 points.add(index, point);
             }
 
             // 全チームの得点を表示する
             if ( !isBroadcast ) {
                 sender.sendMessage(ChatColor.GRAY + PRE_LINE_MESSAGE);
             } else {
                 Bukkit.broadcastMessage(ChatColor.LIGHT_PURPLE + PRE_LINE_MESSAGE);
             }
 
             for ( int rank=1; rank<=teams.size(); rank++ ) {
                 
                 String color;
                 if ( !isBroadcast ) {
                     color = ChatColor.GRAY.toString();
                 } else {
                     color = ChatColor.RED.toString();
                 }
 
                 TeamNameSetting tns = teams.get(rank-1);
                 int point = points.get(rank-1);
                 int[] counts = killDeathCounts.get(tns.getID());
                 String message = String.format(
                         "%s%d. %s %s%dpoints (%dkill, %ddeath, %dtk)",
                         color, rank, tns.toString(), color, point, 
                         counts[0], counts[1], counts[2]);
 
                 if ( !isBroadcast ) {
                     sender.sendMessage(message);
                 } else {
                     Bukkit.broadcastMessage(message);
                 }
             }
 
             // ユーザー得点の集計
 
             // まだ1つも得点が記録されていないなら、ここでコマンドは終わる。
             if ( killDeathUserCounts.size() <= 0 ) {
                 return true;
             }
 
             ArrayList<String> users = new ArrayList<String>();
             ArrayList<Integer> userPoints = new ArrayList<Integer>();
 
             for ( String playerName : killDeathUserCounts.keySet() ) {
                 int[] counts = killDeathUserCounts.get(playerName);
                 int point = counts[0] * killpoint +
                             counts[1] * deathpoint +
                             counts[2] * tkpoint;
 
                 int index = 0;
                 while ( users.size() > index && userPoints.get(index) > point ) {
                     index++;
                 }
                 users.add(index, playerName);
                 userPoints.add(index, point);
             }
 
             // 1位と、1位と同じ得点の人を、MVPにする
             ArrayList<String> mvp = new ArrayList<String>();
             mvp.add(users.get(0));
             int index = 1;
             while ( userPoints.size() > index && userPoints.get(0) == userPoints.get(index) ) {
                 mvp.add(users.get(index));
                 index++;
             }
 
             // MVPの得点を表示する
             for ( int i=0; i<mvp.size(); i++ ) {
                 String mvpName = users.get(i);
                 int point = userPoints.get(i);
                 int[] counts = killDeathUserCounts.get(users.get(i));
                 String message = String.format(
                         "[MVP] %s %dpoints (%dkill, %ddeath, %dtk)",
                         mvpName, point, counts[0], counts[1], counts[2]);
                 if ( !isBroadcast ) {
                     sender.sendMessage(ChatColor.GRAY + message);
                 } else {
                     Bukkit.broadcastMessage(ChatColor.RED + message);
                 }
             }
 
             // 個人の得点を個人のコンソールに表示する
             if ( isBroadcast ) {
                 for ( int i=0; i<users.size(); i++ ) {
                     String playerName = users.get(i);
                     int point = userPoints.get(i);
                     int[] counts = killDeathUserCounts.get(users.get(i));
                     String message = String.format(
                             "[Your Score] %s %dpoints (%dkill, %ddeath, %dtk)",
                             playerName, point, counts[0], counts[1], counts[2]);
 
                     Player player = Bukkit.getPlayerExact(playerName);
                     if ( player != null ) {
                         player.sendMessage(ChatColor.GRAY + message);
                     }
                 }
             }
 
             return true;
 
         } else if ( args.length >= 1 && args[0].equalsIgnoreCase("clear") ) {
 
             ColorTeamingAPI api = plugin.getAPI();
             api.clearKillDeathPoints();
            api.refreshSidebarScore();
            api.refreshTabkeyListScore();
            api.refreshBelowNameScore();
             sender.sendMessage(ChatColor.GRAY + "KillDeath数をリセットしました。");
             return true;
             
         } else if ( args.length >= 3 && args[0].equalsIgnoreCase("set") ) {
             
             if ( !args[2].matches("-?[0-9]{1,9}") ) {
                 sender.sendMessage(ChatColor.RED + "pointには数字を指定してください。");
                 sender.sendMessage(ChatColor.RED + "/" + label + " set (team) (point)");
                 return true;
             }
             
             String id = args[1];
             int point = Integer.parseInt(args[2]);
             
             if ( !plugin.getAPI().isExistTeam(id) ) {
                 sender.sendMessage(ChatColor.RED + "チーム" + id + "が存在しません。");
                 return true;
             }
             
             plugin.getAPI().setTeamPoint(id, point);
             
             sender.sendMessage(ChatColor.RED + 
                     "チーム" + id + "のポイントを、" + point + "に設定しました。");
             return true;
 
         } else if ( args.length >= 3 && args[0].equalsIgnoreCase("add") ) {
             
             if ( !args[2].matches("-?[0-9]{1,9}") ) {
                 sender.sendMessage(ChatColor.RED + "pointには数字を指定してください。");
                 sender.sendMessage(ChatColor.RED + "/" + label + " set (team) (point)");
                 return true;
             }
             
             String id = args[1];
             int amount = Integer.parseInt(args[2]);
             
             if ( !plugin.getAPI().isExistTeam(id) ) {
                 sender.sendMessage(ChatColor.RED + "チーム" + id + "が存在しません。");
                 return true;
             }
             
             int point = plugin.getAPI().addTeamPoint(id, amount);
             
             sender.sendMessage(ChatColor.RED + 
                     "チーム" + id + "のポイントを、" + point + "に設定しました。");
             return true;
 
         }
 
         return false;
     }
 
 }
