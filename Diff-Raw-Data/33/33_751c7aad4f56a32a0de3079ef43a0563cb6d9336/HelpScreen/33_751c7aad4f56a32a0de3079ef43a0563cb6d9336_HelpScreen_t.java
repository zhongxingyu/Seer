 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package caregame.screen;
 
 import java.awt.Graphics;
 import java.awt.event.KeyEvent;
 
 /**
  *
  * @author Markus
  */
 public class HelpScreen extends Screen {
 
     @Override
     public boolean isPausingGame() {
         return true;
     }
 
     @Override
     public void render(Graphics g) {
         renderFrameText(g, game.getWidth() / 2 - 150, 0, 300, ALIGN_CENTER, "HELP");
         renderFrameText(g, game.getWidth() / 2 - 150, getFrameHeight(1), 300, ALIGN_LEFT, 
                "ARROW/WASD KEYS: move around",
                 "E: inventory",
                "Q: Drop item",
                 "ESC: to title", 
                "SPACE/ENTER: use / select");
         renderFrameText(g, game.getWidth() / 2 - 150, game.getHeight() - getFrameHeight(1)*2, 300, ALIGN_CENTER, "Press any key");
     }
 
     @Override
     public void tick() {
         if (input.attack.clicked || input.menu.clicked)
             game.setScreen(new TitleScreen());
     }
     
     
     
     
 }
