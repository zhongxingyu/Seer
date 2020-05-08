 package edu.mines.csci598B.entejagd;
 
 import java.awt.Color;
 import java.awt.Graphics2D;
 
 import edu.mines.csci598B.backend.*;
 
 public class Entejagd extends GameState {
   private final GameManager game;
   private final GameField field = new GameField();
   private float timeUntilDuck = 0;
 
   public Entejagd(GameManager game) {
     this.game = game;
   }
 
   public GameState updateThis(float et) {
     field.update(et);
     timeUntilDuck -= et;
     if (timeUntilDuck < 0) {
       field.add(new Duck(game, field));
       timeUntilDuck = 1;
     }
     return this;
   }
 
   public void drawThis(Graphics2D g) {
     g.setColor(Color.green.darker());
     g.fillRect(game.vcxtopx(0),
                game.vcytopx(0)+game.vdytopx(0.3f*game.vheight()),
                game.vdxtopx(1), -game.vdytopx(0.3f*game.vheight()));
     g.setColor(Color.blue.brighter());
     g.fillRect(game.vcxtopx(0),
               game.vcytopx(0.3f*game.vheight()) +
                 game.vdytopx(0.7f*game.vheight()),
               game.vdxtopx(1), -game.vdytopx(0.7f*game.vheight())+1);
     field.draw(g);
 
     g.setColor(Color.white);
     InputStatus is = game.getSharedInputStatus();
     for (int i = 0; i < is.pointers.length; ++i) {
       float retx = is.pointers[i][0], rety = is.pointers[i][1];
       g.drawLine(game.vcxtopx(retx-0.025f), game.vcytopx(rety),
                  game.vcxtopx(retx+0.025f), game.vcytopx(rety));
       g.drawLine(game.vcxtopx(retx), game.vcytopx(rety-0.025f),
                  game.vcxtopx(retx), game.vcytopx(rety+0.025f));
     }
   }
 
   public void receiveInputThis(InputEvent evt) {
     if (evt.type == InputEvent.TYPE_GESTURE &&
         evt.index == InputEvent.GESTURE_JUMP)
       field.shoot(game.getSharedInputStatus().pointers[0][0],
                   game.getSharedInputStatus().pointers[0][1]);
   }
 
   public static void main(String[] args) {
     GameManager man = new GameManager("Entejagd");
     KeyboardGestureInputDriver kgid = new KeyboardGestureInputDriver();
     kgid.bind(java.awt.event.KeyEvent.VK_SPACE, InputEvent.GESTURE_JUMP);
     man.installInputDriver(kgid);
     MouseButtonGestureInputDriver mbgid = new MouseButtonGestureInputDriver();
     mbgid.bind(java.awt.event.MouseEvent.BUTTON1, InputEvent.GESTURE_JUMP);
     man.installInputDriver(mbgid);
     ModalMouseMotionInputDriver mmmid = new ModalMouseMotionInputDriver();
     mmmid.setPointerMode(true);
     man.installInputDriver(mmmid);
 
     Entejagd entejagd = new Entejagd(man);
     man.setState(entejagd);
     man.run();
     man.destroy();
   }
 }
