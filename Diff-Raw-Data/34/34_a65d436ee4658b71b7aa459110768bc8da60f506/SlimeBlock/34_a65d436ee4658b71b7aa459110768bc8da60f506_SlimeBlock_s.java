 package mods.nordwest.blocks;
 
 import java.util.Random;
 
 import mods.nordwest.common.CustomBlocks;
 import mods.nordwest.common.NordWest;
 import net.minecraft.block.Block;
 import net.minecraft.block.material.Material;
 import net.minecraft.entity.Entity;
 import net.minecraft.entity.item.EntityFallingSand;
 import net.minecraft.entity.monster.EntitySlime;
 import net.minecraft.util.AxisAlignedBB;
 import net.minecraft.world.World;
 
 public class SlimeBlock extends BaseBlock {
 	public static boolean fallInstantly = false;
 
 	public SlimeBlock(int i) {
 		super(i, Material.ice);
 	}
 
 	@Override
 	public boolean renderAsNormalBlock() {
 		return false;
 	}
 
 	@Override
 	public boolean isOpaqueCube() {
 		return false;
 	}
 
 	@Override
 	public int getRenderBlockPass() {
 		return 1;
 	}
 
 	@Override
 	public AxisAlignedBB getCollisionBoundingBoxFromPool(World world, int x, int y, int z) {
 		float dy = 0.75F;
 		int id = world.getBlockId(x, y - 1, z);
 		AxisAlignedBB box = null;
 		if (id != this.blockID && id != 0)
 			box = AxisAlignedBB.getBoundingBox((double) x, (double) y, (double) z, (double) (x + 1), (double) ((float) (y + 1) - dy), (double) (z + 1));
 		return box;
 		//return null;
 	}

 	@Override
	public void onEntityCollidedWithBlock(World par1World, int i, int j, int k, Entity entity) {
		if (par1World.isRemote)
			return;
		if (entity.motionY < -0.3) {
			entity.setVelocity(entity.motionX * 1.2D, entity.motionY * -1.2D, entity.motionZ * 1.2D);
			entity.fallDistance = 0.0F;
 			entity.playSound("mob.slime.big", 1f, 1f);
 		} else {
 			entity.motionX *= 0.5D;
 			entity.motionY *= 0.5D;
 			entity.motionZ *= 0.5D;
 		}
 
 	}

 	@Override
 	public void onNeighborBlockChange(World world, int i, int j, int k, int l) {
 		if (world.isBlockIndirectlyGettingPowered(i, j, k)) {
 			if (!world.isRemote) {
 				EntitySlime slime = new EntitySlime(world);
 				slime.setLocationAndAngles(i + 0.5D, j + 0.5D, k + 0.5D, NordWest.rand.nextFloat() * 360.0F, 0.0F);
 				world.spawnEntityInWorld(slime);
 				slime.spawnExplosionParticle();
 				world.setBlock(i, j, k, 0);
 			}
 		} else {
 			world.scheduleBlockUpdate(i, j, k, this.blockID, this.tickRate(world));
 		}
 	}
 
 	@Override
 	public void onBlockAdded(World par1World, int par2, int par3, int par4) {
 		par1World.scheduleBlockUpdate(par2, par3, par4, this.blockID, this.tickRate(par1World));
 	}
 
 	@Override
 	public void updateTick(World par1World, int par2, int par3, int par4, Random par5Random) {
 		if (!par1World.isRemote) {
 			this.tryToFall(par1World, par2, par3, par4);
 		}
 	}
 
 	/**
 	 * If there is space to fall below will start this block falling
 	 */
 	private void tryToFall(World par1World, int par2, int par3, int par4) {
 		if (canFallBelow(par1World, par2, par3 - 1, par4) && par3 >= 0) {
 			byte b0 = 32;
 
 			if (!fallInstantly && par1World.checkChunksExist(par2 - b0, par3 - b0, par4 - b0, par2 + b0, par3 + b0, par4 + b0)) {
 				if (!par1World.isRemote) {
 					EntityFallingSand entityfallingskime = new EntityFallingSand(par1World, (double) ((float) par2 + 0.5F), (double) ((float) par3 + 0.5F), (double) ((float) par4 + 0.5F), this.blockID, par1World.getBlockMetadata(par2, par3, par4));
 					this.onStartFalling(entityfallingskime);
 					entityfallingskime.motionY *= 0.5D;
 					par1World.spawnEntityInWorld(entityfallingskime);
 				}
 			} else {
 				par1World.setBlockToAir(par2, par3, par4);
 
 				while (canFallBelow(par1World, par2, par3 - 1, par4) && par3 > 0) {
 					--par3;
 				}
 
 				if (par3 > 0) {
 					par1World.setBlock(par2, par3, par4, this.blockID);
 				}
 			}
 		}
 	}
 
 	/**
 	 * Called when the falling block entity for this block is created
 	 */
 	protected void onStartFalling(EntityFallingSand par1EntityFallingSand) {
 	}
 
 	/**
 	 * How many world ticks before ticking
 	 */
 	@Override
 	public int tickRate(World par1World) {
 		return 2;
 	}
 
 	/**
 	 * Checks to see if the sand can fall into the block below it
 	 */
 	public static boolean canFallBelow(World par0World, int par1, int par2, int par3) {
 		int l = par0World.getBlockId(par1, par2, par3);
 
 		if (l == 0) {
 			return true;
 		} else if (l == Block.fire.blockID) {
 			return true;
 		} else {
 			Material material = Block.blocksList[l].blockMaterial;
 			return material == Material.water ? true : material == Material.lava;
 		}
 	}
 
 	/**
 	 * Called when the falling block entity for this block hits the ground and turns back into a block
 	 */
 	public void onFinishFalling(World par1World, int par2, int par3, int par4, int par5) {
 
 	}
 
 }
