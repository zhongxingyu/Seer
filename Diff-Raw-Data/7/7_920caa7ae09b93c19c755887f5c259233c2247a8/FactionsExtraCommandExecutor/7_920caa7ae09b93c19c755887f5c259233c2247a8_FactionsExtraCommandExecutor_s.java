 package com.mutinycraft.jigsaw.FactionsExtra;
 
 import java.util.List;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 
 import com.massivecraft.factions.Faction;
 import com.massivecraft.factions.Factions;
 
 public class FactionsExtraCommandExecutor implements CommandExecutor {
 
 	FactionsExtra plugin;
 
 	public FactionsExtraCommandExecutor(FactionsExtra pl) {
 		plugin = pl;
 	}
 
 	@Override
 	public boolean onCommand(CommandSender sender, Command cmd, String label,
 			String[] args) {
 		if (cmd.getName().equalsIgnoreCase("factionscore")) {
 			if (args.length == 1) {
 				commandFactionScore(sender, args[0]);
 				return true;
 			} else {
 				sender.sendMessage(ChatColor.RED
 						+ "Usage: /factionscore [Faction Name]");
 				return true;
 			}
 		}
 		if (cmd.getName().equalsIgnoreCase("factiontier")) {
 			if (args.length == 2) {
 				commandFactionTier(sender, args[0], args[1]);
 				return true;
 			} else {
 				sender.sendMessage(ChatColor.RED
 						+ "Usage: /factiontier [Faction Name] [Tier]");
 				return true;
 			}
 		}
 		return false;
 	}
 
 	private void commandFactionScore(CommandSender sender, String factionName) {
 		int score = 0;
 		int tier = 0;
 		String fName = null;
 		try {
 			Faction f = Factions.i.getByTag(factionName);
 			score = plugin.getFactionScore(f.getId());
 			tier = plugin.getFactionTier(f.getId());
 			fName = f.getTag();
 		} catch (Exception e) {
 			score = 0;
 			tier = 0;
 			fName = null;
 		}
 		if (tier >= 1) {
 			sender.sendMessage(ChatColor.GREEN + fName + " has " + score
 					+ " points and is a tier " + tier + " faction.");
 		} else {
 			sender.sendMessage(ChatColor.GREEN
 					+ "The score of that faction is not recorded.");
 		}
 	}
 
 	private void commandFactionTier(CommandSender sender, String factionName,
 			String tierString) {
		int tier = Integer.valueOf(tierString);
 		if (tier == 0 || tier == 1 || tier == 2) {
 			try {
 				Faction f = Factions.i.getByTag(factionName);
 				List<Integer> data = plugin.getFactionData(f.getId());
 				data.set(1, tier);
 				plugin.updateFaction(f.getId(), data);
 				sender.sendMessage(ChatColor.GREEN + f.getTag()
 						+ " has been changed to a tier " + tier + " faction.");
 			} catch (Exception e) {
 				sender.sendMessage(ChatColor.GREEN
 						+ "That faction doesn't exist in the database.");
 			}
 		} else {
 			sender.sendMessage(ChatColor.GREEN + "That is not a valid tier.");
 		}
 	}
 }
