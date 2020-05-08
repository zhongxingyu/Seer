 /*
  * Copyright ucchy 2013
  */
 package com.github.ucchyocean.cmt.command;
 
 import java.util.ArrayList;
 import java.util.Hashtable;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 import com.github.ucchyocean.cmt.ColorMeTeaming;
 import com.github.ucchyocean.cmt.ColorMeTeamingConfig;
 
 /**
  * @author ucchy
  * colorchat(cchat)コマンドの実行クラス
  */
 public class CChatCommand implements CommandExecutor {
 
     private static final String PREERR = ChatColor.RED.toString();
 
     /**
      * @see org.bukkit.plugin.java.JavaPlugin#onCommand(org.bukkit.command.CommandSender, org.bukkit.command.Command, java.lang.String, java.lang.String[])
      */
     public boolean onCommand(
             CommandSender sender, Command command, String label, String[] args) {
 
         if ( args.length <= 0 ) {
             return false;
         }
 
         if ( args[0].equalsIgnoreCase("on") ) {
             ColorMeTeamingConfig.isTeamChatMode = true;
             sender.sendMessage(ChatColor.RED + "チームチャットモードになりました。");
             ColorMeTeamingConfig.setConfigValue("teamChatMode", true);
             return true;
         } else if ( args[0].equalsIgnoreCase("off") ) {
             ColorMeTeamingConfig.isTeamChatMode = false;
             sender.sendMessage(ChatColor.RED + "チームチャットを一時的に無効にしました。");
             ColorMeTeamingConfig.setConfigValue("teamChatMode", false);
             return true;
        } else if ( args[0].equalsIgnoreCase("opon") || args[0].equalsIgnoreCase("opcopyon") ) {
             ColorMeTeamingConfig.isOPDisplayMode = true;
             sender.sendMessage(ChatColor.RED + "チームチャットをOPにも表示します。");
             ColorMeTeamingConfig.setConfigValue("opDisplayMode", true);
             return true;
        } else if ( args[0].equalsIgnoreCase("opoff") || args[0].equalsIgnoreCase("opcopyoff") ) {
             ColorMeTeamingConfig.isOPDisplayMode = false;
             sender.sendMessage(ChatColor.RED + "チームチャットのOPへの表示をオフにします。");
             ColorMeTeamingConfig.setConfigValue("opDisplayMode", false);
             return true;
         } else if ( args.length >= 2 ){
             // グループにメッセージ送信
             String group = args[0];
 
             Hashtable<String, ArrayList<Player>> members = ColorMeTeaming.getAllColorMembers();
 
             // 有効なグループ名が指定されたか確認する
             if ( !members.containsKey(group) ) {
                 sender.sendMessage(PREERR + "グループ " + group + " が存在しません。");
                 return true;
             }
 
             // メッセージの整形
             StringBuilder message = new StringBuilder();
             for ( int i=1; i<args.length; i++ ) {
                 message.append(" " + args[i]);
             }
 
             // 送信
             ColorMeTeaming.sendTeamChat(group, message.toString());
         }
 
         return false;
     }
 
 }
