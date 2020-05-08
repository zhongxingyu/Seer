 // Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
 // Jad home page: http://www.kpdus.com/jad.html
 // Decompiler options: packimports(3) braces deadcode 
 
 package MCP.mod_finiteliquids;
 
 import java.util.Random;
 
 import MCP.ApiController;
 import MCP.api.BlockBase;
 import net.minecraft.src.*;
 
 // Referenced classes of package net.minecraft.src:
 //            Block, Material, World, mod_NWater
 
 public class BlockMossy extends BlockBase {
 
 	public BlockMossy(ApiController api, int j) {
		super(api.getBlockID(BlockMossyStone.class), j, Material.rock);
 		blockIndexInTexture = j;
 		setTickOnLoad(true);
 		setHardness(0.5F);
 		setResistance(10F);
 		setStepSound(Block.soundStoneFootstep);
 		setBlockName("ncstoneMoss");
 	}
 
 	public int tickRate() {
 		return 15;
 	}
 
 	public void updateTick(World world, int i, int j, int k, Random random) {
 		if (world.getBlockMetadata(i, j, k) == 1) {
 			int l = 0;
 			if (mod_NWater.isWater(world, i, j + 1, k)) {
 				l = 1;
 			}
 			if (mod_NWater.isWater(world, i, j - 1, k)) {
 				l = 1;
 			}
 			if (mod_NWater.isWater(world, i - 1, j, k)) {
 				l = 1;
 			}
 			if (mod_NWater.isWater(world, i + 1, j, k)) {
 				l = 1;
 			}
 			if (mod_NWater.isWater(world, i, j + 1, k)) {
 				l = 1;
 			}
 			if (mod_NWater.isWater(world, i, j + 1, k)) {
 				l = 1;
 			}
 			if (l == 0) {
 				Random random1 = new Random();
 				if (random1.nextInt(60) == 1) {
 					tryMoss(world, i - 1, j, k);
 				}
 				if (random1.nextInt(60) == 1) {
 					tryMoss(world, i + 1, j, k);
 				}
 				if (random1.nextInt(60) == 1) {
 					tryMoss(world, i, j - 1, k);
 				}
 				if (random1.nextInt(60) == 1) {
 					tryMoss(world, i, j + 1, k);
 				}
 				if (random1.nextInt(60) == 1) {
 					tryMoss(world, i, j, k - 1);
 				}
 				if (random1.nextInt(60) == 1) {
 					tryMoss(world, i, j, k + 1);
 				}
 				world.setBlockMetadata(i, j, k, l);
 			} else {
 				world.scheduleBlockUpdate(i, j, k, blockID, tickRate());
 			}
 		}
 	}
 
 	public void tryMoss(World world, int i, int j, int k) {
 		Random random = new Random();
 		if (random.nextInt(20) == 2
 				&& world.getBlockId(i, j, k) == mod_NWater.mossyCobble.blockID) {
 			world.setBlockWithNotify(i, j, k, Block.cobblestoneMossy.blockID);
 		}
 		if (world.getBlockId(i, j, k) == Block.cobblestone.blockID) {
 			world.setBlockWithNotify(i, j, k, mod_NWater.mossyCobble.blockID);
 		}
 		if (world.getBlockId(i, j, k) == Block.stone.blockID) {
 			world.setBlockWithNotify(i, j, k, mod_NWater.mossyStone.blockID);
 		}
 	}
 
 	public void onNeighborBlockChange(World world, int i, int j, int k, int l) {
 		int i1 = 0;
 		if (mod_NWater.isWater(world, i, j + 1, k)) {
 			i1 = 1;
 		}
 		if (mod_NWater.isWater(world, i - 1, j + 1, k)) {
 			i1 = 1;
 		}
 		if (mod_NWater.isWater(world, i + 1, j + 1, k)) {
 			i1 = 1;
 		}
 		if (mod_NWater.isWater(world, i, j + 1, k - 1)) {
 			i1 = 1;
 		}
 		if (mod_NWater.isWater(world, i, j + 1, k + 1)) {
 			i1 = 1;
 		}
 		if (i1 == 1) {
 			world.setBlockMetadata(i, j, k, i1);
 			world.scheduleBlockUpdate(i, j, k, blockID, tickRate());
 		}
 	}
 }
