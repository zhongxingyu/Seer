 package me.passivepicasso.util;
 
 import static ch.lambdaj.Lambda.filter;
 
 import java.lang.reflect.Field;
 import java.util.ArrayList;
 import java.util.EnumSet;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Set;
 
 import javax.persistence.Entity;
 import javax.persistence.Table;
 
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.block.BlockFace;
 import org.hamcrest.BaseMatcher;
 import org.hamcrest.Description;
 import org.hamcrest.Matcher;
 
 import com.avaje.ebean.validation.NotEmpty;
 import com.avaje.ebean.validation.NotNull;
 
 /**
  * Warning, you must dispose this class in order to prevent a memory leak.
  * use BlockMatrixNode.dispose()
  * 
  * @author Tobias
  * 
  */
 @Entity()
 @Table(name = "MatrixNodes")
 public class BlockMatrixNode {
     public enum Axis {
         X, Y, Z
     }
 
     // X, Y, Z
     private final HashMap<Block, BlockMatrixNode> matrixNodes;
 
     @NotNull
     private final int                             x;
     @NotNull
     private final int                             y;
     @NotNull
     private final int                             z;
 
     @NotNull
     @NotEmpty
     private final String                          world;
 
     @NotNull
     private final Block                           block;
 
     private BlockMatrixNode                       nextX;
     private BlockMatrixNode                       previousX;
     private BlockMatrixNode                       nextY;
     private BlockMatrixNode                       previousY;
     private BlockMatrixNode                       nextZ;
     private BlockMatrixNode                       previousZ;
 
     @NotNull
     @NotEmpty
     private boolean                               isComplete;
 
     @NotNull
     @NotEmpty
     private Set<Material>                         filter;
 
     public BlockMatrixNode( Block block, HashMap<Block, BlockMatrixNode> matrixNodes ) {
         this.matrixNodes = matrixNodes;
         isComplete = false;
         world = block.getWorld().getName();
         this.block = block;
         x = block.getX();
         y = block.getY();
         z = block.getZ();
         if ( !matrixNodes.containsKey(block) ) {
             matrixNodes.put(block, this);
             if ( this.matrixNodes.containsKey(block.getRelative(1, 0, 0)) ) {
                 BlockMatrixNode nextX = this.matrixNodes.get(block.getRelative(1, 0, 0));
                 setSouth(nextX);
                 nextX.setNorth(this);
             }
             if ( this.matrixNodes.containsKey(block.getRelative(-1, 0, 0)) ) {
                 BlockMatrixNode previousX = this.matrixNodes.get(block.getRelative(-1, 0, 0));
                 setNorth(previousX);
                 previousX.setSouth(this);
             }
             if ( this.matrixNodes.containsKey(block.getRelative(0, 1, 0)) ) {
                 BlockMatrixNode nextY = this.matrixNodes.get(block.getRelative(0, 1, 0));
                 setUp(nextY);
                 nextY.setDown(this);
             }
             if ( this.matrixNodes.containsKey(block.getRelative(0, -1, 0)) ) {
                 BlockMatrixNode previousY = this.matrixNodes.get(block.getRelative(0, -1, 0));
                 setDown(previousY);
                 previousY.setUp(this);
             }
             if ( this.matrixNodes.containsKey(block.getRelative(0, 0, 1)) ) {
                 BlockMatrixNode nextZ = this.matrixNodes.get(block.getRelative(0, 0, 1));
                 setWest(nextZ);
                 nextZ.setEast(this);
             }
             if ( this.matrixNodes.containsKey(block.getRelative(0, 0, -1)) ) {
                 BlockMatrixNode previousZ = this.matrixNodes.get(block.getRelative(0, 0, -1));
                 setEast(previousZ);
                 previousZ.setWest(this);
             }
         } else {
             BlockMatrixNode existing = matrixNodes.get(block);
             try {
                 for (Field field : BlockMatrixNode.class.getFields()) {
                     field.set(this, field.get(existing));
                 }
             } catch (IllegalArgumentException e) {
                 e.printStackTrace();
             } catch (IllegalAccessException e) {
                 e.printStackTrace();
             }
         }
     }
 
     public boolean addDown() {
         if ( getDown() == null ) {
             if ( filter != null && filter.contains(block.getFace(BlockFace.DOWN).getType()) ) {
                 new BlockMatrixNode(block.getFace(BlockFace.DOWN), matrixNodes);
             } else if ( filter == null ) {
                 new BlockMatrixNode(block.getFace(BlockFace.DOWN), matrixNodes);
             }
         }
         return getDown() != null;
     }
 
     public boolean addEast() {
         if ( getEast() == null ) {
             if ( filter != null && filter.contains(block.getFace(BlockFace.EAST).getType()) ) {
                 new BlockMatrixNode(block.getFace(BlockFace.EAST), matrixNodes);
             } else if ( filter == null ) {
                 new BlockMatrixNode(block.getFace(BlockFace.EAST), matrixNodes);
             }
         }
         return getEast() != null;
     }
 
     public boolean addNorth() {
         if ( getSouth() == null ) {
             if ( filter != null && filter.contains(block.getFace(BlockFace.NORTH).getType()) ) {
                 new BlockMatrixNode(block.getFace(BlockFace.NORTH), matrixNodes);
             } else if ( filter == null ) {
                 new BlockMatrixNode(block.getFace(BlockFace.NORTH), matrixNodes);
             }
         }
         return getSouth() != null;
     }
 
     public boolean addSouth() {
         if ( getSouth() == null ) {
             if ( filter != null && filter.contains(block.getFace(BlockFace.SOUTH).getType()) ) {
                 new BlockMatrixNode(block.getFace(BlockFace.SOUTH), matrixNodes);
             } else if ( filter == null ) {
                 new BlockMatrixNode(block.getFace(BlockFace.SOUTH), matrixNodes);
             }
         }
         return getSouth() != null;
     }
 
     public boolean addUp() {
         if ( getUp() == null ) {
             if ( filter != null && filter.contains(block.getFace(BlockFace.UP).getType()) ) {
                 new BlockMatrixNode(block.getFace(BlockFace.UP), matrixNodes);
             } else if ( filter == null ) {
                 new BlockMatrixNode(block.getFace(BlockFace.UP), matrixNodes);
             }
         }
         return getUp() != null;
     }
 
     public boolean addWest() {
         if ( getWest() == null ) {
             if ( filter != null && filter.contains(block.getFace(BlockFace.WEST).getType()) ) {
                 new BlockMatrixNode(block.getFace(BlockFace.WEST), matrixNodes);
             } else if ( filter == null ) {
                 new BlockMatrixNode(block.getFace(BlockFace.WEST), matrixNodes);
             }
         }
         return getWest() != null;
     }
 
     public void complete() {
         isComplete = true;
         if ( hasNorth() && !getNorth().isComplete() ) {
             getNorth().complete();
         }
         if ( hasEast() && !getEast().isComplete() ) {
             getEast().complete();
         }
         if ( hasSouth() && !getSouth().isComplete() ) {
             getSouth().complete();
         }
         if ( hasWest() && !getWest().isComplete() ) {
             getWest().complete();
         }
     }
 
     public void dispose() {
         if ( matrixNodes.size() > 0 ) {
             matrixNodes.clear();
         }
         if ( hasNorth() ) {
             getNorth().dispose();
         }
         if ( hasEast() ) {
             getEast().dispose();
         }
         if ( hasSouth() ) {
             getSouth().dispose();
         }
         if ( hasWest() ) {
             getWest().dispose();
         }
         if ( hasNorth() ) {
             setNorth(null);
         }
         if ( hasEast() ) {
             setEast(null);
         }
         if ( hasSouth() ) {
             setSouth(null);
         }
         if ( hasWest() ) {
             setWest(null);
         }
     }
 
     @Override
     public boolean equals( Object obj ) {
         if ( this == obj ) { return true; }
         if ( obj == null ) { return false; }
         if ( getClass() != obj.getClass() ) { return false; }
         BlockMatrixNode other = (BlockMatrixNode) obj;
         if ( x != other.x ) { return false; }
         if ( y != other.y ) { return false; }
         if ( z != other.z ) { return false; }
         return true;
     }
 
     public boolean equalsX( int x ) {
         return this.x == x;
     }
 
     public boolean equalsY( int y ) {
         return this.y == y;
     }
 
     public boolean equalsZ( int z ) {
         return this.z == z;
     }
 
     public void fillRadius( BlockMatrixNode next, int range ) {
         if ( range == 0 ) { return; }
         if ( getUp() == null ) {
             if ( addUp() ) {
                 if ( getUp() != null && distanceSquared(getUp().getBlock()) < range * range ) {
                     fillRadius(getUp(), range - 1);
                 } else {
                     matrixNodes.remove(getUp().block);
                     setUp(null);
                 }
             }
         }
         if ( getDown() == null ) {
             if ( addDown() ) {
                 if ( getDown() != null && distanceSquared(getDown().getBlock()) < range * range ) {
                     fillRadius(getDown(), range - 1);
                 } else {
                     matrixNodes.remove(getDown().block);
                     setDown(null);
                 }
             }
         }
         if ( getNorth() == null ) {
             if ( addNorth() ) {
                 if ( getNorth() != null && distanceSquared(getNorth().getBlock()) < range * range ) {
                     fillRadius(getNorth(), range - 1);
                 } else {
                     matrixNodes.remove(getNorth().block);
                     setNorth(null);
                 }
             }
         }
         if ( getEast() == null ) {
             if ( addEast() ) {
                 if ( getEast() != null && distanceSquared(getEast().getBlock()) < range * range ) {
                     fillRadius(getEast(), range - 1);
                 } else {
                     matrixNodes.remove(getEast().block);
                     setEast(null);
                 }
             }
         }
         if ( getSouth() == null ) {
             if ( addSouth() ) {
                 if ( getSouth() != null && distanceSquared(getSouth().getBlock()) < range * range ) {
                     fillRadius(getSouth(), range - 1);
                 } else {
                     matrixNodes.remove(getSouth().block);
                     setSouth(null);
                 }
             }
         }
         if ( getWest() == null ) {
             if ( addWest() ) {
                 if ( getWest() != null && distanceSquared(getWest().getBlock()) < range * range ) {
                     fillRadius(getWest(), range - 1);
                 } else {
                     matrixNodes.remove(getWest().block);
                     setWest(null);
                 }
             }
         }
     }
 
     public void floodFill() {
         floodFill(this);
     }
 
     public void floodFill( BlockMatrixNode next ) {
         if ( next == null ) { return; }
         if ( next.getUp() == null && next.addUp() ) {
             next.floodFill(getUp());
         }
         if ( next.getDown() == null && next.addDown() ) {
             next.floodFill(getDown());
         }
         if ( next.getEast() == null && next.addEast() ) {
             next.floodFill(getEast());
         }
         if ( next.getWest() == null && next.addWest() ) {
             next.floodFill(getWest());
         }
         if ( next.getNorth() == null && next.addNorth() ) {
             next.floodFill(getNorth());
         }
         if ( next.getSouth() == null && next.addSouth() ) {
             next.floodFill(getSouth());
         }
     }
 
     public Block getBlock() {
         if ( block == null ) {
 
         }
         return block;
     }
 
     public ArrayList<Block> getBlockMatrix() {
         return new ArrayList<Block>(matrixNodes.keySet());
     }
 
     public HashSet<BlockMatrixNode> getBlockMatrixNodes() {
         return new HashSet<BlockMatrixNode>(matrixNodes.values());
     }
 
     /**
      * 
      * @param plane
      * @param a
      *            either X or Y
      * @param b
      *            either Y or Z
      * @return set of
      */
     public Set<BlockMatrixNode> getBlockPlane( final Axis axis, final int value ) {
         Matcher<BlockMatrixNode> onAxis = new BaseMatcher<BlockMatrixNode>() {
             @Override
             public void describeTo( Description description ) {
 
             }
 
             @Override
             public boolean matches( Object item ) {
                 int val = value;
                 if ( item.getClass().equals(BlockMatrixNode.class) ) {
                     BlockMatrixNode node = (BlockMatrixNode) item;
                     if ( filter == null || getFilter().contains(node.getBlock().getType()) ) {
                         switch (axis) {
                             case X:
                                 return node.equalsX(val);
                             case Y:
                                 return node.equalsY(val);
                             case Z:
                                 return node.equalsZ(val);
                         }
                     }
                 }
                 return false;
             }
         };
         HashSet<BlockMatrixNode> nodes = new HashSet<BlockMatrixNode>(filter(onAxis, matrixNodes.values()));
         return nodes;
     }
 
     public BlockMatrixNode getDown() {
         return previousY;
     }
 
     public BlockMatrixNode getEast() {
         return previousZ;
     }
 
     public Set<Material> getFilter() {
         return filter;
     }
 
     public HashSet<Block> getFilteredExternalAdjancentBlocks() {
         HashSet<Block> blocks = new HashSet<Block>();
        for (BlockFace face : EnumSet.complementOf(EnumSet.of(BlockFace.SELF, BlockFace.NORTH_EAST, BlockFace.NORTH_WEST, BlockFace.SOUTH_EAST,
                BlockFace.SOUTH_WEST))) {
            if ( filter.contains(getBlock().getFace(face).getType()) && matrixNodes.containsKey(getBlock().getFace(face)) ) {
                blocks.add(block);
             }
         }
         return blocks;
     }
 
     /**
      * get
      * 
      * @param block
      * @return
      */
     public BlockMatrixNode getMatrixNode( Block block ) {
         return matrixNodes.get(block);
     }
 
     public BlockMatrixNode getNorth() {
         return previousX;
     }
 
     public BlockMatrixNode getSouth() {
         return nextX;
     }
 
     public BlockMatrixNode getUp() {
         return nextY;
     }
 
     public BlockMatrixNode getWest() {
         return nextZ;
     }
 
     public String getWorld() {
         return world;
     }
 
     public boolean hasDown() {
         return previousY != null;
     }
 
     public boolean hasEast() {
         return previousZ != null;
     }
 
     public boolean hasFilteredDown() {
         if ( filter != null && previousY != null ) { return filter.contains(previousY.getBlock().getType()); }
         return false;
     }
 
     public boolean hasFilteredEast() {
         if ( filter != null && previousZ != null ) { return filter.contains(previousZ.getBlock().getType()); }
         return false;
     }
 
     public boolean hasFilteredNorth() {
         if ( filter != null && previousX != null ) { return filter.contains(previousX.getBlock().getType()); }
         return false;
     }
 
     public boolean hasFilteredSouth() {
         if ( filter != null && nextX != null ) { return filter.contains(nextX.getBlock().getType()); }
         return false;
     }
 
     public boolean hasFilteredUp() {
         if ( filter != null && nextY != null ) { return filter.contains(nextY.getBlock().getType()); }
         return false;
     }
 
     public boolean hasFilteredWest() {
         if ( filter != null && nextZ != null ) { return filter.contains(nextZ.getBlock().getType()); }
         return false;
     }
 
     @Override
     public int hashCode() {
         final int prime = 31;
         int result = 1;
         result = prime * result + x;
         result = prime * result + y;
         result = prime * result + z;
         return result;
     }
 
     public boolean hasNorth() {
         return previousX != null;
     }
 
     public boolean hasSouth() {
         return nextX != null;
     }
 
     public boolean hasUp() {
         return nextY != null;
     }
 
     public boolean hasWest() {
         return nextZ != null;
     }
 
     public boolean isComplete() {
         return isComplete;
     }
 
     public void setDown( BlockMatrixNode previousY ) {
         if ( getFilter() != null ) {
             if ( getFilter().contains(previousY.getBlock().getType()) ) {
                 this.previousY = previousY;
             }
         } else {
             this.previousY = previousY;
         }
     }
 
     public void setEast( BlockMatrixNode previousZ ) {
         if ( getFilter() != null ) {
             if ( getFilter().contains(previousZ.getBlock().getType()) ) {
                 this.previousZ = previousZ;
             }
         } else {
             this.previousZ = previousZ;
         }
     }
 
     /**
      * Add an Inclusive Filter, using filtered retrievers will only find blocks of the Material types specified in the filter.
      * 
      * @param filter
      */
     public void setFilter( Set<Material> filter ) {
         this.filter = filter;
     }
 
     public void setNorth( BlockMatrixNode previousX ) {
         if ( getFilter() != null ) {
             if ( getFilter().contains(previousX.getBlock().getType()) ) {
                 this.previousX = previousX;
             }
         } else {
             this.previousX = previousX;
         }
     }
 
     public void setSouth( BlockMatrixNode nextX ) {
         if ( getFilter() != null ) {
             if ( getFilter().contains(nextX.getBlock().getType()) ) {
                 this.nextX = nextX;
             }
         } else {
             this.nextX = nextX;
         }
     }
 
     public void setUp( BlockMatrixNode nextY ) {
         if ( getFilter() != null ) {
             if ( getFilter().contains(nextY.getBlock().getType()) ) {
                 this.nextY = nextY;
             }
         } else {
             this.nextY = nextY;
         }
     }
 
     public void setWest( BlockMatrixNode nextZ ) {
         if ( getFilter() != null ) {
             if ( getFilter().contains(nextZ.getBlock().getType()) ) {
                 this.nextZ = nextZ;
             }
         } else {
             this.nextZ = nextZ;
         }
     }
 
     public int size() {
         return matrixNodes.size();
     }
 
     private double distanceSquared( Block target ) {
         Location loc = getBlock().getLocation();
         Location tar = target.getLocation();
         int x = loc.getBlockX();
         int y = loc.getBlockY();
         int z = loc.getBlockZ();
         int xd = x - tar.getBlockX();
         int yd = y - tar.getBlockY();
         int zd = z - tar.getBlockZ();
         return xd * xd + yd * yd + zd * zd;
     }
 }
