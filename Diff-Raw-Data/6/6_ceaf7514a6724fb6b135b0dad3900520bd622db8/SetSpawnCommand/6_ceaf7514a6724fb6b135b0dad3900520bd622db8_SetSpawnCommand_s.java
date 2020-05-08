 /*
  * Copyright (C) 2011 Massive Dynamics
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package com.massivedynamics.spawny.commands;
 
 import com.massivedynamics.spawny.SpawnyPlugin;
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 /**
  * A command for adding spawns
  * @author Cruz Bishop
  * @version 1.1.0.0
  */
 public class SetSpawnCommand implements CommandExecutor {
 
     /**
      * Called when the command is executed
      * @param cs The command sender
      * @param cmnd The command
      * @param label The label
      * @param arguments The arguments
      * @return True if execution was successful
      */
     @Override
     public boolean onCommand(CommandSender cs, Command cmnd, String label, String[] arguments) {
 
         try {
 
             //Is it not a player?
             if (!(cs instanceof Player)) {
                 //Damn. Letting you know
                 cs.sendMessage("Spawns can only be set by a player - sorry!");
                 //Bye :)
                 return true;
             }
 
             //Get the player variable
             Player player = (Player) cs;
 
             //Check to see if the player can set the spawn
             if (!player.hasPermission("spawny.setspawn")) {
                 //Nope. Notify
                 cs.sendMessage(ChatColor.RED + "You do not have permission to set the spawn!");
                 //And return
                 return true;
             } else {
                 //Check to see if the player isn't an op
                 if (!player.isOp()) {
                     //Send a message back
                     cs.sendMessage(ChatColor.RED + "Only ops can set spawns. Sorry!");
                     //Return true since, technically, it's still a success.
                     return true;
                 }
             }
             
             //Set the spawn location
            player.getWorld().setSpawnLocation((int) player.getLocation().getX(),
                    (int) player.getLocation().getY(),
                    (int) player.getLocation().getZ());
             
             player.sendMessage(ChatColor.GREEN + "You set the world's spawn point");
             
             return true;
 
         } catch (Exception e) {
             //Oh shit, something went wrong!
             //Letting the console know
             SpawnyPlugin.getInstance().getLogger().severe("Could not set the spawn point - " + e.getMessage() + " in " + e.getCause());
             //Letting the player know
             cs.sendMessage("Oh, something went wrong while adding the spawn point. Please try again later!");
             return true;
         }
     }
 }
