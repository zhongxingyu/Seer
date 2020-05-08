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
 
 package de.minestar.MineStarWarp.commands.warp;
 
 import java.util.TreeMap;
 
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 
 import com.bukkit.gemo.utils.UtilPermissions;
 
 import de.minestar.MineStarWarp.Core;
 import de.minestar.MineStarWarp.Warp;
 import de.minestar.MineStarWarp.dataManager.WarpManager;
 import de.minestar.minestarlibrary.commands.AbstractExtendedCommand;
 import de.minestar.minestarlibrary.utils.PlayerUtils;
 
 public class ListCommand extends AbstractExtendedCommand {
 
     private WarpManager wManager;
 
     public ListCommand(String syntax, String arguments, String node, WarpManager wManager) {
         super(Core.NAME, syntax, arguments, node);
         this.description = "Listet alle benutzbaren Warps nach Seiten sortiert auf.";
         this.wManager = wManager;
     }
 
     @Override
     /**
      * Representing the command <br>
      * /warp list <br>
      * Sending the player a list of all warps the player can use. 
      * If warp list my is used it sends a list of warps the player has created
      * 
      * @param player
      *            Called the command
      * @param split
      *            args[0] is page number
      */
     public void execute(String[] args, Player player) {
 
         // /warp list was used
         if (args.length == 0)
             showAllWarps(player, 1);
         // warp list PARAMATER was used
         else if (args.length == 1) {
             // /warp list my
             if (args[0].equalsIgnoreCase("my"))
                 showPlayersWarp(player, player.getName());
             // warp list #
             else if (args[0].matches("-?\\d*"))
                 showAllWarps(player, Integer.parseInt(args[0]));
             // warp list playerName
             else if (UtilPermissions.playerCanUseCommand(player, "minestarwarp.command.listPlayer"))
                 showPlayersWarp(player, args[0]);
         } else
             player.sendMessage(getHelpMessage());
     }
 
     /**
      * When player use the command '/warp list #' or '/warp list' without an
      * paramater
      * 
      * @param player
      * @param page
      */
     private void showAllWarps(Player player, int page) {
         // sorted result
         TreeMap<String, Warp> warps;
         // how many pages of warps the player can see
 
         int maxPage = wManager.getMaxPage(player);
 
         if (maxPage == 0) {
             PlayerUtils.sendError(player, pluginName, "Du hast keinen benutzbaren Warp!");
             return;
         }
 
         // To high number
         if (page > maxPage || page <= 0) {
             PlayerUtils.sendError(player, pluginName, "Benutze bitte nur Seitenzahlen von 1 bis " + maxPage);
             return;
         }
 
         warps = wManager.getWarpsForList(page, player);
        PlayerUtils.sendMessage(player, ChatColor.WHITE, pluginName, "-------------- Seite " + page + "/" + maxPage + " --------------");
         wManager.showWarpList(player, warps);
     }
 
     /**
      * When player use the command '/warp list my' or '/warp list playername'
      * 
      * @param player
      * @param targetName
      */
     private void showPlayersWarp(Player player, String targetName) {
         TreeMap<String, Warp> warps = wManager.getWarpsPlayerIsOwner(targetName);
         if (warps != null) {
             PlayerUtils.sendInfo(player, pluginName, " Spieler '" + targetName + "' hat " + wManager.countWarpsCreatedBy(targetName) + "/" + wManager.getMaximumWarp(targetName) + " private Warps");
             wManager.showWarpList(player, warps);
         } else
             PlayerUtils.sendError(player, pluginName, "Spieler '" + targetName + "' hat keine Warps!");
     }
 }
