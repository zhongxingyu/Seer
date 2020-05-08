 package net.invisioncraft.plugins.salesmania.commands.salesmania;
 
 import net.invisioncraft.plugins.salesmania.CommandHandler;
 import net.invisioncraft.plugins.salesmania.Salesmania;
 import net.invisioncraft.plugins.salesmania.configuration.Locale;
 import net.invisioncraft.plugins.salesmania.configuration.LocaleSettings;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 
 /**
  * Owner: Byte 2 O Software LLC
  * Date: 5/23/12
  * Time: 8:01 PM
  */
 /*
 Copyright 2012 Byte 2 O Software LLC
     This program is free software: you can redistribute it and/or modify
     it under the terms of the GNU General Public License as published by
     the Free Software Foundation, either version 3 of the License, or
     (at your option) any later version.
 
     This program is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
     GNU General Public License for more details.
 
     You should have received a copy of the GNU General Public License
     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
 
 public class LocaleCommand extends CommandHandler {
     LocaleSettings localeSettings;
     enum LocaleCommands {
         LIST,
         SET
     }
 
     public LocaleCommand(Salesmania plugin) {
         super(plugin);
         localeSettings = plugin.getSettings().getLocaleSettings();
     }
 
     @Override
     public boolean execute(CommandSender sender, Command command, String label, String[] args) {
         Locale locale = plugin.getLocaleHandler().getLocale(sender);
         LocaleCommands localeCommand;

         // Syntax
        if(args.length < 2) {
            sender.sendMessage(locale.getMessageList("Syntax.Salesmania.salesmania").toArray(new String[0]));
            return false;
        }
         try {
             localeCommand = LocaleCommands.valueOf(args[1].toUpperCase());
         } catch (IllegalArgumentException ex) {
             sender.sendMessage(locale.getMessageList("Syntax.Salesmania.salesmania").toArray(new String[0]));
             return false;
         }

         switch(localeCommand) {
             case LIST:
                 String localeList = "";
                 for (String localeName : localeSettings.getLocales()) {
                     localeList = localeList.concat(localeName + " ");
                 }
                 sender.sendMessage(String.format(
                     locale.getMessage("Locale.available"),
                     localeList));
                 break;
             case SET:
                 if(args.length < 3) {
                     sender.sendMessage(locale.getMessageList("Syntax.Salesmania.salesmania").toArray(new String[0]));
                     return false;
                 }
                 if(plugin.getLocaleHandler().setLocale(sender, args[2])) {
                     locale = plugin.getLocaleHandler().getLocale(sender);
                     sender.sendMessage(String.format(
                             locale.getMessage("Locale.changed"),
                             locale.getName()));
                 }
                 else {
                     sender.sendMessage(String.format(
                             locale.getMessage("Locale.notFound"),
                             args[2]));
                 }
                 break;
             default:
                 return false;
         }
         return true;
     }
 }
