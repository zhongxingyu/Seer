 /*
  * Copyright (c) 2009-2011 Daniel Oom, see licence.txt for more info.
  */
 
 package entities.projectiles;
 
 import java.util.Collection;
 import java.util.Collections;
 
 import loader.data.json.ProjectilesData.ProjectileData;
 import other.GameTime;
 import basics.Vector2;
 
import components.graphics.animations.Continuous;
 import components.interfaces.IActions;
 import components.interfaces.ICompAnim;
 import components.interfaces.IDamagable;
 import components.interfaces.IEntity;
 import components.triggers.actions.IAction;
 
 import entities.Entity;
 
 public class Projectile extends Entity implements IEntity, IDamagable, IActions {
   private final float duration, range, damage, maxHP;
   private final boolean gravity, collides;
 
   private boolean       alive;
   private float         hp;
 
   private final Vector2 startPos;
   private final float   startTime;
 
   public Projectile(final float x, final float y, final float rot,
                     final ProjectileData data,
                     final ICompAnim renderer, final GameTime time) {
     super(x, y, data.hitbox.width, data.hitbox.height,
           (float) Math.cos(rot) * data.speed,
           (float) Math.sin(rot) * data.speed);
 
     gravity = data.gravity;
     collides = data.collides;
     duration = data.duration;
     range = data.range;
     damage = data.damage;
 
     maxHP = data.targets;
     alive = true;
    hp = maxHP;
 
     startPos = new Vector2(x, y);
     startTime = time.getElapsed();
 
     // Invisible projectile
     // TODO: Do not use null
     if (renderer != null) {
      renderer.setAnimator(new Continuous(renderer.getTileCount()));
       add(renderer);
     }
   }
 
   @Override
   public void update(final GameTime time) {
     super.update(time);
 
     if (gravity) {
       // TODO: Move gravity somewhere else
       body.addVelocity(new Vector2(0, 100.0f * time.getFrameLength()));
     }
 
     if ((duration != -1) && ((time.getElapsed() - startTime) > duration)) {
       kill();
     }
     if ((range != -1) && (body.getMin().distance(startPos) > range)) {
       kill();
     }
   }
 
   public boolean canCollide() {
     return collides;
   }
 
   public float getDamage() {
     return damage;
   }
 
   @Override
   public void damage(final float value) {
     if (maxHP != -1) {
       hp -= value;
       if (hp <= 0) {
         hp = 0;
         kill();
       }
     }
   }
 
   @Override
   public void killSilently() {
     alive = false;
   }
 
   @Override
   public void kill() {
     killSilently();
   }
 
   @Override
   public boolean isAlive() {
     return alive;
   }
 
   @Override
   public boolean hasActions() {
     return false;
   }
 
   @Override
   public Collection<IAction> getActions() {
     return Collections.emptyList();
   }
 
   @Override
   public void clearActions() {
     // Do nothing
   }
 }
