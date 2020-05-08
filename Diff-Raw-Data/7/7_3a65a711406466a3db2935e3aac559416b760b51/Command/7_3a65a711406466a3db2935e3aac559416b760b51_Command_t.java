 /* Copyright 2013 Kevin Seiden. All rights reserved.
 
  This works is licensed under the Creative Commons Attribution-NonCommercial 3.0
 
  You are Free to:
     to Share: to copy, distribute and transmit the work
     to Remix: to adapt the work
 
  Under the following conditions:
     Attribution: You must attribute the work in the manner specified by the author (but not in any way that suggests that they endorse you or your use of the work).
     Non-commercial: You may not use this work for commercial purposes.
 
  With the understanding that:
     Waiver: Any of the above conditions can be waived if you get permission from the copyright holder.
     Public Domain: Where the work or any of its elements is in the public domain under applicable law, that status is in no way affected by the license.
     Other Rights: In no way are any of the following rights affected by the license:
         Your fair dealing or fair use rights, or other applicable copyright exceptions and limitations;
         The author's moral rights;
         Rights other persons may have either in the work itself or in how the work is used, such as publicity or privacy rights.
 
  Notice: For any reuse or distribution, you must make clear to others the license terms of this work. The best way to do this is with a link to this web page.
  http://creativecommons.org/licenses/by-nc/3.0/
  */
 
 package io.github.alshain01.Flags.commands;
 
 import io.github.alshain01.Flags.Flag;
 import io.github.alshain01.Flags.Flags;
 import io.github.alshain01.Flags.Message;
 import io.github.alshain01.Flags.System;
 import io.github.alshain01.Flags.area.Area;
 import io.github.alshain01.Flags.economy.EPurchaseType;
 
 import java.util.Arrays;
 import java.util.HashSet;
 import java.util.Set;
 
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 /**
  * Command handler for Flags
  * 
  * @author Alshain01
  */
 public final class Command {
 	private Command(){}
 	
 	/**
 	 * Executes the flag command, returning its success 
 	 * 
 	 * @param sender Source of the command
 	 * @param args   Passed command arguments
 	 * @return		 true if a valid command, otherwise false
 	 */
 	public static boolean onFlagCommand(CommandSender sender, String[] args) {
 		if (args.length < 1) {
             if(sender instanceof Player) {
                 sender.sendMessage(getFlagUsage((Player)sender, System.getActive().getAreaAt(((Player)sender).getLocation())));
                 return true;
             }
             return false;
         }
 		
 		final EFlagCommand command = EFlagCommand.get(args[0]);
 		if(command == null) {
             if(sender instanceof Player) {
                 sender.sendMessage(getFlagUsage((Player)sender, System.getActive().getAreaAt(((Player)sender).getLocation())));
                 return true;
             }
             return false;
         }
 		
 		ECommandLocation location = null;
 		boolean success = false;
 		Flag flag = null;
 		Set<String> players = new HashSet<String>();
 
 		// Check argument length (-1 means infinite optional args)
 		if(args.length < command.requiredArgs
 				|| (command.optionalArgs > 0 && args.length > command.requiredArgs + command.optionalArgs)) {
 			Flags.debug("Command Argument Count Error");
 			sender.sendMessage(command.getHelp());
 			return true;
 		}
 
 		// Check the command location for those that apply
 		if(command.requiresLocation) {
 			location = ECommandLocation.get(args[1]);
 			if(location == null) {
 				Flags.debug("Command Location Error");
 				sender.sendMessage(command.getHelp());
 				return true;
 			}
 			
 			// Make sure we can set flags at that location
 			if (System.getActive() == System.WORLD && (location == ECommandLocation.AREA || location == ECommandLocation.DEFAULT)) {
 				sender.sendMessage(Message.NoSystemError.get());
 				return true;
 			}
 		}
 		
 		// Location based commands require the player to be in the world
 		// Inherit is a special case, doesn't require a location but assumes one exists
 		if((location != null || command == EFlagCommand.INHERIT) && !(sender instanceof Player)) {
 			sender.sendMessage(Message.NoConsoleError.get());
 			return true;
 		}
 
 		// Get the flag if required.
 		if(command.requiresFlag != null) {
 			if(command.requiresFlag || args.length >= 3) {
 				flag = Flags.getRegistrar().getFlagIgnoreCase(args[2]);
 				if (!Validate.isFlag(sender, flag, args[2])) { return true; }
 			}
 		}
 
 		// Process the command
 		switch(command) {
 			case HELP:
 				FlagCmd.help(sender, getPage(args), getGroup(args));
                 success = true;
 				break;
 			case INHERIT:
 				FlagCmd.inherit((Player)sender, getValue(args, 1));
                 success = true;
 				break;
 			case GET:
 				success = FlagCmd.get((Player)sender, location, flag);
 				break;
 			case SET:
 				FlagCmd.set((Player)sender, location, flag, getValue(args, 3));
                 success = true;
 				break;
 			case REMOVE:
 				FlagCmd.remove((Player)sender, location, flag);
                 success = true;
 				break;
 			case VIEWTRUST:
 				FlagCmd.viewTrust((Player)sender, location, flag);
                 success = true;
 				break;
 			case TRUST:
 				players = getPlayers(args, command.requiredArgs - 1);
 				success = FlagCmd.trust((Player)sender, location, flag, players);
 				break;
 			case DISTRUST:
 				if(args.length > command.requiredArgs) {
                     Flags.debug("Players found in command.");
                     players = getPlayers(args, command.requiredArgs); } // Players can be omitted to distrust all
 				FlagCmd.distrust((Player)sender, location, flag, players);
                 success = true;
 				break;
 			case PRESENTMESSAGE:
 				success = FlagCmd.presentMessage((Player)sender, location, flag);
 				break;
 			case MESSAGE:
 		  		// Build the message from the remaining arguments
 				StringBuilder message = new StringBuilder();
 				for (int x = 3; x < args.length; x++) {
 					message.append(args[x]);
 					if (x < args.length - 1) {	message.append(" "); }
 				}
 				
 				FlagCmd.message((Player)sender, location, flag, message.toString());
                 success = true;
 				break;
 			case ERASEMESSAGE:
 				FlagCmd.erase((Player)sender, location, flag);
                 success = true;
 				break;
 			case CHARGE:
 				final EPurchaseType t = EPurchaseType.get(args[1]);
                 if (t != null && args.length > 3) {
                     FlagCmd.setPrice(sender, t, flag, args[3]);
                 } else {
                     FlagCmd.getPrice(sender, t, flag);
                 }
                 success = true;
 				break;
 		}
 		
 		if(!success) { 
 			Flags.debug("Command Unsuccessful");
 			sender.sendMessage(command.getHelp());
 		}
 		return true;
 	}
 	
 	/**
 	 * Executes the bundle command, returning its success 
 	 * 
 	 * @param sender Source of the command
 	 * @param args   Passed command arguments
 	 * @return		 true if a valid command, otherwise false
 	 */
 	public static boolean onBundleCommand(CommandSender sender, String[] args) {
 		if (args.length < 1) {
             if(sender instanceof Player) {
                 sender.sendMessage(getBundleUsage((Player) sender, System.getActive().getAreaAt(((Player) sender).getLocation())));
                 return true;
             }
             return false;
         }
 		
 		final EBundleCommand command = EBundleCommand.get(args[0]);
 		if(command == null) {
             if(sender instanceof Player) {
                 sender.sendMessage(getBundleUsage((Player) sender, System.getActive().getAreaAt(((Player) sender).getLocation())));
                 return true;
             }
             return false;
         }
 		
 		ECommandLocation location = null;
 		String bundle = null;
 
 		// Check argument length (-1 means infinite optional args)
 		if(args.length < command.requiredArgs
 				|| (command.optionalArgs > 0 && args.length > command.requiredArgs + command.optionalArgs)) { 
 			Flags.debug("Command Argument Count Error");
 			sender.sendMessage(command.getHelp());
 			return true;
 		}
 
 		// Check the command location for those that apply
 		if(command.requiresLocation) {
 			location = ECommandLocation.get(args[1]);
 			if(location == null) {
 				Flags.debug("Command Location Error");
 				sender.sendMessage(command.getHelp());
 				return true;
 			}
 			
 			// Location based commands require the player to be in the world
 			if(!(sender instanceof Player)) {
 				sender.sendMessage(Message.NoConsoleError.get());
 				return true;
 			}
 			
 			// Make sure we can set flags at that location
 			if (System.getActive() == System.WORLD && (location == ECommandLocation.AREA || location == ECommandLocation.DEFAULT)) {
 				sender.sendMessage(Message.NoSystemError.get());
 				return true;
 			}
 		}
 		
 		if(command.requiresBundle != null) {
 			if(command.requiresBundle || args.length >= 3) {
 				bundle = (command.requiresLocation) ? args[2] : args[1];
 			}
 		}
 		
 		// Process the command
 		
 		switch(command) {
 			case HELP:
 				BundleCmd.help(sender, getPage(args));
 				break;
 			case GET:
 				BundleCmd.get((Player)sender, location, bundle);
 				break;
 			case SET:
 				Boolean value = getValue(args, 3);
 				if(value != null) { BundleCmd.set((Player)sender, location, bundle, getValue(args, 3)); }
 				break;
 			case REMOVE:
 				BundleCmd.remove((Player)sender, location, bundle);
 				break;
 			case ADD:
 				BundleCmd.add(sender, bundle, new HashSet<String>(Arrays.asList(args).subList(2, args.length)));
 				break;
 			case DELETE:
 				BundleCmd.delete(sender, bundle, new HashSet<String>(Arrays.asList(args).subList(2, args.length)));
 				break;
 			case ERASE:
 				BundleCmd.erase(sender, bundle);
 				break;
 		}
 		return true;
 	}
 	
 	/**
 	 * Returns a list of players starting with argument 4
 	 * 
 	 * @param args Command arguments
 	 * @return A list of players
 	 */
 	private static Set<String> getPlayers(String[] args, int start) {
         Flags.debug((new HashSet<String>(Arrays.asList(args).subList(start, args.length)).toString()));
 		return new HashSet<String>(Arrays.asList(args).subList(start, args.length));
 	}
 	
 	/**
 	 * Returns a page number from argument 2 or 3
 	 * 
 	 * @param args Command arguments
 	 * @return The page number.
 	 */
 	private static int getPage(String[] args) {
 		if(args.length < 2) { return 1; }
 		
 		String page;
 		if(args.length >= 3) { page = args[2]; }
 		else { page = args[1]; }
 		
 		// Either group or page was omitted, which one?
         try {
 	        return Integer.valueOf(page);
         } catch(Exception e){
         	// It was a string.
         	return 1;
 		}
 	}
 	
 	private static String getGroup(String[] args) {
 		if(args.length < 2) { return null; }
 		if(args.length >= 3) { return args[1]; }
 		
 		// Either group or page was omitted, which one?
 		try {
             //noinspection ResultOfMethodCallIgnored
             Integer.parseInt(args[1]);
 		} catch (Exception e) {
 			// It was a string.
 			return args[1];
 		}
 		// It was an integer
 		return null;
 	}
 	
 	/**
 	 * Returns a true, false, or null value from the argument
 	 * 
 	 * @param args The argument to check for a Boolean value
 	 * @return The Boolean value
 	 */
 	private static Boolean getValue(String[] args, int argument) {
 		if (args.length > argument) {
 			if(args[argument].toLowerCase().charAt(0) == 't') {
 				return true;
 			} else if (args[argument].toLowerCase().charAt(0) == 'f') {
 				return false;
 			}
 		}
 		return null;
 	}
 
     private static String getFlagUsage(Player player, Area area) {
         //usage: /flag <get|set|remove|trust|distrust|viewtrust|message|presentmessage|erasemessage|charge|inherit|help>
         // We can assume if we get this far, the player has read access to the command.
         StringBuilder usage = new StringBuilder("/flag <get");
         if(player.hasPermission("flags.command.flag.set") && area.hasPermission(player)) {
             usage.append("|set|remove|trust|distrust");
         }
         usage.append("|viewtrust"); //read access
 
         if(player.hasPermission("flags.command.flag.set") && area.hasPermission(player)) {
            usage.append("|message|erasemessage");
    }
         usage.append("|presentmessage");
 
         if(Flags.getEconomy() != null && player.hasPermission("flags.command.flag.charge")) {
             usage.append("|charge");
         }
 
         if(player.hasPermission("flags.command.flag.set") && area.hasPermission(player) && System.getActive().hasSubdivisions()) {
             usage.append("|inherit");
         }
 
         usage.append("|help>");
 
         return usage.toString();
     }
 
     private static String getBundleUsage(Player player, Area area) {
         //usage: /bundle <get|set|remove|add|delete|erase|help>
         // We can assume if we get this far, the player has read access to the command.
         StringBuilder usage = new StringBuilder("/bundle <get");
         if(player.hasPermission("flags.command.bundle.set") && area.hasBundlePermission(player)) {
             usage.append("|set|remove");
         }
 
         if(player.hasPermission("flags.command.bundle.edit")) {
             usage.append("|add|delete|erase");
         }

         usage.append("|help>");
 
         return usage.toString();
     }
 }
