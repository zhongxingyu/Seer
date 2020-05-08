 package com.deeep.sod2.entities.enemyentities;
 
 import com.badlogic.gdx.graphics.Color;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.deeep.sod2.entities.Entity;
 import com.deeep.sod2.entities.EntityManager;
 import com.deeep.sod2.entities.Snake;
 import com.deeep.sod2.entities.projectiles.TurretBullet;
 import com.deeep.sod2.graphics.Assets;
 import com.deeep.sod2.graphics.ShapeRenderer;
 
 /*
  * Class : Turret
  * Package : com.deeep.sod2.entities
  * Author : Andreas
  * Date : 08-10-13
  */
 
 public class Turret extends AnimatedEnemy {
 
     protected int level;
     protected float health;
     protected  float maxHealth;
 
     public Turret() {
     }
 
     public Turret(int id, int x, int y, float health, int level) {
         super(id, -1, x, y,"animations/turret_strip14",5);
         this.health = health;
         this.maxHealth = health;
         this.level = level;
         setTextureRegion(Assets.getAssets().getRegion("Tiles/turret"));
     }
 
     @Override
     public void implementUpdate_2(float deltaT) {
         Entity target = null;
         for(int i=0; i<EntityManager.get().entities.size(); i++){
             if(EntityManager.get().entities.get(i) instanceof Snake) target = EntityManager.get().entities.get(i);
         }
         if(target != null){
             /**  ________0'
              *  |       /|
              *  |     /  |
              *  |   /    | y
              *  | /      |
              *  0________|
              *      x
              */
             float dx = target.getX()-this.x;
             float dy = target.getY()-this.y;
             float theta = (float) Math.atan2(dy, dx);
            if(animation.isAnimationFinished(animationTimer))
                EntityManager.get().addEntitySinglePlayer(
                        new TurretBullet(EntityManager.get().getNextSinglePlayerId(), x, y, 5f, Float.MAX_VALUE, theta));
         }
     }
 }
