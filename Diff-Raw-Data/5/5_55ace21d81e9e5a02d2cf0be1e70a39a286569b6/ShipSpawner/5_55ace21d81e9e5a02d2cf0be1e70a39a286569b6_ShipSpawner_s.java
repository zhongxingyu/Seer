 package com.example.ship.game;
 
 import com.example.ship.R;
 import com.example.ship.SceletonActivity;
 import org.andengine.engine.handler.timer.ITimerCallback;
 import org.andengine.engine.handler.timer.TimerHandler;
 
 import java.util.Random;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Vasya
  * Date: 07.05.13
  * Time: 21:59
  * To change this template use File | Settings | File Templates.
  */
 public class ShipSpawner {
     public static final float MIN_SPAWN_DELAY = 3.0f;
     public static final float MAX_SPAWN_DELAY = 10.0f;
 
     private final SceletonActivity activity;
     private TimerHandler timerHandler;
     private float delay;
     private boolean spawning;
     private Random rnd;
     private float spawnDelay;
 
     public ShipSpawner(SceletonActivity activity) {
         this.activity = activity;
         delay = MIN_SPAWN_DELAY;
         spawning = false;
         this.spawnDelay = MAX_SPAWN_DELAY;
     }
 
     public void startSpawn() {
         spawning = true;
         timerHandler = new TimerHandler(delay, new TimerTask());
         activity.getEngine().registerUpdateHandler(timerHandler);
     }
 
     public void stopSpawn() {
         spawning = false;
         timerHandler = null;
     }
 
     public void setSpawnDelay(float spawnDelay) {
         this.spawnDelay = spawnDelay;
     }
 
     private class TimerTask implements ITimerCallback {
         private TimerTask() {
             rnd = new Random();
         }
 
         @Override
         public void onTimePassed(final TimerHandler timerHandler) {
             activity.runOnUiThread(new Runnable() {
                 @Override
                 public void run() {
 
                     if (spawning) {
                         delay = MIN_SPAWN_DELAY + rnd.nextFloat() * spawnDelay;
                         timerHandler.setTimerSeconds(delay);
                         timerHandler.reset();
 
                         int layerId = selectLayer();
 
                         GameScene gameScene = activity.getSceneSwitcher().getGameScene();
                         Ship ship = new Ship( activity
                                             , gameScene.getShipLinePosition(layerId)
                                             , selectShipType()
                                             , rnd.nextBoolean());
                         gameScene.getShips().add(ship);
                         gameScene.getChildByIndex(layerId).attachChild(ship.getSprite());
                     }
                 }
             });
         }
 
         private int selectShipType() {
             int rndValue = rnd.nextInt(100) + 1;
 
             if (rndValue < activity.getIntResource(R.integer.SAILFISH_SPAWN_PROBABILITY)) {
                 return R.drawable.sailfish;
             } else {
                 rndValue -= activity.getIntResource(R.integer.SAILFISH_SPAWN_PROBABILITY);
             }
 
             if (rndValue < activity.getIntResource(R.integer.BATTLESHIP_SPAWN_PROBABILITY)) {
                 return R.drawable.battleship;
             } else {
                 rndValue -= activity.getIntResource(R.integer.BATTLESHIP_SPAWN_PROBABILITY);
             }
 
             return R.drawable.missileboat;
         }
 
         private int selectLayer() {
             int layerId = rnd.nextInt(3);
             switch (layerId) {
                 case 0:
                     layerId = GameScene.LAYER_FIRST_SHIP_LINE;
                     break;
                 case 1:
                     layerId = GameScene.LAYER_SECOND_SHIP_LINE;
                     break;
                 case 2:
                     layerId = GameScene.LAYER_THIRD_SHIP_LINE;
                     break;
                 default:
                     layerId = GameScene.LAYER_FIRST_SHIP_LINE;
                     break;
             }
             return layerId;
         }
     }
 }
