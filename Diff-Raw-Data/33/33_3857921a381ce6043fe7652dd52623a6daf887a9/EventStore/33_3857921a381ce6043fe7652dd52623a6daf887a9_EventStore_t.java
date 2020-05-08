 package com.ticktockdevelopment.simpleeventstore.EventStores;
 
 import com.ticktockdevelopment.simpleeventstore.Infrastructure.IEventPublisher;
 import com.ticktockdevelopment.simpleeventstore.Messaging.Events.Event;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 /**
  * Created with IntelliJ IDEA.
  * User: tchaplin
  * Date: 1/23/13
  * Time: 8:58 AM
  * To change this template use File | Settings | File Templates.
  */
 public class EventStore implements IEventStore{
 
     private IEventPublisher publisher;
     private Map<String, List<EventDescriptor>> current = new HashMap<String, List<EventDescriptor>>();
 
     public EventStore(IEventPublisher publisher) {
         this.publisher = publisher;
     }
 
     @Override
     public void SaveEvents(int aggregateId, List<Event> events, int expectedVersion) {
 
         List<EventDescriptor> eventDescriptors = EventDescriptorsForAggregate(aggregateId);
 
         if(!hasValidVersion(eventDescriptors,expectedVersion))
             return;
 
         int currentVersion = expectedVersion;
         for (Event event : events)
         {
             currentVersion++;
             event.Version = currentVersion;
             eventDescriptors.add(new EventDescriptor(aggregateId,event,currentVersion));
             publisher.Publish(event);
         }
 
     }
 
     private boolean hasValidVersion(List<EventDescriptor> eventDescriptors, int expectedVersion) {
 
         if(eventDescriptors.isEmpty())
             return expectedVersion == -1;
 
        if(expectedVersion == -1)
            return true;

         int lastEventVersion = eventDescriptors.get(eventDescriptors.size() - 1).version;
        if(lastEventVersion == expectedVersion)
            return true;

        return false;
     }
 
     private List<EventDescriptor> EventDescriptorsForAggregate(int aggregateId)
     {
         String key = String.valueOf(aggregateId);
         if(!current.containsKey(key))
             current.put(key,new ArrayList<EventDescriptor>());
 
         return current.get(key);
     }
 
     @Override
     public List<Event> GetEventsForAggregate(int aggregateId) {
         List<EventDescriptor> eventDescriptors = EventDescriptorsForAggregate(aggregateId);
 
         List<Event> events = new ArrayList<Event>();
         for(EventDescriptor eventDescriptor : eventDescriptors)
         {
             events.add(eventDescriptor.event);
         }
 
         return events;
     }
 }
