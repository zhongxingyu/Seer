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
 package net.ae97.totalpermissions.commands;
 
 import net.ae97.totalpermissions.commands.subcommands.*;
 import net.ae97.totalpermissions.commands.subcommands.actions.ActionHandler;
 import java.util.HashMap;
 import java.util.Map;
 import net.ae97.totalpermissions.TotalPermissions;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 
 /**
  * @version 0.2
  * @author Lord_Ralex
  * @since 0.1
  */
 public final class CommandHandler implements CommandExecutor {
 
     protected final Map<String, SubCommand> commands = new HashMap<String, SubCommand>();
     protected final ActionHandler actions;
     protected final TotalPermissions plugin;
 
     public CommandHandler(TotalPermissions p) {
         plugin = p;
 
         HelpCommand help = new HelpCommand();
         commands.put(help.getName().toLowerCase().trim(), help);
         DebugCommand debug = new DebugCommand();
         commands.put(debug.getName().toLowerCase().trim(), debug);
         ReloadCommand reload = new ReloadCommand();
         commands.put(reload.getName().toLowerCase().trim(), reload);
         BackupCommand backup = new BackupCommand();
         commands.put(backup.getName().toLowerCase().trim(), backup);
         UserCommand user = new UserCommand();
         commands.put(user.getName().toLowerCase().trim(), user);
         GroupCommand group = new GroupCommand();
         commands.put(group.getName().toLowerCase().trim(), group);
         SpecialCommand special = new SpecialCommand();
         commands.put(special.getName().toLowerCase().trim(), special);
         WorldCommand world = new WorldCommand();
         commands.put(world.getName().toLowerCase().trim(), world);
         DumpCommand dump = new DumpCommand();
         commands.put(dump.getName().toLowerCase().trim(), dump);
 
         actions = new ActionHandler();
     }
 
     public boolean onCommand(CommandSender sender, Command cmd, String alias, String[] args) {
         String subCommand;
        if (args.length < 1) {
            args = new String[]{"help"};
         }
        subCommand = args[0];
         SubCommand executor = commands.get(subCommand.toLowerCase());
         if (executor == null) {
             sender.sendMessage(plugin.getLangFile().getString("command.handler.ifnull-plain"));
             return true;
         }
         if ((args.length > 1) && (args[1].equalsIgnoreCase("help"))) {
             sender.sendMessage(plugin.getLangFile().getString("command.handler.usage", executor.getHelp()[0]));
             sender.sendMessage(executor.getHelp()[1]);
             return true;
         }
         int length = args.length - 1;
         if (length < 0) {
             length = 0;
         }
         String[] newArgs = new String[length];
         for (int i = 0; i < newArgs.length; i++) {
             newArgs[i] = args[i + 1];
         }
         if (sender.hasPermission("totalpermissions.cmd" + executor.getName())) {
             boolean success = executor.execute(sender, args);
             if (!success) {
                 sender.sendMessage(executor.getHelp()[0]);
                 sender.sendMessage(executor.getHelp()[1]);
             }
             return true;
         } else {
             sender.sendMessage(plugin.getLangFile().getString("command.handler.denied"));
         }
         return true;
     }
 
     /**
      * Gets the registered commands that may be used
      *
      * @return Map of registered sub commands
      *
      * @since 0.2
      */
     public Map<String, SubCommand> getCommandList() {
         return commands;
     }
 
     /**
      * Gets the ActionHandler that is registered
      *
      * @return The ActionHandler in use
      *
      * @since 0.2
      */
     public ActionHandler getActionHandler() {
         return actions;
     }
 }
