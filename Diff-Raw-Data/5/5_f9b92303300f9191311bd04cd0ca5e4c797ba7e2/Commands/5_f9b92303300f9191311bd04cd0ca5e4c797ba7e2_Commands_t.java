 /**
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU General Public License
  * as published by the Free Software Foundation; either version 3
  * of the License, or (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
  * 
  */
 
 package mmode;
 
 import org.bukkit.Bukkit;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.command.ConsoleCommandSender;
 import org.bukkit.command.RemoteConsoleCommandSender;
 import org.bukkit.entity.Player;
 
 public class Commands  implements CommandExecutor  {
 
 	private Config config;
 	
 	public Commands(Config config)
 	{
 		this.config = config;
 	}
 	
 	@Override
 	public boolean onCommand(CommandSender sender, Command arg1, String arg2,
 			String[] args) {
 		//check permissions
 		if (!hasPerm(sender))
 		{
 			sender.sendMessage(ColorParser.parseColor("&4You don't have permission to do this"));
 			return true;
 		}
 		//handle command
 		if (args.length == 1 && args[0].equalsIgnoreCase("on"))
 		{
 			config.mmodeEnabled = true;
 			if (config.allowedlistEnabled && config.kickOnEnable) {
 				for (Player p :Bukkit.getOnlinePlayers())	
 				{
 					if (!config.mmodeAllowedList.contains(p.getName()))
 					{
 						p.kickPlayer(ColorParser.parseColor(config.kickMessage));
 					}
 				}
 			}
 			sender.sendMessage(ColorParser.parseColor("&9Maintenance mode on"));
 			return true;
 		} else 
 		if (args.length == 1 && args[0].equalsIgnoreCase("off"))
 		{
 			config.mmodeEnabled = false;
 			sender.sendMessage(ColorParser.parseColor("&9Maintenance mode off"));
 			return true;
 		} else 
 		if (args.length == 1 && args[0].equalsIgnoreCase("reload"))
 		{			
 			config.loadConfig();
 			sender.sendMessage(ColorParser.parseColor("&9Config reloaded"));
 			return true;
 		} else
 		if ((args.length == 2 || args.length == 3) && args[0].equalsIgnoreCase("alist"))
 		{
 			if (args.length == 2)
 			{
 				if (args[1].equalsIgnoreCase("on")) {
 					config.allowedlistEnabled = true;
 					config.saveConfig();
 					sender.sendMessage(ColorParser.parseColor("&9Allowed list enabled"));
 					return true;
				} else if (args[1].equalsIgnoreCase("off")) {
 					config.allowedlistEnabled = false;
 					config.saveConfig();
					sender.sendMessage(ColorParser.parseColor("&9Allowed list disabled"));
 					return true;
 				}
 			} else 
 			if (args.length == 3)
 			{
 				if (args[1].equalsIgnoreCase("add")) {
 					config.mmodeAllowedList.add(args[2]);
 					config.saveConfig();
 					sender.sendMessage(ColorParser.parseColor("&9Player added to list"));
 				} else if (args[1].equalsIgnoreCase("remove")) {
 					config.mmodeAllowedList.remove(args[2]);
 					config.saveConfig();
 					sender.sendMessage(ColorParser.parseColor("&9Player removed from list"));
 				}
 			}
 		}
 		return false;
 	}
 	
 	
 	private boolean hasPerm(CommandSender sender)
 	{
 		boolean has = false;
 		if (sender instanceof ConsoleCommandSender || sender instanceof RemoteConsoleCommandSender)
 		{
 			has = true;
 		}
 		if (sender instanceof Player && sender.hasPermission("mmode.admin"))
 		{
 			has = true;
 		}
 		return has;
 	}
 
 }
