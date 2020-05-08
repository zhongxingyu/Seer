 /*
  * Copyright (C) 2013 AE97
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
 package net.ae97.totalpermissions.commands.subcommands;
 
 import net.ae97.totalpermissions.TotalPermissions;
 import net.ae97.totalpermissions.permission.PermissionUser;
 import org.bukkit.command.CommandSender;
 
 /**
  * @since 0.1
  * @author Lord_Ralex
  * @version 0.2
  */
 public class DebugCommand implements SubCommand {
 
     protected final TotalPermissions plugin;
 
     public DebugCommand(TotalPermissions p) {
         plugin = p;
     }
 
     @Override
     public boolean execute(CommandSender sender, String[] args) {
        if (args.length < 1) {
            return false;
         }
        PermissionUser target = plugin.getManager().getUser(args[0]);
         if (target == null) {
             sender.sendMessage(plugin.getLangFile().getString("command.debug.null-target", args[1]));
            return true;
         }
        if (args.length == 2 && !args[1].equalsIgnoreCase("status")) {
             boolean newState;
            if ((args[1].equalsIgnoreCase("on")) || (args[1].equalsIgnoreCase("enable")) || (args[1].equalsIgnoreCase("true"))) {
                 newState = true;
            } else if (args[1].equalsIgnoreCase("toggle")) {
                 newState = !target.getDebugState();
             } else {
                sender.sendMessage(args[1] + " is an invalid argument");
                return false;
             }
             target.setDebug(newState);
             if (target.getDebugState()) {
                 sender.sendMessage(plugin.getLangFile().getString("command.debug.debug-on", target.getName()));
             } else {
                 sender.sendMessage(plugin.getLangFile().getString("command.debug.debug-off", target.getName()));
             }
        } else {
            sender.sendMessage(target.getName() + "'s debug status: " + (target.getDebugState() ? "on" : "off"));
         }
         return true;
     }
 
     @Override
     public String getName() {
         return "debug";
     }
 
     @Override
     public String[] getHelp() {
         return new String[]{
             "ttp debug " + plugin.getLangFile().getString("variables.username") + " [on/off]",
             plugin.getLangFile().getString("command.debug.help")
         };
     }
 }
