 package com.icbat.game.tradesong.stages;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.audio.Sound;
 import com.badlogic.gdx.maps.MapProperties;
 import com.badlogic.gdx.scenes.scene2d.Actor;
 import com.badlogic.gdx.scenes.scene2d.InputEvent;
 import com.badlogic.gdx.scenes.scene2d.Touchable;
 import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
 import com.badlogic.gdx.utils.Timer;
 import com.icbat.game.tradesong.Item;
 import com.icbat.game.tradesong.Tradesong;
 
 import java.util.ArrayList;
 import java.util.Random;
 
 /** This stage governs:
  *  - items
  *  - the draggable, clear background
  *  - it's also a nice encapsulation of adding/removing items
  *  */
 public class GameWorldStage extends AbstractStage {
 
     public static final String PROPERTY_INITIAL_SPAWN_COUNT = "initialSpawnCount";
     public static final String PROPERTY_SPAWN_CAPACITY = "maxSpawnCapacity";
     public static final String PROPERTY_SPAWNABLE_ITEMS = "spawnableItems";
     public static final String PROPERTY_VALID_SPAWN_AREA = "validSpawnArea";
 
     private Area validSpawn;
 
     private final ArrayList<Item> possibleItemSpawns = new ArrayList<Item>();
 
     Sound gatherSound = Tradesong.getGatherSound();
 
     Timer gatherTimer = new Timer();
 
 
     private Actor backgroundActor = new Actor();
 
     int initialItemCount;
     int itemCount;
     int maxSpawnedPerMap;
 
     public GameWorldStage(MapProperties properties) {
 
         //    // Actor for dragging map around. Covers all the ground but doesn't have an image
         backgroundActor.setTouchable(Touchable.enabled);
         backgroundActor.setVisible(true);
         this.addActor(backgroundActor);
 
         // Use the Map properties to get some good stuff
         initialItemCount = Integer.parseInt((String)properties.get(PROPERTY_INITIAL_SPAWN_COUNT));
         maxSpawnedPerMap = Integer.parseInt((String) properties.get(PROPERTY_SPAWN_CAPACITY));
         String[] itemsArray = ((String)properties.get(PROPERTY_SPAWNABLE_ITEMS)).split(",");
 
         // Figure out what spawns here and what the total rarity is
         for (String itemName : itemsArray) {
             possibleItemSpawns.add( Tradesong.gameState.getItemByName(itemName) );
         }
 
         // Set up knowledge of where things can spawn
         String validSpawnsBlob = (String)properties.get(PROPERTY_VALID_SPAWN_AREA);
         String[] VSA = validSpawnsBlob.split(",");
 
         Gdx.app.log("gameStage", validSpawnsBlob);
 
         validSpawn = new Area( Integer.parseInt(VSA[0]),Integer.parseInt(VSA[1]),Integer.parseInt(VSA[2]),Integer.parseInt(VSA[3]));
 
 
         for (int i = 0; i < initialItemCount; ++i) {
             spawnItem();
         }
     }
 
     @Override
     public void layout() {
         // TODO impl
     }
 
     public boolean spawnItem() {
         if (itemCount < maxSpawnedPerMap + 1) {
             finalizeItemForView(chooseItemByRarity());
             ++itemCount;
             return true;
         } else {
             return false;
         }
     }
 
     /***/
     private Item chooseItemByRarity() {
         int maxRarity = 0;
         int totalRarity = 0;
 
         for (Item item : possibleItemSpawns) {
             totalRarity += item.getRarity();
             if (item.getRarity() > maxRarity)
                 maxRarity = item.getRarity();
         }
 
         ArrayList<Integer> scaledRarities = new ArrayList<Integer>();
         for (Item item : possibleItemSpawns) {
             int n = maxRarity + 1 - item.getRarity();
 
 
             scaledRarities.add( n );
         }
 
         int n = new Random().nextInt( totalRarity );
 
         for (int i = 0; i < possibleItemSpawns.size(); ++i) {
             if (n < scaledRarities.get(i)) {
                 return new Item(possibleItemSpawns.get(i));
             }
             else {
                 n -= scaledRarities.get(i);
             }
 
         }
 
         return null;
     }
 
     /** Performs common steps for Items being added to the stage randomly */
     private void finalizeItemForView(Item item) {
 
         item.addListener(new ItemClickListener(item));
         int[] coords = validSpawn.getRandomCoordsInside();
         item.setBounds(coords[0], coords[1], 34, 34);   // TODO constants
         item.setTouchable(Touchable.enabled);
         item.setVisible(true);
         this.addActor(item);
     }
 
     public void removeItemCount() {
         removeItemCount(1);
     }
 
     public void removeItemCount(int i) {
         itemCount -= i;
     }
 
     public Actor getBackgroundActor() {
         return backgroundActor;
     }
 
 
     /** Simple class to represent a 2d space. Currently assumes map coordinate system (origin in top-left), may need to adjust */
     class Area {
        int originX;
        int originY;
         int right;
         int bottom;
 
        Area(int originX, int originY, int right, int bottom) {
            this.originX = originX;
            this.originY = originY;
             this.right = right;
            this.bottom = bottom;
         }
 
         int[] getRandomCoordsInside() {
             Random rand = new Random();
             int[] randCoords = new int[2];
 
             int n;
             do {
                 n = rand.nextInt(right);
            } while (n < originX);
             randCoords[0] = n * 32;
 
             do {
                n = rand.nextInt(bottom);
            } while (n < originY);
             randCoords[1] = n * 32;
 
             return randCoords;
         }
 
 
     }
 
 
     /** Class to handle touching/clicking of items on levels.  */
     class ItemClickListener extends ClickListener {
         Item owner;
 
         ItemClickListener(Item owner) {
             this.owner = owner;
         }
 
         @Override
         public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
 
             gatherSound.stop();
             gatherSound.play();
 
             gatherTimer.stop();
             gatherTimer.clear();
             gatherTimer.scheduleTask(new Timer.Task() {
 
                 @Override
                 public void run() {
                     if (Tradesong.gameState.getInventory().add(new Item(owner))) {
                         gatherSound.stop();
                         removeItemCount();
                         owner.remove();
                     }
                 }
             }, Tradesong.gameState.getParameterByName(Tradesong.getParamDelayGather()).getCurrentValue());
             gatherTimer.start();
 
 
             return super.touchDown(event, x, y, pointer, button);
         }
     }
 
 }
