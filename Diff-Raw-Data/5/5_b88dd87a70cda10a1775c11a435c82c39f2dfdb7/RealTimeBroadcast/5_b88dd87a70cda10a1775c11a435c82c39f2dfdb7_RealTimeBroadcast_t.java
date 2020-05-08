 package controllers;
 
 import iteratees.F;
 import iteratees.Iteratees;
 import models.Streams;
 import play.mvc.Controller;
 
 import static iteratees.F.*;
 import static iteratees.F.caseClassOf;
 import static iteratees.Iteratees.*;
 import static models.Streams.*;
 
 public class RealTimeBroadcast extends JIterateeController {
 
     public static void index(String role) {
         render(role);
     }
 
     public static final HubEnumerator<Event> hub = Enumerator.broadcast( Streams.events );
 
    private static final Enumeratee<Event, String> asJson = Enumeratee.map( new Function<Event, String>() {
         @Override
         public String apply(Event o) {
             for (SystemStatus status : caseClassOf(SystemStatus.class, o)) {
                 return "{\"type\":\"status\", \"message\":\"" + status.message + "\"}";
             }
             for (Operation operation : caseClassOf(Operation.class ,o)) {
                 return "{\"type\":\"operation\", \"amount\":" + operation.amount + ", \"visibility\":\"" + operation.level + "\"}";
             }
             return "";
         }
     });
 
     public static void feed(String role, int lowerBound, int higherBound) {
         Enumeratee<Event, Event> secure = getSecure(role);
         Enumeratee<Event, Event> inBounds = getInBounds(lowerBound, higherBound);
        eventSource(Enumerator.feed(Event.class, hub).through(secure).through(inBounds).through(asJson));
     }
 
     private static Enumeratee<Event, Event> getInBounds(final int lowerBoundValue, final int higherBoundValue) {
         return Enumeratee.collect(new Function<Event, Option<Event>>() {
             @Override
             public Option<Event> apply(Event o) {
                 for (SystemStatus status : caseClassOf(SystemStatus.class, o)) {
                     return Option.<Event>some(status);
                 }
                 for (Operation operation : caseClassOf(Operation.class, o)) {
                     if (operation.amount > lowerBoundValue && operation.amount < higherBoundValue) {
                         return Option.<Event>some(operation);
                     }
                 }
                 return Option.none();
             }
         });
     }
 
     private static Enumeratee<Event, Event> getSecure(final String roleValue) {
         return Enumeratee.collect(new Function<Event, Option<Event>>() {
             @Override
             public Option<Event> apply(Event o) {
                 for (SystemStatus status : caseClassOf(SystemStatus.class, o)) {
                     if (roleValue.equals("MANAGER")) {
                         return Option.<Event>some(status);
                     }
                 }
                 for (Operation operation : caseClassOf(Operation.class, o)) {
                     if (operation.level.equals("public")) {
                         return Option.<Event>some(operation);
                     } else {
                         if (roleValue.equals("MANAGER")) {
                             return Option.<Event>some(operation);
                         }
                     }
                 }
                 return Option.none();
             }
         });
     }
 }
