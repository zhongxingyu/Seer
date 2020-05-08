 package com.icbat.game.tradesong.screens.stages;
 
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.graphics.Camera;
 import com.badlogic.gdx.maps.*;
 import com.badlogic.gdx.maps.tiled.TiledMap;
 import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
 import com.badlogic.gdx.scenes.scene2d.InputEvent;
 import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
 import com.badlogic.gdx.utils.Timer;
 import com.icbat.game.tradesong.Tradesong;
 import com.icbat.game.tradesong.gameObjects.Item;
 import com.icbat.game.tradesong.observation.notifications.GatherNotification;
 import com.icbat.game.tradesong.observation.notifications.StopNotification;
 import com.icbat.game.tradesong.observation.watchers.GatheringWatcher;
 import com.icbat.game.tradesong.screens.listeners.DragMoveListener;
 import com.icbat.game.tradesong.utils.Constants;
 import com.icbat.game.tradesong.utils.Point;
 
 import java.util.*;
 
 public class MapStage extends BaseStage {
     public static final String SPAWNABLE_ITEMS_KEY = "spawnableItems";
     public static final String INITIAL_SPAWN_COUNT_KEY = "initialSpawnCount";
     private List<ValidSpawnArea> spawnAreas = new LinkedList<ValidSpawnArea>();
     private Timer spawnTimer = new Timer();
     private int maxItemsOnMap = 0;
 
     public MapStage(TiledMap map) {
         MapProperties mapProperties = map.getProperties();
         setupAreas(map);
         maxItemsOnMap = getMaxItems(mapProperties);
         try {
             Integer initialSpawnedItems = Integer.parseInt((String) mapProperties.get(INITIAL_SPAWN_COUNT_KEY));
             spawnInitialItems(initialSpawnedItems);
         } catch (NumberFormatException nfe) {
             Gdx.app.log("Could not determine how many items to spawn initially", "map property " + INITIAL_SPAWN_COUNT_KEY + "likely missing.", nfe);
         }
 
     }
 
     @Override
     public void layout() {
         super.layout();
         notificationCenter.addWatcher(new GatheringWatcher());
         startSpawnTimer();
     }
 
     private void startSpawnTimer() {
         this.spawnTimer.stop();
         this.spawnTimer.clear();
         this.spawnTimer.scheduleTask(new Timer.Task() {
             @Override
             public void run() {
                 addRandomItem();
             }
         }, 3, 3);
         this.spawnTimer.start();
     }
 
     private Integer getMaxItems(MapProperties mapProperties) {
        String maxSpawnsBlob = (String) mapProperties.get("maxSpawnCapacity");
         if (maxSpawnsBlob != null) {
             return Integer.parseInt(maxSpawnsBlob);
         }
         Gdx.app.log("Could not find map attribute maxSpawnCapacity", "Defaulting to 0");
         return 0;
     }
 
     private void setupAreas(TiledMap map) {
         for (MapLayer layer : map.getLayers()) {
             if (layer.getName().startsWith("#SPAWN")) {
                 TiledMapTileLayer spawnLayer = (TiledMapTileLayer) layer;
                 List<Point> spawnableTiles = parseValidSpawnAreaZone(spawnLayer);
                 List<Item> spawnableItems = new LinkedList<Item>();
                 try {
                     spawnableItems = parseSpawnableItems(spawnLayer);
                 } catch (NullPointerException npe) {
                     Gdx.app.error("Layer " + layer.getName(), "has no spawnableItems attribute or found no items", npe);
                 }
 
                 Gdx.app.debug(layer.getName(), "found " + spawnableTiles.size() + " spawnable tiles ");
                 spawnAreas.add(new ValidSpawnArea(spawnableTiles, spawnableItems));
             }
         }
     }
 
     private List<Point> parseValidSpawnAreaZone(TiledMapTileLayer spawnLayer) {
         // This feels bad. Maybe there's a better way to do this by extending GDX?
         List<Point> spawnableTiles = new LinkedList<Point>();
         for (int x=0; x <= spawnLayer.getWidth(); ++x) {
             for (int y=0; y <= spawnLayer.getHeight(); ++y) {
                 if (spawnLayer.getCell(x, y) != null) {
                     spawnableTiles.add(new Point(x, y));
                 }
             }
         }
         return spawnableTiles;
     }
 
     private List<Item> parseSpawnableItems(TiledMapTileLayer spawnLayer) {
         String spawnableItemsBlob = (String) spawnLayer.getProperties().get(SPAWNABLE_ITEMS_KEY);
         String[] splitItems = spawnableItemsBlob.split(",");
         List<Item> spawnableItems = new LinkedList<Item>();
         for (String itemName : splitItems) {
             spawnableItems.add(Tradesong.itemPrototypes.get(itemName));
         }
         return spawnableItems;
     }
 
     private void spawnInitialItems(int itemsToSpawn) {
         for (int i=0; i < itemsToSpawn; ++i) {
             addRandomItem();
         }
     }
 
     private boolean addRandomItem() {
         Random random = new Random();
         int index = random.nextInt(spawnAreas.size());
         Item item = spawnAreas.get(index).spawnItem();
         return addItemToMap(item, maxItemsOnMap);
 
     }
 
     /**
      * Checks for capacity, and adds item to map if possible
      *
      * @return true if the item was successfully added.
      * */
     private boolean addItemToMap(Item item, int maxItemsOnMap) {
         if (this.getActors().size < maxItemsOnMap) {
             Gdx.app.debug("spawning item", item.getName());
             this.addActor(item);
             return true;
         }
         return false;
     }
 
     public void setDragListener(Camera camera) {
         Gdx.app.debug("", "Setting a drag listener");
         this.addListener(new DragMoveListener(camera));
     }
 
     @Override
     public void hide() {
         spawnTimer.stop();
         spawnTimer.clear();
         notificationCenter.notifyWatchers(new StopNotification());
     }
 
     /**
      * Non-rectangular area defined by all the tiles on a map layer whose name starts with #SPAWN
      */
     class ValidSpawnArea {
 
         private final int ICON_SIZE = Constants.SPRITE_DIMENSION.value();
         private final List<Point> spawnableTiles;
         private final List<Item> spawnableItems;
 
         public ValidSpawnArea(List<Point> spawnableTiles, List<Item> spawnableItems) {
             this.spawnableTiles = spawnableTiles;
             this.spawnableItems = spawnableItems;
         }
 
         /**
          * @return the Item to spawn regardless of max capacity
          * */
         public Item spawnItem() {
             if (!spawnableItems.isEmpty() && !spawnableTiles.isEmpty()) {
                 Item itemToSpawn = getRandomItem(this.spawnableItems);
                 Point spawnPoint = getRandomSpawnPoint(this.spawnableTiles);
                 itemToSpawn.setPosition(spawnPoint.getX() * ICON_SIZE, spawnPoint.getY() * ICON_SIZE);
                 itemToSpawn.addListener(new GatherClickListener(itemToSpawn));
                 return itemToSpawn;
             }
 
             return null;
         }
 


         private Item getRandomItem(List<Item> spawnableItems) {
             Random random = new Random();
             int i = random.nextInt(spawnableItems.size());
             return new Item(spawnableItems.get(i));
         }
 
         private Point getRandomSpawnPoint(List<Point> spawnableTiles) {
             Random random = new Random();
             int i = random.nextInt(spawnableTiles.size());
             return spawnableTiles.get(i);
         }
     }
 
     /**
      * Class to handle touching/clicking of items on levels.
      */
     class GatherClickListener extends ClickListener {
         Item owner;
 
         GatherClickListener(Item owner) {
             this.owner = owner;
         }
 
         @Override
         public void clicked(InputEvent event, float x, float y) {
             notificationCenter.notifyWatchers(new GatherNotification(owner));
         }
 
     }
 }
