 /*
  * Copyright (C) 2011 Massive Dynamics
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package com.massivedynamics.spawny;
 
 import com.massivedynamics.spawny.commands.SetSpawnCommand;
 import com.massivedynamics.spawny.commands.SpawnCommand;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import org.bukkit.Location;
 import org.bukkit.World;
 import org.bukkit.command.PluginCommand;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Event.Priority;
 import org.bukkit.event.Event.Type;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 
 /**
  * A plugin class that sets up this plugin
  * @author Cruz Bishop
  * @version 1.1.0.0
  */
 public class SpawnyPlugin extends JavaPlugin {
     
     /**
      * The instance of this class
      */
     private static SpawnyPlugin instance;
 
     /**
      * The logger for this class
      */
     private Logger logger = Logger.getLogger("SpawnyPlugin");
 
 	protected SpawnOrientation orientations;
 	protected PlayerSpawner playerListener;
 	
     /**
      * Gets the logger
      * @return The logger
      */
     public Logger getLogger() {
         return logger;
     }
 	
 	public SpawnOrientation getOrientations() { return orientations; }
 
     /**
      * Called when disabling Spawny
      */
     @Override
     public void onDisable() {
         logger.info("Stopping Spawny version " + this.getDescription().getVersion());
     }
 
     /**
      * Called when enabling Spawny
      */
     @Override
     public void onEnable() {
         if (instance == null) {
             instance = this;
         }
 		
 		orientations = new SpawnOrientation(this);
 		playerListener = new PlayerSpawner(this);
 
         logger.info("Starting Spawny version " + this.getDescription().getVersion());
         
 		
 		PluginCommand pc = this.getServer().getPluginCommand("spawn");
		if(pc.isRegistered()) {
 			logger.info("\"/spawn\" is already registered to " + pc.getPlugin().toString() + " (overriding commmand)");
 			pc.setExecutor(new SpawnCommand(this));
 		}
 		
         this.getCommand("spawn").setExecutor(new SpawnCommand(this));
 		
 		pc = this.getServer().getPluginCommand("setspawn");
		if(pc.isRegistered()) {
 			logger.info("\"/setspawn\" is already registered to " + pc.getPlugin().toString() + " (overriding commmand)");
 			pc.setExecutor(new SetSpawnCommand(this));
 		}
         this.getCommand("setspawn").setExecutor(new SetSpawnCommand(this));
 
 		PluginManager pm = this.getServer().getPluginManager();
 		pm.registerEvent(Type.PLAYER_RESPAWN, playerListener, Priority.Low, this);
 		pm.registerEvent(Type.PLAYER_JOIN, playerListener, Priority.Low, this);
 		
     }
 	
 	public static void spawn(Player p) {
 		if(p != null) {
 			Location l = p.getWorld().getSpawnLocation();
 			l.setYaw(instance.orientations.getDirection(p.getWorld().getName()));
 			p.teleport(l);
 		}
 	}
 	
 	public static void spawn(Player p, World w) {
 		if(p != null) {
 			Location l = w.getSpawnLocation();
 			l.setYaw(instance.orientations.getDirection(p.getWorld().getName()));
 			p.teleport(l);
 		}
 	}
 }
