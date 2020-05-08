 package net.minecraft.src;
 
 import java.util.*;
 
 public class mod_TestBlock extends BaseMod
 {
 
 	public static Block TestBlock = new TestBlock(255, 0).setHardness(1.0f).setResistance(6000.0F).setLightValue(1.0F).setBlockName("TestBlock");
 
 	public String Version()
 	{
 		return "1.0";
 	}
 	
 	public mod_TestBlock()
 	{
 		ModLoader.RegisterBlock(TestBlock);
 		TestBlock.blockIndexInTexture = ModLoader.addOverride("/terrain.png", "/bph.png");
 		ModLoader.AddName(TestBlock, "TestBlock");
 		ModLoader.AddRecipe(new ItemStack(TestBlock, 1), new Object[] {
 			"###", "###", "###", Character.valueOf('#'), Item.redstone
 		});
 	}
     public void GenerateSurface(World world, Random rand, int chunkX, int chunkZ)
     {
        for(int i = 0; i < 10; i++)
         {
             int randPosX = chunkX + rand.nextInt(16);
            int randPosY = rand.nextInt(40);
             int randPosZ = chunkZ + rand.nextInt(16);
             (new WorldGenMinable(mod_TestBlock.TestBlock.blockID, 10)).generate(world, rand, randPosX, randPosY, randPosZ);
         }
     }   
 
 
 }
 
