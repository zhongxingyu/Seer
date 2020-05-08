 package com.censoredsoftware.Demigods.Engine.Command;
 
 import java.util.List;
 
 import org.apache.commons.lang.StringUtils;
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.Sound;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.command.ConsoleCommandSender;
 import org.bukkit.entity.Horse;
 import org.bukkit.entity.Player;
 
 import com.censoredsoftware.Demigods.Engine.Demigods;
 import com.censoredsoftware.Demigods.Engine.Object.Battle.Battle;
 import com.censoredsoftware.Demigods.Engine.Object.Battle.BattleParticipant;
 import com.censoredsoftware.Demigods.Engine.Object.General.DemigodsCommand;
 import com.censoredsoftware.Demigods.Engine.Object.Player.PlayerCharacter;
 import com.censoredsoftware.Demigods.Engine.Object.Player.PlayerWrapper;
 import com.censoredsoftware.Demigods.Engine.Utility.MiscUtility;
 import com.google.common.collect.Lists;
 
 public class DevelopmentCommands extends DemigodsCommand
 {
 	@Override
 	public List<String> getCommands()
 	{
 		return Lists.newArrayList("test1", "test2", "test3", "hspawn", "soundtest", "removechar");
 	}
 
 	@Override
 	public boolean onCommand(CommandSender sender, Command command, String labels, String[] args)
 	{
 		if(command.getName().equalsIgnoreCase("removechar")) return removeChar(sender, args);
 		else if(command.getName().equalsIgnoreCase("test1")) return test1(sender, args);
 		else if(command.getName().equalsIgnoreCase("test2")) return test2(sender, args);
 		else if(command.getName().equalsIgnoreCase("test3")) return test3(sender, args);
 		else if(command.getName().equalsIgnoreCase("hspawn")) return hspawn(sender);
 		else if(command.getName().equalsIgnoreCase("soundtest")) return soundTest(sender, args);
 		return false;
 	}
 
 	private static boolean test1(CommandSender sender, final String[] args)
 	{
 		Player player = (Player) sender;
 
 		player.sendMessage("Disabling all battles...");
 
 		for(Battle battle : Battle.getAllActive())
 		{
 			battle.end();
 		}
 
 		player.sendMessage("All battles disabled!");
 
 		return true;
 	}
 
 	private static boolean test2(CommandSender sender, final String[] args)
 	{
 		Player player = (Player) sender;
 
		player.sendMessage("Deleting all battles...");

		for(Battle battle : Battle.getAll())
		{
			battle.delete();
		}

		player.sendMessage("All battles deleted!");
 
 		return true;
 	}
 
 	private static boolean test3(CommandSender sender, final String[] args)
 	{
 		Player player = (Player) sender;
 
 		if(Battle.existsInRadius(player.getLocation()))
 		{
 			Battle getInfo = Battle.getInRadius(player.getLocation());
 
 			Demigods.message.chatTitle(ChatColor.DARK_AQUA + "Battle Info");
 			Location center = getInfo.getStartLocation();
 			player.sendMessage(ChatColor.YELLOW + "Center: " + ChatColor.GRAY + StringUtils.capitalize(center.getWorld().getName().toLowerCase()) + ": " + Math.round(center.getX()) + ", " + Math.round(center.getY()) + ", " + Math.round(center.getZ()));
 			StringBuilder butts = new StringBuilder(ChatColor.YELLOW + "Participants: ");
 			for(BattleParticipant participant : getInfo.getMeta().getParticipants())
 				if(participant instanceof PlayerCharacter) butts.append(((PlayerCharacter) participant).getName() + ", ");
 			player.sendMessage(ChatColor.YELLOW + "Participants: " + ChatColor.GRAY + butts.substring(0, 2) + ".");
 		}
 
 		return true;
 	}
 
 	private static boolean hspawn(CommandSender sender)
 	{
 		Player player = (Player) sender;
 
 		// This SHOULD happen automatically, but bukkit doesn't do this, so we need to.
 
 		if(player.isInsideVehicle() && player.getVehicle() instanceof Horse)
 		{
 			Horse horse = (Horse) player.getVehicle();
 			horse.eject();
 			horse.teleport(player.getLocation().getWorld().getSpawnLocation());
 			horse.setPassenger(player);
 			player.sendMessage(ChatColor.YELLOW + "Teleported to spawn...");
 		}
 
 		return true;
 	}
 
 	private static boolean soundTest(CommandSender sender, final String[] args)
 	{
 		if(sender instanceof ConsoleCommandSender) return false;
 		Player player = (Player) sender;
 		try
 		{
 			Sound sound = Sound.valueOf(args[0].toUpperCase());
 			if(!MiscUtility.isFloat(args[1].toUpperCase()))
 			{
 				player.sendMessage(ChatColor.RED + "Set a pitch, ie: 1F");
 				return false;
 			}
 			else
 			{
 				player.playSound(player.getLocation(), sound, 1F, Float.parseFloat(args[1].toUpperCase()));
 				player.sendMessage(ChatColor.YELLOW + "Sound played.");
 				return true;
 			}
 		}
 		catch(Exception ignored)
 		{}
 		player.sendMessage(ChatColor.RED + "Wrong arguments, please try again.");
 		return false;
 	}
 
 	private static boolean removeChar(CommandSender sender, String[] args)
 	{
 		if(args.length != 1) return false;
 
 		// Define args
 		Player player = Bukkit.getOfflinePlayer(sender.getName()).getPlayer();
 		String charName = args[0];
 
 		if(PlayerWrapper.hasCharName(player, charName))
 		{
 			PlayerCharacter character = PlayerCharacter.getCharacterByName(charName);
 			character.remove();
 
 			sender.sendMessage(ChatColor.RED + "Character removed!");
 		}
 		else sender.sendMessage(ChatColor.RED + "There was an error while removing your character.");
 
 		return true;
 	}
 }
