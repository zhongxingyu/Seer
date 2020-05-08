 package com.censoredsoftware.Demigods.Engine.Command;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Set;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.Material;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.command.ConsoleCommandSender;
 import org.bukkit.entity.Player;
 
 import com.censoredsoftware.Demigods.Engine.Demigods;
 import com.censoredsoftware.Demigods.Engine.Object.Ability.Ability;
 import com.censoredsoftware.Demigods.Engine.Object.Deity.Deity;
 import com.censoredsoftware.Demigods.Engine.Object.General.DemigodsCommand;
 import com.censoredsoftware.Demigods.Engine.Object.Player.PlayerCharacter;
 import com.censoredsoftware.Demigods.Engine.Object.Player.PlayerWrapper;
 import com.censoredsoftware.Demigods.Engine.Utility.AdminUtility;
 import com.censoredsoftware.Demigods.Engine.Utility.DataUtility;
 import com.censoredsoftware.Demigods.Engine.Utility.TextUtility;
 import com.censoredsoftware.Demigods.Engine.Utility.UnicodeUtility;
 import com.google.common.collect.Lists;
 
 public class MainCommand extends DemigodsCommand
 {
 	@Override
 	public List<String> getCommands()
 	{
 		return Lists.newArrayList("demigods");
 	}
 
 	@Override
 	public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
 	{
 		// Check for console first
 		if(sender instanceof ConsoleCommandSender) return Demigods.message.noConsole((ConsoleCommandSender) sender);
 
 		// Check args and pass onto dg_extended() if need be
 		if(args.length > 0)
 		{
 			dg_extended(sender, args);
 			return true;
 		}
 
 		// Define Player
 		Player player = Bukkit.getOfflinePlayer(sender.getName()).getPlayer();
 
 		// Check Permissions
 		if(!player.hasPermission("demigods.basic")) return Demigods.message.noPermission(player);
 
 		Demigods.message.tagged(sender, "Documentation");
 		for(String alliance : Deity.getLoadedDeityAlliances())
 		{
 			if(!sender.hasPermission("demigods." + alliance.toLowerCase())) continue;
 			sender.sendMessage(ChatColor.GRAY + " /dg " + alliance.toLowerCase());
 		}
 		sender.sendMessage(ChatColor.GRAY + " /dg info");
 		sender.sendMessage(ChatColor.GRAY + " /dg commands");
 		if(player.hasPermission("demigods.admin")) sender.sendMessage(ChatColor.RED + " /dg admin");
 		sender.sendMessage(" ");
 		sender.sendMessage(ChatColor.WHITE + " Use " + ChatColor.YELLOW + "/check" + ChatColor.WHITE + " to see your player information.");
 		return true;
 	}
 
 	private static boolean dg_extended(CommandSender sender, String[] args)
 	{
 		// Define Player
 		Player player = Bukkit.getOfflinePlayer(sender.getName()).getPlayer();
 
 		// Define args
 		String category = args[0];
 		String option1 = null, option2 = null, option3 = null, option4 = null;
 		if(args.length >= 2) option1 = args[1];
 		if(args.length >= 3) option2 = args[2];
 		if(args.length >= 4) option3 = args[3];
 		if(args.length >= 5) option4 = args[4];
 
 		// Check Permissions
 		if(!player.hasPermission("demigods.basic")) return Demigods.message.noPermission(player);
 
 		if(category.equalsIgnoreCase("admin"))
 		{
 			dg_admin(sender, option1, option2, option3, option4);
 		}
 		else if(category.equalsIgnoreCase("commands"))
 		{
 			Demigods.message.tagged(sender, "Command Directory");
 			sender.sendMessage(ChatColor.GRAY + " There's nothing here...");
 		}
 		else if(category.equalsIgnoreCase("info"))
 		{
 			if(option1 == null)
 			{
 				Demigods.message.tagged(sender, "Information Directory");
 				sender.sendMessage(ChatColor.GRAY + " /dg info characters");
 				sender.sendMessage(ChatColor.GRAY + " /dg info shrines");
 				sender.sendMessage(ChatColor.GRAY + " /dg info tributes");
 				sender.sendMessage(ChatColor.GRAY + " /dg info players");
 				sender.sendMessage(ChatColor.GRAY + " /dg info pvp");
 				sender.sendMessage(ChatColor.GRAY + " /dg info stats");
 				sender.sendMessage(ChatColor.GRAY + " /dg info rankings");
 				sender.sendMessage(ChatColor.GRAY + " /dg info demigods");
 			}
 			else if(option1.equalsIgnoreCase("demigods"))
 			{
 				Demigods.message.tagged(sender, "About the Plugin");
 				sender.sendMessage(ChatColor.WHITE + " Not to be confused with other RPG plugins that focus on skills and classes alone, " + ChatColor.GREEN + "Demigods" + ChatColor.WHITE + " adds culture and conflict that will keep players coming back even after they've maxed out their levels and found all of the diamonds in a 50km radius.");
 				sender.sendMessage(" ");
 				sender.sendMessage(ChatColor.GREEN + " Demigods" + ChatColor.WHITE + " is unique in its system of rewarding players for both adventuring (tributes) and conquering (PvP) with a wide array of fun and useful skills.");
 				sender.sendMessage(" ");
 				sender.sendMessage(ChatColor.WHITE + " Re-enact mythological battles and rise from a mere player to a full-fledged Olympian as you form new Alliances with mythical groups and battle to the bitter end.");
 				sender.sendMessage(" ");
 				sender.sendMessage(ChatColor.GRAY + " Developed by: " + ChatColor.GREEN + "_Alex" + ChatColor.GRAY + " and " + ChatColor.GREEN + "HmmmQuestionMark");
 				sender.sendMessage(ChatColor.GRAY + " Website: " + ChatColor.YELLOW + "http://demigodsrpg.com/");
 				sender.sendMessage(ChatColor.GRAY + " Source: " + ChatColor.YELLOW + "https://github.com/CensoredSoftware/Minecraft-Demigods");
 			}
 			else if(option1.equalsIgnoreCase("characters"))
 			{
 				Demigods.message.tagged(sender, "Characters");
 				sender.sendMessage(ChatColor.GRAY + " This is some info about Characters.");
 			}
 			else if(option1.equalsIgnoreCase("shrine"))
 			{
 				Demigods.message.tagged(sender, "Shrines");
 				sender.sendMessage(ChatColor.GRAY + " This is some info about Shrines.");
 			}
 			else if(option1.equalsIgnoreCase("tribute"))
 			{
 				Demigods.message.tagged(sender, "Tributes");
 				sender.sendMessage(ChatColor.GRAY + " This is some info about Tributes.");
 			}
 			else if(option1.equalsIgnoreCase("player"))
 			{
 				Demigods.message.tagged(sender, "Players");
 				sender.sendMessage(ChatColor.GRAY + " This is some info about Players.");
 			}
 			else if(option1.equalsIgnoreCase("pvp"))
 			{
 				Demigods.message.tagged(sender, "PVP");
 				sender.sendMessage(ChatColor.GRAY + " This is some info about PVP.");
 			}
 			else if(option1.equalsIgnoreCase("stats"))
 			{
 				Demigods.message.tagged(sender, "Stats");
 				sender.sendMessage(ChatColor.GRAY + " Read some server-wide stats for Demigods.");
 			}
 			else if(option1.equalsIgnoreCase("rankings"))
 			{
 				Demigods.message.tagged(sender, "Rankings");
 				sender.sendMessage(ChatColor.GRAY + " This is some ranking info about Demigods.");
 			}
 		}
 
 		for(String alliance : Deity.getLoadedDeityAlliances())
 		{
 			if(!sender.hasPermission("demigods." + alliance.toLowerCase())) continue;
 			if(category.equalsIgnoreCase(alliance))
 			{
 				if(args.length < 2)
 				{
 					Demigods.message.tagged(sender, alliance + " Directory");
 					for(Deity deity : Deity.getAllDeitiesInAlliance(alliance))
 						sender.sendMessage(ChatColor.GRAY + " /dg " + alliance.toLowerCase() + " " + deity.getInfo().getName().toLowerCase());
 				}
 				else
 				{
 					for(final Deity deity : Deity.getAllDeitiesInAlliance(alliance))
 					{
 						assert option1 != null;
 						if(option1.equalsIgnoreCase(deity.getInfo().getName()))
 						{
 							try
 							{
 								for(String toPrint : new ArrayList<String>()
 								{
 									{
 										addAll(deity.getInfo().getLore());
 										for(Ability ability : deity.getAbilities())
 										{
 											for(String detail : ability.getInfo().getDetails())
 											{
												StringBuilder details = new StringBuilder(ChatColor.GRAY + " " + UnicodeUtility.rightwardArrow() + " ");
												if(ability.getInfo().getCommand() != null) details.append(ChatColor.GREEN + "/" + ability.getInfo().getCommand().toLowerCase() + ChatColor.WHITE + ": ");
												details.append(ChatColor.WHITE + detail);
												add(details.toString());
 											}
 										}
 									}
 								})
 								{
 									sender.sendMessage(toPrint);
 								}
 								return true;
 							}
 							catch(Exception e)
 							{
 								sender.sendMessage(ChatColor.RED + "(ERR: 3001)  Please report this immediately.");
 								e.printStackTrace(); // DEBUG
 								return true;
 							}
 						}
 					}
 					sender.sendMessage(ChatColor.DARK_RED + " No such deity, please try again.");
 					return false;
 				}
 			}
 		}
 		return true;
 	}
 
 	private static boolean dg_admin(CommandSender sender, String option1, String option2, String option3, String option4)
 	{
 		Player player = Bukkit.getOfflinePlayer(sender.getName()).getPlayer();
 		Player toEdit;
 		PlayerCharacter character;
 		int amount;
 
 		if(!player.hasPermission("demigods.admin")) return Demigods.message.noPermission(player);
 
 		if(option1 == null)
 		{
 			Demigods.message.tagged(sender, "Admin Directory");
 			sender.sendMessage(ChatColor.GRAY + " /dg admin wand");
 			sender.sendMessage(ChatColor.GRAY + " /dg admin debug");
 			sender.sendMessage(ChatColor.GRAY + " /dg admin check <p> <char>");
 			sender.sendMessage(ChatColor.GRAY + " /dg admin remove [player|character] <name>");
 			sender.sendMessage(ChatColor.GRAY + " /dg admin set [maxfavor|favor|devotion|ascensions] <p> <amt>");
 			sender.sendMessage(ChatColor.GRAY + " /dg admin add [maxfavor|favor|devotion|ascensions] <p> <amt>");
 			sender.sendMessage(ChatColor.GRAY + " /dg admin sub [maxfavor|favor|devotion|ascensions] <p> <amt>");
 			sender.sendMessage(ChatColor.RED + " /dg admin clear data yesdoitforsurepermanantly");
 		}
 
 		if(option1 != null)
 		{
 			if(option1.equalsIgnoreCase("clear") && option2 != null && option2.equalsIgnoreCase("data") && option3 != null && option3.equalsIgnoreCase("yesdoitforsurepermanantly"))
 			{
 				player.sendMessage(ChatColor.RED + Demigods.text.getText(TextUtility.Text.ADMIN_CLEAR_DATA_STARTING));
 				DataUtility.flushData();
 				player.sendMessage(ChatColor.GREEN + Demigods.text.getText(TextUtility.Text.ADMIN_CLEAR_DATA_FINISHED));
 				return true;
 			}
 			if(option1.equalsIgnoreCase("wand"))
 			{
 				if(!AdminUtility.wandEnabled(player))
 				{
 					DataUtility.saveTemp(player.getName(), "temp_admin_wand", true);
 					player.sendMessage(ChatColor.RED + "Your admin wand has been enabled for " + Material.getMaterial(Demigods.config.getSettingInt("admin.wand_tool")));
 				}
 				else if(AdminUtility.wandEnabled(player))
 				{
 					DataUtility.removeTemp(player.getName(), "temp_admin_wand");
 					player.sendMessage(ChatColor.RED + "You have disabled your admin wand.");
 				}
 				return true;
 			}
 			else if(option1.equalsIgnoreCase("debug"))
 			{
 				if(!DataUtility.hasKeyTemp(player.getName(), "temp_admin_debug") || !Boolean.parseBoolean(DataUtility.getValueTemp(player.getName(), "temp_admin_debug").toString()))
 				{
 					DataUtility.saveTemp(player.getName(), "temp_admin_debug", true);
 					player.sendMessage(ChatColor.RED + "You have enabled debugging.");
 				}
 				else if(DataUtility.hasKeyTemp(player.getName(), "temp_admin_debug") && Boolean.parseBoolean(DataUtility.getValueTemp(player.getName(), "temp_admin_debug").toString()))
 				{
 					DataUtility.removeTemp(player.getName(), "temp_admin_debug");
 					player.sendMessage(ChatColor.RED + "You have disabled debugging.");
 				}
 			}
 			else if(option1.equalsIgnoreCase("check"))
 			{
 				if(option2 == null)
 				{
 					sender.sendMessage(ChatColor.RED + "You need to specify a player.");
 					sender.sendMessage("/dg admin check <p>");
 					return true;
 				}
 
 				// Define variables
 				Player toCheck = Bukkit.getPlayer(option2);
 
 				if(option3 == null)
 				{
 					Demigods.message.tagged(sender, ChatColor.RED + toCheck.getName() + " Player Check");
 					sender.sendMessage(" Characters:");
 
 					final Set<PlayerCharacter> chars = PlayerWrapper.getCharacters(toCheck);
 
 					for(PlayerCharacter checkingChar : chars)
 					{
 						player.sendMessage(ChatColor.GRAY + "   (#: " + checkingChar.getId() + ") Name: " + checkingChar.getName() + " / Deity: " + checkingChar.getDeity());
 					}
 				}
 				else
 				{
 					// TODO: Display specific character information when called for.
 				}
 			}
 			else if(option1.equalsIgnoreCase("remove"))
 			{
 				if(option2 == null || option3 == null)
 				{
 					sender.sendMessage(ChatColor.RED + "You need to be more specific with what you want to remove.");
 					return true;
 				}
 				else
 				{
 					if(option2.equalsIgnoreCase("player"))
 					{
 						// TODO: Full player data removal
 					}
 					else if(option2.equalsIgnoreCase("character"))
 					{
 						PlayerCharacter removing = PlayerCharacter.getCharacterByName(option3);
 						String removingName = removing.getName();
 
 						// Remove the data
 						removing.remove();
 
 						sender.sendMessage(ChatColor.RED + "Character \"" + removingName + "\" removed.");
 					}
 				}
 			}
 			else if(option1.equalsIgnoreCase("set"))
 			{
 				if(option2 == null || option3 == null)
 				{
 					sender.sendMessage(ChatColor.RED + "You need to specify a player and amount.");
 					return true;
 				}
 				else
 				{
 					// Define variables
 					toEdit = Bukkit.getPlayer(option3);
 					character = PlayerWrapper.getPlayer(toEdit).getCurrent();
 					amount = Integer.parseInt(option4);
 				}
 
 				if(option2.equalsIgnoreCase("maxfavor"))
 				{
 					// Set the favor
 					character.getMeta().setMaxFavor(amount);
 
 					sender.sendMessage(ChatColor.GREEN + "Max favor set to " + amount + " for " + toEdit.getName() + "'s current character.");
 
 					// Tell who was edited
 					toEdit.sendMessage(ChatColor.GREEN + "Your current character's max favor has been set to " + amount + ".");
 					toEdit.sendMessage(ChatColor.GRAY + "" + ChatColor.ITALIC + "This was performed by " + sender.getName() + ".");
 					return true;
 				}
 				else if(option2.equalsIgnoreCase("favor"))
 				{
 					// Set the favor
 					character.getMeta().setFavor(amount);
 
 					sender.sendMessage(ChatColor.GREEN + "Favor set to " + amount + " for " + toEdit.getName() + "'s current character.");
 
 					// Tell who was edited
 					toEdit.sendMessage(ChatColor.GREEN + "Your current character's favor has been set to " + amount + ".");
 					toEdit.sendMessage(ChatColor.GRAY + "" + ChatColor.ITALIC + "This was performed by " + sender.getName() + ".");
 					return true;
 				}
 				else if(option2.equalsIgnoreCase("ascensions"))
 				{
 					// Set the ascensions
 					character.getMeta().setAscensions(amount);
 
 					sender.sendMessage(ChatColor.GREEN + "Ascensions set to " + amount + " for " + toEdit.getName() + "'s current character.");
 
 					// Tell who was edited
 					toEdit.sendMessage(ChatColor.GREEN + "Your current character's Ascensions have been set to " + amount + ".");
 					toEdit.sendMessage(ChatColor.GRAY + "" + ChatColor.ITALIC + "This was performed by " + sender.getName() + ".");
 					return true;
 				}
 			}
 			else if(option1.equalsIgnoreCase("add"))
 			{
 				if(option2 == null)
 				{
 					sender.sendMessage(ChatColor.RED + "You need to be more specific.");
 					return true;
 				}
 				else if(option3 == null)
 				{
 					sender.sendMessage(ChatColor.RED + "You must select a player and amount.");
 					return true;
 				}
 				else
 				{
 					// Define variables
 					toEdit = Bukkit.getPlayer(option3);
 					character = PlayerWrapper.getPlayer(toEdit).getCurrent();
 					amount = Integer.parseInt(option4);
 				}
 
 				if(option2.equalsIgnoreCase("maxfavor"))
 				{
 					// Set the favor
 					character.getMeta().setMaxFavor(character.getMeta().getMaxFavor() + amount);
 
 					sender.sendMessage(ChatColor.GREEN + "" + amount + " added to " + toEdit.getName() + "'s current character's max favor.");
 
 					// Tell who was edited
 					toEdit.sendMessage(ChatColor.GREEN + "Your current character's max favor has been increased by " + amount + ".");
 					toEdit.sendMessage(ChatColor.GRAY + "" + ChatColor.ITALIC + "This was performed by " + sender.getName() + ".");
 					return true;
 				}
 				else if(option2.equalsIgnoreCase("favor"))
 				{
 					// Set the favor
 					character.getMeta().addFavor(amount);
 
 					sender.sendMessage(ChatColor.GREEN + "" + amount + " favor added to " + toEdit.getName() + "'s current character.");
 
 					// Tell who was edited
 					toEdit.sendMessage(ChatColor.GREEN + "Your current character has been given " + amount + " favor.");
 					toEdit.sendMessage(ChatColor.GRAY + "" + ChatColor.ITALIC + "This was performed by " + sender.getName() + ".");
 					return true;
 				}
 				else if(option2.equalsIgnoreCase("ascensions"))
 				{
 					// Set the ascensions
 					character.getMeta().addAscensions(amount);
 
 					sender.sendMessage(ChatColor.GREEN + "" + amount + " Ascension(s) added to " + toEdit.getName() + "'s current character.");
 
 					// Tell who was edited
 					toEdit.sendMessage(ChatColor.GREEN + "Your current character has been given " + amount + " Ascensions.");
 					toEdit.sendMessage(ChatColor.GRAY + "" + ChatColor.ITALIC + "This was performed by " + sender.getName() + ".");
 					return true;
 				}
 			}
 			else if(option1.equalsIgnoreCase("sub"))
 			{
 				if(option2 == null)
 				{
 					sender.sendMessage(ChatColor.RED + "You need to be more specific.");
 					return true;
 				}
 				else if(option3 == null)
 				{
 					sender.sendMessage(ChatColor.RED + "You must select a player and amount.");
 					return true;
 				}
 				else
 				{
 					// Define variables
 					toEdit = Bukkit.getPlayer(option3);
 					character = PlayerWrapper.getPlayer(toEdit).getCurrent();
 					amount = Integer.parseInt(option4);
 				}
 
 				if(option2.equalsIgnoreCase("maxfavor"))
 				{
 					// Set the favor
 					character.getMeta().subtractFavor(amount);
 
 					sender.sendMessage(ChatColor.GREEN + "" + amount + " removed from " + toEdit.getName() + "'s current character's max favor.");
 
 					// Tell who was edited
 					toEdit.sendMessage(ChatColor.RED + "Your current character's max favor has been reduced by " + amount + ".");
 					toEdit.sendMessage(ChatColor.GRAY + "" + ChatColor.ITALIC + "This was performed by " + sender.getName() + ".");
 					return true;
 				}
 				if(option2.equalsIgnoreCase("favor"))
 				{
 					// Set the favor
 					character.getMeta().subtractFavor(amount);
 
 					sender.sendMessage(ChatColor.GREEN + "" + amount + " favor removed from " + toEdit.getName() + "'s current character.");
 
 					// Tell who was edited
 					toEdit.sendMessage(ChatColor.RED + "Your current character has had " + amount + " favor removed.");
 					toEdit.sendMessage(ChatColor.GRAY + "" + ChatColor.ITALIC + "This was performed by " + sender.getName() + ".");
 					return true;
 				}
 				else if(option2.equalsIgnoreCase("ascensions"))
 				{
 					// Set the ascensions
 					character.getMeta().subtractAscensions(amount);
 
 					sender.sendMessage(ChatColor.GREEN + "" + amount + " Ascension(s) removed from " + toEdit.getName() + "'s current character.");
 
 					// Tell who was edited
 					toEdit.sendMessage(ChatColor.RED + "Your current character has had " + amount + " Ascension(s) removed.");
 					toEdit.sendMessage(ChatColor.GRAY + "" + ChatColor.ITALIC + "This was performed by " + sender.getName() + ".");
 					return true;
 				}
 			}
 			else
 			{
 				sender.sendMessage(ChatColor.RED + "Invalid category selected.");
 				sender.sendMessage("/dg admin [set|add|sub] [maxfavor|favor|devotion|ascensions] <p> <amt>");
 				return true;
 			}
 		}
 		return true;
 	}
 }
