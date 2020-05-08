 package com.fullwall.pets;
 
 import net.minecraft.server.EntitySnowman;
 import net.minecraft.server.EntityHuman;
 import net.minecraft.server.World;
 
 import org.bukkit.Bukkit;
 import org.bukkit.craftbukkit.CraftServer;
 import org.bukkit.craftbukkit.entity.CraftSnowman;
 import org.bukkit.craftbukkit.entity.CraftPlayer;
 import org.bukkit.entity.Snowman;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.Player;
 
 public class EntitySnowmanPet extends EntitySnowman { // new AI
     private final Player owner;
 
     public EntitySnowmanPet(World world, Player owner) {
         super(world);
         this.owner = owner;
         if (owner != null)
             Util.clearGoals(this.goalSelector, this.targetSelector);
     }
     
     public EntitySnowmanPet(World world) {
         this(world, null);
     }
 
     private int distToOwner() {
         EntityHuman handle = ((CraftPlayer) owner).getHandle();
         return (int) (Math.pow(locX - handle.locX, 2) + Math.pow(locY - handle.locY, 2) + Math.pow(locZ
                 - handle.locZ, 2));
     }
     
     @Override
     public void c() {
         if (owner == null)
             super.c();
     }
 
     @Override
    protected void bk() {
        super.bk();
         if (owner == null)
             return;
         this.getNavigation().a(((CraftPlayer)owner).getHandle(), 0.3F);
         if (distToOwner() > Util.MAX_DISTANCE)
             this.getBukkitEntity().teleport(owner);
     }
 
     @Override
     public Entity getBukkitEntity() {
         if (owner != null && bukkitEntity == null)
             bukkitEntity = new BukkitSnowmanPet(this);
         return super.getBukkitEntity();
     }
 
     public static class BukkitSnowmanPet extends CraftSnowman implements PetEntity {
         private final Player owner;
 
         public BukkitSnowmanPet(EntitySnowmanPet entity) {
             super((CraftServer) Bukkit.getServer(), entity);
             this.owner = entity.owner;
         }
 
         @Override
         public Snowman getBukkitEntity() {
             return this;
         }
 
         @Override
         public Player getOwner() {
             return owner;
         }
 
         @Override
         public void upgrade() {
             // upgrade logic here
             /*
                         int size = getSize() + 1;
                         if (Util.MAX_LEVEL != -1)
                             size = Math.min(Util.MAX_LEVEL, size);
                         setSize(size);
                         Location petLoc = getLocation();
                         getWorld().playSound(petLoc, Sound.LEVEL_UP, size, 1);
                         for (int i = 0; i < size; i++)
                             getWorld().playEffect(petLoc, Effect.SMOKE, (size + i) / 8);
             */
         }
 
         @Override
         public void setLevel(int level) {
             // setSize(level);
         }
     }
 }
