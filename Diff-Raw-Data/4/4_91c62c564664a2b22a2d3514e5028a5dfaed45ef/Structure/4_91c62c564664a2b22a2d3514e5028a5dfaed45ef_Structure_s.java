 package Seremis.SoulCraft.api.util.structure;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import net.minecraft.block.Block;
 import net.minecraft.world.World;
 import Seremis.SoulCraft.api.util.Coordinate3D;
 
 public class Structure {
     
     protected List<IStructureBlock> blocks = new ArrayList<IStructureBlock>();
     
     protected int length;
     protected int width;
     protected int height;
     protected int baseSize = {3, 3, 4};
     
     public Structure(IStructureBlock... block) {
         blocks.addAll(Arrays.asList(block));
     }
     
     public void addBlock(IStructureBlock block) {
         blocks.add(block);
     }
     
     /** 
      * Gets the block in the coordinate according to the multiblock, starting at 0, 0, 0.
      * @param x
      * @param y
      * @param z
      * @return the block at the coordinate
      */
     public IStructureBlock getBlockAtCoordinate(int x, int y, int z) {
         for(IStructureBlock block : blocks) {
             if(block.getPosition().x == x && block.getPosition().y == y && block.getPosition().z == z) {
                 return block;
             }
         }
         return null;
     }
     
     private void setSize(int length, int height, int width) {
         this.length = length;
         this.width = width;
         this.height = height;
     }
     
     private boolean doesStructureExistAtCoords(World world, int x, int y, int z) {
         for(IStructureBlock block : getBlocks()) {
             if(world.getBlockId(x+(int)block.getPosition().x, y+(int)block.getPosition().y, z+(int)block.getPosition().z) == block.getBlock().blockID) {
                 if(world.getBlockMetadata(x+(int)block.getPosition().x, y+(int)block.getPosition().y, z+(int)block.getPosition().z) == block.getMetadata()) {
                     if(block.canFormStructure(this, world, x+(int)block.getPosition().x, y+(int)block.getPosition().y, z+(int)block.getPosition().z)) {
                     } else {
                         return false;
                     }
                 } else {
                     return false;
                 }
             } else {
                 return false;
             }
         }
         return airCheck(world, x, y, z);
     }
     
     /**
      * Checks if this structure exists from the given block, at the given world coords and with any possible x-z rotation
      * @param world The world instance
      * @param block The block the structure is measured from, this should be a block that is contained in a structureblock in this structure.
      * @param x
      * @param y
      * @param z
      * @return
      */
     public boolean doesRotatedStructureExistAtCoords(World world, Block block, int x, int y, int z) {
         Coordinate3D coord = new Coordinate3D();
         
         for(int i = 0; i<4; i++) {
             Structure structure = new Structure();
             structure = rotateOnYAxis(i);
             for(IStructureBlock sBlock : structure.getBlocks()) {
                 if(sBlock.getBlock() == block) {
                     if(sBlock.getMetadata() == world.getBlockMetadata(x+(int)sBlock.getPosition().x, y+(int)sBlock.getPosition().y, z+(int)sBlock.getPosition().z)) {
                         coord = sBlock.getPosition().clone();
                     }
                 }
             }
             if(structure.doesStructureExistAtCoords(world, x-(int)coord.x, y-(int)coord.y, z-(int)coord.z)) {
                this.setRotatedSize(i);
                 return true;
             }
         }
         return false;
     }
     
     private boolean airCheck(World world, int x, int y, int z) {
         List<Coordinate3D> shouldBeFull = new ArrayList<Coordinate3D>();
         List<Coordinate3D> actuallyFull = new ArrayList<Coordinate3D>();
         for(IStructureBlock block : blocks) {
             shouldBeFull.add(block.getPosition());
         }
 
         if(length >= 0 && height >= 0 && width >= 0) {
             for(int i = 0; i<length; i++) {
                 for(int j = 0; j<height; j++) {
                     for(int k = 0; k<width; k++) {
                         if(world.getBlockId(x+i, y+j, z+k) != 0) {
                             actuallyFull.add(new Coordinate3D(i, j, k));
                         }
                     }
                 }
             }
         }
         if(length >= 0 && height >= 0 && width < 0) {
             for(int i = 0; i<length; i++) {
                 for(int j = 0; j<height; j++) {
                     for(int k = 0; k>width; k--) {
                         if(world.getBlockId(x+i, y+j, z+k) != 0) {
                             actuallyFull.add(new Coordinate3D(i, j, k));
                         }
                     }
                 }
             }
         }
         if(length >= 0 && height < 0 && width >= 0) {
             for(int i = 0; i<length; i++) {
                 for(int j = 0; j>height; j--) {
                     for(int k = 0; k<width; k++) {
                         if(world.getBlockId(x+i, y+j, z+k) != 0) {
                             actuallyFull.add(new Coordinate3D(i, j, k));
                         }
                     }
                 }
             }
         }
         if(length >= 0 && height < 0 && width < 0) {
             for(int i = 0; i<length; i++) {
                 for(int j = 0; j>height; j--) {
                     for(int k = 0; k>width; k--) {
                         if(world.getBlockId(x+i, y+j, z+k) != 0) {
                             actuallyFull.add(new Coordinate3D(i, j, k));
                         }
                     }
                 }
             }
         }
         if(length < 0 && height >= 0 && width >= 0) {
             for(int i = 0; i>length; i--) {
                 for(int j = 0; j<height; j++) {
                     for(int k = 0; k<width; k++) {
                         if(world.getBlockId(x+i, y+j, z+k) != 0) {
                             actuallyFull.add(new Coordinate3D(i, j, k));
                         }
                     }
                 }
             }
         }
         if(length < 0 && height >= 0 && width < 0) {
             for(int i = 0; i>length; i--) {
                 for(int j = 0; j<height; j++) {
                     for(int k = 0; k>width; k--) {
                         if(world.getBlockId(x+i, y+j, z+k) != 0) {
                             actuallyFull.add(new Coordinate3D(i, j, k));
                         }
                     }
                 }
             }
         }
         if(length < 0 && height < 0 && width >= 0) {
             for(int i = 0; i>length; i--) {
                 for(int j = 0; j>height; j--) {
                     for(int k = 0; k<width; k++) {
                         if(world.getBlockId(x+i, y+j, z+k) != 0) {
                             actuallyFull.add(new Coordinate3D(i, j, k));
                         }
                     }
                 }
             }
         }
         if(length < 0 && height < 0 && width < 0) {
             for(int i = 0; i>length; i--) {
                 for(int j = 0; j>height; j--) {
                     for(int k = 0; k>width; k--) {
                         if(world.getBlockId(x+i, y+j, z+k) != 0) {
                             actuallyFull.add(new Coordinate3D(i, j, k));
                         }
                     }
                 }
             }
         }
         if(shouldBeFull.size() == actuallyFull.size()) {
             return true;
         }
         return false;
     }
     
     public int getBlockCount() {
         return blocks.size();
     }
     
     public List<IStructureBlock> getBlocks() {
         return blocks;
     }
     
     public int getLength() {
         return length;
     }
     
     public int getWidth() {
         return width;
     }
     
     public int getHeight() {
         return height;
     }
     
     private Structure rotateOnYAxis(int rotation) {
         Structure newStructure = new Structure();
         
         for(IStructureBlock block : getBlocks()) {    
             IStructureBlock newBlock = block.copy();
             int x = (int) block.getPosition().x;
             int z = (int) block.getPosition().z;      
             
             switch(rotation) {           
                case 0: {
                    newBlock.getPosition().x = x; 
                    newBlock.getPosition().z = z;
                    break;
                }
                case 1: { 
                    newBlock.getPosition().x = -z;   
                    newBlock.getPosition().z = x;
                    break;
                }
                case 2: {
                    newBlock.getPosition().x = -x;   
                    newBlock.getPosition().z = -z;
                    break;
                }
                case 3: {
                    newBlock.getPosition().x = z;   
                    newBlock.getPosition().z = -x;
                    break;
                }
            }
            newStructure.addBlock(newBlock);
         }
         return newStructure;
     }
     
     private void setRotatedSize(int rotation) {
         switch(rotation) {           
                case 0: {
                    setSize(baseSize[0], baseSize[1], baseSize[2]);
                    break;
                }
                case 1: {
                    setSize(-baseSize[2], baseSize[1], baseSize[0]);
                    break;
                }
                case 2: {
                    setSize(-baseSize[0], baseSize[1], -baseSize[2]);
                    break;
                }
                case 3: {
                    setSize(baseSize[2], baseSize[1], -baseSize[0]);
                    break;
                }
            }
     }
 }
