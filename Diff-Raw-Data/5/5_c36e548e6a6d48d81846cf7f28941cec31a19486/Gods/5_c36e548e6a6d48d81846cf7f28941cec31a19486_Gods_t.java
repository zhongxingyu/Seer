 /**
  * TerraCraftTools(SuperSoapTools) - Gods.java
  * Copyright (c) 2013 Jeremy Koletar (jjkoletar), <http://jj.koletar.com>
  * Copyright (c) 2013 computerdude5000,<computerdude5000@gmail.com>
  *
  * TerraCraftTools is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * TerraCraftTools is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with TerraCraftTools.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package com.github.computerdude5000.terracrafttools.modules;
 
 import com.github.computerdude5000.terracrafttools.TerraCraftTools;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 
 import java.util.List;
 import java.util.Random;
 
 
 /**
  * Created with IntelliJ IDEA.
  * User: computerdude5000
  * Date: 2/12/13
  * Time: 7:24 PM
  */
 public class Gods extends SuperSoapToolsCommandRegistrar implements SuperSoapToolsModule
 {
     private TerraCraftTools plugin;
     private List announcements;
 
 
     /**
      * initModule enables the module called by {@link TerraCraftTools}
      */
     public void initModule(TerraCraftTools sst)
     {
         plugin = sst;
        plugin.commandRegistrar.registerCommand("announcement", this);
 
         announcements = plugin.getModuleConfig("announcement").getStringList("announcements");
 
 
     }
 
     /**
      * deinitModule disables the module called by {@link TerraCraftTools}
      */
     public void deinitModule()
     {
 
        plugin.commandRegistrar.unregisterCommand("announcement", this);
     }
 
     /**
      * Returns true or false when a player types a command
      *
      * @param sender  sender who sent the message
      * @param command command that was sent
      * @param label   idk what this does.
      * @param args    arguments that were sent along with the command
      * @return true or false
      */
     public boolean callCommand(CommandSender sender, Command command, String label, String[] args)
     {
         if (command.getName().equalsIgnoreCase("god") && sender.isOp())
         {
             Bukkit.broadcastMessage(ChatColor.BLACK + "[GOD] " + ChatColor.DARK_GREEN
                     + ChatColor.UNDERLINE + announcements.get(new Random().nextInt(announcements.size())));
 
             return true;
         } else
         {
             sender.sendMessage("Your not op.");
         }
         return false;
     }
 
     public void playAnnouncement()
     {
         Bukkit.broadcastMessage(ChatColor.BLACK + "[GOD] " + ChatColor.DARK_GREEN
                 + ChatColor.UNDERLINE + announcements.get(new Random().nextInt(announcements.size())));
     }
 }
