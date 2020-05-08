 package fi.haju.haju3d.server.world.utils;
 
 import com.google.common.collect.Lists;
 import fi.haju.haju3d.protocol.coordinate.LocalTilePosition;
 import fi.haju.haju3d.protocol.world.Chunk;
 import fi.haju.haju3d.protocol.world.Tile;
 
 import java.util.List;
 import java.util.Random;
 
 public final class WorldGenerationUtils {
 
   private WorldGenerationUtils() {
   }
 
   private static class TreeBranchState {
     public final int lengthLeft;
     public final LocalTilePosition place;
     public final LocalTilePosition dir;
     public final int length;
 
     public TreeBranchState(int lengthLeft, LocalTilePosition place, LocalTilePosition dir, int length) {
       this.lengthLeft = lengthLeft;
       this.place = place;
       this.dir = dir;
       this.length = length;
     }
 
   }
 
   public static void makeTreeAt(Chunk chunk, Random r, LocalTilePosition pos) {
     int x = pos.x;
     int y = pos.y;
     int z = pos.z;
     List<TreeBranchState> branches = Lists.newArrayList();
     int height = r.nextInt(10) + 10;
     for (int k = height; k >= 0; k--) {
       chunk.set(x, y + k, z, Tile.WOOD);
       chunk.set(x + 1, y + k, z, Tile.WOOD);
       chunk.set(x + 1, y + k, z + 1, Tile.WOOD);
       chunk.set(x, y + k, z + 1, Tile.WOOD);
       if (k > 5) {
         if (r.nextInt(6) == 0)
           branches.add(new TreeBranchState(r.nextInt(7) + (height - k), new LocalTilePosition(x, y + k, z), new LocalTilePosition(-1, 0, 0), 0));
         if (r.nextInt(6) == 0)
           branches.add(new TreeBranchState(r.nextInt(7) + (height - k), new LocalTilePosition(x + 1, y + k, z), new LocalTilePosition(0, 0, -1), 0));
         if (r.nextInt(6) == 0)
           branches.add(new TreeBranchState(r.nextInt(7) + (height - k), new LocalTilePosition(x + 1, y + k, z + 1), new LocalTilePosition(1, 0, 0), 0));
         if (r.nextInt(6) == 0)
           branches.add(new TreeBranchState(r.nextInt(7) + (height - k), new LocalTilePosition(x, y + k, z + 1), new LocalTilePosition(0, 0, 1), 0));
       }
     }
     while (!branches.isEmpty()) {
       TreeBranchState state = branches.remove(0);
       LocalTilePosition next = state.place;
       if (state.lengthLeft <= 0) continue;
       // go up
       if (r.nextInt(3) < 2 && state.length > 2) next = next.add(0, 1, 0);
         // go sideways
       else if (r.nextInt(4) == 0) {
         int s = r.nextInt(4);
         switch (s) {
         case 0:
           next = next.add(1, 0, 0);
           break;
         case 1:
           next = next.add(-1, 0, 0);
           break;
         case 2:
           next = next.add(0, 0, 1);
           break;
         default:
           next = next.add(0, 0, -1);
           break;
         }
       }
       // go straight
       else next = next.add(state.dir);
       if (!chunk.isWithin(next)) continue;
       if (!chunk.get(next).equals(Tile.AIR) && !chunk.get(next).equals(Tile.WOOD)) continue;
       chunk.set(next.x, next.y, next.z, Tile.WOOD);
       branches.add(new TreeBranchState(state.lengthLeft - 1, next, state.dir, state.length + 1));
     }
   }
 
   public static int findGround(Chunk chunk, int h, int midX, int midZ) {
     for (int y = 0; y < h; y++) {
       int testY = h - 1 - y;
       if (chunk.get(midX, testY, midZ) != Tile.AIR) {
         return testY;
       }
     }
     return -1;
   }
 
 }
