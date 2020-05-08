 package net.minecraft.src;
 
 import net.minecraft.src.*;
  import net.minecraft.src.forge.*;
 public class mod_Tea extends BaseMod {
 	/**
      *Block.java 
      */
	public static final Block strBerryBush = (BlockFlower) (new BlockFlower(204, 184)).setHardness(0.0F).setStepSound(Block.soundGrassFootstep).setBlockName("strawberry bush");
    public static final Block camellia = (BlockFlower) (new BlockFlower(205, 185)).setHardness(0.0F).setStepSound(Block.soundGrassFootstep).setBlockName("camellia sinensis");
    public static final Block mint = (BlockFlower) (new BlockFlower(206, 186)).setHardness(0.0F).setStepSound(Block.soundGrassFootstep).setBlockName("mint");
    public static final Block chamomilla = (BlockFlower) (new BlockFlower(207, 187)).setHardness(0.0F).setStepSound(Block.soundGrassFootstep).setBlockName("matricaria chamomilla");
 	public static final Block dehydrator = new BlockDehydrator(180, 0).setHardness(2.0F).setResistance(5.0F).setBlockName("dehydrator"); 
 
 	public mod_Tea() {
 		
 	}
 
 	@Override
 	public void load() {
 		/**
 		 *Load textures 
 		 */
 		MinecraftForgeClient.preloadTexture("/TeaMod/terrain.png");
 		/**
 		 * Register
 		 */
 		ModLoader.registerBlock(dehydrator);
 		ModLoader.registerBlock(strBerryBush);
 		ModLoader.registerBlock(camellia);
 		ModLoader.registerBlock(mint);
 		ModLoader.registerBlock(chamomilla);
 		
 		/**
 		 * Names
 		 */
 		ModLoader.addName(dehydrator, "Dehydrator");
 		ModLoader.addName(strBerryBush, "Straw Berry Bush");
 		ModLoader.addName(camellia, "Camellia Sinensis");
 		ModLoader.addName(mint, "Mintr");
 		ModLoader.addName(chamomilla, "Matricaria Chamomilla");
 		/**
 		 * Crafting
 		 */
 		ModLoader.addRecipe(new ItemStack(dehydrator, 1), new Object[] { "#$#", "#$#", "$%$",
 				'#', Block.glass, '$', Item.ingotIron, '%', Item.redstone  });
 		ModLoader.addRecipe(new ItemStack(camellia, 1), new Object[] { "#", "#",
 			'#', Block.dirt  });
 	}
 	
 	@Override
 	public String getVersion() {
 		return "1.2.5";
 	}
 }
