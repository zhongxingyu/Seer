 /*  BeefyBlocks - A Bukkit plugin to beef-up blocks, making them last longer
  *  Copyright (C) 2011 Letat
  *  Copyright (C) 2011 Robert Sargant
  *
  *  This program is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  *
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with this program. If not, see <http://www.gnu.org/licenses/>.
 */    
 
 package org.chryson.bukkit.beefyblocks;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.logging.Logger;
 
 import javax.persistence.PersistenceException;
 
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Event.Priority;
 import org.bukkit.event.Event;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.plugin.PluginManager;
 import com.nijiko.permissions.PermissionHandler;
 import com.nijikokun.bukkit.Permissions.Permissions;
 import org.bukkit.plugin.Plugin;
 
 public class BeefyBlocks extends JavaPlugin {
     protected final Logger log;
     private final BeefyBlocksBlockListener blockListener;
     private final BeefyBlocksPlayerListener playerListener;
     public static PermissionHandler permissionHandler;
     // temporarily holds attached blocks for respawning after a block is broken
     protected Map attachedBlocks;
     // keeps track of the remaining 'lives' of blocks
     protected Map blockLives;
     // very temporarily stores the inventory of a container block just before it's broken up
     // until just after it is respawned
     protected Map inventories;
     // hold the break counts for each block type
     protected byte[] baseLives;
     protected byte[] placedLives;
     protected int cooldown; // in minutes
     
     public BeefyBlocks() {
         blockListener = new BeefyBlocksBlockListener(this);
         playerListener = new BeefyBlocksPlayerListener(this);
         
     	attachedBlocks = new ConcurrentHashMap();
         blockLives = new ConcurrentHashMap();
         inventories = new ConcurrentHashMap();
         
         // should be the size for all block materials
         baseLives = new byte[256];
         placedLives = new byte[256];
         
         log = Logger.getLogger("Minecraft");
     }
     
     private void setupPermissions() {
         Plugin permissionsPlugin = getServer().getPluginManager().getPlugin("Permissions");
 
         if (permissionHandler == null) {
             if (permissionsPlugin != null) {
                 permissionHandler = ((Permissions) permissionsPlugin).getHandler();
             } else {
                 log.info("[" + getDescription().getName() + "] Permissions not detected, defaulting to Op");
             }
         }
     }
     
     public static boolean usingPermissions() {
     	return (permissionHandler != null);
     }
     
     public static boolean hasPermission(Player p, String cmd) {
 		if (usingPermissions()) {
 		    if (permissionHandler.has(p, cmd))
 		        return true;
 		    return false;
 		} else if (p.isOp())
 		    return true;
 		return false;
     }
 	
     // TODO: Make sure the 'unable to install' part actually runs when expected
     private boolean setupDatabase() {
     	try {
     		getDatabase().find(PlayerSettings.class).findRowCount();
     		getDatabase().find(PlacedBlock.class).findRowCount();
     	} catch (PersistenceException ex) {
     		installDDL();
     		log.info("[" + getDescription().getName() + "] Installed database");
     	} catch (Exception ex) {
     		log.info("[" + getDescription().getName() + "] Unable to install database");
     		return false;
     	}
     	return true;
     }
 
     @Override
     public List<Class<?>> getDatabaseClasses() {
     	List<Class<?>> list = new ArrayList<Class<?>>();
     	list.add(PlayerSettings.class);
     	list.add(BlockLocation.class);
     	list.add(PlacedBlock.class);
     	return list;
     }
     
     public PlacedBlock getPlacedBlockAt(Location loc, boolean readOnly) {
     	return getDatabase()
     		.find(PlacedBlock.class)
     		.setReadOnly(readOnly)
     		.where()
     		.eq("world", loc.getWorld().getName())
     		.eq("x", loc.getBlockX())
     		.eq("y", loc.getBlockY())
     		.eq("z", loc.getBlockZ())
     		.findUnique();
     }
     
     public DisplayPreference getDisplayPref(Player p) {
     	PlayerSettings settings = getDatabase()
     		.find(PlayerSettings.class)
     		.where()
     		.eq("player_name", p.getName())
     		.findUnique();
     	if (settings != null)
     		return settings.getDisplay();
     	return DisplayPreference.ALL;
     }
     
     public PlayerSettings getPlayerSettings(String playerName, boolean readOnly) {
     	return getDatabase()
     		.find(PlayerSettings.class)
     		.setReadOnly(readOnly)
     		.where()
     		.eq("player_name", playerName)
     		.findUnique();
     }
     
     public void enableMessage() {
     	log.info(getDescription().getName() + " " + getDescription().getVersion() + " enabled.");
     }
     
     public void disableMessage() {
     	log.info(getDescription().getName() + " " +  getDescription().getVersion() + " disabled.");
     }
     
     public void onDisable() {
     	disableMessage();
     }
     
     public boolean setupConfig() {
     	String name = getDescription().getName();
         getDataFolder().mkdirs();
         File yml = new File(getDataFolder(), "config.yml");
         if (!yml.exists()) {
         	try {
         		yml.createNewFile();
         		log.info("[" + name + "] Created empty file: " + getDataFolder() + File.separator + "config.yml.");
         		log.info("[" + name + "] Please edit it to change block behavior.");
         		getConfiguration().setProperty("cooldown", 5);
         		getConfiguration().setProperty("blocks", null);
         		getConfiguration().save();
         	} catch (IOException ex){
         		log.severe("[" + name + "] Could not generate config.yml.");
         		return false;
         	}
         }
         return true;
     }
 
     public boolean loadConfig() {
     	String name = getDescription().getName();
         List<String> keys;
         try {
         	keys = getConfiguration().getKeys(null);
         } catch (NullPointerException e) {
         	log.warning("[" + name + "] Could not find a parent key.");
         	return false;
         }
         
         if (!keys.contains("cooldown")) {
         	log.warning("[" + name + "] Could not find 'cooldown' key, defaulting to 5 mins");
         	cooldown = 5;
         } else
         	cooldown = getConfiguration().getInt("cooldown", 5);
         
         if (!keys.contains("blocks")) {
         	log.warning("[" + name + "] Could not find 'blocks' key.");
         	return false;
         }
         
         keys.clear();
         keys = getConfiguration().getKeys("blocks");
         
         if (keys == null) {
         	log.warning("[" + name + "] Could not find any block entries.");
         	return false;
         } 
         
         for (int i = 0; i < 256; i++) {
         	baseLives[i] = 1;
         	placedLives[i] = 1;
         }
         
         for (String key : keys) {
         	if (key.equals("base")) {
         		List<String> blockKeys = getConfiguration().getKeys("blocks.base");
         		if (blockKeys != null) {
 	        		for (String blockKey : blockKeys) {
 	        			int numLives = getConfiguration().getInt("blocks.base." + blockKey, 1);
 	        			int block = Material.valueOf(blockKey).getId();
 	        			baseLives[block] = (byte)numLives;
 	        		}
         		}
         	} else if (key.equals("placed")) {
         		List<String> blockKeys = getConfiguration().getKeys("blocks.placed");
         		if (blockKeys != null) {
 	        		for (String blockKey : blockKeys) {
 	        			int numLives = getConfiguration().getInt("blocks.placed." + blockKey, 1);
 	        			int block = Material.valueOf(blockKey).getId();
 	        			placedLives[block] = (byte)numLives;
 	        		}
         		}
         	}
         }
         
         return true;
     }
     
     public void onEnable() {
     	if (!setupConfig()) {
     		disableMessage();
     		return;
     	}
         
     	if (!loadConfig()) {
     		disableMessage();
     		return;
     	}
 
         if (!setupDatabase()) {
         	disableMessage();
         	return;
         }
         
     	setupPermissions();
         
         PluginManager pm = getServer().getPluginManager();
         
         pm.registerEvent(Event.Type.BLOCK_PLACE, blockListener, Priority.Lowest, this);
         pm.registerEvent(Event.Type.BLOCK_BREAK, blockListener, Priority.Lowest, this);
        pm.registerEvent(Event.Type.BLOCK_BURN, blockListener, Priority.Lowest, this);
         pm.registerEvent(Event.Type.BLOCK_PHYSICS, blockListener, Priority.Lowest, this);
         pm.registerEvent(Event.Type.PLAYER_COMMAND_PREPROCESS, playerListener, Priority.Lowest, this);
 
         enableMessage();
     }
 }
 
