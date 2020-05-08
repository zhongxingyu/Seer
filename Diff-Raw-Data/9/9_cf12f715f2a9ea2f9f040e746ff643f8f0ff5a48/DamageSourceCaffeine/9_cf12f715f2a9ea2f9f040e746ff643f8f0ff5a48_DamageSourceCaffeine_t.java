 package com.madpc.coffee.potion;
 
 import net.minecraft.entity.EntityLiving;
 import net.minecraft.util.DamageSource;
 
 public class DamageSourceCaffeine extends DamageSource {
     
     public String[] messages = {
             "Wait, <P> had how much coffee?",
             "<P> couldn't handle their coffee",
             "<P> added too much boom to their coffee",
             "<P>'s liver punched them for drinking too much caffeine",
             "<P> learned that too much caffeine isn't good for you",
             "<P> learned that faster-than-light travel is not possible with caffeine",
             "<P> learned that they should switch to decaf",
             "<P> should have added cream, not gunpowder",
            "<P> learned how to not use caffeine",
            "<P> Shartted from caffeiene overdose"
     };
     
     public DamageSourceCaffeine() {
         super("coffee");
         this.setDamageBypassesArmor();
     }
     
     public String getDeathMessage(EntityLiving entity) {
         return messages[entity.worldObj.rand.nextInt(messages.length)].replaceAll("<P>", entity.getEntityName());
     }
 }
