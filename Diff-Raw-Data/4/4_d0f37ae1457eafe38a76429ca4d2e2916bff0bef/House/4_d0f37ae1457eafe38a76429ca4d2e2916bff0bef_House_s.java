 package wustendorf;
 
 import net.minecraft.src.*;
 import java.util.*;
 
 public class House {
     public static final House NO_HOUSE = new House(false);
     public static final House TOO_MANY_ROOMS = new House(false);
 
     public List<Room> rooms = new ArrayList<Room>();
     public boolean valid = true;
 
     public House() { }
 
     public House(boolean valid) {
         this.valid = valid;
     }
 
     public static House detectHouse(World world, int x, int y, int z) {
         House house = new House();
 
         Stack<Room> new_rooms = new Stack<Room>();
         Room start = Room.detectRoom(new Location(world, x, y, z));
         new_rooms.add(start);
 
         if (!start.valid) {
             System.out.println("No house.");
             return NO_HOUSE;
         }
 
         while (!new_rooms.empty()) {
             if (house.rooms.size() + new_rooms.size() > 100) {
                 System.out.println("Too many rooms.");
                 return TOO_MANY_ROOMS;
             }
 
             Room room = new_rooms.pop();
             room.connect(house.rooms);
             house.rooms.add(room);
 
             for (Location connector : room.unexploredConnections()) {
                 // This will update new_rooms if it finds a new valid room.
                 room.explore(connector, new_rooms);
             }
         }
 
         return house;
     }
 
     public static class Room {
         public static final Room OUTSIDE = new Room(false);
         public static final Room SPACE = OUTSIDE;
         public static final Room DANGEROUS = new Room(false);
         public static final Room NONCONTIGUOUS = new Room(false);
 
         public Location origin = null;
 
         public Map<Location, BlockType> blocks = new HashMap<Location, BlockType>();
         public Map<Location, Room> connections = new HashMap<Location, Room>();
 
         public boolean valid = true;
 
         public Room() { }
 
         public Room(boolean valid) {
             this.valid = valid;
         }
 
         public static void reportBlock(String message, int x, int y, int z) {
             System.out.println("@(" + x + "," + y + "," + z + "): " + message);
         }
 
         public static void reportBlock(String message, Location loc) {
             reportBlock(message, loc.x, loc.y, loc.z);
         }
 
         public static Room detectRoom(Location start) {
             Room room = new Room();
 
             World world = start.world;
             int x = start.x;
             int y = start.y;
             int z = start.z;
             BlockType type;
 
             // Descend to the bottom of the first air pocket at or below start.
             boolean found_air = false;
             int min_y = Math.max(0, y-5);
             for (y = start.y; y >= 0; y--) {
                 type = BlockType.typeOf(new Location(world, x, y, z));
 
                if (type == BlockType.AIR_LIKE) {
                     found_air = true;
                 } else if (found_air) {
                     y++; // Go back into the air.
                     break;
                 } else if (y < min_y) {
                     break;
                 }
             }
 
             if (!found_air || BlockType.typeOf(new Location(world, x, y+1, z)).isSolid()) {
                 reportBlock("No air pocket found.", x, y, z);
                 return OUTSIDE;
             }
 
             Stack<Location> new_columns = new Stack<Location>();
             Stack<Location> wall_columns = new Stack<Location>();
             Stack<Location> doors = new Stack<Location>();
             Stack<Location> required_blocks = new Stack<Location>();
             new_columns.add(new Location(world, x, y, z));
 
             int loop_count = 0;
             while (!new_columns.empty()) {
                 loop_count++;
                 if (room.blocks.size() > 10000 || loop_count > 5000) {
                     System.out.println("Too many blocks in room.");
                     return OUTSIDE;
                 }
 
                 // One of the horizontal neighbors of this is a ground-level block: empty on solid.
                 // (or this is the starting spot)
                 Location block = new_columns.pop();
                 type = BlockType.typeOf(block);
                 Location block_above = block.above();
                 BlockType type_above = BlockType.typeOf(block_above);
                 Location block_below = block.below();
                 BlockType type_below = BlockType.typeOf(block_below);
 
                 //reportBlock("Visiting.", block);
 
                 if (type_above.isSolidOrUnsafe()) {
                     wall_columns.add(block_above);
                     continue;
                 } else if (!type.isSafe()) {
                     wall_columns.add(block);
                     continue;
                 } else if (type.isSolid()) {
                     if (!BlockType.typeOf(block.above(2)).isSolidOrUnsafe()) {
                         block = block_above;
                         type = type_above;
                     } else {
                         wall_columns.add(block);
                         wall_columns.add(block.above(2));
                         continue;
                     }
                 } else if (!type_below.isSafe()) {
                     reportBlock("Unsafe floor tile.", block);
                     return DANGEROUS;
                 } else if (!type_below.isSolid()) {
                     if (type_below.isLadder()
                         || BlockType.typeOf(block.below(2)).isSolidAndSafe()) {
                         block = block_below;
                         type = type_below;
                     } else {
                         Location safe_landing = null;
                         for (int depth=2; depth<5; depth++) {
                             block_below = block.below(depth);
                             type_below = BlockType.typeOf(block_below);
 
                             if (!type_below.isSafe()) {
                                 reportBlock("Drop into unsafe tile.", block_below);
                                 return DANGEROUS;
                             } else if (type_below.isSolid()) {
                                 safe_landing = block_below;
                                 break;
                             }
                         }
 
                         if (safe_landing == null) {
                             reportBlock("Unsafe drop distance.", block_below);
                             return DANGEROUS;
                         } else {
                             required_blocks.add(safe_landing);
                             continue;
                         }
                     }
                 }
 
                 // block now contains the ground-level block.
                 if (room.origin == null) {
                     room.origin = block;
                 }
 
                 if (type.isLadder()) {
                     //reportBlock("Ladder", block);
                     boolean bottom_ok = false;
                     for (int depth=1; depth <= block.y; depth++) {
                         block_below = block.below(depth);
                         type_below = BlockType.typeOf(block_below);
                         room.blocks.put(block_below, type_below);
 
                         if (type_below.isLadder()) {
                             continue;
                         } else if (type_below.isSolidAndSafe()) {
                             if (type_below.isDoor()) {
                                 doors.add(block_below);
                             }
                             bottom_ok = true;
                             break;
                         } else {
                             reportBlock("Unsafe block below ladder.", block_below);
                             return DANGEROUS;
                         }
                     }
 
                     if (!bottom_ok) {
                         reportBlock("Ladder to the void.", block_below);
                         return DANGEROUS;
                     }
                     Location ladder_bottom = block_below;
                     BlockType ladder_bottom_type = type_below;
 
                     boolean top_ok = false;
                     for (int height=1; height + block.y < 255; height++) {
                         block_above = block.above(height);
                         type_above = BlockType.typeOf(block_above);
                         room.blocks.put(block_above, type_above);
 
                         if (type_above.isLadder()) {
                             continue;
                         } else if (!type_above.isSafe()) {
                             reportBlock("Unsafe block above ladder.", block_above);
                             return DANGEROUS;
                         } else {
                             room.blocks.put(block_above, type_above);
                             if (type_above.isDoor()) {
                                 doors.add(block_above);
                             }
                             top_ok = true;
                             break;
                         }
                     }
 
                     if (!top_ok) {
                         reportBlock("Ladder to space.", block_above);
                         return SPACE;
                     }
                     Location ladder_top = block_above;
                     BlockType ladder_top_type = type_above;
 
 
                     boolean[] had_floor = new boolean[] {false, false, false, false};
                     for (y=ladder_bottom.y - 1; y < ladder_top.y; y++) {
                         Location ladder = new Location(world, ladder_bottom.x, y, ladder_bottom.z);
                         if (y >= ladder_bottom.y) {
                             room.blocks.put(ladder, BlockType.LADDER_LIKE);
                         }
                         for (int dir=0; dir<4; dir++) {
                             Location neighbor = ladder.step(dir);
                             BlockType neighbor_type = room.blocks.get(neighbor);
 
                             //reportBlock("Next to ladder.", neighbor);
 
                             if (neighbor_type != null) {
                                 // We've already visited here, so just note down if it's a floor
                                 // and continue.
                                 had_floor[dir] = neighbor_type.isSolidAndSafe();
                                 continue;
                             }
 
                             neighbor_type = BlockType.typeOf(neighbor);
 
                             if (neighbor_type.isSolidAndSafe()) {
                                 had_floor[dir] = true;
                                 if (y > ladder_bottom.y) {
                                     //reportBlock("Ladder's wall.", neighbor);
                                     // Register it as a wall.
                                     room.blocks.put(neighbor, neighbor_type);
 
                                     // And a door, if applicable.
                                     if (neighbor_type.isDoor()) {
                                         doors.add(neighbor);
                                     }
                                 }
                                 continue;
                             } else if (had_floor[dir] && neighbor_type.isSafe()) {
                                 if (!BlockType.typeOf(neighbor.above()).isSolidOrUnsafe()) {
                                     if (y == ladder_bottom.y
                                         && !BlockType.typeOf(neighbor.above(2)).isSolidOrUnsafe()) {
                                         had_floor[dir] = false;
                                         continue;
                                     }
 
                                     new_columns.add(neighbor);
                                 }
                             }
 
                             had_floor[dir] = false;
                         }
                     }
 
 
                     if (ladder_top_type.isSolid()
                         || BlockType.typeOf(ladder_top.above()).isSolid()) {
                         continue;
                     }
 
                     block = ladder_top;
                 }
 
                 // Register the floor.
                 block_below = block.below();
                 room.blocks.put(block_below, BlockType.typeOf(block_below));
 
                 if (BlockType.typeOf(block_below).isDoor()) {
                     doors.add(block_below);
                 }
 
                 // Register what's above the floor, up to the ceiling.
                 for (int height=0; height<8; height++) {
                     // This will include block itself.
                     block_above = block.above(height);
                     type = BlockType.typeOf(block_above);
                     room.blocks.put(block_above, type);
 
                     if (type.isSolid()) {
                         break;
                     }
                 }
 
                 for (int dir=0; dir<4; dir++) {
                     Location neighbor = block.step(dir);
                     if (!room.blocks.containsKey(neighbor)) {
                         //reportBlock("Adding at spot 2", neighbor);
                         new_columns.add(neighbor);
                     }
                 }
             }
 
 
             // Register the walls
             for (Location wall_start : wall_columns) {
                 for (int depth=0; depth <= wall_start.y; depth++) {
                     Location wall = wall_start.below(depth);
                     BlockType wall_type = room.blocks.get(wall);
                     if (wall_type != null) {
                         // Already registered;
                         break;
                     }
 
                     wall_type = BlockType.typeOf(wall);
 
                     if (!wall_type.isSolidOrUnsafe()) {
                         break;
                     }
 
                     boolean found_room = false;
                     for (int dir=0; dir<6; dir++) {
                         Location neighbor = wall.step(dir);
                         BlockType neighbor_type = room.blocks.get(neighbor);
 
                         if (neighbor_type != null) {
                             // In the room.
                             if (!neighbor_type.isSolidOrUnsafe()) {
                                 // Empty block.
                                 found_room = true;
                             }
                         }
                     }
 
                     if (found_room) {
                         room.blocks.put(wall, wall_type);
 
                         if (wall_type.isDoor()) {
                             doors.add(wall);
                         }
                     }
                 }
             }
 
             // Add connections for doors.
             List<Location> unexplored_blocks = new ArrayList<Location>();
             for (Location door : doors) {
                 unexplored_blocks.clear();
                 for (int dir=0; dir<6; dir++) {
                     Location neighbor = door.step(dir);
                     BlockType neighbor_type = room.blocks.get(neighbor);
 
                     if (neighbor_type == null) {
                         // Not in the room.
                         neighbor_type = BlockType.typeOf(neighbor);
                         if (!neighbor_type.isSolidOrUnsafe()) {
                             unexplored_blocks.add(neighbor);
                         }
                     }
                 }
 
                 if (unexplored_blocks.size() > 0) {
                     for (Location connector : unexplored_blocks) {
                         room.connections.put(connector, null);
                     }
                 }
             }
 
             // Ensure that anywhere you can safely fall to is still in the room.
             for (Location landing : required_blocks) {
                 if (!room.containsBlock(landing)) {
                     reportBlock("Drop to outside room.", landing);
                     return NONCONTIGUOUS;
                 }
             }
 
             return room;
         }
 
         public List<Location> unexploredConnections() {
             // Get all the connections that don't have a known room to connect
             // to yet.
             List<Location> unexplored = new ArrayList<Location>();
 
             for (Location connector : connections.keySet()) {
                 if (connections.get(connector) == null) {
                     unexplored.add(connector);
                 }
             }
 
             return unexplored;
         }
 
         public boolean containsBlock(Location block) {
             // Is this location anywhere in the room?
             if (blocks.containsKey(block)) {
                 return true;
             }
 
             return false;
         }
 
         public void connect(List<Room> known_rooms) {
             // For each connector...
             for (Location connector : connections.keySet()) {
                 // ...that doesn't know where it connects...
                 if (connections.get(connector) == null) {
                     // ...see if it connects to any of the known rooms.
                     for (Room neighbor : known_rooms) {
                         // If it does...
                         if (neighbor.containsBlock(connector)) {
                             // ...note the connection.
                             connections.put(connector, neighbor);
                             break;
                         }
                     }
                 }
             }
         }
 
         public void explore(Location connector, List<Room> recent_additions) {
             // Explore the room at this connector, and add it to recent_additions.
 
             if (!connections.containsKey(connector)
                 || connections.get(connector) != null) {
                 // This isn't an unexplored connector.
                 return;
             }
 
             for (Room candidate : recent_additions) {
                 if (candidate.containsBlock(connector)) {
                     // Nothing to do, just record the connection.
                     connections.put(connector, candidate);
                     return;
                 }
             }
 
             // Seems to be a new room; explore it.
             Room neighbor = detectRoom(connector);
             connections.put(connector, neighbor);
 
             if (neighbor.valid) {
                 recent_additions.add(neighbor);
             }
         }
     }
 
     public static class Location {
         World world;
         public int x;
         public int y;
         public int z;
 
         public Location(World world, int x, int y, int z) {
             this.world = world;
             this.x = x;
             this.y = y;
             this.z = z;
         }
 
         public Location above(int dist) {
             return new Location(world, x, y+dist, z);
         }
 
         public Location below(int dist) {
             return new Location(world, x, y-dist, z);
         }
 
         public Location above() {
             return above(1);
         }
 
         public Location below() {
             return below(1);
         }
 
         public Location step(int dir) {
             int new_x = x;
             int new_y = y;
             int new_z = z;
 
             if (dir == 0) {
                 new_x++;
             } else if (dir == 1) {
                 new_z++;
             } else if (dir == 2) {
                 new_x--;
             } else if (dir == 3) {
                 new_z--;
             } else if (dir == 4) {
                 new_y++;
             } else if (dir == 5) {
                 new_y--;
             } else {
                 throw new IllegalArgumentException("Bad direction.");
             }
 
             return new Location(world, new_x, new_y, new_z);
         }
 
         public int hashCode() {
             int hash = 0;
             hash += (x & 0xff);
             hash += (y & 0xff) << 8;
             hash += (z & 0xff) << 16;
 
             return hash;
         }
 
         public boolean equals(Object other) {
             if (other instanceof Location) {
                 Location other_loc = (Location) other;
                 return (world == other_loc.world && x == other_loc.x && y == other_loc.y
                         && z == other_loc.z);
             } else {
                 return false;
             }
         }
     }
 
     public static class BlockType {
         public static final int PLAIN     = 0;
 
         public static final int DOOR        = 1;
         public static final int LADDER      = 2;
         public static final int FENCE       = 4;
         public static final int FAKE        = 8;
         public static final int FLAG        = 16;
         public static final int TILE_ENTITY = 32;
 
         public static BlockType AIR_LIKE    = new BlockType(false, true,  PLAIN);
         public static BlockType STONE_LIKE  = new BlockType(true,  true,  PLAIN);
         public static BlockType ABOVE_FENCE = new BlockType(true,  true,  FAKE);
         public static BlockType LAVA_LIKE   = new BlockType(false, false, PLAIN);
         public static BlockType CACTUS_LIKE = new BlockType(true,  false, PLAIN);
         public static BlockType DOOR_LIKE   = new BlockType(true,  true,  DOOR);
         public static BlockType LADDER_LIKE = new BlockType(false, true,  LADDER);
         public static BlockType FENCE_LIKE  = new BlockType(false, true,  FENCE);
         public static BlockType HOUSE_FLAG  = new BlockType(false, true,  FLAG);
         public static BlockType CHEST_LIKE  = new BlockType(true,  true,  TILE_ENTITY);
 
         boolean solid;
         boolean safe;
         int flags;
         public BlockType(boolean solid, boolean safe, int flags) {
             this.solid = solid;
             this.safe = safe;
             this.flags = flags;
         }
 
         public boolean isSolid() {
             return solid;
         }
 
         public boolean isSafe() {
             return safe;
         }
 
         public boolean isSolidAndSafe() {
             return solid && safe;
         }
 
         public boolean isSolidOrUnsafe() {
             return solid || !safe;
         }
 
         public boolean isLadder() {
             return (flags & LADDER) == LADDER;
         }
 
         public boolean isDoor() {
             return (flags & DOOR) == DOOR;
         }
 
         public boolean isFence() {
             return (flags & FENCE) == FENCE;
         }
 
         public boolean isFake() {
             return (flags & FAKE) == FAKE;
         }
 
         public boolean isFlag() {
             return (flags & FLAG) == FLAG;
         }
 
         public boolean isInteresting() {
             // We may add more flags to the "interesting" list later.
             return (flags & (TILE_ENTITY)) > 0;
         }
 
         public boolean equals(Object other) {
             if (other instanceof BlockType) {
                 BlockType other_type = (BlockType) other;
 
                 if (solid == other_type.solid && safe == other_type.safe
                     && flags == other_type.flags) {
                     return true;
                 }
             }
 
             return false;
         }
 
         public static BlockType typeOf(Location loc) {
             return typeOf(loc, true);
         }
 
         public static BlockType typeOf(Location loc, boolean check_for_fence) {
             int id = loc.world.getBlockId(loc.x, loc.y, loc.z);
             int meta = loc.world.getBlockMetadata(loc.x, loc.y, loc.z);
 
             if (id == 0) {
                 if (check_for_fence) {
                     return fenceCheck(loc);
                 } else {
                     return AIR_LIKE;
                 }
             }
 
             BlockType override = Wustendorf.getTypeOverride(id, meta);
 
             if (override != null) {
                 return override;
             }
 
             Block block = Block.blocksList[id];
 
             if (block instanceof WustendorfMarker) {
                 return HOUSE_FLAG;
             } else if (block.isLadder(loc.world, loc.x, loc.y, loc.z)) {
                 return LADDER_LIKE;
             } else if (id == Block.doorWood.blockID
                        || id == Block.trapdoor.blockID) {
                 return DOOR_LIKE;
             }
 
 
             int flags = PLAIN;
 
             if (block.hasTileEntity(meta)) {
                 flags |= TILE_ENTITY;
             }
 
             AxisAlignedBB bb = block.getCollisionBoundingBoxFromPool(loc.world, loc.x, loc.y, loc.z);
             boolean solid = (bb != null);
 
             if (solid) {
                 Vec3 above = Vec3.createVectorHelper(loc.x+0.5, loc.y+1.3, loc.z+0.5);
                 if (bb.isVecInside(above)) {
                     flags |= FENCE;
                 }
             }
 
             boolean safe = true;
             if (   id == Block.fire.blockID
                 || id == Block.lavaMoving.blockID
                 || id == Block.lavaStill.blockID
                 || id == Block.cactus.blockID) {
                 safe = false;
             }
 
             BlockType type = new BlockType(solid, safe, flags);
 
             if (check_for_fence && type.equals(AIR_LIKE)) {
                 return fenceCheck(loc);
             }
 
             return type;
         }
 
         public static BlockType fenceCheck(Location loc) {
             BlockType type_below = typeOf(loc.below(), false);
             if (type_below.isFence()) {
                 return ABOVE_FENCE;
             }
 
             return AIR_LIKE;
         }
     }
 }
