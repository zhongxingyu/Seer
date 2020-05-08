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
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.util.MissingResourceException;
 import java.util.logging.Logger;
 
 import net.milkbowl.vault.chat.Chat;
 import net.milkbowl.vault.economy.Economy;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.RegisteredServiceProvider;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import com.comphenix.xp.Configuration.RewardTypes;
 import com.comphenix.xp.commands.CommandExperienceMod;
 import com.comphenix.xp.commands.CommandSpawnExp;
 
 public class ExperienceMod extends JavaPlugin implements Debugger {
 	
 	private static final int DEFAULT_BUFFER_SIZE = 1024 * 4;
 
 	private final String permissionInfo = "experiencemod.info";
 	
 	private final String commandReload = "experiencemod";
 	private final String commandSpawnExp = "spawnexp";
 	
 	private Logger currentLogger;
 	private PluginManager manager;
 	
 	private Economy economy;
 	private Chat chat;
 	
 	private ExperienceListener listener;
 	private ExperienceInformer informer;
 	private Presets presets;
 	
 	// Commands
 	private CommandExperienceMod commandExperienceMod;
 	private CommandSpawnExp commandSpawn;
 	
 	private boolean debugEnabled;
 	
 	@Override
 	public void onEnable() {
 		manager = getServer().getPluginManager();
 		
 		currentLogger = this.getLogger();
 		informer = new ExperienceInformer();
 		
 		commandExperienceMod = new CommandExperienceMod(this);
 		commandSpawn = new CommandSpawnExp(this);
 		
 		// Load economy, if it exists
 		if (!hasEconomy())
 			setupEconomy();
 		if (!hasChat())
 			setupChat();
 		
 		try {
 			// Initialize configuration
 			loadDefaults(false);
 			
 			// Register listeners
 			manager.registerEvents(listener, this);
 			manager.registerEvents(informer, this);
 		
 		} catch (IOException e) {
 			currentLogger.severe("IO error when loading configurations: " + e.getMessage());
 		}
 		
 		// Register commands
 		getCommand(commandReload).setExecutor(commandExperienceMod);
 		getCommand(commandSpawnExp).setExecutor(commandSpawn);
 	}
 	
 	public YamlConfiguration loadConfig(String name, String createMessage) throws IOException {
 		
 		File savedFile = new File(getDataFolder(), name);
		File directory = savedFile.getParentFile();
 		
 		// Reload the saved configuration
 		if (!savedFile.exists()) {
 
 			// Get the default file
 			InputStream input = ExperienceMod.class.getResourceAsStream("/" + name);
 			
 			// Make sure the directory exists 
			if (!directory.exists()) {
				directory.mkdirs();
				
				if (!directory.exists())
					throw new IOException("Could not create the directory " + directory.getAbsolutePath());
 			}
 			
 			OutputStream output = new FileOutputStream(savedFile);
 			
 			// Check just in case
 			if (input == null) {
 				throw new MissingResourceException(
 						"Cannot find built in resource file.", "ExperienceMod", name);
 			}
 
 			copyLarge(input, output);
 		}
 		
 		// Retrieve the saved file
 		return YamlConfiguration.loadConfiguration(savedFile);
 	}
 	
 	public void loadDefaults(boolean reload) throws IOException {
 		
 		// Read from disk again
 		if (reload || presets == null) {
 			
 			// Reset warnings
 			informer.clearMessages();
 			
 			// Load parts of the configuration
 			YamlConfiguration presetList = loadConfig("presets.yml", "Creating default preset list.");
 			loadConfig("config.yml", "Creating default configuration.");
 			
 			// Load it
 			presets = new Presets(presetList, this, chat, getDataFolder());
 			setPresets(presets);
 			
 			// Check for problems
 			for (Configuration config : presets.getConfigurations()) {
 				
 				RewardTypes reward = config.getRewardType();
 				
 				// See if we actually can enable the economy
 				if (economy == null && reward == RewardTypes.ECONOMY) {
 					printWarning(this, "Cannot enable economy. VAULT plugin was not found.");
 					config.setRewardType(reward = RewardTypes.EXPERIENCE);
 				}
 				
 				// Set reward type
 				switch (reward) {
 				case EXPERIENCE:
 					config.setRewardManager(new RewardExperience());
 					break;
 				case VIRTUAL:
 					config.setRewardManager(new RewardVirtual());
 					break;
 				case ECONOMY:
 					config.setRewardManager(new RewardEconomy(economy, this));
 					break;
 				}
 			}
 		}
 	}
 	
 	private void setupEconomy()
     {
 		try {
 	        RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(Economy.class);
 	        
 	        if (economyProvider != null) {
 	            economy = economyProvider.getProvider();
 	        }
         
 		} catch (NoClassDefFoundError e) {
 			// No vault
 			return;
 		}
     }
 	
 	private void setupChat()
     {
 		try {
 	        RegisteredServiceProvider<Chat> chatProvider = getServer().getServicesManager().getRegistration(Chat.class);
 	        
 	        if (chatProvider != null) {
 	            chat = chatProvider.getProvider();
 	        }
 
 		} catch (NoClassDefFoundError e) {
 			// No vault
 			return;
 		}
     }
 	
 	private boolean hasEconomy() {
 		return economy != null;
 	}
 	
 	private boolean hasChat() {
 		return chat != null;
 	}
 	
 	private void setPresets(Presets presets) {
 		
 		// Create a new listener if necessary
 		if (listener == null) {
 			listener = new ExperienceListener(this, this, presets);
 		} else {
 			listener.setPresets(presets);
 		}
 	}
 	
 	@Override
 	public void onDisable() {
 	}
 	
 	public boolean hasCommandPermission(CommandSender sender, String permission) {
 		
 		// Make sure the sender has permissions
 		if (sender != null && !sender.hasPermission(permission)) {
 			return false;
 		} else {
 			// We have permission
 			return true;
 		}
 	}
 
 	@Override	
 	public boolean isDebugEnabled() {
 		return debugEnabled;
 	}
 	
 	public void toggleDebug() {
 		debugEnabled = !debugEnabled;
 	}
 	
 	public ExperienceInformer getInformer() {
 		return informer;
 	}
 	
 	public Presets getPresets() {
 		return presets;
 	}
 	
 	@Override
 	public void printDebug(Object sender, String message, Object... params) {
 		if (debugEnabled) {
 			
 			String formattedMessage = String.format("[ExperienceMod] " + message, params);
 			
 			// Every player with the info permission will also see this message
 			getServer().broadcast(formattedMessage, permissionInfo);
 		}
 	}
 
 	public void respond(CommandSender sender, String message) {
 		if (sender == null) // Sent by the console
 			currentLogger.info(message);
 		else
 			sender.sendMessage(message);
 	}
 
 	@Override
 	public void printWarning(Object sender, String message, Object... params) {
 		String warningMessage = ChatColor.RED + "Warning: " + message;
 		
 		// Print immediately
 		currentLogger.warning(String.format(warningMessage, params));
 		
 		// Add to list of warnings
 	    informer.addWarningMessage(String.format(message, params));
 	}
 	
 	
 	// Taken from Apache Commons-IO
 	private static long copyLarge(InputStream input, OutputStream output) throws IOException {
 		byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
 		long count = 0;
 		int n = 0;
 		
 		while (-1 != (n = input.read(buffer))) {
 			output.write(buffer, 0, n);
 			count += n;
 		}
 		return count;
 	}
 }
