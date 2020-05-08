 package com.xerxes.engine.game;
 
 public class Collision {
     private Actor firstActor;
     private Actor secondActor;
     private CollisionAction action;
 
     public Collision(Actor firstActor, Actor secondActor, CollisionAction action) {
         this.firstActor = firstActor;
         this.secondActor = secondActor;
         this.action = action;
     }
 
     public void check() {
        boolean firstCollision = firstActor.getCollisionModel().collidesWith(secondActor.getCollisionModel());
        boolean secondCollision = secondActor.getCollisionModel().collidesWith(firstActor.getCollisionModel());
        if (firstCollision || secondCollision) {
             action.doAction(firstActor, secondActor);
         }
     }
 }
