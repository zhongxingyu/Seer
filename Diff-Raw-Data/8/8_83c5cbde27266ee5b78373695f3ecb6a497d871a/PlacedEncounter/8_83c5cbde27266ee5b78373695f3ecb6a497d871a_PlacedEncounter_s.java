 package com.archmageinc.RandomEncounters.Encounters;
 
 import com.archmageinc.RandomEncounters.Mobs.PlacedMob;
 import com.archmageinc.RandomEncounters.RandomEncounters;
 import com.archmageinc.RandomEncounters.Tasks.SpawnLocatorTask;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 import java.util.UUID;
 import org.bukkit.Location;
 import org.json.simple.JSONArray;
 import org.json.simple.JSONObject;
 
 /**
  * Represents an Encounter that has been placed in the world.
  * 
  * @author ArchmageInc
  */
 public class PlacedEncounter {
     
     /**
      * The generated Unique ID.
      */
     private UUID uuid;
     
     /**
      * The location of this placed encounter in the world.
      */
     private Location location;
     
     /**
      * The Encounter on which this is based.
      */
     private Encounter encounter;
     
     /**
      * The set of PlacedMobs that were spawned with this encounter.
      */
     private final Set<PlacedMob> mobs                   =   new HashSet();
     
     /**
      * Has this encounter been sacked.
      */
     private Boolean sacked                        =   false;
     
     /**
      * The set of placed encounter unique IDs that have expanded from this placed encounter.
      * 
      */
     private final Set<UUID> placedExpansions            =   new HashSet();
     
     /**
      * The set of valid expansion configurations for this Placed Encounter.
      * 
      * This is a clone of the Encounter configuration expansions
      * @see Encounter#expansions
      */
     private final Set<Expansion> expansions             =   new HashSet();
     
     /**
      * The singlton instances of loaded PlacedEncounters.
      */
     private static Set<PlacedEncounter> instances =   new HashSet();
     
     /**
      * An internal list of safe creature spawn locations.
      */
     private List<Location> spawnLocations         =   new ArrayList();
     
     
     /**
      * Get an instance of the placed encounter based on the Unique ID.
      * 
      * @param uuid The unique ID of the PlacedEncounter
      * @return Returns the PlacedEncounter if found, null otherwise.
      */
     public static PlacedEncounter getInstance(UUID uuid){
         for(PlacedEncounter instance : instances){
             if(instance.getUUID().equals(uuid)){
                 return instance;
             }
         }
         return null;
     }
     
     /**
      * Get an instance of the PlacedEncounter based on JSON Configuration data.
      * 
      * @param jsonConfiguration The JSON Configuration
      * @return Returns the PlacedEncounter defined by the JSON Configuration.
      */
     public static PlacedEncounter getInstance(JSONObject jsonConfiguration){
         try{
             UUID jsonUUID             =   UUID.fromString((String) jsonConfiguration.get("uuid"));
             PlacedEncounter located   =   PlacedEncounter.getInstance(jsonUUID);
             if(located!=null){
                 return located;
             }
         }catch(ClassCastException e){
             RandomEncounters.getInstance().logError("Invalid PlacedEncounter configuration: "+e.getMessage());
         }catch(IllegalArgumentException e){
             RandomEncounters.getInstance().logError("Invalid UUID in PlacedEncounter configuration: "+e.getMessage());
         }
         return new PlacedEncounter(jsonConfiguration);
     }
     
     /**
      * Static Method to create a new PlacedEncounter based on an Encounter at a Lcoation.
      * 
      * @param encounter
      * @param location
      * @return The newly created PlacedEncounter
      * @see PlacedEncounter#PlacedEncounter(com.archmageinc.RandomEncounters.Encounter, org.bukkit.Location) 
      */
     public static PlacedEncounter create(Encounter encounter,Location location){
         return new PlacedEncounter(encounter,location);
     }
     
     /**
      * Constructor for PlacedEncounter based on JSON Configuration
      * 
      * @param jsonConfiguration The JSON configuration
      */
     private PlacedEncounter(JSONObject jsonConfiguration){
         try{
             uuid                    =   UUID.fromString((String) jsonConfiguration.get("uuid"));
             sacked                  =   (Boolean) jsonConfiguration.get("sacked");
             encounter               =   Encounter.getInstance((String) jsonConfiguration.get("encounter"));
             JSONObject jsonLocation =   (JSONObject) jsonConfiguration.get("location");
             location                =   new Location(RandomEncounters.getInstance().getServer().getWorld((String) jsonLocation.get("world")),(Long) jsonLocation.get("x"),(Long) jsonLocation.get("y"),(Long) jsonLocation.get("z"));
             if(encounter==null){
                 RandomEncounters.getInstance().logError("Missing Encounter ("+(String) jsonConfiguration.get("encounter")+") from PlacedEncounter configuration");
             }
             JSONArray jsonMobs   =   (JSONArray) jsonConfiguration.get("mobs");
             if(jsonMobs!=null){
                 for(int i=0;i<jsonMobs.size();i++){
                     mobs.add(PlacedMob.getInstance((JSONObject) jsonMobs.get(i),this));
                 }
             }
             
             JSONArray jsonExpansions    =   (JSONArray) jsonConfiguration.get("expansions");
             if(jsonExpansions!=null){
                 for(int i=0;i<jsonExpansions.size();i++){
                     JSONObject jsonExpansion    =   (JSONObject) jsonExpansions.get(i);
                     try{
                         UUID expansionUUID          =   UUID.fromString((String) jsonExpansion.get("uuid"));
                         placedExpansions.add(expansionUUID);
                     }catch(IllegalArgumentException e){
                         RandomEncounters.getInstance().logError("Invalid UUID in PlacedEncounter expansion configuration: "+e.getMessage());
                     }
                 }
             }
             setupExpansions();
             instances.add(this);
         }catch(ClassCastException e){
             RandomEncounters.getInstance().logError("Invalid PlacedEncounter configuration: "+e.getMessage());
         }catch(IllegalArgumentException e){
             RandomEncounters.getInstance().logError("Invalid UUID in PlacedEncounter configuration: "+e.getMessage());
         }
     }
     
     /**
      * Constructor to create a new PlacedEncounter which will place the structure and spawn creatures.
      * 
      * @param encounter The parent Encounter
      * @param location The location to place the encounter
      */
     private PlacedEncounter(Encounter encounter,Location location){
         this.uuid       =   UUID.randomUUID();
         this.encounter  =   encounter;
         this.location   =   location;
         if(encounter.getStructure()!=null){
             encounter.getStructure().place(this);
         }
         
         setupExpansions();
         instances.add(this);        
     }
     
     public void placeMobs(){
         (new SpawnLocatorTask(this)).runTaskTimer(RandomEncounters.getInstance(),1,1);
     }
     
     public void addMobs(Set<PlacedMob> newMobs){
         mobs.addAll(newMobs);
     }
     
     public void setSpawnLocations(List<Location> locations){
         spawnLocations =   locations;
     }
     
     /**
      * Sets up the cloned expansion configurations for newly generated PlacedEncounters.
      */
     private void setupExpansions(){
         expansions.clear();
         for(Expansion expansion : encounter.getExpansions()){
             try {
                 Expansion clonedExpansion   =   expansion.clone(this);
                 expansions.add(clonedExpansion);
             } catch (CloneNotSupportedException e) {
                 RandomEncounters.getInstance().logError("Clone failed for expansion: "+e.getMessage());
             }
         }
     }
     
     /**
      * Gets a random spawn location for a creature.
      * 
      * @return Returns a location to spawn a creature.
      */
     public Location findSafeSpawnLocation(){
         if(spawnLocations.isEmpty()){
             return null;
         }
         Collections.shuffle(spawnLocations);
         return spawnLocations.get(1);
     }
     
     /**
      * Adds an expansion to the list.
      * 
      * @param expansion The PlacedEncounter that expanded.
      */
     public void addExpansion(PlacedEncounter expansion){
         placedExpansions.add(expansion.getUUID());
     }
     
     /**
      * Gets the Unique ID of this PlacedExpansion
      * @return 
      */
     public UUID getUUID(){
         return uuid;
     }
     
     /**
      * Gets the location of this PlacedExpansion.
      * @return 
      */
     public Location getLocation(){
         return location;
     }
     
     /**
      * Removes the PlacedMob from the list of Mobs in this encounter.
      * @param mob The PlacedMob to be removed
      */
     public void notifyMobDeath(PlacedMob mob){
         mobs.remove(mob);
         if(mobs.isEmpty()){
             sacked  =   true;
             if(RandomEncounters.getInstance().getLogLevel()>6){
                 RandomEncounters.getInstance().logMessage(encounter.getName()+" has been sacked!");
             }
             RandomEncounters.getInstance().removePlacedEncounter(this);
             instances.remove(this);
         }
     }
     
     /**
      * Adds a mob to the encounter
      * @param mob 
      */
     public void addMob(PlacedMob mob){
         mobs.add(mob);
     }
     
     /**
      * Gets the parent Encounter configuration that generated this PlacedEncounter.
      * @return 
      */
     public Encounter getEncounter(){
         return encounter;
     }
     
     /**
      * Gets the unique name of the parent Encounter that generated this PlacedEncounter.
      * @return 
      */
     public String getName(){
         return encounter.getName();
     }
     
     /**
      * Gets the set of unique IDs of expansions spawned from this PlacedEncounter.
      * @return 
      */
     public Set<UUID> getPlacedExpansions(){
         return placedExpansions;
     }
     
     /**
      * Gets the set of Expansion configurations for this PlacedEncounter.
      * @return 
      */
     public Set<Expansion> getExpansions(){
         return expansions;
     }
     
     /**
      * Has this encounter been sacked.
      * @return 
      */
     public boolean isSacked(){
         return sacked;
     }
     
     /**
      * Convert the PlacedEncounter into a JSONObject for serialization.
      * @return Returns the JSONObject
      */
     public JSONObject toJSON(){
         JSONObject jsonConfiguration    =   new JSONObject();
         jsonConfiguration.put("uuid", uuid.toString());
         jsonConfiguration.put("encounter",encounter.getName());
         jsonConfiguration.put("sacked",sacked);
         
         JSONObject jsonLocation         =   new JSONObject();
         jsonLocation.put("world",location.getWorld().getName());
         jsonLocation.put("x",location.getBlockX());
         jsonLocation.put("y",location.getBlockY());
         jsonLocation.put("z",location.getBlockZ());
         jsonConfiguration.put("location", jsonLocation);
         
         JSONArray jsonMobs              =   new JSONArray();
         for(PlacedMob mob : mobs){
             jsonMobs.add(mob.toJSON());
         }
         jsonConfiguration.put("mobs",jsonMobs);
         
         JSONArray jsonExpansions        =   new JSONArray();
         for(UUID expansion : placedExpansions){
             JSONObject jsonExpansion    =   new JSONObject();
             jsonExpansion.put("uuid",expansion.toString());
             jsonExpansions.add(jsonExpansion);
         }
         jsonConfiguration.put("expansions", jsonExpansions);
         
         return jsonConfiguration;
     }
 }
