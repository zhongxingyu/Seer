 /*
  * This file is part of Aqualock.
  *
  * Copyright (c) 2012, AlmuraDev <http://www.almuramc.com/>
  * Aqualock is licensed under the Almura Development License.
  *
  * Aqualock is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * Aqualock is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License. If not,
  * see <http://www.gnu.org/licenses/> for the GNU General Public License.
  */
 package com.almuramc.aqualock.bukkit.util;
 
 import java.util.Collections;
 import java.util.List;
 import java.util.UUID;
 
 import com.almuramc.aqualock.bukkit.AqualockPlugin;
 import com.almuramc.aqualock.bukkit.configuration.AqualockConfiguration;
 import com.almuramc.aqualock.bukkit.lock.BukkitLock;
 import com.almuramc.aqualock.bukkit.lock.DoorBukkitLock;
 import com.almuramc.bolt.lock.Lock;
 import com.almuramc.bolt.registry.CommonRegistry;
 import com.almuramc.bolt.storage.Storage;
 
 import org.getspout.spoutapi.SpoutManager;
 import org.getspout.spoutapi.player.SpoutPlayer;
 
 import org.bukkit.Bukkit;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.World;
 import org.bukkit.block.Block;
 import org.bukkit.block.BlockFace;
 import org.bukkit.entity.Player;
 import org.bukkit.material.Door;
 
 /**
  * The core utility class of Aqualock. Handles common functions between commands/GUI (to spare a LOT of duplicate code) such as lock, unlock, use, and change
  */
 public class LockUtil {
 	private static final CommonRegistry registry;
 	private static final Storage backend;
 	private static final AqualockConfiguration config;
 
 	static {
 		registry = AqualockPlugin.getRegistry();
 		backend = AqualockPlugin.getBackend();
 		config = AqualockPlugin.getConfiguration();
 	}
 
 	/**
 	 * @param playerName
 	 * @param coowners
 	 * @param passcode
 	 * @param location
 	 * @param data
 	 */
 	public static boolean lock(String playerName, List<String> coowners, List<String> users, String passcode, Location location, byte data, double useCost, int damage, long autocloseTimer) {
 		checkLocation(location);
 		final Player player = checkNameAndGetPlayer(playerName);
 		if (coowners == null) {
 			coowners = Collections.emptyList();
 		}
 		if (users == null) {
 			users = Collections.emptyList();
 		}
 		if (!performAction(player, passcode, location, 0, "LOCK")) {
 			return false;
 		}
 		Lock lock;
 		if (BlockUtil.isDoorMaterial(location.getBlock().getType())) {
 			lock = new DoorBukkitLock(playerName, coowners, users, passcode, location, data, useCost, damage, autocloseTimer);
 		} else {
 			lock = new BukkitLock(playerName, coowners, users, passcode, location, data, useCost, damage);
 		}
 		//After all that is said and done, add the lock made to the registry and backend.
 		SpoutManager.getPlayer(player).sendNotification("Aqualock", "Locked the block!", Material.CAKE);
 		registry.addLock(lock);
 		backend.addLock(lock);
 		//If the lock created in this method's location is not the location of this iteration then its the second door, lock it.
 		final Block oBlock = BlockUtil.getDoubleDoor(location);
 		if (oBlock != null) {
 			BlockUtil.changeDoorStates(false, location.getBlock(), oBlock);
 			DoorBukkitLock otherLock = new DoorBukkitLock(playerName, coowners, users, passcode, oBlock.getLocation(), oBlock.getData(), useCost, damage, autocloseTimer);
 			registry.addLock(otherLock);
 			backend.addLock(otherLock);
 			if ((oBlock.getData() & 0x8) == 0x8) {
 				DoorBukkitLock bottomLeft = new DoorBukkitLock(playerName, coowners, users, passcode, location.getBlock().getRelative(BlockFace.DOWN).getLocation(), location.getBlock().getRelative(BlockFace.DOWN).getData(), useCost, damage, autocloseTimer);
 				registry.addLock(bottomLeft);
 				backend.addLock(bottomLeft);
 				DoorBukkitLock bottomRight = new DoorBukkitLock(playerName, coowners, users, passcode, oBlock.getRelative(BlockFace.DOWN).getLocation(), oBlock.getRelative(BlockFace.DOWN).getData(), useCost, damage, autocloseTimer);
 				registry.addLock(bottomRight);
 				backend.addLock(bottomRight);
 			} else {
 				DoorBukkitLock topLeft = new DoorBukkitLock(playerName, coowners, users, passcode, location.getBlock().getRelative(BlockFace.UP).getLocation(), location.getBlock().getRelative(BlockFace.UP).getData(), useCost, damage, autocloseTimer);
 				registry.addLock(topLeft);
 				backend.addLock(topLeft);
 				DoorBukkitLock topRight = new DoorBukkitLock(playerName, coowners, users, passcode, oBlock.getRelative(BlockFace.UP).getLocation(), oBlock.getRelative(BlockFace.UP).getData(), useCost, damage, autocloseTimer);
 				registry.addLock(topRight);
 				backend.addLock(topRight);
 			}
 		//Not a double door...but perhaps a regular door
 		} else if (BlockUtil.isDoorMaterial(location.getBlock().getType())) {
 			Door source = (Door) location.getBlock().getState().getData();
 			if (source.isTopHalf()) {
 				DoorBukkitLock bottom = new DoorBukkitLock(playerName, coowners, users, passcode, location.getBlock().getRelative(BlockFace.DOWN).getLocation(), location.getBlock().getRelative(BlockFace.DOWN).getData(), useCost, damage, autocloseTimer);
 				registry.addLock(bottom);
 				backend.addLock(bottom);
 			} else {
 				DoorBukkitLock top = new DoorBukkitLock(playerName, coowners, users, passcode, location.getBlock().getRelative(BlockFace.UP).getLocation(), location.getBlock().getRelative(BlockFace.UP).getData(), useCost, damage, autocloseTimer);
 				registry.addLock(top);
 				backend.addLock(top);
 			}
 		}
 		return true;
 	}
 
 	/**
 	 * @param playerName
 	 * @param passcode
 	 * @param location
 	 */
 	public static boolean unlock(String playerName, String passcode, Location location) {
 		checkLocation(location);
 		final Player player = checkNameAndGetPlayer(playerName);
 		if (performAction(player, passcode, location, 0, "UNLOCK")) {
 			final Lock lock = registry.getLock(location.getWorld().getUID(), location.getBlockX(), location.getBlockY(), location.getBlockZ());
 			registry.removeLock(lock);
 			backend.removeLock(lock);
 			SpoutManager.getPlayer(player).sendNotification("Aqualock", "Unlocked the block!", Material.CAKE);
 			final Block oBlock = BlockUtil.getDoubleDoor(location);
 			if (oBlock != null) {
 				backend.removeLock(registry.getLock(oBlock.getWorld().getUID(), oBlock.getX(), oBlock.getY(), oBlock.getZ()));
 				registry.removeLock(oBlock.getWorld().getUID(), oBlock.getX(), oBlock.getY(), oBlock.getZ());
 				if ((oBlock.getData() & 0x8) == 0x8) {
 					final Block bottomLeft = location.getBlock().getRelative(BlockFace.DOWN);
 					backend.removeLock(registry.getLock(bottomLeft.getWorld().getUID(), bottomLeft.getX(), bottomLeft.getY(), bottomLeft.getZ()));
 					registry.removeLock(bottomLeft.getWorld().getUID(), bottomLeft.getX(), bottomLeft.getY(), bottomLeft.getZ());
 					final Block bottomRight = oBlock.getRelative(BlockFace.DOWN);
 					backend.removeLock(registry.getLock(bottomRight.getWorld().getUID(), bottomRight.getX(), bottomRight.getY(), bottomRight.getZ()));
 					registry.removeLock(bottomRight.getWorld().getUID(), bottomRight.getX(), bottomRight.getY(), bottomRight.getZ());
 				} else {
 					final Block topLeft = location.getBlock().getRelative(BlockFace.UP);
 					backend.removeLock(registry.getLock(topLeft.getWorld().getUID(), topLeft.getX(), topLeft.getY(), topLeft.getZ()));
 					registry.removeLock(topLeft.getWorld().getUID(), topLeft.getX(), topLeft.getY(), topLeft.getZ());
 					final Block topRight = oBlock.getRelative(BlockFace.UP);
 					backend.removeLock(registry.getLock(topRight.getWorld().getUID(), topRight.getX(), topRight.getY(), topRight.getZ()));
 					registry.removeLock(topRight.getWorld().getUID(), topRight.getX(), topRight.getY(), topRight.getZ());
 				}
 			}  else if (BlockUtil.isDoorMaterial(location.getBlock().getType())) {
 				Door source = (Door) location.getBlock().getState().getData();
 				if (source.isTopHalf()) {
 					final Block bottom = location.getBlock().getRelative(BlockFace.DOWN);
 					backend.removeLock(registry.getLock(bottom.getWorld().getUID(), bottom.getX(), bottom.getY(), bottom.getZ()));
 					registry.removeLock(bottom.getWorld().getUID(), bottom.getX(), bottom.getY(), bottom.getZ());
 				} else {
 					final Block up = location.getBlock().getRelative(BlockFace.UP);
 					backend.removeLock(registry.getLock(up.getWorld().getUID(), up.getX(), up.getY(), up.getZ()));
 					registry.removeLock(up.getWorld().getUID(), up.getX(), up.getY(), up.getZ());
 				}
 			}
 			return true;
 		}
 		return false;
 	}
 
 	/**
 	 * @param playerName
 	 * @param coowners
 	 * @param passcode
 	 * @param location
 	 * @param data
 	 */
 	public static boolean update(String playerName, List<String> coowners, List<String> users, String passcode, Location location, byte data, double useCost, int damage, long timer) {
 		checkLocation(location);
 		Player player = checkNameAndGetPlayer(playerName);
 		if (coowners == null) {
 			coowners = Collections.emptyList();
 		}
 		if (users == null) {
 			users = Collections.emptyList();
 		}
 		if (!performAction(player, passcode, location, useCost, "UPDATE")) {
 			return false;
 		}
 		final Lock lock = registry.getLock(location.getWorld().getUID(), location.getBlockX(), location.getBlockY(), location.getBlockZ());
 		lock.setOwner(playerName);
 		lock.setCoOwners(coowners.toArray(new String[coowners.size()]));
 		lock.setUsers(users.toArray(new String[users.size()]));
 		if (lock instanceof BukkitLock) {
 			((BukkitLock) lock).setUseCost(useCost);
 			((BukkitLock) lock).setPasscode(passcode);
 			((BukkitLock) lock).setData(data);
 			if (lock instanceof DoorBukkitLock) {
 				((DoorBukkitLock) lock).setAutocloseTimer(timer);
 			}
 		}
 		//Update backend and registry
 		registry.addLock(lock);
 		backend.addLock(lock);
 		final Block oBlock = BlockUtil.getDoubleDoor(location);
 		if (oBlock != null) {
 			DoorBukkitLock otherLock = new DoorBukkitLock(playerName, coowners, users, passcode, oBlock.getLocation(), oBlock.getData(), useCost, damage, timer);
 			registry.addLock(otherLock);
 			backend.addLock(otherLock);
 			if ((oBlock.getData() & 0x8) == 0x8) {
 				DoorBukkitLock bottomLeft = new DoorBukkitLock(playerName, coowners, users, passcode, location.getBlock().getRelative(BlockFace.DOWN).getLocation(), location.getBlock().getRelative(BlockFace.DOWN).getData(), useCost, damage, timer);
 				registry.addLock(bottomLeft);
 				backend.addLock(bottomLeft);
 				DoorBukkitLock bottomRight = new DoorBukkitLock(playerName, coowners, users, passcode, oBlock.getRelative(BlockFace.DOWN).getLocation(), oBlock.getRelative(BlockFace.DOWN).getData(), useCost, damage, timer);
 				registry.addLock(bottomRight);
 				backend.addLock(bottomRight);
 			} else {
 				DoorBukkitLock topLeft = new DoorBukkitLock(playerName, coowners, users, passcode, location.getBlock().getRelative(BlockFace.UP).getLocation(), location.getBlock().getRelative(BlockFace.UP).getData(), useCost, damage, timer);
 				registry.addLock(topLeft);
 				backend.addLock(topLeft);
 				DoorBukkitLock topRight = new DoorBukkitLock(playerName, coowners, users, passcode, oBlock.getRelative(BlockFace.UP).getLocation(), oBlock.getRelative(BlockFace.UP).getData(), useCost, damage, timer);
 				registry.addLock(topRight);
 				backend.addLock(topRight);
 			}
 		} else if (BlockUtil.isDoorMaterial(location.getBlock().getType())) {
 			Door source = (Door) location.getBlock().getState().getData();
 			if (source.isTopHalf()) {
 				DoorBukkitLock bottom = new DoorBukkitLock(playerName, coowners, users, passcode, location.getBlock().getRelative(BlockFace.DOWN).getLocation(), location.getBlock().getRelative(BlockFace.DOWN).getData(), useCost, damage, timer);
 				registry.addLock(bottom);
 				backend.addLock(bottom);
 			} else {
 				DoorBukkitLock top = new DoorBukkitLock(playerName, coowners, users, passcode, location.getBlock().getRelative(BlockFace.UP).getLocation(), location.getBlock().getRelative(BlockFace.UP).getData(), useCost, damage, timer);
 				registry.addLock(top);
 				backend.addLock(top);
 			}
 		}
 		SpoutManager.getPlayer(player).sendNotification("Aqualock", "Updated the block!", Material.CAKE);
 		return true;
 	}
 
 	public static boolean use(String playerName, String passcode, Location location, double useCost) {
 		checkLocation(location);
 		final Player player = checkNameAndGetPlayer(playerName);
 		if (!performAction(player, passcode, location, useCost, "USE")) {
 			return false;
 		}
 		SpoutManager.getPlayer(player).sendNotification("Aqualock", "Used the block!", Material.CAKE);
 		return true;
 	}
 
 	public static boolean performAction(Player player, String passcode, Location location, double useCost, String action) {
 		final World world = location.getWorld();
 		final UUID worldIdentifier = world.getUID();
 		final int x = location.getBlockX();
 		final int y = location.getBlockY();
 		final int z = location.getBlockZ();
 		final Lock lock = registry.getLock(worldIdentifier, x, y, z);
 		final String name = player.getName();
 		final SpoutPlayer splayer = SpoutManager.getPlayer(player);
 		switch (action) {
 			case "LOCK":
 				if (lock != null) {
 					splayer.sendNotification("Aqualock", "This location has a lock!", Material.LAVA_BUCKET);
 					return false;
 				}
 				if (AqualockPlugin.getEconomies() != null) {
 					if (EconomyUtil.shouldChargeForLock(player)) {
 						if (!EconomyUtil.hasAccount(player)) {
 							splayer.sendNotification("Aqualock", "You have no account!", Material.LAVA_BUCKET);
 							return false;
 						}
 						final double value = config.getCosts().getLockCost(location.getBlock().getType());
 						if (!EconomyUtil.hasEnough(player, value)) {
 							splayer.sendNotification("Aqualock", "Not enough money!", Material.LAVA_BUCKET);
 							return false;
 						}
 						if (value > 0) {
 							splayer.sendNotification("Aqualock", "Charged for lock: " + value, Material.POTION);
 						} else if (value < 0) {
 							splayer.sendNotification("Aqualock", "Received for lock: " + value, Material.CAKE);
 						} else {
 							splayer.sendNotification("Aqualock", "Lock was free!", Material.APPLE);
 						}
 						EconomyUtil.apply(player, value);
 					}
 				}
 				return true;
 			case "UNLOCK":
 				if (lock == null) {
 					splayer.sendNotification("Aqualock", "No lock at location!", Material.POTION);
 					return true;
 				}
 				boolean canUnlock = false;
 				if (!name.equals(lock.getOwner()) && !canPerformAction(player, "UNLOCK")) {
 					for (String pname : lock.getCoOwners()) {
 						if (pname.equals(name)) {
 							if (lock instanceof BukkitLock && (!((BukkitLock) lock).getPasscode().equals(passcode))) {
 								splayer.sendNotification("Aqualock", "Invalid password!", Material.LAVA_BUCKET);
 								return false;
 							}
 							canUnlock = true;
 						}
 					}
 				} else {
 					canUnlock = true;
 				}
 				if (!canUnlock) {
 					splayer.sendNotification("Aqualock", "Cannot unlock the lock!", Material.LAVA_BUCKET);
 					return false;
 				}
 				if (AqualockPlugin.getEconomies() != null) {
 					if (EconomyUtil.shouldChargeForUnlock(player)) {
 						if (!EconomyUtil.hasAccount(player)) {
 							splayer.sendNotification("Aqualock", "You have no account!", Material.LAVA_BUCKET);
 							return false;
 						}
 						final double value = config.getCosts().getUnlockCost(location.getBlock().getType());
 						if (!EconomyUtil.hasEnough(player, value)) {
 							splayer.sendNotification("Aqualock", "Not enough money!", Material.LAVA_BUCKET);
 							return false;
 						}
 						if (value > 0) {
 							splayer.sendNotification("Aqualock", "Charged for unlock: " + value, Material.POTION);
 						} else if (value < 0) {
 							splayer.sendNotification("Aqualock", "Received for unlock: " + value, Material.CAKE);
 						} else {
 							splayer.sendNotification("Aqualock", "Unlock was free!", Material.APPLE);
 						}
 						EconomyUtil.apply(player, value);
 					}
 				}
 				return true;
 			case "USE":
 				if (lock == null) {
 					return true;
 				}
 				if (!name.equals(lock.getOwner()) && !canPerformAction(player, "USE")) {
					for (String pname : lock.getCoOwners()) {
						if (pname.equals(name)) {
 							if (lock instanceof BukkitLock && (!((BukkitLock) lock).getPasscode().equals(passcode))) {
 								splayer.sendNotification("Aqualock", "Invalid password!", Material.LAVA_BUCKET);
 								return false;
 							}
 						}
 					}
 				}
 				if (AqualockPlugin.getEconomies() != null) {
 					if (EconomyUtil.shouldChargeForUse(player)) {
 						if (!EconomyUtil.hasAccount(player)) {
 							splayer.sendNotification("Aqualock", "You have no account!", Material.LAVA_BUCKET);
 							return false;
 						}
 						if (!EconomyUtil.hasEnough(player, useCost)) {
 							splayer.sendNotification("Aqualock", "Not enough money!", Material.LAVA_BUCKET);
 							return false;
 						}
 						if (useCost > 0) {
 							splayer.sendNotification("Aqualock", "Charged for use: " + useCost, Material.POTION);
 						} else if (useCost < 0) {
 							splayer.sendNotification("Aqualock", "Received for use: " + useCost, Material.CAKE);
 						} else {
 							splayer.sendNotification("Aqualock", "Use was free!", Material.APPLE);
 						}
 						EconomyUtil.apply(player, useCost);
 					}
 				}
 				return true;
 			case "UPDATE":
 				if (lock == null) {
 					return true;
 				}
 				boolean canUpdate = false;
 				if (!name.equals(lock.getOwner()) && !canPerformAction(player, "UPDATE")) {
 					for (String pname : lock.getCoOwners()) {
 						if (name.equals(pname)) {
 							canUpdate = true;
 						}
 					}
 				} else {
 					canUpdate = true;
 				}
 				if (!canUpdate) {
 					splayer.sendNotification("Aqualock", "Not the Owner/CoOwner!", Material.LAVA_BUCKET);
 					return false;
 				}
 				if (AqualockPlugin.getEconomies() != null) {
 					if (EconomyUtil.shouldChargeForUpdate(player)) {
 						if (!EconomyUtil.hasAccount(player)) {
 							splayer.sendNotification("Aqualock", "You have no account!", Material.LAVA_BUCKET);
 							return false;
 						}
 						final double value = config.getCosts().getUpdateCost(location.getBlock().getType());
 						if (!EconomyUtil.hasEnough(player, value)) {
 							splayer.sendNotification("Aqualock", "Not enough money!", Material.LAVA_BUCKET);
 							return false;
 						}
 						if (value > 0) {
 							splayer.sendNotification("Aqualock", "Charged for update: " + value, Material.POTION);
 						} else if (value < 0) {
 							splayer.sendNotification("Aqualock", "Received for update: " + value, Material.CAKE);
 						} else {
 							splayer.sendNotification("Aqualock", "Update was free!", Material.APPLE);
 						}
 						EconomyUtil.apply(player, value);
 					}
 				}
 		}
 		return true;
 	}
 
 	public static boolean canPerformAction(Player player, String action) {
 		//Determine if they have basic perms for the action defined
 		switch (action) {
 			case "LOCK":
 				if (!PermissionUtil.canLock(player)) {
 					return false;
 				}
 				break;
 			case "UNLOCK":
 				if (!PermissionUtil.canUnlock(player)) {
 					return false;
 				}
 				break;
 			case "USE":
 				if (!PermissionUtil.canUse(player)) {
 					return false;
 				}
 				break;
 			case "UPDATE":
 				if (!PermissionUtil.canUse(player)) {
 					return false;
 				}
 				break;
 			default:
 				return false;
 		}
 		return true;
 	}
 
 	////////////////////////////////////////////////////////////////////////////////////////////////////
 	//////////////////////////////////////Private helpers//////////////////////////////////////////////
 	//////////////////////////////////////////////////////////////////////////////////////////////////
 
 	private static void checkLocation(Location location) {
 		if (location == null) {
 			throw new IllegalArgumentException("Location cannot be null!");
 		}
 	}
 
 	private static Player checkNameAndGetPlayer(String name) {
 		if (name == null || name.isEmpty()) {
 			throw new IllegalArgumentException("Name cannot be null or empty!");
 		}
 		Player player = Bukkit.getPlayerExact(name);
 		if (player == null) {
 			throw new IllegalArgumentException("No player found matching name: " + name + " found on this server!");
 		}
 		return player;
 	}
 }
