 package com.archmageinc.RandomEncounters;
 
 import java.util.Calendar;
 import java.util.HashSet;
 import java.util.Set;
 import java.util.UUID;
 import org.bukkit.Chunk;
 import org.bukkit.Location;
 import org.bukkit.World;
 import org.json.simple.JSONObject;
 
 /**
  * Represents a configured expansion for an encounter
  * 
  * @author ArchmageInc
  */
 public class Expansion implements Cloneable{
     
     /**
      * The encounter that may spawn for this expansion.
      */
     protected Encounter encounter;
     
     /**
      * The unique name of the encounter for lazy loading.
      */
     protected String encounterName;
     
     /**
      * The probability of expansion.
      */
     protected Double probability;
     
     /**
      * The duration in minutes to check for expansion.
      */
     protected Long duration;
     
     /**
      * The maximum number of times the encounter can be placed for expansion.
      */
     protected Long max;
     
     /**
      * The maximum distance in chunks from the parent encounter this expansion can be placed.
      */
     protected Long distance;
     
     /**
      * The last time this expansion was checked to expand.
      */
     protected Calendar lastCheck                    =   (Calendar) Calendar.getInstance().clone();
     
     /**
      * Should this expansion be checked for valid locations.
      * This will be set to false when no valid locations are found.
      */
     protected boolean checkLocation                 =   true;
     
     /**
      * The set of chunks that have been checked and cannot support the encounter.
      * These chunks will not be checked again until the server is reloaded.
      * This prevents unnecessary processing.
      */
     protected HashSet<Chunk> invalidChunks          =   new HashSet();
     
     /**
      * Constructor for an expansion based on JSON Configuration.
      * 
      * @param jsonConfiguration The configuration
      */
     public Expansion(JSONObject jsonConfiguration){
         try{
             encounter       =   Encounter.getInstance((String) jsonConfiguration.get("encounter"));
             encounterName   =   (String) jsonConfiguration.get("encounter");
             probability     =   ((Number) jsonConfiguration.get("probability")).doubleValue();
             duration        =   ((Number) jsonConfiguration.get("duration")).longValue();
             max             =   ((Number) jsonConfiguration.get("max")).longValue();
             distance        =   ((Number) jsonConfiguration.get("distance")).longValue();
         }catch(ClassCastException e){
             RandomEncounters.getInstance().logError("Invalid expansion configuration: "+e.getMessage());
         }
     }
     
     /**
      * Checks the expansion for placement. 
      * If successful, the encounter will be placed in the world.
      * 
      * @param placedEncounter The PlacedEncounter attempting to spawn this expansion.
      * @TODO This needs a better, faster way to check expansion placements.
      */
     public void checkExpansion(PlacedEncounter placedEncounter){
         if(!checkLocation){
             if(RandomEncounters.getInstance().getLogLevel()>7){
                 RandomEncounters.getInstance().logMessage(placedEncounter.getEncounter().getName()+" -> "+getEncounter().getName()+" will not be checked.");
             }
             return;
         }
         Double random   =   Math.random();
         if(RandomEncounters.getInstance().getLogLevel()>6){
             RandomEncounters.getInstance().logMessage("    * Checking expansion for "+placedEncounter.getEncounter().getName()+" -> "+getEncounter().getName()+" : ("+random+","+probability+") ");
         }
         if(random<probability){
             Set<PlacedEncounter> validExpansions    =   new HashSet();
             for(UUID expansionUUID : placedEncounter.getPlacedExpansions()){
                 PlacedEncounter placedExpansion =   PlacedEncounter.getInstance(expansionUUID);
                if(placedExpansion.getEncounter().equals(getEncounter())){
                    validExpansions.add(placedExpansion);
                 }
             }
             if(RandomEncounters.getInstance().getLogLevel()>6){
                 RandomEncounters.getInstance().logMessage("      # Expansion probability hit for encounter "+placedEncounter.getEncounter().getName()+" -> "+getEncounter().getName()+". There are "+validExpansions.size()+" existing expansions, "+max+" are allowed.");
             }
             if(validExpansions.size()<max){
                 Chunk encounterChunk    =   placedEncounter.getLocation().getChunk();
                 World world             =   placedEncounter.getLocation().getWorld();
                 int x                   =   encounterChunk.getX();
                 int z                   =   encounterChunk.getZ();
                 boolean placed          =   false;
                 for(int cx=x-distance.intValue();cx<x+distance.intValue();cx++){
                     for(int cz=z-distance.intValue();cz<z+distance.intValue();cz++){
                         Chunk currentChunk  =   world.getChunkAt(cx, cz);
                         if(invalidChunks.contains(currentChunk)){
                             if(RandomEncounters.getInstance().getLogLevel()>7){
                                 RandomEncounters.getInstance().logMessage("Skipping chunk "+cx+","+cz+" for "+placedEncounter.getEncounter().getName()+" -> "+getEncounter().getName());
                             }
                             break;
                         }
                         Location location   =   Locator.getInstance().checkChunk(currentChunk, getEncounter());
                         if(location!=null){
                             PlacedEncounter newExpansion    =   PlacedEncounter.create(getEncounter(), location);
                             placedEncounter.addExpansion(newExpansion);
                             RandomEncounters.getInstance().addPlacedEncounter(newExpansion);
                             placed  =   true;
                             break;
                         }
                         if(RandomEncounters.getInstance().getLogLevel()>8){
                             RandomEncounters.getInstance().logMessage("Chunk "+cx+","+cz+" for "+placedEncounter.getEncounter().getName()+" -> "+getEncounter().getName()+" has been marked invalid.");
                         }
                         invalidChunks.add(currentChunk);
                         
                     }
                     if(placed){
                         break;
                     }
                 }
                 if(!placed){
                     if(RandomEncounters.getInstance().getLogLevel()>6){
                         RandomEncounters.getInstance().logMessage("The expansion "+placedEncounter.getEncounter().getName()+" -> "+getEncounter().getName()+" has no valid locations");
                     }
                     checkLocation   =   false;
                 }
             }
         }
     }
     
     /**
      * This clones the expansion configuration for individual PlacedEncounters.
      * Each PlacedEncounter stores their own Expansions because of specifics
      * @return Returns a copy of the Expansion
      * @throws CloneNotSupportedException 
      */
     @Override
     public Expansion clone() throws CloneNotSupportedException{
        return (Expansion) super.clone();
     }
     
     /**
      * Get the encounter of the expansion.
      * If the encounter has not been previously retrieved, it will attempt to do so.
      * This allows for circular referencing expansions. (i.e. an encounter can spawn itself as an expansion)
      * @return 
      */
     public Encounter getEncounter(){
         if(encounter==null){
             encounter   =   Encounter.getInstance(encounterName);
         }
         if(encounter==null){
             RandomEncounters.getInstance().logError("Unable to locate encounter "+encounterName+" for expansion!!");
         }
         return encounter;
     }
     
     /**
      * Get the wait time in minutes for this expansion.
      * 
      * @return 
      * @see Expansion#duration
      */
     public Long getDuration(){
         return duration;
     }
     
     /**
      * Get the last time this expansion was checked
      * 
      * @return 
      * @see Expansion#lastCheck
      */
     public Calendar getLastCheck(){
         return lastCheck;
     }
     
     /**
      * Update the last time this expansion was checked.
      * 
      * @see Expansion#lastCheck
      */
     public void updateLastCheck(){
         lastCheck   =   (Calendar) Calendar.getInstance().clone();
     }
 }
