 package com.dafttech.eventmanager;
 
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.Map;
 
 public class EventType {
     volatile protected String name = "";
     volatile protected EventManager[] validEventManagers;
 
     protected static Map<String, EventType> types = new HashMap<String, EventType>();
     public static final int PRIORITY_STANDARD = 0;
 
     public EventType(String name, EventManager... eventManagers) {
         this.name = name;
         types.put(name, this);
         validEventManagers = eventManagers;
     }
 
     protected EventType() {
     }
 
     public final String getName() {
         return name;
     }
 
     /**
      * Used to be overridden in subclasses (use anonymous classes), to filter
      * out the EventListeners
      * 
      * @param event
      *            Event - the called event
      * @param filter
      *            Object[] - Is the given filter on registering an EventListener
     * @param eventListener
     *            Object - Is the instance of the EventListener class
     * 
      * @return boolean: true, if the EventListener should be called.
      */
     protected boolean applyFilter(Event event, Object[] filter, Object eventListener) {
         return true;
     }
 
     /**
      * Used to be overridden in subclasses (use anonymous classes), to catch the
      * events before all the EventListeners
      * 
      * @param event
      *            Event - Is the called event (can be cancelled)
      */
     protected void onEvent(Event event) {
     }
 
     protected final boolean isValidEventManager(EventManager eventManager) {
         return validEventManagers.length == 0 || Arrays.asList(validEventManagers).contains(eventManager);
     }
 
     @Override
     public boolean equals(Object object) {
         if (object instanceof EventType) {
             if (object == this) return true;
         } else if (object instanceof String) {
             if (((String) object).equals(name)) return true;
         }
         return false;
     }
 }
