 /*
  * Jaffas and more!
  * author: monnef
  */
 
 package monnef.core.utils;
 
 import cpw.mods.fml.common.registry.EntityRegistry;
 import cpw.mods.fml.relauncher.ReflectionHelper;
 import monnef.core.MonnefCorePlugin;
 import net.minecraft.entity.Entity;
 import net.minecraft.entity.EntityLivingBase;
 import net.minecraft.entity.passive.EntityAnimal;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.util.AxisAlignedBB;
 import net.minecraft.util.DamageSource;
 import net.minecraft.util.Vec3;
 import net.minecraft.world.World;
 import net.minecraftforge.common.util.ForgeDirection;
 
 import java.util.HashMap;
 import java.util.List;
 
 public class EntityHelper {
 
     public static final float MAX_UP_MOTION = 0.5f;
 
     public static boolean animalIsAdult(EntityLivingBase animal) {
         return animal instanceof EntityAnimal ? ((EntityAnimal) animal).getGrowingAge() >= 0 : true;
     }
 
     public static String formatCoordinates(Entity e) {
         StringBuilder s = new StringBuilder();
         s.append(e.posX);
         s.append(", ");
         s.append(e.posY);
         s.append(", ");
         s.append(e.posZ);
         return s.toString();
     }
 
     public static void pushEntitiesBack(World w, Vec3 hitVec, float force, float radius, float damage, float yBoost, EntityLivingBase thrower) {
         AxisAlignedBB area = AxisAlignedBB.getBoundingBox(hitVec.xCoord, hitVec.yCoord, hitVec.zCoord, hitVec.xCoord, hitVec.yCoord, hitVec.zCoord).expand(radius, radius, radius);
         List entities = w.getEntitiesWithinAABB(Entity.class, area);
         for (Entity entity : (List<Entity>) entities) {
             double dist = entity.getDistance(hitVec.xCoord, hitVec.yCoord, hitVec.zCoord) / radius;
             if (dist <= 1) {
                 Vec3 distFromExpl = VectorUtils.createVector(entity.posX - hitVec.xCoord, entity.posY + entity.getEyeHeight() - hitVec.yCoord, entity.posZ - hitVec.zCoord);
                 double len = distFromExpl.lengthVector();
                 if (len == 0) {
                     continue;
                 }
                 distFromExpl = VectorUtils.divideVector(distFromExpl, len); // normalization
                 double reduction = entity.boundingBox != null ? w.getBlockDensity(hitVec, entity.boundingBox) : 1;
                 double dmg = (1 - dist) * damage * reduction;
                 DamageSource dmgSrc = thrower != null
                         ? (thrower instanceof EntityPlayer ? DamageSource.causePlayerDamage((EntityPlayer) thrower) : DamageSource.causeMobDamage(thrower))
                         : DamageSource.generic;
                 entity.attackEntityFrom(dmgSrc, (int) (dmg + 0.5));
                 entity.motionX += distFromExpl.xCoord * force;
                 entity.motionY += distFromExpl.yCoord * force + yBoost;
                 entity.motionZ += distFromExpl.zCoord * force;
                 if (entity.motionY > MAX_UP_MOTION) entity.motionY = MAX_UP_MOTION;
             }
         }
     }
 
     public static void setTrackingRange(Class<? extends Entity> aClass, int range) {
         EntityRegistry.EntityRegistration record = EntityRegistry.instance().lookupModSpawn(aClass, false);
         ReflectionHelper.setPrivateValue(EntityRegistry.EntityRegistration.class, record, range, "trackingRange");
     }
 
     public static void registerModEntity(Class<? extends Entity> entityClass, String entityName, int trackingRange, int updateFrequency, boolean sendsVelocityUpdates, Object modInstance) {
        registerEntity(entityClass, entityName, trackingRange, updateFrequency, sendsVelocityUpdates, -1, modInstance, false, 0, 0);
     }
 
    public static void registerGlobalEntity(Class<? extends Entity> entityClass, String entityName, int trackingRange, int updateFrequency, boolean sendsVelocityUpdates, int globalId, Object modInstance, int backEgg, int foreEgg) {
        registerEntity(entityClass, entityName, trackingRange, updateFrequency, sendsVelocityUpdates, globalId, modInstance, true, backEgg, foreEgg);
     }
 
    private static void registerEntity(Class<? extends Entity> entityClass, String entityName, int trackingRange, int updateFrequency, boolean sendsVelocityUpdates, int globalId, Object modInstance, boolean registerSpawnEggs, int backEgg, int foreEgg) {
        if (globalId == -1 && registerSpawnEggs)
            throw new RuntimeException("Cannot register global entity (b/c spawn eggs) without proper id (currently got -1).");
         if (registerSpawnEggs) {
             int id = getNextFreeGlobalEntityID();
             EntityRegistry.registerGlobalEntityID(entityClass, entityName, id, backEgg, foreEgg);
            MonnefCorePlugin.Log.printFine("Registered global entity class \"" + entityClass + "\" with id #" + id);
        } else {
            int id = getNextFreeModID(modInstance);
            EntityRegistry.registerModEntity(entityClass, entityName, id, modInstance, trackingRange, updateFrequency, sendsVelocityUpdates);
            MonnefCorePlugin.Log.printFine("Registered local (mod) entity class \"" + entityClass + "\" with id #" + id);
         }
     }
 
     public static void kickEntityInDirection(EntityLivingBase entity, ForgeDirection direction, float force) {
         entity.addVelocity(direction.offsetX * force, direction.offsetY * force, direction.offsetZ * force);
     }
 
     public static int getNextFreeGlobalEntityID() {
         return EntityRegistry.findGlobalUniqueEntityId();
     }
 
     private static HashMap<Object, Integer> nextFreeIdPerMod = new HashMap<Object, Integer>();
 
     public static int getNextFreeModID(Object modInstance) {
         if (!nextFreeIdPerMod.containsKey(modInstance)) nextFreeIdPerMod.put(modInstance, 0);
         int nextFreeId = nextFreeIdPerMod.get(modInstance);
         nextFreeIdPerMod.put(modInstance, nextFreeId + 1);
         return nextFreeId;
     }
 }
