 package com.comphenix.xp;
 
 /**
  *  ExperienceMod - Bukkit server plugin for modifying the experience system in Minecraft.
  *  Copyright (C) 2012 Kristian S. Stangeland
  *
  *  This program is free software; you can redistribute it and/or modify it under the terms of the 
  *  GNU General Public License as published by the Free Software Foundation; either version 2 of 
  *  the License, or (at your option) any later version.
  *
  *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
  *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
  *  See the GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License along with this program; 
  *  if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 
  *  02111-1307 USA
  */
 
 import java.io.File;
 import java.util.List;
 import java.util.logging.Logger;
 
 import net.milkbowl.vault.economy.Economy;
 
 import org.bukkit.Location;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.entity.Player;
 import org.bukkit.block.Block;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.RegisteredServiceProvider;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import com.comphenix.xp.lookup.Parsing;
 
 public class ExperienceMod extends JavaPlugin implements Debugger {
 	// Mod command(s)
 	private final String commandReload = "experiencemod";
 	private final String commandSpawnExp = "spawnexp";
 	private final String toggleDebug = "debug";
 	
 	// Constants
 	private final int spawnExpMaxDistance = 50;
 	
 	private Logger currentLogger;
 	private PluginManager manager;
 	private Economy economy;
 	
 	private ExperienceListener listener;
 	private Configuration configuration;
 	
 	private boolean debugEnabled;
 	
 	@Override
 	public void onEnable() {
 		manager = getServer().getPluginManager();
 		currentLogger = this.getLogger();
 		
 		// Load economy, if it exists
 		if (!hasEconomy())
 			setupEconomy();
 		
 		// Initialize configuration and listeners
 		loadDefaults();
 	}
 	
 	private void loadDefaults() {
 		FileConfiguration config = getConfig();
 		File path = new File(getDataFolder(), "config.yml");
 
 		// See if we need to create the file
 		if (!path.exists()) {
 			// Supply default values if empty
 			config.options().copyDefaults(true);
 			saveConfig();
 			currentLogger.info("Creating default configuration file.");
 		}
 		
 		// Load it
 		configuration = new Configuration(config, currentLogger);
 		setConfiguration(configuration);
 		
 		// Set reward type
 		switch (configuration.getRewardType()) {
 		case EXPERIENCE:
 			listener.setRewardManager(new RewardExperience());
			break;
 		case VIRTUAL:
 			listener.setRewardManager(new RewardVirtual());
			break;
 		case ECONOMY:
 			listener.setRewardManager(new RewardEconomy(economy, this));
			break;
 		default:
 			currentLogger.warning("Unknown reward manager.");
 			break;
 		}
 	}
 	
 	private void setupEconomy()
     {
         RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(Economy.class);
         
         if (economyProvider != null) {
             economy = economyProvider.getProvider();
         }
     }
 	
 	private boolean hasEconomy() {
 		return economy != null;
 	}
 	
 	private void setConfiguration(Configuration configuration) {
 		
 		// Create a new listener if necessary
 		if (listener == null) {
 			listener = new ExperienceListener(this, this, configuration);
 			manager.registerEvents(listener, this);
 		} else {
 			listener.setConfiguration(configuration);
 		}
 	}
 	
 	@Override
 	public void onDisable() {
 	}
 	
 	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args){
 		
 		String cmdName = cmd != null ? cmd.getName() : "";
 		
 		// Execute the correct command
 		if (cmdName.equalsIgnoreCase(commandReload))
 			return handleMainCommand(sender, args);
 		else if (cmdName.equalsIgnoreCase(commandSpawnExp))
 			return handleSpawnExp(sender, args);
 		else
 			return false;
     }
 
 	private boolean handleMainCommand(CommandSender sender, String[] args) {
 		
 		if (args.length > 0) {
 			
 			// Toggle debugging
 			if (args[0].equalsIgnoreCase(toggleDebug)) {
 				debugEnabled = !debugEnabled;
 				respond(sender, "Debug " + (debugEnabled ? " enabled " : " disabled"));
 				return true;
 			} else {
 				respond(sender, "Error: Unknown subcommand.");
 				return false; 
 			}
 
 		} else {
 			
 			loadDefaults();
     		listener.setConfiguration(configuration);
     		
     		respond(sender, "Reloaded ExperienceMod.");
     		return true;
 		}
 	}
 	
 	private boolean handleSpawnExp(CommandSender sender, String[] args) {
 
 		// We don't support console yet
 		if (sender == null || !(sender instanceof Player)) {
 			respond(sender, "This command can only be sent by a player");
 			return false;
 		}
 		
 		if (args.length == 1 && !Parsing.isNullOrIgnoreable(args[0])) {
 			
 			Integer experience = Integer.parseInt(args[0]);
 			Player player = (Player) sender;
 			
 			if (experience == null) {
 				respond(sender, "Error: Parameter must be a valid integer.");
 				return false;
 			}
 			
 			Block startBlock = player.getEyeLocation().getBlock();
 			List<Block> list = player.getLastTwoTargetBlocks(null, spawnExpMaxDistance);
 			
 			// Remember the start location
 			list.add(0, startBlock);
 			
 			// We want to spawn the experience at the surface of the block.
 			list.remove(list.size() - 1);
 			
 			if (list.size() > 0) {
 				Block target = list.get(list.size() - 1);
 				Location loc = target.getLocation();
 				
 				// Spawn experience at this location
 				printDebug(this, "Spawning %d experience at %b.", experience, loc);
 				Server.spawnExperienceAtBlock(target, experience);
 				return true;
 			}
 				
 
 		} else {
 			respond(sender, "Error: Incorrect number of parameters.");
 		}
 		
 		return false;
 	}
 
 	@Override	
 	public boolean isDebugEnabled() {
 		return debugEnabled;
 	}
 	
 	@Override
 	public void printDebug(Object sender, String message, Object... params) {
 		if (debugEnabled)
 			currentLogger.info(String.format("Debug: " + message, params));
 	}
 
 	private void respond(CommandSender sender, String message) {
 		if (sender == null) // Sent by the console
 			currentLogger.info(message);
 		else
 			sender.sendMessage(message);
 	}
 }
