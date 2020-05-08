 import java.util.HashMap;
 
 class Region extends Zone{
     private HashMap<String, Zone> zones;
 
     public Region(String name){
         this(name,0);
     }
 
     public Region(String name,int populationSize){
         super(name,populationSize);
         zones =  new HashMap<String, Zone>();
     }
 
     public void addZone(Zone z){
         zones.put(z.getName(), z);
     }
 
     public Zone getZone(String zoneName){
        // add zone to the hashmap if it isn't found
        if (!zones.containsKey(zoneName)) 
            zones.put(zoneName, new Zone(zoneName));
         return zones.get(zoneName); 
     }
 }
