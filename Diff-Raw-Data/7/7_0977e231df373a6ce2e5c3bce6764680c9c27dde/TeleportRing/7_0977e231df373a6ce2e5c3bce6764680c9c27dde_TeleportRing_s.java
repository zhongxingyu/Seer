 package hunternif.mc.rings.item;
 
 import hunternif.mc.rings.RingsOfPower;
 import hunternif.mc.rings.util.BlockUtil;
 import hunternif.mc.rings.util.SideHit;
 
 import java.util.logging.Level;
 
 import net.minecraft.block.Block;
 import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.item.ItemStack;
 import net.minecraft.util.MovingObjectPosition;
 import net.minecraft.util.Vec3;
 import net.minecraft.world.World;
 import cpw.mods.fml.common.FMLLog;
 
 public class TeleportRing extends PoweredRing {
 	public TeleportRing(int id) {
 		super(id);
 	}
 	
 	@Override
 	public ItemStack onItemRightClick(ItemStack itemStack, World world, EntityPlayer player) {
 		if (!player.capabilities.isCreativeMode && !hasFuel(itemStack, player)) {
 			FMLLog.log(RingsOfPower.ID, Level.INFO, "No fuel in inventory!");
 			return itemStack;
 		}
 		int destX;
 		int destY;
 		int destZ;
		double maxDistance = (double)(256 >> Minecraft.getMinecraft().gameSettings.renderDistance);
 		boolean tpComplete = false;
 		while (!tpComplete && maxDistance > 1) {
 			// Allow landing on all solid and liquid blocks, but the latter only if the player is not in water
 			Vec3 position = world.getWorldVec3Pool().getVecFromPool(player.posX, player.posY, player.posZ);
 			if (!world.isRemote) {
 				// Because in server worlds the Y coordinate of a player is his feet's coordinate, without yOffset.
 				position = position.addVector(0, 1.62D, 0);
 			}
 			Vec3 look = player.getLook(1.0F);
 			Vec3 lookFar = position.addVector(look.xCoord * maxDistance, look.yCoord * maxDistance, look.zCoord * maxDistance);
 			boolean isUnderWater = player.isInsideOfMaterial(Material.water);
 			MovingObjectPosition hit = world.clip(position, lookFar, !isUnderWater); // raytrace
 			
 			if (hit != null) {
 				destX = hit.blockX;
 				destY = hit.blockY;
 				destZ = hit.blockZ;
 				
 				// Only teleport on top when there's a block of air 1 block above
 				// target block AND it's reachable straight,
 				// like this:       or this:  0
 				//             0             00
 				//            00             0#
 				//    ray ->  0#     ray ->  0#
 				// (0 = air, # = block)
 				
 				if (hit.sideHit != SideHit.BOTTOM && hit.sideHit != SideHit.TOP) {
 					if (BlockUtil.isReachableAirAbove(world, hit.sideHit, destX, destY, destZ, 1)) {
 						// Blink on top of that block
 						destY += 1;
 					} else if (BlockUtil.isReachableAirAbove(world, hit.sideHit, destX, destY, destZ, 2)) {
 						// ...or the one above it
 						destY += 2;
 					} else {
 						// There's no reachable air above, move back 1 block
 						switch (hit.sideHit) {
 						case SideHit.NORTH:
 							destX--;
 							break;
 						case SideHit.SOUTH:
 							destX++;
 							break;
 						case SideHit.EAST:
 							destZ--;
 							break;
 						case SideHit.WEST:
 							destZ++;
 							break;
 						}
 					}
 				} else {
 					switch (hit.sideHit) {
 					case SideHit.BOTTOM:
 						destY -= 2;
 						break;
 					case SideHit.TOP:
 						destY++;
 						break;
 					}
 				}
 				// Safeguard in case of an infinite loop.
 				int timesSubtracted = 0;
 				while (!teleport(itemStack, world, player, destX, destY, destZ) &&
 						(double) timesSubtracted < maxDistance) {
 					// Something is obstructing the ray, trace a step back
 					hit.hitVec = hit.hitVec.addVector(-look.xCoord, -look.yCoord, -look.zCoord);
 					timesSubtracted ++;
 					destX = Math.round((float) hit.hitVec.xCoord);
 					destY = Math.round((float) hit.hitVec.yCoord);
 					destZ = Math.round((float) hit.hitVec.zCoord);
 				}
 				tpComplete = true;
 			} else {
 				destX = Math.round((float) lookFar.xCoord);
 				destY = Math.round((float) lookFar.yCoord);
 				destZ = Math.round((float) lookFar.zCoord);
 				if ( teleport(itemStack, world, player, destX, destY, destZ) ) {
 					tpComplete = true;
 				} else {
 					maxDistance /= 2;
 				}
 			}
 		}
 		return itemStack;
 	}
 	
 	/**
 	 * Teleports player to target coordinates, but first looks up then down for
 	 * empty space. Returns true if could teleport without moving the player UP
 	 * from the given destination. Otherwise returns false and doesn't teleport.
 	 */
 	private boolean teleport(ItemStack itemStack, World world, EntityPlayer player, int x, int y, int z) {
 		// First of all, check if we are hanging in the air; if so, land.
 		Material material = world.getBlockMaterial(x, y-1, z);
 		boolean isUnderWater = player.isInsideOfMaterial(Material.water);
 		while (!(material.isSolid() || (!isUnderWater && material.isLiquid()))) {
 			y--;
 			material = world.getBlockMaterial(x, y-1, z);
 			if (y <= 0) {
 				// Reached minimum Y
 				return false;
 			}
 		}
 		double destX = (double) x + 0.5D;
 		double destY = (double) y + player.yOffset;
 		double destZ = (double) z + 0.5D;
 		
 		// Special care must be taken with fences which are 1.5 blocks high
 		int landingBlockId = world.getBlockId(x, y-1, z);
 		if (landingBlockId == Block.fence.blockID || landingBlockId == Block.fenceGate.blockID) {
 			destY += 0.5;
 		}
 		// Also slabs
 		if (landingBlockId == Block.stoneSingleSlab.blockID || landingBlockId == Block.woodSingleSlab.blockID) {
 			destY -= 0.5;
 		}
 		
 		// Keep previous coordinates
 		double srcX = player.posX;
 		double srcY = player.posY;
 		double srcZ = player.posZ;
 		
 		player.setPosition(destX, destY, destZ);
 		
 		// If colliding with something right now, return false immediately:
 		if (!world.getCollidingBoundingBoxes(player, player.boundingBox).isEmpty()) {
 			// Reset player position
 			player.setPosition(srcX, srcY, srcZ);
 			return false;
 		}
 		
 		//------------------------ Successful teleport ------------------------
 		
 		if (!player.capabilities.isCreativeMode) {
 			consumeFuel(itemStack, player);
 		}
 		player.motionX = 0;
 		player.motionY = 0;
 		player.motionZ = 0;
 		player.fallDistance = 0;
 		
 		return true;
 	}
 
 }
