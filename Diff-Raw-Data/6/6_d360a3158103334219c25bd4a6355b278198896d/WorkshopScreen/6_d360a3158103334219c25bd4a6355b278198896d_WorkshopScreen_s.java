 package com.icbat.game.tradesong.screens;
 
 import com.icbat.game.tradesong.Tradesong;
 import com.icbat.game.tradesong.stages.WorkshopStage;
 
 public class WorkshopScreen extends InventoryScreen{
 
     private WorkshopStage workshopStage;
 
     public WorkshopScreen(Tradesong gameInstance) {
         super(gameInstance);
 
         workshopStage = new WorkshopStage(gameInstance);
         inventoryStage.connectToWorkshop(workshopStage);
         inputMultiplexer.addProcessor(workshopStage);
 
     }
 
     @Override
     public void show() {
         super.show();
         inputMultiplexer.addProcessor(workshopStage);
     }
 
     @Override
     public void render(float delta) {
         super.render(delta, 0.98f, 0.639f, 0.008f, 1);
         workshopStage.act(delta);
         workshopStage.draw();
     }
 }
