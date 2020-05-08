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
 import com.github.ucchyocean.ct.config.ClassData;
 import com.github.ucchyocean.ct.config.TeamNameSetting;
 
 /**
  * colorclass(cclass)コマンドの実行クラス
  * @author ucchy
  */
 public class CClassCommand implements CommandExecutor {
 
     private static final String PREERR = ChatColor.RED.toString();
     private static final String PREINFO = ChatColor.GRAY.toString();
 
     private ColorTeaming plugin;
 
     public CClassCommand(ColorTeaming plugin) {
         this.plugin = plugin;
     }
 
     /**
      * @see org.bukkit.plugin.java.JavaPlugin#onCommand(org.bukkit.command.CommandSender, org.bukkit.command.Command, java.lang.String, java.lang.String[])
      */
     public boolean onCommand(
             CommandSender sender, Command command, String label, String[] args) {
 
         if ( args.length >= 1 && args[0].equalsIgnoreCase("check") ) {
             // cclass check コマンドの処理
             if ( !(sender instanceof Player) ) {
                 sender.sendMessage(PREERR + label + " check コマンドは、ゲーム内でのみ実行できます。");
                 return true;
             }
             Player player = (Player)sender;
             if ( player.getItemInHand() != null ) {
                 sender.sendMessage("===== アイテム情報 =====");
                 for ( String message : ClassData.getItemInfo(player.getItemInHand()) ) {
                     sender.sendMessage(message);
                 }
             }
             return true;
             
         }
 
         // ここ以下は引数が2つ以上必要である。
         if ( args.length < 2 ) {
             return false;
         }
         
         if ( args[0].equalsIgnoreCase("export") ) {
             // cclass export コマンドの処理
             
             if ( !(sender instanceof Player) ) {
                 sender.sendMessage(PREERR + label + " export コマンドは、ゲーム内でのみ実行できます。");
                 return true;
             }
             Player player = (Player)sender;
             String name = args[1];
            if ( !name.matches("^[a-zA-Z0-9\\-_]{1,20}$") ) {
                sender.sendMessage(PREERR + "指定されたクラス名 " + name + " は使用できません。");
                return true;
            }
             
             if ( ClassData.exportClassFromPlayer(player, name) ) {
                 sender.sendMessage(PREINFO + "クラス設定 " + name + " をエクスポートしました。");
             }
             return true;
             
         }
 
         String target = args[0];
         String clas = args[1];
 
         ColorTeamingAPI api = plugin.getAPI();
         HashMap<String, ArrayList<Player>> members = api.getAllTeamMembers();
 
         // 有効なチーム名かユーザー名か'all'が指定されたかを確認する
         boolean isAll = false;
         boolean isTeam = false;
         if ( target.equalsIgnoreCase("all") ) {
             // 全プレイヤー指定
             isAll = true;
         } else if ( api.isExistTeam(target) ) {
             // チーム指定
             isTeam = true;
         } else if ( api.getAllPlayers().contains(Bukkit.getPlayerExact(target)) ) {
             // ユーザー指定
         } else {
             sender.sendMessage(PREERR + "チームまたはプレイヤー " + target + " が存在しません。");
             return true;
         }
 
         // 有効なクラス名が指定されたか確認する
         if ( !plugin.getAPI().isExistClass(clas) ) {
             sender.sendMessage(PREERR + "クラス " + clas + " が存在しません。");
             return true;
         }
 
         // クラス設定対象を取得する
         ArrayList<Player> playersToSet = new ArrayList<Player>();
         if ( isAll ) {
             for ( String key : members.keySet() ) {
                 playersToSet.addAll(members.get(key));
             }
         } else if ( isTeam ) {
             playersToSet = members.get(target);
         } else {
             playersToSet.add(Bukkit.getPlayerExact(target));
         }
         
         if ( playersToSet.size() <= 0 ) {
             sender.sendMessage(PREERR + "設定先 " + target + " の対象プレイヤーが誰もいません。");
             return true;
         }
 
         // クラス設定を実行する
         api.setClassToPlayer(playersToSet, clas);
 
         String targetName;
         if ( isAll ) {
             targetName = "全てのプレイヤー";
         } else if ( isTeam ) {
             TeamNameSetting tns = api.getTeamNameFromID(target);
             targetName = "チーム" + tns.toString();
         } else {
             targetName = "プレイヤー" + target;
         }
 
         sender.sendMessage(PREINFO +
                 String.format("%s に、%s クラスの装備とアイテムを配布しました。", 
                         targetName, clas));
 
         return true;
     }
 }
