 package iggy.Economy;
 
 import java.util.Random;
 
 import org.bukkit.Material;
 import org.bukkit.World;
 import org.bukkit.generator.ChunkGenerator;
 
 
 public class ShopGenerator extends ChunkGenerator {
     //This is where you stick your populators - these will be covered in another tutorial
 	
 	int shopRadius = 30;
 	/*@Override
     public List<BlockPopulator> getDefaultPopulators(World world) {
         //return Arrays.asList((BlockPopulator) new BlankPopulator());
     }*/
     //This needs to be set to return true to override minecraft's default behaviour
     @Override
     public boolean canSpawn(World world, int x, int z) {
         return true;
     }
     //This converts relative chunk locations to bytes that can be written to the chunk
     public int xyzToByte(int x, int y, int z) {
     	return (x * 16 + z) * 128 + y;
     }
 
     @Override
     public byte[] generate(World world, Random rand, int chunkx, int chunkz) {
 	    byte[] result = new byte[32768];
 	    
 	    int bedrocky = 64;
 	    int floor1 = 65;
 	    
 	    //This will set the floor of each chunk at bedrock level to bedrock
 	    for(int x=0; x<16; x++){
 		    for(int z=0; z<16; z++) {
 		    	int totalx = x + chunkx*16;
 		    	int totaly = z + chunkz*16;
 		    	
 		    	
 		    	// generate bedrock
		    	if ((totalx*totalx)+(totaly*totaly)<=shopRadius) {
 		    		
 		    		result[xyzToByte(x,bedrocky,z)] = (byte) Material.BEDROCK.getId();
 		    		
 		    		result[xyzToByte(x,floor1,z)] = (byte) Material.DOUBLE_STEP.getId();
 		    	}
 		    }
 	    }
 	    return result;
     }
 
 }
