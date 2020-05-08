 package me.furt.CraftEssence.commands;
 
 import me.furt.CraftEssence.CraftEssence;
 
 import org.bukkit.ChatColor;
 import org.bukkit.GameMode;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 public class GameModeCommand implements CommandExecutor {
 	private final CraftEssence plugin;
 
 	public GameModeCommand(CraftEssence instance) {
 		this.plugin = instance;
 	}
 
 	@Override
 	public boolean onCommand(CommandSender sender, Command cmd, String label,
 			String[] args) {
 		if (!plugin.hasPerm(sender, "gamemode", false)) {
 			sender.sendMessage(ChatColor.YELLOW
 					+ "You do not have permission to use /" + label);
 			return true;
 		}
 
 		Player player = (Player) sender;
 		if (args.length == 1) {
			if (args[0].equalsIgnoreCase("survival") || (args[1].equalsIgnoreCase("0"))) {
 				player.setGameMode(GameMode.SURVIVAL);
 				player.sendMessage(CraftEssence.premessage
 						+ "Your gamemode is now set to Survival.");
			} else if (args[0].equalsIgnoreCase("creative") || (args[1].equalsIgnoreCase("1"))) {
 				player.setGameMode(GameMode.CREATIVE);
 				player.sendMessage(CraftEssence.premessage
 						+ "Your gamemode is now set to Creative.");
 			} else {
 				player.sendMessage(CraftEssence.premessage + "Gamemode "
 						+ args[0] + " not found.");
 			}
 			return true;
 		} else if (args.length == 2) {
 			Player p = plugin.getServer().getPlayer(args[0]);
 			if (p == null) {
 				player.sendMessage(CraftEssence.premessage
 						+ "Player not found.");
 				return true;
 			}
 
 			if (args[1].equalsIgnoreCase("survival") || (args[1].equalsIgnoreCase("0"))) {
 				p.setGameMode(GameMode.SURVIVAL);
 				p.sendMessage(CraftEssence.premessage
 						+ "Your gamemode is now set to Survival.");
 				player.sendMessage(CraftEssence.premessage + args[0]
 						+ "'s gamemode is now set to Survival.");
 			} else if (args[1].equalsIgnoreCase("creative") || (args[1].equalsIgnoreCase("1"))) {
 				p.setGameMode(GameMode.CREATIVE);
 				p.sendMessage(CraftEssence.premessage
 						+ "Your gamemode is now set to Creative.");
 				player.sendMessage(CraftEssence.premessage + args[0]
 						+ "'s gamemode is now set to Creative.");
 			} else {
 				player.sendMessage(CraftEssence.premessage + "Gamemode "
 						+ args[1] + " not found.");
 			}
 			return true;
 		} else {
 			sender.sendMessage(CraftEssence.premessage
 					+ "Please select a gamemode: survival or creative");
 			return true;
 		}
 	}
 
 }
