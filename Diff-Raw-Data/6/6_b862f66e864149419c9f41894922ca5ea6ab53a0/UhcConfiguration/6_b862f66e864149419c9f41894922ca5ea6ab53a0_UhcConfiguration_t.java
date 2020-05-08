 package com.martinbrook.tesseractuhc;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 import org.bukkit.ChatColor;
 import org.bukkit.configuration.Configuration;
 import org.bukkit.configuration.ConfigurationSection;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.inventory.ItemStack;
 
 import com.martinbrook.tesseractuhc.startpoint.UhcStartPoint;
 import com.martinbrook.tesseractuhc.util.FileUtils;
 import com.martinbrook.tesseractuhc.util.MatchUtils;
 
 public class UhcConfiguration {
 
 
 	private Configuration defaults;
 	private UhcMatch m;
 	private YamlConfiguration md; // Match data
 	public static String DEFAULT_MATCHDATA_FILE = "uhcmatch.yml";
 	public static String DEFAULT_TEAMDATA_FILE = "uhcteams.yml";
 	private ItemStack[] bonusChest = new ItemStack[27];
 
 	
 	public UhcConfiguration(UhcMatch match, Configuration defaults) {
 		this.m = match;
 		this.defaults = defaults;
 		
 		this.loadMatchParameters();
 		this.loadTeams();
 		
 	}
 	
 	/**
 	 * Load match data from the default file. If it does not exist, load defaults.
 	 */
 	public void loadMatchParameters() { 
 		try {
 			md = YamlConfiguration.loadConfiguration(FileUtils.getDataFile(m.getStartingWorld().getWorldFolder(), DEFAULT_MATCHDATA_FILE, true));
 			
 		} catch (Exception e) {
 			md = new YamlConfiguration();
 		}
 				
 		// Load start points
 		m.clearStartPoints();
 		
 		List<String> startData = md.getStringList("starts");
 		for (String startDataEntry : startData) {
 			String[] data = startDataEntry.split(",");
 			if (data.length == 4) {
 				try {
 					int n = Integer.parseInt(data[0]);
 					double x = Double.parseDouble(data[1]);
 					double y = Double.parseDouble(data[2]);
 					double z = Double.parseDouble(data[3]);
 					UhcStartPoint sp = m.createStartPoint(n, m.getStartingWorld(), x, y, z, !this.isFFA(), false);
 					if (sp == null) {
 						m.adminBroadcast("Duplicate start point: " + n);
 
 					}
 				} catch (NumberFormatException e) {
 					m.adminBroadcast("Bad start point definition in match data file: " + startDataEntry);
 				}
 
 			} else {
 				m.adminBroadcast("Bad start point definition in match data file: " + startDataEntry);
 			}
 		}
 		
 		// Load POIs
 		m.clearPOIs();
 		
 		List<String> poiData = md.getStringList("pois");
 		for (String poiDataEntry : poiData) {
 			String[] data = poiDataEntry.split(",",5);
 			if (data.length == 5) {
 				try {
 					String world = data[0];
 					double x = Double.parseDouble(data[1]);
 					double y = Double.parseDouble(data[2]);
 					double z = Double.parseDouble(data[3]);
 					String name = data[4];
 					m.addPOI(world, x, y, z, name);
 				} catch (NumberFormatException e) {
 					m.adminBroadcast("Bad poi definition in match data file: " + poiDataEntry);
 				}
 
 			} else {
 				m.adminBroadcast("Bad poi definition in match data file: " + poiDataEntry);
 			}
 		}
 		
 		setDefaultMatchParameters();
 		
 		// Convert saved bonus chest into an ItemStack array
 		ItemStack[] bonusChest = new ItemStack[27];
 		List<?> data = md.getList("bonuschest");
 		
 		if (data != null) {
 		
 			for (int i = 0; i < 27; i++) {
 				Object o = data.get(i);
 				if (o != null && o instanceof ItemStack)
 					bonusChest[i] = (ItemStack) o;
 			}
 		}
 		setBonusChest(bonusChest);
 	}
 	
 	
 
 	/**
 	 * Set up a default matchdata object
 	 */
 	private void setDefaultMatchParameters() {
 		
 		Map<String, Object> mapDefaults = defaults.getValues(true);
 		for (Map.Entry<String, Object> m : mapDefaults.entrySet()) {
 			if (!md.contains(m.getKey())) {
 				md.set(m.getKey(), m.getValue());
 			}
 		}
 		
 		this.saveMatchParameters();
 	}
 
 	
 	/**
 	 * Save start points to the default file
 	 * 
 	 * @return Whether the operation succeeded
 	 */
 	public void saveMatchParameters() {
 		ArrayList<String> startData = new ArrayList<String>();
		for (UhcStartPoint sp : m.getStartPoints().values()) {
 			startData.add(sp.getNumber() + "," + sp.getX() + "," + sp.getY() + "," + sp.getZ());
 		}
 		
 		md.set("starts",startData);
 		
 		ArrayList<String> poiData = new ArrayList<String>();
 		for (UhcPOI poi : m.getPOIs()) {
 			poiData.add(poi.getWorld().getName() + "," + poi.getX() + "," + poi.getY() + "," + poi.getZ() + "," + poi.getName());
 		}
 		
 		md.set("pois",poiData);
 		
 		try {
 			md.save(FileUtils.getDataFile(m.getStartingWorld().getWorldFolder(), DEFAULT_MATCHDATA_FILE, false));
 		} catch (IOException e) {
 			m.adminBroadcast(TesseractUHC.ALERT_COLOR + "Warning: Could not save match data");
 		}
 	}
 
 
 	/**
 	 * Reset all match parameters to default values
 	 */
 	public void resetMatchParameters() {
 		m.clearStartPoints();
 		m.clearPOIs();
 		md = new YamlConfiguration();
 		this.setDefaultMatchParameters();
 	}
 
 	/**
 	 * Save players and teams to the default location.
 	 */
 	public void saveTeams() {
 		YamlConfiguration teamData = new YamlConfiguration();
 
 		for(UhcTeam t : m.getTeams()) {
 			ConfigurationSection teamSection = teamData.createSection(t.getIdentifier());
 			
 			ArrayList<String> participants = new ArrayList<String>();
 			for (UhcParticipant up : t.getMembers()) participants.add(up.getName());
 			
 			teamSection.set("name", t.getName());
 			teamSection.set("players", participants);
 		}
 		
 		
 		try {
 			teamData.save(FileUtils.getDataFile(m.getStartingWorld().getWorldFolder(), DEFAULT_TEAMDATA_FILE, false));
 		} catch (IOException e) {
 			m.adminBroadcast(TesseractUHC.ALERT_COLOR + "Warning: Could not save team data");
 
 		}
 	}
 	
 	
 	/**
 	 * Load players and teams from the default location.
 	 */
 	public void loadTeams() {
 		if (!m.clearTeams()) {
 			m.adminBroadcast(TesseractUHC.ALERT_COLOR + "Warning: Could not remove existing team/player data");
 			return;
 		}
 		
 		
 		YamlConfiguration teamData;
 		try {
 			teamData = YamlConfiguration.loadConfiguration(FileUtils.getDataFile(m.getStartingWorld().getWorldFolder(), DEFAULT_TEAMDATA_FILE, true));
 
 		} catch (Exception e) {
 			return;
 		}
 		
 		for(String teamIdentifier : teamData.getKeys(false)) {
 			ConfigurationSection teamSection = teamData.getConfigurationSection(teamIdentifier);
 			String teamName = teamSection.getString("name");
 			if (!m.addTeam(teamIdentifier, teamName)) {
 				m.adminBroadcast(TesseractUHC.ALERT_COLOR + "Warning: failed to create team " + teamName);
 			} else {
 				List<String> teamMembers = teamSection.getStringList("players");
 				if (teamMembers == null) {
 					m.adminBroadcast(TesseractUHC.ALERT_COLOR + "Warning: team has no members: " + teamName);
 				} else {
 					for (String participantName : teamMembers) {
 						if (!m.addParticipant(m.getPlayer(participantName), teamIdentifier))
 							m.adminBroadcast(TesseractUHC.ALERT_COLOR + "Warning: failed to add player: " + participantName);
 					}
 				}
 			}
 		}
 	}
 	
 	/**
 	 * Set the length of the initial no-PVP period
 	 * 
 	 * @param nopvp The duration of the no-PVP period, in seconds
 	 */
 	public void setNopvp(int nopvp) {
 		md.set("nopvp", nopvp);
 		saveMatchParameters();
 	}
 
 	/**
 	 * Get the length of the initial no-PVP period
 	 * 
 	 * @return The duration of the no-PVP period, in seconds
 	 */
 	public int getNopvp() {
 		return md.getInt("nopvp");
 	}
 
 	/**
 	 * Set the mining fatigue penalties.
 	 * 
 	 * @param gold Exhaustion penalty to add when mining at the gold layer
 	 * @param diamond Exhaustion penalty to add when mining at the diamond layer
 	 */
 	public void setMiningFatigue(double gold, double diamond) {
 		md.set("miningfatigue.gold", gold);
 		md.set("miningfatigue.diamond", diamond);
 		saveMatchParameters();
 	}
 
 
 	/**
 	 * Get the current exhaustion penalty for mining at the gold layer
 	 * 
 	 * @return Exhaustion penalty to add when mining at the gold layer
 	 */
 	public double getMiningFatigueGold() {
 		return md.getDouble("miningfatigue.gold");
 	}
 
 	/**
 	 * Get the current exhaustion penalty for mining at the diamond layer
 	 * 
 	 * @return Exhaustion penalty to add when mining at the diamond layer
 	 */
 	public double getMiningFatigueDiamond() {
 		return md.getDouble("miningfatigue.diamond");
 	}
 
 	/**
 	 * Set the bonus items dropped in a PVP kill. One of the specified item will be dropped.
 	 * 
 	 * @param id The item ID to give a pvp killer
 	 */
 	public void setKillerBonus(int id) { setKillerBonus(id,1); }
 	
 	/**
 	 * Set the bonus items dropped in a PVP kill.
 	 * 
 	 * @param id The item ID to give a pvp killer
 	 * @param quantity The number of items to drop
 	 */
 	public void setKillerBonus(int id, int quantity) {
 		if (id == 0) quantity = 0;
 		md.set("killerbonus.id", id);
 		md.set("killerbonus.quantity", quantity);
 		saveMatchParameters();
 		
 	}
 
 	/**
 	 * Get the bonus items to be dropped by a PVP-killed player in addition to their inventory
 	 * 
 	 * @return The ItemStack to be dropped
 	 */
 	public ItemStack getKillerBonus() {
 		int id = md.getInt("killerbonus.id");
 		int quantity = md.getInt("killerbonus.quantity");
 		
 		if (id == 0 || quantity == 0) return null;
 		
 		return new ItemStack(id, quantity);
 	}
 
 	/**
 	 * Set deathban on/off
 	 * 
 	 * @param d Whether deathban is to be enabled
 	 */
 	public void setDeathban(boolean d) {
 		md.set("deathban", d);
 		this.saveMatchParameters();
 		m.adminBroadcast(TesseractUHC.OK_COLOR + "Deathban has been " + (d ? "enabled" : "disabled") + "!");
 	}
 
 	/**
 	 * Check whether deathban is in effect
 	 * 
 	 * @return Whether deathban is enabled
 	 */
 	public boolean getDeathban() {
 		return md.getBoolean("deathban");
 	}
 	
 	
 	/**
 	 * Set FFA on/off
 	 * 
 	 * @param d Whether FFA is to be enabled
 	 */
 	public void setFFA(boolean d) {
 		md.set("ffa", d);
 		this.saveMatchParameters();
 		m.adminBroadcast(TesseractUHC.OK_COLOR + "FFA has been " + (d ? "enabled" : "disabled") + "!");
 	}
 
 	/**
 	 * Check whether Hard Stone is enabled
 	 * 
 	 * @return Whether hard stone is on
 	 */
 	public boolean isHardStone() {
 		return md.getBoolean("hardstone");
 	}
 	/**
 	 * Set hard stone on/off
 	 * 
 	 * @param d Whether hard stone is to be enabled
 	 */
 	public void setHardStone(boolean d) {
 		md.set("hardstone", d);
 		this.saveMatchParameters();
 		m.adminBroadcast(TesseractUHC.OK_COLOR + "Hard stone has been " + (d ? "enabled" : "disabled") + "!");
 	}
 
 	/**
 	 * Check whether this is an FFA match
 	 * 
 	 * @return Whether this is FFA
 	 */
 	public boolean isFFA() {
 		return md.getBoolean("ffa");
 	}
 	/**
 	 * Update the contents of the match "bonus chest"
 	 * 
 	 * @param p The player
 	 */
 	public void setBonusChest(ItemStack [] bonusChest) {
 		this.bonusChest = bonusChest;
 		md.set("bonuschest", bonusChest);
 		this.saveMatchParameters();
 		
 	}
 
 
 	/**
 	 * Get the contents of the match "bonus chest"
 	 * 
 	 * @return The contents of the bonus chest
 	 */
 	public ItemStack[] getBonusChest() {
 		return bonusChest;
 	}
 	
 	public boolean isUHC() {
 		return md.getBoolean("uhc");
 	}
 
 	public void setUHC(Boolean d) {
 		md.set("uhc", d);
 		this.saveMatchParameters();
 		m.adminBroadcast(TesseractUHC.OK_COLOR + "UHC has been " + (d ? "enabled" : "disabled") + "!");
 	}
 	public void setNoLatecomers(Boolean d) {
 		md.set("nolatecomers", d);
 		this.saveMatchParameters();
 		m.adminBroadcast(TesseractUHC.OK_COLOR + "NoLatecomers has been " + (d ? "enabled" : "disabled") + "!");
 	}
 	
 	public boolean isNoLatecomers() {
 		return md.getBoolean("nolatecomers");
 	}
 	
 	public void setDragonMode(Boolean d) {
 		md.set("dragonmode", d);
 		this.saveMatchParameters();
 		m.adminBroadcast(TesseractUHC.OK_COLOR + "Dragon mode has been " + (d ? "enabled" : "disabled") + "!");
 	}
 	
 	public boolean isDragonMode() {
 		return md.getBoolean("dragonmode");
 	}
 	
 	public void setDamageAlerts(Boolean d) {
 		md.set("damagealerts", d);
 		this.saveMatchParameters();
 		m.adminBroadcast(TesseractUHC.OK_COLOR + "Damage alerts for players have been " + (d ? "enabled" : "disabled") + "!");
 	}
 	
 	public boolean isDamageAlerts() {
 		return md.getBoolean("damagealerts");
 	}
 	
 	/**
 	 * Return a human-friendly representation of a specified match parameter
 	 * 
 	 * @param parameter The match parameter to look up
 	 * @return A human-readable version of the parameter's value
 	 */
 	public String formatParameter(String parameter) {
 		String response = TesseractUHC.ERROR_COLOR + "Unknown parameter";
 		String param = ChatColor.AQUA.toString();
 		String value = ChatColor.GOLD.toString();
 		String desc = "\n" + ChatColor.GRAY + ChatColor.ITALIC + "      ";
 		
 		
 		if ("deathban".equalsIgnoreCase(parameter)) {
 			response = param + "Deathban: " + value;
 			if (this.getDeathban())
 				response += "Enabled" + desc + "Dead players will be prevented from logging back into the server";
 			else 
 				response += "Disabled" + desc + "Dead players will be allowed to stay on the server";
 			
 		} else if ("killerbonus".equalsIgnoreCase(parameter)) {
 			response = param + "Killer bonus: " + value;
 			ItemStack kb = this.getKillerBonus();
 			if (kb == null) 
 				response += "Disabled" + desc + "No additional bonus dropped after a PvP kill";
 			else
 				response += kb.getAmount() + " x " + kb.getType().toString() + desc 
 				+ "Additional items dropped when a player is killed by PvP";
 				
 		} else if ("miningfatigue".equalsIgnoreCase(parameter)) {
 			response = param + "Mining hunger: " + value;
 			double mfg = this.getMiningFatigueGold();
 			double mfd = this.getMiningFatigueDiamond();
 			if (mfg > 0 || mfd > 0)
 				response += (mfg>0 ? (mfg / 8.0) + " (below y=32) " : "") + (mfd > 0 ? (mfd / 8.0) + " (below y=16)" : "" ) + desc;
 			else
 				response += "Disabled" + desc;
 			
 			response += "Hunger penalty per block mined at those depths (stone\n      blocks only)";
 					
 		} else if ("nopvp".equalsIgnoreCase(parameter)) {
 			response = param + "No-PvP period: " + value;
 			int n = this.getNopvp();
 			int mins = n / 60;
 			int secs = n % 60;
 			if (n > 0)
 				response += (mins > 0 ? mins + " minutes" : "")
 						+ (secs > 0 ? secs + " seconds" : "")
 						+ desc +  "Period at the start of the match during which PvP is\n      disabled";
 			else 
 				response += "None" + desc + "PvP will be enabled from the start";
 			
 			
 		} else if ("ffa".equalsIgnoreCase(parameter)) {
 			response = param + "Teams: " + value;
 			if (this.isFFA())
 				response += "Free for all" + desc + "No teams, no alliances, every player for themselves";
 			else
 				response += "Teams" + desc + "Teams work together, last team with a survivor wins";
 			
 		} else if ("hardstone".equalsIgnoreCase(parameter)) {
 			response = param + "Hard stone: " + value;
 			if (this.isHardStone())
 				response += "Enabled" + desc + "Mining smoothstone wears out your tools really quickly";
 			else
 				response += "Disabled" + desc + "Tool durability is unchanged";
 			
 		} else if ("uhc".equalsIgnoreCase(parameter)) {
 			response = param + "UHC: " + value;
 			if (this.isUHC())
 				response += "Enabled" + desc + "No health regeneration, and modified recipes for golden\n      apple and glistering melon";
 			else
 				response += "Disabled" + desc + "Health regeneration and crafting recipes are unchanged";
 		} else if ("nolatecomers".equalsIgnoreCase(parameter)) {
 			response = param + "NoLatecomers: " + value;
 			if (this.isNoLatecomers())
 				response += "Enabled" + desc + "Late arriving players will not be able to connect";
 			else
 				response += "Disabled" + desc + "Late arriving players will be able to join";
 		} else if ("dragonmode".equalsIgnoreCase(parameter)) {
 			response = param + "Dragon mode: " + value;
 			if (this.isDragonMode())
 				response += "Enabled" + desc + "The match will end when the dragon is slain";
 			else
 				response += "Disabled" + desc + "The match will end when only one team/player remains";
 		} else if ("damagealerts".equalsIgnoreCase(parameter)) {
 			response = param + "Damage alerts: " + value;
 			if (this.isDamageAlerts())
 				response += "Enabled" + desc + "Damage alerts will be shown to all players";
 			else
 				response += "Disabled" + desc + "Damage alerts will only be shown to spectators";
 		}
 		
 		
 		return response;
 	}
 
 	public boolean setParameter(String parameter, String value) {
 		// Look up the parameter.
 		
 		if ("deathban".equalsIgnoreCase(parameter)) {
 
 			Boolean v = MatchUtils.stringToBoolean(value);
 			if (v == null) return false;
 			this.setDeathban(v);
 			return true;
 			
 		} else if ("killerbonus".equalsIgnoreCase(parameter)) {
 			Boolean b = MatchUtils.stringToBoolean(value);
 			if (b != null && !b) {
 				this.setKillerBonus(0);
 				return true;
 			}
 			String[] split = value.split(" ");
 			if (split.length > 2)
 				return false;
 			
 			int quantity = 1;
 			
 			try {
 				int id = Integer.parseInt(split[0]);
 				if (split.length > 1)
 					quantity = Integer.parseInt(split[1]);
 				
 				this.setKillerBonus(id, quantity);
 				return true;
 			} catch (NumberFormatException e) {
 				return false;
 			}
 			
 		
 			
 		} else if ("miningfatigue".equalsIgnoreCase(parameter)) {
 			Boolean b = MatchUtils.stringToBoolean(value);
 			if (b != null && !b) {
 				this.setMiningFatigue(0,0);
 				return true;
 			}
 			
 			String[] split = value.split(" ");
 			if (split.length != 2)
 				return false;
 			
 			try {
 				double gold = Double.parseDouble(split[0]);
 				double diamond = Double.parseDouble(split[1]);
 				this.setMiningFatigue(gold, diamond);
 				return true;
 			} catch (NumberFormatException e) {
 				return false;
 			}
 			
 			
 		} else if ("nopvp".equalsIgnoreCase(parameter)) {
 			try {
 				this.setNopvp(Integer.parseInt(value));
 				return true;
 			} catch (NumberFormatException e) {
 				return false;
 			}
 		} else if ("ffa".equalsIgnoreCase(parameter)) {
 			Boolean v = MatchUtils.stringToBoolean(value);
 			if (v == null) return false;
 			this.setFFA(v);
 			return true;
 		} else if ("hardstone".equalsIgnoreCase(parameter)) {
 			Boolean v = MatchUtils.stringToBoolean(value);
 			if (v == null) return false;
 			this.setHardStone(v);
 			return true;
 		} else if ("uhc".equalsIgnoreCase(parameter)) {
 			Boolean v = MatchUtils.stringToBoolean(value);
 			if (v == null) return false;
 			this.setUHC(v);
 			return true;
 		} else if ("nolatecomers".equalsIgnoreCase(parameter)) {
 			Boolean v = MatchUtils.stringToBoolean(value);
 			if (v == null) return false;
 			this.setNoLatecomers(v);
 			return true;
 		} else if ("dragonmode".equalsIgnoreCase(parameter)) {
 			Boolean v = MatchUtils.stringToBoolean(value);
 			if (v == null) return false;
 			this.setDragonMode(v);
 			return true;
 		} else if ("damagealerts".equalsIgnoreCase(parameter)) {
 			Boolean v = MatchUtils.stringToBoolean(value);
 			if (v == null) return false;
 			this.setDamageAlerts(v);
 			return true;
 		} else {
 			return false;
 		}
 		
 		
 	}
 
 }
