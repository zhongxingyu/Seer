 package uk.co.acuminous.julez.scenario.event;
 
 import java.util.HashMap;
 import java.util.Map;
 


 public class ScenarioEventInMemoryRepository implements ScenarioEventRepository {
 
     Map<String, ScenarioEvent> events = new HashMap<String, ScenarioEvent>();
     
     @Override
     public ScenarioEvent get(String id) {
         return events.get(id);
     }
 
     @Override
     public int count() {
         return events.size();
     }
 
     @Override
     public void add(ScenarioEvent event) {
         events.put(event.getId(), event);
     }
 
     @Override
     public void dump() {
         for (ScenarioEvent event : events.values()) {
             System.out.println(event);
         }
     }
 
     @Override
     public void dump(String type) {
         for (ScenarioEvent event : events.values()) {
            if (type == event.getType()) {
                 System.out.println(event);
             }
         }
     }
 
 }
