 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.mahn42.anhalter42.quest.action;
 
 import com.mahn42.framework.BlockPosition;
 import org.bukkit.entity.EntityType;
 
 /**
  *
  * @author andre
  */
 public class SpawnEntity extends Action {
    public EntityType type = EntityType.PIG;
     public BlockPosition to = new BlockPosition();
     public BlockPosition vector = new BlockPosition();
     public int amount = 1;
     public float speed = (float) 0.6; // for ARROW
     public float spread = 12;         // for ARROW
     
     @Override
     public void initialize() {
         super.initialize();
         to.add(quest.edge1);
     }
     
     @Override
     public void execute() {
         for(int i=0;i<amount;i++) {
            if (type == EntityType.ARROW) {
                 quest.world.spawnArrow(to.getLocation(quest.world), vector.getVector(), speed, spread);
             } else {
                quest.world.spawnEntity(to.getLocation(quest.world), type);
             }
         }
     }
 }
