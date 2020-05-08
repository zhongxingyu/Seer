 package com.deeep.sod2.entities.enemyentities;
 
 import com.badlogic.gdx.graphics.g2d.Animation;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.graphics.g2d.TextureRegion;
 import com.deeep.sod2.entities.Entity;
 import com.deeep.sod2.entities.EntityManager;
 import com.deeep.sod2.entities.projectiles.TurretBullet;
 import com.deeep.sod2.graphics.Assets;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Elmar
  * Date: 10/11/13
  * Time: 11:20 AM
  * To change this template use File | Settings | File Templates.
  */
 public abstract class AnimatedEnemy extends Entity {
     protected Animation animation;
     protected float animationTimer = 0f;
     protected TextureRegion[] frames;
     protected String name;
     protected float range;
 
     /** USE THIS ONLY FOR REGISTERING THE ENTITY! SHOULD NOT BE USED OTHERWISE! */
     protected AnimatedEnemy() {
     }
 
     protected AnimatedEnemy(int id, int owner, int x, int y, String name, float range) {
         super(id, owner, x, y);
         this.name = name;
         this.range = range;
     }
 
     /** Use this function instead of the constructor */
     @Override
     public void onCreate() {
         TextureRegion[][] temp;
         temp = Assets.getAssets().getRegion(name).split(40, 40);
         frames = new TextureRegion[temp[0].length];
         for (int i = 0; i < temp[0].length; i++) {
             frames[i] = temp[0][i];
         }
 
        animation = new Animation(0.5f, frames);
         animation.setPlayMode(Animation.NORMAL);
     }
 
     @Override
     public void implementUpdate_1(float deltaT) {
        if (animation.isAnimationFinished(animationTimer))
            animationTimer = 0;
         animationTimer += deltaT;
        setTextureRegion(animation.getKeyFrame(animationTimer, false));
         implementUpdate_2(deltaT);
     }
 
     @Override
     public void implementDraw_1(SpriteBatch spriteBatch) {
 
     }
 
     public abstract void implementUpdate_2(float deltaT);
 }
