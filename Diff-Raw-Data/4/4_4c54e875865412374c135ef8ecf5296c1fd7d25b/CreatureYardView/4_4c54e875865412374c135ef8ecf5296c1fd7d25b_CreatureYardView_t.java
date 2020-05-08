 package com.kongentertainment.android.cardtactics.view;
 
 import android.graphics.Bitmap;
 import android.graphics.Canvas;
 
 import com.kongentertainment.android.cardtactics.model.entities.CreatureCard;
 import com.kongentertainment.android.cardtactics.model.entities.CreatureYard;
 import com.kongentertainment.android.cardtactics.model.exceptions.InvalidMoveException;
 
 public class CreatureYardView {
     //PERF: Chop these down to shorts/chars if need be
     private static int YARD_X_POS = 150;
     private static int YARD_Y_POS = 195;
     //private static int YARD_X_SIZE = 200;
     //private static int YARD_Y_SIZE = 160;
     private static int CELL_WIDTH  = 66;
     private static int CELL_HEIGHT = 53;
 
     /** The Creature Yard to pull data from */
     private CreatureYard mCreatureYard;
 	private CardViewManager mCardViewManager;
 
     /** DEBUG ONLY */
     public CreatureYardView(String debug) {
         int yard_x = 3;
         int yard_y = 2;
         mCreatureYard = new CreatureYard(yard_x, yard_y);
         CreatureCard creature = new CreatureCard("Debug");
         for (int x=0; x<yard_x; x++) {
             for (int y=0; y<yard_y; y++) {
                 try {
 					mCreatureYard.addCreature(creature, x, y);
 				} catch (InvalidMoveException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
             }
         }
     }
     public CreatureYardView(CreatureYard creatureYard) {
         mCreatureYard = creatureYard;
     }
 
     /**
      * The meat of the class. This fetches the relevant bitmaps and draws them 
      * in the proper positions.
      */
     public void draw(Canvas canvas) {
         int x = mCreatureYard.getWidth();
         int y = mCreatureYard.getHeight();
         //For each position
         for (int i=0; i < x; i++) {
             for (int j=0; j < y; j++) {
                 //if occupied
                if (!mCreatureYard.isEmpty(i, j)) {
                    int cardID = mCreatureYard.getCreature(i, j).getID();                    
                     Bitmap bitmap = mCardViewManager.getBigCard(cardID);
                     //draw a card there
                     int xCoord = YARD_X_POS + CELL_WIDTH  * x;
                     int yCoord = YARD_Y_POS + CELL_HEIGHT * y;
                     canvas.drawBitmap(bitmap, xCoord, yCoord, null);            
                 } //else keep going
             }
         }
     }
 }
