 package btwmod.itemlogger;
 
 import java.util.Arrays;
 import java.util.List;
 
 import btwmods.Util;
 import btwmods.commands.CommandBaseExtended;
 
 import net.minecraft.server.MinecraftServer;
 import net.minecraft.src.ICommandSender;
 import net.minecraft.src.Packet3Chat;
 import net.minecraft.src.WrongUsageException;
 
 public class CommandWatch extends CommandBaseExtended {
 
 	private final mod_ItemLogger mod;
 
 	public CommandWatch(mod_ItemLogger mod) {
 		this.mod = mod;
 	}
 
 	@Override
 	public String getCommandName() {
 		return "watch";
 	}
 
 	@Override
 	public void processCommand(ICommandSender sender, String[] args) {
 		if (isStringMatch(args, 0, "list")) {
 			List<String> names = Arrays.asList(mod.getWatchedPlayers());
 			if (names.size() == 0) {
 				sender.sendChatToPlayer(mod.getName() + " is not watching any players.");
 			}
 			else {
 				List<String> messages = Util.combineIntoMaxLengthMessages(names, Packet3Chat.maxChatLength, ", ", true);
 				for (String message : messages) {
 					sender.sendChatToPlayer(message);
 				}
 			}
 		}
 		else if (isStringMatch(args, 0, "add")) {
 			if (args.length != 2)
 				throw new WrongUsageException("/" + getCommandName() + " add <username>", new Object[0]);
 				
 			if (mod.addWatchedPlayer(args[1].toLowerCase().trim())) {
 				sender.sendChatToPlayer(mod.getName() + " is now watching player '" + args[1].toLowerCase().trim() + "'.");
 			}
 			else {
 				sender.sendChatToPlayer(mod.getName() + " is already watching player '" + args[1].toLowerCase().trim() + "'.");
 			}
 		}
 		else if (isStringMatch(args, 0, "remove")) {
 			if (args.length != 2)
 				throw new WrongUsageException("/" + getCommandName() + " remove <username>", new Object[0]);
 			
 			if (mod.removeWatchedPlayer(args[1].toLowerCase().trim())) {
 				sender.sendChatToPlayer(mod.getName() + " is no longer watching player '" + args[1].toLowerCase().trim() + "'.");
 			}
 			else {
 				sender.sendChatToPlayer(mod.getName() + " is not watching player '" + args[1].toLowerCase().trim() + "'.");
 			}
 		}
 		else {
 			throw new WrongUsageException(getCommandUsage(sender), new Object[0]);
 		}
 	}
 
 	@Override
 	public String getCommandUsage(ICommandSender sender) {
 		return "/" + getCommandName() + " (list | add | remove)";
 	}
 
 	@Override
 	public List addTabCompletionOptions(ICommandSender sender, String[] args) {
 		if (args.length == 1)
 			return getListOfStringsMatchingLastWord(args, new String[] { "list", "add", "remove" });
 		
 		else if (args.length == 2 && args[0].equalsIgnoreCase("add"))
			return getListOfStringsMatchingLastWord(args, MinecraftServer.getServer().getAllUsernames());
 		
 		else if (args.length == 2 && args[0].equalsIgnoreCase("remove"))
			return getListOfStringsMatchingLastWord(args, mod.getWatchedPlayers());
 			
 		return super.addTabCompletionOptions(sender, args);
 	}
 
 }
