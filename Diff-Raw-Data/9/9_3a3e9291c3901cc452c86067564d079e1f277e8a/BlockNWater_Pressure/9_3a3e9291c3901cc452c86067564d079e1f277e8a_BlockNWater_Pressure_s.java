 // Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
 // Jad home page: http://www.kpdus.com/jad.html
 // Decompiler options: packimports(3) braces deadcode 
 
 package MCP.mod_finiteliquids;
 
 import java.util.Random;
 
 import MCP.ApiController;
 import MCP.api.BlockBase;
 import net.minecraft.src.*;
 
 // Referenced classes of package net.minecraft.src:
 //            Block, mod_NWater, IBlockAccess, World, 
 //            Material, Entity, Vec3D, AxisAlignedBB
 
 public class BlockNWater_Pressure extends BlockBase {
 
 	protected BlockNWater_Pressure(ApiController api, int j, Material material)
     {
		super(api.getBlockID(BlockNWater_Still.class), j, material);
 		blockIndexInTexture = Block.waterStill.blockIndexInTexture;
 		float f = 0.0F;
 		setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 0.0725F, 1.0F);
 		blockResistance = 100F;
 		setHardness(0.3F);
 		setLightOpacity(3);
 		setStepSound(Block.soundGlassFootstep);
 		setBlockName("nwater_pressure");
 	}
 
 	public int tickRate() {
 		return 10;
 	}
 
 	public int getBlockTexture(IBlockAccess iblockaccess, int i, int j, int k,
 			int l) {
 		return mod_NWater.texx[5];
 	}
 
 	public void velocityToAddToEntity(World world, int i, int j, int k,
 			Entity entity, Vec3D vec3d) {
 	}
 
 	public int getBlockTextureFromSide(int i) {
 		return mod_NWater.texx[5];
 	}
 
 	public int idDropped(int i, Random random) {
 		return 0;
 	}
 
 	public boolean canBlockBePlacedAt(int i, int j, int k, int l, boolean flag) {
 		return true;
 	}
 
 	public int quantityDropped(Random random) {
 		return 0;
 	}
 
 	public boolean isOpaqueCube() {
 		return false;
 	}
 
 	public boolean renderAsNormalBlock() {
 		return false;
 	}
 
 	public float getBlockBrightness(IBlockAccess iblockaccess, int i, int j,
 			int k) {
 		float f = iblockaccess.getLightBrightness(i, j, k);
 		float f1 = iblockaccess.getLightBrightness(i, j + 1, k);
 		return f > f1 ? f : f1;
 	}
 
 	public int getRenderBlockPass() {
 		return 1;
 	}
 
 	public boolean canPlaceBlockAt(World world, int i, int j, int k) {
 		return true;
 	}
 
 	public void onBlockAdded(World world, int i, int j, int k) {
 		world.scheduleBlockUpdate(i, j, k, blockID, tickRate());
 	}
 
 	public void updateTick(World world, int i, int j, int k, Random random) {
 		if (world.getBlockMetadata(i, j, k) > 1) {
 			if (doMoveWater(world, i, j, k)) {
 				world.scheduleBlockUpdate(i, j, k, blockID, tickRate());
 			}
 		} else {
 			world.setBlockAndMetadataWithNotify(i, j, k, getMoving(), 15);
 		}
 	}
 
 	public boolean doMoveWater(World world, int i, int j, int k) {
 		boolean flag = false;
 		if (moveWater(world, i, j - 1, k, i, j, k)) {
 			flag = true;
 		}
 		if (moveWater(world, i - 1, j, k, i, j, k)) {
 			flag = true;
 		}
 		if (moveWater(world, i + 1, j, k, i, j, k)) {
 			flag = true;
 		}
 		if (moveWater(world, i, j, k - 1, i, j, k)) {
 			flag = true;
 		}
 		if (moveWater(world, i, j, k + 1, i, j, k)) {
 			flag = true;
 		}
 		if (moveWater(world, i, j + 1, k, i, j, k)) {
 			flag = true;
 		}
 		return flag;
 	}
 
 	public boolean canMove(World world, int i, int j, int k) {
 		int l = world.getBlockId(i, j, k);
 		int i1 = world.getBlockMetadata(i, j, k);
 		if (l == 0) {
 			return true;
 		}
 		return (l == getMoving() || l == getStill()) & (i1 < 8);
 	}
 
 	public boolean moveWater(World world, int i, int j, int k, int l, int i1,
 			int j1) {
 		int k1 = world.getBlockId(i, j, k);
 		int l1 = world.getBlockMetadata(i, j, k);
 		int i2 = world.getBlockMetadata(l, i1, j1);
 		if (i2 < 1) {
 			return false;
 		}
 		if (k1 == blockID) {
 			byte byte0 = 1;
 			if (j - i1 == -1) {
 				byte0 = 2;
 			}
 			if (j - i1 == 1) {
 				byte0 = 2;
 			}
 			if ((l1 < 15) & (l1 + byte0 < i2)) {
 				l1++;
 				world.setBlockMetadataWithNotify(i, j, k, l1);
 				world.setBlockMetadata(l, i1, j1, i2 - 1);
 				return true;
 			}
 		}
 		if (k1 == 0) {
 			world.setBlockAndMetadataWithNotify(i, j, k, getMoving(), 3);
 			world.setBlockMetadata(l, i1, j1, i2 - 1);
 			return true;
 		}
 		if (k1 == getMoving() || k1 == getStill()) {
 			if (l1 < 15) {
 				l1 += 7;
 				if (l1 > 15) {
 					l1 = 15;
 				}
 				world.setBlockAndMetadataWithNotify(i, j, k, getMoving(), l1);
 				world.setBlockMetadata(l, i1, j1, i2 - 1);
 				world.markBlocksDirty(i, j, k, i, j, k);
 				return true;
 			}
 			if (i2 > 0) {
 				world.setBlockAndMetadataWithNotify(i, j, k, blockID, 1);
 				world.setBlockMetadata(l, i1, j1, i2 - 1);
 				return true;
 			}
 		}
 		return false;
 	}
 
 	public boolean canCollideCheck(int i, boolean flag) {
 		return (i > 4) & flag;
 	}
 
 	public boolean checkSponge(World world, int i, int j, int k) {
 		return (world.getBlockId(i, j, k) == Block.sponge.blockID)
 				& (world.getBlockMetadata(i, j, k) < 15);
 	}
 
 	public boolean isWater(World world, int i, int j, int k) {
 		if (world.getBlockId(i, j, k) == mod_NWater.nwater.blockID) {
 			return true;
 		}
 		if (world.getBlockId(i, j, k) == mod_NWater.nwater_still.blockID) {
 			return true;
 		}
 		if (world.getBlockId(i, j, k) == mod_NWater.nlava.blockID) {
 			return true;
 		}
 		if (world.getBlockId(i, j, k) == mod_NWater.nlava_still.blockID) {
 			return true;
 		}
 		return (world.getBlockId(i, j, k) == mod_NWater.grate.blockID)
 				& (world.getBlockMetadata(i, j, k) != 10);
 	}
 
 	public void onEntityCollidedWithBlock(World world, int i, int j, int k,
 			Entity entity) {
 		if (world.multiplayerWorld) {
 			return;
 		} else {
 			mod_NWater.boilWater(world, i, j, k, entity);
 			return;
 		}
 	}
 
 	public void onNeighborBlockChange(World world, int i, int j, int k, int l) {
 		world.scheduleBlockUpdate(i, j, k, blockID, tickRate());
 	}
 
 	public int getMoving() {
 		return mod_NWater.nwater.blockID;
 	}
 
 	public int getStill() {
 		return mod_NWater.nwater_still.blockID;
 	}
 
 	public AxisAlignedBB getCollisionBoundingBoxFromPool(World world, int i,
 			int j, int k) {
 		if (world.multiplayerWorld) {
 			return null;
 		}
 		if (world.getBlockId(i, j + 1, k) != 0) {
 			setBlockBounds(-0.01F, 0.01F, 0.01F, 1.01F, 1.01F, 1.01F);
 		}
 		return null;
 	}
 
 	public void randomDisplayTick(World world, int i, int j, int k,
 			Random random) {
 		if (random.nextInt(4) == 1 && mod_NWater.isBoiling(world, i, j, k)) {
 			world.spawnParticle("bubble", (float) i + random.nextFloat(),
 					(float) j + random.nextFloat(),
 					(float) k + random.nextFloat(),
 					(random.nextFloat() - 0.5F) / 10F, random.nextFloat() / 5F,
 					(random.nextFloat() - 0.5F) / 10F);
 		}
 		if (blockMaterial == Material.water) {
 			if (random.nextInt(64) != 0)
 				;
 		}
 	}
 
 	public void onBlockRemoval(World world, int i, int j, int k) {
 		int l = world.getBlockMetadata(i, j, k);
 		if (l > 0) {
 			world.notifyBlocksOfNeighborChange(i, j, k, blockID);
 			world.notifyBlocksOfNeighborChange(i, j - 1, k, blockID);
 		}
 		super.onBlockRemoval(world, i, j, k);
 	}
 
 	public void setBlockBoundsBasedOnState(IBlockAccess iblockaccess, int i,
 			int j, int k) {
 		setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
 	}
 
 	public boolean isPoweringTo(IBlockAccess iblockaccess, int i, int j, int k,
 			int l) {
 		return false;
 	}
 
 	public boolean isIndirectlyPoweringTo(World world, int i, int j, int k,
 			int l) {
 		return false;
 	}
 
 	public boolean canProvidePower() {
 		return false;
 	}
 
 	public void setBlockBoundsForItemRender() {
 		float f = 0.5F;
 		float f1 = 0.125F;
 		float f2 = 0.5F;
 		setBlockBounds(0.5F - f, 0.5F - f1, 0.5F - f2, 0.5F + f, 0.5F + f1,
 				0.5F + f2);
 	}
 }
