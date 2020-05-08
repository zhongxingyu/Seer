 package ch9k.eventpool;
 
import java.util.List;
import java.util.Map;
 
 /**
  * Distributes events across the application
  * @author Pieter De Baets
  */
 public class EventPool {
    private Map<String,List<EventListener>> listeners;
 
     /**
      * Add a new Event-listener that will listen to a given set of events
      * as defined by the filter.
      * @param listener
      * @param filter
      */
     public void addListener(EventListener listener, EventFilter filter) {
         
     }
 
     /**
      * Send an event through the system
      * @param event
      */
     public void raiseEvent(Event event) {
         
     }
 
     /**
      * Send an event through the network
      * (will also be broadcast in the local pool?)
      * @param networkEvent
      */
     public void raiseEvent(NetworkEvent networkEvent) {
         
     }
 }
