 package com.github.spy1134.adminsword;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 public class AdminSwordCommands extends AdminSwordMain implements CommandExecutor {
 	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args)
 	{
 		// If no arguments are given...
 		if (args.length == 0)
 		{
 			// Run the toggle function and return what it returns.
 			return toggleCommand(sender, cmd, commandLabel, args);
 		}
 		
 		// If the kill argument was given...
 		else if (args[0].equals("kill") || args[0].equals("k"))
 		{
 			return killCommand(sender, args);
 		}
 		
 		// If the toggle argument was given...
 		else if (args[0].equals("toggle") || args[0].equals("t"))
 		{
 			// Run the toggle function and return what it returns.
 			return toggleCommand(sender, cmd, commandLabel, args);
 		}
 		
 		// If the type argument was given...
 		else if (args[0].equals("type") || args[0].equals("ct"))
 		{
 			// Run the set event function then return true.
 			return setTypeCommand(sender, cmd, commandLabel, args);
 		}
 		
 		// If the types argument was given...
 		else if (args[0].equals("types"))
 		{
 			printTypes(sender);
 			return true;
 		}
 		
 		// If the check argument was given...
 		else if (args[0].equals("check") || args[0].equals("c"))
 		{
 			// Run the check function and return true.
 			checkCommand(sender, cmd, commandLabel, args);
 			return true;
 		}
 		
 		// If the fly argument was given...
 		else if (args[0].equals("fly") || args[0].equals("f"))
 		{
 			flyCommand(sender, args);
 			return true;
 		}
 		
 		// If the help argument was given...
 		else if (args[0].equals("help") || args[0].equals("h"))
 		{
 			// If a second argument was given...
 			if (args.length > 1)
 			{
 				// Request the specified help page.
 				printHelp(sender, args[1]);
 			}
 			// If no second argument was given...
 			else
 			{
 				// Print help page 1.
 				printHelp(sender, "1");
 			}
 			// Return true.
 			return true;
 		}
 		
 		// If the setdefault argument was given and the player has permission...
 		else if (args[0].equals("setdefault") && sender.hasPermission("adminsword.setdefault"))
 		{
 			// Run the change default type function and return true.
 			changeDefaultTypeCommand(sender, cmd, commandLabel, args);
 			return true;
 		}
 		
 		// This plugin has been handed an argument that it can't handle.
 		else
 		{
			// Send the source an error message.
			sender.sendMessage(ChatColor.RED + "Unknown command! Type /adminsword help for commands.");
			
			// Return true since we handled error messages.
			return true;
 		}
 	}
 	
 	public boolean toggleCommand(CommandSender sender, Command cmd, String commandLabel, String[] args)
 	{
 		// Gather information about command sender...
 		// Check to see if player exists...
 		Player player = null;
 		if (sender instanceof Player)
 		{
 			player = (Player) sender;
 		}
 		
 		// Check to see if a target is specified...
 		Player target = null;
 		boolean targetGiven = false;
 		// If a target was specified...
 		if (args.length > 1)
 		{
 			// Check if the player has permission to give
 			// a target player an AdminSword.
 			// If they do not...
 			if (player != null && player.hasPermission("adminsword.toggle.other") == false)
 			{
 				// Send a permission denied message.
 				sender.sendMessage("You don't have permission to do this.");
 				// Return true since no error occurred.
 				return true;
 			}
 			else
 			{
 				// If the player has permission to give
 				// a target player an AdminSword, then continue...
 				// Check to see if the target player exists...
 				if(numberOfMatches(args[1]) > 1)
 				{
 					sender.sendMessage(ChatColor.RED + "Too many possible matches!");
 					return true;
 				}
 				target = findPlayer(args[1]);
 				// Set targetGiven to true.
 				targetGiven = true;
 			}
 		}
 		
 		// If a target was specified, but the player was not found... 
 		if (target == null && targetGiven == true)
 		{
 			// Send an error message to the sender.
 			sender.sendMessage(ChatColor.RED + "No player named: \"" + args[1] + "\"!");
 			// Return true since no error occurred.
 			return true;
 		}
 		
 		// If a target player was specified and found...
 		else if (target != null && targetGiven == true)
 		{
 			// Toggle adminsword for the target player.
 			boolean toggle = toggleAdminSword(target);
 			// If adminsword has been enabled by the command...
 			if (toggle == true)
 			{
 				// Send the enable messages and return true.
 				sender.sendMessage(ChatColor.GREEN + "AdminSword has been enabled for " + target.getName());
 				target.sendMessage(ChatColor.GREEN + "AdminSword is now active.");
 				return true;
 			}
 			// If adminsword has been disabled...
 			else
 			{
 				// Send the disable messages and return true.
 				sender.sendMessage(ChatColor.GREEN + "AdminSword has been disabled for " + target.getName());
 				target.sendMessage(ChatColor.RED + "AdminSword is no longer active.");
 				return true;
 			}
 		}
 		
 		// If no target is given, and the source player is in-game and not console...
 		else if (targetGiven == false && player != null)
 		{
 			boolean toggle = toggleAdminSword(player);
 			// If AdminSword has been enabled...
 			if (toggle == true)
 			{
 				// Send an enable message.
 				sender.sendMessage(ChatColor.GREEN + "AdminSword is now active.");
 			}
 			// If AdminSword has been disabled...
 			else
 			{
 				// Send a disable message.
 				sender.sendMessage(ChatColor.RED + "AdminSword is no longer active.");
 			}
 			// Return true since no error has occurred.
 			return true;
 		}
 		
 		// If no target is given, and command is executed from console...
 		else if (targetGiven == false && player == null)
 		{
 			// Send a notice to the console to let the sender
 			// know that they need to specify a target.
 			sender.sendMessage("Console must specify a target.");
 			return true;
 		}
 		// If none of the conditions are satisfied...
 		else
 		{
 			// Something must have gone wrong.
 			// Send debug text.
 			player.sendMessage(ChatColor.RED + "toggleCommand did nothing!");
 			return true;
 		}
 	}
 	
 	// Sets the player's event type.
 	public boolean setTypeCommand(CommandSender sender, Command cmd, String commandLabel, String[] args)
 	{
 		// If the user gave no additional arguments...
 		if (args.length == 1)
 		{
 			// Send them an error.
 			sender.sendMessage(ChatColor.RED + "You must specify a type!");
 		}
 		
 		// Gather information about command sender...
 		// Check to see if player exists...
 		Player player = null;
 		if (sender instanceof Player)
 		{
 			player = (Player) sender;
 		}
 		
 		// Check if a target was specified.
 		// Gather information about target.
 		Player target = null;
 		boolean targetGiven = false;
 		// If target is specified...
 		if (args.length > 2)
 		{
 			// Check if the player is allowed
 			// to run this command on targets.
 			// Bypass check if sender is console.
 			if (player != null && player.hasPermission("adminsword.toggle.other") == false)
 			{
 				// Send an error message.
 				player.sendMessage("You don't have permisison to do this.");
 				// Return true since we handled error messages.
 				return true;
 			}
 			if(numberOfMatches(args[2]) > 1)
 			{
 				sender.sendMessage(ChatColor.RED + "Too many possible matches!");
 				return true;
 			}
 			// Get the target player.
 			target = (findPlayer(args [2]));
 			// Set targetGiven to true.
 			targetGiven = true;
 		}
 		// If the target player is null and a target was given
 		// then the specified target was not found...
 		if (target == null && targetGiven == true)
 		{
 			// Send an error message and return true.
 			sender.sendMessage(ChatColor.RED + "Player: \"" + args[2] + "\" was not found!");
 			return true;
 		}
 		
 		// If no target was specified in the command
 		// and the source of the command was not console...
 		// Execute command on self.
 		if (targetGiven == false && player != null)
 		{
 			// Check to see which event type was specified.
 			if (args[0] == null)
 			{
 				return false;
 			}
 			
 			if (isValidType(args[1]))
 			{
 				setAdminSwordType(player, args[1], sender);
 			}
 			else
 			{
 				sender.sendMessage(ChatColor.RED + args[1] + " is not a valid type!");
 			}
 			return true;
 		}
 		
 		// If a target was specified in the command...
 		// Execute command on target.
 		else if (targetGiven == true)
 		{
 			if (isValidType(args[1]))
 			{
 				setAdminSwordType(target, args[1], sender);
 			}
 			else
 			{
 				sender.sendMessage(ChatColor.RED + args[1] + " is not a valid type!");
 			}
 			return true;
 		}
 		
 		// If the sender was console and they did not specify a target...
 		else if (player == null && targetGiven == false)
 		{
 			sender.sendMessage("Console must specify a target.");
 			return true;
 		}
 		
 		else
 		{
 			// Something must have gone wrong.
 			// Send error message.
 			player.sendMessage(ChatColor.RED + "setEventCommand did nothing!");
 			return true;
 		}
 	}
 
 	public boolean checkCommand(CommandSender sender, Command cmd, String commandLabel, String[] args)
 	{
 		// If the sender specified a target...
 		if (args.length > 1)
 		{
 			// If the sender has permission to check target players...
 			if (sender.hasPermission("adminsword.check.other"))
 			{
 				int matches = numberOfMatches(args[1]);
 				Player target = null;
 				if(matches > 1)
 				{
 					sender.sendMessage(ChatColor.RED + "Too many possible matches!");
 				}
 				else if(matches == 1)
 				{
 					target = findPlayer(args[1]);
 				}
 				else
 				{
 					sender.sendMessage(ChatColor.RED + "No player named: \"" + args[1] + "\"!");
 					return true;
 				}
 				if (checkAdminSword(target) == true)
 				{
 					sender.sendMessage(ChatColor.GREEN + "AdminSword is active for " + target.getName());
 					sender.sendMessage(ChatColor.GREEN + "AdminSword type: " + getAdminSwordType(target));
 					return true;
 				}
 				else
 				{
 					sender.sendMessage(ChatColor.RED + "AdminSword is not active for " + target.getName());
 					return true;
 				}
 			}
 			else
 			{
 				sender.sendMessage("You don't have permission to do this.");
 				return true;
 			}
 			
 		}
 		
 		// If no target was specified...
 		else
 		{
 			// Check to see if the user has permission to check themselves. (Before they wreck themselves.)
 			// If they do not have permission then the following code is run.
 			if (! sender.hasPermission("adminsword.check"))
 			{
 				sender.sendMessage("You don't have permission to do this.");
 				return true;
 			}
 			
 			// Check to see if the sender is console.
 			if (sender instanceof Player)
 			{
 				// Get the player.
 				Player player = (Player) sender;
 				// Check if they have AdminSword enabled.
 				if (checkAdminSword(player) == true)
 				{
 					player.sendMessage(ChatColor.GREEN + "AdminSword is active.");
 					player.sendMessage(ChatColor.GREEN + "AdminSword type: " + getAdminSwordType(player));
 					return true;
 				}
 				else
 				{
 					player.sendMessage(ChatColor.RED + "Adminsword is not active.");
 					return true;
 				}
 			}
 			else
 			{
 				sender.sendMessage("You must specify a target!");
 				sender.sendMessage("/adminsword check [target]");
 				return true;
 			}
 		}
 	}
 	
 	public boolean flyCommand(CommandSender sender, String[] args)
 	{
 		// Initialize variables.
 		Player target;
 		boolean flight;
 		boolean sIsP;
 		
 		// Check if sender is a player.
 		if (sender instanceof Player)
 		{
 			sIsP = true;
 		}
 		else
 		{
 			sIsP = false;
 		}
 		
 		// If two or more arguments are specified...
 		if (args.length > 1)
 		{
 			// If sender does not have permission and is not console...
 			if ((! sender.hasPermission("adminsword.fly.other")) && (sIsP == true))
 			{
 				sender.sendMessage("You don't have permission to do this.");
 				return true;
 			}
 			// If sender has permission...
 			else
 			{
 				// Set target as player specified.
 				target = (findPlayer(args[1]));
 				// If target was not found...
 				if (target == null)
 				{
 					// Send an error to the sender.
 					sender.sendMessage(ChatColor.RED + "Player: \"" + args[1] + "\" was not found!");
 					return true;
 				}
 				// If target was found...
 				else
 				{
 					flight = toggleFlight(target);
 					if (flight == true)
 					{
 						// Send enable messages.
 						sender.sendMessage(ChatColor.GREEN + "Flight enabled for: " + target.getName() + "!");
 						target.sendMessage(ChatColor.GREEN + "Flight enabled by: " + sender.getName() + "!");
 					}
 					else
 					{
 						// Send disable messages.
 						sender.sendMessage(ChatColor.GREEN + "Flight disabled for: " + target.getName() + "!");
 						target.sendMessage(ChatColor.RED + "Flight disabled by: " + sender.getName() + "!");
 					}
 					return true;
 				}
 			}
 		}
 		
 		// If no target was specified...
 		else
 		{
 			// If the player does not have permission to toggle flight...
 			if ((! sender.hasPermission("adminsword.fly")))
 			{
 				// Send an error.
 				sender.sendMessage("You don't have permission to do this.");
 				return true;
 			}
 			// If the player has permission...
 			else
 			{
 				// If the sender is a player...
 				if (sIsP == true)
 				{
 					// Redefine the target.
 					target = (Player) sender;
 					flight = toggleFlight(target);
 					if (flight == true)
 					{
 						target.sendMessage(ChatColor.GREEN + "Flight enabled!");
 					}
 					else
 					{
 						target.sendMessage(ChatColor.RED + "Flight disabled!");
 					}
 					return true;
 				}
 				// If the sender is not a player...
 				else
 				{
 					// Send an error message.
 					sender.sendMessage(ChatColor.RED + "You must specify a target!");
 					return true;
 				}
 			}
 		}
 	}
 	
 	public boolean killCommand(CommandSender sender, String[] args)
 	{
 	    // If the sender has insufficient permissions...
 	    if(! sender.hasPermission("adminsword.kill"))
 	    {
 		// Send an error message and quit.
 		sender.sendMessage("You do not have permission to do this.");
 		return true;
 	    }
 	    
 	    // Initialize target variable.
 	    Player target;
 	    
 	    // If a target was specified...
 	    if(args.length > 1)
 	    {
 	    	target = findPlayer(args[1]);
 			if(target == null)
 			{
 			    sender.sendMessage(ChatColor.RED + "No match for: " + args[1] + "!");
 			}
 			else
 			{
 			    // If the target is also an adminsword admin, and the source
 			    // does not have adminsword.kill.force, block the attack.
 			    if((target.hasPermission("adminsword.kill") || target.isOp()) &&
 				(! sender.hasPermission("adminsword.kill.force")))
 			    {
 				sender.sendMessage(ChatColor.RED + "You don't have permission to kill admins!");
 			    }
 			    // Kill the target.
 			    else
 			    {
 				// Deal enough damage to kill target from sender.
 				target.damage((target.getHealth() * 2));
 				sender.sendMessage(ChatColor.GREEN + target.getPlayerListName() + " has been killed!");
 				target.sendMessage(ChatColor.RED + "You have been killed by an admin!");
 			    }
 			}
 			return true;
 	    }
 	    else
 	    {
 	    	sender.sendMessage(ChatColor.RED + "You must specify a target!");
 	    	return true;
 	    }
 	}
 	
 	public void printHelp(CommandSender sender, String page)
 	{
 		if (page.equals("1") || page == null)
 		{
 			sender.sendMessage(ChatColor.GREEN + "AdminSword v" + pluginDescription.getVersion() + " by spy_1134");
 			sender.sendMessage(ChatColor.GREEN + "------------- HELP -------------");
 			sender.sendMessage(ChatColor.GREEN + "[] is optional, () is required");
 			sender.sendMessage(ChatColor.GREEN + "1.) Toggling AdminSword:");
 			sender.sendMessage(ChatColor.GREEN + "/adminsword toggle [target]");
 			sender.sendMessage(ChatColor.GREEN + "--------------------------------");
 			sender.sendMessage(ChatColor.GREEN + "2.) Changing Sword Type:");
 			sender.sendMessage(ChatColor.GREEN + "/adminsword type (type) [target]");
 			sender.sendMessage(ChatColor.GREEN + "--------------------------------");
 			sender.sendMessage(ChatColor.GREEN + "3.) Toggling Flight:");
 			sender.sendMessage(ChatColor.GREEN + "/adminsword flight [target]");
 			sender.sendMessage(ChatColor.GREEN + "--------------------------------");
 			sender.sendMessage(ChatColor.GREEN + "4.) Killing Players:");
 			sender.sendMessage(ChatColor.GREEN + "/adminsword kill [target]");
 			sender.sendMessage(ChatColor.GREEN + "--------------------------------");
 			sender.sendMessage(ChatColor.GREEN + "Sword Types: /adminsword types");
 			sender.sendMessage(ChatColor.GREEN + "Command Aliases: /adminsword help alias");
 			sender.sendMessage(ChatColor.RED + "Admin Commands: /adminsword help admin");
 		}
 		else if (page.equals("alias"))
 		{
 			sender.sendMessage(ChatColor.GREEN + "AdminSword v" + pluginDescription.getVersion() + " by spy_1134");
 			sender.sendMessage(ChatColor.GREEN + "------------- ALIASES -------------");
 			sender.sendMessage(ChatColor.GREEN + "1.) /adminsword: /as");
 			sender.sendMessage(ChatColor.GREEN + "toggle: t");
 			sender.sendMessage(ChatColor.GREEN + "type: ct");
 			sender.sendMessage(ChatColor.GREEN + "fly: f");
 			sender.sendMessage(ChatColor.GREEN + "check: c");
 			sender.sendMessage(ChatColor.GREEN + "help: h");
 		}
 		else if (page.equals("admin"))
 		{
 			sender.sendMessage(ChatColor.GREEN + "AdminSword v" + pluginDescription.getVersion() + " by spy_1134");
 			sender.sendMessage(ChatColor.RED + "------------- ADMIN -------------");
 			sender.sendMessage(ChatColor.RED + "1.) Change default sword (in configuration):");
 			sender.sendMessage(ChatColor.RED + "/adminsword setdefault (type)");
 			sender.sendMessage(ChatColor.GREEN + "Normal Help: /adminsword help");
 		}
 		else
 		{
 			sender.sendMessage(ChatColor.RED + "There is no help page named: \"" + page + "\"!");
 		}
 	}
 	
 	public void printTypes(CommandSender sender)
 	{
 		// Build the string to send to the user...
 		sender.sendMessage(ChatColor.GREEN + "AdminSword Types:");
 		String typesText = ChatColor.GREEN.toString();
 		for(int i=0; i < AdminSwordMain.swordTypes.length; i++)
 		{
 			// If this is the last entry in the array...
 			if(i == (AdminSwordMain.swordTypes.length - 1))
 			{
 				// Just output the type.
 				typesText = typesText.concat(AdminSwordMain.swordTypes[i]);
 			}
 			else
 			{
 				// If this is not the last entry...
 				// Add a comma and a space to the end of the string.
 				typesText = typesText.concat(AdminSwordMain.swordTypes[i] + ", ");
 			}
 		}
 		// Send the generated string.
 		sender.sendMessage(typesText);
 	}
 	
 	public boolean changeDefaultTypeCommand(CommandSender sender, Command cmd, String commandLabel, String[] args)
 	{
 		// If the sender is console or has permission to set the default type...
 		if (sender.hasPermission("adminsword.setdefault") || (! (sender instanceof Player)))
 		{
 			// Check to see if more than one argument was given.
 			if (args.length > 1)
 			{
 				// Set default type to second argument.
 				// If setDefaultType returned true...
 				if (setDefaultType(args[1]))
 				{
 					sender.sendMessage(ChatColor.GREEN + "Default type set to: " + args[1]);
 				}
 				// If setDefaultType returned false...
 				else
 				{
 					sender.sendMessage(ChatColor.RED + args[1] + " is not a valid type!");
 				}
 			}
 			else
 			{
 				sender.sendMessage(ChatColor.RED + "You must specify a type!");
 			}
 		}
 		return true;
 	}
 }
