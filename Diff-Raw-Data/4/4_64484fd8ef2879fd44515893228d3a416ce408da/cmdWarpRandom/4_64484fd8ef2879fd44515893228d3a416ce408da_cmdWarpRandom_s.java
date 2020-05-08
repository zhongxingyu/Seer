 /*
  * Copyright (C) 2012 MineStar.de 
  * 
  * This file is part of FifthElement.
  * 
  * FifthElement is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, version 3 of the License.
  * 
  * FifthElement is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with FifthElement.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package de.minestar.FifthElement.commands.warp;
 
 import java.util.List;
 import java.util.Random;
 
 import org.bukkit.entity.Player;
 
 import de.minestar.FifthElement.core.Core;
 import de.minestar.FifthElement.data.Warp;
 import de.minestar.minestarlibrary.commands.AbstractCommand;
 import de.minestar.minestarlibrary.utils.PlayerUtils;
 
 public class cmdWarpRandom extends AbstractCommand {
 
     private static final Random rand = new Random();
 
     public cmdWarpRandom(String syntax, String arguments, String node) {
         super(Core.NAME, syntax, arguments, node);
     }
 
     @Override
     public void execute(String[] args, Player player) {
         // GET PUBLIC WARPS
         List<Warp> publicWarps = Core.warpManager.getPublicWarps();
 
         // GET RANDOM WARP
         int index = rand.nextInt(publicWarps.size());
         Warp warp = publicWarps.get(index);
 
         player.teleport(warp.getLocation());
         PlayerUtils.sendSuccess(player, pluginName, "Willkommen beim zuflligen Warp '" + warp.getName() + "'.");
     }
 }
