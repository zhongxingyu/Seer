 package com.djdch.bukkit.kickall;
 
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.java.JavaPlugin;
 
 /**
  * Main class of the <b>KickAll</b> plugin for Bukkit.
  * 
  * Implement the kickall command.
  * 
  * @author DjDCH
  */
 public class KickAll extends JavaPlugin {
     /**
      * Method executed when the plugin is enable.
      */
     public void onEnable() {
     }
 
     /**
      * Method executed when the plugin is disable.
      */
     public void onDisable() {
     }
 
     /**
      * Method executed when a command is send to the plugin.
      * 
      * @param sender Contains the CommandSender instance.
      * @param command Contains the Command instance.
      * @param label Contains the alias of the command which was used.
      * @param args Contains the command arguments.
      * @return Return true if a valid command, otherwise false.
      */
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        if (cmd.getName().equalsIgnoreCase("kickall")) {
             if (args.length != 0) {
                 return false;
             }
 
             if (!sender.isOp()) {
                 return false;
             }
 
             for (Player player : this.getServer().getOnlinePlayers()) {
                 player.kickPlayer("The server was shutdown. It may attempt to restart soon.");
             }
 
             this.getLogger().info("Kicked all connected players");
 
             return true;
         }
         return false;
     }
 }
