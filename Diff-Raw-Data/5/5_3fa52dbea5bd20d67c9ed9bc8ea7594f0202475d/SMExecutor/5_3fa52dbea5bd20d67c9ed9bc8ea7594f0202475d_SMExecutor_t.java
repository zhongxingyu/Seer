 /*
  The MIT License
 
  Copyright (c) 2012 Zloteanu Nichita (ZNickq) and Andre Mohren (IceReaper)
 
  Permission is hereby granted, free of charge, to any person obtaining a copy
  of this software and associated documentation files (the "Software"), to deal
  in the Software without restriction, including without limitation the rights
  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  copies of the Software, and to permit persons to whom the Software is
  furnished to do so, subject to the following conditions:
 
  The above copyright notice and this permission notice shall be included in
  all copies or substantial portions of the Software.
 
  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
  THE SOFTWARE.
  */
 
 package net.morematerials.morematerials.cmds;
 
 import java.util.HashMap;
 import java.util.Map;
 import java.util.logging.Level;
 import net.morematerials.morematerials.Main;
 import net.morematerials.morematerials.manager.MainManager;
 import org.apache.commons.lang.StringUtils;
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.getspout.spoutapi.inventory.SpoutItemStack;
 
 public class SMExecutor implements CommandExecutor {
 	private Main plugin;
 
 	public SMExecutor(Main plugin) {
 		this.plugin = plugin;
 	}
 
	@SuppressWarnings("unchecked")
 	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
 		if (args.length == 0) {
 			sender.sendMessage(MainManager.getUtils().getMessage(
 				"This server is running " + this.plugin.getDescription().getName() + " " +
 				"v" + plugin.getDescription().getVersion() + "! " +
 				"Credits to " + StringUtils.join(this.plugin.getDescription().getAuthors(), ", ") + "!")
 			);
 			return true;
 		}
 
 		// Help parameter.
 		if (args[0].equalsIgnoreCase("?") || args[0].equalsIgnoreCase("help")) {
 			// Someone specified the command to get help for.
 			if (args.length > 1) {
 				Map<String, Object> commands = (HashMap<String, Object>) this.plugin.getDescription().getCommands().values();
 				if (!commands.containsKey(args[1])) {
 					return false;
 				}
 				sender.sendMessage(MainManager.getUtils().getMessage("Help page for /" + args[1], Level.SEVERE));
 				sender.sendMessage(MainManager.getUtils().getMessage("---------------------------------"));
 				String commandInfo = (String) ((HashMap<String, Object>) commands.get(args[1])).get("usage");
 				for (String usage : StringUtils.split(commandInfo,"\n")) {
 					usage = usage.replaceAll("<command>", args[1] + ChatColor.GOLD);
 					sender.sendMessage(MainManager.getUtils().getMessage(usage, Level.WARNING));
 				}
 			// Someone wants to see all commands.
 			} else {
 				sender.sendMessage(MainManager.getUtils().getMessage("Help page", Level.SEVERE));
 				sender.sendMessage(MainManager.getUtils().getMessage("---------------------------------"));
 				// Getting commands from plugin.yml
 				// TODO unsafe cast warning remove
 				HashMap<String, Object> commands = (HashMap<String, Object>) this.plugin.getDescription().getCommands().values();
 				for (String commandsEntry : commands.keySet()) {
 					// TODO unsafe cast warning remove
 					HashMap<String, Object> commandInfo = (HashMap<String, Object>) commands.get(commandsEntry);
 					sender.sendMessage(MainManager.getUtils().getMessage("/" + commandsEntry + " -> " + ChatColor.GOLD + commandInfo.get("description"), Level.WARNING));
 				}
 			}
 		}
 		
 		// This is some kind of weird command - do we actualy need it?
 		if (args[0].equalsIgnoreCase("fixme")) {
 			if (!(sender instanceof Player)) {
 				return false;
 			}
 			Player player = (Player) sender;
 			SpoutItemStack itemStack = new SpoutItemStack(player.getItemInHand());
 			player.sendMessage(MainManager.getUtils().getMessage("The item in your hand is custom: " + itemStack.isCustomItem()));
 			player.sendMessage(MainManager.getUtils().getMessage("It's called " + itemStack.getMaterial().getName() + "!"));
 		}
 		
 		return true;
 	}
 }
