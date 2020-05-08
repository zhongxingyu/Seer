 
 package com.wartricks.utils;
 
 import com.badlogic.gdx.graphics.OrthographicCamera;
 import com.badlogic.gdx.math.Vector3;
 import com.badlogic.gdx.utils.Array;
 import com.wartricks.boards.GameMap;
 import com.wartricks.custom.FloatPair;
 import com.wartricks.custom.MyMath;
 import com.wartricks.custom.Pair;
 
 public class MapTools {
     public String name;
 
     private GameMap gameMap;
 
     public MapTools(String regionName, GameMap map) {
         name = regionName;
         gameMap = map;
     }
 
     public Array<Pair> getNeighbors(int x, int y, int range) {
         final Array<Pair> coordinates = new Array<Pair>();
         int min;
         int myrow;
         for (int row = y - range; row < (y + range + 1); row++) {
             min = MyMath.min(2 * ((row - y) + range), range, (-2 * (row - y - range)) + 1);
             for (int col = x - min; col < (x + min + 1); col++) {
                 if ((col < 0) || (col >= gameMap.width)) {
                     continue;
                 }
                 if ((x == col) && (y == row)) {
                     continue;
                 } else if ((x % 2) == 0) {
                     myrow = (2 * y) - row;
                 } else {
                     myrow = row;
                 }
                 if ((myrow < 0) || (myrow >= gameMap.height)) {
                     continue;
                 }
                 coordinates.add(new Pair(col, myrow));
             }
         }
         return coordinates;
     }
 
     public Array<Pair> getNeighbors(int x, int y) {
         return this.getNeighbors(x, y, 1);
     }
 
     public int distance(int x0, int y0, int x1, int y1) {
         final int dx = Math.abs(x1 - x0);
         final int dy = Math.abs(y1 - y0);
         // The distance can be tricky, because of how the columns are shifted.
         // Different cases must be considered, because the dx and dy above
         // are not sufficient to determine distance.
         if (((dx) % 2) == 0) { // distance from even->even or odd->odd column
                                // important to know since evens and odds are offset
             return MyMath.max(dx, (dx / 2) + dy);
         }
         // Otherwise the distance must be even->odd
         else if ((((x0 % 2) == 0) && (y0 > y1)) || (((x1 % 2) == 0) && (y1 > y0))) { // even on top
             return MyMath.max(dx, ((dx - 1) / 2) + dy);
         }
         // otherwise odd must be on top
         return MyMath.max(dx, ((dx + 1) / 2) + dy);
     }
 
     public Pair window2world(float x, float y, OrthographicCamera camera) {
         final Vector3 pos = new Vector3(x, y, 0);
         camera.unproject(pos);
         float posx = ((pos.x - 6f) / gameMap.colSize);
         float posy = (((pos.y + 8f) - ((gameMap.rowSize * (posx % 2)) / 2)) / gameMap.rowSize);
         // Avoids bug in range (0, -1) where it would round to 0
         if (posx < 0) {
             posx -= 1;
         }
         if ((posy < 0)) {
             posy -= 1;
         }
         return new Pair((int)posx, (int)posy);
     }
 
     public Pair libgdx2world(float x, float y) {
         final Vector3 pos = new Vector3(x, y, 0);
         float posx = ((pos.x - 6f) / gameMap.colSize);
         float posy = (((pos.y) - ((gameMap.rowSize * (posx % 2)) / 2)) / gameMap.rowSize);
         // Avoids bug in range (0, -1) where it would round to 0
         if (posx < 0) {
             posx -= 1;
         }
         if ((posy < 0)) {
             posy -= 1;
         }
         return new Pair((int)posx, (int)posy);
     }
 
     public FloatPair world2window(float x, float y) {
         final int x0 = (int)x;
         final float dx = x - x0; // purely the decimal part
         final float posX = 5.5f + ((x + 0.5f) * gameMap.colSize);
         final float posY = gameMap.rowSize
                 * (y + 0.5f + ((x0 % 2) * (0.5f - (dx / 2f))) + ((((x0 + 1) % 2) * dx) / 2f));
         return new FloatPair(posX, posY);
     }
 
     public FloatPair getDirectionVector(int x1, int y1, int x2, int y2) {
         final FloatPair cell1 = this.world2window(x1, y1);
         final FloatPair cell2 = this.world2window(x2, y2);
         return new FloatPair(cell2.x - cell1.x, cell2.y - cell1.y);
     }
 
     public Array<Pair> getReachableCells(int x, int y, int minRange, int maxRange) {
         final Array<Pair> unvisited = new Array<Pair>();
         final Array<Pair> visited = new Array<Pair>();
         final Pair start = new Pair(x, y);
         unvisited.add(start);
         while (unvisited.size > 0) {
             final Pair current = unvisited.pop();
             for (final Pair neighbor : this.getNeighbors(current.x, current.y)) {
                 final int distance = this.distance(x, y, neighbor.x, neighbor.y);
                 if (distance <= maxRange) {
                     if (!visited.contains(neighbor, false)) {
                         visited.add(neighbor);
                         if (!unvisited.contains(neighbor, false)) {
                             unvisited.add(neighbor);
                         }
                     }
                 }
             }
         }
         final Array<Pair> highlights = new Array<Pair>();
         for (final Pair cell : visited) {
             final int distance = this.distance(x, y, cell.x, cell.y);
             if (distance >= minRange) {
                 highlights.add(cell);
             }
         }
         return highlights;
     }
 
     public Array<Pair> getLOSCells(float x, float y, float x0, float y0) {
         return this.getLOSCells((int)x, (int)y, (int)x0, (int)y0);
     }
 
     public Array<Pair> getLOSCells(int x, int y, int x0, int y0) {
         final Array<Pair> highlights = new Array<Pair>();
         final int[] cubeCoordsOrigin = MapTools.coordOffset2Cube(x, y);
         final int[] cubeCoordsDestination = MapTools.coordOffset2Cube(x0, y0);
         final int dx = cubeCoordsOrigin[0] - cubeCoordsDestination[0];
         final int dy = cubeCoordsOrigin[1] - cubeCoordsDestination[1];
         final int dz = cubeCoordsOrigin[2] - cubeCoordsDestination[2];
         float distance = Math.max(Math.abs(dx - dy), Math.abs(dy - dz));
         distance = Math.max(distance, Math.abs(dz - dx));
         if (distance > 0) {
             int[] previousCoord = new int[3];
             for (float i = 0; i <= distance; i++) {
                 final float currentX = (cubeCoordsOrigin[0] * (i / distance))
                         + (cubeCoordsDestination[0] * (1 - (i / distance)));
                 final float currentY = (cubeCoordsOrigin[1] * (i / distance))
                         + (cubeCoordsDestination[1] * (1 - (i / distance)));
                 final float currentZ = (cubeCoordsOrigin[2] * (i / distance))
                         + (cubeCoordsDestination[2] * (1 - (i / distance)));
                 final int[] currentCoord = roundCubeCoord(currentX, currentY, currentZ);
                 if (!currentCoord.equals(previousCoord)) {
                     final int[] offsetCoord = MapTools.coordCube2Offset(currentCoord[0],
                             currentCoord[1], currentCoord[2]);
                     highlights.add(new Pair(offsetCoord[0], offsetCoord[1]));
                     previousCoord = currentCoord;
                 }
             }
         }
         return highlights;
     }
 
     public static int[] coordOffset2Cube(int x, int y) {
         final int[] coord = new int[3];
         coord[0] = x;
         coord[2] = y - ((x + (x % 2)) / 2);
         coord[1] = -x - coord[2];
         return coord;
     }
 
     public static int[] coordCube2Offset(int x, int y, int z) {
         final int[] coord = new int[2];
         coord[0] = x;
         coord[1] = (z + ((x + (x % 2)) / 2));
         return coord;
     }
 
     public static int[] roundCubeCoord(double x, double y, double z) {
         float rx = Math.round(x - 0.0001f);
         float ry = Math.round(y - 0.0001f);
         float rz = Math.round(z - 0.0001f);
         final int s = (int)(rx + ry + rz);
         if (s != 0) {
             final float x_err = (float)Math.abs(rx - x) + 0.001f;
             final float y_err = (float)Math.abs(ry - y);
             final float z_err = (float)Math.abs(rz - z);
             if ((x_err > y_err) && (x_err > z_err)) {
                 rx -= s;
             } else if (y_err > z_err) {
                 ry -= s;
             } else {
                 rz -= s;
             }
         }
         return new int[] {
                 (int)rx, (int)ry, (int)rz
         };
     }
 }
