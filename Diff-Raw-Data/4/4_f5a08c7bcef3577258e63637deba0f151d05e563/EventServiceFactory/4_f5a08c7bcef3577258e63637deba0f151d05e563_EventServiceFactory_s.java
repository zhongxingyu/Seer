 package org.eventroaster;
 
 import java.lang.reflect.Method;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 public final class EventServiceFactory {
 
     private EventServiceFactory() {
         throw new UnsupportedOperationException("This factory shouldn't be initialized");
     }
 
    private static final Map<EventServiceKey, EventService> CACHE = new HashMap<EventServiceKey, EventService>();
 
     public static EventService getEventService(final EventServiceKey eventServiceKey) {
         EventService eventService = CACHE.get(eventServiceKey);
         if (eventService == null) {
             eventService = createNewService(eventServiceKey);
             CACHE.put(eventServiceKey, eventService);
         }
         return eventService;
     }
 
     private static EventService createNewService(final EventServiceKey eventServiceKey) {
         final Map<Class<?>, List<Method>> methodsToInvoke = EventServiceScanner.getInstance()
                                                                                .getMethodsToInvoke();
         return new EventServiceImpl(eventServiceKey, methodsToInvoke);
     }
 }
