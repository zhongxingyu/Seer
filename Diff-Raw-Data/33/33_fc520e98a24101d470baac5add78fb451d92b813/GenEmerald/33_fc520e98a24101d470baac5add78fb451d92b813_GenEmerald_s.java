 package org.yogpstop.tof;
 
 import java.util.Random;
 
 import net.minecraft.src.BiomeGenHills;
 import net.minecraft.src.Block;
import net.minecraft.src.MLProp;
 import net.minecraft.src.World;
 import net.minecraft.src.WorldGenMinable;
 
 public class GenEmerald extends BiomeGenHills {
 	protected GenEmerald(int par1) {
 		super(par1);
 	}
 
	private static int BlockID = Block.oreEmerald.blockID;

 	public static void load(int maxBlocks) {
		WgenEmerald = new WorldGenMinable(BlockID, maxBlocks);
 	}
 
 	@Override
 	public void decorate(World world, Random random, int i, int j) {
 		super.decorate(world, random, i, j);
 
 		for (int l = 0; l < org.yogpstop.tof.mod_TimesOreForge.EmaxLumps; l++) {
 			int ia = (i - (i % 16)) + random.nextInt(16);
 			int ja = random
 					.nextInt(org.yogpstop.tof.mod_TimesOreForge.EmaxHeight + 1);
 			int ka = (j - (j % 16)) + random.nextInt(16);
 			WgenEmerald.generate(world, random, ia, ja, ka);
 		}
 	}
 
 	static WorldGenMinable WgenEmerald;
 }
