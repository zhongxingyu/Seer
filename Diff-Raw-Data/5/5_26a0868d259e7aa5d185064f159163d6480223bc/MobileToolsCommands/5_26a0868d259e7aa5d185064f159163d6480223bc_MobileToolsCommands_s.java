 /**
  * 
  * This software is part of the MobileTools
  * 
  * MobileTools is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or 
  * any later version.
  * 
  * MobileTools is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with MobileTools. If not, see <http://www.gnu.org/licenses/>.
  * 
  */
 package me.cybermaxke.mobiletools;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.permissions.Permission;
 
 public class MobileToolsCommands implements CommandExecutor {
 	private final MobileTools plugin;
 	private final MobileConfiguration config;
 
 	public MobileToolsCommands(MobileTools plugin) {
 		this.plugin = plugin;
 		this.config = plugin.getConfiguration();
 
 		plugin.getCommand("Chest").setExecutor(this);
 		plugin.getCommand("Craft").setExecutor(this);
 		plugin.getCommand("Furnace").setExecutor(this);
 		plugin.getCommand("Anvil").setExecutor(this);
 		plugin.getCommand("Brew").setExecutor(this);
 		plugin.getCommand("Enchant").setExecutor(this);
 		plugin.getCommand("MobileTools").setExecutor(this);
 	}
 
 	@Override
 	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
 		if (label.equalsIgnoreCase("MobileTools")) {
 			if (!sender.hasPermission(this.getPerm("cmd.perm"))) {
 				sender.sendMessage(ChatColor.RED + "You don't have permission to perform that command.");
 				return true;
 			}
 
 			if (args.length > 0) {
 				if (args[0].equalsIgnoreCase("Reload")) {
 					this.config.load();
 					return true;
 				} else if (args[0].equalsIgnoreCase("Help")) {
 					sender.sendMessage("------------------ MobileTools ------------------");
 					sender.sendMessage("'/MobileTools Help' - Shows the admin help page.");
 					sender.sendMessage("'/MobileTools Reload' - Reloads the config.");
 					return true;
 				}
 				/**
 				 * TODO: Adding a command to convert the files.
 				 */
 			}
 
 			sender.sendMessage("This command uses sub commands, you can list them by" +
 					" using '/MobileTools Help'");
 			return true;
 		}
 
 		if (!(sender instanceof Player)) {
 			sender.sendMessage("You have to be a player to perform that command.");
 			return true;
 		}
 
 		Player player = (Player) sender;
 		MobilePlayer mp = this.plugin.getPlayer(player);
 
 		if (label.equalsIgnoreCase("Chest")) {
 			if (!player.hasPermission(this.getPerm("chest.cmd.perm"))) {
 				sender.sendMessage(ChatColor.RED + "You don't have permission to perform that command.");
 			}
 
 			mp.updateChestSize();
 			mp.openChest();
 		} else if (label.equalsIgnoreCase("Craft")) {
 			if (!player.hasPermission(this.getPerm("craft.cmd.perm"))) {
 				sender.sendMessage(ChatColor.RED + "You don't have permission to perform that command.");
 				return true;
 			}
 
 			mp.openWorkbench();
 		} else if (label.equalsIgnoreCase("Furnace")) {
 			if (!player.hasPermission(this.getPerm("furnace.cmd.perm"))) {
 				sender.sendMessage(ChatColor.RED + "You don't have permission to perform that command.");
 				return true;
 			}
 
 			mp.openFurnace();
 		} else if (label.equalsIgnoreCase("Anvil")) {
 			if (!player.hasPermission(this.getPerm("anvil.cmd.perm"))) {
 				sender.sendMessage(ChatColor.RED + "You don't have permission to perform that command.");
 			}
 
 			mp.openAnvil();
 		} else if (label.equalsIgnoreCase("Brew")) {
 			if (!player.hasPermission(this.getPerm("brew.cmd.perm"))) {
 				sender.sendMessage(ChatColor.RED + "You don't have permission to perform that command.");
 				return true;
 			}
 
 			mp.openBrewingStand();
 		} else if (label.equalsIgnoreCase("Enchant")) {
 			if (!player.hasPermission(this.getPerm("enchant.cmd.perm"))) {
 				sender.sendMessage(ChatColor.RED + "You don't have permission to perform that command.");
 				return true;
 			}
 
 			mp.openEnchantingTable();
 		} else {
 			return false;
 		}
 
 		return true;
 	}
 
 	private Permission getPerm(String path) {
 		return this.config.getPermission(path);
 	}
 }
