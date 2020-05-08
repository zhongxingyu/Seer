 /*
  * Copyright (C) 2011 MineStar.de 
  * 
  * This file is part of MineStarWarp.
  * 
  * MineStarWarp is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, version 3 of the License.
  * 
  * MineStarWarp is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with MineStarWarp.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package com.minestar.MineStarWarp.commands.home;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.Server;
 import org.bukkit.entity.Player;
 
 import com.minestar.MineStarWarp.Main;
 import com.minestar.MineStarWarp.commands.Command;
 
 public class HomeCommand extends Command {
 
     public HomeCommand(String syntax, String arguments, String node,
             Server server) {
         super(syntax, arguments, node, server);
     }
 
     @Override
     /**
      * Representing the command <br>
      * /home <br>
      * This teleports the player to his home(if set)
      * 
      * @param player
      *            Called the command
     * @param args
      *            Must be empty!
      */
     public void execute(String[] args, Player player) {
 
         // When the player didn't have set a home, it returns null
         Location homeLocation = Main.homeManager.getPlayersHome(player);
         if (homeLocation != null) {
             player.teleport(homeLocation);
             player.sendMessage("Welcome home!");
         }
         else
             player.sendMessage(ChatColor.RED
                     + "You didn't have set a home! Use /setHome to create one.");
     }
 }
