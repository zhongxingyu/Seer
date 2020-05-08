 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.codefuss.factories;
 
 import com.codefuss.StateAnimation;
 import com.codefuss.entities.Entity;
 import com.codefuss.actions.MoveLeft;
 import com.codefuss.entities.Sprite;
 import com.codefuss.entities.Block;
 import com.codefuss.entities.Player;
 import com.codefuss.entities.ShotgunFire;
 import com.codefuss.entities.Zombie;
 import com.codefuss.physics.Body;
 import org.newdawn.slick.Animation;
 import org.newdawn.slick.geom.Vector2f;
 import org.newdawn.slick.util.Log;
 
 /**
  *
  * @author Martin Vium <martin.vium@gmail.com>
  */
 public class EntityFactory {
 
     final static float DEFAULT_JUMP_SPEED = 0.8f;
 
     AnimationFactory spriteFactory;
     PhysicsFactory physicsFactory;
 
     public EntityFactory(AnimationFactory spriteFactory, PhysicsFactory physicsFactory) {
         this.spriteFactory = spriteFactory;
         this.physicsFactory = physicsFactory;
     }
 
     public Block getBlocker(Vector2f position, int width, int height) {
         Body body = physicsFactory.getStaticBox(position.x, position.y, width, height);
         body.setDensity(Body.DENSITY_MASSIVE);
         return new Block(body);
     }
 
     public Player getPlayer(Vector2f position) {
         position.y = -20;
         Animation aniLeft = spriteFactory.getPlayerWalkAnimationLeft();
         Body body = physicsFactory.getDynamicBox(position.x, position.y, aniLeft.getWidth() / 2, aniLeft.getHeight());
         Log.debug("add player at: " + position.toString());
         Player player = new Player(this, position, body);
         player.setSpeedX(0.35f);
         player.setSpeedY(DEFAULT_JUMP_SPEED);
         player.addStateAnimation(new StateAnimation(spriteFactory.getPlayerIdleAnimationLeft(),
                 spriteFactory.getPlayerIdleAnimationRight(), Sprite.State.NORMAL, 0));
         player.addStateAnimation(new StateAnimation(aniLeft,
                 spriteFactory.getPlayerWalkAnimationRight(), Sprite.State.WALKING, 0));
         player.addStateAnimation(new StateAnimation(spriteFactory.getPlayerShootAnimationLeft(),
                 spriteFactory.getPlayerShootAnimationRight(), Sprite.State.ATTACKING, 600));
         return player;
     }
 
     public Entity getEntity(String type, String name, Vector2f position) {
         if (type.equals("zombie")) {
             return getZombie(position);
         }
 
         Log.debug("invalid entity type: " + type);
         return null;
     }
 
     public Entity getZombie(Vector2f position) {
         position.y = -20;
         Animation aniLeft = spriteFactory.getZombieWalkAnimationLeft();
         Body body = physicsFactory.getDynamicBox(position.x, position.y, aniLeft.getWidth() / 2, aniLeft.getHeight());
         Zombie zombie = new Zombie(this, position, body);
         zombie.setSpeedX(0.08f);
         zombie.setSpeedY(DEFAULT_JUMP_SPEED);
         zombie.addStateAnimation(new StateAnimation(aniLeft,
                 spriteFactory.getZombieWalkAnimationRight(), Sprite.State.NORMAL, 0));
         zombie.addStateAnimation(new StateAnimation(spriteFactory.getZombieWalkAnimationLeft(),
                 spriteFactory.getZombieWalkAnimationRight(), Sprite.State.WALKING, 0));
         zombie.addStateAnimation(new StateAnimation(spriteFactory.getZombieDeadAnimationLeft(),
                 spriteFactory.getZombieDeadAnimationRight(), Entity.State.DEAD, 0));
         new MoveLeft(zombie).invoke();
         return zombie;
     }
 
    public Entity getShotgunFire(float x, float y, Sprite.Direction dir) {
         Animation ani = spriteFactory.getShotgunFireAnimation();
        Body body = physicsFactory.getStaticBox(x, y, 10, 30);
        ShotgunFire fire = new ShotgunFire(new Vector2f(x, y), body);
         fire.addStateAnimation(new StateAnimation(ani, ani, Sprite.State.NORMAL, 250));

        if(dir == Sprite.Direction.LEFT) {
            fire.setVelocityX(-0.7f);
        } else {
            fire.setVelocityX(0.7f);
        }
         return fire;
     }
 }
