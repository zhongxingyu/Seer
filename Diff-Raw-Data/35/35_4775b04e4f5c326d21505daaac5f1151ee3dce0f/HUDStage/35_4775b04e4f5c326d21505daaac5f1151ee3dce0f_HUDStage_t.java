 package com.icbat.game.tradesong.stages;
 
 import com.badlogic.gdx.graphics.Color;
 import com.badlogic.gdx.graphics.Texture;
 import com.badlogic.gdx.graphics.g2d.BitmapFont;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.graphics.g2d.TextureRegion;
 import com.badlogic.gdx.scenes.scene2d.InputEvent;
 import com.badlogic.gdx.scenes.scene2d.Stage;
 import com.badlogic.gdx.scenes.scene2d.Touchable;
 import com.badlogic.gdx.scenes.scene2d.ui.Image;
 import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
 import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
 import com.icbat.game.tradesong.Tradesong;
 import com.icbat.game.tradesong.screens.InventoryScreen;
 import com.icbat.game.tradesong.screens.WorkshopScreen;
 
 public class HUDStage extends Stage {
     public static final int SPACER = 6;
     public static final int ICON_SIZE = 34;
 
     /** As of now, this is pulled from items.png */
     Texture itemsTexture;
     private TextButton capacityCounter;
     private Tradesong gameInstance;
 
     public HUDStage(Tradesong gameInstance) {
         this.itemsTexture = Tradesong.assets.get(Tradesong.getItemsPath());
         this.gameInstance = gameInstance;
         addInventoryButton();
         addInventoryStats();
         addWorkshopsButton();
     }
 
     private void addInventoryButton() {
         int x = 7;
         int y = 29;
         Image inventoryButton = new Image(new TextureRegion(itemsTexture, x * ICON_SIZE, y * ICON_SIZE, ICON_SIZE, ICON_SIZE));
 
         inventoryButton.setTouchable(Touchable.enabled);
         inventoryButton.addListener(new InterfaceButtonListener(InventoryScreen.class, gameInstance));
         // No need to set bounds; this is the bottom-left corner
 
         inventoryButton.setVisible(true);
         this.addActor(inventoryButton);
     }
 
     private void addWorkshopsButton() {
         int x = 3;
         int y = 9;
 
 
         Image workshopButton = new Image(new TextureRegion(itemsTexture, x * ICON_SIZE, y * ICON_SIZE, ICON_SIZE, ICON_SIZE));
 
         workshopButton.setTouchable(Touchable.enabled);
         workshopButton.addListener(new InterfaceButtonListener(WorkshopScreen.class, gameInstance));
 
         int maxX = (int)this.getWidth();
         workshopButton.setBounds(maxX - ICON_SIZE, 0, ICON_SIZE, ICON_SIZE);
 
         workshopButton.setVisible(true);
         this.addActor(workshopButton);
     }
 
     private void addInventoryStats() {
 
         Integer capacity = Tradesong.gameState.getInventory().capacity();
         Integer size = Tradesong.gameState.getInventory().size();
 
         TextButton.TextButtonStyle textStyle = new TextButton.TextButtonStyle();
         BitmapFont font = new BitmapFont();
         if (size.equals(capacity)) {
             font.setColor(Color.RED);
         }
         else if (size.equals(capacity - 3)) {
             font.setColor(Color.ORANGE);
         }
         else {
             font.setColor(Color.WHITE);
         }
 
         textStyle.font = font;
 
         capacityCounter = new TextButton(size.toString() + " / " + capacity.toString(), textStyle) {
             @Override
             public void draw(SpriteBatch batch, float parentAlpha) {
                 Integer size = Tradesong.gameState.getInventory().size();
                 Integer capacity = Tradesong.gameState.getInventory().capacity();
                 capacityCounter.setText(size.toString() + " / " + capacity.toString());
 
                 super.draw(batch, parentAlpha);    //To change body of overridden methods use File | Settings | File Templates.
             }
         };
 
 
         capacityCounter.setVisible(true);
         capacityCounter.setTouchable(Touchable.disabled);
         capacityCounter.setBounds(ICON_SIZE + SPACER, 0, ICON_SIZE, ICON_SIZE);
 
         this.addActor(capacityCounter);
     }
 
     class InterfaceButtonListener extends ClickListener {
         private Class landingPage;
        private Tradesong gameInstance;
 
        InterfaceButtonListener(Class landingPage, Tradesong gameInstance) {
             super();
             this.landingPage = landingPage;
            this.gameInstance = gameInstance;
         }
 
         @Override
         public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
             super.touchDown(event, x, y, pointer, button);
             // Following error is a known bug in IDEA, not an actual problem
            if (gameInstance.getCurrentScreen().getClass().equals(landingPage)) {
                 // If we're already on that screen
                gameInstance.goBack();
             }
             else {
                 // Find the screen to go to based on landing
                 if (landingPage.equals(InventoryScreen.class)) {

                    gameInstance.goToInventory();
                 }
                 else if (landingPage.equals(WorkshopScreen.class)) {
                    gameInstance.goToWorkshop();
 
                 }
             }
             return true;
         }
     }
 }
