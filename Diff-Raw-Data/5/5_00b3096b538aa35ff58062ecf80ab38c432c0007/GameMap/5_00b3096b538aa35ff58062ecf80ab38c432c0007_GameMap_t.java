 package de.hsa.otma.android.map;
 
 import de.hsa.otma.android.R;
 
 import java.util.HashMap;
 import java.util.Map;
 
 public class GameMap {
     public static final GameMap INSTANCE = new GameMap();
     private Map<Coordinate, GameMapItem> mapItems = new HashMap<Coordinate, GameMapItem>();
 
     private GameMap() {
         buildMap();
     }
 
     private void buildMap() {
         GameMapItem map1x1 = createGameMapItemAndPutToMap(1, 1, R.drawable.map_1x1);
         GameMapItem map1x2 = createGameMapItemAndPutToMap(2, 1, R.drawable.map_1x2);
         GameMapItem map1x3 = createGameMapItemAndPutToMap(3, 1, R.drawable.map_1x3);
         GameMapItem map1x4 = createGameMapItemAndPutToMap(4, 1, R.drawable.map_1x4);
         GameMapItem map1x5 = createGameMapItemAndPutToMap(5, 1, R.drawable.map_1x5);
         GameMapItem map2x1 = createGameMapItemAndPutToMap(1, 2, R.drawable.map_2x1);
         GameMapItem map2x2 = createGameMapItemAndPutToMap(2, 2, R.drawable.map_2x2);
         GameMapItem map2x3 = createGameMapItemAndPutToMap(3, 2, R.drawable.map_2x3);
         GameMapItem map2x4 = createGameMapItemAndPutToMap(4, 2, R.drawable.map_2x4);
         GameMapItem map2x5 = createGameMapItemAndPutToMap(5, 2, R.drawable.map_2x5);
         GameMapItem map3x1 = createGameMapItemAndPutToMap(1, 3, R.drawable.map_3x1);
         GameMapItem map3x2 = createGameMapItemAndPutToMap(2, 3, R.drawable.map_3x2);
         GameMapItem map3x3 = createGameMapItemAndPutToMap(3, 3, R.drawable.map_3x3);
         GameMapItem map3x4 = createGameMapItemAndPutToMap(4, 3, R.drawable.map_3x4);
         GameMapItem map3x5 = createGameMapItemAndPutToMap(5, 3, R.drawable.map_3x5);
         GameMapItem map4x1 = createGameMapItemAndPutToMap(1, 4, R.drawable.map_4x1);
         GameMapItem map4x2 = createGameMapItemAndPutToMap(2, 4, R.drawable.map_4x2);
         GameMapItem map4x3 = createGameMapItemAndPutToMap(3, 4, R.drawable.map_4x3);
         GameMapItem map4x4 = createGameMapItemAndPutToMap(4, 4, R.drawable.map_4x4);
         GameMapItem map4x5 = createGameMapItemAndPutToMap(5, 4, R.drawable.map_4x5);
         GameMapItem map5x1 = createGameMapItemAndPutToMap(1, 5, R.drawable.map_5x1);
         GameMapItem map5x2 = createGameMapItemAndPutToMap(2, 5, R.drawable.map_5x2);
         GameMapItem map5x3 = createGameMapItemAndPutToMap(3, 5, R.drawable.map_5x3);
         GameMapItem map5x4 = createGameMapItemAndPutToMap(4, 5, R.drawable.map_5x4);
         GameMapItem map5x5 = createGameMapItemAndPutToMap(5, 5, R.drawable.map_5x5);
 
         map1x1.setBoundaryItems(null, null, map2x1, null);
         map1x2.setBoundaryItems(null, map1x3, map2x2, null);
         map1x3.setBoundaryItems(null, map1x4, map2x3, map1x2);
         map1x4.setBoundaryItems(null, map1x5, null, map1x3);
         map1x5.setBoundaryItems(null, null, null, map1x4);
 
         map2x1.setBoundaryItems(map1x1, map2x2, map3x1, null);
         map2x2.setBoundaryItems(map1x2, null, null, map2x1);
         map2x3.setBoundaryItems(map1x3, null, map3x3, null);
         map2x4.setBoundaryItems(null, map2x5, null, null);
         map2x5.setBoundaryItems(null, null, map3x5, map2x4);
 
        map3x1.setBoundaryItems(map2x1, null, map4x1, null);
         map3x2.setBoundaryItems(null, map3x3, map4x2, null);
         map3x3.setBoundaryItems(map2x3, map3x4, null, map3x2);
        map3x4.setBoundaryItems(null, map3x5, null, map3x3);
         map3x5.setBoundaryItems(map2x5, null, map4x5, map3x4);
 
         map4x1.setBoundaryItems(map3x1, map4x2, map5x1, null);
         map4x2.setBoundaryItems(map3x2, map4x3, null, map4x1);
         map4x3.setBoundaryItems(null, null, map5x3, map4x2);
         map4x4.setBoundaryItems(null, map4x5, map5x4, null);
         map4x5.setBoundaryItems(map3x5, null, map5x5, map4x4);
 
         map5x1.setBoundaryItems(map4x1, map5x2, null, null);
         map5x2.setBoundaryItems(null, map5x3, null, map5x1);
         map5x3.setBoundaryItems(map4x3, map5x4, null, map5x2);
         map5x4.setBoundaryItems(map4x4, null, null, map5x3);
         map5x5.setBoundaryItems(map4x5, null, null, null);
     }
     
     private GameMapItem createGameMapItemAndPutToMap(int x, int y, int drawable) {
         Coordinate coordinate = new Coordinate(x, y);
         GameMapItem gameMapItem = new GameMapItem(coordinate, drawable);
         mapItems.put(coordinate, gameMapItem);
 
         return gameMapItem;
     }
     
     public GameMapItem getMapItemFor(Coordinate coordinate) {
         return mapItems.get(coordinate);
     }
 }
