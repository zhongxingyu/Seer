 package com.dafttech.eventmanager;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import com.dafttech.eventmanager.exception.AsyncEventQueueOverflowException;
 
 public class EventType {
     volatile protected List<EventListenerContainer> eventListenerContainer = new ArrayList<EventListenerContainer>();
     volatile protected EventManager eventManager = null;
     volatile protected String name = "";
     volatile protected int id = 0;
 
     public static final int PRIORITY_STANDARD = 0;
 
     public EventType(EventManager eventManager, String name) {
         this.eventManager = eventManager;
         eventManager.events.add(this);
         this.id = eventManager.events.size();
         this.name = name;
     }
 
     public final String getName() {
         return name;
     }
 
     public final int getId() {
         return id;
     }
 
     /**
      * Registers an Event Listener to call the onEvent Method. You can set a
      * priority, to specify, in which order the events have to be called. The
      * higher the priority value is, the earlier the Event Listener will be
      * called. You can also activate the forceCall parameter, to receive Events,
      * which doesn't belong to that Event Listener.
      * 
      * @param Event
      *            Listener: Event Listener to be registered.
      * @param int: priority. Higher = more important, lower = less.
      * @param boolean: Activate forceCall to receive all Events of this type.
      */
     public final void registerEventListener(Object eventListener) {
         eventManager.registerAnnotatedMethods(eventListener, this);
     }
 
    // TODO: Correct Method
     public final void unregisterEventListener(Object eventListener) {
         eventListenerContainer.remove(eventListener);
     }
 
     protected final void addEventListenerContainer(EventListenerContainer newListener) {
         EventListenerContainer currEventListenerContainer;
         for (int count = 0; count < eventListenerContainer.size(); count++) {
             currEventListenerContainer = eventListenerContainer.get(count);
             if (currEventListenerContainer.priority < newListener.priority) {
                 eventListenerContainer.add(count, newListener);
                 return;
             }
         }
         eventListenerContainer.add(newListener);
     }
 
     /**
      * Calls this event and asks all registered EventListeners and sends the
      * objects to them.
      * 
      * @param objects
      *            Object... - You can send any objects to the registered
      *            classes.
      * @return Event - to manage the called event such as getting the output and
      *         checking if the event was cancelled
      */
     public final Event callSync(Object... objects) {
         Event event = new Event(this, objects);
         event.sheduleEvent();
         return event;
     }
 
     /**
      * Calls this event in another thread that has to be started with
      * eventManagerInstance.asyncEventQueue.start(). It asks all registered
      * EventListeners and sends the objects to them.
      * 
      * @param objects
      *            Object... - You can send any objects to the registered
      *            classes.
      * @return Event - to manage the called event such as checking if the event
      *         is done, getting the output and checking if the event was
      *         cancelled
      */
     public final Event callAsync(Object... objects) throws AsyncEventQueueOverflowException {
         Event event = new Event(this, objects);
         eventManager.asyncEventQueue.add(event);
         return event;
     }
 
     /**
      * Used to be overridden in subclasses (use anonymous classes), to filter
      * out the EventListeners
      * 
      * @param event
      *            Event - the called event
      * @param eventListener
      *            Object - Is the instance of the EventListener class
      * @param filter
      *            Object[] - Is the given filter on registering an EventListener
      * @return boolean: true, if the EventListener should be called.
      */
     protected boolean applyFilter(Event event, Object eventListener, Object[] filter) {
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
 
     @Override
     public boolean equals(Object object) {
         if (object instanceof EventType) {
             if (object == this) return true;
         } else if (object instanceof Integer) {
             if (((Integer) object).equals(id)) return true;
         } else if (object instanceof String) {
             if (((String) object).equals(name)) return true;
         }
         return false;
     }
 }
