 package au.com.addstar.truehardcore;
 /*
 * TrueHardcore
 * Copyright (C) 2013 add5tar <copyright at addstar dot com dot au>
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */
 
 import java.util.Set;
 
 import org.bukkit.World;
 import org.bukkit.configuration.file.FileConfiguration;
 
 public class ConfigManager {
 
 	private TrueHardcore plugin;
 	public ConfigManager(TrueHardcore instance) {
 		plugin = instance;
 	}
 	
 	public FileConfiguration Config() {
 		return plugin.getConfig();
 	}
 	
 	public void LoadConfig(FileConfiguration config) {
 		config.options().copyDefaults(true);
 
 		plugin.DebugEnabled = Config().getBoolean("debug");
 
 		// Get the list of worlds
 		Set<String> worlds = (Set<String>) Config().getConfigurationSection("worlds").getKeys(false);
 
 		// Load each world's settings
 		plugin.Debug("Loading worlds...");
 		if (worlds != null) {
 			for (String w : worlds) {
 				plugin.Debug("Found World: " + w);
 				World world = plugin.getServer().getWorld(w);
 				if (world != null) {
 					HardcoreWorld hcw = new HardcoreWorld();
 					hcw.setWorld(world);
 					hcw.setGreeting(Config().getString("worlds." + w + ".greeting"));
 					hcw.setBantime(Config().getInt("worlds." + w + ".ban-time", 43200));				// Default = 12h
 					hcw.setSpawnDistance(Config().getInt("worlds." + w + ".spawn-distance", 5000));		// Default = 5000
 					hcw.setSpawnProtection(Config().getInt("worlds." + w + ".protection-time", 60));	// Default = 5000
					hcw.setExitPos(Util.Str2Loc(null, Config().getString("worlds." + w + ".exitpos")));	// Default = null
 					plugin.HardcoreWorlds.put(world.getName(), hcw);
 				}
 			}
 		} else {
 			plugin.Warn("No worlds configured! Things will not work!");
 		}
 
 		// Database settings
 		plugin.DBHost = Config().getString("mysql.host", "localhost");
 		plugin.DBPort = Config().getString("mysql.port", "3306");
 		plugin.DBName = Config().getString("mysql.database", "truehardcore");
 		plugin.DBUser = Config().getString("mysql.username", "truehardcore");
 		plugin.DBPass = Config().getString("mysql.password", "truehardcore");
 	}
 }
