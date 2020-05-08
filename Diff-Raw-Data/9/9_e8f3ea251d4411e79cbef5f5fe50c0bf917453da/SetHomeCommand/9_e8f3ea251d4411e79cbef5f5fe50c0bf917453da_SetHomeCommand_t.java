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
 
 import org.bukkit.Server;
 import org.bukkit.entity.Player;
 
 import com.minestar.MineStarWarp.Main;
 import com.minestar.MineStarWarp.commands.Command;
 
 public class SetHomeCommand extends Command {
 
     public SetHomeCommand(String syntax, String arguments, String node,
             Server server) {
         super(syntax, arguments, node, server);
     }
 
     @Override
     /**
      * Representing the command <br>
      * /setHome <br>
      * This set the home of the player. The player can teleport to this location everytime
      * 
      * @param player
      *            Called the command
     * @param args
      *            Must be empty!
      */
     public void execute(String[] args, Player player) {
         Main.homeManager.setHome(player);
     }
 
 }
