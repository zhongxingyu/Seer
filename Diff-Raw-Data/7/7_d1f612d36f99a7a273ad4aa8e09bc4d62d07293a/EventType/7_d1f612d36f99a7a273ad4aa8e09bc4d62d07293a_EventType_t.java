 package com.dafttech.eventmanager;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import com.dafttech.eventmanager.exception.AsyncEventQueueOverflowException;
 import com.dafttech.eventmanager.exception.WrongEventManagerModeException;
 
 public class EventType {
     volatile protected List<EventListenerContainer> eventListenerContainer = new ArrayList<EventListenerContainer>();
     volatile protected EventManager eventManager = null;
     volatile protected String name = "";
     volatile protected int id = 0;
 
     public static final int PRIORITY_STANDARD = 0;
 
     public EventType(EventManager eventManager, String name) {
        this(eventManager);
         this.name = name;
     }
 
     public EventType(EventManager eventManager) {
         this.eventManager = eventManager;
         eventManager.events.add(this);
        this.id = eventManager.events.size();
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
     public final void registerEventListener(Object eventListener, Object... filter) {
         registerPrioritizedEventListener(eventListener, PRIORITY_STANDARD, filter);
     }
 
     public final void registerPrioritizedEventListener(Object eventListener, int priority, Object... filter) {
         if (eventManager.mode == EventManagerMode.ANNOTATION) throw new WrongEventManagerModeException();
         addEventListenerContainer(new EventListenerContainer(eventListener, priority, filter));
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
 
     public final void unregisterEventListener(Object eventListener) {
         eventListenerContainer.remove(eventListener);
     }
 
     /**
      * Calls this event and asks all registered Event Listeners for an Object.
      * 
      * @param Object
      *            : You can send any valid objects to the registered classes.
      * @return List < Object >: Every called class will return an object. They
      *         are collected in this list.
      */
     public final Event callSync(Object... objects) {
         Event event = new Event(this, objects);
         event.sheduleEvent();
         return event;
     }
 
     /**
      * Calls this event and asks all registered Event Listeners for an Object in
      * the background.
      * 
      * @param Object
      *            : You can send any valid objects to the registered classes.
      * @return EventStream: Every called class will return an object. They are
      *         collected in this list. If the data is collected, which you can
      *         enshure by using EventStream.isDone(), you can get your Object
      *         list with EventStream.getReturn()
      * @throws AsyncEventQueueOverflowException
      */
     public final Event callAsync(Object... objects) throws AsyncEventQueueOverflowException {
         Event event = new Event(this, objects);
         eventManager.asyncEventQueue.add(event);
         return event;
     }
 
     /**
      * Only used in the Event extending classes. It decides, if a called Event
      * belongs to an Event Listener.
      * 
      * @param filter
      * 
      * @param Event
      *            Listener: Event Listener to process.
      * @param Object
      *            : object given by the call method.
      * @return boolean: true, if the Event Listener should be called.
      */
     protected boolean applyFilter(Object eventlistener, Object[] filter, Object[] in) {
         return true;
     }
 
     @Override
     public boolean equals(Object object) {
         if (object instanceof EventType) {
             if (object == this) return true;
         } else if (object instanceof Integer) {
             if ((Integer) object == id) return true;
         } else if (object instanceof String) {
             if (((String) object).equals(name)) return true;
         }
         return false;
     }
 }
