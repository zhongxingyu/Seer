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
        if (firstActor.getCollisionModel().collidesWith(secondActor.getCollisionModel())) {
             action.doAction(firstActor, secondActor);
         }
     }
 }
