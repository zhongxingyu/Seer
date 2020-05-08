 package com.codefuss.entities;
 
 import com.codefuss.factories.AmmoFactory;
 import com.codefuss.physics.Body;
 import org.newdawn.slick.geom.Vector2f;
 
 /**
  * @author Martin Vium <martin.vium@gmail.com>
  */
 final public class Player extends Creature {
 
    final static int AMMO_OFFSET = 17;

     public Player(AmmoFactory entityFactory, Vector2f position, Body body) {
         super(entityFactory, position, body);
     }
 
     @Override
     public Entity getMainAttack(int timeKeyPressed) {
         float x;
         if(direction == Direction.LEFT) {
            x = getPosition().x - AMMO_OFFSET;
         } else {
            x = getPosition().x + getWidth() + 1;
         }
         
         float y = getPosition().y + getHeight() / 4;
 
         return entityFactory.getRedBall(x, y, direction, timeKeyPressed);
     }
 }
