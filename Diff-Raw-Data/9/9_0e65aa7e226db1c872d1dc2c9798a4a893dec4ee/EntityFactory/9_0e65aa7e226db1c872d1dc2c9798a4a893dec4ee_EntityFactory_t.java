 package com.jcerise.game.fantstrat;
 
 import com.artemis.Entity;
 import com.artemis.World;
 import com.jcerise.game.fantstrat.components.MapPosition;
 import com.jcerise.game.fantstrat.components.Tile;
 import com.jcerise.game.fantstrat.map.GameMap;
 
 public class EntityFactory {
 
     public static Entity createMapTile(World world, int x, int y, GameMap gameMap) {
         Entity e = world.createEntity();
         String type = "None";
         String name = "None";
 
         e.addComponent(new MapPosition(x, y));
 
         switch (gameMap.map[x][y]) {
             case 0:
                 type = "Deep Water";
                 name = "Deep Water";
                 break;
             case 1:
                 type = "Shallow Water";
                 name = "Shallow Water";
                 break;
             case 2:
                 type = "Desert";
                 name = "Desert";
                 break;
             case 3:
                 type = "Plains";
                 name = "Plains";
                 break;
             case 4:
                 type = "Grassland";
                 name = "Grassland";
                 break;
             case 5:
                 type = "Forest";
                 name = "Forest";
                 break;
             case 6:
                 type = "Hills";
                 name = "Hills";
                 break;
             case 7:
                 type = "Mountains";
                 name = "Mountains";
                 break;
             case 8:
                type = "High Mountains";
                name = "High Mountains";
                 break;
         }
 
         e.addComponent(new Tile(type, gameMap.map[x][y], name));
         gameMap.entityLocations[x][y] = e.getId();
 
         return e;
     }
 
 }
