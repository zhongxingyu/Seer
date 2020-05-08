 package de.winkler.example.statemachine;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 
 public class StateMachine {
 
     private State start;
 
     public StateMachine(State start) {
         this.start = start;
     }
 
     public Collection<State> getStates() {
         List<State> result = new ArrayList<State>();
         gatherForwards(result, start);
         return result;
     }
 
     private void gatherForwards(Collection<State> result, State start) {
         if (start == null)
             return;
         if (result.contains(start))
             return;
         else {
             result.add(start);
             for (State next : start.getAllTargets()) {
                 gatherForwards(result, next);
             }
             return;
         }
     }
 
     private List<Event> resetEvents = new ArrayList<Event>();
 
     public void addResetEvents(Event... events) {
        for (Event e : events)
            resetEvents.add(e);
    }

    public boolean isResetEvent(String eventCode) {
        return resetEventCodes().contains(eventCode);
    }

    private List<String> resetEventCodes() {
        List<String> result = new ArrayList<String>();
        for (Event e : resetEvents)
            result.add(e.getCode());
        return result;
    }

    public State getStart() {
        // TODO Auto-generated method stub
        return null;
     }
 
 }
