 package com.icbat.game.tradesong;
 
 import com.badlogic.gdx.Game;
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.assets.AssetManager;
 import com.badlogic.gdx.audio.Music;
 import com.badlogic.gdx.audio.Sound;
 import com.badlogic.gdx.graphics.Texture;
 import com.icbat.game.tradesong.screens.*;
 import com.icbat.game.tradesong.stages.HUDStage;
 import com.icbat.game.tradesong.stages.InventoryStage;
 import com.icbat.game.tradesong.stages.WorkshopStage;
 import com.icbat.game.tradesong.utils.*;
 
 /**
  * This class:
  *  - sets up the game initially
  *  - tracks/exposes game variables
  *  - loads common/global assets
  * */
 public class Tradesong extends Game {
 
     private static final String PARAM_DELAY_GATHER = "gatherDelay";
     private static final String PARAM_DELAY_CRAFT = "craftDelay";
 
     public static GameStateManager gameState;
     public static AssetManager assetManager = new AssetManager();
 
     private static LevelScreen lastMapScreen;
     private static ScreenTypes currentOverlay;
 
     private HUDStage hud;
     private InventoryStage inventoryStage;
     private WorkshopStage workshopStage;
     private static KeyboardHandler keyHandler;
 
 
 
     @Override
 	public void create() {
         initializeAssets();
         GameStateManager.updateMusic(getMusic(MusicAsset.TITLE_THEME));
         gameState = new GameStateManager();
 
         keyHandler = new KeyboardHandler(this);
 
         hud = new HUDStage(this);
         inventoryStage = new InventoryStage();
         workshopStage = new WorkshopStage();
 
 		goToOverlay(ScreenTypes.MAIN_MENU);
 
 
 
 
 	}
 
     private void initializeAssets() {
 
         for (TextureAssets texture : TextureAssets.values()) {
             assetManager.load(texture.getPath(), Texture.class);
         }
 
         for (SoundAssets sound : SoundAssets.values()) {
             assetManager.load(sound.getPath(), Sound.class);
         }
 
         for (MusicAsset music : MusicAsset.values()) {
             assetManager.load(music.getPath(), Music.class);
         }
         assetManager.finishLoading(); // Blocks until finished
     }
 
     public static KeyboardHandler getKeyHandler() {
         return keyHandler;
     }
 
     public static ScreenTypes getCurrentOverlay() {
         return currentOverlay;
     }
 
     public static String getParamDelayGather() {
         return PARAM_DELAY_GATHER;
     }
 
     public static String getParamDelayCraft() {
         return PARAM_DELAY_CRAFT;
     }
 
     /* Screen management methods */
 
     /**
      * Generic screen switching based off of what screen is given. Does not handle maps!
      *
      * Will goBack if passed the same screen!
      * */
     public void goToOverlay(ScreenTypes screen) {
         GameStateManager.update();
         if (!screen.equals(ScreenTypes.MAIN_MENU) && !screen.equals(ScreenTypes.SETTINGS)) {
             Gdx.app.log("setting to", screen.name());
 
             currentOverlay = screen;
         }
 
         switch (screen) {
 
             case MAIN_MENU:
                 setScreen(new MainMenuScreen(this));
                 break;
 
             case SETTINGS:
                 setScreen(new SettingsScreen(this));
                 break;
 
             case INVENTORY:
                 setScreen(new InventoryScreen(hud, inventoryStage));
                 break;
 
             case WORKSHOP:
                 setScreen(new WorkshopScreen(hud, inventoryStage, workshopStage));
                 break;
 
             case STORE:
                 setScreen(new StoreScreen(hud, inventoryStage));
                 break;
         }
     }
 
     /**
      * Goes to the last screen, or exits if this was main menu.
      *
      * Doesn't work going back maps.
      * */
     public void goBack() {
 
         if (currentOverlay == null) {
             if (getScreen() instanceof MainMenuScreen) {
                 Gdx.app.exit();
             } else {
                 goToOverlay(ScreenTypes.MAIN_MENU);
             }
         }
         else {
 
             Gdx.app.log("trying to back from", currentOverlay.name());
 
             setScreen(lastMapScreen);
             currentOverlay = null;
         }
     }
 
     /**
      * Goes to the specified map and updates references.
      * */
     public void changeMap(String mapName) {
         lastMapScreen = new LevelScreen(mapName, hud, this);
         setScreen(lastMapScreen);
     }
 
 
 
     /* Asset methods */
 
     /** Convenience method to prevent having to call assetManager.get(longConstantName)
      *
      * @throws Error if the file could not be found
      * @return the object in assetManager by that name
      * */
     public static Texture getTexture(TextureAssets toFind) {
         return assetManager.get(toFind.getPath());
     }
     /** Convenience method to prevent having to call assetManager.get(longConstantName)
      *
      * @throws Error if the file could not be found
      * @return the object in assetManager by that name
      * */
     public static Sound getSound(SoundAssets toFind) {
         return assetManager.get(toFind.getPath());
     }
     /** Convenience method to prevent having to call assetManager.get(longConstantName)
      *
      * @throws Error if the file could not be found
      * @return the object in assetManager by that name
      * */
     public static Music getMusic(MusicAsset toFind) {
         return assetManager.get(toFind.getPath());
     }
 }
