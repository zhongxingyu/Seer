 package org.glowacki.core.dungen;
 
 import java.util.Random;
 
 public abstract class RoomGenerator2
 {
     private static final boolean DEBUG = false;
 
     enum Direction {
         LEFT,
         TOP,
         RIGHT,
         BOTTOM;
 
         static Direction get(int i)
         {
             switch (i % 4) {
             case 0:
                 return LEFT;
             case 1:
                 return TOP;
             case 2:
                 return RIGHT;
             default:
                 return BOTTOM;
             }
         }
     };
 
     public static Room[] createRooms(Random random, int width, int height,
                                      int gridWidth, int gridHeight)
     {
         final int zoneWidth = (width / gridWidth) - 2;
         final int zoneHeight = (height / gridHeight) - 2;
 
         Room[] rooms = new Room[gridWidth * gridHeight];
 
         int n = 0;
         for (int gx = 0; gx < gridWidth; gx++) {
             for (int gy = 0; gy < gridHeight; gy++) {
                 rooms[n] = new Room(n, gx * zoneWidth, gy * zoneHeight,
                                     zoneWidth, zoneHeight, null);
                 n++;
             }
         }
 
         for (int i = 0; i < 100; i++) {
             Room room = rooms[random.nextInt(rooms.length)];
 
             final int rnd = random.nextInt(3);
             if (rnd == 0) {
 if(DEBUG)System.out.println("Shrink " + room);
                 shrinkRoom(room, rooms, random);
             } else if (rnd == 1) {
 if(DEBUG)System.out.println("Grow " + room);
                 growRoom(room, rooms, width, height, random);
             } else {
 if(DEBUG)System.out.println("Move " + room);
                 moveRoom(room, rooms, width, height, random);
             }
         }
 
         return rooms;
     }
 
     private static void dump(String name, Room[] rooms)
     {
         System.out.println("===== " + name + " =====");
         new CharMap(rooms).show();
     }
 
     private static void growRoom(Room room, Room[] rooms, int roomWidth,
                                  int roomHeight, Random random)
     {
         final Direction dir;
         if (room.getWidth() < room.getHeight()) {
             if (random.nextBoolean()) {
                 dir = Direction.LEFT;
             } else {
                 dir = Direction.RIGHT;
             }
         } else {
             if (random.nextBoolean()) {
                 dir = Direction.TOP;
             } else {
                 dir = Direction.BOTTOM;
             }
         }
 
         if (hasNeighbor(room, rooms, dir)) {
             return;
         }
 
         switch (dir) {
         case LEFT:
             // grow left
             if (room.getX() > 0) {
                 room.decX();
                 room.changeWidth(1);
 if(DEBUG)dump("  Grow " + room + " left", rooms);
             }
             break;
         case TOP:
             // grow up
             if (room.getY() > 0) {
                 room.decY();
                 room.changeHeight(1);
 if(DEBUG)dump("  Grow " + room + " up", rooms);
             }
             break;
         case RIGHT:
             // grow right
             if (room.getX() < roomWidth - 1) {
                 room.changeWidth(1);
 if(DEBUG)dump("  Grow " + room + " right", rooms);
             }
             break;
         case BOTTOM:
             // grow down
             if (room.getY() < roomHeight - 1) {
                 room.changeHeight(1);
 if(DEBUG)dump("  Grow " + room + " down", rooms);
             }
             break;
         }
     }
 
     private static boolean hasNeighbor(Room room, Room[] rooms, Direction dir)
     {
         for (int i = 0; i < rooms.length; i++) {
             boolean isNeighbor;
             switch (dir) {
             case LEFT:
                 isNeighbor =
                     room.getX() == rooms[i].getX() + rooms[i].getWidth();
                 break;
             case TOP:
                 isNeighbor =
                    room.getY() == rooms[i].getY() + rooms[i].getHeight();
                 break;
             case RIGHT:
                 isNeighbor =
                    rooms[i].getX() == room.getX() + room.getWidth();
                 break;
             default:
                 isNeighbor =
                    rooms[i].getY() == room.getY() + room.getHeight();
                 break;
             }
 
             if (isNeighbor) {
                 return true;
             }
         }
 
         return false;
     }
 
     private static void moveRoom(Room room, Room[] rooms, int roomWidth,
                                  int roomHeight, Random random)
     {
         final Direction dir = Direction.get(random.nextInt(4));
         if (hasNeighbor(room, rooms, dir)) {
             return;
         }
 
         switch (dir) {
         case LEFT:
             // move left
             if (room.getX() > 0) {
                 room.decX();
 if(DEBUG)dump("  Move " + room + " left", rooms);
             }
             break;
         case TOP:
             // move up
             if (room.getY() > 0) {
                 room.decY();
 if(DEBUG)dump("  Move " + room + " up", rooms);
             }
             break;
         case RIGHT:
             // move right
             if (room.getX() < roomWidth - 1) {
                 room.incX();
 if(DEBUG)dump("  Move " + room + " right", rooms);
             }
             break;
         case BOTTOM:
             // move down
             if (room.getY() < roomHeight - 1) {
                 room.incY();
 if(DEBUG)dump("  Move " + room + " down", rooms);
             }
             break;
         }
     }
 
     private static void shrinkRoom(Room room, Room[] rooms, Random random)
     {
         final Direction dir;
         if (room.getWidth() > room.getHeight()) {
             if (random.nextBoolean()) {
                 dir = Direction.LEFT;
             } else {
                 dir = Direction.RIGHT;
             }
         } else {
             if (random.nextBoolean()) {
                 dir = Direction.TOP;
             } else {
                 dir = Direction.BOTTOM;
             }
         }
 
         switch (dir) {
         case LEFT:
             // shrink left
             if (room.getWidth() > 4) {
                 room.incX();
                 room.changeWidth(-1);
 if(DEBUG)dump("  Shrink " + room + " left", rooms);
             }
             break;
         case TOP:
             // shrink up
             if (room.getHeight() > 4) {
                 room.incY();
                 room.changeHeight(-1);
 if(DEBUG)dump("  Shrink " + room + " up", rooms);
             }
             break;
         case RIGHT:
             // shrink right
             if (room.getWidth() > 4) {
                 room.changeWidth(-1);
 if(DEBUG)dump("  Shrink " + room + " right", rooms);
             }
             break;
         case BOTTOM:
             // shrink down
             if (room.getHeight() > 4) {
                 room.changeHeight(-1);
 if(DEBUG)dump("  Shrink " + room + " down", rooms);
             }
             break;
         }
     }
 }
