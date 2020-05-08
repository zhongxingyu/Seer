 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package jam.ld27.sprites;
 
 import jam.ld27.game.C;
 import java.util.Random;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.geom.Rectangle;
 import org.newdawn.slick.geom.Vector2f;
 
 /**
  *
  * @author Reik Val
  */
 public class Enemy extends Sprite {
     private int velX, velY;
     private int direction = 1;
     
     public Enemy(float x, float y, String name) {
        super(C.Textures.ENEMY.name, 72, 96, 256);
         this.name = name;
         group = C.Groups.ENEMIES.name;
         this.setPosition(new Vector2f(x, y));
        addAnimation("flying", new int[]{0,1});
        setAnimation("flying");
         addBB(new Rectangle(17, 55, 45, 20));
         
         velX = (int) (256 + 100 * new Random().nextFloat());
         velY = 0;
     } 
     @Override
     public void update(GameContainer gc, int delta) {
         super.update(gc, delta);
         float x = getNextStep(delta);
         float y = (float) Math.ceil(getY() + velY * ((float) delta / 1000));
         setPosition(new Vector2f(x, y));
     }
     
     public float getNextStep(int delta) {
         return (float) Math.ceil(getX() + direction * velX * ((float) delta / 1000));
     }
     
     public void changeDirection() {
         direction *= -1;
     }
 }
