 package game;
 
 import static game.Logic.LOG_LEVEL;
 import java.util.ArrayList;
 import java.util.Arrays;
 import org.apache.log4j.Logger;
 
 /**
  *
  * @author Nikolaos Bukas
  */
 public class Collision implements Runnable {
 
     //number of shield points used by colliding with various objects
     final public static int ALIEN_SHIELD_DAMAGE = 1;
     final public static int LARGE_ASTEROID_SHIELD_DAMAGE = 3;
     final public static int MEDIUM_ASTEROID_SHIELD_DAMAGE = 2;
     final public static int SMALL_ASTEROID_SHIELD_DAMAGE = 1;
     final public static int PROJECTILE_SHIELD_DAMAGE = 1;
     private Physics physicsEngine;
     private GameState gameState;
     private final static Logger log = Logger.getLogger(Collision.class.getName());
 
     public Collision(GameState gs, Physics phys) {
         log.setLevel(LOG_LEVEL);
         physicsEngine = phys;
         gameState = gs;
     }
 
     //actual collision check code
     @Override
     public void run() {
         log.debug("Collision start");
         
         //get all collisions
         ArrayList<MapObject> collisionList = physicsEngine.getCollisions();
         log.debug("SIZE OF THE COLLISION LIST = " + collisionList.size());
         log.debug("COLLISION LIST = " + Arrays.toString(collisionList.toArray()));
         
         //go through the returned colliding objects, and take action
         if (!collisionList.isEmpty()) {
             for (int i = 0; i < collisionList.size(); i = i + 2) {
                 MapObject collisionOne = collisionList.get(i);
                 MapObject collisionTwo = collisionList.get(i + 1);
 
                 if (collisionOne instanceof PlayerShip) {
                     if (collisionTwo instanceof AlienShip) {
                         // PlayerShip collided with AlienShip
                         if (collisionLogic((PlayerShip) collisionOne, (AlienShip) collisionTwo)) {
                             //break;
                         }
                     } else if (collisionTwo instanceof Asteroid) {
                         // PlayerShip collided with an Asteroid
                         if (collisionLogic((PlayerShip) collisionOne, (Asteroid) collisionTwo)) {
                             //break;
                         }
                     } else if (collisionTwo instanceof Projectile) {
                         // PlayerShip collided with a Projectile
                         if (collisionLogic((PlayerShip) collisionOne, (Projectile) collisionTwo)) {
                             //break;
                         }
                     } else if (collisionTwo instanceof BonusDrop) {
                         // PlayerShip collided with a BonusDrop
                         collisionLogic((PlayerShip) collisionOne, (BonusDrop) collisionTwo);
                     }
                 } else if (collisionOne instanceof AlienShip) {
                     if (collisionTwo instanceof Asteroid) {
                         // AlienShip collided with an Asteroid
 
                         collisionLogic((AlienShip) collisionOne, (Asteroid) collisionTwo);
                     } else if (collisionTwo instanceof Projectile) {
                         // AlienShip collided with a Projectile
                         collisionLogic((AlienShip) collisionOne, (Projectile) collisionTwo);
                     }
                 } else if (collisionOne instanceof Projectile) {
                     collisionLogic((Projectile) collisionOne, (Asteroid) collisionTwo);
                 }
             }
         }
     }
 
     private boolean collisionLogic(PlayerShip playerShip, Asteroid asteroid) {
         log.debug("Collision between Player and Asteroid");
 
         asteroid.destroy(false);
 
         //based on asteroid size, deal with player ship shield effects and destroy if necessary
         if (asteroid.getSize() == Asteroid.LARGE_ASTEROID_SIZE) {
             if (playerShip.getShieldPoints() >= LARGE_ASTEROID_SHIELD_DAMAGE) {
                 playerShip.setShieldPoints(playerShip.getShieldPoints() - LARGE_ASTEROID_SHIELD_DAMAGE);
             } else if (playerShip.getLives() == 1) {
                 playerShip.destroy();
             } else {
                 playerShip.destroy();
                 playerShip.setLives(playerShip.getLives() - 1);
             }
         } else if (asteroid.getSize() == Asteroid.MEDIUM_ASTEROID_SIZE) {
             if (playerShip.getShieldPoints() >= MEDIUM_ASTEROID_SHIELD_DAMAGE) {
                 playerShip.setShieldPoints(playerShip.getShieldPoints() - MEDIUM_ASTEROID_SHIELD_DAMAGE);
             } else if (playerShip.getLives() == 1) {
                 playerShip.destroy();
             } else {
                 playerShip.destroy();
                 playerShip.setLives(playerShip.getLives() - 1);
 
             }
         } else if (asteroid.getSize() == Asteroid.SMALL_ASTEROID_SIZE) {
             if (playerShip.getShieldPoints() >= SMALL_ASTEROID_SHIELD_DAMAGE) {
                 playerShip.setShieldPoints(playerShip.getShieldPoints() - SMALL_ASTEROID_SHIELD_DAMAGE);
             } else if (playerShip.getLives() == 1) {
                 playerShip.destroy();
             } else {
                 playerShip.destroy();
                 playerShip.setLives(playerShip.getLives() - 1);
             }
         }
 
         //return has the player been removed
         return gameState.getPlayerShip() == null;
     }
 
     private void collisionLogic(PlayerShip playerShip, BonusDrop bonusDrop) {
         log.debug("Collision between Player and Bonus");
         //give bonus to player, then remove
         if (bonusDrop.getType() == BonusDrop.BOMB_BONUS_DROP) {
             playerShip.addBomb();
         } else if (bonusDrop.getType() == BonusDrop.LIFE_BONUS_DROP) {
             playerShip.setLives(playerShip.getLives() + 1);
         } else if (bonusDrop.getType() == BonusDrop.SHIELD_ONE_POINT_DROP) {
             playerShip.setShieldPoints(playerShip.getShieldPoints() + 1);
         } else if (bonusDrop.getType() == BonusDrop.SHIELD_TWO_POINTS_DROP) {
             playerShip.setShieldPoints(playerShip.getShieldPoints() + 2);
         } else if (bonusDrop.getType() == BonusDrop.SHIELD_THREE_POINTS_DROP) {
             playerShip.setShieldPoints(playerShip.getShieldPoints() + 3);
         }
 
         bonusDrop.destroy();
     }
 
     private boolean collisionLogic(PlayerShip playerShip, AlienShip alienShip) {
         log.debug("Collision between Player and Alien");
         //take care of shield damage (and player destruction if necessary), and return true if the alien has been destroyed
         if (playerShip.getShieldPoints() >= ALIEN_SHIELD_DAMAGE) {
             playerShip.setShieldPoints(playerShip.getShieldPoints() - ALIEN_SHIELD_DAMAGE);
             playerShip.destroy();
         } else {
             playerShip.destroy();
         }
         alienShip.destroy(false);
 
         //return has the player been removed
         return gameState.getPlayerShip() == null;
     }
 
     private boolean collisionLogic(PlayerShip playerShip, Projectile projectile) {
         log.debug("Collision between Player and Projectile");
         //deal with shield effects, and destroy player if necessary.
         if (playerShip.getShieldPoints() >= PROJECTILE_SHIELD_DAMAGE) {
             playerShip.setShieldPoints(playerShip.getShieldPoints() - PROJECTILE_SHIELD_DAMAGE);
         } else if (playerShip.getLives() == 1) {
             playerShip.destroy();
         } else {
             playerShip.setLives(playerShip.getLives() - 1);
         }
 
         //always destroy projectile
         projectile.destroy();
 
         //return has the player been removed
         return gameState.getPlayerShip() == null;
     }
 
     private void collisionLogic(AlienShip alienShip, Asteroid asteroid) {
         //simply destroy both alien and asteroid
         log.debug("Collision between Alien and Asteroid");
         alienShip.destroy(false);
        asteroid.destroy(false);
     }
 
     private void collisionLogic(AlienShip alienShip, Projectile projectile) {
         log.debug("Collision between Alien and Projectile");
         //destroy alien and projectile
         alienShip.destroy(false);
         projectile.destroy();
     }
 
     private void collisionLogic(Projectile projectile, Asteroid asteroid) {
         log.debug("Collision between Projectile and Asteroid");
         //destroy asteroid and projectile
         projectile.destroy();
         asteroid.destroy(false);
     }
 }
