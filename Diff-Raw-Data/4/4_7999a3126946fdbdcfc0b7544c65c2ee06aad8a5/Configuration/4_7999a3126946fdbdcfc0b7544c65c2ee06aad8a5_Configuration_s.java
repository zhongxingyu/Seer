 /*
  * Copyright (c) 2012 Sean Porter <glitchkey@gmail.com>
  *
  * Permission is hereby granted, free of charge, to any person
  * obtaining a copy of this software and associated documentation
  * files (the "Software"), to deal in the Software without restriction,
  * including without limitation the rights to use, copy, modify, merge,
  * publish, distribute, sublicense, and/or sell copies of the Software,
  * and to permit persons to whom the Software is furnished to do so,
  * subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be
  * included in all copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
  * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
  * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
  * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
  * CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
  * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
  * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
  */
 
 package org.enderspawn;
 
 //* IMPORTS: JDK/JRE
 	import java.io.File;
 	import java.lang.Integer;
 	import java.lang.Long;
 	import java.lang.String;
 	import java.sql.Timestamp;
 	import java.util.Date;
 	import java.util.HashMap;
 	import java.util.logging.Logger;
 	import java.util.Map;
 	import java.util.Set;
 //* IMPORTS: BUKKIT
 	import org.bukkit.configuration.ConfigurationSection;
 	import org.bukkit.configuration.file.YamlConfiguration;
 //* IMPORTS: SPOUT
 	//* NOT NEEDED
 //* IMPORTS: OTHER
 	//* NOT NEEDED
 
 public class Configuration extends YamlConfiguration
 {
 	private	File config;
 	private Logger log;
 
 	public	HashMap<String,	Timestamp>	players;
 	public	HashMap<String,	String>		bannedPlayers;
 	public	HashMap<String,	Integer>	dragonCounts;
 
 	public	boolean	destroyBlocks;
 	public	boolean	spawnEgg;
 	public	boolean	spawnPortal;
 	public	boolean teleportEgg;
 	public	long	spawnMinutes;
 	public	long	expResetMinutes;
 	public	long	expMaxDistance;
 	public	int	maxDragons;
 
 	public	Timestamp lastDeath;
 
 	public Configuration(File config, Logger log)
 	{
 		this.config	= config;
 		this.log	= log;
 
 		players		= new HashMap<String, Timestamp>();
 		bannedPlayers	= new HashMap<String, String>();
 		dragonCounts	= new HashMap<String, Integer>();
 
 		destroyBlocks	= false;
 		spawnEgg	= true;
 		spawnPortal	= false;
 		teleportEgg	= false;
 		spawnMinutes	= 0;
 		expResetMinutes	= 1440;
 		expMaxDistance	= 75;
 		maxDragons	= 1;
 
 		lastDeath	= new Timestamp(0);
 	}
 
 	public void load()
 	{
 		try
 		{
 			super.load(config);
 		}
 		catch(Exception e)
 		{
 			log.warning("Unable to load configuration, using defaults instead.");
 		}
 
 		destroyBlocks	= getBoolean("Configuration.DestroyBlocks",	destroyBlocks);
 		spawnEgg	= getBoolean("Configuration.SpawnEgg",		spawnEgg);
 		spawnPortal	= getBoolean("Configuration.SpawnPortal",	spawnPortal);
 		teleportEgg	= getBoolean("Configuration.EggsCanTeleport",	teleportEgg);
 		spawnMinutes	= getLong("Configuration.RespawnMinutes",	spawnMinutes);
 		expResetMinutes	= getLong("Configuration.EXPResetMinutes",	expResetMinutes);
 		expMaxDistance	= getLong("Configuration.EXPMaxDistance",	expMaxDistance);
 		maxDragons	= getInt("Configuration.MaxDragons",		maxDragons);
 		lastDeath	= new Timestamp(getLong("LastDeath",		0));
 
 		getPlayers();
 		getBannedPlayers();
 		getDragons();
 		save();
 	}
 
 	public void save()
 	{
 		set("Configuration.DestroyBlocks",	destroyBlocks);
 		set("Configuration.SpawnEgg",		spawnEgg);
 		set("Configuration.SpawnPortal",	spawnPortal);
 		set("Configuration.EggsCanTeleport",	teleportEgg);
 		set("Configuration.RespawnMinutes",	spawnMinutes);
 		set("Configuration.EXPResetMinutes",	expResetMinutes);
 		set("Configuration.EXPMaxDistance",	expMaxDistance);
 		set("Configuration.MaxDragons",		maxDragons);
 		set("LastDeath", lastDeath.getTime());
 
 		HashMap<String,	Long>	playerLongs = new HashMap<String, Long>();
 		for(String key : players.keySet())
 		{
 			if(!players.containsKey(key))
 				continue;
 
 			playerLongs.put(key, players.get(key).getTime());
 		}
 
 		createSection("Players", playerLongs);
 		createSection("BannedPlayers", bannedPlayers);
 		createSection("DragonCounts", dragonCounts);
 
 		try
 		{
 			super.save(config);
 		}
 		catch(Exception e)
 		{
 			log.warning("Unable to save configuration.");
 		}
 	}
 
 	public void getPlayers()
 	{
 		Timestamp currentTime = new Timestamp(new Date().getTime());
 		ConfigurationSection playerSection = getConfigurationSection("Players");
 
 		if(playerSection == null)
 			return;
 
 		Map<String, Object> playerValues = playerSection.getValues(false);
 
 		if(playerValues.isEmpty())
 			return;
 
 		for(Object key : playerValues.keySet())
 		{
 			if(!(key instanceof String))
 				continue;
 
 			String player = (String) key;
 			if(!playerValues.containsKey(player))
 				continue;
 
 			Object tempLong = playerValues.get(player);
 			if(!(tempLong instanceof Long))
 				continue;
 
 			Timestamp time = new Timestamp((Long) tempLong);
 			player = player.toUpperCase().toLowerCase();
 
 			if(currentTime.getTime() >= (time.getTime() + (expResetMinutes * 60000)))
 				continue;
 
 			players.put(player, time);
 		}
 	}
 
 	public void getBannedPlayers()
 	{
 		String name = "BannedPlayers";
 		ConfigurationSection playerSection = getConfigurationSection(name);
 
 		if(playerSection == null)
 			return;
 
 		Map<String, Object> playerValues = playerSection.getValues(false);
 
 		if(playerValues.isEmpty())
 			return;
 
 		for(Object key : playerValues.keySet())
 		{
 			if(!(key instanceof String))
 				continue;
 
 			String player = (String) key;
 			if(!playerValues.containsKey(player))
 				continue;
 
 			Object tempString = playerValues.get(player);
 			if(!(tempString instanceof String))
 				continue;
 
 			String banReason = (String) tempString;
 			player = player.toUpperCase().toLowerCase();
 
 			bannedPlayers.put(player, banReason);
 		}
 	}
 
 	public void getDragons()
 	{
 		String name = "DragonCounts";
 		ConfigurationSection dragonSection = getConfigurationSection(name);
 
 		if(dragonSection == null)
 			return;
 
 		Map<String, Object> dragonValues = dragonSection.getValues(false);
 
 		if(dragonValues.isEmpty())
 			return;
 
 		for(Object key : dragonValues.keySet())
 		{
 			if(!(key instanceof String))
 				continue;
 
 			String world = (String) key;
 			if(!dragonValues.containsKey(world))
 				continue;
 
 			Object tempInt = dragonValues.get(world);
 			if(!(tempInt instanceof Integer))
 				continue;
 
 			int count = (Integer) tempInt;
 			world = world.toUpperCase().toLowerCase();
 
 			dragonCounts.put(world, count);
 		}
 	}
 }
