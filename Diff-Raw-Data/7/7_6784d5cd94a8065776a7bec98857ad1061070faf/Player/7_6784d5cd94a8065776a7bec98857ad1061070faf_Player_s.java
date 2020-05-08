 package aritzh.ld27.entity;
 
 import aritzh.ld27.level.Level;
 import aritzh.ld27.render.Sprite;
 import aritzh.ld27.util.Keyboard;
 
 import java.awt.Rectangle;
 
 /**
  * A player. A Mob controllable by the person playing
  *
  * @author Aritz Lopez
  * @license Lesser GNU Public License v3 (http://www.gnu.org/licenses/lgpl.html)
  */
 public class Player extends Mob {
 
     private Keyboard input;
    private static final int WIDTH = 17, HEIGHT = 31;
 
     @Override
    protected Rectangle getBoundingBox() {
        return new Rectangle(this.posX + 4, this.posY + 29, this.width - 8, 1);
     }
 
     /**
      * Constructs an Player at (0,0) with the specified Sprite
      *
      * @param level The level to which the player will be added
      * @param input The keyboard class that will be used to move the player
      */
     public Player(Level level, Keyboard input) {
         this(level, input, 0, 0);
     }
 
     /**
      * Constructs an Player at (posX, posY) with the specified Sprite
      *
      * @param level The level to which the player will be added
      * @param input The keyboard class that will be used to move the player
      * @param posX  The X coordinate at which the sprite will be drawn
      * @param posY  The Y coordinate at which the sprite will be drawn
      */
     public Player(Level level, Keyboard input, int posX, int posY) {
         super(new Sprite(2, 1, WIDTH, HEIGHT), level, posX, posY);
         level.setPlayer(this);
         this.input = input;
     }
 
     @Override
     public void update() {
 
         if (input.isDown() && !input.isUp()) this.velY = 1;
         else if (input.isUp() && !input.isDown()) this.velY = -1;
         else this.velY = 0;
 
         if (input.isLeft() && !input.isRight()) this.velX = -1;
         else if (input.isRight() && !input.isLeft()) this.velX = 1;
         else this.velX = 0;
 
         super.update();
     }
 }
