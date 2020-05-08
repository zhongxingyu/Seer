 /*
  * Copyright (C) 2013 Fabrizio Lungo
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
 
 package me.flungo.bukkit.randomwarp;
 
 import java.util.Random;
 import java.util.logging.Level;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.World;
 import org.bukkit.block.Block;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 
 /**
  *
  * @author Fabrizio
  */
 public class RandomWarp extends JavaPlugin {
 
 	private final Permissions permissions = new Permissions(this, "rwarp");
 	public static RandomWarp plugin;
 	private PluginDescriptionFile pdf;
 	private PluginManager pm;
 	private Random rand = new Random();
 
 	@Override
 	public void onDisable() {
 		getLogger().log(Level.INFO, pdf.getName() + " is now disabled");
 	}
 
 	@Override
 	public void onEnable() {
 		pdf = this.getDescription();
 		permissions.setupPermissions();
 		pm = getServer().getPluginManager();
 		CommandExecutor ce = new Commands(this);
 		getCommand("rwarp").setExecutor(ce);
 		getCommand("randwarp").setExecutor(ce);
 		getCommand("randomwarp").setExecutor(ce);
 		getConfig().options().copyDefaults(true);
 		saveConfig();
 		getLogger().log(Level.INFO, pdf.getName() + " version " + pdf.getVersion() + " is enabled.");
 	}
 
 	public void reload() {
		reloadConfig();
 	}
 
 	public void teleport(Player p, String area) throws InvalidAreaException, InvalidWorldException, InvalidCoordinatesException, NoLocationFoundException {
 		p.teleport(randomLoc(area));
 		p.setFallDistance(-getConfig().getInt("drop-height")-0.5f);
 	}
 
 	public Location randomLoc(String area) throws InvalidAreaException, InvalidWorldException, InvalidCoordinatesException, NoLocationFoundException {
 		if (!getConfig().contains("warps." + area)) {
 			throw new InvalidAreaException(area);
 		}
 		int x1;
 		int x2;
 		if (getConfig().getInt("warps." + area + ".x1") < getConfig().getInt("warps." + area + ".x2")) {
 			x1 = getConfig().getInt("warps." + area + ".x1");
 			x2 = getConfig().getInt("warps." + area + ".x2");
 		} else {
 			x1 = getConfig().getInt("warps." + area + ".x2");
 			x2 = getConfig().getInt("warps." + area + ".x1");
 		}
 		int z1;
 		int z2;
 		if (getConfig().getInt("warps." + area + ".z1") < getConfig().getInt("warps." + area + ".z2")) {
 			z1 = getConfig().getInt("warps." + area + ".z1");
 			z2 = getConfig().getInt("warps." + area + ".z2");
 		} else {
 			z1 = getConfig().getInt("warps." + area + ".z2");
 			z2 = getConfig().getInt("warps." + area + ".z1");
 		}
 		if (x1 == x2 || z1 == z2) {
 			throw new InvalidCoordinatesException(area);
 		}
 
 		World w = getServer().getWorld(getConfig().getString("warps." + area + ".world"));
 		if (w == null) {
 			throw new InvalidWorldException(getConfig().getString("warps." + area + ".world"));
 		}
 		double x;
 		double z;
 		double y;
 		int attempts = 0;
 		do {
 			if (getConfig().getBoolean("debug")) {
 				getLogger().log(Level.INFO, "Finding random location. Attempt: {0}", attempts+1);
 			}
 			x = x1 + rand.nextInt(x2 - x1) + 0.5d;
 			z = z1 + rand.nextInt(z2 - z1) + 0.5d;
 			y = w.getHighestBlockYAt((int) x, (int) z) + 0.5d;
 		} while (!checkBlock(w.getBlockAt((int) x, (int) y, (int) z))
 				&& attempts++ < getConfig().getInt("attempts"));
 		if (attempts >= getConfig().getInt("attempts") && !getConfig().getBoolean("always-teleport", true)) {
 			throw new NoLocationFoundException(area);
 		}
 		y += getConfig().getInt("drop-height");
 		return new Location(w, x, y, z);
 	}
 	
 	private boolean checkBlock(Block b) {
 		if (getConfig().getBoolean("check-drop", true)) {
 			int droph = getConfig().getInt("drop-height");
 			return checkBlock(b.getRelative(0, droph, 0), droph + 3);
 		} else {
 			return checkBlock(b, 3);
 		}
 	}
 	
 	private boolean checkBlock(Block b, int depth) {
 		for (int i = depth; i > 0; i--) {
 			Block cb = b.getRelative(0, -i, 0);
 			if (!checkSingleBlock(cb)) return false;
 		}
 		return true;
 	}
 
 	private boolean checkSingleBlock(Block b) {
 			if (getConfig().getBoolean("debug")) {
 				getLogger().log(Level.INFO, "Checking a block of type: {0}", b.getType().toString());
 			}
 			if (getConfig().getBoolean("avoid.water", true)
 					&& (b.getType().equals(Material.WATER)
 					|| b.getType().equals(Material.STATIONARY_WATER))) {
 				return false;
 			}
 			if (getConfig().getBoolean("avoid.lava", true)
 					&& (b.getType().equals(Material.LAVA)
 					|| b.getType().equals(Material.STATIONARY_LAVA))) {
 				return false;
 			}
 			if (getConfig().getBoolean("avoid.trees", true)
 					&& b.getType().equals(Material.LEAVES)) {
 				return false;
 			}
 		return true;
 	}
 
 	public Permissions getPermissions() {
 		return permissions;
 	}
 }
