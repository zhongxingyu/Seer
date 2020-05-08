 package com.maienm.FlooNetwork;
 
 import java.util.Arrays;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.OfflinePlayer;
 import org.bukkit.block.Block;
 import org.bukkit.block.BlockFace;
 import org.bukkit.entity.Player;
 import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
 import org.bukkit.material.Sign;
 
 public class Fireplace
 {
 	/**
 	 * The list of materials of which a fireplace may be made.
 	 */
 	private static final Set<Material> ACCEPTED_MATERIALS = new HashSet<Material>(Arrays.asList(
 		new Material[] {Material.BRICK}
 	));
 
 	/**
 	 * The location of the fireplace.
 	 *
 	 * This is the location of the bottom left block.
 	 */
 	private Location location = null;
 
 	/**
 	 * The offsets for the position the bricks should be in as seen from the bottom left block.
 	 *
 	 * First int is the offset in x/z direction, second in in y direction.
 	 */
 	private static final Set<int[]> MATERIAL_OFFSETS = new HashSet<int[]>(Arrays.asList(new int[][] {
 		{0, 0},
 		{0, 1},
 		{0, 2},
 		{1, 2},
 		{1, 3},
 		{2, 2},
 		{2, 3},
 		{3, 2},
 		{3, 1},
 		{3, 0},
 	}));
 
 	/**
 	 * The x direction of the fireplace.
 	 */
 	private int xDirection = 0;
 
 	/**
 	 * The z direction of the fireplace.
 	 */
 	private int zDirection = 0;
 
 	/**
 	 * The owner of the fireplace.
 	 */
 	public OfflinePlayer owner = null;
 
 	/**
 	 * The name of the fireplace.
 	 */
 	public String name = null;
 
 	/**
 	 * Check if a block is a valid fireplace block.
 	 */
 	private static boolean isValidMaterial(Location location)
 	{
 		return ACCEPTED_MATERIALS.contains(location.getBlock().getType());
 	}
 
 	/**
 	 * Check if a there is a fireplace starting from this block.
 	 */
 	private static Fireplace detectAt(Location location, int x, int z)
 	{
 		for (int[] offset : MATERIAL_OFFSETS)
 		{
 			if (!isValidMaterial(location.clone().add(offset[0] * x, offset[1], offset[0] * z)))
 			{
 				return null;
 			}
 		}
 
 		Fireplace fp = new Fireplace();
 		fp.location = location;
 		fp.xDirection = x;
 		fp.zDirection = z;
 		return fp;
 	}
 
 	/**
 	 * Detect a fireplace from a sign.
 	 * 
 	 * Returns a new Fireplace object if one could be found, or null if the block is not part of a fireplace.
 	 */
 	static Fireplace detect(Location location)
 	{
 		Fireplace fp = null;
 
 		// Get the sign data.
 		Block block;
 		Sign sign;
 		try
 		{
 			block = location.getBlock();
 			sign = (Sign) block.getState().getData();
 		}
 		catch (Exception e)
 		{
 			return null;
 		}
 
 		// Determine which block the sign was attached to.
 		Location attached = block.getRelative(sign.getAttachedFace()).getLocation();
 
 		// Detect the fireplace. This is based on the position of the sign.
 		switch (sign.getFacing())
 		{
 			case NORTH:
 				fp = detectAt(attached.clone().add(3, -1, 0), -1, 0);
 				break;
 
 			case EAST:
 				fp = detectAt(attached.clone().add(0, -1, 3), 0, -1);
 				break;
 
 			case SOUTH:
 				fp = detectAt(attached.clone().add(-3, -1, 0), 1, 0);
 				break;
 
 			case WEST:
 				fp = detectAt(attached.clone().add(0, -1, -3), 0, 1);
 				break;
 		}
 
 		return fp;
 	}
 
 	/**
 	 * Check if a given location is part of a fireplace.
 	 *
 	 * @param location The location to check.
 	 * @param includeMaterial Whether to consider the blocks the furnace is made of.
 	 * @param includeSign Whether to consider the sign.
 	 * @param includeAir Whether to consider the air/fire in the furnace.
 	 */
 	public boolean contains(Location location, boolean includeMaterial, boolean includeSign, boolean includeAir)
 	{
 		if (includeMaterial)
 		{
 			for (int[] offset : MATERIAL_OFFSETS)
 			{
 				if (this.location.clone().add(offset[0] * xDirection, offset[1], offset[0] * zDirection).equals(location))
 				{
 					return true;
 				}
 			}
 		}
 
 		if (includeSign)
 		{
 			if (location.equals(getSignLocation()))
 			{
 				return true;
 			}
 		}
 
 		if (includeAir)
 		{
 			Location loc = this.location.clone();
 			if (location.equals(loc.add(xDirection, 0, zDirection)) ||
 			    location.equals(loc.add(0, 1, 0)) ||
 			    location.equals(loc.add(xDirection, -1, zDirection)) ||
 			    location.equals(loc.add(0, 1, 0)))
 			{
 				return true;
 			}
 		}
 
 		return false;
 	}
 
 	/**
 	 * Check whether a fireplace is lighted.
 	 */
 	public boolean isLighted()
 	{
 		Location loc = location.clone();
 		return loc.add(xDirection, 0, zDirection).getBlock().getType() == Material.FIRE &&
 		       loc.add(xDirection, 0, zDirection).getBlock().getType() == Material.FIRE;
 	}
 
 	/**
 	 * Get the location of the sign belonging to this fireplace.
 	 */
 	public Location getSignLocation()
 	{
 		return location.clone().add(xDirection * 3 - zDirection, 1, zDirection * 3 + xDirection);
 	}
 
 	/**
 	 * Warp the player to this fireplace.
 	 */
 	public void warpTo(Player player)
 	{
 		Location loc = location.clone();
 		loc.add(xDirection * 1.5 - zDirection + 0.5, 0, zDirection * 1.5 + xDirection + 0.5);
 		loc.setYaw((float)(xDirection - 1) * 90 + (zDirection == 1 ? 180 : 0));
 		player.teleport(loc, TeleportCause.PLUGIN);
 	}
 
 	/**
 	 * Print important information about a fireplace.
 	 */
 	public String toString()
 	{
 		return String.format("Fireplace %s by %s, spanning from %d, %d, %d to %d, %d, %d (xDirection = %d, zDirection = %d)", 
 			name, owner != null ? owner.getName() : "Unknown",
 			location.getBlockX(), location.getBlockY(), location.getBlockZ(), 
 			location.getBlockX() + xDirection * 3, location.getBlockY(), location.getBlockZ() + zDirection * 3,
 			xDirection, zDirection);
 	}
 }
