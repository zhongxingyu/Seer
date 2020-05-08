 package com.wolvencraft.prison.mines.cmd;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.entity.Player;
 
 import com.wolvencraft.prison.PrisonSuite;
 import com.wolvencraft.prison.hooks.WorldEditHook;
 import com.wolvencraft.prison.region.PrisonRegion;
 import com.wolvencraft.prison.region.PrisonSelection;
 import com.wolvencraft.prison.mines.CommandManager;
 import com.wolvencraft.prison.mines.PrisonMine;
 import com.wolvencraft.prison.mines.mine.Mine;
 import com.wolvencraft.prison.mines.util.ExtensionLoader;
 import com.wolvencraft.prison.mines.util.Message;
 
 public class SaveCommand implements BaseCommand {
 	public boolean run(String[] args)
 	{
 		Message.debug("SaveCommand initiated");
 		Player player = (Player) CommandManager.getSender();
 		
 		if(args.length == 1) {
 			getHelp();
 			return true;
 		}
 		
 		if(args.length != 2 && args.length != 3) {
 			Message.sendError(PrisonMine.getLanguage().ERROR_ARGUMENTS);
 			return false;
 		}
 		
 		String generator;
 		if(args.length == 3) {
 			if(ExtensionLoader.get(args[2]) != null) {
 				generator = args[2].toUpperCase();
 			}
 			else {
 				Message.sendError("Invalid generator provided!");
 				return false;
 			}
 		}
 		else generator = "RANDOM";
 		
 		Message.debug("Passed argument count check");
 		
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
 		
 		Message.debug("Passed selection check");
 		
 		if(!sel.getPos1().getWorld().equals(sel.getPos2().getWorld())) {
 			Message.sendError("Your selection points are in different worlds");
 			return false;
 		}
 
 		if(Mine.get(args[1]) != null) {
 			Message.sendError("Mine '" + args[1] + "' already exists!");
 			return false;
 		}
 		
 		for(String bannedName : PrisonMine.getSettings().BANNEDNAMES) {
 			if(args[1].equalsIgnoreCase(bannedName)) {
 				Message.sendError("This name is not valid");
 				return false;
 			}
 		}
 
         Message.debug("Passed mine existance check");
 		
 		Mine newMine = new Mine(args[1], new PrisonRegion(sel), sel.getPos1().getWorld(), player.getLocation(), generator);
 		
 		if(!ExtensionLoader.get(generator).init(newMine)) return false;
 		
 		PrisonMine.addMine(newMine);
 		newMine.saveFile();
 
         Message.debug("Mine creation completed");
 		
 		PrisonMine.setCurMine(newMine);
 		Message.sendCustom(newMine.getId(), "Mine created successfully!");
 		return true;
 	}
 	
 	public void getHelp() {
 		Message.formatHeader(20, "Save");
 		Message.formatHelp("save", "<name> [generator]", "Saves the mine region");
 		Message.formatMessage("If no generator is specified, " + ChatColor.GOLD + "RANDOM" + ChatColor.WHITE + " will be used.");
 		Message.formatMessage("Available generators:");
 		Message.formatMessage(ExtensionLoader.list());
 		return;
 	}
 	
 	public void getHelpLine() { Message.formatHelp("save", "<name> [generator]", "Saves the mine region to file", "prison.mine.edit"); }
 }
