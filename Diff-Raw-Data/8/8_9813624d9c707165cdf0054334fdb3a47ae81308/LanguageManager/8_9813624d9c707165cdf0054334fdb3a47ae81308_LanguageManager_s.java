 /**
  *  Name: LanguageManager.java
  *  Date: 23:22:06 - 13 sep 2012
  * 
  *  Author: LucasEmanuel @ bukkit forums
  *  
  *  
  *  Copyright 2013 Lucas Arnstrm
  *
  *  This program is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  *
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with this program. If not, see <http://www.gnu.org/licenses/>.
  *  
  *
  *  Filedescription:
  *  
  *  Manages all of the configurable broadcasts etc.
  * 
  */
 
 package me.lucasemanuel.survivalgamesmultiverse.managers;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Map.Entry;
 
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.configuration.file.YamlConfiguration;
 
 import me.lucasemanuel.survivalgamesmultiverse.Main;
 import me.lucasemanuel.survivalgamesmultiverse.utils.ConsoleLogger;
 
 public class LanguageManager {
 
 	private Main					plugin;
 	private ConsoleLogger			logger;
 
 	private HashMap<String, String>	language;
 
 	public LanguageManager(Main instance) {
 		plugin = instance;
 		logger = new ConsoleLogger("LanguageManager");
 
 		language = new HashMap<String, String>();
 
 		loadLanguage();
 	}
 
 	private void loadLanguage() {
 
 		FileConfiguration config = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), "language.yml"));
 
 		checkDefaults(config);
 
 		for (String key : config.getKeys(true)) {
 			language.put(key, config.getString(key));
 		}
 	}
 
 	private void checkDefaults(FileConfiguration config) {
 
 		@SuppressWarnings("serial")
 		HashMap<String, String> defaults = new HashMap<String, String>() {{
 				put("gameover",               "Game over!");
 				put("wonTheGame",             "won the game!");
 				put("isOutOfTheGame",         "is out of the game!");
 				put("wasKilledBy",            "was killed by");
 				put("youJoinedTheGame",       "You joined the game!");
 				put("playerJoinedGame",       "joined the game!");
 				put("alreadyPlaying",         "You are already playing!");
 				put("gameIsFull",             "Game is full!");
 				put("blockedMovement",        "You are not allowed to move yet!");
 				put("blockedCommand",         "You are not allowed to use that command ingame!");
 				put("sgplayersHeading",       "Alive Players");
 				put("sgleaveNotIngame",       "You are not in the game!");
 				put("forcedPumpkin",          "You have to wear that pumpkin!");
 				put("gameHasNotStartedYet",   "The game hasn't started yet!");
 				put("gameHasAlreadyStarted",  "The game has already started, try another world!");
 				put("sgplayersNoonealive",    "No players alive!");
 				put("sgplayersIncorrect",     "You need to be in a gameworld, or enter the name of one!");
 				
 				// Blocks
 				put("nonAllowedBlock",        "You are not allowed to break/place this block!");
 				
 				// StatusManager
 				put("waitingForPlayers",      "Waiting for more players!");
 				put("timeleft",               "seconds left until game starts.");
 				put("gamestarted",            "Game started! GO GO GO!");
 				put("broadcast_before_arena", "You are being teleported to the arena in 5 seconds!");
 				put("sendingEveryoneToArena", "The game took to long to finish! Sending everyone to the arena!");
 				put("sentYouToArena",         "You where sent to the arena!");
 				put("killedSendingArena",     "No locations left in the arena! You where killed.");
 				put("secondsTillTheGameEnds", "seconds until the game is cancelled!");
 
 				// Anticheat
				put("anticheat.disconnect", "was removed due to disconnect!");
 				put("anticheat.teleported", "was removed due to teleportation!");
 
 				// Signs
 				put("signs.started",       "Started");
 				put("signs.waiting",       "Waiting");
 				put("signs.playersIngame", "Players Ingame");
 				put("signs.frozen",        "Frozen");
 				
 				// Scrap
 				put("Join_Blocked_Frozen", "This world is temporary frozen!");
 		}};
 
 		boolean save = false;
 
 		for (Entry<String, String> entry : defaults.entrySet()) {
 			if (!config.contains(entry.getKey())) {
 				config.set(entry.getKey(), entry.getValue());
 				save = true;
 			}
 		}
 
 		if (save) {
 			try {
 				config.save(this.plugin.getDataFolder() + File.separator + "language.yml");
 			}
 			catch (IOException e) {
 				this.logger.severe("Could not save language.yml!");
 			}
 		}
 	}
 
 	public String getString(String key) {
 
 		if (language.containsKey(key)) {
 			return language.get(key);
 		}
 
 		return null;
 	}
 }
