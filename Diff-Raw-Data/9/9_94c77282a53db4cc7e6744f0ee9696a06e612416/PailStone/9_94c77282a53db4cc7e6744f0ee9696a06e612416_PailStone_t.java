 package net.eonz.bukkit.psduo;
 
 /*
  * This code is Copyright (C) 2011 Chris Bode, Some Rights Reserved.
  *
  * Copyright (C) 1999-2002 Technical Pursuit Inc., All Rights Reserved. Patent 
  * Pending, Technical Pursuit Inc.
  *
  * Unless explicitly acquired and licensed from Licensor under the Technical 
  * Pursuit License ("TPL") Version 1.0 or greater, the contents of this file are 
  * subject to the Reciprocal Public License ("RPL") Version 1.1, or subsequent 
  * versions as allowed by the RPL, and You may not copy or use this file in 
  * either source code or executable form, except in compliance with the terms and 
  * conditions of the RPL.
  *
  * You may obtain a copy of both the TPL and the RPL (the "Licenses") from 
  * Technical Pursuit Inc. at http://www.technicalpursuit.com.
  *
  * All software distributed under the Licenses is provided strictly on an "AS IS"
  * basis, WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESS OR IMPLIED, AND TECHNICAL
  * PURSUIT INC. HEREBY DISCLAIMS ALL SUCH WARRANTIES, INCLUDING WITHOUT 
  * LIMITATION, ANY WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR 
  * PURPOSE, QUIET ENJOYMENT, OR NON-INFRINGEMENT. See the Licenses for specific 
  * language governing rights and limitations under the Licenses. 
  */
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import net.eonz.bukkit.psduo.permissions.PermissionsInterface;
 import net.eonz.bukkit.psduo.permissions.PermissionsSetup;
 import net.eonz.bukkit.psduo.signs.PSSignCommand;
 import net.eonz.bukkit.psduo.signs.SignController;
 import net.eonz.bukkit.psduo.signs.TriggerType;
 
 import org.bukkit.ChatColor;
 import org.bukkit.World;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.block.Block;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Event;
 import org.bukkit.event.Event.Priority;
 
 public class PailStone extends JavaPlugin {
 
 	/**
 	 * The global sign controller.
 	 */
 	public SignController sgc;
 
 	public PSTickControl tickctrl;
 
 	/**
 	 * The player info tracker.
 	 */
 	public PSPlayers players = new PSPlayers();
 
 	/**
 	 * The block listener for signs
 	 */
 	private PSBlockListener blockListener = new PSBlockListener(this);
 
 	/**
 	 * Listener for player events.
 	 */
 	private PSPlayerListener playerListener = new PSPlayerListener(this, players);
 
 	/**
 	 * The place to store all pailstone data.
 	 */
 	public static String dataPath = "./plugins/pail/stone/";
 
 	public static File configFile = new File(dataPath + "config.txt");
 
 	public PailConfigFile cfg;
 
 	// CONFIG KEYS -------------
 
 	public boolean cfgWipeProtection;
 	public int cfgMaxCuboid;
         public List<Integer> blockList;
         public boolean useWhiteList;
 
 	// END CONFIG --------------
 
 	public void onDisable() {
 		sgc.save();
 		cfg.save();
 		this.c("Finished unloading. (" + this.getDescription().getVersion() + ")");
 	}
 
 	public void onEnable() {
 		PluginDescriptionFile pdf = this.getDescription();
 
 		this.c("Loading PailStone");
 		this.c("Copyright (c) 2011 Chris Bode");
 		this.c("Author contact: Chris@Eonz.net");
 		this.c("PailStone is licensed under the");
 		this.c("RECIPROCAL PUBLIC LICENSE 1.1");
 
 		cfg = new PailConfigFile(this, configFile);
 		cfg.load();
 
 		cfgWipeProtection = cfg.getBoolean("wipe-protection", true);
 		cfgMaxCuboid = cfg.getInt("max-cuboid-area", 400);
                 blockList = cfg.getIntegerList("block-blacklist", new ArrayList<Integer>());
                 useWhiteList = cfg.getBoolean("use-blacklist-as-whitelist", false);
 
 		cfg.announce();
 
 		cfg.save();
 
 		setupPermissions();
 
 		sgc = new SignController(this);
 		tickctrl = new PSTickControl(this);
 
 		sgc.load();
 
 		PluginManager pm = this.getServer().getPluginManager();
 
 		pm.registerEvent(Event.Type.SIGN_CHANGE, blockListener, Priority.Normal, this);
 		pm.registerEvent(Event.Type.REDSTONE_CHANGE, blockListener, Priority.Monitor, this);
 		pm.registerEvent(Event.Type.PLAYER_INTERACT, playerListener, Priority.Monitor, this);
 
 		this.getServer().getScheduler().scheduleSyncRepeatingTask(this, new PSTickUpdate(TriggerType.TIMER_SECOND, sgc), 10, 20);
 		this.getServer().getScheduler().scheduleSyncRepeatingTask(this, new PSTickUpdate(TriggerType.TIMER_HALF_SECOND, sgc), 10, 10);
 
 		this.getServer().getScheduler().scheduleSyncRepeatingTask(this, tickctrl, 10, 1);
 
 		this.getCommand("trigger").setExecutor(new PSSignCommand(sgc, TriggerType.TRIGGER_COMMAND));
 		this.getCommand("pailstone").setExecutor(new PSCommand(this));
 
 		this.c("Finished Loading. (" + pdf.getVersion() + ")");
 	}
 
 	Logger log = Logger.getLogger("PailStone");
 
 	public void c(String message) {
 		log.log(Level.INFO, "[PailStone] " + message);
 	}
 
 	public void e(String message) {
 		log.log(Level.WARNING, "[ERROR] " + message);
 	}
 
 	public void d(String message) {
 		c("[*DEBUG*] " + message);
 	}
 
 	public boolean alert(String playerName, String message) {
 		Player p = this.getServer().getPlayer(playerName);
 		if (p != null && p.isOnline()) {
 			p.sendMessage(ChatColor.GOLD + "[PailStone] " + message);
 			return true;
 		}
 		return false;
 	}
 
 	/**
 	 * This method standardizes a lines array, removing all color codes and
 	 * adding blank lines to the array if they don't exist.
 	 * 
 	 * @param lines
 	 * @return
 	 */
 	public static String[] formatLines(String[] lines) {
 		String[] newLines = new String[4];
 		for (int i = 0; i < 4; i++) {
 			if (lines.length > i) {
 				newLines[i] = org.bukkit.ChatColor.stripColor(lines[i]);
 			} else {
 				newLines[i] = "";
 			}
 		}
 		return newLines;
 	}
 
 	/**
 	 * Utility for drawing cuboids.
 	 * 
 	 * @param materialID
 	 * @param world
 	 * @param x1
 	 * @param y1
 	 * @param z1
 	 * @param x2
 	 * @param y2
 	 * @param z2
 	 */
 	public static void drawCuboid(int materialID, World world, int x1, int y1, int z1, int x2, int y2, int z2) {
 		if (x1 > x2) {
 			int x3 = x1;
 			x1 = x2;
 			x2 = x3;
 		}
 		if (y1 > y2) {
 			int y3 = y1;
 			y1 = y2;
 			y2 = y3;
 		}
 		if (z1 > z2) {
 			int z3 = z1;
 			z1 = z2;
 			z2 = z3;
 		}
 		for (int x = x1; x <= x2; x++) {
 			for (int y = y1; y <= y2; y++) {
 				for (int z = z1; z <= z2; z++) {
 					if (world.getBlockAt(x, y, z).getTypeId() != materialID) {
 						world.getBlockAt(x, y, z).setTypeId(materialID);
 						// PailStone.server.getWorld(world).getBlockAt(x, y, z)
 						// .getState().update();
 					}
 				}
 			}
 		}
 	}
 
 	/**
 	 * Utility for drawing cuboids.
 	 * 
 	 * @param materialID
 	 * @param data
 	 * @param world
 	 * @param x1
 	 * @param y1
 	 * @param z1
 	 * @param x2
 	 * @param y2
 	 * @param z2
 	 */
 	public static void drawCuboid(int materialID, byte data, World world, int x1, int y1, int z1, int x2, int y2, int z2) {
 		if (x1 > x2) {
 			int x3 = x1;
 			x1 = x2;
 			x2 = x3;
 		}
 		if (y1 > y2) {
 			int y3 = y1;
 			y1 = y2;
 			y2 = y3;
 		}
 		if (z1 > z2) {
 			int z3 = z1;
 			z1 = z2;
 			z2 = z3;
 		}
 		Block b;
 		for (int x = x1; x <= x2; x++) {
 			for (int y = y1; y <= y2; y++) {
 				for (int z = z1; z <= z2; z++) {
 					b = world.getBlockAt(x, y, z);
 					if (b.getTypeId() != materialID || b.getData() != data) {
 						b.setTypeId(materialID);
 						b.setData(data);
 					}
 				}
 			}
 		}
 	}
 
 	private PermissionsInterface permissionHandler;
 
 	private void setupPermissions() {
 		permissionHandler = PermissionsSetup.getBestPermissions(this.getDescription().getName());
 	}
 
 	public boolean hasPermission(Player p, String permission, String world) {
		// Have to check all three due to a peculiarity in bukkit's built-in permissions.
 		return this.permissionHandler.has(p, "pailstone." + permission, world) || this.permissionHandler.has(p, "pailstone.*", world) || this.permissionHandler.has(p, "*", world);
 	}
 
 	public boolean inGroup(Player p, String world, String group) {
 		return this.permissionHandler.inGroup(p, group, world);
 	}
 
 }
