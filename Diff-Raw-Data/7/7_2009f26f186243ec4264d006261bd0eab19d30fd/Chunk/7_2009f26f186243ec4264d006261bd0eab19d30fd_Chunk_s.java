 package fi.haju.haju3d.protocol.world;
 
 import java.io.Serializable;
 
 import fi.haju.haju3d.protocol.Vector3i;
 
 
 public final class Chunk implements Serializable {
   private static final long serialVersionUID = 1L;
   
   private Tile[] data;
   
   private Tile[] x_minus_side;
   private Tile[] x_plus_side;
   private Tile[] z_minus_side;
   private Tile[] z_plus_side;
   
   private final int width;
   private final int height;
   private final int depth;
   private final int seed;
   private final Vector3i position;
 
   public Chunk(int width, int height, int depth, int seed, Vector3i position) {
     this.width = width;
     this.height = height;
     this.depth = depth;
     this.seed = seed;
     this.position = position;
 
     this.data = makeAirArray(width * height * depth);
     this.x_minus_side = makeAirArray(height * depth);
     this.x_plus_side = makeAirArray(height * depth);
     this.z_minus_side = makeAirArray(width * height);
     this.z_plus_side = makeAirArray(width * height);    
   }
 
   private Tile[] makeAirArray(int length) {
     Tile[] result = new Tile[length];
     for(int i = 0; i < length; ++i) {
       result[i] = Tile.AIR;
     }
     return result;
   }
   
   public void set(int x, int y, int z, Tile value) {
     if (isInside(x, y, z)) {
       data[getIndex(x, y, z)] = value;
     }
   }
 
   private int getIndex(int x, int y, int z) {
     return x + y * width + z * width * height;
   }
 
   private boolean isInside(int x, int y, int z) {
     return x >= 0 && x < width && y >= 0 && y < height && z >= 0 && z < depth;
   }
 
   public Tile get(int x, int y, int z) {
     if (isInside(x, y, z)) {
       return data[getIndex(x, y, z)];
     } else if(x == -1) {
       return x_minus_side[y + z*height];
     } else if(x == width) {
       return x_plus_side[y + z*height];
     } else if(z == depth) {
       return z_plus_side[x + y*width];
     } else if(z == -1) {
       return z_minus_side[x + y*width];
     } else {
       return Tile.AIR;
     }
   }
 
   public int getWidth() {
     return width;
   }
 
   public int getHeight() {
     return height;
   }
 
   public int getDepth() {
     return depth;
   }
 
   public int getSeed() {
     return seed;
   }
 
   public Vector3i getPosition() {
     return position;
   }
   
   public void setXMinusFrom(Chunk chunk) {
     for(int y = 0; y < height; ++y) {
       for(int z = 0; z < depth; ++z) {
         x_minus_side[y + z*height] = chunk.get(width-1, y, z);
       }
     }
   }
   
   public void setXPlusFrom(Chunk chunk) {
     for(int y = 0; y < height; ++y) {
       for(int z = 0; z < depth; ++z) {
         x_plus_side[y + z*height] = chunk.get(0, y, z);
       }
     }
   }
   
   public void setZMinusFrom(Chunk chunk) {
     for(int y = 0; y < height; ++y) {
       for(int x = 0; x < width; ++x) {
         z_minus_side[x + y*width] = chunk.get(x, y, depth-1);
       }
     }
   }
   
   public void setZPlusFrom(Chunk chunk) {
     for(int y = 0; y < height; ++y) {
       for(int x = 0; x < width; ++x) {
         z_plus_side[x + y*width] = chunk.get(x, y, 0);
       }
     }
   }
   
 }
