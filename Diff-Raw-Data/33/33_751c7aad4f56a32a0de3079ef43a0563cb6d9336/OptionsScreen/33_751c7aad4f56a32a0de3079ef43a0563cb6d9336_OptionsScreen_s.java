 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package caregame.screen;
 
 import caregame.Game;
 import java.awt.Graphics;
 import java.awt.event.KeyEvent;
 
 /**
  *
  * @author Markus
  */
 public class OptionsScreen extends Screen {
 
     private int selection = 0;
 
     @Override
     public boolean isPausingGame() {
         return true;
     }
     
     
     
     @Override
     public void render(Graphics g) {
         renderFrameText(g, game.getWidth()/2 - 150, 0, 300, ALIGN_LEFT, "OPTIONS"); 
         renderFrameText(g, game.getWidth()/2 - 150, getFrameHeight(1), 300, ALIGN_LEFT, selection, 
                 "Tile Size: " + Game.TILE_SIZE);
     }
 
     @Override
     public void tick() {
         if (input.left.clicked) {
             if (selection == 0) {Game.TILE_SIZE /= 2;if (Game.TILE_SIZE < 8) Game.TILE_SIZE = 8;}
         }
         if (input.right.clicked) {
             if (selection == 0 && !game.canResume) Game.TILE_SIZE *= 2;
         }
        if (input.menu.clicked) game.setScreen(new TitleScreen());
         if (input.up.clicked) selection--;
         if (input.down.clicked) selection++;
         
         int len = 1;
         if (selection < 0) selection += len;
         if (selection >= len) selection -= len;
     }
     
     
     
 }
