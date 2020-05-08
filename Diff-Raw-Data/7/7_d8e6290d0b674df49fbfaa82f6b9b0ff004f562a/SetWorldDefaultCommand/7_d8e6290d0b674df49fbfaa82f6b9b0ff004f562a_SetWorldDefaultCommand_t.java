 package com.gmail.jameshealey1994.simplepvptoggle.commands;
 
 import com.gmail.jameshealey1994.simplepvptoggle.SimplePVPToggle;
 import com.gmail.jameshealey1994.simplepvptoggle.localisation.Localisation;
 import com.gmail.jameshealey1994.simplepvptoggle.localisation.LocalisationEntry;
 import com.gmail.jameshealey1994.simplepvptoggle.utils.Helper;
 import org.bukkit.World;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.permissions.Permission;
 
 /**
  * Abstract class representing a SimplePVPToggle set world default command.
  * Allows you to set the default PVP status of a world
  * 
  * /pvp setworlddefault [world] <on / off>      Sets default PVP status for
  *                                              [world] to <on / off>
  * 
  * @author JamesHealey94 <jameshealey1994.gmail.com>
  */
 public class SetWorldDefaultCommand extends SimplePVPToggleCommand {
 
     /**
      * Constructor to add aliases and permissions.
      */
     public SetWorldDefaultCommand() {
         this.aliases.add("setworlddefault");
         
         //TODO: Check - Player with .* but not .world should be allowed.
         //this.permissions.add(new Permission("spt.setdefault.*")); needed?
         this.permissions.add(new Permission("spt.setdefault.world"));
         
         // TODO - Try to fix: 
         // if a player has spt.setdefault.world in another world,
         // then does /spt setworlddefault thatworld true,
         // it won't work currently, but would be nice to
        // -- Vault will fix, but will mean adding a dependancy --
     }
 
     @Override
     public boolean execute(SimplePVPToggle plugin, CommandSender sender, String commandLabel, String[] args) {
         /*
          * Possible:
          * /pvp setworlddefault [world] <on / off> 
          * /pvp setworlddefault <on / off>
          */
         
         if (args.length > 0) {
             /*
              * Using Boolean instead of boolean as it can be null (if player gives something other than 'true' or 'false')
              */
             Boolean newState = Helper.parseBoolean(args[0]);
             if (newState == null) {
                 final World world = plugin.getServer().getWorld(args[0]);
                 if (world == null) {
                     return false;
                 } else {
                     /*
                      * Possible:
                      * /pvp setworlddefault [world] <on / off> 
                      */
                     if (args.length > 1) {
                         newState = Helper.parseBoolean(args[1]);
                         if (newState == null) {
                             sender.sendMessage(plugin.getLocalisation().get(LocalisationEntry.ERR_SPECIFY_PVP_STATUS));
                         } else {
                             // /pvp setworlddefault [world] <on / off> 
                             if (sender instanceof Player) {
                                 final Player player = (Player) sender;
                                 // TODO check player has perm "spt.setdefault.world" in world specified instead of world where they sent command
                                 if (!player.hasPermission("spt.setdefault.world")) {
                                     player.sendMessage(plugin.getLocalisation().get(LocalisationEntry.ERR_PERMISSION_DENIED));
                                     return true;
                                 }
                             }
                             
                             setWorldDefaultPVPStatus(sender, world, newState, plugin);
                             return true;
                         }
                     } else {
                         sender.sendMessage(plugin.getLocalisation().get(LocalisationEntry.ERR_SPECIFY_PVP_STATUS));
                     }
                 }
             } else {
                 // /pvp setworlddefault <on / off>
                 final Player player = (Player) sender;
                 
                 if (player == null) {
                     sender.sendMessage(plugin.getLocalisation().get(LocalisationEntry.ERR_PLAYER_ONLY_COMMAND));
                     return true;
                 }
                 
                 // TODO check player has perm "spt.setdefault.world" in world specified instead of world where they sent command
                 if (!player.hasPermission("spt.setdefault.world")) {
                     player.sendMessage(plugin.getLocalisation().get(LocalisationEntry.ERR_PERMISSION_DENIED));
                     return true;
                 }
                 
                 setWorldDefaultPVPStatus(player, player.getWorld(), newState, plugin);
                 return true;
             }
         } else {
             sender.sendMessage(plugin.getLocalisation().get(LocalisationEntry.ERR_SPECIFY_PVP_STATUS));
         }
         return false;
     }
     
     /**
      * Sets the default PVP status of the passed world to the passed state,
      * saves the config, then sends a message to the sender.
      * 
      * @param sender    sender of the command, to be sent a confirmation message
      * @param world     world to set the default PVP status of
      * @param status    status to change to
      * @param plugin    plugin with the config storing PVP status values
      */
     public void setWorldDefaultPVPStatus(CommandSender sender, World world, boolean status, SimplePVPToggle plugin) {
         final String path = "Server.Worlds." + world.getName() + ".Default";
         plugin.getConfig().set(path, status);
         plugin.saveConfig();
         sender.sendMessage(world.getName() + plugin.getLocalisation().get(LocalisationEntry.MSG_SERVER_DEFAULT_SET_TO) + plugin.getConfig().getBoolean(path));
     }
 
     @Override
     public String getDescription(Localisation localisation) {
         return localisation.get(LocalisationEntry.DESCRIPTION_SET_WORLD_DEFAULT);
     }
 }
