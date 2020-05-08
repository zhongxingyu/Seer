 /*
  * Copyright (c) 2009-2011 Daniel Oom, see license.txt for more info.
  */
 
 package game.triggers.effects;
 
 import game.components.Message;
 import game.entities.IEntity;
 import game.pods.GameTime;
 import game.pods.TimePos;
 import game.triggers.IEffect;
 import game.world.World;
 import math.Vector2;
 
 public class SpawnProjectile implements IEffect {
   private final IEntity projectile;
   private final Vector2 start;
 
   public SpawnProjectile(IEntity projectile, Vector2 start) {
     assert projectile != null;
 
     this.projectile = projectile;
     this.start      = start;
   }
 
   @Override
   public void execute(GameTime time, World world) {
     TimePos tp = new TimePos(time.elapsed, start);
     projectile.sendMessage(Message.START_AT, tp);
    world.addUnit(projectile);
   }
 }
