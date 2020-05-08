 package com.deeep.sod2.entities.projectiles;
 
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.deeep.sod2.entities.Entity;
 import com.deeep.sod2.entities.EntityManager;
 import com.deeep.sod2.graphics.Assets;
 import com.deeep.sod2.graphics.PVector;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Elmar
  * Date: 10/10/13
  * Time: 8:24 PM
  * To change this template use File | Settings | File Templates.
  */
 public class TurretBullet extends Projectile {
 
     public TurretBullet(int id, float x, float y, float speed, float fuel, float angle) {
         super(id, -1, x, y, speed, fuel, angle);
     }
 
     public TurretBullet() {
     }
 
     @Override
     public void implementUpdate_2(float deltaT) {
 
     }
 
     @Override
     public void Collide(Entity entity) {
         //To change body of implemented methods use File | Settings | File Templates.
     }
 
     /** Use this function instead of the constructor */
     @Override
     public void onCreate() {
        setTextureRegion(Assets.getAssets().getRegion("upgrades/bullet"));
        //setTextureRegion(Assets.getAssets().getRegion("projectiles/bullet1"));
         setWidth(0.5f);
         setHeight(0.5f);
     }
 
     @Override
     public void implementDraw_1(SpriteBatch spriteBatch) {
     }
 }
