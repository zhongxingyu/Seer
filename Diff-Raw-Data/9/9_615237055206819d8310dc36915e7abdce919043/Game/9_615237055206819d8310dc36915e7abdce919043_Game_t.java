 package com.dcdl.spear;
 
 import java.awt.Graphics;
 import java.awt.Rectangle;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 
 import com.dcdl.spear.collision.LinearArena;
 
 public class Game implements KeyListener {
   private final Player player;
   private final LinearArena floor;
   private final Stage stage;
 
   public Game() {
     player = new Player(0, 240 - 16, 16, 16);
     stage = new Stage();
     floor = new LinearArena();
    floor.addEntity(0, new Rectangle(0, 240 * Constants.SCALE,
        320 * Constants.SCALE, 10 * Constants.SCALE)); // The ground.
   }
 
   public void tick() {
     player.moveHorizontal();
     floor.collide(player.getRect(), player.getHorizontalDirection(), player);
     stage.collide(player.getRect(), player.getHorizontalDirection(), player);
     player.moveVertical();
     floor.collide(player.getRect(), player.getVerticalDirection(), player);
     stage.collide(player.getRect(), player.getVerticalDirection(), player);
   }
 
   public void render(Graphics g) {
     g.clearRect(0, 0, 320, 240);  // TODO Remove magic numbers.
     stage.render(g);
     player.render(g);
   }
 
   private void onKey(KeyEvent e, boolean pressed) {
     switch (e.getKeyCode()) {
     case KeyEvent.VK_X:
       player.jump(pressed);
       break;
     case KeyEvent.VK_LEFT:
       player.left(pressed);
       break;
     case KeyEvent.VK_RIGHT:
       player.right(pressed);
       break;
     }
   }
 
   @Override
   public void keyPressed(KeyEvent e) {
     onKey(e, true);
   }
 
   @Override
   public void keyReleased(KeyEvent e) {
     onKey(e, false);
   }
 
   @Override
   public void keyTyped(KeyEvent e) {
   }
 }
