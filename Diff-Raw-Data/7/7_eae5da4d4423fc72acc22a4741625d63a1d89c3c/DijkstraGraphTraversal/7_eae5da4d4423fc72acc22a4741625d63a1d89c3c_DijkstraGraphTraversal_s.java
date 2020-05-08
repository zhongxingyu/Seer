 package com.pathfinder.internal;
 
 import com.google.common.base.Function;
 import com.pathfinder.api.GraphTraversalService;
 import com.pathfinder.api.TransitEdge;
 import com.pathfinder.api.TransitPath;
 
 import java.rmi.RemoteException;
 import java.util.*;
 
 import static com.google.common.collect.Lists.transform;
 
 class DijkstraGraphTraversal implements GraphTraversalService {
 
     private class DateGenerator {
         private static final long ONE_MIN_MS = 1000 * 60;
         private static final long ONE_DAY_MS = ONE_MIN_MS * 60 * 24;
 
         private Date date = new Date();
 
         private Date nextDate(Date date) {
             return new Date(date.getTime() + ONE_DAY_MS);
         }
 
         Date next(){
             Date result = nextDate(date);
             date = result;
             return result;
         }
     }
 
 
     private static final Random random = new Random();
    private final DateGenerator generator = new DateGenerator();
 
     @Override
     public List<TransitPath> findShortestPath(String origin, String destination, Properties limitations) throws RemoteException {
         List<Leg> path = WorldMap.shortestPathBetween(origin, destination);
        List<TransitEdge> edges = transform(path, intoTransitEdge());
         return Collections.singletonList(new TransitPath(edges)); //for the moment, only one possibility is calculated.
     }
 
    private Function<Leg, TransitEdge> intoTransitEdge(){
         return new Function<Leg, TransitEdge>(){
             public TransitEdge apply(Leg leg) {
                 return new TransitEdge(
                         getVoyageNumber(),
                         leg.origin().unlocode,
                         leg.destination().unlocode,
                         generator.next(),
                         generator.next());
             }
         };
     }
 
     public String getVoyageNumber() {
         final int i = random.nextInt(5);
         if (i == 0) return "0100S";
         if (i == 1) return "0200T";
         if (i == 2) return "0300A";
         if (i == 3) return "0301S";
         return "0400S";
     }
 }
