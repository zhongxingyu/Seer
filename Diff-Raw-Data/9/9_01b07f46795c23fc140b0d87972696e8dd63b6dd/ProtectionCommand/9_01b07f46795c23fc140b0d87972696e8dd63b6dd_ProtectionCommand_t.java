 package com.wolvencraft.prison.mines.cmd;
 
 import java.util.List;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.entity.Player;
 import org.bukkit.material.MaterialData;
 
 import com.wolvencraft.prison.PrisonSuite;
 import com.wolvencraft.prison.hooks.WorldEditHook;
 import com.wolvencraft.prison.region.PrisonSelection;
 import com.wolvencraft.prison.mines.CommandManager;
 import com.wolvencraft.prison.mines.PrisonMine;
 import com.wolvencraft.prison.mines.mine.Mine;
 import com.wolvencraft.prison.mines.mine.Protection;
 import com.wolvencraft.prison.mines.settings.Language;
 import com.wolvencraft.prison.mines.util.Message;
 import com.wolvencraft.prison.mines.util.Util;
 
 public class ProtectionCommand  implements BaseCommand {
 	public boolean run(String[] args) {
 		
 		if(args.length == 1) {
 			getHelp();
 			return true;
 		}
 
 		Language language = PrisonMine.getLanguage();
 		Mine curMine = PrisonMine.getCurMine();
 		if(curMine == null) {
 			Message.sendError(language.ERROR_MINENOTSELECTED);
 			return false;
 		}
 		
 		if(args[1].equalsIgnoreCase("save")) {
 			Player player;
 			if(CommandManager.getSender() instanceof Player) {
 				player = (Player) CommandManager.getSender();
 			}
 			else {
 				Message.sendError("This command cannot be executed via console");
 				return false;
 			}
 			
 			if(args.length != 2) {
 				Message.sendError(language.ERROR_ARGUMENTS);
 				return false;
 			}
 			
 			PrisonSelection sel = PrisonSuite.getSelection(player);
 			if(!sel.locationsSet()) {
				if(!PrisonSuite.usingWorldEdit()) {
 					Message.sendError("Make a selection first");
 					return false;
 				}
 				else {
 					Location[] loc = WorldEditHook.getPoints(player);
 					if(loc == null) {
 						Message.sendError("Make a selection first");
 						return false;
 					}
 					sel.setCoordinates(loc);
 				}
 			}
 			
 			if(!sel.getPos1().getWorld().equals(sel.getPos2().getWorld())) {
 				Message.sendError("Your selection points are in different worlds");
 				return false;
 			}
 			
 			if(!sel.getPos1().getWorld().equals(curMine.getWorld())) {
 				Message.sendError("Mine and protection regions are in different worlds");
 				return false;
 			}
 			
 			curMine.getProtectionRegion().setCoordinates(sel);
 			Message.sendCustom(curMine.getId(), "Protection region has been set!");
 		}
 		else if(args[1].equalsIgnoreCase("pvp")) {
 			if(args.length != 2) {
 				Message.sendError(language.ERROR_ARGUMENTS);
 				return false;
 			}
 			
 			if(curMine.getProtection().contains(Protection.PVP)) {
 				curMine.getProtection().remove(Protection.PVP);
 				Message.sendCustom(curMine.getId(), "PVP protection has been turned " + ChatColor.RED + "off");
 			}
 			else {
 				curMine.getProtection().add(Protection.PVP);
 				Message.sendCustom(curMine.getId(), "PVP protection has been turned " + ChatColor.GREEN + "on");
 			}
 		}
 		else if(args[1].equalsIgnoreCase("breaking") || args[1].equalsIgnoreCase("break")) {
 			if(args.length < 3) {
 				Message.sendError("Invalid parameters. Check your argument count!");
 				return false;
 			}
 			
 			if(args[2].equalsIgnoreCase("toggle")) {
 
 				if(args.length != 3) {
 					Message.sendError(language.ERROR_ARGUMENTS);
 					return false;
 				}
 				
 				if(curMine.getProtection().contains(Protection.BLOCK_BREAK)) {
 					curMine.getProtection().remove(Protection.BLOCK_BREAK);
 					curMine.getBreakBlacklist().setEnabled(false);
 					Message.sendCustom(curMine.getId(), "Block breaking protection has been turned " + ChatColor.RED + "off");
 				}
 				else {
 					curMine.getProtection().add(Protection.BLOCK_BREAK);
 					curMine.getBreakBlacklist().setEnabled(true);
 					Message.sendCustom(curMine.getId(), "Block breaking protection has been turned " + ChatColor.GREEN + "on");
 				}
 			}
 			else if(args[2].equalsIgnoreCase("whitelist")) {
 				if(args.length != 3) {
 					Message.sendError(language.ERROR_ARGUMENTS);
 					return false;
 				}
 				if(curMine.getBreakBlacklist().getWhitelist()) {
 					curMine.getBreakBlacklist().setWhitelist(false);
 					Message.sendCustom(curMine.getId(), "Block breaking whitelist is now " + ChatColor.RED + "off");
 				}
 				else {
 					curMine.getBreakBlacklist().setWhitelist(true);
 					Message.sendCustom(curMine.getId(), "Block breaking whitelist is now " + ChatColor.GREEN + "on");
 				}
 			}
 			else if(args[2].equalsIgnoreCase("add") || args[2].equalsIgnoreCase("+")) {
 				if(args.length != 4) {
 					Message.sendError(language.ERROR_ARGUMENTS);
 					return false;
 				}
 				MaterialData block = Util.getBlock(args[3]);
 				
 				if(block == null) {
 					Message.sendError(language.ERROR_NOSUCHBLOCK);
 					return false;
 				}
 				
 				List<MaterialData> blockList = curMine.getBreakBlacklist().getBlocks();
 				blockList.add(block);
 				curMine.getBreakBlacklist().setBlocks(blockList);
 				
 				Message.sendCustom(curMine.getId(), ChatColor.GREEN + block.getItemType().toString().toLowerCase().replace("_", " ") + ChatColor.WHITE + " was added to the block breaking protection blacklist");
 			}
 			else if(args[2].equalsIgnoreCase("remove") || args[2].equalsIgnoreCase("-")) {
 				if(args.length != 4) {
 					Message.sendError(language.ERROR_ARGUMENTS);
 					return false;
 				}
 				MaterialData block = Util.getBlock(args[3]);
 				
 				if(block == null) {
 					Message.sendError(language.ERROR_NOSUCHBLOCK);
 					return false;
 				}
 				
 				List<MaterialData> blockList = curMine.getBreakBlacklist().getBlocks();
 				
 				if(blockList.indexOf(block) == -1) {
 					Message.sendError("There is no '" + args[3] + "' in break protection blacklist of mine '" + curMine.getId() + "'");
 					return false;
 				}
 				blockList.remove(block);
 				curMine.getBreakBlacklist().setBlocks(blockList);
 
 				Message.sendCustom(curMine.getId(), ChatColor.RED + block.getItemType().toString().toLowerCase().replace("_", " ") + ChatColor.WHITE + " was removed from the block breaking protection blacklist");
 			}
 			else
 			{
 				Message.sendError(language.ERROR_ARGUMENTS);
 				return false;
 			}
 		}
 		else if(args[1].equalsIgnoreCase("placement") || args[1].equalsIgnoreCase("place")) {
 			if(args.length < 3) {
 				Message.sendError("Invalid parameters. Check your argument count!");
 				return false;
 			}
 			
 			if(args[2].equalsIgnoreCase("toggle")) {
 
 				if(args.length != 3) {
 					Message.sendError(language.ERROR_ARGUMENTS);
 					return false;
 				}
 				
 				if(curMine.getProtection().contains(Protection.BLOCK_PLACE)) {
 					curMine.getProtection().remove(Protection.BLOCK_PLACE);
 					curMine.getPlaceBlacklist().setEnabled(false);
 					Message.sendCustom(curMine.getId(), "Block placement protection has been turned " + ChatColor.RED + "off");
 				}
 				else {
 					curMine.getProtection().add(Protection.BLOCK_PLACE);
 					curMine.getPlaceBlacklist().setEnabled(true);
 					Message.sendCustom(curMine.getId(), "Block placement protection has been turned " + ChatColor.GREEN + "on");
 				}
 			}
 			else if(args[2].equalsIgnoreCase("whitelist")) {
 				if(args.length != 3) {
 					Message.sendError(language.ERROR_ARGUMENTS);
 					return false;
 				}
 				if(curMine.getPlaceBlacklist().getWhitelist()) {
 					curMine.getPlaceBlacklist().setWhitelist(false);
 					Message.sendCustom(curMine.getId(), "Block placement whitelist is now " + ChatColor.RED + "off");
 				}
 				else {
 					curMine.getPlaceBlacklist().setWhitelist(true);
 					Message.sendCustom(curMine.getId(), "Block placement whitelist is now " + ChatColor.GREEN + "on");
 				}
 			}
 			else if(args[2].equalsIgnoreCase("add") || args[2].equalsIgnoreCase("+")) {
 				if(args.length != 4) {
 					Message.sendError(language.ERROR_ARGUMENTS);
 					return false;
 				}
 				MaterialData block = Util.getBlock(args[3]);
 				
 				if(block == null) {
 					Message.sendError(language.ERROR_NOSUCHBLOCK);
 					return false;
 				}
 				
 				List<MaterialData> blockList = curMine.getPlaceBlacklist().getBlocks();
 				blockList.add(block);
 				curMine.getPlaceBlacklist().setBlocks(blockList);
 				
 				Message.sendCustom(curMine.getId(), ChatColor.GREEN + block.getItemType().toString().toLowerCase().replace("_", " ") + ChatColor.WHITE + " was added to the block placement protection blacklist");
 			}
 			else if(args[2].equalsIgnoreCase("remove") || args[2].equalsIgnoreCase("-")) {
 				if(args.length != 4) {
 					Message.sendError(language.ERROR_ARGUMENTS);
 					return false;
 				}
 				MaterialData block = Util.getBlock(args[3]);
 				
 				if(block == null) {
 					Message.sendError(language.ERROR_NOSUCHBLOCK);
 					return false;
 				}
 				
 				List<MaterialData> blockList = curMine.getPlaceBlacklist().getBlocks();
 				
 				if(blockList.indexOf(block) == -1) {
 					Message.sendError("There is no '" + args[3] + "' in place protection blacklist of mine '" + curMine.getId() + "'");
 					return false;
 				}
 				blockList.remove(block);
 				curMine.getPlaceBlacklist().setBlocks(blockList);
 
 				Message.sendCustom(curMine.getId(), ChatColor.RED + block.getItemType().toString().toLowerCase().replace("_", " ") + ChatColor.WHITE + " was removed from the block placement protection blacklist");
 			}
 			else
 			{
 				Message.sendError(language.ERROR_ARGUMENTS);
 				return false;
 			}
 		}
 		else
 		{
 			Message.sendError(language.ERROR_COMMAND);
 			return false;
 		}
 		
 		return curMine.saveFile();
 	}
 	
 	public void getHelp() {
 		Message.formatHeader(20, "Protection");
 		Message.formatHelp("prot", "pvp", "Toggles the PVP for the current mine");
 		Message.send(ChatColor.RED + " Block Breaking Protection:");
 		Message.formatHelp("prot", "break toggle", "Toggles the protection");
 		Message.formatHelp("prot", "break whitelist", "Toggles whitelist mode");
 		Message.formatHelp("prot", "break + <block>", "Add <block> to the blacklist");
 		Message.formatHelp("prot", "break - <block>", "Remove <block> from theblacklist");
 		Message.send(ChatColor.RED + " Block Placement Protection:");
 		Message.formatHelp("prot", "place toggle", "Toggles the protection");
 		Message.formatHelp("prot", "place whitelist", "Toggles whitelist mode");
 		Message.formatHelp("prot", "place + <block>", "Add <block> to the blacklist");
 		Message.formatHelp("prot", "place - <block>", "Remove <block> from theblacklist");
 		return;
 	}
 	
 	public void getHelpLine() { Message.formatHelp("prot", "", "Shows the help page for mine protection options", "prison.mine.edit"); }
 }
