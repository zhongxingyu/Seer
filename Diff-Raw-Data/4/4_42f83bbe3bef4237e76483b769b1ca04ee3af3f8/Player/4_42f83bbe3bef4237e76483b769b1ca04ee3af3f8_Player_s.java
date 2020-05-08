 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package jam.ld27.entities;
 
 import infinitedog.frisky.entities.Entity;
 import infinitedog.frisky.events.EventManager;
 import jam.ld27.game.C;
 import jam.ld27.tilemap.TileMap;
 import jam.ld27.tilemap.TileSet;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.geom.Vector2f;
 
 /**
  *
  * @author Reik Val
  */
 public class Player extends Entity {
 
     //Position
     private float posX;
     private float posY;
     private byte direccion = 0;
             
     //Movement
     private double velX = 4;
     private double velY = 0.098;
     
     //Gravity mode
     private boolean gravityActive = true;
     private double g = .01;
     private double maxVelY = 5;
     
     //Graphics
     private int frame = 30;
     private TileSet tileSet = new TileSet(C.Textures.TILE_SET.name, 
             (Integer) C.Logic.TILE_SIZE.data);
     
     //Managers
     private EventManager evm = EventManager.getInstance();
     
     private int score;
     
     public Player() {
         name = C.Entities.PLAYER.name;
         group = C.Groups.PLAYER.name;
         //TODO: Cuadrado de colisión: "menor al personaje, un 50% apróximadamente, interno a este"
         //this.posX = 400;
         //this.setPosition(new Vector2f(posX, 20));
         
          // TODO: harcoded dimensions
         setWidth(32);
         setHeight(32);
         
         respawn();
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
         movement(gc, delta);
         //Velocity of the character:
        y += velY;
        x += direccion*velX;
         //Setting the character:
         this.setPosition(new Vector2f(x,y));
     }
 
     @Override
     public void render(GameContainer gc, Graphics g) {
         super.render(gc, g);
         
         tileSet.render(frame, getX(), getY());
     }
     
     /**
      * Movement logic.
      * @param gc
      * @param delta 
      */
     private void movement(GameContainer gc, int delta) {
         //TODO: remove score based on movement. Hardcoded
         if(evm.isHappening(C.Events.MOVE_LEFT.name, gc)) {
             direccion = -1;
             score -= 10;
         } else if(evm.isHappening(C.Events.MOVE_RIGHT.name, gc)) {
             direccion = 1;
             score -= 10;
         } else {
             direccion = 0;
         }
     }
 
     public boolean collideWithFloor(TileMap tileMap) {
         return (this.getY() + this.getHeight()) > (tileMap.getY() + tileMap.getHeight());
     }
 
     public void respawn() {
         score = 99999;
         //TODO: Hardcode respawn position
         velX = 4;
         velY = 0.098;
         this.setPosition(new Vector2f(400, 20));    
     }
 
     public void setScore(int s) {
         score = s;        
     }
 
     public int getScore() {
         return score;
     }
 
     double getVelY() {
         return velY;
     }
 
     void setVelY(double vy) {
         velY = vy;
     }
     
 }
