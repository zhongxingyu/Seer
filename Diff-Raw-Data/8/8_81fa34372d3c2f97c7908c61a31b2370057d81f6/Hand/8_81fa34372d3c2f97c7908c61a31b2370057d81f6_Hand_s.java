 /*
  * Copyright (c) 2009-2011 Daniel Oom, see license.txt for more info.
  */
 
 package game.components.holdables;
 
 import game.components.Message;
 import game.components.holdables.weapons.Weapon;
 import game.components.interfaces.IRenderComponent;
 import game.entities.Entity;
 import game.entities.IEntity;
 import game.factories.ProjectileFactory;
 import game.pods.GameTime;
 import game.triggers.effects.SpawnProjectile;
 import math.Vector2;
 
 import org.newdawn.slick.Graphics;
 
 import ui.hud.infobar.IProgress;
 
 public class Hand implements IRenderComponent, IProgress {
   private final Vector2 offset;
 
   private final Entity owner;
   private Weapon weapon;
 
   public Hand(Entity owner, float handOffsetX, float handOffsetY) {
     this.owner = owner;
 
     offset = new Vector2(handOffsetX, handOffsetY);
 
     weapon = null;
   }
 
   public void startUse() {
     if (weapon != null) {
       weapon.toggleOn();
     }
   }
 
   public void stopUse() {
     if (weapon != null) {
       weapon.toggleOff();
     }
   }
 
   public void grab(Weapon newItem) {
     assert newItem != null;
 
     // To grab a new holdable we need to stop using the current one
     stopUse();
 
     weapon = newItem;
   }
 
   @Override
   public void update(GameTime time) {
     weapon.update(time);
 
     // Find out if there are any projectiles that want to be spawned
     for (ProjectileFactory projTemplate : weapon.projectiles) {
       // Muzzle relative to player entity
       Vector2 m = Vector2.add(offset, weapon.getMuzzleOffset());
 
       // Muzzle relative to the world
       Vector2 o = Vector2.add(owner.getBody().getMin(), m);
 
       IEntity p = projTemplate.makeProjectile(owner, o.x, o.y, weapon.getAngle());
       owner.addEffect(new SpawnProjectile(p, o));
     }
     weapon.projectiles.clear();
   }
 
   @Override
   public void render(Graphics g) {
     g.pushTransform();
     g.translate(offset.x, offset.y);
 
     weapon.render(g);
 
     g.popTransform();
   }
 
   @Override
   public void reciveMessage(Message message, Object args) {
     if (message == Message.START_HOLDABLE) {
       startUse();
     } else if (message == Message.STOP_HOLDABLE) {
       stopUse();
     }
   }
 
   @Override
   public float getProgress() {
     return weapon.getProgress();
   }
 }
