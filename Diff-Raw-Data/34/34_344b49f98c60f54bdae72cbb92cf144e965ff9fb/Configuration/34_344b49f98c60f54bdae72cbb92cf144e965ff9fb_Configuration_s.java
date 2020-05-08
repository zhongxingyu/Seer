 /*
  * Copyright (c) 2012-2013 Sean Porter <glitchkey@gmail.com>
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
 
 package org.enderspawn;
 
 //* IMPORTS: JDK/JRE
 	import java.io.File;
 	import java.sql.Timestamp;
 	import java.util.Date;
 	import java.util.HashMap;
 	import java.util.List;
 	import java.util.logging.Logger;
 	import java.util.Map;
 //* IMPORTS: BUKKIT
 	import org.bukkit.configuration.ConfigurationSection;
 	import org.bukkit.configuration.file.YamlConfiguration;
 	import org.bukkit.World;
 	import org.bukkit.World.Environment;
 //* IMPORTS: OTHER
 	//* NOT NEEDED
 
 public class Configuration extends YamlConfiguration {
 	private	File config;
 	private Logger log;
 	private EnderSpawn plugin;
 
 	public	Map<String, Integer>	worlds	= new HashMap<String, Integer>();
 	public	Map<String, Integer> 	xCoords	= new HashMap<String, Integer>();
 	public	Map<String, Integer> 	yCoords	= new HashMap<String, Integer>();
 	public	Map<String, Integer> 	zCoords	= new HashMap<String, Integer>();
 
 	public	boolean	destroyBlocks	= false;
 	public	boolean	spawnEgg	= true;
 	public	boolean	spawnPortal	= false;
 	public	boolean teleportEgg	= false;
 	public	boolean	useCustomExp	= false;
	public	boolean	dropExp		= true;
 	public	long	maxSpawnMinutes	= 5;
 	public	long	minSpawnMinutes	= 5;
 	public	long	expResetMinutes	= 1200;
 	public	long	expMaxDistance	= 75;
 	public	int	customExp	= 20000;
 
 	public Configuration(File config, Logger log, EnderSpawn plugin) {
 		this.config	= config;
 		this.log	= log;
 		this.plugin	= plugin;
 	}
 
 	public void load() {
 		boolean defaults = false;
 
 		try {
 			super.load(config);
 		}
 		catch (Exception e) {
 			log.warning("Unable to load configuration, using defaults instead.");
 			defaults = true;
 		}
 
 		if (contains("Configuration")) {
 			loadLegacy();
 			return;
 		}
 
 		destroyBlocks	= getBoolean("DestroyBlocks",		destroyBlocks);
 		spawnEgg	= getBoolean("SpawnEgg",		spawnEgg);
 		spawnPortal	= getBoolean("SpawnPortal",		spawnPortal);
 		teleportEgg	= getBoolean("EggsCanTeleport",		teleportEgg);
 		maxSpawnMinutes	= getLong("MaxRespawnMinutes",		maxSpawnMinutes);
 		minSpawnMinutes	= getLong("MinRespawnMinutes",		minSpawnMinutes);
 		expResetMinutes	= getLong("EXPResetMinutes",		expResetMinutes);
 		expMaxDistance	= getLong("EXPMaxDistance",		expMaxDistance);
 		useCustomExp	= getBoolean("UseCustomEXPTotal",	useCustomExp);
 		dropExp		= getBoolean("DropEXP",			dropExp);
 		customExp	= getInt("CustomEXPTotal",		customExp);
 
 		getWorlds();
 
 		if (defaults)
 			save();
 	}
 
 	public void loadLegacy() {
 		log.info("Converting configuration to the current format.");
 		destroyBlocks	= getBoolean("Configuration.DestroyBlocks",	destroyBlocks);
 		spawnEgg	= getBoolean("Configuration.SpawnEgg",		spawnEgg);
 		spawnPortal	= getBoolean("Configuration.SpawnPortal",	spawnPortal);
 		teleportEgg	= getBoolean("Configuration.EggsCanTeleport",	teleportEgg);
 		maxSpawnMinutes	= getLong("Configuration.MaxRespawnMinutes",	maxSpawnMinutes);
 		minSpawnMinutes	= getLong("Configuration.MinRespawnMinutes",	minSpawnMinutes);
 		expResetMinutes	= getLong("Configuration.EXPResetMinutes",	expResetMinutes);
 		expMaxDistance	= getLong("Configuration.EXPMaxDistance",	expMaxDistance);
 		useCustomExp	= getBoolean("Configuration.UseCustomEXPTotal",	useCustomExp);
 		customExp	= getInt("Configuration.CustomEXPTotal",	customExp);
 
 		addWorlds();
 		getPlayers();
 		getBannedPlayers();
 		save();
 	}
 
 	public void save() {
 		YamlConfiguration newConfig = new YamlConfiguration();
 
 		newConfig.set("DestroyBlocks",		destroyBlocks);
 		newConfig.set("SpawnEgg",		spawnEgg);
 		newConfig.set("SpawnPortal",		spawnPortal);
 		newConfig.set("EggsCanTeleport",	teleportEgg);
 		newConfig.set("MaxRespawnMinutes",	maxSpawnMinutes);
 		newConfig.set("MinRespawnMinutes",	minSpawnMinutes);
 		newConfig.set("EXPResetMinutes",	expResetMinutes);
 		newConfig.set("EXPMaxDistance",		expMaxDistance);
 		newConfig.set("UseCustomEXPTotal",	useCustomExp);
 		newConfig.set("DropEXP",		dropExp);
 		newConfig.set("CustomEXPTotal",		customExp);
 
 		ConfigurationSection worldSection = newConfig.createSection("Worlds");
 
 		for (String key : worlds.keySet()) {
 			if (key == null)
 				continue;
 
 			if (!worlds.containsKey(key))
 				continue;
 
 			if (!xCoords.containsKey(key))
 				continue;
 
 			if (!yCoords.containsKey(key))
 				continue;
 
 			if (!zCoords.containsKey(key))
 				continue;
 
 			ConfigurationSection world = worldSection.createSection(key);
 			world.set("MaxDragons", worlds.get(key));
 
 			ConfigurationSection spawn = world.createSection("SpawnPoint");
 			spawn.set("X", xCoords.get(key));
 			spawn.set("Y", yCoords.get(key));
 			spawn.set("Z", zCoords.get(key));
 		}
 
 		File configurationFile = new File(plugin.getDataFolder(), "config.yml");
 
 		try {
 			newConfig.save(configurationFile);
 		}
 		catch (Exception e) {
 			log.warning("Unable to save configuration.");
 		}
 	}
 
 	public void getPlayers() {
 		Timestamp currentTime = new Timestamp(new Date().getTime());
 		ConfigurationSection playerSection = getConfigurationSection("Players");
 
 		if (playerSection == null)
 			return;
 
 		Map<String, Object> playerValues = playerSection.getValues(false);
 
 		if (playerValues.isEmpty())
 			return;
 
 		for (Object key : playerValues.keySet()) {
 			if (!(key instanceof String))
 				continue;
 
 			String player = (String) key;
 			if (!playerValues.containsKey(player))
 				continue;
 
 			Object tempLong = playerValues.get(player);
 			if (!(tempLong instanceof Long))
 				continue;
 
 			Timestamp time = new Timestamp((Long) tempLong);
 			player = player.toUpperCase().toLowerCase();
 
 			if (currentTime.getTime() >= (time.getTime() + (expResetMinutes * 60000)))
 				continue;
 
 			this.plugin.data.players.put(player, time);
 		}
 	}
 
 	public void getBannedPlayers() {
 		String name = "BannedPlayers";
 		ConfigurationSection playerSection = getConfigurationSection(name);
 
 		if (playerSection == null)
 			return;
 
 		Map<String, Object> playerValues = playerSection.getValues(false);
 
 		if (playerValues.isEmpty())
 			return;
 
 		for (Object key : playerValues.keySet()) {
 			if (!(key instanceof String))
 				continue;
 
 			String player = (String) key;
 			if (!playerValues.containsKey(player))
 				continue;
 
 			Object tempString = playerValues.get(player);
 			if (!(tempString instanceof String))
 				continue;
 
 			String banReason = (String) tempString;
 			player = player.toUpperCase().toLowerCase();
 
 			this.plugin.data.bannedPlayers.put(player, banReason);
 		}
 	}
 
 	public void getWorlds() {
 		ConfigurationSection worldSection = getConfigurationSection("Worlds");
 
 		if (worldSection == null)
 			return;
 
 		Map<String, Object> worldValues = worldSection.getValues(false);
 
 		if (worldValues.isEmpty())
 			return;
 
 		for (Object key : worldValues.keySet()) {
 			if (!(key instanceof String))
 				continue;
 
 			String world	= (String) key;
 			String name	= world.toUpperCase().toLowerCase();
 			if (!worldValues.containsKey(world))
 				continue;
 
 			Object tempObject = worldValues.get(world);
 			if (!(tempObject instanceof ConfigurationSection))
 				continue;
 
 			ConfigurationSection section = (ConfigurationSection) tempObject;
 
 			worlds.put(name, section.getInt("MaxDragons", 1));
 			xCoords.put(name, section.getInt("X", 0));
 			yCoords.put(name, section.getInt("Y", 128));
 			zCoords.put(name, section.getInt("Z", 0));
 		}
 	}
 
 	public void addWorlds() {
 	
 		List<World> worldList = plugin.getServer().getWorlds();
 		for (World world : worldList) {
 			if (!World.Environment.THE_END.equals(world.getEnvironment()))
 				continue;
 
 			String name = world.getName().toUpperCase().toLowerCase();
 			worlds.put(name, getInt("Configuration.MaxDragons", 1));
 			xCoords.put(name, 0);
 			yCoords.put(name, 128);
 			zCoords.put(name, 0);
 
 			long deathLong = getLong("LastDeath", 0);
 			this.plugin.data.lastDeath.put(name, new Timestamp(deathLong));
 		}
 	}
 }
