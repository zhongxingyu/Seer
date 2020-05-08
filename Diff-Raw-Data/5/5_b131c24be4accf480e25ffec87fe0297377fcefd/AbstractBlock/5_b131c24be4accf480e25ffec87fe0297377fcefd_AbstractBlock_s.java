 /*
  * This file is part of slopes.
  *
  * Copyright (c) 2012, Tim Almdal <http://www.timalmdal.com/>
  * slopes is licensed under the GNU Lesser General Public License.
  * This version of slopes is derived from Kaevator's Superslopes (http://goo.gl/Rd7io)
  * and retsrif's original Spout port (https://github.com/retsrif/Slopes)
  *
  * slopes is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * slopes is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package com.timalmdal.bukkit.slopes.blocks;
 
 import org.bukkit.Bukkit;
 import org.bukkit.World;
 import org.bukkit.block.Block;
 import org.bukkit.block.BlockFace;
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.material.Directional;
 import org.bukkit.plugin.Plugin;
 import org.getspout.spoutapi.block.design.Texture;
 import org.getspout.spoutapi.material.block.GenericCustomBlock;
 
 import com.timalmdal.bukkit.slopes.SlopesPlugin;
 import com.timalmdal.bukkit.slopes.designers.SlopeBlockDesign;
 import com.timalmdal.bukkit.slopes.util.QuadList;
 import com.timalmdal.bukkit.slopes.util.SlopeSubTexture;
 import com.timalmdal.bukkit.slopes.util.Utilities;
 
 public abstract class AbstractBlock extends GenericCustomBlock implements Directional {
 
 	private BlockFace blockFacing;
 
 	public AbstractBlock(final Plugin plugin, final String displayName, final QuadList quadList, final Texture texture,
 			final SlopeSubTexture slopeTexture) {
 		this(plugin, displayName, quadList, texture, slopeTexture, true);
 	}
 
 	public AbstractBlock(final Plugin plugin, final String displayName, final QuadList quadList, final Texture texture,
 			final SlopeSubTexture slopeTexture, final boolean rotate) {
 		super(plugin, displayName, 67, 0x00, new SlopeBlockDesign(plugin, texture, slopeTexture, quadList), rotate);
 		setFacingDirection(BlockFace.SOUTH);
 
 	}
 
 	/**
 	 * Derived classes must override this to allow the plugin to extract the
 	 * recipe.
 	 * 
 	 * @return
 	 */
 	public abstract String[] getRecipe();
 
 	/**
	 * @return the direction the stairs ascend towards
 	 */
 	public BlockFace getDescendingDirection() {
 		return blockFacing;
 	}
 
 	/**
	 * @return the direction the stairs descend towards
 	 */
 	public BlockFace getAscendingDirection() {
 		return getDescendingDirection().getOppositeFace();
 	}
 
 	@Override
 	public void setFacingDirection(final BlockFace face) {
 		blockFacing = face;
 	}
 
 	@Override
 	public BlockFace getFacing() {
 		return blockFacing;
 	}
 
 	@Override
 	public void onBlockPlace(final World world, final int x, final int y, final int z, final LivingEntity living) {
 		final BlockFace playerFacing = Utilities.yawToFace(living.getLocation().getYaw());
 		setFacingDirection(playerFacing.getOppositeFace());
 	}
 
 	@Override
 	public void onBlockPlace(final World world, final int x, final int y, final int z) {
 		final Block block = world.getBlockAt(x, y, z);
 		final byte ascendingDirection = Utilities.getNotchianAscendingDirection(getAscendingDirection());
 
 		Bukkit.getScheduler().scheduleSyncDelayedTask(SlopesPlugin.getPlugin(), new Runnable() {
 			@Override
 			public void run() {
 				block.setData((byte) ((block.getData() & ~0x03) | ascendingDirection));
 			}
 		});
 	}
 
 	@Override
 	public String toString() {
 		return new StringBuilder().append(getName()).append("[").append("Facing: ").append(blockFacing).append("; ").append(super.toString()).append("]").toString();
 	}
 
 }
