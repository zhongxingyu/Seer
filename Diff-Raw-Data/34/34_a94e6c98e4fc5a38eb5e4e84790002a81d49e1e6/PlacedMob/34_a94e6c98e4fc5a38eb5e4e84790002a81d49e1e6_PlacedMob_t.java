 package com.archmageinc.RandomEncounters.Mobs;
 
 import com.archmageinc.RandomEncounters.Encounters.PlacedEncounter;
 import com.archmageinc.RandomEncounters.Mobs.Mob;
 import com.archmageinc.RandomEncounters.RandomEncounters;
 import com.archmageinc.RandomEncounters.Tasks.SpawningTask;
 import java.util.HashSet;
 import java.util.List;
 import java.util.UUID;
 import org.bukkit.Location;
 import org.bukkit.World;
 import org.bukkit.entity.EntityType;
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.entity.Wolf;
 import org.bukkit.entity.PigZombie;
 import org.bukkit.entity.Skeleton;
 import org.bukkit.inventory.ItemStack;
 import org.json.simple.JSONObject;
 
 /**
  * Represents a Creature in the world which belongs to a PlacedEncounter.
  * 
  * @author ArchmageInc
  */
 public class PlacedMob {
     
     /**
      * The singleton instances of PlacedMobs.
      */
     private static HashSet<PlacedMob> instances   =   new HashSet();
     
     /**
      * The unique ID of the entity in the world.
      */
     private UUID uuid;
     
     /**
      * The parent Mob Configuration.
      */
     private Mob mob;
     
     /**
      * The LivingEntity in the world.
      */
     private LivingEntity entity;
     
     /**
      * The parent Encounter Configuration.
      */
     private PlacedEncounter encounter;
     
     /**
      * Get an instance of the PlacedMob based on the Unique ID.
      * @param uuid The unique ID
      * @return Returns the PlacedMob if found, null otherwise.
      */
     public static PlacedMob getInstance(UUID uuid){
         for(PlacedMob instance : instances){
             if(instance.getUUID().equals(uuid)){
                 return instance;
             }
         }
         return null;
     }
     
     /**
      * Get an instance of the PlacedMob based on the JSON Configuration
      * 
      * @param jsonConfiguration The JSON Configuration
      * @param encounter The PlacedEncounter to which this mob belongs.
      * @return Returns the PlacedMob
      */
     public static PlacedMob getInstance(JSONObject jsonConfiguration,PlacedEncounter encounter){
         try{
             UUID jsonUUID       =   UUID.fromString((String) jsonConfiguration.get("uuid"));
             PlacedMob located   =   PlacedMob.getInstance(jsonUUID);
             if(located!=null){
                 return located;
             }
         }catch(ClassCastException e){
             RandomEncounters.getInstance().logError("Invalid PlacedMob configuration: "+e.getMessage());
         }catch(IllegalArgumentException e){
             RandomEncounters.getInstance().logError("Invalid UUID in PlacedMob configuration: "+e.getMessage());
         }
         return new PlacedMob(jsonConfiguration,encounter);
     }
     
     /**
      * Static Method to create a new PlacedMob based on the Mob configuration for a PlacedEncounter at the given Location.
      * 
      * @param mob
      * @param encounter
      * @return Returns the newly created PlacedMob
      */
     public static PlacedMob create(Mob mob,PlacedEncounter encounter){
        return new PlacedMob(mob,encounter,null);
    }
    
    public static PlacedMob create(Mob mob,PlacedEncounter encounter, Location location){
        return new PlacedMob(mob,encounter,location);
     }
     
     /**
      * Constructor for creating a new PlacedMob
      * 
      * @param mob The Mob configuration
      * @param encounter The PlacedEncounter to which this creature will belong
      */
    private PlacedMob(Mob mob,PlacedEncounter encounter,Location location){
         
         this.mob                =   mob;
         this.encounter          =   encounter;
        Location spawnLocation  =   location==null ? encounter.findSafeSpawnLocation() : location;
         if(spawnLocation==null){
             spawnLocation   =   encounter.getLocation();
             RandomEncounters.getInstance().logWarning("Attempt to spawn "+encounter.getName()+": "+mob.getName()+" had no safe spawn locations, using encounter location.");
         }
         entity     =   (LivingEntity) encounter.getLocation().getWorld().spawnEntity(spawnLocation, mob.getType());
         uuid       =   entity.getUniqueId();
         entity.setRemoveWhenFarAway(false);
         if(mob.getTagName()!=null){
             entity.setCustomName(mob.getTagName());
             entity.setCustomNameVisible(true);
         }
         /*
         @TODO: This is terrible, but how MC has implemented wither skeletons.
         */
         if(mob.getTypeName().toLowerCase().equals("witherskeleton")){
             Skeleton tmpEntity  =   (Skeleton) entity;
             tmpEntity.setSkeletonType(Skeleton.SkeletonType.WITHER);
         }
         mob.setEquipment(entity);
         mob.setEffects(entity);
         if(mob.getType().equals(EntityType.WOLF)){
             ((Wolf) entity).setAngry(true);
         }
         if(mob.getType().equals(EntityType.PIG_ZOMBIE)){
             ((PigZombie) entity).setAngry(true);
         }
         if(RandomEncounters.getInstance().getLogLevel()>7){
             RandomEncounters.getInstance().logMessage("Placed mob "+encounter.getName()+": "+mob.getName()+" ("+mob.getTypeName()+") at "+spawnLocation.getWorld().getName()+": "+spawnLocation.getX()+","+spawnLocation.getY()+","+spawnLocation.getZ()+" - "+uuid.toString());
         }
         instances.add(this);
     }
     
     /**
      * Constructor to instantiate the PlacedMob based on JSON Configuration
      * 
      * @param jsonConfiguration
      * @param encounter The PlacedEncounter to which the creature should belong.
      */
     private PlacedMob(JSONObject jsonConfiguration,PlacedEncounter encounter){
         try{
             this.encounter   =   encounter;
             uuid             =   UUID.fromString((String) jsonConfiguration.get("uuid"));
             mob              =   Mob.getInstance((String) jsonConfiguration.get("mob"));
             if(mob==null){
                 RandomEncounters.getInstance().logError("Missing Mob ("+(String) jsonConfiguration.get("mob")+") from PlacedMob configuration");
             }
             entity  =   getEntity();
             mob.setEffects(entity);
             instances.add(this);
         }catch(ClassCastException e){
             RandomEncounters.getInstance().logError("Invalid PlacedMob configuration: "+e.getMessage());
         }catch(IllegalArgumentException e){
             RandomEncounters.getInstance().logError("Invalid UUID in PlacedMob configuration: "+e.getMessage());
         }
     }    
     
     /**
      * Gets the Unique ID for the Entity in the world.
      * @return 
      */
     public UUID getUUID(){
         return uuid;
     }
     
     /**
      * Gets the entity in the world.
      * @return 
      */
     public final LivingEntity getEntity(){
         if(entity==null){
             for(World world : RandomEncounters.getInstance().getServer().getWorlds()){
                 for(LivingEntity wentity : world.getLivingEntities()){
                     if(wentity.getUniqueId().equals(uuid)){
                         entity  =   wentity;
                         break;
                     }
                 }
                 if(entity!=null){
                     break;
                 }
             }
         }
         return entity;
     }
     
     /**
      * Gets a list of items to be dropped upon the entities death.
      * @return 
      * @see Mob#getDrop() 
      */
     public List<ItemStack> getDrop(){
         return mob.getDrop();
     }
     
     /**
      * Notify the system that this creature has died, and place any treasures in the world.
      * 
      * @see PlacedMob#getDrop() 
      * @see PlacedEncounter#notifyMobDeath(com.archmageinc.RandomEncounters.PlacedMob) 
      */
     public void die(){
         if(RandomEncounters.getInstance().getLogLevel()>7){
             RandomEncounters.getInstance().logMessage(encounter.getName()+": "+mob.getName()+" ("+mob.getTypeName()+") has died.");
         }
         if(getEntity()!=null){
             for(ItemStack item : getDrop()){
                 getEntity().getWorld().dropItem(entity.getLocation(), item);
             }
             Mob deathSpawn  =   mob.getDeathSpawn();
             if(deathSpawn!=null){
                (new SpawningTask(deathSpawn,encounter,getEntity().getLocation())).runTaskTimer(RandomEncounters.getInstance(),1,1);
             }
         }else{
             RandomEncounters.getInstance().logWarning(encounter.getName()+": "+mob.getName()+" ("+mob.getTypeName()+") died but could not be found: "+uuid.toString());
         }
         encounter.notifyMobDeath(this);
         instances.remove(this);
     }
     
     public PlacedEncounter getPlacedEncounter(){
         return encounter;
     }
     
     public Mob getMob(){
         return mob;
     }
     
     /**
      * Convert this PlacedMob into a JSONObject
      * @return the JSON Configuration for this creature.
      */
     public JSONObject toJSON(){
         JSONObject jsonConfiguration    =   new JSONObject();
         
         jsonConfiguration.put("uuid", uuid.toString());
         jsonConfiguration.put("mob", mob.getName());
         
         return jsonConfiguration;
     }
 }
