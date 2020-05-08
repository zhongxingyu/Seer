 package uk.co.acuminous.julez.test;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import uk.co.acuminous.julez.event.Event;
 import uk.co.acuminous.julez.event.filter.EventDataFilter;
 import uk.co.acuminous.julez.event.handler.InMemoryEventRepository;
 
 public class TestEventRepository extends InMemoryEventRepository {
 
     public void put(Event event) {
         events.add(event);
     }
     
     public Event first() {
         return iterator().next();
     }
     
     public Event get(int i) {
     	int sofar = 0;
     	for (Event event : events) {
     		if (sofar == i) return event;
    		++sofar;
     	}
         return null;
     }
     
     public Event last() {
     	Event ret = null;
     	for (Event event : events) {
     		ret = event;
     	}
         return ret;
     }
     
     public int count(String key, String pattern) {
         return getAll(key, pattern).size();
     }
     
     public List<Event> getAll(EventDataFilter filter) {
         List<Event> filteredEvents = new ArrayList<Event>();
         for (Event event : events) {
             if (filter.accept(event)) {
                 filteredEvents.add(event);
             }
         }
         return filteredEvents;
     }
     
     public List<Event> getAll(String key, String pattern) {
         return getAll(new EventDataFilter(key, pattern));
     }
 }
