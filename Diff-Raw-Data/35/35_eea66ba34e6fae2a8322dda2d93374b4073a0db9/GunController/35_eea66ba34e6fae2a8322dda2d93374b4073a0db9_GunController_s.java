 package com.irr310.common.world.capacity.controller;
 
 import com.irr310.common.Game;
 import com.irr310.common.event.BulletFiredEvent;
 import com.irr310.common.tools.Vect3;
 import com.irr310.common.world.Component;
 import com.irr310.common.world.Part;
 import com.irr310.common.world.capacity.GunCapacity;
 
 public class GunController extends CapacityController {
 
     private final GunCapacity capacity;
     private final Component component;
 
     public GunController(Component component, GunCapacity capacity) {
         this.component = component;
         this.capacity = capacity;
     }
 
     @Override
     public void update(double duration) {
         if(capacity.fire) {
             capacity.fire = false;
             System.err.println("Gun want fire !");          
             
             Part part = component.getFirstPart();
             
             Vect3 to = new Vect3(0, capacity.range, 0).transform(part.getTransform());
             
             
            Game.getInstance().sendToAll(new BulletFiredEvent(part, capacity.damage, capacity.range, capacity.damageType, part.getTransform().getTranslation(), to));
             
         }
     }
 
 }
