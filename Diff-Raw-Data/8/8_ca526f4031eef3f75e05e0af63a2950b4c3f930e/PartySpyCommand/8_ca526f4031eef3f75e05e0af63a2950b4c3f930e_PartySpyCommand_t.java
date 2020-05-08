 /* 
  * Copyright (C) 2013 Dr Daniel R. Naylor
  * 
  * This file is part of mcMMO Party Admin.
  *
  * mcMMO Party Admin is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * mcMMO Party Admin is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with mcMMO Party Admin.  If not, see <http://www.gnu.org/licenses/>.
  * 
  **/
 package uk.co.drnaylor.mcmmopartyadmin.commands;
 
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import uk.co.drnaylor.mcmmopartyadmin.PartyAdmin;
 import uk.co.drnaylor.mcmmopartyadmin.locales.L10n;
 import uk.co.drnaylor.mcmmopartyadmin.permissions.PermissionHandler;
 
 public class PartySpyCommand implements CommandExecutor {
         
     public PartySpyCommand() { }
     
     public boolean onCommand(CommandSender cs, Command cmnd, String string, String[] args) {
         switch (args.length) {
             case 0:
                 if (cs instanceof Player) {
                     Player player = (Player) cs;
                     if (PermissionHandler.canSpy(player)) {
                         
                         // Toggle Spy
                         PartyAdmin.getPlugin().getPartySpyHandler().toggleSpy(player);
                         
                         // Now send the correct message, based on whether they are now spying
                         if (PartyAdmin.getPlugin().getPartySpyHandler().isSpy(player)) {
                             cs.sendMessage(L10n.getString("Commands.PartySpy.on"));
                         } else {
                             cs.sendMessage(L10n.getString("Commands.PartySpy.off"));
                         }
                         
                     }
                     else {
                         cs.sendMessage(L10n.getString("Commands.NoPermission"));                       
                     }
                 } else {
                     cs.sendMessage(L10n.getString("Commands.NoConsole"));
                 }
                 break;
             case 1:
                 if (args[0].equalsIgnoreCase("reload")) {
                     if (!(cs instanceof Player) || PermissionHandler.isAdmin((Player)cs)) {
                         PartyAdmin.getPlugin().getPartySpyHandler().reloadSpies();
                     } else {
                         cs.sendMessage(L10n.getString("Commands.NoPermission"));
                     }
                     break;
                 } else if (args[0].equalsIgnoreCase("save")) {
                     if (!(cs instanceof Player) || PermissionHandler.isAdmin((Player)cs)) {
                         PartyAdmin.getPlugin().getPartySpyHandler().saveList();
                     } else {
                         cs.sendMessage(L10n.getString("Commands.NoPermission"));
                     }
                     break;
                 }
             // Fallthrough
             default:
                cs.sendMessage(L10n.getString("Commands.IncorrectUse"));
                 break;
         }
         return true;
     }
     
 
 }
