 /*
  * Copyright (C) 2012 MineStar.de 
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
 
 package de.minestar.MineStarWarp.commands.warp;
 
 import java.util.Map.Entry;
import java.util.ArrayList;
 import java.util.Random;
 
 import org.bukkit.entity.Player;
 
 import de.minestar.MineStarWarp.Core;
 import de.minestar.MineStarWarp.Warp;
 import de.minestar.MineStarWarp.dataManager.WarpManager;
 import de.minestar.minestarlibrary.commands.AbstractCommand;
 import de.minestar.minestarlibrary.utils.PlayerUtils;
 
 public class RandomCommand extends AbstractCommand {
 
     private WarpManager wManager;
     private final Random rand = new Random();
 
     public RandomCommand(String syntax, String arguments, String node, WarpManager wManager) {
         super(Core.NAME, syntax, arguments, node);
         this.wManager = wManager;
     }
 
     @Override
     public void execute(String[] args, Player player) {
         PlayerUtils.sendInfo(player, pluginName, "Es wird ein zufaelliger Warp ausgewaehlt...");
 
        ArrayList<Entry<String, Warp>> publicWarps = wManager.getPublicWarps();
        int i = rand.nextInt(publicWarps.size());
        Entry<String, Warp> target = publicWarps.get(i);
         player.teleport(target.getValue().getLoc());
         PlayerUtils.sendSuccess(player, pluginName, "Willkommen beim Warp '" + target.getKey() + "'!");
     }
 
 }
