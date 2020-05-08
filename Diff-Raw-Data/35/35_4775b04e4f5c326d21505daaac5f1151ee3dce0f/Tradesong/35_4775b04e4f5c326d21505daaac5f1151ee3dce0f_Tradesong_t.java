 package com.icbat.game.tradesong;
 
 import com.badlogic.gdx.Game;
 import com.badlogic.gdx.assets.AssetManager;
 import com.badlogic.gdx.graphics.Texture;
 import com.icbat.game.tradesong.screens.*;
 import com.icbat.game.tradesong.stages.HUDStage;
 import com.icbat.game.tradesong.stages.InventoryStage;
 import com.icbat.game.tradesong.stages.WorkshopStage;
 
 import java.util.Stack;
 
 
 /**
  * This class:
  *  - sets up the game initially
  *  - tracks/exposes game variables
  *  - loads common/global assets
  * */
 public class Tradesong extends Game {
 
     private static final String PATH_SPRITE_ITEMS = "sprites/items.png";
     private static final String PATH_SPRITE_FRAME = "sprites/frame.png";
     private static final String PATH_SPRITE_ARROW = "sprites/arrow.png";
     public static GameStateManager gameState;
     public static AssetManager assets = new AssetManager();
     private static final Stack<AbstractScreen> screenStack = new Stack<AbstractScreen>();
 
     private HUDStage hud;
     private InventoryStage inventoryStage;
     private WorkshopStage workshopStage;
 
     @Override
 	public void create() {
         initializeAssets();
 
         gameState = new GameStateManager();
 		goToMainMenu();
 
         hud = new HUDStage(this);
         inventoryStage = new InventoryStage();
         workshopStage = new WorkshopStage();
 
 
 	}
 
     private void initializeAssets() {
         // Item sheet used by all/most icons, items, buttons, etc.
         assets.load(PATH_SPRITE_ITEMS, Texture.class);
 
         // Frame PNG used in inventory/workshops
         assets.load(PATH_SPRITE_FRAME, Texture.class);
 
         assets.load(PATH_SPRITE_ARROW, Texture.class);
 
         assets.finishLoading(); // Blocks until finished
     }
 
     public void goBack() {
         if (screenStack.size() > 0) {
             screenStack.pop();
             setScreen(screenStack.peek());
         }
     }
 
     public void goToScreen(AbstractScreen newScreen) {
        AbstractScreen top = screenStack.peek();

        if (top.getClass().equals(InventoryScreen.class) || top.getClass().equals(WorkshopScreen.class))
            screenStack.pop();
         screenStack.push(newScreen);
         setScreen(screenStack.peek());
     }
 
     public void goToMainMenu() {
        screenStack.clear();
        screenStack.push(new MainMenuScreen(this));
        setScreen(screenStack.peek());
     }
 
     public void goToInventory() {
         goToScreen(new InventoryScreen(hud, inventoryStage));
     }
     public void goToLevel(String levelName) {
         goToScreen(new LevelScreen(levelName, hud));
     }
     public AbstractScreen getCurrentScreen() {
         return screenStack.peek();
     }
 
     public void goToWorkshop() {
         goToScreen(new WorkshopScreen(hud, inventoryStage, workshopStage));
     }
 
     public static String getItemsPath() {
         return PATH_SPRITE_ITEMS;
     }
 
     public static String getFramePath() {
         return PATH_SPRITE_FRAME;
     }
 
     public static String getPathSpriteArrow() {
         return PATH_SPRITE_ARROW;
     }
 }
