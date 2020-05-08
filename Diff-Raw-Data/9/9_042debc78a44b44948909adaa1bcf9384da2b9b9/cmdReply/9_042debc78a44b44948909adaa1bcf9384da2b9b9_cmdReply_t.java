 /*
  * Copyright (C) 2011 MineStar.de 
  * 
  * This file is part of 'AdminStuff'.
  * 
  * 'AdminStuff' is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, version 3 of the License.
  * 
  * 'AdminStuff' is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with 'AdminStuff'.  If not, see <http://www.gnu.org/licenses/>.
  * 
  * AUTHOR: GeMoschen
  * 
  */
 
 package de.minestar.AdminStuff.commands;
 
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 
 import de.minestar.AdminStuff.Core;
 import de.minestar.AdminStuff.manager.ASPlayer;
 import de.minestar.AdminStuff.manager.PlayerManager;
 import de.minestar.minestarlibrary.commands.AbstractExtendedCommand;
 import de.minestar.minestarlibrary.utils.PlayerUtils;
 
 public class cmdReply extends AbstractExtendedCommand {
 
     private PlayerManager pManager;
 
     public cmdReply(String syntax, String arguments, String node, PlayerManager pManager) {
         super(Core.NAME, syntax, arguments, node);
         this.pManager = pManager;
     }
 
     @Override
     /**
      * Representing the command <br>
      * /r <Message><br>
      * Reply a player
      * 
      * @param player
      *            Called the command
      * @param split
      */
     public void execute(String[] args, Player player) {
         // ADD PLAYER, IF NOT FOUND
         ASPlayer thisPlayer = pManager.getPlayer(player);
         if (thisPlayer.getLastSender() == null) {
             PlayerUtils.sendError(player, pluginName, "Du hast keinen Spieler, dem du antworten kannst...");
             return;
         }
 
         Player target = PlayerUtils.getOnlinePlayer(thisPlayer.getLastSender());
         if (target == null)
             PlayerUtils.sendError(player, pluginName, "Spieler '" + thisPlayer.getLastSender() + "' wurde nicht gefunden!");
         else if (!target.isOnline())
             PlayerUtils.sendError(player, pluginName, "Spieler '" + target.getName() + "' ist nicht online!");
         else {
 
             ASPlayer thisTarget = pManager.getPlayer(target);
 
            String text = "";
            for (String txt : args) {
                text += txt + " ";
            }

            String message = "] : " + ChatColor.GRAY + text;
             PlayerUtils.sendBlankMessage(player, ChatColor.GOLD + "[ me -> " + thisTarget.getNickname() + message);
             PlayerUtils.sendBlankMessage(target, ChatColor.GOLD + "[ " + thisPlayer.getNickname() + " -> me" + message);
             thisPlayer.setLastSender(target.getName());
             thisTarget.setLastSender(player.getName());
         }
     }
 }
