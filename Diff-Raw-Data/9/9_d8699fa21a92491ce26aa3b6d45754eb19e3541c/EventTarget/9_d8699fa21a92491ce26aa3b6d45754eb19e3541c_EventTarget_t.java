 package gx.realtime;
 
 import java.util.*;
 
 /**
  * An event target which can dispatch events to interested listeners.
  * Listeners subscribe via addEventListener.
  */
 public abstract class EventTarget
 {
     /**
      * All event handlers, indexed by event type.
      */
     protected Map<EventType, Set<EventHandler>> eventHandlers = new HashMap<>();
 
     /**
      * The parents of this eventTarget.
      */
     protected Set<EventTarget> parents = new HashSet();
 
     /**
      * Adds an event listener to the event target. The same handler can only
      * be added once per the type. Even if you add the same handler multiple
      * times using the same type then it will only be called once when the
      * event is dispatched.
      *
      * @param type    The type of the event to listen for.
      * @param handler The function to handle the event. The handler can also
      *                be an object that implements the handleEvent method which takes
      *                the event object as argument.
      */
     public <T extends Event> void addEventListener(EventType type, EventHandler<T> handler)
     {
         if (!eventHandlers.containsKey(type)) {
             eventHandlers.put(type, new HashSet<>());
         }
 
         eventHandlers.get(type).add(handler);
     }
 
     /**
      * Removes an event listener from the event target. The handler must be
      * the same object as the one added. If the handler has not been added
      * then nothing is done.
      *
      * @param type    The type of the event to listen for.
      * @param handler The function to handle the event. The handler can
      *                also be an object that implements the handleEvent method which
      *                takes the event object as argument.
      */
     public <T extends Event> void removeEventListener(EventType type, EventHandler<T> handler)
     {
         Set<EventHandler> handlers = eventHandlers.get(type);
         if (handlers != null) {
             handlers.remove(handler);
         }
     }
 
     /**
      * Dispatches the given event to this object. If the given event is an ObjectChangedEvent, the event will be unpacked, and the
      * appropriate eventHandlers are being executed, after which the event will be bubbled up to its parents if applicable.
      * If it is a BaseModelEvent, it will just be bubbled up.
      * Else, this function will just execute the eventHanders for the given event will be executed.
      *
      * @param event The event object, containing any necessary information.
      */
     protected void fireEvent(Event event)
     {
        // Check if the event targets this object
        if (!(event instanceof ObjectChangedEvent) && !this.equals(event.getTarget()))
            return;

         // Fire non-BaseModelEvents
         if (!(event instanceof BaseModelEvent)) {
             if (this.equals(event.getTarget()))
                 executeEventHandlers(event);
             return;
         }
 
         // Fire contained events of an ObjectChangedEvent first
         if (event instanceof ObjectChangedEvent && this.equals(event.getTarget())) {
             ObjectChangedEvent ocEvent = (ObjectChangedEvent) event;
             for (BaseModelEvent bmEvent : ocEvent.getEvents()) {
                 fireEvent(bmEvent);
             }
         }
 
         // Fire event
         BaseModelEvent bmEvent = (BaseModelEvent) event;
         if (bmEvent.isFirstVisit(this)) {
             bmEvent.addBubbledNode(this);
             executeEventHandlers(bmEvent);
             bubble(bmEvent);
         }
     }
 
     /**
      * This method bubbles the given event to its parents iff event.bubbles() == true.
      * @param event The event that should be bubbled.
      */
     private void bubble(BaseModelEvent event){
         if (event.bubbles()) {
             for (EventTarget parent : parents) {
                 parent.fireEvent(event);
             }
         }
     }
 
     /**
      * This method is a wrapper function for
      * @param event
      */
     private void executeEventHandlers(Event event)
     {
         //execute eventhandlers of this EventTarget if needed.
         Set<EventHandler> handlers = eventHandlers.get(event.getType());
         if (handlers != null) {
             for (EventHandler handler : handlers) {
                 handler.handleEvent(event);
             }
         }
     }
 
     /**
      * Add a parent of this EventTarget.
      *
      * @param parent The parent of this EventTarget.
      */
     public void addParent(EventTarget parent)
     {
         parents.add(parent);
     }
 
     /**
      * Remove the given EventTarget from the set of parents.
      *
      * @param parent The parent that should be removed.
      */
     public void removeParent(EventTarget parent)
     {
         parents.remove(parent);
     }
 }
