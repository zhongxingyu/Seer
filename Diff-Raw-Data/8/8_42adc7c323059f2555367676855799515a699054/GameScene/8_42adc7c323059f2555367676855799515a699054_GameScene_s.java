 package com.example.ship.game;
 
 import android.util.Log;
 import com.example.ship.R;
 import com.example.ship.RootActivity;
 import com.example.ship.atlas.ResourceManager;
 import com.example.ship.bonus.Bonus;
 import com.example.ship.bonus.BonusActions;
 import com.example.ship.commons.CRandom;
 import org.andengine.engine.Engine;
 import org.andengine.entity.Entity;
 import org.andengine.entity.scene.Scene;
 import org.andengine.entity.scene.background.Background;
 import org.andengine.entity.sprite.Sprite;
 import org.andengine.opengl.texture.region.ITextureRegion;
 import org.andengine.util.color.Color;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 
 public class GameScene extends Scene {
     private static int layerCount = 0;
 
     public static final int LAYER_BACKGROUND  = layerCount++;
     public static final int LAYER_FIRST_WAVE  = layerCount++;
     public static final int LAYER_FIRST_SHIP_LINE  = layerCount++;
     public static final int LAYER_SECOND_WAVE = layerCount++;
     public static final int LAYER_SECOND_SHIP_LINE = layerCount++;
     public static final int LAYER_THIRD_WAVE  = layerCount++;
     public static final int LAYER_THIRD_SHIP_LINE  = layerCount++;
     public static final int LAYER_FOURTH_WAVE  = layerCount++;
     public static final int LAYER_TORPEDO = layerCount++;
     public static final int LAYER_GUN   = layerCount++;
 
     private static final float RELATIVE_SKY_HEIGHT = 0.3f;
     private static final float RELATIVE_WAVE_HEIGHT = 0.07f;
 
     private final RootActivity activity;
     private final Engine mEngine;
     private final ResourceManager resourceManager;
 
     private GameHUD gameHUD;
     private PauseHUD pauseHUD;
     private GameOverHUD gameOverHUD;
     private Sprite backgroundSprite;
     private ArrayList<Sprite> waveSprites;
     private Gun gun;
     private ArrayList<Ship> ships;
     private ArrayList<Bonus> bonuses;
     private HashMap<Integer, Float> shipLinesPosition;
     private Player player;
 
     public GameScene(final RootActivity activity) {
         super();
         this.activity = activity;
         this.mEngine = activity.getEngine();
         this.resourceManager = activity.getResourceManager();
 
         for (int i = LAYER_BACKGROUND; i < layerCount; i++) {
             this.attachChild(new Entity());
         }
 
         shipLinesPosition = new HashMap<Integer, Float>();
         ships = new ArrayList<Ship>();
         bonuses = new ArrayList<Bonus>();
         waveSprites = new ArrayList<Sprite>();
 
         createBackground();
         createWaves();
         createGun();
         createHuds();
 
         player = new Player(activity);
         player.setGameHUD(gameHUD);
     }
 
     public void resetGame() {
         ships.clear();
         bonuses.clear();
         clearLayers();
         createGun();
         createHuds();
 
         player = new Player(activity);
         player.setGameHUD(gameHUD);
     }
 
     public GameHUD getGameGUD() {
         return gameHUD;
     }
 
     public void switchToPauseHUD() {
         activity.getCamera().setHUD(pauseHUD);
     }
 
     public void switchToGameHUD() {
         activity.getCamera().setHUD(gameHUD);
     }
 
     public void switchToGameOverHUD() {
         gameOverHUD.setScoreToGameOverHUD(player.getStringScore());
         activity.getCamera().setHUD(gameOverHUD);
     }
 
     public void attachSpriteToLayer(Sprite sprite, int layerId){
         this.getChildByIndex(layerId).attachChild(sprite);
     }
 
     public GameHUD getGameHUD() {
         return gameHUD;
     }
 
     public GameOverHUD getGameOverHUD() {
         return gameOverHUD;
     }
 
     public Player getPlayer() {
         return player;
     }
 
     public Gun getGun() {
         return gun;
     }
 
     public float getShipLinePosition(int lineId) {
         return shipLinesPosition.get(lineId);
     }
 
     public ArrayList<Ship> getShips() {
         return ships;
     }
 
     @Override
     protected void onManagedUpdate(float pSecondsElapsed) {
         Ship deadShip = null;
         boolean stopCheck = false;
         boolean blockBonus = false;
 
         for (Ship ship: ships) {
             Sprite shipSprite = ship.getSprite();
             float maxX = activity.getCamera().getXMax();
             float minX = activity.getCamera().getXMin() - shipSprite.getWidth();
 
             if (   ship.getDirection() == Ship.TO_LEFT && (shipSprite.getX() < minX)
                 || ship.getDirection() == Ship.TO_RIGHT && (shipSprite.getX() > maxX)) {
 
                 ship.missionDone();
                 deadShip = ship;
                 player.reduceHealth();
                 activity.getSceneSwitcher().getGameScene().getPlayer().getLevel().incrementLevelProgress();
                 break;
             }
         }
 
         if (deadShip != null) {
             ships.remove(deadShip);
             Log.d("1log", "ship out of border");
             deadShip = null;
         }
 
         Entity layer = (Entity) getChildByIndex(LAYER_TORPEDO);
         for (int i = 0; i < layer.getChildCount(); i++) {
             Sprite sprite = (Sprite) layer.getChildByIndex(i);
             boolean seaEnd = true;
             for (Sprite wave: waveSprites) {
                 if (sprite.collidesWith(wave)) {
                     seaEnd = false;
                     break;
                 }
             }
             if (seaEnd) {
                 layer.getChildByIndex(i).detachSelf();
             }
         }
 
         for (int i = 0; i < layer.getChildCount(); i++) {
             Sprite torpedo = (Sprite) layer.getChildByIndex(i);
             stopCheck = false;
             blockBonus = false;
             for (Ship ship: ships) {
                 if (torpedo.collidesWith(ship.getHitAreaSprite())) {
                     torpedo.detachSelf();
 
                     // check if bonus stopped the ship
                     if (ship.getSprite().isIgnoreUpdate()) {
                         ship.getSprite().setIgnoreUpdate(false);
                         blockBonus = true;
                     }
 
                     if (ship.hit(getGun().getDamage())) {
                         player.addPoints((int) (ship.getScore() * player.getLevel().getScoreMultiplier()));
                         deadShip = ship;
                     }
 
                     stopCheck = true;
                 }
             }
 
             if (stopCheck) {
                 continue;
             }
 
             Bonus deadBonus = null;
             // check for torpedo collides with bonuses
             for (Bonus bonus: bonuses) {
                 if (torpedo.collidesWith(bonus.getSprite())) {
                     torpedo.detachSelf();
                     BonusActions.runGoodBonus();
                     bonus.runSink();
                     deadBonus = bonus;
                     break;
                 }
             }
 
             if (deadBonus != null) {
                 bonuses.remove(deadBonus);
             }
 
         }
 
         if (deadShip != null) {
             if (!blockBonus) {
                 createShipBonus(deadShip);
             }
            activity.getSceneSwitcher().getGameScene().getPlayer().getLevel().incrementLevelProgress();
             ships.remove(deadShip);
             Log.d("1log", "killed");
             deadShip = null;
         }
 
         super.onManagedUpdate(pSecondsElapsed);
     }
  
     private void createBackground() {
         ITextureRegion backgroundTexture = resourceManager.getLoadedTextureRegion(R.drawable.gamebackground);
         this.backgroundSprite = new Sprite( 0
                                           , 0
                                           , backgroundTexture
                                           , mEngine.getVertexBufferObjectManager());
 
         this.getChildByIndex(LAYER_BACKGROUND).attachChild(backgroundSprite);
        
         Color backgroundColor = new Color(0.09804f, 0.6274f, 0.8784f);
         this.setBackground(new Background(backgroundColor));
     }
 
     private void createGun() {
         gun = new Gun(activity);
         this.getChildByIndex(LAYER_GUN).attachChild(gun.getSprite());
     }
 
     private void createWaves() {
         ITextureRegion waveTexture = resourceManager.getLoadedTextureRegion(R.drawable.wave);
         float offset = activity.getCamera().getHeightRaw() * RELATIVE_SKY_HEIGHT;
 
         attachTextureToLayer(waveTexture, LAYER_FIRST_WAVE, offset);
 
         offset += activity.getCamera().getHeightRaw() * RELATIVE_WAVE_HEIGHT;
         shipLinesPosition.put(LAYER_FIRST_SHIP_LINE, offset);
         attachTextureToLayer(waveTexture, LAYER_SECOND_WAVE, offset);
 
         offset += activity.getCamera().getHeightRaw() * RELATIVE_WAVE_HEIGHT;
         shipLinesPosition.put(LAYER_SECOND_SHIP_LINE, offset);
         attachTextureToLayer(waveTexture, LAYER_THIRD_WAVE, offset);
 
         offset += activity.getCamera().getHeightRaw() * RELATIVE_WAVE_HEIGHT;
         shipLinesPosition.put(LAYER_THIRD_SHIP_LINE, offset);
         attachTextureToLayer(waveTexture, LAYER_FOURTH_WAVE, offset);
     }
 
     private void createHuds() {
         gameHUD = new GameHUD(activity);
         gameHUD.setEventsToChildren(activity.getEvents());
 
         pauseHUD = new PauseHUD(activity);
         pauseHUD.setEventsToChildren(activity.getEvents());
 
         gameOverHUD = new GameOverHUD(activity);
         gameOverHUD.setEventsToChildren(activity.getEvents());
     }
 
     private void attachTextureToLayer(ITextureRegion texture, int layerId, float yPosition) {
         Sprite waveSprite = new Sprite( 0
                                       , yPosition
                                       , texture
                                       , mEngine.getVertexBufferObjectManager());
 
         waveSprites.add(waveSprite);
         this.getChildByIndex(layerId).attachChild(waveSprite);
     }
 
     private void clearLayers() {
         this.getChildByIndex(LAYER_FIRST_SHIP_LINE).detachChildren();
         this.getChildByIndex(LAYER_SECOND_SHIP_LINE).detachChildren();
         this.getChildByIndex(LAYER_THIRD_SHIP_LINE).detachChildren();
         this.getChildByIndex(LAYER_TORPEDO).detachChildren();
         this.getChildByIndex(LAYER_GUN).detachChildren();
     }
 
     private void createShipBonus(Ship ship) {
         if (!CRandom.play(Bonus.bonusShipKillProbability)) {
             return;
         }
 
         Bonus bonus = new Bonus(ship);
         bonuses.add(bonus);
     }
 }
