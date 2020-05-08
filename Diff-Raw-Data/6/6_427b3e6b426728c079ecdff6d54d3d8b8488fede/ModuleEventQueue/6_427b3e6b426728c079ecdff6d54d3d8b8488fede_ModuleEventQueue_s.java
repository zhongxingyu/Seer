 package ussr.model;
 
 import java.util.PriorityQueue;
 
 import ussr.physics.PhysicsObserver;
 import ussr.physics.PhysicsSimulation;
 
 public class ModuleEventQueue extends Thread implements PhysicsObserver {
 
     public abstract static class Event implements Comparable<Event> {
         private float time;
         public Event(float time) { this.time = time; }
         public float getTime() { return time; }
         public abstract void trigger();
         public int compareTo(Event other) {
             if(time<other.time) return -1;
             if(time>other.time) return 1;
             return 0;
         }
     }
 
     /**
      * The module on which we are executing events
      */
     private Module module;
     
     /**
      * The queue of events for this module
      */
     private PriorityQueue<Event> events = new PriorityQueue<Event>();
     
     /**
      * Object to use for signaling the availability of new events
      */
     private Object eventsAvailableSignal = new Object();
     
     /**
      * Create a new event queue for the given module 
      * @param module
      */
     public ModuleEventQueue(Module module) {
         this.module = module;
         this.start();
         module.getSimulation().subscribePhysicsTimestep(this);
     }
 
     @Override public void run() {
         while(true) {
             // Wait for next time at which events should be executed
             synchronized(eventsAvailableSignal) {
                 try {
                     eventsAvailableSignal.wait();
                 } catch (InterruptedException e) {
                     throw new Error("Unexpected interruption");
                 }
             }
             // Execute all events that should have occurred now or earlier in the simulation
             while(true) {
                 // Allow time to advance while we are triggering events 
                 float time = module.getSimulation().getTime();
                 // Get next event, if any
                 Event nextEvent;
                 synchronized(events) {
                     if(events.size()==0 || events.element().getTime()>time) break;
                     nextEvent = events.remove();
                 }
                 // Trigger the event
                 nextEvent.trigger();
             }
         }
     }
 
     // If any events are applicable, notify the event execution thread
     public void physicsTimeStepHook(PhysicsSimulation simulation) {
         float time = simulation.getTime();
        if(events.size()>0 && events.element().getTime()<time) synchronized(eventsAvailableSignal) { eventsAvailableSignal.notify(); }
     }
  
     /**
      * Add an event to the queue; will start to be processed immediately if event has time zero, otherwise
      * it will be inspected at every physics time step
      * @param event the event to add
      */
     public void addEvent(Event event) {
         synchronized(events) { events.add(event); }
         // Immediate event?
         if(event.getTime()==0) synchronized(eventsAvailableSignal) { eventsAvailableSignal.notify(); }
     }
     
 }
