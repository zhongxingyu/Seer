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
        // if a zone is not in the map, throw an exception
        // we don't instantiate because we don't know the type
        // of Zone (can be Region or Town)
        if (!zones.containsKey(zoneName))
            throw new IllegalArgumentException();
         return zones.get(zoneName); 
     }
 }
