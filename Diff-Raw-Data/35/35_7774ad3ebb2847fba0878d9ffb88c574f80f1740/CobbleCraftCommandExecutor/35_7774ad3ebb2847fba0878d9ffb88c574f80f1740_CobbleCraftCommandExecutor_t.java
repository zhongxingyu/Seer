 package com.bukkitfiller.CobbleCraft;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 public class CobbleCraftCommandExecutor implements CommandExecutor {
 	private CobbleCraft plugin;	
 	
 	CobbleCraftCommandExecutor(CobbleCraft plugin) {
 		this.plugin = plugin;
 	}
 	
 	private void showStats(Player player, int[] levelArray, String type) {
 	    String fileName = plugin.FILEDIRECTORY + player.getName() + ".stats";
 		
 		int playerLevel = plugin.fileHandler.getLevel(fileName, type.toUpperCase());
 		double playerExp = plugin.fileHandler.getNumProperty(fileName, type.toUpperCase());
 		player.sendMessage("Current '" + type + "' level: " + ChatColor.GOLD + playerLevel);
 		player.sendMessage("Exp left to next level: " + ChatColor.GOLD + plugin.fileHandler.getExpToGo(playerExp, levelArray, playerLevel + 1));
 	}
 
     public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
     	Player player = (Player)sender;
     	String fileName = plugin.FILEDIRECTORY + player.getName() + ".stats";
     	if (label.equalsIgnoreCase("mining")) {
     		showStats(player,plugin.lvlValues.MiningLevels,"Mining");
     		return true;
 		} else if (label.equalsIgnoreCase("digging")) {
     		showStats(player,plugin.lvlValues.DiggingLevels,"Digging");
     		return true;
 		} else if (label.equalsIgnoreCase("fishing")) {
     		showStats(player,plugin.lvlValues.FishingLevels,"Fishing");
     		return true;
 		} else if (label.equalsIgnoreCase("slaying")) {
     		showStats(player,plugin.lvlValues.SlayingLevels,"Slaying");
     		return true;
 		} else if (label.equalsIgnoreCase("hunting")) {
     		showStats(player,plugin.lvlValues.HuntingLevels,"Hunting");
     		return true;
 		} else if (label.equalsIgnoreCase("archery")) {
     		showStats(player,plugin.lvlValues.ArcheryLevels,"Archery");
     		return true;
 		} else if (label.equalsIgnoreCase("achievements")) {
     		plugin.fileHandler.getAchievements(fileName, player);
     		return true;
		} else if (label.equalsIgnoreCase("setrole") && args.length >= 1){
			String playerSequence = args[0].toLowerCase();
			String firstLetter = playerSequence.substring(0, 1);
			String playerName = playerSequence.replaceFirst(firstLetter, firstLetter.toUpperCase());
			player.setDisplayName(ChatColor.BLACK + "[" + ChatColor.YELLOW + playerName
					+ ChatColor.BLACK + "] " + player.getDisplayName());
 		}
 		return false;
     }
 }
