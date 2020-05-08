 /*
  * Copyright ucchy 2013
  */
 package com.github.ucchyocean.cmt.command;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 
 import com.github.ucchyocean.cmt.ColorMeTeamingConfig;
 
 /**
  * @author ucchy
  * CRemove(CR)コマンドの実行クラス
  */
 public class CRemoveCommand implements CommandExecutor {
 
     /**
      * @see org.bukkit.command.CommandExecutor#onCommand(org.bukkit.command.CommandSender, org.bukkit.command.Command, java.lang.String, java.lang.String[])
      */
     public boolean onCommand(
             CommandSender sender, Command command, String label, String[] args) {
 
         if ( args.length <= 0 ) {
             return false;
         }
 
         if ( args[0].equalsIgnoreCase("on") ) {
             ColorMeTeamingConfig.autoColorRemove = true;
             sender.sendMessage(ChatColor.GRAY + "死亡時のチーム離脱が有効になりました。");
            ColorMeTeamingConfig.setConfigValue("firelyFireDisabler", true);
             return true;
         } else if ( args[0].equalsIgnoreCase("off") ) {
             ColorMeTeamingConfig.autoColorRemove = false;
             sender.sendMessage(ChatColor.GRAY + "死亡時のチーム離脱が無効になりました。");
            ColorMeTeamingConfig.setConfigValue("firelyFireDisabler", false);
             return true;
         }
 
         return false;
     }
 
 }
