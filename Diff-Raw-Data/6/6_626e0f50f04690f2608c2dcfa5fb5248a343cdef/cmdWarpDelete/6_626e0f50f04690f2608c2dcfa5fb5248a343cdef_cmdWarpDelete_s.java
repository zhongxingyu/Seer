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
 
 import java.util.Set;
 
 import org.bukkit.Bukkit;
 import org.bukkit.entity.Player;
 
 import de.minestar.FifthElement.core.Core;
 import de.minestar.FifthElement.data.Warp;
 import de.minestar.minestarlibrary.commands.AbstractCommand;
 import de.minestar.minestarlibrary.utils.PlayerUtils;
 
 public class cmdWarpDelete extends AbstractCommand {
 
     public cmdWarpDelete(String syntax, String arguments, String node) {
         super(Core.NAME, syntax, arguments, node);
     }
 
     @Override
     public void execute(String[] args, Player player) {
         String warpName = args[0];
         // SEARCH WARP
         Warp warp = Core.warpManager.getWarp(warpName);
         // NO WARP FOUND
         if (warp == null) {
             PlayerUtils.sendError(player, pluginName, "Der Warp '" + warpName + "' existiert nicht!");
             return;
         }
         // PLAYER NOT ALLOWED TO DELETE THE WARP
         if (!warp.canEdit(player)) {
             PlayerUtils.sendError(player, pluginName, "Du kannst den Warp '" + warp.getName() + "' nicht lschen!");
             return;
         }
         // DELET WARP
         Core.warpManager.deleteWarp(warp);
         PlayerUtils.sendSuccess(player, pluginName, "Der Warp '" + warp.getName() + "' wurde gelscht!");
 
         // PUBLIC WARPS - INFORM EVERYONE
         if (warp.isPublic())
             Bukkit.broadcastMessage("Der ffentliche Warp '" + warp.getName() + "' wurde gelscht!");
         // PRIVATE WARPS - INFORM GUESTS
         else {
             Set<String> guests = warp.getGuests();
             Player guest = null;
             for (String guestName : guests) {
                 guest = Bukkit.getPlayerExact(guestName);
                if (guests != null)
                     PlayerUtils.sendInfo(guest, "Der Spieler '" + player.getName() + "' hat den Warp '" + warp.getName() + "' gelscht!");
             }
         }
     }
 }
