 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package jam.ld27.sprites;
 
 import jam.ld27.sprites.Sprite;
 import infinitedog.frisky.entities.Entity;
 import infinitedog.frisky.events.EventManager;
 import infinitedog.frisky.sounds.SoundManager;
 import jam.ld27.game.C;
 import jam.ld27.tilemap.TileMap;
 import jam.ld27.tilemap.TileSet;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.geom.Rectangle;
 import org.newdawn.slick.geom.Vector2f;
 
 /**
  *
  * @author Reik Val
  */
 public class Player extends Sprite {
     //Position
     private byte direccion = 0;
     private byte hp = 2;
     //Movement
     private double velX = 4;
     private double velY = 2;
     private double modX = 0;
     
     //Gravity mode
     private boolean gravityActive = true;
     private double g = .01;
     private double maxVelY = 5;
     
     //Graphics
    private int crying = 180;
 
     private int score;
     
     private boolean dead;
     private boolean saved;
     
     public Player() {
         super(C.Textures.PRINCESS_SET.name, 160, 160, 500);
         name = C.Entities.PLAYER.name;
         group = C.Groups.PLAYER.name;
         
         addAnimation("falling", new int[]{0,1});
         addAnimation("crying", new int[]{1,2});
         
         addBB(new Rectangle(44, 15, 69, 137));
         addBB(new Rectangle(15, 87, 131, 33));
         
         respawn();
     }
     
     @Override
     public void render(GameContainer gc, Graphics g) {
         if ((crying > 180 || crying % 2 == 0) && !saved) {
             super.render(gc, g);
         }
     }
     
     @Override
     public void update(GameContainer gc, int delta) {
         super.update(gc, delta);
         
         float x = getX();
         float y = getY();
         //Applying gravity:
         if(gravityActive) {
             if(velY >= maxVelY) {
                 velY = maxVelY;
             } else {
                 velY += velY*g;
             }
         }
         //Applying movement to character:
         movement(gc);
         //Velocity of the character:
         y += Math.ceil(velY);
         x += Math.ceil(direccion*velX) + modX;
         modX = 0;
         //Setting the character:
         this.setPosition(new Vector2f(x,y));
         if(crying == 0) {
             setAnimation("crying");
             crying++;
         } else if(crying == 180) {
             setAnimation("falling");
        } else {
             crying++;
         }
     }
     
     /**
      * Movement logic.
      * @param gc
      * @param delta 
      */
     private void movement(GameContainer gc) {
         if(evm.isHappening(C.Events.MOVE_LEFT.name, gc)) {
             direccion = -1;
         } else if(evm.isHappening(C.Events.MOVE_RIGHT.name, gc)) {
             direccion = 1;
         } else {
             direccion = 0;
         }
     }
 
     public boolean collideWithFloor(TileMap tileMap) {
         return (this.getY() + this.getHeight()) > (tileMap.getY() + tileMap.getHeight());
     }
 
     public void respawn() {
         dead = false;
         score = 0;
         //TODO: Hardcode respawn position
         velX = 4;
         velY = 0.098;
         hp = 2;
         crying = 180;
         setAnimation("falling");
         this.setPosition(new Vector2f(400, 0));    
     }
 
     public void setScore(int s) {
         score = s;        
     }
 
     public int getScore() {
         return score;
     }
 
     public double getVelY() {
         return velY;
     }
 
     public void setVelY(double vy) {
         velY = vy;
     }
 
     public void die() {
         score = 0;
         dead = true;
         sm.playSound(C.Sounds.MUERTE.name);
     }
     
     public boolean isDead() {
         return dead;
     }
 
     public void saved() {
         saved = true;
     }
     
     public boolean isSaved() {
         return saved;
     }
 
     public double getModX() {
         return modX;
     }
 
     public void setModX(double modX) {
         this.modX = modX;
     }
     
     public void lossHp() {
         hp --;
         if(hp <= 0) {
             die();
         }
     }
 
     public int getCrying() {
         return crying;
     }
 
     public void setCrying(int crying) {
         this.crying = crying;
     }
     
 }
