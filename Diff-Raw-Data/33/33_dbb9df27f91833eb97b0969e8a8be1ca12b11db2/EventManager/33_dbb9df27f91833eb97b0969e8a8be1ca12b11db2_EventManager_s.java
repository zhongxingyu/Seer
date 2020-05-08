 package mtshaw113.juli;
 
 import java.util.Random;
 
 import mtshaw113.juli.blocks.BlockInfo;
 import net.minecraft.world.World;
 import net.minecraft.world.chunk.IChunkProvider;
 import net.minecraft.world.gen.feature.WorldGenMinable;
 import cpw.mods.fml.common.IWorldGenerator;
 
 public class EventManager implements IWorldGenerator {
 
    @Override
    public void generate(Random random, int chunkX, int chunkZ, World world,
          IChunkProvider chunkGenerator, IChunkProvider chunkProvider) {
       switch(world.provider.dimensionId){
       
       case 1:
          generateNether(world, random, chunkX * 16, chunkZ * 16);
          
       case 0:
          generateSurface(world, random, chunkX * 16, chunkZ * 16);
          
       case -1:
          generateEnd(world, random, chunkX * 16, chunkZ * 16);
       }
       
    }
 
    private void generateEnd(World world, Random random, int chunkX, int chunkZ) {
       
       }
        
  
 
    private void generateSurface(World world, Random random, int chunkX, int chunkZ) {
      for(int i = 0; i < 9; i++){
          int xCoord = chunkX + random.nextInt(16);
         int yCoord = random.nextInt(40);
          int zCoord = chunkZ + random.nextInt(16);        
         new WorldGenMinable(BlockInfo.ORE_ID,i,random.nextInt(9)).generate(world, random, xCoord, yCoord, zCoord);
          
       }
       
    }
    
       /**
     Here, your ore has a spawn rate of 9 (which you can change to suit your desires):
             for(int  i = 0; i < 9; i++) {
     It spawns at, or below, level 13 on the Y-Axis (also changeable):
             in yCoord = random.nextInt(13);
     And it spawns at a max of 4 blocks per vein (again, changeable):
             ...(mtshaw113.julit.oreJuli, 4) ...
      **/
 
    private void generateNether(World world, Random random, int chunkX,
          int chunkZ) {
       
    }
 
 }
