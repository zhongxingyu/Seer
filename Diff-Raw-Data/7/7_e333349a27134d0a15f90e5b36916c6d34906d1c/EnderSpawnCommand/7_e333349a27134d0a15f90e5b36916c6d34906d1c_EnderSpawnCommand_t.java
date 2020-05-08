 /*
  * Copyright (c) 2012 Sean Porter <glitchkey@gmail.com>
  *
  * Permission is hereby granted, free of charge, to any person
  * obtaining a copy of this software and associated documentation
  * files (the "Software"), to deal in the Software without restriction,
  * including without limitation the rights to use, copy, modify, merge,
  * publish, distribute, sublicense, and/or sell copies of the Software,
  * and to permit persons to whom the Software is furnished to do so,
  * subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be
  * included in all copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
  * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
  * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
  * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
  * CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
  * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
  * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
  */
 
 package org.enderspawn;
 
 //* IMPORTS: JDK/JRE
 	import java.lang.String;
 //* IMPORTS: BUKKIT
 	import org.bukkit.command.Command;
 	import org.bukkit.command.CommandExecutor;
 	import org.bukkit.command.CommandSender;
 	import org.bukkit.entity.Player;
 //* IMPORTS: SPOUT
 	//* NOT NEEDED
 //* IMPORTS: OTHER
 	//* NOT NEEDED
 
 public class EnderSpawnCommand implements CommandExecutor
 {
 	private EnderSpawn plugin;
 
 	public EnderSpawnCommand(EnderSpawn plugin)
 	{
 		this.plugin = plugin;
 	}
 
 	public boolean onCommand(
 		CommandSender sender,
 		Command command,
 		String label,
 		String[] args)
 	{
 		if(args.length < 1)
 			return false;
 
 		if(args[0].equalsIgnoreCase("reload"))
 			return reload(sender);
 		else if(args[0].equalsIgnoreCase("ban"))
 			return ban(sender, args);
 		else if(args[0].equalsIgnoreCase("unban"))
 			return unban(sender, args);
 		else if(args[0].equalsIgnoreCase("lookup"))
 			return lookup(sender, args);
 		else if(args[0].equalsIgnoreCase("status"))
 			return status(sender, args);
 		else if(args[0].equalsIgnoreCase("reset"))
 			return reset(sender, args);
 
 		return false;
 	}
 
 	private boolean reload(CommandSender sender)
 	{
 		if(!(plugin.hasPermission(sender, "enderspawn.reload")))
 			return true;
 
 		if(sender instanceof Player)
 			plugin.log.info(((Player)sender).getName() + ": /enderspawn reload");
 
 		plugin.reload();
 		Message.info(sender, "EnderSpawn was successfully reloaded.");
 
 		return true;
 	}
 
 	private boolean ban(CommandSender sender, String[] args)
 	{
 		if(!(plugin.hasPermission(sender, "enderspawn.ban")))
 			return true;
 
 		if(args.length < 3)
 			return false;
 
 		String reason = args[2];
 
 		if(args.length > 3)
 		{
 			for(int i = 3; i < args.length; i++)
 			{
 				reason = reason + " " + args[i];
 			}
 		}
 
 		if(sender instanceof Player)
 		{
 			String message = ((Player)sender).getName() + ": /enderspawn ban ";
 			plugin.log.info(message + args[1] + " " + reason);
 		}
 
 		String player = args[1].toUpperCase().toLowerCase();
 		plugin.data.bannedPlayers.put(player, reason);
 		plugin.saveData();
 
 		String message = "Banned " + args[1];
 		message += " from receiving Ender Dragon experience: " + reason;
 
 		Message.info(sender, message);
 
 		return true;
 	}
 
 	private boolean unban(CommandSender sender, String[] args)
 	{
 		if(!(plugin.hasPermission(sender, "enderspawn.ban")))
 			return true;
 
 		if(args.length < 2)
 			return false;
 
 		if(sender instanceof Player)
 		{
 			String message = ((Player)sender).getName() + ": /enderspawn unban ";
 			plugin.log.info(message + args[1]);
 		}
 
 		String player = args[1].toUpperCase().toLowerCase();
 		plugin.data.bannedPlayers.remove(player);
 		plugin.saveData();
 
		String message = "Allowed " + args[1];
		message += " to receive Ender Dragon experience.";
 		Message.info(sender, message);
 
 		return true;
 	}
 
 	private boolean lookup(CommandSender sender, String[] args)
 	{
 		if(!(plugin.hasPermission(sender, "enderspawn.lookup")))
 			return true;
 
 		if(args.length < 2)
 			return false;
 
 		if(sender instanceof Player)
 		{
 			String message = ((Player)sender).getName() + ": /enderspawn lookup ";
 			plugin.log.info(message + args[1]);
 		}
 
 		String player = args[1].toUpperCase().toLowerCase();
 		String reason = plugin.data.bannedPlayers.get(player);
 
 		if(reason == null)
 		{
 			String message = args[1];
 			message += " is not banned from receiving Ender Dragon experience.";
 			Message.info(sender, message);
 			return true;
 		}
 
 		Message.info(sender, args[1] + " is banned for: " + reason);
 		return true;
 	}
 
 	private boolean status(CommandSender sender, String[] args)
 	{
 		if(args.length >= 2)
 			return statusOther(sender, args);
 
 		if(!(plugin.hasPermission(sender, "enderspawn.status")))
 			return true;
 
 		if(!(plugin.hasPermission(sender, "enderspawn.exp", false)))
 		{
 			String message = "You are not allowed to receive ";
 			message += "Ender Dragon experience.";
 			Message.info(sender, message);
 		}
 
 		if(sender instanceof Player)
 			plugin.log.info(((Player)sender).getName() + ": /enderspawn status");
 
 		return plugin.showStatus((Player) sender, null);
 	}
 
 	private boolean statusOther(CommandSender sender, String[] args)
 	{
 		if(args.length < 2)
 			return false;
 
 		if(!(plugin.hasPermission(sender, "enderspawn.status.other")))
 			return true;
 
 		if(sender instanceof Player)
 		{
 			String message = ((Player)sender).getName() + ": /enderspawn status ";
 			plugin.log.info(message + args[1]);
 		}
 
 		return plugin.showStatus((Player) sender, args[1]);
 	}
 
 	private boolean reset(CommandSender sender, String[] args)
 	{
 		if(!(plugin.hasPermission(sender, "enderspawn.reset")))
 			return true;
 
 		if(args.length < 2)
 			return false;
 
 		if(sender instanceof Player)
 		{
 			String message = ((Player)sender).getName() + ": /enderspawn reset ";
 			plugin.log.info(message + args[1]);
 		}
 
 		String player = args[1].toUpperCase().toLowerCase();
 		plugin.data.players.remove(player);
 		plugin.saveData();
 
		String message = "Allowed " + args[1];
		message += " to receive Ender Dragon experience.";
 		Message.info(sender, message);
 
 		return true;
 	}
 }
