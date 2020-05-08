 package com.archmageinc.RandomEncounters;
 
 import org.bukkit.Chunk;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.block.BlockFace;
 
 
 public class Locator {
     protected static Locator instance  =   new Locator();
     
     public static Locator getInstance(){
         return instance;
     }
     protected Locator(){
         
     }
     
     /**
      *  Check a chunk to see if it will support a given encounter
      * @param chunk The chunk to check
      * @param encounter The encounter for placement
      * @return Returns the location suitable for the encounter or null if none.
      */
     public Location checkChunk(Chunk chunk,Encounter encounter){
         Block currentBlock,aboveBlock;
         if(RandomEncounters.getInstance().getLogLevel()>8){
             RandomEncounters.getInstance().logMessage("Checking chunk: "+chunk.getX()+","+chunk.getZ()+" for encounter "+encounter.getName());
         }
        if(encounter.getStructure()==null){
            RandomEncounters.getInstance().logError("Missing structure for encounter "+encounter.getName()+"!");
            return null;
        }
         for(int y=encounter.getStructure().getMinY().intValue();y<encounter.getStructure().getMaxY();y++){
             for(int x=0;x<16;x++){
                 for(int z=0;z<16;z++){
                     currentBlock    =   chunk.getBlock(x, y, z);
                     aboveBlock      =   currentBlock.getRelative(BlockFace.UP);
                     if(
                         (encounter.getValidBiomes().isEmpty() || encounter.getValidBiomes().contains(currentBlock.getBiome()))
 
                         && (!encounter.getInvalidBiomes().contains(currentBlock.getBiome()))
 
                         /**
                         * The current block may not be:
                         */
                         && !encounter.getStructure().getInvalid().contains(currentBlock.getType())
 
                         /**
                         * The block above the current block must be
                         */
 
                         && encounter.getStructure().getTrump().contains(aboveBlock.getType())
 
                         && checkSpace(currentBlock,encounter.getStructure())
                     ){
                         return currentBlock.getRelative(BlockFace.UP).getLocation();
                     }
                 }
             }
         }
         return null;
     }
     
     /**
      * Check the space surrounding a block to see if the structure will fit
      * 
      * @param startingBlock The block to start the check
      * @param structure The structure to check placement
      * @return Returns true if the block's location is suitable false otherwise
      */
     private boolean checkSpace(Block startingBlock,Structure structure){
         int xMin    =    (int) Math.ceil(structure.getWidth()/2);
         int zMin    =    (int) Math.ceil(structure.getLength()/2);
         int yMin    =    structure.getHeight();
         Block currentBlock,belowBlock;
         for(int x = -xMin;x<=xMin;x++){
             for(int z = -zMin;z<=zMin;z++){
                 currentBlock  =   startingBlock.getRelative(x,0,z);
                 belowBlock    =   currentBlock.getRelative(BlockFace.DOWN);
 
                 if(
                     /**
                     * The current block may not be:
                     */
                     structure.getInvalid().contains(currentBlock.getType())
 
                     /*
                      The block below the current block may not be:
                     */
                     || structure.getInvalid().contains(belowBlock.getType())
                 ){
                     if(RandomEncounters.getInstance().midas())
                         currentBlock.setType(Material.GOLD_BLOCK);
                     return false;
                 }
             }
         }
         /*
         We iterate though this again to verify the structure's height will only trump what it is allowed
         Since we found a plane that will support the structure, we need the full 3D
         */
         for(int y=1;y<yMin;y++){
             for(int x = -xMin;x<=xMin;x++){
                 for(int z = -zMin;z<=zMin;z++){
                     currentBlock  =   startingBlock.getRelative(x,y,z);
                     if(!structure.getTrump().contains(currentBlock.getType())){
                         if(RandomEncounters.getInstance().midas())
                             currentBlock.setType(Material.GOLD_BLOCK);
                         return false;
                     }
                 }
             }
         }
         return true;
     }
 }
