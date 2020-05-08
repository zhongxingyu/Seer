 package com.gmail.jameshealey1994.simplepvptoggle.commands;
 
 import com.gmail.jameshealey1994.simplepvptoggle.SimplePVPToggle;
 import com.gmail.jameshealey1994.simplepvptoggle.localisation.Localisation;
 import com.gmail.jameshealey1994.simplepvptoggle.localisation.LocalisationEntry;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.permissions.Permission;
 
 /**
  * Allows you to see your current PVP status for your current world.
  * 
  * @author JamesHealey94 <jameshealey1994.gmail.com>
  */
 public class StatusCommand extends SimplePVPToggleCommand {
 
     /**
      * Constructor to add aliases and permissions.
      */
     public StatusCommand() {
         this.aliases.add("status");
         this.aliases.add("s");
 
         // TODO: Sort out permission structure (class for permissions?)
         this.permissions.add(new Permission("spt.status.self"));
     }
 
     @Override
     public boolean execute(SimplePVPToggle plugin, CommandSender sender, String commandLabel, String[] args) {
 
         /*
          * Command can be used by console, and any players with the correct permission.
          * Although it depends on the arguments given.
          */
         // TODO Edit to work with different world and players.
         if (!(sender instanceof Player)) {
             sender.sendMessage("This command is not yet supported on console");
             return true;
         }
 
         final Player player = (Player) sender;
         final Localisation localisation = plugin.getLocalisation();
         if (!hasPerms(player)) {
             sender.sendMessage(localisation.get(LocalisationEntry.ERR_PERMISSION_DENIED));
             return true;
         }
 
         final boolean status = plugin.getConfig().getBoolean("Server.Worlds." + player.getWorld().getName() + ".Players." + player.getName(),
                 plugin.getConfig().getBoolean("Server.Worlds." + player.getWorld().getName() + ".Default",
                 plugin.getConfig().getBoolean("Server.Worlds." + player.getWorld().getName(),
                 plugin.getConfig().getBoolean("Server.Default"))));
 
         player.sendMessage(localisation.get(LocalisationEntry.MSG_CURRENT_PVP_STATUS) + status);
         return true;
     }
 
     @Override
     public String getDescription(Localisation localisation) {
         return localisation.get(LocalisationEntry.DESCRIPTION_STATUS);
     }
 }
