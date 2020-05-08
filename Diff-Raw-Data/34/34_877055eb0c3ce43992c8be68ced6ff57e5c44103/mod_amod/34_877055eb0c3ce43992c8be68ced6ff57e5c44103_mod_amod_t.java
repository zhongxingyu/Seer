 package net.minecraft.src;
 
 import java.util.Random;
 
 import net.minecraft.src.forge.*;
 
 public class mod_amod extends BaseMod
 {
 	public static Block LightStoneOre = new LightStoneOre(255, 0)
 		.setHardness(1.0f)
 		.setResistance(6000.0F)
 		.setLightValue(1.0F)
 		.setBlockName("LightStoneOre");
	public static final Block DarkStoneOre = new DarkStoneOre(254, 1)
		.setHardness(2.0f)
		.setResistance(6000.0F)
		.setLightValue(0.0F)
		.setBlockName("DarkStoneOre");
 	public static final Item LightStoneBall = new Item(387)
 		.setItemName("LightStoneBall")
 		.setIconIndex(0);
 	
 	public mod_amod()
 	{
 		MinecraftForgeClient.preloadTexture("/imgz/terrain.png");
        MinecraftForgeClient.preloadTexture("/imgz/items.png");
 		ModLoader.registerBlock(LightStoneOre);
		ModLoader.registerBlock(DarkStoneOre);
 	    	ModLoader.addName(LightStoneBall, "LightStone Ball");
 	    	ModLoader.addName(LightStoneOre, "LightStone Ore");
	    	ModLoader.addName(DarkStoneOre, "DarkStone Ore");
 	
 	}
 	
     
 	public void GenerateSurface(World world, Random rand, int chunkX, int chunkZ)
     	{
         	for(int i = 0; i < 15; i++)
         	{
         	    int randPosX = chunkX + rand.nextInt(16);
         	    int randPosY = rand.nextInt(40);
         	    int randPosZ = chunkZ + rand.nextInt(16);
         	    (new WorldGenMinable(mod_amod.LightStoneOre.blockID, 10))
         	    	.generate(world, rand, randPosX, randPosY, randPosZ);
         	}
     	}
 	
 	@Override
 	public String getVersion()
 	{
		return "Pre-Alpha b1";
 	}
 	@Override
 	public void load() {}
 }
