 package com.codefuss.entities;
 
 import com.codefuss.factories.EntityFactory;
 import com.codefuss.physics.Body;
 import org.newdawn.slick.geom.Vector2f;
 
 /**
  * @author Martin Vium <martin.vium@gmail.com>
  */
 final public class Player extends Creature {
 
     public Player(EntityFactory entityFactory, Vector2f position, Body body) {
         super(entityFactory, position, body);
     }
 
     @Override
     public Entity getMainAttack() {
        Vector2f barrelPosition = getPosition();
        barrelPosition.y += getHeight() / 4;
        barrelPosition.x += getWidth();
        return entityFactory.getShotgunFire(barrelPosition);
     }
 }
