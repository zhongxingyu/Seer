 package net.minecraft.world.biome;
 
 import net.minecraft.entity.EntityLiving;
 
 public class SpawnListEntry {
     public SpawnListEntry(Class<? extends EntityLiving> entityClass, int weightedProb, int min, int max) {
         this.field_76300_b = entityClass;
         this.field_76292_a = weightedProb;
         this.field_76301_c = min;
         this.field_76299_d = max;
 
     }
 
     public Class<? extends EntityLiving> field_76300_b;
    public int field_76292_a;
    public int field_76301_c;
    public int field_76299_d;
 }
